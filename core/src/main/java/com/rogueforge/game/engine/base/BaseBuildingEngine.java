package com.rogueforge.game.engine.base;

import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import java.util.UUID;

/**
 * Engine-level facade for structure placement and reserve-bot assignments.
 */
public class BaseBuildingEngine {
    private final StructureDefinitionRegistry structureRegistry;
    private final BasePlacementValidator placementValidator;

    public BaseBuildingEngine() {
        this(new StructureDefinitionRegistry(), new BasePlacementValidator());
    }

    public BaseBuildingEngine(StructureDefinitionRegistry structureRegistry, BasePlacementValidator placementValidator) {
        this.structureRegistry = structureRegistry != null ? structureRegistry : new StructureDefinitionRegistry();
        this.placementValidator = placementValidator != null ? placementValidator : new BasePlacementValidator();
    }

    public StructureDefinitionRegistry getStructureRegistry() {
        return structureRegistry;
    }

    public BasePlacementResult validatePlacement(
        String structureDefinitionId,
        BaseState baseState,
        TmxWorldLoader.LoadedZone zone,
        FrontierTerrainSampler terrainSampler,
        String claimedSiteId,
        float worldX,
        float worldY
    ) {
        return placementValidator.validate(structureRegistry.get(structureDefinitionId), baseState, zone, terrainSampler, claimedSiteId, worldX, worldY);
    }

    public PlacedStructure placeStructure(
        String structureDefinitionId,
        BaseState baseState,
        TmxWorldLoader.LoadedZone zone,
        FrontierTerrainSampler terrainSampler,
        String claimedSiteId,
        float worldX,
        float worldY
    ) {
        StructureDefinition definition = structureRegistry.get(structureDefinitionId);
        BasePlacementResult result = placementValidator.validate(definition, baseState, zone, terrainSampler, claimedSiteId, worldX, worldY);
        if (!result.isAllowed()) {
            return null;
        }
        PlacedStructure structure = new PlacedStructure(
            createStructureInstanceId(definition),
            definition.getId(),
            baseState.getZoneId(),
            claimedSiteId,
            result.getBounds(),
            definition.getMaxHitPoints()
        );
        baseState.addPlacedStructure(structure);
        return structure;
    }

    public boolean removeStructure(BaseState baseState, String structureInstanceId) {
        return baseState != null && baseState.removeStructure(structureInstanceId);
    }

    public boolean assignDefender(BaseState baseState, String structureInstanceId, String robotId, DefenderRole role) {
        if (baseState == null || structureInstanceId == null || structureInstanceId.isEmpty() || robotId == null || robotId.isEmpty()) {
            return false;
        }
        PlacedStructure structure = baseState.findStructure(structureInstanceId);
        if (structure == null) {
            return false;
        }
        StructureDefinition definition = structureRegistry.get(structure.getStructureDefinitionId());
        if (definition == null || definition.getDefenderCapacity() <= 0) {
            return false;
        }
        if (baseState.hasRobotAssignment(robotId)) {
            return false;
        }
        if (baseState.getAssignedDefenderCount(structureInstanceId) >= definition.getDefenderCapacity()) {
            return false;
        }
        baseState.addDefenderAssignment(new DefenderAssignment(structureInstanceId, robotId, role));
        return true;
    }

    public boolean applyStructureDamage(BaseState baseState, String structureInstanceId, int damage) {
        if (baseState == null || structureInstanceId == null || structureInstanceId.isEmpty() || damage <= 0) {
            return false;
        }
        PlacedStructure structure = baseState.findStructure(structureInstanceId);
        if (structure == null || !structure.isActive()) {
            return false;
        }
        structure.setCurrentHitPoints(structure.getCurrentHitPoints() - damage);
        if (structure.getCurrentHitPoints() <= 0) {
            structure.setActive(false);
        }
        return true;
    }

    public int repairStructure(PlacedStructure structure, StructureDefinition definition, int repairAmount) {
        if (structure == null || definition == null || repairAmount <= 0) {
            return 0;
        }
        int before = structure.getCurrentHitPoints();
        int repaired = Math.min(definition.getMaxHitPoints(), before + repairAmount) - before;
        if (repaired > 0) {
            structure.setCurrentHitPoints(before + repaired);
            structure.setActive(true);
        }
        return repaired;
    }

    private String createStructureInstanceId(StructureDefinition definition) {
        String prefix = definition != null && definition.getId() != null ? definition.getId() : "structure";
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
