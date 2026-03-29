package com.rogueforge.game.engine.meta;

import com.rogueforge.game.data.MetaProgressionState;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CyberneticEnhancementEngineTest {

    @Test
    void strongRunDraftOffersEnhancementsAndBonusesAccumulate() {
        CyberneticEnhancementEngine engine = new CyberneticEnhancementEngine();
        MetaProgressionState state = new MetaProgressionState();
        state.setOwnedEnhancementIds(List.of("reinforced_lattice", "field_reservoir"));

        DeathDraftResult draft = engine.buildDeathDraft(state, new RunOutcomeSummary("C", 35, 420f, 4, 1, 2, 1));

        assertTrue(draft.getTier().ordinal() >= DeathDraftTier.SOLID.ordinal());
        assertFalse(draft.getChoices().isEmpty());
        assertTrue(draft.getChoices().stream().allMatch(choice -> choice.getKind() == DeathDraftChoice.Kind.ENHANCEMENT));

        CyberneticBonuses bonuses = engine.getBonuses(state);
        assertEquals(26f, bonuses.getHpBonus(), 0.001f);
        assertEquals(2, bonuses.getStartingHealingPotionsBonus());
    }

    @Test
    void strongRunWithActiveCurseOffersCurseClearance() {
        CyberneticEnhancementEngine engine = new CyberneticEnhancementEngine();
        MetaProgressionState state = new MetaProgressionState();
        state.setDeathCount(4);
        state.setActiveCurseIds(List.of("thin_plating"));

        DeathDraftResult draft = engine.buildDeathDraft(state, new RunOutcomeSummary("B", 48, 540f, 5, 1, 3, 1));

        assertTrue(draft.getTier().ordinal() >= DeathDraftTier.STRONG.ordinal());
        assertTrue(draft.getChoices().stream().anyMatch(choice -> choice.getKind() == DeathDraftChoice.Kind.CLEAR_CURSE));
    }

    @Test
    void repeatedEarlyDeathsProduceCurseDraft() {
        CyberneticEnhancementEngine engine = new CyberneticEnhancementEngine();
        MetaProgressionState state = new MetaProgressionState();
        state.setDeathCount(2);
        state.setCollapseStreak(1);

        DeathDraftResult draft = engine.buildDeathDraft(state, new RunOutcomeSummary("G", 1, 20f, 1, 0, 0, 0));

        assertEquals(DeathDraftTier.CATASTROPHIC, draft.getTier());
        assertTrue(state.getCollapseStreak() >= 2);
        assertTrue(draft.getChoices().stream().allMatch(choice -> choice.getKind() == DeathDraftChoice.Kind.CURSE));
    }

    @Test
    void applyingCurseAddsPersistentPenalty() {
        CyberneticEnhancementEngine engine = new CyberneticEnhancementEngine();
        MetaProgressionState state = new MetaProgressionState();

        assertTrue(engine.applyDraftChoice(state, new DeathDraftChoice(
            DeathDraftChoice.Kind.CURSE,
            "thin_plating",
            "Thin Plating",
            "Penalty"
        )));

        CyberneticBonuses bonuses = engine.getBonuses(state);
        assertTrue(state.getActiveCurseIds().contains("thin_plating"));
        assertTrue(bonuses.getHpBonus() < 0f);
    }

    @Test
    void applyingCurseClearanceRemovesPersistentPenalty() {
        CyberneticEnhancementEngine engine = new CyberneticEnhancementEngine();
        MetaProgressionState state = new MetaProgressionState();
        state.setActiveCurseIds(List.of("thin_plating", "fogged_uplink"));

        assertTrue(engine.applyDraftChoice(state, new DeathDraftChoice(
            DeathDraftChoice.Kind.CLEAR_CURSE,
            "thin_plating",
            "Purge Thin Plating",
            "Remove curse"
        )));

        CyberneticBonuses bonuses = engine.getBonuses(state);
        assertFalse(state.getActiveCurseIds().contains("thin_plating"));
        assertTrue(state.getActiveCurseIds().contains("fogged_uplink"));
        assertEquals(0f, bonuses.getHpBonus(), 0.001f);
    }
}
