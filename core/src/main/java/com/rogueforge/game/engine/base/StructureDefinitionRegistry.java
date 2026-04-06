package com.rogueforge.game.engine.base;

import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Small reusable starter registry for base-building structures.
 */
public class StructureDefinitionRegistry {
    private final Map<String, StructureDefinition> definitionsById = new HashMap<>();

    public StructureDefinitionRegistry() {
        registerDefaults();
    }

    public StructureDefinition get(String structureId) {
        return definitionsById.get(structureId);
    }

    public List<StructureDefinition> getAll() {
        return new ArrayList<>(definitionsById.values());
    }

    public void register(StructureDefinition definition) {
        if (definition == null || definition.getId() == null || definition.getId().isEmpty()) {
            return;
        }
        definitionsById.put(definition.getId(), definition);
    }

    private void registerDefaults() {
        register(new StructureDefinition(
            "palisade_wall",
            "Palisade Wall",
            StructureCategory.WALL,
            1,
            1,
            true,
            120,
            0,
            0,
            Map.of("bone_fiber", 2, "scrap_alloy", 1),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "sentry_post",
            "Sentry Post",
            StructureCategory.DEFENSE,
            2,
            2,
            true,
            220,
            0,
            2,
            Map.of("scrap_alloy", 4, "bone_fiber", 2),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "supply_crate",
            "Supply Crate",
            StructureCategory.STORAGE,
            2,
            1,
            true,
            160,
            24,
            0,
            Map.of("scrap_alloy", 3),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.GROVE,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "field_fabricator",
            "Field Fabricator",
            StructureCategory.CRAFTING,
            2,
            2,
            true,
            240,
            0,
            0,
            Map.of("scrap_alloy", 5, "slime_resin", 3),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "power_pylon",
            "Power Pylon",
            StructureCategory.POWER,
            1,
            2,
            true,
            180,
            0,
            0,
            Map.of("scrap_alloy", 3, "slime_resin", 2),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "storm_relay",
            "Storm Relay",
            StructureCategory.UTILITY,
            2,
            1,
            true,
            240,
            0,
            2,
            Map.of("scout_gyro", 3, "hex_crystal", 2, "scrap_alloy", 2),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.MEADOW,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
        register(new StructureDefinition(
            "fusion_anvil",
            "Fusion Anvil",
            StructureCategory.CRAFTING,
            2,
            2,
            true,
            280,
            0,
            0,
            Map.of("fortress_plate", 2, "hex_crystal", 2, "primal_core", 1),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.STONE_FLATS,
                FrontierTerrainSampler.TerrainType.SCRUB
            )
        ));
        register(new StructureDefinition(
            "dragon_roost_beacon",
            "Dragon Roost Beacon",
            StructureCategory.UTILITY,
            2,
            2,
            true,
            320,
            8,
            1,
            Map.of("drake_heart", 1, "fortress_plate", 2, "scout_gyro", 2),
            EnumSet.of(
                FrontierTerrainSampler.TerrainType.GROVE,
                FrontierTerrainSampler.TerrainType.SCRUB,
                FrontierTerrainSampler.TerrainType.STONE_FLATS
            )
        ));
    }
}
