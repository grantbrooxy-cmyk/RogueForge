package com.rogueforge.game.engine.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FrontierLandmarkPrefabDefinition {
    private final String id;
    private final String label;
    private final String category;
    private final float widthTiles;
    private final float heightTiles;
    private final List<FrontierTerrainSampler.TerrainType> allowedTerrainTypes;
    private final String objectAssetFolder;
    private final String interactionMessage;

    public FrontierLandmarkPrefabDefinition(
        String id,
        String label,
        String category,
        float widthTiles,
        float heightTiles,
        String objectAssetFolder,
        String interactionMessage,
        FrontierTerrainSampler.TerrainType... allowedTerrainTypes
    ) {
        this.id = id;
        this.label = label;
        this.category = category;
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.objectAssetFolder = objectAssetFolder;
        this.interactionMessage = interactionMessage;
        this.allowedTerrainTypes = allowedTerrainTypes == null || allowedTerrainTypes.length == 0
            ? Collections.emptyList()
            : Collections.unmodifiableList(Arrays.asList(allowedTerrainTypes));
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public float getWidthTiles() {
        return widthTiles;
    }

    public float getHeightTiles() {
        return heightTiles;
    }

    public List<FrontierTerrainSampler.TerrainType> getAllowedTerrainTypes() {
        return allowedTerrainTypes;
    }

    public String getObjectAssetFolder() {
        return objectAssetFolder;
    }

    public String getInteractionMessage() {
        return interactionMessage;
    }

    public boolean allows(FrontierTerrainSampler.TerrainType terrainType) {
        return terrainType != null && (allowedTerrainTypes.isEmpty() || allowedTerrainTypes.contains(terrainType));
    }
}
