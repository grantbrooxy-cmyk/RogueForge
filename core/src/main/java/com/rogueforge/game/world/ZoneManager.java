package com.rogueforge.game.world;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.event.ZoneChangedEvent;

import java.util.HashMap;
import java.util.Map;

public class ZoneManager {
    private ZoneDefinition currentZone;
    private TiledMap currentMap;
    private final AssetManager assetManager;
    private final EventBus eventBus;
    private final Map<String, ZoneDefinition> zones = new HashMap<>();

    public ZoneManager(AssetManager assetManager, EventBus eventBus) {
        this.assetManager = assetManager;
        this.eventBus = eventBus;
        this.currentZone = null;
        this.currentMap = null;
    }

    /** Register a zone definition so it can be loaded by id. */
    public void registerZone(ZoneDefinition zone) {
        if (zone != null && zone.getId() != null) {
            zones.put(zone.getId(), zone);
        }
    }

    public void loadZone(String zoneId) {
        if (currentMap != null) {
            try {
                String oldPath = currentZone != null ? currentZone.getTilemapPath() : null;
                if (oldPath != null) assetManager.unload(oldPath);
            } catch (Exception e) { /* already unloaded */ }
            currentMap = null;
        }

        currentZone = zones.get(zoneId);
        if (currentZone != null) {
            String mapPath = currentZone.getTilemapPath();
            assetManager.load(mapPath, TiledMap.class);
            assetManager.finishLoading();
            currentMap = assetManager.get(mapPath, TiledMap.class);

            eventBus.fire(new ZoneChangedEvent(currentZone));
        }
    }

    public ZoneDefinition getCurrentZone() {
        return currentZone;
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public String getZoneRankFloor() {
        return currentZone != null ? currentZone.getRankFloor() : "G";
    }

    public String getZoneRankCeiling() {
        return currentZone != null ? currentZone.getRankCeiling() : "G";
    }

    public void dispose() {
        if (currentMap != null) {
            try {
                String mapPath = currentZone != null ? currentZone.getTilemapPath() : "";
                assetManager.unload(mapPath);
            } catch (Exception e) { /* silent */ }
            currentMap = null;
        }
        currentZone = null;
    }
}
