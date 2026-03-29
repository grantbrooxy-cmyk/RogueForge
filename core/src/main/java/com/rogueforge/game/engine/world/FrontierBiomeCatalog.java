package com.rogueforge.game.engine.world;

import com.badlogic.gdx.graphics.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Catalog of seeded biome definitions used by expansive frontier zones.
 */
public class FrontierBiomeCatalog {
    private final Map<FrontierTerrainSampler.TerrainType, FrontierBiomeDefinition> byTerrainType =
        new EnumMap<>(FrontierTerrainSampler.TerrainType.class);
    private final Map<String, FrontierBiomeDefinition> byId = new HashMap<>();

    public FrontierBiomeCatalog() {
        registerDefaults();
    }

    public FrontierBiomeDefinition resolve(FrontierTerrainSampler.TerrainType terrainType) {
        FrontierBiomeDefinition definition = byTerrainType.get(terrainType);
        if (definition != null) {
            return definition;
        }
        return byTerrainType.get(FrontierTerrainSampler.TerrainType.MEADOW);
    }

    public FrontierBiomeDefinition resolve(String biomeId) {
        if (biomeId == null || biomeId.isEmpty()) {
            return resolve(FrontierTerrainSampler.TerrainType.MEADOW);
        }
        FrontierBiomeDefinition definition = byId.get(biomeId);
        return definition != null ? definition : resolve(FrontierTerrainSampler.TerrainType.MEADOW);
    }

    private void registerDefaults() {
        register(new FrontierBiomeDefinition(
            "meadow",
            "Open Meadow",
            FrontierTerrainSampler.TerrainType.MEADOW,
            "biomes/meadow/tiles",
            "biomes/meadow/objects",
            new Color(0.58f, 0.68f, 0.34f, 1f),
            new Color(0.40f, 0.50f, 0.24f, 1f),
            new Color(0.22f, 0.40f, 0.30f, 1f),
            new Color(0.60f, 0.58f, 0.52f, 1f)
        ));
        register(new FrontierBiomeDefinition(
            "grove",
            "Rooted Grove",
            FrontierTerrainSampler.TerrainType.GROVE,
            "biomes/grove/tiles",
            "biomes/grove/objects",
            new Color(0.30f, 0.50f, 0.24f, 1f),
            new Color(0.18f, 0.34f, 0.16f, 1f),
            new Color(0.22f, 0.40f, 0.30f, 1f),
            new Color(0.50f, 0.54f, 0.42f, 1f)
        ));
        register(new FrontierBiomeDefinition(
            "marsh",
            "Sour Marsh",
            FrontierTerrainSampler.TerrainType.MARSH,
            "biomes/marsh/tiles",
            "biomes/marsh/objects",
            new Color(0.26f, 0.42f, 0.28f, 1f),
            new Color(0.16f, 0.28f, 0.18f, 1f),
            new Color(0.18f, 0.34f, 0.28f, 1f),
            new Color(0.46f, 0.50f, 0.44f, 1f)
        ));
        register(new FrontierBiomeDefinition(
            "scrub",
            "Dry Scrub",
            FrontierTerrainSampler.TerrainType.SCRUB,
            "biomes/scrub/tiles",
            "biomes/scrub/objects",
            new Color(0.48f, 0.52f, 0.26f, 1f),
            new Color(0.32f, 0.36f, 0.16f, 1f),
            new Color(0.24f, 0.34f, 0.22f, 1f),
            new Color(0.58f, 0.54f, 0.44f, 1f)
        ));
        register(new FrontierBiomeDefinition(
            "ruin_field",
            "Ruin Field",
            FrontierTerrainSampler.TerrainType.RUIN_FIELD,
            "biomes/ruin_field/tiles",
            "biomes/ruin_field/objects",
            new Color(0.52f, 0.46f, 0.30f, 1f),
            new Color(0.36f, 0.30f, 0.18f, 1f),
            new Color(0.24f, 0.32f, 0.24f, 1f),
            new Color(0.62f, 0.58f, 0.48f, 1f)
        ));
        register(new FrontierBiomeDefinition(
            "stone_flats",
            "Stone Flats",
            FrontierTerrainSampler.TerrainType.STONE_FLATS,
            "biomes/stone_flats/tiles",
            "biomes/stone_flats/objects",
            new Color(0.58f, 0.56f, 0.48f, 1f),
            new Color(0.40f, 0.38f, 0.32f, 1f),
            new Color(0.28f, 0.34f, 0.32f, 1f),
            new Color(0.68f, 0.66f, 0.60f, 1f)
        ));
    }

    private void register(FrontierBiomeDefinition definition) {
        if (definition == null || definition.getTerrainType() == null || definition.getId() == null || definition.getId().isEmpty()) {
            return;
        }
        byTerrainType.put(definition.getTerrainType(), definition);
        byId.put(definition.getId(), definition);
    }
}
