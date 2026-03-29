package com.rogueforge.game.engine.base;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reconciles assigned reserve bots into live overworld defenders with anchor points.
 */
public class BaseDefenseDirector {
    private static final float ANCHOR_OFFSET = 38f;
    private static final float PATROL_OFFSET = 82f;

    public List<BaseDefenderUnit> synchronize(
        BaseState baseState,
        StructureDefinitionRegistry structureRegistry,
        Map<String, BaseDefenderProfile> profilesByRobotId,
        List<BaseDefenderUnit> existingUnits
    ) {
        List<BaseDefenderUnit> units = new ArrayList<>();
        if (baseState == null || structureRegistry == null || profilesByRobotId == null || profilesByRobotId.isEmpty()) {
            return units;
        }

        Map<String, BaseDefenderUnit> existingByKey = new HashMap<>();
        if (existingUnits != null) {
            for (BaseDefenderUnit unit : existingUnits) {
                if (unit != null && unit.getAssignmentKey() != null) {
                    existingByKey.put(unit.getAssignmentKey(), unit);
                }
            }
        }

        Map<String, Integer> slotsByStructure = new HashMap<>();
        for (DefenderAssignment assignment : baseState.getDefenderAssignments()) {
            if (assignment == null || assignment.getStructureInstanceId() == null || assignment.getRobotId() == null) {
                continue;
            }
            PlacedStructure structure = baseState.findStructure(assignment.getStructureInstanceId());
            if (structure == null || !structure.isActive()) {
                continue;
            }
            StructureDefinition definition = structureRegistry.get(structure.getStructureDefinitionId());
            if (definition == null || definition.getDefenderCapacity() <= 0) {
                continue;
            }
            BaseDefenderProfile profile = profilesByRobotId.get(assignment.getRobotId());
            if (profile == null) {
                continue;
            }

            int slotIndex = slotsByStructure.getOrDefault(structure.getInstanceId(), 0);
            slotsByStructure.put(structure.getInstanceId(), slotIndex + 1);

            Vector2 guardPosition = computeSlotPoint(structure.getBounds(), slotIndex, false);
            Vector2 patrolPosition = assignment.getRole() == DefenderRole.PATROL
                ? computeSlotPoint(structure.getBounds(), slotIndex, true)
                : new Vector2(guardPosition);
            String assignmentKey = buildAssignmentKey(assignment);
            BaseDefenderUnit existing = existingByKey.get(assignmentKey);
            units.add(existing != null
                ? rebuildExisting(existing, assignment, profile, guardPosition, patrolPosition)
                : createNew(assignmentKey, assignment, profile, guardPosition, patrolPosition));
        }

        return units;
    }

    public Vector2 getIdleTarget(BaseDefenderUnit unit, float elapsedSeconds) {
        if (unit == null) {
            return new Vector2();
        }
        if (unit.getRole() != DefenderRole.PATROL) {
            return new Vector2(unit.getGuardPosition());
        }
        boolean patrolSweep = ((int) Math.floor(Math.max(0f, elapsedSeconds) / 2.6f)) % 2 == 0;
        return patrolSweep ? new Vector2(unit.getPatrolPosition()) : new Vector2(unit.getGuardPosition());
    }

    public String buildAssignmentKey(DefenderAssignment assignment) {
        return assignment.getStructureInstanceId() + "::" + assignment.getRobotId();
    }

    private BaseDefenderUnit createNew(
        String assignmentKey,
        DefenderAssignment assignment,
        BaseDefenderProfile profile,
        Vector2 guardPosition,
        Vector2 patrolPosition
    ) {
        return new BaseDefenderUnit(
            assignmentKey,
            profile.getRobotId(),
            profile.getDisplayName(),
            assignment.getStructureInstanceId(),
            assignment.getRole(),
            guardPosition,
            guardPosition,
            patrolPosition,
            profile.getMaxHealth(),
            profile.getMaxHealth(),
            profile.getAttackPower(),
            profile.getDefense(),
            profile.getMoveSpeed(),
            profile.getAttackRange(),
            profile.getDetectionRange(),
            profile.getAttackCooldown(),
            0f,
            0f,
            true
        );
    }

    private BaseDefenderUnit rebuildExisting(
        BaseDefenderUnit existing,
        DefenderAssignment assignment,
        BaseDefenderProfile profile,
        Vector2 guardPosition,
        Vector2 patrolPosition
    ) {
        Vector2 position = existing.isActive() ? existing.getPosition() : guardPosition;
        return new BaseDefenderUnit(
            existing.getAssignmentKey(),
            profile.getRobotId(),
            profile.getDisplayName(),
            assignment.getStructureInstanceId(),
            assignment.getRole(),
            position,
            guardPosition,
            patrolPosition,
            profile.getMaxHealth(),
            Math.min(profile.getMaxHealth(), existing.getCurrentHealth()),
            profile.getAttackPower(),
            profile.getDefense(),
            profile.getMoveSpeed(),
            profile.getAttackRange(),
            profile.getDetectionRange(),
            profile.getAttackCooldown(),
            existing.getAttackTimer(),
            existing.getAnimationTime(),
            existing.isActive()
        );
    }

    private Vector2 computeSlotPoint(Rectangle bounds, int slotIndex, boolean patrolOffset) {
        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;
        int side = Math.floorMod(slotIndex, 4);
        int ring = Math.max(0, slotIndex / 4);
        float offset = (patrolOffset ? PATROL_OFFSET : ANCHOR_OFFSET) + ring * 18f;
        switch (side) {
            case 0:
                return new Vector2(centerX, bounds.y + bounds.height + offset);
            case 1:
                return new Vector2(bounds.x + bounds.width + offset, centerY);
            case 2:
                return new Vector2(centerX, bounds.y - offset);
            default:
                return new Vector2(bounds.x - offset, centerY);
        }
    }
}
