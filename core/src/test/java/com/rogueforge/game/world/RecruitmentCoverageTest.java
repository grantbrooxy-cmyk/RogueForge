package com.rogueforge.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.robot.RobotDefinition;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentCoverageTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void recruitmentCatalogAndWorldDiscoveriesCoverTenRobots() {
        RecruitmentDefinition[] recruitments = new Json().fromJson(
            RecruitmentDefinition[].class,
            Gdx.files.internal("data/recruitment.json").readString()
        );
        RobotDefinition[] robots = new Json().fromJson(
            RobotDefinition[].class,
            Gdx.files.internal("data/robots.json").readString()
        );
        ZoneDefinition[] zones = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );

        assertNotNull(recruitments);
        assertNotNull(robots);
        assertNotNull(zones);
        assertEquals(10, recruitments.length, "Expected ten discoverable recruitment events.");

        Set<String> robotIds = Arrays.stream(robots).map(RobotDefinition::getId).collect(Collectors.toSet());
        Set<String> eventIds = new HashSet<>();
        Set<String> recruitedRobotIds = new HashSet<>();
        Set<String> chestRecruitEvents = new HashSet<>();
        TmxWorldLoader loader = new TmxWorldLoader();

        for (RecruitmentDefinition recruitment : recruitments) {
            assertTrue(eventIds.add(recruitment.getEventId()), "Duplicate recruitment event: " + recruitment.getEventId());
            assertTrue(
                recruitedRobotIds.add(recruitment.getRobotId()),
                "Each recruitment event should point at a unique robot: " + recruitment.getRobotId()
            );
            assertTrue(robotIds.contains(recruitment.getRobotId()), "Unknown robot id in recruitment data: " + recruitment.getRobotId());
            assertNotNull(recruitment.getJoinedWorldFlag(), "Recruitment should define a world flag.");
        }

        for (ZoneDefinition zone : zones) {
            TmxWorldLoader.LoadedZone loadedZone = loader.load(zone);
            for (TmxWorldLoader.ChestData chest : loadedZone.chests) {
                if (chest.recruitEventId != null && !chest.recruitEventId.isEmpty()) {
                    chestRecruitEvents.add(chest.recruitEventId);
                }
            }
        }

        assertTrue(
            chestRecruitEvents.contains("scout_mk2_join"),
            "Whispering Forest should expose the Mushroom Grotto robot discovery."
        );
        assertTrue(chestRecruitEvents.size() >= 8, "Expected at least eight chest-driven robot discoveries in the world.");
    }
}
