package com.rogueforge.game.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class WorldRenderer {
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap currentMap;
    private final float unitScale;

    public WorldRenderer(float unitScale) {
        this.unitScale = unitScale;
        this.mapRenderer = null;
        this.currentMap = null;
    }

    public void setMap(TiledMap map) {
        if (this.mapRenderer != null) {
            this.mapRenderer.dispose();
        }
        this.currentMap = map;
        if (map != null) {
            this.mapRenderer = new OrthogonalTiledMapRenderer(map, unitScale);
        }
    }

    public void render(OrthographicCamera camera) {
        if (mapRenderer != null && currentMap != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public OrthogonalTiledMapRenderer getMapRenderer() {
        return mapRenderer;
    }

    public void dispose() {
        if (mapRenderer != null) {
            mapRenderer.dispose();
            mapRenderer = null;
        }
        if (currentMap != null) {
            currentMap.dispose();
            currentMap = null;
        }
    }
}
