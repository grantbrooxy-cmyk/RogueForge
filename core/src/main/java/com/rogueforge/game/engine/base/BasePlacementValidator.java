package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import com.rogueforge.game.engine.world.TmxWorldLoader;

/**
 * Reusable placement rules for structures inside expansive frontier zones.
 */
public class BasePlacementValidator {

    public BasePlacementResult validate(
        StructureDefinition definition,
        BaseState baseState,
        TmxWorldLoader.LoadedZone zone,
        FrontierTerrainSampler terrainSampler,
        String claimedSiteId,
        float worldX,
        float worldY
    ) {
        if (definition == null) {
            return BasePlacementResult.denied(PlacementFailureReason.INVALID_DEFINITION, "Unknown structure definition.", null);
        }
        if (baseState == null || zone == null || zone.id == null || !zone.id.equals(baseState.getZoneId())) {
            return BasePlacementResult.denied(PlacementFailureReason.INVALID_ZONE, "Base state does not match the current zone.", null);
        }
        if (claimedSiteId == null || claimedSiteId.isEmpty() || !baseState.hasClaimedSite(claimedSiteId)) {
            return BasePlacementResult.denied(PlacementFailureReason.MISSING_CLAIM, "You must claim an outpost site before building here.", null);
        }
        TmxWorldLoader.Feature site = findClaimedSite(zone, claimedSiteId);
        if (site == null || site.bounds == null) {
            return BasePlacementResult.denied(PlacementFailureReason.UNKNOWN_CLAIM_SITE, "The claimed outpost site could not be resolved.", null);
        }

        Rectangle bounds = new Rectangle(
            worldX,
            worldY,
            definition.getWidthTiles() * zone.tileWidth,
            definition.getHeightTiles() * zone.tileHeight
        );

        if (bounds.x < 0f || bounds.y < 0f || bounds.x + bounds.width > zone.pixelWidth || bounds.y + bounds.height > zone.pixelHeight) {
            return BasePlacementResult.denied(PlacementFailureReason.OUT_OF_BOUNDS, "Structure extends beyond the frontier bounds.", bounds);
        }
        if (!site.bounds.contains(bounds.x, bounds.y) || !site.bounds.contains(bounds.x + bounds.width, bounds.y + bounds.height)) {
            return BasePlacementResult.denied(PlacementFailureReason.MISSING_CLAIM, "Structure must stay inside the claimed outpost site.", bounds);
        }

        Vector2 center = new Vector2(bounds.x + bounds.width * 0.5f, bounds.y + bounds.height * 0.5f);
        if (zone.isSafeAt(center)) {
            return BasePlacementResult.denied(
                PlacementFailureReason.TOO_CLOSE_TO_STARTER_SAFE_ZONE,
                "Player-built structures must stay outside the starter safe zone.",
                bounds
            );
        }

        if (terrainSampler != null) {
            FrontierTerrainSampler.TerrainType terrainType = terrainSampler.sampleWorld(center.x, center.y, zone.tileWidth, zone.tileHeight).type;
            if (!definition.allowsTerrain(terrainType)) {
                return BasePlacementResult.denied(PlacementFailureReason.INVALID_TERRAIN, "That terrain is not suitable for this structure.", bounds);
            }
        }

        for (Rectangle collision : zone.collisions) {
            if (bounds.overlaps(collision)) {
                return BasePlacementResult.denied(PlacementFailureReason.BLOCKED_BY_WORLD, "Structure overlaps world collision.", bounds);
            }
        }
        for (TmxWorldLoader.Feature feature : zone.features) {
            if (feature == site || feature.bounds == null || !feature.blocksMovement) {
                continue;
            }
            if (bounds.overlaps(feature.bounds)) {
                return BasePlacementResult.denied(PlacementFailureReason.BLOCKED_BY_WORLD, "Structure overlaps a blocked world feature.", bounds);
            }
        }
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure.isActive() && bounds.overlaps(structure.getBounds())) {
                return BasePlacementResult.denied(PlacementFailureReason.OVERLAPS_STRUCTURE, "Structure overlaps an existing base structure.", bounds);
            }
        }

        return BasePlacementResult.allowed(bounds);
    }

    private TmxWorldLoader.Feature findClaimedSite(TmxWorldLoader.LoadedZone zone, String claimedSiteId) {
        for (TmxWorldLoader.Feature feature : zone.features) {
            if ("claim_outpost_site".equals(feature.interactionType) && claimedSiteId.equals(feature.persistentStateId)) {
                return feature;
            }
        }
        return null;
    }
}
