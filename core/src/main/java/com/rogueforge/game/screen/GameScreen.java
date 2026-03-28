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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.core.GameLoop;
import com.rogueforge.game.core.GameState;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityRegistry;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.ForgeComponentDefinition;
import com.rogueforge.game.data.ForgeIngredientDefinition;
import com.rogueforge.game.data.ForgeRecipeDefinition;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.data.ShopDefinition;
import com.rogueforge.game.data.ShopEntryDefinition;
import com.rogueforge.game.data.StoryEventDefinition;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.economy.ShopInventory;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;
import com.rogueforge.game.progression.AbilityEvolutionManager;
import com.rogueforge.game.progression.AbilityProgressionState;
import com.rogueforge.game.progression.RobotEvolutionManager;
import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.progression.WeaponProficiencyState;
import com.rogueforge.game.progression.CombatArtRegistry;
import com.rogueforge.game.progression.WeaponProficiencyTracker;
import com.rogueforge.game.robot.RobotDefinition;
import com.rogueforge.game.world.TmxWorldLoader;
import com.rogueforge.game.world.DialogueSystem;
import com.rogueforge.game.world.QuestManager;
import com.rogueforge.game.world.RobotRecruitmentManager;
import com.rogueforge.game.world.SettlementManager;
import com.rogueforge.game.world.SettlementState;
import com.rogueforge.game.world.InfiniteDungeonLayoutGenerator;
import com.rogueforge.game.world.WorldStateManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main gameplay screen with a moveable player square, robot companions,
 * spawning enemies, combat mechanics, and HUD overlay.
 */
public class GameScreen implements Screen {
    private static final float WORLD_VIEW_WIDTH = 1280f;
    private static final float WORLD_VIEW_HEIGHT = 720f;

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameLoop gameLoop;
    private final OrthographicCamera gameCamera;
    private final OrthographicCamera uiCamera;
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
    private Texture doorTexture;
    private Texture chestTexture;
    private Texture shadowTexture;
    private AnimationSet playerAnimation;
    private AnimationSet[] robotAnimations;
    private AnimationSet[] enemyAnimations;
    private AnimationSet[] npcAnimations;

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
    private static final float PLAYER_AGILITY = 26f;
    private static final float PLAYER_STRENGTH = 18f;
    private static final float PLAYER_INTELLIGENCE = 14f;
    private static final float PLAYER_STAMINA = 20f;
    private static final String[] GRADE_ORDER = {"G", "F", "E", "D", "C", "B", "A", "S", "S+", "S++", "S+++"};
    private final RobotCompanion[] robots = new RobotCompanion[ROBOT_COUNT];
    private final List<House> houses = new ArrayList<>();
    private final List<Npc> npcs = new ArrayList<>();
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
    private final List<ForgeRecipeDefinition> forgeRecipes = new ArrayList<>();
    private final List<StoryEventDefinition> storyEvents = new ArrayList<>();
    private final TmxWorldLoader worldLoader = new TmxWorldLoader();
    private final InfiniteDungeonLayoutGenerator infiniteDungeonLayoutGenerator = new InfiniteDungeonLayoutGenerator();
    private final SettingsManager settingsManager = new SettingsManager();
    private final SaveManager saveManager = new SaveManager();
    private final QuestManager questManager = new QuestManager();
    private final DialogueSystem dialogueSystem = new DialogueSystem();
    private final WorldStateManager worldStateManager = new WorldStateManager();
    private final RobotRecruitmentManager recruitmentManager = new RobotRecruitmentManager();
    private final SettlementManager settlementManager = new SettlementManager();
    private final Map<String, Boolean> openedChestStates = new HashMap<>();
    private final Map<String, Boolean> questFlags = new HashMap<>();
    private final List<String> keyItems = new ArrayList<>();
    private TmxWorldLoader.LoadedZone currentZone;
    private ZoneDefinition currentZoneDefinition;
    private String difficultyMode = "NORMAL";

    // Robot attack visualization (current frame only)
    private final List<RobotAttackLine> robotAttackLines = new ArrayList<>();

    // Gold and stats
    private long totalGold = 0;
    private int totalEnemiesKilled = 0;
    private float survivalTime = 0f;
    private int healingPotions = 3;
    private int playerLevel = 1;
    private int playerExperience = 0;

    // Gold popup floating text
    private List<GoldPopup> goldPopups = new ArrayList<>();
    private Map<String, Map<String, String>> robotEquipment = new HashMap<>();
    private Map<String, String> playerEquipment = new HashMap<>();
    private List<String> collectedRobotIds = new ArrayList<>();
    private List<String> activeRobotIds = new ArrayList<>();
    private String currentZoneId = "verdant_fields";
    private int currentSaveSlot = 0;
    private String activeDialog = null;
    private String activeSpeaker = null;

    private boolean isPaused = false;
    private boolean battleActive = false;
    private GameInputProcessor gameInputProcessor;

    public GameScreen(RogueForgeGame game, ScreenManager screenManager) {
        this(game, screenManager, null);
    }

    public GameScreen(RogueForgeGame game, ScreenManager screenManager, SaveFile saveFile) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameLoop = new GameLoop();
        this.gameCamera = new OrthographicCamera();
        this.uiCamera = new OrthographicCamera();
        this.gameViewport = new FitViewport(WORLD_VIEW_WIDTH, WORLD_VIEW_HEIGHT, gameCamera);
        this.uiViewport = new ScreenViewport(uiCamera);
        this.gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.hudOverlay = new HUDOverlay();
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.settingsManager.load();
        this.difficultyMode = settingsManager.getSettings().getDifficultyMode();
        loadVisualAssets();
        this.playerName = saveFile != null && saveFile.getPlayerName() != null
            ? saveFile.getPlayerName()
            : "Player";
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
            activeRobotIds.add("guardian_mk1");
            activeRobotIds.add("striker_mk1");
        }
        normalizeActiveRobotSlots();
        if (collectedRobotIds.isEmpty()) {
            for (String robotId : activeRobotIds) {
                if (robotId != null && !robotId.isEmpty() && !collectedRobotIds.contains(robotId)) {
                    collectedRobotIds.add(robotId);
                }
            }
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
        worldStateManager.initialize(gameState);
        questManager.initialize(gameState);

        initializeEquipmentCatalog();
        loadZoneDefinitions();
        loadRobotDefinitions();
        loadMonsterDefinitions();
        loadForgeComponentDefinitions();
        loadForgeRecipes();
        loadStoryEvents();
        loadShopDefinitions();
        ensureRobotProgressionStates();
        initializeActiveRobotHealthForNewRun();
        loadZone(currentZoneId, "town_square", true);

        hudOverlay.setPlayerHealth(playerHealth, playerMaxHealth);
        hudOverlay.setCurrency(totalGold);
        hudOverlay.setRobotHealth(getRobotHealthValues(), getRobotMaxHealthValues());

        if (saveFile != null) {
            loadFromSave(saveFile);
        }
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
    }

    private void ensureGameInputProcessor() {
        if (gameInputProcessor == null) {
            gameInputProcessor = new GameInputProcessor(this);
        }
        Gdx.input.setInputProcessor(gameInputProcessor);
    }

    private void loadVisualAssets() {
        groundTileTexture = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_03.png");
        wallTileTexture = loadTexture("2 Dungeon Tileset/1 Tiles/Tile_57.png");
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
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
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
        if (!isPaused && !battleActive) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                pauseGame();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                openWorkshop();
                return;
            }
        }

        if (!isPaused) {
            gameLoop.update(delta);
            updatePlayer(delta);
            updateRobots(delta);
            updateEnemies(delta);
            updateAttackEffects(delta);
            updateGoldPopups(delta);
            survivalTime += delta;

            // Check player death
            if (!hasLivingPartyMember()) {
                transitionToGameOver();
                return;
            }
        }

        // Camera follows player
        gameViewport.apply();
        gameCamera.position.set(playerPos.x, playerPos.y, 0);
        gameCamera.update();

        Gdx.gl.glClearColor(0.12f, 0.14f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawGroundTiles();

        // Draw enemies
        drawHouses();
        drawNpcs();
        drawEnemies();

        drawPlayerSprite();
        drawAttackFlash();

        drawRobots();
        drawRobotAttackLines();

        // Draw gold popups
        drawGoldPopups();

        // HUD on top
        hudOverlay.render();
        drawDialogOverlay();

        robotAttackLines.clear();
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
        enemy.alive = true;
        enemy.attackCooldown = ENEMY_MELEE_COOLDOWN;
        enemy.attackTimer = 0f;
        enemy.spriteIndex = Math.floorMod(index, enemyAnimations.length);
        enemy.patrolTarget = randomPatrolTarget(spawnPoint);
        return enemy;
    }

    private int getInfiniteDungeonEnemyCount() {
        if (currentZone == null) {
            return 0;
        }
        int floor = getInfiniteDungeonCurrentFloor();
        if (floor > 0 && floor % INFINITE_DUNGEON_BOSS_INTERVAL == 0) {
            return 1;
        }
        int desired = 2 + Math.min(2, Math.max(0, (floor - 1) / 4));
        return Math.min(Math.max(2, desired), currentZone.enemySpawns.size);
    }

    private MonsterDefinition pickInfiniteDungeonMonster(int index) {
        if (currentZoneDefinition == null || currentZoneDefinition.getMonsterIds() == null || currentZoneDefinition.getMonsterIds().length == 0) {
            return null;
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
        float tileSize = currentZone != null ? currentZone.tileWidth : 48f;
        float halfW = (gameCamera.viewportWidth * gameCamera.zoom) / 2f + tileSize;
        float halfH = (gameCamera.viewportHeight * gameCamera.zoom) / 2f + tileSize;
        float maxWidth = currentZone != null ? currentZone.pixelWidth : playerPos.x + halfW;
        float maxHeight = currentZone != null ? currentZone.pixelHeight : playerPos.y + halfH;
        int startX = Math.max(0, (int) Math.floor((playerPos.x - halfW) / tileSize) - 1);
        int endX = Math.min((int) Math.ceil(maxWidth / tileSize), (int) Math.ceil((playerPos.x + halfW) / tileSize) + 1);
        int startY = Math.max(0, (int) Math.floor((playerPos.y - halfH) / tileSize) - 1);
        int endY = Math.min((int) Math.ceil(maxHeight / tileSize), (int) Math.ceil((playerPos.y + halfH) / tileSize) + 1);

        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        for (int gx = startX; gx <= endX; gx++) {
            for (int gy = startY; gy <= endY; gy++) {
                Texture tile = ((gx + gy) & 1) == 0 ? groundTileTexture : wallTileTexture;
                applyGroundTint(((gx + gy) & 1) == 0);
                batch.draw(tile, gx * tileSize, gy * tileSize, tileSize, tileSize);
            }
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void applyGroundTint(boolean primaryTile) {
        if (currentZone == null) {
            batch.setColor(Color.WHITE);
            return;
        }
        switch (currentZone.groundStyle) {
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
        boolean allDead = true;

        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            allDead = false;

            // Update attack timer
            if (enemy.attackTimer > 0) {
                enemy.attackTimer -= delta;
            }

            float distToPlayer = enemy.pos.dst(playerPos);

            if (distToPlayer <= ENEMY_AGGRO_RANGE) {
                Vector2 direction = new Vector2(playerPos).sub(enemy.pos);
                if (direction.len() > 0) {
                    direction.nor();
                    enemy.facing.set(direction);
                    enemy.animationTime += delta;
                }
                enemy.pos.x += direction.x * enemy.speed * delta;
                enemy.pos.y += direction.y * enemy.speed * delta;
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
            if (!enemy.alive) continue;
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
            if (!enemy.alive) continue;
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
        if (isFeatureInteractable(feature) && isFacingInteractionRect(feature.bounds)) {
            font.draw(batch, "E: " + getFeatureActionLabel(feature), feature.bounds.x + 8f, feature.bounds.y - 10f);
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
        for (Npc npc : npcs) {
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

        uiViewport.apply();
        float w = uiViewport.getWorldWidth();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.05f, 0.06f, 0.1f, 0.92f);
        shapeRenderer.rect(30f, 30f, w - 60f, 100f);
        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, activeSpeaker, 48f, 112f);
        font.draw(batch, activeDialog, 48f, 78f);
        font.draw(batch, "Press E near an NPC to change the conversation.", 48f, 48f);
        batch.end();
    }

    private void transitionToGameOver() {
        GameOverScreen gameOverScreen = new GameOverScreen(game, screenManager);
        gameOverScreen.setGameStats((int) totalGold, totalEnemiesKilled, survivalTime);
        screenManager.replace(gameOverScreen);
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        uiViewport.update(width, height, true);
        hudOverlay.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        hudOverlay.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        groundTileTexture.dispose();
        wallTileTexture.dispose();
        doorTexture.dispose();
        chestTexture.dispose();
        shadowTexture.dispose();
        disposeAnimationSet(playerAnimation);
        for (AnimationSet animationSet : robotAnimations) {
            disposeAnimationSet(animationSet);
        }
        for (AnimationSet animationSet : enemyAnimations) {
            disposeAnimationSet(animationSet);
        }
        for (AnimationSet animationSet : npcAnimations) {
            disposeAnimationSet(animationSet);
        }
    }

    private void disposeAnimationSet(AnimationSet animationSet) {
        for (Texture texture : animationSet.getOwnedTextures()) {
            texture.dispose();
        }
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
            screenManager.push(new ForgeScreen(game, screenManager, this));
        }
    }

    public void dismissMessages() {
        activeSpeaker = null;
        activeDialog = null;
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

    public SaveFile buildSaveFile(int slot) {
        syncActiveRobotHealthToProgression();
        SaveFile sf = new SaveFile();
        sf.setPlayerName(playerName);
        sf.setPlayerHp((int) playerHealth);
        sf.setPlayerMaxHp((int) playerMaxHealth);
        sf.setPlayerX(playerPos.x);
        sf.setPlayerY(playerPos.y);
        sf.setCurrencyBalance(gameState.getTotalGold());
        sf.setHealingPotions(gameState.getHealingPotions());
        sf.setPlayerLevel(gameState.getPlayerLevel());
        sf.setPlayerExperience(gameState.getPlayerExperience());
        sf.setPlayerEquipment(new HashMap<>(gameState.getPlayerEquipmentSlots()));
        sf.setOwnedEquipmentIds(new ArrayList<>(gameState.getOwnedEquipmentIds()));
        sf.setQuestFlags(new HashMap<>(gameState.getQuestFlags()));
        sf.setQuestStates(new HashMap<>(gameState.getQuestStates()));
        sf.setBestiaryScanLevels(new HashMap<>(gameState.getBestiaryScanLevels()));
        sf.setKeyItems(new ArrayList<>(gameState.getKeyItems()));
        sf.setWorldStateFlags(new HashMap<>(gameState.getWorldStateFlags()));
        sf.setSettlementUpgrades(new HashMap<>(gameState.getSettlementUpgrades()));
        sf.setForgeComponents(new HashMap<>(gameState.getForgeComponents()));
        sf.setShardInventory(new HashMap<>(gameState.getShardInventory()));
        sf.setForgeCoreLevel(gameState.getForgeCoreLevel());
        sf.setInfiniteDungeonCurrentFloor(gameState.getInfiniteDungeonCurrentFloor());
        sf.setInfiniteDungeonBestFloor(gameState.getInfiniteDungeonBestFloor());
        sf.setInfiniteDungeonFloorsCleared(gameState.getInfiniteDungeonFloorsCleared());
        sf.setInfiniteDungeonRunActive(gameState.isInfiniteDungeonRunActive());
        sf.setDefeatedBossIds(new ArrayList<>(gameState.getDefeatedBossIds()));
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
        gameState.setInfiniteDungeonCurrentFloor(saveFile.getInfiniteDungeonCurrentFloor());
        gameState.setInfiniteDungeonBestFloor(saveFile.getInfiniteDungeonBestFloor());
        gameState.setInfiniteDungeonFloorsCleared(saveFile.getInfiniteDungeonFloorsCleared());
        gameState.setInfiniteDungeonRunActive(saveFile.isInfiniteDungeonRunActive());
        loadZone(currentZoneId, null, true);
        playerHealth = saveFile.getPlayerHp();
        playerMaxHealth = saveFile.getPlayerMaxHp() > 0 ? saveFile.getPlayerMaxHp() : playerMaxHealth;
        playerPos.set(saveFile.getPlayerX(), saveFile.getPlayerY());
        totalGold = saveFile.getCurrencyBalance();
        healingPotions = saveFile.getHealingPotions();
        playerLevel = Math.max(1, saveFile.getPlayerLevel());
        playerExperience = Math.max(0, saveFile.getPlayerExperience());
        gameState.setPlayerHealth(playerHealth);
        gameState.setPlayerMaxHealth(playerMaxHealth);
        gameState.setTotalGold(totalGold);
        gameState.setHealingPotions(healingPotions);
        gameState.setPlayerLevel(playerLevel);
        gameState.setPlayerExperience(playerExperience);
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
        questFlags.clear();
        if (saveFile.getQuestFlags() != null) {
            questFlags.putAll(saveFile.getQuestFlags());
        }
        gameState.setQuestFlags(questFlags);
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
        gameState.setShardInventory(saveFile.getShardInventory());
        gameState.setForgeCoreLevel(saveFile.getForgeCoreLevel());
        gameState.setDefeatedBossIds(saveFile.getDefeatedBossIds());
        worldStateManager.initialize(gameState);
        questManager.initialize(gameState);
        questManager.syncProgress(gameState, worldStateManager);
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
        normalizeActiveRobotSlots();
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
            SaveFile.EnemyState enemyState = new SaveFile.EnemyState();
            enemyState.setMonsterId(enemy.monsterId);
            enemyState.setX(enemy.pos.x);
            enemyState.setY(enemy.pos.y);
            enemyState.setHp(enemy.hp);
            enemyState.setMaxHp(enemy.maxHp);
            enemyState.setSpeed(enemy.speed);
            enemyState.setSize(enemy.size);
            enemyState.setDefense(enemy.defense);
            enemyState.setAgility(enemy.agility);
            enemyState.setStrength(enemy.strength);
            enemyState.setIntelligence(enemy.intelligence);
            enemyState.setStamina(enemy.stamina);
            enemyState.setRewardGold(enemy.rewardGold);
            enemyState.setRewardExperience(enemy.rewardExperience);
            enemyState.setName(enemy.name);
            enemyState.setAlive(enemy.alive);
            enemyState.setAttackTimer(enemy.attackTimer);
            enemyState.setPatrolTargetX(enemy.patrolTarget.x);
            enemyState.setPatrolTargetY(enemy.patrolTarget.y);
            enemyState.setDungeonFloor(enemy.dungeonFloor);
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
                gameState.addShard(entry.getKey(), entry.getValue());
            }
        }
        if (result.droppedComponents != null) {
            for (Map.Entry<String, Integer> entry : result.droppedComponents.entrySet()) {
                gameState.addForgeComponent(entry.getKey(), entry.getValue());
            }
        }
        handleBattleStoryEvents(result);
        handleInfiniteDungeonBattleResolution(result);

        if (!hasLivingPartyMember()) {
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
        regenerateInfiniteDungeonFloor("from_boss_gate", true);
        activeSpeaker = "Bolt Simulation";
        activeDialog = completedFloor % INFINITE_DUNGEON_BOSS_INTERVAL == 0
            ? "Boss floor cleared. Routing you deeper into the challenge loop."
            : "Floor " + completedFloor + " cleared. Preparing the next trial.";
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
        for (String enemyId : defeatedEnemyIds) {
            if (!isBossMonster(enemyId)) {
                continue;
            }
            if (gameState.markBossDefeated(enemyId)) {
                milestoneTriggered = updateForgeCoreMilestones() || milestoneTriggered;
            }
            triggerStoryEvents("BOSS_DEFEAT", enemyId);
        }
        questManager.syncProgress(gameState, worldStateManager);
        if (milestoneTriggered) {
            refreshHud();
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
        if (definition.getAddKeyItem() != null && !definition.getAddKeyItem().isEmpty()) {
            addKeyItem(definition.getAddKeyItem());
        }
        if (definition.getRewardExperience() > 0) {
            addExperience(definition.getRewardExperience());
        }
        if (definition.getSpeaker() != null && !definition.getSpeaker().isEmpty()) {
            activeSpeaker = definition.getSpeaker();
        }
        if (definition.getText() != null && !definition.getText().isEmpty()) {
            activeDialog = definition.getText();
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
                if (gameState.consumeForgeComponents(cost)) {
                    evolveRobotAtIndex(i, state);
                } else {
                    state.setEvolutionTier(tierBefore);
                }
            }
        }
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
        hudOverlay.setObjectiveText(getCurrentObjective());
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
            normalized.add(robotId != null && !robotId.isEmpty() ? robotId : null);
        }
        while (normalized.size() < ROBOT_COUNT) {
            normalized.add(null);
        }
        if (normalized.size() > ROBOT_COUNT) {
            normalized = new ArrayList<>(normalized.subList(0, ROBOT_COUNT));
        }
        activeRobotIds = normalized;
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
        ZoneDefinition[] definitions = new Json().fromJson(ZoneDefinition[].class, Gdx.files.internal("data/zones.json").readString());
        if (definitions == null) {
            return;
        }
        for (ZoneDefinition definition : definitions) {
            zoneDefinitions.put(definition.getId(), definition);
        }
    }

    private void loadRobotDefinitions() {
        RobotDefinition[] definitions = new Json().fromJson(RobotDefinition[].class, Gdx.files.internal("data/robots.json").readString());
        if (definitions == null) {
            return;
        }
        for (RobotDefinition definition : definitions) {
            if (definition != null && definition.getId() != null) {
                robotDefinitions.put(definition.getId(), definition);
            }
        }
    }

    private void loadMonsterDefinitions() {
        MonsterDefinition[] definitions = new Json().fromJson(MonsterDefinition[].class, Gdx.files.internal("data/monsters.json").readString());
        if (definitions == null) {
            return;
        }
        for (MonsterDefinition definition : definitions) {
            monsterDefinitions.put(definition.getId(), definition);
        }
    }

    private void loadForgeComponentDefinitions() {
        ForgeComponentDefinition[] definitions = new Json().fromJson(
            ForgeComponentDefinition[].class,
            Gdx.files.internal("data/forge_components.json").readString()
        );
        if (definitions == null) {
            return;
        }
        for (ForgeComponentDefinition definition : definitions) {
            if (definition != null && definition.getId() != null) {
                forgeComponentDefinitions.put(definition.getId(), definition);
            }
        }
    }

    private void loadForgeRecipes() {
        ForgeRecipeDefinition[] definitions = new Json().fromJson(
            ForgeRecipeDefinition[].class,
            Gdx.files.internal("data/forge_recipes.json").readString()
        );
        if (definitions == null) {
            return;
        }
        forgeRecipes.clear();
        for (ForgeRecipeDefinition definition : definitions) {
            if (definition != null && definition.getId() != null) {
                forgeRecipes.add(definition);
            }
        }
    }

    private void loadStoryEvents() {
        StoryEventDefinition[] definitions = new Json().fromJson(
            StoryEventDefinition[].class,
            Gdx.files.internal("data/story_events.json").readString()
        );
        storyEvents.clear();
        if (definitions == null) {
            return;
        }
        for (StoryEventDefinition definition : definitions) {
            if (definition != null && definition.getId() != null) {
                storyEvents.add(definition);
            }
        }
    }

    private void loadShopDefinitions() {
        ShopDefinition[] definitions = new Json().fromJson(ShopDefinition[].class, Gdx.files.internal("data/shop_inventories.json").readString());
        if (definitions == null) {
            return;
        }
        shopsByZoneId.clear();
        for (ShopDefinition definition : definitions) {
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
        currentZone = worldLoader.load(definition);
        handleInfiniteDungeonZoneLoad(previousZoneId, spawnId);
        if (isInfiniteDungeonZone()) {
            currentZone = infiniteDungeonLayoutGenerator.generate(currentZone, getInfiniteDungeonCurrentFloor());
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

        for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
            if (npcData.hiddenUntilFlag != null && !npcData.hiddenUntilFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, npcData.hiddenUntilFlag)) {
                continue;
            }
            if (npcData.requiredWorldFlag != null && !npcData.requiredWorldFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, npcData.requiredWorldFlag)) {
                continue;
            }
            npcs.add(new Npc(npcData.id, npcData.name, new Vector2(npcData.position), ""));
        }

        addSettlementTownContent();

        Vector2 resolvedSpawn = resolvePlayerSpawn(spawnId);
        if (resolvedSpawn != null) {
            playerPos.set(resolvedSpawn);
            positionRobotsBehindPlayer();
        }

        if (resetEnemies) {
            spawnEnemies();
        }
        triggerStoryEvents("ZONE_ENTER", zoneId);
        refreshHud();
        autosave();
    }

    private void handleInfiniteDungeonZoneLoad(String previousZoneId, String spawnId) {
        if (!isInfiniteDungeonZone()) {
            return;
        }
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
            activeSpeaker = "Bolt Simulation";
            activeDialog = "Challenge loop synchronized. Resuming at floor "
                + gameState.getInfiniteDungeonCurrentFloor() + ".";
        } else if ("from_boss_gate".equals(spawnId)) {
            activeSpeaker = "Bolt Simulation";
            activeDialog = "Deep trial gate accepted. Floor "
                + gameState.getInfiniteDungeonCurrentFloor() + " is now active.";
        }
    }

    private void regenerateInfiniteDungeonFloor(String spawnId, boolean resetEnemies) {
        if (!isInfiniteDungeonZone() || currentZoneDefinition == null) {
            return;
        }
        currentZone = worldLoader.load(currentZoneDefinition);
        currentZone = infiniteDungeonLayoutGenerator.generate(currentZone, getInfiniteDungeonCurrentFloor());
        houses.clear();
        npcs.clear();
        for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
            npcs.add(new Npc(npcData.id, npcData.name, new Vector2(npcData.position), ""));
        }
        Vector2 resolvedSpawn = resolvePlayerSpawn(spawnId);
        if (resolvedSpawn != null) {
            playerPos.set(resolvedSpawn);
            positionRobotsBehindPlayer();
        }
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
        return "town".equals(currentZoneId) || "verdant_fields".equals(currentZoneId);
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
            npcs.add(new Npc("quartermaster", "Quartermaster",
                workshop != null ? new Vector2(workshop.x + workshop.width + 22f, workshop.y + 22f) : new Vector2(280f, 210f),
                "The forge rail is live again. Toma's workshop can finally stock heavy chassis parts."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.watchtower_network")) {
            npcs.add(new Npc("lookout", "Lookout",
                lodge != null ? new Vector2(lodge.x + lodge.width + 18f, lodge.y + lodge.height - 6f) : new Vector2(190f, 270f),
                "The watchtower lamps are sweeping farther every night. Routes that used to vanish now stay marked."));
        }
        if (worldStateManager.isFlagActive(gameState, "settlement.survey_drones")) {
            npcs.add(new Npc("dispatcher", "Dispatcher",
                workshop != null ? new Vector2(workshop.x - 18f, workshop.y + workshop.height - 4f) : new Vector2(235f, 250f),
                "Survey drones are airborne again. Bring me fresh route intel and I'll keep Ironhaven's map board honest."));
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
        } else {
            return null;
        }
        house.zoneId = currentZoneId;
        return house;
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

    private boolean hasQuestFlag(String flag) {
        return gameState.hasQuestFlag(flag);
    }

    private void setQuestFlag(String flag, boolean value) {
        gameState.setQuestFlag(flag, value);
        questFlags.put(flag, value);
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

    private DialogueSystem.DialogueResult resolveNpcDialogue(String npcId, String speakerName) {
        questManager.syncProgress(gameState, worldStateManager);
        DialogueSystem.DialogueResult result = dialogueSystem.resolve(
            npcId,
            currentZoneId,
            gameState,
            questManager,
            worldStateManager
        );
        if (result == null) {
            result = new DialogueSystem.DialogueResult();
            result.speaker = speakerName;
            result.text = "The road keeps changing, but a good crew still finds its way.";
        }
        applyDialogueResult(result);
        refreshHud();
        return result;
    }

    private void applyDialogueResult(DialogueSystem.DialogueResult result) {
        if (result == null) {
            return;
        }
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
        if (result.setWorldFlag != null && !result.setWorldFlag.isEmpty()) {
            worldStateManager.setFlag(gameState, result.setWorldFlag, true);
        }
        if (result.settlementUpgradeId != null && !result.settlementUpgradeId.isEmpty()) {
            applySettlementUpgrade(result.settlementUpgradeId);
        }
        if (result.recruitEventId != null && !result.recruitEventId.isEmpty()) {
            applyRecruitment(result.recruitEventId);
        }
        questManager.syncProgress(gameState, worldStateManager);
    }

    private void applyRecruitment(String eventId) {
        RobotRecruitmentManager.RecruitmentResult recruitment = recruitmentManager.apply(eventId, collectedRobotIds, activeRobotIds);
        if (recruitment == null) {
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
        }
    }

    private void applySettlementUpgrade(String upgradeId) {
        SettlementState state = gameState.getSettlementUpgrade(upgradeId);
        if (state == null) {
            state = new SettlementState(upgradeId, 0);
        }
        state.setLevel(state.getLevel() + 1);
        gameState.putSettlementUpgrade(state);
        SettlementManager settlement = settlementManager;
        if (settlement.get(upgradeId) != null) {
            if (settlement.get(upgradeId).getRewardEquipmentId() != null
                && !settlement.get(upgradeId).getRewardEquipmentId().isEmpty()) {
                unlockEquipment(settlement.get(upgradeId).getRewardEquipmentId());
            }
            if (settlement.get(upgradeId).getRewardPotions() > 0) {
                addHealingPotions(settlement.get(upgradeId).getRewardPotions());
            }
        }
        worldStateManager.setFlag(gameState, "settlement." + upgradeId, true);
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
        EquipmentItem[] definitions = new Json().fromJson(
            EquipmentItem[].class,
            Gdx.files.internal("data/equipment.json").readString()
        );
        if (definitions != null) {
            for (EquipmentItem item : definitions) {
                addEquipmentToCatalog(item);
            }
        }
        seedStarterOwnedEquipment();
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
        Npc nearbyNpc = null;
        float nearestDistance = 70f;

        for (Npc npc : npcs) {
            float distance = playerPos.dst(npc.pos);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearbyNpc = npc;
            }
        }

        if (nearbyNpc != null) {
            DialogueSystem.DialogueResult conversation = resolveNpcDialogue(nearbyNpc.id, nearbyNpc.name);
            activeSpeaker = conversation.speaker != null ? conversation.speaker : nearbyNpc.name;
            activeDialog = conversation.text;
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
            activeSpeaker = null;
            activeDialog = null;
        }
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
                activeSpeaker = null;
                activeDialog = null;
                screenManager.push(new HouseInteriorScreen(game, screenManager, this, house));
                return true;
            }
        }
        return false;
    }

    private boolean tryReadHouseSign() {
        for (House house : houses) {
            if (canReadHouseSign(house)) {
                activeSpeaker = "Sign";
                activeDialog = house.name;
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
                activeSpeaker = "Warning";
                activeDialog = door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage + " You can still press through if you're ready."
                    : "This route is dangerous for an unprepared party.";
            }
            if (door.lockedByFlag != null
                && !hasQuestFlag(door.lockedByFlag)
                && !worldStateManager.isFlagActive(gameState, door.lockedByFlag)) {
                activeSpeaker = "Warning";
                activeDialog = door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage + " The frontier won't stop you, but it may punish you."
                    : "You have not completed the local objective yet.";
            }
            if (door.requiredWorldFlag != null && !door.requiredWorldFlag.isEmpty()
                && !worldStateManager.isFlagActive(gameState, door.requiredWorldFlag)) {
                activeSpeaker = "Warning";
                activeDialog = door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage
                    : "The route is dormant for now.";
                return true;
            }
            if (door.targetZoneId != null) {
                if (isInfiniteDungeonZone() && currentZoneId.equals(door.targetZoneId)) {
                    if (!allDungeonEnemiesDefeated()) {
                        activeSpeaker = "Bolt Simulation";
                        activeDialog = door.lockMessage != null && !door.lockMessage.isEmpty()
                            ? door.lockMessage
                            : "Clear the current floor before the next gate will answer.";
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
                loadZone(door.targetZoneId, door.targetSpawnId, true);
                refreshHud();
                return true;
            }
        }
        return false;
    }

    private boolean currentZoneIsSafe() {
        return currentZone != null && currentZone.safeZone;
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
            if (chest.questFlag != null && !chest.questFlag.isEmpty()) {
                setQuestFlag(chest.questFlag, true);
                worldStateManager.setFlag(gameState, chest.questFlag, true);
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
            activeSpeaker = "Chest";
            activeDialog = chest.message;
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
            if (hasWorldInteractionCapability(feature.interactionType)) {
                if (feature.completionWorldFlag != null && !feature.completionWorldFlag.isEmpty()) {
                    worldStateManager.setFlag(gameState, feature.completionWorldFlag, true);
                }
                questManager.syncProgress(gameState, worldStateManager);
                activeSpeaker = feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier";
                activeDialog = feature.interactionMessage != null && !feature.interactionMessage.isEmpty()
                    ? feature.interactionMessage
                    : "Your crew clears the obstruction and opens the route ahead.";
                refreshHud();
                return true;
            }
            activeSpeaker = feature.label != null && !feature.label.isEmpty() ? feature.label : "Frontier";
            activeDialog = feature.blockedMessage != null && !feature.blockedMessage.isEmpty()
                ? feature.blockedMessage
                : "Your current crew can't clear this obstacle yet.";
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
        return feature != null
            && feature.completionWorldFlag != null
            && !feature.completionWorldFlag.isEmpty()
            && worldStateManager.isFlagActive(gameState, feature.completionWorldFlag);
    }

    private boolean isFeatureInteractable(TmxWorldLoader.Feature feature) {
        return feature != null
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
            case "scan_hidden_path":
                return "Scan";
            case "burn_barrier":
                return "Burn Away";
            case "strength_boulder":
                return "Clear";
            default:
                return "Interact";
        }
    }

    private boolean hasWorldInteractionCapability(String interactionType) {
        switch (interactionType) {
            case "scan_hidden_path":
                return hasAnyActiveAbility("scan", "deep_scan");
            case "burn_barrier":
                return hasAnyActiveAbility("rapid_fire", "storm_barrage", "limit_breaker");
            case "strength_boulder":
                return hasAnyActiveAbility("power_strike", "seismic_break", "limit_breaker", "shield_wall", "bulwark_matrix");
            default:
                return false;
        }
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
        gameState.addGold(amount);
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
        gameState.addHealingPotions(amount);
        healingPotions = gameState.getHealingPotions();
        refreshHud();
    }

    public int getHealingPotions() {
        return gameState.getHealingPotions();
    }

    public void addExperience(int amount) {
        gameState.setPlayerHealth(playerHealth);
        gameState.addExperience(amount, this::getExperienceRequirementForLevel);
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
        EquipmentTotals equipmentTotals = getPlayerEquipmentTotals();
        float levelOffset = playerLevel - 1;
        return new RobotStatBlock(
            playerHealth,
            playerMaxHealth + (levelOffset * 6f) + equipmentTotals.hpBonus,
            PLAYER_AGILITY + (levelOffset * 0.5f) + equipmentTotals.agilityBonus,
            PLAYER_STRENGTH + (levelOffset * 0.7f) + equipmentTotals.strengthBonus,
            PLAYER_INTELLIGENCE + (levelOffset * 0.65f) + equipmentTotals.intelligenceBonus,
            PLAYER_STAMINA + (levelOffset * 0.6f) + equipmentTotals.staminaBonus
        );
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
        List<String> lines = new ArrayList<>();
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
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        EquipmentTotals equipmentTotals = getEquipmentTotals(index);
        int robotLevel = progressionState != null ? progressionState.getLevel() : 1;
        int evolutionTier = progressionState != null ? progressionState.getEvolutionTier() : 1;
        float levelBonus = Math.max(0f, (robotLevel - 1) * RobotEvolutionManager.levelBonusPerLevel());
        float evolutionMultiplier = RobotEvolutionManager.statMultiplier(evolutionTier);
        float maxHealth = getRobotEffectiveMaxHealth(getRobotId(index), progressionState);
        RobotStatBlock block = new RobotStatBlock(
            robot.health,
            maxHealth,
            (robot.agility + levelBonus) * evolutionMultiplier + equipmentTotals.agilityBonus,
            (robot.strength + levelBonus) * evolutionMultiplier + equipmentTotals.strengthBonus,
            (robot.intelligence + levelBonus) * evolutionMultiplier + equipmentTotals.intelligenceBonus,
            (robot.stamina + levelBonus) * evolutionMultiplier + equipmentTotals.staminaBonus
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
                    && candidate.getSlotType().equals(item1.getSlotType())) {
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
        return new ArrayList<>(forgeRecipes);
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
            if (count > 0) {
                lines.add(definition.getName() + " x" + count + " [" + definition.getRarity() + "] (Sell "
                    + getForgeComponentSellValue(definition.getId()) + "g)");
            }
        }
        if (lines.isEmpty()) {
            lines.add("No forge components collected yet.");
        }
        return lines;
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
            if (count > 0) {
                lines.add(grade + "-Grade Shard x" + count + " (Sell " + getShardSellValue(grade) + "g)");
            }
        }
        if (lines.isEmpty()) {
            lines.add("No graded shards recovered yet.");
        }
        return lines;
    }

    public boolean canForgeRecipe(ForgeRecipeDefinition recipe) {
        if (recipe == null || recipe.getResultEquipmentId() == null) {
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
        if (recipeIndex < 0 || recipeIndex >= forgeRecipes.size()) {
            return "That forge pattern is unavailable.";
        }
        ForgeRecipeDefinition recipe = forgeRecipes.get(recipeIndex);
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
            if ("healing".equals(entry.getType())) {
                inventory.addHealingItem(entry.getLabel(), Math.max(1, entry.getQuantity()), entry.getCost());
                continue;
            }
            if ("equipment".equals(entry.getType())) {
                EquipmentItem item = findEquipmentItem(entry.getItemId());
                if (item != null) {
                    inventory.addEquipmentItem(item, entry.getCost() > 0 ? entry.getCost() : item.getCost());
                }
            }
        }
        return inventory;
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
        activeSpeaker = "Forge Core";
        if (targetLevel == 2) {
            activeDialog = "Five boss systems have fallen. Forge Core Lv2 is online. Tier-II evolutions and a second party slot are now unlocked.";
        } else if (targetLevel == 3) {
            activeDialog = "Ten boss systems have fallen. Forge Core Lv3 surges to life. Tier-III evolutions and a third party slot are now unlocked.";
        } else {
            activeDialog = "Fifteen boss systems have fallen. Forge Core Lv4 reaches full resonance. Ironhaven's highest forge functions are now available.";
        }
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
     * Inner class representing an enemy.
     */
    private static class Enemy {
        Vector2 pos;
        Vector2 facing;
        float hp, maxHp, speed, size;
        float defense;
        float agility;
        float strength;
        float intelligence;
        float stamina;
        int rewardGold;
        int rewardExperience;
        String name;
        String monsterId;
        boolean alive;
        int spriteIndex;
        float animationTime;
        float attackCooldown, attackTimer;
        Vector2 patrolTarget;
        int dungeonFloor;
    }

    /**
     * Inner class representing an individual robot companion.
     */
    private static class RobotCompanion {
        Vector2 pos;
        Vector2 facing;
        String grade;
        float health;
        float maxHealth;
        float attackTimer;
        float angleDeg;
        float animationTime;
        float agility;
        float strength;
        float intelligence;
        float stamina;
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
    }

    /**
     * Simple static NPC with a single dialogue line.
     */
    private static class Npc {
        String id;
        String name;
        Vector2 pos;
        String dialog;

        Npc(String id, String name, Vector2 pos, String dialog) {
            this.id = id;
            this.name = name;
            this.pos = pos;
            this.dialog = dialog;
        }
    }

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

        Texture[] getOwnedTextures() {
            List<Texture> textures = new ArrayList<>();
            appendTexture(textures, downIdle);
            appendTexture(textures, downWalk);
            appendTexture(textures, sideIdle);
            appendTexture(textures, sideWalk);
            appendTexture(textures, upIdle);
            appendTexture(textures, upWalk);
            return textures.toArray(new Texture[0]);
        }

        private void appendTexture(List<Texture> textures, TextureRegion[] frames) {
            if (frames.length == 0) {
                return;
            }
            Texture texture = frames[0].getTexture();
            if (!textures.contains(texture)) {
                textures.add(texture);
            }
        }
    }
}
