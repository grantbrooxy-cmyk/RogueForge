package com.rogueforge.game.engine;

import com.rogueforge.game.engine.base.BaseBuildingEngine;
import com.rogueforge.game.engine.base.BaseDefenseDirector;
import com.rogueforge.game.engine.meta.CyberneticEnhancementEngine;
import com.rogueforge.game.engine.world.FrontierZoneGenerator;
import com.rogueforge.game.engine.world.InfiniteDungeonLayoutGenerator;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;
import com.rogueforge.game.world.DialogueSystem;
import com.rogueforge.game.world.QuestManager;
import com.rogueforge.game.world.RobotRecruitmentManager;
import com.rogueforge.game.world.SettlementManager;
import com.rogueforge.game.world.WorldStateManager;

/**
 * Shared runtime services for reusable engine systems and managers.
 */
public class GameEngineServices {
    private final BaseBuildingEngine baseBuildingEngine = new BaseBuildingEngine();
    private final BaseDefenseDirector baseDefenseDirector = new BaseDefenseDirector();
    private final CyberneticEnhancementEngine cyberneticEnhancementEngine = new CyberneticEnhancementEngine();
    private final TmxWorldLoader worldLoader = new TmxWorldLoader();
    private final InfiniteDungeonLayoutGenerator infiniteDungeonLayoutGenerator = new InfiniteDungeonLayoutGenerator();
    private final FrontierZoneGenerator frontierZoneGenerator = new FrontierZoneGenerator();
    private final SettingsManager settingsManager = new SettingsManager();
    private final SaveManager saveManager = new SaveManager();
    private final QuestManager questManager = new QuestManager();
    private final DialogueSystem dialogueSystem = new DialogueSystem();
    private final WorldStateManager worldStateManager = new WorldStateManager();
    private final RobotRecruitmentManager recruitmentManager = new RobotRecruitmentManager();
    private final SettlementManager settlementManager = new SettlementManager();

    public BaseBuildingEngine getBaseBuildingEngine() { return baseBuildingEngine; }

    public BaseDefenseDirector getBaseDefenseDirector() { return baseDefenseDirector; }

    public CyberneticEnhancementEngine getCyberneticEnhancementEngine() { return cyberneticEnhancementEngine; }

    public TmxWorldLoader getWorldLoader() { return worldLoader; }

    public InfiniteDungeonLayoutGenerator getInfiniteDungeonLayoutGenerator() { return infiniteDungeonLayoutGenerator; }

    public FrontierZoneGenerator getFrontierZoneGenerator() { return frontierZoneGenerator; }

    public SettingsManager getSettingsManager() { return settingsManager; }

    public SaveManager getSaveManager() { return saveManager; }

    public QuestManager getQuestManager() { return questManager; }

    public DialogueSystem getDialogueSystem() { return dialogueSystem; }

    public WorldStateManager getWorldStateManager() { return worldStateManager; }

    public RobotRecruitmentManager getRecruitmentManager() { return recruitmentManager; }

    public SettlementManager getSettlementManager() { return settlementManager; }
}
