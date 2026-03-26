package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * HUD overlay rendered on top of the game screen.
 * Draws health bar, currency, and robot status using ShapeRenderer + BitmapFont (no skin needed).
 */
public class HUDOverlay {
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Texture panelTexture;
    private final Texture barTexture;
    private final Texture iconTexture;

    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private long currency = 0;
    private int playerExperience = 0;
    private int nextLevelExperience = 100;
    private String zoneName = "Verdant Fields";
    private String objectiveText = "Speak with Mira in Verdant Fields.";
    private float displayedHealth = 100f;
    private float displayedExperience = 0f;
    private float displayedGold = 0f;
    private float[] robotHealth = new float[0];
    private float[] robotMaxHealth = new float[0];

    public HUDOverlay() {
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.4f);
        this.camera = new OrthographicCamera();
        this.viewport = new ScreenViewport(camera);
        this.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.panelTexture = loadTexture("4 GUI/1 Frames/Interface windows.png");
        this.barTexture = loadTexture("4 GUI/2 Bars/HealthBar1.png");
        this.iconTexture = loadTexture("4 GUI/7 Numbers/plus.png");
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    public void render() {
        viewport.apply();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        animateDisplayedValues();

        float barX = 16f;
        float panelY = h - 168f;
        float barW = 240f;
        float barH = 16f;
        float hpY = h - 40f;
        float expY = h - 68f;
        float goldY = h - 96f;
        float hpPct = Math.max(0f, Math.min(1f, displayedHealth / Math.max(1f, playerMaxHealth)));
        float expPct = Math.max(0f, Math.min(1f, displayedExperience / Math.max(1f, nextLevelExperience)));
        float goldWindow = Math.max(100f, (float) (Math.ceil(Math.max(100d, currency) / 100d) * 100d));
        float goldPct = Math.max(0f, Math.min(1f, displayedGold / goldWindow));

        Gdx.gl.glEnable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(panelTexture, 8f, panelY, 460f, 160f);
        batch.draw(iconTexture, barX + 4f, hpY - 8f, 18f, 18f);
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawBarBack(barX, hpY, barW, barH);
        drawBarBack(barX, expY, barW, barH);
        drawBarBack(barX, goldY, barW, barH);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(hpPct > 0.25f ? new Color(0.52f, 0.95f, 0.58f, 1f) : new Color(1f, 0.45f, 0.45f, 1f));
        batch.draw(barTexture, barX, hpY, barW * hpPct, barH);
        batch.setColor(new Color(0.48f, 0.74f, 1f, 1f));
        batch.draw(barTexture, barX, expY, barW * expPct, barH);
        batch.setColor(new Color(1f, 0.82f, 0.3f, 1f));
        batch.draw(barTexture, barX, goldY, barW * goldPct, barH);
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP  " + (int) displayedHealth + "/" + (int) playerMaxHealth, barX + 8f, hpY + 14f);
        font.draw(batch, "EXP  " + (int) displayedExperience + "/" + nextLevelExperience, barX + 8f, expY + 14f);
        font.draw(batch, "Gold  " + (long) displayedGold, barX + 8f, goldY + 14f);
        for (int i = 0; i < robotHealth.length; i++) {
            String robotStatus = robotHealth[i] > 0
                ? "Bot " + (i + 1) + ": " + (int) robotHealth[i] + "/" + (int) robotMaxHealth[i]
                : "Bot " + (i + 1) + ": DOWN";
            font.draw(batch, robotStatus, 290f, h - 40f - (i * 22f));
        }
        font.draw(batch, zoneName, w - 200, h - 18);
        font.draw(batch, objectiveText, 18f, 28f);
        batch.end();
    }

    private void animateDisplayedValues() {
        float delta = Gdx.graphics.getDeltaTime();
        displayedHealth = moveTowards(displayedHealth, playerHealth, delta * Math.max(30f, playerMaxHealth));
        displayedExperience = moveTowards(displayedExperience, playerExperience, delta * Math.max(60f, nextLevelExperience));
        displayedGold = moveTowards(displayedGold, currency, delta * Math.max(120f, Math.max(100f, currency)));
    }

    private float moveTowards(float current, float target, float amount) {
        if (current < target) {
            return Math.min(target, current + amount);
        }
        return Math.max(target, current - amount);
    }

    private void drawBarBack(float x, float y, float width, float height) {
        shapeRenderer.setColor(0.1f, 0.1f, 0.16f, 0.9f);
        shapeRenderer.rect(x, y, width, height);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void setPlayerHealth(float current, float max) {
        this.playerHealth = current;
        this.playerMaxHealth = max;
    }

    public void setCurrency(long amount) { this.currency = amount; }
    public void setExperience(int current, int nextLevel) {
        this.playerExperience = Math.max(0, current);
        this.nextLevelExperience = Math.max(1, nextLevel);
    }
    public void setZoneName(String name) { this.zoneName = name; }
    public void setObjectiveText(String objectiveText) {
        this.objectiveText = objectiveText != null ? objectiveText : "";
    }

    public void setRobotHealth(float[] current, float[] max) {
        this.robotHealth = current != null ? current.clone() : new float[0];
        this.robotMaxHealth = max != null ? max.clone() : new float[0];
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        panelTexture.dispose();
        barTexture.dispose();
        iconTexture.dispose();
    }
}
