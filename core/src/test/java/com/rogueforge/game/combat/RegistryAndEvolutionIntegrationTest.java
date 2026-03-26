package com.rogueforge.game.combat;

import com.rogueforge.game.progression.AbilityEvolutionManager;
import com.rogueforge.game.progression.AbilityProgressionState;
import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.support.GdxTestSupport;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryAndEvolutionIntegrationTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @BeforeEach
    void clearRegistry() {
        AbilityRegistry.clear();
    }

    @Test
    void abilityRegistryLoadsRealDefinitionsAndCreatesProgressedInstances() {
        Map<String, AbilityProgressionState> progressionStates = new HashMap<>();

        AbilityDefinition powerStrike = AbilityRegistry.get("power_strike");
        assertNotNull(powerStrike);
        assertEquals(WeaponType.SWORD, powerStrike.getWeaponType());
        assertTrue(AbilityRegistry.getCount() > 0);

        List<AbilityInstance> instances = AbilityRegistry.createInstances(List.of("power_strike", "heal_pulse"), progressionStates);

        assertEquals(2, instances.size());
        assertEquals(2, progressionStates.size());
        assertTrue(AbilityRegistry.getAllIds().contains("sword_cleave"));
    }

    @Test
    void masteryUnlocksPromoteAbilityAndPreserveProgressionState() {
        RobotProgressionState robot = new RobotProgressionState("rust_mk1", "Rust");
        AbilityProgressionState state = new AbilityProgressionState("dash");
        state.setProficiencyLevel(10);
        robot.setKnownAbilityIds(new ArrayList<>(List.of("dash")));
        robot.getAbilityProgression().put("dash", state);

        Map<String, RobotProgressionState> party = new HashMap<>();
        party.put(robot.getRobotId(), robot);

        List<String> messages = AbilityEvolutionManager.applyMasteryUnlocks(robot, party);

        assertTrue(robot.getKnownAbilityIds().contains("dash_prime"));
        assertFalse(robot.getAbilityProgression().containsKey("dash"));
        assertTrue(messages.stream().anyMatch(line -> line.contains("mastered Dash")));
    }

    @Test
    void uniqueSkillUnlocksWhenNoOtherPartyMemberHasIt() throws Exception {
        forceRegistryInitializedForManualRegistration();

        AbilityDefinition deepScan = new AbilityDefinition();
        deepScan.setId("deep_scan");
        deepScan.setName("Deep Scan");
        deepScan.setUniqueSkillId("omniscience_eye");
        AbilityRegistry.register(deepScan);

        AbilityDefinition omniscience = new AbilityDefinition();
        omniscience.setId("omniscience_eye");
        omniscience.setName("Omniscience Eye");
        AbilityRegistry.register(omniscience);

        RobotProgressionState robot = new RobotProgressionState("ivy_mk1", "Ivy");
        AbilityProgressionState state = new AbilityProgressionState("deep_scan");
        state.setProficiencyLevel(10);
        robot.setKnownAbilityIds(new ArrayList<>(List.of("deep_scan")));
        robot.getAbilityProgression().put("deep_scan", state);

        Map<String, RobotProgressionState> party = new HashMap<>();
        party.put(robot.getRobotId(), robot);

        List<String> messages = AbilityEvolutionManager.applyMasteryUnlocks(robot, party);

        assertTrue(robot.getKnownAbilityIds().contains("omniscience_eye"));
        assertTrue(messages.stream().anyMatch(line -> line.contains("Omniscience Eye")));
    }

    @Test
    void uniqueSkillIsNotDuplicatedAcrossParty() {
        RobotProgressionState first = new RobotProgressionState("rust_mk1", "Rust");
        first.setKnownAbilityIds(new ArrayList<>(List.of("phantom_drive")));

        RobotProgressionState second = new RobotProgressionState("ivy_mk1", "Ivy");
        AbilityProgressionState secondState = new AbilityProgressionState("dash");
        secondState.setProficiencyLevel(10);
        second.setKnownAbilityIds(new ArrayList<>(List.of("dash")));
        second.getAbilityProgression().put("dash", secondState);

        Map<String, RobotProgressionState> party = new HashMap<>();
        party.put(first.getRobotId(), first);
        party.put(second.getRobotId(), second);

        List<String> messages = AbilityEvolutionManager.applyMasteryUnlocks(second, party);

        assertTrue(second.getKnownAbilityIds().contains("dash_prime"));
        assertFalse(second.getKnownAbilityIds().contains("phantom_drive"));
        assertEquals(1, messages.size());
    }

    private static void forceRegistryInitializedForManualRegistration() throws Exception {
        Field initialized = AbilityRegistry.class.getDeclaredField("initialized");
        initialized.setAccessible(true);
        initialized.setBoolean(null, true);
    }
}
