package com.rogueforge.game.combat;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.core.EventHandler;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.economy.RankMultiplier;
import com.rogueforge.game.event.CurrencyEarnedEvent;
import com.rogueforge.game.event.EquipmentDropEvent;
import com.rogueforge.game.event.EntityKilledEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves loot drops and currency rewards when entities are killed.
 * Subscribes to EntityKilledEvent via EventBus.
 */
public class LootResolver {
    private EventBus eventBus;
    private List<EquipmentItem> equipmentPool;
    private static final float EQUIPMENT_DROP_CHANCE = 0.1f; // 10% chance

    public LootResolver(EventBus eventBus) {
        this.eventBus = eventBus;
        this.equipmentPool = new ArrayList<>();
        // Subscribe to kill events
        eventBus.subscribe(this);
    }

    /**
     * Sets the pool of available equipment items for drops.
     *
     * @param pool The list of equipment items to draw from
     */
    public void setEquipmentPool(List<EquipmentItem> pool) {
        this.equipmentPool = pool != null ? pool : new ArrayList<>();
    }

    /**
     * Handles entity kill events and calculates loot rewards.
     * Called by EventBus when EntityKilledEvent is fired.
     *
     * @param event The entity killed event containing the dead entity
     */
    @EventHandler
    public void onEntityKilledEvent(EntityKilledEvent event) {
        Object entity = event.getEntity();

        // Only process MonsterDefinition entities
        if (!(entity instanceof MonsterDefinition)) {
            return;
        }

        MonsterDefinition monsterDefinition = (MonsterDefinition) entity;

        // Get base loot value from monster
        long baseLoot = monsterDefinition.getBaseLoot();

        // Get rank multiplier from the rank string
        String rankStr = monsterDefinition.getRank();
        RankMultiplier rank = RankMultiplier.fromString(rankStr);
        if (rank == null) {
            rank = RankMultiplier.G; // Default to G if rank is invalid
        }
        int multiplier = rank.getMultiplier();

        // Calculate total currency reward
        long totalCurrency = baseLoot * multiplier;

        // Fire currency earned event
        CurrencyEarnedEvent currencyEvent = new CurrencyEarnedEvent(totalCurrency, monsterDefinition.getName());
        eventBus.fire(currencyEvent);

        // Check for equipment drops
        rollEquipmentDrop(monsterDefinition);
    }

    /**
     * Attempts to roll for an equipment drop based on monster properties.
     *
     * @param monsterDefinition The killed monster
     */
    private void rollEquipmentDrop(com.rogueforge.game.data.MonsterDefinition monsterDefinition) {
        // Roll random chance for equipment drop
        if (Math.random() >= EQUIPMENT_DROP_CHANCE) {
            return; // No drop this time
        }

        // Get the monster's rank to determine eligible equipment tiers
        String rankStr = monsterDefinition.getRank();
        RankMultiplier rank = RankMultiplier.fromString(rankStr);
        if (rank == null) {
            rank = RankMultiplier.G;
        }

        // Filter equipment pool by tier based on monster rank
        List<EquipmentItem> eligibleEquipment = new ArrayList<>();
        for (EquipmentItem item : equipmentPool) {
            if (isEligibleForRank(item.getTier(), rank)) {
                eligibleEquipment.add(item);
            }
        }

        // If no eligible equipment, no drop
        if (eligibleEquipment.isEmpty()) {
            return;
        }

        // Select random item from eligible equipment
        int randomIndex = (int) (Math.random() * eligibleEquipment.size());
        EquipmentItem droppedItem = eligibleEquipment.get(randomIndex);

        // Fire equipment drop event
        EquipmentDropEvent event = new EquipmentDropEvent(droppedItem, monsterDefinition.getName());
        eventBus.fire(event);
    }

    /**
     * Determines if an equipment tier is eligible for drops from a given monster rank.
     *
     * @param tier The equipment tier (1-3)
     * @param rank The monster's rank
     * @return true if the tier is eligible for this rank
     */
    private boolean isEligibleForRank(int tier, RankMultiplier rank) {
        switch (rank) {
            case G:
            case F:
                return tier <= 1;
            case E:
            case D:
                return tier <= 2;
            case C:
            case B:
            case A:
                return tier <= 3;
            case S:
            case S_PLUS:
            case S_PLUS_PLUS:
            case S_PLUS_PLUS_PLUS:
                return true; // All tiers for S and above
            default:
                return tier <= 1;
        }
    }

    /**
     * Gets the currency multiplier for a given monster rank.
     *
     * @param rank The monster's rank
     * @return The currency multiplier
     */
    public int getCurrencyMultiplier(RankMultiplier rank) {
        return rank.getMultiplier();
    }
}
