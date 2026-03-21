package com.rogueforge.game.event;

import com.rogueforge.game.combat.AbilityResult;

/**
 * Event fired when an ability is used during combat.
 * Contains information about which ability was used and the result.
 */
public class AbilityUsedEvent {

    private String abilityId;
    private String casterName;
    private AbilityResult result;

    /**
     * Constructor for ability used event
     *
     * @param abilityId The ID of the ability that was used
     * @param casterName Name of the entity using the ability
     * @param result The result of the ability execution
     */
    public AbilityUsedEvent(String abilityId, String casterName, AbilityResult result) {
        this.abilityId = abilityId;
        this.casterName = casterName;
        this.result = result;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public String getCasterName() {
        return casterName;
    }

    public AbilityResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "AbilityUsedEvent{" +
                "abilityId='" + abilityId + '\'' +
                ", casterName='" + casterName + '\'' +
                ", result=" + result +
                '}';
    }
}
