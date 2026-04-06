package com.rogueforge.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.rogueforge.game.support.GdxTestSupport;
import com.rogueforge.game.world.SettlementUpgradeDefinition;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentCatalogDataTest {
    private static final Set<String> SLOT_TYPES = Set.of("HEAD", "BODY", "ARMS", "LEGS", "WEAPON", "ACCESSORY");
    private static final Set<String> GRADE_ORDER = Set.of("G", "F", "E", "D", "C", "B", "A", "S", "S+", "S++", "S+++");
    private static final Set<String> EQUIP_TARGETS = Set.of(EquipmentItem.TARGET_PLAYER, EquipmentItem.TARGET_ROBOT);
    private static final Set<String> PLAYER_BANNED_KEYWORDS = Set.of(
        "actuator", "carapace", "chassis", "circlet", "core", "drive", "frame", "loop", "prism", "relay", "servo", "visor"
    );

    @BeforeAll
    static void bootGdx() {
        GdxTestSupport.init();
    }

    @Test
    void equipmentCatalogCoversAllTiersSlotsAndGrades() {
        EquipmentItem[] items = new Json().fromJson(
            EquipmentItem[].class,
            Gdx.files.internal("data/equipment.json").readString()
        );

        assertNotNull(items);
        assertTrue(items.length >= 36, "Expected at least 36 equipment entries.");

        Set<String> ids = new HashSet<>();
        Set<String> grades = new HashSet<>();
        Set<String> targets = new HashSet<>();
        for (EquipmentItem item : items) {
            assertTrue(ids.add(item.getId()), "Duplicate equipment id: " + item.getId());
            assertTrue(SLOT_TYPES.contains(item.getSlotType()), "Unexpected slot type: " + item.getSlotType());
            assertTrue(item.getTier() >= 1 && item.getTier() <= 6, "Unexpected tier for " + item.getId());
            grades.add(item.getGradeRequirement());
            assertTrue(EQUIP_TARGETS.contains(item.getEquipTarget()), "Unexpected equip target: " + item.getEquipTarget());
            targets.add(item.getEquipTarget());
        }

        assertEquals(GRADE_ORDER, grades, "Equipment catalog should cover the full grade ladder.");
        assertEquals(EQUIP_TARGETS, targets, "Equipment catalog should define both player and robot gear.");

        for (int tier = 1; tier <= 6; tier++) {
            final int currentTier = tier;
            Set<String> tierSlots = Arrays.stream(items)
                .filter(item -> item.getTier() == currentTier)
                .map(EquipmentItem::getSlotType)
                .collect(Collectors.toSet());
            assertEquals(SLOT_TYPES, tierSlots, "Tier " + currentTier + " should cover every slot type.");
        }

        for (String target : EQUIP_TARGETS) {
            Set<String> targetSlots = Arrays.stream(items)
                .filter(item -> target.equals(item.getEquipTarget()))
                .map(EquipmentItem::getSlotType)
                .collect(Collectors.toSet());
            assertEquals(SLOT_TYPES, targetSlots, target + " gear should cover every slot type.");
        }
    }

    @Test
    void shopForgeAndSettlementReferencesResolveToEquipmentCatalog() {
        EquipmentItem[] items = new Json().fromJson(
            EquipmentItem[].class,
            Gdx.files.internal("data/equipment.json").readString()
        );
        ForgeComponentDefinition[] components = new Json().fromJson(
            ForgeComponentDefinition[].class,
            Gdx.files.internal("data/forge_components.json").readString()
        );
        Set<String> ids = Arrays.stream(items).map(EquipmentItem::getId).collect(Collectors.toSet());
        Set<String> componentIds = Arrays.stream(components).map(ForgeComponentDefinition::getId).collect(Collectors.toSet());

        ShopDefinition[] shops = new Json().fromJson(
            ShopDefinition[].class,
            Gdx.files.internal("data/shop_inventories.json").readString()
        );
        for (ShopDefinition shop : shops) {
            for (ShopEntryDefinition entry : shop.getEntries()) {
                if (!"equipment".equals(entry.getType())) {
                    continue;
                }
                assertTrue(
                    ids.contains(entry.getItemId()),
                    "Shop " + shop.getId() + " references missing equipment " + entry.getItemId()
                );
            }
        }

        ForgeRecipeDefinition[] recipes = new Json().fromJson(
            ForgeRecipeDefinition[].class,
            Gdx.files.internal("data/forge_recipes.json").readString()
        );
        Set<String> recipeIds = new HashSet<>();
        for (ForgeRecipeDefinition recipe : recipes) {
            assertTrue(recipeIds.add(recipe.getId()), "Duplicate forge recipe id " + recipe.getId());
            assertTrue(
                ids.contains(recipe.getResultEquipmentId()),
                "Forge recipe " + recipe.getId() + " references missing equipment " + recipe.getResultEquipmentId()
            );
            assertTrue(recipe.getGoldCost() >= 0L, "Forge recipe should not use a negative gold cost: " + recipe.getId());
            assertTrue(recipe.getShardCost() >= 0, "Forge recipe should not use a negative shard cost: " + recipe.getId());
            for (ForgeIngredientDefinition ingredient : recipe.getIngredients()) {
                assertTrue(
                    componentIds.contains(ingredient.getComponentId()),
                    "Forge recipe " + recipe.getId() + " references missing component " + ingredient.getComponentId()
                );
                assertTrue(ingredient.getQuantity() > 0, "Forge recipe ingredient quantity should be positive for " + recipe.getId());
            }
        }

        SettlementUpgradeDefinition[] upgrades = new Json().fromJson(
            SettlementUpgradeDefinition[].class,
            Gdx.files.internal("data/settlement_upgrades.json").readString()
        );
        for (SettlementUpgradeDefinition upgrade : upgrades) {
            if (upgrade.getRewardEquipmentId() == null || upgrade.getRewardEquipmentId().isEmpty()) {
                continue;
            }
            assertTrue(
                ids.contains(upgrade.getRewardEquipmentId()),
                "Settlement upgrade " + upgrade.getId() + " references missing equipment " + upgrade.getRewardEquipmentId()
            );
        }

        assertFalse(ids.isEmpty());
        assertTrue(recipeIds.contains("phoenix_visor_recipe"));
        assertTrue(recipeIds.contains("solaris_frame_recipe"));
        assertTrue(recipeIds.contains("comet_striders_recipe"));
        assertTrue(recipeIds.contains("starforged_relay_recipe"));
    }

    @Test
    void playerGearUsesHumanFacingNamesAndPlayerRecipesStayOutOfRobotCategories() {
        EquipmentItem[] items = new Json().fromJson(
            EquipmentItem[].class,
            Gdx.files.internal("data/equipment.json").readString()
        );
        assertNotNull(items);

        Map<String, EquipmentItem> itemsById = Arrays.stream(items)
            .collect(Collectors.toMap(EquipmentItem::getId, item -> item));

        for (EquipmentItem item : items) {
            if (!item.isPlayerEquipment()) {
                continue;
            }
            String normalizedName = item.getName().toLowerCase();
            for (String bannedKeyword : PLAYER_BANNED_KEYWORDS) {
                assertFalse(
                    normalizedName.contains(bannedKeyword),
                    "Player gear should avoid robot-style naming: " + item.getId() + " -> " + item.getName()
                );
            }
        }

        ForgeRecipeDefinition[] recipes = new Json().fromJson(
            ForgeRecipeDefinition[].class,
            Gdx.files.internal("data/forge_recipes.json").readString()
        );
        assertNotNull(recipes);

        for (ForgeRecipeDefinition recipe : recipes) {
            EquipmentItem result = itemsById.get(recipe.getResultEquipmentId());
            assertNotNull(result, "Recipe result should resolve: " + recipe.getId());
            if (result.isPlayerEquipment()) {
                assertFalse(
                    recipe.getCategory().toLowerCase().contains("robot"),
                    "Player recipe category should not read like robot gear: " + recipe.getId()
                );
                assertFalse(
                    recipe.getDescription().toLowerCase().contains("robot"),
                    "Player recipe description should not describe robot gear: " + recipe.getId()
                );
            }
        }
    }
}
