package com.rogueforge.game.engine.base;

import com.rogueforge.game.engine.social.OwnershipRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime and persistent base state for one frontier zone.
 */
public class BaseState {
    private final String zoneId;
    private final List<String> claimedSiteIds = new ArrayList<>();
    private final List<PlacedStructure> placedStructures = new ArrayList<>();
    private final List<DefenderAssignment> defenderAssignments = new ArrayList<>();
    private final Map<String, Float> defenderHealthByRobotId = new HashMap<>();
    private final Map<String, OwnershipRecord> claimedSiteOwnershipById = new HashMap<>();
    private final Map<String, OwnershipRecord> structureOwnershipByInstanceId = new HashMap<>();
    private final BaseRaidState raidState = new BaseRaidState();

    public BaseState(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public List<String> getClaimedSiteIds() {
        return new ArrayList<>(claimedSiteIds);
    }

    public boolean claimSite(String siteId) {
        return claimSite(siteId, null);
    }

    public boolean claimSite(String siteId, OwnershipRecord ownershipRecord) {
        if (siteId == null || siteId.isEmpty() || claimedSiteIds.contains(siteId)) {
            return false;
        }
        claimedSiteIds.add(siteId);
        if (ownershipRecord != null) {
            claimedSiteOwnershipById.put(siteId, ownershipRecord);
        }
        return true;
    }

    public boolean hasClaimedSite(String siteId) {
        return siteId != null && claimedSiteIds.contains(siteId);
    }

    public List<PlacedStructure> getPlacedStructures() {
        return new ArrayList<>(placedStructures);
    }

    public void addPlacedStructure(PlacedStructure structure) {
        if (structure != null) {
            placedStructures.add(structure);
        }
    }

    public void setClaimedSiteOwnership(String siteId, OwnershipRecord ownershipRecord) {
        if (siteId == null || siteId.isEmpty() || ownershipRecord == null) {
            return;
        }
        claimedSiteOwnershipById.put(siteId, ownershipRecord);
    }

    public OwnershipRecord getClaimedSiteOwnership(String siteId) {
        return siteId != null ? claimedSiteOwnershipById.get(siteId) : null;
    }

    public Map<String, OwnershipRecord> getClaimedSiteOwnershipById() {
        return new HashMap<>(claimedSiteOwnershipById);
    }

    public void setClaimedSiteOwnershipById(Map<String, OwnershipRecord> ownershipById) {
        claimedSiteOwnershipById.clear();
        if (ownershipById != null) {
            claimedSiteOwnershipById.putAll(ownershipById);
        }
    }

    public PlacedStructure findStructure(String structureInstanceId) {
        if (structureInstanceId == null || structureInstanceId.isEmpty()) {
            return null;
        }
        for (PlacedStructure structure : placedStructures) {
            if (structureInstanceId.equals(structure.getInstanceId())) {
                return structure;
            }
        }
        return null;
    }

    public boolean removeStructure(String structureInstanceId) {
        PlacedStructure structure = findStructure(structureInstanceId);
        if (structure == null) {
            return false;
        }
        placedStructures.remove(structure);
        defenderAssignments.removeIf(assignment -> structureInstanceId.equals(assignment.getStructureInstanceId()));
        structureOwnershipByInstanceId.remove(structureInstanceId);
        return true;
    }

    public void setStructureOwnership(String structureInstanceId, OwnershipRecord ownershipRecord) {
        if (structureInstanceId == null || structureInstanceId.isEmpty() || ownershipRecord == null) {
            return;
        }
        structureOwnershipByInstanceId.put(structureInstanceId, ownershipRecord);
    }

    public OwnershipRecord getStructureOwnership(String structureInstanceId) {
        return structureInstanceId != null ? structureOwnershipByInstanceId.get(structureInstanceId) : null;
    }

    public Map<String, OwnershipRecord> getStructureOwnershipByInstanceId() {
        return new HashMap<>(structureOwnershipByInstanceId);
    }

    public void setStructureOwnershipByInstanceId(Map<String, OwnershipRecord> ownershipById) {
        structureOwnershipByInstanceId.clear();
        if (ownershipById != null) {
            structureOwnershipByInstanceId.putAll(ownershipById);
        }
    }

    public List<DefenderAssignment> getDefenderAssignments() {
        return new ArrayList<>(defenderAssignments);
    }

    public void addDefenderAssignment(DefenderAssignment assignment) {
        if (assignment != null) {
            defenderAssignments.add(assignment);
        }
    }

    public long getAssignedDefenderCount(String structureInstanceId) {
        return defenderAssignments.stream()
            .filter(assignment -> structureInstanceId != null && structureInstanceId.equals(assignment.getStructureInstanceId()))
            .count();
    }

    public boolean hasRobotAssignment(String robotId) {
        return defenderAssignments.stream().anyMatch(assignment -> robotId != null && robotId.equals(assignment.getRobotId()));
    }

    public float getDefenderHealth(String robotId, float fallbackHealth) {
        return robotId != null ? defenderHealthByRobotId.getOrDefault(robotId, fallbackHealth) : fallbackHealth;
    }

    public void setDefenderHealth(String robotId, float currentHealth) {
        if (robotId == null || robotId.isEmpty()) {
            return;
        }
        defenderHealthByRobotId.put(robotId, Math.max(0f, currentHealth));
    }

    public Map<String, Float> getDefenderHealthByRobotId() {
        return new HashMap<>(defenderHealthByRobotId);
    }

    public void setDefenderHealthByRobotId(Map<String, Float> healthByRobotId) {
        defenderHealthByRobotId.clear();
        if (healthByRobotId != null) {
            for (Map.Entry<String, Float> entry : healthByRobotId.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().isEmpty()) {
                    defenderHealthByRobotId.put(entry.getKey(), Math.max(0f, entry.getValue() != null ? entry.getValue() : 0f));
                }
            }
        }
    }

    public BaseRaidState getRaidState() {
        return raidState;
    }
}
