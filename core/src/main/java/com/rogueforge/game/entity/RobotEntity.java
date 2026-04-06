package com.rogueforge.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.entity.component.InventoryComponent;
import com.rogueforge.game.entity.component.PositionComponent;
import com.rogueforge.game.entity.component.RobotCoreComponent;
import com.rogueforge.game.entity.component.StatsComponent;
import com.rogueforge.game.robot.RobotDefinition;
import java.util.List;

public class RobotEntity extends GameEntity {
    public enum AIState {
        FOLLOW, ATTACK, RETREAT, IDLE
    }

    private RobotDefinition definition;
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 targetPosition;
    private float currentHp;
    private float speed;
    private AIState currentState;
    private float stateTimer;
    private float attackCooldown;
    private float attackCooldownTimer;
    private static final float FOLLOW_STOP_DISTANCE = 50f;
    private static final float ATTACK_RANGE = 100f;
    private static final float RETREAT_THRESHOLD = 0.3f;
    private final PositionComponent positionComponent = new PositionComponent();
    private final StatsComponent statsComponent = new StatsComponent();
    private final InventoryComponent inventoryComponent = new InventoryComponent();
    private final RobotCoreComponent robotCoreComponent = new RobotCoreComponent();

    public RobotEntity(RobotDefinition definition, Vector2 startPosition) {
        super(definition != null ? definition.getId() : null);
        registerComponent(PositionComponent.class, positionComponent);
        registerComponent(StatsComponent.class, statsComponent);
        registerComponent(InventoryComponent.class, inventoryComponent);
        registerComponent(RobotCoreComponent.class, robotCoreComponent);
        this.definition = definition;
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(0, 0);
        this.targetPosition = new Vector2(startPosition);
        this.currentHp = definition.getBaseHp();
        this.speed = definition.getBaseSpeed();
        this.currentState = AIState.IDLE;
        this.stateTimer = 0f;
        this.attackCooldown = 1.0f; // Default attack cooldown
        this.attackCooldownTimer = 0f;
        syncComponents();
    }

    public void update(float delta, Vector2 playerPos, List<MonsterEntity> monsters) {
        attackCooldownTimer -= delta;
        stateTimer += delta;

        // Determine AI state
        float distToPlayer = position.dst(playerPos);
        float hpPercent = currentHp / definition.getBaseHp();

        // Retreat when low HP
        if (hpPercent < RETREAT_THRESHOLD) {
            currentState = AIState.RETREAT;
        } else if (distToPlayer < ATTACK_RANGE && !monsters.isEmpty()) {
            currentState = AIState.ATTACK;
        } else {
            currentState = AIState.FOLLOW;
        }

        // Execute AI based on state
        switch (currentState) {
            case FOLLOW:
                updateFollow(playerPos, delta);
                break;
            case ATTACK:
                updateAttack(monsters, delta);
                break;
            case RETREAT:
                updateRetreat(playerPos, delta);
                break;
            case IDLE:
                velocity.setZero();
                break;
        }

        // Apply velocity and gravity-like settling
        position.add(velocity.x * delta, velocity.y * delta);

        // Dampen velocity over time
        velocity.scl(0.95f);
        syncComponents();
    }

    private void updateFollow(Vector2 playerPos, float delta) {
        Vector2 dirToPlayer = new Vector2(playerPos).sub(position);
        float distToPlayer = dirToPlayer.len();

        if (distToPlayer > FOLLOW_STOP_DISTANCE) {
            dirToPlayer.nor();
            velocity.set(dirToPlayer).scl(speed);
            targetPosition.set(playerPos);
        } else {
            velocity.setZero();
        }
    }

    private void updateAttack(List<MonsterEntity> monsters, float delta) {
        if (monsters.isEmpty()) {
            currentState = AIState.IDLE;
            return;
        }

        // Find nearest monster
        MonsterEntity nearestMonster = null;
        float nearestDistance = Float.MAX_VALUE;

        for (MonsterEntity monster : monsters) {
            if (monster.isAlive()) {
                float dist = position.dst(monster.getPosition());
                if (dist < nearestDistance) {
                    nearestDistance = dist;
                    nearestMonster = monster;
                }
            }
        }

        if (nearestMonster != null) {
            Vector2 dirToMonster = new Vector2(nearestMonster.getPosition()).sub(position);
            float distToMonster = dirToMonster.len();

            if (distToMonster > ATTACK_RANGE * 0.5f) {
                // Move closer
                dirToMonster.nor();
                velocity.set(dirToMonster).scl(speed);
            } else {
                // Attack
                velocity.scl(0.5f); // Slow down while attacking
                if (attackCooldownTimer <= 0) {
                    nearestMonster.takeDamage(definition.getBaseAttack());
                    attackCooldownTimer = attackCooldown;
                }
            }
        }
    }

    private void updateRetreat(Vector2 playerPos, float delta) {
        Vector2 dirAwayFromPlayer = new Vector2(position).sub(playerPos);
        if (dirAwayFromPlayer.len() > 0) {
            dirAwayFromPlayer.nor();
            velocity.set(dirAwayFromPlayer).scl(speed * 0.8f);
        }
    }

    public void render(SpriteBatch batch) {
        // Render colored rectangle as placeholder robot
        batch.end();
        com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        // Color based on state
        switch (currentState) {
            case ATTACK:
                shapeRenderer.setColor(1, 0, 0, 1); // Red
                break;
            case RETREAT:
                shapeRenderer.setColor(1, 1, 0, 1); // Yellow
                break;
            case FOLLOW:
                shapeRenderer.setColor(0, 0, 1, 1); // Blue
                break;
            default:
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1); // Gray
        }

        shapeRenderer.rect(position.x, position.y, 24, 24);
        shapeRenderer.end();
        batch.begin();
    }

    public void takeDamage(float damage) {
        currentHp -= damage;
        currentHp = Math.max(0, currentHp);
        syncComponents();
    }

    public PositionComponent position() {
        syncComponents();
        return positionComponent;
    }

    public StatsComponent stats() {
        syncComponents();
        return statsComponent;
    }

    public InventoryComponent inventory() {
        return inventoryComponent;
    }

    public RobotCoreComponent robotCore() {
        syncComponents();
        return robotCoreComponent;
    }

    private void syncComponents() {
        positionComponent.position = position;
        positionComponent.velocity = velocity;
        statsComponent.currentHealth = currentHp;
        statsComponent.maxHealth = definition != null ? definition.getBaseHp() : currentHp;
        statsComponent.speed = speed;
        statsComponent.attack = definition != null ? definition.getBaseAttack() : 0f;
        statsComponent.defense = definition != null ? definition.getBaseDefense() : 0f;
        robotCoreComponent.robotId = definition != null ? definition.getId() : null;
        robotCoreComponent.robotName = definition != null ? definition.getName() : null;
        robotCoreComponent.role = definition != null && definition.getRole() != null ? definition.getRole().name() : null;
    }

    public float getCurrentHp() {
        return currentHp;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 newPosition) {
        position.set(newPosition);
    }

    public AIState getCurrentState() {
        return currentState;
    }

    public RobotDefinition getDefinition() {
        return definition;
    }
}
