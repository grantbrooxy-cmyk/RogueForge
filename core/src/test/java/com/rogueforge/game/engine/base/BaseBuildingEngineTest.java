package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;
import com.rogueforge.game.engine.social.OwnershipRecord;
import com.rogueforge.game.engine.social.OwnershipScope;
import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseBuildingEngineTest {
    private static final Rectangle SITE_BOUNDS = new Rectangle(620f, 620f, 384f, 384f);

    @Test
    void placesStructureInsideClaimedSiteWhenTerrainAndSpaceAreValid() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        baseState.claimSite("site_alpha");
        TmxWorldLoader.LoadedZone zone = buildZone();
        FrontierTerrainSampler sampler = new FrontierTerrainSampler(424242L);

        float[] meadowSpot = findValidPlacement(engine, "field_fabricator", baseState, zone, sampler, SITE_BOUNDS);
        assertNotNull(meadowSpot);

        PlacedStructure structure = engine.placeStructure(
            "field_fabricator",
            baseState,
            zone,
            sampler,
            "site_alpha",
            meadowSpot[0],
            meadowSpot[1]
        );

        assertNotNull(structure);
        assertEquals("field_fabricator", structure.getStructureDefinitionId());
        assertEquals(1, baseState.getPlacedStructures().size());
    }

    @Test
    void placementFailsWithoutClaimedSite() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        TmxWorldLoader.LoadedZone zone = buildZone();

        BasePlacementResult result = engine.validatePlacement(
            "palisade_wall",
            baseState,
            zone,
            new FrontierTerrainSampler(424242L),
            "site_alpha",
            640f,
            640f
        );

        assertFalse(result.isAllowed());
        assertEquals(PlacementFailureReason.MISSING_CLAIM, result.getFailureReason());
    }

    @Test
    void placementFailsWhenOverlappingExistingStructure() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        baseState.claimSite("site_alpha");
        TmxWorldLoader.LoadedZone zone = buildZone();
        FrontierTerrainSampler sampler = new FrontierTerrainSampler(424242L);

        float[] scrubSpot = findValidPlacement(engine, "palisade_wall", baseState, zone, sampler, SITE_BOUNDS);
        assertNotNull(scrubSpot);

        PlacedStructure first = engine.placeStructure(
            "palisade_wall",
            baseState,
            zone,
            sampler,
            "site_alpha",
            scrubSpot[0],
            scrubSpot[1]
        );
        assertNotNull(first);

        BasePlacementResult result = engine.validatePlacement(
            "palisade_wall",
            baseState,
            zone,
            sampler,
            "site_alpha",
            scrubSpot[0],
            scrubSpot[1]
        );

        assertFalse(result.isAllowed());
        assertEquals(PlacementFailureReason.OVERLAPS_STRUCTURE, result.getFailureReason());
    }

    @Test
    void defenderAssignmentsRespectStructureCapacityAndCleanupOnRemoval() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        baseState.claimSite("site_alpha");
        TmxWorldLoader.LoadedZone zone = buildZone();
        FrontierTerrainSampler sampler = new FrontierTerrainSampler(424242L);

        float[] buildSpot = findValidPlacement(engine, "sentry_post", baseState, zone, sampler, SITE_BOUNDS);
        assertNotNull(buildSpot);

        PlacedStructure post = engine.placeStructure(
            "sentry_post",
            baseState,
            zone,
            sampler,
            "site_alpha",
            buildSpot[0],
            buildSpot[1]
        );
        assertNotNull(post);

        assertTrue(engine.assignDefender(baseState, post.getInstanceId(), "scout_mk1", DefenderRole.GUARD));
        assertTrue(engine.assignDefender(baseState, post.getInstanceId(), "guardian_mk1", DefenderRole.PATROL));
        assertFalse(engine.assignDefender(baseState, post.getInstanceId(), "striker_mk1", DefenderRole.GUARD));

        assertEquals(2, baseState.getDefenderAssignments().size());
        assertTrue(engine.removeStructure(baseState, post.getInstanceId()));
        assertTrue(baseState.getDefenderAssignments().isEmpty());
        assertNull(baseState.findStructure(post.getInstanceId()));
    }

    @Test
    void structureDamageAndRepairUpdateHitPoints() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        baseState.claimSite("site_alpha");
        TmxWorldLoader.LoadedZone zone = buildZone();
        FrontierTerrainSampler sampler = new FrontierTerrainSampler(424242L);

        float[] buildSpot = findValidPlacement(engine, "sentry_post", baseState, zone, sampler, SITE_BOUNDS);
        assertNotNull(buildSpot);

        PlacedStructure post = engine.placeStructure(
            "sentry_post",
            baseState,
            zone,
            sampler,
            "site_alpha",
            buildSpot[0],
            buildSpot[1]
        );
        assertNotNull(post);

        assertTrue(engine.applyStructureDamage(baseState, post.getInstanceId(), 80));
        assertEquals(140, post.getCurrentHitPoints());

        StructureDefinition definition = engine.getStructureRegistry().get("sentry_post");
        assertEquals(40, engine.repairStructure(post, definition, 40));
        assertEquals(180, post.getCurrentHitPoints());
    }

    @Test
    void claimedSiteAndStructureOwnershipPersistInsideBaseState() {
        BaseBuildingEngine engine = new BaseBuildingEngine();
        BaseState baseState = new BaseState("verdant_fields");
        OwnershipRecord owner = new OwnershipRecord(OwnershipScope.PERSONAL, "Grant", "", "", false, Set.of());
        baseState.claimSite("site_alpha", owner);
        TmxWorldLoader.LoadedZone zone = buildZone();
        FrontierTerrainSampler sampler = new FrontierTerrainSampler(424242L);

        float[] buildSpot = findValidPlacement(engine, "supply_crate", baseState, zone, sampler, SITE_BOUNDS);
        assertNotNull(buildSpot);

        PlacedStructure crate = engine.placeStructure(
            "supply_crate",
            baseState,
            zone,
            sampler,
            "site_alpha",
            buildSpot[0],
            buildSpot[1]
        );

        assertNotNull(crate);
        baseState.setStructureOwnership(crate.getInstanceId(), owner);
        assertEquals("Grant", baseState.getClaimedSiteOwnership("site_alpha").getOwnerPlayerId());
        assertEquals("Grant", baseState.getStructureOwnership(crate.getInstanceId()).getOwnerPlayerId());
    }

    private TmxWorldLoader.LoadedZone buildZone() {
        TmxWorldLoader.LoadedZone zone = new TmxWorldLoader.LoadedZone();
        zone.id = "verdant_fields";
        zone.tileWidth = 48;
        zone.tileHeight = 48;
        zone.mapWidthTiles = 64;
        zone.mapHeightTiles = 64;
        zone.pixelWidth = zone.mapWidthTiles * zone.tileWidth;
        zone.pixelHeight = zone.mapHeightTiles * zone.tileHeight;
        zone.safeCenter = new com.badlogic.gdx.math.Vector2(240f, 240f);
        zone.safeRadius = 180f;

        TmxWorldLoader.Feature site = new TmxWorldLoader.Feature();
        site.id = "frontier_base_site_0";
        site.persistentStateId = "site_alpha";
        site.interactionType = "claim_outpost_site";
        site.bounds = new Rectangle(SITE_BOUNDS);
        zone.features.add(site);

        return zone;
    }

    private float[] findValidPlacement(
        BaseBuildingEngine engine,
        String structureId,
        BaseState baseState,
        TmxWorldLoader.LoadedZone zone,
        FrontierTerrainSampler sampler,
        Rectangle siteBounds
    ) {
        for (float y = siteBounds.y; y <= siteBounds.y + siteBounds.height - zone.tileHeight; y += zone.tileHeight) {
            for (float x = siteBounds.x; x <= siteBounds.x + siteBounds.width - zone.tileWidth; x += zone.tileWidth) {
                BasePlacementResult result = engine.validatePlacement(
                    structureId,
                    baseState,
                    zone,
                    sampler,
                    "site_alpha",
                    x,
                    y
                );
                if (result.isAllowed()) {
                    return new float[] {x, y};
                }
            }
        }
        return null;
    }
}
