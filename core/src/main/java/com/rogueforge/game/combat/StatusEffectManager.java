package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active statuses on a combatant and resolves turn-based effects.
 */
public class StatusEffectManager {
    private final List<ActiveStatusEffect> activeEffects = new ArrayList<>();

    public void apply(StatusEffectType type, int turns) {
        apply(type, turns, 0f);
    }

    public void apply(StatusEffectType type, int turns, float magnitude) {
        ActiveStatusEffect existing = get(type);
        if (existing != null) {
            existing.refresh(turns);
            return;
        }
        activeEffects.add(new ActiveStatusEffect(type, turns, magnitude));
    }

    public boolean has(StatusEffectType type) {
        return get(type) != null;
    }

    public ActiveStatusEffect get(StatusEffectType type) {
        for (ActiveStatusEffect effect : activeEffects) {
            if (effect.getType() == type) {
                return effect;
            }
        }
        return null;
    }

    public void remove(StatusEffectType type) {
        activeEffects.removeIf(effect -> effect.getType() == type);
    }

    public List<ActiveStatusEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }

    public List<String> beginTurn(BattleCombatant combatant) {
        List<String> log = new ArrayList<>();
        if (combatant == null || !combatant.isAlive()) {
            return log;
        }

        if (has(StatusEffectType.BURN)) {
            int damage = Math.max(1, Math.round(combatant.getMaxHealth() * 0.05f));
            combatant.applyDirectDamage(damage);
            log.add(combatant.getName() + " is scorched for " + damage + " damage.");
        }
        if (has(StatusEffectType.POISON)) {
            int damage = Math.max(1, Math.round(combatant.getMaxHealth() * 0.08f));
            combatant.applyDirectDamage(damage);
            log.add(combatant.getName() + " suffers " + damage + " poison damage.");
        }
        if (has(StatusEffectType.REGEN)) {
            int healing = Math.max(1, Math.round(combatant.getMaxHealth() * 0.05f));
            combatant.heal(healing);
            log.add(combatant.getName() + " regenerates " + healing + " HP.");
        }
        return log;
    }

    public List<String> onActionTaken(BattleCombatant combatant) {
        List<String> log = new ArrayList<>();
        if (combatant == null || !combatant.isAlive()) {
            return log;
        }
        if (has(StatusEffectType.BLEED)) {
            int damage = Math.max(1, Math.round(combatant.getMaxHealth() * 0.03f));
            combatant.applyDirectDamage(damage);
            log.add(combatant.getName() + " bleeds for " + damage + " damage.");
        }
        return log;
    }

    public List<String> endTurn(BattleCombatant combatant) {
        List<String> log = new ArrayList<>();
        Iterator<ActiveStatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            ActiveStatusEffect effect = iterator.next();
            if (effect.tickDown()) {
                iterator.remove();
                log.add(combatant.getName() + " is no longer affected by " + prettify(effect.getType()) + ".");
            }
        }
        return log;
    }

    public float getSpeedMultiplier() {
        float multiplier = 1f;
        if (has(StatusEffectType.SLOW)) {
            multiplier *= 0.5f;
        }
        if (has(StatusEffectType.HASTE)) {
            multiplier *= 1.5f;
        }
        return multiplier;
    }

    public float getPhysicalDamageTakenMultiplier() {
        float multiplier = 1f;
        if (has(StatusEffectType.DEFENDING)) {
            multiplier *= 0.5f;
        }
        if (has(StatusEffectType.PROTECT)) {
            multiplier *= 0.7f;
        }
        if (has(StatusEffectType.FREEZE)) {
            multiplier *= 1.5f;
        }
        return multiplier;
    }

    public float getAbilityDamageTakenMultiplier() {
        float multiplier = has(StatusEffectType.DEFENDING) ? 0.75f : 1f;
        if (has(StatusEffectType.SHELL)) {
            multiplier *= 0.7f;
        }
        return multiplier;
    }

    public boolean shouldSkipTurn() {
        if (has(StatusEffectType.STUN) || has(StatusEffectType.FREEZE)) {
            return true;
        }
        return has(StatusEffectType.PARALYZE) && Math.random() < 0.3f;
    }

    public boolean canUseAbilities() {
        return !has(StatusEffectType.SILENCE);
    }

    public float getPhysicalDamageDealtMultiplier() {
        float multiplier = 1f;
        if (has(StatusEffectType.WEAKEN)) {
            multiplier *= 0.75f;
        }
        if (has(StatusEffectType.BERSERK)) {
            multiplier *= 1.5f;
        }
        return multiplier;
    }

    public float getPhysicalHitChanceMultiplier() {
        return has(StatusEffectType.BLIND) ? 0.5f : 1f;
    }

    private String prettify(StatusEffectType type) {
        String lower = type.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
