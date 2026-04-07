package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityInstance;
import com.rogueforge.game.combat.BattleCombatant;
import com.rogueforge.game.combat.BattleResultSummary;
import com.rogueforge.game.combat.BattleState;
import com.rogueforge.game.combat.BestiaryManager;
import com.rogueforge.game.combat.CombatSystem;
import com.rogueforge.game.combat.CombatResolver;
import com.rogueforge.game.combat.DamageResult;
import com.rogueforge.game.combat.Element;
import com.rogueforge.game.combat.ElementalSystem;
import com.rogueforge.game.combat.MonsterCombatant;
import com.rogueforge.game.combat.PlayerCombatant;
import com.rogueforge.game.combat.StatusEffectType;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.progression.ProficiencyTracker;
import com.rogueforge.game.ui.BattleCommandPanel;
import com.rogueforge.game.ui.BattleInspectorPanel;
import com.rogueforge.game.ui.BattleTimelinePanel;
import com.rogueforge.game.ui.DebugOverlay;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turn-based battle screen that delegates rules to the combat package.
 */
public class BattleScreen implements Screen {
    static final String ORIGIN_CORE_ID = "origin_core_s";
    static final String VOLT_SPECTER_ID = "volt_specter_b";
    static final String NULL_WARDEN_ID = "null_warden_a";
    static final String THE_UNMAKER_ID = "the_unmaker_s";

    private static final String[] ROOT_ACTIONS = {"Attack", "Ability", "Item", "Defend", "Analyze", "Flee"};
    /** Shown in place of ROOT_ACTIONS when the active actor has the BERSERK status. */
    private static final String[] BERSERK_ROOT_ACTIONS = {"Attack"};
    private static final AttackMove[] ATTACK_MOVES = {
        new AttackMove("Quick Slash", 80, 1.0f, Element.NONE),
        new AttackMove("Power Crush", 120, 1.45f, Element.EARTH),
        new AttackMove("Arc Pulse", 95, 1.15f, Element.LIGHTNING)
    };

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final Encounter encounter;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont smallFont;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final CombatResolver combatResolver;
    private final CombatSystem combatSystem;
    private final BestiaryManager bestiaryManager = new BestiaryManager();
    private final BattleState battleState;
    private final List<String> battleLog = new ArrayList<>();
    private final Map<String, Integer> abilityXpGains = new LinkedHashMap<>();
    private final Map<String, Integer> weaponXpGains = new LinkedHashMap<>();
    private final List<String> masteryUnlocks = new ArrayList<>();
    private final Map<String, Integer> bossPhases = new HashMap<>();
    private final Stage hudStage;
    private final BattleTimelinePanel timelinePanel;
    private final BattleCommandPanel commandPanel;
    private final BattleInspectorPanel inspectorPanel;
    private final DebugOverlay debugOverlay;
    private final InputAdapter battleInputProcessor = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            return handleBattleInput(keycode);
        }
    };
    private InputContextRouter inputContextRouter;

    private Mode mode = Mode.ROOT;
    private int selectedIndex;
    private int selectedAttackIndex = -1;
    private int selectedAbilityIndex = -1;
    private BattleCombatant activeActor;
    private boolean turnInitialized;
    private boolean closingToResults;
    private float actionDelay;
    private int healingPotions;
    private final List<ImpactBurst> impactBursts = new ArrayList<>();
    private float battleShakeIntensity;
    private float battleShakeTimer;
    private float awakeningBannerTimer;
    private String awakeningBannerText;

    public BattleScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen, Encounter encounter) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.encounter = encounter;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.smallFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.titleFont.getData().setScale(2.2f);
        this.bodyFont.getData().setScale(1.1f);
        this.smallFont.getData().setScale(0.95f);
        this.hudStage = new Stage(new ScreenViewport());
        this.timelinePanel = new BattleTimelinePanel(bodyFont, smallFont);
        this.commandPanel = new BattleCommandPanel(bodyFont, bodyFont, smallFont);
        this.inspectorPanel = new BattleInspectorPanel(bodyFont, bodyFont, smallFont);
        this.hudStage.addActor(timelinePanel);
        this.hudStage.addActor(commandPanel);
        this.hudStage.addActor(inspectorPanel);
        this.debugOverlay = new DebugOverlay(this::buildDebugOverlaySections, true);
        this.combatResolver = new CombatResolver(game.getEventBus());
        this.healingPotions = encounter.healingPotions;
        this.bestiaryManager.importData(gameScreen.getBestiaryScanLevels());
        List<BattleCombatant> combatants = buildCombatants();
        this.combatSystem = new CombatSystem(combatants, combatResolver);
        this.battleState = combatSystem.getBattleState();
        battleLog.add("Encounter! " + join(encounter.enemyNames));
        applyBattleStartUniqueBoosts(combatants);
        initializeBossPhases(combatants);
    }

    private List<BattleCombatant> buildCombatants() {
        List<BattleCombatant> combatants = new ArrayList<>();
        combatants.add(new PlayerCombatant(
            "player",
            encounter.playerName,
            -1,
            "PLAYER",
            "PLAYER",
            "Player",
            encounter.playerHealth,
            encounter.playerMaxHealth,
            encounter.playerAgility,
            encounter.playerStrength,
            encounter.playerIntelligence,
            encounter.playerStamina,
            gameScreen.getPartyAbilityInstances(-1),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            0,
            0,
            null,
            gameScreen.getUniqueBoostsForPartyIndex(-1)
        ));
        if (encounter.robotHealth != null) {
            for (int i = 0; i < encounter.robotHealth.length; i++) {
                int partySlot = encounter.robotPartySlots != null && i < encounter.robotPartySlots.length
                    ? encounter.robotPartySlots[i]
                    : i;
                combatants.add(new PlayerCombatant(
                    "ally_" + i,
                    encounter.robotNames[i],
                    partySlot,
                    "ALLY",
                    "ALLY",
                    gameScreen.getRobotClass(partySlot),
                    encounter.robotHealth[i],
                    encounter.robotMaxHealth[i],
                    encounter.robotAgility[i],
                    encounter.robotStrength[i],
                    encounter.robotIntelligence[i],
                    encounter.robotStamina[i],
                    gameScreen.getPartyAbilityInstances(partySlot),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0,
                    0,
                    null,
                    gameScreen.getUniqueBoostsForPartyIndex(partySlot)
                ));
            }
        }
        for (int i = 0; i < encounter.enemyNames.length; i++) {
            combatants.add(new MonsterCombatant(
                encounter.enemyIds != null && i < encounter.enemyIds.length ? encounter.enemyIds[i] : "enemy_" + i,
                encounter.enemyNames[i],
                i,
                encounter.enemyRanks != null && i < encounter.enemyRanks.length ? encounter.enemyRanks[i] : "G",
                encounter.enemyAiProfiles != null && i < encounter.enemyAiProfiles.length ? encounter.enemyAiProfiles[i] : "PATROL",
                "Enemy",
                encounter.enemyHealth[i],
                encounter.enemyMaxHealth[i],
                encounter.enemyAgility[i],
                encounter.enemyStrength[i],
                encounter.enemyIntelligence[i],
                encounter.enemyStamina[i],
                new ArrayList<>(),
                toElements(encounter.enemyWeaknesses != null && i < encounter.enemyWeaknesses.length ? encounter.enemyWeaknesses[i] : null),
                toElements(encounter.enemyResistances != null && i < encounter.enemyResistances.length ? encounter.enemyResistances[i] : null),
                toElements(encounter.enemyAbsorbs != null && i < encounter.enemyAbsorbs.length ? encounter.enemyAbsorbs[i] : null),
                encounter.enemyRewardGold[i],
                encounter.enemyExperienceReward[i],
                encounter.enemyReferences[i],
                new ArrayList<>()
            ));
        }
        return combatants;
    }

    private List<Element> toElements(String[] values) {
        List<Element> elements = new ArrayList<>();
        if (values == null) {
            return elements;
        }
        for (String value : values) {
            Element element = Element.fromString(value);
            if (element != Element.NONE) {
                elements.add(element);
            }
        }
        return elements;
    }

    @Override
    public void show() {
        inputContextRouter = new InputContextRouter();
        inputContextRouter.addSharedProcessor(debugOverlay.getInputProcessor());
        inputContextRouter.setProcessors(InputContext.COMBAT_COMMAND, hudStage, battleInputProcessor);
        inputContextRouter.setProcessors(InputContext.COMBAT_TARGETING, hudStage, battleInputProcessor);
        inputContextRouter.setProcessors(InputContext.COMBAT_RESULTS, hudStage, battleInputProcessor);
        inputContextRouter.activate(resolveInputContext());
    }

    @Override
    public void render(float delta) {
        actionDelay = Math.max(0f, actionDelay - delta);
        updateImpactFeedback(delta);
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        applyBattleShake();
        if (!closingToResults) {
            tickBattleFlow();
        }

        // Zone-aware sky — replaces the previous flat dark navy.
        float[] sky = getBattleSkyColor();
        Gdx.gl.glClearColor(sky[0], sky[1], sky[2], 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        drawBattleAtmosphere(w, h);
        drawPanels(w, h);
        drawCombatants(w, h);
        updateScene2dHud(w, h);
        drawImpactBursts();
        drawAwakeningBanner(h);
        hudStage.act(delta);
        hudStage.draw();
        debugOverlay.render();
    }

    /**
     * Returns a zone-themed RGB triple for the battle screen clear colour.
     * Each entry matches the visual identity of its ground style: desaturated
     * enough to stay readable but distinct enough to signal the zone at a glance.
     */
    private float[] getBattleSkyColor() {
        switch (gameScreen.getCurrentGroundStyle()) {
            case "void":     return new float[]{0.03f, 0.01f, 0.07f};
            case "abyss":    return new float[]{0.02f, 0.05f, 0.09f};
            case "volcanic": return new float[]{0.11f, 0.03f, 0.01f};
            case "frozen":   return new float[]{0.06f, 0.08f, 0.13f};
            case "sky":      return new float[]{0.05f, 0.07f, 0.13f};
            case "crystal":  return new float[]{0.03f, 0.07f, 0.12f};
            case "forest":   return new float[]{0.03f, 0.07f, 0.04f};
            case "coastal":  return new float[]{0.03f, 0.07f, 0.07f};
            case "rust":     return new float[]{0.09f, 0.05f, 0.02f};
            case "sanctum":  return new float[]{0.05f, 0.05f, 0.05f};
            case "cave":     return new float[]{0.05f, 0.05f, 0.07f};
            case "meadow":   return new float[]{0.04f, 0.07f, 0.03f};
            default:         return new float[]{0.07f, 0.08f, 0.11f};
        }
    }

    /**
     * Draws a vertical gradient across the full screen using the zone's accent
     * colour.  Dark panels are drawn on top; this colour shows through the
     * margins and the gap between the two combatant columns, giving each zone
     * a distinctive atmosphere without needing additional texture assets.
     */
    private void drawBattleAtmosphere(float w, float h) {
        float r, g, b;
        switch (gameScreen.getCurrentGroundStyle()) {
            case "void":     r=0.08f; g=0.03f; b=0.16f; break;
            case "abyss":    r=0.04f; g=0.12f; b=0.20f; break;
            case "volcanic": r=0.24f; g=0.06f; b=0.02f; break;
            case "frozen":   r=0.10f; g=0.14f; b=0.24f; break;
            case "sky":      r=0.08f; g=0.13f; b=0.22f; break;
            case "crystal":  r=0.06f; g=0.16f; b=0.22f; break;
            case "forest":   r=0.06f; g=0.16f; b=0.08f; break;
            case "coastal":  r=0.06f; g=0.14f; b=0.14f; break;
            case "rust":     r=0.20f; g=0.10f; b=0.04f; break;
            case "sanctum":  r=0.10f; g=0.08f; b=0.08f; break;
            case "cave":     r=0.08f; g=0.08f; b=0.12f; break;
            case "meadow":   r=0.07f; g=0.12f; b=0.05f; break;
            default:         r=0.08f; g=0.09f; b=0.12f; break;
        }
        // Ground glow (bottom) fades to a darker tone at the top — all opaque
        // so no GL blending flag is required.
        Color groundGlow = new Color(r, g, b, 1f);
        Color skyDark = new Color(r * 0.35f, g * 0.35f, b * 0.35f, 1f);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // rect(x, y, w, h, c_bottomLeft, c_bottomRight, c_topRight, c_topLeft)
        shapeRenderer.rect(0f, 0f, w, h, groundGlow, groundGlow, skyDark, skyDark);
        shapeRenderer.end();
    }

    private void tickBattleFlow() {
        if (!battleState.hasLivingEnemies()) {
            finishBattle(true, false);
            return;
        }
        if (!battleState.hasLivingAllies()) {
            finishBattle(false, false);
            return;
        }
        BattleCombatant current = battleState.getTurnTimeline().getCurrentActor(battleState.getCombatants());
        if (current == null) {
            return;
        }
        if (activeActor == null || !activeActor.getId().equals(current.getId())) {
            activeActor = current;
            turnInitialized = false;
            mode = Mode.ROOT;
            selectedIndex = 0;
            selectedAttackIndex = -1;
            selectedAbilityIndex = -1;
        }
        if (!turnInitialized) {
            beginTurn(activeActor);
            turnInitialized = true;
        }
        if (actionDelay > 0f || activeActor == null || !activeActor.isAlive()) {
            return;
        }
        if (!activeActor.isAlly()) {
            executeEnemyTurn(activeActor);
        }
    }

    private void beginTurn(BattleCombatant actor) {
        appendLogs(actor.getStatusEffectManager().beginTurn(actor));
        if (!actor.isAlive()) {
            endTurn(actor, 40);
            return;
        }
        if (actor.getStatusEffectManager().shouldSkipTurn()) {
            // Contextual message — name the actual status that caused the skip.
            String reason;
            if (actor.getStatusEffectManager().has(StatusEffectType.FREEZE)) {
                reason = "is frozen solid";
            } else if (actor.getStatusEffectManager().has(StatusEffectType.PARALYZE)) {
                reason = "is paralyzed";
            } else {
                reason = "is stunned";
            }
            battleLog.add(actor.getName() + " " + reason + " and loses the turn.");
            endTurn(actor, 100);
            // IMPORTANT: return here so the caller does not fall through to enemy-turn
            // dispatch on the same frame. The actionDelay set by endTurn() currently
            // prevents that, but an explicit return makes the intent self-documenting
            // and eliminates the dependency on that side-effect.
            return;
        }
    }

    private boolean handleBattleInput(int keycode) {
        if (actionDelay > 0f || activeActor == null || !activeActor.isAlive() || !activeActor.isAlly()) {
            return false;
        }
        if (keycode == Input.Keys.UP) {
            selectedIndex = Math.max(0, selectedIndex - 1);
            return true;
        } else if (keycode == Input.Keys.DOWN) {
            selectedIndex = Math.min(getCurrentOptions().length - 1, selectedIndex + 1);
            return true;
        } else if (keycode == Input.Keys.ESCAPE) {
            goBack();
            return true;
        } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
            confirmSelection();
            return true;
        } else if (keycode == Input.Keys.NUM_1) {
            chooseDirect(0);
            return true;
        } else if (keycode == Input.Keys.NUM_2) {
            chooseDirect(1);
            return true;
        } else if (keycode == Input.Keys.NUM_3) {
            chooseDirect(2);
            return true;
        } else if (keycode == Input.Keys.NUM_4) {
            chooseDirect(3);
            return true;
        } else if (keycode == Input.Keys.NUM_5) {
            chooseDirect(4);
            return true;
        } else if (keycode == Input.Keys.NUM_6) {
            chooseDirect(5);
            return true;
        }
        return false;
    }

    private void chooseDirect(int index) {
        if (index < getCurrentOptions().length) {
            selectedIndex = index;
            confirmSelection();
        }
    }

    private void confirmSelection() {
        switch (mode) {
            case ROOT:
                handleRootSelection();
                break;
            case ATTACK:
                if (isBackSelection()) {
                    goBack();
                    return;
                }
                selectedAttackIndex = selectedIndex;
                if (livingEnemies().size() > 1) {
                    mode = Mode.TARGET_ENEMY;
                    selectedIndex = 0;
                } else {
                    performAttack(activeActor, livingEnemies().get(0), ATTACK_MOVES[selectedAttackIndex]);
                }
                break;
            case ABILITY:
                if (isBackSelection()) {
                    goBack();
                    return;
                }
                selectedAbilityIndex = selectedIndex;
                AbilityInstance ability = readyAbilityAt(activeActor, selectedAbilityIndex);
                if (ability == null || !ability.isReady()) {
                    battleLog.add("That ability is not ready.");
                    return;
                }
                AbilityDefinition.TargetType targetType = ability.getDefinition().getTargetType();
                if (targetType == AbilityDefinition.TargetType.SINGLE_ENEMY) {
                    mode = Mode.TARGET_ENEMY;
                    selectedIndex = 0;
                } else if (targetType == AbilityDefinition.TargetType.SINGLE_ALLY) {
                    mode = Mode.TARGET_ALLY;
                    selectedIndex = 0;
                } else {
                    useAbility(activeActor, ability, null);
                }
                break;
            case TARGET_ENEMY:
                if (isBackSelection()) {
                    goBack();
                    return;
                }
                BattleCombatant enemy = livingEnemies().get(selectedIndex);
                if (selectedAbilityIndex >= 0 && modeBeforeTargetWasAbility()) {
                    useAbility(activeActor, readyAbilityAt(activeActor, selectedAbilityIndex), enemy);
                } else if (modeBeforeTargetWasAnalyze()) {
                    analyzeEnemy(activeActor, enemy);
                } else {
                    performAttack(activeActor, enemy, ATTACK_MOVES[selectedAttackIndex]);
                }
                break;
            case TARGET_ALLY:
                if (isBackSelection()) {
                    goBack();
                    return;
                }
                useAbility(activeActor, readyAbilityAt(activeActor, selectedAbilityIndex), livingAllies().get(selectedIndex));
                break;
            case ANALYZE_TARGET:
                if (isBackSelection()) {
                    goBack();
                    return;
                }
                analyzeEnemy(activeActor, livingEnemies().get(selectedIndex));
                break;
            default:
                break;
        }
    }

    private boolean modeBeforeTargetWasAbility() {
        return selectedAbilityIndex >= 0 && selectedAttackIndex < 0;
    }

    private boolean modeBeforeTargetWasAnalyze() {
        return selectedAbilityIndex < 0 && selectedAttackIndex < 0;
    }

    private void handleRootSelection() {
        selectedAttackIndex = -1;
        selectedAbilityIndex = -1;
        switch (selectedIndex) {
            case 0:
                mode = Mode.ATTACK;
                selectedIndex = 0;
                break;
            case 1:
                mode = Mode.ABILITY;
                selectedIndex = 0;
                break;
            case 2:
                useItem(activeActor);
                break;
            case 3:
                defend(activeActor);
                break;
            case 4:
                if (livingEnemies().size() > 1) {
                    mode = Mode.ANALYZE_TARGET;
                    selectedIndex = 0;
                } else {
                    analyzeEnemy(activeActor, livingEnemies().get(0));
                }
                break;
            case 5:
                attemptFlee(activeActor);
                break;
            default:
                break;
        }
    }

    private void goBack() {
        switch (mode) {
            case ATTACK:
            case ABILITY:
            case ANALYZE_TARGET:
                mode = Mode.ROOT;
                selectedIndex = 0;
                selectedAttackIndex = -1;
                selectedAbilityIndex = -1;
                break;
            case TARGET_ENEMY:
                if (modeBeforeTargetWasAbility()) {
                    mode = Mode.ABILITY;
                    selectedIndex = clampSelection(selectedAbilityIndex, getCurrentOptions().length);
                    selectedAttackIndex = -1;
                } else {
                    mode = Mode.ATTACK;
                    selectedIndex = clampSelection(selectedAttackIndex, getCurrentOptions().length);
                    selectedAbilityIndex = -1;
                }
                break;
            case TARGET_ALLY:
                mode = Mode.ABILITY;
                selectedIndex = clampSelection(selectedAbilityIndex, getCurrentOptions().length);
                selectedAttackIndex = -1;
                break;
            default:
                break;
        }
    }

    private boolean isBackSelection() {
        String[] options = getCurrentOptions();
        return options.length > 0 && selectedIndex == options.length - 1 && !"Flee".equals(options[selectedIndex]);
    }

    private int clampSelection(int preferredIndex, int optionCount) {
        if (optionCount <= 0) {
            return 0;
        }
        if (preferredIndex < 0) {
            return 0;
        }
        return Math.min(preferredIndex, optionCount - 1);
    }

    private void performAttack(BattleCombatant actor, BattleCombatant target, AttackMove move) {
        if (actor == null || target == null || move == null) {
            return;
        }
        if (Math.random() > actor.getStatusEffectManager().getPhysicalHitChanceMultiplier()) {
            battleLog.add(actor.getName() + " misses " + target.getName() + ".");
            endTurn(actor, adjustedSpeedCost(actor, move.speedCost));
            return;
        }
        float weaponMultiplier = actor.getPartyIndex() >= 0
            ? gameScreen.getWeaponDamageMultiplier(actor.getPartyIndex(), gameScreen.getEquippedWeaponType(actor.getPartyIndex()))
            : 1f;
        int damage = combatResolver.resolvePhysicalDamage(actor, target, move.multiplier, weaponMultiplier);
        ElementalOutcome elementalOutcome = applyElementalOutcome(target, move.element, damage);
        damage = elementalOutcome.damage;
        combatResolver.applyDamage(actor, target, damage);
        handleBossPhaseTransition(target);
        if (damage < 0) {
            battleLog.add(actor.getName() + " uses " + move.name + " on " + target.getName()
                + ", but it is absorbed for " + Math.abs(damage) + " HP.");
        } else {
            battleLog.add(actor.getName() + " uses " + move.name + " on " + target.getName() + " for " + damage + " damage.");
        }
        if (move.element != Element.NONE && elementalOutcome.multiplier != 1f) {
            battleLog.add("Elemental hit: " + ElementalSystem.describeHit(elementalOutcome.multiplier) + ".");
        }
        if (elementalOutcome.triggeredBreak) {
            battleLog.add(target.getName() + "'s " + move.element.name() + " guard breaks.");
        }
        triggerImpactFeedback(target, damage, elementalOutcome.triggeredBreak, move.element);
        applyPostHitUniqueEffects(actor, target, damage, false, move.name);
        awardWeaponProgress(actor, gameScreen.getEquippedWeaponType(actor.getPartyIndex()), move.name);
        endTurn(actor, adjustedSpeedCost(actor, move.speedCost));
    }

    private void useAbility(BattleCombatant actor, AbilityInstance ability, BattleCombatant explicitTarget) {
        if (actor == null || ability == null || !ability.isReady()) {
            battleLog.add("That ability is unavailable.");
            return;
        }
        if (!actor.getStatusEffectManager().canUseAbilities()) {
            battleLog.add(actor.getName() + " is silenced and cannot use abilities.");
            return;
        }
        AbilityDefinition definition = ability.getDefinition();
        switch (definition.getTargetType()) {
            case SELF:
                applyAbilityToTarget(actor, actor, ability);
                break;
            case SINGLE_ENEMY:
            case SINGLE_ALLY:
                applyAbilityToTarget(actor, explicitTarget, ability);
                break;
            case ALL_ENEMIES:
                for (BattleCombatant target : livingEnemies()) {
                    applyAbilityToTarget(actor, target, ability);
                }
                break;
            case ALL_ALLIES:
                for (BattleCombatant target : livingAllies()) {
                    applyAbilityToTarget(actor, target, ability);
                }
                break;
            default:
                break;
        }
        ability.use();
        recordAbilityGain(actor, ability, ProficiencyTracker.xpForAbilityUse());
        awardWeaponProgress(actor, definition.getWeaponType(), definition.getName());
        endTurn(actor, adjustedSpeedCost(actor, definition.getSpeedCost()), ability);
    }

    private void applyAbilityToTarget(BattleCombatant actor, BattleCombatant target, AbilityInstance ability) {
        AbilityDefinition definition = ability.getDefinition();
        if (target == null) {
            return;
        }
        switch (definition.getType()) {
            case DAMAGE:
                DamageResult damageResult = combatResolver.resolveAbilityDamage(actor, target, definition, ability.getPowerMultiplier());
                boolean triggeredBreak = damageResult.elementalBreak();
                int damage = damageResult.damage();
                combatResolver.applyDamage(actor, target, damage);
                handleBossPhaseTransition(target);
                if (damage < 0) {
                    battleLog.add(target.getName() + " absorbs " + definition.getName() + " and restores " + Math.abs(damage) + " HP.");
                } else {
                    battleLog.add(actor.getName() + " casts " + definition.getName() + " on " + target.getName() + " for " + damage + " damage.");
                    // Primary status: when no secondaryStatus but secondaryTargetType is set,
                    // the primary status is redirected to that target (e.g. Berserker Rush
                    // applies Berserk to the CASTER, not to the enemy it just hit).
                    if (definition.getAppliedStatus() != null) {
                        boolean primaryRedirected = definition.getSecondaryStatus() == null
                            && definition.getSecondaryTargetType() != null;
                        BattleCombatant primaryStatusTarget = primaryRedirected
                            ? resolveSecondaryTarget(actor, definition.getSecondaryTargetType())
                            : target;
                        if (primaryStatusTarget != null) {
                            primaryStatusTarget.getStatusEffectManager().apply(
                                definition.getAppliedStatus(),
                                adjustedStatusTurns(actor, definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()))
                            );
                            battleLog.add(primaryStatusTarget.getName() + " is afflicted with "
                                + prettifyStatus(definition.getAppliedStatus()) + ".");
                        }
                    }
                    // Secondary status (e.g. Gravity Well slows ALL_ENEMIES and protects SELF).
                    if (definition.getSecondaryStatus() != null) {
                        BattleCombatant secondaryStatusTarget = definition.getSecondaryTargetType() != null
                            ? resolveSecondaryTarget(actor, definition.getSecondaryTargetType())
                            : target;
                        if (secondaryStatusTarget != null) {
                            secondaryStatusTarget.getStatusEffectManager().apply(
                                definition.getSecondaryStatus(),
                                adjustedStatusTurns(actor, definition.getSecondaryStatus(),
                                    Math.max(1, definition.getSecondaryStatusTurns()))
                            );
                            battleLog.add(secondaryStatusTarget.getName() + " is affected by "
                                + prettifyStatus(definition.getSecondaryStatus()) + ".");
                        }
                    }
                }
                applyPostHitUniqueEffects(actor, target, damage, true, definition.getName());
                if (definition.getElement() != Element.NONE) {
                    float multiplier = ElementalSystem.getMultiplier(definition.getElement(), target);
                    if (multiplier != 1f) {
                        battleLog.add("Elemental hit: " + ElementalSystem.describeHit(multiplier) + ".");
                    }
                    if (damage > 0 && triggeredBreak) {
                        battleLog.add(target.getName() + "'s " + definition.getElement().name() + " guard breaks.");
                    }
                }
                triggerImpactFeedback(target, damage, triggeredBreak, definition.getElement());
                break;
            case HEAL:
                int healAmount = combatResolver.resolveHealing(actor, definition, ability.getPowerMultiplier());
                target.heal(healAmount);
                battleLog.add(actor.getName() + " restores " + healAmount + " HP to " + target.getName() + " with " + definition.getName() + ".");
                break;
            case BUFF:
            case UTILITY:
                if (definition.getAppliedStatus() != null) {
                    target.getStatusEffectManager().apply(
                        definition.getAppliedStatus(),
                        adjustedStatusTurns(actor, definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()))
                    );
                }
                // Secondary status for buffs (e.g. Genesis Field gives REGEN + SHELL to all allies).
                if (definition.getSecondaryStatus() != null) {
                    BattleCombatant secondaryStatusTarget = definition.getSecondaryTargetType() != null
                        ? resolveSecondaryTarget(actor, definition.getSecondaryTargetType())
                        : target;
                    if (secondaryStatusTarget != null) {
                        secondaryStatusTarget.getStatusEffectManager().apply(
                            definition.getSecondaryStatus(),
                            adjustedStatusTurns(actor, definition.getSecondaryStatus(),
                                Math.max(1, definition.getSecondaryStatusTurns()))
                        );
                    }
                }
                battleLog.add(actor.getName() + " uses " + definition.getName() + " on " + target.getName() + ".");
                break;
            case DEBUFF:
                if (definition.getAppliedStatus() != null) {
                    target.getStatusEffectManager().apply(
                        definition.getAppliedStatus(),
                        adjustedStatusTurns(actor, definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()))
                    );
                }
                // Secondary status for debuffs (e.g. Omniscience Eye applies WEAKEN + SLOW).
                if (definition.getSecondaryStatus() != null) {
                    BattleCombatant secondaryStatusTarget = definition.getSecondaryTargetType() != null
                        ? resolveSecondaryTarget(actor, definition.getSecondaryTargetType())
                        : target;
                    if (secondaryStatusTarget != null) {
                        secondaryStatusTarget.getStatusEffectManager().apply(
                            definition.getSecondaryStatus(),
                            adjustedStatusTurns(actor, definition.getSecondaryStatus(),
                                Math.max(1, definition.getSecondaryStatusTurns()))
                        );
                    }
                }
                battleLog.add(actor.getName() + " afflicts " + target.getName() + " with " + definition.getName() + ".");
                break;
            default:
                break;
        }
    }

    /**
     * Resolves a single combatant that should receive a secondary or redirected
     * status effect, based on the override target type declared in the ability.
     *
     * <p>SELF always maps back to the acting combatant. Enemy/Ally types return
     * the first available member of that group (used only when a single secondary
     * target must be resolved — AoE abilities that loop over targets handle their
     * own iteration in {@link #useAbility}).
     */
    private BattleCombatant resolveSecondaryTarget(BattleCombatant actor,
                                                    AbilityDefinition.TargetType targetType) {
        if (targetType == null || actor == null) {
            return null;
        }
        switch (targetType) {
            case SELF:
                return actor;
            case SINGLE_ALLY:
            case ALL_ALLIES:
                // Treat as "the casting unit" when resolving a single secondary recipient.
                return actor;
            case SINGLE_ENEMY:
            case ALL_ENEMIES:
                List<BattleCombatant> enemies = livingEnemies();
                return enemies.isEmpty() ? null : enemies.get(0);
            default:
                return actor;
        }
    }

    /** Capitalises a StatusEffectType name for readable battle-log output. */
    private String prettifyStatus(StatusEffectType type) {
        if (type == null) return "";
        String lower = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private void analyzeEnemy(BattleCombatant actor, BattleCombatant enemy) {
        int bonusLevels = actor.getPartyIndex() >= 0 && hasAbility(actor, "scan") ? 2 : 1;
        int scanLevel = bestiaryManager.recordScan(enemy.getId(), bonusLevels);
        battleLog.add(actor.getName() + " analyzes " + enemy.getName() + ".");
        if (scanLevel >= 1) {
            battleLog.add("HP " + (int) enemy.getHealth() + "/" + (int) enemy.getMaxHealth() + " | Element " + describeElements(enemy));
        }
        if (scanLevel >= 2) {
            battleLog.add("STR " + (int) enemy.getStrength() + " INT " + (int) enemy.getIntelligence() + " STA " + (int) enemy.getStamina());
        }
        if (scanLevel >= 3) {
            battleLog.add("Scan complete. Gold reward: " + enemy.getRewardGold() + ".");
        }
        endTurn(actor, 50);
    }

    private String describeElements(BattleCombatant enemy) {
        if (!enemy.getWeaknesses().isEmpty()) {
            return "Weak " + enemy.getWeaknesses().get(0).name();
        }
        if (!enemy.getResistances().isEmpty()) {
            return "Resist " + enemy.getResistances().get(0).name();
        }
        return "Neutral";
    }

    private void defend(BattleCombatant actor) {
        actor.getStatusEffectManager().apply(StatusEffectType.DEFENDING, 1);
        if (actor != null && actor.isCombatClass("Vanguard")) {
            actor.getStatusEffectManager().apply(StatusEffectType.PROTECT, 1);
            battleLog.add(actor.getName() + " projects a vanguard guard.");
        }
        battleLog.add(actor.getName() + " takes a defensive stance.");
        endTurn(actor, adjustedSpeedCost(actor, 40));
    }

    private void useItem(BattleCombatant actor) {
        if (healingPotions <= 0) {
            battleLog.add("No repair kits left.");
            return;
        }
        healingPotions--;
        int healAmount = Math.max(10, Math.round(actor.getMaxHealth() * 0.22f));
        if (actor != null && actor.isCombatClass("Support")) {
            healAmount = Math.round(healAmount * 1.15f);
        }
        actor.heal(healAmount);
        battleLog.add(actor.getName() + " uses a repair kit and restores " + healAmount + " HP.");
        endTurn(actor, adjustedSpeedCost(actor, 70));
    }

    private void attemptFlee(BattleCombatant actor) {
        float partySpeed = 0f;
        for (BattleCombatant ally : livingAllies()) {
            partySpeed += ally.getEffectiveSpeed();
        }
        float enemySpeed = 0f;
        for (BattleCombatant enemy : livingEnemies()) {
            enemySpeed += enemy.getEffectiveSpeed();
        }
        float partyAverage = livingAllies().isEmpty() ? 0f : partySpeed / livingAllies().size();
        float enemyAverage = livingEnemies().isEmpty() ? 0f : enemySpeed / livingEnemies().size();
        float fleeChance = Math.min(0.9f, Math.max(0.2f, 0.5f + ((partyAverage - enemyAverage) / 100f)));
        if (Math.random() <= fleeChance) {
            battleLog.add(actor.getName() + " leads the party to safety.");
            finishBattle(false, true);
            return;
        }
        battleLog.add(actor.getName() + " cannot escape.");
        endTurn(actor, adjustedSpeedCost(actor, 100));
    }

    private void executeEnemyTurn(BattleCombatant actor) {
        BattleCombatant target = chooseEnemyTarget();
        if (target == null) {
            finishBattle(false, false);
            return;
        }
        int speedCost = 80;
        if ("RANGED".equals(actor.getAiProfile()) || "BOSS".equals(actor.getAiProfile())) {
            AbilityDefinition enemyAbility = createEnemyAbility(actor);
            DamageResult damageResult = combatResolver.resolveAbilityDamage(actor, target, enemyAbility);
            int damage = damageResult.damage();
            combatResolver.applyDamage(target, damage);
            if (damage < 0) {
                battleLog.add(target.getName() + " absorbs " + enemyAbility.getName()
                    + " and restores " + Math.abs(damage) + " HP.");
            } else {
                battleLog.add(actor.getName() + " unleashes " + enemyAbility.getName()
                    + " on " + target.getName() + " for " + damage + " damage.");
                applyEnemyAbilityStatus(target, enemyAbility);
            }
            triggerImpactFeedback(target, damage, damageResult.elementalBreak(), enemyAbility.getElement());
            speedCost = 95;
        } else {
            int damage = combatResolver.resolvePhysicalDamage(actor, target, 1.2f);
            combatResolver.applyDamage(target, damage);
            battleLog.add(actor.getName() + " strikes " + target.getName() + " for " + damage + " damage.");
            triggerImpactFeedback(target, damage, false, Element.NONE);
        }
        endTurn(actor, speedCost);
    }

    private AbilityDefinition createEnemyAbility(BattleCombatant actor) {
        return createEnemyAbilityFor(
            actor != null ? actor.getId() : null,
            actor != null ? bossPhases.getOrDefault(actor.getId(), 1) : 1
        );
    }

    static AbilityDefinition createEnemyAbilityFor(String actorId, int phase) {
        AbilityDefinition definition = new AbilityDefinition(
            "enemy_mind_lance",
            "Mind Lance",
            AbilityDefinition.AbilityType.DAMAGE,
            AbilityDefinition.TargetType.SINGLE_ENEMY,
            0f,
            18f,
            0f,
            "Enemy tech strike"
        );
        definition.setElement(Element.LIGHTNING);
        definition.setSpeedCost(95);
        if (ORIGIN_CORE_ID.equals(actorId)) {
            if (phase == 2) {
                definition.setName("Entropy Spear");
                definition.setElement(Element.FIRE);
                definition.setPower(22f);
                definition.setAppliedStatus(StatusEffectType.WEAKEN);
                definition.setStatusTurns(2);
            } else if (phase >= 3) {
                definition.setName("Genesis Reset");
                definition.setElement(Element.WATER);
                definition.setPower(26f);
                definition.setAppliedStatus(StatusEffectType.SLOW);
                definition.setStatusTurns(2);
            }
        } else if (VOLT_SPECTER_ID.equals(actorId) && phase >= 2) {
            definition.setName("Thunder Refrain");
            definition.setElement(Element.LIGHTNING);
            definition.setPower(23f);
            definition.setAppliedStatus(StatusEffectType.PARALYZE);
            definition.setStatusTurns(2);
        } else if (NULL_WARDEN_ID.equals(actorId) && phase >= 2) {
            definition.setName("Null Lock");
            definition.setElement(Element.WATER);
            definition.setPower(24f);
            definition.setAppliedStatus(StatusEffectType.SILENCE);
            definition.setStatusTurns(2);
        } else if (THE_UNMAKER_ID.equals(actorId) && phase >= 2) {
            definition.setName("Ruin Wake");
            definition.setElement(Element.FIRE);
            definition.setPower(28f);
            definition.setAppliedStatus(StatusEffectType.WEAKEN);
            definition.setStatusTurns(3);
        }
        return definition;
    }

    private void initializeBossPhases(List<BattleCombatant> combatants) {
        if (combatants == null) {
            return;
        }
        for (BattleCombatant combatant : combatants) {
            if (combatant != null && isPhasedBoss(combatant.getId())) {
                bossPhases.put(combatant.getId(), 1);
            }
        }
    }

    private void handleBossPhaseTransition(BattleCombatant target) {
        if (target == null || !target.isAlive() || !isPhasedBoss(target.getId())) {
            return;
        }
        int currentPhase = bossPhases.getOrDefault(target.getId(), 1);
        int nextPhase = determineBossPhase(target.getId(), currentPhase, target.getHealth(), target.getMaxHealth());
        if (nextPhase == currentPhase) {
            return;
        }
        if (ORIGIN_CORE_ID.equals(target.getId())) {
            transitionOriginCore(target, nextPhase);
        } else if (VOLT_SPECTER_ID.equals(target.getId())) {
            transitionVoltSpecter(target, nextPhase);
        } else if (NULL_WARDEN_ID.equals(target.getId())) {
            transitionNullWarden(target, nextPhase);
        } else if (THE_UNMAKER_ID.equals(target.getId())) {
            transitionTheUnmaker(target, nextPhase);
        }
    }

    static boolean isPhasedBoss(String combatantId) {
        return ORIGIN_CORE_ID.equals(combatantId)
            || VOLT_SPECTER_ID.equals(combatantId)
            || NULL_WARDEN_ID.equals(combatantId)
            || THE_UNMAKER_ID.equals(combatantId);
    }

    static int determineBossPhase(String combatantId, int currentPhase, float currentHealth, float maxHealth) {
        float healthRatio = currentHealth / Math.max(1f, maxHealth);
        if (ORIGIN_CORE_ID.equals(combatantId)) {
            if (currentPhase == 1 && healthRatio <= 0.66f) {
                return 2;
            }
            if (currentPhase == 2 && healthRatio <= 0.33f) {
                return 3;
            }
        } else if (VOLT_SPECTER_ID.equals(combatantId) && currentPhase == 1 && healthRatio <= 0.5f) {
            return 2;
        } else if (NULL_WARDEN_ID.equals(combatantId) && currentPhase == 1 && healthRatio <= 0.5f) {
            return 2;
        } else if (THE_UNMAKER_ID.equals(combatantId) && currentPhase == 1 && healthRatio <= 0.45f) {
            return 2;
        }
        return currentPhase;
    }

    private void transitionOriginCore(BattleCombatant target, int nextPhase) {
        bossPhases.put(target.getId(), nextPhase);
        target.heal(target.getMaxHealth() * 0.12f);
        clearBossElements(target);
        if (nextPhase == 2) {
            target.getWeaknesses().add(Element.WATER);
            target.getResistances().add(Element.EARTH);
            target.getStatusEffectManager().apply(StatusEffectType.HASTE, 3);
            target.getStatusEffectManager().apply(StatusEffectType.SHELL, 3);
            battleLog.add("Origin Core ruptures its outer shell and enters Phase 2.");
            battleLog.add("Its weakness rotates to WATER as the chamber floods with unstable coolant.");
        } else {
            target.getWeaknesses().add(Element.FIRE);
            target.getResistances().add(Element.WATER);
            target.getResistances().add(Element.LIGHTNING);
            target.getStatusEffectManager().apply(StatusEffectType.BERSERK, 4);
            target.getStatusEffectManager().apply(StatusEffectType.REGEN, 4);
            battleLog.add("Origin Core fractures reality and enters Phase 3.");
            battleLog.add("Its weakness rotates to FIRE as Genesis Reset destabilizes the chamber.");
        }
    }

    private void transitionVoltSpecter(BattleCombatant target, int nextPhase) {
        bossPhases.put(target.getId(), nextPhase);
        target.heal(target.getMaxHealth() * 0.1f);
        clearBossElements(target);
        target.getWeaknesses().add(Element.WATER);
        target.getResistances().add(Element.LIGHTNING);
        target.getResistances().add(Element.WATER);
        target.getStatusEffectManager().apply(StatusEffectType.HASTE, 3);
        for (BattleCombatant ally : livingAllies()) {
            ally.getStatusEffectManager().apply(StatusEffectType.SLOW, 2);
        }
        battleLog.add("Volt Specter detonates into a storm lattice and enters Phase 2.");
        battleLog.add("Its weakness rotates to WATER as the arena floods with conductive vapor.");
    }

    private void transitionNullWarden(BattleCombatant target, int nextPhase) {
        bossPhases.put(target.getId(), nextPhase);
        target.heal(target.getMaxHealth() * 0.14f);
        clearBossElements(target);
        target.getWeaknesses().add(Element.LIGHTNING);
        target.getResistances().add(Element.WATER);
        target.getResistances().add(Element.EARTH);
        target.getStatusEffectManager().apply(StatusEffectType.PROTECT, 3);
        target.getStatusEffectManager().apply(StatusEffectType.SHELL, 3);
        for (BattleCombatant ally : livingAllies()) {
            ally.getStatusEffectManager().apply(StatusEffectType.WEAKEN, 2);
        }
        battleLog.add("Null Warden seals the chamber and enters Phase 2.");
        battleLog.add("Its weakness rotates to LIGHTNING while the party is pressed under a null-lock field.");
    }

    private void transitionTheUnmaker(BattleCombatant target, int nextPhase) {
        bossPhases.put(target.getId(), nextPhase);
        target.heal(target.getMaxHealth() * 0.15f);
        clearBossElements(target);
        target.getWeaknesses().add(Element.ICE);
        target.getResistances().add(Element.FIRE);
        target.getResistances().add(Element.WATER);
        target.getStatusEffectManager().apply(StatusEffectType.BERSERK, 4);
        target.getStatusEffectManager().apply(StatusEffectType.REGEN, 4);
        for (BattleCombatant ally : livingAllies()) {
            ally.getStatusEffectManager().apply(StatusEffectType.SLOW, 2);
        }
        battleLog.add("The Unmaker tears through its own shell and enters Phase 2.");
        battleLog.add("Its weakness rotates to ICE as ruin ash slows the entire party.");
    }

    private void clearBossElements(BattleCombatant target) {
        target.getWeaknesses().clear();
        target.getResistances().clear();
        target.getAbsorbs().clear();
    }

    private void applyEnemyAbilityStatus(BattleCombatant target, AbilityDefinition enemyAbility) {
        if (target == null || enemyAbility == null || enemyAbility.getAppliedStatus() == null) {
            return;
        }
        target.getStatusEffectManager().apply(
            enemyAbility.getAppliedStatus(),
            Math.max(1, enemyAbility.getStatusTurns())
        );
    }

    private BattleCombatant chooseEnemyTarget() {
        for (BattleCombatant ally : livingAllies()) {
            if (ally.getStatusEffectManager().has(StatusEffectType.TAUNT)) {
                return ally;
            }
        }
        BattleCombatant lowest = null;
        for (BattleCombatant ally : livingAllies()) {
            if (lowest == null || ally.getHealth() < lowest.getHealth()) {
                lowest = ally;
            }
        }
        return lowest;
    }

    private void endTurn(BattleCombatant actor, int speedCost) {
        endTurn(actor, speedCost, null);
    }

    private void endTurn(BattleCombatant actor, int speedCost, AbilityInstance usedAbility) {
        tickCooldowns(actor, usedAbility);
        appendLogs(actor.getStatusEffectManager().onActionTaken(actor));
        appendLogs(actor.getStatusEffectManager().endTurn(actor));
        battleState.getTurnTimeline().consumeTurn(actor, speedCost);
        activeActor = null;
        turnInitialized = false;
        mode = Mode.ROOT;
        selectedIndex = 0;
        selectedAttackIndex = -1;
        selectedAbilityIndex = -1;
        actionDelay = 0.18f;
    }

    private void tickCooldowns(BattleCombatant actor, AbilityInstance usedAbility) {
        if (actor == null) {
            return;
        }
        for (AbilityInstance ability : actor.getAbilities()) {
            if (ability == usedAbility) {
                continue;
            }
            ability.update(1f);
        }
    }

    private ElementalOutcome applyElementalOutcome(BattleCombatant target, Element element, int baseDamage) {
        ElementalOutcome outcome = new ElementalOutcome();
        outcome.damage = baseDamage;
        outcome.multiplier = 1f;
        if (target == null || element == null || element == Element.NONE || baseDamage == 0) {
            return outcome;
        }
        outcome.multiplier = ElementalSystem.getMultiplier(element, target);
        if (outcome.multiplier < 0f) {
            outcome.damage = -Math.max(1, baseDamage);
            return outcome;
        }
        outcome.damage = Math.max(1, Math.round(baseDamage * outcome.multiplier));
        if (outcome.damage > 0) {
            outcome.triggeredBreak = maybeTriggerElementalBreak(target, element);
        }
        return outcome;
    }

    private void applyBattleStartUniqueBoosts(List<BattleCombatant> combatants) {
        for (BattleCombatant combatant : combatants) {
            if (combatant == null || !combatant.isAlive()) {
                continue;
            }
            if (combatant.hasUniqueBoost("BARRIER_MATRIX")) {
                combatant.getStatusEffectManager().apply(StatusEffectType.PROTECT, 2);
                battleLog.add(combatant.getName() + " deploys a barrier matrix.");
            }
        }
    }

    private void applyPostHitUniqueEffects(BattleCombatant actor, BattleCombatant target, int damage, boolean abilityHit, String actionName) {
        if (actor == null || target == null || damage <= 0) {
            return;
        }
        if (!abilityHit && actor.hasUniqueBoost("LIFE_TAP")) {
            int healAmount = Math.max(1, Math.round(damage * 0.2f));
            float before = actor.getHealth();
            actor.heal(healAmount);
            int restored = Math.round(actor.getHealth() - before);
            if (restored > 0) {
                battleLog.add(actor.getName() + " siphons " + restored + " HP through " + actionName + ".");
            }
        }
        if (target.hasUniqueBoost("COUNTER_FIELD") && actor != target) {
            int reflectDamage = Math.max(1, Math.round(damage * 0.15f));
            actor.applyDirectDamage(reflectDamage);
            battleLog.add(target.getName() + "'s counter field jolts " + actor.getName() + " for " + reflectDamage + " damage.");
        }
    }

    private int adjustedSpeedCost(BattleCombatant actor, int baseSpeedCost) {
        if (actor != null && actor.isCombatClass("Scout")) {
            return Math.max(20, Math.round(baseSpeedCost * 0.85f));
        }
        return baseSpeedCost;
    }

    private int adjustedStatusTurns(BattleCombatant actor, StatusEffectType status, int baseTurns) {
        if (actor == null || status == null) {
            return baseTurns;
        }
        if (actor.isCombatClass("Vanguard") && (status == StatusEffectType.PROTECT || status == StatusEffectType.TAUNT)) {
            return baseTurns + 1;
        }
        if (actor.isCombatClass("Support") && (status == StatusEffectType.HASTE
            || status == StatusEffectType.PROTECT
            || status == StatusEffectType.REGEN
            || status == StatusEffectType.SHELL)) {
            return baseTurns + 1;
        }
        return baseTurns;
    }

    private boolean maybeTriggerElementalBreak(BattleCombatant target, Element element) {
        if (target == null || element == null || element == Element.NONE || target.hasElementalBreak(element)) {
            return false;
        }
        int chain = target.registerElementalHit(element);
        return chain >= 3 && target.hasElementalBreak(element);
    }

    private void finishBattle(boolean enemyDefeated, boolean escaped) {
        BattleResult result = new BattleResult();
        result.playerHealth = battleState.getAllies().isEmpty() ? 0f : battleState.getAllies().get(0).getHealth();
        result.robotHealth = buildRobotHealth();
        result.robotPartySlots = encounter.robotPartySlots;
        result.enemyHealth = buildEnemyHealth();
        result.healingPotions = healingPotions;
        result.enemyDefeated = enemyDefeated;
        result.enemyEscaped = escaped;
        result.goldEarned = buildGoldRewards(enemyDefeated);
        result.experienceEarned = buildExperienceRewards(enemyDefeated);
        result.enemyReferences = encounter.enemyReferences;
        result.updatedBestiary = bestiaryManager.exportData();
        result.droppedEquipmentIds = new String[0];
        result.droppedEquipmentNames = new String[0];
        result.droppedShards = buildDroppedShards(enemyDefeated);
        result.droppedShardNames = gameScreen.resolveShardDropNames(result.droppedShards);
        result.droppedComponents = buildDroppedComponents(enemyDefeated);
        result.droppedComponentNames = gameScreen.resolveForgeDropNames(result.droppedComponents);

        if (!enemyDefeated) {
            gameScreen.resolveBattle(result);
            return;
        }

        closingToResults = true;
        GameScreen.BattleProgressionPreview robotPreview = gameScreen.previewRobotBattleProgression(sum(result.experienceEarned));
        BattleResultSummary summary = new BattleResultSummary(
            sum(result.goldEarned),
            sum(result.experienceEarned),
            gameScreen.previewLevelUps(sum(result.experienceEarned)),
            countBestiaryUpdates(),
            combineDrops(result.droppedShardNames, result.droppedComponentNames),
            toProgressLines(abilityXpGains, "XP"),
            toProgressLines(weaponXpGains, "XP"),
            masteryUnlocks.toArray(new String[0]),
            robotPreview.robotProgress.toArray(new String[0])
        );
        screenManager.push(new BattleResultsScreen(game, screenManager, gameScreen, result, summary));
    }

    private void recordAbilityGain(BattleCombatant actor, AbilityInstance ability, int xpAmount) {
        if (actor == null || ability == null || !actor.isAlly() || actor.getPartyIndex() < 0) {
            return;
        }
        int beforeLevel = ability.getProficiencyLevel();
        int levelsGained = ability.addProficiencyXp(xpAmount);
        String key = actor.getName() + ": " + ability.getDefinition().getName();
        abilityXpGains.put(key, abilityXpGains.getOrDefault(key, 0) + xpAmount);
        if (levelsGained > 0) {
            masteryUnlocks.add(key + " proficiency reached Lv." + ability.getProficiencyLevel() + ".");
            awakeningBannerText = key + " awakened";
            awakeningBannerTimer = 1.2f;
        }
        gameScreen.recordRobotUsage(actor.getPartyIndex(), usageKeyForAbility(ability.getDefinition()), 1);
        List<String> newUnlocks = gameScreen.applyAbilityMasteryUnlocks(actor.getPartyIndex());
        masteryUnlocks.addAll(newUnlocks);
        if (!newUnlocks.isEmpty()) {
            syncActorAbilities(actor);
        }
    }

    private void awardWeaponProgress(BattleCombatant actor, WeaponType weaponType, String actionName) {
        if (actor == null || !actor.isAlly() || actor.getPartyIndex() < 0 || weaponType == null
            || weaponType == WeaponType.NONE) {
            return;
        }
        GameScreen.WeaponGain gain = gameScreen.awardWeaponProficiency(
            actor.getPartyIndex(),
            weaponType,
            com.rogueforge.game.progression.WeaponProficiencyTracker.xpForAttack()
        );
        String key = actor.getName() + ": " + weaponType.name();
        weaponXpGains.put(key, weaponXpGains.getOrDefault(key, 0)
            + com.rogueforge.game.progression.WeaponProficiencyTracker.xpForAttack());
        gameScreen.recordRobotUsage(actor.getPartyIndex(), "assault", 1);
        if (gain != null && gain.toLevel > gain.fromLevel) {
            masteryUnlocks.add(actor.getName() + " raised " + weaponType.name() + " proficiency to Lv." + gain.toLevel
                + " with " + actionName + ".");
            for (String unlockLabel : gain.unlockLabels) {
                masteryUnlocks.add(actor.getName() + " unlocked " + unlockLabel + " for " + weaponType.name() + ".");
            }
        }
    }

    private void syncActorAbilities(BattleCombatant actor) {
        if (actor == null || actor.getPartyIndex() < 0) {
            return;
        }
        Map<Object, AbilityInstance> byProgression = new HashMap<>();
        Map<String, AbilityInstance> byId = new HashMap<>();
        for (AbilityInstance existing : actor.getAbilities()) {
            byId.put(existing.getDefinition().getId(), existing);
            if (existing.getProgressionState() != null) {
                byProgression.put(existing.getProgressionState(), existing);
            }
        }

        List<AbilityInstance> refreshed = gameScreen.getPartyAbilityInstances(actor.getPartyIndex());
        for (AbilityInstance replacement : refreshed) {
            AbilityInstance source = replacement.getProgressionState() != null
                ? byProgression.get(replacement.getProgressionState())
                : byId.get(replacement.getDefinition().getId());
            if (source != null) {
                replacement.setCurrentCooldown(source.getCurrentCooldown());
            }
        }
        actor.getAbilities().clear();
        actor.getAbilities().addAll(refreshed);
    }

    private String[] toProgressLines(Map<String, Integer> values, String suffix) {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            lines.add(entry.getKey() + " +" + entry.getValue() + " " + suffix);
        }
        return lines.toArray(new String[0]);
    }

    private Map<String, Integer> buildDroppedShards(boolean enemyDefeated) {
        Map<String, Integer> drops = new HashMap<>();
        if (!enemyDefeated) {
            return drops;
        }
        for (BattleCombatant enemy : battleState.getEnemies()) {
            if (enemy.isAlive()) {
                continue;
            }
            String grade = enemy.getRank() != null && !enemy.getRank().isEmpty() ? enemy.getRank() : "G";
            drops.put(grade, drops.getOrDefault(grade, 0) + 1);
        }
        return drops;
    }

    private Map<String, Integer> buildDroppedComponents(boolean enemyDefeated) {
        Map<String, Integer> drops = new HashMap<>();
        if (!enemyDefeated) {
            return drops;
        }
        for (BattleCombatant enemy : battleState.getEnemies()) {
            if (enemy.isAlive()) {
                continue;
            }
            Map<String, Integer> rolled = gameScreen.rollForgeDropsForEnemy(enemy.getId(), enemy.getRank());
            for (Map.Entry<String, Integer> entry : rolled.entrySet()) {
                drops.put(entry.getKey(), drops.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        return drops;
    }

    private String[] combineDrops(String[] shardDrops, String[] componentDrops) {
        List<String> combined = new ArrayList<>();
        if (shardDrops != null) {
            combined.addAll(Arrays.asList(shardDrops));
        }
        if (componentDrops != null) {
            combined.addAll(Arrays.asList(componentDrops));
        }
        return combined.toArray(new String[0]);
    }

    private int countBestiaryUpdates() {
        int updates = 0;
        for (BattleCombatant enemy : battleState.getEnemies()) {
            if (bestiaryManager.getScanLevel(enemy.getId()) > gameScreen.getBestiaryScanLevel(enemy.getId())) {
                updates++;
            }
        }
        return updates;
    }

    private float[] buildRobotHealth() {
        List<BattleCombatant> allies = battleState.getAllies();
        if (allies.size() <= 1) {
            return new float[0];
        }
        float[] values = new float[allies.size() - 1];
        for (int i = 1; i < allies.size(); i++) {
            values[i - 1] = allies.get(i).getHealth();
        }
        return values;
    }

    private float[] buildEnemyHealth() {
        List<BattleCombatant> enemies = battleState.getEnemies();
        float[] values = new float[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            values[i] = enemies.get(i).getHealth();
        }
        return values;
    }

    private int[] buildGoldRewards(boolean enemyDefeated) {
        List<BattleCombatant> enemies = battleState.getEnemies();
        return new int[enemies.size()];
    }

    private int[] buildExperienceRewards(boolean enemyDefeated) {
        List<BattleCombatant> enemies = battleState.getEnemies();
        int[] values = new int[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            values[i] = enemyDefeated && !enemies.get(i).isAlive() ? enemies.get(i).getRewardExperience() : 0;
        }
        return values;
    }

    private int sum(int[] values) {
        int total = 0;
        if (values == null) {
            return total;
        }
        for (int value : values) {
            total += value;
        }
        return total;
    }

    private void appendLogs(List<String> logs) {
        for (String log : logs) {
            battleLog.add(log);
        }
        while (battleLog.size() > 9) {
            battleLog.remove(0);
        }
    }

    private List<BattleCombatant> livingEnemies() {
        List<BattleCombatant> living = new ArrayList<>();
        for (BattleCombatant enemy : battleState.getEnemies()) {
            if (enemy.isAlive()) {
                living.add(enemy);
            }
        }
        return living;
    }

    private List<BattleCombatant> livingAllies() {
        List<BattleCombatant> living = new ArrayList<>();
        for (BattleCombatant ally : battleState.getAllies()) {
            if (ally.isAlive()) {
                living.add(ally);
            }
        }
        return living;
    }

    private AbilityInstance readyAbilityAt(BattleCombatant actor, int index) {
        if (actor == null) {
            return null;
        }
        if (index < 0 || index >= actor.getAbilities().size()) {
            return null;
        }
        return actor.getAbilities().get(index);
    }

    private boolean hasAbility(BattleCombatant actor, String abilityId) {
        for (AbilityInstance ability : actor.getAbilities()) {
            if (ability.getDefinition().getId().equals(abilityId)) {
                return true;
            }
        }
        return false;
    }

    private String[] getCurrentOptions() {
        switch (mode) {
            case ATTACK:
                return attackOptions();
            case ABILITY:
                return abilityOptions();
            case TARGET_ENEMY:
            case ANALYZE_TARGET:
                return combatantOptions(livingEnemies());
            case TARGET_ALLY:
                return combatantOptions(livingAllies());
            case ROOT:
            default:
                // BERSERK command lock: when the actor is berserked, only Attack is available.
                if (activeActor != null && activeActor.getStatusEffectManager().canAttackOnly()) {
                    return BERSERK_ROOT_ACTIONS;
                }
                return ROOT_ACTIONS;
        }
    }

    private String[] attackOptions() {
        String[] options = new String[ATTACK_MOVES.length + 1];
        for (int i = 0; i < ATTACK_MOVES.length; i++) {
            options[i] = ATTACK_MOVES[i].name + " [" + ATTACK_MOVES[i].speedCost + "]";
        }
        options[options.length - 1] = "Back";
        return options;
    }

    private String[] abilityOptions() {
        String[] options = new String[activeActor.getAbilities().size() + 1];
        for (int i = 0; i < activeActor.getAbilities().size(); i++) {
            AbilityInstance ability = activeActor.getAbilities().get(i);
            String cooldown = ability.isReady() ? "" : " [" + Math.max(1, Math.round(ability.getCurrentCooldown())) + "]";
            options[i] = ability.getDefinition().getName() + cooldown;
        }
        options[options.length - 1] = "Back";
        return options;
    }

    private String[] combatantOptions(List<BattleCombatant> combatants) {
        String[] options = new String[combatants.size() + 1];
        for (int i = 0; i < combatants.size(); i++) {
            BattleCombatant combatant = combatants.get(i);
            options[i] = combatant.getName() + " HP " + (int) combatant.getHealth() + "/" + (int) combatant.getMaxHealth();
        }
        options[options.length - 1] = "Back";
        return options;
    }

    private void drawPanels(float w, float h) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.11f, 0.12f, 0.16f, 1f);
        shapeRenderer.rect(32f, h - 150f, w - 64f, 118f);
        shapeRenderer.rect(32f, 32f, w - 64f, 170f);
        shapeRenderer.rect(32f, 220f, (w * 0.45f), h - 390f);
        shapeRenderer.rect(w * 0.52f, 220f, w * 0.43f, h - 390f);
        shapeRenderer.end();
    }

    private void drawCombatants(float w, float h) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        titleFont.setColor(1f, 0.87f, 0.56f, 1f);
        titleFont.draw(batch, "ROGUE FORGE BATTLE", 48f, h - 54f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, activeActor != null ? activeActor.getName() + "'s turn" : "Resolving...", 48f, h - 92f);

        float leftY = h - 180f;
        bodyFont.draw(batch, "Party", 48f, leftY);
        for (int i = 0; i < battleState.getAllies().size(); i++) {
            drawCombatantLine(48f, leftY - 34f - (i * 56f), battleState.getAllies().get(i));
        }

        float rightY = h - 180f;
        bodyFont.draw(batch, "Enemies", w * 0.55f, rightY);
        for (int i = 0; i < battleState.getEnemies().size(); i++) {
            drawCombatantLine(w * 0.55f, rightY - 34f - (i * 56f), battleState.getEnemies().get(i));
        }
        batch.end();
    }

    private void drawCombatantLine(float x, float y, BattleCombatant combatant) {
        bodyFont.setColor(combatant.isAlive() ? Color.WHITE : Color.GRAY);
        bodyFont.draw(batch, combatant.getName(), x, y);
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch,
            "HP " + (int) combatant.getHealth() + "/" + (int) combatant.getMaxHealth()
                + "  SPD " + (int) combatant.getEffectiveSpeed()
                + "  " + statusSummary(combatant),
            x,
            y - 22f);
    }

    private String statusSummary(BattleCombatant combatant) {
        if (combatant.getStatusEffectManager().getActiveEffects().isEmpty()) {
            return "No status";
        }
        return combatant.getStatusEffectManager().getActiveEffects().get(0).getType().name();
    }

    private void updateTimelinePanel(float w, float h) {
        List<BattleCombatant> turns = battleState.getTurnTimeline().getNextTurns(battleState.getCombatants(), 8);
        timelinePanel.updateTimeline(turns);
        timelinePanel.setPosition(w - 235f, h - 62f);
    }

    private void updateScene2dHud(float w, float h) {
        updateTimelinePanel(w, h);
        commandPanel.updateOptions(
            "Commands",
            getCurrentOptions(),
            selectedIndex,
            "Enter confirm  |  Esc back  |  Arrows move"
        );
        commandPanel.setPosition(28f, 42f);
        inspectorPanel.updateContent(
            "Battle Feed",
            buildInspectorDetails(),
            buildRecentBattleLogLines()
        );
        inspectorPanel.setPosition(w * 0.5f + 36f, 26f);
        inputContextRouter.activate(resolveInputContext());
    }

    private void updateImpactFeedback(float delta) {
        battleShakeTimer = Math.max(0f, battleShakeTimer - delta);
        awakeningBannerTimer = Math.max(0f, awakeningBannerTimer - delta);
        for (int i = impactBursts.size() - 1; i >= 0; i--) {
            ImpactBurst burst = impactBursts.get(i);
            burst.life -= delta;
            burst.radius += delta * 90f;
            if (burst.life <= 0f) {
                impactBursts.remove(i);
            }
        }
    }

    private void applyBattleShake() {
        if (battleShakeTimer <= 0f) {
            return;
        }
        float strength = battleShakeIntensity * (battleShakeTimer / 0.28f);
        camera.position.x += (float) ((Math.random() - 0.5f) * strength);
        camera.position.y += (float) ((Math.random() - 0.5f) * strength);
        camera.update();
    }

    private void triggerImpactFeedback(BattleCombatant target, int damage, boolean elementalBreak, Element element) {
        if (damage <= 0) {
            return;
        }
        battleShakeIntensity = Math.max(battleShakeIntensity, damage >= 30 ? 14f : damage >= 18 ? 8f : 4f);
        battleShakeTimer = 0.28f;
        impactBursts.add(new ImpactBurst(
            target != null && target.isAlly() ? 220f : 980f,
            480f,
            burstColorFor(elementalBreak, element),
            elementalBreak ? 1.1f : 0.55f
        ));
        if (elementalBreak) {
            impactBursts.add(new ImpactBurst( target != null && target.isAlly() ? 220f : 980f,
                480f,
                new Color(1f, 0.95f, 0.68f, 1f),
                1.35f
            ));
        }
    }

    private Color burstColorFor(boolean elementalBreak, Element element) {
        if (elementalBreak) {
            return new Color(1f, 0.75f, 0.35f, 1f);
        }
        if (element == Element.FIRE) {
            return new Color(1f, 0.45f, 0.28f, 1f);
        }
        if (element == Element.LIGHTNING) {
            return new Color(0.78f, 0.9f, 1f, 1f);
        }
        if (element == Element.ICE) {
            return new Color(0.7f, 0.92f, 1f, 1f);
        }
        if (element == Element.EARTH) {
            return new Color(0.72f, 0.58f, 0.34f, 1f);
        }
        return new Color(1f, 1f, 1f, 1f);
    }

    private String usageKeyForAbility(AbilityDefinition definition) {
        if (definition == null) {
            return "control";
        }
        switch (definition.getType()) {
            case HEAL:
            case BUFF:
                return "support";
            case DEBUFF:
            case UTILITY:
                return "control";
            case DAMAGE:
            default:
                return "assault";
        }
    }

    private void drawImpactBursts() {
        if (impactBursts.isEmpty()) {
            return;
        }
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (ImpactBurst burst : impactBursts) {
            float alpha = Math.max(0f, burst.life);
            shapeRenderer.setColor(burst.color.r, burst.color.g, burst.color.b, alpha);
            shapeRenderer.circle(burst.x, burst.y, burst.radius * burst.scale, 18);
        }
        shapeRenderer.end();
    }

    private void drawAwakeningBanner(float h) {
        if (awakeningBannerTimer <= 0f || awakeningBannerText == null || awakeningBannerText.isEmpty()) {
            return;
        }
        batch.begin();
        titleFont.setColor(1f, 0.92f, 0.62f, Math.min(1f, awakeningBannerTimer));
        titleFont.draw(batch, awakeningBannerText, 64f, h - 122f);
        batch.end();
    }

    private InputContext resolveInputContext() {
        if (closingToResults) {
            return InputContext.COMBAT_RESULTS;
        }
        return mode == Mode.TARGET_ENEMY || mode == Mode.TARGET_ALLY || mode == Mode.ANALYZE_TARGET
            ? InputContext.COMBAT_TARGETING
            : InputContext.COMBAT_COMMAND;
    }

    private List<DebugOverlay.DebugSection> buildDebugOverlaySections() {
        List<DebugOverlay.DebugSection> sections = new ArrayList<>();
        sections.add(new DebugOverlay.DebugSection("Battle", List.of(
            "FPS: " + Gdx.graphics.getFramesPerSecond(),
            "Zone: " + gameScreen.getCurrentZoneId(),
            "Seed: " + gameScreen.getWorldSeed(),
            "Input: " + resolveInputContext().name(),
            "Action delay: " + String.format("%.2f", actionDelay),
            "Heap: " + getUsedHeapMegabytes() + " MB / " + getMaxHeapMegabytes() + " MB"
        )));
        sections.add(new DebugOverlay.DebugSection("Field State", List.of(
            "Allies: " + livingAllies().size() + "  Enemies: " + livingEnemies().size(),
            "Mode: " + mode.name() + "  Selected: " + selectedIndex,
            "Active actor: " + (activeActor != null ? activeActor.getName() : "none"),
            "Timeline lead: " + describeTimelineLead()
        )));
        sections.add(new DebugOverlay.DebugSection("Statuses", buildStatusDebugLines()));
        return sections;
    }

    private List<String> buildInspectorDetails() {
        List<String> lines = new ArrayList<>();
        BattleCombatant focus = activeActor != null ? activeActor : firstTimelineCombatant();
        if (focus == null) {
            lines.add("Waiting for initiative.");
            return lines;
        }
        lines.add("Actor: " + focus.getName() + "  HP " + (int) focus.getHealth() + "/" + (int) focus.getMaxHealth());
        lines.add("Class: " + focus.getCombatClass() + "  SPD " + (int) focus.getEffectiveSpeed());
        lines.add("Effects: " + buildCombatantEffectSummary(focus));
        lines.add("Weak: " + buildElementSummary(focus.getWeaknesses()) + "  Res: " + buildElementSummary(focus.getResistances()));
        lines.add("Unique: " + (focus.getUniqueBoosts().isEmpty() ? "none" : String.join(", ", focus.getUniqueBoosts())));
        return lines;
    }

    private List<String> buildRecentBattleLogLines() {
        List<String> lines = new ArrayList<>();
        for (int i = battleLog.size() - 1; i >= 0 && lines.size() < 6; i--) {
            lines.add(battleLog.get(i));
        }
        return lines;
    }

    private String buildCombatantEffectSummary(BattleCombatant combatant) {
        if (combatant == null || combatant.getStatusEffectManager().getActiveEffects().isEmpty()) {
            return "none";
        }
        List<String> names = new ArrayList<>();
        combatant.getStatusEffectManager().getActiveEffects().forEach(effect ->
            names.add(effect.getType().name() + "(" + effect.getRemainingTurns() + ")"));
        return String.join(", ", names);
    }

    private String buildElementSummary(List<Element> elements) {
        if (elements == null || elements.isEmpty()) {
            return "-";
        }
        List<String> names = new ArrayList<>();
        for (Element element : elements) {
            if (element != null && element != Element.NONE) {
                names.add(element.name());
            }
        }
        return names.isEmpty() ? "-" : String.join("/", names);
    }

    private String describeTimelineLead() {
        List<BattleCombatant> turns = battleState.getTurnTimeline().getNextTurns(battleState.getCombatants(), 3);
        if (turns.isEmpty()) {
            return "none";
        }
        List<String> names = new ArrayList<>();
        for (BattleCombatant turn : turns) {
            names.add(turn.getName());
        }
        return String.join(" -> ", names);
    }

    private BattleCombatant firstTimelineCombatant() {
        List<BattleCombatant> turns = battleState.getTurnTimeline().getNextTurns(battleState.getCombatants(), 1);
        return turns.isEmpty() ? null : turns.get(0);
    }

    private long getUsedHeapMegabytes() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
    }

    private long getMaxHeapMegabytes() {
        return Runtime.getRuntime().maxMemory() / (1024L * 1024L);
    }

    private List<String> buildStatusDebugLines() {
        List<String> lines = new ArrayList<>();
        for (BattleCombatant combatant : battleState.getCombatants()) {
            if (combatant == null || !combatant.isAlive()) {
                continue;
            }
            lines.add(combatant.getName() + ": " + buildCombatantEffectSummary(combatant));
            if (lines.size() >= 6) {
                break;
            }
        }
        if (lines.isEmpty()) {
            lines.add("No active status effects.");
        }
        return lines;
    }

    private String join(String[] values) {
        if (values == null || values.length == 0) {
            return "Unknown threat";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(i == values.length - 1 ? " and " : ", ");
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        hudStage.getViewport().update(width, height, true);
        debugOverlay.resize(width, height);
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
        smallFont.dispose();
        hudStage.dispose();
        debugOverlay.dispose();
    }

    public static class Encounter {
        public String[] enemyNames;
        public String[] enemyIds;
        public String[] enemyRanks;
        public String[] enemyAiProfiles;
        public float[] enemyHealth;
        public float[] enemyMaxHealth;
        public float[] enemyAgility;
        public float[] enemyStrength;
        public float[] enemyIntelligence;
        public float[] enemyStamina;
        public String[][] enemyWeaknesses;
        public String[][] enemyResistances;
        public String[][] enemyAbsorbs;
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
        public int[] robotPartySlots;
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
        public int[] robotPartySlots;
        public float[] enemyHealth;
        public int healingPotions;
        public boolean enemyDefeated;
        public boolean enemyEscaped;
        public int[] goldEarned;
        public int[] experienceEarned;
        public Object[] enemyReferences;
        public Map<String, Integer> updatedBestiary;
        public String[] droppedEquipmentIds;
        public String[] droppedEquipmentNames;
        public Map<String, Integer> droppedShards;
        public String[] droppedShardNames;
        public Map<String, Integer> droppedComponents;
        public String[] droppedComponentNames;
    }

    private enum Mode {
        ROOT,
        ATTACK,
        ABILITY,
        TARGET_ENEMY,
        TARGET_ALLY,
        ANALYZE_TARGET
    }

    private static class AttackMove {
        private final String name;
        private final int speedCost;
        private final float multiplier;
        private final Element element;

        private AttackMove(String name, int speedCost, float multiplier, Element element) {
            this.name = name;
            this.speedCost = speedCost;
            this.multiplier = multiplier;
            this.element = element;
        }
    }

    private static class ElementalOutcome {
        int damage;
        float multiplier;
        boolean triggeredBreak;
    }

    private static class ImpactBurst {
        final float x;
        final float y;
        final Color color;
        float life;
        float radius = 14f;
        final float scale;

        ImpactBurst(float x, float y, Color color, float scale) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.life = 0.45f;
            this.scale = scale;
        }
    }
}
