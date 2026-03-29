package com.rogueforge.game.engine.base;

import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reusable buildable structure definition with footprint, terrain, and role data.
 */
public class StructureDefinition {
    private final String id;
    private final String displayName;
    private final StructureCategory category;
    private final int widthTiles;
    private final int heightTiles;
    private final boolean blocksMovement;
    private final int maxHitPoints;
    private final int storageCapacity;
    private final int defenderCapacity;
    private final Map<String, Integer> buildCosts;
    private final Set<FrontierTerrainSampler.TerrainType> allowedTerrainTypes;

    public StructureDefinition(
        String id,
        String displayName,
        StructureCategory category,
        int widthTiles,
        int heightTiles,
        boolean blocksMovement,
        int maxHitPoints,
        int storageCapacity,
        int defenderCapacity,
        Map<String, Integer> buildCosts,
        Set<FrontierTerrainSampler.TerrainType> allowedTerrainTypes
    ) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.widthTiles = Math.max(1, widthTiles);
        this.heightTiles = Math.max(1, heightTiles);
        this.blocksMovement = blocksMovement;
        this.maxHitPoints = Math.max(1, maxHitPoints);
        this.storageCapacity = Math.max(0, storageCapacity);
        this.defenderCapacity = Math.max(0, defenderCapacity);
        this.buildCosts = buildCosts != null ? Collections.unmodifiableMap(new HashMap<>(buildCosts)) : Collections.emptyMap();
        this.allowedTerrainTypes = allowedTerrainTypes != null && !allowedTerrainTypes.isEmpty()
            ? Collections.unmodifiableSet(EnumSet.copyOf(allowedTerrainTypes))
            : Collections.emptySet();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public StructureCategory getCategory() {
        return category;
    }

    public int getWidthTiles() {
        return widthTiles;
    }

    public int getHeightTiles() {
        return heightTiles;
    }

    public boolean blocksMovement() {
        return blocksMovement;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public int getDefenderCapacity() {
        return defenderCapacity;
    }

    public Map<String, Integer> getBuildCosts() {
        return buildCosts;
    }

    public Set<FrontierTerrainSampler.TerrainType> getAllowedTerrainTypes() {
        return allowedTerrainTypes;
    }

    public boolean allowsTerrain(FrontierTerrainSampler.TerrainType terrainType) {
        return terrainType != null && (allowedTerrainTypes.isEmpty() || allowedTerrainTypes.contains(terrainType));
    }
}
