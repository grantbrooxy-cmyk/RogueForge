package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityInstance;
import com.rogueforge.game.combat.AbilityRegistry;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turn-based encounter screen with multi-target encounters, abilities, and results flow.
 */
public class BattleScreen implements Screen {
    private static final String[] ROOT_ACTIONS = {"Attack", "Ability", "Item", "Defend", "Run"};
    private static final String[] ATTACK_ACTIONS = {"Quick Slash", "Power Crush", "Arc Pulse"};

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final Encounter encounter;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final List<String> battleLog = new ArrayList<>();
    private final List<Combatant> combatants = new ArrayList<>();
    private final List<Combatant> turnOrder = new ArrayList<>();
    private final List<Combatant> enemyCombatants = new ArrayList<>();
    private final Texture backgroundTexture;
    private final Texture shadowTexture;
    private final Texture buttonTexture;
    private final AnimationSet playerAnimation;
    private final AnimationSet[] robotAnimations;
    private final AnimationSet[] enemyAnimations;

    private boolean battleResolved = false;
    private boolean showingAttackMenu = false;
    private boolean showingAbilityMenu = false;
    private boolean selectingTarget = false;
    private boolean selectingAllyTarget = false;
    private boolean selectingAbilityTarget = false;
    private boolean resultsVisible = false;
    private boolean pendingTurnAdvance = false;
    private int hoveredOption = -1;
    private int turnIndex = 0;
    private int healingPotions;
    private int selectedActionIndex = 0;
    private float actionLockTimer = 0f;
    private float battleAnimationTime = 0f;
    private BattleResult pendingResult;
    private int previewLevelUps;
    private int previewGoldEarned;
    private int previewExperienceEarned;

    public BattleScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen, Encounter encounter) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.encounter = encounter;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.buttonFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backgroundTexture = loadTexture("Backgrounds/background 4/orig_big.png");
        this.shadowTexture = loadTexture("1 Characters/Other/Shadow.png");
        this.buttonTexture = loadTexture("4 GUI/6 Buttons/ButtonMap3.png");
        this.playerAnimation = loadAnimationSet("1 Characters/1");
        this.robotAnimations = new AnimationSet[] {
            loadAnimationSet("1 Characters/2"),
            loadAnimationSet("1 Characters/3"),
            loadSingleFrameSet("sprites/Tank_Robot.png", 32, 32)
        };
        this.enemyAnimations = new AnimationSet[] {
            loadAnimationSet("3 Dungeon Enemies/1"),
            loadAnimationSet("3 Dungeon Enemies/2"),
            loadAnimationSet("3 Dungeon Enemies/3"),
            loadAnimationSet("3 Dungeon Enemies/4")
        };
        this.titleFont.getData().setScale(2.8f);
        this.bodyFont.getData().setScale(1.15f);
        this.buttonFont.getData().setScale(1.2f);
        this.healingPotions = encounter.healingPotions;

        buildCombatants();
        rebuildTurnOrder();
        battleLog.add("Encounter! " + joinEnemyNames(encounter.enemyNames));
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
            frames[i] = new TextureRegion(texture, i * frameWidth, 0,
                Math.min(frameWidth, texture.getWidth() - (i * frameWidth)),
                Math.min(frameHeight, texture.getHeight()));
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
            loadStripFrames(basePath + "/U_Walk.png", 32, 32),
            loadStripFrames(basePath + "/D_Attack.png", 32, 32),
            loadStripFrames(basePath + "/S_Attack.png", 32, 32),
            loadStripFrames(basePath + "/U_Attack.png", 32, 32),
            loadStripFrames(basePath + "/D_Hurt.png", 32, 32),
            loadStripFrames(basePath + "/S_Hurt.png", 32, 32),
            loadStripFrames(basePath + "/U_Hurt.png", 32, 32)
        );
    }

    private AnimationSet loadSingleFrameSet(String path, int frameWidth, int frameHeight) {
        TextureRegion frame = loadStripFrame(path, frameWidth, frameHeight);
        return new AnimationSet(
            new TextureRegion[] {frame}, new TextureRegion[] {frame},
            new TextureRegion[] {frame}, new TextureRegion[] {frame},
            new TextureRegion[] {frame}, new TextureRegion[] {frame},
            new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame},
            new TextureRegion[] {frame}, new TextureRegion[] {frame}, new TextureRegion[] {frame}
        );
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera.setToOrtho(false, w, h);
        battleAnimationTime += delta;
        updateAnimationStates(delta);
        updateCooldowns(delta);
        updateActionLock(delta);
        processAutoTurns();

        if (battleResolved && !resultsVisible) {
            return;
        }

        handleInput(w, h);

        Gdx.gl.glClearColor(0.07f, 0.06f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawBackdrop(w, h);
        drawEnemyPanel(w, h);
        drawPartyPanel(h);
        drawTurnOrderPanel(w, h);
        drawLogPanel(w, h);
        drawActions(w, h);
        if (resultsVisible) {
            drawResultsOverlay(w, h);
        }
    }

    private void updateAnimationStates(float delta) {
        for (Combatant combatant : combatants) {
            if (combatant.animTimer > 0f) {
                combatant.animTimer = Math.max(0f, combatant.animTimer - delta);
                if (combatant.animTimer == 0f) {
                    combatant.animState = "idle";
                }
            }
        }
    }

    private void updateCooldowns(float delta) {
        for (Combatant combatant : combatants) {
            for (AbilityInstance ability : combatant.abilities) {
                ability.update(delta);
            }
        }
    }

    private void updateActionLock(float delta) {
        if (actionLockTimer <= 0f) {
            return;
        }
        actionLockTimer = Math.max(0f, actionLockTimer - delta);
        if (actionLockTimer == 0f && pendingTurnAdvance) {
            pendingTurnAdvance = false;
            endActorTurn();
        }
    }

    private void buildCombatants() {
        combatants.clear();
        enemyCombatants.clear();

        Combatant player = new Combatant();
        player.name = encounter.playerName;
        player.ally = true;
        player.facing = new Vector2(1f, 0f);
        player.health = encounter.playerHealth;
        player.maxHealth = encounter.playerMaxHealth;
        player.agility = encounter.playerAgility;
        player.strength = encounter.playerStrength;
        player.intelligence = encounter.playerIntelligence;
        player.stamina = encounter.playerStamina;
        player.partyIndex = -1;
        player.abilities = AbilityRegistry.createInstances(gameScreen.getPartyAbilityIds(-1));
        combatants.add(player);

        if (encounter.robotHealth != null) {
            for (int i = 0; i < encounter.robotHealth.length; i++) {
                Combatant robot = new Combatant();
                robot.name = encounter.robotNames != null && i < encounter.robotNames.length
                    ? encounter.robotNames[i]
                    : "Bot " + (i + 1);
                robot.ally = true;
                robot.facing = new Vector2(1f, 0f);
                robot.health = encounter.robotHealth[i];
                robot.maxHealth = encounter.robotMaxHealth[i];
                robot.agility = encounter.robotAgility[i];
                robot.strength = encounter.robotStrength[i];
                robot.intelligence = encounter.robotIntelligence[i];
                robot.stamina = encounter.robotStamina[i];
                robot.partyIndex = i;
                robot.abilities = AbilityRegistry.createInstances(gameScreen.getPartyAbilityIds(i));
                combatants.add(robot);
            }
        }

        for (int i = 0; i < encounter.enemyHealth.length; i++) {
            Combatant enemy = new Combatant();
            enemy.name = encounter.enemyNames[i];
            enemy.ally = false;
            enemy.facing = new Vector2(-1f, 0f);
            enemy.health = encounter.enemyHealth[i];
            enemy.maxHealth = encounter.enemyMaxHealth[i];
            enemy.agility = encounter.enemyAgility[i];
            enemy.strength = encounter.enemyStrength[i];
            enemy.intelligence = encounter.enemyIntelligence[i];
            enemy.stamina = encounter.enemyStamina[i];
            enemy.partyIndex = i;
            combatants.add(enemy);
            enemyCombatants.add(enemy);
        }
    }

    private void rebuildTurnOrder() {
        turnOrder.clear();
        for (Combatant combatant : combatants) {
            if (combatant.health > 0f) {
                turnOrder.add(combatant);
            }
        }

        turnOrder.sort(Comparator
            .comparingDouble((Combatant c) -> getEffectiveAgility(c)).reversed()
            .thenComparing(c -> c.ally ? 0 : 1)
            .thenComparing(c -> c.name));

        if (!turnOrder.isEmpty() && turnIndex >= turnOrder.size()) {
            turnIndex = 0;
        }
    }

    private float getEffectiveAgility(Combatant combatant) {
        return combatant.agility + combatant.agilityBoost;
    }

    private void processAutoTurns() {
        if (battleResolved || actionLockTimer > 0f || resultsVisible) {
            return;
        }

        Combatant actor = getCurrentActor();
        if (actor == null || actor.ally) {
            return;
        }
        enemyAction(actor);
    }

    private void handleInput(float w, float h) {
        if (resultsVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.justTouched()) {
                resultsVisible = false;
                battleResolved = true;
                gameScreen.resolveBattle(pendingResult);
            }
            return;
        }

        Combatant actor = getCurrentActor();
        if (actor == null || !actor.ally || actionLockTimer > 0f) {
            return;
        }

        String[] options = getCurrentOptions(actor);
        float buttonW = 200f;
        float buttonH = 46f;
        float buttonGap = 16f;
        float startX = 60f;
        float buttonY = 70f;

        hoveredOption = -1;
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        for (int i = 0; i < options.length; i++) {
            float bx = startX + i * (buttonW + buttonGap);
            if (mx >= bx && mx <= bx + buttonW && my >= buttonY && my <= buttonY + buttonH) {
                hoveredOption = i;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (selectingTarget) {
                selectingTarget = false;
                selectingAllyTarget = false;
                if (selectingAbilityTarget) {
                    showingAbilityMenu = true;
                } else {
                    showingAttackMenu = true;
                }
                selectingAbilityTarget = false;
                return;
            }
            if (showingAbilityMenu) {
                showingAbilityMenu = false;
                return;
            }
            if (showingAttackMenu) {
                showingAttackMenu = false;
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            chooseOption(0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            chooseOption(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            chooseOption(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            chooseOption(3);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            chooseOption(4);
        }

        if (Gdx.input.justTouched() && hoveredOption >= 0) {
            chooseOption(hoveredOption);
        }
    }

    private void chooseOption(int index) {
        Combatant actor = getCurrentActor();
        if (actor == null || !actor.ally || battleResolved) {
            return;
        }

        if (selectingTarget) {
            chooseTargetAndResolve(actor, index);
            return;
        }

        if (showingAbilityMenu) {
            chooseAbility(actor, index);
            return;
        }

        if (showingAttackMenu) {
            if (index < 0 || index >= ATTACK_ACTIONS.length) {
                return;
            }
            selectedActionIndex = index;
            showingAttackMenu = false;
            if (countLivingEnemies() > 1) {
                selectingTarget = true;
                selectingAllyTarget = false;
                selectingAbilityTarget = false;
            } else {
                performAttack(actor, selectedActionIndex, getSelectedEnemy(0));
            }
            return;
        }

        switch (index) {
            case 0:
                showingAttackMenu = true;
                break;
            case 1:
                showingAbilityMenu = true;
                break;
            case 2:
                useItem(actor);
                break;
            case 3:
                defend(actor);
                break;
            case 4:
                attemptRun(actor);
                break;
            default:
                break;
        }
    }

    private String[] getCurrentOptions(Combatant actor) {
        if (selectingTarget) {
            return selectingAllyTarget ? getLivingAllyNames() : getLivingEnemyNames();
        }
        if (showingAbilityMenu) {
            return getAbilityOptions(actor);
        }
        return showingAttackMenu ? ATTACK_ACTIONS : ROOT_ACTIONS;
    }

    private String[] getAbilityOptions(Combatant actor) {
        List<String> labels = new ArrayList<>();
        for (AbilityInstance ability : actor.abilities) {
            String label = ability.getDefinition().getName();
            if (!ability.isReady()) {
                label += " [" + Math.max(1, Math.round(ability.getCurrentCooldown())) + "]";
            }
            labels.add(label);
        }
        return labels.toArray(new String[0]);
    }

    private String[] getLivingEnemyNames() {
        List<String> names = new ArrayList<>();
        for (Combatant enemy : enemyCombatants) {
            if (enemy.health > 0f) {
                names.add(enemy.name + " HP " + (int) enemy.health);
            }
        }
        return names.toArray(new String[0]);
    }

    private String[] getLivingAllyNames() {
        List<String> names = new ArrayList<>();
        for (Combatant combatant : combatants) {
            if (combatant.ally && combatant.health > 0f) {
                names.add(combatant.name + " HP " + (int) combatant.health);
            }
        }
        return names.toArray(new String[0]);
    }

    private Combatant getSelectedEnemy(int livingIndex) {
        int seen = 0;
        for (Combatant enemy : enemyCombatants) {
            if (enemy.health <= 0f) {
                continue;
            }
            if (seen == livingIndex) {
                return enemy;
            }
            seen++;
        }
        return null;
    }

    private Combatant getSelectedAlly(int livingIndex) {
        int seen = 0;
        for (Combatant combatant : combatants) {
            if (!combatant.ally || combatant.health <= 0f) {
                continue;
            }
            if (seen == livingIndex) {
                return combatant;
            }
            seen++;
        }
        return null;
    }

    private int countLivingEnemies() {
        int count = 0;
        for (Combatant enemy : enemyCombatants) {
            if (enemy.health > 0f) {
                count++;
            }
        }
        return count;
    }

    private void chooseAbility(Combatant actor, int index) {
        if (index < 0 || index >= actor.abilities.size()) {
            return;
        }
        AbilityInstance ability = actor.abilities.get(index);
        if (!ability.isReady()) {
            battleLog.add(ability.getDefinition().getName() + " is still cooling down.");
            return;
        }

        selectedActionIndex = index;
        AbilityDefinition.TargetType targetType = ability.getDefinition().getTargetType();
        if (targetType == AbilityDefinition.TargetType.SINGLE_ENEMY) {
            if (countLivingEnemies() > 1) {
                selectingTarget = true;
                selectingAllyTarget = false;
                selectingAbilityTarget = true;
                showingAbilityMenu = false;
            } else {
                useAbility(actor, ability, getSelectedEnemy(0));
                showingAbilityMenu = false;
            }
            return;
        }
        if (targetType == AbilityDefinition.TargetType.SINGLE_ALLY) {
            selectingTarget = true;
            selectingAllyTarget = true;
            selectingAbilityTarget = true;
            showingAbilityMenu = false;
            return;
        }

        useAbility(actor, ability, null);
        showingAbilityMenu = false;
    }

    private void chooseTargetAndResolve(Combatant actor, int targetIndex) {
        if (selectingAbilityTarget && selectingAllyTarget) {
            AbilityInstance ability = actor.abilities.get(selectedActionIndex);
            Combatant target = getSelectedAlly(targetIndex);
            if (target != null) {
                useAbility(actor, ability, target);
            }
        } else if (selectingAbilityTarget) {
            AbilityInstance ability = actor.abilities.get(selectedActionIndex);
            Combatant target = getSelectedEnemy(targetIndex);
            if (target != null) {
                useAbility(actor, ability, target);
            }
        } else {
            Combatant target = getSelectedEnemy(targetIndex);
            if (target != null) {
                performAttack(actor, selectedActionIndex, target);
            }
        }

        selectingTarget = false;
        selectingAllyTarget = false;
        selectingAbilityTarget = false;
    }

    private void performAttack(Combatant actor, int attackIndex, Combatant target) {
        if (target == null || target.health <= 0f) {
            return;
        }
        float damage;
        String attackName;

        switch (attackIndex) {
            case 0:
                attackName = "Quick Slash";
                damage = actor.strength * 1.15f + getEffectiveAgility(actor) * 0.35f - target.stamina * 0.45f;
                break;
            case 1:
                attackName = "Power Crush";
                damage = actor.strength * 1.75f + actor.stamina * 0.25f - target.stamina * 0.25f;
                break;
            case 2:
                attackName = "Arc Pulse";
                damage = actor.intelligence * 1.6f + getEffectiveAgility(actor) * 0.2f - target.stamina * 0.2f;
                break;
            default:
                return;
        }

        int appliedDamage = Math.max(3, Math.round(damage));
        applyDamage(target, appliedDamage);
        actor.animState = "attack";
        actor.animTimer = 0.42f;
        target.animState = "hurt";
        target.animTimer = 0.32f;
        battleLog.add(actor.name + " uses " + attackName + " on " + target.name + " for " + appliedDamage + " damage.");
        lockTurnAdvance(0.42f);
    }

    private void useAbility(Combatant actor, AbilityInstance ability, Combatant target) {
        AbilityDefinition definition = ability.getDefinition();
        switch (definition.getId()) {
            case "power_strike":
                if (target == null) {
                    target = getSelectedEnemy(0);
                }
                if (target != null) {
                    int damage = Math.max(6, Math.round(actor.strength * 1.4f + definition.getPower() - target.stamina * 0.25f));
                    applyDamage(target, damage);
                    battleLog.add(actor.name + " unleashes Power Strike on " + target.name + " for " + damage + " damage.");
                    animateAction(actor, target);
                }
                break;
            case "rapid_fire":
                for (Combatant enemy : enemyCombatants) {
                    if (enemy.health <= 0f) {
                        continue;
                    }
                    int damage = Math.max(4, Math.round(actor.strength * 0.65f + definition.getPower() - enemy.stamina * 0.15f));
                    applyDamage(enemy, damage);
                    enemy.animState = "hurt";
                    enemy.animTimer = 0.32f;
                }
                actor.animState = "attack";
                actor.animTimer = 0.42f;
                battleLog.add(actor.name + " sprays Rapid Fire across the enemy line.");
                break;
            case "heal_pulse":
                for (Combatant ally : combatants) {
                    if (!ally.ally || ally.health <= 0f) {
                        continue;
                    }
                    int healAmount = Math.round(definition.getPower() + actor.intelligence * 0.35f);
                    ally.health = Math.min(ally.maxHealth, ally.health + healAmount);
                }
                actor.animState = "attack";
                actor.animTimer = 0.3f;
                battleLog.add(actor.name + " releases a Heal Pulse across the party.");
                break;
            case "repair_aura":
                for (Combatant ally : combatants) {
                    if (!ally.ally || ally.health <= 0f) {
                        continue;
                    }
                    int healAmount = Math.round(ability.getDefinition().getPower() * 3f + actor.intelligence * 0.25f);
                    ally.health = Math.min(ally.maxHealth, ally.health + healAmount);
                }
                actor.animState = "attack";
                actor.animTimer = 0.3f;
                battleLog.add(actor.name + " coats the team in a Repair Aura.");
                break;
            case "dash":
                actor.agilityBoost = Math.max(actor.agilityBoost, 10f);
                actor.defending = true;
                actor.animState = "attack";
                actor.animTimer = 0.3f;
                battleLog.add(actor.name + " dashes into position and sharpens their timing.");
                break;
            case "scan":
                if (target == null) {
                    target = getSelectedEnemy(0);
                }
                if (target != null) {
                    target.stamina = Math.max(1f, target.stamina - 4f);
                    battleLog.add(actor.name + " scans " + target.name + ": STR " + (int) target.strength
                        + "  INT " + (int) target.intelligence + ". Armor weakened.");
                    animateAction(actor, target);
                }
                break;
            case "shield_wall":
                actor.defending = true;
                actor.staminaBoost = Math.max(actor.staminaBoost, 8f);
                actor.animState = "attack";
                actor.animTimer = 0.3f;
                battleLog.add(actor.name + " braces behind a Shield Wall.");
                break;
            case "taunt":
                actor.tauntTurns = 2;
                actor.animState = "attack";
                actor.animTimer = 0.3f;
                battleLog.add(actor.name + " taunts the enemy group and draws their focus.");
                break;
            default:
                if (definition.getType() == AbilityDefinition.AbilityType.HEAL && target != null) {
                    int healAmount = Math.round(definition.getPower() + actor.intelligence * 0.25f);
                    target.health = Math.min(target.maxHealth, target.health + healAmount);
                    battleLog.add(actor.name + " restores " + healAmount + " HP to " + target.name + ".");
                    animateAction(actor, target);
                }
                break;
        }

        ability.use();
        lockTurnAdvance(0.4f);
    }

    private void animateAction(Combatant actor, Combatant target) {
        actor.animState = "attack";
        actor.animTimer = 0.42f;
        if (target != null) {
            target.animState = "hurt";
            target.animTimer = 0.32f;
        }
    }

    private void defend(Combatant actor) {
        actor.defending = true;
        battleLog.add(actor.name + " takes a defensive stance.");
        lockTurnAdvance(0.22f);
    }

    private void useItem(Combatant actor) {
        if (healingPotions <= 0) {
            battleLog.add("No repair kits left.");
            return;
        }

        healingPotions--;
        int healed = Math.round(26f + actor.stamina * 0.8f);
        actor.health = Math.min(actor.maxHealth, actor.health + healed);
        battleLog.add(actor.name + " uses a repair kit and recovers " + healed + " HP.");
        lockTurnAdvance(0.28f);
    }

    private void attemptRun(Combatant actor) {
        Combatant fastestEnemy = getFastestLivingEnemy();
        float enemyAgility = fastestEnemy != null ? getEffectiveAgility(fastestEnemy) : 0f;
        float escapeChance = 0.45f + Math.max(0f, getEffectiveAgility(actor) - enemyAgility) / 100f;
        if (Math.random() < Math.min(0.9f, escapeChance)) {
            battleLog.add(actor.name + " leads the party to safety.");
            finishBattle(false, true);
        } else {
            battleLog.add(actor.name + " fails to escape.");
            lockTurnAdvance(0.24f);
        }
    }

    private Combatant getFastestLivingEnemy() {
        Combatant fastest = null;
        for (Combatant enemy : enemyCombatants) {
            if (enemy.health <= 0f) {
                continue;
            }
            if (fastest == null || getEffectiveAgility(enemy) > getEffectiveAgility(fastest)) {
                fastest = enemy;
            }
        }
        return fastest;
    }

    private void enemyAction(Combatant actor) {
        Combatant target = chooseEnemyTarget();
        if (target == null) {
            finishBattle(false, false);
            return;
        }

        boolean usesTech = Math.random() < 0.35f;
        float rawDamage = usesTech
            ? actor.intelligence * 1.45f + getEffectiveAgility(actor) * 0.15f - getEffectiveStamina(target) * 0.25f
            : actor.strength * 1.35f + getEffectiveAgility(actor) * 0.2f - getEffectiveStamina(target) * 0.35f;
        int appliedDamage = Math.max(2, Math.round(rawDamage));
        applyDamage(target, appliedDamage);
        actor.animState = "attack";
        actor.animTimer = 0.42f;
        target.animState = "hurt";
        target.animTimer = 0.32f;
        battleLog.add(actor.name + (usesTech ? " uses Mind Lance on " : " strikes ")
            + target.name + " for " + appliedDamage + " damage.");
        lockTurnAdvance(0.42f);
    }

    private float getEffectiveStamina(Combatant target) {
        return target.stamina + target.staminaBoost;
    }

    private void applyDamage(Combatant target, int appliedDamage) {
        int reducedDamage = target.defending ? Math.max(1, Math.round(appliedDamage * 0.5f)) : appliedDamage;
        target.health = Math.max(0f, target.health - reducedDamage);
        if (target.defending) {
            target.defending = false;
        }
    }

    private void lockTurnAdvance(float durationSeconds) {
        actionLockTimer = Math.max(actionLockTimer, durationSeconds);
        pendingTurnAdvance = true;
    }

    private Combatant chooseEnemyTarget() {
        Combatant tauntingTarget = null;
        for (Combatant combatant : combatants) {
            if (combatant.ally && combatant.health > 0f && combatant.tauntTurns > 0) {
                tauntingTarget = combatant;
                break;
            }
        }
        if (tauntingTarget != null) {
            return tauntingTarget;
        }

        List<Combatant> allies = new ArrayList<>();
        for (Combatant combatant : combatants) {
            if (combatant.ally && combatant.health > 0f) {
                allies.add(combatant);
            }
        }
        if (allies.isEmpty()) {
            return null;
        }

        allies.sort(Comparator
            .comparingDouble((Combatant c) -> c.health)
            .thenComparing(c -> c.partyIndex));
        return allies.get(0);
    }

    private void endActorTurn() {
        if (battleResolved && !resultsVisible) {
            return;
        }

        if (!hasLivingEnemies()) {
            finishBattle(true, false);
            return;
        }
        if (!hasLivingAllies()) {
            finishBattle(false, false);
            return;
        }

        showingAttackMenu = false;
        showingAbilityMenu = false;
        selectingTarget = false;
        selectingAllyTarget = false;
        selectingAbilityTarget = false;

        Combatant actor = getCurrentActor();
        if (actor != null && actor.tauntTurns > 0) {
            actor.tauntTurns--;
        }

        rebuildTurnOrder();
        if (turnOrder.isEmpty()) {
            return;
        }

        turnIndex++;
        if (turnIndex >= turnOrder.size()) {
            turnIndex = 0;
            rebuildTurnOrder();
        }
    }

    private Combatant getCurrentActor() {
        rebuildTurnOrder();
        if (turnOrder.isEmpty()) {
            return null;
        }
        if (turnIndex >= turnOrder.size()) {
            turnIndex = 0;
        }
        return turnOrder.get(turnIndex);
    }

    private boolean hasLivingAllies() {
        for (Combatant combatant : combatants) {
            if (combatant.ally && combatant.health > 0f) {
                return true;
            }
        }
        return false;
    }

    private boolean hasLivingEnemies() {
        for (Combatant enemy : enemyCombatants) {
            if (enemy.health > 0f) {
                return true;
            }
        }
        return false;
    }

    private void drawBackdrop(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0f, 0f, 0f, 0.35f);
        batch.draw(buttonTexture, 0f, 0f, w, h);
        batch.setColor(Color.WHITE);
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, "BATTLE", 58f, h - 46f);
        bodyFont.setColor(Color.WHITE);
        Combatant actor = getCurrentActor();
        String label = actionLockTimer > 0f
            ? "Action resolving..."
            : actor != null ? actor.name + "'s turn" : "Resolving...";
        bodyFont.draw(batch, label, 60f, h - 82f);
        batch.end();
    }

    private void drawEnemyPanel(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.11f, 0.15f, 0.96f);
        shapeRenderer.rect(w - 400f, h - 340f, 340f, 250f);
        shapeRenderer.end();

        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Enemy Team", w - 370f, h - 110f);

        int row = 0;
        for (Combatant enemy : enemyCombatants) {
            float panelY = h - 150f - (row * 66f);
            if (enemy.health <= 0f) {
                bodyFont.setColor(0.7f, 0.7f, 0.7f, 1f);
            } else {
                bodyFont.setColor(Color.WHITE);
            }
            drawShadow(w - 315f, panelY - 10f, 60f, 18f, 0.45f);
            drawAnimatedSprite(getCombatantFrame(enemy, enemyAnimations[row % enemyAnimations.length]), enemy.facing,
                w - 350f, panelY - 36f, 52f, 52f);
            bodyFont.draw(batch, enemy.name, w - 280f, panelY + 18f);
            bodyFont.draw(batch, enemy.health > 0f
                ? "HP " + (int) enemy.health + "/" + (int) enemy.maxHealth
                : "DOWN",
                w - 280f, panelY - 4f);
            bodyFont.draw(batch, statsLine(enemy), w - 280f, panelY - 26f);
            row++;
        }
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        row = 0;
        for (Combatant enemy : enemyCombatants) {
            float barY = h - 163f - (row * 66f);
            shapeRenderer.setColor(0.16f, 0.16f, 0.2f, 1f);
            shapeRenderer.rect(w - 280f, barY, 180f, 10f);
            shapeRenderer.setColor(0.82f, 0.24f, 0.24f, 1f);
            float hpPercent = enemy.maxHealth > 0f ? Math.max(0f, enemy.health / enemy.maxHealth) : 0f;
            shapeRenderer.rect(w - 280f, barY, 180f * hpPercent, 10f);
            row++;
        }
        shapeRenderer.end();
    }

    private void drawPartyPanel(float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.11f, 0.15f, 0.96f);
        shapeRenderer.rect(50f, 180f, 420f, h - 320f);
        shapeRenderer.end();

        batch.begin();
        bodyFont.setColor(Color.WHITE);
        int row = 0;
        for (Combatant combatant : combatants) {
            if (!combatant.ally) {
                continue;
            }
            float y = h - 146f - (row * 54f);
            TextureRegion sprite = combatant.partyIndex < 0
                ? getCombatantFrame(combatant, playerAnimation)
                : getCombatantFrame(combatant, robotAnimations[combatant.partyIndex % robotAnimations.length]);
            drawShadow(110f, y - 10f, 46f, 14f, 0.4f);
            drawAnimatedSprite(sprite, combatant.facing, 58f, y - 42f, 44f, 44f);
            String hpText = combatant.health > 0f
                ? (int) combatant.health + "/" + (int) combatant.maxHealth
                : "DOWN";
            bodyFont.draw(batch, combatant.name + "  HP " + hpText, 112f, y);
            bodyFont.draw(batch, statsLine(combatant), 112f, y - 22f);
            row++;
        }
        bodyFont.draw(batch, "Repair Kits: " + healingPotions, 72f, 210f);
        batch.end();
    }

    private void drawTurnOrderPanel(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.11f, 0.15f, 0.96f);
        shapeRenderer.rect(w - 400f, 180f, 340f, 190f);
        shapeRenderer.end();

        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Turn Order", w - 370f, 345f);
        for (int i = 0; i < turnOrder.size(); i++) {
            Combatant combatant = turnOrder.get(i);
            String marker = i == turnIndex ? "> " : "  ";
            bodyFont.draw(batch, marker + combatant.name + " (" + (int) getEffectiveAgility(combatant) + " AGI)", w - 370f, 315f - (i * 24f));
        }
        batch.end();
    }

    private void drawLogPanel(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.10f, 0.11f, 0.15f, 0.96f);
        shapeRenderer.rect(500f, 70f, w - 560f, 120f);
        shapeRenderer.end();

        batch.begin();
        bodyFont.setColor(Color.WHITE);
        int lineCount = Math.min(4, battleLog.size());
        for (int i = 0; i < lineCount; i++) {
            String line = battleLog.get(battleLog.size() - lineCount + i);
            bodyFont.draw(batch, line, 520f, 165f - (i * 24f));
        }
        batch.end();
    }

    private void drawActions(float w, float h) {
        if (resultsVisible) {
            return;
        }

        Combatant actor = getCurrentActor();
        if (actor == null || !actor.ally) {
            return;
        }

        String[] options = getCurrentOptions(actor);
        float buttonW = 200f;
        float buttonH = 46f;
        float buttonGap = 16f;
        float startX = 60f;
        float y = 70f;

        batch.begin();
        for (int i = 0; i < options.length; i++) {
            float bx = startX + i * (buttonW + buttonGap);
            batch.setColor(i == hoveredOption ? Color.WHITE : new Color(0.82f, 0.82f, 0.82f, 1f));
            batch.draw(buttonTexture, bx, y, buttonW, buttonH);
        }
        batch.setColor(Color.WHITE);
        bodyFont.setColor(Color.WHITE);
        String heading = selectingTarget
            ? (selectingAllyTarget ? "Choose ally target" : "Choose enemy target")
            : showingAbilityMenu ? "Choose ability"
            : showingAttackMenu ? "Choose attack"
            : "Choose action";
        bodyFont.draw(batch, heading, startX, y + 72f);
        for (int i = 0; i < options.length; i++) {
            float bx = startX + i * (buttonW + buttonGap);
            String label = (i + 1) + ". " + options[i];
            layout.setText(buttonFont, label);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, label, bx + (buttonW - layout.width) / 2f, y + (buttonH + layout.height) / 2f);
        }
        if (showingAttackMenu || showingAbilityMenu || selectingTarget) {
            bodyFont.draw(batch, "Esc: Back", startX + 670f, y + 72f);
        }
        batch.end();
    }

    private void drawResultsOverlay(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.76f);
        shapeRenderer.rect(0f, 0f, w, h);
        shapeRenderer.setColor(0.08f, 0.09f, 0.14f, 0.98f);
        shapeRenderer.rect(w * 0.24f, h * 0.22f, w * 0.52f, h * 0.46f);
        shapeRenderer.end();

        batch.begin();
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, "VICTORY", w * 0.34f, h * 0.62f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Gold earned: " + previewGoldEarned, w * 0.31f, h * 0.53f);
        bodyFont.draw(batch, "Experience earned: " + previewExperienceEarned, w * 0.31f, h * 0.48f);
        bodyFont.draw(batch, "Level-ups: " + previewLevelUps, w * 0.31f, h * 0.43f);
        bodyFont.draw(batch, "Repair kits remaining: " + healingPotions, w * 0.31f, h * 0.38f);
        bodyFont.draw(batch, "Press Enter to return to the field.", w * 0.31f, h * 0.30f);
        batch.end();
    }

    private String statsLine(Combatant combatant) {
        return "AGI " + (int) getEffectiveAgility(combatant)
            + "  STR " + (int) combatant.strength
            + "  INT " + (int) combatant.intelligence
            + "  STA " + (int) getEffectiveStamina(combatant);
    }

    private TextureRegion getCombatantFrame(Combatant combatant, AnimationSet animationSet) {
        TextureRegion[] frames = animationSet.getFrames(combatant.facing, false);
        if ("attack".equals(combatant.animState)) {
            frames = animationSet.getAttackFrames(combatant.facing);
        } else if ("hurt".equals(combatant.animState)) {
            frames = animationSet.getHurtFrames(combatant.facing);
        }
        int index = (int) (battleAnimationTime / 0.10f) % frames.length;
        return frames[index];
    }

    private void drawShadow(float x, float y, float width, float height, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(shadowTexture, x - width / 2f, y - height / 2f, width, height);
        batch.setColor(Color.WHITE);
    }

    private void drawAnimatedSprite(TextureRegion frame, Vector2 facing, float x, float y, float width, float height) {
        boolean flip = Math.abs(facing.x) > Math.abs(facing.y) && facing.x > 0f;
        if (flip) {
            batch.draw(frame, x + width, y, -width, height);
        } else {
            batch.draw(frame, x, y, width, height);
        }
    }

    private String joinEnemyNames(String[] names) {
        if (names == null || names.length == 0) {
            return "Unknown threat";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                sb.append(i == names.length - 1 ? " and " : ", ");
            }
            sb.append(names[i]);
        }
        return sb.toString();
    }

    private void finishBattle(boolean enemyDefeated, boolean escaped) {
        if (battleResolved) {
            return;
        }

        BattleResult result = new BattleResult();
        result.playerHealth = combatants.isEmpty() ? 0f : combatants.get(0).health;
        result.robotHealth = new float[encounter.robotHealth != null ? encounter.robotHealth.length : 0];
        for (int i = 0; i < result.robotHealth.length; i++) {
            int combatantIndex = i + 1;
            if (combatantIndex < combatants.size()) {
                result.robotHealth[i] = combatants.get(combatantIndex).health;
            }
        }
        result.enemyHealth = new float[enemyCombatants.size()];
        result.goldEarned = new int[enemyCombatants.size()];
        result.experienceEarned = new int[enemyCombatants.size()];
        result.enemyReferences = encounter.enemyReferences != null
            ? encounter.enemyReferences.clone()
            : new Object[enemyCombatants.size()];
        for (int i = 0; i < enemyCombatants.size(); i++) {
            Combatant enemy = enemyCombatants.get(i);
            result.enemyHealth[i] = enemy.health;
            result.goldEarned[i] = enemy.health <= 0f && enemyDefeated ? encounter.enemyRewardGold[i] : 0;
            result.experienceEarned[i] = enemy.health <= 0f && enemyDefeated ? encounter.enemyExperienceReward[i] : 0;
        }
        result.healingPotions = healingPotions;
        result.enemyDefeated = enemyDefeated;
        result.enemyEscaped = escaped;

        if (enemyDefeated) {
            pendingResult = result;
            previewGoldEarned = sum(result.goldEarned);
            previewExperienceEarned = sum(result.experienceEarned);
            previewLevelUps = gameScreen.previewLevelUps(previewExperienceEarned);
            resultsVisible = true;
            return;
        }

        battleResolved = true;
        gameScreen.resolveBattle(result);
    }

    private int sum(int[] values) {
        int total = 0;
        if (values == null) {
            return 0;
        }
        for (int value : values) {
            total += value;
        }
        return total;
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        titleFont.dispose();
        bodyFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        shadowTexture.dispose();
        buttonTexture.dispose();
        disposeAnimationSet(playerAnimation);
        for (AnimationSet animationSet : robotAnimations) {
            disposeAnimationSet(animationSet);
        }
        for (AnimationSet animationSet : enemyAnimations) {
            disposeAnimationSet(animationSet);
        }
    }

    private void disposeAnimationSet(AnimationSet animationSet) {
        for (Texture texture : animationSet.getOwnedTextures()) {
            texture.dispose();
        }
    }

    public static class Encounter {
        public String[] enemyNames;
        public float[] enemyHealth;
        public float[] enemyMaxHealth;
        public float[] enemyAgility;
        public float[] enemyStrength;
        public float[] enemyIntelligence;
        public float[] enemyStamina;
        public int[] enemyRewardGold;
        public int[] enemyExperienceReward;
        public Object[] enemyReferences;
        public String playerName;
        public int playerLevel;
        public float playerHealth;
        public float playerMaxHealth;
        public float playerAgility;
        public float playerStrength;
        public float playerIntelligence;
        public float playerStamina;
        public String[] robotNames;
        public float[] robotHealth;
        public float[] robotMaxHealth;
        public float[] robotAgility;
        public float[] robotStrength;
        public float[] robotIntelligence;
        public float[] robotStamina;
        public int healingPotions;
    }

    public static class BattleResult {
        public float playerHealth;
        public float[] robotHealth;
        public float[] enemyHealth;
        public int healingPotions;
        public boolean enemyDefeated;
        public boolean enemyEscaped;
        public int[] goldEarned;
        public int[] experienceEarned;
        public Object[] enemyReferences;
    }

    private static class Combatant {
        String name;
        boolean ally;
        int partyIndex;
        Vector2 facing;
        String animState = "idle";
        float animTimer;
        float health;
        float maxHealth;
        float agility;
        float agilityBoost;
        float strength;
        float intelligence;
        float stamina;
        float staminaBoost;
        boolean defending;
        int tauntTurns;
        List<AbilityInstance> abilities = new ArrayList<>();
    }

    private static class AnimationSet {
        final TextureRegion[] downIdle;
        final TextureRegion[] downWalk;
        final TextureRegion[] sideIdle;
        final TextureRegion[] sideWalk;
        final TextureRegion[] upIdle;
        final TextureRegion[] upWalk;
        final TextureRegion[] downAttack;
        final TextureRegion[] sideAttack;
        final TextureRegion[] upAttack;
        final TextureRegion[] downHurt;
        final TextureRegion[] sideHurt;
        final TextureRegion[] upHurt;

        AnimationSet(TextureRegion[] downIdle, TextureRegion[] downWalk, TextureRegion[] sideIdle,
                     TextureRegion[] sideWalk, TextureRegion[] upIdle, TextureRegion[] upWalk,
                     TextureRegion[] downAttack, TextureRegion[] sideAttack, TextureRegion[] upAttack,
                     TextureRegion[] downHurt, TextureRegion[] sideHurt, TextureRegion[] upHurt) {
            this.downIdle = downIdle;
            this.downWalk = downWalk;
            this.sideIdle = sideIdle;
            this.sideWalk = sideWalk;
            this.upIdle = upIdle;
            this.upWalk = upWalk;
            this.downAttack = downAttack;
            this.sideAttack = sideAttack;
            this.upAttack = upAttack;
            this.downHurt = downHurt;
            this.sideHurt = sideHurt;
            this.upHurt = upHurt;
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

        TextureRegion[] getAttackFrames(Vector2 facing) {
            boolean vertical = Math.abs(facing.y) >= Math.abs(facing.x);
            if (vertical && facing.y > 0f) {
                return upAttack;
            }
            if (vertical) {
                return downAttack;
            }
            return sideAttack;
        }

        TextureRegion[] getHurtFrames(Vector2 facing) {
            boolean vertical = Math.abs(facing.y) >= Math.abs(facing.x);
            if (vertical && facing.y > 0f) {
                return upHurt;
            }
            if (vertical) {
                return downHurt;
            }
            return sideHurt;
        }

        Texture[] getOwnedTextures() {
            List<Texture> textures = new ArrayList<>();
            appendTexture(textures, downIdle);
            appendTexture(textures, downWalk);
            appendTexture(textures, sideIdle);
            appendTexture(textures, sideWalk);
            appendTexture(textures, upIdle);
            appendTexture(textures, upWalk);
            appendTexture(textures, downAttack);
            appendTexture(textures, sideAttack);
            appendTexture(textures, upAttack);
            appendTexture(textures, downHurt);
            appendTexture(textures, sideHurt);
            appendTexture(textures, upHurt);
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
