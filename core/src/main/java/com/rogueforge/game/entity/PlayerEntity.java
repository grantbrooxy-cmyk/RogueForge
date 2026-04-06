package com.rogueforge.game.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.entity.component.InventoryComponent;
import com.rogueforge.game.entity.component.PositionComponent;
import com.rogueforge.game.entity.component.StatsComponent;

public class PlayerEntity extends GameEntity {
    public enum AnimationState {
        IDLE, WALK, ATTACK, HIT, DEAD
    }

    private Vector2 position;
    private Vector2 velocity;
    private float speed;
    private float maxHealth;
    private float currentHealth;
    private AnimationState currentState;
    private AnimationState previousState;
    private float stateTimer;
    private TextureRegion currentFrame;
    private final PositionComponent positionComponent = new PositionComponent();
    private final StatsComponent statsComponent = new StatsComponent();
    private final InventoryComponent inventoryComponent = new InventoryComponent();

    public PlayerEntity(Vector2 startPosition) {
        super("player");
        registerComponent(PositionComponent.class, positionComponent);
        registerComponent(StatsComponent.class, statsComponent);
        registerComponent(InventoryComponent.class, inventoryComponent);
        this.position = new Vector2(startPosition);
        this.velocity = new Vector2(0, 0);
        this.speed = 200f;
        this.maxHealth = 100f;
        this.currentHealth = maxHealth;
        this.currentState = AnimationState.IDLE;
        this.previousState = AnimationState.IDLE;
        this.stateTimer = 0f;
        syncComponents();
    }

    public void handleInput() {
        velocity.setZero();
        boolean isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y += speed;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y -= speed;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x -= speed;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x += speed;
            isMoving = true;
        }

        // Normalize diagonal movement
        if (velocity.len() > 0) {
            velocity.nor().scl(speed);
        }

        // Update animation state based on input
        if (currentState != AnimationState.ATTACK && currentState != AnimationState.HIT && currentState != AnimationState.DEAD) {
            if (isMoving) {
                setAnimationState(AnimationState.WALK);
            } else {
                setAnimationState(AnimationState.IDLE);
            }
        }

        // Handle attack input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            setAnimationState(AnimationState.ATTACK);
        }
    }

    public void update(float delta) {
        handleInput();

        // Apply velocity
        position.add(velocity.x * delta, velocity.y * delta);

        // Update animation state timer
        stateTimer += delta;

        // Reset attack state
        if (currentState == AnimationState.ATTACK && stateTimer > 0.3f) {
            if (velocity.len() > 0) {
                setAnimationState(AnimationState.WALK);
            } else {
                setAnimationState(AnimationState.IDLE);
            }
        }

        // Reset hit state
        if (currentState == AnimationState.HIT && stateTimer > 0.2f) {
            if (velocity.len() > 0) {
                setAnimationState(AnimationState.WALK);
            } else {
                setAnimationState(AnimationState.IDLE);
            }
        }
        syncComponents();
    }

    public void render(SpriteBatch batch) {
        // Render placeholder sprite
        if (currentFrame != null) {
            batch.draw(currentFrame, position.x, position.y, 32, 32);
        } else {
            // Render colored rectangle as placeholder
            batch.end();
            com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
            shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(position.x, position.y, 32, 32);
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void setAnimationState(AnimationState newState) {
        if (currentState != newState) {
            previousState = currentState;
            currentState = newState;
            stateTimer = 0f;
        }
    }

    public void takeDamage(float damage) {
        currentHealth -= damage;
        currentHealth = Math.max(0, currentHealth);
        if (currentState != AnimationState.DEAD) {
            setAnimationState(AnimationState.HIT);
        }
        if (currentHealth <= 0) {
            setAnimationState(AnimationState.DEAD);
        }
        syncComponents();
    }

    public void heal(float amount) {
        currentHealth += amount;
        currentHealth = Math.min(maxHealth, currentHealth);
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

    private void syncComponents() {
        positionComponent.position = position;
        positionComponent.velocity = velocity;
        statsComponent.currentHealth = currentHealth;
        statsComponent.maxHealth = maxHealth;
        statsComponent.speed = speed;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 newPosition) {
        position.set(newPosition);
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getHealthPercent() {
        return currentHealth / maxHealth;
    }

    public AnimationState getAnimationState() {
        return currentState;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
