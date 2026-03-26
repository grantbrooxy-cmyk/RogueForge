package com.rogueforge.game.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active statuses on a combatant and resolves turn-based effects.
 *
 * All 15 game-design statuses are mechanically active. Quick reference:
 *
 *  Damage-over-time (beginTurn):
 *    BURN     — 5 % max HP fire damage per turn
 *    POISON   — 8 % max HP poison damage per turn
 *    BLEED    — 3 % max HP on action taken (onActionTaken)
 *
 *  Healing-over-time (beginTurn):
 *    REGEN    — 5 % max HP healing per turn
 *
 *  Speed modifiers (getSpeedMultiplier → TurnTimeline delay):
 *    SLOW     — 0.5× speed → double tick delay → acts half as often
 *    HASTE    — 1.5× speed → two-thirds tick delay → acts 50 % more often
 *
 *  Turn-skip control (shouldSkipTurn → battle screen skips the turn):
 *    STUN     — always skips turn
 *    FREEZE   — always skips turn, also +50 % physical damage taken
 *    PARALYZE — 30 % chance to skip turn
 *
 *  Damage-reduction buffs (getPhysicalDamageTakenMultiplier /
 *                          getAbilityDamageTakenMultiplier):
 *    DEFENDING — −50 % physical, −25 % ability (set by Defend command)
 *    PROTECT   — −30 % physical
 *    SHELL     — −30 % ability
 *
 *  Attacker modifiers:
 *    BLIND    — 50 % physical hit chance (getPhysicalHitChanceMultiplier;
 *               miss returns 0 from CombatResolver.resolvePhysicalDamage)
 *    WEAKEN   — −25 % physical damage dealt (getPhysicalDamageDealtMultiplier)
 *    BERSERK  — +50 % physical damage dealt; forces Attack-only commands
 *               (canAttackOnly — battle screen enforces the restriction)
 *
 *  Command-block:
 *    SILENCE  — blocks Ability command (canUseAbilities returns false;
 *               battle screen must check before showing Ability menu)
 *
 *  Also tracked: TAUNT (forces enemies to target the taunting unit;
 *                enforced by enemy AI in the battle screen, not here).
 *  DEFENDING is cleared at the start of the actor's next turn by the
 *  normal tick-down in endTurn().
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

    /**
     * Applies all begin-of-turn effects (DoTs, HoTs) and returns log lines.
     *
     * Status coverage:
     *   BURN    — 5 % max HP fire damage
     *   POISON  — 8 % max HP poison damage
     *   REGEN   — 5 % max HP healing
     *   (BLEED fires in onActionTaken, not here)
     */
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
        if (combatant.hasUniqueBoost("AUTO_REPAIR")) {
            int healing = Math.max(1, Math.round(combatant.getMaxHealth() * 0.06f));
            combatant.heal(healing);
            log.add(combatant.getName() + "'s auto-repair restores " + healing + " HP.");
        }

        // BERSERK reminder: command restriction is enforced by canAttackOnly() —
        // the battle screen must check that before building its command menu.
        if (has(StatusEffectType.BERSERK)) {
            log.add(combatant.getName() + " is berserk and can only attack!");
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

    /**
     * BLIND: physical attacks (Attack command, physical-type abilities) have
     * a 50 % chance to miss. CombatResolver.resolvePhysicalDamage() checks
     * this and returns 0 on a miss. The caller should log "{name} missed!"
     */
    public float getPhysicalHitChanceMultiplier() {
        return has(StatusEffectType.BLIND) ? 0.5f : 1f;
    }

    /**
     * BERSERK command lock: when true the battle screen must restrict the
     * actor's command menu to Attack only. Berserk boosts physical output
     * (handled by getPhysicalDamageDealtMultiplier) but removes tactical choice.
     *
     * SILENCE is the ability-blocking equivalent — use canUseAbilities().
     */
    public boolean canAttackOnly() {
        return has(StatusEffectType.BERSERK);
    }

    private String prettify(StatusEffectType type) {
        String lower = type.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
