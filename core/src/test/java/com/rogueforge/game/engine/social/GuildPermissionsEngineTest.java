package com.rogueforge.game.engine.social;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuildPermissionsEngineTest {

    @Test
    void defaultGuildMasterCanPerformManagementActions() {
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "player_founder");
        GuildPermissionsEngine engine = new GuildPermissionsEngine();

        assertTrue(engine.canPerform(guild, "player_founder", PermissionAction.MANAGE_RANKS));
        assertTrue(engine.canPerform(guild, "player_founder", PermissionAction.CREATE_NPCS));
        assertTrue(engine.canPerform(guild, "player_founder", PermissionAction.CLAIM_LAND));
    }

    @Test
    void architectCanBuildButCannotManageMembers() {
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "player_founder");
        guild.upsertMembership(new GuildMembership("player_architect", GuildDefinition.RANK_ARCHITECT, true));
        GuildPermissionsEngine engine = new GuildPermissionsEngine();

        assertTrue(engine.canPerform(guild, "player_architect", PermissionAction.BUILD));
        assertTrue(engine.canPerform(guild, "player_architect", PermissionAction.EDIT_STRUCTURES));
        assertFalse(engine.canPerform(guild, "player_architect", PermissionAction.MANAGE_MEMBERS));
    }

    @Test
    void quartermasterCanManageStorageButNotCreateNpcs() {
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "player_founder");
        guild.upsertMembership(new GuildMembership("player_quartermaster", GuildDefinition.RANK_QUARTERMASTER, true));
        GuildPermissionsEngine engine = new GuildPermissionsEngine();

        assertTrue(engine.canPerform(guild, "player_quartermaster", PermissionAction.MANAGE_STORAGE));
        assertFalse(engine.canPerform(guild, "player_quartermaster", PermissionAction.CREATE_NPCS));
    }

    @Test
    void inactiveOrUnknownMembersAreDenied() {
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "player_founder");
        guild.upsertMembership(new GuildMembership("player_inactive", GuildDefinition.RANK_OFFICER, false));
        GuildPermissionsEngine engine = new GuildPermissionsEngine();

        assertFalse(engine.canPerform(guild, "player_unknown", PermissionAction.POST_QUESTS));
        assertFalse(engine.canPerform(guild, "player_inactive", PermissionAction.POST_QUESTS));
    }

    @Test
    void ownershipRecordSupportsPersonalAndGuildChecks() {
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks("guild_iron", "Iron Vanguard", "player_founder");
        guild.upsertMembership(new GuildMembership("player_architect", GuildDefinition.RANK_ARCHITECT, true));
        GuildPermissionsEngine engine = new GuildPermissionsEngine();

        OwnershipRecord personalRecord = new OwnershipRecord(
            OwnershipScope.PERSONAL,
            "player_builder",
            "",
            "",
            false,
            Set.of("player_editor")
        );
        OwnershipRecord guildRecord = new OwnershipRecord(
            OwnershipScope.GUILD,
            "",
            "guild_iron",
            "",
            false,
            Set.of()
        );

        assertTrue(engine.canActOnRecord(personalRecord, guild, "player_builder", PermissionAction.EDIT_STRUCTURES));
        assertTrue(engine.canActOnRecord(personalRecord, guild, "player_editor", PermissionAction.EDIT_STRUCTURES));
        assertFalse(engine.canActOnRecord(personalRecord, guild, "player_other", PermissionAction.EDIT_STRUCTURES));
        assertTrue(engine.canActOnRecord(guildRecord, guild, "player_architect", PermissionAction.BUILD));
        assertFalse(engine.canActOnRecord(guildRecord, guild, "player_architect", PermissionAction.MANAGE_MEMBERS));
    }
}
