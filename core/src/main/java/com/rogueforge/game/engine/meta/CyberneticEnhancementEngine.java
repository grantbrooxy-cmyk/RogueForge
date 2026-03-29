package com.rogueforge.game.engine.meta;

import com.rogueforge.game.data.MetaProgressionState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Roguelite meta-progression engine for judged death drafts, enhancements, and curses.
 */
public class CyberneticEnhancementEngine {
    private final Map<String, CyberneticEnhancementDefinition> enhancementsById = new HashMap<>();
    private final Map<String, CurseCardDefinition> cursesById = new HashMap<>();

    public CyberneticEnhancementEngine() {
        registerDefaults();
    }

    public CyberneticBonuses getBonuses(MetaProgressionState state) {
        CyberneticBonuses bonuses = new CyberneticBonuses();
        if (state == null) {
            return bonuses;
        }
        for (String enhancementId : state.getOwnedEnhancementIds()) {
            CyberneticEnhancementDefinition definition = enhancementsById.get(enhancementId);
            if (definition == null) {
                continue;
            }
            bonuses.addHpBonus(definition.getHpBonus());
            bonuses.addAgilityBonus(definition.getAgilityBonus());
            bonuses.addStrengthBonus(definition.getStrengthBonus());
            bonuses.addIntelligenceBonus(definition.getIntelligenceBonus());
            bonuses.addStaminaBonus(definition.getStaminaBonus());
            bonuses.addStartingHealingPotionsBonus(definition.getStartingHealingPotionsBonus());
            bonuses.addStartingGoldBonus(definition.getStartingGoldBonus());
            bonuses.addHarvestYieldBonus(definition.getHarvestYieldBonus());
            bonuses.multiplyExperienceMultiplier(definition.getExperienceMultiplier());
            for (Map.Entry<String, Integer> entry : definition.getStartingForgeComponents().entrySet()) {
                bonuses.addStartingForgeComponent(entry.getKey(), entry.getValue());
            }
        }
        for (String curseId : state.getActiveCurseIds()) {
            CurseCardDefinition curse = cursesById.get(curseId);
            if (curse == null) {
                continue;
            }
            bonuses.addHpBonus(curse.getHpBonus());
            bonuses.addAgilityBonus(curse.getAgilityBonus());
            bonuses.addStrengthBonus(curse.getStrengthBonus());
            bonuses.addIntelligenceBonus(curse.getIntelligenceBonus());
            bonuses.addStaminaBonus(curse.getStaminaBonus());
            bonuses.addStartingHealingPotionsBonus(curse.getStartingHealingPotionsBonus());
            bonuses.addHarvestYieldBonus(curse.getHarvestYieldBonus());
            bonuses.multiplyExperienceMultiplier(curse.getExperienceMultiplier());
        }
        return bonuses;
    }

    public DeathDraftResult buildDeathDraft(MetaProgressionState state, RunOutcomeSummary summary) {
        MetaProgressionState safeState = state != null ? state : new MetaProgressionState();
        RunOutcomeSummary safeSummary = summary != null
            ? summary
            : new RunOutcomeSummary("G", 0, 0f, 1, 0, 0, 0);

        int score = computeProgressScore(safeSummary);
        DeathDraftTier tier = determineTier(score);
        boolean badRun = tier == DeathDraftTier.CATASTROPHIC || tier == DeathDraftTier.POOR;
        if (badRun && score < 40) {
            safeState.setCollapseStreak(safeState.getCollapseStreak() + 1);
        } else if (tier.ordinal() >= DeathDraftTier.SOLID.ordinal()) {
            safeState.setCollapseStreak(Math.max(0, safeState.getCollapseStreak() - 1));
        }

        boolean curseDraft = badRun && safeState.getCollapseStreak() >= 2;
        List<DeathDraftChoice> choices = curseDraft
            ? buildCurseChoices(safeState, 3)
            : buildRewardChoices(safeState, tier);

        String summaryText = curseDraft
            ? "System collapse detected. Repeated shallow runs are destabilizing your frame."
            : buildRewardSummary(tier, safeState);
        return new DeathDraftResult(tier, score, summaryText, choices);
    }

    public boolean applyDraftChoice(MetaProgressionState state, DeathDraftChoice choice) {
        if (state == null || choice == null || choice.getId() == null || choice.getId().isEmpty()) {
            return false;
        }
        switch (choice.getKind()) {
            case CURSE:
                if (!cursesById.containsKey(choice.getId()) || state.getActiveCurseIds().contains(choice.getId())) {
                    return false;
                }
                state.getActiveCurseIds().add(choice.getId());
                return true;
            case CLEAR_CURSE:
                return state.getActiveCurseIds().remove(choice.getId());
            case ENHANCEMENT:
            default:
                if (!enhancementsById.containsKey(choice.getId()) || state.getOwnedEnhancementIds().contains(choice.getId())) {
                    return false;
                }
                state.getOwnedEnhancementIds().add(choice.getId());
                return true;
        }
    }

    public int computeProgressScore(RunOutcomeSummary summary) {
        if (summary == null) {
            return 0;
        }
        int score = 0;
        score += summary.getEnemiesKilled();
        score += Math.round(summary.getSurvivalTimeSeconds() / 30f) * 3;
        score += Math.max(0, summary.getPlayerLevel() - 1) * 10;
        score += summary.getBossesDefeated() * 40;
        score += summary.getStructuresBuilt() * 4;
        score += summary.getClaimedSites() * 10;
        score += rankScore(summary.getZoneRank());
        return Math.max(0, score);
    }

    private DeathDraftTier determineTier(int score) {
        if (score < 25) {
            return DeathDraftTier.CATASTROPHIC;
        }
        if (score < 70) {
            return DeathDraftTier.POOR;
        }
        if (score < 140) {
            return DeathDraftTier.SOLID;
        }
        if (score < 240) {
            return DeathDraftTier.STRONG;
        }
        return DeathDraftTier.EXCEPTIONAL;
    }

    private int rewardTierForDraft(DeathDraftTier tier) {
        switch (tier) {
            case EXCEPTIONAL:
                return 4;
            case STRONG:
                return 3;
            case SOLID:
                return 2;
            case POOR:
            case CATASTROPHIC:
            default:
                return 1;
        }
    }

    private int enhancementCountForTier(DeathDraftTier tier) {
        switch (tier) {
            case EXCEPTIONAL:
                return 4;
            case STRONG:
            case SOLID:
                return 3;
            case POOR:
            case CATASTROPHIC:
            default:
                return 2;
        }
    }

    private List<DeathDraftChoice> buildRewardChoices(MetaProgressionState state, DeathDraftTier tier) {
        int enhancementCount = enhancementCountForTier(tier);
        List<DeathDraftChoice> choices = buildEnhancementChoices(state, enhancementCount, rewardTierForDraft(tier));
        if (state.getActiveCurseIds().isEmpty() || tier.ordinal() < DeathDraftTier.STRONG.ordinal()) {
            return choices;
        }

        int clearCount = tier == DeathDraftTier.EXCEPTIONAL ? 2 : 1;
        List<DeathDraftChoice> purgeChoices = buildCurseClearChoices(state, clearCount);
        if (purgeChoices.isEmpty()) {
            return choices;
        }

        while (!choices.isEmpty() && choices.size() + purgeChoices.size() > enhancementCount) {
            choices.remove(choices.size() - 1);
        }
        choices.addAll(purgeChoices);
        return choices;
    }

    private List<DeathDraftChoice> buildEnhancementChoices(MetaProgressionState state, int count, int maxRewardTier) {
        List<CyberneticEnhancementDefinition> available = new ArrayList<>();
        for (CyberneticEnhancementDefinition definition : enhancementsById.values()) {
            if (!state.getOwnedEnhancementIds().contains(definition.getId()) && definition.getRewardTier() <= maxRewardTier) {
                available.add(definition);
            }
        }
        available.sort(Comparator.comparing(CyberneticEnhancementDefinition::getName, String.CASE_INSENSITIVE_ORDER));
        if (available.isEmpty()) {
            return new ArrayList<>();
        }
        int start = Math.floorMod((state.getDeathCount() + state.getCollapseStreak()) * 2, available.size());
        List<DeathDraftChoice> choices = new ArrayList<>();
        for (int i = 0; i < Math.min(count, available.size()); i++) {
            CyberneticEnhancementDefinition definition = available.get((start + i) % available.size());
            choices.add(new DeathDraftChoice(
                DeathDraftChoice.Kind.ENHANCEMENT,
                definition.getId(),
                definition.getName(),
                definition.getDescription()
            ));
        }
        return choices;
    }

    private List<DeathDraftChoice> buildCurseClearChoices(MetaProgressionState state, int count) {
        List<CurseCardDefinition> active = new ArrayList<>();
        for (String curseId : state.getActiveCurseIds()) {
            CurseCardDefinition definition = cursesById.get(curseId);
            if (definition != null) {
                active.add(definition);
            }
        }
        active.sort(Comparator.comparing(CurseCardDefinition::getName, String.CASE_INSENSITIVE_ORDER));
        if (active.isEmpty()) {
            return new ArrayList<>();
        }
        int start = Math.floorMod(state.getDeathCount() + state.getCollapseStreak(), active.size());
        List<DeathDraftChoice> choices = new ArrayList<>();
        for (int i = 0; i < Math.min(count, active.size()); i++) {
            CurseCardDefinition definition = active.get((start + i) % active.size());
            choices.add(new DeathDraftChoice(
                DeathDraftChoice.Kind.CLEAR_CURSE,
                definition.getId(),
                "Purge " + definition.getName(),
                "Remove this active curse from future runs."
            ));
        }
        return choices;
    }

    private List<DeathDraftChoice> buildCurseChoices(MetaProgressionState state, int count) {
        List<CurseCardDefinition> available = new ArrayList<>();
        for (CurseCardDefinition definition : cursesById.values()) {
            if (!state.getActiveCurseIds().contains(definition.getId())) {
                available.add(definition);
            }
        }
        available.sort(Comparator.comparing(CurseCardDefinition::getName, String.CASE_INSENSITIVE_ORDER));
        int start = available.isEmpty() ? 0 : Math.floorMod((state.getCollapseStreak() + state.getDeathCount()), available.size());
        List<DeathDraftChoice> choices = new ArrayList<>();
        for (int i = 0; i < Math.min(count, available.size()); i++) {
            CurseCardDefinition definition = available.get((start + i) % available.size());
            choices.add(new DeathDraftChoice(
                DeathDraftChoice.Kind.CURSE,
                definition.getId(),
                definition.getName(),
                definition.getDescription()
            ));
        }
        return choices;
    }

    private String buildRewardSummary(DeathDraftTier tier, MetaProgressionState state) {
        if (!state.getActiveCurseIds().isEmpty() && tier.ordinal() >= DeathDraftTier.STRONG.ordinal()) {
            return "Recovery analysis complete. This run was strong enough to draft augments or purge old curse damage.";
        }
        return "Recovery analysis complete. Your next run earns a tiered upgrade draft.";
    }

    private int rankScore(String rank) {
        if (rank == null || rank.isEmpty()) {
            return 0;
        }
        switch (rank) {
            case "S++":
            case "S+++":
                return 96;
            case "S":
                return 84;
            case "A":
                return 72;
            case "B":
                return 56;
            case "C":
                return 42;
            case "D":
                return 28;
            case "E":
                return 18;
            case "F":
                return 10;
            case "G":
            default:
                return 4;
        }
    }

    private void registerDefaults() {
        registerEnhancement(new CyberneticEnhancementDefinition("reinforced_lattice", "Reinforced Lattice",
            "+26 max HP and tougher flesh-steel bracing.", 1, 26f, 0f, 0f, 0f, 2f, 0, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("adrenal_pump", "Adrenal Pump",
            "+5 agility and faster reaction speed.", 1, 0f, 5f, 0f, 0f, 0f, 0, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("siege_tendons", "Siege Tendons",
            "+5 strength and heavier melee output.", 1, 0f, 0f, 5f, 0f, 0f, 0, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("field_reservoir", "Field Reservoir",
            "Start each run with +2 healing potions.", 1, 0f, 0f, 0f, 0f, 0f, 2, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("scavenger_weave", "Scavenger Weave",
            "Harvest nodes yield +1 extra resource.", 2, 0f, 0f, 0f, 0f, 0f, 0, 0L, 1, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("hunter_uplink", "Hunter Uplink",
            "Gain 18% more experience from combat and events.", 2, 0f, 0f, 0f, 0f, 0f, 0, 0L, 0, 1.18f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("scrap_synapse", "Scrap Synapse",
            "Begin each run with extra forge components.", 2, 0f, 0f, 0f, 0f, 0f, 0, 0L, 0, 1f, Map.of("scrap_alloy", 3, "bone_fiber", 2)));
        registerEnhancement(new CyberneticEnhancementDefinition("marrow_plating", "Marrow Plating",
            "+5 stamina for stronger defense and endurance.", 2, 0f, 0f, 0f, 0f, 5f, 0, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("credit_tap", "Credit Tap",
            "Begin each run with 40 extra gold.", 3, 0f, 0f, 0f, 0f, 0f, 0, 40L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("tactician_mesh", "Tactician Mesh",
            "+5 intelligence and improved tactical processing.", 3, 0f, 0f, 0f, 5f, 0f, 0, 0L, 0, 1f, Map.of()));
        registerEnhancement(new CyberneticEnhancementDefinition("overclock_heart", "Overclock Heart",
            "+8 agility, +8 strength, but only shows up after powerful runs.", 4, 0f, 8f, 8f, 0f, 0f, 0, 0L, 0, 1f, Map.of()));

        registerCurse(new CurseCardDefinition("cracked_coolant", "Cracked Coolant",
            "Start each run with 1 fewer potion.", 0f, 0f, 0f, 0f, 0f, -1, 0, 1f));
        registerCurse(new CurseCardDefinition("feedback_tremor", "Feedback Tremor",
            "-3 agility from shaky servos.", 0f, -3f, 0f, 0f, 0f, 0, 0, 1f));
        registerCurse(new CurseCardDefinition("thin_plating", "Thin Plating",
            "-18 max HP from compromised shell plating.", -18f, 0f, 0f, 0f, -1f, 0, 0, 1f));
        registerCurse(new CurseCardDefinition("wasteful_salvage", "Wasteful Salvage",
            "Harvest nodes yield 1 fewer resource.", 0f, 0f, 0f, 0f, 0f, 0, -1, 1f));
        registerCurse(new CurseCardDefinition("fogged_uplink", "Fogged Uplink",
            "Gain 12% less experience.", 0f, 0f, 0f, 0f, 0f, 0, 0, 0.88f));
        registerCurse(new CurseCardDefinition("blunted_motors", "Blunted Motors",
            "-3 strength and -2 stamina.", 0f, 0f, -3f, 0f, -2f, 0, 0, 1f));
    }

    private void registerEnhancement(CyberneticEnhancementDefinition definition) {
        if (definition != null && definition.getId() != null && !definition.getId().isEmpty()) {
            enhancementsById.put(definition.getId(), definition);
        }
    }

    private void registerCurse(CurseCardDefinition definition) {
        if (definition != null && definition.getId() != null && !definition.getId().isEmpty()) {
            cursesById.put(definition.getId(), definition);
        }
    }
}
