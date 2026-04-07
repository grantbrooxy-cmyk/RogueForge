package com.rogueforge.game.engine.world;

import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontierChunkManagerTest {

    @Test
    void updateTracksChunkTransitionsAndRegions() {
        FrontierChunkManager manager = new FrontierChunkManager(32, 1, 2);

        manager.update(new Vector2(32f, 32f), 16, 16);
        assertFalse(manager.getActiveChunks().isEmpty());
        assertFalse(manager.getActiveRegions().isEmpty());

        manager.update(new Vector2(1200f, 32f), 16, 16);

        assertFalse(manager.getEnteredChunks().isEmpty());
        assertFalse(manager.getExitedChunks().isEmpty());
        assertTrue(manager.getCenterChunk().chunkX > 0);
    }
}
