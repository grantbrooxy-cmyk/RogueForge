package com.rogueforge.game.world;

import java.util.Map;

/**
 * Centralized progression gating for dangerous zones so expedition-board travel
 * and direct world doors enforce the same access rules.
 */
public final class ZoneAccessPolicy {

    private ZoneAccessPolicy() {
    }

    public static AccessDecision evaluate(
        String zoneId,
        int playerLevel,
        String unlockedGrade,
        int forgeCoreLevel,
        Map<String, Boolean> worldFlags,
        Map<String, String> questStates
    ) {
        if (zoneId == null || zoneId.isEmpty() || "town".equals(zoneId) || "verdant_fields".equals(zoneId)) {
            return AccessDecision.allowed();
        }

        if ("whispering_forest".equals(zoneId)) {
            return requireLevel(zoneId, playerLevel, 10, "Whispering Forest opens once your crew reaches level 10.");
        }
        if ("rusty_quarry".equals(zoneId) || "coastal_shallows".equals(zoneId)) {
            return requireLevel(zoneId, playerLevel, 20, "This frontier route is gated until your crew reaches level 20.");
        }
        if ("shadow_caves".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "access.workshop_pass")) {
                return AccessDecision.blocked("Shadow Caves remains sealed until Mira issues the Workshop Pass.");
            }
            return requireLevel(zoneId, playerLevel, 20, "Shadow Caves is too lethal before level 20.");
        }
        if ("dragon_peak".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.peak_lift_unlocked")) {
                return AccessDecision.blocked("Dragon Peak is locked until the Peak Sigil route is restored.");
            }
            return requireLevel(zoneId, playerLevel, 35, "Dragon Peak is gated until your crew reaches level 35.");
        }
        if ("scorched_plateau".equals(zoneId) || "frozen_vale".equals(zoneId) || "crystal_depths".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.warding_pylons_online")) {
                return AccessDecision.blocked("These advanced frontier zones stay closed until the Sun Core restores the warding pylons.");
            }
            return requireLevel(zoneId, playerLevel, 40, "This route is gated until your crew reaches level 40.");
        }
        if ("sunken_abyss".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.abyss_signal_found")
                && !"speak_maren".equals(getQuestState(questStates, "abyss_archive"))) {
                return AccessDecision.blocked("Sunken Abyss opens only after the deep-signal route has been confirmed.");
            }
            return requireLevel(zoneId, playerLevel, 50, "Sunken Abyss is gated until your crew reaches level 50.");
        }
        if ("sky_fortress".equals(zoneId) || "clockwork_sanctum".equals(zoneId) || "volcanic_core".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.sky_route_mapped")) {
                return AccessDecision.blocked("This route opens only after the Archive Chart maps the sky approach.");
            }
            return requireLevel(zoneId, playerLevel, 55, "This route is gated until your crew reaches level 55.");
        }
        if ("abyssal_rift".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.command_rails_online")) {
                return AccessDecision.blocked("Abyssal Rift stays sealed until the command rails are back online.");
            }
            return requireLevel(zoneId, playerLevel, 65, "Abyssal Rift is gated until your crew reaches level 65.");
        }
        if ("the_void".equals(zoneId)) {
            if (!isFlagActive(worldFlags, "frontier.void_gate_stable")) {
                return AccessDecision.blocked("The Void remains inaccessible until the Void Gate is stabilized.");
            }
            return requireLevel(zoneId, playerLevel, 75, "The Void is gated until your crew reaches level 75.");
        }
        if ("infinite_dungeon".equals(zoneId)) {
            if (forgeCoreLevel < 4) {
                return AccessDecision.blocked("The Infinite Dungeon stays locked until Forge Core Lv4 comes online.");
            }
            return requireLevel(zoneId, playerLevel, 60, "The Infinite Dungeon is gated until your crew reaches level 60.");
        }

        return AccessDecision.allowed();
    }

    private static AccessDecision requireLevel(String zoneId, int playerLevel, int requiredLevel, String blockedReason) {
        if (playerLevel < requiredLevel) {
            return AccessDecision.blocked(blockedReason);
        }
        return AccessDecision.allowed();
    }

    private static boolean isFlagActive(Map<String, Boolean> worldFlags, String flag) {
        return worldFlags != null && Boolean.TRUE.equals(worldFlags.get(flag));
    }

    private static String getQuestState(Map<String, String> questStates, String questId) {
        if (questStates == null || questId == null || questId.isEmpty()) {
            return QuestManager.NOT_STARTED;
        }
        return questStates.getOrDefault(questId, QuestManager.NOT_STARTED);
    }

    public static final class AccessDecision {
        private final boolean allowed;
        private final String blockedReason;

        private AccessDecision(boolean allowed, String blockedReason) {
            this.allowed = allowed;
            this.blockedReason = blockedReason != null ? blockedReason : "";
        }

        public static AccessDecision allowed() {
            return new AccessDecision(true, "");
        }

        public static AccessDecision blocked(String blockedReason) {
            return new AccessDecision(false, blockedReason);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getBlockedReason() {
            return blockedReason;
        }
    }
}
