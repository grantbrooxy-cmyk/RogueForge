package com.rogueforge.game.economy;

import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.ZoneDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages shop inventory and purchases.
 * Items are populated based on zone rank and can be restocked.
 */
public class ShopInventory {
    private List<ShopEntry> items;

    public ShopInventory() {
        this.items = new ArrayList<>();
    }

    /**
     * Loads shop inventory for a specific zone.
     * Item selection is based on the zone's rank range.
     *
     * @param zoneDefinition The zone to load inventory for
     */
    public void loadForZone(ZoneDefinition zoneDefinition) {
        items.clear();

        // TODO: Implement zone-based shop loading
        // - Query available equipment for zone rank range
        // - Calculate prices based on rank and base cost
        // - Populate items list with ShopEntry objects

        // Example placeholder:
        // RankMultiplier minRank = RankMultiplier.G;
        // RankMultiplier maxRank = zoneDefinition.getMaxRank();
        // List<EquipmentItem> availableEquipment = getEquipmentForRankRange(minRank, maxRank);
        // for (EquipmentItem equipment : availableEquipment) {
        //     items.add(new ShopEntry(equipment, calculatePrice(equipment)));
        // }
    }

    /**
     * Purchases an item from the shop using the provided wallet.
     *
     * @param entry The shop entry to purchase
     * @param wallet The wallet to deduct currency from
     * @return The purchased equipment item
     * @throws IllegalArgumentException if wallet cannot afford the item
     */
    public EquipmentItem purchase(ShopEntry entry, CurrencyWallet wallet) {
        if (entry == null) {
            throw new IllegalArgumentException("Shop entry cannot be null");
        }

        long cost = entry.getCost();

        if (!wallet.canAfford(cost)) {
            throw new IllegalArgumentException(
                String.format("Cannot afford item: need %d, have %d", cost, wallet.getBalance())
            );
        }

        wallet.spend(cost);
        items.remove(entry);
        return entry.getEquipment();
    }

    public void addEquipmentItem(EquipmentItem equipment, long cost) {
        if (equipment == null) {
            return;
        }
        items.add(ShopEntry.forEquipment(equipment, cost));
    }

    public void addHealingItem(String label, int healingPotions, long cost) {
        items.add(ShopEntry.forHealing(label, healingPotions, cost));
    }

    /**
     * Gets all currently available items in the shop.
     *
     * @return A copy of the available items list
     */
    public List<ShopEntry> getAvailableItems() {
        return new ArrayList<>(items);
    }

    /**
     * Gets a specific item by index.
     *
     * @param index The item index
     * @return The shop entry at that index
     */
    public ShopEntry getItem(int index) {
        if (index < 0 || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }

    /**
     * Gets the number of items currently in stock.
     *
     * @return The number of items
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Restocks the shop inventory.
     * Regenerates items for the current zone.
     */
    public void restock() {
        // TODO: Implement restocking logic
        // - Clear current items
        // - Reload inventory for the current zone
        // - Generate new prices/variants
    }

    /**
     * Inner class representing a shop entry (item + cost).
     */
    public static class ShopEntry {
        private EquipmentItem equipment;
        private String label;
        private int healingPotions;
        private boolean consumable;
        private long cost;

        public ShopEntry(EquipmentItem equipment, String label, int healingPotions, boolean consumable, long cost) {
            this.equipment = equipment;
            this.label = label;
            this.healingPotions = healingPotions;
            this.consumable = consumable;
            this.cost = cost;
        }

        public static ShopEntry forEquipment(EquipmentItem equipment, long cost) {
            return new ShopEntry(equipment, equipment.getName(), 0, false, cost);
        }

        public static ShopEntry forHealing(String label, int healingPotions, long cost) {
            return new ShopEntry(null, label, healingPotions, true, cost);
        }

        public EquipmentItem getEquipment() {
            return equipment;
        }

        public String getLabel() {
            return label;
        }

        public int getHealingPotions() {
            return healingPotions;
        }

        public boolean isConsumable() {
            return consumable;
        }

        public long getCost() {
            return cost;
        }

        @Override
        public String toString() {
            return String.format("%s - %d gold", label, cost);
        }
    }

}
