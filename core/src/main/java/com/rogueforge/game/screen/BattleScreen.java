package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.combat.AbilityDefinition;
import com.rogueforge.game.combat.AbilityInstance;
import com.rogueforge.game.combat.BattleCombatant;
import com.rogueforge.game.combat.BattleResultSummary;
import com.rogueforge.game.combat.BattleState;
import com.rogueforge.game.combat.BestiaryManager;
import com.rogueforge.game.combat.CombatResolver;
import com.rogueforge.game.combat.Element;
import com.rogueforge.game.combat.ElementalSystem;
import com.rogueforge.game.combat.StatusEffectType;
import com.rogueforge.game.combat.WeaponType;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.progression.ProficiencyTracker;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turn-based battle screen that delegates rules to the combat package.
 */
public class BattleScreen implements Screen {
    private static final String[] ROOT_ACTIONS = {"Attack", "Ability", "Item", "Defend", "Analyze", "Flee"};
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
    private final BestiaryManager bestiaryManager = new BestiaryManager();
    private final BattleState battleState;
    private final List<String> battleLog = new ArrayList<>();
    private final Map<String, Integer> abilityXpGains = new LinkedHashMap<>();
    private final Map<String, Integer> weaponXpGains = new LinkedHashMap<>();
    private final List<String> masteryUnlocks = new ArrayList<>();

    private Mode mode = Mode.ROOT;
    private int selectedIndex;
    private int selectedAttackIndex = -1;
    private int selectedAbilityIndex = -1;
    private BattleCombatant activeActor;
    private boolean turnInitialized;
    private boolean closingToResults;
    private float actionDelay;
    private int healingPotions;

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
        this.combatResolver = new CombatResolver(RogueForgeGame.getEventBus());
        this.healingPotions = encounter.healingPotions;
        this.bestiaryManager.importData(gameScreen.getBestiaryScanLevels());
        this.battleState = new BattleState(buildCombatants());
        battleLog.add("Encounter! " + join(encounter.enemyNames));
    }

    private List<BattleCombatant> buildCombatants() {
        List<BattleCombatant> combatants = new ArrayList<>();
        combatants.add(new BattleCombatant(
            "player",
            encounter.playerName,
            true,
            -1,
            "PLAYER",
            "PLAYER",
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
            null
        ));
        if (encounter.robotHealth != null) {
            for (int i = 0; i < encounter.robotHealth.length; i++) {
                int partySlot = encounter.robotPartySlots != null && i < encounter.robotPartySlots.length
                    ? encounter.robotPartySlots[i]
                    : i;
                combatants.add(new BattleCombatant(
                    "ally_" + i,
                    encounter.robotNames[i],
                    true,
                    partySlot,
                    "ALLY",
                    "ALLY",
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
                    null
                ));
            }
        }
        for (int i = 0; i < encounter.enemyNames.length; i++) {
            combatants.add(new BattleCombatant(
                encounter.enemyIds != null && i < encounter.enemyIds.length ? encounter.enemyIds[i] : "enemy_" + i,
                encounter.enemyNames[i],
                false,
                i,
                encounter.enemyRanks != null && i < encounter.enemyRanks.length ? encounter.enemyRanks[i] : "G",
                encounter.enemyAiProfiles != null && i < encounter.enemyAiProfiles.length ? encounter.enemyAiProfiles[i] : "PATROL",
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
                encounter.enemyReferences[i]
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
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        actionDelay = Math.max(0f, actionDelay - delta);
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (!closingToResults) {
            tickBattleFlow();
        }

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        drawPanels(w, h);
        drawCombatants(w, h);
        drawTimeline(w, h);
        drawActionPanel(w, h);
        drawBattleLog(w, h);
        handleInput();
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
            battleLog.add(actor.getName() + " is stunned and loses the turn.");
            endTurn(actor, 100);
        }
    }

    private void handleInput() {
        if (actionDelay > 0f || activeActor == null || !activeActor.isAlive() || !activeActor.isAlly()) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = Math.min(getCurrentOptions().length - 1, selectedIndex + 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            confirmSelection();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            chooseDirect(0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            chooseDirect(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            chooseDirect(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            chooseDirect(3);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            chooseDirect(4);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            chooseDirect(5);
        }
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
                if (ability == null) {
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
            endTurn(actor, move.speedCost);
            return;
        }
        float weaponMultiplier = actor.getPartyIndex() >= 0
            ? gameScreen.getWeaponDamageMultiplier(actor.getPartyIndex(), gameScreen.getEquippedWeaponType(actor.getPartyIndex()))
            : 1f;
        int damage = combatResolver.resolvePhysicalDamage(actor, target, move.multiplier, weaponMultiplier);
        combatResolver.applyDamage(target, damage);
        battleLog.add(actor.getName() + " uses " + move.name + " on " + target.getName() + " for " + damage + " damage.");
        float multiplier = ElementalSystem.getMultiplier(move.element, target);
        if (move.element != Element.NONE && multiplier != 1f) {
            battleLog.add("Elemental hit: " + ElementalSystem.describeHit(multiplier) + ".");
        }
        awardWeaponProgress(actor, gameScreen.getEquippedWeaponType(actor.getPartyIndex()), move.name);
        endTurn(actor, move.speedCost);
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
        endTurn(actor, definition.getSpeedCost());
    }

    private void applyAbilityToTarget(BattleCombatant actor, BattleCombatant target, AbilityInstance ability) {
        AbilityDefinition definition = ability.getDefinition();
        if (target == null) {
            return;
        }
        switch (definition.getType()) {
            case DAMAGE:
                int damage = combatResolver.resolveAbilityDamage(actor, target, definition, ability.getPowerMultiplier());
                combatResolver.applyDamage(target, damage);
                if (damage < 0) {
                    battleLog.add(target.getName() + " absorbs " + definition.getName() + " and restores " + Math.abs(damage) + " HP.");
                } else {
                    battleLog.add(actor.getName() + " casts " + definition.getName() + " on " + target.getName() + " for " + damage + " damage.");
                    if (definition.getAppliedStatus() != null) {
                        target.getStatusEffectManager().apply(definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()));
                    }
                }
                if (definition.getElement() != Element.NONE) {
                    float multiplier = ElementalSystem.getMultiplier(definition.getElement(), target);
                    if (multiplier != 1f) {
                        battleLog.add("Elemental hit: " + ElementalSystem.describeHit(multiplier) + ".");
                    }
                }
                break;
            case HEAL:
                int healAmount = combatResolver.resolveHealing(actor, definition, ability.getPowerMultiplier());
                target.heal(healAmount);
                battleLog.add(actor.getName() + " restores " + healAmount + " HP to " + target.getName() + " with " + definition.getName() + ".");
                break;
            case BUFF:
            case UTILITY:
                if (definition.getAppliedStatus() != null) {
                    target.getStatusEffectManager().apply(definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()));
                }
                battleLog.add(actor.getName() + " uses " + definition.getName() + " on " + target.getName() + ".");
                break;
            case DEBUFF:
                if (definition.getAppliedStatus() != null) {
                    target.getStatusEffectManager().apply(definition.getAppliedStatus(), Math.max(1, definition.getStatusTurns()));
                }
                battleLog.add(actor.getName() + " afflicts " + target.getName() + " with " + definition.getName() + ".");
                break;
            default:
                break;
        }
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
        battleLog.add(actor.getName() + " takes a defensive stance.");
        endTurn(actor, 40);
    }

    private void useItem(BattleCombatant actor) {
        if (healingPotions <= 0) {
            battleLog.add("No repair kits left.");
            return;
        }
        healingPotions--;
        int healAmount = Math.max(10, Math.round(actor.getMaxHealth() * 0.22f));
        actor.heal(healAmount);
        battleLog.add(actor.getName() + " uses a repair kit and restores " + healAmount + " HP.");
        endTurn(actor, 70);
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
        endTurn(actor, 100);
    }

    private void executeEnemyTurn(BattleCombatant actor) {
        BattleCombatant target = chooseEnemyTarget();
        if (target == null) {
            finishBattle(false, false);
            return;
        }
        int speedCost = 80;
        if ("RANGED".equals(actor.getAiProfile()) || "BOSS".equals(actor.getAiProfile())) {
            int damage = combatResolver.resolveAbilityDamage(actor, target, createEnemyAbility());
            combatResolver.applyDamage(target, damage);
            battleLog.add(actor.getName() + " unleashes Mind Lance on " + target.getName() + " for " + Math.abs(damage) + " damage.");
            speedCost = 95;
        } else {
            int damage = combatResolver.resolvePhysicalDamage(actor, target, 1.2f);
            combatResolver.applyDamage(target, damage);
            battleLog.add(actor.getName() + " strikes " + target.getName() + " for " + damage + " damage.");
        }
        endTurn(actor, speedCost);
    }

    private AbilityDefinition createEnemyAbility() {
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
        return definition;
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
        tickCooldowns();
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

    private void tickCooldowns() {
        for (BattleCombatant combatant : battleState.getCombatants()) {
            for (AbilityInstance ability : combatant.getAbilities()) {
                ability.update(1f);
            }
        }
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
        result.droppedEquipmentIds = buildDroppedEquipmentIds(enemyDefeated);
        result.droppedEquipmentNames = resolveDropNames(result.droppedEquipmentIds);

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
            result.droppedEquipmentNames,
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
        }
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

    private String[] buildDroppedEquipmentIds(boolean enemyDefeated) {
        if (!enemyDefeated) {
            return new String[0];
        }
        List<EquipmentItem> catalog = gameScreen.getFullEquipmentCatalog();
        List<String> drops = new ArrayList<>();
        for (BattleCombatant enemy : battleState.getEnemies()) {
            if (enemy.isAlive()) {
                continue;
            }
            if (Math.random() > getDropChance(enemy.getRank())) {
                continue;
            }
            EquipmentItem drop = chooseDropForRank(catalog, enemy.getRank(), drops);
            if (drop != null) {
                drops.add(drop.getId());
            }
        }
        return drops.toArray(new String[0]);
    }

    private float getDropChance(String rank) {
        switch (rank) {
            case "B":
            case "A":
            case "S":
                return 0.45f;
            case "C":
            case "D":
                return 0.3f;
            default:
                return 0.2f;
        }
    }

    private EquipmentItem chooseDropForRank(List<EquipmentItem> catalog, String rank, List<String> existingDrops) {
        int maxTier = maxTierForRank(rank);
        List<EquipmentItem> eligible = new ArrayList<>();
        for (EquipmentItem item : catalog) {
            if (item.getTier() <= maxTier && !existingDrops.contains(item.getId())) {
                eligible.add(item);
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        return eligible.get((int) (Math.random() * eligible.size()));
    }

    private int maxTierForRank(String rank) {
        switch (rank) {
            case "S":
            case "A":
            case "B":
                return 3;
            case "C":
            case "D":
            case "E":
                return 2;
            default:
                return 1;
        }
    }

    private String[] resolveDropNames(String[] ids) {
        String[] names = new String[ids != null ? ids.length : 0];
        for (int i = 0; i < names.length; i++) {
            EquipmentItem item = gameScreen.findEquipmentItem(ids[i]);
            names[i] = item != null ? item.getName() : ids[i];
        }
        return names;
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
        int[] values = new int[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            values[i] = enemyDefeated && !enemies.get(i).isAlive() ? enemies.get(i).getRewardGold() : 0;
        }
        return values;
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

    private void drawTimeline(float w, float h) {
        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Timeline", w - 235f, h - 56f);
        List<BattleCombatant> turns = battleState.getTurnTimeline().getNextTurns(battleState.getCombatants(), 8);
        for (int i = 0; i < turns.size(); i++) {
            BattleCombatant combatant = turns.get(i);
            smallFont.setColor(combatant.isAlly() ? new Color(0.71f, 0.9f, 1f, 1f) : new Color(1f, 0.78f, 0.74f, 1f));
            smallFont.draw(batch, (i + 1) + ". " + combatant.getName(), w - 235f, h - 82f - (i * 18f));
        }
        batch.end();
    }

    private void drawActionPanel(float w, float h) {
        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Commands", 48f, 180f);
        String[] options = getCurrentOptions();
        for (int i = 0; i < options.length; i++) {
            bodyFont.setColor(i == selectedIndex ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            bodyFont.draw(batch, (i + 1) + ". " + options[i], 48f, 148f - (i * 22f));
        }
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "Enter to confirm, Esc to back, Arrow keys to move.", w * 0.38f, 52f);
        batch.end();
    }

    private void drawBattleLog(float w, float h) {
        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Battle Log", w * 0.52f, 180f);
        for (int i = 0; i < battleLog.size(); i++) {
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, battleLog.get(battleLog.size() - 1 - i), w * 0.52f, 148f - (i * 18f));
        }
        batch.end();
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
}
