package com.rogueforge.game.engine.meta;

import com.rogueforge.game.data.MetaProgressionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeLegacyEngineTest {

    @Test
    void affordableNodeCanBePurchasedAndAppliesBonuses() {
        ForgeLegacyEngine engine = new ForgeLegacyEngine();
        MetaProgressionState state = new MetaProgressionState();
        state.setForgeShards(150);

        ForgeLegacyNodeDefinition next = engine.getNextAffordableNode(state);

        assertNotNull(next);
        assertEquals("reserve_cell", next.getId());
        assertTrue(engine.purchaseNode(state, next.getId()));
        assertEquals(50, state.getForgeShards());

        ForgeLegacyBonuses bonuses = engine.getBonuses(state);
        assertEquals(1, bonuses.getStartingPotionBonus());
    }

    @Test
    void actFiveMilestonesUnlockChallengeSystemsAtExpectedFloors() {
        ForgeLegacyEngine engine = new ForgeLegacyEngine();

        assertEquals(0, engine.getLegendaryRobotsUnlocked(9));
        assertEquals(1, engine.getLegendaryRobotsUnlocked(10));
        assertEquals(3, engine.getLegendaryRobotsUnlocked(35));
        assertTrue(engine.areChallengeRunsUnlocked(15));
        assertEquals(2, engine.getHardModeSeedsUnlocked(30));
    }
}
