package com.rogueforge.game.world;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.core.EventPriority;
import com.rogueforge.game.core.GameState;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.engine.ServiceLifecycle;
import com.rogueforge.game.event.WorldDynamicEventTriggeredEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Spawns follow-up world beats after major world milestones like boss clears.
 */
public class DynamicWorldEventSystem implements ServiceLifecycle {
    private final EventBus eventBus;

    public DynamicWorldEventSystem(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public List<DynamicWorldEvent> handleBossClear(String bossId, String clearedZoneId,
                                                   Collection<ZoneDefinition> availableZones,
                                                   GameState gameState, WorldStateManager worldStateManager) {
        List<DynamicWorldEvent> events = new ArrayList<>();
        if (bossId == null || bossId.isEmpty() || worldStateManager == null || gameState == null) {
            return events;
        }

        String onceFlag = "world.dynamic.boss_clear." + bossId;
        if (worldStateManager.isFlagActive(gameState, onceFlag)) {
            return events;
        }
        worldStateManager.setFlag(gameState, onceFlag, true);

        String securedFlag = "world.zone.secured." + clearedZoneId;
        worldStateManager.setFlag(gameState, securedFlag, true);
        events.add(new DynamicWorldEvent(
            DynamicWorldEvent.Type.WORLD_CHANGE,
            clearedZoneId,
            "Route Stabilized",
            "The corridor around " + formatZoneId(clearedZoneId) + " is stabilizing after the boss collapse.",
            securedFlag,
            null,
            null,
            null
        ));

        String contractZoneId = resolveContractZone(clearedZoneId, availableZones);
        if (contractZoneId != null && !contractZoneId.isEmpty()) {
            String contractFlag = "world.dynamic.contract." + bossId;
            worldStateManager.setFlag(gameState, contractFlag, true);
            events.add(new DynamicWorldEvent(
                DynamicWorldEvent.Type.CONTRACT,
                contractZoneId,
                "Emergency Contract Posted",
                "Guild dispatch has posted a follow-up recovery contract in " + formatZoneId(contractZoneId) + ".",
                contractFlag,
                "DYNAMIC_RECOVERY",
                "Aftershock Recovery",
                "Sweep the zone for salvage, survivors, and anything the boss kept buried."
            ));
        }

        String ambushZoneId = resolveAmbushZone(clearedZoneId, availableZones);
        if (ambushZoneId != null && !ambushZoneId.isEmpty()) {
            String ambushFlag = "world.dynamic.ambush." + bossId;
            worldStateManager.setFlag(gameState, ambushFlag, true);
            events.add(new DynamicWorldEvent(
                DynamicWorldEvent.Type.AMBUSH,
                ambushZoneId,
                "Hostile Counterpush",
                "Scattered hostiles are regrouping into ambush lanes near " + formatZoneId(ambushZoneId) + ".",
                ambushFlag,
                null,
                null,
                null
            ));
        }

        for (DynamicWorldEvent event : events) {
            if (eventBus != null) {
                eventBus.queue(new WorldDynamicEventTriggeredEvent(
                    event.getType().name(),
                    event.getZoneId(),
                    event.getTitle(),
                    event.getDescription()
                ), EventPriority.NORMAL);
            }
        }
        return events;
    }

    private String resolveContractZone(String clearedZoneId, Collection<ZoneDefinition> availableZones) {
        if (clearedZoneId != null && !clearedZoneId.isEmpty() && !"town".equals(clearedZoneId)) {
            return clearedZoneId;
        }
        return selectAlternateZone(clearedZoneId, availableZones, 0);
    }

    private String resolveAmbushZone(String clearedZoneId, Collection<ZoneDefinition> availableZones) {
        return selectAlternateZone(clearedZoneId, availableZones, 1);
    }

    private String selectAlternateZone(String excludedZoneId, Collection<ZoneDefinition> availableZones, int offset) {
        if (availableZones == null || availableZones.isEmpty()) {
            return null;
        }
        List<String> zoneIds = new ArrayList<>();
        for (ZoneDefinition definition : availableZones) {
            if (definition == null || definition.getId() == null || definition.getId().isEmpty()) {
                continue;
            }
            if ("town".equals(definition.getId()) || excludedZoneId != null && excludedZoneId.equals(definition.getId())) {
                continue;
            }
            zoneIds.add(definition.getId());
        }
        zoneIds.sort(Comparator.naturalOrder());
        if (zoneIds.isEmpty()) {
            return null;
        }
        return zoneIds.get(Math.floorMod(offset, zoneIds.size()));
    }

    private String formatZoneId(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return "the frontier";
        }
        return zoneId.replace('_', ' ').toLowerCase(Locale.ROOT);
    }

    public static final class DynamicWorldEvent {
        public enum Type {
            CONTRACT,
            AMBUSH,
            WORLD_CHANGE
        }

        private final Type type;
        private final String zoneId;
        private final String title;
        private final String description;
        private final String worldFlag;
        private final String contractKind;
        private final String contractTitle;
        private final String contractDescription;

        public DynamicWorldEvent(Type type, String zoneId, String title, String description,
                                 String worldFlag, String contractKind, String contractTitle,
                                 String contractDescription) {
            this.type = type;
            this.zoneId = zoneId;
            this.title = title;
            this.description = description;
            this.worldFlag = worldFlag;
            this.contractKind = contractKind;
            this.contractTitle = contractTitle;
            this.contractDescription = contractDescription;
        }

        public Type getType() {
            return type;
        }

        public String getZoneId() {
            return zoneId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getWorldFlag() {
            return worldFlag;
        }

        public String getContractKind() {
            return contractKind;
        }

        public String getContractTitle() {
            return contractTitle;
        }

        public String getContractDescription() {
            return contractDescription;
        }
    }
}
