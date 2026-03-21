package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.combat.BattleResultSummary;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;

/**
 * Results screen shown after a victory before returning to the overworld.
 */
public class BattleResultsScreen implements Screen {
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final BattleScreen.BattleResult result;
    private final BattleResultSummary summary;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final OrthographicCamera camera;
    private boolean closed;

    public BattleResultsScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen,
                               BattleScreen.BattleResult result, BattleResultSummary summary) {
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.result = result;
        this.summary = summary;
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.titleFont.getData().setScale(2.2f);
        this.bodyFont.getData().setScale(1.2f);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if (!closed && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.justTouched())) {
            closed = true;
            screenManager.pop();
            gameScreen.resolveBattle(result);
            return;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        Gdx.gl.glClearColor(0.06f, 0.07f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.13f, 0.18f, 1f);
        shapeRenderer.rect(w * 0.18f, h * 0.18f, w * 0.64f, h * 0.64f);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        titleFont.setColor(1f, 0.88f, 0.56f, 1f);
        titleFont.draw(batch, "Victory", w * 0.38f, h * 0.72f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Gold earned: " + summary.getGoldEarned(), w * 0.28f, h * 0.60f);
        bodyFont.draw(batch, "Experience earned: " + summary.getExperienceEarned(), w * 0.28f, h * 0.53f);
        bodyFont.draw(batch, "Projected level ups: " + summary.getLevelUps(), w * 0.28f, h * 0.46f);
        bodyFont.draw(batch, "Bestiary updates: " + summary.getBestiaryUpdates(), w * 0.28f, h * 0.39f);
        String[] drops = summary.getDrops();
        if (drops.length > 0) {
            bodyFont.draw(batch, "Equipment drops:", w * 0.28f, h * 0.32f);
            for (int i = 0; i < drops.length; i++) {
                bodyFont.draw(batch, "- " + drops[i], w * 0.31f, h * 0.27f - (i * 0.04f * h));
            }
        } else {
            bodyFont.draw(batch, "Equipment drops: none", w * 0.28f, h * 0.32f);
        }
        drawSection("Ability growth", summary.getAbilityProgress(), w * 0.52f, h * 0.60f);
        drawSection("Weapon growth", summary.getWeaponProgress(), w * 0.52f, h * 0.45f);
        drawSection("Mastery unlocks", summary.getMasteryUnlocks(), w * 0.52f, h * 0.30f);
        drawSection("Robot progress", summary.getRobotProgress(), w * 0.28f, h * 0.12f);
        bodyFont.draw(batch, "Press Enter or click to return to the frontier.", w * 0.28f, h * 0.18f);
        batch.end();
    }

    private void drawSection(String title, String[] lines, float x, float startY) {
        bodyFont.draw(batch, title + ":", x, startY);
        if (lines == null || lines.length == 0) {
            bodyFont.draw(batch, "None", x + 20f, startY - 26f);
            return;
        }
        for (int i = 0; i < lines.length && i < 4; i++) {
            bodyFont.draw(batch, "- " + lines[i], x + 12f, startY - 26f - (i * 24f));
        }
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
    }
}
