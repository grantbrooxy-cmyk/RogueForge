package com.rogueforge.game.data;

import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.robot.RobotDefinition;
import com.rogueforge.game.world.DialogueDefinition;
import com.rogueforge.game.world.QuestDefinition;
import com.rogueforge.game.world.RecruitmentDefinition;
import com.rogueforge.game.world.SettlementNpcScheduleDefinition;
import com.rogueforge.game.world.SettlementUpgradeDefinition;
import com.rogueforge.game.world.WorldStateDefinition;

/**
 * Shared typed registries for static gameplay definitions.
 */
public final class DefinitionRegistries {
    public static final DefinitionRegistry<AbilityDefinition> ABILITIES = new DefinitionRegistry<>(
        StaticDataPaths.ABILITIES,
        AbilityDefinition[].class,
        AbilityDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.requireText(definition.getDescription(), "description");
        }
    );

    public static final DefinitionRegistry<ZoneDefinition> ZONES = new DefinitionRegistry<>(
        StaticDataPaths.ZONES,
        ZoneDefinition[].class,
        ZoneDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.requireText(definition.getTilemapPath(), "tilemapPath");
            collector.require(definition.getMonsterIds() != null && definition.getMonsterIds().length > 0,
                "Expected at least one monsterId.");
        }
    );

    public static final DefinitionRegistry<MonsterDefinition> MONSTERS = new DefinitionRegistry<>(
        StaticDataPaths.MONSTERS,
        MonsterDefinition[].class,
        MonsterDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.requireText(definition.getRank(), "rank");
            collector.require(definition.getHp() > 0, "hp must be > 0.");
        }
    );

    public static final DefinitionRegistry<RobotDefinition> ROBOTS = new DefinitionRegistry<>(
        StaticDataPaths.ROBOTS,
        RobotDefinition[].class,
        RobotDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.require(definition.getRole() != null, "Missing required field 'role'.");
            collector.require(definition.getBaseHp() > 0, "baseHp must be > 0.");
        }
    );

    public static final DefinitionRegistry<ForgeComponentDefinition> FORGE_COMPONENTS = new DefinitionRegistry<>(
        StaticDataPaths.FORGE_COMPONENTS,
        ForgeComponentDefinition[].class,
        ForgeComponentDefinition::getId,
        (definition, collector) -> collector.requireText(definition.getName(), "name")
    );

    public static final DefinitionRegistry<BlueprintFragmentDefinition> BLUEPRINT_FRAGMENTS = new DefinitionRegistry<>(
        StaticDataPaths.BLUEPRINT_FRAGMENTS,
        BlueprintFragmentDefinition[].class,
        BlueprintFragmentDefinition::getId,
        (definition, collector) -> collector.requireText(definition.getName(), "name")
    );

    public static final DefinitionRegistry<ForgeRecipeDefinition> FORGE_RECIPES = new DefinitionRegistry<>(
        StaticDataPaths.FORGE_RECIPES,
        ForgeRecipeDefinition[].class,
        ForgeRecipeDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.requireText(definition.getResultEquipmentId(), "resultEquipmentId");
        }
    );

    public static final DefinitionRegistry<StoryEventDefinition> STORY_EVENTS = new DefinitionRegistry<>(
        StaticDataPaths.STORY_EVENTS,
        StoryEventDefinition[].class,
        StoryEventDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getTriggerType(), "triggerType");
            collector.requireText(definition.getTriggerId(), "triggerId");
            collector.requireText(definition.getText(), "text");
            collector.require(definition.getMinimumForgeCoreLevel() >= 0, "minimumForgeCoreLevel must be >= 0.");
        }
    );

    public static final DefinitionRegistry<ShopDefinition> SHOPS = new DefinitionRegistry<>(
        StaticDataPaths.SHOPS,
        ShopDefinition[].class,
        ShopDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getVendorName(), "vendorName");
            collector.require(definition.getEntries() != null && definition.getEntries().length > 0,
                "Expected at least one shop entry.");
        }
    );

    public static final DefinitionRegistry<EquipmentItem> EQUIPMENT = new DefinitionRegistry<>(
        StaticDataPaths.EQUIPMENT,
        EquipmentItem[].class,
        EquipmentItem::getId,
        (definition, collector) -> {
            collector.requireText(definition.getName(), "name");
            collector.requireText(definition.getSlotType(), "slotType");
        }
    );

    public static final DefinitionRegistry<QuestDefinition> QUESTS = new DefinitionRegistry<>(
        StaticDataPaths.QUESTS,
        QuestDefinition[].class,
        QuestDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getTitle(), "title");
            collector.require(definition.getSteps().length > 0, "Expected at least one quest step.");
            if (definition.isAutoStart()) {
                collector.requireText(definition.getStartStepId(), "startStepId");
            }
        }
    );

    public static final DefinitionRegistry<DialogueDefinition> DIALOGUE = new DefinitionRegistry<>(
        StaticDataPaths.DIALOGUE,
        DialogueDefinition[].class,
        DialogueDefinition::getId,
        (definition, collector) -> {
            collector.requireText(definition.getNpcId(), "npcId");
            collector.require(definition.getLines().length > 0, "Expected at least one dialogue line.");
        }
    );

    public static final DefinitionRegistry<WorldStateDefinition> WORLD_STATE = new DefinitionRegistry<>(
        StaticDataPaths.WORLD_STATE,
        WorldStateDefinition[].class,
        WorldStateDefinition::getId
    );

    public static final DefinitionRegistry<RecruitmentDefinition> RECRUITMENT = new DefinitionRegistry<>(
        StaticDataPaths.RECRUITMENT,
        RecruitmentDefinition[].class,
        RecruitmentDefinition::getEventId,
        (definition, collector) -> collector.requireText(definition.getRobotId(), "robotId")
    );

    public static final DefinitionRegistry<SettlementUpgradeDefinition> SETTLEMENT_UPGRADES = new DefinitionRegistry<>(
        StaticDataPaths.SETTLEMENT_UPGRADES,
        SettlementUpgradeDefinition[].class,
        SettlementUpgradeDefinition::getId,
        (definition, collector) -> collector.requireText(definition.getName(), "name")
    );

    public static final DefinitionRegistry<SettlementNpcScheduleDefinition> SETTLEMENT_NPC_SCHEDULES = new DefinitionRegistry<>(
        StaticDataPaths.SETTLEMENT_NPC_SCHEDULES,
        SettlementNpcScheduleDefinition[].class,
        SettlementNpcScheduleDefinition::getNpcId
    );

    private DefinitionRegistries() {
    }
}
