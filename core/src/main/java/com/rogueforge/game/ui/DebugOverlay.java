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
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Lightweight debug overlay toggled with F3.
 */
public class DebugOverlay {
    private final Supplier<List<DebugSection>> sectionSupplier;
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
        this.sectionSupplier = () -> wrapLines(lineSupplier);
        font.getData().setScale(1.0f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public DebugOverlay(Supplier<List<DebugSection>> sectionSupplier, boolean sectioned) {
        this.sectionSupplier = sectionSupplier;
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
        if (!visible || sectionSupplier == null) {
            return;
        }
        List<DebugSection> sections = sectionSupplier.get();
        if (sections == null || sections.isEmpty()) {
            return;
        }

        viewport.apply();
        float width = 468f;
        float lineHeight = 18f;
        float height = 18f;
        for (DebugSection section : sections) {
            if (section == null) {
                continue;
            }
            height += 24f;
            height += (section.lines.size() * lineHeight);
            height += 8f;
        }
        float x = 12f;
        float y = viewport.getWorldHeight() - 12f - height;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.05f, 0.09f, 0.12f, 0.9f);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.76f, 0.55f, 0.19f, 0.9f);
        shapes.rect(x, y + height - 6f, width, 6f);
        shapes.setColor(0.13f, 0.24f, 0.28f, 0.98f);
        shapes.rect(x + 8f, y + height - 34f, width - 16f, 24f);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "Debug Overlay", x + 14f, y + height - 16f);
        float cursorY = y + height - 48f;
        for (DebugSection section : sections) {
            if (section == null || (section.title == null && section.lines.isEmpty())) {
                continue;
            }
            font.setColor(new Color(0.99f, 0.86f, 0.57f, 1f));
            font.draw(batch, section.title != null ? section.title : "Section", x + 14f, cursorY);
            cursorY -= 18f;
            font.setColor(Color.WHITE);
            for (String line : section.lines) {
                font.draw(batch, line, x + 20f, cursorY);
                cursorY -= lineHeight;
            }
            cursorY -= 8f;
        }
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }

    private static List<DebugSection> wrapLines(Supplier<List<String>> lineSupplier) {
        if (lineSupplier == null) {
            return Collections.emptyList();
        }
        List<String> lines = lineSupplier.get();
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<DebugSection> sections = new ArrayList<>();
        sections.add(new DebugSection("Overview", lines));
        return sections;
    }

    public static final class DebugSection {
        public final String title;
        public final List<String> lines;

        public DebugSection(String title, List<String> lines) {
            this.title = title;
            this.lines = lines != null ? lines : Collections.emptyList();
        }
    }
}
