package com.rogueforge.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.entity.component.ProficiencyComponent;
import com.rogueforge.game.entity.component.TransformComponent;

/**
 * Lightweight settlement/world NPC entity backed by shared components.
 */
public class NpcEntity extends GameEntity {
    public final String id;
    public String name;
    public String dialog;
    public Vector2 pos;
    public Vector2 spawnPos;
    public Vector2 homePosition;
    public Vector2 morningPosition;
    public Vector2 dayPosition;
    public Vector2 eveningPosition;
    public Vector2 nightPosition;
    public String morningActivity = "Opening up";
    public String dayActivity = "On duty";
    public String eveningActivity = "Winding down";
    public String nightActivity = "Resting";

    private final TransformComponent transform = new TransformComponent();
    private final ProficiencyComponent proficiency = new ProficiencyComponent();

    public NpcEntity(String entityId, String name, Vector2 position, String dialog) {
        super(entityId);
        this.id = entityId;
        addComponent(TransformComponent.class, transform);
        addComponent(ProficiencyComponent.class, proficiency);
        this.name = name;
        this.dialog = dialog;
        this.pos = position != null ? new Vector2(position) : new Vector2();
        this.spawnPos = new Vector2(this.pos);
        this.homePosition = new Vector2(this.pos);
        this.morningPosition = new Vector2(this.pos);
        this.dayPosition = new Vector2(this.pos);
        this.eveningPosition = new Vector2(this.pos);
        this.nightPosition = new Vector2(this.pos);
        syncComponents();
    }

    public void setPosition(Vector2 position) {
        pos.set(position);
        syncComponents();
    }

    public void applyFloatingOriginShift(Vector2 shift) {
        pos.sub(shift);
        spawnPos.sub(shift);
        homePosition.sub(shift);
        morningPosition.sub(shift);
        dayPosition.sub(shift);
        eveningPosition.sub(shift);
        nightPosition.sub(shift);
        transform.applyFloatingOriginShift(shift.x, shift.y);
        syncComponents();
    }

    public void setSchedule(Vector2 homePosition, Vector2 morningPosition, Vector2 dayPosition,
                            Vector2 eveningPosition, Vector2 nightPosition,
                            String morningActivity, String dayActivity,
                            String eveningActivity, String nightActivity) {
        this.homePosition = homePosition != null ? new Vector2(homePosition) : new Vector2(pos);
        this.morningPosition = morningPosition != null ? new Vector2(morningPosition) : new Vector2(this.homePosition);
        this.dayPosition = dayPosition != null ? new Vector2(dayPosition) : new Vector2(pos);
        this.eveningPosition = eveningPosition != null ? new Vector2(eveningPosition) : new Vector2(pos);
        this.nightPosition = nightPosition != null ? new Vector2(nightPosition) : new Vector2(this.homePosition);
        if (morningActivity != null && !morningActivity.isEmpty()) {
            this.morningActivity = morningActivity;
        }
        if (dayActivity != null && !dayActivity.isEmpty()) {
            this.dayActivity = dayActivity;
        }
        if (eveningActivity != null && !eveningActivity.isEmpty()) {
            this.eveningActivity = eveningActivity;
        }
        if (nightActivity != null && !nightActivity.isEmpty()) {
            this.nightActivity = nightActivity;
        }
        syncComponents();
    }

    public Vector2 getScheduledPosition(float timeOfDayHours) {
        if (timeOfDayHours < 6f) {
            return nightPosition;
        }
        if (timeOfDayHours < 9f) {
            return morningPosition;
        }
        if (timeOfDayHours < 17f) {
            return dayPosition;
        }
        if (timeOfDayHours < 20f) {
            return eveningPosition;
        }
        return nightPosition;
    }

    public String getCurrentActivity(float timeOfDayHours) {
        if (timeOfDayHours < 6f) {
            return nightActivity;
        }
        if (timeOfDayHours < 9f) {
            return morningActivity;
        }
        if (timeOfDayHours < 17f) {
            return dayActivity;
        }
        if (timeOfDayHours < 20f) {
            return eveningActivity;
        }
        return nightActivity;
    }

    public TransformComponent transform() {
        syncComponents();
        return transform;
    }

    public ProficiencyComponent proficiency() {
        return proficiency;
    }

    private void syncComponents() {
        transform.position.set(pos);
        transform.velocity.setZero();
    }
}
