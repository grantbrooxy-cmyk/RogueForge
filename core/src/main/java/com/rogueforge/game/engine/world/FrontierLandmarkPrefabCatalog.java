package com.rogueforge.game.engine.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FrontierLandmarkPrefabCatalog {
    private final List<FrontierLandmarkPrefabDefinition> prefabs;

    public FrontierLandmarkPrefabCatalog() {
        List<FrontierLandmarkPrefabDefinition> entries = new ArrayList<>();
        entries.add(new FrontierLandmarkPrefabDefinition(
            "village_small_a",
            "Hidden Hamlet",
            "village",
            16f,
            12f,
            "prefabs/village_small_a",
            "A weathered frontier hamlet stands here, half reclaimed by the fields.",
            FrontierTerrainSampler.TerrainType.MEADOW,
            FrontierTerrainSampler.TerrainType.GROVE
        ));
        entries.add(new FrontierLandmarkPrefabDefinition(
            "ruined_village_a",
            "Ruin Hamlet",
            "ruin",
            15f,
            11f,
            "prefabs/ruined_village_a",
            "Collapsed cottages and broken salvage frames suggest this place fell long before Ironhaven stabilized.",
            FrontierTerrainSampler.TerrainType.RUIN_FIELD,
            FrontierTerrainSampler.TerrainType.STONE_FLATS
        ));
        entries.add(new FrontierLandmarkPrefabDefinition(
            "trader_camp_a",
            "Trader Camp",
            "camp",
            11f,
            9f,
            "prefabs/trader_camp_a",
            "A small trader encampment once used this patch as a relay stop.",
            FrontierTerrainSampler.TerrainType.MEADOW,
            FrontierTerrainSampler.TerrainType.SCRUB,
            FrontierTerrainSampler.TerrainType.STONE_FLATS
        ));
        entries.add(new FrontierLandmarkPrefabDefinition(
            "watchpost_a",
            "Broken Watchpost",
            "watchpost",
            10f,
            10f,
            "prefabs/watchpost_a",
            "A ruined watchpost overlooks the surrounding routes.",
            FrontierTerrainSampler.TerrainType.STONE_FLATS,
            FrontierTerrainSampler.TerrainType.SCRUB,
            FrontierTerrainSampler.TerrainType.GROVE
        ));
        entries.add(new FrontierLandmarkPrefabDefinition(
            "shrine_a",
            "Waystone Shrine",
            "shrine",
            8f,
            8f,
            "prefabs/shrine_a",
            "A lone shrine marks an older route through the frontier.",
            FrontierTerrainSampler.TerrainType.GROVE,
            FrontierTerrainSampler.TerrainType.MARSH,
            FrontierTerrainSampler.TerrainType.MEADOW
        ));
        prefabs = Collections.unmodifiableList(entries);
    }

    public List<FrontierLandmarkPrefabDefinition> getAll() {
        return prefabs;
    }
}
