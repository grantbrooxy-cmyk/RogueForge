package com.rogueforge.game.engine.world;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Resolves world-feature interactions from abilities and lightweight crew
 * proficiencies instead of hard-coded screen logic.
 */
public class EnvironmentalInteractionSystem {

    public InteractionResolution evaluate(TmxWorldLoader.Feature feature, InteractionProfile profile) {
        if (feature == null || feature.interactionType == null || feature.interactionType.isEmpty()) {
            return InteractionResolution.blocked("Interact", "There is nothing useful to do here.");
        }
        String interactionType = feature.interactionType.trim().toLowerCase(Locale.ROOT);
        switch (interactionType) {
            case "scan_hidden_path":
                return resolveAbilityOrProficiency(
                    profile,
                    "Scan",
                    "analysis",
                    1,
                    "scan",
                    "deep_scan"
                );
            case "burn_barrier":
                return resolveAbilityOrProficiency(
                    profile,
                    "Burn Away",
                    "demolition",
                    1,
                    "rapid_fire",
                    "storm_barrage",
                    "limit_breaker"
                );
            case "strength_boulder":
                return resolveAbilityOrProficiency(
                    profile,
                    "Clear",
                    "labor",
                    1,
                    "power_strike",
                    "seismic_break",
                    "limit_breaker",
                    "shield_wall",
                    "bulwark_matrix"
                );
            case "cut_vines":
                return resolveAbilityOrProficiency(
                    profile,
                    "Cut Through",
                    "field_ops",
                    1,
                    "power_strike",
                    "rapid_fire",
                    "limit_breaker"
                );
            case "mine_ore":
                return resolveAbilityOrProficiency(
                    profile,
                    "Mine",
                    "mining",
                    1,
                    "power_strike",
                    "seismic_break",
                    "limit_breaker"
                ).withYieldBonus(lookupProficiencyLevel(profile, "mining") / 2);
            case "hack_ruins":
                return resolveAbilityOrProficiency(
                    profile,
                    "Hack",
                    "hacking",
                    1,
                    "scan",
                    "deep_scan",
                    "bulwark_matrix"
                );
            case "harvest_resource":
                return resolveHarvest(feature, profile);
            default:
                return InteractionResolution.blocked("Interact", "Your crew does not have a protocol for this obstacle yet.");
        }
    }

    public String getActionLabel(TmxWorldLoader.Feature feature, InteractionProfile profile) {
        return evaluate(feature, profile).getActionLabel();
    }

    public boolean canInteract(TmxWorldLoader.Feature feature, InteractionProfile profile) {
        return evaluate(feature, profile).canInteract();
    }

    private InteractionResolution resolveHarvest(TmxWorldLoader.Feature feature, InteractionProfile profile) {
        String resourceId = feature != null && feature.resourceId != null ? feature.resourceId.toLowerCase(Locale.ROOT) : "";
        if (resourceId.contains("ore") || resourceId.contains("crystal")) {
            return resolveAbilityOrProficiency(profile, "Mine", "mining", 1,
                "power_strike", "seismic_break", "limit_breaker")
                .withYieldBonus(1 + (lookupProficiencyLevel(profile, "mining") / 2));
        }
        if (resourceId.contains("data") || resourceId.contains("relic")) {
            return resolveAbilityOrProficiency(profile, "Hack", "hacking", 1,
                "scan", "deep_scan", "bulwark_matrix")
                .withYieldBonus(lookupProficiencyLevel(profile, "hacking") / 2);
        }
        return resolveAbilityOrProficiency(profile, "Harvest", "field_ops", 1,
            "rapid_fire", "power_strike", "limit_breaker")
            .withYieldBonus(lookupProficiencyLevel(profile, "field_ops") / 2);
    }

    private InteractionResolution resolveAbilityOrProficiency(InteractionProfile profile, String actionLabel,
                                                              String proficiencyId, int requiredLevel,
                                                              String... abilityIds) {
        if (hasAnyAbility(profile, abilityIds) || lookupProficiencyLevel(profile, proficiencyId) >= requiredLevel) {
            return InteractionResolution.allowed(actionLabel, 0);
        }
        String blockedMessage = "Requires " + formatProficiencyName(proficiencyId) + " proficiency Lv."
            + requiredLevel + " or the right field ability.";
        return InteractionResolution.blocked(actionLabel, blockedMessage);
    }

    private boolean hasAnyAbility(InteractionProfile profile, String... abilityIds) {
        if (profile == null || abilityIds == null) {
            return false;
        }
        for (String abilityId : abilityIds) {
            if (abilityId != null && profile.abilityIds.contains(abilityId)) {
                return true;
            }
        }
        return false;
    }

    private int lookupProficiencyLevel(InteractionProfile profile, String proficiencyId) {
        if (profile == null || proficiencyId == null || proficiencyId.isEmpty()) {
            return 0;
        }
        Integer level = profile.proficiencyLevels.get(proficiencyId);
        return level != null ? Math.max(0, level) : 0;
    }

    private String formatProficiencyName(String proficiencyId) {
        if (proficiencyId == null || proficiencyId.isEmpty()) {
            return "crew";
        }
        return proficiencyId.replace('_', ' ');
    }

    public static final class InteractionProfile {
        private final Set<String> abilityIds;
        private final Map<String, Integer> proficiencyLevels;

        public InteractionProfile(Set<String> abilityIds, Map<String, Integer> proficiencyLevels) {
            this.abilityIds = abilityIds != null ? new LinkedHashSet<>(abilityIds) : Collections.emptySet();
            this.proficiencyLevels = proficiencyLevels != null ? proficiencyLevels : Collections.emptyMap();
        }
    }

    public static final class InteractionResolution {
        private final boolean canInteract;
        private final String actionLabel;
        private final String blockedMessage;
        private final int yieldBonus;

        private InteractionResolution(boolean canInteract, String actionLabel, String blockedMessage, int yieldBonus) {
            this.canInteract = canInteract;
            this.actionLabel = actionLabel;
            this.blockedMessage = blockedMessage;
            this.yieldBonus = Math.max(0, yieldBonus);
        }

        public static InteractionResolution allowed(String actionLabel, int yieldBonus) {
            return new InteractionResolution(true, actionLabel, null, yieldBonus);
        }

        public static InteractionResolution blocked(String actionLabel, String blockedMessage) {
            return new InteractionResolution(false, actionLabel, blockedMessage, 0);
        }

        public boolean canInteract() {
            return canInteract;
        }

        public String getActionLabel() {
            return actionLabel;
        }

        public String getBlockedMessage() {
            return blockedMessage;
        }

        public int getYieldBonus() {
            return yieldBonus;
        }

        public InteractionResolution withYieldBonus(int yieldBonus) {
            return new InteractionResolution(canInteract, actionLabel, blockedMessage, yieldBonus);
        }
    }
}
