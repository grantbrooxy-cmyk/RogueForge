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
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight JRPG-style cutscene screen for multi-page dialogue sequences.
 */
public class CutsceneScreen implements Screen {
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final List<Page> pages;
    private final Runnable onComplete;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final BitmapFont speakerFont;
    private final GlyphLayout layout;
    private int currentPageIndex = 0;

    public CutsceneScreen(RogueForgeGame game, ScreenManager screenManager, List<Page> pages, Runnable onComplete) {
        this.game = game;
        this.screenManager = screenManager;
        this.pages = pages != null ? new ArrayList<>(pages) : new ArrayList<>();
        this.onComplete = onComplete;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.2f);
        this.speakerFont = new BitmapFont();
        this.speakerFont.getData().setScale(1.35f);
        this.layout = new GlyphLayout();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        update();

        Gdx.gl.glClearColor(0.02f, 0.03f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        draw();
    }

    private void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            advance();
        }
    }

    private void advance() {
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            return;
        }
        screenManager.pop();
        if (onComplete != null) {
            onComplete.run();
        }
    }

    private void draw() {
        Page page = getCurrentPage();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.02f, 0.03f, 0.06f, 1f);
        shapeRenderer.rect(0f, 0f, camera.viewportWidth, camera.viewportHeight);
        shapeRenderer.setColor(0.09f, 0.12f, 0.18f, 1f);
        shapeRenderer.rect(64f, 64f, camera.viewportWidth - 128f, camera.viewportHeight - 128f);
        shapeRenderer.setColor(0.14f, 0.18f, 0.26f, 1f);
        shapeRenderer.rect(96f, 112f, camera.viewportWidth - 192f, 172f);
        shapeRenderer.setColor(0.04f, 0.05f, 0.09f, 0.96f);
        shapeRenderer.rect(96f, 24f, camera.viewportWidth - 192f, 232f);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        speakerFont.setColor(new Color(0.92f, 0.94f, 0.98f, 1f));
        speakerFont.draw(batch, page.speaker, 126f, 222f);

        font.setColor(Color.WHITE);
        float y = 186f;
        for (String line : wrapTextLines(page.text, camera.viewportWidth - 252f)) {
            font.draw(batch, line, 126f, y);
            y -= 28f;
        }

        font.setColor(new Color(0.78f, 0.84f, 0.95f, 1f));
        font.draw(batch, "E / Enter / Space: Continue", camera.viewportWidth - 360f, 58f);
        font.draw(batch, (currentPageIndex + 1) + " / " + Math.max(1, pages.size()), 126f, 58f);

        batch.end();
    }

    private Page getCurrentPage() {
        if (pages.isEmpty()) {
            return new Page("Cutscene", "...");
        }
        int index = Math.max(0, Math.min(currentPageIndex, pages.size() - 1));
        return pages.get(index);
    }

    private List<String> wrapTextLines(String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            layout.setText(font, candidate);
            if (currentLine.length() > 0 && layout.width > maxWidth) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(word);
            } else {
                currentLine.setLength(0);
                currentLine.append(candidate);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
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
        font.dispose();
        speakerFont.dispose();
    }

    public static class Page {
        public final String speaker;
        public final String text;

        public Page(String speaker, String text) {
            this.speaker = speaker != null ? speaker : "";
            this.text = text != null ? text : "";
        }
    }
}
