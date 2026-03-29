package com.rogueforge.game.engine.world;

/**
 * Shared seeded terrain sampler for expansive frontier zones so generation,
 * rendering, and future building rules all agree on the same terrain classes.
 */
public class FrontierTerrainSampler {

    public enum TerrainType {
        MEADOW,
        GROVE,
        MARSH,
        SCRUB,
        RUIN_FIELD,
        STONE_FLATS
    }

    public static class TerrainSample {
        public final TerrainType type;
        public final float macro;
        public final float moisture;
        public final float ruggedness;

        public TerrainSample(TerrainType type, float macro, float moisture, float ruggedness) {
            this.type = type;
            this.macro = macro;
            this.moisture = moisture;
            this.ruggedness = ruggedness;
        }
    }

    private final long worldSeed;

    public FrontierTerrainSampler(long worldSeed) {
        this.worldSeed = worldSeed != 0L ? worldSeed : 1L;
    }

    public TerrainSample sample(int tileX, int tileY) {
        float macro = layeredNoise(tileX, tileY, 0.012f, 0.034f, 0.09f, 0);
        float moisture = layeredNoise(tileX + 913, tileY - 417, 0.016f, 0.048f, 0.11f, 19);
        float ruggedness = layeredNoise(tileX - 271, tileY + 641, 0.02f, 0.06f, 0.16f, 41);
        return new TerrainSample(classify(macro, moisture, ruggedness), macro, moisture, ruggedness);
    }

    public TerrainSample sampleWorld(float worldX, float worldY, int tileWidth, int tileHeight) {
        int tileX = Math.max(0, (int) Math.floor(worldX / Math.max(1, tileWidth)));
        int tileY = Math.max(0, (int) Math.floor(worldY / Math.max(1, tileHeight)));
        return sample(tileX, tileY);
    }

    public boolean isPreferredResourceTerrain(String resourceId, TerrainType terrainType) {
        if (resourceId == null || terrainType == null) {
            return false;
        }
        switch (resourceId) {
            case "scrap_alloy":
                return terrainType == TerrainType.RUIN_FIELD || terrainType == TerrainType.STONE_FLATS;
            case "slime_resin":
                return terrainType == TerrainType.MARSH || terrainType == TerrainType.GROVE;
            case "bone_fiber":
                return terrainType == TerrainType.MEADOW || terrainType == TerrainType.GROVE;
            default:
                return false;
        }
    }

    public boolean isBuildFriendly(TerrainType terrainType) {
        return terrainType == TerrainType.MEADOW
            || terrainType == TerrainType.SCRUB
            || terrainType == TerrainType.STONE_FLATS;
    }

    public boolean isGoodEnemyTerrain(TerrainType terrainType) {
        return terrainType == TerrainType.SCRUB
            || terrainType == TerrainType.RUIN_FIELD
            || terrainType == TerrainType.STONE_FLATS
            || terrainType == TerrainType.GROVE;
    }

    private TerrainType classify(float macro, float moisture, float ruggedness) {
        if (moisture > 0.5f && macro < 0.05f) {
            return TerrainType.MARSH;
        }
        if (macro > 0.42f || ruggedness > 0.52f) {
            return moisture < 0.1f ? TerrainType.STONE_FLATS : TerrainType.RUIN_FIELD;
        }
        if (macro < -0.34f) {
            return moisture > 0.1f ? TerrainType.GROVE : TerrainType.SCRUB;
        }
        if (moisture < -0.34f) {
            return TerrainType.SCRUB;
        }
        if (moisture > 0.24f) {
            return TerrainType.GROVE;
        }
        return TerrainType.MEADOW;
    }

    private float layeredNoise(int gx, int gy, float scaleA, float scaleB, float scaleC, int saltOffset) {
        float x = gx + 0.5f;
        float y = gy + 0.5f;
        return sampleNoise(x * scaleA, y * scaleA, saltOffset) * 0.55f
            + sampleNoise(x * scaleB, y * scaleB, saltOffset + 1) * 0.3f
            + sampleNoise(x * scaleC, y * scaleC, saltOffset + 2) * 0.15f;
    }

    private float sampleNoise(float x, float y, int salt) {
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = x0 + 1;
        int y1 = y0 + 1;
        float tx = x - x0;
        float ty = y - y0;

        float n00 = noiseHash(x0, y0, salt);
        float n10 = noiseHash(x1, y0, salt);
        float n01 = noiseHash(x0, y1, salt);
        float n11 = noiseHash(x1, y1, salt);

        float sx = tx * tx * (3f - 2f * tx);
        float sy = ty * ty * (3f - 2f * ty);
        float ix0 = n00 + (n10 - n00) * sx;
        float ix1 = n01 + (n11 - n01) * sx;
        return ix0 + (ix1 - ix0) * sy;
    }

    private float noiseHash(int x, int y, int salt) {
        long n = worldSeed;
        n ^= 0x9E3779B97F4A7C15L * (x + 0x100000L);
        n ^= 0xC2B2AE3D27D4EB4FL * (y + 0x200000L);
        n ^= 0x165667B19E3779F9L * (salt + 1L);
        n ^= (n >>> 33);
        n *= 0xff51afd7ed558ccdL;
        n ^= (n >>> 33);
        n *= 0xc4ceb9fe1a85ec53L;
        n ^= (n >>> 33);
        return ((n & 0xFFFFFFL) / (float) 0x7FFFFF) - 1f;
    }
}
