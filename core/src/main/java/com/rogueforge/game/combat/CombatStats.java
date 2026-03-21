package com.rogueforge.game.combat;

/**
 * Represents combat statistics for an entity (attack, defense, etc).
 */
public class CombatStats {
    private float attack;
    private float defense;
    private float hp;
    private float maxHp;

    public CombatStats(float attack, float defense, float maxHp) {
        this.attack = attack;
        this.defense = defense;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public float getAttack() {
        return attack;
    }

    public void setAttack(float attack) {
        this.attack = attack;
    }

    public float getDefense() {
        return defense;
    }

    public void setDefense(float defense) {
        this.defense = defense;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = Math.max(0, Math.min(hp, maxHp));
    }

    public float getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(float maxHp) {
        this.maxHp = maxHp;
        if (this.hp > maxHp) {
            this.hp = maxHp;
        }
    }

    public void takeDamage(float damage) {
        setHp(hp - damage);
    }

    public void heal(float amount) {
        setHp(hp + amount);
    }

    public boolean isAlive() {
        return hp > 0;
    }
}
