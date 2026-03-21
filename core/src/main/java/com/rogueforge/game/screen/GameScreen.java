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
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.core.GameLoop;
import com.rogueforge.game.core.GameState;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityRegistry;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.data.SaveFile;
import com.rogueforge.game.data.ShopDefinition;
import com.rogueforge.game.data.ShopEntryDefinition;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.economy.ShopInventory;
import com.rogueforge.game.persistence.SaveManager;
import com.rogueforge.game.persistence.SettingsManager;
import com.rogueforge.game.progression.AbilityEvolutionManager;
import com.rogueforge.game.progression.AbilityProgressionState;
import com.rogueforge.game.progression.RobotEvolutionManager;
import com.rogueforge.game.progression.RobotProgressionState;
import com.rogueforge.game.progression.WeaponProficiencyState;
import com.rogueforge.game.progression.WeaponProficiencyTracker;
import com.rogueforge.game.robot.RobotDefinition;
import com.rogueforge.game.world.TmxWorldLoader;
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
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameLoop gameLoop;
    private final OrthographicCamera gameCamera;
    private final OrthographicCamera uiCamera;
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
    private final Map<String, RobotDefinition> robotDefinitions = new HashMap<>();
    private final TmxWorldLoader worldLoader = new TmxWorldLoader();
    private final SettingsManager settingsManager = new SettingsManager();
    private final SaveManager saveManager = new SaveManager();
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
        this.gameCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        if (collectedRobotIds.isEmpty()) {
            collectedRobotIds.addAll(activeRobotIds);
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

        initializeEquipmentCatalog();
        loadZoneDefinitions();
        loadRobotDefinitions();
        loadMonsterDefinitions();
        loadShopDefinitions();
        ensureRobotProgressionStates();
        loadZone(currentZoneId, "town_square", true);

        hudOverlay.setPlayerHealth(playerHealth, playerMaxHealth);
        hudOverlay.setCurrency(totalGold);
        hudOverlay.setRobotHealth(getRobotHealthValues(), getRobotMaxHealthValues());

        if (saveFile != null) {
            loadFromSave(saveFile);
        }
    }

    @Override
    public void show() {
        gameInputProcessor = new GameInputProcessor(this);
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
        int count = Math.min(INITIAL_ENEMY_COUNT, currentZone.enemySpawns.size);
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
        MonsterDefinition monster = pickMonsterForCurrentZone(index);
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
            enemy.rewardGold = monster.getBaseLoot();
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
            enemy.rewardGold = 15 + (int) (Math.random() * 16);
            enemy.name = "Scrap Beast";
        }
        enemy.alive = true;
        enemy.attackCooldown = ENEMY_MELEE_COOLDOWN;
        enemy.attackTimer = 0f;
        enemy.spriteIndex = Math.floorMod(index, enemyAnimations.length);
        enemy.patrolTarget = randomPatrolTarget(spawnPoint);
        return enemy;
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
        float halfW = Gdx.graphics.getWidth() / 2f + tileSize;
        float halfH = Gdx.graphics.getHeight() / 2f + tileSize;
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
            case "cave":
                batch.setColor(primaryTile ? new Color(0.42f, 0.44f, 0.5f, 1f) : new Color(0.22f, 0.24f, 0.3f, 1f));
                break;
            case "peak":
                batch.setColor(primaryTile ? new Color(0.78f, 0.8f, 0.88f, 1f) : new Color(0.58f, 0.62f, 0.72f, 1f));
                break;
            default:
                batch.setColor(primaryTile ? new Color(0.58f, 0.76f, 0.42f, 1f) : new Color(0.38f, 0.6f, 0.32f, 1f));
                break;
        }
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
                    if (!tryOpenWorldChest()) {
                        interactWithNpc();
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
        for (int i = 0; i < ROBOT_COUNT; i++) {
            RobotCompanion robot = robots[i];
            if (robot.health <= 0f) {
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
        int goldDrop = 10 + (int) (Math.random() * 21);
        onEnemyKilled(enemy, goldDrop);
    }

    private void onEnemyKilled(Enemy enemy, int goldDrop) {
        gameState.addGold(goldDrop);
        totalGold = gameState.getTotalGold();
        totalEnemiesKilled++;
        enemy.alive = false;

        GoldPopup popup = new GoldPopup();
        popup.pos = new Vector2(enemy.pos);
        popup.text = "+" + goldDrop + " Gold";
        popup.lifetime = 1f;
        popup.timer = 0f;
        goldPopups.add(popup);
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
                if (!"house".equals(feature.kind)) {
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
        if ("cliff".equals(feature.kind)) {
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
            if (robot.health <= 0f) {
                continue;
            }
            drawShadow(robot.pos.x, robot.pos.y, ROBOT_SIZE + 12f, 16f, 0.4f);
            TextureRegion sprite = getAnimatedFrame(robotAnimations[i % robotAnimations.length], robot.facing, true, robot.animationTime);
            drawAnimatedSprite(sprite, robot.facing, robot.pos.x - ROBOT_SIZE / 2, robot.pos.y - ROBOT_SIZE / 2, ROBOT_SIZE, ROBOT_SIZE);
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (RobotCompanion robot : robots) {
            if (robot.health <= 0f) {
                continue;
            }
            float barWidth = ROBOT_SIZE;
            float barHeight = 3f;
            float barX = robot.pos.x - barWidth / 2;
            float barY = robot.pos.y + ROBOT_SIZE / 2 + 4f;
            float hpPercent = robot.health / robot.maxHealth;

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

        for (RobotCompanion robot : robots) {
            if (robot.health <= 0f) {
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
            values[i] = robots[i].health;
        }
        return values;
    }

    private float[] getRobotMaxHealthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = getRobotStats(i).maxHealth;
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
            values[i] = getRobotStats(i).agility;
        }
        return values;
    }

    private float[] getRobotStrengthValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = getRobotStats(i).strength;
        }
        return values;
    }

    private float[] getRobotIntelligenceValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = getRobotStats(i).intelligence;
        }
        return values;
    }

    private float[] getRobotStaminaValues() {
        float[] values = new float[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            values[i] = getRobotStats(i).stamina;
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

        float w = Gdx.graphics.getWidth();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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
        gameCamera.setToOrtho(false, width, height);
        uiCamera.setToOrtho(false, width, height);
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

    public void resumeGame() { isPaused = false; }
    public boolean isPaused() { return isPaused; }
    public GameLoop getGameLoop() { return gameLoop; }
    public HUDOverlay getHUDOverlay() { return hudOverlay; }

    public SaveFile buildSaveFile(int slot) {
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
        sf.setBestiaryScanLevels(new HashMap<>(gameState.getBestiaryScanLevels()));
        sf.setKeyItems(new ArrayList<>(gameState.getKeyItems()));
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
        gameState.setBestiaryScanLevels(saveFile.getBestiaryScanLevels());
        keyItems.clear();
        if (saveFile.getKeyItems() != null) {
            keyItems.addAll(saveFile.getKeyItems());
        }
        gameState.setKeyItems(keyItems);
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
        activeRobotIds = saveFile.getActiveRobotIds() != null
            ? new ArrayList<>(saveFile.getActiveRobotIds())
            : new ArrayList<>();
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
            enemyState.setName(enemy.name);
            enemyState.setAlive(enemy.alive);
            enemyState.setAttackTimer(enemy.attackTimer);
            enemyState.setPatrolTargetX(enemy.patrolTarget.x);
            enemyState.setPatrolTargetY(enemy.patrolTarget.y);
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
            encounter.enemyRewardGold[i] = groupEnemy.rewardGold;
            encounter.enemyExperienceReward[i] = 20 + groupEnemy.rewardGold;
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
        encounter.robotNames = getRobotNames();
        encounter.robotHealth = getRobotHealthValues();
        encounter.robotMaxHealth = getRobotMaxHealthValues();
        encounter.robotAgility = getRobotAgilityValues();
        encounter.robotStrength = getRobotStrengthValues();
        encounter.robotIntelligence = getRobotIntelligenceValues();
        encounter.robotStamina = getRobotStaminaValues();
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
        applyRobotHealth(result.robotHealth);
        if (result.enemyReferences != null && result.enemyHealth != null) {
            int totalGoldEarned = 0;
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
                    totalGoldEarned += i < result.goldEarned.length ? result.goldEarned[i] : 0;
                    totalExperienceEarned += i < result.experienceEarned.length ? result.experienceEarned[i] : 0;
                    onEnemyKilled(enemy, i < result.goldEarned.length ? result.goldEarned[i] : enemy.rewardGold);
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
        if (result.droppedEquipmentIds != null) {
            for (String itemId : result.droppedEquipmentIds) {
                unlockEquipment(itemId);
            }
        }

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

    public BattleProgressionPreview previewRobotBattleProgression(int experienceEarned) {
        BattleProgressionPreview preview = new BattleProgressionPreview();
        for (int i = 0; i < activeRobotIds.size() && i < ROBOT_COUNT; i++) {
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
            if (simulatedLevel >= 10 && isGradeUnlocked("C")) {
                simulatedTier = 3;
            } else if (simulatedLevel >= 5 && isGradeUnlocked("E")) {
                simulatedTier = Math.max(simulatedTier, 2);
            }
            if (simulatedLevel > currentLevel) {
                preview.robotProgress.add(getRobotName(i) + " +" + (simulatedLevel - currentLevel) + " level(s)");
            }
            if (simulatedTier > currentTier) {
                preview.robotProgress.add(getRobotName(i) + " evolved to Tier " + simulatedTier);
            }
        }
        return preview;
    }

    private void applyRobotBattleExperience(int experienceEarned) {
        for (int i = 0; i < activeRobotIds.size() && i < ROBOT_COUNT; i++) {
            RobotProgressionState state = getRobotProgressionStateForPartyIndex(i);
            if (state == null) {
                continue;
            }
            RobotEvolutionManager.addExperience(state, experienceEarned);
            boolean evolved = RobotEvolutionManager.applyEvolution(state, getUnlockedGrade());
            if (evolved) {
                evolveRobotAtIndex(i, state);
            }
        }
    }

    private void applyRobotHealth(float[] values) {
        if (values == null) {
            return;
        }
        for (int i = 0; i < ROBOT_COUNT && i < values.length; i++) {
            robots[i].health = Math.max(0f, Math.min(getRobotStats(i).maxHealth, values[i]));
        }
    }

    private void refreshHud() {
        hudOverlay.setPlayerHealth(playerHealth, getPlayerStats().maxHealth);
        hudOverlay.setCurrency(totalGold);
        hudOverlay.setExperience(playerExperience, getExperienceForNextLevel());
        hudOverlay.setRobotHealth(getRobotHealthValues(), getRobotMaxHealthValues());
        hudOverlay.setZoneName(currentZone != null ? currentZone.displayName : formatZoneName(currentZoneId));
        hudOverlay.setObjectiveText(getCurrentObjective());
    }

    private boolean hasLivingPartyMember() {
        if (playerHealth > 0f) {
            return true;
        }
        for (RobotCompanion robot : robots) {
            if (robot.health > 0f) {
                return true;
            }
        }
        return false;
    }

    private String[] getRobotNames() {
        String[] names = new String[ROBOT_COUNT];
        for (int i = 0; i < ROBOT_COUNT; i++) {
            names[i] = getRobotName(i);
        }
        return names;
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

    private void loadShopDefinitions() {
        ShopDefinition[] definitions = new Json().fromJson(ShopDefinition[].class, Gdx.files.internal("data/shop_inventories.json").readString());
        if (definitions == null) {
            return;
        }
        for (ShopDefinition definition : definitions) {
            shopDefinitions.put(definition.getId(), definition);
        }
    }

    private void loadZone(String zoneId, String spawnId, boolean resetEnemies) {
        ZoneDefinition definition = zoneDefinitions.get(zoneId);
        if (definition == null) {
            return;
        }

        currentZoneId = zoneId;
        gameState.setCurrentZoneId(zoneId);
        currentZoneDefinition = definition;
        currentZone = worldLoader.load(definition);
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

        for (TmxWorldLoader.NpcData npcData : currentZone.npcs) {
            npcs.add(new Npc(npcData.id, npcData.name, new Vector2(npcData.position), ""));
        }

        if (spawnId != null && currentZone.playerSpawns.containsKey(spawnId)) {
            playerPos.set(currentZone.playerSpawns.get(spawnId));
            positionRobotsBehindPlayer();
        }

        if (resetEnemies) {
            spawnEnemies();
        }
        refreshHud();
        autosave();
    }

    private void positionRobotsBehindPlayer() {
        for (int i = 0; i < ROBOT_COUNT; i++) {
            robots[i].pos.set(playerPos.x, playerPos.y - ((i + 1) * ROBOT_FOLLOW_GAP));
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
        return hasKeyItem("workshop_pass") || hasQuestFlag("quest_shard_started");
    }

    private void addKeyItem(String keyItem) {
        gameState.addKeyItem(keyItem);
        if (keyItem != null && !keyItem.isEmpty() && !keyItems.contains(keyItem)) {
            keyItems.add(keyItem);
        }
    }

    private String getCurrentObjective() {
        if (!hasQuestFlag("quest_shard_started")) {
            return "Speak with Mira in Verdant Fields.";
        }
        if (!hasQuestFlag("quest_shard_completed")) {
            return hasKeyItem("luminous_shard")
                ? "Return the Luminous Shard to Mira."
                : "Search Shadow Caves for the Luminous Shard.";
        }
        if (!hasQuestFlag("quest_core_started")) {
            return "Travel to Dragon Peak and speak with Edda.";
        }
        if (!hasQuestFlag("quest_core_completed")) {
            return hasKeyItem("sun_core")
                ? "Return the Sun Core to Edda."
                : "Search Dragon Peak for the Sun Core.";
        }
        return "Explore the frontier and strengthen your crew.";
    }

    private NpcConversation buildNpcConversation(Npc npc) {
        NpcConversation conversation = new NpcConversation();
        if ("mira".equals(npc.id)) {
            if (!hasQuestFlag("quest_shard_started")) {
                setQuestFlag("quest_shard_started", true);
                addKeyItem("workshop_pass");
                conversation.dialog = "Take this Workshop Pass and search Shadow Caves for the Luminous Shard. Bring it back and I'll stabilize the lift to Dragon Peak. You received the Workshop Pass.";
            } else if (hasQuestFlag("quest_shard_found") && !hasQuestFlag("quest_shard_completed")) {
                setQuestFlag("quest_shard_completed", true);
                addKeyItem("peak_sigil");
                conversation.rewardGold = 120;
                conversation.rewardPotions = 2;
                conversation.rewardExperience = 90;
                conversation.dialog = "You found it. With this shard tuned, the peak lift will answer your sigil. Take these supplies and keep climbing.";
            } else if (!hasQuestFlag("quest_shard_completed")) {
                conversation.dialog = "The cave gate will open for the Workshop Pass. Find the Luminous Shard and watch for old scout caches.";
            } else {
                conversation.dialog = "Your bots are carrying real momentum now. The lift to Dragon Peak should answer the sigil I tuned for you.";
            }
        } else if ("tor".equals(npc.id)) {
            conversation.dialog = hasQuestFlag("quest_shard_completed")
                ? "The frontier opens in layers. Keep one eye on your party order and the other on the sky."
                : "Mira's pass should get you into the caves. Once you're inside, keep right at the broken pillars.";
        } else if ("edda_town".equals(npc.id)) {
            conversation.dialog = hasQuestFlag("quest_shard_completed")
                ? "The mountain lift is humming again. When you're ready, Dragon Peak is waiting."
                : "Mira won't let anyone near the lift until the cave shard is secured.";
        } else if ("cave_scout".equals(npc.id)) {
            conversation.dialog = hasKeyItem("luminous_shard")
                ? "That glow in your pack means you found the cache. Head back before the tunnels shift."
                : "I saw a strange cache deeper in. It was tucked beyond the central pillar where the cave forks east.";
        } else if ("edda_peak".equals(npc.id)) {
            if (!hasQuestFlag("quest_shard_completed")) {
                conversation.dialog = "The lift should not have carried you here without Mira's calibration. Strange.";
            } else if (!hasQuestFlag("quest_core_started")) {
                setQuestFlag("quest_core_started", true);
                conversation.dialog = "Dragon Peak's reactors are starving. Search the ridge for a Sun Core and we'll relight the old warding pylons.";
            } else if (hasQuestFlag("quest_core_found") && !hasQuestFlag("quest_core_completed")) {
                setQuestFlag("quest_core_completed", true);
                conversation.rewardGold = 220;
                conversation.rewardPotions = 3;
                conversation.rewardExperience = 140;
                conversation.dialog = "Perfect. The Sun Core is stable. Take this reward and keep pushing outward. The whole frontier is starting to wake up.";
            } else if (!hasQuestFlag("quest_core_completed")) {
                conversation.dialog = "The Sun Core should be stashed past the wind-broken ridge on the eastern path.";
            } else {
                conversation.dialog = "You've done more than reopen the lift. The frontier feels alive again.";
            }
        } else {
            conversation.dialog = npc.dialog != null && !npc.dialog.isEmpty()
                ? npc.dialog
                : "The road keeps changing, but a good crew still finds its way.";
        }
        refreshHud();
        return conversation;
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
        addEquipmentToCatalog(new EquipmentItem("bronze_edge", "Bronze Edge", "WEAPON", 0, 6, 0, 0, 0, 80, 1, "G", ""));
        addEquipmentToCatalog(new EquipmentItem("swift_boots", "Swift Boots", "LEGS", 0, 0, 1, 6, 0, 65, 1, "G", ""));
        addEquipmentToCatalog(new EquipmentItem("copper_core", "Copper Core", "BODY", 12, 0, 4, 0, 0, 95, 1, "F", ""));
        addEquipmentToCatalog(new EquipmentItem("tactics_lens", "Tactics Lens", "HEAD", 4, 0, 0, 2, 5, 90, 1, "F", ""));
        addEquipmentToCatalog(new EquipmentItem("heavy_plating", "Heavy Plating", "BODY", 20, 0, 8, -2, 0, 140, 2, "E", ""));
        addEquipmentToCatalog(new EquipmentItem("spark_ring", "Spark Ring", "ACCESSORY", 6, 2, 2, 2, 2, 110, 1, "E", ""));
        addEquipmentToCatalog(new EquipmentItem("mentor_sigil", "Mentor Sigil", "ACCESSORY", 0, 0, 0, 2, 6, 220, 2, "D", "XP_BOOST"));
        addEquipmentToCatalog(new EquipmentItem("vanguard_frame", "Vanguard Frame", "BODY", 24, 8, 6, 0, 0, 260, 2, "C", "FIRST_STRIKE"));
        addEquipmentToCatalog(new EquipmentItem("oracle_prism", "Oracle Prism", "HEAD", 8, 0, 0, 4, 12, 320, 3, "B", "ARCANE_SURGE"));
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
        return "bot_" + index;
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
        gameState.putRobotProgressionState(state);
        return state;
    }

    private void ensureRobotProgressionStates() {
        for (String robotId : collectedRobotIds) {
            getOrCreateRobotProgressionState(robotId);
        }
        for (String robotId : activeRobotIds) {
            getOrCreateRobotProgressionState(robotId);
        }
    }

    private int robotExperienceRequirementForLevel(int level) {
        return 35 + (Math.max(1, level) * 18);
    }

    private boolean isGradeUnlocked(String grade) {
        return gradeIndex(getUnlockedGrade()) >= gradeIndex(grade);
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
        Map<String, String> equipped = robotEquipment.get(getRobotId(partyIndex));
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
        state.addXp(xpAmount);
        List<String> unlocked = new ArrayList<>();
        for (int level = beforeLevel + 1; level <= state.getLevel(); level++) {
            String unlock = WeaponProficiencyTracker.unlockLabel(level);
            if (!unlock.isEmpty() && state.unlockMilestone(unlock)) {
                unlocked.add(unlock);
            }
        }
        return new WeaponGain(weaponType, beforeLevel, state.getLevel(), unlocked);
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
        Map<String, String> equipped = robotEquipment.get(getRobotId(index));
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
            NpcConversation conversation = buildNpcConversation(nearbyNpc);
            activeSpeaker = nearbyNpc.name;
            activeDialog = conversation.dialog;
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
            if (door.lockedByFlag != null && !hasQuestFlag(door.lockedByFlag)) {
                activeSpeaker = "Warning";
                activeDialog = door.lockMessage != null && !door.lockMessage.isEmpty()
                    ? door.lockMessage + " The frontier won't stop you, but it may punish you."
                    : "You have not completed the local objective yet.";
            }
            if (door.targetZoneId != null) {
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
            }
            activeSpeaker = "Chest";
            activeDialog = chest.message;
            refreshHud();
            return true;
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

    public String getRobotName(int index) {
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
        return robots[index].grade;
    }

    public int getRobotLevel(int index) {
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getLevel() : 1;
    }

    public int getRobotExperience(int index) {
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getExperience() : 0;
    }

    public int getRobotEvolutionTier(int index) {
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        return progressionState != null ? progressionState.getEvolutionTier() : 1;
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
        RobotCompanion robot = robots[index];
        EquipmentTotals equipmentTotals = getEquipmentTotals(index);
        RobotProgressionState progressionState = getRobotProgressionStateForPartyIndex(index);
        int robotLevel = progressionState != null ? progressionState.getLevel() : 1;
        int evolutionTier = progressionState != null ? progressionState.getEvolutionTier() : 1;
        float levelBonus = Math.max(0f, (robotLevel - 1) * RobotEvolutionManager.levelBonusPerLevel());
        float evolutionMultiplier = RobotEvolutionManager.statMultiplier(evolutionTier);
        return new RobotStatBlock(
            robot.health,
            (robot.maxHealth + (levelBonus * 4f)) * evolutionMultiplier + equipmentTotals.hpBonus,
            (robot.agility + levelBonus) * evolutionMultiplier + equipmentTotals.agilityBonus,
            (robot.strength + levelBonus) * evolutionMultiplier + equipmentTotals.strengthBonus,
            (robot.intelligence + levelBonus) * evolutionMultiplier + equipmentTotals.intelligenceBonus,
            (robot.stamina + levelBonus) * evolutionMultiplier + equipmentTotals.staminaBonus
        );
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
        String robotId = getRobotId(index);
        return gameState.getRobotEquipmentSlots(robotId);
    }

    public boolean equipRobotItem(int index, EquipmentItem item) {
        if (!gameState.equipRobotItem(getRobotId(index), robots[index].grade, item)) {
            return false;
        }
        robotEquipment = copyRobotEquipment(gameState.getRobotEquipment());
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
            enemy.name = enemyState.getName();
            enemy.monsterId = enemyState.getMonsterId();
            enemy.alive = enemyState.isAlive();
            enemy.attackCooldown = ENEMY_MELEE_COOLDOWN;
            enemy.attackTimer = enemyState.getAttackTimer();
            enemy.patrolTarget = new Vector2(enemyState.getPatrolTargetX(), enemyState.getPatrolTargetY());
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
        String name;
        String monsterId;
        boolean alive;
        int spriteIndex;
        float animationTime;
        float attackCooldown, attackTimer;
        Vector2 patrolTarget;
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

        House(int id, String name, float x, float y, float width, float height,
              List<InteriorNpc> interiorNpcs, List<Chest> chests) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.interiorNpcs = interiorNpcs;
            this.chests = chests;
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
            npcs.add(new InteriorNpc("Toma", new Vector2(220f, 250f),
                "If you find old parts in the wild, bring them here and I can rebuild them.", "workshop"));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("workshop_cache", new Vector2(120f, 128f), 30, 0, true));
            return new House(id, name, x, y, width, height, npcs, chests);
        }

        static House createLodge(int id, String name, float x, float y, float width, float height) {
            List<InteriorNpc> npcs = new ArrayList<>();
            npcs.add(new InteriorNpc("Nia", new Vector2(245f, 215f),
                "Travelers leave rumors behind. One says there is a buried vault east of town."));
            npcs.add(new InteriorNpc("Bram", new Vector2(150f, 250f),
                "Rest when you can. Even machines need a rhythm."));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("lodge_stash", new Vector2(300f, 122f), 15, 1, false));
            return new House(id, name, x, y, width, height, npcs, chests);
        }

        static House createHerbalist(int id, String name, float x, float y, float width, float height) {
            List<InteriorNpc> npcs = new ArrayList<>();
            npcs.add(new InteriorNpc("Iris", new Vector2(210f, 238f),
                "Hidden chests react to careful eyes. Walk close and watch the outline flicker.", "apothecary"));
            List<Chest> chests = new ArrayList<>();
            chests.add(new Chest("herbalist_hidden", new Vector2(92f, 238f), 10, 2, true));
            chests.add(new Chest("herbalist_supplies", new Vector2(310f, 136f), 0, 1, false));
            return new House(id, name, x, y, width, height, npcs, chests);
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
        String name;
        Vector2 pos;
        String dialog;
        String shopId;

        InteriorNpc(String name, Vector2 pos, String dialog) {
            this(name, pos, dialog, null);
        }

        InteriorNpc(String name, Vector2 pos, String dialog, String shopId) {
            this.name = name;
            this.pos = pos;
            this.dialog = dialog;
            this.shopId = shopId;
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
