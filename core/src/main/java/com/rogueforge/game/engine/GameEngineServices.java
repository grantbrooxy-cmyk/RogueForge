package com.rogueforge.game.engine;

import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.engine.base.BaseBuildingEngine;
import com.rogueforge.game.engine.base.BaseDefenseDirector;
import com.rogueforge.game.engine.meta.CyberneticEnhancementEngine;
import com.rogueforge.game.engine.social.GuildPermissionsEngine;
import com.rogueforge.game.engine.world.EnvironmentalInteractionSystem;
import com.rogueforge.game.engine.world.FrontierZoneGenerator;
import com.rogueforge.game.engine.world.InfiniteDungeonLayoutGenerator;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.persistence.MetaProgressionManager;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;
import com.rogueforge.game.world.DialogueSystem;
import com.rogueforge.game.world.QuestManager;
import com.rogueforge.game.world.RobotRecruitmentManager;
import com.rogueforge.game.world.SettlementManager;
import com.rogueforge.game.world.DynamicWorldEventSystem;
import com.rogueforge.game.world.WarPhaseManager;
import com.rogueforge.game.world.WorldStateManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared runtime services for reusable engine systems and managers.
 */
public class GameEngineServices implements ServiceLifecycle {
    private final Map<Class<?>, Object> services = new LinkedHashMap<>();
    private boolean initialized;

    public GameEngineServices() {
        this(new EventBus(), new SettingsManager(), new SaveManager(), new MetaProgressionManager());
    }

    public GameEngineServices(EventBus eventBus, SettingsManager settingsManager, SaveManager saveManager, MetaProgressionManager metaProgressionManager) {
        registerService(BaseBuildingEngine.class, new BaseBuildingEngine());
        registerService(BaseDefenseDirector.class, new BaseDefenseDirector());
        registerService(CyberneticEnhancementEngine.class, new CyberneticEnhancementEngine());
        registerService(GuildPermissionsEngine.class, new GuildPermissionsEngine());
        registerService(TmxWorldLoader.class, new TmxWorldLoader());
        registerService(InfiniteDungeonLayoutGenerator.class, new InfiniteDungeonLayoutGenerator());
        registerService(FrontierZoneGenerator.class, new FrontierZoneGenerator());
        registerService(EnvironmentalInteractionSystem.class, new EnvironmentalInteractionSystem());
        registerService(SettingsManager.class, settingsManager != null ? settingsManager : new SettingsManager());
        registerService(SaveManager.class, saveManager != null ? saveManager : new SaveManager());
        registerService(MetaProgressionManager.class, metaProgressionManager != null ? metaProgressionManager : new MetaProgressionManager());
        registerService(QuestManager.class, new QuestManager());
        registerService(DialogueSystem.class, new DialogueSystem());
        registerService(WorldStateManager.class, new WorldStateManager());
        registerService(RobotRecruitmentManager.class, new RobotRecruitmentManager());
        registerService(SettlementManager.class, new SettlementManager());
        registerService(DynamicWorldEventSystem.class, new DynamicWorldEventSystem(eventBus));
        registerService(WarPhaseManager.class, new WarPhaseManager());
    }

    public final <T> void registerService(Class<T> type, T service) {
        if (type == null || service == null) {
            throw new IllegalArgumentException("Service type and instance are required.");
        }
        services.put(type, service);
    }

    public <T> T getService(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalArgumentException("No service registered for " + type.getSimpleName());
        }
        return type.cast(service);
    }

    public Collection<Object> getAllServices() {
        return services.values();
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        for (Object service : services.values()) {
            if (service instanceof ServiceLifecycle) {
                ((ServiceLifecycle) service).initialize();
            }
        }
    }

    @Override
    public void dispose() {
        for (Object service : services.values()) {
            if (service instanceof ServiceLifecycle) {
                ((ServiceLifecycle) service).dispose();
            }
        }
        initialized = false;
    }

    public BaseBuildingEngine getBaseBuildingEngine() { return getService(BaseBuildingEngine.class); }

    public BaseDefenseDirector getBaseDefenseDirector() { return getService(BaseDefenseDirector.class); }

    public CyberneticEnhancementEngine getCyberneticEnhancementEngine() { return getService(CyberneticEnhancementEngine.class); }

    public GuildPermissionsEngine getGuildPermissionsEngine() { return getService(GuildPermissionsEngine.class); }

    public TmxWorldLoader getWorldLoader() { return getService(TmxWorldLoader.class); }

    public InfiniteDungeonLayoutGenerator getInfiniteDungeonLayoutGenerator() { return getService(InfiniteDungeonLayoutGenerator.class); }

    public FrontierZoneGenerator getFrontierZoneGenerator() { return getService(FrontierZoneGenerator.class); }

    public EnvironmentalInteractionSystem getEnvironmentalInteractionSystem() { return getService(EnvironmentalInteractionSystem.class); }

    public SettingsManager getSettingsManager() { return getService(SettingsManager.class); }

    public SaveManager getSaveManager() { return getService(SaveManager.class); }

    public MetaProgressionManager getMetaProgressionManager() { return getService(MetaProgressionManager.class); }

    public QuestManager getQuestManager() { return getService(QuestManager.class); }

    public DialogueSystem getDialogueSystem() { return getService(DialogueSystem.class); }

    public WorldStateManager getWorldStateManager() { return getService(WorldStateManager.class); }

    public RobotRecruitmentManager getRecruitmentManager() { return getService(RobotRecruitmentManager.class); }

    public SettlementManager getSettlementManager() { return getService(SettlementManager.class); }

    public DynamicWorldEventSystem getDynamicWorldEventSystem() { return getService(DynamicWorldEventSystem.class); }

    public WarPhaseManager getWarPhaseManager() { return getService(WarPhaseManager.class); }
}
