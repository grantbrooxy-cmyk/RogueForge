package com.rogueforge.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.support.GdxTestSupport;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ShopCoverageDataTest {

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void everyZoneHasAtLeastOneShopWithResolvableEntries() {
        ZoneDefinition[] zones = new Json().fromJson(
            ZoneDefinition[].class,
            Gdx.files.internal("data/zones.json").readString()
        );
        ShopDefinition[] shops = new Json().fromJson(
            ShopDefinition[].class,
            Gdx.files.internal("data/shop_inventories.json").readString()
        );
        EquipmentItem[] items = new Json().fromJson(
            EquipmentItem[].class,
            Gdx.files.internal("data/equipment.json").readString()
        );

        assertNotNull(zones);
        assertNotNull(shops);
        assertNotNull(items);
        assertTrue(shops.length >= zones.length, "Expected at least one shop per zone.");

        Set<String> zoneIds = Arrays.stream(zones).map(ZoneDefinition::getId).collect(Collectors.toSet());
        Set<String> shopZoneIds = Arrays.stream(shops)
            .map(ShopDefinition::getZoneId)
            .filter(zoneId -> zoneId != null && !zoneId.isEmpty())
            .collect(Collectors.toSet());
        Set<String> equipmentIds = Arrays.stream(items).map(EquipmentItem::getId).collect(Collectors.toSet());

        assertEquals(zoneIds, shopZoneIds, "Every zone should have a mapped shop definition.");

        for (ShopDefinition shop : shops) {
            assertNotNull(shop.getId(), "Shop id should be present.");
            assertTrue(zoneIds.contains(shop.getZoneId()), "Shop " + shop.getId() + " targets an unknown zone.");
            assertNotNull(shop.getEntries(), "Shop " + shop.getId() + " should have entries.");
            assertTrue(shop.getEntries().length >= 3, "Shop " + shop.getId() + " should stock at least 3 entries.");

            boolean hasHealing = false;
            for (ShopEntryDefinition entry : shop.getEntries()) {
                assertNotNull(entry.getType(), "Shop " + shop.getId() + " has an entry without a type.");
                if ("healing".equals(entry.getType())) {
                    hasHealing = true;
                    assertTrue(entry.getQuantity() > 0, "Healing entry in " + shop.getId() + " should restore supplies.");
                } else if ("equipment".equals(entry.getType())) {
                    assertTrue(
                        equipmentIds.contains(entry.getItemId()),
                        "Shop " + shop.getId() + " references missing equipment " + entry.getItemId()
                    );
                } else {
                    fail("Unsupported shop entry type in " + shop.getId() + ": " + entry.getType());
                }
            }
            assertTrue(hasHealing, "Shop " + shop.getId() + " should stock at least one healing item.");
        }
    }
}
