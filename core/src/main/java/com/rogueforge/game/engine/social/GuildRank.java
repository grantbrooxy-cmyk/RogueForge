package com.rogueforge.game.engine.social;

/**
 * Named guild rank with a reusable permission set.
 */
public class GuildRank {
    private final String id;
    private final String displayName;
    private final PermissionSet permissionSet;

    public GuildRank(String id, String displayName, PermissionSet permissionSet) {
        this.id = id != null ? id : "";
        this.displayName = displayName != null ? displayName : "";
        this.permissionSet = permissionSet != null ? permissionSet : PermissionSet.none();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PermissionSet getPermissionSet() {
        return permissionSet;
    }
}
