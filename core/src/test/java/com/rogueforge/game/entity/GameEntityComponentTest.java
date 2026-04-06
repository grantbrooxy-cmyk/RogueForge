package com.rogueforge.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.entity.component.PositionComponent;
import com.rogueforge.game.entity.component.RobotCoreComponent;
import com.rogueforge.game.entity.component.StatsComponent;
import com.rogueforge.game.robot.RobotDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEntityComponentTest {

    @Test
    void playerEntityExposesSharedPositionAndStatsComponents() {
        PlayerEntity player = new PlayerEntity(new Vector2(10f, 20f));

        PositionComponent position = player.position();
        StatsComponent stats = player.stats();

        assertNotNull(position);
        assertNotNull(stats);
        assertEquals(10f, position.position.x);
        assertEquals(20f, position.position.y);
        assertEquals(100f, stats.maxHealth);
        assertTrue(player.hasComponent(PositionComponent.class));
        assertTrue(player.hasComponent(StatsComponent.class));
    }

    @Test
    void robotEntityRegistersRobotCoreComponent() {
        RobotDefinition definition = new RobotDefinition(
            "bolt",
            "Bolt",
            RobotDefinition.RoleEnum.SUPPORT,
            80,
            12,
            10,
            16,
            new RobotDefinition.EquipmentSlot[0],
            List.of()
        );
        RobotEntity robot = new RobotEntity(definition, new Vector2(5f, 6f));

        RobotCoreComponent robotCore = robot.robotCore();

        assertNotNull(robotCore);
        assertEquals("bolt", robotCore.robotId);
        assertEquals("Bolt", robotCore.robotName);
        assertEquals("SUPPORT", robotCore.role);
        assertTrue(robot.hasComponent(RobotCoreComponent.class));
    }
}
