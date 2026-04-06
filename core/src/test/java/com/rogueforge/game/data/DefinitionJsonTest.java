package com.rogueforge.game.data;

import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.Element;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.robot.RobotDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefinitionJsonTest {

    @Test
    void abilityDefinitionsReadEnumsCaseInsensitively() {
        Json json = DefinitionJson.newJson();

        AbilityDefinition definition = json.fromJson(
            AbilityDefinition.class,
            "{"
                + "\"id\":\"arc_pulse\","
                + "\"name\":\"Arc Pulse\","
                + "\"type\":\"damage\","
                + "\"targetType\":\"single_enemy\","
                + "\"cooldown\":4,"
                + "\"power\":18,"
                + "\"duration\":0,"
                + "\"description\":\"Pulse attack\","
                + "\"element\":\"lightning\","
                + "\"weaponType\":\"sword\""
                + "}"
        );

        assertEquals(AbilityDefinition.AbilityType.DAMAGE, definition.getType());
        assertEquals(AbilityDefinition.TargetType.SINGLE_ENEMY, definition.getTargetType());
        assertEquals(Element.LIGHTNING, definition.getElement());
        assertEquals(WeaponType.SWORD, definition.getWeaponType());
    }

    @Test
    void robotDefinitionsGetSafeCollectionDefaults() {
        Json json = DefinitionJson.newJson();

        RobotDefinition definition = json.fromJson(
            RobotDefinition.class,
            "{"
                + "\"id\":\"rust\","
                + "\"name\":\"Rust\","
                + "\"role\":\"support\","
                + "\"baseHp\":80,"
                + "\"baseAttack\":10,"
                + "\"baseDefense\":8,"
                + "\"baseSpeed\":14"
                + "}"
        );

        assertNotNull(definition.getEquipmentSlots());
        assertNotNull(definition.getAbilityIds());
        assertEquals(0, definition.getEquipmentSlots().length);
        assertEquals(0, definition.getAbilityIds().size());
        assertEquals(RobotDefinition.RoleEnum.SUPPORT, definition.getRole());
    }
}
