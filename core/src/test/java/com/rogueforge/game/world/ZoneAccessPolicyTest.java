package com.rogueforge.game.world;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZoneAccessPolicyTest {

    @Test
    void shadowCavesRequireWorkshopPassAndCombatReadiness() {
        ZoneAccessPolicy.AccessDecision noPass = ZoneAccessPolicy.evaluate(
            "shadow_caves",
            25,
            "E",
            1,
            Map.of(),
            Map.of()
        );
        assertFalse(noPass.isAllowed());

        ZoneAccessPolicy.AccessDecision underleveled = ZoneAccessPolicy.evaluate(
            "shadow_caves",
            15,
            "F",
            1,
            Map.of("access.workshop_pass", true),
            Map.of()
        );
        assertFalse(underleveled.isAllowed());

        ZoneAccessPolicy.AccessDecision ready = ZoneAccessPolicy.evaluate(
            "shadow_caves",
            22,
            "E",
            1,
            Map.of("access.workshop_pass", true),
            Map.of()
        );
        assertTrue(ready.isAllowed());
    }

    @Test
    void actThreeAndFourZonesStayLockedUntilStoryRoutesOpen() {
        assertFalse(ZoneAccessPolicy.evaluate(
            "sunken_abyss",
            55,
            "B",
            3,
            Map.of(),
            Map.of()
        ).isAllowed());

        assertTrue(ZoneAccessPolicy.evaluate(
            "sunken_abyss",
            55,
            "B",
            3,
            Map.of("frontier.abyss_signal_found", true),
            Map.of()
        ).isAllowed());

        assertFalse(ZoneAccessPolicy.evaluate(
            "sky_fortress",
            60,
            "A",
            3,
            Map.of(),
            Map.of()
        ).isAllowed());

        assertTrue(ZoneAccessPolicy.evaluate(
            "sky_fortress",
            60,
            "A",
            3,
            Map.of("frontier.sky_route_mapped", true),
            Map.of()
        ).isAllowed());

        assertFalse(ZoneAccessPolicy.evaluate(
            "the_void",
            80,
            "S",
            4,
            Map.of("frontier.command_rails_online", true),
            Map.of()
        ).isAllowed());

        assertTrue(ZoneAccessPolicy.evaluate(
            "the_void",
            80,
            "S",
            4,
            Map.of("frontier.void_gate_stable", true),
            Map.of()
        ).isAllowed());
    }

    @Test
    void infiniteDungeonRequiresLateGameProgression() {
        assertFalse(ZoneAccessPolicy.evaluate(
            "infinite_dungeon",
            70,
            "S",
            3,
            Map.of(),
            Map.of()
        ).isAllowed());

        assertFalse(ZoneAccessPolicy.evaluate(
            "infinite_dungeon",
            45,
            "C",
            4,
            Map.of(),
            Map.of()
        ).isAllowed());

        assertTrue(ZoneAccessPolicy.evaluate(
            "infinite_dungeon",
            60,
            "A",
            4,
            Map.of(),
            Map.of()
        ).isAllowed());
    }
}
