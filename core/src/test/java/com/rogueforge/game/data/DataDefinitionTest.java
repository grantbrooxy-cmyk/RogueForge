package com.rogueforge.game.data;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataDefinitionTest {

    @Test
    void forgeRecipeDefinitionProvidesSafeDefaults() throws Exception {
        ForgeRecipeDefinition recipe = new ForgeRecipeDefinition();

        setField(recipe, "category", null);
        setField(recipe, "description", null);
        setField(recipe, "shardGrade", null);
        setField(recipe, "goldCost", -50L);
        setField(recipe, "shardCost", -2);
        setField(recipe, "ingredients", null);

        assertEquals("General", recipe.getCategory());
        assertEquals("", recipe.getDescription());
        assertEquals("G", recipe.getShardGrade());
        assertEquals(0L, recipe.getGoldCost());
        assertEquals(0, recipe.getShardCost());
        assertNotNull(recipe.getIngredients());
        assertEquals(0, recipe.getIngredients().length);
    }

    @Test
    void forgeComponentDefinitionProvidesSafeDefaults() throws Exception {
        ForgeComponentDefinition component = new ForgeComponentDefinition();

        setField(component, "name", null);
        setField(component, "description", null);
        setField(component, "rarity", null);
        setField(component, "dropTags", null);

        assertEquals("", component.getName());
        assertEquals("", component.getDescription());
        assertEquals("COMMON", component.getRarity());
        assertEquals(0, component.getDropTags().length);
    }

    @Test
    void forgeIngredientDefinitionClampsNegativeQuantity() throws Exception {
        ForgeIngredientDefinition ingredient = new ForgeIngredientDefinition();

        setField(ingredient, "quantity", -4);

        assertEquals(0, ingredient.getQuantity());
    }

    @Test
    void storyEventDefinitionExposesConfiguredFields() throws Exception {
        StoryEventDefinition event = new StoryEventDefinition();

        setField(event, "id", "boss_drake_down");
        setField(event, "triggerType", "BOSS_DEFEAT");
        setField(event, "triggerId", "storm_drake");
        setField(event, "onceFlag", "story.drake.defeated");
        setField(event, "speaker", "Nia");
        setField(event, "text", "The ridge is safe again.");
        setField(event, "setWorldFlag", "ridge.safe");
        setField(event, "addKeyItem", "drake_core");
        setField(event, "setQuestId", "hunt_the_drake");
        setField(event, "setQuestStep", "report_back");
        setField(event, "completeQuestId", "ridge_emergency");
        setField(event, "rewardExperience", 250);

        assertEquals("boss_drake_down", event.getId());
        assertEquals("BOSS_DEFEAT", event.getTriggerType());
        assertEquals("storm_drake", event.getTriggerId());
        assertEquals("story.drake.defeated", event.getOnceFlag());
        assertEquals("Nia", event.getSpeaker());
        assertEquals("The ridge is safe again.", event.getText());
        assertEquals("ridge.safe", event.getSetWorldFlag());
        assertEquals("drake_core", event.getAddKeyItem());
        assertEquals("hunt_the_drake", event.getSetQuestId());
        assertEquals("report_back", event.getSetQuestStep());
        assertEquals("ridge_emergency", event.getCompleteQuestId());
        assertEquals(250, event.getRewardExperience());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
