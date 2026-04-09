package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rogueforge.game.core.GameContext;
import com.rogueforge.game.core.EventHandler;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.core.GameLoop;
import com.rogueforge.game.core.GameState;
import com.rogueforge.game.engine.base.BaseBuildingEngine;
import com.rogueforge.game.engine.base.BaseDefenderProfile;
import com.rogueforge.game.engine.base.BaseDefenderUnit;
import com.rogueforge.game.engine.base.BaseDefenseDirector;
import com.rogueforge.game.engine.base.BasePlacementResult;
import com.rogueforge.game.engine.base.BaseRaidState;
import com.rogueforge.game.engine.base.BaseState;
import com.rogueforge.game.engine.base.DefenderAssignment;
import com.rogueforge.game.engine.base.DefenderRole;
import com.rogueforge.game.engine.base.PlacedStructure;
import com.rogueforge.game.engine.base.StructureCategory;
import com.rogueforge.game.engine.base.StructureDefinition;
import com.rogueforge.game.engine.GameEngineServices;
import com.rogueforge.game.engine.TimeSystem;
import com.rogueforge.game.engine.meta.CyberneticBonuses;
import com.rogueforge.game.engine.meta.CyberneticEnhancementEngine;
import com.rogueforge.game.engine.meta.DeathDraftResult;
import com.rogueforge.game.engine.meta.ForgeLegacyBonuses;
import com.rogueforge.game.engine.meta.ForgeLegacyEngine;
import com.rogueforge.game.engine.meta.ForgeLegacyNodeDefinition;
import com.rogueforge.game.engine.meta.RunOutcomeSummary;
import com.rogueforge.game.engine.meta.ShardRunManager;
import com.rogueforge.game.engine.social.GuildPermissionsEngine;
import com.rogueforge.game.engine.social.GuildDefinition;
import com.rogueforge.game.engine.social.GuildMembership;
import com.rogueforge.game.engine.social.GuildRank;
import com.rogueforge.game.engine.social.OwnershipRecord;
import com.rogueforge.game.engine.social.OwnershipScope;
import com.rogueforge.game.engine.social.PermissionAction;
import com.rogueforge.game.engine.social.PermissionSet;
import com.rogueforge.game.engine.world.FrontierTerrainSampler;
import com.rogueforge.game.engine.world.FrontierBiomeCatalog;
import com.rogueforge.game.engine.world.FrontierBiomeDefinition;
import com.rogueforge.game.engine.world.FrontierChunkManager;
import com.rogueforge.game.engine.world.FrontierZoneGenerator;
import com.rogueforge.game.engine.world.InfiniteDungeonLayoutGenerator;
import com.rogueforge.game.engine.world.EnvironmentalInteractionSystem;
import com.rogueforge.game.engine.world.TmxWorldLoader;
import com.rogueforge.game.engine.world.WorldGenerator;
import com.rogueforge.game.engine.world.ZoneLoader;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityRegistry;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.BlueprintFragmentDefinition;
import com.rogueforge.game.data.DefinitionRegistry;
import com.rogueforge.game.data.DefinitionRegistries;
import com.rogueforge.game.data.ForgeComponentDefinition;
import com.rogueforge.game.data.ForgeIngredientDefinition;
import com.rogueforge.game.data.ForgeRecipeDefinition;
import com.rogueforge.game.data.MetaProgressionState;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.data.ShopDefinition;
import com.rogueforge.game.data.ShopEntryDefinition;
import com.rogueforge.game.data.StaticDataPaths;
import com.rogueforge.game.data.StoryEventDefinition;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.entity.NpcEntity;
import com.rogueforge.game.event.NpcScheduleEvent;
import com.rogueforge.game.economy.ShopInventory;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;
import com.rogueforge.game.persistence.MetaProgressionManager;
import com.rogueforge.game.progression.AbilityEvolutionManager;
import com.rogueforge.game.progression.AbilityProgressionState;
import com.rogueforge.game.progression.RobotEvolutionManager;
import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.progression.WeaponProficiencyState;
import com.rogueforge.game.progression.CombatArtRegistry;
import com.rogueforge.game.progression.WeaponProficiencyTracker;
import com.rogueforge.game.robot.RobotDefinition;
import com.rogueforge.game.world.DialogueSystem;
import com.rogueforge.game.world.ActTwoSupportDirector;
import com.rogueforge.game.world.CameraController;
import com.rogueforge.game.world.QuestManager;
import com.rogueforge.game.world.RobotRecruitmentManager;
import com.rogueforge.game.world.SettlementManager;
import com.rogueforge.game.world.SettlementNpcScheduleDefinition;
import com.rogueforge.game.world.SettlementState;
import com.rogueforge.game.world.SettlementTimeManager;
import com.rogueforge.game.world.SettlementUpgradeDefinition;
import com.rogueforge.game.world.DynamicWorldEventSystem;
import com.rogueforge.game.world.WarPhaseManager;
import com.rogueforge.game.world.WarPhaseSnapshot;
import com.rogueforge.game.world.WorldStateManager;
import com.rogueforge.game.world.ZoneAccessPolicy;
import com.rogueforge.game.world.actor.Enemy;
import com.rogueforge.game.world.actor.RobotCompanion;
import com.rogueforge.game.ui.DebugOverlay;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Main gameplay screen with a moveable player square, robot companions,
 * spawning enemies, combat mechanics, and HUD overlay.
 */
public class GameScreen implements Screen {
    private static final float WORLD_VIEW_WIDTH = 1280f;
    private static final float WORLD_VIEW_HEIGHT = 720f;

    private final GameContext context;
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameLoop gameLoop;
    private final OrthographicCamera gameCamera;
    private final OrthographicCamera uiCamera;
    private final CameraController cameraController;
    private final Viewport gameViewport;
    private final Viewport uiViewport;
    private final HUDOverlay hudOverlay;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final String playerName;
    private final GameState gameState;
    private Texture groundTileTexture;
    private Texture wallTileTexture;
    private Texture villageGroundTileTexture;
    private Texture villageWallTileTexture;
    // Tiled map renderer — used when the zone TMX has painted tile layers
    private TiledMap currentTiledMap;
    private String currentTiledMapPath;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private Texture doorTexture;
    private Texture chestTexture;
    private Texture shadowTexture;
    private AnimationSet playerAnimation;
    private AnimationSet[] robotAnimations;
    private AnimationSet[] enemyAnimations;
    private AnimationSet[] npcAnimations;
    private final Set<String> managedTexturePaths = new LinkedHashSet<>();

    // Player state
    private final Vector2 playerPos = new Vector2(640, 360);
    private static final float PLAYER_SIZE = 56f;
    private static final float PLAYER_SPEED = 200f;
    private static final float INTERACTION_REACH = 34f;
    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private float playerAttackCooldown = 0f;
    private static final float PLAYER_ATTACK_COOLDOWN = 0.5f;
    private static final float PLAYER_ATTACK_RANGE = 50f;
    private static final float PLAYER_ATTACK_DAMAGE = 25f;

    // Attack flash effect
    private float attackFlashAlpha = 0f;
    private float attackFlashTimer = 0f;
    private static final float ATTACK_FLASH_DURATION = 0.2f;

    // Enemy state
    private List<Enemy> enemies = new ArrayList<>();
    private static final int INITIAL_ENEMY_COUNT = 6;
    private static final String INFINITE_DUNGEON_ZONE_ID = "infinite_dungeon";
    private static final int INFINITE_DUNGEON_START_FLOOR = 1;
    private static final int INFINITE_DUNGEON_BOSS_INTERVAL = 10;
    private static final String ENDGAME_MODE_STANDARD = "STANDARD";
    private static final String ENDGAME_MODE_CHALLENGE = "CHALLENGE";
    private static final String ENDGAME_MODE_BOSS_RUSH = "BOSS_RUSH";
    private static final String[] ENDGAME_MODE_ORDER = {ENDGAME_MODE_STANDARD, ENDGAME_MODE_CHALLENGE, ENDGAME_MODE_BOSS_RUSH};
    private static final String[] CHALLENGE_MODIFIER_ORDER = {"GLASS_CANNON", "THIN_SUPPLIES", "OVERCLOCKED"};
    private static final String[] HARD_MODE_SEED_NAMES = {"Baseline", "Ashstorm", "Null Rift", "Empirefall"};
    private static final List<String> ENDGAME_BOSS_IDS = List.of(
        "origin_core_s",
        "the_unmaker_s",
        "apex_predator_s",
        "dungeon_overlord_s"
    );
    private static final String[] LEGENDARY_ROBOT_UNLOCK_IDS = {"legendary_scout", "legendary_guardian", "legendary_striker"};
    private static final String[] LEGENDARY_ROBOT_EVENTS = {"legendary_scout_join", "legendary_guardian_join", "legendary_striker_join"};
    private static final String[] LEGACY_LEGENDARY_ROBOT_IDS = {"scout_mk3", "guardian_mk3", "striker_mk3"};
    private static final String ACT4_OPERATION_IRON_LIFELINE = "act4.operation.iron_lifeline";
    private static final String ACT4_OPERATION_GLOAM_ARCHIVE = "act4.operation.gloam_archive";
    private static final String ACT4_OPERATION_HELLCLIMB = "act4.operation.hellclimb";
    private static final String ACT4_OPERATION_LAST_LIGHT = "act4.operation.last_light";
    private static final String ACT4_CAMPAIGN_EPILOGUE = "act4.campaign.epilogue";
    private static final String ACT4_SIDEARC_COMMAND_BASTION = "act4.sidearc.command_bastion";
    private static final String ACT4_SIDEARC_GUILD_ASCENDANCY = "act4.sidearc.guild_ascendancy";
    private static final String ACT4_SIDEARC_ARCHIVE_RECLAMATION = "act4.sidearc.archive_reclamation";
    private static final float ENEMY_SIZE = 48f;
    private static final float ENEMY_SPAWN_MIN_DISTANCE = 400f;
    private static final float ENEMY_SPAWN_MAX_DISTANCE = 800f;
    private static final float ENEMY_AGGRO_RANGE = 200f;
    private static final float ENEMY_MELEE_COOLDOWN = 1f;
    private static final float ENEMY_MELEE_DAMAGE = 10f;
    private static final float ENEMY_PATROL_RADIUS = 150f;
    private static final float ENCOUNTER_TRIGGER_RANGE = 34f;
    private float enemyRespawnTimer = 0f;
    private static final float ENEMY_RESPAWN_DELAY = 3f;

    // Robot companion state
    private static final int ROBOT_COUNT = 3;
    private static final float ROBOT_SIZE = 44f;
    private static final float ROBOT_FOLLOW_SPEED = 320f;
    private static final float ROBOT_FOLLOW_GAP = 52f;
    private static final float ROBOT_MAX_HEALTH = 75f;
    private static final float BASE_DEFENDER_SIZE = 42f;
    private static final float BASE_DEFENDER_LEASH_RANGE = 132f;
    private static final float BASE_DEFENDER_ATTACK_DAMAGE_SCALE = 0.7f;
    private static final float BASE_DEFENDER_CONTACT_RANGE = 28f;
    private static final float BASE_RAID_THREAT_PER_SECOND = 0.02f;
    private static final float BASE_RAID_TRIGGER_THREAT = 1f;
    private static final float BASE_RAID_COOLDOWN_SECONDS = 85f;
    private static final int BASE_STRUCTURE_REPAIR_STEP = 40;
    private static final float BASE_STRUCTURE_CONTACT_RANGE = 26f;
    private static final float PLAYER_AGILITY = 26f;
    private static final float PLAYER_STRENGTH = 18f;
    private static final float PLAYER_INTELLIGENCE = 14f;
    private static final float PLAYER_STAMINA = 20f;
    private static final String[] GRADE_ORDER = {"G", "F", "E", "D", "C", "B", "A", "S", "S+", "S++", "S+++"};
    private final RobotCompanion[] robots = new RobotCompanion[ROBOT_COUNT];
    private final List<House> houses = new ArrayList<>();
    private final List<NpcEntity> npcs = new ArrayList<>();
    private final List<EquipmentItem> equipmentCatalog = new ArrayList<>();
    private final List<String> ownedEquipmentIds = new ArrayList<>();
    private final Vector2 lastMoveDirection = new Vector2(0f, -1f);
    private float playerAnimationTime = 0f;
    private final Map<String, ZoneDefinition> zoneDefinitions = new HashMap<>();
    private final Map<String, MonsterDefinition> monsterDefinitions = new HashMap<>();
    private final Map<String, ShopDefinition> shopDefinitions = new HashMap<>();
    private final Map<String, List<ShopDefinition>> shopsByZoneId = new HashMap<>();
    private final Map<String, RobotDefinition> robotDefinitions = new HashMap<>();
    private final Map<String, ForgeComponentDefinition> forgeComponentDefinitions = new HashMap<>();
    private final Map<String, BlueprintFragmentDefinition> blueprintFragmentDefinitions = new HashMap<>();
    private final List<ForgeRecipeDefinition> forgeRecipes = new ArrayList<>();
    private final List<StoryEventDefinition> storyEvents = new ArrayList<>();
    private final GameEngineServices engineServices;
    private final BaseBuildingEngine baseBuildingEngine;
    private final BaseDefenseDirector baseDefenseDirector;
    private final CyberneticEnhancementEngine cyberneticEnhancementEngine;
    private final ForgeLegacyEngine forgeLegacyEngine = new ForgeLegacyEngine();
    private final GuildPermissionsEngine guildPermissionsEngine;
    private final TmxWorldLoader worldLoader;
    private final ZoneLoader zoneLoader;
    private final InfiniteDungeonLayoutGenerator infiniteDungeonLayoutGenerator;
    private final FrontierZoneGenerator frontierZoneGenerator;
    private final WorldGenerator worldGenerator;
    private final EnvironmentalInteractionSystem environmentalInteractionSystem;
    private final TimeSystem timeSystem;
    private final SettingsManager settingsManager;
    private final SaveManager saveManager;
    private final MetaProgressionManager metaProgressionManager;
    private final QuestManager questManager;
    private final DialogueSystem dialogueSystem;
    private final WorldStateManager worldStateManager;
    private final RobotRecruitmentManager recruitmentManager;
    private final SettlementManager settlementManager;
    private final DynamicWorldEventSystem dynamicWorldEventSystem;
    private final WarPhaseManager warPhaseManager;
    private final ShardRunManager shardRunManager;
    private final Map<String, Boolean> openedChestStates = new HashMap<>();
    private final List<String> harvestedFrontierFeatureIds = new ArrayList<>();
    private final List<String> claimedFrontierBaseSiteIds = new ArrayList<>();
    private final Map<String, BaseState> baseStatesByZoneId = new HashMap<>();
    private final Map<String, GuildDefinition> guildDefinitionsById = new HashMap<>();
    private final List<BaseDefenderUnit> activeBaseDefenders = new ArrayList<>();
    private final List<String> keyItems = new ArrayList<>();
    private TmxWorldLoader.LoadedZone currentZone;
    private ZoneDefinition currentZoneDefinition;
    private FrontierTerrainSampler frontierTerrainSampler;
    private FrontierBiomeCatalog frontierBiomeCatalog;
    private final FrontierChunkManager frontierChunkManager = new FrontierChunkManager();
    private final SettlementTimeManager settlementTimeManager;
    private final Vector2 floatingOriginOffset = new Vector2();
    private final Rectangle localWorldBounds = new Rectangle();
    private String difficultyMode = "NORMAL";
    private final MetaProgressionState metaProgressionState;

    // Robot attack visualization (current frame only)
    private final List<RobotAttackLine> robotAttackLines = new ArrayList<>();

    // Gold and stats
    private long totalGold = 0;
    private int totalEnemiesKilled = 0;
    private float survivalTime = 0f;
    private long worldSeed = 0L;
    private int healingPotions = 3;
    private int playerLevel = 1;
    private int playerExperience = 0;

    // Gold popup floating text
    private List<GoldPopup> goldPopups = new ArrayList<>();
    private Map<String, Map<String, String>> robotEquipment = new HashMap<>();
    private Map<String, String> playerEquipment = new HashMap<>();
    private List<String> collectedRobotIds = new ArrayList<>();
    private List<String> activeRobotIds = new ArrayList<>();
    private String currentZoneId = "town";
    private int currentSaveSlot = 0;
    private String activeDialog = null;
    private String activeSpeaker = null;
    private boolean questMenuOpen = false;
    private int questMenuTabIndex = 0;
    private int strategicMapSelectionIndex = 0;
    private boolean guildMenuOpen = false;
    private boolean expeditionBoardOpen = false;
    private int expeditionBoardSelectionIndex = 0;
    private String pinnedExpeditionContractTitle = null;
    private String pinnedExpeditionContractText = null;
    private String pinnedExpeditionContractZoneId = null;
    private String pinnedExpeditionContractKind = null;
    private String pinnedExpeditionContractTargetId = null;
    private boolean pinnedExpeditionContractCompleted = false;
    private int expeditionBoardReputation = 0;
    private final Map<String, Integer> factionInfluenceById = new HashMap<>();
    private final Map<String, String> activeWorldBossFrontsByZoneId = new HashMap<>();
    private final Map<String, String> activeRegionalIncidentsByZoneId = new HashMap<>();
    private final Map<String, String> activeSettlementCrisesByZoneId = new HashMap<>();
    private final List<PlayerQuestContract> playerQuestContracts = new ArrayList<>();
    private final List<PlayerCreatedNpc> playerCreatedNpcs = new ArrayList<>();
    private int guildMenuSelectionIndex = 0;
    private boolean buildModeOpen = false;
    private int selectedBuildStructureIndex = 0;
    private String activeClaimGuildId = null;
    private String expeditionBoardMode = ENDGAME_MODE_STANDARD;
    private int selectedChallengeModifierIndex = 0;
    private int selectedHardModeSeedIndex = 0;
    private String activeChallengeModifierId = null;
    private int activeHardModeSeedIndex = 0;
    private static final String OPENING_HOME_INTRO_FLAG = "intro.player_home_seen";
    private static final String[] QUEST_MENU_TABS = {"Quests", "Command", "War", "Map", "Materials", "Shards", "Blueprints", "Items"};
    private static final String[] WATCHED_DEFINITION_PATHS = {
        StaticDataPaths.ABILITIES,
        StaticDataPaths.ZONES,
        StaticDataPaths.MONSTERS,
        StaticDataPaths.ROBOTS,
        StaticDataPaths.FORGE_COMPONENTS,
        StaticDataPaths.BLUEPRINT_FRAGMENTS,
        StaticDataPaths.FORGE_RECIPES,
        StaticDataPaths.STORY_EVENTS,
        StaticDataPaths.SHOPS,
        StaticDataPaths.EQUIPMENT,
        StaticDataPaths.QUESTS,
        StaticDataPaths.DIALOGUE,
        StaticDataPaths.WORLD_STATE,
        StaticDataPaths.RECRUITMENT,
        StaticDataPaths.SETTLEMENT_UPGRADES,
        StaticDataPaths.SETTLEMENT_NPC_SCHEDULES
    };
    private boolean pendingOpeningCutscene = false;
    private final List<DialogueSystem.DialoguePage> activeDialogueSequence = new ArrayList<>();
    private int activeDialogueSequenceIndex = 0;
    private int dialogPageIndex = 0;
    private String dialogPageTrackingText = null;
    private String dialogPageTrackingSpeaker = null;
    private final Map<String, Long> watchedDefinitionTimestamps = new HashMap<>();
    private final List<String> changedDefinitionPaths = new ArrayList<>();
    private float definitionWatchPollTimer;
    private boolean pendingNpcScheduleRefresh = true;

    private boolean isPaused = false;
    private boolean battleActive = false;
    private final List<String> pendingRobotAwakeningMessages = new ArrayList<>();
    private GameInputProcessor gameInputProcessor;
    private InputContextRouter inputContextRouter;
    private final DebugOverlay debugOverlay;

    public GameScreen(RogueForgeGame game, ScreenManager screenManager) {
        this(game.getContext(), null);
    }

    public GameScreen(RogueForgeGame game, ScreenManager screenManager, SaveFile saveFile) {
        this(game.getContext(), saveFile);
    }

    public GameScreen(GameContext context) {
        this(context, null);
    }

    public GameScreen(GameContext context, SaveFile saveFile) {
        this.context = context;
        this.game = context.getGame();
        this.screenManager = context.getScreenManager();
        this.engineServices = context.getEngineServices();
        this.baseBuildingEngine = engineServices.getBaseBuildingEngine();
        this.baseDefenseDirector = engineServices.getBaseDefenseDirector();
        this.cyberneticEnhancementEngine = engineServices.getCyberneticEnhancementEngine();
        this.guildPermissionsEngine = engineServices.getGuildPermissionsEngine();
        this.worldLoader = engineServices.getWorldLoader();
        this.infiniteDungeonLayoutGenerator = engineServices.getInfiniteDungeonLayoutGenerator();
        this.frontierZoneGenerator = engineServices.getFrontierZoneGenerator();
        this.worldGenerator = engineServices.getWorldGenerator();
        this.environmentalInteractionSystem = engineServices.getEnvironmentalInteractionSystem();
        this.timeSystem = engineServices.getTimeSystem();
        this.settingsManager = engineServices.getSettingsManager();
        this.saveManager = engineServices.getSaveManager();
        this.metaProgressionManager = engineServices.getMetaProgressionManager();
        this.questManager = engineServices.getQuestManager();
        this.dialogueSystem = engineServices.getDialogueSystem();
        this.worldStateManager = engineServices.getWorldStateManager();
        this.recruitmentManager = engineServices.getRecruitmentManager();
        this.settlementManager = engineServices.getSettlementManager();
        this.dynamicWorldEventSystem = engineServices.getDynamicWorldEventSystem();
        this.warPhaseManager = engineServices.getWarPhaseManager();
        this.settlementTimeManager = timeSystem.getClock();
        game.getEventBus().subscribe(this);
        this.gameLoop = new GameLoop();
        this.gameCamera = new OrthographicCamera();
        this.uiCamera = new OrthographicCamera();
        this.cameraController = new CameraController(gameCamera);
        this.gameViewport = new FitViewport(WORLD_VIEW_WIDTH, WORLD_VIEW_HEIGHT, gameCamera);
        this.uiViewport = new ScreenViewport(uiCamera);
        this.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.hudOverlay = new HUDOverlay(game);
        this.debugOverlay = new DebugOverlay(this::buildDebugOverlaySections, true);
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.zoneLoader = new ZoneLoader(game, worldLoader);
        this.difficultyMode = settingsManager.getSettings().getDifficultyMode();
        loadVisualAssets();
        this.playerName = saveFile != null && saveFile.getPlayerName() != null
            ? saveFile.getPlayerName()
            : "Player";
        this.metaProgressionState = metaProgressionManager.load();
        migrateLegacyLegendaryUnlocks();
        this.worldSeed = saveFile != null && saveFile.getWorldSeed() != 0L
            ? saveFile.getWorldSeed()
            : generateWorldSeed();
        this.gameState = new GameState(playerName);

        for (int i = 0; i < ROBOT_COUNT; i++) {
            RobotCompanion robot = new RobotCompanion();
            robot.angleDeg = 180f;
            robot.facing = new Vector2(0f, -1f);
            robot.pos = new Vector2(playerPos.x, playerPos.y - ((i + 1) * ROBOT_FOLLOW_GAP));
            robot.health = ROBOT_MAX_HEALTH;
            robot.maxHealth = ROBOT_MAX_HEALTH;
            initializeRobotStats(robot, i);
            robot.grade = i == 0 ? "G" : (i == 1 ? "F" : "E");
            robots[i] = robot;
        }
        if (activeRobotIds.isEmpty()) {
            activeRobotIds.add("scout_mk1");
        }
        normalizeActiveRobotSlots();
        clampActiveRobotsToUnlockedSlots();
        if (collectedRobotIds.isEmpty()) {
            collectedRobotIds.add("scout_mk1");
        }
        gameState.setActiveRobotIds(activeRobotIds);
        gameState.setCollectedRobotIds(collectedRobotIds);
        gameState.setCurrentZoneId(currentZoneId);
        gameState.setCurrentSaveSlot(currentSaveSlot);
        gameState.setPlayerHealth(playerHealth);
        gameState.setPlayerMaxHealth(playerMaxHealth);
        gameState.setPlayerLevel(playerLevel);
        gameState.setPlayerExperience(playerExperience);
        gameState.setHealingPotions(healingPotions);
        gameState.setTotalGold(totalGold);
        this.shardRunManager = new ShardRunManager(gameState, forgeLegacyEngine);
        worldStateManager.initialize(gameState);
        questManager.initialize(gameState);

        initializeEquipmentCatalog();
        loadZoneDefinitions();
        loadRobotDefinitions();
        loadMonsterDefinitions();
        loadForgeComponentDefinitions();
        loadBlueprintFragmentDefinitions();
        loadForgeRecipes();
        loadStoryEvents();
        loadShopDefinitions();
        ensureRobotProgressionStates();
        initializeDefinitionWatcher();
        initializeActiveRobotHealthForNewRun();
        loadZone(currentZoneId, saveFile == null ? "home_spawn" : "town_square", true);

        hudOverlay.setPlayerHealth(playerHealth, playerMaxHealth);
        hudOverlay.setCurrency(totalGold);
        hudOverlay.setRobotHealth(getRobotHealthValues(), getRobotMaxHealthValues());

        if (saveFile != null) {
            loadFromSave(saveFile);
        } else {
            applyMetaEnhancementsToFreshRun();
            pendingOpeningCutscene = true;
        }
        ensureWarPhaseStateInitialized();
        syncAct5EndgameState(false);
    }

    private void initializeActiveRobotHealthForNewRun() {
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (!hasActiveRobotAt(i)) {
                robots[i].health = 0f;
                continue;
            }
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            float maxHealth = getRobotEffectiveMaxHealth(getRobotId(i), state);
            robots[i].health = maxHealth;
            if (state != null) {
                state.setCurrentHealth(maxHealth);
            }
        }
    }

    @Override
    public void show() {
        ensureGameInputProcessor();
        triggerOpeningCutsceneIfNeeded();
    }

    private void ensureGameInputProcessor() {
        if (gameInputProcessor == null) {
            gameInputProcessor = new GameInputProcessor(this);
        }
        if (inputContextRouter == null) {
            inputContextRouter = new InputContextRouter();
            inputContextRouter.addSharedProcessor(debugOverlay.getInputProcessor());
            inputContextRouter.setProcessors(InputContext.EXPLORATION, gameInputProcessor);
            inputContextRouter.setProcessors(InputContext.DIALOG, gameInputProcessor);
            inputContextRouter.setProcessors(InputContext.SETTLEMENT, gameInputProcessor);
            inputContextRouter.setProcessors(InputContext.BUILD, gameInputProcessor);
        }
        inputContextRouter.activate(resolveInputContext());
    }

    private void loadVisualAssets() {
        groundTileTexture = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_03.png");
        wallTileTexture = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_57.png");
        villageGroundTileTexture = loadTexture("village/grass_a.png");
        villageWallTileTexture = loadTexture("village/grass_b.png");
        doorTexture = loadTexture("2 Dungeon Tileset/3 Animated objects/Door_S.png");
        chestTexture = loadTexture("2 Dungeon Tileset/3 Animated objects/Chest1_S.png");
        shadowTexture = loadTexture("1 Characters/Other/Shadow.png");
        playerAnimation = loadAnimationSet("1 Characters/1");
        robotAnimations = new AnimationSet[] {
            loadAnimationSet("1 Characters/2"),
            loadAnimationSet("1 Characters/3"),
            loadSingleFrameSet("sprites/Tank_Robot.png", 32, 32)
        };
        enemyAnimations = new AnimationSet[] {
            loadAnimationSet("3 Dungeon Enemies/1"),
            loadAnimationSet("3 Dungeon Enemies/2"),
            loadAnimationSet("3 Dungeon Enemies/3"),
            loadAnimationSet("3 Dungeon Enemies/4")
        };
        npcAnimations = new AnimationSet[] {
            loadAnimationSet("1 Characters/2"),
            loadAnimationSet("1 Characters/3"),
            loadAnimationSet("1 Characters/1")
        };
    }

    private Texture loadTexture(String relativePath) {
        managedTexturePaths.add(relativePath);
        return game.loadTexture(relativePath);
    }

    private TextureRegion loadStripFrame(String relativePath, int frameWidth, int frameHeight) {
        Texture texture = loadTexture(relativePath);
        return new TextureRegion(texture, 0, 0, Math.min(frameWidth, texture.getWidth()), Math.min(frameHeight, texture.getHeight()));
    }

    private TextureRegion[] loadStripFrames(String relativePath, int frameWidth, int frameHeight) {
        Texture texture = loadTexture(relativePath);
        int frameCount = Math.max(1, texture.getWidth() / frameWidth);
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(texture, i * frameWidth, 0, Math.min(frameWidth, texture.getWidth() - (i * frameWidth)), Math.min(frameHeight, texture.getHeight()));
        }
        return frames;
    }

    private AnimationSet loadAnimationSet(String basePath) {
        return new AnimationSet(
            loadStripFrames(basePath + "/D_Idle.png", 32, 32),
            loadStripFrames(basePath + "/D_Walk.png", 32, 32),
            loadStripFrames(basePath + "/S_Idle.png", 32, 32),
            loadStripFrames(basePath + "/S_Walk.png", 32, 32),
            loadStripFrames(basePath + "/U_Idle.png", 32, 32),
            loadStripFrames(basePath + "/U_Walk.png", 32, 32)
        );
    }

    private AnimationSet loadSingleFrameSet(String path, int frameWidth, int frameHeight) {
        TextureRegion frame = loadStripFrame(path, frameWidth, frameHeight);
        return new AnimationSet(new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame});
    }

    @Override
    public void render(float delta) {
        handleDefinitionReloadShortcut();
        pollDefinitionWatcher(delta);

        if (!isPaused && !battleActive) {
            if (questMenuOpen) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    questMenuOpen = false;
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                    cycleQuestMenuTab(-1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                    cycleQuestMenuTab(1);
                }
                if (questMenuTabIndex == 3) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                        cycleStrategicMapSelection(-1);
                    }
                    if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                        cycleStrategicMapSelection(1);
                    }
                    if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                        launchSelectedStrategicMapDestination();
                        return;
                    }
                }
            } else if (expeditionBoardOpen) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    expeditionBoardOpen = false;
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    cycleExpeditionBoardSelection(-1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    cycleExpeditionBoardSelection(1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                    cycleEndgameDeploymentMode(-1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                    cycleEndgameDeploymentMode(1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                    cycleHardModeSeed(1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
                    cycleChallengeModifier(1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    launchSelectedExpeditionDestination();
                    return;
                }
            } else if (guildMenuOpen) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.G) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    guildMenuOpen = false;
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                    cycleGuildSelection(-1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    cycleGuildSelection(1);
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                    createAndSelectGuild();
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    postPlayerQuestForSelectedGuild();
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                    createPlayerNpcForSelectedGuild();
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    selectHighlightedGuildForClaims();
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) || Gdx.input.isKeyJustPressed(Input.Keys.DEL)) {
                    clearActiveClaimGuildSelection();
                    return;
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                    questMenuOpen = true;
                    questMenuTabIndex = 0;
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    expeditionBoardOpen = false;
                    guildMenuOpen = false;
                    questMenuOpen = true;
                    questMenuTabIndex = Math.min(1, QUEST_MENU_TABS.length - 1);
                    return;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                    guildMenuOpen = true;
                    clampGuildMenuSelection();
                    return;
                }
            }
            if (!questMenuOpen && !expeditionBoardOpen && !guildMenuOpen && Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                toggleBuildMode();
                return;
            }
        }

        if (!isPaused && !battleActive && !questMenuOpen && !expeditionBoardOpen && !guildMenuOpen && !buildModeOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                pauseGame();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                openWorkshop();
                return;
            }
        }

        if (!isPaused && !battleActive && buildModeOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                buildModeOpen = false;
                return;
            }
            handleBuildModeShortcuts();
        }

        if (!isPaused && !questMenuOpen && !expeditionBoardOpen && !guildMenuOpen) {
            gameLoop.update(delta);
            updatePlayer(delta);
            updateRobots(delta);
            updateBaseDefenders(delta);
            updateEnemies(delta);
            updateNpcSchedules(delta);
            updateAttackEffects(delta);
            updateGoldPopups(delta);
            timeSystem.update(delta);
            survivalTime += delta;

            // Check player death
            if (!hasLivingPartyMember()) {
                transitionToGameOver();
                return;
            }
        }

        // Camera follows player
        gameViewport.apply();
        cameraController.setTarget(playerPos);
        Vector2 worldShift = cameraController.update(delta);
        if (worldShift != null && !worldShift.isZero()) {
            applyFloatingOriginShift(worldShift);
            cameraController.setTarget(playerPos);
            cameraController.snapToTarget();
        }
        if (shouldUseFrontierStreaming() && currentZone != null) {
            frontierChunkManager.update(playerPos, currentZone.tileWidth, currentZone.tileHeight);
        }

        float daylight = settlementTimeManager.getDaylightStrength();
        Gdx.gl.glClearColor(0.04f + (0.08f * daylight), 0.05f + (0.09f * daylight), 0.07f + (0.03f * daylight), 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawGroundTiles();
        drawBuildPreview();

        // Draw enemies
        drawHouses();
        drawNpcs();
        drawEnemies();

        drawPlayerSprite();
        drawAttackFlash();

        drawBaseDefenders();
        drawRobots();
        drawRobotAttackLines();

        // Draw gold popups
        drawGoldPopups();

        // HUD on top
        hudOverlay.render();
        if (!expeditionBoardOpen) {
            drawDialogOverlay();
        }
        drawQuestOverlay();
        drawExpeditionBoardOverlayExpanded();
        drawGuildOverlay();
        drawBuildOverlay();
        debugOverlay.render();

        robotAttackLines.clear();
    }

    private void drawBuildPreview() {
        if (!buildModeOpen || currentZone == null) {
            return;
        }
        StructureDefinition structureDefinition = getSelectedBuildStructure();
        if (structureDefinition == null) {
            return;
        }
        BasePlacementResult placement = getCurrentBuildPlacementResult(structureDefinition);
        float[] preview = getBuildPreviewOrigin(structureDefinition);
        Rectangle bounds = new Rectangle(
            preview[0],
            preview[1],
            structureDefinition.getWidthTiles() * currentZone.tileWidth,
            structureDefinition.getHeightTiles() * currentZone.tileHeight
        );

        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Color fill = placement != null && placement.isAllowed()
            ? new Color(0.24f, 0.72f, 0.44f, 0.26f)
            : new Color(0.88f, 0.24f, 0.20f, 0.26f);
        shapeRenderer.setColor(fill);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();
    }

    private void spawnEnemies() {
        enemies.clear();
        if (currentZone == null || currentZone.safeZone || currentZone.enemySpawns.size == 0) {
            enemyRespawnTimer = 0f;
            return;
        }
        int count = isInfiniteDungeonZone()
            ? getInfiniteDungeonEnemyCount()
            : Math.min(INITIAL_ENEMY_COUNT, currentZone.enemySpawns.size);
        for (int i = 0; i < count; i++) {
            Vector2 spawn = currentZone.enemySpawns.get(i % currentZone.enemySpawns.size);
            enemies.add(createZoneEnemy(spawn, i));
        }
        enemyRespawnTimer = 0f;
    }

    private Enemy createZoneEnemy(Vector2 spawnPoint, int index) {
        float jitterX = ((float) Math.random() - 0.5f) * 40f;
        float jitterY = ((float) Math.random() - 0.5f) * 40f;
        float x = spawnPoint.x + jitterX;
        float y = spawnPoint.y + jitterY;
        MonsterDefinition monster = isInfiniteDungeonZone()
            ? pickInfiniteDungeonMonster(index)
            : pickMonsterForCurrentZone(index);
        Enemy enemy = new Enemy();
        enemy.pos = new Vector2(x, y);
        enemy.facing = new Vector2(0f, -1f);
        enemy.speed = 60f + (float) Math.random() * 35f;
        enemy.size = ENEMY_SIZE;
        if (monster != null) {
            enemy.monsterId = monster.getId();
            enemy.hp = monster.getHp();
            enemy.maxHp = monster.getHp();
            enemy.defense = monster.getDefense();
            enemy.agility = monster.getSpeed();
            enemy.strength = monster.getAttack();
            enemy.intelligence = Math.max(8f, monster.getAttack() * 0.7f);
            enemy.stamina = Math.max(8f, monster.getDefense() * 1.1f);
            enemy.rewardGold = 0;
            enemy.rewardExperience = 20 + monster.getBaseLoot();
            enemy.name = monster.getName();
            enemy.speed = Math.max(50f, monster.getSpeed() * 1.2f);
        } else {
            enemy.hp = 30f;
            enemy.maxHp = 30f;
            enemy.defense = 4f + (float) Math.random() * 4f;
            enemy.agility = 16f + (float) Math.random() * 16f;
            enemy.strength = 12f + (float) Math.random() * 10f;
            enemy.intelligence = 8f + (float) Math.random() * 10f;
            enemy.stamina = 10f + (float) Math.random() * 10f;
            enemy.rewardGold = 0;
            enemy.rewardExperience = 25;
            enemy.name = "Scrap Beast";
        }
        if (isInfiniteDungeonZone()) {
            applyInfiniteDungeonScaling(enemy);
        }
        if (isActiveWorldBossFront(currentZoneId) && isWorldBossFrontTarget(enemy.monsterId)) {
            applyWorldBossFrontScaling(enemy);
        }
        applyZoneWarConditionScaling(enemy, currentZoneId);
        enemy.alive = true;
        enemy.attackCooldown = ENEMY_MELEE_COOLDOWN;
        enemy.attackTimer = 0f;
        enemy.spriteIndex = Math.floorMod(index, enemyAnimations.length);
        enemy.patrolTarget = randomPatrolTarget(spawnPoint);
        return enemy;
    }

    private boolean isWorldBossFrontTarget(String monsterId) {
        String activeBossId = getActiveWorldBossFrontBossId(currentZoneId);
        return monsterId != null && activeBossId != null && monsterId.equals(activeBossId);
    }

    private void applyWorldBossFrontScaling(Enemy enemy) {
        if (enemy == null) {
            return;
        }
        String behavior = getWorldBossFrontBehavior(currentZoneId);
        enemy.maxHp *= 1.35f;
        enemy.hp = enemy.maxHp;
        enemy.strength *= 1.18f;
        enemy.defense *= 1.12f;
        enemy.intelligence *= 1.18f;
        enemy.stamina *= 1.14f;
        enemy.agility *= 1.08f;
        enemy.speed *= 1.06f;
        enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.45f));
        if ("Siege".equals(behavior)) {
            enemy.maxHp *= 1.12f;
            enemy.hp = enemy.maxHp;
            enemy.defense *= 1.15f;
            enemy.stamina *= 1.12f;
        } else if ("Hunter-Killer".equals(behavior)) {
            enemy.strength *= 1.08f;
            enemy.intelligence *= 1.1f;
            enemy.agility *= 1.14f;
            enemy.speed *= 1.12f;
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.12f));
        } else if ("Breakthrough".equals(behavior)) {
            enemy.strength *= 1.14f;
            enemy.intelligence *= 1.14f;
        } else if ("Fortified".equals(behavior)) {
            enemy.maxHp *= 1.08f;
            enemy.hp = enemy.maxHp;
            enemy.defense *= 1.14f;
        }
        if (enemy.name != null && !enemy.name.contains("Front")) {
            enemy.name = enemy.name + " " + behavior + " Front";
        }
    }

    private boolean isZoneClaimedForWar(String zoneId) {
        BaseState baseState = zoneId != null ? baseStatesByZoneId.get(zoneId) : null;
        return baseState != null && !baseState.getClaimedSiteIds().isEmpty();
    }

    private boolean isGuildHallWarZone(String zoneId) {
        return getPublishingGuildForZone(zoneId) != null;
    }

    private int countActiveStructuresByCategory(BaseState baseState, StructureCategory category) {
        if (baseState == null || category == null) {
            return 0;
        }
        int count = 0;
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure == null || !structure.isActive() || structure.getCurrentHitPoints() <= 0) {
                continue;
            }
            StructureDefinition definition = baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId());
            if (definition != null && definition.getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    private int getZoneInfrastructureSupportScore(String zoneId) {
        BaseState baseState = zoneId != null ? baseStatesByZoneId.get(zoneId) : null;
        if (baseState == null) {
            return 0;
        }
        return countActiveStructuresByCategory(baseState, StructureCategory.STORAGE) * 2
            + countActiveStructuresByCategory(baseState, StructureCategory.DEFENSE) * 2
            + countActiveStructuresByCategory(baseState, StructureCategory.WALL)
            + countActiveStructuresByCategory(baseState, StructureCategory.UTILITY)
            + countActiveStructuresByCategory(baseState, StructureCategory.POWER)
            + countActiveStructuresByCategory(baseState, StructureCategory.CRAFTING);
    }

    private String getWorldBossFrontBehavior(String zoneId) {
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        if (hostile >= 65) {
            return "Siege";
        }
        if (zoneId != null && isGuildHallWarZone(zoneId) && guilds >= 55) {
            return "Hunter-Killer";
        }
        if (zoneId != null && isZoneClaimedForWar(zoneId) && command >= 60) {
            return "Fortified";
        }
        return "Breakthrough";
    }

    private String getWorldBossFrontVariantName(String zoneId) {
        String behavior = getWorldBossFrontBehavior(zoneId);
        if ("sky_fortress".equals(zoneId)) {
            return "Siege".equals(behavior) ? "Ashen Crown Encirclement"
                : "Hunter-Killer".equals(behavior) ? "Raptor Lattice Pursuit"
                : "Fortified".equals(behavior) ? "Bastion Halo Lock"
                : "Furnace Ascent Break";
        }
        if ("shadow_caves".equals(zoneId)) {
            return "Siege".equals(behavior) ? "Gravesignal Choke"
                : "Hunter-Killer".equals(behavior) ? "Ghostwire Pursuit"
                : "Fortified".equals(behavior) ? "Burial Vault Lock"
                : "Dusk Echo Breach";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "Siege".equals(behavior) ? "Prism Tomb Lockdown"
                : "Hunter-Killer".equals(behavior) ? "Shardfang Pursuit"
                : "Fortified".equals(behavior) ? "Cathedral Vein Hold"
                : "Glassfall Break";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "Siege".equals(behavior) ? "Iron Pit Encirclement"
                : "Hunter-Killer".equals(behavior) ? "Scrap Hound Sweep"
                : "Fortified".equals(behavior) ? "Quarry Bulwark Ring"
                : "Breaker Line Surge";
        }
        return getZoneDisplayName(zoneId) + " " + behavior + " Front";
    }

    private String getWorldBossFrontVariantBonusText(String zoneId) {
        if ("sky_fortress".equals(zoneId)) {
            return "+1 board reputation and superior summit salvage";
        }
        if ("shadow_caves".equals(zoneId)) {
            return "+1 Forge Shard from signal-ghost salvage";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "+25 gold and +1 Forge Shard from crystal war salvage";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "+25 gold from reclaimed extraction stock";
        }
        return "enhanced theater payout";
    }

    private String getRegionalIncidentName(String zoneId) {
        String incidentId = zoneId != null ? activeRegionalIncidentsByZoneId.get(zoneId) : null;
        if ("ghost_signal".equals(incidentId)) {
            return "Ghost Signal Cascade";
        }
        if ("shard_storm".equals(incidentId)) {
            return "Shard Storm Bloom";
        }
        if ("air_raid_siren".equals(incidentId)) {
            return "Air-Raid Siren Window";
        }
        if ("scrap_stampede".equals(incidentId)) {
            return "Scrap Stampede";
        }
        if ("frontier_blackout".equals(incidentId)) {
            return "Frontier Blackout";
        }
        return null;
    }

    private String getRegionalIncidentSummary(String zoneId) {
        String incidentId = zoneId != null ? activeRegionalIncidentsByZoneId.get(zoneId) : null;
        if ("ghost_signal".equals(incidentId)) {
            return "signal ghosts are confusing recovery teams and drawing scavengers into the cave dark";
        }
        if ("shard_storm".equals(incidentId)) {
            return "crystal weather is weaponizing the shelf and enriching salvage for crews that survive it";
        }
        if ("air_raid_siren".equals(incidentId)) {
            return "sky lanes are briefly open but brutally contested";
        }
        if ("scrap_stampede".equals(incidentId)) {
            return "rogue machine herds are crushing extraction paths and spilling salvage";
        }
        if ("frontier_blackout".equals(incidentId)) {
            return "power loss and dusk-static are degrading route certainty";
        }
        return null;
    }

    private String getSettlementCrisisName(String zoneId) {
        String crisisId = zoneId != null ? activeSettlementCrisesByZoneId.get(zoneId) : null;
        if ("frontline_panic".equals(crisisId)) {
            return "Frontline Panic";
        }
        if ("supply_breakdown".equals(crisisId)) {
            return "Supply Breakdown";
        }
        if ("defender_overstretch".equals(crisisId)) {
            return "Defender Overstretch";
        }
        return null;
    }

    private String getSettlementCrisisSummary(String zoneId) {
        String crisisId = zoneId != null ? activeSettlementCrisesByZoneId.get(zoneId) : null;
        if ("frontline_panic".equals(crisisId)) {
            return "the outpost is too close to the active front and morale is slipping";
        }
        if ("supply_breakdown".equals(crisisId)) {
            return "light infrastructure and repeated pressure are choking route upkeep";
        }
        if ("defender_overstretch".equals(crisisId)) {
            return "reserve bots are covering too much ground and repair cycles are lagging";
        }
        return null;
    }

    private String getRegionalIncidentArrivalText(String zoneId) {
        String incidentId = zoneId != null ? activeRegionalIncidentsByZoneId.get(zoneId) : null;
        if ("ghost_signal".equals(incidentId)) {
            return "Field dispatch: ghost signals are ricocheting through the cave band. Recovery crews are losing bearings and hostile scavengers are moving on the echoes.";
        }
        if ("shard_storm".equals(incidentId)) {
            return "Field dispatch: crystal weather has turned violent. The shelf is shedding shard-fire, but anything you bring out of it will be worth the risk.";
        }
        if ("air_raid_siren".equals(incidentId)) {
            return "Field dispatch: the air-raid window is open. Sky lanes are briefly usable, but every hostile gunline knows it too.";
        }
        if ("scrap_stampede".equals(incidentId)) {
            return "Field dispatch: rogue machine herds are stampeding the quarry lanes and smashing extraction lines apart.";
        }
        if ("frontier_blackout".equals(incidentId)) {
            return "Field dispatch: the region is in blackout. Static, low power, and bad sightlines are making every route feel half-lost.";
        }
        return null;
    }

    private String getSettlementCrisisArrivalText(String zoneId) {
        String crisisId = zoneId != null ? activeSettlementCrisesByZoneId.get(zoneId) : null;
        if ("frontline_panic".equals(crisisId)) {
            return "Outpost dispatch: this settlement is hugging the active front too closely. Morale is buckling and the crews need a visible win fast.";
        }
        if ("supply_breakdown".equals(crisisId)) {
            return "Outpost dispatch: route upkeep has broken down. Storage is thin, relay discipline is worse, and one good delivery could steady the whole line.";
        }
        if ("defender_overstretch".equals(crisisId)) {
            return "Outpost dispatch: reserve bots are stretched across too many approach lanes. The next pressure spike could break the repair cycle.";
        }
        return null;
    }

    private void maybeShowWarArrivalDispatch(String zoneId) {
        if (zoneId == null || zoneId.isEmpty() || hasActiveDialog()) {
            return;
        }
        String incidentId = activeRegionalIncidentsByZoneId.get(zoneId);
        if (incidentId != null && !incidentId.isEmpty()) {
            String flag = "war.dispatch.incident." + zoneId + "." + incidentId;
            if (!worldStateManager.isFlagActive(gameState, flag)) {
                worldStateManager.setFlag(gameState, flag, true);
                showStandaloneDialog("Field Dispatch", getRegionalIncidentArrivalText(zoneId));
                return;
            }
        }
        String crisisId = activeSettlementCrisesByZoneId.get(zoneId);
        if (crisisId != null && !crisisId.isEmpty()) {
            String flag = "war.dispatch.crisis." + zoneId + "." + crisisId;
            if (!worldStateManager.isFlagActive(gameState, flag)) {
                worldStateManager.setFlag(gameState, flag, true);
                showStandaloneDialog("Outpost Dispatch", getSettlementCrisisArrivalText(zoneId));
            }
        }
    }

    private void resolveRegionalIncident(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return;
        }
        String incidentId = activeRegionalIncidentsByZoneId.remove(zoneId);
        if (incidentId != null && !incidentId.isEmpty()) {
            worldStateManager.setFlag(gameState, "war.thread.cleared." + zoneId, true);
        }
        propagateRegionalIncidentResolution(zoneId, incidentId);
    }

    private void resolveSettlementCrisis(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return;
        }
        String crisisId = activeSettlementCrisesByZoneId.remove(zoneId);
        if (crisisId != null && !crisisId.isEmpty()) {
            worldStateManager.setFlag(gameState, "war.thread.cleared." + zoneId, true);
        }
        propagateSettlementCrisisResolution(zoneId, crisisId);
    }

    private void propagateRegionalIncidentResolution(String zoneId, String incidentId) {
        if (zoneId == null || zoneId.isEmpty() || incidentId == null || incidentId.isEmpty()) {
            return;
        }
        List<String> adjacentZones = getAdjacentWarZones(zoneId);
        if (adjacentZones.isEmpty()) {
            return;
        }
        if (factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0) >= 60) {
            for (String adjacentZoneId : adjacentZones) {
                if (activeSettlementCrisesByZoneId.containsKey(adjacentZoneId)) {
                    activeSettlementCrisesByZoneId.remove(adjacentZoneId);
                    worldStateManager.setFlag(gameState, "war.consequence.stabilized." + adjacentZoneId, true);
                    return;
                }
            }
        }
        String spillZoneId = getMostVulnerableAdjacentZone(adjacentZones);
        if (spillZoneId != null
            && !activeRegionalIncidentsByZoneId.containsKey(spillZoneId)
            && shouldZoneReceiveRegionalIncident(spillZoneId)
            && factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) >= 55) {
            activeRegionalIncidentsByZoneId.put(spillZoneId, remapRegionalIncidentForZone(incidentId, spillZoneId));
            worldStateManager.setFlag(gameState, "war.consequence.spillover." + spillZoneId, true);
        }
    }

    private void propagateSettlementCrisisResolution(String zoneId, String crisisId) {
        if (zoneId == null || zoneId.isEmpty() || crisisId == null || crisisId.isEmpty()) {
            return;
        }
        List<String> adjacentZones = getAdjacentWarZones(zoneId);
        if (adjacentZones.isEmpty()) {
            return;
        }
        for (String adjacentZoneId : adjacentZones) {
            if (activeSettlementCrisesByZoneId.containsKey(adjacentZoneId)
                && getZoneInfrastructureSupportScore(adjacentZoneId) >= 4) {
                activeSettlementCrisesByZoneId.remove(adjacentZoneId);
                worldStateManager.setFlag(gameState, "war.consequence.relief." + adjacentZoneId, true);
                return;
            }
        }
        String spillZoneId = getMostVulnerableAdjacentZone(adjacentZones);
        if (spillZoneId != null
            && !activeSettlementCrisesByZoneId.containsKey(spillZoneId)
            && isZoneClaimedForWar(spillZoneId)
            && getZoneInfrastructureSupportScore(spillZoneId) <= 3
            && factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) >= 58) {
            activeSettlementCrisesByZoneId.put(spillZoneId, remapSettlementCrisisForZone(crisisId, spillZoneId));
            worldStateManager.setFlag(gameState, "war.consequence.chain." + spillZoneId, true);
        }
    }

    private List<String> getAdjacentWarZones(String zoneId) {
        if ("verdant_fields".equals(zoneId)) {
            return List.of("whispering_forest", "rusty_quarry", "shadow_caves");
        }
        if ("whispering_forest".equals(zoneId)) {
            return List.of("verdant_fields", "shadow_caves");
        }
        if ("shadow_caves".equals(zoneId)) {
            return List.of("whispering_forest", "verdant_fields", "crystal_depths");
        }
        if ("rusty_quarry".equals(zoneId)) {
            return List.of("verdant_fields", "crystal_depths", "sky_fortress");
        }
        if ("crystal_depths".equals(zoneId)) {
            return List.of("shadow_caves", "rusty_quarry", "sky_fortress");
        }
        if ("sky_fortress".equals(zoneId)) {
            return List.of("rusty_quarry", "crystal_depths");
        }
        return List.of();
    }

    private String getMostVulnerableAdjacentZone(List<String> adjacentZones) {
        String selectedZoneId = null;
        int lowestSupport = Integer.MAX_VALUE;
        for (String adjacentZoneId : adjacentZones) {
            if (adjacentZoneId == null || adjacentZoneId.isEmpty() || !zoneDefinitions.containsKey(adjacentZoneId)) {
                continue;
            }
            int support = getZoneInfrastructureSupportScore(adjacentZoneId);
            if (!isZoneClaimedForWar(adjacentZoneId)) {
                support -= 2;
            }
            if (support < lowestSupport) {
                lowestSupport = support;
                selectedZoneId = adjacentZoneId;
            }
        }
        return selectedZoneId;
    }

    private String remapRegionalIncidentForZone(String incidentId, String zoneId) {
        if ("sky_fortress".equals(zoneId)) {
            return "air_raid_siren";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "shard_storm";
        }
        if ("shadow_caves".equals(zoneId)) {
            return "ghost_signal";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "scrap_stampede";
        }
        return incidentId != null && !incidentId.isEmpty() ? incidentId : "frontier_blackout";
    }

    private String remapSettlementCrisisForZone(String crisisId, String zoneId) {
        if (isActiveWorldBossFront(zoneId)) {
            return "frontline_panic";
        }
        if (getZoneInfrastructureSupportScore(zoneId) <= 2) {
            return "supply_breakdown";
        }
        return crisisId != null && !crisisId.isEmpty() ? crisisId : "defender_overstretch";
    }

    private String getWorldBossFrontEffectSummary(String zoneId) {
        String behavior = getWorldBossFrontBehavior(zoneId);
        if ("Siege".equals(behavior)) {
            return "extra fortress mass, heavier armor, and sustained pressure on owned corridors";
        }
        if ("Hunter-Killer".equals(behavior)) {
            return "faster kill pressure, sharper strikes, and aggressive pursuit";
        }
        if ("Fortified".equals(behavior)) {
            return "reinforced plating, stronger guard posture, and higher defensive endurance";
        }
        return "high-tempo breakthrough pressure with elevated attack output";
    }

    private List<String> getFactionDirectiveLines() {
        List<String> lines = new ArrayList<>();
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        if (hostile >= 65) {
            lines.add("Hostile directive: sabotage outer lanes, force high-pressure raids, and deny safe banking windows.");
        }
        if (command >= 55) {
            lines.add("Command directive: fortify claimed corridors, rotate convoy escorts, and hold front-line logistics intact.");
        }
        if (guilds >= 50) {
            lines.add("Guild directive: monetize stable corridors, post strike orders, and keep contract boards active.");
        }
        return lines;
    }

    private List<String> getWarBulletinLines() {
        List<String> lines = new ArrayList<>();
        if (!activeWorldBossFrontsByZoneId.isEmpty()) {
            for (Map.Entry<String, String> entry : activeWorldBossFrontsByZoneId.entrySet()) {
                lines.add(buildWarBulletinLine(entry.getKey(), entry.getValue()));
                if (lines.size() >= 2) {
                    break;
                }
            }
        }
        if (lines.size() < 2 && isHostileSabotageProjectActive()) {
            lines.add("Bulletin: sabotage cells are disrupting calmer routes and raising the value of secured corridors.");
        }
        if (lines.size() < 2 && isCoalitionExchangeProjectActive()) {
            lines.add("Bulletin: coalition brokers are paying premium rates for stabilized deliveries and funded guild lanes.");
        }
        if (lines.isEmpty()) {
            lines.add("Bulletin: no emergency theater update. Frontier command is rotating crews and waiting on the next pressure spike.");
        }
        return lines;
    }

    private String buildWarBulletinLine(String zoneId, String bossId) {
        String zoneName = getZoneDisplayName(zoneId);
        String bossName = getMonsterDisplayName(bossId);
        String behavior = getWorldBossFrontBehavior(zoneId).toLowerCase(Locale.ROOT);
        if ("shadow_caves".equals(zoneId)) {
            return "Bulletin: " + zoneName + " is throwing back old signal ghosts; a " + behavior
                + " front around " + bossName + " is dragging scavenger crews into the dark.";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "Bulletin: " + zoneName + " has entered a twilight surge. " + bossName
                + " is anchoring a " + behavior + " front through the crystal shelf.";
        }
        if ("sky_fortress".equals(zoneId)) {
            return "Bulletin: " + zoneName + " has become a hardcore ascent lane. " + bossName
                + " is holding a " + behavior + " front above the restored approach.";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "Bulletin: " + zoneName + " is grinding crews down by attrition. " + bossName
                + " now commands a " + behavior + " front over the extraction pits.";
        }
        return "Bulletin: " + zoneName + " reports a " + behavior + " front led by " + bossName + ".";
    }

    private float getZoneRaidPressureMultiplier(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return 1f;
        }
        float multiplier = 1f;
        int support = getZoneInfrastructureSupportScore(zoneId);
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        if (support > 0) {
            multiplier *= Math.max(0.76f, 1f - support * 0.03f);
        }
        if (activeSettlementCrisesByZoneId.containsKey(zoneId)) {
            multiplier *= 1.18f;
        }
        if (isZoneClaimedForWar(zoneId) && command >= 60) {
            multiplier *= 0.72f;
        }
        if (isGuildHallWarZone(zoneId) && guilds >= 55) {
            multiplier *= 0.86f;
        }
        if (hostile >= 60) {
            multiplier *= isZoneClaimedForWar(zoneId) ? 1.16f : 1.08f;
        }
        return Math.max(0.55f, Math.min(1.35f, multiplier));
    }

    private float getZoneRaidCooldownMultiplier(String zoneId) {
        float pressure = getZoneRaidPressureMultiplier(zoneId);
        if (activeSettlementCrisesByZoneId.containsKey(zoneId)) {
            return 0.78f;
        }
        if (pressure <= 0.85f) {
            return 1.2f;
        }
        if (pressure >= 1.1f) {
            return 0.85f;
        }
        return 1f;
    }

    private void applyFactionStructureSupport(BaseState baseState, float delta) {
        if (baseState == null || delta <= 0f || !hasOperationalBase(baseState)) {
            return;
        }
        String zoneId = baseState.getZoneId();
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        if (!(isZoneClaimedForWar(zoneId) && command >= 60) && !(isGuildHallWarZone(zoneId) && guilds >= 55)) {
            return;
        }
        int repairRate = command >= 60 && guilds >= 55 ? 5 : command >= 60 ? 4 : 3;
        repairRate += Math.min(3, getZoneInfrastructureSupportScore(zoneId) / 3);
        int repairAmount = Math.max(1, Math.round(delta * repairRate));
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure == null || !structure.isActive() || structure.getCurrentHitPoints() <= 0) {
                continue;
            }
            StructureDefinition definition = baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId());
            if (definition == null) {
                continue;
            }
            structure.setCurrentHitPoints(Math.min(definition.getMaxHitPoints(), structure.getCurrentHitPoints() + repairAmount));
        }
    }

    private String getStructureWarSupportLine(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return "Structure support: no active theater modifier.";
        }
        int support = getZoneInfrastructureSupportScore(zoneId);
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        if (isZoneClaimedForWar(zoneId) && command >= 60 && isGuildHallWarZone(zoneId) && guilds >= 55) {
            return "Structure support: command engineers and guild crews are restoring owned structures between attack windows."
                + (support > 0 ? " Infrastructure support score " + support + "." : "");
        }
        if (isZoneClaimedForWar(zoneId) && command >= 60) {
            return "Structure support: command corridor active. Owned structures auto-reinforce between raids."
                + (support > 0 ? " Infrastructure support score " + support + "." : "");
        }
        if (isGuildHallWarZone(zoneId) && guilds >= 55) {
            return "Structure support: guild logistics active. Hall structures are being patched and rearmed in the field."
                + (support > 0 ? " Infrastructure support score " + support + "." : "");
        }
        if (hostile >= 60) {
            return "Structure support: hostile pressure spike. Expect heavier raids and shorter breathing room.";
        }
        return "Structure support: standard frontier upkeep only."
            + (support > 0 ? " Infrastructure support score " + support + "." : "");
    }

    private void applyZoneWarConditionScaling(Enemy enemy, String zoneId) {
        if (enemy == null || zoneId == null || zoneId.isEmpty() || "town".equals(zoneId) || INFINITE_DUNGEON_ZONE_ID.equals(zoneId)) {
            return;
        }
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        boolean claimed = isZoneClaimedForWar(zoneId);
        boolean guildHall = isGuildHallWarZone(zoneId);

        if (!claimed && hostile >= 60) {
            enemy.maxHp *= 1.12f;
            enemy.hp = enemy.maxHp;
            enemy.strength *= 1.08f;
            enemy.defense *= 1.06f;
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.1f));
            if (enemy.name != null && !enemy.name.contains("Raid")) {
                enemy.name = enemy.name + " Raid";
            }
            return;
        }
        if (claimed && command >= 60) {
            enemy.maxHp *= 0.92f;
            enemy.hp = enemy.maxHp;
            enemy.strength *= 0.94f;
            enemy.defense *= 0.95f;
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.08f));
        }
        if (guildHall && guilds >= 55) {
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.12f));
            if (enemy.name != null && !enemy.name.contains("Marked")) {
                enemy.name = enemy.name + " Marked";
            }
        }
    }

    private int getInfiniteDungeonEnemyCount() {
        if (currentZone == null) {
            return 0;
        }
        if (isBossRushModeActive()) {
            return 1;
        }
        int floor = getInfiniteDungeonCurrentFloor();
        if (floor > 0 && floor % INFINITE_DUNGEON_BOSS_INTERVAL == 0) {
            return 1;
        }
        int desired = 2 + Math.min(2, Math.max(0, (floor - 1) / 4));
        if (activeHardModeSeedIndex > 0) {
            desired += 1;
        }
        return Math.min(Math.max(2, desired), currentZone.enemySpawns.size);
    }

    private MonsterDefinition pickInfiniteDungeonMonster(int index) {
        if (currentZoneDefinition == null || currentZoneDefinition.getMonsterIds() == null || currentZoneDefinition.getMonsterIds().length == 0) {
            return null;
        }
        if (isBossRushModeActive()) {
            String bossId = ENDGAME_BOSS_IDS.get(Math.floorMod(getInfiniteDungeonCurrentFloor() - 1, ENDGAME_BOSS_IDS.size()));
            MonsterDefinition bossDefinition = monsterDefinitions.get(bossId);
            if (bossDefinition != null) {
                return bossDefinition;
            }
        }
        int floor = getInfiniteDungeonCurrentFloor();
        String monsterId;
        if (floor > 0 && floor % INFINITE_DUNGEON_BOSS_INTERVAL == 0) {
            monsterId = floor % 20 == 0 ? "apex_predator_s" : "dungeon_overlord_s";
        } else if (floor >= 16) {
            monsterId = index % 3 == 0 ? "rift_horror_a" : "clockwork_bishop_b";
        } else if (floor >= 8) {
            monsterId = index % 2 == 0 ? "rift_horror_a" : "clockwork_bishop_b";
        } else {
            monsterId = "clockwork_bishop_b";
        }
        MonsterDefinition definition = monsterDefinitions.get(monsterId);
        if (definition != null) {
            return definition;
        }
        return pickMonsterForCurrentZone(index);
    }

    private void applyInfiniteDungeonScaling(Enemy enemy) {
        if (enemy == null) {
            return;
        }
        int floor = getInfiniteDungeonCurrentFloor();
        boolean bossFloor = floor > 0 && floor % INFINITE_DUNGEON_BOSS_INTERVAL == 0;
        float healthScale = 1f + Math.max(0, floor - 1) * 0.14f;
        float offenseScale = 1f + Math.max(0, floor - 1) * 0.08f;
        float speedScale = 1f + Math.max(0, floor - 1) * 0.03f;
        if (bossFloor) {
            healthScale += 0.45f;
            offenseScale += 0.18f;
        }
        if (isBossRushModeActive()) {
            healthScale += 0.60f;
            offenseScale += 0.24f;
            speedScale += 0.08f;
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * 1.6f));
        }
        String challengeModifierId = activeChallengeModifierId != null ? activeChallengeModifierId : getSelectedChallengeModifierId();
        if (ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode) || activeChallengeModifierId != null) {
            if ("GLASS_CANNON".equals(challengeModifierId)) {
                healthScale *= 0.86f;
                offenseScale += 0.32f;
            } else if ("THIN_SUPPLIES".equals(challengeModifierId)) {
                healthScale += 0.18f;
                offenseScale += 0.12f;
            } else if ("OVERCLOCKED".equals(challengeModifierId)) {
                speedScale += 0.18f;
                offenseScale += 0.14f;
            }
        }
        if (activeHardModeSeedIndex > 0) {
            healthScale += 0.10f * activeHardModeSeedIndex;
            offenseScale += 0.08f * activeHardModeSeedIndex;
            speedScale += 0.04f * activeHardModeSeedIndex;
            enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * (1f + activeHardModeSeedIndex * 0.18f)));
        }
        enemy.maxHp = Math.max(enemy.maxHp, enemy.maxHp * healthScale);
        enemy.hp = enemy.maxHp;
        enemy.strength *= offenseScale;
        enemy.defense *= 1f + Math.max(0, floor - 1) * 0.06f;
        enemy.intelligence *= offenseScale;
        enemy.stamina *= 1f + Math.max(0, floor - 1) * 0.05f;
        enemy.agility *= speedScale;
        enemy.speed *= speedScale;
        enemy.rewardExperience = Math.max(enemy.rewardExperience, Math.round(enemy.rewardExperience * (1f + floor * 0.12f)));
        enemy.name = enemy.name + " F" + floor;
        enemy.dungeonFloor = floor;
    }

    private boolean isBossRushModeActive() {
        return ENDGAME_MODE_BOSS_RUSH.equals(expeditionBoardMode);
    }

    private MonsterDefinition pickMonsterForCurrentZone(int index) {
        if (currentZoneDefinition == null || currentZoneDefinition.getMonsterIds() == null || currentZoneDefinition.getMonsterIds().length == 0) {
            return null;
        }
        String[] encounterTable = getEncounterTableForCurrentZone(index);
        String monsterId = encounterTable[Math.abs(index) % encounterTable.length];
        MonsterDefinition definition = monsterDefinitions.get(monsterId);
        if (definition != null) {
            return definition;
        }
        return monsterDefinitions.get(currentZoneDefinition.getMonsterIds()[0]);
    }

    private String[] getEncounterTableForCurrentZone(int seed) {
        if (currentZoneDefinition == null || currentZoneDefinition.getMonsterIds() == null || currentZoneDefinition.getMonsterIds().length == 0) {
            return new String[] {"slime_g"};
        }
        String[] ids = currentZoneDefinition.getMonsterIds();
        if (ids.length == 1) {
            return new String[] {ids[0]};
        }
        int roll = Math.abs(seed + (int) (Math.random() * 5f)) % 5;
        switch (roll) {
            case 0:
                return new String[] {ids[0]};
            case 1:
                return new String[] {ids[1 % ids.length]};
            case 2:
                return new String[] {ids[0], ids[1 % ids.length]};
            case 3:
                return new String[] {ids[1 % ids.length], ids[0]};
            default:
                return new String[] {ids[0], ids[0], ids[1 % ids.length]};
        }
    }

    private Vector2 randomPatrolTarget(Vector2 spawnPoint) {
        return new Vector2(
            spawnPoint.x + (float) (Math.random() - 0.5f) * ENEMY_PATROL_RADIUS * 2f,
            spawnPoint.y + (float) (Math.random() - 0.5f) * ENEMY_PATROL_RADIUS * 2f
        );
    }

    private void drawGroundTiles() {
        if (tiledMapRenderer != null && currentTiledMap != null) {
            tiledMapRenderer.setView(gameCamera);
            tiledMapRenderer.render();
            return;
        }

        float tileSize = currentZone != null ? currentZone.tileWidth : 48f;
        float halfW = (gameCamera.viewportWidth * gameCamera.zoom) / 2f + tileSize;
        float halfH = (gameCamera.viewportHeight * gameCamera.zoom) / 2f + tileSize;
        Rectangle activeBounds = shouldUseFrontierStreaming()
            ? frontierChunkManager.getActiveWorldBounds()
            : new Rectangle(localWorldBounds);
        float maxWidth = activeBounds.width > 0f ? activeBounds.x + activeBounds.width : playerPos.x + halfW;
        float maxHeight = activeBounds.height > 0f ? activeBounds.y + activeBounds.height : playerPos.y + halfH;
        int startX = (int) Math.floor((Math.max(activeBounds.x, playerPos.x - halfW)) / tileSize) - 1;
        int endX = (int) Math.ceil((Math.min(maxWidth, playerPos.x + halfW)) / tileSize) + 1;
        int startY = (int) Math.floor((Math.max(activeBounds.y, playerPos.y - halfH)) / tileSize) - 1;
        int endY = (int) Math.ceil((Math.min(maxHeight, playerPos.y + halfH)) / tileSize) + 1;

        String groundStyle = currentZone != null ? currentZone.groundStyle : "";
        boolean isVillage = "village".equals(groundStyle);
        Texture primaryTex = isVillage ? villageGroundTileTexture : groundTileTexture;
        Texture secondaryTex = isVillage ? villageWallTileTexture : wallTileTexture;

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (int gx = startX; gx <= endX; gx++) {
            for (int gy = startY; gy <= endY; gy++) {
                Texture tile = ((gx + gy) & 1) == 0 ? primaryTex : secondaryTex;
                applyGroundTint(gx, gy, ((gx + gy) & 1) == 0);
                batch.draw(tile, gx * tileSize, gy * tileSize, tileSize, tileSize);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void applyGroundTint(int gx, int gy, boolean primaryTile) {
        if (currentZone == null) {
            batch.setColor(Color.WHITE);
            return;
        }
        if (currentZoneDefinition != null && currentZoneDefinition.isExpansiveFrontier() && frontierTerrainSampler != null) {
            applyExpansiveFrontierTint(gx, gy, primaryTile);
            return;
        }
        switch (currentZone.groundStyle) {
            // ── Town / village ───────────────────────────────────────────────
            case "village":
                // Ironhaven: true pixel-art colours from Serene Village sheet — no tint
                batch.setColor(Color.WHITE);
                break;
            // ── Early zones ──────────────────────────────────────────────────
            case "meadow":
                // Verdant Fields: lush, slightly warm green
                batch.setColor(primaryTile ? new Color(0.54f, 0.74f, 0.36f, 1f) : new Color(0.36f, 0.58f, 0.28f, 1f));
                break;
            case "forest":
                // Whispering Forest: deep moss-green, misty
                batch.setColor(primaryTile ? new Color(0.28f, 0.52f, 0.30f, 1f) : new Color(0.16f, 0.36f, 0.20f, 1f));
                break;
            case "coastal":
                // Coastal Shallows: sandy tan with seafoam undertones
                batch.setColor(primaryTile ? new Color(0.64f, 0.70f, 0.52f, 1f) : new Color(0.44f, 0.54f, 0.40f, 1f));
                break;
            case "rust":
                // Rusty Quarry: oxidised iron, warm brown-orange
                batch.setColor(primaryTile ? new Color(0.52f, 0.34f, 0.20f, 1f) : new Color(0.34f, 0.20f, 0.10f, 1f));
                break;
            // ── Mid zones ────────────────────────────────────────────────────
            case "cave":
                // Shadow Caves / generic dungeon: deep slate, oppressive dark
                batch.setColor(primaryTile ? new Color(0.30f, 0.28f, 0.34f, 1f) : new Color(0.16f, 0.14f, 0.20f, 1f));
                break;
            case "crystal":
                // Crystal Depths: cold cyan-teal shimmer
                batch.setColor(primaryTile ? new Color(0.24f, 0.54f, 0.62f, 1f) : new Color(0.12f, 0.34f, 0.44f, 1f));
                break;
            case "frozen":
                // Frozen Vale: pale icy blue-white, frosted
                batch.setColor(primaryTile ? new Color(0.72f, 0.84f, 0.94f, 1f) : new Color(0.50f, 0.66f, 0.82f, 1f));
                break;
            case "sanctum":
                // Clockwork Sanctum: dark iron-brown, mechanised interior
                batch.setColor(primaryTile ? new Color(0.26f, 0.22f, 0.18f, 1f) : new Color(0.14f, 0.12f, 0.10f, 1f));
                break;
            // ── High-tier zones ──────────────────────────────────────────────
            case "sky":
                // Dragon Peak / Sky Fortress: bright open-sky blue
                batch.setColor(primaryTile ? new Color(0.56f, 0.72f, 0.90f, 1f) : new Color(0.38f, 0.54f, 0.76f, 1f));
                break;
            case "volcanic":
                // Volcanic Core / Scorched Plateau: dark char-red with ember glow
                batch.setColor(primaryTile ? new Color(0.38f, 0.12f, 0.06f, 1f) : new Color(0.22f, 0.06f, 0.02f, 1f));
                break;
            case "peak":
                // Dragon Peak rock face fallback: stone grey
                batch.setColor(primaryTile ? new Color(0.56f, 0.54f, 0.52f, 1f) : new Color(0.36f, 0.34f, 0.32f, 1f));
                break;
            // ── End-game zones ───────────────────────────────────────────────
            case "abyss":
                // Sunken Abyss / Abyssal Rift: lightless deep-ocean black-teal
                batch.setColor(primaryTile ? new Color(0.06f, 0.18f, 0.22f, 1f) : new Color(0.02f, 0.08f, 0.12f, 1f));
                break;
            case "void":
                // The Void: absolute dark — near-black with faint violet
                batch.setColor(primaryTile ? new Color(0.08f, 0.04f, 0.14f, 1f) : new Color(0.04f, 0.02f, 0.08f, 1f));
                break;
            default:
                // Safety fallback — plain green
                batch.setColor(primaryTile ? new Color(0.58f, 0.76f, 0.42f, 1f) : new Color(0.38f, 0.60f, 0.32f, 1f));
                break;
        }
    }

    private void applyExpansiveFrontierTint(int gx, int gy, boolean primaryTile) {
        int tileWidth = currentZone != null ? currentZone.tileWidth : 48;
        int tileHeight = currentZone != null ? currentZone.tileHeight : 48;
        FrontierTerrainSampler.TerrainSample sample = frontierTerrainSampler.sample(
            toAbsoluteTileX(gx, tileWidth),
            toAbsoluteTileY(gy, tileHeight)
        );
        FrontierBiomeDefinition biome = frontierBiomeCatalog != null
            ? frontierBiomeCatalog.resolve(sample.type)
            : new FrontierBiomeCatalog().resolve(sample.type);
        Color tint = primaryTile ? biome.getPrimaryTint() : biome.getSecondaryTint();
        float daylight = settlementTimeManager.getDaylightStrength();
        tint = lerpColor(new Color(0.08f, 0.1f, 0.16f, 1f), tint, 0.35f + (0.65f * daylight));

        if (sample.moisture > 0.44f) {
            tint = lerpColor(tint, biome.getMoistureTint(), 0.2f);
        } else if (sample.ruggedness > 0.45f) {
            tint = lerpColor(tint, biome.getRuggedTint(), 0.18f);
        }
        batch.setColor(tint);
    }

    private Color lerpColor(Color from, Color to, float alpha) {
        return new Color(
            from.r + (to.r - from.r) * alpha,
            from.g + (to.g - from.g) * alpha,
            from.b + (to.b - from.b) * alpha,
            1f
        );
    }

    /** Returns the groundStyle of the currently loaded zone, or "meadow" if none is loaded. */
    public String getCurrentGroundStyle() {
        return currentZone != null ? currentZone.groundStyle : "meadow";
    }

    private void drawPlayerSprite() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        drawShadow(playerPos.x, playerPos.y, 54f, 22f, 0.5f);
        drawAnimatedSprite(
            getAnimatedFrame(playerAnimation, lastMoveDirection, isPlayerMoving(), playerAnimationTime),
            lastMoveDirection,
            playerPos.x - PLAYER_SIZE / 2f,
            playerPos.y - PLAYER_SIZE / 2f,
            PLAYER_SIZE,
            PLAYER_SIZE
        );
        font.setColor(Color.WHITE);
        font.draw(batch, playerName, playerPos.x - 24, playerPos.y + PLAYER_SIZE / 2 + 18);
        batch.end();
    }

    private void drawAttackFlash() {
        if (attackFlashAlpha <= 0f) {
            return;
        }
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, attackFlashAlpha);
        shapeRenderer.circle(playerPos.x, playerPos.y, PLAYER_ATTACK_RANGE);
        shapeRenderer.end();
    }

    private void drawShadow(float x, float y, float width, float height, float alpha) {
        if (shadowTexture == null) {
            return;
        }
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(shadowTexture, x - width / 2f, y - height / 2f - 12f, width, height);
        batch.setColor(Color.WHITE);
    }

    private boolean isPlayerMoving() {
        return Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.UP)
            || Gdx.input.isKeyPressed(Input.Keys.S)
            || Gdx.input.isKeyPressed(Input.Keys.DOWN)
            || Gdx.input.isKeyPressed(Input.Keys.A)
            || Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.D)
            || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    private TextureRegion getAnimatedFrame(AnimationSet set, Vector2 facing, boolean moving, float animationTime) {
        TextureRegion[] frames = set.getFrames(facing, moving);
        int index = (int) (animationTime / (moving ? 0.10f : 0.18f)) % frames.length;
        return frames[index];
    }

    private void drawAnimatedSprite(TextureRegion frame, Vector2 facing, float x, float y, float width, float height) {
        boolean flip = Math.abs(facing.x) > Math.abs(facing.y) && facing.x > 0f;
        if (flip) {
            batch.draw(frame, x + width, y, -width, height);
        } else {
            batch.draw(frame, x, y, width, height);
        }
    }

    private void updatePlayer(float delta) {
        // Movement
        float dx = 0, dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))    dy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))  dy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))  dx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) dx += 1;

        // Normalize diagonal movement
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            dx /= len;
            dy /= len;
            lastMoveDirection.set(dx, dy);
            playerAnimationTime += delta;
            maybeCompleteTownMovementTutorial();
        }

        float nextX = playerPos.x + dx * PLAYER_SPEED * delta;
        float nextY = playerPos.y + dy * PLAYER_SPEED * delta;
        if (!isBlockedAt(nextX, nextY, PLAYER_SIZE)) {
            playerPos.set(nextX, nextY);
        }

        if (playerAttackCooldown > 0) {
            playerAttackCooldown -= delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (hasActiveDialog()) {
                dismissMessages();
                return;
            }
            if (buildModeOpen && tryPlaceSelectedStructure()) {
                return;
            }
            if (!tryInteractWithDoor()) {
                if (!tryReadHouseSign()) {
                    if (!tryInteractWithWorldFeature()) {
                        if (!tryOpenWorldChest()) {
                            interactWithNpc();
                        }
                    }
                }
            }
        }
        bankExpeditionHaulIfPossible(false);
    }

    private void maybeCompleteTownMovementTutorial() {
        if (!isHubTownZone() || worldStateManager.isFlagActive(gameState, "tutorial.town_movement_complete")) {
            return;
        }
        if (!"learn_movement".equals(questManager.getQuestState(gameState, "ironhaven_arrival"))) {
            return;
        }
        worldStateManager.setFlag(gameState, "tutorial.town_movement_complete", true);
        questManager.syncProgress(gameState, worldStateManager);
        showStandaloneDialog(
            "Ironhaven",
            "Movement confirmed. Walk the town, speak with Mira, Tor, and Edda, and get a feel for how Ironhaven works before you head into the frontier."
        );
    }

    private void updateAttackEffects(float delta) {
        if (attackFlashAlpha > 0) {
            attackFlashTimer += delta;
            if (attackFlashTimer >= ATTACK_FLASH_DURATION) {
                attackFlashAlpha = 0f;
            } else {
                attackFlashAlpha = 0.5f * (1f - attackFlashTimer / ATTACK_FLASH_DURATION);
            }
        }
    }

    private void updateEnemies(float delta) {
        updateBaseRaidState(delta);
        boolean allDead = true;

        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            if (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(enemy.pos)) {
                allDead = false;
                continue;
            }
            allDead = false;

            // Update attack timer
            if (enemy.attackTimer > 0) {
                enemy.attackTimer -= delta;
            }

            Vector2 targetPos = selectEnemyAggroTarget(enemy);
            float targetDistance = targetPos != null ? enemy.pos.dst(targetPos) : Float.MAX_VALUE;
            float distToPlayer = enemy.pos.dst(playerPos);

            if (targetPos != null && targetDistance <= ENEMY_AGGRO_RANGE) {
                Vector2 direction = new Vector2(targetPos).sub(enemy.pos);
                if (direction.len() > 0) {
                    direction.nor();
                    enemy.facing.set(direction);
                    enemy.animationTime += delta;
                }
                enemy.pos.x += direction.x * enemy.speed * delta;
                enemy.pos.y += direction.y * enemy.speed * delta;
                BaseDefenderUnit defenderTarget = findClosestLivingBaseDefender(enemy.pos);
                if (defenderTarget != null
                    && enemy.pos.dst(defenderTarget.getPosition()) <= BASE_DEFENDER_CONTACT_RANGE
                    && enemy.attackTimer <= 0f) {
                    float damage = Math.max(2f, ENEMY_MELEE_DAMAGE + enemy.strength * 0.2f - defenderTarget.getDefense() * 0.12f);
                    defenderTarget.setCurrentHealth(defenderTarget.getCurrentHealth() - damage);
                    defenderTarget.setAttackTimer(Math.max(defenderTarget.getAttackTimer(), 0.2f));
                    enemy.attackTimer = ENEMY_MELEE_COOLDOWN;
                    if (defenderTarget.getCurrentHealth() <= 0f) {
                        defenderTarget.setActive(false);
                    }
                    persistDefenderHealth(defenderTarget);
                }
                PlacedStructure structureTarget = findClosestAttackableStructure(enemy.pos);
                if (structureTarget != null
                    && enemy.pos.dst(getStructureCenter(structureTarget)) <= BASE_STRUCTURE_CONTACT_RANGE
                    && enemy.attackTimer <= 0f) {
                    int damage = Math.max(4, Math.round(ENEMY_MELEE_DAMAGE + enemy.strength * 0.3f));
                    baseBuildingEngine.applyStructureDamage(getCurrentBaseState(), structureTarget.getInstanceId(), damage);
                    refreshStructureFeatureLabel(structureTarget.getInstanceId());
                    if (!structureTarget.isActive()) {
                        removeInactiveStructureFeature(structureTarget.getInstanceId());
                    }
                    enemy.attackTimer = ENEMY_MELEE_COOLDOWN;
                }
                if (!battleActive && distToPlayer <= ENCOUNTER_TRIGGER_RANGE) {
                    startEncounter(enemy);
                    return;
                }
            } else {
                // Patrol randomly
                float distToPatrol = enemy.pos.dst(enemy.patrolTarget);
                if (distToPatrol < 20f) {
                    // Pick new patrol target
                    enemy.patrolTarget = new Vector2(
                        enemy.pos.x + (float) (Math.random() - 0.5f) * ENEMY_PATROL_RADIUS * 2,
                        enemy.pos.y + (float) (Math.random() - 0.5f) * ENEMY_PATROL_RADIUS * 2
                    );
                }
                Vector2 direction = new Vector2(enemy.patrolTarget).sub(enemy.pos);
                if (direction.len() > 0) {
                    direction.nor();
                    enemy.facing.set(direction);
                    enemy.animationTime += delta;
                }
                enemy.pos.x += direction.x * enemy.speed * delta;
                enemy.pos.y += direction.y * enemy.speed * delta;
            }
        }

        // Respawn if all dead
        if (!currentZoneIsSafe() && allDead) {
            enemyRespawnTimer += delta;
            if (enemyRespawnTimer >= ENEMY_RESPAWN_DELAY) {
                spawnEnemies();
            }
        }

        // Update HUD
        refreshHud();
    }

    private void updateBaseRaidState(float delta) {
        BaseState baseState = getCurrentBaseState();
        if (baseState == null || currentZoneDefinition == null || !currentZoneDefinition.isExpansiveFrontier()) {
            return;
        }
        BaseRaidState raidState = baseState.getRaidState();
        if (raidState.getCooldownSeconds() > 0f) {
            raidState.setCooldownSeconds(raidState.getCooldownSeconds() - delta);
        }
        if (!hasOperationalBase(baseState)) {
            raidState.setActive(false);
            raidState.setThreatLevel(0f);
            raidState.setWaveIndex(0);
            return;
        }
        applyFactionStructureSupport(baseState, delta);
        if (!raidState.isActive()) {
            float threatGain = delta * BASE_RAID_THREAT_PER_SECOND * Math.max(1, baseState.getPlacedStructures().size())
                * getZoneRaidPressureMultiplier(baseState.getZoneId());
            raidState.setThreatLevel(Math.min(BASE_RAID_TRIGGER_THREAT, raidState.getThreatLevel() + threatGain));
            if (raidState.getCooldownSeconds() <= 0f && raidState.getThreatLevel() >= BASE_RAID_TRIGGER_THREAT) {
                launchBaseRaid(baseState);
            }
            return;
        }
        if (countLiveRaidEnemies() == 0) {
            raidState.setActive(false);
            raidState.setThreatLevel(0f);
            raidState.setCooldownSeconds(BASE_RAID_COOLDOWN_SECONDS * getZoneRaidCooldownMultiplier(baseState.getZoneId()));
            showStandaloneDialog("Frontier", "Raid repelled. Your base holds for now.");
        }
    }

    private Enemy findClosestEnemyForDefender(BaseDefenderUnit defender) {
        Enemy nearestEnemy = null;
        float nearestDistance = defender != null ? defender.getDetectionRange() : Float.MAX_VALUE;
        if (defender == null) {
            return null;
        }
        for (Enemy enemy : enemies) {
            if (enemy == null || !enemy.alive || (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(enemy.pos))) {
                continue;
            }
            float distance = defender.getPosition().dst(enemy.pos);
            float anchorDistance = enemy.pos.dst(defender.getGuardPosition());
            if (distance <= nearestDistance || anchorDistance <= defender.getDetectionRange()) {
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEnemy = enemy;
                }
            }
        }
        return nearestEnemy;
    }

    private Vector2 selectEnemyAggroTarget(Enemy enemy) {
        if (enemy == null) {
            return null;
        }
        Vector2 target = new Vector2(playerPos);
        float bestDistance = enemy.pos.dst(playerPos);

        RobotCompanion robot = findClosestLivingRobot(enemy.pos);
        if (robot != null) {
            float robotDistance = enemy.pos.dst(robot.pos);
            if (robotDistance < bestDistance) {
                bestDistance = robotDistance;
                target = new Vector2(robot.pos);
            }
        }

        BaseDefenderUnit defender = findClosestLivingBaseDefender(enemy.pos);
        if (defender != null) {
            float defenderDistance = enemy.pos.dst(defender.getPosition());
            if (defenderDistance < bestDistance) {
                bestDistance = defenderDistance;
                target = new Vector2(defender.getPosition());
            }
        }

        PlacedStructure structure = findClosestAttackableStructure(enemy.pos);
        if (structure != null) {
            Vector2 structureCenter = getStructureCenter(structure);
            float structureDistance = enemy.pos.dst(structureCenter);
            if (structureDistance < bestDistance) {
                target = structureCenter;
            }
        }
        return target;
    }

    private BaseDefenderUnit findClosestLivingBaseDefender(Vector2 source) {
        BaseDefenderUnit nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        if (source == null) {
            return null;
        }
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender == null || !defender.isActive()) {
                continue;
            }
            float distance = source.dst(defender.getPosition());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = defender;
            }
        }
        return nearest;
    }

    private PlacedStructure findClosestAttackableStructure(Vector2 source) {
        BaseState baseState = getCurrentBaseState();
        if (baseState == null || source == null) {
            return null;
        }
        PlacedStructure nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure == null || !structure.isActive()) {
                continue;
            }
            float distance = source.dst(getStructureCenter(structure));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = structure;
            }
        }
        return nearest;
    }

    private Vector2 getStructureCenter(PlacedStructure structure) {
        Rectangle bounds = structure != null ? structure.getBounds() : new Rectangle();
        return new Vector2(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f);
    }

    private void moveDefenderTowardThreat(BaseDefenderUnit defender, Enemy target, float delta) {
        if (defender == null || target == null) {
            return;
        }
        Vector2 desiredTarget = new Vector2(target.pos);
        if (target.pos.dst(defender.getGuardPosition()) > BASE_DEFENDER_LEASH_RANGE) {
            desiredTarget = new Vector2(defender.getGuardPosition()).lerp(target.pos, 0.45f);
        }
        moveBaseDefender(defender, desiredTarget, delta);
    }

    private void moveDefenderTowardIdlePoint(BaseDefenderUnit defender, float delta) {
        if (defender == null) {
            return;
        }
        moveBaseDefender(defender, baseDefenseDirector.getIdleTarget(defender, survivalTime), delta);
    }

    private void moveBaseDefender(BaseDefenderUnit defender, Vector2 target, float delta) {
        if (defender == null || target == null) {
            return;
        }
        Vector2 move = new Vector2(target).sub(defender.getPosition());
        if (move.len2() <= 4f) {
            return;
        }
        float maxStep = defender.getMoveSpeed() * delta;
        if (move.len() > maxStep && maxStep > 0f) {
            move.nor().scl(maxStep);
        }
        float nextX = defender.getPosition().x + move.x;
        float nextY = defender.getPosition().y + move.y;
        if (!isBlockedAt(nextX, nextY, BASE_DEFENDER_SIZE)) {
            defender.getPosition().set(nextX, nextY);
        }
        defender.setAnimationTime(defender.getAnimationTime() + delta);
    }

    private boolean hasOperationalBase(BaseState baseState) {
        if (baseState == null) {
            return false;
        }
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure != null && structure.isActive() && structure.getCurrentHitPoints() > 0) {
                return true;
            }
        }
        return false;
    }

    private int countLiveRaidEnemies() {
        int live = 0;
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.alive && enemy.raidSpawned) {
                live++;
            }
        }
        return live;
    }

    private void launchBaseRaid(BaseState baseState) {
        if (baseState == null || currentZone == null) {
            return;
        }
        BaseRaidState raidState = baseState.getRaidState();
        raidState.setActive(true);
        raidState.setWaveIndex(raidState.getWaveIndex() + 1);
        raidState.setThreatLevel(BASE_RAID_TRIGGER_THREAT);
        PlacedStructure anchor = findClosestAttackableStructure(playerPos);
        if (anchor == null) {
            return;
        }
        Vector2 center = getStructureCenter(anchor);
        float pressureMultiplier = getZoneRaidPressureMultiplier(baseState.getZoneId());
        int spawnCount = Math.min(Math.max(2, Math.round((3 + Math.max(0, raidState.getWaveIndex() - 1)) * pressureMultiplier)), 7);
        for (int i = 0; i < spawnCount; i++) {
            Enemy enemy = createZoneEnemy(
                new Vector2(center.x + 180f + i * 18f, center.y + ((i & 1) == 0 ? 96f : -96f)),
                enemies.size() + i
            );
            enemy.raidSpawned = true;
            enemy.patrolTarget = new Vector2(center);
            enemies.add(enemy);
        }
        showStandaloneDialog("Frontier", "Raid detected near your base. Defenders to stations. " + getStructureWarSupportLine(baseState.getZoneId()));
    }

    private void persistDefenderHealth(BaseDefenderUnit defender) {
        BaseState baseState = getCurrentBaseState();
        if (baseState == null || defender == null || defender.getRobotId() == null || defender.getRobotId().isEmpty()) {
            return;
        }
        baseState.setDefenderHealth(defender.getRobotId(), defender.getCurrentHealth());
    }

    private void removeInactiveStructureFeature(String structureInstanceId) {
        if (currentZone == null || structureInstanceId == null || structureInstanceId.isEmpty()) {
            return;
        }
        for (int i = currentZone.features.size - 1; i >= 0; i--) {
            TmxWorldLoader.Feature feature = currentZone.features.get(i);
            if (feature != null && structureInstanceId.equals(feature.id)) {
                currentZone.features.removeIndex(i);
            }
        }
        syncCurrentZoneBaseDefenders();
    }

    private void updateRobots(float delta) {
        robotAttackLines.clear();

        Vector2 followTarget = playerPos;
        int robotSlotLimit = getPartySlotLimit();
        for (int i = 0; i < robotSlotLimit; i++) {
            RobotCompanion robot = robots[i];
            if (!hasActiveRobotAt(i) || robot.health <= 0f) {
                continue;
            }

            if (robot.attackTimer > 0f) {
                robot.attackTimer -= delta;
            }

            Vector2 toTarget = new Vector2(followTarget).sub(robot.pos);
            if (toTarget.isZero()) {
                toTarget.set(lastMoveDirection).scl(-1f);
            }
            Vector2 desiredPos = new Vector2(followTarget).sub(new Vector2(toTarget).nor().scl(ROBOT_FOLLOW_GAP));
            Vector2 toDesired = new Vector2(desiredPos).sub(robot.pos);
            float maxStep = ROBOT_FOLLOW_SPEED * delta;
            if (toDesired.len() > maxStep && maxStep > 0f) {
                toDesired.nor().scl(maxStep);
            }
            robot.pos.add(toDesired);
            if (toDesired.len2() > 0.001f) {
                robot.facing.set(toDesired).nor();
                robot.animationTime += delta;
            }
            robot.angleDeg = robot.facing.angleDeg();
            followTarget = robot.pos;
        }
    }

    private void updateBaseDefenders(float delta) {
        if (currentZone == null || activeBaseDefenders.isEmpty()) {
            return;
        }
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender == null || !defender.isActive()) {
                continue;
            }
            if (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(defender.getPosition())) {
                continue;
            }
            if (defender.getAttackTimer() > 0f) {
                defender.setAttackTimer(defender.getAttackTimer() - delta);
            }

            Enemy target = findClosestEnemyForDefender(defender);
            if (target != null && target.alive) {
                moveDefenderTowardThreat(defender, target, delta);
                float distance = defender.getPosition().dst(target.pos);
                if (distance <= defender.getAttackRange() && defender.getAttackTimer() <= 0f) {
                    RobotAttackLine attackLine = new RobotAttackLine();
                    attackLine.from = new Vector2(defender.getPosition());
                    attackLine.to = new Vector2(target.pos);
                    robotAttackLines.add(attackLine);
                    target.hp -= defender.getAttackPower() * BASE_DEFENDER_ATTACK_DAMAGE_SCALE;
                    defender.setAttackTimer(defender.getAttackCooldown());
                    if (target.hp <= 0f) {
                        onEnemyKilled(target);
                    }
                }
                persistDefenderHealth(defender);
            } else {
                moveDefenderTowardIdlePoint(defender, delta);
                persistDefenderHealth(defender);
            }
        }
    }

    private void onEnemyKilled(Enemy enemy) {
        totalEnemiesKilled++;
        enemy.alive = false;
    }

    private void updateGoldPopups(float delta) {
        for (int i = goldPopups.size() - 1; i >= 0; i--) {
            GoldPopup popup = goldPopups.get(i);
            popup.timer += delta;
            popup.pos.y += 40f * delta; // Float upward
            if (popup.timer >= popup.lifetime) {
                goldPopups.remove(i);
            }
        }
    }

    private void drawEnemies() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (Enemy enemy : enemies) {
            if (!enemy.alive || (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(enemy.pos))) continue;
            drawShadow(enemy.pos.x, enemy.pos.y, enemy.size + 18f, 18f, 0.45f);
            TextureRegion sprite = getAnimatedFrame(
                enemyAnimations[enemy.spriteIndex % enemyAnimations.length],
                enemy.facing,
                true,
                enemy.animationTime
            );
            drawAnimatedSprite(sprite, enemy.facing, enemy.pos.x - enemy.size / 2, enemy.pos.y - enemy.size / 2, enemy.size, enemy.size);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) {
            if (!enemy.alive || (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(enemy.pos))) continue;
            // Draw HP bar above enemy
            float barWidth = enemy.size;
            float barHeight = 2f;
            float barX = enemy.pos.x - barWidth / 2;
            float barY = enemy.pos.y + enemy.size / 2 + 4f;

            // HP bar background (dark)
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            // HP bar fill (green to red based on health)
            float hpPercent = enemy.hp / enemy.maxHp;
            if (hpPercent > 0.5f) {
                shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 0.9f);
            } else if (hpPercent > 0.25f) {
                shapeRenderer.setColor(1f, 0.8f, 0.2f, 0.9f);
            } else {
                shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.9f);
            }
            shapeRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
        }
        shapeRenderer.end();
    }

    private void drawHouses() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        if (currentZone != null) {
            for (TmxWorldLoader.Feature feature : currentZone.features) {
                if (!"house".equals(feature.kind) && isFeatureVisible(feature)) {
                    drawFeature(feature);
                }
            }
        }
        for (House house : houses) {
            for (float x = house.x; x < house.x + house.width; x += 24f) {
                for (float y = house.y; y < house.y + house.height; y += 24f) {
                    batch.draw(wallTileTexture, x, y, 24f, 24f);
                }
            }
            batch.setColor(0.82f, 0.68f, 0.5f, 1f);
            batch.draw(doorTexture, house.x + house.width * 0.4f, house.y, house.width * 0.2f, house.height * 0.38f);
            batch.setColor(Color.WHITE);
        }
        drawSettlementTownProps(batch);
        drawWorldChests();
        for (House house : houses) {
            drawHouseSign(batch, house);
            if (canUseHouseDoor(house)) {
                font.draw(batch, "E: Enter", house.x + 18f, house.y - 10f);
            } else if (canReadHouseSign(house)) {
                font.draw(batch, "E: Read", house.getSignBounds().x - 12f, house.getSignBounds().y - 10f);
            }
        }
        if (currentZone != null) {
            for (TmxWorldLoader.Door door : currentZone.doors) {
                if (door.houseId < 0) {
                    batch.setColor(1f, 0.92f, 0.72f, 1f);
                    batch.draw(doorTexture, door.bounds.x, door.bounds.y, door.bounds.width, door.bounds.height);
                    batch.setColor(Color.WHITE);
                    font.draw(batch, "EXIT: " + door.label, door.bounds.x - 8f, door.bounds.y + door.bounds.height + 28f);
                }
                Vector2 center = new Vector2(door.bounds.x + door.bounds.width / 2f, door.bounds.y + door.bounds.height / 2f);
                if (distanceToRect(playerPos, door.bounds) <= 42f && door.houseId < 0) {
                    font.draw(batch, "E: Travel to " + door.label, center.x - 54f, center.y - 24f);
                }
            }
        }
        batch.end();
    }

    private void drawSettlementTownProps(SpriteBatch spriteBatch) {
        if (!isHubTownZone()) {
            return;
        }

        House workshop = findHouseById(0);
        House lodge = findHouseById(1);
        House herbalist = findHouseById(2);
        House playerHome = findHouseById(3);

        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools") && workshop != null) {
            spriteBatch.setColor(0.85f, 0.58f, 0.22f, 1f);
            spriteBatch.draw(wallTileTexture, workshop.x + workshop.width + 10f, workshop.y + 8f, 20f, 20f);
            spriteBatch.draw(wallTileTexture, workshop.x + workshop.width + 32f, workshop.y + 8f, 20f, 20f);
            spriteBatch.setColor(1f, 0.85f, 0.45f, 1f);
            spriteBatch.draw(wallTileTexture, workshop.x + workshop.width + 18f, workshop.y + 32f, 28f, 6f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Forge Rail Online", workshop.x + workshop.width - 18f, workshop.y + workshop.height + 18f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock") && herbalist != null) {
            spriteBatch.setColor(0.34f, 0.68f, 0.42f, 1f);
            spriteBatch.draw(wallTileTexture, herbalist.x - 18f, herbalist.y + 6f, 14f, 26f);
            spriteBatch.draw(wallTileTexture, herbalist.x - 2f, herbalist.y + 6f, 14f, 26f);
            spriteBatch.setColor(0.76f, 0.9f, 0.78f, 1f);
            spriteBatch.draw(wallTileTexture, herbalist.x - 18f, herbalist.y + 32f, 30f, 8f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Supply Stall", herbalist.x - 18f, herbalist.y + herbalist.height + 18f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.watchtower_network") && lodge != null) {
            float poleX = lodge.x + lodge.width + 12f;
            float poleY = lodge.y + 4f;
            spriteBatch.setColor(0.4f, 0.3f, 0.22f, 1f);
            spriteBatch.draw(wallTileTexture, poleX, poleY, 10f, 52f);
            spriteBatch.setColor(1f, 0.86f, 0.35f, 1f);
            spriteBatch.draw(wallTileTexture, poleX - 6f, poleY + 46f, 22f, 16f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Watchtower Link", poleX - 20f, poleY + 76f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.survey_drones") && workshop != null) {
            float padX = workshop.x - 40f;
            float padY = workshop.y + workshop.height + 6f;
            spriteBatch.setColor(0.18f, 0.62f, 0.82f, 1f);
            spriteBatch.draw(wallTileTexture, padX, padY, 18f, 18f);
            spriteBatch.draw(wallTileTexture, padX + 22f, padY, 18f, 18f);
            spriteBatch.setColor(0.82f, 0.92f, 1f, 1f);
            spriteBatch.draw(wallTileTexture, padX + 9f, padY + 24f, 24f, 6f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Drone Pad", padX - 8f, padY + 48f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")) {
            float annexX = currentZone != null ? currentZone.pixelWidth - 700f : 1220f;
            float annexY = playerHome != null ? playerHome.y + 44f : 420f;

            spriteBatch.setColor(0.18f, 0.24f, 0.3f, 1f);
            spriteBatch.draw(wallTileTexture, annexX, annexY + 180f, 260f, 22f);
            spriteBatch.draw(wallTileTexture, annexX, annexY, 260f, 22f);

            spriteBatch.setColor(0.42f, 0.3f, 0.18f, 1f);
            spriteBatch.draw(wallTileTexture, annexX + 24f, annexY + 24f, 84f, 60f);
            spriteBatch.draw(wallTileTexture, annexX + 122f, annexY + 24f, 92f, 60f);
            spriteBatch.draw(wallTileTexture, annexX + 38f, annexY + 108f, 180f, 48f);

            spriteBatch.setColor(0.76f, 0.62f, 0.34f, 1f);
            spriteBatch.draw(wallTileTexture, annexX + 20f, annexY + 166f, 204f, 10f);
            spriteBatch.draw(wallTileTexture, annexX + 102f, annexY + 28f, 8f, 128f);

            spriteBatch.setColor(0.58f, 0.76f, 0.88f, 1f);
            spriteBatch.draw(wallTileTexture, annexX + 240f, annexY + 32f, 16f, 134f);
            spriteBatch.draw(wallTileTexture, annexX + 232f, annexY + 162f, 32f, 18f);

            spriteBatch.setColor(0.68f, 0.5f, 0.22f, 1f);
            spriteBatch.draw(wallTileTexture, annexX + 16f, annexY - 30f, 48f, 28f);
            spriteBatch.draw(wallTileTexture, annexX + 72f, annexY - 30f, 48f, 28f);
            spriteBatch.draw(wallTileTexture, annexX + 128f, annexY - 30f, 48f, 28f);

            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "FRONTIER ANNEX", annexX + 22f, annexY + 214f);
            font.draw(spriteBatch, "Depot Counter", annexX + 48f, annexY + 138f);
            font.draw(spriteBatch, "Salvage Yard", annexX + 58f, annexY - 42f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.tavern_open")) {
            float tavernX = 104f;
            float tavernY = 408f;
            spriteBatch.setColor(0.42f, 0.24f, 0.14f, 1f);
            spriteBatch.draw(wallTileTexture, tavernX, tavernY, 112f, 58f);
            spriteBatch.setColor(0.82f, 0.68f, 0.28f, 1f);
            spriteBatch.draw(wallTileTexture, tavernX + 20f, tavernY + 62f, 72f, 10f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Tavern", tavernX + 24f, tavernY + 90f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.hangar_open")) {
            float hangarX = 262f;
            float hangarY = 286f;
            spriteBatch.setColor(0.24f, 0.3f, 0.38f, 1f);
            spriteBatch.draw(wallTileTexture, hangarX, hangarY, 144f, 50f);
            spriteBatch.draw(wallTileTexture, hangarX + 18f, hangarY - 20f, 32f, 18f);
            spriteBatch.draw(wallTileTexture, hangarX + 58f, hangarY - 20f, 32f, 18f);
            spriteBatch.draw(wallTileTexture, hangarX + 98f, hangarY - 20f, 32f, 18f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Hangar", hangarX + 28f, hangarY + 78f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.archive_open")) {
            float archiveX = 896f;
            float archiveY = 302f;
            spriteBatch.setColor(0.3f, 0.22f, 0.36f, 1f);
            spriteBatch.draw(wallTileTexture, archiveX, archiveY, 108f, 48f);
            spriteBatch.setColor(0.68f, 0.8f, 0.96f, 1f);
            spriteBatch.draw(wallTileTexture, archiveX + 14f, archiveY + 54f, 80f, 8f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Archive", archiveX + 18f, archiveY + 78f);
        }

        if (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open")) {
            float yardX = 532f;
            float yardY = 308f;
            spriteBatch.setColor(0.56f, 0.38f, 0.2f, 1f);
            spriteBatch.draw(wallTileTexture, yardX, yardY, 18f, 60f);
            spriteBatch.draw(wallTileTexture, yardX + 62f, yardY, 18f, 60f);
            spriteBatch.draw(wallTileTexture, yardX - 6f, yardY + 58f, 92f, 8f);
            spriteBatch.setColor(Color.WHITE);
            font.draw(spriteBatch, "Training Ground", yardX - 26f, yardY + 92f);
        }
    }

    private void drawHouseSign(SpriteBatch spriteBatch, House house) {
        Rectangle sign = house.getSignBounds();
        spriteBatch.setColor(new Color(0.48f, 0.34f, 0.2f, 1f));
        spriteBatch.draw(wallTileTexture, sign.x, sign.y, sign.width, sign.height);
        spriteBatch.setColor(new Color(0.78f, 0.68f, 0.52f, 1f));
        spriteBatch.draw(wallTileTexture, sign.x + 3f, sign.y + 8f, sign.width - 6f, sign.height - 8f);
        spriteBatch.setColor(Color.WHITE);
    }

    private void drawFeature(TmxWorldLoader.Feature feature) {
        Color tint;
        if ("shop".equals(feature.interactionType)) {
            tint = new Color(0.22f, 0.68f, 0.46f, 0.95f);
        } else if ("scan_hidden_path".equals(feature.interactionType)) {
            tint = new Color(0.28f, 0.6f, 0.88f, 0.92f);
        } else if ("burn_barrier".equals(feature.interactionType)) {
            tint = new Color(0.9f, 0.44f, 0.2f, 0.92f);
        } else if ("strength_boulder".equals(feature.interactionType)) {
            tint = new Color(0.54f, 0.46f, 0.36f, 1f);
        } else if ("harvest_resource".equals(feature.interactionType)) {
            tint = new Color(0.82f, 0.72f, 0.34f, 0.96f);
        } else if ("claim_outpost_site".equals(feature.interactionType)) {
            tint = new Color(0.34f, 0.64f, 0.88f, 0.9f);
        } else if ("player_structure".equals(feature.kind)) {
            tint = getPlayerStructureTint(feature);
        } else if ("cliff".equals(feature.kind)) {
            tint = new Color(0.36f, 0.36f, 0.42f, 1f);
        } else if ("gate".equals(feature.kind)) {
            tint = new Color(0.62f, 0.62f, 0.72f, 1f);
        } else {
            tint = new Color(0.5f, 0.5f, 0.56f, 1f);
        }
        for (float x = feature.bounds.x; x < feature.bounds.x + feature.bounds.width; x += 24f) {
            for (float y = feature.bounds.y; y < feature.bounds.y + feature.bounds.height; y += 24f) {
                batch.setColor(tint);
                batch.draw(wallTileTexture, x, y, 24f, 24f);
            }
        }
        batch.setColor(Color.WHITE);
        if (feature.label != null && !feature.label.isEmpty()) {
            font.draw(batch, feature.label, feature.bounds.x + 8f, feature.bounds.y + feature.bounds.height + 26f);
        }
        if ("player_structure".equals(feature.kind) && buildModeOpen && isFacingInteractionRect(feature.bounds)) {
            String action = isAssignableDefenderPost(feature) ? "R: Remove  T: Repair  F: Assign" : "R: Remove  T: Repair";
            font.draw(batch, action, feature.bounds.x + 8f, feature.bounds.y - 10f);
        } else if (isFeatureInteractable(feature) && isFacingInteractionRect(feature.bounds)) {
            font.draw(batch, "E: " + getFeatureActionLabel(feature), feature.bounds.x + 8f, feature.bounds.y - 10f);
        }
    }

    private Color getPlayerStructureTint(TmxWorldLoader.Feature feature) {
        PlacedStructure structure = getPlacedStructure(feature != null ? feature.persistentStateId : null);
        StructureDefinition definition = structure != null
            ? baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId())
            : null;
        if (definition == null) {
            return new Color(0.56f, 0.56f, 0.62f, 1f);
        }
        switch (definition.getCategory()) {
            case DEFENSE:
                return new Color(0.76f, 0.38f, 0.26f, 0.96f);
            case STORAGE:
                return new Color(0.58f, 0.46f, 0.24f, 0.96f);
            case CRAFTING:
                return new Color(0.42f, 0.66f, 0.84f, 0.96f);
            case POWER:
                return new Color(0.84f, 0.78f, 0.34f, 0.96f);
            case WALL:
            default:
                return new Color(0.56f, 0.56f, 0.62f, 0.96f);
        }
    }

    private void drawWorldChests() {
        if (currentZone == null) {
            return;
        }
        for (TmxWorldLoader.ChestData chest : currentZone.chests) {
            boolean opened = isChestOpened(currentZoneId, -1, chest.id);
            if (chest.hidden && !opened && playerPos.dst(chest.position) > 60f) {
                continue;
            }
            batch.setColor(1f, 1f, 1f, opened ? 0.5f : 1f);
            batch.draw(chestTexture, chest.position.x - 18f, chest.position.y - 18f, 36f, 36f);
            batch.setColor(Color.WHITE);
            if (!opened && playerPos.dst(chest.position) <= 40f) {
                font.draw(batch, "E: Open", chest.position.x - 22f, chest.position.y - 24f);
            }
        }
    }

    private void drawNpcs() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        int npcIndex = 0;
        for (NpcEntity npc : npcs) {
            if (shouldUseFrontierStreaming() && !isWithinActiveFrontierWorld(npc.pos)) {
                npcIndex++;
                continue;
            }
            drawShadow(npc.pos.x, npc.pos.y, 42f, 16f, 0.4f);
            TextureRegion sprite = getAnimatedFrame(npcAnimations[npcIndex % npcAnimations.length], new Vector2(0f, -1f), false, survivalTime);
            drawAnimatedSprite(sprite, new Vector2(-1f, 0f), npc.pos.x - 22f, npc.pos.y - 22f, 44f, 44f);
            font.setColor(Color.WHITE);
            font.draw(batch, npc.name, npc.pos.x - 24f, npc.pos.y + 28f);
            if (playerPos.dst(npc.pos) <= 58f) {
                font.draw(batch, "E: Talk", npc.pos.x - 22f, npc.pos.y - 22f);
            }
            npcIndex++;
        }
        batch.end();
    }

    private void drawRobots() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (int i = 0; i < robots.length; i++) {
            RobotCompanion robot = robots[i];
            if (!hasActiveRobotAt(i) || robot.health <= 0f) {
                continue;
            }
            drawShadow(robot.pos.x, robot.pos.y, ROBOT_SIZE + 12f, 16f, 0.4f);
            TextureRegion sprite = getAnimatedFrame(robotAnimations[i % robotAnimations.length], robot.facing, true, robot.animationTime);
            drawAnimatedSprite(sprite, robot.facing, robot.pos.x - ROBOT_SIZE / 2, robot.pos.y - ROBOT_SIZE / 2, ROBOT_SIZE, ROBOT_SIZE);
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < robots.length; i++) {
            RobotCompanion robot = robots[i];
            if (!hasActiveRobotAt(i) || robot.health <= 0f) {
                continue;
            }
            float barWidth = ROBOT_SIZE;
            float barHeight = 3f;
            float barX = robot.pos.x - barWidth / 2;
            float barY = robot.pos.y + ROBOT_SIZE / 2 + 4f;
            float hpPercent = robot.health / Math.max(1f, getRobotStats(i).maxHealth);

            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            shapeRenderer.setColor(0.2f, 0.8f, 0.9f, 0.9f);
            shapeRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
        }

        shapeRenderer.end();
    }

    private void drawBaseDefenders() {
        if (activeBaseDefenders.isEmpty()) {
            return;
        }

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender == null || !defender.isActive()) {
                continue;
            }
            Vector2 guardHeading = new Vector2(defender.getGuardPosition()).sub(defender.getPosition());
            if (guardHeading.isZero()) {
                guardHeading.set(0f, -1f);
            }
            drawShadow(defender.getPosition().x, defender.getPosition().y, BASE_DEFENDER_SIZE + 12f, 16f, 0.38f);
            TextureRegion sprite = getAnimatedFrame(
                robotAnimations[getDefenderSpriteIndex(defender.getRobotId())],
                guardHeading,
                true,
                defender.getAnimationTime()
            );
            drawAnimatedSprite(
                sprite,
                guardHeading,
                defender.getPosition().x - BASE_DEFENDER_SIZE / 2f,
                defender.getPosition().y - BASE_DEFENDER_SIZE / 2f,
                BASE_DEFENDER_SIZE,
                BASE_DEFENDER_SIZE
            );
            font.setColor(Color.WHITE);
            font.draw(batch, defender.getDisplayName(), defender.getPosition().x - 28f, defender.getPosition().y + BASE_DEFENDER_SIZE / 2f + 18f);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender == null || !defender.isActive()) {
                continue;
            }
            float barWidth = BASE_DEFENDER_SIZE;
            float barHeight = 3f;
            float barX = defender.getPosition().x - barWidth / 2f;
            float barY = defender.getPosition().y + BASE_DEFENDER_SIZE / 2f + 4f;
            float hpPercent = defender.getCurrentHealth() / Math.max(1f, defender.getMaxHealth());
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            shapeRenderer.setColor(0.84f, 0.68f, 0.22f, 0.92f);
            shapeRenderer.rect(barX, barY, barWidth * hpPercent, barHeight);
        }
        shapeRenderer.end();
    }

    private void drawRobotAttackLines() {
        if (robotAttackLines.isEmpty()) {
            return;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 1f, 0f, 1f);
        for (RobotAttackLine attackLine : robotAttackLines) {
            shapeRenderer.line(attackLine.from, attackLine.to);
        }
        shapeRenderer.end();
    }

    private RobotCompanion findClosestLivingRobot(Vector2 source) {
        RobotCompanion nearestRobot = null;
        float nearestDistance = Float.MAX_VALUE;

        for (int i = 0; i < robots.length; i++) {
            RobotCompanion robot = robots[i];
            if (!hasActiveRobotAt(i) || robot.health <= 0f) {
                continue;
            }

            float distance = source.dst(robot.pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestRobot = robot;
            }
        }

        return nearestRobot;
    }

    private float[] getRobotHealthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? robots[i].health : 0f;
        }
        return values;
    }

    private float[] getRobotMaxHealthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? getRobotStats(i).maxHealth : 0f;
        }
        return values;
    }

    private float[] getRobotBaseMaxHealthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = robots[i].maxHealth;
        }
        return values;
    }

    private float[] getRobotAgilityValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? getRobotStats(i).agility : 0f;
        }
        return values;
    }

    private float[] getRobotStrengthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? getRobotStats(i).strength : 0f;
        }
        return values;
    }

    private float[] getRobotIntelligenceValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? getRobotStats(i).intelligence : 0f;
        }
        return values;
    }

    private float[] getRobotStaminaValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = hasActiveRobotAt(i) ? getRobotStats(i).stamina : 0f;
        }
        return values;
    }

    private float[] getRobotXValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = robots[i].pos.x;
        }
        return values;
    }

    private float[] getRobotYValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = robots[i].pos.y;
        }
        return values;
    }

    private float[] getRobotAngleValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = robots[i].angleDeg;
        }
        return values;
    }

    private float[] getRobotAttackTimerValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = robots[i].attackTimer;
        }
        return values;
    }

    private void drawGoldPopups() {
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();

        for (GoldPopup popup : goldPopups) {
            float alpha = 1f - (popup.timer / popup.lifetime);
            font.setColor(1f, 1f, 0f, alpha); // yellow
            font.draw(batch, popup.text, popup.pos.x - 20, popup.pos.y);
        }

        batch.end();
    }

    private void drawDialogOverlay() {
        if (activeDialog == null || activeSpeaker == null) {
            return;
        }

        resetDialogPageIfNeeded();
        List<String> dialogPages = paginateDialog(activeDialog, 82, 3);
        String currentPage = dialogPages.get(Math.min(dialogPageIndex, dialogPages.size() - 1));
        List<String> wrappedDialog = wrapTextLines(currentPage, 82);
        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.06f, 0.1f, 0.92f);
        shapeRenderer.rect(30f, 30f, w - 60f, 120f);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, activeSpeaker, 48f, 128f);
        float y = 98f;
        for (String line : wrappedDialog) {
            font.draw(batch, line, 48f, y);
            y -= 22f;
            if (y < 58f) {
                break;
            }
        }
        font.draw(batch,
            dialogPageIndex + 1 < dialogPages.size()
                ? "Press E or Enter to continue."
                : "Press E near an NPC to change the conversation.",
            48f, 48f);
        batch.end();
    }

    private void drawQuestOverlay() {
        if (!questMenuOpen) {
            return;
        }

        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.04f, 0.08f, 0.86f);
        shapeRenderer.rect(0f, 0f, w, h);
        shapeRenderer.setColor(0.09f, 0.11f, 0.16f, 0.97f);
        shapeRenderer.rect(140f, 88f, w - 280f, h - 176f);
        drawQuestMenuTabBackgrounds(w, h);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Field Ledger", 172f, h - 126f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Q or ESC to close  |  Left/Right or Tab to switch tabs", w - 560f, h - 126f);

        drawQuestMenuTabs(batch, h);
        drawQuestMenuTabContent(batch, h);
        batch.end();
    }

    private void drawQuestMenuTabBackgrounds(float width, float height) {
        float startX = 172f;
        float y = height - 164f;
        float availableWidth = Math.max(780f, width - 344f);
        float tabWidth = Math.max(96f, Math.min(150f, (availableWidth - (QUEST_MENU_TABS.length - 1) * 10f) / QUEST_MENU_TABS.length));
        for (int i = 0; i < QUEST_MENU_TABS.length; i++) {
            boolean active = i == questMenuTabIndex;
            shapeRenderer.setColor(active ? new Color(0.24f, 0.30f, 0.44f, 0.95f) : new Color(0.10f, 0.12f, 0.18f, 0.92f));
            shapeRenderer.rect(startX + i * (tabWidth + 10f), y - 24f, tabWidth, 30f);
        }
    }

    private void drawQuestMenuTabs(SpriteBatch batch, float height) {
        float startX = 172f;
        float y = height - 164f;
        float availableWidth = Math.max(780f, uiViewport.getWorldWidth() - 344f);
        float tabWidth = Math.max(96f, Math.min(150f, (availableWidth - (QUEST_MENU_TABS.length - 1) * 10f) / QUEST_MENU_TABS.length));
        for (int i = 0; i < QUEST_MENU_TABS.length; i++) {
            boolean active = i == questMenuTabIndex;
            font.setColor(active ? Color.WHITE : Color.LIGHT_GRAY);
            font.draw(batch, QUEST_MENU_TABS[i], startX + 14f + i * (tabWidth + 10f), y - 4f);
        }
    }

    private void drawQuestMenuTabContent(SpriteBatch batch, float height) {
        switch (questMenuTabIndex) {
            case 1:
                drawQuestMenuListSection(batch, height, getCommandTabHeading(), getCommandTabLines(), "Command data unavailable.");
                break;
            case 2:
                drawQuestMenuListSection(batch, height, "War Theater", getWarStatusLines(), "War-state data unavailable.");
                break;
            case 3:
                drawQuestMenuListSection(batch, height, "Strategic Map", getStrategicMapLines(), "Strategic map unavailable.");
                break;
            case 4:
                drawQuestMenuListSection(batch, height, "Forge Components", getMaterialInventoryLines(), "No forge components collected yet.");
                break;
            case 5:
                drawQuestMenuListSection(batch, height, "Graded Shards", getShardInventoryLines(), "No graded shards recovered yet.");
                break;
            case 6:
                drawQuestMenuListSection(batch, height, "Blueprint Fragments", getBlueprintFragmentInventoryLines(), "No blueprint fragments recovered yet.");
                break;
            case 7:
                drawQuestMenuListSection(batch, height, "Items", getItemInventoryLines(), "No usable items carried.");
                break;
            case 0:
            default:
                drawQuestJournalTab(batch, height);
                break;
        }
    }

    private void drawQuestJournalTab(SpriteBatch batch, float height) {
        List<String> questLines = getQuestJournalLines();
        List<String> wrappedObjective = wrapTextLines(getCurrentObjective(), 64);
        float y = height - 218f;
        font.setColor(new Color(0.88f, 0.9f, 0.98f, 1f));
        font.draw(batch, "Current Objective", 172f, y);
        y -= 34f;
        font.setColor(Color.WHITE);
        for (String line : wrappedObjective) {
            font.draw(batch, line, 172f, y);
            y -= 26f;
        }

        y -= 20f;
        font.setColor(new Color(0.88f, 0.9f, 0.98f, 1f));
        font.draw(batch, "Active Quests", 172f, y);
        y -= 34f;
        font.setColor(Color.WHITE);
        if (questLines.isEmpty()) {
            font.draw(batch, "No active quests. Explore the frontier and talk to the crew.", 172f, y);
            return;
        }
        int drawn = 0;
        for (String questLine : questLines) {
            for (String wrapped : wrapTextLines(questLine, 78)) {
                if (drawn >= 12) {
                    return;
                }
                font.draw(batch, wrapped, 172f, y);
                y -= 24f;
                drawn++;
            }
            if (drawn >= 12) {
                return;
            }
            y -= 10f;
        }
    }

    public List<String> getActTwoCommandLines() {
        List<String> lines = new ArrayList<>();
        int forgeCoreLevel = getForgeCoreLevel();
        int defeatedBosses = gameState.getDefeatedBossCount();
        if (forgeCoreLevel < 2) {
            lines.add("Act 2 gate: Forge Core Lv2 unlocks after 5 boss defeats. Current progress: " + defeatedBosses + "/5.");
            lines.add("Priority: keep pushing frontier bosses until Ironhaven's economy districts come online.");
            return lines;
        }

        lines.add("Forge Core Lv" + forgeCoreLevel + " online. Boss defeats logged: " + defeatedBosses + ".");
        if (forgeCoreLevel < 3) {
            lines.add("Next milestone: Forge Core Lv3 at 10 boss defeats. Remaining: " + Math.max(0, 10 - defeatedBosses) + ".");
        } else if (forgeCoreLevel < 4) {
            lines.add("Next milestone: Forge Core Lv4 at 15 boss defeats. Remaining: " + Math.max(0, 15 - defeatedBosses) + ".");
        } else {
            lines.add("Forge Core is at maximum resonance. Act 2 command systems are fully online.");
        }

        String forgeCoreQuestState = questManager.getQuestState(gameState, "forge_core_rising");
        if (!QuestManager.NOT_STARTED.equals(forgeCoreQuestState) && !QuestManager.COMPLETED.equals(forgeCoreQuestState)) {
            lines.add("Command briefing active: " + questManager.getCurrentObjective(gameState));
        }

        lines.add(buildFacilityProgressLine("Data Vaults", "settlement.data_vaults", "settlement_plan", 3, "Professor Cogs"));
        lines.add(buildFacilityProgressLine("Prototype Lab", "settlement.prototype_lab", "forge_schema", 4, "Professor Cogs"));
        lines.add("Hangar: " + (worldStateManager.isFlagActive(gameState, "settlement.hangar_open") ? "online" : "offline")
            + "  |  Archive: " + (worldStateManager.isFlagActive(gameState, "settlement.archive_open") ? "online" : "offline")
            + "  |  Training Grounds: " + (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open") ? "online" : "offline"));
        lines.add(buildApothecaryReadinessLine());
        lines.add(buildArchiveResearchLine());

        boolean addedRobotLine = false;
        int slotLimit = Math.min(getPartySlotLimit(), activeRobotIds.size());
        for (int i = 0; i < slotLimit; i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            String robotName = getRobotName(i);
            if (state.getEvolutionTier() >= 2) {
                lines.add(robotName + ": Mk-II online at Lv" + state.getLevel() + ".");
            } else if (state.getLevel() >= 5) {
                lines.add(robotName + ": ready for Mk-II if you can pay 3 Bone Fiber and 2 Bot Chassis Fragments.");
            } else {
                lines.add(robotName + ": Lv" + state.getLevel() + "  |  Mk-II unlocks at Lv5 plus Forge Core Lv2.");
            }
            addedRobotLine = true;
            if (i >= 1) {
                break;
            }
        }
        if (!addedRobotLine) {
            lines.add("No active robots registered for Act 2 progression checks.");
        }

        lines.add("Mk-II reserve frames require 2 Bot Chassis Fragments when recovered in the frontier.");
        return lines;
    }

    private String getCommandTabHeading() {
        if (warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return "Act 4 Command";
        }
        if (isActThreeCommandPhaseActive()) {
            return "Act 3 Command";
        }
        return "Act 2 Command";
    }

    private List<String> getCommandTabLines() {
        if (warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return getActFourCommandLines();
        }
        if (isActThreeCommandPhaseActive()) {
            return getActThreeCommandLines();
        }
        return getActTwoCommandLines();
    }

    private boolean isActThreeCommandPhaseActive() {
        return getForgeCoreLevel() >= 3
            || worldStateManager.isFlagActive(gameState, "frontier.sky_route_mapped")
            || worldStateManager.isFlagActive(gameState, "frontier.command_rails_online")
            || worldStateManager.isFlagActive(gameState, "frontier.dragon_roosts_active")
            || !QuestManager.NOT_STARTED.equals(questManager.getQuestState(gameState, "abyss_archive"))
            || !QuestManager.NOT_STARTED.equals(questManager.getQuestState(gameState, "fortress_command"))
            || !QuestManager.NOT_STARTED.equals(questManager.getQuestState(gameState, "void_key"));
    }

    private List<String> getActThreeCommandLines() {
        List<String> lines = new ArrayList<>();
        int forgeCoreLevel = getForgeCoreLevel();
        int defeatedBosses = gameState.getDefeatedBossCount();
        lines.add("Forge Core Lv" + forgeCoreLevel + " online. Unlocked grade: " + getUnlockedGrade() + ".");
        if (forgeCoreLevel < 4) {
            lines.add("Next milestone: Forge Core Lv4 at 15 boss defeats. Remaining: " + Math.max(0, 15 - defeatedBosses) + ".");
        } else {
            lines.add("Forge Core resonance maxed. Final-route support lattice is fully energized.");
        }

        String actThreeObjective = getCurrentActThreeObjective();
        if (actThreeObjective != null) {
            lines.add("Act 3 operation: " + actThreeObjective);
        }

        lines.add("Route control: Archive Chart " + stateLabel("frontier.sky_route_mapped", "mapped", "pending")
            + "  |  Dragon roosts " + stateLabel("frontier.dragon_roosts_active", "active", "offline")
            + "  |  Command rails " + stateLabel("frontier.command_rails_online", "active", "sealed"));
        lines.add("Danger zones: Sunken Abyss " + zoneAccessLabel(true)
            + "  |  Sky Fortress " + zoneAccessLabel(worldStateManager.isFlagActive(gameState, "frontier.sky_route_mapped"))
            + "  |  Abyssal Rift " + zoneAccessLabel(worldStateManager.isFlagActive(gameState, "frontier.command_rails_online")));
        lines.add("Advanced systems: Fusion Forge " + stateLabel("settlement.fusion_forge", "online", "offline")
            + "  |  Command Hub " + stateLabel("settlement.command_hub", "online", "offline")
            + "  |  Prototype Lab " + stateLabel("settlement.prototype_lab", "online", "offline"));
        lines.add("Field science: Experimental Compounds " + stateLabel("settlement.experimental_compounds", "ready", "pending")
            + "  |  Data Vaults " + stateLabel("settlement.data_vaults", "indexed", "pending"));
        lines.add("Elemental break: three matching elemental hits crack resistance and trigger a one-round break window.");

        boolean addedRobotLine = false;
        int slotLimit = Math.min(getPartySlotLimit(), activeRobotIds.size());
        for (int i = 0; i < slotLimit; i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            String robotName = getRobotName(i);
            if (state.getEvolutionTier() >= 3) {
                lines.add(robotName + ": Mk-III online at Lv" + state.getLevel() + ". Unique ability lattice stabilized.");
            } else if (state.getLevel() >= 10 && forgeCoreLevel >= 3 && isGradeUnlocked("C")) {
                lines.add(robotName + ": ready for Mk-III if you can pay 2 Drake Hearts.");
            } else if (state.getEvolutionTier() >= 2) {
                lines.add(robotName + ": Mk-II deployed. Mk-III requires Lv10, grade C, and Forge Core Lv3.");
            } else {
                lines.add(robotName + ": still below advanced-frame thresholds. Reach Mk-II first, then push toward Lv10.");
            }
            addedRobotLine = true;
        }
        if (!addedRobotLine) {
            lines.add("No active robots registered for Act 3 progression checks.");
        }

        lines.add("Advanced forge queue: Phoenix Visor, Solaris Frame, Comet Greaves, and Starforged Relay are now valid chase crafts.");
        return lines;
    }

    private String getCurrentActThreeObjective() {
        if (isQuestActive("abyss_archive") || isQuestActive("fortress_command") || isQuestActive("void_key")) {
            return questManager.getCurrentObjective(gameState);
        }
        if (!worldStateManager.isFlagActive(gameState, "frontier.sky_route_mapped")) {
            return "Recover the Archive Chart and open the first air route.";
        }
        if (!worldStateManager.isFlagActive(gameState, "frontier.command_rails_online")) {
            return "Restore the Command Sigil and reopen the fortress rail network.";
        }
        if (!worldStateManager.isFlagActive(gameState, "frontier.dragon_roosts_active")) {
            return "Secure Dragon Peak and activate the dragon roost network.";
        }
        return "Strengthen fusion, MK-III frames, and air mobility before the final gate.";
    }

    private boolean isQuestActive(String questId) {
        String state = questManager.getQuestState(gameState, questId);
        return !QuestManager.NOT_STARTED.equals(state) && !QuestManager.COMPLETED.equals(state);
    }

    private String stateLabel(String worldFlag, String activeLabel, String inactiveLabel) {
        return worldStateManager.isFlagActive(gameState, worldFlag) ? activeLabel : inactiveLabel;
    }

    private String zoneAccessLabel(boolean unlocked) {
        return unlocked ? "reachable" : "locked";
    }

    private List<String> getActFourCommandLines() {
        return new ArrayList<>(getCurrentWarPhaseSnapshot().getCommandLines());
    }

    private List<String> getWarStatusLines() {
        List<String> lines = new ArrayList<>();
        WarPhaseSnapshot snapshot = getCurrentWarPhaseSnapshot();
        lines.add("War theater: " + getActiveWorldEventName() + ".");
        if (!snapshot.isUnlocked()) {
            lines.add("Act 4 is not online yet. Bring Forge Core Lv4 online, secure the command hub, or push deeper boss progression.");
            lines.add("Current frontier scale: " + getTotalClaimedTerritories() + " territories, " + getGuildSettlementCount() + " guild settlements.");
            lines.add("Infinite Dungeon best floor: " + gameState.getInfiniteDungeonBestFloor() + ".");
            return lines;
        }

        lines.add("Influence map: territory " + snapshot.getTerritoryInfluence() + "%  |  settlement risk "
            + snapshot.getSettlementAttackRisk() + "%  |  world fronts " + snapshot.getWorldBossFrontCount() + ".");
        lines.add("Operations: convoy routes " + snapshot.getConvoyRouteCount() + "  |  large expeditions "
            + snapshot.getLargeExpeditionCount() + "  |  player quest boards " + snapshot.getPlayerQuestBoardCount() + ".");
        lines.add("Factions: Command " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0)
            + "%  |  Guilds " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0)
            + "%  |  Hostiles " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) + "%.");
        lines.addAll(getActFourCampaignStatusLines());

        if (activeWorldBossFrontsByZoneId.isEmpty()) {
            lines.add("Active fronts: none. Command scouts are rotating for the next breach window.");
        } else {
            for (Map.Entry<String, String> entry : activeWorldBossFrontsByZoneId.entrySet()) {
                lines.add("Front: " + getWorldBossFrontVariantName(entry.getKey()) + " in " + getZoneDisplayName(entry.getKey())
                    + " held by " + getMonsterDisplayName(entry.getValue())
                    + " [" + getWorldBossFrontBehavior(entry.getKey()) + " - " + getWorldBossFrontEffectSummary(entry.getKey())
                    + " | bonus " + getWorldBossFrontVariantBonusText(entry.getKey()) + "].");
            }
        }

        lines.addAll(getWarBulletinLines());
        lines.addAll(getFactionDirectiveLines());

        List<String> pressureZones = getWarPressureZoneLines();
        if (pressureZones.isEmpty()) {
            lines.add("Zone pressure: no major frontline modifiers are active right now.");
        } else {
            lines.addAll(pressureZones);
        }

        List<String> infrastructureLines = getWarInfrastructureLines();
        if (infrastructureLines.isEmpty()) {
            lines.add("Infrastructure map: no claimed corridor bonuses are online yet.");
        } else {
            lines.addAll(infrastructureLines);
        }

        List<String> projectLines = getActiveSettlementProjectLines();
        if (projectLines.isEmpty()) {
            lines.add("Settlement projects: no faction megaprojects are active yet.");
        } else {
            lines.addAll(projectLines);
        }

        lines.add(buildLargeDungeonStatusLine());
        if (pinnedExpeditionContractKind != null && pinnedExpeditionContractKind.startsWith("LARGE_DUNGEON_EXPEDITION")) {
            lines.add("Pinned descent order: " + pinnedExpeditionContractText);
        }
        return lines;
    }

    private List<String> getStrategicMapLines() {
        List<String> lines = new ArrayList<>();
        WarPhaseSnapshot snapshot = getCurrentWarPhaseSnapshot();
        List<ExpeditionLaunchDestination> destinations = getStrategicMapDestinations();
        clampStrategicMapSelection();
        ExpeditionLaunchDestination selectedDestination = destinations.isEmpty()
            ? null
            : destinations.get(Math.min(strategicMapSelectionIndex, destinations.size() - 1));
        lines.add("Strategic theater: " + getActiveWorldEventName() + ".");
        if (!snapshot.isUnlocked()) {
            lines.add("Strategic map is still forming. Act 4 unlocks once the command layer is online.");
            lines.add("Current reach: " + getTotalClaimedTerritories() + " territories  |  " + getGuildSettlementCount() + " guild settlements.");
            return lines;
        }

        lines.add("Global picture: world influence " + snapshot.getTerritoryInfluence() + "%  |  settlement risk "
            + snapshot.getSettlementAttackRisk() + "%  |  active fronts " + snapshot.getWorldBossFrontCount() + ".");
        lines.add("Faction balance: Command " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0)
            + "%  |  Guilds " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0)
            + "%  |  Hostiles " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) + "%.");
        lines.add("Route capacity: convoy lanes " + snapshot.getConvoyRouteCount() + "  |  large expeditions "
            + snapshot.getLargeExpeditionCount() + "  |  quest boards " + snapshot.getPlayerQuestBoardCount() + ".");
        lines.addAll(getActFourCampaignStatusLines());

        List<String> frontLines = buildStrategicFrontLines();
        if (frontLines.isEmpty()) {
            lines.add("Front map: no active world-boss sieges are fixed on the board right now.");
        } else {
            lines.addAll(frontLines);
        }

        List<String> corridorLines = buildStrategicCorridorLines();
        if (corridorLines.isEmpty()) {
            lines.add("Corridors: no player-controlled regions are projecting support yet.");
        } else {
            lines.addAll(corridorLines);
        }

        List<String> projectLines = buildStrategicProjectPriorityLines();
        if (projectLines.isEmpty()) {
            lines.add("Projects: no strategic megaproject has reached activation thresholds.");
        } else {
            lines.addAll(projectLines);
        }

        lines.addAll(getWarBulletinLines());

        if (selectedDestination != null) {
            for (int i = 0; i < destinations.size(); i++) {
                ExpeditionLaunchDestination destination = destinations.get(i);
                String prefix = i == strategicMapSelectionIndex ? "> " : "  ";
                lines.add(prefix + destination.label + " [" + destination.contractTierLabel + "]");
            }
            lines.add("Selected theater: " + selectedDestination.label + ".");
            lines.add(buildStrategicDirectiveLine(selectedDestination));
            lines.add(buildStrategicSupportLine(selectedDestination.zoneId));
            if (getRegionalIncidentName(selectedDestination.zoneId) != null) {
                lines.add("Regional incident: " + getRegionalIncidentName(selectedDestination.zoneId)
                    + "  |  " + getRegionalIncidentSummary(selectedDestination.zoneId) + ".");
            }
            if (getSettlementCrisisName(selectedDestination.zoneId) != null) {
                lines.add("Settlement crisis: " + getSettlementCrisisName(selectedDestination.zoneId)
                    + "  |  " + getSettlementCrisisSummary(selectedDestination.zoneId) + ".");
            }
            lines.add("Up/Down move theater focus. E deploys directly from the map.");
        }
        lines.add(buildStrategicPriorityLine());
        return lines;
    }

    private WarPhaseSnapshot getCurrentWarPhaseSnapshot() {
        return warPhaseManager.buildSnapshot(
            gameState,
            baseStatesByZoneId,
            guildDefinitionsById,
            playerName,
            factionInfluenceById,
            activeWorldBossFrontsByZoneId
        );
    }

    private boolean shouldOfferConvoyEscort(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        return warPhaseManager.isConvoyEscortRecommended(getCurrentWarPhaseSnapshot(), baseState);
    }

    private GuildDefinition getPublishingGuildForZone(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return null;
        }
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild == null) {
                continue;
            }
            if (zoneId.equals(guild.getHallZoneId())
                && guild.getHallClaimedSiteId() != null
                && !guild.getHallClaimedSiteId().isEmpty()
                && guildPermissionsEngine.canPerform(guild, playerName, PermissionAction.POST_QUESTS)) {
                return guild;
            }
        }
        return null;
    }

    private PlayerQuestContract getActivePlayerQuestContractForZone(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return null;
        }
        for (PlayerQuestContract contract : playerQuestContracts) {
            if (contract != null && contract.active && zoneId.equals(contract.zoneId)) {
                return contract;
            }
        }
        return null;
    }

    private int getActivePlayerQuestContractCount() {
        int count = 0;
        for (PlayerQuestContract contract : playerQuestContracts) {
            if (contract != null && contract.active) {
                count++;
            }
        }
        return count;
    }

    private boolean shouldOfferPlayerCreatedQuest(ExpeditionLaunchDestination destination) {
        return destination != null
            && destination.zoneId != null
            && !destination.zoneId.isEmpty()
            && getActivePlayerQuestContractForZone(destination.zoneId) != null;
    }

    private PlayerQuestContract getActivePlayerQuestContractForGuild(String guildId) {
        if (guildId == null || guildId.isEmpty()) {
            return null;
        }
        for (PlayerQuestContract contract : playerQuestContracts) {
            if (contract != null && contract.active && guildId.equals(contract.guildId)) {
                return contract;
            }
        }
        return null;
    }

    private String getLatestPlayerQuestContractKindForGuild(String guildId) {
        if (guildId == null || guildId.isEmpty()) {
            return null;
        }
        for (int i = playerQuestContracts.size() - 1; i >= 0; i--) {
            PlayerQuestContract contract = playerQuestContracts.get(i);
            if (contract != null && guildId.equals(contract.guildId) && contract.kind != null && !contract.kind.isEmpty()) {
                return contract.kind;
            }
        }
        return null;
    }

    private boolean shouldOfferGuildBoardContract(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            return false;
        }
        if (isActiveWorldBossFront(destination.zoneId) || shouldOfferConvoyEscort(destination)) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        return baseState != null
            && !baseState.getClaimedSiteIds().isEmpty()
            && getPublishingGuildForZone(destination.zoneId) != null;
    }

    private boolean shouldOfferGuildStrikeContract(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        GuildDefinition guild = getPublishingGuildForZone(destination.zoneId);
        ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
        boolean bossActive = definition != null
            && definition.getBossId() != null
            && !definition.getBossId().isEmpty()
            && !gameState.hasDefeatedBoss(definition.getBossId());
        boolean blockedByHigherPriorityThreat = isActiveWorldBossFront(destination.zoneId) || shouldOfferConvoyEscort(destination);
        return warPhaseManager.isGuildStrikeRecommended(baseState, guild, bossActive, blockedByHigherPriorityThreat);
    }

    private boolean shouldOfferPublicBoardContract(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            return false;
        }
        if (shouldOfferGuildBoardContract(destination)
            || shouldOfferGuildStrikeContract(destination)
            || isActiveWorldBossFront(destination.zoneId)
            || shouldOfferConvoyEscort(destination)) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        return baseState != null
            && !baseState.getClaimedSiteIds().isEmpty()
            && warPhaseManager.isWarPhaseUnlocked(gameState)
            && getDestinationFragmentId(destination.zoneId) == null
            && (expeditionBoardReputation >= 4 || worldStateManager.isFlagActive(gameState, "settlement.command_hub"));
    }

    private boolean shouldOfferPublicRecoveryContract(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        boolean fragmentRouteAvailable = getDestinationFragmentId(destination.zoneId) != null;
        boolean blockedByHigherPriorityThreat = shouldOfferGuildBoardContract(destination)
            || shouldOfferGuildStrikeContract(destination)
            || isActiveWorldBossFront(destination.zoneId)
            || shouldOfferConvoyEscort(destination);
        return warPhaseManager.isPublicRecoveryRecommended(baseState, fragmentRouteAvailable, blockedByHigherPriorityThreat);
    }

    private boolean shouldOfferLargeDungeonExpedition(ExpeditionLaunchDestination destination) {
        if (destination == null || !INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            return false;
        }
        return warPhaseManager.isWarPhaseUnlocked(gameState) && expeditionBoardReputation >= 4;
    }

    private String buildFacilityProgressLine(String facilityName, String worldFlag, String fragmentId, int requiredCount, String contactName) {
        if (worldFlag != null && !worldFlag.isEmpty() && worldStateManager.isFlagActive(gameState, worldFlag)) {
            return facilityName + ": online.";
        }
        int current = fragmentId != null && !fragmentId.isEmpty() ? gameState.getBlueprintFragmentCount(fragmentId) : 0;
        String fragmentName = fragmentId != null && !fragmentId.isEmpty() ? getBlueprintFragmentName(fragmentId) : "fragments";
        return facilityName + ": " + current + "/" + requiredCount + " " + fragmentName
            + " banked. Check with " + contactName + " once you're ready to fund it.";
    }

    private String buildApothecaryReadinessLine() {
        if (!worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock")) {
            return "Apothecary: basic supplies only. Elena still needs the first stock restoration.";
        }
        int potions = Math.max(0, healingPotions);
        if (worldStateManager.isFlagActive(gameState, "settlement.experimental_compounds")) {
            return "Apothecary: compound lab online. Current field kits ready: " + potions + ".";
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.field_kit_supply")) {
            return "Apothecary: field-kit line online. Current field kits ready: " + potions + ".";
        }
        return "Apothecary: stocked for emergency care. Current field kits ready: " + potions + ".";
    }

    private String buildArchiveResearchLine() {
        if (!worldStateManager.isFlagActive(gameState, "settlement.archive_open")) {
            return "Archive: offline. Professor Cogs is still building the desk and indexing lanes.";
        }
        int scanned = 0;
        int fullyScanned = 0;
        for (Integer level : gameState.getBestiaryScanLevels().values()) {
            if (level == null || level <= 0) {
                continue;
            }
            scanned++;
            if (level >= 3) {
                fullyScanned++;
            }
        }
        return "Archive: " + scanned + " hostile profiles logged, " + fullyScanned + " fully decoded. Blueprint stock: "
            + sumInventoryAmounts(gameState.getBlueprintFragments()) + ".";
    }

    private void drawQuestMenuListSection(SpriteBatch batch, float height, String heading, List<String> lines, String emptyText) {
        float y = height - 218f;
        font.setColor(new Color(0.88f, 0.9f, 0.98f, 1f));
        font.draw(batch, heading, 172f, y);
        y -= 34f;
        font.setColor(Color.WHITE);
        if (lines == null || lines.isEmpty()) {
            font.draw(batch, emptyText, 172f, y);
            return;
        }
        int drawn = 0;
        for (String line : lines) {
            for (String wrapped : wrapTextLines(line, 78)) {
                if (drawn >= 15) {
                    return;
                }
                font.draw(batch, wrapped, 172f, y);
                y -= 24f;
                drawn++;
            }
            if (drawn >= 15) {
                return;
            }
            y -= 8f;
        }
    }

    private void drawExpeditionBoardOverlayExpanded() {
        if (!expeditionBoardOpen) {
            return;
        }

        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        float panelX = 120f;
        float panelY = 88f;
        float panelW = w - 240f;
        float panelH = h - 176f;
        float gutter = 18f;
        float columnW = (panelW - 48f - gutter * 2f) / 3f;
        float leftX = panelX + 24f;
        float midX = leftX + columnW + gutter;
        float rightX = midX + columnW + gutter;
        float columnTop = h - 212f;

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.04f, 0.08f, 0.88f);
        shapeRenderer.rect(0f, 0f, w, h);
        shapeRenderer.setColor(0.09f, 0.11f, 0.16f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.setColor(0.12f, 0.16f, 0.22f, 0.95f);
        shapeRenderer.rect(leftX - 12f, panelY + 28f, columnW + 18f, panelH - 132f);
        shapeRenderer.rect(midX - 12f, panelY + 28f, columnW + 18f, panelH - 132f);
        shapeRenderer.rect(rightX - 12f, panelY + 28f, columnW + 18f, panelH - 132f);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, getExpeditionBoardTitle(), panelX + 26f, h - 126f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "P or ESC to close  |  Review launch point, checklist, and frontier projects before the next sortie.",
            panelX + 26f, h - 150f);

        drawOverlayListSection(batch, "Launch Point", getExpeditionBoardLaunchLines(), leftX, columnTop, 30);
        drawOverlayListSection(batch, "Launch Checklist", getExpeditionChecklistLines(), midX, columnTop, 30);
        drawOverlayListSection(batch, "Frontier Projects", getExpeditionProjectLines(), rightX, columnTop, 30);
        batch.end();
    }

    private void drawOverlayListSection(SpriteBatch batch, String heading, List<String> lines, float x, float topY, int maxWrappedLines) {
        float y = topY;
        int drawn = 0;
        font.setColor(new Color(0.88f, 0.9f, 0.98f, 1f));
        font.draw(batch, heading, x, y);
        y -= 34f;
        font.setColor(Color.WHITE);
        if (lines == null || lines.isEmpty()) {
            font.draw(batch, "No data available.", x, y);
            return;
        }
        for (String line : lines) {
            for (String wrapped : wrapTextLines(line, 34)) {
                if (drawn >= maxWrappedLines) {
                    return;
                }
                font.draw(batch, wrapped, x, y);
                y -= 24f;
                drawn++;
            }
            if (drawn >= maxWrappedLines) {
                return;
            }
            y -= 8f;
        }
    }

    private String getExpeditionBoardTitle() {
        if (isHubTownZone()) {
            return "Expedition Board - Ironhaven Command";
        }
        return "Expedition Board - Frontier Staging";
    }

    private List<String> getExpeditionBoardLaunchLines() {
        List<String> lines = new ArrayList<>();
        WarPhaseSnapshot warSnapshot = getCurrentWarPhaseSnapshot();
        List<ExpeditionLaunchDestination> destinations = getExpeditionLaunchDestinations();
        clampExpeditionBoardSelection();
        ExpeditionLaunchDestination selectedDestination = destinations.isEmpty()
            ? null
            : destinations.get(Math.min(expeditionBoardSelectionIndex, destinations.size() - 1));
        if (isHubTownZone()) {
            lines.add("Current launch point: Ironhaven. Full banking, forge, shops, and recovery support are available here.");
            lines.add("Town districts online: Hangar " + stateLabel("settlement.hangar_open", "ready", "offline")
                + ", Archive " + stateLabel("settlement.archive_open", "ready", "offline")
                + ", Training Grounds " + stateLabel("settlement.training_grounds_open", "ready", "offline") + ".");
            lines.add("Owned frontier territories: " + getTotalClaimedTerritories() + ". Guild settlements: " + getGuildSettlementCount() + ".");
        } else {
            BaseState baseState = getCurrentBaseState();
            int claimedSites = baseState != null ? baseState.getClaimedSiteIds().size() : 0;
            lines.add("Current launch point: owned outpost in " + getCurrentZoneDisplayName() + ".");
            lines.add("Claimed sites in this zone: " + claimedSites + ". Operational storage nearby: " + (isNearOperationalStorage() ? "yes" : "no") + ".");
            lines.add("Outpost banking " + (canBankExpeditionHaulHere() ? "is available from this staging point." : "is unavailable until storage is built and staffed here."));
        }
        lines.add("World event pressure: " + getActiveWorldEventName() + ".");
        lines.add("Board reputation: " + expeditionBoardReputation + "  |  Contract tier: " + getExpeditionBoardTierLabel() + ".");
        lines.add("Sponsor status: " + getExpeditionSponsorStatusLine() + ".");
        if (warSnapshot.isUnlocked()) {
            lines.add("War fronts: " + activeWorldBossFrontsByZoneId.size() + " active  |  Territory influence " + warSnapshot.getTerritoryInfluence()
                + "%  |  Settlement risk " + warSnapshot.getSettlementAttackRisk() + "%.");
            if (selectedDestination != null) {
                String incident = getRegionalIncidentName(selectedDestination.zoneId);
                String crisis = getSettlementCrisisName(selectedDestination.zoneId);
                if (incident != null) {
                    lines.add("Regional incident: " + incident + " - " + getRegionalIncidentSummary(selectedDestination.zoneId) + ".");
                }
                if (crisis != null) {
                    lines.add("Settlement crisis: " + crisis + " - " + getSettlementCrisisSummary(selectedDestination.zoneId) + ".");
                }
            }
        }
        if (selectedDestination != null) {
            for (int i = 0; i < destinations.size(); i++) {
                ExpeditionLaunchDestination destination = destinations.get(i);
                String prefix = i == expeditionBoardSelectionIndex ? "> " : "  ";
                lines.add(prefix + destination.label + " [" + destination.contractTierLabel + "]" + (destination.currentZone ? " [Here]" : ""));
            }
            lines.add("Selected deployment: " + selectedDestination.label + ".");
            lines.add(selectedDestination.detail);
            if (INFINITE_DUNGEON_ZONE_ID.equals(selectedDestination.zoneId)) {
                lines.add("Endgame mode: " + formatEndgameModeLabel(expeditionBoardMode)
                    + "  |  Seed: " + getSelectedHardModeSeedLabel() + ".");
            }
            if (isActiveWorldBossFront(selectedDestination.zoneId)) {
                lines.add("Marked world-boss front: " + getWorldBossFrontVariantName(selectedDestination.zoneId)
                    + ". Expect a hardened boss signature and " + getWorldBossFrontVariantBonusText(selectedDestination.zoneId) + ".");
            }
        }
        lines.add("Up/Down choose destination. Left/Right switch dungeon mode. Tab cycles hard seed. T cycles challenge rule. E launches immediately.");
        return lines;
    }

    private List<String> getExpeditionChecklistLines() {
        List<String> lines = new ArrayList<>();
        ExpeditionLaunchDestination destination = getSelectedExpeditionDestination();
        WarPhaseSnapshot warSnapshot = getCurrentWarPhaseSnapshot();
        if (pinnedExpeditionContractText != null && !pinnedExpeditionContractText.isEmpty()) {
            lines.add("Pinned contract: " + pinnedExpeditionContractText);
        }
        int slotLimit = getPartySlotLimit();
        lines.add("Forge Core Lv" + getForgeCoreLevel() + "  |  Grade " + getUnlockedGrade() + "  |  Party slots " + slotLimit + "/3.");
        String nextSlot = getPartySlotNextGrade();
        if (nextSlot != null) {
            lines.add("Next party slot unlock: " + nextSlot + ".");
        }
        lines.add("Field kits ready: " + Math.max(0, getHealingPotions()) + ".");
        long unbankedGold = Math.max(0L, gameState.getUnbankedGold());
        int unbankedComponents = sumInventoryAmounts(gameState.getUnbankedForgeComponents());
        int unbankedShards = sumInventoryAmounts(gameState.getUnbankedShards());
        int unbankedBlueprints = sumInventoryAmounts(gameState.getUnbankedBlueprintFragments());
        if (unbankedGold > 0L || unbankedComponents > 0 || unbankedShards > 0 || unbankedBlueprints > 0) {
            lines.add("Warning: unbanked haul at risk - " + unbankedGold + "g, "
                + unbankedComponents + " components, " + unbankedShards + " shards, " + unbankedBlueprints + " fragments.");
        } else {
            lines.add("No unbanked haul at risk. The next sortie starts clean.");
        }

        int activeCount = 0;
        for (int i = 0; i < Math.min(slotLimit, activeRobotIds.size()); i++) {
            if (!hasActiveRobotAt(i)) {
                lines.add("Party slot " + (i + 1) + ": empty.");
                continue;
            }
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            String robotName = getRobotName(i);
            String tierLabel = state != null ? "Mk-" + state.getEvolutionTier() : "Mk-I";
            int level = state != null ? state.getLevel() : 1;
            lines.add("Slot " + (i + 1) + ": " + robotName + " Lv" + level + " " + tierLabel + ".");
            activeCount++;
        }
        if (activeCount == 0) {
            lines.add("No active robots assigned. Visit the workshop before launching.");
        }
        lines.add("Reserve frames available: " + getReserveRobotLines().size() + ".");
        if (warSnapshot.isUnlocked()) {
            lines.add("Faction pressure: Command " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0)
                + "%  |  Guilds " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0)
                + "%  |  Hostiles " + factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) + "%.");
        }
        if (destination != null && INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            lines.add("Dungeon command profile: " + formatEndgameModeLabel(expeditionBoardMode)
                + "  |  Hard-mode seed " + getSelectedHardModeSeedLabel() + ".");
            if (ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
                lines.add("Challenge rule: " + getSelectedChallengeModifierLabel() + ".");
            }
        }
        lines.addAll(getDestinationChecklistNotes(destination, activeCount, slotLimit));
        return lines;
    }

    private List<String> getDestinationChecklistNotes(ExpeditionLaunchDestination destination, int activeCount, int slotLimit) {
        List<String> lines = new ArrayList<>();
        if (destination == null) {
            return lines;
        }
        if ("town".equals(destination.zoneId)) {
            lines.add("Town readiness: spend haul, evolve frames, and restock before deploying again.");
            return lines;
        }

        ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
        if (definition == null) {
            return lines;
        }

        int recommendedKits = definition.isExpansiveFrontier() ? 3 : definition.getBossId() != null && !definition.getBossId().isEmpty() ? 4 : 2;
        int currentKits = Math.max(0, getHealingPotions());
        if (currentKits < recommendedKits) {
            lines.add("Readiness warning: " + destination.label + " is better approached with about " + recommendedKits
                + " field kits. Current stock is " + currentKits + ".");
        } else {
            lines.add("Supply check: field kits meet the current recommendation for " + destination.label + ".");
        }

        String rankFloor = definition.getRankFloor();
        if (rankFloor != null && !rankFloor.isEmpty() && gradeIndex(getUnlockedGrade()) < gradeIndex(rankFloor)) {
            lines.add("Power warning: zone floor rank is " + rankFloor + " but your current grade is " + getUnlockedGrade() + ".");
        } else {
            lines.add("Power check: your grade is on pace for this deployment band.");
        }

        if (slotLimit < 2 && definition.isExpansiveFrontier()) {
            lines.add("Team warning: frontier expansion is smoother once Forge Core Lv2 and a second party slot are online.");
        } else if (activeCount < Math.min(2, slotLimit) && definition.isExpansiveFrontier()) {
            lines.add("Team warning: deploy at least two active frames before pushing deeper into the frontier.");
        }

        int lowestRobotLevel = Integer.MAX_VALUE;
        for (int i = 0; i < Math.min(slotLimit, activeRobotIds.size()); i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state != null) {
                lowestRobotLevel = Math.min(lowestRobotLevel, state.getLevel());
            }
        }
        if (lowestRobotLevel != Integer.MAX_VALUE) {
            int recommendedLevel = definition.isExpansiveFrontier() ? 4 : definition.getBossId() != null && !definition.getBossId().isEmpty() ? 6 : 5;
            if (lowestRobotLevel < recommendedLevel) {
                lines.add("Frame warning: lowest active robot level is " + lowestRobotLevel + ". Recommended floor for this route is around Lv" + recommendedLevel + ".");
            } else {
                lines.add("Frame check: active robot levels are holding above the suggested floor for this route.");
            }
        }

        String fragmentGoal = getDestinationFragmentGoal(destination.zoneId);
        if (fragmentGoal != null) {
            lines.add(fragmentGoal);
        }

        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        if (definition.isExpansiveFrontier() && (baseState == null || baseState.getClaimedSiteIds().isEmpty())) {
            lines.add("Foothold warning: no claimed site is active there yet. Prioritize a claim before a long salvage run.");
        }
        return lines;
    }

    private String getDestinationFragmentGoal(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return null;
        }
        if ("verdant_fields".equals(zoneId)) {
            return "Recommended target: Bot Chassis Fragments for Mk-II frames and reserve recruitment.";
        }
        if ("shadow_caves".equals(zoneId)) {
            return "Recommended target: Settlement Plan Fragments to finish Data Vaults and deepen Ironhaven's archive.";
        }
        if ("rusty_quarry".equals(zoneId) || "crystal_depths".equals(zoneId)) {
            return "Recommended target: Forge Schema Fragments for Prototype Lab and stronger gear projects.";
        }
        return null;
    }

    private void cycleExpeditionBoardSelection(int delta) {
        List<ExpeditionLaunchDestination> destinations = getExpeditionLaunchDestinations();
        if (destinations.isEmpty()) {
            expeditionBoardSelectionIndex = 0;
            return;
        }
        expeditionBoardSelectionIndex = Math.floorMod(expeditionBoardSelectionIndex + delta, destinations.size());
    }

    private void clampExpeditionBoardSelection() {
        List<ExpeditionLaunchDestination> destinations = getExpeditionLaunchDestinations();
        if (destinations.isEmpty()) {
            expeditionBoardSelectionIndex = 0;
            return;
        }
        expeditionBoardSelectionIndex = Math.floorMod(expeditionBoardSelectionIndex, destinations.size());
    }

    private void cycleEndgameDeploymentMode(int delta) {
        ExpeditionLaunchDestination destination = getSelectedExpeditionDestination();
        if (destination == null || !INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            return;
        }
        List<String> availableModes = getAvailableEndgameModes();
        if (availableModes.isEmpty()) {
            expeditionBoardMode = ENDGAME_MODE_STANDARD;
            return;
        }
        int currentIndex = Math.max(0, availableModes.indexOf(expeditionBoardMode));
        expeditionBoardMode = availableModes.get(Math.floorMod(currentIndex + delta, availableModes.size()));
    }

    private List<String> getAvailableEndgameModes() {
        List<String> modes = new ArrayList<>();
        modes.add(ENDGAME_MODE_STANDARD);
        if (forgeLegacyEngine.areChallengeRunsUnlocked(gameState.getInfiniteDungeonBestFloor())) {
            modes.add(ENDGAME_MODE_CHALLENGE);
        }
        if (forgeLegacyEngine.isBossRushUnlocked(getDefeatedEndgameBossCount())) {
            modes.add(ENDGAME_MODE_BOSS_RUSH);
        }
        return modes;
    }

    private void cycleChallengeModifier(int delta) {
        ExpeditionLaunchDestination destination = getSelectedExpeditionDestination();
        if (destination == null || !INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId) || !ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
            return;
        }
        selectedChallengeModifierIndex = Math.floorMod(selectedChallengeModifierIndex + delta, CHALLENGE_MODIFIER_ORDER.length);
    }

    private void cycleHardModeSeed(int delta) {
        ExpeditionLaunchDestination destination = getSelectedExpeditionDestination();
        if (destination == null || !INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            return;
        }
        int unlockedSeeds = forgeLegacyEngine.getHardModeSeedsUnlocked(gameState.getInfiniteDungeonBestFloor());
        int maxIndex = Math.min(unlockedSeeds, HARD_MODE_SEED_NAMES.length - 1);
        selectedHardModeSeedIndex = Math.floorMod(selectedHardModeSeedIndex + delta, Math.max(1, maxIndex + 1));
    }

    private String getSelectedChallengeModifierId() {
        return CHALLENGE_MODIFIER_ORDER[Math.floorMod(selectedChallengeModifierIndex, CHALLENGE_MODIFIER_ORDER.length)];
    }

    private String getSelectedChallengeModifierLabel() {
        String modifierId = getSelectedChallengeModifierId();
        if ("GLASS_CANNON".equals(modifierId)) {
            return "Glass Cannon";
        }
        if ("THIN_SUPPLIES".equals(modifierId)) {
            return "Thin Supplies";
        }
        if ("OVERCLOCKED".equals(modifierId)) {
            return "Overclocked";
        }
        return "Challenge";
    }

    private String getSelectedHardModeSeedLabel() {
        return HARD_MODE_SEED_NAMES[Math.min(selectedHardModeSeedIndex, HARD_MODE_SEED_NAMES.length - 1)];
    }

    private String formatEndgameModeLabel(String modeId) {
        if (ENDGAME_MODE_CHALLENGE.equals(modeId)) {
            return "Challenge Run";
        }
        if (ENDGAME_MODE_BOSS_RUSH.equals(modeId)) {
            return "Boss Rush";
        }
        return "Standard Descent";
    }

    private List<ExpeditionLaunchDestination> getStrategicMapDestinations() {
        List<ExpeditionLaunchDestination> destinations = new ArrayList<>();
        for (ExpeditionLaunchDestination destination : getExpeditionLaunchDestinations()) {
            if (destination != null && destination.zoneId != null && !"town".equals(destination.zoneId)) {
                destinations.add(destination);
            }
        }
        return destinations;
    }

    private void cycleStrategicMapSelection(int delta) {
        List<ExpeditionLaunchDestination> destinations = getStrategicMapDestinations();
        if (destinations.isEmpty()) {
            strategicMapSelectionIndex = 0;
            return;
        }
        strategicMapSelectionIndex = Math.floorMod(strategicMapSelectionIndex + delta, destinations.size());
    }

    private void clampStrategicMapSelection() {
        List<ExpeditionLaunchDestination> destinations = getStrategicMapDestinations();
        if (destinations.isEmpty()) {
            strategicMapSelectionIndex = 0;
            return;
        }
        strategicMapSelectionIndex = Math.floorMod(strategicMapSelectionIndex, destinations.size());
    }

    private ExpeditionLaunchDestination getSelectedStrategicMapDestination() {
        List<ExpeditionLaunchDestination> destinations = getStrategicMapDestinations();
        if (destinations.isEmpty()) {
            return null;
        }
        clampStrategicMapSelection();
        return destinations.get(strategicMapSelectionIndex);
    }

    private void launchSelectedStrategicMapDestination() {
        ExpeditionLaunchDestination destination = getSelectedStrategicMapDestination();
        if (destination == null) {
            showStandaloneDialog("Strategic Map", "No deployable theater is currently available.");
            return;
        }
        questMenuOpen = false;
        pinExpeditionContract(destination);
        loadZone(destination.zoneId, destination.spawnId, true);
        showStandaloneDialog("Strategic Map", "Strategic deployment confirmed: " + destination.label + ". "
            + (pinnedExpeditionContractText != null ? "Directive: " + pinnedExpeditionContractText : ""));
    }

    private List<ExpeditionLaunchDestination> getExpeditionLaunchDestinations() {
        syncWarPhaseState(false);
        List<ExpeditionLaunchDestination> destinations = new ArrayList<>();
        addExpeditionLaunchDestination(destinations, "town", "town_square", "Ironhaven",
            "Return to Ironhaven for full banking, drafting, and facility support.");
        for (Map.Entry<String, BaseState> entry : baseStatesByZoneId.entrySet()) {
            String zoneId = entry.getKey();
            BaseState baseState = entry.getValue();
            if (zoneId == null || zoneId.isEmpty() || "town".equals(zoneId) || baseState == null || baseState.getClaimedSiteIds().isEmpty()) {
                continue;
            }
            String label = getZoneDisplayName(zoneId);
            String detail = "Deploy to " + label + " from your claimed frontier staging route. Claimed sites: "
                + baseState.getClaimedSiteIds().size() + ".";
            addExpeditionLaunchDestination(destinations, zoneId, null, label, detail);
        }
        if (destinations.size() == 1 && zoneDefinitions.containsKey("verdant_fields")) {
            addExpeditionLaunchDestination(destinations, "verdant_fields", null, getZoneDisplayName("verdant_fields"),
                "Deploy through the first frontier gate and begin a fresh scouting push.");
        }
        addSponsoredExpeditionDestinations(destinations);
        return destinations;
    }

    private void addSponsoredExpeditionDestinations(List<ExpeditionLaunchDestination> destinations) {
        if (expeditionBoardReputation >= 4) {
            if (zoneDefinitions.containsKey("whispering_forest")) {
                addExpeditionLaunchDestination(destinations, "whispering_forest", null, "Whispering Forest Relay",
                    "Sponsored relay route. The board has charted a quieter approach into Whispering Forest for silver-tier contracts.");
            }
            if (getForgeCoreLevel() >= 2 && zoneDefinitions.containsKey("rusty_quarry")) {
                addExpeditionLaunchDestination(destinations, "rusty_quarry", null, "Rusty Quarry Survey",
                    "Sponsored extraction route. Silver board standing unlocks a direct salvage lane into Rusty Quarry.");
            }
        }
        if (expeditionBoardReputation >= 10) {
            if (getForgeCoreLevel() >= 3 && zoneDefinitions.containsKey("crystal_depths")) {
                addExpeditionLaunchDestination(destinations, "crystal_depths", null, "Crystal Depths Charter",
                    "Gold board charter. High-value fragment teams are being routed straight into Crystal Depths.");
            }
            if (worldStateManager.isFlagActive(gameState, "frontier.sky_route_mapped") && zoneDefinitions.containsKey("sky_fortress")) {
                addExpeditionLaunchDestination(destinations, "sky_fortress", null, "Sky Fortress Writ",
                    "Gold board writ. A rare command flight plan has opened a sponsored strike route to Sky Fortress.");
            }
        }
        if (warPhaseManager.isWarPhaseUnlocked(gameState)
            && expeditionBoardReputation >= 4
            && zoneDefinitions.containsKey(INFINITE_DUNGEON_ZONE_ID)) {
            addExpeditionLaunchDestination(destinations, INFINITE_DUNGEON_ZONE_ID, "home_spawn", "Legacy Descent",
                "Large expedition order. Bolt Simulation is now being used as a live war-depth rehearsal for multi-floor strike crews.");
        }
        addDragonRoostDestinations(destinations);
    }

    private void addDragonRoostDestinations(List<ExpeditionLaunchDestination> destinations) {
        if (!worldStateManager.isFlagActive(gameState, "frontier.dragon_roosts_active")) {
            return;
        }
        if (zoneDefinitions.containsKey("scorched_plateau")) {
            addExpeditionLaunchDestination(destinations, "scorched_plateau", null, "Scorched Plateau Roost",
                "Dragon roost route. Your drake can drop the crew directly onto the plateau's outer ridge.");
        }
        if (zoneDefinitions.containsKey("frozen_vale")) {
            addExpeditionLaunchDestination(destinations, "frozen_vale", null, "Frozen Vale Roost",
                "Dragon roost route. Aerial insertion cuts past the cliff road and lands near the frozen cloister.");
        }
        if (zoneDefinitions.containsKey("crystal_depths")) {
            addExpeditionLaunchDestination(destinations, "crystal_depths", null, "Crystal Depths Overflight",
                "Dragon roost route. A high drop into the crystal shelf reaches the depths faster than the old cave trail.");
        }
        if (worldStateManager.isFlagActive(gameState, "frontier.sky_route_mapped") && zoneDefinitions.containsKey("sky_fortress")) {
            addExpeditionLaunchDestination(destinations, "sky_fortress", null, "Sky Fortress Air Rail",
                "Dragon-assisted air route. Your mount carries the crew onto the restored sky approach beneath the fortress.");
        }
    }

    private void addExpeditionLaunchDestination(
        List<ExpeditionLaunchDestination> destinations,
        String zoneId,
        String spawnId,
        String label,
        String detail
    ) {
        if (zoneId == null || zoneId.isEmpty() || !zoneDefinitions.containsKey(zoneId)) {
            return;
        }
        ZoneAccessPolicy.AccessDecision accessDecision = evaluateZoneAccess(zoneId);
        if (!zoneId.equals(currentZoneId) && !accessDecision.isAllowed()) {
            return;
        }
        for (ExpeditionLaunchDestination existing : destinations) {
            if (existing.zoneId.equals(zoneId)) {
                return;
            }
        }
        destinations.add(new ExpeditionLaunchDestination(
            zoneId,
            spawnId,
            label,
            detail,
            zoneId.equals(currentZoneId),
            getDestinationContractTierLabel(zoneId)
        ));
    }

    private void launchSelectedExpeditionDestination() {
        List<ExpeditionLaunchDestination> destinations = getExpeditionLaunchDestinations();
        if (destinations.isEmpty()) {
            showStandaloneDialog("Expedition Board", "No valid launch destinations are available yet.");
            expeditionBoardOpen = false;
            return;
        }
        clampExpeditionBoardSelection();
        ExpeditionLaunchDestination destination = destinations.get(expeditionBoardSelectionIndex);
        ZoneAccessPolicy.AccessDecision accessDecision = evaluateZoneAccess(destination.zoneId);
        if (!accessDecision.isAllowed()) {
            showStandaloneDialog("Expedition Board", accessDecision.getBlockedReason());
            return;
        }
        armEndgameDeploymentProfile(destination);
        pinExpeditionContract(destination);
        expeditionBoardOpen = false;
        loadZone(destination.zoneId, destination.spawnId, true);
        showStandaloneDialog("Expedition Board", "Deployment confirmed: " + destination.label + ". "
            + (pinnedExpeditionContractText != null ? "Pinned contract: " + pinnedExpeditionContractText : ""));
    }

    private ExpeditionLaunchDestination getSelectedExpeditionDestination() {
        List<ExpeditionLaunchDestination> destinations = getExpeditionLaunchDestinations();
        if (destinations.isEmpty()) {
            return null;
        }
        clampExpeditionBoardSelection();
        return destinations.get(expeditionBoardSelectionIndex);
    }

    private void armEndgameDeploymentProfile(ExpeditionLaunchDestination destination) {
        if (destination == null || !INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            expeditionBoardMode = ENDGAME_MODE_STANDARD;
            activeChallengeModifierId = null;
            activeHardModeSeedIndex = 0;
            return;
        }
        activeHardModeSeedIndex = selectedHardModeSeedIndex;
        if (ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
            activeChallengeModifierId = getSelectedChallengeModifierId();
        } else {
            activeChallengeModifierId = null;
        }
    }

    private String getZoneDisplayName(String zoneId) {
        ZoneDefinition definition = zoneId != null ? zoneDefinitions.get(zoneId) : null;
        if (definition != null && definition.getName() != null && !definition.getName().isEmpty()) {
            return definition.getName();
        }
        return formatZoneName(zoneId);
    }

    private ZoneAccessPolicy.AccessDecision evaluateZoneAccess(String zoneId) {
        return ZoneAccessPolicy.evaluate(
            zoneId,
            gameState.getPlayerLevel(),
            getUnlockedGrade(),
            getForgeCoreLevel(),
            gameState.getWorldStateFlags(),
            gameState.getQuestStates()
        );
    }

    private String getExpeditionBoardTierLabel() {
        if (expeditionBoardReputation >= 10) {
            return "Gold";
        }
        if (expeditionBoardReputation >= 4) {
            return "Silver";
        }
        return "Bronze";
    }

    private String getExpeditionSponsorStatusLine() {
        String silver = worldStateManager.isFlagActive(gameState, "event.board_sponsor_silver_claimed")
            ? "silver package secured"
            : "silver package pending";
        String gold = worldStateManager.isFlagActive(gameState, "event.board_sponsor_gold_claimed")
            ? "gold package secured"
            : "gold package pending";
        return silver + "  |  " + gold;
    }

    private String getDestinationContractTierLabel(String zoneId) {
        ZoneDefinition definition = zoneDefinitions.get(zoneId);
        if ("town".equals(zoneId)) {
            return getExpeditionBoardTierLabel();
        }
        if (INFINITE_DUNGEON_ZONE_ID.equals(zoneId) && ENDGAME_MODE_BOSS_RUSH.equals(expeditionBoardMode)) {
            return "Boss Rush";
        }
        if (INFINITE_DUNGEON_ZONE_ID.equals(zoneId) && ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
            return "Challenge";
        }
        if (INFINITE_DUNGEON_ZONE_ID.equals(zoneId) && warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return expeditionBoardReputation >= 10 ? "Gold Descent" : expeditionBoardReputation >= 4 ? "Silver Descent" : "Bronze Descent";
        }
        if (isActiveWorldBossFront(zoneId)) {
            return expeditionBoardReputation >= 10 ? "Gold Front" : expeditionBoardReputation >= 4 ? "Silver Front" : "Bronze Front";
        }
        if (warPhaseManager.isConvoyEscortRecommended(getCurrentWarPhaseSnapshot(), baseStatesByZoneId.get(zoneId))) {
            return expeditionBoardReputation >= 10 ? "Gold Convoy" : expeditionBoardReputation >= 4 ? "Silver Convoy" : "Bronze Convoy";
        }
        ExpeditionLaunchDestination synthetic = new ExpeditionLaunchDestination(zoneId, null, getZoneDisplayName(zoneId), "", zoneId.equals(currentZoneId), "");
        if (shouldOfferPlayerCreatedQuest(synthetic)) {
            return expeditionBoardReputation >= 10 ? "Gold Charter" : expeditionBoardReputation >= 4 ? "Silver Charter" : "Bronze Charter";
        }
        if (shouldOfferGuildBoardContract(synthetic)) {
            return expeditionBoardReputation >= 10 ? "Gold Guild" : expeditionBoardReputation >= 4 ? "Silver Guild" : "Bronze Guild";
        }
        if (shouldOfferGuildStrikeContract(synthetic)) {
            return expeditionBoardReputation >= 10 ? "Gold Strike" : expeditionBoardReputation >= 4 ? "Silver Strike" : "Bronze Strike";
        }
        if (shouldOfferPublicRecoveryContract(synthetic)) {
            return expeditionBoardReputation >= 10 ? "Gold Recovery" : expeditionBoardReputation >= 4 ? "Silver Recovery" : "Bronze Recovery";
        }
        if (shouldOfferPublicBoardContract(synthetic)) {
            return expeditionBoardReputation >= 10 ? "Gold Public" : expeditionBoardReputation >= 4 ? "Silver Public" : "Bronze Public";
        }
        if (definition != null && definition.getBossId() != null && !definition.getBossId().isEmpty()
            && !gameState.hasDefeatedBoss(definition.getBossId())) {
            return expeditionBoardReputation >= 10 ? "Gold Bounty" : expeditionBoardReputation >= 4 ? "Silver Bounty" : "Bronze Bounty";
        }
        if (getDestinationFragmentId(zoneId) != null) {
            return expeditionBoardReputation >= 10 ? "Gold Recovery" : expeditionBoardReputation >= 4 ? "Silver Recovery" : "Bronze Recovery";
        }
        return definition != null && definition.isExpansiveFrontier()
            ? (expeditionBoardReputation >= 4 ? "Silver Frontier" : "Bronze Frontier")
            : getExpeditionBoardTierLabel();
    }

    private void pinExpeditionContract(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty() || "town".equals(destination.zoneId)) {
            pinnedExpeditionContractTitle = "Ironhaven Command";
            pinnedExpeditionContractText = "Prepare the next sortie, spend banked haul, and choose a stronger frontier target.";
            pinnedExpeditionContractZoneId = "town";
            pinnedExpeditionContractKind = "PREPARE";
            pinnedExpeditionContractTargetId = "town";
            pinnedExpeditionContractCompleted = false;
            return;
        }
        pinnedExpeditionContractTitle = destination.label;
        pinnedExpeditionContractKind = determinePinnedContractKind(destination);
        pinnedExpeditionContractTargetId = determinePinnedContractTargetId(destination, pinnedExpeditionContractKind);
        pinnedExpeditionContractText = buildPinnedExpeditionContract(destination);
        pinnedExpeditionContractZoneId = destination.zoneId;
        if (pinnedExpeditionContractKind != null && pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")) {
            pinnedExpeditionContractTitle = getWorldBossFrontVariantName(destination.zoneId);
        }
        pinnedExpeditionContractCompleted = false;
    }

    private String buildPinnedExpeditionContract(ExpeditionLaunchDestination destination) {
        String objective = getDestinationObjective(destination);
        if (pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("CONVOY_ESCORT")
            || pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")
            || pinnedExpeditionContractKind.startsWith("GUILD_STRIKE")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_")
            || pinnedExpeditionContractKind.startsWith("LARGE_DUNGEON_EXPEDITION"))) {
            return objective;
        }
        String fragmentGoal = getDestinationFragmentGoal(destination.zoneId);
        if (fragmentGoal != null && !fragmentGoal.isEmpty()) {
            return objective + " " + fragmentGoal;
        }
        return objective;
    }

    private String determinePinnedContractKind(ExpeditionLaunchDestination destination) {
        if (destination == null || destination.zoneId == null || destination.zoneId.isEmpty()) {
            return "PREPARE";
        }
        if ("verdant_fields".equals(destination.zoneId) && !worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_claimed")) {
            return "CLAIM_SITE";
        }
        if ("verdant_fields".equals(destination.zoneId) && !worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_banked")) {
            return "BANK_HAUL";
        }
        if (INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId) && ENDGAME_MODE_BOSS_RUSH.equals(expeditionBoardMode)) {
            return "BOSS_RUSH";
        }
        if (INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId) && ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
            return "CHALLENGE_RUN";
        }
        if (shouldOfferLargeDungeonExpedition(destination)) {
            return expeditionBoardReputation >= 10 ? "LARGE_DUNGEON_EXPEDITION_GOLD"
                : expeditionBoardReputation >= 4 ? "LARGE_DUNGEON_EXPEDITION_SILVER"
                : "LARGE_DUNGEON_EXPEDITION";
        }
        if (shouldOfferPlayerCreatedQuest(destination)) {
            PlayerQuestContract contract = getActivePlayerQuestContractForZone(destination.zoneId);
            if (contract != null && "STRIKE".equals(contract.kind)) {
                return expeditionBoardReputation >= 10 ? "PLAYER_CREATED_STRIKE_GOLD"
                    : expeditionBoardReputation >= 4 ? "PLAYER_CREATED_STRIKE_SILVER"
                    : "PLAYER_CREATED_STRIKE";
            }
            if (contract != null && "RECOVERY".equals(contract.kind)) {
                return expeditionBoardReputation >= 10 ? "PLAYER_CREATED_RECOVERY_GOLD"
                    : expeditionBoardReputation >= 4 ? "PLAYER_CREATED_RECOVERY_SILVER"
                    : "PLAYER_CREATED_RECOVERY";
            }
            return expeditionBoardReputation >= 10 ? "PLAYER_CREATED_SUPPLY_GOLD"
                : expeditionBoardReputation >= 4 ? "PLAYER_CREATED_SUPPLY_SILVER"
                : "PLAYER_CREATED_SUPPLY";
        }
        if (isActiveWorldBossFront(destination.zoneId)) {
            return expeditionBoardReputation >= 10 ? "WORLD_BOSS_FRONT_GOLD"
                : expeditionBoardReputation >= 4 ? "WORLD_BOSS_FRONT_SILVER"
                : "WORLD_BOSS_FRONT";
        }
        if (shouldOfferConvoyEscort(destination)) {
            return expeditionBoardReputation >= 10 ? "CONVOY_ESCORT_GOLD"
                : expeditionBoardReputation >= 4 ? "CONVOY_ESCORT_SILVER"
                : "CONVOY_ESCORT";
        }
        if (shouldOfferGuildBoardContract(destination)) {
            return expeditionBoardReputation >= 10 ? "GUILD_BOARD_SUPPLY_GOLD"
                : expeditionBoardReputation >= 4 ? "GUILD_BOARD_SUPPLY_SILVER"
                : "GUILD_BOARD_SUPPLY";
        }
        if (shouldOfferGuildStrikeContract(destination)) {
            return expeditionBoardReputation >= 10 ? "GUILD_STRIKE_GOLD"
                : expeditionBoardReputation >= 4 ? "GUILD_STRIKE_SILVER"
                : "GUILD_STRIKE";
        }
        if (shouldOfferPublicRecoveryContract(destination)) {
            return expeditionBoardReputation >= 10 ? "PUBLIC_BOARD_RECOVERY_GOLD"
                : expeditionBoardReputation >= 4 ? "PUBLIC_BOARD_RECOVERY_SILVER"
                : "PUBLIC_BOARD_RECOVERY";
        }
        if (shouldOfferPublicBoardContract(destination)) {
            return expeditionBoardReputation >= 10 ? "PUBLIC_BOARD_DELIVERY_GOLD"
                : expeditionBoardReputation >= 4 ? "PUBLIC_BOARD_DELIVERY_SILVER"
                : "PUBLIC_BOARD_DELIVERY";
        }
        ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
        if (definition != null && definition.getBossId() != null && !definition.getBossId().isEmpty()
            && !gameState.hasDefeatedBoss(definition.getBossId())) {
            return expeditionBoardReputation >= 10 ? "DEFEAT_BOSS_GOLD" : expeditionBoardReputation >= 4 ? "DEFEAT_BOSS_SILVER" : "DEFEAT_BOSS";
        }
        if (getDestinationFragmentId(destination.zoneId) != null) {
            return expeditionBoardReputation >= 10 ? "RECOVER_FRAGMENT_GOLD" : expeditionBoardReputation >= 4 ? "RECOVER_FRAGMENT_SILVER" : "RECOVER_FRAGMENT";
        }
        return "DEPLOY";
    }

    private String determinePinnedContractTargetId(ExpeditionLaunchDestination destination, String contractKind) {
        if (destination == null) {
            return null;
        }
        if (contractKind != null && contractKind.startsWith("CONVOY_ESCORT")) {
            return destination.zoneId;
        }
        if ("BOSS_RUSH".equals(contractKind)) {
            return "boss_rush_chain";
        }
        if ("CHALLENGE_RUN".equals(contractKind)) {
            return getSelectedChallengeModifierId();
        }
        if (contractKind != null && contractKind.startsWith("LARGE_DUNGEON_EXPEDITION")) {
            return "floor:" + getLargeDungeonExpeditionTargetFloor(contractKind);
        }
        if (contractKind != null && contractKind.startsWith("PLAYER_CREATED_SUPPLY")) {
            return destination.zoneId;
        }
        if (contractKind != null && contractKind.startsWith("PLAYER_CREATED_STRIKE")) {
            PlayerQuestContract contract = getActivePlayerQuestContractForZone(destination.zoneId);
            return contract != null ? contract.targetId : null;
        }
        if (contractKind != null && contractKind.startsWith("PLAYER_CREATED_RECOVERY")) {
            PlayerQuestContract contract = getActivePlayerQuestContractForZone(destination.zoneId);
            return contract != null ? contract.targetId : null;
        }
        if (contractKind != null && contractKind.startsWith("WORLD_BOSS_FRONT")) {
            String frontBossId = getActiveWorldBossFrontBossId(destination.zoneId);
            if (frontBossId != null && !frontBossId.isEmpty()) {
                return frontBossId;
            }
        }
        if (contractKind != null && contractKind.startsWith("GUILD_STRIKE")) {
            ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
            return definition != null ? definition.getBossId() : null;
        }
        if (contractKind != null && contractKind.startsWith("DEFEAT_BOSS")) {
            ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
            return definition != null ? definition.getBossId() : null;
        }
        if (contractKind != null && contractKind.startsWith("RECOVER_FRAGMENT")) {
            return getDestinationFragmentId(destination.zoneId);
        }
        if (contractKind != null && contractKind.startsWith("PUBLIC_BOARD_RECOVERY")) {
            return getDestinationFragmentId(destination.zoneId);
        }
        return destination.zoneId;
    }

    private String getDestinationFragmentId(String zoneId) {
        if ("verdant_fields".equals(zoneId)) {
            return "bot_chassis_schema";
        }
        if ("shadow_caves".equals(zoneId)) {
            return "settlement_plan";
        }
        if ("rusty_quarry".equals(zoneId) || "crystal_depths".equals(zoneId)) {
            return "forge_schema";
        }
        return null;
    }

    private int getLargeDungeonExpeditionTargetFloor(String contractKind) {
        if (contractKind == null || contractKind.isEmpty()) {
            return 3;
        }
        if (contractKind.endsWith("_GOLD")) {
            return 10;
        }
        if (contractKind.endsWith("_SILVER")) {
            return 6;
        }
        return 3;
    }

    private int getPinnedLargeDungeonTargetFloor() {
        if (pinnedExpeditionContractTargetId == null || !pinnedExpeditionContractTargetId.startsWith("floor:")) {
            return 0;
        }
        try {
            return Integer.parseInt(pinnedExpeditionContractTargetId.substring("floor:".length()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void completePlayerQuestContractForZone(String zoneId) {
        PlayerQuestContract contract = getActivePlayerQuestContractForZone(zoneId);
        if (contract != null) {
            contract.active = false;
        }
    }

    private void handlePinnedContractClaim(String siteId) {
        if (!"CLAIM_SITE".equals(pinnedExpeditionContractKind) || pinnedExpeditionContractCompleted) {
            return;
        }
        if (pinnedExpeditionContractZoneId != null && pinnedExpeditionContractZoneId.equals(currentZoneId)) {
            if ("verdant_fields".equals(pinnedExpeditionContractZoneId)
                && !worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_banked")) {
                pinnedExpeditionContractKind = "BANK_HAUL";
                pinnedExpeditionContractTargetId = pinnedExpeditionContractZoneId;
                pinnedExpeditionContractText = "Bank one live haul at your outpost to lock in the frontier extraction loop.";
                pinnedExpeditionContractCompleted = false;
                refreshHud();
                autosave();
                showStandaloneDialog("Expedition Board", "Contract updated: foothold secured. Next step is banking one live haul at the outpost.");
                return;
            }
            completePinnedExpeditionContract("Contract complete. Foothold claimed in " + getZoneDisplayName(currentZoneId) + ".");
        }
    }

    private void handlePinnedContractFragmentRecovery(String fragmentId, int amount) {
        if (pinnedExpeditionContractCompleted || pinnedExpeditionContractKind == null) {
            return;
        }
        if (fragmentId == null || amount <= 0) {
            return;
        }
        if ((pinnedExpeditionContractKind.startsWith("RECOVER_FRAGMENT")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_RECOVERY"))
            && pinnedExpeditionContractTargetId != null
            && pinnedExpeditionContractTargetId.equals(fragmentId)) {
            recordActFourRecoveryVictory(currentZoneId, pinnedExpeditionContractKind);
            resolveRegionalIncident(currentZoneId);
            if (pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY")) {
                adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 5);
                adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 2);
                adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -4);
                syncWarPhaseState(false);
                completePinnedExpeditionContract("Contract complete. Recovery teams secured the fragment cache before hostile crews could strip it.");
                return;
            }
            if (pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_RECOVERY")) {
                completePlayerQuestContractForZone(pinnedExpeditionContractZoneId);
                adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 5);
                adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -3);
                syncWarPhaseState(false);
                completePinnedExpeditionContract("Contract complete. Player-authored recovery charter fulfilled.");
                return;
            }
            completePinnedExpeditionContract("Contract complete. Required fragment recovered for this route.");
        }
    }

    private void handlePinnedContractBossDefeat(String bossId) {
        if (bossId == null || bossId.isEmpty()) {
            return;
        }
        if (!pinnedExpeditionContractCompleted
            && pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("DEFEAT_BOSS")
            || pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")
            || pinnedExpeditionContractKind.startsWith("GUILD_STRIKE")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_STRIKE"))
            && bossId.equals(pinnedExpeditionContractTargetId)) {
            recordActFourBossVictory(currentZoneId, pinnedExpeditionContractKind);
            resolveRegionalIncident(currentZoneId);
            resolveSettlementCrisis(currentZoneId);
            if (pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")) {
                resolveWorldBossFrontByBossId(bossId);
                completePinnedExpeditionContract("Contract complete. World boss front collapsed.");
            } else if (pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_STRIKE")) {
                completePlayerQuestContractForZone(pinnedExpeditionContractZoneId);
                adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 7);
                adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 2);
                adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -6);
                syncWarPhaseState(false);
                completePinnedExpeditionContract("Contract complete. Player-authored strike charter resolved.");
            } else if (pinnedExpeditionContractKind.startsWith("GUILD_STRIKE")) {
                adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 8);
                adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 3);
                adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -8);
                syncWarPhaseState(false);
                completePinnedExpeditionContract("Contract complete. Guild strike order resolved and the route leadership shifted back to allied crews.");
            } else {
                completePinnedExpeditionContract("Contract complete. Zone boss neutralized.");
            }
            return;
        }

        if (isBossInActiveFront(bossId)) {
            resolveWorldBossFrontByBossId(bossId);
            refreshHud();
        }
    }

    private void completePinnedExpeditionContract(String completionMessage) {
        pinnedExpeditionContractCompleted = true;
        if (pinnedExpeditionContractText != null && !pinnedExpeditionContractText.endsWith(" Return to the Expedition Board for the next assignment.")) {
            pinnedExpeditionContractText += " Return to the Expedition Board for the next assignment.";
        }
        String rewardSummary = applyExpeditionContractRewards();
        refreshHud();
        autosave();
        if (completionMessage != null && !completionMessage.isEmpty()) {
            showStandaloneDialog("Expedition Board",
                rewardSummary != null && !rewardSummary.isEmpty()
                    ? completionMessage + " " + rewardSummary
                    : completionMessage);
        }
    }

    private String applyExpeditionContractRewards() {
        int goldReward = 25;
        int shardReward = 0;
        int reputationReward = 1;
        int potionReward = 0;
        int sponsorPremiumGold = getExpeditionBoardTierBonusGold();
        if ("CLAIM_SITE".equals(pinnedExpeditionContractKind)) {
            goldReward = 40;
        } else if ("BANK_HAUL".equals(pinnedExpeditionContractKind)) {
            goldReward = 50;
        } else if ("CONVOY_ESCORT".equals(pinnedExpeditionContractKind)) {
            goldReward = 85;
            shardReward = 1;
            reputationReward = 2;
        } else if ("CONVOY_ESCORT_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 120;
            shardReward = 2;
            reputationReward = 2;
        } else if ("CONVOY_ESCORT_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 165;
            shardReward = 3;
            reputationReward = 3;
        } else if ("WORLD_BOSS_FRONT".equals(pinnedExpeditionContractKind)) {
            goldReward = 145;
            shardReward = 3;
            reputationReward = 3;
        } else if ("WORLD_BOSS_FRONT_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 205;
            shardReward = 4;
            reputationReward = 3;
        } else if ("WORLD_BOSS_FRONT_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 280;
            shardReward = 6;
            reputationReward = 4;
        } else if ("LARGE_DUNGEON_EXPEDITION".equals(pinnedExpeditionContractKind)) {
            goldReward = 140;
            shardReward = 3;
            reputationReward = 2;
        } else if ("LARGE_DUNGEON_EXPEDITION_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 200;
            shardReward = 5;
            reputationReward = 3;
        } else if ("LARGE_DUNGEON_EXPEDITION_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 285;
            shardReward = 7;
            reputationReward = 4;
        } else if ("CHALLENGE_RUN".equals(pinnedExpeditionContractKind)) {
            goldReward = 180;
            shardReward = 6 + activeHardModeSeedIndex;
            reputationReward = 3;
            potionReward = 1;
        } else if ("BOSS_RUSH".equals(pinnedExpeditionContractKind)) {
            goldReward = 260;
            shardReward = 8 + activeHardModeSeedIndex;
            reputationReward = 4;
        } else if ("PLAYER_CREATED_SUPPLY".equals(pinnedExpeditionContractKind)) {
            goldReward = 95;
            reputationReward = 2;
        } else if ("PLAYER_CREATED_SUPPLY_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 135;
            shardReward = 1;
            reputationReward = 3;
        } else if ("PLAYER_CREATED_SUPPLY_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 180;
            shardReward = 2;
            reputationReward = 3;
        } else if ("PLAYER_CREATED_STRIKE".equals(pinnedExpeditionContractKind)) {
            goldReward = 125;
            shardReward = 2;
            reputationReward = 2;
        } else if ("PLAYER_CREATED_STRIKE_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 170;
            shardReward = 3;
            reputationReward = 3;
        } else if ("PLAYER_CREATED_STRIKE_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 225;
            shardReward = 4;
            reputationReward = 4;
        } else if ("PLAYER_CREATED_RECOVERY".equals(pinnedExpeditionContractKind)) {
            goldReward = 100;
            shardReward = 2;
            reputationReward = 2;
        } else if ("PLAYER_CREATED_RECOVERY_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 145;
            shardReward = 3;
            reputationReward = 3;
        } else if ("PLAYER_CREATED_RECOVERY_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 195;
            shardReward = 4;
            reputationReward = 4;
        } else if ("GUILD_BOARD_SUPPLY".equals(pinnedExpeditionContractKind)) {
            goldReward = 70;
            reputationReward = 2;
        } else if ("GUILD_BOARD_SUPPLY_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 105;
            shardReward = 1;
            reputationReward = 2;
        } else if ("GUILD_BOARD_SUPPLY_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 145;
            shardReward = 2;
            reputationReward = 3;
        } else if ("GUILD_STRIKE".equals(pinnedExpeditionContractKind)) {
            goldReward = 115;
            shardReward = 2;
            reputationReward = 2;
        } else if ("GUILD_STRIKE_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 155;
            shardReward = 3;
            reputationReward = 3;
        } else if ("GUILD_STRIKE_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 210;
            shardReward = 4;
            reputationReward = 3;
        } else if ("PUBLIC_BOARD_DELIVERY".equals(pinnedExpeditionContractKind)) {
            goldReward = 60;
            reputationReward = 1;
        } else if ("PUBLIC_BOARD_DELIVERY_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 95;
            shardReward = 1;
            reputationReward = 2;
        } else if ("PUBLIC_BOARD_DELIVERY_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 130;
            shardReward = 2;
            reputationReward = 2;
        } else if ("PUBLIC_BOARD_RECOVERY".equals(pinnedExpeditionContractKind)) {
            goldReward = 85;
            shardReward = 1;
            reputationReward = 2;
        } else if ("PUBLIC_BOARD_RECOVERY_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 120;
            shardReward = 2;
            reputationReward = 2;
        } else if ("PUBLIC_BOARD_RECOVERY_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 160;
            shardReward = 3;
            reputationReward = 3;
        } else if ("RECOVER_FRAGMENT".equals(pinnedExpeditionContractKind)) {
            goldReward = 60;
            shardReward = 1;
        } else if ("RECOVER_FRAGMENT_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 80;
            shardReward = 1;
            reputationReward = 2;
        } else if ("RECOVER_FRAGMENT_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 110;
            shardReward = 2;
            reputationReward = 2;
        } else if ("DEFEAT_BOSS".equals(pinnedExpeditionContractKind)) {
            goldReward = 90;
            shardReward = 2;
            reputationReward = 2;
        } else if ("DEFEAT_BOSS_SILVER".equals(pinnedExpeditionContractKind)) {
            goldReward = 130;
            shardReward = 3;
            reputationReward = 2;
        } else if ("DEFEAT_BOSS_GOLD".equals(pinnedExpeditionContractKind)) {
            goldReward = 180;
            shardReward = 4;
            reputationReward = 3;
        }
        if (pinnedExpeditionContractKind != null && pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")) {
            if ("sky_fortress".equals(pinnedExpeditionContractZoneId)) {
                reputationReward += 1;
                goldReward += 20;
            } else if ("shadow_caves".equals(pinnedExpeditionContractZoneId)) {
                shardReward += 1;
            } else if ("crystal_depths".equals(pinnedExpeditionContractZoneId)) {
                goldReward += 25;
                shardReward += 1;
            } else if ("rusty_quarry".equals(pinnedExpeditionContractZoneId)) {
                goldReward += 25;
            }
        }
        if (pinnedExpeditionContractZoneId != null && activeRegionalIncidentsByZoneId.containsKey(pinnedExpeditionContractZoneId)) {
            goldReward += 20;
            if (pinnedExpeditionContractKind != null
                && (pinnedExpeditionContractKind.startsWith("RECOVER_FRAGMENT")
                || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY")
                || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_RECOVERY")
                || pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT"))) {
                shardReward += 1;
            }
        }
        if (pinnedExpeditionContractZoneId != null && activeSettlementCrisesByZoneId.containsKey(pinnedExpeditionContractZoneId)) {
            reputationReward += 1;
            if (pinnedExpeditionContractKind != null
                && (pinnedExpeditionContractKind.startsWith("CONVOY_ESCORT")
                || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_SUPPLY")
                || pinnedExpeditionContractKind.startsWith("GUILD_BOARD_SUPPLY")
                || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_DELIVERY"))) {
                goldReward += 30;
            }
        }
        int infrastructureBonusGold = getZoneContractSupportBonus(pinnedExpeditionContractZoneId, pinnedExpeditionContractKind);
        goldReward += infrastructureBonusGold;
        if (isCoalitionExchangeProjectActive()
            && pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("CONVOY_ESCORT")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_SUPPLY")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_RECOVERY")
            || pinnedExpeditionContractKind.startsWith("GUILD_BOARD_SUPPLY")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_DELIVERY")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY"))) {
            goldReward += 35;
            if (pinnedExpeditionContractKind.endsWith("_GOLD")) {
                potionReward += 1;
            }
        }
        if (isCommandBastionProjectActive()
            && pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("CONVOY_ESCORT")
            || pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_STRIKE")
            || pinnedExpeditionContractKind.startsWith("GUILD_STRIKE")
            || pinnedExpeditionContractKind.startsWith("LARGE_DUNGEON_EXPEDITION"))) {
            reputationReward += 1;
        }
        if (isArchiveWarCollegeProjectActive()
            && pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("WORLD_BOSS_FRONT")
            || pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_RECOVERY")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_RECOVERY")
            || pinnedExpeditionContractKind.startsWith("RECOVER_FRAGMENT")
            || pinnedExpeditionContractKind.startsWith("LARGE_DUNGEON_EXPEDITION"))) {
            shardReward += 1;
            reputationReward += 1;
        }
        if (isHostileSabotageProjectActive()
            && pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_DELIVERY")
            || pinnedExpeditionContractKind.startsWith("GUILD_BOARD_SUPPLY"))) {
            goldReward = Math.max(0, goldReward - 15);
        }
        goldReward += sponsorPremiumGold;

        if (goldReward > 0) {
            addGold(goldReward);
        }
        if (shardReward > 0) {
            metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + shardReward);
            metaProgressionManager.save(metaProgressionState);
        }
        if (potionReward > 0) {
            addHealingPotions(potionReward);
        }
        expeditionBoardReputation += reputationReward;
        String sponsorRewardSummary = maybeGrantExpeditionBoardSponsorReward();
        autosave();

        List<String> rewards = new ArrayList<>();
        if (goldReward > 0) {
            rewards.add("+" + goldReward + " gold");
        }
        if (infrastructureBonusGold > 0) {
            rewards.add("+" + infrastructureBonusGold + " corridor support");
        }
        if (shardReward > 0) {
            rewards.add("+" + shardReward + " Forge Shard" + (shardReward == 1 ? "" : "s"));
        }
        if (potionReward > 0) {
            rewards.add("+" + potionReward + " field kit" + (potionReward == 1 ? "" : "s"));
        }
        rewards.add("+" + reputationReward + " board reputation");
        String rewardText = rewards.isEmpty() ? "" : "Reward: " + String.join(", ", rewards) + ".";
        if (sponsorRewardSummary != null && !sponsorRewardSummary.isEmpty()) {
            rewardText = rewardText.isEmpty() ? sponsorRewardSummary : rewardText + " " + sponsorRewardSummary;
        }
        return rewardText;
    }

    private int getExpeditionBoardTierBonusGold() {
        if (expeditionBoardReputation >= 10) {
            return 35;
        }
        if (expeditionBoardReputation >= 4) {
            return 15;
        }
        return 0;
    }

    private String maybeGrantExpeditionBoardSponsorReward() {
        List<String> rewards = new ArrayList<>();
        if (expeditionBoardReputation >= 4 && !worldStateManager.isFlagActive(gameState, "event.board_sponsor_silver_claimed")) {
            worldStateManager.setFlag(gameState, "event.board_sponsor_silver_claimed", true);
            addGold(120);
            addHealingPotions(1);
            metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + 1);
            metaProgressionManager.save(metaProgressionState);
            rewards.add("Silver sponsor package: +120 gold, +1 field kit, +1 Forge Shard");
        }
        if (expeditionBoardReputation >= 10 && !worldStateManager.isFlagActive(gameState, "event.board_sponsor_gold_claimed")) {
            worldStateManager.setFlag(gameState, "event.board_sponsor_gold_claimed", true);
            addGold(260);
            addHealingPotions(2);
            metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + 2);
            metaProgressionManager.save(metaProgressionState);
            rewards.add("Gold sponsor package: +260 gold, +2 field kits, +2 Forge Shards");
        }
        return rewards.isEmpty() ? "" : String.join(". ", rewards) + ".";
    }

    private List<String> getExpeditionProjectLines() {
        List<String> lines = new ArrayList<>();
        ExpeditionLaunchDestination destination = getSelectedExpeditionDestination();
        if (pinnedExpeditionContractTitle != null && !pinnedExpeditionContractTitle.isEmpty()
            && pinnedExpeditionContractText != null && !pinnedExpeditionContractText.isEmpty()) {
            lines.add((pinnedExpeditionContractCompleted ? "Completed contract [" : "Pinned contract [")
                + pinnedExpeditionContractTitle + "]: " + pinnedExpeditionContractText);
        }
        lines.add("Primary objective: " + getDestinationObjective(destination));
        if (destination != null && INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            lines.add("Deployment profile: " + formatEndgameModeLabel(expeditionBoardMode)
                + "  |  Hard seed " + getSelectedHardModeSeedLabel() + ".");
            if (ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
                lines.add("Challenge modifier: " + getSelectedChallengeModifierLabel() + ".");
            }
        }
        lines.addAll(getDestinationThreatLines(destination));
        List<String> questLines = getQuestJournalLines();
        int addedQuestLines = 0;
        for (String questLine : questLines) {
            if (questLine == null || questLine.isEmpty()) {
                continue;
            }
            lines.add(questLine);
            addedQuestLines++;
            if (addedQuestLines >= 3) {
                break;
            }
        }
        if (addedQuestLines == 0) {
            lines.add("No active quest contracts. Push the frontier and talk to Ironhaven's crew.");
        }
        int contractBoards = 0;
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild != null && guild.getHallClaimedSiteId() != null && !guild.getHallClaimedSiteId().isEmpty()) {
                contractBoards++;
            }
        }
        if (contractBoards > 0) {
            lines.add("Guild boards online: " + contractBoards + ". Player-authored logistics and strike contracts can now surface on frontier routes.");
        }
        if (getActivePlayerQuestContractCount() > 0) {
            lines.add("Live player charters: " + getActivePlayerQuestContractCount() + ". Posted guild directives are currently deployable from the board.");
        }
        if (warPhaseManager.isWarPhaseUnlocked(gameState) && worldStateManager.isFlagActive(gameState, "settlement.command_hub")) {
            lines.add("Public command board: active. Delivery, recovery, and stabilization jobs are now rotating between controlled outposts.");
        }
        List<String> commandLines = getCommandTabLines();
        for (int i = 0; i < Math.min(4, commandLines.size()); i++) {
            lines.add(commandLines.get(i));
        }
        return lines;
    }

    private String getDestinationObjective(ExpeditionLaunchDestination destination) {
        if (destination == null) {
            return getCurrentObjective();
        }
        if ("town".equals(destination.zoneId)) {
            return "Prepare the next sortie, spend banked haul, and bring Ironhaven's next project online.";
        }
        if (destination != null && INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId) && ENDGAME_MODE_BOSS_RUSH.equals(expeditionBoardMode)) {
            return "Boss rush order authorized. Break three chained endgame bosses in sequence and extract with the crew still standing.";
        }
        if (destination != null && INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId) && ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
            return "Challenge run live. Clear the descent while honoring the " + getSelectedChallengeModifierLabel()
                + " rule and bank the run for extra legacy payout.";
        }
        if (shouldOfferLargeDungeonExpedition(destination)) {
            int targetFloor = getLargeDungeonExpeditionTargetFloor(determinePinnedContractKind(destination));
            return "Large dungeon expedition authorized. Push the Legacy Descent to floor " + targetFloor
                + ", then extract with the run intact to prove the route can support multi-team war deployments.";
        }
        if (shouldOfferPlayerCreatedQuest(destination)) {
            PlayerQuestContract contract = getActivePlayerQuestContractForZone(destination.zoneId);
            if (contract != null && contract.description != null && !contract.description.isEmpty()) {
                return contract.title + ": " + contract.description;
            }
        }
        if (isActiveWorldBossFront(destination.zoneId)) {
            if ("sky_fortress".equals(destination.zoneId)) {
                return "World boss front active in " + destination.label
                    + ". The air rail has turned into a hard-ascent war lane. Break the marked boss and reopen the sky route.";
            }
            if ("shadow_caves".equals(destination.zoneId) || "crystal_depths".equals(destination.zoneId)) {
                return "World boss front active in " + destination.label
                    + ". Reclaim the ruin-dark, break the flagged boss, and drag this dusk-struck corridor back into allied hands.";
            }
            return "World boss front active in " + destination.label
                + ". Hunt the flagged boss, break its hold, and reopen the wider route for allied factions.";
        }
        if (shouldOfferConvoyEscort(destination)) {
            return warPhaseManager.buildConvoyEscortObjective(
                destination.label,
                baseStatesByZoneId.get(destination.zoneId)
            );
        }
        if (shouldOfferGuildBoardContract(destination)) {
            GuildDefinition guild = getPublishingGuildForZone(destination.zoneId);
            String guildName = guild != null ? guild.getDisplayName() : "your guild";
            return guildName + " has posted a supply contract here. Bank a live haul at the hall outpost to keep guild projects funded and defenses staffed.";
        }
        if (shouldOfferGuildStrikeContract(destination)) {
            GuildDefinition guild = getPublishingGuildForZone(destination.zoneId);
            ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
            String guildName = guild != null ? guild.getDisplayName() : null;
            String bossName = definition != null ? getMonsterDisplayName(definition.getBossId()) : null;
            return warPhaseManager.buildGuildStrikeObjective(destination.label, guildName, bossName);
        }
        if (shouldOfferPublicBoardContract(destination)) {
            return "The public contract board needs a live delivery here. Bank a haul at the outpost to keep the route open and the local network supplied.";
        }
        if (shouldOfferPublicRecoveryContract(destination)) {
            String fragmentId = getDestinationFragmentId(destination.zoneId);
            return warPhaseManager.buildPublicRecoveryObjective(
                destination.label,
                fragmentId != null ? getBlueprintFragmentName(fragmentId) : null
            );
        }
        if ("verdant_fields".equals(destination.zoneId)) {
            if (!worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_claimed")) {
                return "Claim your first outpost and build storage so Verdant Fields becomes a real foothold.";
            }
            if (!worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_banked")) {
                return "Bank one live haul at your outpost to lock in the frontier extraction loop.";
            }
            return "Push beyond your footholds and bring back salvage, shards, and blueprint fragments.";
        }
        ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
        if (definition != null && definition.getBossId() != null && !definition.getBossId().isEmpty()
            && !gameState.hasDefeatedBoss(definition.getBossId())) {
            if ("sky_fortress".equals(destination.zoneId)) {
                return "Advance through " + destination.label + " the hard way and break the boss gate at the top of the restored ascent.";
            }
            return "Advance through " + destination.label + " and break the zone boss hold there.";
        }
        return "Use " + destination.label + " as a staging ground for tougher salvage runs and local projects.";
    }

    private List<String> getDestinationThreatLines(ExpeditionLaunchDestination destination) {
        List<String> lines = new ArrayList<>();
        if (destination == null) {
            return lines;
        }
        ZoneDefinition definition = zoneDefinitions.get(destination.zoneId);
        if (definition == null) {
            return lines;
        }
        if ("town".equals(destination.zoneId)) {
            lines.add("Threat: safe hub. Best use is banking, refitting, and choosing the next frontier objective.");
            return lines;
        }

        if (INFINITE_DUNGEON_ZONE_ID.equals(destination.zoneId)) {
            int targetFloor = getLargeDungeonExpeditionTargetFloor(determinePinnedContractKind(destination));
            lines.add("Threat band: scaling dungeon loop  |  Best floor " + gameState.getInfiniteDungeonBestFloor()
                + "  |  Target floor " + targetFloor + ".");
            lines.add("Mode profile: " + formatEndgameModeLabel(expeditionBoardMode)
                + "  |  Hard seed " + getSelectedHardModeSeedLabel() + ".");
            if (ENDGAME_MODE_CHALLENGE.equals(expeditionBoardMode)) {
                lines.add("Challenge pressure: " + getSelectedChallengeModifierLabel() + " is active for this route.");
            } else if (ENDGAME_MODE_BOSS_RUSH.equals(expeditionBoardMode)) {
                lines.add("Boss chain: expect one endgame boss per floor for the first three gates.");
            }
            lines.add("Large expedition: complete a stable descent to floor " + targetFloor
                + " to certify the route for late-war strike crews.");
            lines.add("Run status: " + (gameState.isInfiniteDungeonRunActive()
                ? "active on floor " + getInfiniteDungeonCurrentFloor()
                : "idle, ready for a fresh descent") + ".");
            return lines;
        }

        lines.add("Threat band: " + definition.getRankFloor() + " to " + definition.getRankCeiling()
            + "  |  Player grade " + getUnlockedGrade() + " / Forge Core Lv" + getForgeCoreLevel() + ".");
        if (definition.getBossId() != null && !definition.getBossId().isEmpty()) {
            lines.add(gameState.hasDefeatedBoss(definition.getBossId())
                ? "Boss status: zone boss already defeated."
                : "Boss status: zone boss still active.");
        }
        BaseState baseState = baseStatesByZoneId.get(destination.zoneId);
        int claimedSites = baseState != null ? baseState.getClaimedSiteIds().size() : 0;
        int structures = baseState != null ? baseState.getPlacedStructures().size() : 0;
        lines.add("Foothold status: " + claimedSites + " claimed site" + (claimedSites == 1 ? "" : "s")
            + ", " + structures + " built structure" + (structures == 1 ? "" : "s") + ".");
        if (isActiveWorldBossFront(destination.zoneId)) {
            String bossId = getActiveWorldBossFrontBossId(destination.zoneId);
            lines.add("War front: priority world boss target " + (bossId != null ? getMonsterDisplayName(bossId) : "registered") + " is destabilizing this route.");
            lines.add("Elite front: " + getWorldBossFrontVariantName(destination.zoneId) + " ["
                + getWorldBossFrontBehavior(destination.zoneId) + "]. +35% base max HP, elevated payout, "
                + getWorldBossFrontEffectSummary(destination.zoneId) + ", and " + getWorldBossFrontVariantBonusText(destination.zoneId) + ".");
        }
        if (getRegionalIncidentName(destination.zoneId) != null) {
            lines.add("Incident field: " + getRegionalIncidentName(destination.zoneId) + " - "
                + getRegionalIncidentSummary(destination.zoneId) + ".");
        }
        if (getSettlementCrisisName(destination.zoneId) != null) {
            lines.add("Settlement field: " + getSettlementCrisisName(destination.zoneId) + " - "
                + getSettlementCrisisSummary(destination.zoneId) + ".");
        }
        if (shouldOfferConvoyEscort(destination)) {
            BaseRaidState raidState = baseState != null ? baseState.getRaidState() : null;
            lines.add(raidState != null && raidState.isActive()
                ? "War contract: major convoy escort requested. Banking a live haul here will break the active raid lane."
                : "War contract: escort supplies here before the current pressure spike turns into a major raid.");
        }
        if (shouldOfferPlayerCreatedQuest(destination)) {
            PlayerQuestContract contract = getActivePlayerQuestContractForZone(destination.zoneId);
            lines.add("Player charter: " + (contract != null ? contract.title : "Custom directive") + " is posted on this route.");
        } else if (shouldOfferGuildBoardContract(destination)) {
            GuildDefinition guild = getPublishingGuildForZone(destination.zoneId);
            lines.add("Guild board: " + (guild != null ? guild.getDisplayName() : "Guild") + " is requesting a funded haul to this hall route.");
        } else if (shouldOfferGuildStrikeContract(destination)) {
            GuildDefinition guild = getPublishingGuildForZone(destination.zoneId);
            lines.add("Guild strike order: " + (guild != null ? guild.getDisplayName() : "Guild Coalition")
                + " wants the active boss removed before contractors lose control of the corridor.");
        } else if (shouldOfferPublicBoardContract(destination)) {
            lines.add("Public board: locals are posting delivery requests to keep this route serviceable.");
        } else if (shouldOfferPublicRecoveryContract(destination)) {
            lines.add("Public board: salvage recovery teams are paying for strategic fragments before hostile scavengers strip the zone.");
        }
        String zoneCondition = getZoneWarConditionSummary(destination.zoneId);
        if (zoneCondition != null) {
            lines.add(zoneCondition);
        }
        if (definition.isExpansiveFrontier()) {
            lines.add("Recommended focus: outpost growth, local banking, and fragment recovery from deeper frontier bands.");
        } else if (definition.getBossId() != null && !definition.getBossId().isEmpty() && !gameState.hasDefeatedBoss(definition.getBossId())) {
            lines.add("Recommended focus: clear the route, preserve kits, and arrive at the boss gate with a banked fallback nearby.");
        } else {
            lines.add("Recommended focus: harvest tougher materials and convert the zone into a safer repeatable route.");
        }
        return lines;
    }

    private void drawGuildOverlay() {
        if (!guildMenuOpen) {
            return;
        }

        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        clampGuildMenuSelection();

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.04f, 0.08f, 0.88f);
        shapeRenderer.rect(0f, 0f, w, h);
        shapeRenderer.setColor(0.09f, 0.11f, 0.16f, 0.97f);
        shapeRenderer.rect(180f, 94f, w - 360f, h - 188f);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Guild Charter", 212f, h - 128f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "G or ESC close  |  Up/Down select  |  Enter choose active claim guild  |  P post charter  |  N commission NPC  |  C create guild  |  Del personal mode", 212f, h - 154f);

        String activeMode = activeClaimGuildId != null && !activeClaimGuildId.isEmpty()
            ? getGuildDisplayName(activeClaimGuildId)
            : "Personal Claims";
        font.setColor(new Color(0.88f, 0.94f, 0.98f, 1f));
        font.draw(batch, "Claim Ownership Mode: " + activeMode, 212f, h - 194f);

        float y = h - 238f;
        if (controllableGuilds.isEmpty()) {
            font.setColor(Color.WHITE);
            font.draw(batch, "No guilds available yet. Press C to create one anchored to your player profile.", 212f, y);
            batch.end();
            return;
        }

        for (int i = 0; i < controllableGuilds.size(); i++) {
            GuildDefinition guild = controllableGuilds.get(i);
            boolean selected = i == guildMenuSelectionIndex;
            boolean active = guild.getGuildId().equals(activeClaimGuildId);
            font.setColor(selected ? new Color(0.96f, 0.92f, 0.62f, 1f) : Color.WHITE);
            font.draw(batch, (selected ? "> " : "  ") + guild.getDisplayName() + (active ? " [Active]" : ""), 212f, y);
            y -= 24f;
            font.setColor(Color.LIGHT_GRAY);
            String hall = guild.getHallClaimedSiteId() != null && !guild.getHallClaimedSiteId().isEmpty()
                ? guild.getHallZoneId() + " / " + guild.getHallClaimedSiteId()
                : "No hall claimed yet";
            font.draw(batch, "Founder: " + guild.getFounderPlayerId() + "  |  Hall: " + hall, 232f, y);
            y -= 34f;
            PlayerQuestContract activeContract = guild.getGuildId() != null ? getActivePlayerQuestContractForGuild(guild.getGuildId()) : null;
            if (activeContract != null) {
                font.draw(batch, "Posted charter: " + activeContract.title + " [" + activeContract.kind + "]", 232f, y);
                y -= 30f;
            }
            PlayerCreatedNpc steward = guild.getGuildId() != null ? getPlayerCreatedNpcForGuild(guild.getGuildId()) : null;
            if (steward != null) {
                font.draw(batch, "Commissioned NPC: " + steward.name + " [" + steward.role + "]", 232f, y);
                y -= 30f;
            }
        }
        batch.end();
    }

    private void cycleQuestMenuTab(int direction) {
        int tabCount = QUEST_MENU_TABS.length;
        if (tabCount <= 0) {
            questMenuTabIndex = 0;
            return;
        }
        questMenuTabIndex = (questMenuTabIndex + direction) % tabCount;
        if (questMenuTabIndex < 0) {
            questMenuTabIndex += tabCount;
        }
    }

    private void cycleGuildSelection(int delta) {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        if (controllableGuilds.isEmpty()) {
            guildMenuSelectionIndex = 0;
            return;
        }
        guildMenuSelectionIndex = Math.floorMod(guildMenuSelectionIndex + delta, controllableGuilds.size());
    }

    private void clampGuildMenuSelection() {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        if (controllableGuilds.isEmpty()) {
            guildMenuSelectionIndex = 0;
            return;
        }
        guildMenuSelectionIndex = Math.floorMod(guildMenuSelectionIndex, controllableGuilds.size());
    }

    private List<GuildDefinition> getControllableGuilds() {
        List<GuildDefinition> guilds = new ArrayList<>();
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild != null && guildPermissionsEngine.canPerform(guild, playerName, PermissionAction.CLAIM_LAND)) {
                guilds.add(guild);
            }
        }
        guilds.sort((left, right) -> left.getDisplayName().compareToIgnoreCase(right.getDisplayName()));
        return guilds;
    }

    private void createAndSelectGuild() {
        String guildId = buildNextGuildId();
        String guildName = buildNextGuildName();
        GuildDefinition guild = GuildDefinition.createWithDefaultRanks(guildId, guildName, playerName);
        guildDefinitionsById.put(guildId, guild);
        activeClaimGuildId = guildId;
        clampGuildMenuSelectionToGuild(guildId);
        autosave();
        showStandaloneDialog("Guild Charter", guildName + " founded. New claims can now be assigned to this guild.");
    }

    private void postPlayerQuestForSelectedGuild() {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        if (controllableGuilds.isEmpty()) {
            showStandaloneDialog("Guild Charter", "Create or control a guild before posting a player charter.");
            return;
        }
        clampGuildMenuSelection();
        GuildDefinition guild = controllableGuilds.get(guildMenuSelectionIndex);
        if (guild.getHallZoneId() == null || guild.getHallZoneId().isEmpty()
            || guild.getHallClaimedSiteId() == null || guild.getHallClaimedSiteId().isEmpty()) {
            showStandaloneDialog("Guild Charter", "That guild needs a claimed hall before it can post player-authored contracts.");
            return;
        }
        PlayerQuestContract existing = getActivePlayerQuestContractForGuild(guild.getGuildId());
        if (existing != null) {
            existing.active = false;
        }

        PlayerQuestContract contract = buildNextPlayerQuestContract(guild);
        if (contract == null) {
            showStandaloneDialog("Guild Charter", "No valid player-authored charter template is available for that guild yet.");
            return;
        }
        playerQuestContracts.add(contract);
        autosave();
        showStandaloneDialog("Guild Charter", "Posted charter: " + contract.title + ".");
    }

    private void createPlayerNpcForSelectedGuild() {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        if (controllableGuilds.isEmpty()) {
            showStandaloneDialog("Guild Charter", "Create or control a guild before commissioning a guild NPC.");
            return;
        }
        clampGuildMenuSelection();
        GuildDefinition guild = controllableGuilds.get(guildMenuSelectionIndex);
        if (guild.getHallZoneId() == null || guild.getHallZoneId().isEmpty()
            || guild.getHallClaimedSiteId() == null || guild.getHallClaimedSiteId().isEmpty()) {
            showStandaloneDialog("Guild Charter", "That guild needs a claimed hall before it can commission an NPC.");
            return;
        }
        if (getPlayerCreatedNpcForGuild(guild.getGuildId()) != null) {
            showStandaloneDialog("Guild Charter", "That guild already has a commissioned NPC stationed in Ironhaven.");
            return;
        }
        PlayerCreatedNpc createdNpc = buildPlayerCreatedNpc(guild);
        playerCreatedNpcs.add(createdNpc);
        autosave();
        showStandaloneDialog("Guild Charter", createdNpc.name + " commissioned as " + createdNpc.role + " for " + guild.getDisplayName() + ".");
    }

    private PlayerCreatedNpc buildPlayerCreatedNpc(GuildDefinition guild) {
        int ordinal = playerCreatedNpcs.size() + 1;
        String[] roles = {"Quartermaster", "Warden", "Lorekeeper"};
        String[] names = {"Morrow", "Tamsin", "Vale", "Orin", "Sera", "Ilex"};
        String role = roles[Math.floorMod(ordinal - 1, roles.length)];
        String name = names[Math.floorMod(ordinal - 1, names.length)] + " " + guild.getDisplayName().charAt(0) + ".";
        PlayerCreatedNpc npc = new PlayerCreatedNpc();
        npc.npcId = guild.getGuildId() + "_npc_" + ordinal;
        npc.guildId = guild.getGuildId();
        npc.zoneId = "town";
        npc.role = role;
        npc.name = name;
        npc.dialog = buildPlayerCreatedNpcDialog(guild, role);
        return npc;
    }

    private String buildPlayerCreatedNpcDialog(GuildDefinition guild, String role) {
        String guildName = guild != null ? guild.getDisplayName() : "your guild";
        if ("Quartermaster".equals(role)) {
            return "I've turned " + guildName + "'s hall into a live supply spine. Give me a route and I'll keep the frontier fed.";
        }
        if ("Warden".equals(role)) {
            return "Claims mean nothing if nobody can hold them. " + guildName + " now has a warden's eye on every live border.";
        }
        return "I catalog what " + guildName + " builds, loses, and learns. Empire survives by remembering.";
    }

    private PlayerCreatedNpc getPlayerCreatedNpcForGuild(String guildId) {
        if (guildId == null || guildId.isEmpty()) {
            return null;
        }
        for (PlayerCreatedNpc npc : playerCreatedNpcs) {
            if (npc != null && guildId.equals(npc.guildId)) {
                return npc;
            }
        }
        return null;
    }

    private PlayerQuestContract buildNextPlayerQuestContract(GuildDefinition guild) {
        if (guild == null || guild.getHallZoneId() == null || guild.getHallZoneId().isEmpty()) {
            return null;
        }
        ZoneDefinition zoneDefinition = zoneDefinitions.get(guild.getHallZoneId());
        String contractId = guild.getGuildId() + "_charter_" + (playerQuestContracts.size() + 1);
        PlayerQuestContract contract = new PlayerQuestContract();
        contract.contractId = contractId;
        contract.guildId = guild.getGuildId();
        contract.zoneId = guild.getHallZoneId();
        contract.authorPlayerId = playerName;
        contract.active = true;

        String nextKind = "SUPPLY";
        String previousKind = getLatestPlayerQuestContractKindForGuild(guild.getGuildId());
        if (previousKind != null) {
            nextKind = "SUPPLY".equals(previousKind) ? "STRIKE" : "STRIKE".equals(previousKind) ? "RECOVERY" : "SUPPLY";
        }
        if ("STRIKE".equals(nextKind) && (zoneDefinition == null || zoneDefinition.getBossId() == null || zoneDefinition.getBossId().isEmpty())) {
            nextKind = getDestinationFragmentId(guild.getHallZoneId()) != null ? "RECOVERY" : "SUPPLY";
        }
        if ("RECOVERY".equals(nextKind) && getDestinationFragmentId(guild.getHallZoneId()) == null) {
            nextKind = zoneDefinition != null && zoneDefinition.getBossId() != null && !zoneDefinition.getBossId().isEmpty() ? "STRIKE" : "SUPPLY";
        }

        contract.kind = nextKind;
        if ("STRIKE".equals(nextKind)) {
            contract.targetId = zoneDefinition != null ? zoneDefinition.getBossId() : guild.getHallZoneId();
            contract.title = buildPlayerQuestTitle(guild, guild.getHallZoneId(), "STRIKE");
            contract.description = buildPlayerQuestDescription(guild, guild.getHallZoneId(), "STRIKE", contract.targetId);
        } else if ("RECOVERY".equals(nextKind)) {
            contract.targetId = getDestinationFragmentId(guild.getHallZoneId());
            contract.title = buildPlayerQuestTitle(guild, guild.getHallZoneId(), "RECOVERY");
            contract.description = buildPlayerQuestDescription(guild, guild.getHallZoneId(), "RECOVERY", contract.targetId);
        } else {
            contract.targetId = guild.getHallZoneId();
            contract.title = buildPlayerQuestTitle(guild, guild.getHallZoneId(), "SUPPLY");
            contract.description = buildPlayerQuestDescription(guild, guild.getHallZoneId(), "SUPPLY", contract.targetId);
        }
        return contract;
    }

    private String buildPlayerQuestTitle(GuildDefinition guild, String zoneId, String kind) {
        String guildName = guild != null ? guild.getDisplayName() : "Guild";
        if ("STRIKE".equals(kind)) {
            if ("sky_fortress".equals(zoneId)) {
                return guildName + " Hell Climb Charter";
            }
            if ("rusty_quarry".equals(zoneId)) {
                return guildName + " Attrition Break Charter";
            }
            return guildName + " Strike Charter";
        }
        if ("RECOVERY".equals(kind)) {
            if ("shadow_caves".equals(zoneId)) {
                return guildName + " Ghost Archive Charter";
            }
            if ("crystal_depths".equals(zoneId)) {
                return guildName + " Dusk Memory Charter";
            }
            return guildName + " Recovery Charter";
        }
        if ("verdant_fields".equals(zoneId)) {
            return guildName + " Frontier Lifeline Charter";
        }
        if ("frozen_vale".equals(zoneId)) {
            return guildName + " Last Light Supply Charter";
        }
        return guildName + " Supply Charter";
    }

    private String buildPlayerQuestDescription(GuildDefinition guild, String zoneId, String kind, String targetId) {
        String zoneName = getZoneDisplayName(zoneId);
        String guildName = guild != null ? guild.getDisplayName() : "Guild";
        if ("STRIKE".equals(kind)) {
            String bossName = getMonsterDisplayName(targetId);
            if ("sky_fortress".equals(zoneId)) {
                return "Push the fortress the hard way, break " + bossName
                    + ", and prove " + guildName + " can survive a no-shortcuts ascent.";
            }
            return "Hunt " + bossName + " in " + zoneName
                + " and prove " + guildName + " can hold that corridor under hell-mode pressure.";
        }
        if ("RECOVERY".equals(kind)) {
            String fragmentName = getBlueprintFragmentName(targetId);
            if ("shadow_caves".equals(zoneId) || "crystal_depths".equals(zoneId)) {
                return "Recover " + fragmentName + " from " + zoneName
                    + " before the dusk beyond the frontier swallows what the old world still remembers.";
            }
            return "Recover " + fragmentName + " from " + zoneName
                + " for " + guildName + " research and logistics.";
        }
        if ("verdant_fields".equals(zoneId)) {
            return "Bank a live haul at the guild hall outpost in " + zoneName
                + " and keep the first true lifeline corridor open for future crews.";
        }
        return "Bank a live haul at the guild hall outpost in " + zoneName
            + " to keep the posted corridor funded and defended.";
    }

    private void selectHighlightedGuildForClaims() {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        if (controllableGuilds.isEmpty()) {
            showStandaloneDialog("Guild Charter", "Create a guild first.");
            return;
        }
        clampGuildMenuSelection();
        GuildDefinition guild = controllableGuilds.get(guildMenuSelectionIndex);
        activeClaimGuildId = guild.getGuildId();
        autosave();
        showStandaloneDialog("Guild Charter", guild.getDisplayName() + " is now the active owner for future claims.");
    }

    private void clearActiveClaimGuildSelection() {
        activeClaimGuildId = null;
        autosave();
        showStandaloneDialog("Guild Charter", "Future claims will be personal until you select a guild again.");
    }

    private void clampGuildMenuSelectionToGuild(String guildId) {
        List<GuildDefinition> controllableGuilds = getControllableGuilds();
        for (int i = 0; i < controllableGuilds.size(); i++) {
            if (controllableGuilds.get(i).getGuildId().equals(guildId)) {
                guildMenuSelectionIndex = i;
                return;
            }
        }
        clampGuildMenuSelection();
    }

    private String buildNextGuildId() {
        int nextIndex = guildDefinitionsById.size() + 1;
        String base = "guild_" + playerName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        String candidate = base + "_" + nextIndex;
        while (guildDefinitionsById.containsKey(candidate)) {
            nextIndex++;
            candidate = base + "_" + nextIndex;
        }
        return candidate;
    }

    private String buildNextGuildName() {
        String baseName = playerName + "'s Guild";
        if (!guildDisplayNameExists(baseName)) {
            return baseName;
        }
        int nextIndex = 2;
        String candidate = baseName + " " + nextIndex;
        while (guildDisplayNameExists(candidate)) {
            nextIndex++;
            candidate = baseName + " " + nextIndex;
        }
        return candidate;
    }

    private boolean guildDisplayNameExists(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return false;
        }
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild != null && displayName.equalsIgnoreCase(guild.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    private String getGuildDisplayName(String guildId) {
        GuildDefinition guild = guildId != null ? guildDefinitionsById.get(guildId) : null;
        return guild != null && guild.getDisplayName() != null && !guild.getDisplayName().isEmpty() ? guild.getDisplayName() : "Unknown Guild";
    }

    private void drawBuildOverlay() {
        if (!buildModeOpen) {
            return;
        }

        StructureDefinition selected = getSelectedBuildStructure();
        BasePlacementResult placement = selected != null ? getCurrentBuildPlacementResult(selected) : null;
        String claimedSiteId = selected != null ? findBuildClaimSiteId(selected) : null;
        String materialLine = selected != null ? buildStructureCostLine(selected) : "No structure selected.";

        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.03f, 0.06f, 0.08f, 0.84f);
        shapeRenderer.rect(24f, h - 194f, w - 48f, 170f);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Build Mode", 42f, h - 42f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "B/ESC close  LEFT/RIGHT cycle  E place  R remove  T repair  F assign reserve bot", 42f, h - 68f);
        font.setColor(new Color(0.88f, 0.94f, 0.98f, 1f));
        font.draw(batch, selected != null ? selected.getDisplayName() : "No structure", 42f, h - 100f);
        font.setColor(Color.WHITE);
        font.draw(batch, materialLine, 42f, h - 124f);
        String statusLine;
        BaseRaidState raidState = getCurrentBaseState() != null ? getCurrentBaseState().getRaidState() : null;
        if (selected == null) {
            statusLine = "No structure definitions loaded.";
        } else if (claimedSiteId == null || claimedSiteId.isEmpty()) {
            statusLine = "Stand inside a claimed outpost site to place structures.";
        } else if (placement != null && placement.isAllowed()) {
            statusLine = "Placement valid at " + claimedSiteId + ".";
        } else {
            statusLine = placement != null && placement.getMessage() != null ? placement.getMessage() : "Placement blocked.";
        }
        font.draw(batch, statusLine, 42f, h - 148f);
        if (raidState != null) {
            font.draw(batch,
                raidState.isActive()
                    ? "Raid active: wave " + Math.max(1, raidState.getWaveIndex()) + " threat " + String.format(Locale.US, "%.2f", raidState.getThreatLevel())
                    : "Raid pressure " + String.format(Locale.US, "%.2f", raidState.getThreatLevel()) + "  cooldown " + String.format(Locale.US, "%.0fs", raidState.getCooldownSeconds()),
                42f, h - 168f);
        }
        if (getCurrentBaseState() != null) {
            font.draw(batch, getStructureWarSupportLine(getCurrentBaseState().getZoneId()), 420f, h - 168f);
        }
        batch.end();
    }

    private List<String> wrapTextLines(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() <= maxChars) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current.setLength(0);
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private List<String> paginateDialog(String text, int maxChars, int linesPerPage) {
        List<String> wrappedLines = wrapTextLines(text, maxChars);
        List<String> pages = new ArrayList<>();
        if (wrappedLines.isEmpty()) {
            pages.add("");
            return pages;
        }
        StringBuilder currentPage = new StringBuilder();
        int lineCount = 0;
        for (String line : wrappedLines) {
            if (currentPage.length() > 0) {
                currentPage.append('\n');
            }
            currentPage.append(line);
            lineCount++;
            if (lineCount >= linesPerPage) {
                pages.add(currentPage.toString());
                currentPage.setLength(0);
                lineCount = 0;
            }
        }
        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }
        return pages;
    }

    private void showStandaloneDialog(String speaker, String text) {
        activeDialogueSequence.clear();
        activeDialogueSequenceIndex = 0;
        activeDialogueSequence.add(new DialogueSystem.DialoguePage(speaker, text));
        showCurrentDialogueSequencePage();
    }

    private void showDialogueSequence(List<DialogueSystem.DialoguePage> pages, String fallbackSpeaker, String fallbackText) {
        activeDialogueSequence.clear();
        activeDialogueSequenceIndex = 0;
        if (pages != null) {
            for (DialogueSystem.DialoguePage page : pages) {
                if (page == null || page.text == null || page.text.isEmpty()) {
                    continue;
                }
                activeDialogueSequence.add(page);
            }
        }
        if (activeDialogueSequence.isEmpty()) {
            activeDialogueSequence.add(new DialogueSystem.DialoguePage(fallbackSpeaker, fallbackText));
        }
        showCurrentDialogueSequencePage();
    }

    private void showCurrentDialogueSequencePage() {
        if (activeDialogueSequence.isEmpty()) {
            clearActiveDialog();
            return;
        }
        DialogueSystem.DialoguePage page = activeDialogueSequence.get(Math.min(activeDialogueSequenceIndex, activeDialogueSequence.size() - 1));
        activeSpeaker = page.speaker;
        activeDialog = page.text;
        dialogPageIndex = 0;
        dialogPageTrackingText = activeDialog;
        dialogPageTrackingSpeaker = activeSpeaker;
    }

    private boolean hasActiveDialog() {
        return activeSpeaker != null && activeDialog != null && !activeDialog.isEmpty();
    }

    private void resetDialogPageIfNeeded() {
        if (!hasActiveDialog()) {
            dialogPageIndex = 0;
            dialogPageTrackingText = null;
            dialogPageTrackingSpeaker = null;
            return;
        }
        if (!activeDialog.equals(dialogPageTrackingText) || !activeSpeaker.equals(dialogPageTrackingSpeaker)) {
            dialogPageIndex = 0;
            dialogPageTrackingText = activeDialog;
            dialogPageTrackingSpeaker = activeSpeaker;
        }
    }

    private boolean advanceDialogPage() {
        if (!hasActiveDialog()) {
            return false;
        }
        resetDialogPageIfNeeded();
        List<String> pages = paginateDialog(activeDialog, 82, 3);
        if (dialogPageIndex + 1 < pages.size()) {
            dialogPageIndex++;
            return true;
        }
        return false;
    }

    private boolean advanceDialogueSequence() {
        if (activeDialogueSequenceIndex + 1 < activeDialogueSequence.size()) {
            activeDialogueSequenceIndex++;
            showCurrentDialogueSequencePage();
            return true;
        }
        return false;
    }

    private void clearActiveDialog() {
        activeSpeaker = null;
        activeDialog = null;
        activeDialogueSequence.clear();
        activeDialogueSequenceIndex = 0;
        dialogPageIndex = 0;
        dialogPageTrackingText = null;
        dialogPageTrackingSpeaker = null;
    }

    private void transitionToGameOver() {
        clearUnbankedExpeditionHaul();
        handleInfiniteDungeonRunEnd(false);
        metaProgressionState.setDeathCount(metaProgressionState.getDeathCount() + 1);
        DeathDraftResult deathDraft = cyberneticEnhancementEngine.buildDeathDraft(metaProgressionState, buildRunOutcomeSummary());
        metaProgressionManager.save(metaProgressionState);
        GameOverScreen gameOverScreen = new GameOverScreen(
            game,
            screenManager,
            metaProgressionState,
            deathDraft,
            metaProgressionManager,
            cyberneticEnhancementEngine
        );
        gameOverScreen.setGameStats((int) totalGold, totalEnemiesKilled, survivalTime);
        screenManager.replace(gameOverScreen);
    }

    private void clearUnbankedExpeditionHaul() {
        gameState.setUnbankedGold(0L);
        gameState.clearUnbankedForgeComponents();
        gameState.clearUnbankedShards();
        gameState.clearUnbankedBlueprintFragments();
    }

    private void openExpeditionBoard() {
        if (!canAccessExpeditionBoard()) {
            showStandaloneDialog(
                "Expedition Board",
                "You need to be in Ironhaven or at one of your claimed frontier outposts to review the next launch plan."
            );
            return;
        }
        expeditionBoardOpen = true;
        expeditionBoardSelectionIndex = 0;
        guildMenuOpen = false;
        questMenuOpen = false;
        buildModeOpen = false;
        clearActiveDialog();
    }

    private boolean canAccessExpeditionBoard() {
        if (isHubTownZone()) {
            return true;
        }
        BaseState baseState = getCurrentBaseState();
        return baseState != null && !baseState.getClaimedSiteIds().isEmpty();
    }

    private void awardForgeShards(int amount, String speaker, String reason) {
        if (amount <= 0) {
            return;
        }
        metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + amount);
        metaProgressionManager.save(metaProgressionState);
        if (speaker != null && !speaker.isEmpty() && reason != null && !reason.isEmpty()) {
            showStandaloneDialog(speaker, reason + " Forge Shards +" + amount + ".");
        }
    }

    private void handleInfiniteDungeonRunEnd(boolean sealedExit) {
        if (!gameState.isInfiniteDungeonRunActive()) {
            return;
        }
        int floorReached = Math.max(0, getInfiniteDungeonCurrentFloor() - 1);
        handleLargeDungeonExpeditionProgress(floorReached, sealedExit);
        int reward = sealedExit ? forgeLegacyEngine.getSealRunBonus() : forgeLegacyEngine.getDeathShardBonus(floorReached);
        if (reward > 0) {
            awardForgeShards(reward, sealedExit ? "Legacy Vault" : "Bolt Simulation",
                sealedExit
                    ? "Run sealed at floor " + floorReached + ". Legacy data archived."
                    : "Run collapse recorded at floor " + floorReached + ".");
        }
        if (gameState.isShardRunActive()) {
            shardRunManager.endShardRun(sealedExit);
        }
        gameState.setInfiniteDungeonRunActive(false);
        gameState.setInfiniteDungeonCurrentFloor(INFINITE_DUNGEON_START_FLOOR);
        activeChallengeModifierId = null;
        activeHardModeSeedIndex = 0;
        expeditionBoardMode = ENDGAME_MODE_STANDARD;
        syncAct5EndgameState(sealedExit);
    }

    private void syncAct5EndgameState(boolean showUnlockDialog) {
        if (gameState.getInfiniteDungeonBestFloor() > 0) {
            worldStateManager.setFlag(gameState, "meta.shard_run_unlocked", true);
        }
        if (gameState.getInfiniteDungeonBestFloor() >= 15) {
            worldStateManager.setFlag(gameState, "meta.floor15_cleared", true);
        }
        if (gameState.getInfiniteDungeonBestFloor() >= 30) {
            worldStateManager.setFlag(gameState, "meta.floor30_cleared", true);
        }
        if (getGuildSettlementCount() >= 2) {
            worldStateManager.setFlag(gameState, "frontier.guild_settlements_founded", true);
        }
        if (forgeLegacyEngine.areChallengeRunsUnlocked(gameState.getInfiniteDungeonBestFloor())) {
            worldStateManager.setFlag(gameState, "frontier.challenge_doctrine_stable", true);
        }
        if (playerCreatedNpcs.size() >= 1 && gameState.getInfiniteDungeonBestFloor() >= 15) {
            worldStateManager.setFlag(gameState, "frontier.legacy_command_online", true);
        }
        unlockLegendaryRobots(showUnlockDialog);
    }

    private void unlockLegendaryRobots(boolean showUnlockDialog) {
        int unlockedCount = forgeLegacyEngine.getLegendaryRobotsUnlocked(gameState.getInfiniteDungeonBestFloor());
        for (int i = 0; i < unlockedCount && i < LEGENDARY_ROBOT_UNLOCK_IDS.length; i++) {
            String robotId = LEGENDARY_ROBOT_UNLOCK_IDS[i];
            if (metaProgressionState.getUnlockedLegendaryRobotIds().contains(robotId)) {
                continue;
            }
            metaProgressionState.getUnlockedLegendaryRobotIds().add(robotId);
            applyRecruitment(LEGENDARY_ROBOT_EVENTS[i]);
            metaProgressionState.setWorldEventCompletions(metaProgressionState.getWorldEventCompletions() + 1);
            if (showUnlockDialog) {
                showStandaloneDialog("Legacy Vault", getRobotDisplayName(robotId)
                    + " has been archived as a legendary frame and added to your roster.");
            }
        }
        metaProgressionManager.save(metaProgressionState);
    }

    private void migrateLegacyLegendaryUnlocks() {
        boolean changed = false;
        for (int i = 0; i < Math.min(LEGENDARY_ROBOT_UNLOCK_IDS.length, LEGACY_LEGENDARY_ROBOT_IDS.length); i++) {
            String currentId = LEGENDARY_ROBOT_UNLOCK_IDS[i];
            String legacyId = LEGACY_LEGENDARY_ROBOT_IDS[i];
            if (metaProgressionState.getUnlockedLegendaryRobotIds().contains(legacyId)) {
                metaProgressionState.getUnlockedLegendaryRobotIds().remove(legacyId);
                if (!metaProgressionState.getUnlockedLegendaryRobotIds().contains(currentId)) {
                    metaProgressionState.getUnlockedLegendaryRobotIds().add(currentId);
                }
                changed = true;
            }
        }
        if (changed) {
            metaProgressionManager.save(metaProgressionState);
        }
    }

    private int getTotalClaimedTerritories() {
        int total = 0;
        for (BaseState baseState : baseStatesByZoneId.values()) {
            if (baseState != null) {
                total += baseState.getClaimedSiteIds().size();
            }
        }
        return total;
    }

    private int getGuildSettlementCount() {
        int total = 0;
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild != null && guild.getHallClaimedSiteId() != null && !guild.getHallClaimedSiteId().isEmpty()) {
                total++;
            }
        }
        return total;
    }

    private int getDefeatedEndgameBossCount() {
        int count = 0;
        for (String bossId : ENDGAME_BOSS_IDS) {
            if (gameState.hasDefeatedBoss(bossId)) {
                count++;
            }
        }
        return count;
    }

    private String getActiveWorldEventName() {
        syncWarPhaseState(false);
        int territories = getTotalClaimedTerritories();
        int guildSettlements = getGuildSettlementCount();
        if (gameState.getInfiniteDungeonBestFloor() >= 30) {
            return "Rift Storm";
        }
        if (!activeSettlementCrisesByZoneId.isEmpty()) {
            return "Settlement Crisis";
        }
        if (!activeRegionalIncidentsByZoneId.isEmpty()) {
            return "Regional Incident";
        }
        if (!activeWorldBossFrontsByZoneId.isEmpty()) {
            return "World Boss Front";
        }
        int hostileInfluence = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        int commandInfluence = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        if (guildSettlements >= 1 && territories >= 4) {
            return "Guild Summit";
        }
        if (hostileInfluence >= 60) {
            return "Siege Warning";
        }
        if (commandInfluence >= 60) {
            return "Command Offensive";
        }
        if (territories >= 2) {
            return "Salvage Surge";
        }
        return "Frontier Recon";
    }

    private void ensureWarPhaseStateInitialized() {
        if (factionInfluenceById.isEmpty()) {
            factionInfluenceById.putAll(warPhaseManager.createDefaultFactionInfluence());
        } else {
            for (Map.Entry<String, Integer> entry : warPhaseManager.createDefaultFactionInfluence().entrySet()) {
                factionInfluenceById.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        syncWarPhaseState(false);
    }

    private void syncWarPhaseState(boolean autosaveAfterSync) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)) {
            activeWorldBossFrontsByZoneId.clear();
            activeRegionalIncidentsByZoneId.clear();
            activeSettlementCrisesByZoneId.clear();
            return;
        }
        ensureFactionInfluenceBounds();
        pruneResolvedWorldBossFronts();
        populateWorldBossFrontsIfNeeded();
        pruneResolvedRegionalIncidents();
        populateRegionalIncidentsIfNeeded();
        pruneResolvedSettlementCrises();
        populateSettlementCrisesIfNeeded();
        syncActFourWarCampaignState();
        if (autosaveAfterSync) {
            autosave();
        }
    }

    private void pruneResolvedRegionalIncidents() {
        List<String> zonesToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeRegionalIncidentsByZoneId.entrySet()) {
            String zoneId = entry.getKey();
            if (zoneId == null || zoneId.isEmpty() || !zoneDefinitions.containsKey(zoneId)) {
                zonesToRemove.add(zoneId);
                continue;
            }
            if ("town".equals(zoneId) || INFINITE_DUNGEON_ZONE_ID.equals(zoneId)) {
                zonesToRemove.add(zoneId);
            }
        }
        for (String zoneId : zonesToRemove) {
            activeRegionalIncidentsByZoneId.remove(zoneId);
        }
    }

    private void populateRegionalIncidentsIfNeeded() {
        int desiredIncidents = expeditionBoardReputation >= 10 ? 3 : expeditionBoardReputation >= 4 ? 2 : 1;
        for (ZoneDefinition definition : zoneDefinitions.values()) {
            if (definition == null || definition.getId() == null || definition.getId().isEmpty()) {
                continue;
            }
            String zoneId = definition.getId();
            if ("town".equals(zoneId) || INFINITE_DUNGEON_ZONE_ID.equals(zoneId)) {
                continue;
            }
            if (activeRegionalIncidentsByZoneId.size() >= desiredIncidents) {
                return;
            }
            if (activeRegionalIncidentsByZoneId.containsKey(zoneId)) {
                continue;
            }
            ZoneAccessPolicy.AccessDecision accessDecision = evaluateZoneAccess(zoneId);
            if (!accessDecision.isAllowed()) {
                continue;
            }
            if (!shouldZoneReceiveRegionalIncident(zoneId)) {
                continue;
            }
            activeRegionalIncidentsByZoneId.put(zoneId, determineRegionalIncidentType(zoneId));
        }
    }

    private boolean shouldZoneReceiveRegionalIncident(String zoneId) {
        return isActiveWorldBossFront(zoneId)
            || "shadow_caves".equals(zoneId)
            || "crystal_depths".equals(zoneId)
            || "rusty_quarry".equals(zoneId)
            || "sky_fortress".equals(zoneId);
    }

    private String determineRegionalIncidentType(String zoneId) {
        if ("shadow_caves".equals(zoneId)) {
            return "ghost_signal";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "shard_storm";
        }
        if ("sky_fortress".equals(zoneId)) {
            return "air_raid_siren";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "scrap_stampede";
        }
        return "frontier_blackout";
    }

    private void pruneResolvedSettlementCrises() {
        List<String> zonesToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeSettlementCrisesByZoneId.entrySet()) {
            String zoneId = entry.getKey();
            BaseState baseState = zoneId != null ? baseStatesByZoneId.get(zoneId) : null;
            if (zoneId == null || zoneId.isEmpty() || baseState == null || baseState.getClaimedSiteIds().isEmpty()) {
                zonesToRemove.add(zoneId);
            }
        }
        for (String zoneId : zonesToRemove) {
            activeSettlementCrisesByZoneId.remove(zoneId);
        }
    }

    private void populateSettlementCrisesIfNeeded() {
        int desiredCrises = expeditionBoardReputation >= 10 ? 2 : 1;
        List<Map.Entry<String, BaseState>> entries = new ArrayList<>(baseStatesByZoneId.entrySet());
        entries.sort((left, right) -> Integer.compare(
            getZoneInfrastructureSupportScore(left.getKey()),
            getZoneInfrastructureSupportScore(right.getKey())
        ));
        for (Map.Entry<String, BaseState> entry : entries) {
            String zoneId = entry.getKey();
            BaseState baseState = entry.getValue();
            if (activeSettlementCrisesByZoneId.size() >= desiredCrises) {
                return;
            }
            if (zoneId == null || zoneId.isEmpty() || baseState == null || baseState.getClaimedSiteIds().isEmpty()) {
                continue;
            }
            if (activeSettlementCrisesByZoneId.containsKey(zoneId)) {
                continue;
            }
            if (getZoneInfrastructureSupportScore(zoneId) >= 6 && !isUnderWarPressure(zoneId)) {
                continue;
            }
            activeSettlementCrisesByZoneId.put(zoneId, determineSettlementCrisisType(zoneId));
        }
    }

    private boolean isUnderWarPressure(String zoneId) {
        BaseState baseState = zoneId != null ? baseStatesByZoneId.get(zoneId) : null;
        BaseRaidState raidState = baseState != null ? baseState.getRaidState() : null;
        return isActiveWorldBossFront(zoneId)
            || raidState != null && (raidState.isActive() || raidState.getThreatLevel() >= 0.45f)
            || factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) >= 60;
    }

    private String determineSettlementCrisisType(String zoneId) {
        if (isActiveWorldBossFront(zoneId)) {
            return "frontline_panic";
        }
        if (getZoneInfrastructureSupportScore(zoneId) <= 2) {
            return "supply_breakdown";
        }
        return "defender_overstretch";
    }

    private void syncActFourWarCampaignState() {
        activateActFourOperationIfNeeded(ACT4_OPERATION_IRON_LIFELINE);
        if (isActFourOperationStarted(ACT4_OPERATION_IRON_LIFELINE)
            && !isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            showActFourOperationBriefingIfNeeded(ACT4_OPERATION_IRON_LIFELINE);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_IRON_LIFELINE)
            && isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            showActFourOperationDebriefIfNeeded(ACT4_OPERATION_IRON_LIFELINE);
            activateActFourOperationIfNeeded(ACT4_OPERATION_GLOAM_ARCHIVE);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_GLOAM_ARCHIVE)
            && !isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            showActFourOperationBriefingIfNeeded(ACT4_OPERATION_GLOAM_ARCHIVE);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_GLOAM_ARCHIVE)
            && isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            showActFourOperationDebriefIfNeeded(ACT4_OPERATION_GLOAM_ARCHIVE);
            activateActFourOperationIfNeeded(ACT4_OPERATION_HELLCLIMB);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_HELLCLIMB)
            && !isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            showActFourOperationBriefingIfNeeded(ACT4_OPERATION_HELLCLIMB);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_HELLCLIMB)
            && isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            showActFourOperationDebriefIfNeeded(ACT4_OPERATION_HELLCLIMB);
            activateActFourOperationIfNeeded(ACT4_OPERATION_LAST_LIGHT);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_LAST_LIGHT)
            && !isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            showActFourOperationBriefingIfNeeded(ACT4_OPERATION_LAST_LIGHT);
        }
        if (isActFourOperationStarted(ACT4_OPERATION_LAST_LIGHT)
            && isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            showActFourOperationDebriefIfNeeded(ACT4_OPERATION_LAST_LIGHT);
            showActFourCampaignEpilogueIfNeeded();
        }
        syncActFourFactionSideArcs();
    }

    private void syncActFourFactionSideArcs() {
        activateActFourSideArcIfNeeded(ACT4_SIDEARC_COMMAND_BASTION);
        activateActFourSideArcIfNeeded(ACT4_SIDEARC_GUILD_ASCENDANCY);
        activateActFourSideArcIfNeeded(ACT4_SIDEARC_ARCHIVE_RECLAMATION);
        maybeCompleteActFourSideArc(ACT4_SIDEARC_COMMAND_BASTION);
        maybeCompleteActFourSideArc(ACT4_SIDEARC_GUILD_ASCENDANCY);
        maybeCompleteActFourSideArc(ACT4_SIDEARC_ARCHIVE_RECLAMATION);
    }

    private void activateActFourSideArcIfNeeded(String sideArcId) {
        if (sideArcId == null || sideArcId.isEmpty() || isActFourSideArcStarted(sideArcId)) {
            return;
        }
        if (ACT4_SIDEARC_COMMAND_BASTION.equals(sideArcId) && isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            worldStateManager.setFlag(gameState, operationFlag(sideArcId, "started"), true);
        } else if (ACT4_SIDEARC_GUILD_ASCENDANCY.equals(sideArcId) && isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            worldStateManager.setFlag(gameState, operationFlag(sideArcId, "started"), true);
        } else if (ACT4_SIDEARC_ARCHIVE_RECLAMATION.equals(sideArcId) && isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            worldStateManager.setFlag(gameState, operationFlag(sideArcId, "started"), true);
        }
    }

    private boolean isActFourSideArcStarted(String sideArcId) {
        return worldStateManager.isFlagActive(gameState, operationFlag(sideArcId, "started"));
    }

    private boolean isActFourSideArcCompleted(String sideArcId) {
        return worldStateManager.isFlagActive(gameState, operationFlag(sideArcId, "completed"));
    }

    private void maybeCompleteActFourSideArc(String sideArcId) {
        if (sideArcId == null || sideArcId.isEmpty() || !isActFourSideArcStarted(sideArcId) || isActFourSideArcCompleted(sideArcId)) {
            return;
        }
        boolean shouldComplete = false;
        if (ACT4_SIDEARC_COMMAND_BASTION.equals(sideArcId)) {
            shouldComplete = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0) >= 60
                && isCommandBastionProjectActive();
        } else if (ACT4_SIDEARC_GUILD_ASCENDANCY.equals(sideArcId)) {
            shouldComplete = getGuildSettlementCount() >= 2
                || (isCoalitionExchangeProjectActive() && getActivePlayerQuestContractCount() > 0);
        } else if (ACT4_SIDEARC_ARCHIVE_RECLAMATION.equals(sideArcId)) {
            shouldComplete = isArchiveWarCollegeProjectActive()
                && (gameState.getInfiniteDungeonBestFloor() >= 6 || isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT));
        }
        if (!shouldComplete) {
            return;
        }
        worldStateManager.setFlag(gameState, operationFlag(sideArcId, "completed"), true);
        grantActFourSideArcReward(sideArcId);
    }

    private void grantActFourSideArcReward(String sideArcId) {
        if (worldStateManager.isFlagActive(gameState, operationFlag(sideArcId, "reward_granted"))) {
            return;
        }
        if (ACT4_SIDEARC_COMMAND_BASTION.equals(sideArcId)) {
            addGold(110);
            addHealingPotions(1);
        } else if (ACT4_SIDEARC_GUILD_ASCENDANCY.equals(sideArcId)) {
            addGold(130);
            expeditionBoardReputation += 1;
        } else if (ACT4_SIDEARC_ARCHIVE_RECLAMATION.equals(sideArcId)) {
            addGold(80);
            metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + 2);
            metaProgressionManager.save(metaProgressionState);
        }
        worldStateManager.setFlag(gameState, operationFlag(sideArcId, "reward_granted"), true);
    }

    private void activateActFourOperationIfNeeded(String operationId) {
        if (operationId == null || operationId.isEmpty() || isActFourOperationStarted(operationId)) {
            return;
        }
        worldStateManager.setFlag(gameState, operationFlag(operationId, "started"), true);
        showActFourOperationBriefingIfNeeded(operationId);
    }

    private void showActFourOperationBriefingIfNeeded(String operationId) {
        if (!canShowActFourCampaignDialog()
            || worldStateManager.isFlagActive(gameState, operationFlag(operationId, "briefing_shown"))) {
            return;
        }
        showDialogueSequence(buildActFourOperationBriefing(operationId), "Command", getActFourOperationObjective(operationId));
        worldStateManager.setFlag(gameState, operationFlag(operationId, "briefing_shown"), true);
    }

    private void showActFourOperationDebriefIfNeeded(String operationId) {
        if (!canShowActFourCampaignDialog()
            || worldStateManager.isFlagActive(gameState, operationFlag(operationId, "debrief_shown"))) {
            return;
        }
        showDialogueSequence(buildActFourOperationDebrief(operationId), "Command", getActFourOperationStatusText(operationId));
        worldStateManager.setFlag(gameState, operationFlag(operationId, "debrief_shown"), true);
    }

    private void showActFourCampaignEpilogueIfNeeded() {
        if (!canShowActFourCampaignDialog() || worldStateManager.isFlagActive(gameState, ACT4_CAMPAIGN_EPILOGUE)) {
            return;
        }
        List<DialogueSystem.DialoguePage> pages = new ArrayList<>();
        pages.add(new DialogueSystem.DialoguePage("Commander Rex",
            "This is no longer a survival pocket. You've turned Ironhaven into the hand that moves the frontier. Every corridor, every guild board, every reclaimed ruin is answering to your campaign now."));
        pages.add(new DialogueSystem.DialoguePage("Professor Cogs",
            "The dusk data doesn't read like a dead world anymore. It reads like a world being rewritten by force, memory, and a frankly irresponsible amount of successful field improvisation."));
        pages.add(new DialogueSystem.DialoguePage("Guild Coalition",
            "Your charters are spreading faster than our messengers can pin them. The frontier sees a future again, and that's because you made one under garbage balancing and worse odds."));
        showDialogueSequence(pages, "Ironhaven", "Act 4 campaign secured.");
        worldStateManager.setFlag(gameState, ACT4_CAMPAIGN_EPILOGUE, true);
    }

    private boolean canShowActFourCampaignDialog() {
        return isHubTownZone() && !hasActiveDialog();
    }

    private boolean isActFourOperationStarted(String operationId) {
        return worldStateManager.isFlagActive(gameState, operationFlag(operationId, "started"));
    }

    private boolean isActFourOperationCompleted(String operationId) {
        return worldStateManager.isFlagActive(gameState, operationFlag(operationId, "completed"));
    }

    private void completeActFourOperation(String operationId) {
        if (operationId == null || operationId.isEmpty() || isActFourOperationCompleted(operationId)) {
            return;
        }
        worldStateManager.setFlag(gameState, operationFlag(operationId, "completed"), true);
        grantActFourOperationReward(operationId);
    }

    private String operationFlag(String operationId, String suffix) {
        return operationId + "." + suffix;
    }

    private void recordActFourSupplyVictory(String zoneId, String contractKind) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)
            || zoneId == null
            || zoneId.isEmpty()
            || contractKind == null
            || contractKind.isEmpty()) {
            return;
        }
        if (contractKind.startsWith("CONVOY_ESCORT")
            || contractKind.startsWith("PLAYER_CREATED_SUPPLY")
            || contractKind.startsWith("GUILD_BOARD_SUPPLY")
            || contractKind.startsWith("PUBLIC_BOARD_DELIVERY")) {
            completeActFourOperation(ACT4_OPERATION_IRON_LIFELINE);
        }
    }

    private void recordActFourRecoveryVictory(String zoneId, String contractKind) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)
            || zoneId == null
            || zoneId.isEmpty()
            || contractKind == null
            || contractKind.isEmpty()) {
            return;
        }
        if (("shadow_caves".equals(zoneId) || "crystal_depths".equals(zoneId))
            && (contractKind.startsWith("RECOVER_FRAGMENT")
            || contractKind.startsWith("PUBLIC_BOARD_RECOVERY")
            || contractKind.startsWith("PLAYER_CREATED_RECOVERY"))) {
            completeActFourOperation(ACT4_OPERATION_GLOAM_ARCHIVE);
        }
    }

    private void recordActFourBossVictory(String zoneId, String contractKind) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)
            || !"sky_fortress".equals(zoneId)
            || contractKind == null
            || contractKind.isEmpty()) {
            return;
        }
        if (contractKind.startsWith("DEFEAT_BOSS")
            || contractKind.startsWith("WORLD_BOSS_FRONT")
            || contractKind.startsWith("GUILD_STRIKE")
            || contractKind.startsWith("PLAYER_CREATED_STRIKE")) {
            completeActFourOperation(ACT4_OPERATION_HELLCLIMB);
        }
    }

    private void recordActFourDungeonVictory(String contractKind) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState) || contractKind == null || contractKind.isEmpty()) {
            return;
        }
        if ("LARGE_DUNGEON_EXPEDITION_SILVER".equals(contractKind)
            || "LARGE_DUNGEON_EXPEDITION_GOLD".equals(contractKind)) {
            completeActFourOperation(ACT4_OPERATION_LAST_LIGHT);
        }
    }

    private List<DialogueSystem.DialoguePage> buildActFourOperationBriefing(String operationId) {
        List<DialogueSystem.DialoguePage> pages = new ArrayList<>();
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "Act 4 begins now. Verdant lanes are under full war pressure, and the settlements we saved in earlier phases will starve if we let the routes die."));
            pages.add(new DialogueSystem.DialoguePage("Guild Coalition",
                "Then we post the first lifeline. Convoys, guild supply runs, public relief drops, your own charters, whatever moves the world forward. Hell mode or not, we climb by carrying everyone with us."));
            pages.add(new DialogueSystem.DialoguePage("Directive",
                "Operation Iron Lifeline: complete any convoy escort or supply-style war contract to prove Ironhaven can keep multiple fronts fed."));
            return pages;
        }
        if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Professor Cogs",
                "Shadow Caves and Crystal Depths are coughing up memory cores from the dusk era. If the hostiles strip those first, we'll never know what killed these regions or how to stop the next collapse."));
            pages.add(new DialogueSystem.DialoguePage("Scout Relay",
                "Treat this like a twilight salvage prayer with knives out. Recovery teams go in fast, pull fragments, and get out before the dark starts remembering us back."));
            pages.add(new DialogueSystem.DialoguePage("Directive",
                "Operation Gloam Archive: finish a recovery contract in Shadow Caves or Crystal Depths and return the data before hostile scavengers claim the ruin-memory."));
            return pages;
        }
        if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "The Sky Fortress has become the frontier's filter. If we can't break that summit under active resistance, every lower route stays one bad night away from collapse."));
            pages.add(new DialogueSystem.DialoguePage("Guild Coalition",
                "Good. A war phase should have at least one climb that feels unfair. Strike the fortress boss, front target, or posted charter and show the whole map the summit can bleed."));
            pages.add(new DialogueSystem.DialoguePage("Directive",
                "Operation Hellclimb: win a major boss operation in Sky Fortress."));
            return pages;
        }
        if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Professor Cogs",
                "We have enough routes to feed a world, enough archives to understand a dead age, and enough air control to launch one final proof. Now we descend where no settlement can escort us."));
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "Push a silver or gold Legacy Descent. Bring back proof that Ironhaven can hold the surface and still dominate the abyss below it."));
            pages.add(new DialogueSystem.DialoguePage("Directive",
                "Operation Last Light: complete a silver or gold large dungeon expedition."));
        }
        return pages;
    }

    private List<DialogueSystem.DialoguePage> buildActFourOperationDebrief(String operationId) {
        List<DialogueSystem.DialoguePage> pages = new ArrayList<>();
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Guild Coalition",
                "The first lifeline held. Settlements are talking about routes, not funerals, and that alone changes what this war thinks is possible."));
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "Good. We stop playing defense against starvation and start deciding which parts of the world survive next."));
            pages.add(new DialogueSystem.DialoguePage("Quartermaster", getActFourOperationRewardSummary(operationId)));
            return pages;
        }
        if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Professor Cogs",
                "Recovered archives confirm it: the world didn't just end, it was abandoned in layers. We can reverse some of those layers if we keep winning these ruins back."));
            pages.add(new DialogueSystem.DialoguePage("Archive Relay",
                "The dusk isn't just a grave anymore. It's a map."));
            pages.add(new DialogueSystem.DialoguePage("Quartermaster", getActFourOperationRewardSummary(operationId)));
            return pages;
        }
        if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "Sky Fortress cracked. That climb was supposed to break morale. Instead it became proof that the frontier can be taken upward, not just reclaimed sideways."));
            pages.add(new DialogueSystem.DialoguePage("Guild Coalition",
                "Every board in Ironhaven wants the next impossible contract now. That's your fault, and I mean that as praise."));
            pages.add(new DialogueSystem.DialoguePage("Quartermaster", getActFourOperationRewardSummary(operationId)));
            return pages;
        }
        if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            pages.add(new DialogueSystem.DialoguePage("Professor Cogs",
                "Your descent returned with something more valuable than salvage: certainty. Ironhaven can project power above ground, below ground, and across every dusk-band between."));
            pages.add(new DialogueSystem.DialoguePage("Commander Rex",
                "Then Act 4 is ours. We are no longer reacting to world events. We are generating them."));
            pages.add(new DialogueSystem.DialoguePage("Quartermaster", getActFourOperationRewardSummary(operationId)));
        }
        return pages;
    }

    private String getActFourOperationTitle(String operationId) {
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            return "Operation Iron Lifeline";
        }
        if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            return "Operation Gloam Archive";
        }
        if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            return "Operation Hellclimb";
        }
        if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            return "Operation Last Light";
        }
        return "War Operation";
    }

    private String getActFourOperationObjective(String operationId) {
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            return "Complete a convoy escort or supply-style war contract to prove your settlements can feed multiple fronts.";
        }
        if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            return "Recover strategic fragments from Shadow Caves or Crystal Depths before hostile scavengers erase the dusk record.";
        }
        if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            return "Win a major boss operation in Sky Fortress and turn the summit into a symbol of allied momentum.";
        }
        if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            return "Complete a silver or gold Legacy Descent to prove Ironhaven can dominate the abyss as well as the frontier surface.";
        }
        return "Advance the war phase.";
    }

    private String getActFourOperationStatusText(String operationId) {
        String status = isActFourOperationCompleted(operationId) ? "Complete" : isActFourOperationStarted(operationId) ? "Active" : "Locked";
        return getActFourOperationTitle(operationId) + " [" + status + "]: " + getActFourOperationObjective(operationId);
    }

    private void grantActFourOperationReward(String operationId) {
        if (operationId == null || operationId.isEmpty()
            || worldStateManager.isFlagActive(gameState, operationFlag(operationId, "reward_granted"))) {
            return;
        }
        int goldReward = 0;
        int shardReward = 0;
        int potionReward = 0;
        int reputationReward = 0;
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            goldReward = 140;
            potionReward = 1;
            reputationReward = 1;
        } else if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            goldReward = 90;
            shardReward = 3;
            reputationReward = 1;
        } else if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            goldReward = 170;
            shardReward = 2;
            potionReward = 1;
            reputationReward = 2;
        } else if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            goldReward = 220;
            shardReward = 4;
            potionReward = 2;
            reputationReward = 2;
        }
        if (goldReward > 0) {
            addGold(goldReward);
        }
        if (shardReward > 0) {
            metaProgressionState.setForgeShards(metaProgressionState.getForgeShards() + shardReward);
            metaProgressionManager.save(metaProgressionState);
        }
        if (potionReward > 0) {
            addHealingPotions(potionReward);
        }
        expeditionBoardReputation += reputationReward;
        maybeGrantExpeditionBoardSponsorReward();
        worldStateManager.setFlag(gameState, operationFlag(operationId, "reward_granted"), true);
    }

    private String getActFourOperationRewardSummary(String operationId) {
        if (ACT4_OPERATION_IRON_LIFELINE.equals(operationId)) {
            return "Campaign reward: +140 gold, +1 field kit, +1 board reputation.";
        }
        if (ACT4_OPERATION_GLOAM_ARCHIVE.equals(operationId)) {
            return "Campaign reward: +90 gold, +3 Forge Shards, +1 board reputation.";
        }
        if (ACT4_OPERATION_HELLCLIMB.equals(operationId)) {
            return "Campaign reward: +170 gold, +2 Forge Shards, +1 field kit, +2 board reputation.";
        }
        if (ACT4_OPERATION_LAST_LIGHT.equals(operationId)) {
            return "Campaign reward: +220 gold, +4 Forge Shards, +2 field kits, +2 board reputation.";
        }
        return null;
    }

    private List<String> getActFourCampaignStatusLines() {
        List<String> lines = new ArrayList<>();
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return lines;
        }
        int completed = 0;
        String[] operations = {
            ACT4_OPERATION_IRON_LIFELINE,
            ACT4_OPERATION_GLOAM_ARCHIVE,
            ACT4_OPERATION_HELLCLIMB,
            ACT4_OPERATION_LAST_LIGHT
        };
        for (String operationId : operations) {
            if (isActFourOperationCompleted(operationId)) {
                completed++;
            }
        }
        lines.add("War chronicle: " + completed + "/" + operations.length + " named operations secured.");
        for (String operationId : operations) {
            if (!isActFourOperationStarted(operationId)) {
                continue;
            }
            lines.add(getActFourOperationStatusText(operationId));
            if (!isActFourOperationCompleted(operationId)) {
                break;
            }
        }
        if (worldStateManager.isFlagActive(gameState, ACT4_CAMPAIGN_EPILOGUE)) {
            lines.add("Campaign result: Ironhaven is no longer surviving the frontier. It is setting the frontier's pace.");
        }
        lines.addAll(getRegionalCampaignThreadLines());
        lines.addAll(getActFourFactionStoryLines());
        lines.addAll(getRegionalIncidentStatusLines());
        lines.addAll(getSettlementCrisisStatusLines());
        lines.addAll(getWarConsequenceLines());
        return lines;
    }

    private List<String> getRegionalCampaignThreadLines() {
        List<String> lines = new ArrayList<>();
        String zoneId = getPrimaryWarDispatchZone();
        if (zoneId == null || zoneId.isEmpty()) {
            return lines;
        }
        lines.add("Regional thread: " + getRegionalCampaignThreadTitle(zoneId) + " - " + getRegionalCampaignThreadObjective(zoneId));
        if (worldStateManager.isFlagActive(gameState, "war.thread.cleared." + zoneId)) {
            lines.add("Regional thread result: " + getZoneDisplayName(zoneId) + " has been marked as a cleared campaign lane.");
        }
        return lines;
    }

    private String getRegionalCampaignThreadTitle(String zoneId) {
        if ("shadow_caves".equals(zoneId)) {
            return "Ghostline Recovery";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "Prismfall Accord";
        }
        if ("sky_fortress".equals(zoneId)) {
            return "Summit Mandate";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "Ironwake Extraction";
        }
        if ("verdant_fields".equals(zoneId)) {
            return "Lifeline Corridor";
        }
        return getZoneDisplayName(zoneId) + " Campaign";
    }

    private String getRegionalCampaignThreadObjective(String zoneId) {
        if ("shadow_caves".equals(zoneId)) {
            return "break the ghost-signal loop, recover the dusk record, and deny the scavenger scramble";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "survive the shard weather, claim the salvage window, and turn crystal loss into allied gain";
        }
        if ("sky_fortress".equals(zoneId)) {
            return "win the hard ascent and keep the summit from collapsing back into hostile control";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "stop extraction collapse and convert industrial pressure into secured supply stock";
        }
        if ("verdant_fields".equals(zoneId)) {
            return "hold the first corridor open so every later front has a backbone";
        }
        return "convert the active hotspot into a stable regional lane";
    }

    private List<String> getRegionalIncidentStatusLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeRegionalIncidentsByZoneId.entrySet()) {
            String name = getRegionalIncidentName(entry.getKey());
            String summary = getRegionalIncidentSummary(entry.getKey());
            if (name != null && summary != null) {
                lines.add("Incident: " + name + " in " + getZoneDisplayName(entry.getKey()) + " - " + summary + ".");
            }
            if (lines.size() >= 3) {
                break;
            }
        }
        return lines;
    }

    private List<String> getSettlementCrisisStatusLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeSettlementCrisesByZoneId.entrySet()) {
            String name = getSettlementCrisisName(entry.getKey());
            String summary = getSettlementCrisisSummary(entry.getKey());
            if (name != null && summary != null) {
                lines.add("Crisis: " + name + " at " + getZoneDisplayName(entry.getKey()) + " - " + summary + ".");
            }
            if (lines.size() >= 2) {
                break;
            }
        }
        return lines;
    }

    private List<String> getWarConsequenceLines() {
        List<String> lines = new ArrayList<>();
        for (String zoneId : zoneDefinitions.keySet()) {
            if (worldStateManager.isFlagActive(gameState, "war.consequence.stabilized." + zoneId)) {
                lines.add("Consequence: neighboring corridors around " + getZoneDisplayName(zoneId) + " stabilized after a successful regional containment.");
            } else if (worldStateManager.isFlagActive(gameState, "war.consequence.relief." + zoneId)) {
                lines.add("Consequence: relief pressure spread into " + getZoneDisplayName(zoneId) + " and prevented a second outpost collapse.");
            } else if (worldStateManager.isFlagActive(gameState, "war.consequence.spillover." + zoneId)) {
                lines.add("Consequence: hostile spillover has shifted into " + getZoneDisplayName(zoneId) + " after the last containment sweep.");
            } else if (worldStateManager.isFlagActive(gameState, "war.consequence.chain." + zoneId)) {
                lines.add("Consequence: route strain chained into " + getZoneDisplayName(zoneId) + " and opened a linked settlement crisis.");
            }
            if (lines.size() >= 3) {
                break;
            }
        }
        return lines;
    }

    private int getPositiveWarConsequenceCount() {
        int count = 0;
        for (String zoneId : zoneDefinitions.keySet()) {
            if (worldStateManager.isFlagActive(gameState, "war.consequence.stabilized." + zoneId)
                || worldStateManager.isFlagActive(gameState, "war.consequence.relief." + zoneId)) {
                count++;
            }
        }
        return count;
    }

    private int getNegativeWarConsequenceCount() {
        int count = 0;
        for (String zoneId : zoneDefinitions.keySet()) {
            if (worldStateManager.isFlagActive(gameState, "war.consequence.spillover." + zoneId)
                || worldStateManager.isFlagActive(gameState, "war.consequence.chain." + zoneId)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasTownServiceSurplus() {
        return getPositiveWarConsequenceCount() >= 2 || isCoalitionExchangeProjectActive();
    }

    private boolean hasTownServiceStrain() {
        return getNegativeWarConsequenceCount() >= 2 && !isCoalitionExchangeProjectActive();
    }

    private String getDominantWarFactionId() {
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        int hostiles = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        if (command >= guilds && command >= hostiles) {
            return WarPhaseManager.FACTION_IRONHAVEN_COMMAND;
        }
        if (guilds >= command && guilds >= hostiles) {
            return WarPhaseManager.FACTION_GUILD_COALITION;
        }
        return WarPhaseManager.FACTION_FRONTIER_HOSTILES;
    }

    private List<String> getActFourFactionStoryLines() {
        List<String> lines = new ArrayList<>();
        if (!warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return lines;
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            lines.add("Faction thread: annex crews are drafting emergency lifeline charters for any route that still looks survivable.");
        } else if (!isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            lines.add("Faction thread: archive scouts want dusk-memory cores from Shadow Caves and Crystal Depths before hostile crews erase the record.");
        } else if (!isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            lines.add("Faction thread: command is circulating summit warrants for the next impossible Sky Fortress push.");
        } else if (!isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            lines.add("Faction thread: the war college is underwriting deep-descent crews to prove Ironhaven can own the abyss too.");
        } else {
            lines.add("Faction thread: guild boards are now posting victory-grade charters built around your campaign model.");
        }
        if (!activeWorldBossFrontsByZoneId.isEmpty()) {
            String zoneId = activeWorldBossFrontsByZoneId.keySet().iterator().next();
            lines.add("Faction thread: field reports say " + getZoneDisplayName(zoneId)
                + " is defining the current theater and pulling contract traffic toward that front.");
        }
        lines.addAll(getActFourSideArcStatusLines());
        return lines;
    }

    private List<String> getActFourSideArcStatusLines() {
        List<String> lines = new ArrayList<>();
        lines.add(getActFourSideArcStatusText(
            ACT4_SIDEARC_COMMAND_BASTION,
            "Bastion Doctrine",
            "Stabilize command influence and bring the Bastion Network fully online."
        ));
        lines.add(getActFourSideArcStatusText(
            ACT4_SIDEARC_GUILD_ASCENDANCY,
            "Charter Ascendancy",
            "Expand guild settlement reach and turn posted charters into a standing war economy."
        ));
        lines.add(getActFourSideArcStatusText(
            ACT4_SIDEARC_ARCHIVE_RECLAMATION,
            "Dusk Ledger",
            "Merge archive doctrine and deep-descent proof into a permanent reclamation program."
        ));
        return lines;
    }

    private String getActFourSideArcStatusText(String sideArcId, String title, String objective) {
        String status = isActFourSideArcCompleted(sideArcId) ? "Complete" : isActFourSideArcStarted(sideArcId) ? "Active" : "Locked";
        return "Side arc " + title + " [" + status + "]: " + objective;
    }

    private void ensureFactionInfluenceBounds() {
        for (Map.Entry<String, Integer> entry : warPhaseManager.createDefaultFactionInfluence().entrySet()) {
            int current = factionInfluenceById.getOrDefault(entry.getKey(), entry.getValue());
            factionInfluenceById.put(entry.getKey(), Math.max(0, Math.min(100, current)));
        }
    }

    private void pruneResolvedWorldBossFronts() {
        List<String> zonesToRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeWorldBossFrontsByZoneId.entrySet()) {
            String zoneId = entry.getKey();
            String bossId = entry.getValue();
            ZoneDefinition definition = zoneDefinitions.get(zoneId);
            if (zoneId == null || zoneId.isEmpty() || bossId == null || bossId.isEmpty() || definition == null) {
                zonesToRemove.add(zoneId);
                continue;
            }
            if (!bossId.equals(definition.getBossId()) || gameState.hasDefeatedBoss(bossId)) {
                zonesToRemove.add(zoneId);
            }
        }
        for (String zoneId : zonesToRemove) {
            activeWorldBossFrontsByZoneId.remove(zoneId);
        }
    }

    private void populateWorldBossFrontsIfNeeded() {
        int desiredFronts = expeditionBoardReputation >= 10 ? 3 : expeditionBoardReputation >= 4 ? 2 : 1;
        for (ZoneDefinition definition : zoneDefinitions.values()) {
            if (definition == null || definition.getId() == null || definition.getId().isEmpty()) {
                continue;
            }
            if (activeWorldBossFrontsByZoneId.size() >= desiredFronts) {
                return;
            }
            if (definition.getBossId() == null || definition.getBossId().isEmpty() || gameState.hasDefeatedBoss(definition.getBossId())) {
                continue;
            }
            if (activeWorldBossFrontsByZoneId.containsKey(definition.getId())) {
                continue;
            }
            ZoneAccessPolicy.AccessDecision accessDecision = evaluateZoneAccess(definition.getId());
            if (!accessDecision.isAllowed()) {
                continue;
            }
            activeWorldBossFrontsByZoneId.put(definition.getId(), definition.getBossId());
        }
    }

    private boolean isActiveWorldBossFront(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return false;
        }
        syncWarPhaseState(false);
        return activeWorldBossFrontsByZoneId.containsKey(zoneId);
    }

    private String getActiveWorldBossFrontBossId(String zoneId) {
        return zoneId != null ? activeWorldBossFrontsByZoneId.get(zoneId) : null;
    }

    private boolean isBossInActiveFront(String bossId) {
        return bossId != null && activeWorldBossFrontsByZoneId.containsValue(bossId);
    }

    private void resolveWorldBossFrontByBossId(String bossId) {
        if (bossId == null || bossId.isEmpty()) {
            return;
        }
        String resolvedZoneId = null;
        for (Map.Entry<String, String> entry : activeWorldBossFrontsByZoneId.entrySet()) {
            if (bossId.equals(entry.getValue())) {
                resolvedZoneId = entry.getKey();
                break;
            }
        }
        if (resolvedZoneId != null) {
            activeWorldBossFrontsByZoneId.remove(resolvedZoneId);
        }
        adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 8);
        adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 6);
        adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -14);
        syncWarPhaseState(false);
    }

    private void adjustFactionInfluence(String factionId, int delta) {
        if (factionId == null || factionId.isEmpty() || delta == 0) {
            return;
        }
        int current = factionInfluenceById.getOrDefault(factionId, warPhaseManager.createDefaultFactionInfluence().getOrDefault(factionId, 50));
        factionInfluenceById.put(factionId, Math.max(0, Math.min(100, current + delta)));
    }

    private String getMonsterDisplayName(String monsterId) {
        MonsterDefinition definition = monsterId != null ? monsterDefinitions.get(monsterId) : null;
        if (definition != null && definition.getName() != null && !definition.getName().isEmpty()) {
            return definition.getName();
        }
        return formatZoneName(monsterId);
    }

    private String getZoneWarConditionSummary(String zoneId) {
        if (zoneId == null || zoneId.isEmpty() || "town".equals(zoneId) || INFINITE_DUNGEON_ZONE_ID.equals(zoneId)) {
            return null;
        }
        int hostile = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0);
        int command = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0);
        int guilds = factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0);
        boolean claimed = isZoneClaimedForWar(zoneId);
        boolean guildHall = isGuildHallWarZone(zoneId);

        if (!claimed && hostile >= 60) {
            return "Zone condition: hostile pressure spike. Unsecured enemies here gain extra durability and hit harder.";
        }
        if (claimed && command >= 60) {
            return "Zone condition: command corridor. Local hostiles are weakened by allied patrol coverage.";
        }
        if (guildHall && guilds >= 55) {
            return "Zone condition: guild contract lane. Marked hostiles are being hunted aggressively for higher-value sorties.";
        }
        return "Zone condition: contested but stable. No dominant faction modifier is overriding the route.";
    }

    private String buildZoneInfrastructureBonusLine(String zoneId) {
        if (zoneId == null || zoneId.isEmpty() || "town".equals(zoneId) || INFINITE_DUNGEON_ZONE_ID.equals(zoneId)) {
            return null;
        }
        BaseState baseState = baseStatesByZoneId.get(zoneId);
        if (baseState == null || baseState.getClaimedSiteIds().isEmpty()) {
            return null;
        }
        int storage = countActiveStructuresByCategory(baseState, StructureCategory.STORAGE);
        int defense = countActiveStructuresByCategory(baseState, StructureCategory.DEFENSE)
            + countActiveStructuresByCategory(baseState, StructureCategory.WALL);
        int logistics = countActiveStructuresByCategory(baseState, StructureCategory.UTILITY)
            + countActiveStructuresByCategory(baseState, StructureCategory.POWER)
            + countActiveStructuresByCategory(baseState, StructureCategory.CRAFTING);
        int support = getZoneInfrastructureSupportScore(zoneId);
        return getZoneDisplayName(zoneId) + ": support " + support
            + "  |  storage " + storage
            + "  |  defenses " + defense
            + "  |  logistics " + logistics + ".";
    }

    private List<String> getWarPressureZoneLines() {
        List<String> lines = new ArrayList<>();
        for (ZoneDefinition definition : zoneDefinitions.values()) {
            if (definition == null || definition.getId() == null || definition.getId().isEmpty() || "town".equals(definition.getId())) {
                continue;
            }
            String summary = getZoneWarConditionSummary(definition.getId());
            if (summary == null || summary.contains("contested but stable")) {
                continue;
            }
            lines.add(getZoneDisplayName(definition.getId()) + ": " + summary.replace("Zone condition: ", ""));
            if (lines.size() >= 4) {
                break;
            }
        }
        return lines;
    }

    private List<String> getWarInfrastructureLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, BaseState> entry : baseStatesByZoneId.entrySet()) {
            String line = buildZoneInfrastructureBonusLine(entry.getKey());
            if (line != null) {
                lines.add(line);
            }
            if (lines.size() >= 4) {
                break;
            }
        }
        return lines;
    }

    private List<String> buildStrategicFrontLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : activeWorldBossFrontsByZoneId.entrySet()) {
            String zoneId = entry.getKey();
            String bossId = entry.getValue();
            lines.add("Frontline: " + getWorldBossFrontVariantName(zoneId) + "  |  " + getWorldBossFrontBehavior(zoneId)
                + " front led by " + getMonsterDisplayName(bossId) + "  |  " + getWorldBossFrontVariantBonusText(zoneId) + ".");
            if (lines.size() >= 3) {
                break;
            }
        }
        return lines;
    }

    private List<String> buildStrategicCorridorLines() {
        List<String> lines = new ArrayList<>();
        List<Map.Entry<String, BaseState>> entries = new ArrayList<>(baseStatesByZoneId.entrySet());
        entries.sort((left, right) -> Integer.compare(
            getZoneInfrastructureSupportScore(right.getKey()),
            getZoneInfrastructureSupportScore(left.getKey())
        ));
        for (Map.Entry<String, BaseState> entry : entries) {
            String zoneId = entry.getKey();
            BaseState baseState = entry.getValue();
            if (baseState == null || baseState.getClaimedSiteIds().isEmpty()) {
                continue;
            }
            int support = getZoneInfrastructureSupportScore(zoneId);
            String condition = getZoneWarConditionSummary(zoneId);
            String shortCondition = condition != null && condition.startsWith("Zone condition: ")
                ? condition.substring("Zone condition: ".length())
                : "stable corridor";
            lines.add("Corridor: " + getZoneDisplayName(zoneId) + "  |  support " + support + "  |  " + shortCondition);
            if (lines.size() >= 4) {
                break;
            }
        }
        return lines;
    }

    private List<String> buildStrategicProjectPriorityLines() {
        List<String> lines = new ArrayList<>();
        if (isCommandBastionProjectActive()) {
            lines.add("Project online: Command Bastion Network is anchoring fortified response corridors.");
        }
        if (isCoalitionExchangeProjectActive()) {
            lines.add("Project online: Coalition Exchange is enriching trade, convoy, and guild logistics lanes.");
        }
        if (isArchiveWarCollegeProjectActive()) {
            lines.add("Project online: Archive War College is converting war salvage into doctrine and shards.");
        }
        if (isHostileSabotageProjectActive()) {
            lines.add("Project threat: Hostile Sabotage Cells are degrading calmer civilian lanes.");
        }
        return lines;
    }

    private String buildStrategicPriorityLine() {
        if (!activeWorldBossFrontsByZoneId.isEmpty()) {
            Map.Entry<String, String> front = activeWorldBossFrontsByZoneId.entrySet().iterator().next();
            return "Current priority: break the " + getWorldBossFrontBehavior(front.getKey()).toLowerCase(Locale.ROOT)
                + " front in " + getZoneDisplayName(front.getKey()) + ".";
        }
        if (factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) >= 60) {
            return "Current priority: stabilize threatened corridors and keep hostile influence below siege threshold.";
        }
        if (gameState.getInfiniteDungeonBestFloor() < (expeditionBoardReputation >= 10 ? 10 : expeditionBoardReputation >= 4 ? 6 : 3)) {
            return "Current priority: certify deeper Legacy Descent floors for multi-team war deployment.";
        }
        return "Current priority: expand support corridors and convert logistics strength into world control.";
    }

    private String buildStrategicDirectiveLine(ExpeditionLaunchDestination destination) {
        if (destination == null) {
            return "Directive: await command assignment.";
        }
        String objective = getDestinationObjective(destination);
        if (isActiveWorldBossFront(destination.zoneId)) {
            return "Directive [" + getWorldBossFrontBehavior(destination.zoneId) + " Front - "
                + getWorldBossFrontEffectSummary(destination.zoneId) + "]: " + objective;
        }
        if (shouldOfferLargeDungeonExpedition(destination)) {
            return "Directive [Legacy Descent]: " + objective;
        }
        if (shouldOfferConvoyEscort(destination)) {
            return "Directive [Relief Convoy]: " + objective;
        }
        if (shouldOfferGuildStrikeContract(destination)) {
            return "Directive [Guild Strike]: " + objective;
        }
        if (shouldOfferGuildBoardContract(destination)) {
            return "Directive [Guild Supply]: " + objective;
        }
        if (shouldOfferPublicRecoveryContract(destination)) {
            return "Directive [Recovery Sweep]: " + objective;
        }
        if (shouldOfferPublicBoardContract(destination)) {
            return "Directive [Public Support]: " + objective;
        }
        if ("shadow_caves".equals(destination.zoneId)) {
            return "Directive [Twilight Recovery]: descend through the ruin-dark, keep the line intact, and bring the dead archive back under allied control. " + objective;
        }
        if ("sky_fortress".equals(destination.zoneId)) {
            return "Directive [Hell Climb]: commit to the hard route, clear the upper air rail, and prove this theater can be conquered the brutal way. " + objective;
        }
        if ("crystal_depths".equals(destination.zoneId)) {
            return "Directive [Dusk Descent]: harvest what still remembers the old world before hostile crews turn memory into territory. " + objective;
        }
        return "Directive [Frontier Push]: " + objective;
    }

    private String buildStrategicSupportLine(String zoneId) {
        String condition = getZoneWarConditionSummary(zoneId);
        int support = getZoneInfrastructureSupportScore(zoneId);
        int contractBonus = getZoneContractSupportBonus(zoneId, determinePinnedContractKind(
            new ExpeditionLaunchDestination(zoneId, null, getZoneDisplayName(zoneId), "", zoneId.equals(currentZoneId), "")
        ));
        return "Support picture: corridor " + support + "  |  contract bonus " + contractBonus + "g"
            + (condition != null ? "  |  " + condition.replace("Zone condition: ", "") : ".");
    }

    private String buildLargeDungeonStatusLine() {
        int bestFloor = gameState.getInfiniteDungeonBestFloor();
        String runStatus = gameState.isInfiniteDungeonRunActive()
            ? "run active on floor " + getInfiniteDungeonCurrentFloor()
            : "ready for a fresh descent";
        return "Legacy Descent: best floor " + bestFloor + ", " + runStatus
            + ". Recommended war-depth contract target: floor "
            + (expeditionBoardReputation >= 10 ? 10 : expeditionBoardReputation >= 4 ? 6 : 3) + ".";
    }

    private int getZoneContractSupportBonus(String zoneId, String contractKind) {
        if (zoneId == null || zoneId.isEmpty() || contractKind == null || contractKind.isEmpty()) {
            return 0;
        }
        BaseState baseState = baseStatesByZoneId.get(zoneId);
        if (baseState == null) {
            return 0;
        }
        int storage = countActiveStructuresByCategory(baseState, StructureCategory.STORAGE);
        int defense = countActiveStructuresByCategory(baseState, StructureCategory.DEFENSE)
            + countActiveStructuresByCategory(baseState, StructureCategory.WALL);
        int logistics = countActiveStructuresByCategory(baseState, StructureCategory.UTILITY)
            + countActiveStructuresByCategory(baseState, StructureCategory.POWER)
            + countActiveStructuresByCategory(baseState, StructureCategory.CRAFTING);
        int bonus = 0;
        if (contractKind.startsWith("CONVOY_ESCORT")
            || contractKind.startsWith("GUILD_BOARD_SUPPLY")
            || contractKind.startsWith("PUBLIC_BOARD_DELIVERY")) {
            bonus += storage * 10 + logistics * 6;
        }
        if (contractKind.startsWith("WORLD_BOSS_FRONT")
            || contractKind.startsWith("GUILD_STRIKE")
            || contractKind.startsWith("DEFEAT_BOSS")) {
            bonus += defense * 8 + logistics * 4;
        }
        if (contractKind.startsWith("PUBLIC_BOARD_RECOVERY")
            || contractKind.startsWith("RECOVER_FRAGMENT")) {
            bonus += storage * 6 + logistics * 8;
        }
        if (contractKind.startsWith("LARGE_DUNGEON_EXPEDITION")) {
            bonus += logistics * 10 + defense * 4;
        }
        return Math.min(90, bonus);
    }

    private boolean isCommandBastionProjectActive() {
        return warPhaseManager.isWarPhaseUnlocked(gameState)
            && factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 0) >= 55
            && (worldStateManager.isFlagActive(gameState, "settlement.command_hub")
            || worldStateManager.isFlagActive(gameState, "settlement.watchtower_network"));
    }

    private boolean isCoalitionExchangeProjectActive() {
        return warPhaseManager.isWarPhaseUnlocked(gameState)
            && getGuildSettlementCount() >= 1
            && factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_GUILD_COALITION, 0) >= 50
            && (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")
            || worldStateManager.isFlagActive(gameState, "settlement.tavern_open"));
    }

    private boolean isArchiveWarCollegeProjectActive() {
        return warPhaseManager.isWarPhaseUnlocked(gameState)
            && worldStateManager.isFlagActive(gameState, "settlement.archive_open")
            && worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open")
            && (worldStateManager.isFlagActive(gameState, "settlement.data_vaults")
            || worldStateManager.isFlagActive(gameState, "settlement.prototype_lab"));
    }

    private boolean isHostileSabotageProjectActive() {
        return warPhaseManager.isWarPhaseUnlocked(gameState)
            && factionInfluenceById.getOrDefault(WarPhaseManager.FACTION_FRONTIER_HOSTILES, 0) >= 65;
    }

    private List<String> getActiveSettlementProjectLines() {
        List<String> lines = new ArrayList<>();
        if (isCommandBastionProjectActive()) {
            lines.add("Command Bastion Network: command relays are stabilizing owned corridors and boosting strike coordination.");
        }
        if (isCoalitionExchangeProjectActive()) {
            lines.add("Coalition Exchange: guild annex traffic is turning deliveries and convoy work into richer contract payouts.");
        }
        if (isArchiveWarCollegeProjectActive()) {
            lines.add("Archive War College: archive analysts are converting recovered war data into shard and reputation gains.");
        }
        if (isHostileSabotageProjectActive()) {
            lines.add("Hostile Sabotage Cells: enemy pressure is disrupting calm routes and forcing stronger raid responses.");
        }
        return lines;
    }

    private void handleLargeDungeonExpeditionProgress(int completedFloor, boolean sealedExit) {
        if (pinnedExpeditionContractCompleted
            || pinnedExpeditionContractKind == null
            || !pinnedExpeditionContractKind.startsWith("LARGE_DUNGEON_EXPEDITION")) {
            return;
        }
        int targetFloor = getPinnedLargeDungeonTargetFloor();
        if (targetFloor <= 0 || completedFloor < targetFloor) {
            return;
        }
        adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, sealedExit ? 9 : 7);
        adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, sealedExit ? 6 : 4);
        adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, sealedExit ? -10 : -7);
        resolveRegionalIncident("shadow_caves");
        resolveRegionalIncident("crystal_depths");
        recordActFourDungeonVictory(pinnedExpeditionContractKind);
        syncWarPhaseState(false);
        completePinnedExpeditionContract("Contract complete. Large dungeon expedition certified at floor " + completedFloor + ".");
    }

    private RunOutcomeSummary buildRunOutcomeSummary() {
        BaseState baseState = getCurrentBaseState();
        int structuresBuilt = 0;
        int claimedSites = 0;
        if (baseState != null) {
            for (PlacedStructure structure : baseState.getPlacedStructures()) {
                if (structure != null && structure.isActive()) {
                    structuresBuilt++;
                }
            }
            claimedSites = baseState.getClaimedSiteIds().size();
        }
        int defeatedBosses = gameState.getDefeatedBossCount();
        String rank = currentZoneDefinition != null ? currentZoneDefinition.getRankFloor() : "G";
        return new RunOutcomeSummary(
            rank,
            totalEnemiesKilled,
            survivalTime,
            playerLevel,
            defeatedBosses,
            structuresBuilt,
            claimedSites
        );
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        hudOverlay.resize(width, height);
        debugOverlay.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        game.getEventBus().unsubscribe(this);
        hudOverlay.dispose();
        debugOverlay.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        disposeCurrentTiledMap();
        for (String path : managedTexturePaths) {
            game.unloadTexture(path);
        }
        managedTexturePaths.clear();
    }

    public void handlePauseInput() {
        pauseGame();
    }

    public void openWorkshop() {
        if (!isPaused && !battleActive) {
            screenManager.push(new WorkshopScreen(game, screenManager, this));
        }
    }

    public void openForge() {
        if (!isPaused && !battleActive) {
            if (!isForgeWorkshopUnlocked()) {
                showStandaloneDialog("Town Forge", "The workshop rail is still dark. Restore Workshop Tools before the forge can reopen.");
                return;
            }
            screenManager.push(new ForgeScreen(game, screenManager, this));
        }
    }

    public void dismissMessages() {
        if (advanceDialogPage()) {
            return;
        }
        if (advanceDialogueSequence()) {
            return;
        }
        clearActiveDialog();
    }

    public void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            screenManager.push(new PauseMenuScreen(game, screenManager, this));
        }
    }

    public void resumeGame() {
        isPaused = false;
        ensureGameInputProcessor();
    }
    public boolean isPaused() { return isPaused; }
    public GameLoop getGameLoop() { return gameLoop; }
    public HUDOverlay getHUDOverlay() { return hudOverlay; }
    public String getCurrentZoneId() { return currentZoneId; }
    public long getWorldSeed() { return worldSeed; }

    public InputContext getCurrentInputContext() {
        return resolveInputContext();
    }

    private InputContext resolveInputContext() {
        if (hasActiveDialog()) {
            return InputContext.DIALOG;
        }
        if (buildModeOpen) {
            return InputContext.BUILD;
        }
        if (questMenuOpen || expeditionBoardOpen || guildMenuOpen || isPaused) {
            return InputContext.SETTLEMENT;
        }
        return InputContext.EXPLORATION;
    }

    private List<DebugOverlay.DebugSection> buildDebugOverlaySections() {
        List<DebugOverlay.DebugSection> sections = new ArrayList<>();
        sections.add(new DebugOverlay.DebugSection("World", List.of(
            "FPS: " + Gdx.graphics.getFramesPerSecond(),
            "Zone: " + (currentZoneId != null ? currentZoneId : "none"),
            "Seed: " + worldSeed,
            "Player: (" + (int) playerPos.x + ", " + (int) playerPos.y + ")",
            "Floating origin: (" + (int) floatingOriginOffset.x + ", " + (int) floatingOriginOffset.y + ")"
        )));
        sections.add(new DebugOverlay.DebugSection("Runtime", List.of(
            "Input: " + resolveInputContext().name(),
            "Enemies: " + countAliveEnemies() + "/" + enemies.size(),
            "NPCs: " + npcs.size() + "  Robots: " + countAliveRobots(),
            "Battle active: " + battleActive + "  Build mode: " + buildModeOpen,
            "Chunks: " + frontierChunkManager.getActiveChunks().size() + "  Regions: " + frontierChunkManager.getActiveRegions().size(),
            "Heap: " + getUsedHeapMegabytes() + " MB / " + getMaxHeapMegabytes() + " MB"
        )));
        sections.add(new DebugOverlay.DebugSection("World Events", List.of(
            "Boss fronts: " + activeWorldBossFrontsByZoneId.size(),
            "Incidents: " + activeRegionalIncidentsByZoneId.size(),
            "Crises: " + activeSettlementCrisesByZoneId.size(),
            "Pinned contract: " + (pinnedExpeditionContractTitle != null ? pinnedExpeditionContractTitle : "none")
        )));
        sections.add(new DebugOverlay.DebugSection("Hot Reload", List.of(
            "Watch files: " + WATCHED_DEFINITION_PATHS.length,
            changedDefinitionPaths.isEmpty()
                ? "Definitions stable. Press R or Ctrl+R to reload."
                : changedDefinitionPaths.size() + " changed file(s) detected. Press R to reload."
        )));
        sections.add(new DebugOverlay.DebugSection("Robot Field Skills", buildRobotDebugLines()));
        return sections;
    }

    private List<String> buildRobotDebugLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (!hasActiveRobotAt(i)) {
                continue;
            }
            RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(i);
            lines.add(getRobotName(i) + " Lv" + getRobotLevel(i)
                + " [" + getRobotClass(i) + "]"
                + " {" + (progressionState != null ? progressionState.getPersonalityArchetype() : "Balanced") + "}"
                + "  " + buildRobotFieldRoleSummary(i));
        }
        if (lines.isEmpty()) {
            lines.add("No active robots.");
        }
        return lines;
    }

    private String buildRobotFieldRoleSummary(int index) {
        String robotClass = getRobotClass(index);
        int tier = Math.max(1, getRobotEvolutionTier(index));
        List<String> tags = new ArrayList<>();
        if (robotClass.contains("Scout")) {
            tags.add("hack " + tier);
            tags.add("scan " + (tier + 1));
        }
        if (robotClass.contains("Support")) {
            tags.add("field " + tier);
            tags.add("scan " + tier);
        }
        if (robotClass.contains("Vanguard")) {
            tags.add("mine " + (tier + 1));
            tags.add("labor " + tier);
        }
        if (robotClass.contains("Striker")) {
            tags.add("demo " + tier);
            tags.add("cut " + tier);
        }
        return tags.isEmpty() ? "generalist" : String.join(", ", tags);
    }

    private long getUsedHeapMegabytes() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
    }

    private long getMaxHeapMegabytes() {
        return Runtime.getRuntime().maxMemory() / (1024L * 1024L);
    }

    private int countAliveEnemies() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.alive) {
                count++;
            }
        }
        return count;
    }

    private int countAliveRobots() {
        int count = 0;
        for (RobotCompanion robot : robots) {
            if (robot != null && robot.health > 0f) {
                count++;
            }
        }
        return count;
    }

    private long generateWorldSeed() {
        long seed = System.currentTimeMillis();
        seed ^= ((long) playerName.hashCode()) << 32;
        seed ^= 0x9E3779B97F4A7C15L;
        return seed != 0L ? seed : 1L;
    }

    public SaveFile buildSaveFile(int slot) {
        syncActiveRobotHealthToProgression();
        SaveFile sf = new SaveFile();
        sf.setPlayerName(playerName);
        sf.setPlayerHp((int) playerHealth);
        sf.setPlayerMaxHp((int) playerMaxHealth);
        sf.setPlayerX(playerPos.x);
        sf.setPlayerY(playerPos.y);
        sf.setPlayerWorldX(playerPos.x + floatingOriginOffset.x);
        sf.setPlayerWorldY(playerPos.y + floatingOriginOffset.y);
        sf.setFloatingOriginX(floatingOriginOffset.x);
        sf.setFloatingOriginY(floatingOriginOffset.y);
        sf.setSettlementTimeOfDayHours(settlementTimeManager.getTimeOfDayHours());
        sf.setCurrencyBalance(gameState.getTotalGold());
        sf.setUnbankedCurrencyBalance(gameState.getUnbankedGold());
        sf.setWorldSeed(worldSeed);
        sf.setHealingPotions(gameState.getHealingPotions());
        sf.setPlayerLevel(gameState.getPlayerLevel());
        sf.setPlayerExperience(gameState.getPlayerExperience());
        sf.setPlayerEquipment(new HashMap<>(gameState.getPlayerEquipmentSlots()));
        sf.setOwnedEquipmentIds(new ArrayList<>(gameState.getOwnedEquipmentIds()));
        sf.setQuestStates(new HashMap<>(gameState.getQuestStates()));
        sf.setBestiaryScanLevels(new HashMap<>(gameState.getBestiaryScanLevels()));
        sf.setKeyItems(new ArrayList<>(gameState.getKeyItems()));
        sf.setWorldStateFlags(new HashMap<>(gameState.getWorldStateFlags()));
        sf.setSettlementUpgrades(new HashMap<>(gameState.getSettlementUpgrades()));
        sf.setForgeComponents(new HashMap<>(gameState.getForgeComponents()));
        sf.setUnbankedForgeComponents(new HashMap<>(gameState.getUnbankedForgeComponents()));
        sf.setShardInventory(new HashMap<>(gameState.getShardInventory()));
        sf.setUnbankedShardInventory(new HashMap<>(gameState.getUnbankedShards()));
        sf.setBlueprintFragments(new HashMap<>(gameState.getBlueprintFragments()));
        sf.setUnbankedBlueprintFragments(new HashMap<>(gameState.getUnbankedBlueprintFragments()));
        sf.setForgeCoreLevel(gameState.getForgeCoreLevel());
        sf.setInfiniteDungeonCurrentFloor(gameState.getInfiniteDungeonCurrentFloor());
        sf.setInfiniteDungeonBestFloor(gameState.getInfiniteDungeonBestFloor());
        sf.setInfiniteDungeonFloorsCleared(gameState.getInfiniteDungeonFloorsCleared());
        sf.setInfiniteDungeonRunActive(gameState.isInfiniteDungeonRunActive());
        sf.setDefeatedBossIds(new ArrayList<>(gameState.getDefeatedBossIds()));
        sf.setHarvestedFrontierFeatureIds(new ArrayList<>(harvestedFrontierFeatureIds));
        sf.setClaimedFrontierBaseSiteIds(new ArrayList<>(claimedFrontierBaseSiteIds));
        sf.setBaseStates(buildBaseStateSaveData());
        sf.setGuilds(buildGuildSaveData());
        sf.setActiveClaimGuildId(activeClaimGuildId);
        sf.setPinnedExpeditionContractTitle(pinnedExpeditionContractTitle);
        sf.setPinnedExpeditionContractText(pinnedExpeditionContractText);
        sf.setPinnedExpeditionContractZoneId(pinnedExpeditionContractZoneId);
        sf.setPinnedExpeditionContractKind(pinnedExpeditionContractKind);
        sf.setPinnedExpeditionContractTargetId(pinnedExpeditionContractTargetId);
        sf.setPinnedExpeditionContractCompleted(pinnedExpeditionContractCompleted);
        sf.setExpeditionBoardReputation(expeditionBoardReputation);
        sf.setFactionInfluenceById(new HashMap<>(factionInfluenceById));
        sf.setActiveWorldBossFrontsByZoneId(new HashMap<>(activeWorldBossFrontsByZoneId));
        sf.setActiveRegionalIncidentsByZoneId(new HashMap<>(activeRegionalIncidentsByZoneId));
        sf.setActiveSettlementCrisesByZoneId(new HashMap<>(activeSettlementCrisesByZoneId));
        sf.setPlayerQuestContracts(buildPlayerQuestContractSaveData());
        sf.setPlayerCreatedNpcs(buildPlayerCreatedNpcSaveData());
        sf.setExpeditionBoardMode(expeditionBoardMode);
        sf.setActiveChallengeModifierId(activeChallengeModifierId);
        sf.setHardModeSeedIndex(selectedHardModeSeedIndex);
        sf.setSelectedChallengeModifierIndex(selectedChallengeModifierIndex);
        sf.setCurrentZoneId(gameState.getCurrentZoneId());
        sf.setRobotEquipment(copyRobotEquipment(robotEquipment));
        sf.setCollectedRobotIds(new ArrayList<>(gameState.getCollectedRobotIds()));
        sf.setActiveRobotIds(new ArrayList<>(gameState.getActiveRobotIds()));
        sf.setRobotProgressionStates(new HashMap<>(gameState.getRobotProgressionStates()));
        sf.setRobotHealth(getRobotHealthValues());
        sf.setRobotMaxHealth(getRobotMaxHealthValues());
        sf.setRobotBaseMaxHealth(getRobotBaseMaxHealthValues());
        sf.setRobotX(getRobotXValues());
        sf.setRobotY(getRobotYValues());
        sf.setRobotAngleDeg(getRobotAngleValues());
        sf.setRobotAttackTimers(getRobotAttackTimerValues());
        sf.setTotalEnemiesKilled(totalEnemiesKilled);
        sf.setEnemies(buildEnemySaveStates());
        sf.setChests(buildChestSaveStates());
        sf.setPlayTimeSeconds((long) survivalTime);
        sf.setSaveTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.UK).format(new Date()));
        sf.setSaveSlot(slot);
        return sf;
    }

    private void loadFromSave(SaveFile saveFile) {
        currentZoneId = saveFile.getCurrentZoneId() != null ? saveFile.getCurrentZoneId() : currentZoneId;
        worldSeed = saveFile.getWorldSeed() != 0L ? saveFile.getWorldSeed() : generateWorldSeed();
        loadGuildsFromSave(saveFile);
        activeClaimGuildId = saveFile.getActiveClaimGuildId();
        pinnedExpeditionContractTitle = saveFile.getPinnedExpeditionContractTitle();
        pinnedExpeditionContractText = saveFile.getPinnedExpeditionContractText();
        pinnedExpeditionContractZoneId = saveFile.getPinnedExpeditionContractZoneId();
        pinnedExpeditionContractKind = saveFile.getPinnedExpeditionContractKind();
        pinnedExpeditionContractTargetId = saveFile.getPinnedExpeditionContractTargetId();
        pinnedExpeditionContractCompleted = saveFile.isPinnedExpeditionContractCompleted();
        expeditionBoardReputation = saveFile.getExpeditionBoardReputation();
        factionInfluenceById.clear();
        factionInfluenceById.putAll(saveFile.getFactionInfluenceById());
        activeWorldBossFrontsByZoneId.clear();
        activeWorldBossFrontsByZoneId.putAll(saveFile.getActiveWorldBossFrontsByZoneId());
        activeRegionalIncidentsByZoneId.clear();
        activeRegionalIncidentsByZoneId.putAll(saveFile.getActiveRegionalIncidentsByZoneId());
        activeSettlementCrisesByZoneId.clear();
        activeSettlementCrisesByZoneId.putAll(saveFile.getActiveSettlementCrisesByZoneId());
        loadPlayerQuestContractsFromSave(saveFile);
        loadPlayerCreatedNpcsFromSave(saveFile);
        expeditionBoardMode = saveFile.getExpeditionBoardMode();
        activeChallengeModifierId = saveFile.getActiveChallengeModifierId();
        activeHardModeSeedIndex = saveFile.getHardModeSeedIndex();
        selectedHardModeSeedIndex = saveFile.getHardModeSeedIndex();
        selectedChallengeModifierIndex = saveFile.getSelectedChallengeModifierIndex();
        loadBaseStatesFromSave(saveFile);
        gameState.setInfiniteDungeonCurrentFloor(saveFile.getInfiniteDungeonCurrentFloor());
        gameState.setInfiniteDungeonBestFloor(saveFile.getInfiniteDungeonBestFloor());
        gameState.setInfiniteDungeonFloorsCleared(saveFile.getInfiniteDungeonFloorsCleared());
        gameState.setInfiniteDungeonRunActive(saveFile.isInfiniteDungeonRunActive());
        playerEquipment = saveFile.getPlayerEquipment() != null
            ? new HashMap<>(saveFile.getPlayerEquipment())
            : new HashMap<>();
        gameState.setPlayerEquipment(playerEquipment);
        ownedEquipmentIds.clear();
        if (saveFile.getOwnedEquipmentIds() != null && !saveFile.getOwnedEquipmentIds().isEmpty()) {
            ownedEquipmentIds.addAll(saveFile.getOwnedEquipmentIds());
        } else {
            seedStarterOwnedEquipment();
        }
        gameState.setOwnedEquipmentIds(ownedEquipmentIds);
        gameState.setQuestStates(saveFile.getQuestStates());
        gameState.setBestiaryScanLevels(saveFile.getBestiaryScanLevels());
        keyItems.clear();
        if (saveFile.getKeyItems() != null) {
            keyItems.addAll(saveFile.getKeyItems());
        }
        gameState.setKeyItems(keyItems);
        gameState.setWorldStateFlags(saveFile.getWorldStateFlags());
        gameState.setSettlementUpgrades(saveFile.getSettlementUpgrades());
        gameState.setForgeComponents(saveFile.getForgeComponents());
        gameState.setUnbankedForgeComponents(saveFile.getUnbankedForgeComponents());
        gameState.setShardInventory(saveFile.getShardInventory());
        gameState.setUnbankedShards(saveFile.getUnbankedShardInventory());
        gameState.setBlueprintFragments(saveFile.getBlueprintFragments());
        gameState.setUnbankedBlueprintFragments(saveFile.getUnbankedBlueprintFragments());
        gameState.setForgeCoreLevel(saveFile.getForgeCoreLevel());
        gameState.setDefeatedBossIds(saveFile.getDefeatedBossIds());
        harvestedFrontierFeatureIds.clear();
        harvestedFrontierFeatureIds.addAll(saveFile.getHarvestedFrontierFeatureIds());
        syncClaimedFrontierBaseSiteIds();
        worldStateManager.initialize(gameState);
        questManager.initialize(gameState);
        backfillLegacyFrontierAnnexUnlock();
        syncAct2TownFacilities();
        questManager.syncProgress(gameState, worldStateManager);
        loadZone(currentZoneId, null, true);
        if (saveFile.getSettlementTimeOfDayHours() != null) {
            timeSystem.setTimeOfDayHours(saveFile.getSettlementTimeOfDayHours());
        }
        if (saveFile.getFloatingOriginX() != null || saveFile.getFloatingOriginY() != null) {
            Vector2 savedOriginShift = new Vector2(
                saveFile.getFloatingOriginX() != null ? saveFile.getFloatingOriginX() : 0f,
                saveFile.getFloatingOriginY() != null ? saveFile.getFloatingOriginY() : 0f
            );
            if (!savedOriginShift.isZero()) {
                applyFloatingOriginShift(savedOriginShift);
            }
        }
        cameraController.syncFloatingOrigin(floatingOriginOffset);
        playerHealth = saveFile.getPlayerHp();
        playerMaxHealth = saveFile.getPlayerMaxHp() > 0 ? saveFile.getPlayerMaxHp() : playerMaxHealth;
        float savedPlayerX = saveFile.getPlayerX();
        float savedPlayerY = saveFile.getPlayerY();
        if (saveFile.getPlayerWorldX() != null && saveFile.getPlayerWorldY() != null) {
            savedPlayerX = saveFile.getPlayerWorldX() - floatingOriginOffset.x;
            savedPlayerY = saveFile.getPlayerWorldY() - floatingOriginOffset.y;
        }
        playerPos.set(savedPlayerX, savedPlayerY);
        totalGold = saveFile.getCurrencyBalance();
        healingPotions = saveFile.getHealingPotions();
        playerLevel = Math.max(1, saveFile.getPlayerLevel());
        playerExperience = Math.max(0, saveFile.getPlayerExperience());
        gameState.setPlayerHealth(playerHealth);
        gameState.setPlayerMaxHealth(playerMaxHealth);
        gameState.setTotalGold(totalGold);
        gameState.setUnbankedGold(saveFile.getUnbankedCurrencyBalance());
        gameState.setHealingPotions(healingPotions);
        gameState.setPlayerLevel(playerLevel);
        gameState.setPlayerExperience(playerExperience);
        totalEnemiesKilled = saveFile.getTotalEnemiesKilled();
        survivalTime = saveFile.getPlayTimeSeconds();
        currentSaveSlot = saveFile.getSaveSlot();
        gameState.setCurrentZoneId(currentZoneId);
        gameState.setCurrentSaveSlot(currentSaveSlot);
        robotEquipment = copyRobotEquipment(saveFile.getRobotEquipment());
        gameState.setRobotEquipment(robotEquipment);
        if (saveFile.getOwnedEquipmentIds() == null || saveFile.getOwnedEquipmentIds().isEmpty()) {
            mergeOwnedEquipmentFromLoadout();
        }
        collectedRobotIds = saveFile.getCollectedRobotIds() != null
            ? new ArrayList<>(saveFile.getCollectedRobotIds())
            : new ArrayList<>();
        collectedRobotIds.removeIf(robotId -> robotId == null || robotId.isEmpty());
        activeRobotIds = saveFile.getActiveRobotIds() != null
            ? new ArrayList<>(saveFile.getActiveRobotIds())
            : new ArrayList<>();
        syncLegendaryRosterToCurrentIds();
        normalizeActiveRobotSlots();
        clampActiveRobotsToUnlockedSlots();
        gameState.setCollectedRobotIds(collectedRobotIds);
        gameState.setActiveRobotIds(activeRobotIds);
        gameState.setRobotProgressionStates(saveFile.getRobotProgressionStates());
        ensureRobotProgressionStates();
        restoreRobotState(
            saveFile.getRobotHealth(),
            saveFile.getRobotMaxHealth(),
            saveFile.getRobotBaseMaxHealth(),
            saveFile.getRobotX(),
            saveFile.getRobotY(),
            saveFile.getRobotAngleDeg(),
            saveFile.getRobotAttackTimers()
        );
        syncActiveRobotHealthToProgression();
        restoreEnemyState(saveFile.getEnemies());
        restoreChestState(saveFile.getChests());

        refreshHud();
    }

    private void syncLegendaryRosterToCurrentIds() {
        boolean changed = false;
        for (int i = 0; i < Math.min(LEGENDARY_ROBOT_UNLOCK_IDS.length, LEGACY_LEGENDARY_ROBOT_IDS.length); i++) {
            String currentId = LEGENDARY_ROBOT_UNLOCK_IDS[i];
            String legacyId = LEGACY_LEGENDARY_ROBOT_IDS[i];
            if (collectedRobotIds.remove(legacyId)) {
                changed = true;
            }
            for (int slot = 0; slot < activeRobotIds.size(); slot++) {
                if (legacyId.equals(activeRobotIds.get(slot))) {
                    activeRobotIds.set(slot, currentId);
                    changed = true;
                }
            }
            if (metaProgressionState.getUnlockedLegendaryRobotIds().contains(currentId)
                && !collectedRobotIds.contains(currentId)) {
                collectedRobotIds.add(currentId);
                changed = true;
            }
        }
        if (changed) {
            metaProgressionManager.save(metaProgressionState);
        }
    }

    private void backfillLegacyFrontierAnnexUnlock() {
        if (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")) {
            return;
        }
        if (!gameState.hasDefeatedBoss("rusted_sovereign_c")) {
            return;
        }
        worldStateManager.setFlag(gameState, "frontier.shadow_caves_secured", true);
        applySettlementUpgrade("frontier_annex");
    }

    private void syncAct2TownFacilities() {
        if (getForgeCoreLevel() < 2) {
            return;
        }
        worldStateManager.setFlag(gameState, "event.forge_core_lv2_online", true);
        worldStateManager.setFlag(gameState, "settlement.tavern_open", true);
        worldStateManager.setFlag(gameState, "settlement.hangar_open", true);
        worldStateManager.setFlag(gameState, "settlement.archive_open", true);
        worldStateManager.setFlag(gameState, "settlement.training_grounds_open", true);
    }

    private Map<String, Map<String, String>> copyRobotEquipment(Map<String, Map<String, String>> source) {
        Map<String, Map<String, String>> copy = new HashMap<>();
        if (source == null) {
            return copy;
        }

        for (Map.Entry<String, Map<String, String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), entry.getValue() != null
                ? new HashMap<>(entry.getValue())
                : new HashMap<>());
        }

        return copy;
    }

    private List<SaveFile.EnemyState> buildEnemySaveStates() {
        List<SaveFile.EnemyState> enemyStates = new ArrayList<>();
        for (Enemy enemy : enemies) {
            var transform = enemy.transform();
            var vitals = enemy.vitals();
            var combatStats = enemy.combatStats();
            var motion = enemy.motion();
            SaveFile.EnemyState enemyState = new SaveFile.EnemyState();
            enemyState.setMonsterId(enemy.monsterId);
            enemyState.setX(transform.position.x);
            enemyState.setY(transform.position.y);
            enemyState.setHp(vitals.health);
            enemyState.setMaxHp(vitals.maxHealth);
            enemyState.setSpeed(motion.speed);
            enemyState.setSize(transform.size);
            enemyState.setDefense(combatStats.defense);
            enemyState.setAgility(combatStats.agility);
            enemyState.setStrength(combatStats.strength);
            enemyState.setIntelligence(combatStats.intelligence);
            enemyState.setStamina(combatStats.stamina);
            enemyState.setRewardGold(enemy.rewardGold);
            enemyState.setRewardExperience(enemy.rewardExperience);
            enemyState.setName(enemy.name);
            enemyState.setAlive(vitals.alive);
            enemyState.setAttackTimer(motion.attackTimer);
            enemyState.setPatrolTargetX(motion.patrolTarget.x);
            enemyState.setPatrolTargetY(motion.patrolTarget.y);
            enemyState.setDungeonFloor(enemy.dungeonFloor);
            enemyState.setRaidSpawned(enemy.raidSpawned);
            enemyStates.add(enemyState);
        }
        return enemyStates;
    }

    private List<SaveFile.ChestState> buildChestSaveStates() {
        List<SaveFile.ChestState> chestStates = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : openedChestStates.entrySet()) {
            SaveFile.ChestState chestState = new SaveFile.ChestState();
            String[] parts = entry.getKey().split(":");
            if (parts.length >= 3) {
                chestState.setZoneId(parts[0]);
                chestState.setHouseId(Integer.parseInt(parts[1]));
                chestState.setChestId(parts[2]);
                chestState.setOpened(Boolean.TRUE.equals(entry.getValue()));
                chestStates.add(chestState);
            }
        }
        return chestStates;
    }

    private void startEncounter(Enemy enemy) {
        if (battleActive || enemy == null || !enemy.alive) {
            return;
        }

        battleActive = true;
        screenManager.push(new BattleScreen(game, screenManager, this, buildEncounter(enemy)));
    }

    private BattleScreen.Encounter buildEncounter(Enemy enemy) {
        BattleScreen.Encounter encounter = new BattleScreen.Encounter();
        RobotStatBlock playerStats = getPlayerStats();
        List<Enemy> encounterEnemies = collectEncounterEnemies(enemy);
        encounter.enemyNames = new String[encounterEnemies.size()];
        encounter.enemyIds = new String[encounterEnemies.size()];
        encounter.enemyRanks = new String[encounterEnemies.size()];
        encounter.enemyAiProfiles = new String[encounterEnemies.size()];
        encounter.enemyHealth = new float[encounterEnemies.size()];
        encounter.enemyMaxHealth = new float[encounterEnemies.size()];
        encounter.enemyAgility = new float[encounterEnemies.size()];
        encounter.enemyStrength = new float[encounterEnemies.size()];
        encounter.enemyIntelligence = new float[encounterEnemies.size()];
        encounter.enemyStamina = new float[encounterEnemies.size()];
        encounter.enemyWeaknesses = new String[encounterEnemies.size()][];
        encounter.enemyResistances = new String[encounterEnemies.size()][];
        encounter.enemyAbsorbs = new String[encounterEnemies.size()][];
        encounter.enemyRewardGold = new int[encounterEnemies.size()];
        encounter.enemyExperienceReward = new int[encounterEnemies.size()];
        encounter.enemyReferences = new Object[encounterEnemies.size()];
        for (int i = 0; i < encounterEnemies.size(); i++) {
            Enemy groupEnemy = encounterEnemies.get(i);
            MonsterDefinition definition = groupEnemy.monsterId != null ? monsterDefinitions.get(groupEnemy.monsterId) : null;
            encounter.enemyNames[i] = groupEnemy.name;
            encounter.enemyIds[i] = groupEnemy.monsterId != null ? groupEnemy.monsterId : "enemy_" + i;
            encounter.enemyRanks[i] = definition != null ? definition.getRank() : "G";
            encounter.enemyAiProfiles[i] = definition != null ? definition.getAiProfile() : "PATROL";
            encounter.enemyHealth[i] = groupEnemy.hp;
            encounter.enemyMaxHealth[i] = groupEnemy.maxHp;
            encounter.enemyAgility[i] = groupEnemy.agility;
            encounter.enemyStrength[i] = groupEnemy.strength;
            encounter.enemyIntelligence[i] = groupEnemy.intelligence;
            encounter.enemyStamina[i] = groupEnemy.stamina;
            encounter.enemyWeaknesses[i] = definition != null ? definition.getWeaknesses() : new String[0];
            encounter.enemyResistances[i] = definition != null ? definition.getResistances() : new String[0];
            encounter.enemyAbsorbs[i] = definition != null ? definition.getAbsorbs() : new String[0];
            int experienceReward = groupEnemy.rewardExperience > 0
                ? groupEnemy.rewardExperience
                : (definition != null
                    ? 20 + definition.getBaseLoot()
                    : 25 + Math.round(groupEnemy.strength + groupEnemy.defense));
            encounter.enemyRewardGold[i] = 0;
            encounter.enemyExperienceReward[i] = experienceReward;
            encounter.enemyReferences[i] = groupEnemy;
        }
        encounter.playerName = playerName;
        encounter.playerHealth = playerHealth;
        encounter.playerMaxHealth = playerStats.maxHealth;
        encounter.playerLevel = playerLevel;
        encounter.playerAgility = playerStats.agility;
        encounter.playerStrength = playerStats.strength;
        encounter.playerIntelligence = playerStats.intelligence;
        encounter.playerStamina = playerStats.stamina;
        List<Integer> activeSlots = getActiveRobotSlotIndices();
        encounter.robotNames = new String[activeSlots.size()];
        encounter.robotPartySlots = new int[activeSlots.size()];
        encounter.robotHealth = new float[activeSlots.size()];
        encounter.robotMaxHealth = new float[activeSlots.size()];
        encounter.robotAgility = new float[activeSlots.size()];
        encounter.robotStrength = new float[activeSlots.size()];
        encounter.robotIntelligence = new float[activeSlots.size()];
        encounter.robotStamina = new float[activeSlots.size()];
        for (int i = 0; i < activeSlots.size(); i++) {
            int slotIndex = activeSlots.get(i);
            RobotStatBlock robotStats = getRobotStats(slotIndex);
            encounter.robotNames[i] = getRobotName(slotIndex);
            encounter.robotPartySlots[i] = slotIndex;
            encounter.robotHealth[i] = robots[slotIndex].health;
            encounter.robotMaxHealth[i] = robotStats.maxHealth;
            encounter.robotAgility[i] = robotStats.agility;
            encounter.robotStrength[i] = robotStats.strength;
            encounter.robotIntelligence[i] = robotStats.intelligence;
            encounter.robotStamina[i] = robotStats.stamina;
        }
        encounter.healingPotions = healingPotions;
        return encounter;
    }

    private List<Enemy> collectEncounterEnemies(Enemy primaryEnemy) {
        List<Enemy> group = new ArrayList<>();
        group.add(primaryEnemy);
        List<Enemy> nearby = new ArrayList<>();
        for (Enemy enemy : enemies) {
            if (enemy == primaryEnemy || !enemy.alive) {
                continue;
            }
            nearby.add(enemy);
        }
        nearby.sort((a, b) -> Float.compare(a.pos.dst2(primaryEnemy.pos), b.pos.dst2(primaryEnemy.pos)));
        for (Enemy enemy : nearby) {
            float distance = enemy.pos.dst(primaryEnemy.pos);
            if (distance <= 320f || group.size() == 1) {
                group.add(enemy);
            }
            if (group.size() >= 5) {
                break;
            }
        }
        return group;
    }

    public void resolveBattle(BattleScreen.BattleResult result) {
        battleActive = false;
        playerHealth = Math.max(0f, Math.min(getPlayerStats().maxHealth, result.playerHealth));
        healingPotions = result.healingPotions;
        applyRobotHealth(result.robotHealth, result.robotPartySlots);
        if (result.enemyReferences != null && result.enemyHealth != null) {
            int totalExperienceEarned = 0;
            for (int i = 0; i < result.enemyReferences.length && i < result.enemyHealth.length; i++) {
                Enemy enemy = result.enemyReferences[i] instanceof Enemy ? (Enemy) result.enemyReferences[i] : null;
                if (enemy == null) {
                    continue;
                }
                enemy.hp = Math.max(0f, result.enemyHealth[i]);
                if (result.enemyEscaped) {
                    Vector2 away = new Vector2(enemy.pos).sub(playerPos);
                    if (away.isZero()) {
                        away.set(1f, 0f);
                    }
                    away.nor().scl(180f + (i * 30f));
                    enemy.pos.set(playerPos.x + away.x, playerPos.y + away.y);
                    enemy.patrolTarget = new Vector2(enemy.pos);
                } else if (enemy.hp <= 0f) {
                    totalExperienceEarned += i < result.experienceEarned.length ? result.experienceEarned[i] : 0;
                    onEnemyKilled(enemy);
                }
            }
            if (totalExperienceEarned > 0) {
                addExperience(totalExperienceEarned);
                applyRobotBattleExperience(totalExperienceEarned);
            }
        }
        if (result.updatedBestiary != null) {
            gameState.setBestiaryScanLevels(result.updatedBestiary);
        }
        if (result.droppedShards != null) {
            for (Map.Entry<String, Integer> entry : result.droppedShards.entrySet()) {
                addShardLoot(entry.getKey(), entry.getValue());
            }
        }
        if (result.droppedComponents != null) {
            for (Map.Entry<String, Integer> entry : result.droppedComponents.entrySet()) {
                addForgeComponentLoot(entry.getKey(), entry.getValue());
            }
        }
        Map<String, Integer> blueprintDrops = rollBlueprintFragmentDropsForBattle(result);
        if (!blueprintDrops.isEmpty()) {
            for (Map.Entry<String, Integer> entry : blueprintDrops.entrySet()) {
                addBlueprintFragmentLoot(entry.getKey(), entry.getValue());
            }
        }
        handleBattleStoryEvents(result);
        handleInfiniteDungeonBattleResolution(result);
        if (!pendingRobotAwakeningMessages.isEmpty()) {
            showStandaloneDialog("Awakening", String.join(" ", pendingRobotAwakeningMessages));
            pendingRobotAwakeningMessages.clear();
        }

        if (!hasLivingPartyMember()) {
            if (gameState.isShardRunActive()) {
                shardRunManager.endShardRun(false);
                showStandaloneDialog("Bolt Simulation", "Critical failure. Run terminated. Forge Shards recorded.");
                screenManager.pop();
                return;
            }
            screenManager.pop();
            transitionToGameOver();
            return;
        }

        if (playerHealth <= 0f) {
            playerHealth = 1f;
        }

        refreshHud();
        if (result.enemyDefeated) {
            autosave();
        }

        screenManager.pop();
    }

    private void handleInfiniteDungeonBattleResolution(BattleScreen.BattleResult result) {
        if (!isInfiniteDungeonZone() || result == null || !result.enemyDefeated) {
            return;
        }
        if (!allDungeonEnemiesDefeated()) {
            return;
        }

        int completedFloor = getInfiniteDungeonCurrentFloor();
        gameState.setInfiniteDungeonFloorsCleared(gameState.getInfiniteDungeonFloorsCleared() + 1);
        gameState.setInfiniteDungeonBestFloor(Math.max(gameState.getInfiniteDungeonBestFloor(), completedFloor));
        gameState.setInfiniteDungeonCurrentFloor(completedFloor + 1);
        gameState.setInfiniteDungeonRunActive(true);
        int shardReward = forgeLegacyEngine.getFloorClearReward()
            + (completedFloor % INFINITE_DUNGEON_BOSS_INTERVAL == 0 ? forgeLegacyEngine.getBossFloorReward() : 0);
        if (activeHardModeSeedIndex > 0) {
            shardReward += activeHardModeSeedIndex * 3;
        }
        if (activeChallengeModifierId != null) {
            shardReward += 4;
        }
        if (isBossRushModeActive()) {
            shardReward += 10;
        }
        awardForgeShards(shardReward, "", "");
        handleLargeDungeonExpeditionProgress(completedFloor, false);
        if (isBossRushModeActive() && completedFloor >= 3) {
            worldStateManager.setFlag(gameState, "frontier.boss_rush_cleared", true);
            completePinnedExpeditionContract("Boss rush cleared. The chain collapsed before your strike crew did.");
            handleInfiniteDungeonRunEnd(true);
            loadZone("town", "town_square", true);
            return;
        }
        if (activeChallengeModifierId != null
            && !pinnedExpeditionContractCompleted
            && completedFloor >= Math.max(6, 4 + activeHardModeSeedIndex * 2)) {
            if (activeHardModeSeedIndex > 0) {
                worldStateManager.setFlag(gameState, "frontier.hard_seed_mastered", true);
            }
            completePinnedExpeditionContract("Challenge run certified. The Legacy Vault logged the modifier as a stable build line.");
        }
        syncAct5EndgameState(true);
        regenerateInfiniteDungeonFloor("from_boss_gate", true);
        showStandaloneDialog("Bolt Simulation", completedFloor % INFINITE_DUNGEON_BOSS_INTERVAL == 0
            ? "Boss floor cleared. Routing you deeper into the challenge loop. Forge Shards +" + shardReward + "."
            : "Floor " + completedFloor + " cleared. Preparing the next trial. Forge Shards +" + shardReward + ".");
        refreshHud();
    }

    private boolean allDungeonEnemiesDefeated() {
        if (enemies.isEmpty()) {
            return false;
        }
        for (Enemy enemy : enemies) {
            if (enemy.alive) {
                return false;
            }
        }
        return true;
    }

    private void handleBattleStoryEvents(BattleScreen.BattleResult result) {
        if (result == null || !result.enemyDefeated || result.enemyReferences == null || result.enemyHealth == null) {
            return;
        }
        if (!currentZoneIsSafe()) {
            worldStateManager.setFlag(gameState, "arrival.first_battle_won", true);
        }
        List<String> defeatedEnemyIds = new ArrayList<>();
        for (int i = 0; i < result.enemyReferences.length && i < result.enemyHealth.length; i++) {
            if (result.enemyHealth[i] > 0f) {
                continue;
            }
            Enemy enemy = result.enemyReferences[i] instanceof Enemy ? (Enemy) result.enemyReferences[i] : null;
            if (enemy != null && enemy.monsterId != null && !enemy.monsterId.isEmpty()) {
                defeatedEnemyIds.add(enemy.monsterId);
            }
        }
        boolean milestoneTriggered = false;
        List<String> dynamicWorldReactionLines = new ArrayList<>();
        for (String enemyId : defeatedEnemyIds) {
            if (!isBossMonster(enemyId)) {
                continue;
            }
            if (gameState.markBossDefeated(enemyId)) {
                milestoneTriggered = updateForgeCoreMilestones() || milestoneTriggered;
            }
            handlePinnedContractBossDefeat(enemyId);
            triggerStoryEvents("BOSS_DEFEAT", enemyId);
            applyDynamicWorldEvents(enemyId, dynamicWorldReactionLines);
        }
        questManager.syncProgress(gameState, worldStateManager);
        if (!dynamicWorldReactionLines.isEmpty()) {
            showStandaloneDialog("World Bulletin", String.join(" ", dynamicWorldReactionLines));
        }
        if (milestoneTriggered) {
            refreshHud();
        }
    }

    private Map<String, Integer> rollBlueprintFragmentDropsForBattle(BattleScreen.BattleResult result) {
        Map<String, Integer> drops = new HashMap<>();
        if (result == null || result.enemyReferences == null || result.enemyHealth == null || gameState.getForgeCoreLevel() < 2) {
            return drops;
        }
        for (int i = 0; i < result.enemyReferences.length && i < result.enemyHealth.length; i++) {
            if (result.enemyHealth[i] > 0f) {
                continue;
            }
            Enemy enemy = result.enemyReferences[i] instanceof Enemy ? (Enemy) result.enemyReferences[i] : null;
            if (enemy == null || enemy.monsterId == null || enemy.monsterId.isEmpty()) {
                continue;
            }
            MonsterDefinition monster = monsterDefinitions.get(enemy.monsterId);
            if (monster == null) {
                continue;
            }
            mergeLootMap(drops, rollBlueprintFragmentDropsForEnemy(enemy.monsterId, monster.getRank(), isBossMonster(enemy.monsterId)));
        }
        return drops;
    }

    private Map<String, Integer> rollBlueprintFragmentDropsForEnemy(String monsterId, String rank, boolean bossKill) {
        Map<String, Integer> drops = new HashMap<>();
        if (monsterId == null || monsterId.isEmpty() || rank == null || rank.isEmpty()) {
            return drops;
        }
        String fragmentId = getBlueprintFragmentIdForEnemy(monsterId);
        if (fragmentId == null || fragmentId.isEmpty()) {
            return drops;
        }
        if (bossKill) {
            drops.put(fragmentId, 2);
            return drops;
        }
        if (!isActTwoBlueprintRank(rank) || Math.random() > getBlueprintFragmentDropChance(rank)) {
            return drops;
        }
        drops.put(fragmentId, 1);
        return drops;
    }

    private boolean isActTwoBlueprintRank(String rank) {
        return "C".equals(rank) || "B".equals(rank) || "A".equals(rank) || "S".equals(rank);
    }

    private float getBlueprintFragmentDropChance(String rank) {
        switch (rank) {
            case "S":
                return 0.55f;
            case "A":
                return 0.4f;
            case "B":
                return 0.28f;
            default:
                return 0.18f;
        }
    }

    private String getBlueprintFragmentIdForEnemy(String monsterId) {
        String normalized = monsterId.toLowerCase(Locale.ROOT);
        if (normalized.contains("sovereign") || normalized.contains("forge") || normalized.contains("drake")) {
            return "forge_schema";
        }
        if (normalized.contains("warden") || normalized.contains("sentinel") || normalized.contains("core") || normalized.contains("construct")) {
            return "bot_chassis_schema";
        }
        if (normalized.contains("raider") || normalized.contains("scout") || normalized.contains("marauder") || normalized.contains("specter")) {
            return "settlement_plan";
        }
        switch (currentZoneId) {
            case "shadow_caves":
            case "dragon_peak":
                return "forge_schema";
            case "rusty_quarry":
            case "coastal_shallows":
                return "settlement_plan";
            default:
                return "bot_chassis_schema";
        }
    }

    private void mergeLootMap(Map<String, Integer> target, Map<String, Integer> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            target.put(entry.getKey(), target.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    private void triggerStoryEvents(String triggerType, String triggerId) {
        if (triggerType == null || triggerType.isEmpty() || triggerId == null || triggerId.isEmpty()) {
            return;
        }
        boolean appliedAny = false;
        for (StoryEventDefinition definition : storyEvents) {
            if (definition == null) {
                continue;
            }
            if (!triggerType.equals(definition.getTriggerType()) || !triggerId.equals(definition.getTriggerId())) {
                continue;
            }
            if (applyStoryEvent(definition)) {
                appliedAny = true;
            }
        }
        if (appliedAny) {
            questManager.syncProgress(gameState, worldStateManager);
        }
    }

    private boolean applyStoryEvent(StoryEventDefinition definition) {
        if (definition == null) {
            return false;
        }
        if (definition.getMinimumForgeCoreLevel() > 0 && getForgeCoreLevel() < definition.getMinimumForgeCoreLevel()) {
            return false;
        }
        if (!settlementTimeManager.isWithinPhase(definition.getRequiredTimePhase())) {
            return false;
        }
        String onceFlag = definition.getOnceFlag();
        if (onceFlag != null && !onceFlag.isEmpty() && worldStateManager.isFlagActive(gameState, onceFlag)) {
            return false;
        }
        if (onceFlag != null && !onceFlag.isEmpty()) {
            worldStateManager.setFlag(gameState, onceFlag, true);
        }
        if (definition.getSetWorldFlag() != null && !definition.getSetWorldFlag().isEmpty()) {
            worldStateManager.setFlag(gameState, definition.getSetWorldFlag(), true);
        }
        if (definition.getSetQuestId() != null && !definition.getSetQuestId().isEmpty()) {
            if (QuestManager.NOT_STARTED.equals(questManager.getQuestState(gameState, definition.getSetQuestId()))) {
                questManager.startQuest(gameState, definition.getSetQuestId());
            }
            if (definition.getSetQuestStep() != null && !definition.getSetQuestStep().isEmpty()) {
                questManager.setQuestStep(gameState, definition.getSetQuestId(), definition.getSetQuestStep());
            }
        }
        if (definition.getCompleteQuestId() != null && !definition.getCompleteQuestId().isEmpty()) {
            questManager.completeQuest(gameState, definition.getCompleteQuestId());
        }
        if (definition.getSettlementUpgradeId() != null && !definition.getSettlementUpgradeId().isEmpty()) {
            applySettlementUpgrade(definition.getSettlementUpgradeId());
        }
        if (definition.getAddKeyItem() != null && !definition.getAddKeyItem().isEmpty()) {
            addKeyItem(definition.getAddKeyItem());
        }
        if (definition.getRewardExperience() > 0) {
            addExperience(definition.getRewardExperience());
        }
        if (definition.getSocialFactionId() != null && !definition.getSocialFactionId().isEmpty()
            && definition.getSocialReputationDelta() != 0) {
            adjustFactionInfluence(definition.getSocialFactionId(), definition.getSocialReputationDelta());
        }
        if (definition.getSpeaker() != null && !definition.getSpeaker().isEmpty()) {
            showStandaloneDialog(definition.getSpeaker(), definition.getText());
        } else if (definition.getText() != null && !definition.getText().isEmpty()) {
            showStandaloneDialog("", definition.getText());
        }
        return true;
    }

    public BattleProgressionPreview previewRobotBattleProgression(int experienceEarned) {
        BattleProgressionPreview preview = new BattleProgressionPreview();
        int slotLimit = getPartySlotLimit();
        for (int i = 0; i < activeRobotIds.size() && i < slotLimit; i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            int currentLevel = state.getLevel();
            int currentTier = state.getEvolutionTier();
            int simulatedLevel = currentLevel;
            int simulatedXp = state.getExperience() + Math.max(0, experienceEarned);
            while (simulatedXp >= robotExperienceRequirementForLevel(simulatedLevel)) {
                simulatedXp -= robotExperienceRequirementForLevel(simulatedLevel);
                simulatedLevel++;
            }
            int simulatedTier = currentTier;
            int fcl = getForgeCoreLevel();
            if (simulatedLevel >= 10 && isGradeUnlocked("C") && fcl >= 3) {
                simulatedTier = 3;
            } else if (simulatedLevel >= 5 && isGradeUnlocked("E") && fcl >= 2) {
                simulatedTier = Math.max(simulatedTier, 2);
            }
            if (simulatedLevel > currentLevel) {
                preview.robotProgress.add(getRobotName(i) + " +" + (simulatedLevel - currentLevel) + " level(s)");
            }
            if (simulatedTier > currentTier) {
                // Note: the results screen preview is optimistic — it shows the evolution
                // will happen but doesn't account for material cost availability.
                preview.robotProgress.add(getRobotName(i) + " evolution ready (Tier " + simulatedTier + ")");
            }
        }
        return preview;
    }

    private void applyRobotBattleExperience(int experienceEarned) {
        int slotLimit = getPartySlotLimit();
        for (int i = 0; i < activeRobotIds.size() && i < slotLimit; i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            RobotEvolutionManager.addExperience(state, experienceEarned);
            int tierBefore = state.getEvolutionTier();
            boolean tierChanged = RobotEvolutionManager.applyEvolution(
                state, getUnlockedGrade(), getForgeCoreLevel());
            if (tierChanged) {
                // Try to pay the material cost. If the player can't afford it,
                // revert the tier change — the robot will evolve once materials
                // are gathered and a subsequent battle is won.
                java.util.Map<String, Integer> cost =
                    RobotEvolutionManager.evolutionMaterialCost(state.getEvolutionTier());
                String blueprintFragmentId = RobotEvolutionManager.evolutionBlueprintFragmentId(state.getEvolutionTier());
                int blueprintFragmentCost = RobotEvolutionManager.evolutionBlueprintFragmentCost(state.getEvolutionTier());
                boolean paidComponents = gameState.consumeForgeComponents(cost);
                boolean paidBlueprints = blueprintFragmentCost <= 0
                    || blueprintFragmentId.isEmpty()
                    || gameState.consumeBlueprintFragments(blueprintFragmentId, blueprintFragmentCost);
                if (paidComponents && paidBlueprints) {
                    evolveRobotAtIndex(i, state);
                } else {
                    if (paidComponents) {
                        for (Map.Entry<String, Integer> entry : cost.entrySet()) {
                            gameState.addForgeComponent(entry.getKey(), entry.getValue());
                        }
                    }
                    state.setEvolutionTier(tierBefore);
                }
            }
        }
    }

    public void recordRobotUsage(int partyIndex, String usageKey, int amount) {
        RobotProgressionState state = getRobotProgressionStateForPartyIndex(partyIndex);
        if (state == null) {
            return;
        }
        state.recordUsage(usageKey, amount);
        gameState.putRobotProgressionState(state);
    }

    private void applyRobotHealth(float[] values, int[] slotIndices) {
        if (values == null) {
            return;
        }
        for (int i = 0; i < values.length; i++) {
            int slotIndex = slotIndices != null && i < slotIndices.length ? slotIndices[i] : i;
            if (slotIndex < 0 || slotIndex >= ROBOT_COUNT || !hasActiveRobotAt(slotIndex)) {
                continue;
            }
            robots[slotIndex].health = Math.max(0f, Math.min(getRobotStats(slotIndex).maxHealth, values[i]));
        }
        syncActiveRobotHealthToProgression();
    }

    private void refreshHud() {
        hudOverlay.setPlayerHealth(playerHealth, getPlayerStats().maxHealth);
        hudOverlay.setCurrency(totalGold);
        hudOverlay.setExperience(playerExperience, getExperienceForNextLevel());
        hudOverlay.setRobotHealth(getRobotHealthValues(), getRobotMaxHealthValues());
        hudOverlay.setZoneName(getCurrentZoneDisplayName());
        hudOverlay.setObjectiveText(getDisplayedObjectiveText());
    }

    private String getCurrentZoneDisplayName() {
        String baseName = currentZone != null ? currentZone.displayName : formatZoneName(currentZoneId);
        if (!isInfiniteDungeonZone()) {
            return baseName;
        }
        return baseName + " - Floor " + getInfiniteDungeonCurrentFloor()
            + " (Best " + gameState.getInfiniteDungeonBestFloor() + ")";
    }

    private boolean hasLivingPartyMember() {
        if (playerHealth > 0f) {
            return true;
        }
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (hasActiveRobotAt(i) && robots[i].health > 0f) {
                return true;
            }
        }
        return false;
    }

    private String[] getRobotNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (hasActiveRobotAt(i)) {
                names.add(getRobotName(i));
            }
        }
        return names.toArray(new String[0]);
    }

    private void normalizeActiveRobotSlots() {
        List<String> normalized = new ArrayList<>();
        for (String robotId : activeRobotIds) {
            if (robotId != null && !robotId.isEmpty() && !normalized.contains(robotId)) {
                normalized.add(robotId);
            }
        }
        while (normalized.size() < ROBOT_COUNT) {
            normalized.add(null);
        }
        if (normalized.size() > ROBOT_COUNT) {
            normalized = new ArrayList<>(normalized.subList(0, ROBOT_COUNT));
        }
        activeRobotIds = normalized;
    }

    private void clampActiveRobotsToUnlockedSlots() {
        int slotLimit = getPartySlotLimit();
        for (int i = slotLimit; i < ROBOT_COUNT && i < activeRobotIds.size(); i++) {
            activeRobotIds.set(i, null);
            robots[i].health = 0f;
        }
    }

    private void syncActiveRobotHealthToProgression() {
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (!hasActiveRobotAt(i)) {
                continue;
            }
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            state.setCurrentHealth(Math.max(0f, Math.min(getRobotStats(i).maxHealth, robots[i].health)));
        }
    }

    private float getStoredRobotHealth(String robotId, float fallbackMaxHealth) {
        RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
        if (state == null) {
            return Math.max(1f, fallbackMaxHealth);
        }
        float stored = state.getCurrentHealth();
        if (stored < 0f) {
            stored = fallbackMaxHealth;
        }
        return Math.max(0f, Math.min(fallbackMaxHealth, stored));
    }

    public boolean hasActiveRobotAt(int index) {
        return index >= 0 && index < activeRobotIds.size()
            && activeRobotIds.get(index) != null
            && !activeRobotIds.get(index).isEmpty();
    }

    private List<Integer> getActiveRobotSlotIndices() {
        int limit = getPartySlotLimit();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (hasActiveRobotAt(i)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private String formatZoneName(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return "Unknown Zone";
        }
        String[] words = zoneId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }

    private void loadZoneDefinitions() {
        zoneDefinitions.clear();
        for (ZoneDefinition definition : DefinitionRegistries.ZONES.getAll()) {
            zoneDefinitions.put(definition.getId(), definition);
        }
    }

    private void loadRobotDefinitions() {
        robotDefinitions.clear();
        for (RobotDefinition definition : DefinitionRegistries.ROBOTS.getAll()) {
            if (definition != null && definition.getId() != null) {
                robotDefinitions.put(definition.getId(), definition);
            }
        }
    }

    private void loadMonsterDefinitions() {
        monsterDefinitions.clear();
        for (MonsterDefinition definition : DefinitionRegistries.MONSTERS.getAll()) {
            monsterDefinitions.put(definition.getId(), definition);
        }
    }

    private void loadForgeComponentDefinitions() {
        forgeComponentDefinitions.clear();
        for (ForgeComponentDefinition definition : DefinitionRegistries.FORGE_COMPONENTS.getAll()) {
            if (definition != null && definition.getId() != null) {
                forgeComponentDefinitions.put(definition.getId(), definition);
            }
        }
    }

    private void loadBlueprintFragmentDefinitions() {
        blueprintFragmentDefinitions.clear();
        for (BlueprintFragmentDefinition definition : DefinitionRegistries.BLUEPRINT_FRAGMENTS.getAll()) {
            if (definition != null && definition.getId() != null) {
                blueprintFragmentDefinitions.put(definition.getId(), definition);
            }
        }
    }

    private void loadForgeRecipes() {
        forgeRecipes.clear();
        for (ForgeRecipeDefinition definition : DefinitionRegistries.FORGE_RECIPES.getAll()) {
            if (definition != null && definition.getId() != null) {
                forgeRecipes.add(definition);
            }
        }
    }

    private void loadStoryEvents() {
        storyEvents.clear();
        for (StoryEventDefinition definition : DefinitionRegistries.STORY_EVENTS.getAll()) {
            if (definition != null && definition.getId() != null) {
                storyEvents.add(definition);
            }
        }
    }

    private void loadShopDefinitions() {
        shopDefinitions.clear();
        shopsByZoneId.clear();
        for (ShopDefinition definition : DefinitionRegistries.SHOPS.getAll()) {
            shopDefinitions.put(definition.getId(), definition);
            if (definition != null && definition.getZoneId() != null && !definition.getZoneId().isEmpty()) {
                shopsByZoneId.computeIfAbsent(definition.getZoneId(), ignored -> new ArrayList<>()).add(definition);
            }
        }
    }

    private void loadZone(String zoneId, String spawnId, boolean resetEnemies) {
        ZoneDefinition definition = zoneDefinitions.get(zoneId);
        if (definition == null) {
            return;
        }

        String previousZoneId = currentZoneId;
        currentZoneId = zoneId;
        gameState.setCurrentZoneId(zoneId);
        currentZoneDefinition = definition;
        floatingOriginOffset.setZero();
        disposeCurrentTiledMap();
        ZoneLoader.LoadedZoneContent loadedZone = zoneLoader.load(definition, worldSeed, frontierZoneGenerator, worldGenerator);
        applyLoadedZoneMap(loadedZone);
        currentZone = loadedZone.getZone();
        frontierTerrainSampler = definition.isExpansiveFrontier() ? new FrontierTerrainSampler(worldSeed) : null;
        if (frontierTerrainSampler != null) {
            frontierTerrainSampler.setWorldOriginOffset(floatingOriginOffset.x, floatingOriginOffset.y);
        }
        frontierBiomeCatalog = definition.isExpansiveFrontier() ? new FrontierBiomeCatalog() : null;
        if (definition.isExpansiveFrontier()) {
            hydrateSavedBaseStructures(currentZone, definition.getId());
        }
        handleInfiniteDungeonZoneLoad(previousZoneId, spawnId);
        if (isInfiniteDungeonZone()) {
            currentZone = infiniteDungeonLayoutGenerator.generate(currentZone, getInfiniteDungeonCurrentFloor(), gameState.isShardRunActive());
        }
        localWorldBounds.set(0f, 0f, currentZone.pixelWidth, currentZone.pixelHeight);
        cameraController.setMapBounds(localWorldBounds.x, localWorldBounds.y, localWorldBounds.width, localWorldBounds.height);
        cameraController.resetFloatingOrigin();
        cameraController.syncFloatingOrigin(floatingOriginOffset);
        cameraController.enableFloatingOrigin(definition.isExpansiveFrontier());
        cameraController.configureFloatingOrigin(WORLD_VIEW_WIDTH * 2.5f, WORLD_VIEW_WIDTH);
        if (definition.isExpansiveFrontier()) {
            frontierChunkManager.update(playerPos, currentZone.tileWidth, currentZone.tileHeight);
        }
        addStarterTownUpgradeChest();
        houses.clear();
        npcs.clear();

        for (TmxWorldLoader.Feature feature : currentZone.features) {
            if ("house".equals(feature.kind) && feature.houseId >= 0) {
                House house = createHouseFromFeature(feature);
                if (house != null) {
                    houses.add(house);
                    applyHouseChestState(house);
                }
            }
        }
        addZoneShopFeatureIfNeeded();
        addWarVendorFeatureIfNeeded();

        for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
            if (npcData.hiddenUntilFlag != null && !npcData.hiddenUntilFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, npcData.hiddenUntilFlag)) {
                continue;
            }
            if (npcData.requiredWorldFlag != null && !npcData.requiredWorldFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, npcData.requiredWorldFlag)) {
                continue;
            }
            npcs.add(new NpcEntity(npcData.id, npcData.name, new Vector2(npcData.position), ""));
        }

        addSettlementTownContent();
        pendingNpcScheduleRefresh = true;

        Vector2 resolvedSpawn = resolvePlayerSpawn(spawnId);
        if (resolvedSpawn != null) {
            playerPos.set(resolvedSpawn);
            positionRobotsBehindPlayer();
            cameraController.setTarget(playerPos);
            cameraController.snapToTarget();
        }

        syncCurrentZoneBaseDefenders();

        if (resetEnemies) {
            spawnEnemies();
        }
        triggerStoryEvents("ZONE_ENTER", zoneId);
        maybeShowWarArrivalDispatch(zoneId);
        refreshHud();
        autosave();
    }

    private void applyLoadedZoneMap(ZoneLoader.LoadedZoneContent loadedZone) {
        if (loadedZone == null) {
            return;
        }
        currentTiledMap = loadedZone.getTiledMap();
        currentTiledMapPath = loadedZone.getTilemapPath();
        if (currentTiledMap == null) {
            currentTiledMapPath = null;
            return;
        }
        if (hasRenderableTileLayers(currentTiledMap)) {
            tiledMapRenderer = new OrthogonalTiledMapRenderer(currentTiledMap, 1f);
        } else {
            disposeCurrentTiledMap();
        }
    }

    private boolean hasRenderableTileLayers(TiledMap tiledMap) {
        if (tiledMap == null) {
            return false;
        }
        for (com.badlogic.gdx.maps.MapLayer layer : tiledMap.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                return true;
            }
        }
        return false;
    }

    private void disposeCurrentTiledMap() {
        if (tiledMapRenderer != null) {
            tiledMapRenderer.dispose();
            tiledMapRenderer = null;
        }
        if (currentTiledMap != null) {
            currentTiledMap = null;
        }
        if (currentTiledMapPath != null) {
            zoneLoader.unload(currentTiledMapPath);
            currentTiledMapPath = null;
        }
    }

    private void handleInfiniteDungeonZoneLoad(String previousZoneId, String spawnId) {
        if (!isInfiniteDungeonZone()) {
            return;
        }
        worldStateManager.setFlag(gameState, "meta.shard_run_unlocked", true);
        if (!gameState.isInfiniteDungeonRunActive()) {
            gameState.setInfiniteDungeonCurrentFloor(INFINITE_DUNGEON_START_FLOOR);
            gameState.setInfiniteDungeonRunActive(true);
        } else if (gameState.getInfiniteDungeonCurrentFloor() <= 0) {
            gameState.setInfiniteDungeonCurrentFloor(INFINITE_DUNGEON_START_FLOOR);
        }
        gameState.setInfiniteDungeonBestFloor(Math.max(
            gameState.getInfiniteDungeonBestFloor(),
            gameState.getInfiniteDungeonCurrentFloor()
        ));
        if (!INFINITE_DUNGEON_ZONE_ID.equals(previousZoneId)) {
            showStandaloneDialog("Bolt Simulation", "Challenge loop synchronized. Resuming at floor "
                + gameState.getInfiniteDungeonCurrentFloor() + ".");
        } else if ("from_boss_gate".equals(spawnId)) {
            showStandaloneDialog("Bolt Simulation", "Deep trial gate accepted. Floor "
                + gameState.getInfiniteDungeonCurrentFloor() + " is now active.");
        }
        syncAct5EndgameState(false);
    }

    private void regenerateInfiniteDungeonFloor(String spawnId, boolean resetEnemies) {
        if (!isInfiniteDungeonZone() || currentZoneDefinition == null) {
            return;
        }
        currentZone = worldLoader.load(currentZoneDefinition);
        currentZone = infiniteDungeonLayoutGenerator.generate(currentZone, getInfiniteDungeonCurrentFloor(), gameState.isShardRunActive());
        houses.clear();
        npcs.clear();
        for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
            npcs.add(new NpcEntity(npcData.id, npcData.name, new Vector2(npcData.position), ""));
        }
        Vector2 resolvedSpawn = resolvePlayerSpawn(spawnId);
        if (resolvedSpawn != null) {
            playerPos.set(resolvedSpawn);
            positionRobotsBehindPlayer();
        }
        syncCurrentZoneBaseDefenders();
        if (resetEnemies) {
            spawnEnemies();
        }
        autosave();
    }

    private boolean isInfiniteDungeonZone() {
        return INFINITE_DUNGEON_ZONE_ID.equals(currentZoneId);
    }

    private Vector2 resolvePlayerSpawn(String spawnId) {
        if (currentZone == null || currentZone.playerSpawns == null || currentZone.playerSpawns.isEmpty()) {
            return null;
        }
        if (spawnId != null && currentZone.playerSpawns.containsKey(spawnId)) {
            return currentZone.playerSpawns.get(spawnId);
        }
        if (spawnId != null && !spawnId.isEmpty()) {
            return currentZone.playerSpawns.values().next();
        }
        return null;
    }

    private int getInfiniteDungeonCurrentFloor() {
        int floor = gameState.getInfiniteDungeonCurrentFloor();
        return floor > 0 ? floor : INFINITE_DUNGEON_START_FLOOR;
    }

    private void addStarterTownUpgradeChest() {
        if (currentZone == null || !isHubTownZone()) {
            return;
        }
        if (!"upgrade_workshop".equals(questManager.getQuestState(gameState, "workshop_tools"))) {
            return;
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools")) {
            return;
        }
        for (TmxWorldLoader.ChestData chest : currentZone.chests) {
            if ("starter_workshop_tools".equals(chest.id)) {
                return;
            }
        }

        TmxWorldLoader.ChestData chest = new TmxWorldLoader.ChestData();
        chest.id = "starter_workshop_tools";
        Vector2 spawn = currentZone.playerSpawns.containsKey("town_square")
            ? currentZone.playerSpawns.get("town_square")
            : new Vector2(220f, 220f);
        chest.position = new Vector2(spawn.x + 72f, spawn.y + 28f);
        chest.goldReward = 0;
        chest.potionReward = 0;
        chest.hidden = false;
        chest.message = "You found a sealed crate of restored forge tools. Ironhaven's workshop line can run again.";
        currentZone.chests.add(chest);
    }

    private boolean isHubTownZone() {
        return "town".equals(currentZoneId);
    }

    private void positionRobotsBehindPlayer() {
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (hasActiveRobotAt(i)) {
                robots[i].pos.set(playerPos.x, playerPos.y - ((i + 1) * ROBOT_FOLLOW_GAP));
            }
        }
    }

    private void addSettlementTownContent() {
        if (!isHubTownZone()) {
            return;
        }

        House workshop = findHouseById(0);
        House lodge = findHouseById(1);
        House herbalist = findHouseById(2);

        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools")) {
            npcs.add(new NpcEntity("quartermaster", "Quartermaster",
                workshop != null ? new Vector2(workshop.x + workshop.width + 22f, workshop.y + 22f) : new Vector2(280f, 210f),
                "The forge rail is live again. Toma's workshop can finally stock heavy chassis parts."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.watchtower_network")) {
            npcs.add(new NpcEntity("lookout", "Lookout",
                lodge != null ? new Vector2(lodge.x + lodge.width + 18f, lodge.y + lodge.height - 6f) : new Vector2(190f, 270f),
                "The watchtower lamps are sweeping farther every night. Routes that used to vanish now stay marked."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.survey_drones")) {
            npcs.add(new NpcEntity("dispatcher", "Dispatcher",
                workshop != null ? new Vector2(workshop.x - 18f, workshop.y + workshop.height - 4f) : new Vector2(235f, 250f),
                "Survey drones are airborne again. Bring me fresh route intel and I'll keep Ironhaven's map board honest."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")) {
            if (!hasTownNpc("hale")) {
                npcs.add(new NpcEntity("hale", "Hale",
                    new Vector2(currentZone != null ? currentZone.pixelWidth - 620f : 1298f, 638f),
                    "The annex is hungry for fresh salvage. Bring a live haul home and we keep the east line moving."));
            }
            if (!hasTownNpc("vesa")) {
                npcs.add(new NpcEntity("vesa", "Vesa",
                    new Vector2(currentZone != null ? currentZone.pixelWidth - 492f : 1428f, 580f),
                    "Every crate that clears this yard means another crew can push deeper tomorrow."));
            }
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.tavern_open") && !hasTownNpc("innkeeper_tamsin")) {
            npcs.add(new NpcEntity("innkeeper_tamsin", "Tamsin", new Vector2(162f, 494f),
                "If Ironhaven is going to grow, it needs a room where crews can trade rumors before the next push."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.hangar_open") && !hasTownNpc("hangar_keeper")) {
            npcs.add(new NpcEntity("hangar_keeper", "Hangar Keeper", new Vector2(334f, 350f),
                "Reserve frames, spare chassis, field swaps. The hangar keeps your roster ready for the next climb."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open") && !hasTownNpc("commander_rex")) {
            npcs.add(new NpcEntity("commander_rex", "Commander Rex", new Vector2(612f, 348f),
                "A stronger team starts with disciplined drills and clean command lines."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.archive_open") && !hasTownNpc("professor_cogs")) {
            npcs.add(new NpcEntity("professor_cogs", "Professor Cogs", new Vector2(944f, 344f),
                "The archive remembers what the field forgets."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools") && !hasTownNpc("master_silas")) {
            npcs.add(new NpcEntity("master_silas", "Master Silas",
                workshop != null ? new Vector2(workshop.x + workshop.width + 84f, workshop.y + 64f) : new Vector2(430f, 232f),
                "The workshop is awake. Now we see whether your salvage deserves the fire."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock") && !hasTownNpc("elena_apothecary")) {
            npcs.add(new NpcEntity("elena_apothecary", "Elena",
                herbalist != null ? new Vector2(herbalist.x + 92f, herbalist.y - 28f) : new Vector2(1028f, 226f),
                "Every deep push is paid for twice: once in steel, once in recovery."));
        }
        if (worldStateManager.isFlagActive(gameState, "meta.shard_run_unlocked") && !hasTownNpc("coda")) {
            npcs.add(new NpcEntity("coda", "Coda", new Vector2(742f, 418f),
                "The Legacy Vault records every collapse and every breakthrough. Bring me shards and I'll turn them into permanence."));
        }
        addPlayerCreatedTownNpcs();
        applyTownNpcSchedules();
    }

    private void addPlayerCreatedTownNpcs() {
        if (!isHubTownZone()) {
            return;
        }
        float baseX = 708f;
        float baseY = 298f;
        for (int i = 0; i < playerCreatedNpcs.size(); i++) {
            PlayerCreatedNpc createdNpc = playerCreatedNpcs.get(i);
            if (createdNpc == null || createdNpc.npcId == null || createdNpc.npcId.isEmpty() || hasTownNpc(createdNpc.npcId)) {
                continue;
            }
            npcs.add(new NpcEntity(
                createdNpc.npcId,
                createdNpc.name,
                new Vector2(baseX + (i % 3) * 86f, baseY - (i / 3) * 62f),
                createdNpc.dialog
            ));
        }
        applyTownNpcSchedules();
    }

    private void applyTownNpcSchedules() {
        if (!isHubTownZone()) {
            return;
        }
        for (NpcEntity npc : npcs) {
            if (npc == null) {
                continue;
            }
            SettlementNpcScheduleDefinition schedule = settlementManager.getNpcSchedule(npc.id);
            if (schedule != null) {
                Vector2 base = new Vector2(npc.spawnPos);
                npc.setSchedule(
                    new Vector2(base.x + schedule.getHomeOffsetX(), base.y + schedule.getHomeOffsetY()),
                    new Vector2(base.x + schedule.getHomeOffsetX(), base.y + schedule.getHomeOffsetY()),
                    new Vector2(base.x + schedule.getDayOffsetX(), base.y + schedule.getDayOffsetY()),
                    new Vector2(base.x + schedule.getEveningOffsetX(), base.y + schedule.getEveningOffsetY()),
                    new Vector2(base.x + schedule.getNightOffsetX(), base.y + schedule.getNightOffsetY()),
                    schedule.getMorningActivity(),
                    schedule.getDayActivity(),
                    schedule.getEveningActivity(),
                    schedule.getNightActivity()
                );
            } else {
                npc.setSchedule(new Vector2(npc.spawnPos), new Vector2(npc.spawnPos), new Vector2(npc.spawnPos),
                    new Vector2(npc.spawnPos), new Vector2(npc.spawnPos),
                    "Opening up", "On duty", "Winding down", "Resting");
            }
        }
    }

    private boolean hasTownNpc(String npcId) {
        if (npcId == null || npcId.isEmpty()) {
            return false;
        }
        for (NpcEntity npc : npcs) {
            if (npcId.equals(npc.id)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onNpcScheduleEvent(NpcScheduleEvent event) {
        if (event == null || !isHubTownZone()) {
            return;
        }
        if (event.isPhaseChanged()) {
            pendingNpcScheduleRefresh = true;
        }
    }

    private void updateNpcSchedules(float delta) {
        if (!isHubTownZone() || npcs.isEmpty()) {
            return;
        }
        if (pendingNpcScheduleRefresh) {
            applyTownNpcSchedules();
            pendingNpcScheduleRefresh = false;
        }
        for (NpcEntity npc : npcs) {
            if (npc == null) {
                continue;
            }
            Vector2 scheduleTarget = npc.getScheduledPosition(settlementTimeManager.getTimeOfDayHours());
            if (scheduleTarget == null) {
                continue;
            }
            Vector2 deltaToTarget = new Vector2(scheduleTarget).sub(npc.pos);
            if (deltaToTarget.len2() < 4f) {
                npc.pos.set(scheduleTarget);
                continue;
            }
            deltaToTarget.nor().scl(Math.min(deltaToTarget.len(), 46f * delta));
            npc.pos.add(deltaToTarget);
        }
    }

    private boolean shouldUseFrontierStreaming() {
        return currentZoneDefinition != null
            && currentZoneDefinition.isExpansiveFrontier()
            && currentZone != null
            && currentZone.tileWidth > 0
            && currentZone.tileHeight > 0;
    }

    private boolean isWithinActiveFrontierWorld(Vector2 position) {
        if (!shouldUseFrontierStreaming() || position == null) {
            return true;
        }
        return frontierChunkManager.isActiveWorldPosition(position.x, position.y, currentZone.tileWidth, currentZone.tileHeight);
    }

    private int toAbsoluteTileX(int localTileX, int tileWidth) {
        return Math.max(0, (int) Math.floor((localTileX * Math.max(1, tileWidth) + floatingOriginOffset.x) / Math.max(1, tileWidth)));
    }

    private int toAbsoluteTileY(int localTileY, int tileHeight) {
        return Math.max(0, (int) Math.floor((localTileY * Math.max(1, tileHeight) + floatingOriginOffset.y) / Math.max(1, tileHeight)));
    }

    private void applyFloatingOriginShift(Vector2 shift) {
        if (shift == null || shift.isZero()) {
            return;
        }
        floatingOriginOffset.add(shift);
        if (frontierTerrainSampler != null) {
            frontierTerrainSampler.setWorldOriginOffset(floatingOriginOffset.x, floatingOriginOffset.y);
        }
        cameraController.syncFloatingOrigin(floatingOriginOffset);
        shiftVector(playerPos, shift);
        for (RobotCompanion robot : robots) {
            if (robot != null) {
                shiftVector(robot.pos, shift);
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy == null) {
                continue;
            }
            shiftVector(enemy.pos, shift);
            shiftVector(enemy.patrolTarget, shift);
        }
        for (NpcEntity npc : npcs) {
            if (npc == null) {
                continue;
            }
            npc.applyFloatingOriginShift(shift);
        }
        if (currentZone != null) {
            if (currentZone.safeCenter != null) {
                shiftVector(currentZone.safeCenter, shift);
            }
            for (Rectangle collision : currentZone.collisions) {
                collision.x -= shift.x;
                collision.y -= shift.y;
            }
            for (TmxWorldLoader.Door door : currentZone.doors) {
                if (door != null && door.bounds != null) {
                    door.bounds.x -= shift.x;
                    door.bounds.y -= shift.y;
                }
            }
            for (Vector2 spawn : currentZone.playerSpawns.values()) {
                shiftVector(spawn, shift);
            }
            for (Vector2 spawn : currentZone.enemySpawns) {
                shiftVector(spawn, shift);
            }
            for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
                if (npcData != null) {
                    shiftVector(npcData.position, shift);
                }
            }
            for (TmxWorldLoader.Feature feature : currentZone.features) {
                if (feature != null && feature.bounds != null) {
                    feature.bounds.x -= shift.x;
                    feature.bounds.y -= shift.y;
                }
            }
            for (TmxWorldLoader.ChestData chest : currentZone.chests) {
                if (chest != null) {
                    shiftVector(chest.position, shift);
                }
            }
        }
        for (House house : houses) {
            if (house == null) {
                continue;
            }
            house.x -= shift.x;
            house.y -= shift.y;
        }
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender != null) {
                shiftVector(defender.getPosition(), shift);
                shiftVector(defender.getGuardPosition(), shift);
                shiftVector(defender.getPatrolPosition(), shift);
            }
        }
        localWorldBounds.x -= shift.x;
        localWorldBounds.y -= shift.y;
        cameraController.setMapBounds(localWorldBounds.x, localWorldBounds.y, localWorldBounds.width, localWorldBounds.height);
        shiftCurrentBaseStateStructures(shift);
    }

    private void shiftCurrentBaseStateStructures(Vector2 shift) {
        BaseState baseState = getCurrentBaseState();
        if (baseState != null) {
            baseState.shiftWorld(shift.x, shift.y);
        }
    }

    private void shiftVector(Vector2 value, Vector2 shift) {
        if (value != null) {
            value.sub(shift);
        }
    }

    private House createHouseFromFeature(TmxWorldLoader.Feature feature) {
        House house;
        if (feature.houseId == 0) {
            house = House.createWorkshop(feature.houseId, feature.label, feature.bounds.x, feature.bounds.y, feature.bounds.width, feature.bounds.height);
        } else if (feature.houseId == 1) {
            house = House.createLodge(feature.houseId, feature.label, feature.bounds.x, feature.bounds.y, feature.bounds.width, feature.bounds.height);
        } else if (feature.houseId == 2) {
            house = House.createHerbalist(feature.houseId, feature.label, feature.bounds.x, feature.bounds.y, feature.bounds.width, feature.bounds.height);
        } else if (feature.houseId == 3) {
            house = House.createPlayerHome(feature.houseId, feature.label, feature.bounds.x, feature.bounds.y, feature.bounds.width, feature.bounds.height);
        } else {
            return null;
        }
        house.zoneId = currentZoneId;
        return house;
    }

    private void triggerOpeningCutsceneIfNeeded() {
        if (!pendingOpeningCutscene) {
            return;
        }
        pendingOpeningCutscene = false;
        House playerHome = findHouseById(3);
        if (playerHome == null || worldStateManager.isFlagActive(gameState, OPENING_HOME_INTRO_FLAG)) {
            return;
        }
        worldStateManager.setFlag(gameState, OPENING_HOME_INTRO_FLAG, true);
        activeSpeaker = null;
        activeDialog = null;
        screenManager.push(new CutsceneScreen(
            game,
            screenManager,
            buildOpeningCutscenePages(),
            () -> screenManager.push(new HouseInteriorScreen(game, screenManager, this, playerHome))
        ));
        autosave();
    }

    private List<CutsceneScreen.Page> buildOpeningCutscenePages() {
        List<CutsceneScreen.Page> pages = new ArrayList<>();
        pages.add(new CutsceneScreen.Page(
            "Mechara",
            "The old world did not end all at once. It collapsed in fires, failing machines, and broken defenses until only scattered settlements remained behind whatever walls they could keep powered."
        ));
        pages.add(new CutsceneScreen.Page(
            "Forge Core Archive",
            "Ironhaven is one of those settlements. Its people survive by salvaging what the frontier leaves behind, rebuilding their crews, and sending the strong to hold back what still prowls beyond the gates."
        ));
        pages.add(new CutsceneScreen.Page(
            "Morning Brief",
            "You wake in your own home to the low hum of the Forge Core. Bram has already sent word: he wants to speak with you at once. Something in the frontier is changing, and Ironhaven needs someone ready to grow strong enough to defend what is left."
        ));
        return pages;
    }

    public boolean isWorldFlagActive(String flag) {
        return flag != null && !flag.isEmpty() && worldStateManager.isFlagActive(gameState, flag);
    }

    private void applyHouseChestState(House house) {
        for (Chest chest : house.chests) {
            chest.opened = isChestOpened(house.zoneId, house.id, chest.id);
        }
    }

    private String chestStateKey(String zoneId, int houseId, String chestId) {
        return zoneId + ":" + houseId + ":" + chestId;
    }

    private boolean isChestOpened(String zoneId, int houseId, String chestId) {
        return Boolean.TRUE.equals(openedChestStates.get(chestStateKey(zoneId, houseId, chestId)));
    }

    public void markChestOpened(String zoneId, int houseId, String chestId) {
        openedChestStates.put(chestStateKey(zoneId, houseId, chestId), true);
    }

    public void onInteriorChestOpened(String chestId) {
        if (chestId == null || chestId.isEmpty()) {
            return;
        }
        if ("herbalist_hidden".equals(chestId)) {
            worldStateManager.setFlag(gameState, "chest.herbalist_hidden.opened", true);
            worldStateManager.setFlag(gameState, "recruit.medic_frame_found", true);
        }
        questManager.syncProgress(gameState, worldStateManager);
        refreshHud();
    }

    private boolean isProgressFlagActive(String flag) {
        if (flag == null || flag.isEmpty()) {
            return false;
        }
        return worldStateManager.isFlagActive(gameState, flag);
    }

    private boolean hasKeyItem(String keyItem) {
        return gameState.hasKeyItem(keyItem);
    }

    private boolean hasWorkshopPassAccess() {
        return hasKeyItem("workshop_pass") || worldStateManager.isFlagActive(gameState, "access.workshop_pass");
    }

    private void addKeyItem(String keyItem) {
        gameState.addKeyItem(keyItem);
        if (keyItem != null && !keyItem.isEmpty() && !keyItems.contains(keyItem)) {
            keyItems.add(keyItem);
        }
    }

    private String getCurrentObjective() {
        questManager.syncProgress(gameState, worldStateManager);
        return questManager.getCurrentObjective(gameState);
    }

    private String getDisplayedObjectiveText() {
        String questObjective = getCurrentObjective();
        if (pinnedExpeditionContractText == null || pinnedExpeditionContractText.isEmpty()) {
            return questObjective;
        }
        String contractPrefix = pinnedExpeditionContractCompleted ? "Contract complete: " : "Contract: ";
        if (questObjective == null || questObjective.isEmpty() || questObjective.equals(pinnedExpeditionContractText)) {
            return contractPrefix + pinnedExpeditionContractText;
        }
        return contractPrefix + pinnedExpeditionContractText + "  |  Quest: " + questObjective;
    }

    private DialogueSystem.DialogueResult resolveNpcDialogue(String npcId, String speakerName) {
        questManager.syncProgress(gameState, worldStateManager);
        DialogueSystem.DialogueResult result = buildActFourTownDialogue(npcId, speakerName);
        if (result == null) {
            result = dialogueSystem.resolve(
                npcId,
                currentZoneId,
                gameState,
                questManager,
                worldStateManager
            );
        }
        if (result == null) {
            result = new DialogueSystem.DialogueResult();
            result.speaker = speakerName;
            result.text = "The road keeps changing, but a good crew still finds its way.";
            result.pages.add(new DialogueSystem.DialoguePage(result.speaker, result.text));
        }
        applyDialogueResult(result);
        questManager.recordNpcConversation(gameState, worldStateManager, currentZoneId, npcId);
        questManager.syncProgress(gameState, worldStateManager);
        refreshHud();
        return result;
    }

    private DialogueSystem.DialogueResult buildActFourTownDialogue(String npcId, String speakerName) {
        if (!isHubTownZone() || !warPhaseManager.isWarPhaseUnlocked(gameState) || npcId == null || npcId.isEmpty()) {
            return null;
        }
        List<DialogueSystem.DialoguePage> pages = new ArrayList<>();
        if ("commander_rex".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourCommanderRexLine()));
            pages.add(new DialogueSystem.DialoguePage(speakerName, "Keep reading the war tab like a field map. If the chronicle is stalled, push the exact theater it names."));
        } else if ("professor_cogs".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourProfessorCogsLine()));
            pages.add(new DialogueSystem.DialoguePage(speakerName, "The frontier isn't empty ruin anymore. It's a library written in raids, shards, and the parts you choose to save."));
        } else if ("hale".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourAnnexLine()));
        } else if ("vesa".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourVesaLine()));
        } else if ("innkeeper_tamsin".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourTamsinLine()));
        } else if ("quartermaster".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourQuartermasterLine()));
        } else if ("dispatcher".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourDispatcherLine()));
        } else if ("lookout".equals(npcId)) {
            pages.add(new DialogueSystem.DialoguePage(speakerName, getActFourLookoutLine()));
        } else {
            return null;
        }
        DialogueSystem.DialogueResult result = new DialogueSystem.DialogueResult();
        result.speaker = speakerName;
        result.pages.addAll(pages);
        result.text = pages.isEmpty() ? "The frontier keeps moving." : pages.get(0).text;
        return result;
    }

    private String getActFourCommanderRexLine() {
        if (!isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            return "Iron Lifeline is the whole war in miniature. If we can't feed multiple fronts, none of the harder operations matter.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            return "Gloam Archive is next. Pull the dusk-memory out of those ruins before the hostiles turn ignorance into territory.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            return "Hellclimb is where the map judges us. Take Sky Fortress and every crew in Ironhaven starts believing the hard route is real.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            return "Last Light is a command proof. Surface control means nothing if we fold the moment the descent goes deep and ugly.";
        }
        return "You're not chasing stability anymore. You're setting campaign tempo for the entire frontier.";
    }

    private String getActFourProfessorCogsLine() {
        if (!isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            return "The Shadow Caves and Crystal Depths still remember the age that died here. Bring me fragments before the ruin forgets on purpose.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            return "Archive doctrine says the next truth is below us. The abyss is where a rebuilt world proves whether it's real or just loud.";
        }
        return "We've crossed from archaeology into authorship. The archive is recording a new era because you forced one into existence.";
    }

    private String getActFourAnnexLine() {
        if (!isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            return "We're still on emergency routing. Every convoy, delivery, and supply charter you clear keeps another district from going dark.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            return "The annex can feed the low routes now. What we need from you is confidence at altitude.";
        }
        return "Traffic's cleaner than it's ever been. Hard to believe this yard used to feel like the edge of the world.";
    }

    private String getActFourVesaLine() {
        if (!isActFourOperationCompleted(ACT4_OPERATION_GLOAM_ARCHIVE)) {
            return "Couriers coming back from the dusk bands all say the same thing: the ruins feel like they're waiting to be chosen. Choose faster than the scavengers.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            return "The crews are placing bets on your next descent floor now. That's morale, in Ironhaven terms.";
        }
        return "We've gone from scraping by to scheduling futures. I don't think the town knows what to do with hope yet.";
    }

    private String getActFourTamsinLine() {
        if (!isActFourOperationCompleted(ACT4_OPERATION_IRON_LIFELINE)) {
            return "The tavern's all route maps and emergency drinks tonight. Nobody relaxes until the lifeline sticks.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_HELLCLIMB)) {
            return "After Gloam Archive, the crews started talking like the dead world can be understood. After Hellclimb, they'll talk like it can be beaten.";
        }
        return "Half this room is writing charters now. That's how I know you've changed the place.";
    }

    private String getActFourQuartermasterLine() {
        if (isActFourSideArcCompleted(ACT4_SIDEARC_ARCHIVE_RECLAMATION)
            && isActFourSideArcCompleted(ACT4_SIDEARC_GUILD_ASCENDANCY)
            && isActFourSideArcCompleted(ACT4_SIDEARC_COMMAND_BASTION)) {
            return "All three side programs are funded and stable. That's not a logistics state, it's an empire state.";
        }
        if (!isActFourOperationCompleted(ACT4_OPERATION_LAST_LIGHT)) {
            return "Command packages are already folded into your operation clears. Keep the chronicle moving and the quartermaster keeps signing off on miracle budgets.";
        }
        return "You're on victory-grade logistics now. If a crew wants to model a charter after your campaign, we can actually fund it.";
    }

    private String getActFourDispatcherLine() {
        String zoneId = getPrimaryWarDispatchZone();
        if (zoneId == null) {
            return "Traffic board is steady for once. I'm suspicious of that, but I'll take it.";
        }
        if (worldStateManager.isFlagActive(gameState, "war.consequence.chain." + zoneId)
            || worldStateManager.isFlagActive(gameState, "war.consequence.spillover." + zoneId)) {
            return "We've got spillover on the board. Clear one hotspot too slowly and the neighboring lane starts screaming for help instead.";
        }
        String incident = getRegionalIncidentName(zoneId);
        if (incident != null) {
            return "Priority dispatch is " + incident + " in " + getZoneDisplayName(zoneId)
                + ". Every route note coming in says the same thing: get there fast or lose the window.";
        }
        String crisis = getSettlementCrisisName(zoneId);
        if (crisis != null) {
            return "Priority dispatch is " + crisis + " at " + getZoneDisplayName(zoneId)
                + ". Logistics wants one clean contract there before the line tears wider open.";
        }
        return "No single route owns the board right now. That's usually when the frontier starts preparing a surprise.";
    }

    private String getActFourLookoutLine() {
        String zoneId = getPrimaryWarDispatchZone();
        if (zoneId == null) {
            return "Signal lamps are calm. The frontier only stays this quiet when it's catching its breath.";
        }
        if (worldStateManager.isFlagActive(gameState, "war.consequence.relief." + zoneId)
            || worldStateManager.isFlagActive(gameState, "war.consequence.stabilized." + zoneId)) {
            return "The neighboring lamps settled after the last push. That's the kind of calm you earn, not the kind you wait for.";
        }
        String incident = getRegionalIncidentName(zoneId);
        if (incident != null) {
            return "I'm watching " + getZoneDisplayName(zoneId) + " all night. " + incident
                + " is throwing strange light off the horizon and the crews can feel it even from town.";
        }
        String crisis = getSettlementCrisisName(zoneId);
        if (crisis != null) {
            return getZoneDisplayName(zoneId) + " is flashing irregular support codes. " + crisis
                + " means that outpost needs relief before the next pressure wave lands.";
        }
        return "Watch line is clear enough to plan around. That's your chance to make the next move first.";
    }

    private String getPrimaryWarDispatchZone() {
        if (!activeSettlementCrisesByZoneId.isEmpty()) {
            return activeSettlementCrisesByZoneId.keySet().iterator().next();
        }
        if (!activeRegionalIncidentsByZoneId.isEmpty()) {
            return activeRegionalIncidentsByZoneId.keySet().iterator().next();
        }
        if (!activeWorldBossFrontsByZoneId.isEmpty()) {
            return activeWorldBossFrontsByZoneId.keySet().iterator().next();
        }
        return null;
    }

    private void applyDialogueResult(DialogueSystem.DialogueResult result) {
        if (result == null) {
            return;
        }
        boolean settlementUpgradeApplied = true;
        if (result.setQuestId != null && !result.setQuestId.isEmpty()) {
            if ("NOT_STARTED".equals(questManager.getQuestState(gameState, result.setQuestId))) {
                questManager.startQuest(gameState, result.setQuestId);
            }
            if (result.setQuestStep != null && !result.setQuestStep.isEmpty()) {
                questManager.setQuestStep(gameState, result.setQuestId, result.setQuestStep);
            }
        }
        if (result.completeQuestId != null && !result.completeQuestId.isEmpty()) {
            questManager.completeQuest(gameState, result.completeQuestId);
        }
        if (result.addKeyItem != null && !result.addKeyItem.isEmpty()) {
            addKeyItem(result.addKeyItem);
        }
        if (result.settlementUpgradeId != null && !result.settlementUpgradeId.isEmpty()) {
            settlementUpgradeApplied = applySettlementUpgrade(result.settlementUpgradeId);
        }
        if (settlementUpgradeApplied && result.setWorldFlag != null && !result.setWorldFlag.isEmpty()) {
            worldStateManager.setFlag(gameState, result.setWorldFlag, true);
        }
        if (result.recruitEventId != null && !result.recruitEventId.isEmpty()) {
            applyRecruitment(result.recruitEventId);
        }
        questManager.syncProgress(gameState, worldStateManager);
        if (isHubTownZone()) {
            addStarterTownUpgradeChest();
        }
    }

    private void applyRecruitment(String eventId) {
        List<String> collectedBefore = new ArrayList<>(collectedRobotIds);
        if (hasTownServiceStrain() && eventId != null && !eventId.isEmpty()
            && eventId.startsWith("recruit_") && !worldStateManager.isFlagActive(gameState, "war.recruitment_strain_warning")) {
            worldStateManager.setFlag(gameState, "war.recruitment_strain_warning", true);
            activeDialog = "Ironhaven's recruitment net is under strain from chained frontier crises. New frames can still be stabilized, but support crews are stretched thin.";
            dialogPageTrackingText = null;
        }
        RobotRecruitmentManager.RecruitmentResult recruitment = recruitmentManager.apply(eventId, collectedRobotIds, activeRobotIds, gameState);
        if (recruitment == null) {
            return;
        }
        if (recruitment.blocked) {
            String fragmentName = getBlueprintFragmentName(recruitment.requiredBlueprintFragmentId);
            int current = gameState.getBlueprintFragmentCount(recruitment.requiredBlueprintFragmentId);
            activeDialog = "That frame needs " + recruitment.requiredBlueprintFragmentCount + " " + fragmentName
                + " before Ironhaven can stabilize it. You currently have " + current + ".";
            dialogPageTrackingText = null;
            return;
        }
        gameState.setCollectedRobotIds(collectedRobotIds);
        gameState.setActiveRobotIds(activeRobotIds);
        ensureRobotProgressionStates();
        if (recruitment.joinedWorldFlag != null && !recruitment.joinedWorldFlag.isEmpty()) {
            worldStateManager.setFlag(gameState, recruitment.joinedWorldFlag, true);
        }
        if (recruitment.message != null && !recruitment.message.isEmpty()) {
            activeDialog = activeDialog != null && !activeDialog.isEmpty()
                ? activeDialog + " " + recruitment.message
                : recruitment.message;
            dialogPageTrackingText = null;
        }
        applyWarRecruitmentBonus(collectedBefore);
    }

    private void applyWarRecruitmentBonus(List<String> collectedBefore) {
        if (!warPhaseManager.isWarPhaseUnlocked(gameState) || !hasTownServiceSurplus()) {
            return;
        }
        List<String> previous = collectedBefore != null ? collectedBefore : List.of();
        String dispatchZoneId = getPrimaryWarDispatchZone();
        int bonusXp = getWarRecruitmentBonusXp(dispatchZoneId);
        if (bonusXp <= 0) {
            return;
        }
        for (String robotId : collectedRobotIds) {
            if (robotId == null || robotId.isEmpty() || previous.contains(robotId)) {
                continue;
            }
            RobotProgressionState state = gameState.getRobotProgressionState(robotId);
            if (state == null) {
                continue;
            }
            RobotEvolutionManager.addExperience(state, bonusXp);
            if ("sky_fortress".equals(dispatchZoneId)) {
                state.setLevel(state.getLevel() + 1);
            }
            gameState.putRobotProgressionState(state);
            String dispatchLabel = dispatchZoneId != null ? getZoneDisplayName(dispatchZoneId) : "the frontier";
            String specialty = getWarRecruitmentSpecialty(dispatchZoneId);
            activeDialog = (activeDialog != null && !activeDialog.isEmpty() ? activeDialog + " " : "")
                + state.getDisplayName() + " joined with " + dispatchLabel + " war-drill experience. "
                + specialty + " Bonus training XP +" + bonusXp
                + ("sky_fortress".equals(dispatchZoneId) ? ", bonus level +1." : ".");
            dialogPageTrackingText = null;
        }
    }

    private String getWarRecruitmentSpecialty(String dispatchZoneId) {
        if ("sky_fortress".equals(dispatchZoneId)) {
            return "Specialty: summit assault drills are already baked into its chassis prep.";
        }
        if ("crystal_depths".equals(dispatchZoneId)) {
            return "Specialty: crystal-shelf hazard conditioning improved its field stability.";
        }
        if ("shadow_caves".equals(dispatchZoneId)) {
            return "Specialty: low-visibility ghostline routing sharpened its recovery discipline.";
        }
        if ("rusty_quarry".equals(dispatchZoneId)) {
            return "Specialty: extraction-line pressure work hardened its endurance profile.";
        }
        if ("verdant_fields".equals(dispatchZoneId)) {
            return "Specialty: corridor escort training improved its early-war readiness.";
        }
        return "Specialty: frontier readiness package applied.";
    }

    private int getWarRecruitmentBonusXp(String dispatchZoneId) {
        if (dispatchZoneId == null || dispatchZoneId.isEmpty()) {
            return 18;
        }
        if ("sky_fortress".equals(dispatchZoneId)) {
            return 34;
        }
        if ("crystal_depths".equals(dispatchZoneId) || "shadow_caves".equals(dispatchZoneId)) {
            return 28;
        }
        if ("rusty_quarry".equals(dispatchZoneId) || "verdant_fields".equals(dispatchZoneId)) {
            return 22;
        }
        return 18;
    }

    private boolean applySettlementUpgrade(String upgradeId) {
        if (upgradeId == null || upgradeId.isEmpty()) {
            return false;
        }
        SettlementManager settlement = settlementManager;
        SettlementUpgradeDefinition definition = settlement.get(upgradeId);
        if (definition != null && !canAffordSettlementUpgrade(definition)) {
            String fragmentName = getBlueprintFragmentName(definition.getRequiredBlueprintFragmentId());
            int current = gameState.getBlueprintFragmentCount(definition.getRequiredBlueprintFragmentId());
            activeDialog = "Ironhaven needs " + definition.getRequiredBlueprintFragmentCount() + " " + fragmentName
                + " to finish " + definition.getName() + ". You currently have " + current + ".";
            activeDialogueSequence.clear();
            activeDialogueSequence.add(new DialogueSystem.DialoguePage(activeSpeaker != null ? activeSpeaker : "Ironhaven", activeDialog));
            activeDialogueSequenceIndex = 0;
            dialogPageIndex = 0;
            dialogPageTrackingText = null;
            return false;
        }
        if (definition != null) {
            spendSettlementUpgradeBlueprintCost(definition);
        }
        SettlementState state = gameState.getSettlementUpgrade(upgradeId);
        if (state == null) {
            state = new SettlementState(upgradeId, 0);
        }
        state.setLevel(state.getLevel() + 1);
        gameState.putSettlementUpgrade(state);
        if (definition != null) {
            if (definition.getRewardEquipmentId() != null
                && !definition.getRewardEquipmentId().isEmpty()) {
                unlockEquipment(definition.getRewardEquipmentId());
            }
            if (definition.getRewardPotions() > 0) {
                addHealingPotions(definition.getRewardPotions());
            }
        }
        worldStateManager.setFlag(gameState, "settlement." + upgradeId, true);
        if (!QuestManager.COMPLETED.equals(questManager.getQuestState(gameState, upgradeId))) {
            questManager.completeQuest(gameState, upgradeId);
        }
        refreshHud();
        return true;
    }

    private boolean canAffordSettlementUpgrade(SettlementUpgradeDefinition definition) {
        if (definition == null) {
            return true;
        }
        if (definition.getRequiredBlueprintFragmentCount() <= 0 || definition.getRequiredBlueprintFragmentId().isEmpty()) {
            return true;
        }
        return gameState.getBlueprintFragmentCount(definition.getRequiredBlueprintFragmentId())
            >= definition.getRequiredBlueprintFragmentCount();
    }

    private void spendSettlementUpgradeBlueprintCost(SettlementUpgradeDefinition definition) {
        if (definition == null) {
            return;
        }
        if (definition.getRequiredBlueprintFragmentCount() <= 0 || definition.getRequiredBlueprintFragmentId().isEmpty()) {
            return;
        }
        gameState.consumeBlueprintFragments(
            definition.getRequiredBlueprintFragmentId(),
            definition.getRequiredBlueprintFragmentCount()
        );
    }

    private void initializeRobotStats(RobotCompanion robot, int index) {
        switch (index) {
            case 0:
                robot.agility = 34f;
                robot.strength = 16f;
                robot.intelligence = 12f;
                robot.stamina = 14f;
                break;
            case 1:
                robot.agility = 20f;
                robot.strength = 23f;
                robot.intelligence = 10f;
                robot.stamina = 20f;
                break;
            default:
                robot.agility = 18f;
                robot.strength = 12f;
                robot.intelligence = 24f;
                robot.stamina = 16f;
                break;
        }
    }

    private void initializeEquipmentCatalog() {
        equipmentCatalog.clear();
        gameState.clearEquipmentCatalog();
        for (EquipmentItem item : DefinitionRegistries.EQUIPMENT.getAll()) {
            addEquipmentToCatalog(item);
        }
        seedStarterOwnedEquipment();
    }

    private void handleDefinitionReloadShortcut() {
        boolean ctrlHeld = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        boolean debugReload = debugOverlay.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.R);
        if ((ctrlHeld && Gdx.input.isKeyJustPressed(Input.Keys.R)) || debugReload) {
            reloadDefinitionData();
        }
    }

    private void reloadDefinitionData() {
        DefinitionRegistry.reloadAll();
        loadZoneDefinitions();
        loadRobotDefinitions();
        loadMonsterDefinitions();
        loadForgeComponentDefinitions();
        loadBlueprintFragmentDefinitions();
        loadForgeRecipes();
        loadStoryEvents();
        loadShopDefinitions();
        initializeEquipmentCatalog();
        AbilityRegistry.reloadDefinitions();
        questManager.reloadDefinitions();
        dialogueSystem.reloadDefinitions();
        worldStateManager.reloadDefinitions();
        recruitmentManager.reloadDefinitions();
        settlementManager.reloadDefinitions();
        currentZoneDefinition = currentZoneId != null ? zoneDefinitions.get(currentZoneId) : null;
        applyTownNpcSchedules();
        refreshDefinitionWatcherTimestamps();
        showStandaloneDialog("Debug", "Definition data reloaded from assets/data.");
    }

    private void initializeDefinitionWatcher() {
        watchedDefinitionTimestamps.clear();
        changedDefinitionPaths.clear();
        refreshDefinitionWatcherTimestamps();
    }

    private void refreshDefinitionWatcherTimestamps() {
        for (String path : WATCHED_DEFINITION_PATHS) {
            watchedDefinitionTimestamps.put(path, resolveDefinitionTimestamp(path));
        }
        changedDefinitionPaths.clear();
    }

    private void pollDefinitionWatcher(float delta) {
        if (delta <= 0f) {
            return;
        }
        definitionWatchPollTimer += delta;
        if (definitionWatchPollTimer < 1f) {
            return;
        }
        definitionWatchPollTimer = 0f;
        for (String path : WATCHED_DEFINITION_PATHS) {
            long previous = watchedDefinitionTimestamps.getOrDefault(path, 0L);
            long current = resolveDefinitionTimestamp(path);
            if (current > 0L && previous > 0L && current != previous && !changedDefinitionPaths.contains(path)) {
                changedDefinitionPaths.add(path);
            }
        }
    }

    private long resolveDefinitionTimestamp(String path) {
        if (path == null || path.isEmpty()) {
            return 0L;
        }
        try {
            return Gdx.files.internal(path).lastModified();
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    private void addEquipmentToCatalog(EquipmentItem item) {
        equipmentCatalog.add(item);
        gameState.addEquipmentToCatalog(item);
    }

    private void seedStarterOwnedEquipment() {
        unlockEquipment("bronze_edge");
        unlockEquipment("swift_boots");
    }

    private void mergeOwnedEquipmentFromLoadout() {
        for (String itemId : playerEquipment.values()) {
            unlockEquipment(itemId);
        }
        for (Map<String, String> equipped : robotEquipment.values()) {
            if (equipped == null) {
                continue;
            }
            for (String itemId : equipped.values()) {
                unlockEquipment(itemId);
            }
        }
    }

    private String getRobotId(int index) {
        if (index < 0 || index >= activeRobotIds.size()) {
            return null;
        }
        String robotId = activeRobotIds.get(index);
        return (robotId != null && !robotId.isEmpty()) ? robotId : null;
    }

    private String getLegacyRobotSlotKey(int index) {
        return "bot_" + index;
    }

    private Map<String, String> getEquippedItemsForPartyIndex(int index) {
        String robotId = getRobotId(index);
        if (robotId != null) {
            Map<String, String> equipped = robotEquipment.get(robotId);
            if (equipped != null) {
                return equipped;
            }
            String legacyKey = getLegacyRobotSlotKey(index);
            Map<String, String> legacy = robotEquipment.remove(legacyKey);
            if (legacy != null) {
                robotEquipment.put(robotId, new HashMap<>(legacy));
                gameState.setRobotEquipment(robotEquipment);
                return robotEquipment.get(robotId);
            }
        }
        return new HashMap<>();
    }

    private RobotProgressionState getRobotProgressionStateForPartyIndex(int index) {
        if (index < 0 || index >= activeRobotIds.size()) {
            return null;
        }
        return getOrCreateRobotProgressionState(activeRobotIds.get(index));
    }

    private RobotProgressionState getOrCreateRobotProgressionState(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return null;
        }
        RobotProgressionState state = gameState.getRobotProgressionState(robotId);
        if (state != null) {
            initializeRobotProgressionHealthIfNeeded(state);
            return state;
        }

        RobotDefinition definition = robotDefinitions.get(robotId);
        String displayName = definition != null ? definition.getName() : robotId;
        state = new RobotProgressionState(robotId, displayName);
        if (definition != null && definition.getAbilityIds() != null) {
            state.setKnownAbilityIds(new ArrayList<>(definition.getAbilityIds()));
            for (String abilityId : definition.getAbilityIds()) {
                state.getOrCreateAbilityProgression(abilityId);
            }
        }
        initializeRobotProgressionHealthIfNeeded(state);
        gameState.putRobotProgressionState(state);
        return state;
    }

    private void ensureRobotProgressionStates() {
        for (String robotId : collectedRobotIds) {
            RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
            ensureClassMasteryAbilities(robotId, state);
        }
        for (String robotId : activeRobotIds) {
            RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
            ensureClassMasteryAbilities(robotId, state);
        }
    }

    private void initializeRobotProgressionHealthIfNeeded(RobotProgressionState state) {
        if (state == null || state.getCurrentHealth() >= 0f) {
            return;
        }
        state.setCurrentHealth(getRobotEffectiveMaxHealth(state.getRobotId(), state));
    }

    private float getRobotEffectiveMaxHealth(String robotId, RobotProgressionState state) {
        RobotDefinition definition = robotId != null ? robotDefinitions.get(robotId) : null;
        float baseMaxHealth = definition != null && definition.getBaseHp() > 0
            ? definition.getBaseHp()
            : ROBOT_MAX_HEALTH;
        int level = state != null ? state.getLevel() : 1;
        int evolutionTier = state != null ? state.getEvolutionTier() : 1;
        float levelBonus = Math.max(0f, (level - 1) * RobotEvolutionManager.levelBonusPerLevel());
        float evolutionMultiplier = RobotEvolutionManager.statMultiplier(evolutionTier);
        EquipmentTotals equipmentTotals = getEquipmentTotals(robotId);
        return (baseMaxHealth + (levelBonus * 4f)) * evolutionMultiplier + equipmentTotals.hpBonus;
    }

    private int robotExperienceRequirementForLevel(int level) {
        return 35 + (Math.max(1, level) * 18);
    }

    private boolean isGradeUnlocked(String grade) {
        return gradeIndex(getUnlockedGrade()) >= gradeIndex(grade);
    }

    /**
     * Returns the number of robot battle slots currently available based on the
     * player's unlocked grade.
     *
     * <ul>
     *   <li>G-rank → 1 slot  (index 0 only)</li>
     *   <li>F-rank → 2 slots (indices 0–1)</li>
     *   <li>D-rank and above → 3 slots (indices 0–2)</li>
     * </ul>
     *
     * This is the single authority used by battle entry, overworld companion
     * rendering, and post-battle progression loops.
     */
    public int getPartySlotLimit() {
        int slots = 1;
        if (getForgeCoreLevel() >= 2 && isGradeUnlocked("F")) {
            slots = 2;
        }
        if (getForgeCoreLevel() >= 3 && isGradeUnlocked("D")) {
            slots = 3;
        }
        return slots;
    }

    /**
     * Returns the grade the player must reach to unlock the next party slot,
     * or {@code null} if all three slots are already unlocked.
     */
    public String getPartySlotNextGrade() {
        if (!isGradeUnlocked("F")) return "F";
        if (getForgeCoreLevel() < 2) return "Forge Core Lv2";
        if (!isGradeUnlocked("D")) return "D";
        if (getForgeCoreLevel() < 3) return "Forge Core Lv3";
        return null;
    }

    private int gradeIndex(String grade) {
        if (grade == null || grade.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < GRADE_ORDER.length; i++) {
            if (GRADE_ORDER[i].equals(grade)) {
                return i;
            }
        }
        return 0;
    }

    private WeaponProficiencyState getOrCreateWeaponProficiencyState(int partyIndex, WeaponType weaponType) {
        RobotProgressionState state = getRobotProgressionStateForPartyIndex(partyIndex);
        return WeaponProficiencyTracker.getOrCreate(state, weaponType);
    }

    private void evolveRobotAtIndex(int partyIndex, RobotProgressionState state) {
        if (partyIndex < 0 || partyIndex >= activeRobotIds.size() || state == null) {
            return;
        }
        String oldRobotId = activeRobotIds.get(partyIndex);
        String evolvedRobotId = RobotEvolutionManager.getEvolvedRobotId(oldRobotId, state.getEvolutionTier());
        if (evolvedRobotId == null || evolvedRobotId.equals(oldRobotId)) {
            mergeDefinitionAbilities(state, robotDefinitions.get(oldRobotId));
            return;
        }

        RobotDefinition evolvedDefinition = robotDefinitions.get(evolvedRobotId);
        activeRobotIds.set(partyIndex, evolvedRobotId);
        for (int i = 0; i < collectedRobotIds.size(); i++) {
            if (oldRobotId.equals(collectedRobotIds.get(i))) {
                collectedRobotIds.set(i, evolvedRobotId);
            }
        }
        state.setRobotId(evolvedRobotId);
        if (evolvedDefinition != null) {
            state.setDisplayName(evolvedDefinition.getName());
            mergeDefinitionAbilities(state, evolvedDefinition);
        }
        Map<String, String> equipment = robotEquipment.remove(oldRobotId);
        if (equipment != null) {
            robotEquipment.put(evolvedRobotId, equipment);
            gameState.setRobotEquipment(robotEquipment);
        }
        gameState.setActiveRobotIds(activeRobotIds);
        gameState.setCollectedRobotIds(collectedRobotIds);
        gameState.removeRobotProgressionState(oldRobotId);
        gameState.putRobotProgressionState(state);
        pendingRobotAwakeningMessages.add(state.getDisplayName() + " awakens along the "
            + state.getEvolutionPath() + " path as a " + state.getPersonalityArchetype() + " frame.");
    }

    private void mergeDefinitionAbilities(RobotProgressionState state, RobotDefinition definition) {
        if (state == null || definition == null || definition.getAbilityIds() == null) {
            return;
        }
        for (String abilityId : definition.getAbilityIds()) {
            if (!state.getKnownAbilityIds().contains(abilityId)) {
                state.getKnownAbilityIds().add(abilityId);
            }
            state.getOrCreateAbilityProgression(abilityId);
        }
    }

    public WeaponType getEquippedWeaponType(int partyIndex) {
        if (partyIndex < 0 || partyIndex >= activeRobotIds.size()) {
            return WeaponType.NONE;
        }
        Map<String, String> equipped = getEquippedItemsForPartyIndex(partyIndex);
        if (equipped == null) {
            return WeaponType.NONE;
        }
        String itemId = equipped.get("WEAPON");
        EquipmentItem item = itemId != null ? findEquipmentItem(itemId) : null;
        return item != null ? item.getWeaponType() : WeaponType.NONE;
    }

    public float getWeaponDamageMultiplier(int partyIndex, WeaponType weaponType) {
        WeaponProficiencyState state = getOrCreateWeaponProficiencyState(partyIndex, weaponType);
        return state != null ? WeaponProficiencyTracker.damageMultiplier(state.getLevel()) : 1f;
    }

    public WeaponGain awardWeaponProficiency(int partyIndex, WeaponType weaponType, int xpAmount) {
        WeaponProficiencyState state = getOrCreateWeaponProficiencyState(partyIndex, weaponType);
        if (state == null) {
            return null;
        }
        int beforeLevel = state.getLevel();
        // gainXp handles level-up and auto-unlocks milestones in WeaponProficiencyState;
        // returns the list of newly-unlocked Combat Art ability IDs.
        List<String> newArtIds = WeaponProficiencyTracker.gainXp(state, weaponType, xpAmount);
        List<String> unlockLabels = new ArrayList<>();
        for (String artId : newArtIds) {
            AbilityDefinition def = AbilityRegistry.get(artId);
            String artName = def != null ? def.getName() : artId;
            int tier = CombatArtRegistry.tierOf(weaponType, artId);
            String tierStr = tier > 0 ? " (Tier " + tier + ")" : "";
            unlockLabels.add(artName + tierStr);
        }
        return new WeaponGain(weaponType, beforeLevel, state.getLevel(), unlockLabels);
    }

    public List<String> getUniqueBoostsForPartyIndex(int partyIndex) {
        List<String> boosts = new ArrayList<>();
        Map<String, String> equipped = partyIndex < 0 ? playerEquipment : getEquippedItemsForPartyIndex(partyIndex);
        if (equipped == null) {
            return boosts;
        }
        for (String itemId : equipped.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item == null) {
                continue;
            }
            String uniqueBoost = item.getUniqueBoost();
            if (!uniqueBoost.isEmpty() && !boosts.contains(uniqueBoost)) {
                boosts.add(uniqueBoost);
            }
        }
        return boosts;
    }

    private EquipmentTotals getPlayerEquipmentTotals() {
        EquipmentTotals totals = new EquipmentTotals();
        for (String itemId : playerEquipment.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item == null) {
                continue;
            }
            applyEquipmentToTotals(totals, item);
        }
        return totals;
    }

    private EquipmentTotals getEquipmentTotals(int index) {
        EquipmentTotals totals = new EquipmentTotals();
        Map<String, String> equipped = getEquippedItemsForPartyIndex(index);
        if (equipped == null) {
            return totals;
        }

        for (String itemId : equipped.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item == null) {
                continue;
            }
            applyEquipmentToTotals(totals, item);
        }

        return totals;
    }

    private EquipmentTotals getEquipmentTotals(String robotId) {
        EquipmentTotals totals = new EquipmentTotals();
        if (robotId == null || robotId.isEmpty()) {
            return totals;
        }
        Map<String, String> equipped = robotEquipment.get(robotId);
        if (equipped == null) {
            return totals;
        }
        for (String itemId : equipped.values()) {
            EquipmentItem item = findEquipmentItem(itemId);
            if (item == null) {
                continue;
            }
            applyEquipmentToTotals(totals, item);
        }
        return totals;
    }

    private void applyEquipmentToTotals(EquipmentTotals totals, EquipmentItem item) {
        totals.hpBonus += item.getHpBonus();
        totals.strengthBonus += item.getAttackBonus();
        totals.staminaBonus += item.getDefenseBonus();
        totals.agilityBonus += item.getSpeedBonus();
        totals.intelligenceBonus += item.getIntelligenceBonus();

        if ("XP_BOOST".equals(item.getUniqueBoost())) {
            totals.xpMultiplier += 0.2f;
        } else if ("FIRST_STRIKE".equals(item.getUniqueBoost())) {
            totals.strengthBonus += 4f;
        } else if ("ARCANE_SURGE".equals(item.getUniqueBoost())) {
            totals.intelligenceBonus += 6f;
        }
    }

    private int applyUniqueExperienceBoost(int amount) {
        EquipmentTotals totals = getPlayerEquipmentTotals();
        return Math.round(amount * totals.getXpMultiplier());
    }

    private float getHellDifficultyMultiplier(int level) {
        return 1f + (getFibonacci(Math.max(1, level + 1)) / 100f);
    }

    private int getFibonacci(int n) {
        if (n <= 2) {
            return 1;
        }
        int previous = 1;
        int current = 1;
        for (int i = 3; i <= n; i++) {
            int next = previous + current;
            previous = current;
            current = next;
        }
        return current;
    }

    private boolean gradeMeetsRequirement(String ownerGrade, String requiredGrade) {
        return getGradeIndex(ownerGrade) >= getGradeIndex(requiredGrade);
    }

    private int getGradeIndex(String grade) {
        if (grade == null || grade.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < GRADE_ORDER.length; i++) {
            if (GRADE_ORDER[i].equals(grade)) {
                return i;
            }
        }
        return 0;
    }

    private String getGradeForLevel(int level) {
        if (level >= 99) return "S+++";
        if (level >= 90) return "S++";
        if (level >= 80) return "S+";
        if (level >= 70) return "S";
        if (level >= 60) return "A";
        if (level >= 50) return "B";
        if (level >= 40) return "C";
        if (level >= 30) return "D";
        if (level >= 20) return "E";
        if (level >= 10) return "F";
        return "G";
    }

    private boolean isBlockedAt(float x, float y, float size) {
        if (currentZone != null) {
            float half = size / 2f;
            if (x - half < 0f || x + half > currentZone.pixelWidth || y - half < 0f || y + half > currentZone.pixelHeight) {
                return true;
            }
            Rectangle candidate = new Rectangle(x - half, y - half, size, size);
            for (Rectangle collision : currentZone.collisions) {
                if (candidate.overlaps(collision)) {
                    return true;
                }
            }
            for (TmxWorldLoader.Feature feature : currentZone.features) {
                if (!feature.blocksMovement || !isFeatureVisible(feature)) {
                    continue;
                }
                if (candidate.overlaps(feature.bounds)) {
                    return true;
                }
            }
        }

        float left = x - size / 2f;
        float right = x + size / 2f;
        float bottom = y - size / 2f;
        float top = y + size / 2f;

        for (House house : houses) {
            float doorLeft = house.x + house.width * 0.4f;
            float doorRight = house.x + house.width * 0.6f;
            boolean intersectsDoorway = right > doorLeft && left < doorRight && bottom < house.y + 24f;

            if (intersectsDoorway) {
                continue;
            }

            if (right > house.x && left < house.x + house.width
                && top > house.y && bottom < house.y + house.height) {
                return true;
            }
        }

        return false;
    }

    private void interactWithNpc() {
        NpcEntity nearbyNpc = null;
        float nearestDistance = 70f;

        for (NpcEntity npc : npcs) {
            float distance = playerPos.dst(npc.pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearbyNpc = npc;
            }
        }

        if (nearbyNpc != null) {
            if (handleAct5NpcInteraction(nearbyNpc)) {
                return;
            }
            DialogueSystem.DialogueResult conversation = resolveNpcDialogue(nearbyNpc.id, nearbyNpc.name);
            showDialogueSequence(conversation.pages, conversation.speaker != null ? conversation.speaker : nearbyNpc.name, conversation.text);
            if (conversation.rewardGold > 0) {
                addGold(conversation.rewardGold);
            }
            if (conversation.rewardPotions > 0) {
                addHealingPotions(conversation.rewardPotions);
            }
            if (conversation.rewardExperience > 0) {
                addExperience(conversation.rewardExperience);
            }
        } else {
            clearActiveDialog();
        }
    }

    private boolean handleAct5NpcInteraction(NpcEntity npc) {
        if (npc == null || npc.id == null || npc.id.isEmpty()) {
            return false;
        }
        if ("coda".equals(npc.id)) {
            // Legacy Vault spending
            ForgeLegacyNodeDefinition nextNode = forgeLegacyEngine.getNextLockedNode(metaProgressionState);
            if (nextNode != null && metaProgressionState.getForgeShards() >= nextNode.getCost()) {
                if (forgeLegacyEngine.purchaseNode(metaProgressionState, nextNode.getId())) {
                    metaProgressionManager.save(metaProgressionState);
                    showStandaloneDialog("Coda", "Legacy Vault: " + nextNode.getName() + " unlocked. Shards remaining: " + metaProgressionState.getForgeShards());
                }
            } else if (nextNode != null) {
                showStandaloneDialog("Coda", "Next Legacy Unlock: " + nextNode.getName() + " (" + nextNode.getCost() + " Shards). Balance: " + metaProgressionState.getForgeShards());
            } else {
                showStandaloneDialog("Coda", "All known Legacy nodes have been restored. Your potential is perfected.");
            }
            return true;
        }
        if ("bolt_simulation".equals(npc.id)) {
            if (gameState.isShardRunActive()) {
                showStandaloneDialog("Bolt Simulation", "Active run detected. Level: " + gameState.getInfiniteDungeonCurrentFloor() + ". Continue to floor " + (gameState.getInfiniteDungeonCurrentFloor() + 1) + "?");
            } else {
                shardRunManager.startShardRun("scout_mk1");
                showStandaloneDialog("Bolt Simulation", "Roguelike Shard Run initialized. Equipment purged. Core stats reset. Survival is the only objective. Routing to Floor 1...");
                loadZone(INFINITE_DUNGEON_ZONE_ID, "from_hub", true);
            }
            return true;
        }
        PlayerCreatedNpc createdNpc = getPlayerCreatedNpcById(npc.id);
        if (createdNpc != null) {
            if (createdNpc.guildId != null && getActivePlayerQuestContractForGuild(createdNpc.guildId) == null) {
                GuildDefinition guild = guildDefinitionsById.get(createdNpc.guildId);
                if (guild != null) {
                    PlayerQuestContract contract = buildNextPlayerQuestContract(guild);
                    if (contract != null) {
                        playerQuestContracts.add(contract);
                        autosave();
                        showStandaloneDialog(createdNpc.name, createdNpc.dialog + " I've posted a fresh charter: " + contract.title + ".");
                        return true;
                    }
                }
            }
            showStandaloneDialog(createdNpc.name, createdNpc.dialog);
            return true;
        }
        return false;
    }

    private PlayerCreatedNpc getPlayerCreatedNpcById(String npcId) {
        if (npcId == null || npcId.isEmpty()) {
            return null;
        }
        for (PlayerCreatedNpc npc : playerCreatedNpcs) {
            if (npc != null && npcId.equals(npc.npcId)) {
                return npc;
            }
        }
        return null;
    }

    public DialogueSystem.DialogueResult interactWithInteriorNpc(String npcId, String speakerName) {
        DialogueSystem.DialogueResult result = resolveNpcDialogue(npcId, speakerName);
        if (result.rewardGold > 0) {
            addGold(result.rewardGold);
        }
        if (result.rewardPotions > 0) {
            addHealingPotions(result.rewardPotions);
        }
        if (result.rewardExperience > 0) {
            addExperience(result.rewardExperience);
        }
        return result;
    }

    private boolean tryEnterHouse() {
        for (House house : houses) {
            if (canUseHouseDoor(house)) {
                clearActiveDialog();
                screenManager.push(new HouseInteriorScreen(game, screenManager, this, house));
                return true;
            }
        }
        return false;
    }

    private boolean tryReadHouseSign() {
        for (House house : houses) {
            if (canReadHouseSign(house)) {
                showStandaloneDialog("Sign", house.name);
                return true;
            }
        }
        return false;
    }

    private boolean tryInteractWithDoor() {
        if (currentZone == null) {
            return false;
        }
        for (TmxWorldLoader.Door door : currentZone.doors) {
            if (door.houseId >= 0) {
                if (canUseHouseDoor(findHouseById(door.houseId))) {
                    return tryEnterHouse();
                }
                continue;
            }
            if (distanceToRect(playerPos, door.bounds) > 42f) {
                continue;
            }
            if (door.lockedByKeyItem != null && !hasKeyItem(door.lockedByKeyItem)) {
                showStandaloneDialog("Warning", door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage + " You can still press through if you're ready."
                    : "This route is dangerous for an unprepared party.");
            }
            if (door.lockedByWorldFlag != null
                && !door.lockedByWorldFlag.isEmpty()
                && !isProgressFlagActive(door.lockedByWorldFlag)) {
                showStandaloneDialog("Warning", door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage + " The frontier won't stop you, but it may punish you."
                    : "You have not completed the local objective yet.");
            }
            if (door.requiredWorldFlag != null && !door.requiredWorldFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, door.requiredWorldFlag)) {
                showStandaloneDialog("Warning", door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage
                    : "The route is dormant for now.");
                return true;
            }
            if (door.targetZoneId != null) {
                ZoneAccessPolicy.AccessDecision accessDecision = evaluateZoneAccess(door.targetZoneId);
                if (!accessDecision.isAllowed()) {
                    showStandaloneDialog("Warning", accessDecision.getBlockedReason());
                    return true;
                }
                if (isInfiniteDungeonZone() && currentZoneId.equals(door.targetZoneId)) {
                    if (!allDungeonEnemiesDefeated()) {
                        showStandaloneDialog("Bolt Simulation", door.lockMessage != null && !door.lockMessage.isEmpty()
                            ? door.lockMessage
                            : "Clear the current floor before the next gate will answer.");
                        return true;
                    }
                    gameState.setInfiniteDungeonCurrentFloor(getInfiniteDungeonCurrentFloor() + 1);
                    gameState.setInfiniteDungeonBestFloor(Math.max(
                        gameState.getInfiniteDungeonBestFloor(),
                        gameState.getInfiniteDungeonCurrentFloor()
                    ));
                    loadZone(door.targetZoneId, door.targetSpawnId, true);
                    refreshHud();
                    return true;
                }
                if (isInfiniteDungeonZone() && !currentZoneId.equals(door.targetZoneId)) {
                    handleInfiniteDungeonRunEnd(true);
                }
                loadZone(door.targetZoneId, door.targetSpawnId, true);
                refreshHud();
                return true;
            }
        }
        return false;
    }

    private boolean currentZoneIsSafe() {
        return currentZone != null && currentZone.isSafeAt(playerPos);
    }

    private boolean canUseHouseDoor(House house) {
        return house != null && lastMoveDirection.y > 0.5f && house.getEntryTileBounds().contains(playerPos);
    }

    private boolean canReadHouseSign(House house) {
        return isFacingInteractionRect(house.getSignBounds());
    }

    private boolean isFacingInteractionRect(Rectangle rect) {
        return rect.contains(getInteractionPoint());
    }

    private Vector2 getInteractionPoint() {
        Vector2 direction = new Vector2(lastMoveDirection);
        if (direction.isZero()) {
            direction.set(0f, -1f);
        }
        direction.nor().scl(INTERACTION_REACH);
        return new Vector2(playerPos).add(direction);
    }

    private float distanceToRect(Vector2 point, Rectangle rect) {
        float nearestX = Math.max(rect.x, Math.min(point.x, rect.x + rect.width));
        float nearestY = Math.max(rect.y, Math.min(point.y, rect.y + rect.height));
        return point.dst(nearestX, nearestY);
    }

    private House findHouseById(int houseId) {
        for (House house : houses) {
            if (house.id == houseId) {
                return house;
            }
        }
        return null;
    }

    private boolean tryOpenWorldChest() {
        if (currentZone == null) {
            return false;
        }
        for (TmxWorldLoader.ChestData chest : currentZone.chests) {
            if (chest.hiddenUntilFlag != null && !chest.hiddenUntilFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, chest.hiddenUntilFlag)) {
                continue;
            }
            if (chest.requiredWorldFlag != null && !chest.requiredWorldFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, chest.requiredWorldFlag)) {
                continue;
            }
            Rectangle chestRect = new Rectangle(chest.position.x - 18f, chest.position.y - 18f, 36f, 36f);
            if (distanceToRect(playerPos, chestRect) > 42f || isChestOpened(currentZoneId, -1, chest.id)) {
                continue;
            }
            markChestOpened(currentZoneId, -1, chest.id);
            if (chest.goldReward > 0) {
                addGold(chest.goldReward);
            }
            if (chest.potionReward > 0) {
                addHealingPotions(chest.potionReward);
            }
            if (chest.keyItemReward != null && !chest.keyItemReward.isEmpty()) {
                addKeyItem(chest.keyItemReward);
            }
            if (chest.completionWorldFlag != null && !chest.completionWorldFlag.isEmpty()) {
                worldStateManager.setFlag(gameState, chest.completionWorldFlag, true);
            }
            if (chest.recruitEventId != null && !chest.recruitEventId.isEmpty()) {
                applyRecruitment(chest.recruitEventId);
            }
            if ("starter_workshop_tools".equals(chest.id)) {
                applySettlementUpgrade("workshop_tools");
            }
            if ("herbalist_hidden".equals(chest.id)) {
                worldStateManager.setFlag(gameState, "chest.herbalist_hidden.opened", true);
                worldStateManager.setFlag(gameState, "recruit.medic_frame_found", true);
            }
            questManager.syncProgress(gameState, worldStateManager);
            showStandaloneDialog("Chest", chest.message);
            refreshHud();
            return true;
        }
        return false;
    }

    private boolean tryInteractWithWorldFeature() {
        if (currentZone == null) {
            return false;
        }
        for (TmxWorldLoader.Feature feature : currentZone.features) {
            if (!isFeatureInteractable(feature) || !isFacingInteractionRect(feature.bounds)) {
                continue;
            }
            if ("shop".equals(feature.interactionType) && feature.shopId != null && !feature.shopId.isEmpty()) {
                String shopName = feature.label != null && !feature.label.isEmpty()
                    ? feature.label
                    : formatZoneName(currentZoneId) + " Vendor";
                screenManager.push(new ShopScreen(game, screenManager, this, shopName, createShopInventory(feature.shopId)));
                return true;
            }
            if ("harvest_resource".equals(feature.interactionType)) {
                return tryHarvestResourceFeature(feature);
            }
            if ("claim_outpost_site".equals(feature.interactionType)) {
                return tryClaimFrontierBaseSite(feature);
            }
            EnvironmentalInteractionSystem.InteractionResolution interaction =
                environmentalInteractionSystem.evaluate(feature, buildWorldInteractionProfile());
            if (interaction.canInteract()) {
                if (feature.completionWorldFlag != null && !feature.completionWorldFlag.isEmpty()) {
                    worldStateManager.setFlag(gameState, feature.completionWorldFlag, true);
                }
                questManager.syncProgress(gameState, worldStateManager);
                showStandaloneDialog(feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier",
                    feature.interactionMessage != null && !feature.interactionMessage.isEmpty()
                    ? feature.interactionMessage
                    : "Your crew clears the obstruction and opens the route ahead.");
                refreshHud();
                return true;
            }
            showStandaloneDialog(feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier",
                feature.blockedMessage != null && !feature.blockedMessage.isEmpty()
                ? feature.blockedMessage
                : interaction.getBlockedMessage() != null && !interaction.getBlockedMessage().isEmpty()
                    ? interaction.getBlockedMessage()
                    : "Your current crew can't clear this obstacle yet.");
            return true;
        }
        return false;
    }

    private boolean isFeatureVisible(TmxWorldLoader.Feature feature) {
        if (feature == null) {
            return false;
        }
        if (feature.hiddenUntilFlag != null && !feature.hiddenUntilFlag.isEmpty()
            && !worldStateManager.isFlagActive(gameState, feature.hiddenUntilFlag)) {
            return false;
        }
        if (feature.requiredWorldFlag != null && !feature.requiredWorldFlag.isEmpty()
            && !worldStateManager.isFlagActive(gameState, feature.requiredWorldFlag)) {
            return false;
        }
        return !isFeatureCompleted(feature);
    }

    private boolean isFeatureCompleted(TmxWorldLoader.Feature feature) {
        if (feature == null) {
            return false;
        }
        if ("harvest_resource".equals(feature.interactionType)) {
            return isHarvestedFrontierFeature(feature.persistentStateId);
        }
        if ("claim_outpost_site".equals(feature.interactionType)) {
            return isClaimedFrontierBaseSite(feature.persistentStateId);
        }
        return feature != null
            && feature.completionWorldFlag != null
            && !feature.completionWorldFlag.isEmpty()
            && worldStateManager.isFlagActive(gameState, feature.completionWorldFlag);
    }

    private boolean isFeatureInteractable(TmxWorldLoader.Feature feature) {
        return feature != null
            && !"player_structure".equals(feature.kind)
            && feature.interactionType != null
            && !feature.interactionType.isEmpty()
            && isFeatureVisible(feature);
    }

    private String getFeatureActionLabel(TmxWorldLoader.Feature feature) {
        if (feature == null || feature.interactionType == null) {
            return "Interact";
        }
        switch (feature.interactionType) {
            case "shop":
                return "Shop";
            case "claim_outpost_site":
                return "Survey";
            default:
                return environmentalInteractionSystem.getActionLabel(feature, buildWorldInteractionProfile());
        }
    }

    private boolean hasWorldInteractionCapability(String interactionType) {
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.interactionType = interactionType;
        return environmentalInteractionSystem.canInteract(feature, buildWorldInteractionProfile());
    }

    private boolean tryHarvestResourceFeature(TmxWorldLoader.Feature feature) {
        if (feature == null || feature.resourceId == null || feature.resourceId.isEmpty()) {
            showStandaloneDialog("Frontier", "This node cannot be harvested yet.");
            return true;
        }
        EnvironmentalInteractionSystem.InteractionResolution interaction =
            environmentalInteractionSystem.evaluate(feature, buildWorldInteractionProfile());
        if (!interaction.canInteract()) {
            showStandaloneDialog(feature.label != null ? feature.label : "Frontier",
                interaction.getBlockedMessage() != null ? interaction.getBlockedMessage() : "Your crew can't work this node yet.");
            return true;
        }
        if (isHarvestedFrontierFeature(feature.persistentStateId)) {
            showStandaloneDialog(feature.label != null ? feature.label : "Frontier", "This node has already been stripped clean.");
            return true;
        }
        harvestedFrontierFeatureIds.add(feature.persistentStateId);
        int harvestedAmount = Math.max(1, Math.max(1, feature.resourceAmount)
            + getCyberneticBonuses().getHarvestYieldBonus()
            + interaction.getYieldBonus());
        addForgeComponentLoot(feature.resourceId, harvestedAmount);
        showStandaloneDialog(feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier",
            feature.interactionMessage != null && !feature.interactionMessage.isEmpty()
                ? feature.interactionMessage
                : "Your crew recovers " + harvestedAmount + " " + feature.resourceId + ".");
        refreshHud();
        bankExpeditionHaulIfPossible(true);
        return true;
    }

    private void applyDynamicWorldEvents(String bossId, List<String> bulletinLines) {
        List<DynamicWorldEventSystem.DynamicWorldEvent> events = dynamicWorldEventSystem.handleBossClear(
            bossId,
            currentZoneId,
            zoneDefinitions.values(),
            gameState,
            worldStateManager
        );
        for (DynamicWorldEventSystem.DynamicWorldEvent event : events) {
            applyDynamicWorldEvent(event, bulletinLines);
        }
    }

    private void applyDynamicWorldEvent(DynamicWorldEventSystem.DynamicWorldEvent event, List<String> bulletinLines) {
        if (event == null) {
            return;
        }
        switch (event.getType()) {
            case CONTRACT:
                registerDynamicQuestContract(event);
                break;
            case AMBUSH:
                if (event.getZoneId() != null && !event.getZoneId().isEmpty()) {
                    activeRegionalIncidentsByZoneId.put(event.getZoneId(), determineRegionalIncidentType(event.getZoneId()));
                }
                break;
            case WORLD_CHANGE:
                if (event.getZoneId() != null && !event.getZoneId().isEmpty()) {
                    activeWorldBossFrontsByZoneId.remove(event.getZoneId());
                }
                break;
            default:
                break;
        }
        if (bulletinLines != null && event.getDescription() != null && !event.getDescription().isEmpty()) {
            bulletinLines.add(event.getDescription());
        }
    }

    private void registerDynamicQuestContract(DynamicWorldEventSystem.DynamicWorldEvent event) {
        if (event == null || event.getZoneId() == null || event.getZoneId().isEmpty()) {
            return;
        }
        PlayerQuestContract existing = getActivePlayerQuestContractForZone(event.getZoneId());
        if (existing != null) {
            return;
        }
        PlayerQuestContract contract = new PlayerQuestContract();
        contract.contractId = "dynamic_" + event.getZoneId() + "_" + (playerQuestContracts.size() + 1);
        contract.zoneId = event.getZoneId();
        contract.guildId = "dynamic_world";
        contract.kind = event.getContractKind() != null ? event.getContractKind() : "DYNAMIC_WORLD";
        contract.title = event.getContractTitle() != null ? event.getContractTitle() : event.getTitle();
        contract.description = event.getContractDescription() != null ? event.getContractDescription() : event.getDescription();
        contract.targetId = event.getZoneId();
        contract.authorPlayerId = "World";
        contract.active = true;
        playerQuestContracts.add(contract);
    }

    private EnvironmentalInteractionSystem.InteractionProfile buildWorldInteractionProfile() {
        Set<String> activeAbilityIds = new LinkedHashSet<>();
        Map<String, Integer> proficiencyLevels = new HashMap<>();
        addWorldProficiency(proficiencyLevels, "field_ops", Math.max(1, playerLevel / 8));
        addWorldProficiency(proficiencyLevels, "labor", 1);
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (!hasActiveRobotAt(i)) {
                continue;
            }
            RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(i);
            if (progressionState != null && progressionState.getKnownAbilityIds() != null) {
                activeAbilityIds.addAll(progressionState.getKnownAbilityIds());
            }
            int proficiencyLevel = Math.max(1, getRobotLevel(i) / 4);
            String robotClass = getRobotClass(i);
            if (robotClass.contains("Scout")) {
                addWorldProficiency(proficiencyLevels, "analysis", proficiencyLevel);
                addWorldProficiency(proficiencyLevels, "hacking", proficiencyLevel);
            }
            if (robotClass.contains("Support")) {
                addWorldProficiency(proficiencyLevels, "analysis", proficiencyLevel);
                addWorldProficiency(proficiencyLevels, "field_ops", proficiencyLevel);
            }
            if (robotClass.contains("Vanguard")) {
                addWorldProficiency(proficiencyLevels, "labor", proficiencyLevel);
                addWorldProficiency(proficiencyLevels, "mining", proficiencyLevel);
            }
            if (robotClass.contains("Striker")) {
                addWorldProficiency(proficiencyLevels, "demolition", proficiencyLevel);
                addWorldProficiency(proficiencyLevels, "field_ops", proficiencyLevel);
            }
        }
        if (activeAbilityIds.contains("deep_scan")) {
            addWorldProficiency(proficiencyLevels, "analysis", 2);
            addWorldProficiency(proficiencyLevels, "hacking", 1);
        }
        if (activeAbilityIds.contains("seismic_break")) {
            addWorldProficiency(proficiencyLevels, "mining", 2);
        }
        if (activeAbilityIds.contains("rapid_fire")) {
            addWorldProficiency(proficiencyLevels, "demolition", 1);
        }
        return new EnvironmentalInteractionSystem.InteractionProfile(activeAbilityIds, proficiencyLevels);
    }

    private void addWorldProficiency(Map<String, Integer> proficiencyLevels, String proficiencyId, int amount) {
        if (proficiencyLevels == null || proficiencyId == null || proficiencyId.isEmpty() || amount <= 0) {
            return;
        }
        proficiencyLevels.put(proficiencyId, proficiencyLevels.getOrDefault(proficiencyId, 0) + amount);
    }

    private boolean tryClaimFrontierBaseSite(TmxWorldLoader.Feature feature) {
        if (feature == null || feature.persistentStateId == null || feature.persistentStateId.isEmpty()) {
            return false;
        }
        BaseState baseState = getOrCreateBaseState(currentZoneId);
        OwnershipRecord claimOwnership = resolveDefaultClaimOwnership(feature.persistentStateId);
        baseState.claimSite(feature.persistentStateId, claimOwnership);
        boolean firstFrontierClaim = !worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_claimed");
        if (firstFrontierClaim) {
            worldStateManager.setFlag(gameState, "tutorial.frontier_outpost_claimed", true);
        }
        syncClaimedFrontierBaseSiteIds();
        handlePinnedContractClaim(feature.persistentStateId);
        showStandaloneDialog(feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier",
            firstFrontierClaim
                ? "Foothold secured. This site is yours now. Build a storage crate here so you can bank field haul without running all the way back to Ironhaven."
                : buildClaimSiteMessage(claimOwnership));
        autosave();
        return true;
    }

    private boolean isHarvestedFrontierFeature(String persistentStateId) {
        return persistentStateId != null && harvestedFrontierFeatureIds.contains(persistentStateId);
    }

    private boolean isClaimedFrontierBaseSite(String persistentStateId) {
        if (persistentStateId == null || persistentStateId.isEmpty()) {
            return false;
        }
        BaseState baseState = baseStatesByZoneId.get(currentZoneId);
        return (baseState != null && baseState.hasClaimedSite(persistentStateId))
            || claimedFrontierBaseSiteIds.contains(persistentStateId);
    }

    private BaseState getOrCreateBaseState(String zoneId) {
        String resolvedZoneId = zoneId != null && !zoneId.isEmpty() ? zoneId : currentZoneId;
        return baseStatesByZoneId.computeIfAbsent(resolvedZoneId, BaseState::new);
    }

    private void toggleBuildMode() {
        if (currentZoneDefinition == null || !currentZoneDefinition.isExpansiveFrontier()) {
            showStandaloneDialog("Frontier", "Build mode is only available in the frontier.");
            return;
        }
        buildModeOpen = !buildModeOpen;
    }

    private void handleBuildModeShortcuts() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) {
            cycleBuildSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
            cycleBuildSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            removeTargetedStructure();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            repairTargetedStructure();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            assignReserveBotToTargetedStructure();
        }
    }

    private void cycleBuildSelection(int delta) {
        List<StructureDefinition> definitions = getBuildStructureCatalog();
        if (definitions.isEmpty()) {
            selectedBuildStructureIndex = 0;
            return;
        }
        selectedBuildStructureIndex = Math.floorMod(selectedBuildStructureIndex + delta, definitions.size());
    }

    private List<StructureDefinition> getBuildStructureCatalog() {
        List<StructureDefinition> definitions = baseBuildingEngine.getStructureRegistry().getAll();
        definitions.removeIf(definition -> definition == null || !isStructureUnlocked(definition));
        definitions.sort((left, right) -> left.getDisplayName().compareToIgnoreCase(right.getDisplayName()));
        return definitions;
    }

    private boolean isStructureUnlocked(StructureDefinition definition) {
        if (definition == null || definition.getId() == null) {
            return false;
        }
        switch (definition.getId()) {
            case "field_fabricator":
                return isForgeWorkshopUnlocked();
            case "storm_relay":
                return worldStateManager.isFlagActive(gameState, "settlement.command_hub");
            case "fusion_anvil":
                return isFusionForgeUnlocked();
            case "dragon_roost_beacon":
                return worldStateManager.isFlagActive(gameState, "frontier.dragon_roosts_active");
            default:
                return true;
        }
    }

    private StructureDefinition getSelectedBuildStructure() {
        List<StructureDefinition> definitions = getBuildStructureCatalog();
        if (definitions.isEmpty()) {
            selectedBuildStructureIndex = 0;
            return null;
        }
        selectedBuildStructureIndex = Math.floorMod(selectedBuildStructureIndex, definitions.size());
        return definitions.get(selectedBuildStructureIndex);
    }

    private BasePlacementResult getCurrentBuildPlacementResult(StructureDefinition structureDefinition) {
        if (structureDefinition == null || currentZone == null || frontierTerrainSampler == null) {
            return null;
        }
        float[] preview = getBuildPreviewOrigin(structureDefinition);
        String claimedSiteId = findBuildClaimSiteId(structureDefinition);
        return baseBuildingEngine.validatePlacement(
            structureDefinition.getId(),
            getOrCreateBaseState(currentZoneId),
            currentZone,
            frontierTerrainSampler,
            claimedSiteId,
            preview[0],
            preview[1]
        );
    }

    private String findBuildClaimSiteId(StructureDefinition structureDefinition) {
        if (structureDefinition == null || currentZone == null) {
            return null;
        }
        float[] preview = getBuildPreviewOrigin(structureDefinition);
        Rectangle previewBounds = new Rectangle(
            preview[0],
            preview[1],
            structureDefinition.getWidthTiles() * currentZone.tileWidth,
            structureDefinition.getHeightTiles() * currentZone.tileHeight
        );
        BaseState baseState = getOrCreateBaseState(currentZoneId);
        for (TmxWorldLoader.Feature feature : currentZone.features) {
            if (!"claim_outpost_site".equals(feature.interactionType)
                || feature.persistentStateId == null
                || !baseState.hasClaimedSite(feature.persistentStateId)
                || feature.bounds == null) {
                continue;
            }
            if (feature.bounds.contains(previewBounds.x, previewBounds.y)
                && feature.bounds.contains(previewBounds.x + previewBounds.width, previewBounds.y + previewBounds.height)) {
                return feature.persistentStateId;
            }
        }
        return null;
    }

    private float[] getBuildPreviewOrigin(StructureDefinition structureDefinition) {
        float tileWidth = currentZone != null ? currentZone.tileWidth : 48f;
        float tileHeight = currentZone != null ? currentZone.tileHeight : 48f;
        Vector2 direction = new Vector2(lastMoveDirection);
        if (direction.isZero()) {
            direction.set(0f, -1f);
        } else {
            direction.nor();
        }
        float previewCenterX = playerPos.x + direction.x * Math.max(tileWidth * 2f, 96f);
        float previewCenterY = playerPos.y + direction.y * Math.max(tileHeight * 2f, 96f);
        float snappedX = (float) Math.floor(previewCenterX / tileWidth) * tileWidth;
        float snappedY = (float) Math.floor(previewCenterY / tileHeight) * tileHeight;
        return new float[] {snappedX, snappedY};
    }

    private boolean tryPlaceSelectedStructure() {
        StructureDefinition structureDefinition = getSelectedBuildStructure();
        if (structureDefinition == null) {
            showStandaloneDialog("Build Mode", "No buildable structures are available.");
            return true;
        }
        BasePlacementResult placement = getCurrentBuildPlacementResult(structureDefinition);
        if (placement == null || !placement.isAllowed()) {
            showStandaloneDialog("Build Mode", placement != null && placement.getMessage() != null
                ? placement.getMessage()
                : "That structure cannot be placed here.");
            return true;
        }
        String claimedSiteId = findBuildClaimSiteId(structureDefinition);
        BaseState baseState = getOrCreateBaseState(currentZoneId);
        OwnershipRecord siteOwnership = baseState.getClaimedSiteOwnership(claimedSiteId);
        if (!canCurrentPlayerActOnOwnership(siteOwnership, PermissionAction.BUILD)) {
            showStandaloneDialog("Build Mode", "You do not have permission to build at this outpost.");
            return true;
        }
        if (!gameState.consumeForgeComponents(structureDefinition.getBuildCosts())) {
            showStandaloneDialog("Build Mode", "You do not have the required materials: " + buildStructureCostLine(structureDefinition) + ".");
            return true;
        }
        float[] preview = getBuildPreviewOrigin(structureDefinition);
        PlacedStructure structure = baseBuildingEngine.placeStructure(
            structureDefinition.getId(),
            baseState,
            currentZone,
            frontierTerrainSampler,
            claimedSiteId,
            preview[0],
            preview[1]
        );
        if (structure == null) {
            for (Map.Entry<String, Integer> entry : structureDefinition.getBuildCosts().entrySet()) {
                gameState.addForgeComponent(entry.getKey(), entry.getValue());
            }
            showStandaloneDialog("Build Mode", "The structure could not be placed.");
            return true;
        }
        OwnershipRecord structureOwnership = siteOwnership;
        if (structureOwnership == null) {
            structureOwnership = createPersonalOwnershipRecord();
        }
        baseState.setStructureOwnership(structure.getInstanceId(), structureOwnership);
        if (structureDefinition.getStorageCapacity() > 0 && !worldStateManager.isFlagActive(gameState, "tutorial.frontier_storage_built")) {
            worldStateManager.setFlag(gameState, "tutorial.frontier_storage_built", true);
            addStructureFeatureToCurrentZone(structure);
            syncCurrentZoneBaseDefenders();
            refreshHud();
            autosave();
            showStandaloneDialog("Build Mode", "Storage crate placed. Bring haul back here and bank it safely to turn this claim into a real frontier foothold.");
            return true;
        }
        addStructureFeatureToCurrentZone(structure);
        syncCurrentZoneBaseDefenders();
        refreshHud();
        autosave();
        showStandaloneDialog("Build Mode", structureDefinition.getDisplayName() + " placed.");
        return true;
    }

    private String buildStructureCostLine(StructureDefinition structureDefinition) {
        if (structureDefinition == null || structureDefinition.getBuildCosts().isEmpty()) {
            return "No materials required.";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : structureDefinition.getBuildCosts().entrySet()) {
            if (!first) {
                builder.append("  ");
            }
            first = false;
            builder.append(getForgeComponentName(entry.getKey()))
                .append(" ")
                .append(gameState.getForgeComponentCount(entry.getKey()))
                .append("/")
                .append(entry.getValue());
        }
        return builder.toString();
    }

    private void addStructureFeatureToCurrentZone(PlacedStructure structure) {
        if (currentZone == null || structure == null) {
            return;
        }
        for (int i = currentZone.features.size - 1; i >= 0; i--) {
            TmxWorldLoader.Feature feature = currentZone.features.get(i);
            if (feature != null && structure.getInstanceId().equals(feature.id)) {
                currentZone.features.removeIndex(i);
            }
        }
        currentZone.features.add(createStructureFeature(structure));
    }

    private TmxWorldLoader.Feature createStructureFeature(PlacedStructure structure) {
        StructureDefinition definition = structure != null
            ? baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId())
            : null;
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.id = structure.getInstanceId();
        feature.kind = "player_structure";
        feature.label = buildStructureFeatureLabel(structure, definition);
        feature.bounds = structure.getBounds();
        feature.blocksMovement = definition != null && definition.blocksMovement();
        feature.persistentStateId = structure.getInstanceId();
        return feature;
    }

    private String buildStructureFeatureLabel(PlacedStructure structure, StructureDefinition definition) {
        if (definition == null) {
            return "Structure";
        }
        String hpLabel = structure != null
            ? " HP " + structure.getCurrentHitPoints() + "/" + definition.getMaxHitPoints()
            : "";
        if (definition.getDefenderCapacity() > 0 && structure != null) {
            BaseState baseState = getBaseState(structure.getZoneId());
            long assigned = baseState != null ? baseState.getAssignedDefenderCount(structure.getInstanceId()) : 0L;
            return definition.getDisplayName() + " (" + assigned + "/" + definition.getDefenderCapacity() + ")" + hpLabel;
        }
        return definition.getDisplayName() + hpLabel;
    }

    private PlacedStructure getPlacedStructure(String structureInstanceId) {
        BaseState baseState = getCurrentBaseState();
        return baseState != null ? baseState.findStructure(structureInstanceId) : null;
    }

    private TmxWorldLoader.Feature findTargetedStructureFeature() {
        if (currentZone == null) {
            return null;
        }
        for (TmxWorldLoader.Feature feature : currentZone.features) {
            if (feature == null || !"player_structure".equals(feature.kind) || feature.bounds == null) {
                continue;
            }
            if (distanceToRect(playerPos, feature.bounds) <= 56f && isFacingInteractionRect(feature.bounds)) {
                return feature;
            }
        }
        return null;
    }

    private boolean removeTargetedStructure() {
        TmxWorldLoader.Feature feature = findTargetedStructureFeature();
        if (feature == null) {
            showStandaloneDialog("Build Mode", "Face one of your structures to remove it.");
            return false;
        }
        PlacedStructure structure = getPlacedStructure(feature.id);
        StructureDefinition definition = structure != null
            ? baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId())
            : null;
        BaseState baseState = getCurrentBaseState();
        OwnershipRecord ownershipRecord = baseState != null ? baseState.getStructureOwnership(feature.id) : null;
        if (!canCurrentPlayerActOnOwnership(ownershipRecord, PermissionAction.REMOVE_STRUCTURES)) {
            showStandaloneDialog("Build Mode", "You do not have permission to dismantle this structure.");
            return false;
        }
        if (baseState == null || !baseBuildingEngine.removeStructure(baseState, feature.id)) {
            showStandaloneDialog("Build Mode", "That structure could not be removed.");
            return false;
        }
        if (definition != null) {
            for (Map.Entry<String, Integer> entry : definition.getBuildCosts().entrySet()) {
                gameState.addForgeComponent(entry.getKey(), entry.getValue());
            }
        }
        currentZone.features.removeValue(feature, true);
        syncCurrentZoneBaseDefenders();
        refreshHud();
        autosave();
        showStandaloneDialog("Build Mode", definition != null && !definition.getBuildCosts().isEmpty()
            ? "Structure dismantled. Refunded " + buildStructureRefundLine(definition) + "."
            : "Structure dismantled.");
        return true;
    }

    private boolean repairTargetedStructure() {
        TmxWorldLoader.Feature feature = findTargetedStructureFeature();
        if (feature == null) {
            showStandaloneDialog("Build Mode", "Face one of your structures to repair it.");
            return false;
        }
        PlacedStructure structure = getPlacedStructure(feature.id);
        StructureDefinition definition = structure != null
            ? baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId())
            : null;
        BaseState baseState = getCurrentBaseState();
        OwnershipRecord ownershipRecord = baseState != null ? baseState.getStructureOwnership(feature.id) : null;
        if (!canCurrentPlayerActOnOwnership(ownershipRecord, PermissionAction.EDIT_STRUCTURES)) {
            showStandaloneDialog("Build Mode", "You do not have permission to repair this structure.");
            return false;
        }
        if (structure == null || definition == null) {
            showStandaloneDialog("Build Mode", "That structure could not be repaired.");
            return false;
        }
        if (structure.getCurrentHitPoints() >= definition.getMaxHitPoints()) {
            showStandaloneDialog("Build Mode", "That structure is already fully repaired.");
            return false;
        }
        Map<String, Integer> repairCosts = buildRepairCosts(definition, structure);
        if (!gameState.consumeForgeComponents(repairCosts)) {
            showStandaloneDialog("Build Mode", "Repair materials needed: " + buildRepairCostLine(repairCosts) + ".");
            return false;
        }
        int repaired = baseBuildingEngine.repairStructure(structure, definition, BASE_STRUCTURE_REPAIR_STEP);
        refreshStructureFeatureLabel(structure.getInstanceId());
        syncCurrentZoneBaseDefenders();
        refreshHud();
        autosave();
        showStandaloneDialog("Build Mode", repaired > 0
            ? definition.getDisplayName() + " repaired."
            : "Repair failed.");
        return repaired > 0;
    }

    private Map<String, Integer> buildRepairCosts(StructureDefinition definition, PlacedStructure structure) {
        Map<String, Integer> costs = new HashMap<>();
        if (definition == null || structure == null) {
            return costs;
        }
        float missingRatio = 1f - (structure.getCurrentHitPoints() / Math.max(1f, (float) definition.getMaxHitPoints()));
        float scaledRatio = Math.max(0.25f, missingRatio * 0.75f);
        for (Map.Entry<String, Integer> entry : definition.getBuildCosts().entrySet()) {
            costs.put(entry.getKey(), Math.max(1, Math.round(entry.getValue() * scaledRatio)));
        }
        return costs;
    }

    private String buildRepairCostLine(Map<String, Integer> costs) {
        if (costs == null || costs.isEmpty()) {
            return "No materials required";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : costs.entrySet()) {
            if (!first) {
                builder.append("  ");
            }
            first = false;
            builder.append(getForgeComponentName(entry.getKey()))
                .append(" ")
                .append(gameState.getForgeComponentCount(entry.getKey()))
                .append("/")
                .append(entry.getValue());
        }
        return builder.toString();
    }

    private String buildStructureRefundLine(StructureDefinition structureDefinition) {
        if (structureDefinition == null || structureDefinition.getBuildCosts().isEmpty()) {
            return "no materials";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : structureDefinition.getBuildCosts().entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append(entry.getValue()).append(" ").append(getForgeComponentName(entry.getKey()));
        }
        return builder.toString();
    }

    private boolean assignReserveBotToTargetedStructure() {
        TmxWorldLoader.Feature feature = findTargetedStructureFeature();
        if (feature == null || !isAssignableDefenderPost(feature)) {
            showStandaloneDialog("Build Mode", "Face a sentry post to assign a reserve bot.");
            return false;
        }
        List<String> reserveIds = getReserveRobotIds();
        if (reserveIds.isEmpty()) {
            showStandaloneDialog("Build Mode", "No reserve bots are available.");
            return false;
        }
        BaseState baseState = getCurrentBaseState();
        if (baseState == null) {
            return false;
        }
        PlacedStructure structure = baseState.findStructure(feature.id);
        if (structure == null) {
            return false;
        }
        OwnershipRecord ownershipRecord = baseState.getStructureOwnership(structure.getInstanceId());
        if (!canCurrentPlayerActOnOwnership(ownershipRecord, PermissionAction.EDIT_STRUCTURES)) {
            showStandaloneDialog("Build Mode", "You do not have permission to assign defenders here.");
            return false;
        }
        for (String robotId : reserveIds) {
            if (baseBuildingEngine.assignDefender(baseState, structure.getInstanceId(), robotId, DefenderRole.GUARD)) {
                refreshStructureFeatureLabel(structure.getInstanceId());
                syncCurrentZoneBaseDefenders();
                autosave();
                showStandaloneDialog("Build Mode", getRobotDisplayName(robotId) + " assigned to " + feature.label + ".");
                return true;
            }
        }
        showStandaloneDialog("Build Mode", "That post is full or all reserve bots are already assigned.");
        return false;
    }

    private boolean isAssignableDefenderPost(TmxWorldLoader.Feature feature) {
        if (feature == null || !"player_structure".equals(feature.kind)) {
            return false;
        }
        PlacedStructure structure = getPlacedStructure(feature.id);
        StructureDefinition definition = structure != null
            ? baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId())
            : null;
        return definition != null && definition.getDefenderCapacity() > 0;
    }

    private void refreshStructureFeatureLabel(String structureInstanceId) {
        if (currentZone == null || structureInstanceId == null || structureInstanceId.isEmpty()) {
            return;
        }
        PlacedStructure structure = getPlacedStructure(structureInstanceId);
        if (structure == null) {
            return;
        }
        StructureDefinition definition = baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId());
        for (TmxWorldLoader.Feature feature : currentZone.features) {
            if (feature != null && structureInstanceId.equals(feature.id)) {
                feature.label = buildStructureFeatureLabel(structure, definition);
                return;
            }
        }
    }

    private String getRobotDisplayName(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return "Reserve Bot";
        }
        boolean legendary = metaProgressionState.getUnlockedLegendaryRobotIds().contains(robotId);
        for (int i = 0; i < activeRobotIds.size(); i++) {
            if (robotId.equals(activeRobotIds.get(i))) {
                return legendary ? getRobotName(i) + " [Legendary]" : getRobotName(i);
            }
        }
        RobotProgressionState progressionState = getOrCreateRobotProgressionState(robotId);
        if (progressionState != null && progressionState.getDisplayName() != null && !progressionState.getDisplayName().isEmpty()) {
            return legendary ? progressionState.getDisplayName() + " [Legendary]" : progressionState.getDisplayName();
        }
        RobotDefinition definition = robotDefinitions.get(robotId);
        if (definition != null && definition.getName() != null && !definition.getName().isEmpty()) {
            return legendary ? definition.getName() + " [Legendary]" : definition.getName();
        }
        return legendary ? robotId + " [Legendary]" : robotId;
    }

    private void syncCurrentZoneBaseDefenders() {
        BaseState baseState = getCurrentBaseState();
        List<BaseDefenderUnit> existingUnits = new ArrayList<>(activeBaseDefenders);
        activeBaseDefenders.clear();
        if (baseState == null) {
            return;
        }
        Map<String, BaseDefenderProfile> profiles = new HashMap<>();
        for (DefenderAssignment assignment : baseState.getDefenderAssignments()) {
            if (assignment == null || assignment.getRobotId() == null || assignment.getRobotId().isEmpty()) {
                continue;
            }
            if (activeRobotIds.contains(assignment.getRobotId())) {
                continue;
            }
            RobotStatBlock stats = getRobotStatsForRobotId(assignment.getRobotId());
            profiles.put(assignment.getRobotId(), new BaseDefenderProfile(
                assignment.getRobotId(),
                getRobotDisplayName(assignment.getRobotId()),
                stats.maxHealth,
                Math.max(10f, stats.strength + stats.intelligence * 0.35f),
                stats.stamina,
                120f + stats.agility * 2.2f,
                108f + stats.agility * 0.6f,
                196f + stats.agility * 0.8f,
                Math.max(0.35f, 1.15f - Math.min(0.45f, stats.agility * 0.01f))
            ));
        }
        activeBaseDefenders.addAll(baseDefenseDirector.synchronize(
            baseState,
            baseBuildingEngine.getStructureRegistry(),
            profiles,
            existingUnits
        ));
        for (BaseDefenderUnit defender : activeBaseDefenders) {
            if (defender != null) {
                defender.setCurrentHealth(baseState.getDefenderHealth(defender.getRobotId(), defender.getCurrentHealth()));
            }
        }
    }

    private RobotStatBlock getRobotStatsForRobotId(String robotId) {
        if (robotId == null || robotId.isEmpty()) {
            return new RobotStatBlock(0f, 0f, 0f, 0f, 0f, 0f);
        }
        RobotProgressionState progressionState = getOrCreateRobotProgressionState(robotId);
        EquipmentTotals equipmentTotals = getEquipmentTotals(robotId);
        RobotDefinition definition = robotDefinitions.get(robotId);
        int robotLevel = progressionState != null ? progressionState.getLevel() : 1;
        int evolutionTier = progressionState != null ? progressionState.getEvolutionTier() : 1;
        float levelBonus = Math.max(0f, (robotLevel - 1) * RobotEvolutionManager.levelBonusPerLevel());
        float evolutionMultiplier = RobotEvolutionManager.statMultiplier(evolutionTier);
        float maxHealth = getRobotEffectiveMaxHealth(robotId, progressionState);
        RobotStatBlock block = new RobotStatBlock(
            progressionState != null ? progressionState.getCurrentHealth() : maxHealth,
            maxHealth,
            (((definition != null ? definition.getBaseSpeed() : 18f) + levelBonus) * evolutionMultiplier) + equipmentTotals.agilityBonus,
            (((definition != null ? definition.getBaseAttack() : 16f) + levelBonus) * evolutionMultiplier) + equipmentTotals.strengthBonus,
            ((((definition != null ? definition.getBaseAttack() : 16f) * 0.75f) + levelBonus * 0.8f) * evolutionMultiplier) + equipmentTotals.intelligenceBonus,
            (((definition != null ? definition.getBaseDefense() : 14f) + levelBonus) * evolutionMultiplier) + equipmentTotals.staminaBonus
        );
        applyRobotClassBonuses(block, getRobotClassForRobotId(robotId));
        block.currentHealth = Math.min(block.maxHealth, block.currentHealth);
        return block;
    }

    private int getDefenderSpriteIndex(String robotId) {
        return robotAnimations.length == 0 ? 0 : Math.floorMod(robotId != null ? robotId.hashCode() : 0, robotAnimations.length);
    }

    private List<SaveFile.BaseStateData> buildBaseStateSaveData() {
        List<SaveFile.BaseStateData> saveData = new ArrayList<>();
        for (BaseState baseState : baseStatesByZoneId.values()) {
            if (baseState == null || baseState.getZoneId() == null || baseState.getZoneId().isEmpty()) {
                continue;
            }
            SaveFile.BaseStateData zoneState = new SaveFile.BaseStateData();
            zoneState.setZoneId(baseState.getZoneId());
            zoneState.setClaimedSiteIds(baseState.getClaimedSiteIds());
            zoneState.setClaimedSiteOwnershipById(buildOwnershipRecordSaveMap(baseState.getClaimedSiteOwnershipById()));

            List<SaveFile.PlacedStructureData> structures = new ArrayList<>();
            for (PlacedStructure structure : baseState.getPlacedStructures()) {
                SaveFile.PlacedStructureData structureData = new SaveFile.PlacedStructureData();
                structureData.setInstanceId(structure.getInstanceId());
                structureData.setStructureDefinitionId(structure.getStructureDefinitionId());
                structureData.setZoneId(structure.getZoneId());
                structureData.setClaimedSiteId(structure.getClaimedSiteId());
                structureData.setX(structure.getBounds().x);
                structureData.setY(structure.getBounds().y);
                structureData.setWidth(structure.getBounds().width);
                structureData.setHeight(structure.getBounds().height);
                structureData.setCurrentHitPoints(structure.getCurrentHitPoints());
                structureData.setActive(structure.isActive());
                structures.add(structureData);
            }
            zoneState.setPlacedStructures(structures);
            zoneState.setStructureOwnershipByInstanceId(buildOwnershipRecordSaveMap(baseState.getStructureOwnershipByInstanceId()));

            List<SaveFile.DefenderAssignmentData> assignments = new ArrayList<>();
            for (DefenderAssignment assignment : baseState.getDefenderAssignments()) {
                SaveFile.DefenderAssignmentData assignmentData = new SaveFile.DefenderAssignmentData();
                assignmentData.setStructureInstanceId(assignment.getStructureInstanceId());
                assignmentData.setRobotId(assignment.getRobotId());
                assignmentData.setRole(assignment.getRole().name());
                assignments.add(assignmentData);
            }
            zoneState.setDefenderAssignments(assignments);
            zoneState.setDefenderHealthByRobotId(baseState.getDefenderHealthByRobotId());
            zoneState.setRaidActive(baseState.getRaidState().isActive());
            zoneState.setRaidThreatLevel(baseState.getRaidState().getThreatLevel());
            zoneState.setRaidCooldownSeconds(baseState.getRaidState().getCooldownSeconds());
            zoneState.setRaidWaveIndex(baseState.getRaidState().getWaveIndex());
            saveData.add(zoneState);
        }
        return saveData;
    }

    private void loadBaseStatesFromSave(SaveFile saveFile) {
        baseStatesByZoneId.clear();
        if (saveFile == null) {
            return;
        }
        for (SaveFile.BaseStateData zoneState : saveFile.getBaseStates()) {
            if (zoneState == null || zoneState.getZoneId() == null || zoneState.getZoneId().isEmpty()) {
                continue;
            }
            BaseState baseState = new BaseState(zoneState.getZoneId());
            for (String siteId : zoneState.getClaimedSiteIds()) {
                OwnershipRecord siteOwnership = restoreOwnershipRecord(zoneState.getClaimedSiteOwnershipById().get(siteId));
                baseState.claimSite(siteId, siteOwnership);
                if (baseState.getClaimedSiteOwnership(siteId) == null) {
                    baseState.setClaimedSiteOwnership(siteId, createPersonalOwnershipRecord());
                }
            }
            for (SaveFile.PlacedStructureData structureData : zoneState.getPlacedStructures()) {
                if (structureData == null) {
                    continue;
                }
                PlacedStructure structure = new PlacedStructure(
                    structureData.getInstanceId(),
                    structureData.getStructureDefinitionId(),
                    structureData.getZoneId(),
                    structureData.getClaimedSiteId(),
                    new Rectangle(
                        structureData.getX(),
                        structureData.getY(),
                        structureData.getWidth(),
                        structureData.getHeight()
                    ),
                    structureData.getCurrentHitPoints()
                );
                structure.setActive(structureData.isActive());
                baseState.addPlacedStructure(structure);
                OwnershipRecord structureOwnership = restoreOwnershipRecord(
                    zoneState.getStructureOwnershipByInstanceId().get(structure.getInstanceId())
                );
                if (structureOwnership == null) {
                    structureOwnership = baseState.getClaimedSiteOwnership(structure.getClaimedSiteId());
                }
                if (structureOwnership == null) {
                    structureOwnership = createPersonalOwnershipRecord();
                }
                baseState.setStructureOwnership(structure.getInstanceId(), structureOwnership);
            }
            for (SaveFile.DefenderAssignmentData assignmentData : zoneState.getDefenderAssignments()) {
                if (assignmentData == null) {
                    continue;
                }
                DefenderRole role;
                try {
                    role = assignmentData.getRole() != null ? DefenderRole.valueOf(assignmentData.getRole()) : DefenderRole.GUARD;
                } catch (IllegalArgumentException ex) {
                    role = DefenderRole.GUARD;
                }
                baseState.addDefenderAssignment(new DefenderAssignment(
                    assignmentData.getStructureInstanceId(),
                    assignmentData.getRobotId(),
                    role
                ));
            }
            baseState.setDefenderHealthByRobotId(zoneState.getDefenderHealthByRobotId());
            baseState.getRaidState().setActive(zoneState.isRaidActive());
            baseState.getRaidState().setThreatLevel(zoneState.getRaidThreatLevel());
            baseState.getRaidState().setCooldownSeconds(zoneState.getRaidCooldownSeconds());
            baseState.getRaidState().setWaveIndex(zoneState.getRaidWaveIndex());
            baseStatesByZoneId.put(baseState.getZoneId(), baseState);
        }
        if (saveFile.getBaseStates().isEmpty() && !saveFile.getClaimedFrontierBaseSiteIds().isEmpty()) {
            BaseState legacyBaseState = getOrCreateBaseState("verdant_fields");
            for (String siteId : saveFile.getClaimedFrontierBaseSiteIds()) {
                legacyBaseState.claimSite(siteId, createPersonalOwnershipRecord());
            }
        }
        syncClaimedFrontierBaseSiteIds();
    }

    private List<SaveFile.GuildData> buildGuildSaveData() {
        List<SaveFile.GuildData> guildData = new ArrayList<>();
        for (GuildDefinition guild : guildDefinitionsById.values()) {
            if (guild == null) {
                continue;
            }
            SaveFile.GuildData data = new SaveFile.GuildData();
            data.setGuildId(guild.getGuildId());
            data.setDisplayName(guild.getDisplayName());
            data.setFounderPlayerId(guild.getFounderPlayerId());
            data.setRecruitingOpen(guild.isRecruitingOpen());
            data.setHallZoneId(guild.getHallZoneId());
            data.setHallClaimedSiteId(guild.getHallClaimedSiteId());

            List<SaveFile.GuildRankData> rankData = new ArrayList<>();
            for (GuildRank rank : guild.getRanks()) {
                if (rank == null) {
                    continue;
                }
                SaveFile.GuildRankData savedRank = new SaveFile.GuildRankData();
                savedRank.setRankId(rank.getId());
                savedRank.setDisplayName(rank.getDisplayName());
                List<String> actions = new ArrayList<>();
                for (PermissionAction action : rank.getPermissionSet().getAllowedActions()) {
                    actions.add(action.name());
                }
                savedRank.setAllowedActions(actions);
                rankData.add(savedRank);
            }
            data.setRanks(rankData);

            List<SaveFile.GuildMembershipData> membershipData = new ArrayList<>();
            for (GuildMembership membership : guild.getMemberships()) {
                if (membership == null) {
                    continue;
                }
                SaveFile.GuildMembershipData savedMembership = new SaveFile.GuildMembershipData();
                savedMembership.setPlayerId(membership.getPlayerId());
                savedMembership.setRankId(membership.getRankId());
                savedMembership.setActive(membership.isActive());
                membershipData.add(savedMembership);
            }
            data.setMemberships(membershipData);
            guildData.add(data);
        }
        return guildData;
    }

    private List<SaveFile.PlayerQuestContractData> buildPlayerQuestContractSaveData() {
        List<SaveFile.PlayerQuestContractData> data = new ArrayList<>();
        for (PlayerQuestContract contract : playerQuestContracts) {
            if (contract == null) {
                continue;
            }
            SaveFile.PlayerQuestContractData saved = new SaveFile.PlayerQuestContractData();
            saved.setContractId(contract.contractId);
            saved.setGuildId(contract.guildId);
            saved.setZoneId(contract.zoneId);
            saved.setKind(contract.kind);
            saved.setTitle(contract.title);
            saved.setDescription(contract.description);
            saved.setTargetId(contract.targetId);
            saved.setAuthorPlayerId(contract.authorPlayerId);
            saved.setActive(contract.active);
            data.add(saved);
        }
        return data;
    }

    private List<SaveFile.PlayerCreatedNpcData> buildPlayerCreatedNpcSaveData() {
        List<SaveFile.PlayerCreatedNpcData> data = new ArrayList<>();
        for (PlayerCreatedNpc npc : playerCreatedNpcs) {
            if (npc == null || npc.npcId == null || npc.npcId.isEmpty()) {
                continue;
            }
            SaveFile.PlayerCreatedNpcData saved = new SaveFile.PlayerCreatedNpcData();
            saved.setNpcId(npc.npcId);
            saved.setGuildId(npc.guildId);
            saved.setZoneId(npc.zoneId);
            saved.setRole(npc.role);
            saved.setName(npc.name);
            saved.setDialog(npc.dialog);
            data.add(saved);
        }
        return data;
    }

    private void loadGuildsFromSave(SaveFile saveFile) {
        guildDefinitionsById.clear();
        if (saveFile == null) {
            return;
        }
        for (SaveFile.GuildData data : saveFile.getGuilds()) {
            if (data == null || data.getGuildId() == null || data.getGuildId().isEmpty()) {
                continue;
            }
            GuildDefinition guild = new GuildDefinition(data.getGuildId(), data.getDisplayName(), data.getFounderPlayerId());
            guild.setRecruitingOpen(data.isRecruitingOpen());
            guild.setHallZoneId(data.getHallZoneId());
            guild.setHallClaimedSiteId(data.getHallClaimedSiteId());
            for (SaveFile.GuildRankData rankData : data.getRanks()) {
                if (rankData == null || rankData.getRankId() == null || rankData.getRankId().isEmpty()) {
                    continue;
                }
                java.util.EnumSet<PermissionAction> actions = java.util.EnumSet.noneOf(PermissionAction.class);
                for (String actionName : rankData.getAllowedActions()) {
                    try {
                        actions.add(PermissionAction.valueOf(actionName));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                guild.registerRank(new GuildRank(rankData.getRankId(), rankData.getDisplayName(), new PermissionSet(actions)));
            }
            for (SaveFile.GuildMembershipData membershipData : data.getMemberships()) {
                if (membershipData == null) {
                    continue;
                }
                guild.upsertMembership(new GuildMembership(
                    membershipData.getPlayerId(),
                    membershipData.getRankId(),
                    membershipData.isActive()
                ));
            }
            guildDefinitionsById.put(guild.getGuildId(), guild);
        }
    }

    private void loadPlayerQuestContractsFromSave(SaveFile saveFile) {
        playerQuestContracts.clear();
        if (saveFile == null) {
            return;
        }
        for (SaveFile.PlayerQuestContractData data : saveFile.getPlayerQuestContracts()) {
            if (data == null || data.getContractId() == null || data.getContractId().isEmpty()) {
                continue;
            }
            PlayerQuestContract contract = new PlayerQuestContract();
            contract.contractId = data.getContractId();
            contract.guildId = data.getGuildId();
            contract.zoneId = data.getZoneId();
            contract.kind = data.getKind();
            contract.title = data.getTitle();
            contract.description = data.getDescription();
            contract.targetId = data.getTargetId();
            contract.authorPlayerId = data.getAuthorPlayerId();
            contract.active = data.isActive();
            playerQuestContracts.add(contract);
        }
    }

    private void loadPlayerCreatedNpcsFromSave(SaveFile saveFile) {
        playerCreatedNpcs.clear();
        if (saveFile == null) {
            return;
        }
        for (SaveFile.PlayerCreatedNpcData data : saveFile.getPlayerCreatedNpcs()) {
            if (data == null || data.getNpcId() == null || data.getNpcId().isEmpty()) {
                continue;
            }
            PlayerCreatedNpc npc = new PlayerCreatedNpc();
            npc.npcId = data.getNpcId();
            npc.guildId = data.getGuildId();
            npc.zoneId = data.getZoneId();
            npc.role = data.getRole();
            npc.name = data.getName();
            npc.dialog = data.getDialog();
            playerCreatedNpcs.add(npc);
        }
    }

    private Map<String, SaveFile.OwnershipRecordData> buildOwnershipRecordSaveMap(Map<String, OwnershipRecord> ownershipById) {
        Map<String, SaveFile.OwnershipRecordData> saveMap = new HashMap<>();
        if (ownershipById == null) {
            return saveMap;
        }
        for (Map.Entry<String, OwnershipRecord> entry : ownershipById.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null) {
                continue;
            }
            SaveFile.OwnershipRecordData recordData = new SaveFile.OwnershipRecordData();
            recordData.setScope(entry.getValue().getScope().name());
            recordData.setOwnerPlayerId(entry.getValue().getOwnerPlayerId());
            recordData.setOwnerGuildId(entry.getValue().getOwnerGuildId());
            recordData.setSettlementId(entry.getValue().getSettlementId());
            recordData.setPublicInteractionAllowed(entry.getValue().isPublicInteractionAllowed());
            recordData.setEditorPlayerIds(new ArrayList<>(entry.getValue().getEditorPlayerIds()));
            saveMap.put(entry.getKey(), recordData);
        }
        return saveMap;
    }

    private OwnershipRecord restoreOwnershipRecord(SaveFile.OwnershipRecordData data) {
        if (data == null) {
            return null;
        }
        OwnershipScope scope;
        try {
            scope = data.getScope() != null ? OwnershipScope.valueOf(data.getScope()) : OwnershipScope.PERSONAL;
        } catch (IllegalArgumentException ex) {
            scope = OwnershipScope.PERSONAL;
        }
        return new OwnershipRecord(
            scope,
            data.getOwnerPlayerId(),
            data.getOwnerGuildId(),
            data.getSettlementId(),
            data.isPublicInteractionAllowed(),
            new java.util.HashSet<>(data.getEditorPlayerIds())
        );
    }

    private OwnershipRecord createPersonalOwnershipRecord() {
        return new OwnershipRecord(OwnershipScope.PERSONAL, playerName, "", "", false, java.util.Set.of());
    }

    private OwnershipRecord resolveDefaultClaimOwnership(String claimedSiteId) {
        GuildDefinition controllableGuild = getActiveClaimGuild();
        if (controllableGuild == null) {
            return createPersonalOwnershipRecord();
        }
        if (controllableGuild.getHallClaimedSiteId() == null || controllableGuild.getHallClaimedSiteId().isEmpty()) {
            controllableGuild.setHallZoneId(currentZoneId);
            controllableGuild.setHallClaimedSiteId(claimedSiteId);
        }
        return new OwnershipRecord(OwnershipScope.GUILD, "", controllableGuild.getGuildId(), "", false, java.util.Set.of(playerName));
    }

    private String buildClaimSiteMessage(OwnershipRecord claimOwnership) {
        if (claimOwnership != null && claimOwnership.getScope() == OwnershipScope.GUILD) {
            GuildDefinition guild = guildDefinitionsById.get(claimOwnership.getOwnerGuildId());
            String guildName = guild != null && guild.getDisplayName() != null && !guild.getDisplayName().isEmpty()
                ? guild.getDisplayName()
                : "your guild";
            return "Site logged. This clearing is now held for " + guildName + " and can be expanded under guild permissions.";
        }
        return "Site logged. This clearing is now reserved for future base structures, reserve bot defenders, and local expansion.";
    }

    private GuildDefinition getActiveClaimGuild() {
        if (activeClaimGuildId == null || activeClaimGuildId.isEmpty()) {
            return null;
        }
        GuildDefinition guild = guildDefinitionsById.get(activeClaimGuildId);
        if (guild == null || !guildPermissionsEngine.canPerform(guild, playerName, PermissionAction.CLAIM_LAND)) {
            activeClaimGuildId = null;
            return null;
        }
        return guild;
    }

    private boolean canCurrentPlayerActOnOwnership(OwnershipRecord ownershipRecord, PermissionAction action) {
        if (ownershipRecord == null) {
            return true;
        }
        GuildDefinition guild = ownershipRecord.getOwnerGuildId() != null && !ownershipRecord.getOwnerGuildId().isEmpty()
            ? guildDefinitionsById.get(ownershipRecord.getOwnerGuildId())
            : null;
        return guildPermissionsEngine.canActOnRecord(ownershipRecord, guild, playerName, action);
    }

    private void syncClaimedFrontierBaseSiteIds() {
        claimedFrontierBaseSiteIds.clear();
        for (BaseState baseState : baseStatesByZoneId.values()) {
            for (String siteId : baseState.getClaimedSiteIds()) {
                if (!claimedFrontierBaseSiteIds.contains(siteId)) {
                    claimedFrontierBaseSiteIds.add(siteId);
                }
            }
        }
    }

    private void hydrateSavedBaseStructures(TmxWorldLoader.LoadedZone zone, String zoneId) {
        if (zone == null || zoneId == null || zoneId.isEmpty()) {
            return;
        }
        BaseState baseState = baseStatesByZoneId.get(zoneId);
        if (baseState == null) {
            return;
        }
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure == null || !structure.isActive()) {
                continue;
            }
            zone.features.add(createStructureFeature(structure));
        }
    }

    public BaseState getBaseState(String zoneId) {
        return zoneId == null || zoneId.isEmpty() ? null : baseStatesByZoneId.get(zoneId);
    }

    public BaseState getCurrentBaseState() {
        return getBaseState(currentZoneId);
    }

    private void addZoneShopFeatureIfNeeded() {
        if (currentZone == null || currentZoneId == null || currentZoneId.isEmpty() || zoneHasInteriorShop()) {
            return;
        }
        List<ShopDefinition> zoneShops = shopsByZoneId.get(currentZoneId);
        if (zoneShops == null || zoneShops.isEmpty()) {
            return;
        }
        ShopDefinition definition = zoneShops.get(0);
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.id = "zone_shop_" + definition.getId();
        feature.kind = "stall";
        feature.label = definition.getLocationLabel() != null && !definition.getLocationLabel().isEmpty()
            ? definition.getLocationLabel()
            : formatZoneName(currentZoneId) + " Vendor";
        feature.interactionType = "shop";
        feature.shopId = definition.getId();
        feature.bounds = createZoneShopBounds();
        currentZone.features.add(feature);
    }

    private void addWarVendorFeatureIfNeeded() {
        if (currentZone == null || currentZoneId == null || currentZoneId.isEmpty() || !warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return;
        }
        if (!isHubTownZone() && !hasTownServiceSurplus()) {
            return;
        }
        String dispatchZoneId = getPrimaryWarDispatchZone();
        String shopId = getRegionalWarShopId(dispatchZoneId);
        if (shopId == null || shopId.isEmpty()) {
            return;
        }
        String featureId = "war_vendor_" + dispatchZoneId;
        for (TmxWorldLoader.Feature existing : currentZone.features) {
            if (existing != null && featureId.equals(existing.id)) {
                return;
            }
        }
        TmxWorldLoader.Feature feature = new TmxWorldLoader.Feature();
        feature.id = featureId;
        feature.kind = "stall";
        feature.label = getRegionalWarVendorLabel(dispatchZoneId);
        feature.bounds = isHubTownZone()
            ? new Rectangle(1180f, 560f, 68f, 52f)
            : new Rectangle(playerPos.x + 72f, playerPos.y - 18f, 56f, 44f);
        feature.interactionType = "shop";
        feature.shopId = shopId;
        currentZone.features.add(feature);
    }

    private String getRegionalWarVendorLabel(String zoneId) {
        if ("shadow_caves".equals(zoneId)) {
            return "War Exchange: Ghostline Cache";
        }
        if ("crystal_depths".equals(zoneId)) {
            return "War Exchange: Prismfall Cache";
        }
        if ("sky_fortress".equals(zoneId)) {
            return "War Exchange: Summit Cache";
        }
        if ("rusty_quarry".equals(zoneId)) {
            return "War Exchange: Ironwake Cache";
        }
        if ("verdant_fields".equals(zoneId)) {
            return "War Exchange: Lifeline Depot";
        }
        return "War Exchange: " + getZoneDisplayName(zoneId);
    }

    private String getRegionalWarShopId(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) {
            return null;
        }
        List<ShopDefinition> shops = shopsByZoneId.get(zoneId);
        if (shops == null || shops.isEmpty()) {
            return null;
        }
        return shops.get(0).getId();
    }

    private boolean zoneHasInteriorShop() {
        for (House house : houses) {
            if (house == null || house.interiorNpcs == null) {
                continue;
            }
            for (InteriorNpc npc : house.interiorNpcs) {
                if (npc != null && npc.shopId != null && !npc.shopId.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Rectangle createZoneShopBounds() {
        float width = 72f;
        float height = 60f;
        float x = currentZone.pixelWidth * 0.5f - (width * 0.5f);
        float y = currentZone.pixelHeight * 0.5f - (height * 0.5f);
        if (currentZone.playerSpawns != null && currentZone.playerSpawns.size > 0) {
            Vector2 spawn = currentZone.playerSpawns.values().next();
            x = spawn.x + 96f;
            y = spawn.y + 24f;
        }
        x = Math.max(48f, Math.min(x, currentZone.pixelWidth - width - 48f));
        y = Math.max(48f, Math.min(y, currentZone.pixelHeight - height - 48f));
        return new Rectangle(x, y, width, height);
    }

    private boolean hasAnyActiveAbility(String... abilityIds) {
        if (abilityIds == null || abilityIds.length == 0) {
            return false;
        }
        List<String> required = Arrays.asList(abilityIds);
        for (int i = 0; i < ROBOT_COUNT; i++) {
            if (!hasActiveRobotAt(i)) {
                continue;
            }
            RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(i);
            if (progressionState == null || progressionState.getKnownAbilityIds() == null) {
                continue;
            }
            for (String knownAbilityId : progressionState.getKnownAbilityIds()) {
                if (required.contains(knownAbilityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addGold(long amount) {
        if (amount <= 0L) {
            return;
        }
        if (shouldTreatCurrentRewardAsExpeditionLoot()) {
            gameState.addUnbankedGold(amount);
            bankExpeditionHaulIfPossible(true);
        } else {
            gameState.addGold(amount);
        }
        totalGold = gameState.getTotalGold();
        refreshHud();
    }

    public long getGold() {
        return gameState.getTotalGold();
    }

    public boolean spendGold(long amount) {
        if (!gameState.spendGold(amount)) {
            return false;
        }
        totalGold = gameState.getTotalGold();
        refreshHud();
        return true;
    }

    public void addHealingPotions(int amount) {
        int adjustedAmount = amount;
        if (amount > 0 && hasTownServiceSurplus()) {
            adjustedAmount += 1;
        }
        if (amount > 0 && hasTownServiceStrain()) {
            adjustedAmount = Math.max(1, adjustedAmount - 1);
        }
        gameState.addHealingPotions(adjustedAmount);
        healingPotions = gameState.getHealingPotions();
        refreshHud();
    }

    public int getHealingPotions() {
        return gameState.getHealingPotions();
    }

    private void addForgeComponentLoot(String componentId, int amount) {
        if (componentId == null || componentId.isEmpty() || amount <= 0) {
            return;
        }
        if (shouldTreatCurrentRewardAsExpeditionLoot()) {
            gameState.addUnbankedForgeComponent(componentId, amount);
            bankExpeditionHaulIfPossible(true);
        } else {
            gameState.addForgeComponent(componentId, amount);
        }
        refreshHud();
    }

    private void addShardLoot(String grade, int amount) {
        if (grade == null || grade.isEmpty() || amount <= 0) {
            return;
        }
        if (shouldTreatCurrentRewardAsExpeditionLoot()) {
            gameState.addUnbankedShard(grade, amount);
            bankExpeditionHaulIfPossible(true);
        } else {
            gameState.addShard(grade, amount);
        }
        refreshHud();
    }

    private void addBlueprintFragmentLoot(String fragmentId, int amount) {
        if (fragmentId == null || fragmentId.isEmpty() || amount <= 0) {
            return;
        }
        if (shouldTreatCurrentRewardAsExpeditionLoot()) {
            gameState.addUnbankedBlueprintFragment(fragmentId, amount);
            bankExpeditionHaulIfPossible(true);
        } else {
            gameState.addBlueprintFragment(fragmentId, amount);
        }
        handlePinnedContractFragmentRecovery(fragmentId, amount);
        refreshHud();
    }

    private boolean shouldTreatCurrentRewardAsExpeditionLoot() {
        return currentZoneDefinition != null && !canBankExpeditionHaulHere();
    }

    private boolean canBankExpeditionHaulHere() {
        return currentZoneIsSafe() || isNearOperationalStorage();
    }

    private boolean isNearOperationalStorage() {
        BaseState baseState = getCurrentBaseState();
        if (baseState == null) {
            return false;
        }
        for (PlacedStructure structure : baseState.getPlacedStructures()) {
            if (structure == null || !structure.isActive()) {
                continue;
            }
            StructureDefinition definition = baseBuildingEngine.getStructureRegistry().get(structure.getStructureDefinitionId());
            if (definition == null || definition.getStorageCapacity() <= 0) {
                continue;
            }
            if (!canCurrentPlayerActOnOwnership(baseState.getStructureOwnership(structure.getInstanceId()), PermissionAction.MANAGE_STORAGE)) {
                continue;
            }
            Rectangle bounds = structure.getBounds();
            Rectangle interactionRect = new Rectangle(bounds.x - 56f, bounds.y - 56f, bounds.width + 112f, bounds.height + 112f);
            if (interactionRect.contains(playerPos)) {
                return true;
            }
        }
        return false;
    }

    private void bankExpeditionHaulIfPossible(boolean showFeedback) {
        if (!canBankExpeditionHaulHere()) {
            return;
        }
        boolean bankedAtOutpostStorage = !currentZoneIsSafe() && isNearOperationalStorage();
        long bankedGold = gameState.getUnbankedGold();
        Map<String, Integer> bankedComponents = new HashMap<>(gameState.getUnbankedForgeComponents());
        Map<String, Integer> bankedShards = new HashMap<>(gameState.getUnbankedShards());
        Map<String, Integer> bankedBlueprints = new HashMap<>(gameState.getUnbankedBlueprintFragments());
        if (bankedGold <= 0L && bankedComponents.isEmpty() && bankedShards.isEmpty() && bankedBlueprints.isEmpty()) {
            return;
        }
        gameState.addGold(bankedGold);
        gameState.setUnbankedGold(0L);
        for (Map.Entry<String, Integer> entry : bankedComponents.entrySet()) {
            gameState.addForgeComponent(entry.getKey(), entry.getValue());
        }
        gameState.clearUnbankedForgeComponents();
        for (Map.Entry<String, Integer> entry : bankedShards.entrySet()) {
            gameState.addShard(entry.getKey(), entry.getValue());
        }
        gameState.clearUnbankedShards();
        for (Map.Entry<String, Integer> entry : bankedBlueprints.entrySet()) {
            gameState.addBlueprintFragment(entry.getKey(), entry.getValue());
        }
        gameState.clearUnbankedBlueprintFragments();
        totalGold = gameState.getTotalGold();
        refreshHud();
        autosave();
        boolean actZeroTutorialShown = maybeTriggerActZeroBankingTutorial(bankedGold, bankedComponents, bankedShards);
        boolean actOneTutorialShown = maybeTriggerActOneOutpostBankingTutorial(
            bankedAtOutpostStorage,
            bankedGold,
            bankedComponents,
            bankedShards
        );
        maybeTriggerFrontierAnnexResupplyProgress(bankedGold, bankedComponents, bankedShards);
        List<String> actTwoSupportLines = applyActTwoTownReturnSupport(bankedGold, bankedComponents, bankedShards, bankedBlueprints);
        boolean actTwoBlueprintShown = maybeTriggerActTwoBlueprintTutorial(bankedBlueprints);
        handlePinnedContractBankingProgress(bankedAtOutpostStorage);
        if (showFeedback && !actZeroTutorialShown && !actOneTutorialShown && !actTwoBlueprintShown) {
            String summary = buildBankedHaulSummary(bankedGold, bankedComponents, bankedShards, bankedBlueprints);
            if (!actTwoSupportLines.isEmpty()) {
                summary += " " + String.join(" ", actTwoSupportLines);
            }
            showStandaloneDialog("Expedition Banked", summary);
        }
    }

    private boolean maybeTriggerActZeroBankingTutorial(long bankedGold, Map<String, Integer> bankedComponents, Map<String, Integer> bankedShards) {
        if (!isHubTownZone() || worldStateManager.isFlagActive(gameState, "tutorial.first_haul_banked")) {
            return false;
        }
        if (bankedGold <= 0L && (bankedComponents == null || bankedComponents.isEmpty()) && (bankedShards == null || bankedShards.isEmpty())) {
            return false;
        }
        worldStateManager.setFlag(gameState, "tutorial.first_haul_banked", true);
        questManager.startQuest(gameState, "workshop_tools");
        addStarterTownUpgradeChest();
        autosave();
        showStandaloneDialog(
            "Ironhaven",
            "You banked your first frontier haul. That salvage is safe now, and Ironhaven can actually build with it. Toma at the workshop wants to see what you brought back so he can get the forge line running."
        );
        return true;
    }

    private void handlePinnedContractBankingProgress(boolean bankedAtOutpostStorage) {
        if (pinnedExpeditionContractCompleted) {
            return;
        }
        if (pinnedExpeditionContractZoneId == null || pinnedExpeditionContractZoneId.isEmpty()) {
            return;
        }
        if (pinnedExpeditionContractKind != null && pinnedExpeditionContractKind.startsWith("CONVOY_ESCORT")) {
            if (bankedAtOutpostStorage && pinnedExpeditionContractZoneId.equals(currentZoneId)) {
                recordActFourSupplyVictory(currentZoneId, pinnedExpeditionContractKind);
                resolveSettlementCrisis(currentZoneId);
                String stabilization = stabilizeConvoyRoute(currentZoneId);
                completePinnedExpeditionContract(
                    "Contract complete. Relief convoy secured in " + getZoneDisplayName(currentZoneId) + "."
                        + (stabilization.isEmpty() ? "" : " " + stabilization)
                );
            }
            return;
        }
        if (pinnedExpeditionContractKind != null
            && pinnedExpeditionContractKind.startsWith("PLAYER_CREATED_SUPPLY")
            && bankedAtOutpostStorage
            && pinnedExpeditionContractZoneId.equals(currentZoneId)) {
            completePlayerQuestContractForZone(currentZoneId);
            recordActFourSupplyVictory(currentZoneId, pinnedExpeditionContractKind);
            resolveSettlementCrisis(currentZoneId);
            adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 6);
            adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 2);
            syncWarPhaseState(false);
            completePinnedExpeditionContract("Contract complete. Player-authored supply charter fulfilled.");
            return;
        }
        if ((pinnedExpeditionContractKind != null
            && (pinnedExpeditionContractKind.startsWith("GUILD_BOARD_SUPPLY")
            || pinnedExpeditionContractKind.startsWith("PUBLIC_BOARD_DELIVERY")))
            && bankedAtOutpostStorage
            && pinnedExpeditionContractZoneId.equals(currentZoneId)) {
            recordActFourSupplyVictory(currentZoneId, pinnedExpeditionContractKind);
            resolveSettlementCrisis(currentZoneId);
            String deliverySummary = fulfillBoardSupplyContract(currentZoneId, pinnedExpeditionContractKind);
            completePinnedExpeditionContract("Contract complete. " + deliverySummary);
            return;
        }
        if (!"BANK_HAUL".equals(pinnedExpeditionContractKind)) {
            return;
        }
        if (bankedAtOutpostStorage || isHubTownZone()) {
            completePinnedExpeditionContract("Contract complete. Haul banked safely for the current route.");
        }
    }

    private String fulfillBoardSupplyContract(String zoneId, String contractKind) {
        if (contractKind != null && contractKind.startsWith("GUILD_BOARD_SUPPLY")) {
            GuildDefinition guild = getPublishingGuildForZone(zoneId);
            adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 7);
            adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 2);
            syncWarPhaseState(false);
            return (guild != null ? guild.getDisplayName() : "Guild crews") + " received the delivered haul and expanded their frontier operations.";
        }
        adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 4);
        adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -4);
        syncWarPhaseState(false);
        return "The public board marked the route resupplied and local services are back online.";
    }

    private String stabilizeConvoyRoute(String zoneId) {
        BaseState baseState = zoneId != null ? baseStatesByZoneId.get(zoneId) : null;
        if (baseState == null) {
            return "";
        }
        BaseRaidState raidState = baseState.getRaidState();
        if (raidState == null) {
            return "";
        }

        int dispersedRaiders = 0;
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.alive && enemy.raidSpawned) {
                enemy.alive = false;
                dispersedRaiders++;
            }
        }

        boolean hadActiveRaid = raidState.isActive();
        float previousThreat = raidState.getThreatLevel();
        raidState.setActive(false);
        raidState.setThreatLevel(Math.max(0f, previousThreat - 0.75f));
        raidState.setCooldownSeconds(Math.max(raidState.getCooldownSeconds(), BASE_RAID_COOLDOWN_SECONDS));
        raidState.setWaveIndex(Math.max(0, raidState.getWaveIndex() - 1));
        adjustFactionInfluence(WarPhaseManager.FACTION_IRONHAVEN_COMMAND, 6);
        adjustFactionInfluence(WarPhaseManager.FACTION_GUILD_COALITION, 3);
        adjustFactionInfluence(WarPhaseManager.FACTION_FRONTIER_HOSTILES, -9);
        syncWarPhaseState(false);

        if (hadActiveRaid || dispersedRaiders > 0) {
            return "Raid lanes disrupted and local pressure pushed back.";
        }
        if (previousThreat >= 0.55f) {
            return "Threat pressure reduced before the next strike window could open.";
        }
        return "Supply route confirmed and outpost reserves restored.";
    }

    private boolean maybeTriggerActOneOutpostBankingTutorial(
        boolean bankedAtOutpostStorage,
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards
    ) {
        if (!bankedAtOutpostStorage || worldStateManager.isFlagActive(gameState, "tutorial.frontier_outpost_banked")) {
            return false;
        }
        if (bankedGold <= 0L && (bankedComponents == null || bankedComponents.isEmpty()) && (bankedShards == null || bankedShards.isEmpty())) {
            return false;
        }
        worldStateManager.setFlag(gameState, "tutorial.frontier_outpost_banked", true);
        autosave();
        showStandaloneDialog(
            "Frontier",
            "Outpost banking confirmed. This foothold now shortens your return route and lets you push deeper before heading back to Ironhaven."
        );
        return true;
    }

    private void maybeTriggerFrontierAnnexResupplyProgress(
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards
    ) {
        if (!isHubTownZone()) {
            return;
        }
        if (!worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")
            || worldStateManager.isFlagActive(gameState, "event.frontier_annex_resupplied")) {
            return;
        }
        if (!"bank_supply".equals(questManager.getQuestState(gameState, "annex_resupply"))) {
            return;
        }
        if (bankedGold <= 0L && (bankedComponents == null || bankedComponents.isEmpty()) && (bankedShards == null || bankedShards.isEmpty())) {
            return;
        }
        worldStateManager.setFlag(gameState, "event.frontier_annex_resupplied", true);
        questManager.syncProgress(gameState, worldStateManager);
        autosave();
    }

    private boolean maybeTriggerActTwoBlueprintTutorial(Map<String, Integer> bankedBlueprints) {
        if (bankedBlueprints == null || bankedBlueprints.isEmpty()) {
            return false;
        }
        if (gameState.getForgeCoreLevel() < 2 || worldStateManager.isFlagActive(gameState, "tutorial.first_blueprint_banked")) {
            return false;
        }
        worldStateManager.setFlag(gameState, "tutorial.first_blueprint_banked", true);
        autosave();
        showStandaloneDialog(
            "Archive",
            "Recovered blueprint fragments have been indexed. Ironhaven's archive and workshop crews can now turn those fragments into better robot, forge, and settlement plans."
        );
        return true;
    }

    private List<String> applyActTwoTownReturnSupport(
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards,
        Map<String, Integer> bankedBlueprints
    ) {
        List<String> lines = new ArrayList<>();
        if (!isHubTownZone()) {
            return lines;
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open")) {
            int reserveCount = 0;
            int drillXp = ActTwoSupportDirector.calculateTrainingGroundDrillXp(
                bankedGold,
                bankedComponents,
                bankedShards,
                bankedBlueprints
            );
            if (drillXp > 0) {
                for (String robotId : getReserveRobotIds()) {
                    RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
                    if (state == null) {
                        continue;
                    }
                    RobotEvolutionManager.addExperience(state, drillXp);
                    reserveCount++;
                }
                if (reserveCount > 0) {
                    lines.add("Training Grounds drilled " + reserveCount + " reserve frame"
                        + (reserveCount == 1 ? "" : "s") + " for +" + drillXp + " XP.");
                }
            }
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock")) {
            int maxPotions = worldStateManager.isFlagActive(gameState, "settlement.experimental_compounds") ? 8
                : worldStateManager.isFlagActive(gameState, "settlement.field_kit_supply") ? 6
                : 4;
            int refillAmount = worldStateManager.isFlagActive(gameState, "settlement.experimental_compounds") ? 2 : 1;
            if (healingPotions < maxPotions && calculateTrainingGroundDrillXp(bankedGold, bankedComponents, bankedShards, bankedBlueprints) > 0) {
                int before = healingPotions;
                gameState.setHealingPotions(Math.min(maxPotions, healingPotions + refillAmount));
                healingPotions = gameState.getHealingPotions();
                if (healingPotions > before) {
                    lines.add("Apothecary packed +" + (healingPotions - before) + " field kit"
                        + (healingPotions - before == 1 ? "" : "s") + " for the next sortie.");
                }
            }
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.hangar_open")) {
            int repaired = 0;
            for (String robotId : getReserveRobotIds()) {
                RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
                if (state == null) {
                    continue;
                }
                RobotStatBlock block = getRobotStatsForRobotId(robotId);
                if (block.maxHealth <= 0f) {
                    continue;
                }
                float currentHealth = state.getCurrentHealth() < 0f ? block.maxHealth : state.getCurrentHealth();
                if (currentHealth + 0.5f < block.maxHealth) {
                    state.setCurrentHealth(block.maxHealth);
                    repaired++;
                }
            }
            if (repaired > 0) {
                lines.add("Hangar crews repaired " + repaired + " reserve frame" + (repaired == 1 ? "" : "s") + " to full readiness.");
            }
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.archive_open")) {
            int decoded = sumInventoryAmounts(bankedBlueprints) + sumInventoryAmounts(bankedShards);
            if (decoded > 0) {
                lines.add("Archive staff indexed " + decoded + " new research artifact" + (decoded == 1 ? "" : "s") + " from this return.");
            }
        }
        return lines;
    }

    private int calculateTrainingGroundDrillXp(
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards,
        Map<String, Integer> bankedBlueprints
    ) {
        return ActTwoSupportDirector.calculateTrainingGroundDrillXp(
            bankedGold,
            bankedComponents,
            bankedShards,
            bankedBlueprints
        );
    }

    private int sumInventoryAmounts(Map<String, Integer> inventory) {
        return ActTwoSupportDirector.sumInventoryAmounts(inventory);
    }

    private String buildBankedHaulSummary(
        long bankedGold,
        Map<String, Integer> bankedComponents,
        Map<String, Integer> bankedShards,
        Map<String, Integer> bankedBlueprints
    ) {
        List<String> parts = new ArrayList<>();
        if (bankedGold > 0L) {
            parts.add(bankedGold + " gold");
        }
        for (Map.Entry<String, Integer> entry : bankedComponents.entrySet()) {
            parts.add(entry.getValue() + " " + getForgeComponentName(entry.getKey()));
        }
        for (Map.Entry<String, Integer> entry : bankedShards.entrySet()) {
            parts.add(entry.getValue() + " " + entry.getKey() + " shards");
        }
        for (Map.Entry<String, Integer> entry : bankedBlueprints.entrySet()) {
            parts.add(entry.getValue() + " " + getBlueprintFragmentName(entry.getKey()));
        }
        return parts.isEmpty() ? "No expedition haul to secure." : "Haul secured: " + String.join(", ", parts) + ".";
    }

    public void addExperience(int amount) {
        int adjustedAmount = Math.max(0, Math.round(amount * getCyberneticBonuses().getExperienceMultiplier()));
        gameState.setPlayerHealth(playerHealth);
        gameState.addExperience(adjustedAmount, this::getExperienceRequirementForLevel);
        playerExperience = gameState.getPlayerExperience();
        playerLevel = gameState.getPlayerLevel();
        playerHealth = gameState.getPlayerHealth();
        refreshHud();
    }

    public int getPlayerLevel() {
        return gameState.getPlayerLevel();
    }

    public int getPlayerExperience() {
        return gameState.getPlayerExperience();
    }

    public int getExperienceForNextLevel() {
        return getExperienceRequirementForLevel(playerLevel);
    }

    private int getExperienceRequirementForLevel(int level) {
        int baseRequirement = 40 + (level * 20);
        switch (difficultyMode) {
            case "EASY":
                return Math.max(20, Math.round(baseRequirement * 0.75f));
            case "HARD":
                return Math.max(20, Math.round(baseRequirement * 1.5f));
            case "HELL":
                return Math.max(20, Math.round(baseRequirement * getHellDifficultyMultiplier(level)));
            case "NORMAL":
            default:
                return baseRequirement;
        }
    }

    public int previewLevelUps(int experienceAmount) {
        int levelsGained = 0;
        int simulatedLevel = playerLevel;
        int simulatedExperience = playerExperience + Math.max(0, applyUniqueExperienceBoost(experienceAmount));
        while (simulatedExperience >= getExperienceRequirementForLevel(simulatedLevel)) {
            simulatedExperience -= getExperienceRequirementForLevel(simulatedLevel);
            simulatedLevel++;
            levelsGained++;
        }
        return levelsGained;
    }

    public String getUnlockedGrade() {
        return gameState.getUnlockedGrade();
    }

    /** Returns the current Forge Core level (1–4). Used to gate robot evolution tiers. */
    public int getForgeCoreLevel() {
        return gameState.getForgeCoreLevel();
    }

    public int getDefeatedBossCount() {
        return gameState.getDefeatedBossCount();
    }

    public RobotStatBlock getPlayerStats() {
        CyberneticBonuses bonuses = getCyberneticBonuses();
        ForgeLegacyBonuses legacyBonuses = getForgeLegacyBonuses();
        EquipmentTotals equipmentTotals = getPlayerEquipmentTotals();
        float levelOffset = playerLevel - 1;
        float maxHealth = (playerMaxHealth + (levelOffset * 6f) + equipmentTotals.hpBonus + bonuses.getHpBonus())
            * legacyBonuses.getMaxHealthMultiplier();
        float strength = (PLAYER_STRENGTH + (levelOffset * 0.7f) + equipmentTotals.strengthBonus + bonuses.getStrengthBonus())
            * legacyBonuses.getAttackMultiplier();
        return new RobotStatBlock(
            playerHealth,
            maxHealth,
            PLAYER_AGILITY + (levelOffset * 0.5f) + equipmentTotals.agilityBonus + bonuses.getAgilityBonus(),
            strength,
            PLAYER_INTELLIGENCE + (levelOffset * 0.65f) + equipmentTotals.intelligenceBonus + bonuses.getIntelligenceBonus(),
            PLAYER_STAMINA + (levelOffset * 0.6f) + equipmentTotals.staminaBonus + bonuses.getStaminaBonus()
        );
    }

    private CyberneticBonuses getCyberneticBonuses() {
        return cyberneticEnhancementEngine.getBonuses(metaProgressionState);
    }

    private ForgeLegacyBonuses getForgeLegacyBonuses() {
        return forgeLegacyEngine.getBonuses(metaProgressionState);
    }

    private void applyMetaEnhancementsToFreshRun() {
        CyberneticBonuses bonuses = getCyberneticBonuses();
        ForgeLegacyBonuses legacyBonuses = getForgeLegacyBonuses();
        if (bonuses.getStartingGoldBonus() > 0) {
            addGold(bonuses.getStartingGoldBonus());
        }
        if (legacyBonuses.getStartingGoldBonus() > 0) {
            addGold(legacyBonuses.getStartingGoldBonus());
        }
        gameState.setHealingPotions(Math.max(0,
            healingPotions + bonuses.getStartingHealingPotionsBonus() + legacyBonuses.getStartingPotionBonus()));
        healingPotions = gameState.getHealingPotions();
        for (Map.Entry<String, Integer> entry : bonuses.getStartingForgeComponents().entrySet()) {
            gameState.addForgeComponent(entry.getKey(), entry.getValue());
        }
        playerHealth = getPlayerStats().maxHealth;
        refreshHud();
    }

    public String getPlayerName() {
        return playerName;
    }

    public Map<String, String> getPlayerEquipmentSlots() {
        return gameState.getPlayerEquipmentSlots();
    }

    public boolean equipPlayerItem(EquipmentItem item) {
        if (!gameState.equipPlayerItem(item)) {
            return false;
        }
        playerEquipment = gameState.getPlayerEquipmentSlots();
        playerHealth = Math.min(getPlayerStats().maxHealth, playerHealth);
        refreshHud();
        return true;
    }

    public int getRobotCount() {
        return ROBOT_COUNT;
    }

    public List<String> getReserveRobotLines() {
        List<String> lines = new ArrayList<>();
        for (String robotId : getReserveRobotIds()) {
            RobotProgressionState state = getOrCreateRobotProgressionState(robotId);
            String name = state != null && state.getDisplayName() != null && !state.getDisplayName().isEmpty()
                ? state.getDisplayName()
                : robotId;
            int level = state != null ? state.getLevel() : 1;
            int tier = state != null ? state.getEvolutionTier() : 1;
            lines.add(name + " [" + getRobotClassForRobotId(robotId) + "] Lv." + level + " Evo " + tier);
        }
        return lines;
    }

    public List<String> getReserveRobotIds() {
        List<String> reserveIds = new ArrayList<>();
        for (String robotId : collectedRobotIds) {
            if (!activeRobotIds.contains(robotId)) {
                reserveIds.add(robotId);
            }
        }
        return reserveIds;
    }

    public List<String> getQuestJournalLines() {
        questManager.syncProgress(gameState, worldStateManager);
        List<String> lines = new ArrayList<>();
        lines.addAll(getActFourCampaignStatusLines());
        lines.addAll(questManager.getActiveQuestLines(gameState, true));
        lines.addAll(questManager.getActiveQuestLines(gameState, false));
        return lines;
    }

    public List<String> getSettlementUpgradeLines() {
        List<String> lines = new ArrayList<>();
        for (com.rogueforge.game.world.SettlementUpgradeDefinition definition : settlementManager.getAll()) {
            SettlementState state = gameState.getSettlementUpgrade(definition.getId());
            if (state == null || state.getLevel() <= 0) {
                continue;
            }
            lines.add(definition.getName() + " Lv." + state.getLevel());
        }
        return lines;
    }

    public List<String> getEndgameProgressionLines() {
        List<String> lines = new ArrayList<>();
        ForgeLegacyNodeDefinition nextNode = forgeLegacyEngine.getNextLockedNode(metaProgressionState);
        lines.add("Forge Shards: " + metaProgressionState.getForgeShards());
        lines.add("Infinite Dungeon best floor: " + gameState.getInfiniteDungeonBestFloor());
        lines.add(nextNode != null
            ? "Next Legacy node: " + nextNode.getName() + " (" + nextNode.getCost() + ")"
            : "Legacy tree: all current nodes secured");
        return lines;
    }

    public List<String> getEmpireStatusLines() {
        List<String> lines = new ArrayList<>();
        int territories = getTotalClaimedTerritories();
        int guildSettlements = getGuildSettlementCount();
        WarPhaseSnapshot snapshot = getCurrentWarPhaseSnapshot();
        lines.add("Empire tier: " + forgeLegacyEngine.describeEmpireTier(territories, guildSettlements));
        lines.add("Territories held: " + territories);
        lines.add("Guild settlements: " + guildSettlements);
        lines.add("Player-created NPCs: " + playerCreatedNpcs.size());
        lines.add("World event: " + getActiveWorldEventName());
        if (snapshot.isUnlocked()) {
            lines.add("Strategic map: territory " + snapshot.getTerritoryInfluence() + "%  |  risk " + snapshot.getSettlementAttackRisk()
                + "%  |  fronts " + snapshot.getWorldBossFrontCount() + ".");
            lines.add("Strategic map: convoy lanes " + snapshot.getConvoyRouteCount() + "  |  expeditions "
                + snapshot.getLargeExpeditionCount() + "  |  boards " + snapshot.getPlayerQuestBoardCount() + ".");
        }
        List<String> projectLines = getActiveSettlementProjectLines();
        if (!projectLines.isEmpty()) {
            lines.add(projectLines.get(0));
        }
        return lines;
    }

    public List<String> getChallengeUnlockLines() {
        List<String> lines = new ArrayList<>();
        int bestFloor = gameState.getInfiniteDungeonBestFloor();
        int defeatedEndgameBosses = getDefeatedEndgameBossCount();
        lines.add("Legendary robots: " + metaProgressionState.getUnlockedLegendaryRobotIds().size() + "/3");
        lines.add("Challenge runs: " + (forgeLegacyEngine.areChallengeRunsUnlocked(bestFloor) ? "Unlocked [" + getSelectedChallengeModifierLabel() + "]" : "Locked"));
        lines.add("Boss rush: " + (forgeLegacyEngine.isBossRushUnlocked(defeatedEndgameBosses) ? "Unlocked" : defeatedEndgameBosses + "/2 bosses"));
        lines.add("Hard mode seeds: " + forgeLegacyEngine.getHardModeSeedsUnlocked(bestFloor) + " unlocked  |  Active seed " + getSelectedHardModeSeedLabel());
        return lines;
    }

    public List<String> getUnlockedServiceLines() {
        List<String> lines = new ArrayList<>();
        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools")) {
            lines.add("Workshop stocks advanced forge components.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock")) {
            lines.add("Apothecary keeps expanded repair-kit stock.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.watchtower_network")) {
            lines.add("Watchtower scouts mark safer frontier routes.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.survey_drones")) {
            lines.add("Survey drones unlock higher-tier town inventory.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")) {
            lines.add("Frontier Annex crews now bank and reroute salvage through Ironhaven.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.tavern_open")) {
            lines.add("Tavern rumors now support longer expedition planning.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.hangar_open")) {
            lines.add("Hangar crews keep reserve robots staged for deployment.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.archive_open")) {
            lines.add("Archive staff can turn field scans and notes into usable knowledge.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open")) {
            lines.add("Training grounds now support disciplined team drills.");
        }
        if (isCommandBastionProjectActive()) {
            lines.add("Command Bastion Network is granting extra reputation on major war contracts.");
        }
        if (isCoalitionExchangeProjectActive()) {
            lines.add("Coalition Exchange is increasing convoy, delivery, and supply payouts.");
        }
        if (isArchiveWarCollegeProjectActive()) {
            lines.add("Archive War College is turning expeditions into additional shard and command insight gains.");
        }
        if (hasTownServiceSurplus()) {
            lines.add("War surplus: stabilized corridors are improving stock rotation, kit supply, and town response speed.");
        }
        if (hasTownServiceStrain()) {
            lines.add("War strain: chained crises are dragging on prices, resupply cadence, and reserve readiness.");
        }
        String dominantFactionId = getDominantWarFactionId();
        if (WarPhaseManager.FACTION_IRONHAVEN_COMMAND.equals(dominantFactionId)) {
            lines.add("Command posture: facilities are prioritizing disciplined field kits, strike readiness, and fortified route support.");
        } else if (WarPhaseManager.FACTION_GUILD_COALITION.equals(dominantFactionId)) {
            lines.add("Guild posture: facilities are prioritizing flexible stock, contract traffic, and richer frontier resale flow.");
        } else {
            lines.add("Hostile pressure posture: facilities are operating defensively and trimming nonessential inventory.");
        }
        String dispatchZoneId = getPrimaryWarDispatchZone();
        if (dispatchZoneId != null) {
            lines.add("Regional bias: Ironhaven is currently stocking around " + getZoneDisplayName(dispatchZoneId) + " and its live theater demands.");
        }
        return lines;
    }

    public List<String> getTownChangeLines() {
        List<String> lines = new ArrayList<>();
        if (worldStateManager.isFlagActive(gameState, "settlement.workshop_tools")) {
            lines.add("Forge rail crates and workshop signage are now visible.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.apothecary_stock")) {
            lines.add("A supply stall now stands outside the herbalist.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.watchtower_network")) {
            lines.add("A lit watchtower link beacon now marks the lodge road.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.survey_drones")) {
            lines.add("Survey drone pads now line the workshop yard.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.frontier_annex")) {
            lines.add("A frontier annex and depot counter now stand beside the east gate.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.tavern_open")) {
            lines.add("A proper tavern now anchors the west lane of Ironhaven.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.hangar_open")) {
            lines.add("A robot hangar now sits ready near the lower yard.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.archive_open")) {
            lines.add("An archive desk now catalogs frontier scans and recovered designs.");
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.training_grounds_open")) {
            lines.add("A training ground now marks Ironhaven's central drill yard.");
        }
        return lines;
    }

    public String deployReserveRobotToSlot(int partyIndex, int reserveIndex) {
        if (partyIndex < 0 || partyIndex >= activeRobotIds.size()) {
            return "Choose a robot slot first.";
        }
        // Guard: party slot not yet unlocked by Forge Core / grade progression.
        if (partyIndex >= getPartySlotLimit()) {
            String req = getPartySlotNextGrade();
            return "Slot " + (partyIndex + 1) + " is locked."
                + (req != null ? " Requires " + req + " to unlock." : "");
        }
        List<String> reserveIds = getReserveRobotIds();
        if (reserveIndex < 0 || reserveIndex >= reserveIds.size()) {
            return "That reserve frame is unavailable.";
        }

        String incomingRobotId = reserveIds.get(reserveIndex);
        String outgoingRobotId = activeRobotIds.get(partyIndex);
        if (incomingRobotId == null || incomingRobotId.equals(outgoingRobotId)) {
            return "That frame is already deployed.";
        }

        activeRobotIds.set(partyIndex, incomingRobotId);
        gameState.setActiveRobotIds(activeRobotIds);
        ensureRobotProgressionStates();
        robots[partyIndex].health = getStoredRobotHealth(incomingRobotId, getRobotStats(partyIndex).maxHealth);
        syncActiveRobotHealthToProgression();
        syncCurrentZoneBaseDefenders();
        refreshHud();

        String incomingName = getRobotName(partyIndex);
        RobotProgressionState outgoingState = getOrCreateRobotProgressionState(outgoingRobotId);
        if (outgoingState != null) {
            outgoingState.setCurrentHealth(Math.max(0f, Math.min(getRobotStats(partyIndex).maxHealth, robots[partyIndex].health)));
        }
        String outgoingName = outgoingState != null && outgoingState.getDisplayName() != null && !outgoingState.getDisplayName().isEmpty()
            ? outgoingState.getDisplayName()
            : outgoingRobotId;
        if (outgoingRobotId == null || outgoingRobotId.isEmpty()) {
            return incomingName + " deployed to slot " + (partyIndex + 1) + ".";
        }
        return incomingName + " deployed to slot " + (partyIndex + 1) + ", replacing " + outgoingName + ".";
    }

    public String moveRobotToReserve(int partyIndex) {
        if (!hasActiveRobotAt(partyIndex)) {
            return "That slot is already empty.";
        }
        String outgoingRobotId = activeRobotIds.get(partyIndex);
        RobotProgressionState outgoingState = getOrCreateRobotProgressionState(outgoingRobotId);
        String outgoingName = outgoingState != null && outgoingState.getDisplayName() != null && !outgoingState.getDisplayName().isEmpty()
            ? outgoingState.getDisplayName()
            : outgoingRobotId;
        activeRobotIds.set(partyIndex, null);
        gameState.setActiveRobotIds(activeRobotIds);
        syncCurrentZoneBaseDefenders();
        refreshHud();
        return outgoingName + " moved to reserve. Slot " + (partyIndex + 1) + " is now empty.";
    }

    public String getRobotName(int index) {
        if (!hasActiveRobotAt(index)) {
            return "Empty Slot";
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        if (progressionState != null && progressionState.getDisplayName() != null && !progressionState.getDisplayName().isEmpty()) {
            return progressionState.getDisplayName();
        }
        String robotId = index >= 0 && index < activeRobotIds.size() ? activeRobotIds.get(index) : null;
        RobotDefinition definition = robotId != null ? robotDefinitions.get(robotId) : null;
        if (definition != null && definition.getName() != null && !definition.getName().isEmpty()) {
            return definition.getName();
        }
        return "Bot " + (index + 1);
    }

    public String getRobotGrade(int index) {
        if (!hasActiveRobotAt(index)) {
            return "-";
        }
        return robots[index].grade;
    }

    public int getRobotLevel(int index) {
        if (!hasActiveRobotAt(index)) {
            return 0;
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getLevel() : 1;
    }

    public int getRobotExperience(int index) {
        if (!hasActiveRobotAt(index)) {
            return 0;
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getExperience() : 0;
    }

    public int getRobotEvolutionTier(int index) {
        if (!hasActiveRobotAt(index)) {
            return 0;
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getEvolutionTier() : 1;
    }

    public String getRobotClass(int index) {
        if (!hasActiveRobotAt(index)) {
            return "-";
        }
        return getRobotClassForRobotId(getRobotId(index));
    }

    public List<String> getRobotAbilityProgressionLines(int index) {
        List<String> lines = new ArrayList<>();
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        if (progressionState == null || progressionState.getKnownAbilityIds() == null) {
            return lines;
        }
        for (String abilityId : progressionState.getKnownAbilityIds()) {
            AbilityProgressionState abilityState = progressionState.getAbilityProgression().get(abilityId);
            AbilityDefinition definition = AbilityRegistry.get(abilityId);
            String abilityName = definition != null ? definition.getName() : abilityId;
            int level = abilityState != null ? abilityState.getProficiencyLevel() : 1;
            int xp = abilityState != null ? abilityState.getProficiencyXp() : 0;
            lines.add(abilityName + " Lv." + level + " (" + xp + " XP)");
        }
        return lines;
    }

    public List<String> getRobotWeaponProgressionLines(int index) {
        List<String> lines = new ArrayList<>();
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        if (progressionState == null) {
            return lines;
        }
        for (WeaponProficiencyState state : progressionState.getWeaponProficiencies().values()) {
            if (state == null || state.getWeaponType() == null || state.getWeaponType().isEmpty()) {
                continue;
            }
            lines.add(state.getWeaponType() + " Lv." + state.getLevel() + " (" + state.getXp() + " XP)");
        }
        return lines;
    }

    public RobotStatBlock getRobotStats(int index) {
        if (!hasActiveRobotAt(index)) {
            return new RobotStatBlock(0f, 0f, 0f, 0f, 0f, 0f);
        }
        RobotCompanion robot = robots[index];
        var robotVitals = robot.vitals();
        var robotCombatStats = robot.combatStats();
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        EquipmentTotals equipmentTotals = getEquipmentTotals(index);
        ForgeLegacyBonuses legacyBonuses = getForgeLegacyBonuses();
        int robotLevel = progressionState != null ? progressionState.getLevel() : 1;
        int evolutionTier = progressionState != null ? progressionState.getEvolutionTier() : 1;
        float levelBonus = Math.max(0f, (robotLevel - 1) * RobotEvolutionManager.levelBonusPerLevel());
        float evolutionMultiplier = RobotEvolutionManager.statMultiplier(evolutionTier);
        float maxHealth = getRobotEffectiveMaxHealth(getRobotId(index), progressionState);
        RobotStatBlock block = new RobotStatBlock(
            robotVitals.health,
            maxHealth,
            (robotCombatStats.agility + levelBonus) * evolutionMultiplier + equipmentTotals.agilityBonus + legacyBonuses.getRobotSpeedBonus(),
            (robotCombatStats.strength + levelBonus) * evolutionMultiplier + equipmentTotals.strengthBonus,
            (robotCombatStats.intelligence + levelBonus) * evolutionMultiplier + equipmentTotals.intelligenceBonus,
            (robotCombatStats.stamina + levelBonus) * evolutionMultiplier + equipmentTotals.staminaBonus
        );
        applyRobotClassBonuses(block, getRobotClass(index));
        block.currentHealth = Math.min(block.maxHealth, block.currentHealth);
        return block;
    }

    private void applyRobotClassBonuses(RobotStatBlock block, String robotClass) {
        if (block == null || robotClass == null) {
            return;
        }
        String[] classes = robotClass.split("/");
        float weight = classes.length > 1 ? 0.6f : 1f;
        for (String value : classes) {
            applySingleClassBonus(block, value.trim(), weight);
        }
    }

    private void applySingleClassBonus(RobotStatBlock block, String robotClass, float weight) {
        switch (robotClass) {
            case "Vanguard":
                block.maxHealth *= (1f + (0.08f * weight));
                block.stamina *= (1f + (0.1f * weight));
                block.agility *= (1f - (0.03f * weight));
                break;
            case "Striker":
                block.strength *= (1f + (0.1f * weight));
                block.agility *= (1f + (0.04f * weight));
                block.stamina *= (1f - (0.05f * weight));
                break;
            case "Support":
                block.maxHealth *= (1f + (0.05f * weight));
                block.intelligence *= (1f + (0.12f * weight));
                block.strength *= (1f - (0.05f * weight));
                break;
            case "Scout":
                block.maxHealth *= (1f - (0.06f * weight));
                block.agility *= (1f + (0.12f * weight));
                block.strength *= (1f + (0.04f * weight));
                break;
            default:
                break;
        }
    }

    private String getRobotClassForRobotId(String robotId) {
        RobotDefinition definition = robotId != null ? robotDefinitions.get(robotId) : null;
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Vanguard", 0);
        scores.put("Striker", 0);
        scores.put("Support", 0);
        scores.put("Scout", 0);
        applyBaseRoleScore(scores, definition);

        Map<String, String> equipped = robotId != null ? robotEquipment.get(robotId) : null;
        if (equipped != null) {
            for (String itemId : equipped.values()) {
                EquipmentItem item = findEquipmentItem(itemId);
                if (item == null) {
                    continue;
                }
                scoreEquipmentForClass(scores, item);
            }
        }

        String defaultClass = defaultClassForRole(definition);
        String primaryClass = defaultClass;
        String secondaryClass = defaultClass;
        int bestScore = Integer.MIN_VALUE;
        int secondBestScore = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            int value = entry.getValue();
            if (value > bestScore || (value == bestScore && entry.getKey().equals(defaultClass))) {
                secondBestScore = bestScore;
                secondaryClass = primaryClass;
                bestScore = value;
                primaryClass = entry.getKey();
            } else if (value > secondBestScore || (value == secondBestScore && entry.getKey().equals(defaultClass))) {
                secondBestScore = value;
                secondaryClass = entry.getKey();
            }
        }
        if (!primaryClass.equals(secondaryClass) && shouldHybridize(bestScore, secondBestScore)) {
            return primaryClass + "/" + secondaryClass;
        }
        return primaryClass;
    }

    private boolean shouldHybridize(int bestScore, int secondBestScore) {
        return secondBestScore >= 4 && (bestScore - secondBestScore) <= 1;
    }

    private void applyBaseRoleScore(Map<String, Integer> scores, RobotDefinition definition) {
        if (scores == null || definition == null || definition.getRole() == null) {
            return;
        }
        scores.put(defaultClassForRole(definition), scores.get(defaultClassForRole(definition)) + 2);
    }

    private String defaultClassForRole(RobotDefinition definition) {
        if (definition == null || definition.getRole() == null) {
            return "Striker";
        }
        switch (definition.getRole()) {
            case TANK:
                return "Vanguard";
            case SUPPORT:
                return "Support";
            case SCOUT:
                return "Scout";
            case DPS:
            default:
                return "Striker";
        }
    }

    private void scoreEquipmentForClass(Map<String, Integer> scores, EquipmentItem item) {
        if (scores == null || item == null) {
            return;
        }
        switch (item.getSlotType()) {
            case "BODY":
                scores.put("Vanguard", scores.get("Vanguard") + 3);
                break;
            case "ARMS":
                scores.put("Striker", scores.get("Striker") + 3);
                break;
            case "HEAD":
                scores.put("Support", scores.get("Support") + 3);
                break;
            case "LEGS":
                scores.put("Scout", scores.get("Scout") + 3);
                break;
            default:
                break;
        }

        switch (item.getWeaponType()) {
            case STAFF:
                scores.put("Support", scores.get("Support") + 2);
                break;
            case GUN:
            case BOW:
            case DUAL_BLADE:
                scores.put("Scout", scores.get("Scout") + 2);
                break;
            case SWORD:
            case AXE:
            case LANCE:
            case FIST:
                scores.put("Striker", scores.get("Striker") + 2);
                break;
            default:
                break;
        }

        if (item.getHpBonus() >= 20 || item.getDefenseBonus() >= 10) {
            scores.put("Vanguard", scores.get("Vanguard") + 1);
        }
        if (item.getAttackBonus() >= 8) {
            scores.put("Striker", scores.get("Striker") + 1);
        }
        if (item.getIntelligenceBonus() >= 8) {
            scores.put("Support", scores.get("Support") + 1);
        }
        if (item.getSpeedBonus() >= 8) {
            scores.put("Scout", scores.get("Scout") + 1);
        }

        switch (item.getUniqueBoost()) {
            case "BARRIER_MATRIX":
                scores.put("Vanguard", scores.get("Vanguard") + 3);
                break;
            case "COUNTER_FIELD":
                scores.put("Vanguard", scores.get("Vanguard") + 2);
                scores.put("Striker", scores.get("Striker") + 1);
                break;
            case "LIFE_TAP":
            case "OVERDRIVE_LINK":
                scores.put("Striker", scores.get("Striker") + 3);
                break;
            case "AUTO_REPAIR":
                scores.put("Support", scores.get("Support") + 3);
                break;
            case "ARCANE_SURGE":
            case "XP_BOOST":
                scores.put("Support", scores.get("Support") + 2);
                break;
            case "FIRST_STRIKE":
                scores.put("Scout", scores.get("Scout") + 2);
                break;
            default:
                break;
        }
    }

    private void ensureClassMasteryAbilities(String robotId, RobotProgressionState state) {
        if (robotId == null || robotId.isEmpty() || state == null) {
            return;
        }
        String[] classes = getRobotClassForRobotId(robotId).split("/");
        for (String robotClass : classes) {
            grantClassAbility(state, robotClass.trim());
        }
    }

    private void grantClassAbility(RobotProgressionState state, String robotClass) {
        String abilityId = classAbilityId(robotClass);
        if (abilityId.isEmpty()) {
            return;
        }
        if (!state.getKnownAbilityIds().contains(abilityId)) {
            state.getKnownAbilityIds().add(abilityId);
        }
        state.getOrCreateAbilityProgression(abilityId);
    }

    private String classAbilityId(String robotClass) {
        switch (robotClass) {
            case "Vanguard":
                return "shield_wall";
            case "Striker":
                return "power_strike";
            case "Support":
                return "repair_aura";
            case "Scout":
                return "dash";
            default:
                return "";
        }
    }

    public List<EquipmentItem> getEquipmentCatalog() {
        List<EquipmentItem> owned = new ArrayList<>();
        for (EquipmentItem item : equipmentCatalog) {
            if (gameState.getOwnedEquipmentIds().contains(item.getId())) {
                owned.add(item);
            }
        }
        return owned;
    }

    public List<EquipmentItem> getFullEquipmentCatalog() {
        return new ArrayList<>(equipmentCatalog);
    }

    public List<EquipmentItem> getPlayerEquipmentCatalog() {
        List<EquipmentItem> owned = new ArrayList<>();
        for (EquipmentItem item : getEquipmentCatalog()) {
            if (item.isPlayerEquipment()) {
                owned.add(item);
            }
        }
        return owned;
    }

    public List<EquipmentItem> getRobotEquipmentCatalog() {
        List<EquipmentItem> owned = new ArrayList<>();
        for (EquipmentItem item : getEquipmentCatalog()) {
            if (item.isRobotEquipment()) {
                owned.add(item);
            }
        }
        return owned;
    }

    public List<EquipmentItem> getEquipmentCatalogForPartyMember(int partyMemberIndex) {
        return partyMemberIndex == 0 ? getPlayerEquipmentCatalog() : getRobotEquipmentCatalog();
    }

    public boolean unlockEquipment(String itemId) {
        boolean unlocked = gameState.unlockEquipment(itemId);
        ownedEquipmentIds.clear();
        ownedEquipmentIds.addAll(gameState.getOwnedEquipmentIds());
        return unlocked;
    }

    /**
     * Returns true if the item is currently slotted into any party member's
     * equipment (player or any robot). Used by the fusion system to prevent
     * fusing actively equipped gear.
     */
    public boolean isEquippedByAnyone(String itemId) {
        if (itemId == null) return false;
        if (playerEquipment.containsValue(itemId)) return true;
        for (Map<String, String> slots : robotEquipment.values()) {
            if (slots != null && slots.containsValue(itemId)) return true;
        }
        return false;
    }

    /**
     * Removes an item from the owned-equipment list. Does NOT unequip it —
     * callers must confirm the item is not equipped before calling this.
     */
    private void removeOwnedEquipment(String itemId) {
        if (itemId == null) return;
        ownedEquipmentIds.remove(itemId);
        List<String> updated = new ArrayList<>(gameState.getOwnedEquipmentIds());
        updated.remove(itemId);
        gameState.setOwnedEquipmentIds(updated);
    }

    /**
     * Fuses two owned equipment items of the same tier and slot type into a
     * randomly chosen item one tier higher.
     *
     * <p>Rules:
     * <ul>
     *   <li>Both items must be owned and not currently equipped.</li>
     *   <li>They must share the same tier (1-5) and slot type.</li>
     *   <li>Gold cost = {@code inputTier * 100} gold.</li>
     *   <li>Both input items are consumed on success.</li>
     *   <li>A random Tier+1 item of the same slot is awarded.</li>
     * </ul>
     *
     * @return a player-readable result message
     */
    public String fuseEquipment(String itemId1, String itemId2) {
        if (!isFusionForgeUnlocked()) {
            return "Fusion stays offline until Master Silas restores the fusion cradle.";
        }
        if (itemId1 == null || itemId2 == null || itemId1.equals(itemId2)) {
            return "Select two different items to fuse.";
        }
        List<String> owned = gameState.getOwnedEquipmentIds();
        if (!owned.contains(itemId1) || !owned.contains(itemId2)) {
            return "Both items must be in your collection.";
        }
        EquipmentItem item1 = findEquipmentItem(itemId1);
        EquipmentItem item2 = findEquipmentItem(itemId2);
        if (item1 == null || item2 == null) {
            return "Unknown equipment.";
        }
        if (item1.getTier() != item2.getTier()) {
            return "Items must be the same tier ("
                + item1.getName() + " is T" + item1.getTier()
                + ", " + item2.getName() + " is T" + item2.getTier() + ").";
        }
        if (!item1.getSlotType().equals(item2.getSlotType())) {
            return "Items must fit the same slot ("
                + item1.getSlotType() + " vs " + item2.getSlotType() + ").";
        }
        if (!item1.getEquipTarget().equals(item2.getEquipTarget())) {
            return "Player gear and robot gear cannot be fused together.";
        }
        int inputTier = item1.getTier();
        if (inputTier >= 6) {
            return "Mythic-tier items cannot be fused further.";
        }
        if (isEquippedByAnyone(itemId1) || isEquippedByAnyone(itemId2)) {
            return "Unequip both items before fusing.";
        }
        // Build result pool: all catalog items of tier+1 in the same slot.
        List<EquipmentItem> pool = new ArrayList<>();
        for (EquipmentItem candidate : equipmentCatalog) {
            if (candidate.getTier() == inputTier + 1
                    && candidate.getSlotType().equals(item1.getSlotType())
                    && candidate.getEquipTarget().equals(item1.getEquipTarget())) {
                pool.add(candidate);
            }
        }
        if (pool.isEmpty()) {
            return "No fusion result available for this combination.";
        }
        int goldCost = inputTier * 100;
        if (!spendGold(goldCost)) {
            return "Fusion requires " + goldCost + " gold.";
        }
        removeOwnedEquipment(itemId1);
        removeOwnedEquipment(itemId2);
        EquipmentItem result = pool.get((int) (Math.random() * pool.size()));
        unlockEquipment(result.getId());
        return "Fusion complete: " + result.getName()
            + " (T" + result.getTier() + " " + result.getSlotType() + ") obtained!";
    }

    public List<ForgeRecipeDefinition> getForgeRecipes() {
        List<ForgeRecipeDefinition> visible = new ArrayList<>();
        for (ForgeRecipeDefinition recipe : forgeRecipes) {
            if (isForgeRecipeUnlocked(recipe)) {
                visible.add(recipe);
            }
        }
        return visible;
    }

    public boolean isForgeWorkshopUnlocked() {
        return worldStateManager.isFlagActive(gameState, "settlement.workshop_tools");
    }

    public boolean isFusionForgeUnlocked() {
        return worldStateManager.isFlagActive(gameState, "settlement.fusion_forge");
    }

    public boolean isAdvancedCraftingUnlocked() {
        return worldStateManager.isFlagActive(gameState, "settlement.prototype_lab");
    }

    public String getFusionForgeLockedReason() {
        return "Fusion requires the Fusion Forge settlement upgrade.";
    }

    public String getAdvancedCraftingLockedReason() {
        return "Tier IV forge patterns require the Prototype Lab.";
    }

    public Map<String, Integer> getForgeComponentInventory() {
        return gameState.getForgeComponents();
    }

    public Map<String, Integer> getShardInventory() {
        return gameState.getShardInventory();
    }

    public int getShardCount(String grade) {
        return gameState.getShardCount(grade);
    }

    public int getForgeComponentCount(String componentId) {
        return gameState.getForgeComponentCount(componentId);
    }

    public String getForgeComponentName(String componentId) {
        ForgeComponentDefinition definition = forgeComponentDefinitions.get(componentId);
        return definition != null ? definition.getName() : componentId;
    }

    public String getForgeComponentDescription(String componentId) {
        ForgeComponentDefinition definition = forgeComponentDefinitions.get(componentId);
        return definition != null ? definition.getDescription() : "";
    }

    public List<String> getForgeInventoryLines() {
        List<String> lines = new ArrayList<>();
        for (ForgeComponentDefinition definition : forgeComponentDefinitions.values()) {
            int count = getForgeComponentCount(definition.getId());
            int unbankedCount = gameState.getUnbankedForgeComponentCount(definition.getId());
            if (count > 0) {
                lines.add(definition.getName() + " x" + count
                    + (unbankedCount > 0 ? "  |  Field haul +" + unbankedCount : "")
                    + " [" + definition.getRarity() + "] (Sell "
                    + getForgeComponentSellValue(definition.getId()) + "g)");
            } else if (unbankedCount > 0) {
                lines.add(definition.getName() + " x0  |  Field haul +" + unbankedCount
                    + " [" + definition.getRarity() + "] (Sell "
                    + getForgeComponentSellValue(definition.getId()) + "g)");
            }
        }
        if (lines.isEmpty()) {
            lines.add("No forge components collected yet.");
        }
        return lines;
    }

    public List<String> getMaterialInventoryLines() {
        return getForgeInventoryLines();
    }

    public List<String> getForgeSellableComponentIds() {
        List<String> ids = new ArrayList<>();
        for (ForgeComponentDefinition definition : forgeComponentDefinitions.values()) {
            if (getForgeComponentCount(definition.getId()) > 0) {
                ids.add(definition.getId());
            }
        }
        return ids;
    }

    public List<String> getShardInventoryLines() {
        List<String> lines = new ArrayList<>();
        String[] grades = {"S", "A", "B", "C", "D", "E", "F", "G"};
        for (String grade : grades) {
            int count = getShardCount(grade);
            int unbankedCount = gameState.getUnbankedShardCount(grade);
            if (count > 0) {
                lines.add(grade + "-Grade Shard x" + count
                    + (unbankedCount > 0 ? "  |  Field haul +" + unbankedCount : "")
                    + " (Sell " + getShardSellValue(grade) + "g)");
            } else if (unbankedCount > 0) {
                lines.add(grade + "-Grade Shard x0  |  Field haul +" + unbankedCount
                    + " (Sell " + getShardSellValue(grade) + "g)");
            }
        }
        if (lines.isEmpty()) {
            lines.add("No graded shards recovered yet.");
        }
        return lines;
    }

    public String getBlueprintFragmentName(String fragmentId) {
        BlueprintFragmentDefinition definition = blueprintFragmentDefinitions.get(fragmentId);
        return definition != null ? definition.getName() : fragmentId;
    }

    public String getBlueprintFragmentDescription(String fragmentId) {
        BlueprintFragmentDefinition definition = blueprintFragmentDefinitions.get(fragmentId);
        return definition != null ? definition.getDescription() : "";
    }

    public List<String> getBlueprintFragmentInventoryLines() {
        List<String> lines = new ArrayList<>();
        for (BlueprintFragmentDefinition definition : blueprintFragmentDefinitions.values()) {
            int count = gameState.getBlueprintFragmentCount(definition.getId());
            int unbankedCount = gameState.getUnbankedBlueprintFragmentCount(definition.getId());
            if (count > 0) {
                lines.add(definition.getName() + " x" + count
                    + (unbankedCount > 0 ? "  |  Field haul +" + unbankedCount : "")
                    + "  |  " + definition.getSourceHint());
            } else if (unbankedCount > 0) {
                lines.add(definition.getName() + " x0  |  Field haul +" + unbankedCount
                    + "  |  " + definition.getSourceHint());
            }
        }
        if (lines.isEmpty()) {
            lines.add("No blueprint fragments recovered yet.");
        }
        return lines;
    }

    public List<String> getItemInventoryLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Repair Kits x" + Math.max(0, getHealingPotions()));
        long unbankedGold = gameState.getUnbankedGold();
        lines.add("Unbanked Gold x" + Math.max(0L, unbankedGold));
        List<String> currentKeyItems = gameState.getKeyItems();
        if (!currentKeyItems.isEmpty()) {
            lines.add("Key Items");
            for (String item : currentKeyItems) {
                if (item != null && !item.isEmpty()) {
                    lines.add("- " + item);
                }
            }
        }
        return lines;
    }

    public boolean canForgeRecipe(ForgeRecipeDefinition recipe) {
        if (recipe == null || recipe.getResultEquipmentId() == null) {
            return false;
        }
        if (!isForgeRecipeUnlocked(recipe)) {
            return false;
        }
        if (findEquipmentItem(recipe.getResultEquipmentId()) == null) {
            return false;
        }
        if (gameState.getOwnedEquipmentIds().contains(recipe.getResultEquipmentId())) {
            return false;
        }
        if (gameState.getTotalGold() < recipe.getGoldCost()) {
            return false;
        }
        if (getShardCount(recipe.getShardGrade()) < recipe.getShardCost()) {
            return false;
        }
        for (ForgeIngredientDefinition ingredient : recipe.getIngredients()) {
            if (ingredient == null || getForgeComponentCount(ingredient.getComponentId()) < ingredient.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    public String buildForgeRequirementLine(ForgeRecipeDefinition recipe) {
        if (recipe == null) {
            return "";
        }
        if (!isForgeRecipeUnlocked(recipe)) {
            return getForgeRecipeUnlockReason(recipe);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Gold ").append(recipe.getGoldCost());
        builder.append("  ");
        builder.append(recipe.getShardGrade()).append("-Shard ");
        builder.append(getShardCount(recipe.getShardGrade())).append("/").append(recipe.getShardCost());
        for (ForgeIngredientDefinition ingredient : recipe.getIngredients()) {
            if (ingredient == null) {
                continue;
            }
            builder.append("  ");
            builder.append(getForgeComponentName(ingredient.getComponentId()));
            builder.append(" ");
            builder.append(getForgeComponentCount(ingredient.getComponentId()));
            builder.append("/");
            builder.append(ingredient.getQuantity());
        }
        return builder.toString();
    }

    public String forgeRecipe(int recipeIndex) {
        List<ForgeRecipeDefinition> visibleRecipes = getForgeRecipes();
        if (recipeIndex < 0 || recipeIndex >= visibleRecipes.size()) {
            return "That forge pattern is unavailable.";
        }
        ForgeRecipeDefinition recipe = visibleRecipes.get(recipeIndex);
        EquipmentItem result = findEquipmentItem(recipe.getResultEquipmentId());
        if (result == null) {
            return "The forge pattern is incomplete.";
        }
        if (gameState.getOwnedEquipmentIds().contains(result.getId())) {
            return result.getName() + " has already been forged.";
        }
        if (gameState.getTotalGold() < recipe.getGoldCost()) {
            return "Not enough gold for " + recipe.getName() + ".";
        }
        if (getShardCount(recipe.getShardGrade()) < recipe.getShardCost()) {
            return "Missing " + recipe.getShardGrade() + "-Grade shards for " + recipe.getName() + ".";
        }
        Map<String, Integer> costs = buildForgeCostMap(recipe);
        if (!gameState.consumeForgeComponents(costs)) {
            return "Missing components for " + recipe.getName() + ".";
        }
        if (!gameState.consumeShards(recipe.getShardGrade(), recipe.getShardCost())) {
            for (Map.Entry<String, Integer> entry : costs.entrySet()) {
                gameState.addForgeComponent(entry.getKey(), entry.getValue());
            }
            return "Missing " + recipe.getShardGrade() + "-Grade shards for " + recipe.getName() + ".";
        }
        if (!spendGold(recipe.getGoldCost())) {
            for (Map.Entry<String, Integer> entry : costs.entrySet()) {
                gameState.addForgeComponent(entry.getKey(), entry.getValue());
            }
            gameState.addShard(recipe.getShardGrade(), recipe.getShardCost());
            return "Not enough gold for " + recipe.getName() + ".";
        }
        unlockEquipment(result.getId());
        return "Forged " + result.getName() + ".";
    }

    private boolean isForgeRecipeUnlocked(ForgeRecipeDefinition recipe) {
        if (recipe == null || recipe.getResultEquipmentId() == null || recipe.getResultEquipmentId().isEmpty()) {
            return false;
        }
        if (!isForgeWorkshopUnlocked()) {
            return false;
        }
        EquipmentItem result = findEquipmentItem(recipe.getResultEquipmentId());
        if (result == null) {
            return false;
        }
        return result.getTier() < 4 || isAdvancedCraftingUnlocked();
    }

    private String getForgeRecipeUnlockReason(ForgeRecipeDefinition recipe) {
        if (!isForgeWorkshopUnlocked()) {
            return "Requires Workshop Tools";
        }
        EquipmentItem result = recipe != null ? findEquipmentItem(recipe.getResultEquipmentId()) : null;
        if (result != null && result.getTier() >= 4 && !isAdvancedCraftingUnlocked()) {
            return "Requires Prototype Lab";
        }
        return "Pattern unavailable";
    }

    public long getShardSellValue(String grade) {
        switch (grade) {
            case "S":
                return 300L;
            case "A":
                return 180L;
            case "B":
                return 110L;
            case "C":
                return 70L;
            case "D":
                return 45L;
            case "E":
                return 28L;
            case "F":
                return 18L;
            case "G":
            default:
                return 10L;
        }
    }

    public long getForgeComponentSellValue(String componentId) {
        ForgeComponentDefinition definition = forgeComponentDefinitions.get(componentId);
        String rarity = definition != null ? definition.getRarity() : "COMMON";
        switch (rarity) {
            case "EPIC":
                return 120L;
            case "RARE":
                return 70L;
            case "UNCOMMON":
                return 35L;
            case "COMMON":
            default:
                return 18L;
        }
    }

    public String sellShard(String grade) {
        if (grade == null || grade.isEmpty()) {
            return "That shard cannot be sold.";
        }
        if (!gameState.consumeShards(grade, 1)) {
            return "No " + grade + "-Grade shards available.";
        }
        addGold(getShardSellValue(grade));
        return "Sold 1 " + grade + "-Grade shard.";
    }

    public String sellForgeComponent(String componentId) {
        if (componentId == null || componentId.isEmpty()) {
            return "That component cannot be sold.";
        }
        if (getForgeComponentCount(componentId) <= 0) {
            return "No " + getForgeComponentName(componentId) + " available.";
        }
        gameState.addForgeComponent(componentId, -1);
        addGold(getForgeComponentSellValue(componentId));
        return "Sold 1 " + getForgeComponentName(componentId) + ".";
    }

    private Map<String, Integer> buildForgeCostMap(ForgeRecipeDefinition recipe) {
        Map<String, Integer> costs = new HashMap<>();
        if (recipe == null) {
            return costs;
        }
        for (ForgeIngredientDefinition ingredient : recipe.getIngredients()) {
            if (ingredient == null || ingredient.getComponentId() == null || ingredient.getComponentId().isEmpty()) {
                continue;
            }
            costs.put(
                ingredient.getComponentId(),
                costs.getOrDefault(ingredient.getComponentId(), 0) + ingredient.getQuantity()
            );
        }
        return costs;
    }

    public List<EquipmentItem> getSellableEquipment() {
        List<EquipmentItem> sellable = new ArrayList<>();
        for (EquipmentItem item : getEquipmentCatalog()) {
            if (!isEquipmentEquipped(item.getId())) {
                sellable.add(item);
            }
        }
        return sellable;
    }

    public long getSellPrice(EquipmentItem item) {
        return item != null ? Math.max(1L, Math.round(item.getCost() * 0.5f)) : 0L;
    }

    public String sellEquipment(String itemId) {
        EquipmentItem item = findEquipmentItem(itemId);
        if (item == null) {
            return "That item cannot be sold.";
        }
        if (isEquipmentEquipped(itemId)) {
            return "Unequip " + item.getName() + " before selling it.";
        }
        if (!gameState.removeOwnedEquipment(itemId)) {
            return item.getName() + " is not in your inventory.";
        }
        ownedEquipmentIds.clear();
        ownedEquipmentIds.addAll(gameState.getOwnedEquipmentIds());
        addGold(getSellPrice(item));
        return "Sold " + item.getName() + ".";
    }

    private boolean isEquipmentEquipped(String itemId) {
        if (playerEquipment.containsValue(itemId)) {
            return true;
        }
        for (Map<String, String> equipped : robotEquipment.values()) {
            if (equipped != null && equipped.containsValue(itemId)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, String> getRobotEquipmentSlots(int index) {
        if (!hasActiveRobotAt(index)) {
            return new HashMap<>();
        }
        return new HashMap<>(getEquippedItemsForPartyIndex(index));
    }

    public boolean equipRobotItem(int index, EquipmentItem item) {
        if (!hasActiveRobotAt(index)) {
            return false;
        }
        String robotId = getRobotId(index);
        if (robotId == null || !gameState.equipRobotItem(robotId, robots[index].grade, item)) {
            return false;
        }
        robotEquipment = copyRobotEquipment(gameState.getRobotEquipment());
        ensureClassMasteryAbilities(robotId, getRobotProgressionStateForPartyIndex(index));
        robots[index].health = Math.min(getRobotStats(index).maxHealth, robots[index].health);
        refreshHud();
        return true;
    }

    public EquipmentItem findEquipmentItem(String itemId) {
        for (EquipmentItem item : equipmentCatalog) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public List<String> getPartyAbilityIds(int partyIndex) {
        List<String> ids = new ArrayList<>();
        if (partyIndex < 0) {
            ids.add("power_strike");
            ids.add("heal_pulse");
            return ids;
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(partyIndex);
        if (progressionState != null && progressionState.getKnownAbilityIds() != null) {
            ids.addAll(progressionState.getKnownAbilityIds());
        }
        return ids;
    }

    public List<com.rogueforge.game.combat.AbilityInstance> getPartyAbilityInstances(int partyIndex) {
        if (partyIndex < 0) {
            return com.rogueforge.game.combat.AbilityRegistry.createInstances(getPartyAbilityIds(-1));
        }
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(partyIndex);
        if (progressionState == null) {
            return com.rogueforge.game.combat.AbilityRegistry.createInstances(getPartyAbilityIds(partyIndex));
        }
        return com.rogueforge.game.combat.AbilityRegistry.createInstances(
            progressionState.getKnownAbilityIds(),
            progressionState.getAbilityProgression()
        );
    }

    public List<String> applyAbilityMasteryUnlocks(int partyIndex) {
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(partyIndex);
        if (progressionState == null) {
            return new ArrayList<>();
        }
        return AbilityEvolutionManager.applyMasteryUnlocks(progressionState, gameState.getRobotProgressionStates());
    }

    public ShopInventory createShopInventory(String shopId) {
        ShopInventory inventory = new ShopInventory();
        ShopDefinition definition = shopDefinitions.get(shopId);
        if (definition == null || definition.getEntries() == null) {
            return inventory;
        }
        boolean serviceSurplus = hasTownServiceSurplus();
        boolean serviceStrain = hasTownServiceStrain();
        for (ShopEntryDefinition entry : definition.getEntries()) {
            if (entry == null) {
                continue;
            }
            if (entry.getRequiredWorldFlag() != null && !entry.getRequiredWorldFlag().isEmpty()
                && !worldStateManager.isFlagActive(gameState, entry.getRequiredWorldFlag())) {
                continue;
            }
            if (entry.getBlockedWorldFlag() != null && !entry.getBlockedWorldFlag().isEmpty()
                && worldStateManager.isFlagActive(gameState, entry.getBlockedWorldFlag())) {
                continue;
            }
            long adjustedCost = Math.max(1L, entry.getCost()
                + (serviceStrain ? 10L : 0L)
                - (serviceSurplus ? 5L : 0L));
            if ("healing".equals(entry.getType())) {
                int quantity = Math.max(1, entry.getQuantity()) + (serviceSurplus ? 1 : 0);
                if (serviceStrain) {
                    quantity = Math.max(1, quantity - 1);
                }
                inventory.addHealingItem(entry.getLabel(), quantity, adjustedCost);
                continue;
            }
            if ("equipment".equals(entry.getType())) {
                EquipmentItem item = findEquipmentItem(entry.getItemId());
                if (item != null) {
                    long cost = entry.getCost() > 0 ? adjustedCost : Math.max(1L, item.getCost()
                        + (serviceStrain ? 10L : 0L)
                        - (serviceSurplus ? 5L : 0L));
                    inventory.addEquipmentItem(item, cost);
                }
            }
        }
        addWarBiasedShopEntries(inventory, definition);
        return inventory;
    }

    private void addWarBiasedShopEntries(ShopInventory inventory, ShopDefinition definition) {
        if (inventory == null || definition == null || !isHubTownZone() || !warPhaseManager.isWarPhaseUnlocked(gameState)) {
            return;
        }
        String dominantFactionId = getDominantWarFactionId();
        String dispatchZoneId = getPrimaryWarDispatchZone();
        boolean serviceSurplus = hasTownServiceSurplus();
        boolean serviceStrain = hasTownServiceStrain();
        long baseCost = Math.max(10L, 24L + (serviceStrain ? 10L : 0L) - (serviceSurplus ? 6L : 0L));

        if ("workshop".equals(definition.getId()) || "town_general".equals(definition.getId())) {
            if (WarPhaseManager.FACTION_IRONHAVEN_COMMAND.equals(dominantFactionId)) {
                inventory.addHealingItem("Command Field Pack", serviceSurplus ? 3 : 2, baseCost + 8L);
            } else if (WarPhaseManager.FACTION_GUILD_COALITION.equals(dominantFactionId)) {
                inventory.addHealingItem("Broker Supply Cache", serviceSurplus ? 4 : 3, baseCost + 14L);
            } else {
                inventory.addHealingItem("Emergency Ironhaven Kit", 1, Math.max(8L, baseCost - 4L));
            }
        }

        if ("herbalist".equals(definition.getId()) || "apothecary".equals(definition.getId()) || "town_general".equals(definition.getId())) {
            if ("shadow_caves".equals(dispatchZoneId)) {
                inventory.addHealingItem("Ghostlight Recovery Set", serviceSurplus ? 3 : 2, baseCost + 10L);
            } else if ("crystal_depths".equals(dispatchZoneId)) {
                inventory.addHealingItem("Shardstorm Med Pack", serviceSurplus ? 3 : 2, baseCost + 12L);
            } else if ("sky_fortress".equals(dispatchZoneId)) {
                inventory.addHealingItem("Summit Breach Kit", serviceSurplus ? 2 : 1, baseCost + 16L);
            } else if (dispatchZoneId != null) {
                inventory.addHealingItem("Frontier Relief Crate", serviceSurplus ? 2 : 1, baseCost + 6L);
            }
        }

        if (serviceStrain) {
            inventory.addHealingItem("Rationed Emergency Dose", 1, Math.max(6L, baseCost - 8L));
        }
    }

    public String buyShopEntry(ShopInventory.ShopEntry entry) {
        if (entry == null) {
            return "Nothing selected.";
        }
        if (!spendGold(entry.getCost())) {
            return "Not enough gold.";
        }
        if (entry.isConsumable()) {
            addHealingPotions(entry.getHealingPotions());
            return "Purchased " + entry.getLabel() + ".";
        }
        EquipmentItem equipment = entry.getEquipment();
        if (equipment == null) {
            return "That item cannot be purchased.";
        }
        if (gameState.getOwnedEquipmentIds().contains(equipment.getId())) {
            addGold(entry.getCost());
            return equipment.getName() + " is already owned.";
        }
        unlockEquipment(equipment.getId());
        return "Purchased " + equipment.getName() + ".";
    }

    public int getBestiaryScanLevel(String monsterId) {
        return gameState.getBestiaryScanLevel(monsterId);
    }

    public Map<String, Integer> getBestiaryScanLevels() {
        return gameState.getBestiaryScanLevels();
    }

    /**
     * Returns display lines for the Archive tab, one entry per scanned monster.
     * Each entry expands based on accumulated scan level:
     *   Scan 1 — HP, ATK, DEF visible
     *   Scan 2 — SPD and elemental weaknesses also visible
     *   Scan 3 — Gold reward also visible
     */
    public java.util.List<String> getBestiaryArchiveLines() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        Map<String, Integer> scanLevels = gameState.getBestiaryScanLevels();
        if (scanLevels == null || scanLevels.isEmpty()) {
            return lines;
        }
        for (String monsterId : new java.util.TreeSet<>(scanLevels.keySet())) {
            int level = scanLevels.get(monsterId);
            MonsterDefinition def = monsterDefinitions.get(monsterId);
            String name = def != null ? def.getName() : monsterId;
            String rank = def != null ? def.getRank() : "?";
            lines.add(name + "  [" + rank + "]  Scan " + level + "/3");
            if (def != null) {
                if (level >= 1) {
                    lines.add("  HP " + def.getHp()
                        + "  ATK " + def.getAttack()
                        + "  DEF " + def.getDefense());
                }
                if (level >= 2) {
                    String weak = joinElements(def.getWeaknesses());
                    String res  = joinElements(def.getResistances());
                    lines.add("  SPD " + def.getSpeed()
                        + "  Weak: " + (weak.isEmpty() ? "none" : weak)
                        + "  Res: " + (res.isEmpty() ? "none" : res));
                }
                if (level >= 3) {
                    lines.add("  Gold reward: " + def.getBaseLoot());
                }
            }
            lines.add(""); // blank spacer between entries
        }
        return lines;
    }

    private String joinElements(String[] elements) {
        if (elements == null || elements.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String e : elements) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e);
        }
        return sb.toString();
    }

    public Map<String, Integer> rollForgeDropsForEnemy(String monsterId, String rank) {
        Map<String, Integer> drops = new HashMap<>();
        MonsterDefinition monster = monsterDefinitions.get(monsterId);
        if (monster == null) {
            return drops;
        }

        List<ForgeComponentDefinition> eligible = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        tags.add(monsterId);
        for (String lootTableId : monster.getLootTableIds()) {
            if (lootTableId != null && !lootTableId.isEmpty()) {
                tags.add(lootTableId);
            }
        }

        for (ForgeComponentDefinition definition : forgeComponentDefinitions.values()) {
            for (String dropTag : definition.getDropTags()) {
                if (tags.contains(dropTag)) {
                    eligible.add(definition);
                    break;
                }
            }
        }
        if (eligible.isEmpty()) {
            return drops;
        }
        if (Math.random() > getForgeDropChance(rank)) {
            return drops;
        }

        ForgeComponentDefinition primary = eligible.get((int) (Math.random() * eligible.size()));
        drops.put(primary.getId(), getForgeDropQuantity(rank));
        if (eligible.size() > 1 && isHighRankForgeDrop(rank) && Math.random() < 0.35f) {
            ForgeComponentDefinition secondary = eligible.get((int) (Math.random() * eligible.size()));
            if (!secondary.getId().equals(primary.getId())) {
                drops.put(secondary.getId(), 1);
            }
        }
        return drops;
    }

    public String[] resolveForgeDropNames(Map<String, Integer> drops) {
        List<String> names = new ArrayList<>();
        if (drops == null) {
            return new String[0];
        }
        for (Map.Entry<String, Integer> entry : drops.entrySet()) {
            names.add(getForgeComponentName(entry.getKey()) + " x" + entry.getValue());
        }
        return names.toArray(new String[0]);
    }

    public String[] resolveShardDropNames(Map<String, Integer> drops) {
        List<String> names = new ArrayList<>();
        if (drops == null) {
            return new String[0];
        }
        String[] grades = {"S", "A", "B", "C", "D", "E", "F", "G"};
        for (String grade : grades) {
            int count = drops.getOrDefault(grade, 0);
            if (count > 0) {
                names.add(grade + "-Grade Shard x" + count);
            }
        }
        return names.toArray(new String[0]);
    }

    private float getForgeDropChance(String rank) {
        switch (rank) {
            case "B":
            case "A":
            case "S":
                return 0.95f;
            case "C":
            case "D":
                return 0.8f;
            default:
                return 0.65f;
        }
    }

    private int getForgeDropQuantity(String rank) {
        switch (rank) {
            case "B":
            case "A":
            case "S":
                return Math.random() < 0.5f ? 2 : 1;
            case "C":
            case "D":
                return Math.random() < 0.25f ? 2 : 1;
            default:
                return 1;
        }
    }

    private boolean isHighRankForgeDrop(String rank) {
        return "C".equals(rank) || "B".equals(rank) || "A".equals(rank) || "S".equals(rank);
    }

    private boolean isBossMonster(String monsterId) {
        MonsterDefinition definition = monsterId != null ? monsterDefinitions.get(monsterId) : null;
        return definition != null && "BOSS".equals(definition.getAiProfile());
    }

    private boolean updateForgeCoreMilestones() {
        int defeatedBosses = gameState.getDefeatedBossCount();
        int targetLevel = 1;
        if (defeatedBosses >= 15) {
            targetLevel = 4;
        } else if (defeatedBosses >= 10) {
            targetLevel = 3;
        } else if (defeatedBosses >= 5) {
            targetLevel = 2;
        }
        if (targetLevel <= gameState.getForgeCoreLevel()) {
            return false;
        }
        gameState.setForgeCoreLevel(targetLevel);
        if (targetLevel >= 2) {
            syncAct2TownFacilities();
        }
        String message;
        if (targetLevel == 2) {
            message = "Five boss systems have fallen. Forge Core Lv2 is online. Tier-II evolutions, a second party slot, and Ironhaven's economy districts are now coming online.";
        } else if (targetLevel == 3) {
            message = "Ten boss systems have fallen. Forge Core Lv3 surges to life. Tier-III evolutions and a third party slot are now unlocked.";
        } else {
            message = "Fifteen boss systems have fallen. Forge Core Lv4 reaches full resonance. Ironhaven's highest forge functions are now available.";
        }
        showStandaloneDialog("Forge Core", message);
        return true;
    }

    public void autosave() {
        saveManager.autosave(buildSaveFile(SaveManager.AUTOSAVE_SLOT));
    }

    private void restoreRobotState(
        float[] savedHealth,
        float[] savedMaxHealth,
        float[] savedBaseMaxHealth,
        float[] savedX,
        float[] savedY,
        float[] savedAngleDeg,
        float[] savedAttackTimers
    ) {
        for (int i = 0; i < ROBOT_COUNT; i++) {
            RobotCompanion robot = robots[i];
            if (savedBaseMaxHealth != null && i < savedBaseMaxHealth.length && savedBaseMaxHealth[i] > 0f) {
                robot.maxHealth = savedBaseMaxHealth[i];
            } else if (savedMaxHealth != null && i < savedMaxHealth.length && savedMaxHealth[i] > 0f) {
                robot.maxHealth = normalizeLegacyRobotBaseMaxHealth(i, savedMaxHealth[i]);
            }
            if (savedHealth != null && i < savedHealth.length) {
                robot.health = Math.max(0f, Math.min(getRobotStats(i).maxHealth, savedHealth[i]));
            } else {
                robot.health = getRobotStats(i).maxHealth;
            }

            if (savedAngleDeg != null && i < savedAngleDeg.length) {
                robot.angleDeg = savedAngleDeg[i];
            }

            if (savedAttackTimers != null && i < savedAttackTimers.length) {
                robot.attackTimer = Math.max(0f, savedAttackTimers[i]);
            }

            if (savedX != null && savedY != null && i < savedX.length && i < savedY.length) {
                robot.pos.set(savedX[i], savedY[i]);
            } else {
                robot.pos.set(playerPos.x, playerPos.y - ((i + 1) * ROBOT_FOLLOW_GAP));
            }
        }
    }

    private float normalizeLegacyRobotBaseMaxHealth(int index, float savedDerivedMaxHealth) {
        EquipmentTotals equipmentTotals = getEquipmentTotals(index);
        return Math.max(ROBOT_MAX_HEALTH, savedDerivedMaxHealth - equipmentTotals.hpBonus);
    }

    private void restoreEnemyState(List<SaveFile.EnemyState> savedEnemies) {
        if (savedEnemies == null || savedEnemies.isEmpty()) {
            return;
        }

        enemies.clear();
        for (SaveFile.EnemyState enemyState : savedEnemies) {
            Enemy enemy = new Enemy();
            enemy.pos = new Vector2(enemyState.getX(), enemyState.getY());
            enemy.facing = new Vector2(0f, -1f);
            enemy.hp = enemyState.getHp();
            enemy.maxHp = enemyState.getMaxHp();
            enemy.speed = enemyState.getSpeed();
            enemy.size = enemyState.getSize();
            enemy.defense = enemyState.getDefense();
            enemy.agility = enemyState.getAgility();
            enemy.strength = enemyState.getStrength();
            enemy.intelligence = enemyState.getIntelligence();
            enemy.stamina = enemyState.getStamina();
            enemy.rewardGold = enemyState.getRewardGold();
            enemy.rewardExperience = enemyState.getRewardExperience();
            enemy.name = enemyState.getName();
            enemy.monsterId = enemyState.getMonsterId();
            enemy.alive = enemyState.isAlive();
            enemy.attackCooldown = ENEMY_MELEE_COOLDOWN;
            enemy.attackTimer = enemyState.getAttackTimer();
            enemy.patrolTarget = new Vector2(enemyState.getPatrolTargetX(), enemyState.getPatrolTargetY());
            enemy.dungeonFloor = enemyState.getDungeonFloor();
            enemy.raidSpawned = enemyState.isRaidSpawned();
            enemies.add(enemy);
        }

        enemyRespawnTimer = 0f;
    }

    private void restoreChestState(List<SaveFile.ChestState> savedChests) {
        if (savedChests == null) {
            return;
        }

        openedChestStates.clear();
        for (SaveFile.ChestState chestState : savedChests) {
            String zoneId = chestState.getZoneId() != null ? chestState.getZoneId() : currentZoneId;
            if (chestState.isOpened()) {
                openedChestStates.put(chestStateKey(zoneId, chestState.getHouseId(), chestState.getChestId()), true);
            }
        }
        for (House house : houses) {
            applyHouseChestState(house);
        }
    }

    /**
     * Simple world house footprint.
     */
    static class House {
        String zoneId;
        int id;
        String name;
        float x;
        float y;
        float width;
        float height;
        List<InteriorNpc> interiorNpcs;
        List<Chest> chests;
        List<InteriorFeature> interiorFeatures;

        House(int id, String name, float x, float y, float width, float height,
              List<InteriorNpc> interiorNpcs, List<Chest> chests, List<InteriorFeature> interiorFeatures) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.interiorNpcs = interiorNpcs;
            this.chests = chests;
            this.interiorFeatures = interiorFeatures;
        }

        Vector2 getDoorCenter() {
            return new Vector2(x + width * 0.5f, y + 8f);
        }

        Rectangle getDoorBounds() {
            return new Rectangle(x + width * 0.4f, y - 2f, width * 0.2f, 32f);
        }

        Rectangle getEntryTileBounds() {
            return new Rectangle(x + width * 0.4f, y - 46f, width * 0.2f, 28f);
        }

        Rectangle getSignBounds() {
            return new Rectangle(x + width - 30f, y + 6f, 18f, 22f);
        }

        Vector2 getEntryPoint() {
            return new Vector2(x + width * 0.5f, y - 18f);
        }

        static House createWorkshop(int id, String name, float x, float y, float width, float height) {
            List<InteriorNpc> npcs = new ArrayList<>();
            npcs.add(new InteriorNpc("toma", "Toma", new Vector2(220f, 250f),
                "If you find old parts in the wild, bring them here and I can rebuild them."));
            npcs.add(new InteriorNpc("rooke", "Rooke", new Vector2(138f, 204f),
                "Fresh plates, tuned edges, replacement seals. If the workshop stocks it, I can sell it.", "workshop"));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("workshop_cache", new Vector2(120f, 128f), 30, 0, true));
            List<InteriorFeature> features = new ArrayList<>();
            features.add(new InteriorFeature("forge_station", "Forge", new Vector2(306f, 214f), "forge",
                "The anvil glows with recycled heat. Toma's forge is ready."));
            return new House(id, name, x, y, width, height, npcs, chests, features);
        }

        static House createLodge(int id, String name, float x, float y, float width, float height) {
            List<InteriorNpc> npcs = new ArrayList<>();
            npcs.add(new InteriorNpc("nia", "Nia", new Vector2(245f, 215f),
                "Travelers leave rumors behind. One says there is a buried vault east of town."));
            npcs.add(new InteriorNpc("bram", "Bram", new Vector2(150f, 250f),
                "Rest when you can. Even machines need a rhythm."));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("lodge_stash", new Vector2(300f, 122f), 15, 1, false));
            return new House(id, name, x, y, width, height, npcs, chests, new ArrayList<>());
        }

        static House createHerbalist(int id, String name, float x, float y, float width, float height) {
            List<InteriorNpc> npcs = new ArrayList<>();
            npcs.add(new InteriorNpc("iris", "Iris", new Vector2(210f, 238f),
                "Hidden chests react to careful eyes. Walk close and watch the outline flicker.", "apothecary"));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("herbalist_hidden", new Vector2(92f, 238f), 10, 2, true));
            chests.add(new Chest("herbalist_supplies", new Vector2(310f, 136f), 0, 1, false));
            return new House(id, name, x, y, width, height, npcs, chests, new ArrayList<>());
        }

        static House createPlayerHome(int id, String name, float x, float y, float width, float height) {
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("home_supplies", new Vector2(312f, 130f), 12, 1, false));
            List<InteriorFeature> features = new ArrayList<>();
            features.add(new InteriorFeature("home_journal", "Old Journal", new Vector2(104f, 218f), "inspect",
                "A weathered journal maps the same truth on every page: Mechara only survives if someone is willing to step beyond the walls and hold the dark back."));
            return new House(id, name, x, y, width, height, new ArrayList<>(), chests, features);
        }
    }

    /**
     * Simple static NPC with a single dialogue line.
     */
    static class InteriorNpc {
        String id;
        String name;
        Vector2 pos;
        String dialog;
        String shopId;

        InteriorNpc(String id, String name, Vector2 pos, String dialog) {
            this(id, name, pos, dialog, null);
        }

        InteriorNpc(String id, String name, Vector2 pos, String dialog, String shopId) {
            this.id = id;
            this.name = name;
            this.pos = pos;
            this.dialog = dialog;
            this.shopId = shopId;
        }
    }

    static class InteriorFeature {
        String id;
        String label;
        Vector2 pos;
        String actionType;
        String dialog;

        InteriorFeature(String id, String label, Vector2 pos, String actionType, String dialog) {
            this.id = id;
            this.label = label;
            this.pos = pos;
            this.actionType = actionType;
            this.dialog = dialog;
        }
    }

    static class Chest {
        String id;
        Vector2 pos;
        long goldReward;
        int potionReward;
        boolean hidden;
        boolean opened;

        Chest(String id, Vector2 pos, long goldReward, int potionReward, boolean hidden) {
            this.id = id;
            this.pos = pos;
            this.goldReward = goldReward;
            this.potionReward = potionReward;
            this.hidden = hidden;
            this.opened = false;
        }
    }

    static class RobotStatBlock {
        float currentHealth;
        float maxHealth;
        float agility;
        float strength;
        float intelligence;
        float stamina;

        RobotStatBlock(float currentHealth, float maxHealth, float agility, float strength, float intelligence, float stamina) {
            this.currentHealth = currentHealth;
            this.maxHealth = maxHealth;
            this.agility = agility;
            this.strength = strength;
            this.intelligence = intelligence;
            this.stamina = stamina;
        }
    }

    public static class BattleProgressionPreview {
        public final List<String> robotProgress = new ArrayList<>();
    }

    public static class WeaponGain {
        public final WeaponType weaponType;
        public final int fromLevel;
        public final int toLevel;
        public final List<String> unlockLabels;

        WeaponGain(WeaponType weaponType, int fromLevel, int toLevel, List<String> unlockLabels) {
            this.weaponType = weaponType;
            this.fromLevel = fromLevel;
            this.toLevel = toLevel;
            this.unlockLabels = unlockLabels != null ? unlockLabels : new ArrayList<>();
        }
    }

    private static class EquipmentTotals {
        float hpBonus;
        float agilityBonus;
        float strengthBonus;
        float intelligenceBonus;
        float staminaBonus;
        float xpMultiplier = 1f;

        float getXpMultiplier() {
            return xpMultiplier;
        }
    }

    private static class NpcConversation {
        String dialog;
        long rewardGold;
        int rewardPotions;
        int rewardExperience;
    }

    /**
     * Inner class representing a one-frame robot attack trace.
     */
    private static class RobotAttackLine {
        Vector2 from;
        Vector2 to;
    }

    /**
     * Inner class representing a floating gold popup.
     */
    private static class GoldPopup {
        Vector2 pos;
        String text;
        float lifetime;
        float timer;
    }

    private static class ExpeditionLaunchDestination {
        final String zoneId;
        final String spawnId;
        final String label;
        final String detail;
        final boolean currentZone;
        final String contractTierLabel;

        ExpeditionLaunchDestination(String zoneId, String spawnId, String label, String detail, boolean currentZone, String contractTierLabel) {
            this.zoneId = zoneId;
            this.spawnId = spawnId;
            this.label = label;
            this.detail = detail;
            this.currentZone = currentZone;
            this.contractTierLabel = contractTierLabel;
        }
    }

    private static class PlayerQuestContract {
        String contractId;
        String guildId;
        String zoneId;
        String kind;
        String title;
        String description;
        String targetId;
        String authorPlayerId;
        boolean active;
    }

    private static class PlayerCreatedNpc {
        String npcId;
        String guildId;
        String zoneId;
        String role;
        String name;
        String dialog;
    }

    private static class AnimationSet {
        final TextureRegion[] downIdle;
        final TextureRegion[] downWalk;
        final TextureRegion[] sideIdle;
        final TextureRegion[] sideWalk;
        final TextureRegion[] upIdle;
        final TextureRegion[] upWalk;

        AnimationSet(TextureRegion[] downIdle, TextureRegion[] downWalk, TextureRegion[] sideIdle,
                     TextureRegion[] sideWalk, TextureRegion[] upIdle, TextureRegion[] upWalk) {
            this.downIdle = downIdle;
            this.downWalk = downWalk;
            this.sideIdle = sideIdle;
            this.sideWalk = sideWalk;
            this.upIdle = upIdle;
            this.upWalk = upWalk;
        }

        TextureRegion[] getFrames(Vector2 facing, boolean moving) {
            boolean vertical = Math.abs(facing.y) >= Math.abs(facing.x);
            if (vertical && facing.y > 0f) {
                return moving ? upWalk : upIdle;
            }
            if (vertical) {
                return moving ? downWalk : downIdle;
            }
            return moving ? sideWalk : sideIdle;
        }

    }
}
