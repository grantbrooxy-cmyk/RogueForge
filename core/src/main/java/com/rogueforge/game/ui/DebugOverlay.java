package com.rogueforge.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lightweight debug overlay toggled with F3.
 */
public class DebugOverlay {
    private final Supplier<List<String>> lineSupplier;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new ScreenViewport(camera);
    private final InputAdapter inputAdapter = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.F3) {
                visible = !visible;
                return true;
            }
            return false;
        }
    };

    private boolean visible;

    public DebugOverlay(Supplier<List<String>> lineSupplier) {
        this.lineSupplier = lineSupplier;
        font.getData().setScale(1.0f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public InputAdapter getInputProcessor() {
        return inputAdapter;
    }

    public boolean isVisible() {
        return visible;
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        if (!visible || lineSupplier == null) {
            return;
        }
        List<String> lines = lineSupplier.get();
        if (lines == null || lines.isEmpty()) {
            return;
        }

        viewport.apply();
        float width = 420f;
        float lineHeight = 18f;
        float height = 18f + (lines.size() * lineHeight);
        float x = 12f;
        float y = viewport.getWorldHeight() - 12f - height;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.04f, 0.07f, 0.82f);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.18f, 0.45f, 0.55f, 0.95f);
        shapes.rect(x, y + height - 26f, width, 26f);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Debug Overlay", x + 10f, y + height - 8f);
        for (int i = 0; i < lines.size(); i++) {
            font.draw(batch, lines.get(i), x + 10f, y + height - 34f - (i * lineHeight));
        }
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
