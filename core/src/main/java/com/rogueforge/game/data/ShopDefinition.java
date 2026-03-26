package com.rogueforge.game.data;

/**
 * Data definition for a shop inventory loaded from JSON.
 */
public class ShopDefinition {
    private String id;
    private String zoneId;
    private String vendorName;
    private String locationLabel;
    private ShopEntryDefinition[] entries;

    public String getId() {
        return id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getLocationLabel() {
        return locationLabel;
    }

    public ShopEntryDefinition[] getEntries() {
        return entries;
    }
}
