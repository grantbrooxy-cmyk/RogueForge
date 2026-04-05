package com.rogueforge.game.engine.social;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Small reusable wrapper around permission actions.
 */
public class PermissionSet {
    private final EnumSet<PermissionAction> allowedActions;

    public PermissionSet() {
        this.allowedActions = EnumSet.noneOf(PermissionAction.class);
    }

    public PermissionSet(Set<PermissionAction> actions) {
        this();
        if (actions != null) {
            this.allowedActions.addAll(actions);
        }
    }

    public static PermissionSet none() {
        return new PermissionSet();
    }

    public static PermissionSet of(PermissionAction... actions) {
        PermissionSet set = new PermissionSet();
        if (actions != null) {
            set.allowedActions.addAll(Arrays.asList(actions));
        }
        return set;
    }

    public static PermissionSet all() {
        return new PermissionSet(EnumSet.allOf(PermissionAction.class));
    }

    public boolean allows(PermissionAction action) {
        return action != null && allowedActions.contains(action);
    }

    public Set<PermissionAction> getAllowedActions() {
        return EnumSet.copyOf(allowedActions);
    }
}
