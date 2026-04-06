package com.rogueforge.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.core.EventBus;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.entity.component.PositionComponent;
import com.rogueforge.game.entity.component.StatsComponent;
import com.rogueforge.game.event.LootDropEvent;

public class MonsterEntity extends GameEntity {
    public enum AIProfile {
        PATROL, AGGRO, RANGED, BOSS
    }

    private MonsterDefinition definition;
    private AIProfile aiProfile;
    private Vector2 position;
    private Vector2 velocity;
    private float currentHp;
    private float speed;
    private float stateTimer;
    private Vector2 patrolTarget;
    private static final float AGGRO_RANGE = 150f;
    private static final float RANGED_ATTACK_RANGE = 200f;
    private static final float RANGED_MAINTAIN_DISTANCE = 120f;
    private static final float PATROL_WAYPOINT_DISTANCE = 30f;
    private float attackCooldown;
    private float attackCooldownTimer;
    private boolean hasDroppedLoot;
    private EventBus eventBus;
    private final PositionComponent positionComponent = new PositionComponent();
    private final StatsComponent statsComponent = new StatsComponent();

    public MonsterEntity(MonsterDefinition definition, Vector2 startPosition) {
        this(definition, startPosition, null);
    }

    public MonsterEntity(MonsterDefinition definition, Vector2 startPosition, EventBus eventBus) {
        super(definition != null ? definition.getId() : null);
        registerComponent(PositionComponent.class, positionComponent);
        registerComponent(StatsComponent.class, statsComponent);
        this.definition = definition;
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(0, 0);
        this.currentHp = definition.getHp();
        this.speed = definition.getSpeed();
        this.aiProfile = AIProfile.valueOf(definition.getAiProfile());
        this.stateTimer = 0f;
        this.patrolTarget = generatePatrolTarget();
        this.attackCooldown = 1.0f; // Default attack cooldown
        this.attackCooldownTimer = 0f;
        this.hasDroppedLoot = false;
        this.eventBus = eventBus;
        syncComponents();
    }

    public void update(float delta, Vector2 playerPos) {
        attackCooldownTimer -= delta;
        stateTimer += delta;

        float distToPlayer = position.dst(playerPos);

        switch (aiProfile) {
            case PATROL:
                updatePatrol(delta);
                break;
            case AGGRO:
                updateAggro(playerPos, distToPlayer, delta);
                break;
            case RANGED:
                updateRanged(playerPos, distToPlayer, delta);
                break;
            case BOSS:
                updateBoss(playerPos, distToPlayer, delta);
                break;
        }

        // Apply velocity
        position.add(velocity.x * delta, velocity.y * delta);

        // Dampen velocity
        velocity.scl(0.95f);
        syncComponents();
    }

    private void updatePatrol(float delta) {
        Vector2 dirToWaypoint = new Vector2(patrolTarget).sub(position);
        float distToWaypoint = dirToWaypoint.len();

        if (distToWaypoint < PATROL_WAYPOINT_DISTANCE) {
            patrolTarget = generatePatrolTarget();
        } else {
            dirToWaypoint.nor();
            velocity.set(dirToWaypoint).scl(speed);
        }
    }

    private void updateAggro(Vector2 playerPos, float distToPlayer, float delta) {
        if (distToPlayer < AGGRO_RANGE) {
            // Chase player
            Vector2 dirToPlayer = new Vector2(playerPos).sub(position);
            dirToPlayer.nor();
            velocity.set(dirToPlayer).scl(speed);

            // Attack if close enough (default range 50f)
            if (distToPlayer < 50f && attackCooldownTimer <= 0) {
                // Fire attack event or handle directly
                attackCooldownTimer = attackCooldown;
            }
        } else {
            // Return to patrol
            updatePatrol(delta);
        }
    }

    private void updateRanged(Vector2 playerPos, float distToPlayer, float delta) {
        if (distToPlayer < RANGED_ATTACK_RANGE) {
            Vector2 dirToPlayer = new Vector2(playerPos).sub(position);
            float distToPlayerLen = dirToPlayer.len();

            if (distToPlayerLen > RANGED_MAINTAIN_DISTANCE) {
                // Move closer
                dirToPlayer.nor();
                velocity.set(dirToPlayer).scl(speed);
            } else if (distToPlayerLen < RANGED_MAINTAIN_DISTANCE * 0.7f) {
                // Back away
                dirToPlayer.nor();
                velocity.set(dirToPlayer).scl(-speed * 0.6f);
            } else {
                // Maintain distance
                velocity.scl(0.5f);
            }

            // Attack from range
            if (attackCooldownTimer <= 0) {
                attackCooldownTimer = attackCooldown;
            }
        } else {
            // Patrol when out of range
            updatePatrol(delta);
        }
    }

    private void updateBoss(Vector2 playerPos, float distToPlayer, float delta) {
        // Boss combines features: more aggressive, ranged attacks, special patterns
        Vector2 dirToPlayer = new Vector2(playerPos).sub(position);
        float distToPlayerLen = dirToPlayer.len();

        if (distToPlayerLen < RANGED_ATTACK_RANGE * 1.5f) {
            // Aggressive chase with pattern variations
            if (stateTimer % 2f < 1f) {
                // Chase phase
                dirToPlayer.nor();
                velocity.set(dirToPlayer).scl(speed * 1.2f);
            } else {
                // Strafe phase
                Vector2 strafeDir = new Vector2(-dirToPlayer.y, dirToPlayer.x).nor();
                velocity.set(strafeDir).scl(speed);
            }

            // More frequent attacks
            if (attackCooldownTimer <= 0) {
                attackCooldownTimer = attackCooldown * 0.7f;
            }
        } else {
            updatePatrol(delta);
        }
    }

    private Vector2 generatePatrolTarget() {
        float angle = MathUtils.random(360f);
        float distance = MathUtils.random(50f, 200f);
        float x = position.x + MathUtils.cos(MathUtils.degreesToRadians * angle) * distance;
        float y = position.y + MathUtils.sin(MathUtils.degreesToRadians * angle) * distance;
        return new Vector2(x, y);
    }

    public void render(SpriteBatch batch) {
        // Render colored rectangle as placeholder monster
        batch.end();
        com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        // Color based on AI profile
        switch (aiProfile) {
            case AGGRO:
                shapeRenderer.setColor(1, 0, 0, 1); // Red
                break;
            case RANGED:
                shapeRenderer.setColor(1, 0.5f, 0, 1); // Orange
                break;
            case BOSS:
                shapeRenderer.setColor(0.5f, 0, 0.5f, 1); // Purple
                break;
            default:
                shapeRenderer.setColor(0.5f, 0.2f, 0, 1); // Brown
        }

        shapeRenderer.rect(position.x, position.y, 28, 28);
        shapeRenderer.end();
        batch.begin();
    }

    public void takeDamage(float damage) {
        currentHp -= damage;
        currentHp = Math.max(0, currentHp);

        if (!isAlive() && !hasDroppedLoot) {
            dropLoot();
        }
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

    private void syncComponents() {
        positionComponent.position = position;
        positionComponent.velocity = velocity;
        statsComponent.currentHealth = currentHp;
        statsComponent.maxHealth = definition != null ? definition.getHp() : currentHp;
        statsComponent.speed = speed;
        statsComponent.attack = definition != null ? definition.getAttack() : 0f;
        statsComponent.defense = definition != null ? definition.getDefense() : 0f;
    }

    private void dropLoot() {
        hasDroppedLoot = true;
        if (eventBus != null) {
            eventBus.fire(new LootDropEvent(definition, position));
        }
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

    public float getCurrentHp() {
        return currentHp;
    }

    public String getRank() {
        return definition.getRank();
    }

    public MonsterDefinition getDefinition() {
        return definition;
    }

    public AIProfile getAIProfile() {
        return aiProfile;
    }
}
