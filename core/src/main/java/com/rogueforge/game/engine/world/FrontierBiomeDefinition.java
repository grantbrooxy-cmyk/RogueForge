package com.rogueforge.game.engine.world;

import com.badlogic.gdx.graphics.Color;

/**
 * Data definition for a seeded frontier biome. This maps terrain classes to
 * asset folders and shared presentation data so future projects can swap art
 * packs without rewriting generation rules.
 */
public class FrontierBiomeDefinition {
    private final String id;
    private final String displayName;
    private final FrontierTerrainSampler.TerrainType terrainType;
    private final String groundAssetFolder;
    private final String objectAssetFolder;
    private final Color primaryTint;
    private final Color secondaryTint;
    private final Color moistureTint;
    private final Color ruggedTint;

    public FrontierBiomeDefinition(
        String id,
        String displayName,
        FrontierTerrainSampler.TerrainType terrainType,
        String groundAssetFolder,
        String objectAssetFolder,
        Color primaryTint,
        Color secondaryTint,
        Color moistureTint,
        Color ruggedTint
    ) {
        this.id = id;
        this.displayName = displayName;
        this.terrainType = terrainType;
        this.groundAssetFolder = groundAssetFolder;
        this.objectAssetFolder = objectAssetFolder;
        this.primaryTint = new Color(primaryTint);
        this.secondaryTint = new Color(secondaryTint);
        this.moistureTint = new Color(moistureTint);
        this.ruggedTint = new Color(ruggedTint);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public FrontierTerrainSampler.TerrainType getTerrainType() {
        return terrainType;
    }

    public String getGroundAssetFolder() {
        return groundAssetFolder;
    }

    public String getObjectAssetFolder() {
        return objectAssetFolder;
    }

    public Color getPrimaryTint() {
        return new Color(primaryTint);
    }

    public Color getSecondaryTint() {
        return new Color(secondaryTint);
    }

    public Color getMoistureTint() {
        return new Color(moistureTint);
    }

    public Color getRuggedTint() {
        return new Color(ruggedTint);
    }
}
