package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseDefenseDirectorTest {

    @Test
    void synchronizeCreatesAnchoredDefendersAndPreservesRuntimeState() {
        BaseDefenseDirector director = new BaseDefenseDirector();
        StructureDefinitionRegistry registry = new StructureDefinitionRegistry();
        BaseState baseState = new BaseState("verdant_fields");
        baseState.claimSite("site_alpha");

        PlacedStructure post = new PlacedStructure(
            "sentry_post_1",
            "sentry_post",
            "verdant_fields",
            "site_alpha",
            new Rectangle(720f, 720f, 96f, 96f),
            220
        );
        baseState.addPlacedStructure(post);
        baseState.addDefenderAssignment(new DefenderAssignment("sentry_post_1", "scout_mk1", DefenderRole.GUARD));
        baseState.addDefenderAssignment(new DefenderAssignment("sentry_post_1", "guardian_mk1", DefenderRole.PATROL));

        BaseDefenderUnit existing = new BaseDefenderUnit(
            "sentry_post_1::guardian_mk1",
            "guardian_mk1",
            "Guardian",
            "sentry_post_1",
            DefenderRole.PATROL,
            new Vector2(860f, 780f),
            new Vector2(768f, 854f),
            new Vector2(898f, 768f),
            120f,
            77f,
            20f,
            10f,
            120f,
            130f,
            220f,
            0.8f,
            0.35f,
            1.4f,
            true
        );

        List<BaseDefenderUnit> units = director.synchronize(
            baseState,
            registry,
            Map.of(
                "scout_mk1", new BaseDefenderProfile("scout_mk1", "Scout", 90f, 18f, 9f, 140f, 120f, 220f, 0.7f),
                "guardian_mk1", new BaseDefenderProfile("guardian_mk1", "Guardian", 120f, 22f, 14f, 110f, 100f, 210f, 0.9f)
            ),
            List.of(existing)
        );

        assertEquals(2, units.size());
        BaseDefenderUnit guard = units.stream().filter(unit -> "scout_mk1".equals(unit.getRobotId())).findFirst().orElseThrow();
        BaseDefenderUnit patrol = units.stream().filter(unit -> "guardian_mk1".equals(unit.getRobotId())).findFirst().orElseThrow();

        assertEquals("sentry_post_1", guard.getStructureInstanceId());
        assertTrue(guard.getGuardPosition().dst(post.getBounds().x + post.getBounds().width / 2f, post.getBounds().y + post.getBounds().height / 2f) > 40f);
        assertEquals(77f, patrol.getCurrentHealth(), 0.001f);
        assertEquals(0.35f, patrol.getAttackTimer(), 0.001f);
        assertEquals(1.4f, patrol.getAnimationTime(), 0.001f);
        assertEquals(860f, patrol.getPosition().x, 0.001f);
        assertNotEquals(patrol.getGuardPosition(), patrol.getPatrolPosition());
    }
}
