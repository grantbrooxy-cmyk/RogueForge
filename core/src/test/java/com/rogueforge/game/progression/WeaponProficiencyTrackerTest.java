package com.rogueforge.game.progression;

import com.rogueforge.game.combat.WeaponType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponProficiencyTrackerTest {

    @Test
    void getOrCreateReturnsStateForRealWeaponFamilies() {
        RobotProgressionState robot = new RobotProgressionState("rust_mk1", "Rust");

        WeaponProficiencyState state = WeaponProficiencyTracker.getOrCreate(robot, WeaponType.SWORD);

        assertEquals("SWORD", state.getWeaponType());
        assertEquals(state, robot.getWeaponProficiencies().get("SWORD"));
        assertNull(WeaponProficiencyTracker.getOrCreate(robot, WeaponType.NONE));
    }

    @Test
    void gainXpUnlocksMilestonesAcrossMultipleLevels() {
        WeaponProficiencyState state = new WeaponProficiencyState("SWORD");

        List<String> unlocked = WeaponProficiencyTracker.gainXp(state, WeaponType.SWORD, 200);

        assertEquals(4, state.getLevel());
        assertEquals(17, state.getXp());
        assertEquals(List.of("sword_cleave"), unlocked);
        assertTrue(state.getUnlockedMilestones().contains("sword_cleave"));
    }

    @Test
    void gainXpUnlocksHigherTierMilestonesWithoutDuplicates() {
        WeaponProficiencyState state = new WeaponProficiencyState("SWORD");
        state.setLevel(5);
        state.setXp(0);

        List<String> tierTwo = WeaponProficiencyTracker.gainXp(state, WeaponType.SWORD, 133);
        List<String> tierTwoDuplicate = WeaponProficiencyTracker.gainXp(state, WeaponType.SWORD, 0);
        List<String> tierThree = WeaponProficiencyTracker.gainXp(state, WeaponType.SWORD, 640);

        assertEquals(List.of("sword_guard_break"), tierTwo);
        assertTrue(tierTwoDuplicate.isEmpty());
        assertEquals(List.of("sword_last_rites"), tierThree);
        assertEquals(10, state.getLevel());
    }

    @Test
    void utilityMethodsExposeExpectedCombatScaling() {
        assertEquals(10, WeaponProficiencyTracker.xpForAttack());
        assertEquals("sword_cleave", WeaponProficiencyTracker.unlockArtId(WeaponType.SWORD, 3));
        assertEquals("Combat Art II", WeaponProficiencyTracker.unlockLabel(6));
        assertEquals(1.0f, WeaponProficiencyTracker.damageMultiplier(1));
        assertEquals(1.27f, WeaponProficiencyTracker.damageMultiplier(10));
    }
}
