package com.rogueforge.game.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.rogueforge.game.data.MonsterDefinition;
import com.rogueforge.game.data.ZoneDefinition;
import com.rogueforge.game.entity.MonsterEntity;
import java.util.ArrayList;
import java.util.List;

public class EntitySpawner {
    private List<MonsterEntity> activeMonsters;
    private float respawnCooldown;
    private float respawnTimer;
    private final float spawnRadius;
    private Vector2 spawnCenter;
    private int monstersPerWave;

    public EntitySpawner(float spawnRadius) {
        this.activeMonsters = new ArrayList<>();
        this.spawnRadius = spawnRadius;
        this.spawnCenter = new Vector2(0, 0);
        this.respawnCooldown = 5f; // 5 seconds between waves
        this.respawnTimer = respawnCooldown;
        this.monstersPerWave = 3;
    }

    public void spawnWave(ZoneDefinition zone) {
        if (zone == null) {
            return;
        }

        String minRank = zone.getRankFloor();
        String maxRank = zone.getRankCeiling();

        for (int i = 0; i < monstersPerWave; i++) {
            // Spawn a placeholder monster (simplified - no dynamic rank-based selection)
            // TODO: Implement proper monster selection from zone's monsterIds
            Vector2 spawnPos = getRandomSpawnPosition();
            // MonsterDefinition monsterDef = zone.getMonsterForRank(MathUtils.random(minRank, maxRank));
            // if (monsterDef != null) {
            //     MonsterEntity monster = new MonsterEntity(monsterDef, spawnPos);
            //     activeMonsters.add(monster);
            // }
        }
    }

    private Vector2 getRandomSpawnPosition() {
        float angle = MathUtils.random(360f);
        float distance = MathUtils.random(0f, spawnRadius);
        float x = spawnCenter.x + MathUtils.cos(MathUtils.degreesToRadians * angle) * distance;
        float y = spawnCenter.y + MathUtils.sin(MathUtils.degreesToRadians * angle) * distance;
        return new Vector2(x, y);
    }

    public void setSpawnCenter(Vector2 center) {
        this.spawnCenter.set(center);
    }

    public void setMonstersPerWave(int count) {
        this.monstersPerWave = Math.max(1, count);
    }

    public void setRespawnCooldown(float seconds) {
        this.respawnCooldown = seconds;
        this.respawnTimer = seconds;
    }

    public void update(float delta) {
        respawnTimer -= delta;

        // Remove dead monsters
        activeMonsters.removeIf(monster -> !monster.isAlive());

        // Respawn wave if timer elapsed
        if (respawnTimer <= 0) {
            respawnTimer = respawnCooldown;
            // Spawning is handled by zone manager calling spawnWave
        }
    }

    public List<MonsterEntity> getActiveMonsters() {
        return activeMonsters;
    }

    public void clearMonsters() {
        activeMonsters.clear();
        respawnTimer = respawnCooldown;
    }

    public int getMonsterCount() {
        return activeMonsters.size();
    }
}
