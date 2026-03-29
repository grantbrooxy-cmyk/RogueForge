package com.rogueforge.game.world;

import com.rogueforge.game.engine.world.FrontierBiomeCatalog;
import com.rogueforge.game.engine.world.FrontierBiomeDefinition;
import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FrontierBiomeCatalogTest {

    @Test
    void everySeededTerrainTypeMapsToABiomeDefinitionWithAssetFolders() {
        FrontierBiomeCatalog catalog = new FrontierBiomeCatalog();

        for (FrontierTerrainSampler.TerrainType terrainType : FrontierTerrainSampler.TerrainType.values()) {
            FrontierBiomeDefinition definition = catalog.resolve(terrainType);
            assertNotNull(definition);
            assertEquals(terrainType, definition.getTerrainType());
            assertNotNull(definition.getGroundAssetFolder());
            assertNotNull(definition.getObjectAssetFolder());
            assertFalse(definition.getGroundAssetFolder().isEmpty());
            assertFalse(definition.getObjectAssetFolder().isEmpty());
        }
    }
}
