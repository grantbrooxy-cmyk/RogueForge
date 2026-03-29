package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.MetaProgressionState;
import com.rogueforge.game.engine.meta.CyberneticEnhancementEngine;
import com.rogueforge.game.engine.meta.DeathDraftChoice;
import com.rogueforge.game.engine.meta.DeathDraftResult;
import com.rogueforge.game.persistence.MetaProgressionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Game Over screen displayed when the player dies.
 * Shows final score/stats and options to retry or return to main menu.
 * Rendered using ShapeRenderer for buttons and BitmapFont for text.
 */
public class GameOverScreen implements Screen {
    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont statsFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;

    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;
    private static final float BTN_GAP = 16f;
    private static final float CARD_W = 280f;
    private static final float CARD_H = 120f;
    private static final float CARD_GAP = 18f;

    // Game over stats
    private int finalScore = 0;
    private int totalEnemiesDefeated = 0;
    private float survivalTime = 0f;

    private int hoveredButton = -1;
    private int hoveredChoice = -1;
    private final MetaProgressionState metaProgressionState;
    private final MetaProgressionManager metaProgressionManager;
    private final CyberneticEnhancementEngine cyberneticEnhancementEngine;
    private final DeathDraftResult deathDraft;
    private final List<DeathDraftChoice> offeredChoices;

    public GameOverScreen(
        RogueForgeGame game,
        ScreenManager screenManager,
        MetaProgressionState metaProgressionState,
        DeathDraftResult deathDraft,
        MetaProgressionManager metaProgressionManager,
        CyberneticEnhancementEngine cyberneticEnhancementEngine
    ) {
        this.game = game;
        this.screenManager = screenManager;
        this.metaProgressionState = metaProgressionState;
        this.deathDraft = deathDraft;
        this.offeredChoices = deathDraft != null ? new ArrayList<>(deathDraft.getChoices()) : new ArrayList<>();
        this.metaProgressionManager = metaProgressionManager != null ? metaProgressionManager : new MetaProgressionManager();
        this.cyberneticEnhancementEngine = cyberneticEnhancementEngine != null ? cyberneticEnhancementEngine : new CyberneticEnhancementEngine();
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture("Backgrounds/background 3/orig_big.png");

        // Setup fonts
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
        titleFont.setColor(Color.WHITE);

        statsFont = new BitmapFont();
        statsFont.getData().setScale(1.5f);
        statsFont.setColor(Color.WHITE);

        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        buttonFont.setColor(Color.WHITE);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    /**
     * Set the final game stats to display.
     */
    public void setGameStats(int score, int enemiesDefeated, float survivalTime) {
        this.finalScore = score;
        this.totalEnemiesDefeated = enemiesDefeated;
        this.survivalTime = survivalTime;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(0.04f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Mouse position (flip Y for our camera)
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        // Button layout — centered, stacked
        String[] buttonLabels = getButtonLabels();
        float totalBtnHeight = buttonLabels.length * BTN_H + (buttonLabels.length - 1) * BTN_GAP;
        float startY = (h - totalBtnHeight) / 2f - 80f;

        // Detect hover
        hoveredButton = -1;
        hoveredChoice = -1;
        float cardsWidth = offeredChoices.size() * CARD_W + Math.max(0, offeredChoices.size() - 1) * CARD_GAP;
        float cardsStartX = (w - cardsWidth) / 2f;
        float cardsY = startY + totalBtnHeight + 30f;
        for (int i = 0; i < offeredChoices.size(); i++) {
            float cx = cardsStartX + i * (CARD_W + CARD_GAP);
            if (mx >= cx && mx <= cx + CARD_W && my >= cardsY && my <= cardsY + CARD_H) {
                hoveredChoice = i;
            }
        }
        for (int i = 0; i < buttonLabels.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (buttonLabels.length - 1 - i) * (BTN_H + BTN_GAP);
            if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H) {
                hoveredButton = i;
            }
        }

        // Handle click
        if (Gdx.input.justTouched()) {
            if (hoveredChoice >= 0) {
                onEnhancementChosen(hoveredChoice);
                return;
            }
            if (hoveredButton >= 0) {
                onButtonClicked(hoveredButton);
                return;
            }
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.end();

        // Draw buttons
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.04f, 0.04f, 0.07f, 0.72f);
        shapeRenderer.rect(56f, startY - 150f, w - 112f, totalBtnHeight + 360f);
        for (int i = 0; i < offeredChoices.size(); i++) {
            float cx = cardsStartX + i * (CARD_W + CARD_GAP);
            DeathDraftChoice choice = offeredChoices.get(i);
            Color fill = getChoiceFillColor(choice, i == hoveredChoice);
            shapeRenderer.setColor(fill);
            shapeRenderer.rect(cx, cardsY, CARD_W, CARD_H);
        }
        for (int i = 0; i < buttonLabels.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (buttonLabels.length - 1 - i) * (BTN_H + BTN_GAP);
            if (i == hoveredButton) {
                shapeRenderer.setColor(0.9f, 0.45f, 0.1f, 0.9f); // orange hover
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.28f, 0.85f); // dark idle
            }
            shapeRenderer.rect(bx, by, BTN_W, BTN_H);
        }
        shapeRenderer.end();

        // Draw text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title
        layout.setText(titleFont, "GAME OVER");
        titleFont.setColor(1f, 0.2f, 0.2f, 1f); // red title
        titleFont.draw(batch, "GAME OVER", (w - layout.width) / 2f, startY + totalBtnHeight + 100f);

        // Stats display
        float statsY = startY + totalBtnHeight;
        statsFont.setColor(Color.WHITE);
        statsFont.draw(batch, "Score: " + finalScore, 100f, statsY);
        statsFont.draw(batch, "Enemies: " + totalEnemiesDefeated, 100f, statsY - 50f);
        statsFont.draw(batch, "Time: " + String.format("%.1f", survivalTime) + "s", 100f, statsY - 100f);
        if (!offeredChoices.isEmpty()) {
            String headline = deathDraft != null && deathDraft.getSummary() != null ? deathDraft.getSummary() : "Choose your next card";
            layout.setText(statsFont, headline);
            statsFont.draw(batch, layout, (w - layout.width) / 2f, cardsY + CARD_H + 34f);
        } else {
            layout.setText(statsFont, "No draft cards available. Start another run.");
            statsFont.draw(batch, layout, (w - layout.width) / 2f, statsY - 140f);
        }

        for (int i = 0; i < offeredChoices.size(); i++) {
            DeathDraftChoice choice = offeredChoices.get(i);
            float cx = cardsStartX + i * (CARD_W + CARD_GAP);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, getChoiceHeading(choice), cx + 16f, cardsY + CARD_H - 20f);
            statsFont.setColor(new Color(0.9f, 0.95f, 1f, 1f));
            statsFont.draw(batch, wrap(choice.getDescription(), 34), cx + 16f, cardsY + CARD_H - 58f);
            statsFont.setColor(new Color(0.88f, 0.96f, 1f, 1f));
            statsFont.draw(batch, getChoiceActionText(choice), cx + 16f, cardsY + 20f);
        }

        // Button labels
        for (int i = 0; i < buttonLabels.length; i++) {
            float bx = (w - BTN_W) / 2f;
            float by = startY + (buttonLabels.length - 1 - i) * (BTN_H + BTN_GAP);
            layout.setText(buttonFont, buttonLabels[i]);
            buttonFont.setColor(Color.WHITE);
            buttonFont.draw(batch, buttonLabels[i],
                bx + (BTN_W - layout.width) / 2f,
                by + (BTN_H + layout.height) / 2f);
        }

        batch.end();
    }

    private String wrap(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text != null ? text : "";
        }
        StringBuilder builder = new StringBuilder();
        int lineLength = 0;
        for (String word : text.split(" ")) {
            if (lineLength > 0 && lineLength + word.length() + 1 > maxChars) {
                builder.append('\n');
                lineLength = 0;
            } else if (lineLength > 0) {
                builder.append(' ');
                lineLength++;
            }
            builder.append(word);
            lineLength += word.length();
        }
        return builder.toString();
    }

    private void onEnhancementChosen(int index) {
        if (index < 0 || index >= offeredChoices.size()) {
            return;
        }
        DeathDraftChoice choice = offeredChoices.get(index);
        if (choice != null && metaProgressionState != null) {
            cyberneticEnhancementEngine.applyDraftChoice(metaProgressionState, choice);
            metaProgressionManager.save(metaProgressionState);
        }
        screenManager.replace(new GameScreen(game, screenManager));
    }

    private Color getChoiceFillColor(DeathDraftChoice choice, boolean hovered) {
        float alpha = hovered ? 0.95f : 0.88f;
        if (choice == null) {
            return new Color(0.12f, 0.34f, 0.54f, alpha);
        }
        switch (choice.getKind()) {
            case CURSE:
                return new Color(0.52f, 0.16f, 0.16f, alpha);
            case CLEAR_CURSE:
                return new Color(0.15f, 0.46f, 0.34f, alpha);
            case ENHANCEMENT:
            default:
                return new Color(0.12f, 0.34f, 0.54f, alpha);
        }
    }

    private String getChoiceHeading(DeathDraftChoice choice) {
        if (choice == null) {
            return "";
        }
        switch (choice.getKind()) {
            case CURSE:
                return "Curse: " + choice.getName();
            case CLEAR_CURSE:
                return "Purge: " + choice.getName();
            case ENHANCEMENT:
            default:
                return "Augment: " + choice.getName();
        }
    }

    private String getChoiceActionText(DeathDraftChoice choice) {
        if (choice == null) {
            return "";
        }
        switch (choice.getKind()) {
            case CURSE:
                return "Click to accept the burden";
            case CLEAR_CURSE:
                return "Click to cleanse this curse";
            case ENHANCEMENT:
            default:
                return "Click to install and reforge";
        }
    }

    private void onButtonClicked(int index) {
        String[] buttonLabels = getButtonLabels();
        if (index < 0 || index >= buttonLabels.length) {
            return;
        }
        switch (index) {
            case 0:
                if (offeredChoices.isEmpty()) {
                    screenManager.replace(new GameScreen(game, screenManager));
                    break;
                }
                screenManager.replace(new MainMenuScreen(game, screenManager));
                break;
            case 1:
                screenManager.replace(new MainMenuScreen(game, screenManager));
                break;
        }
    }

    private String[] getButtonLabels() {
        return offeredChoices.isEmpty()
            ? new String[] {"Try Again", "Main Menu"}
            : new String[] {"Main Menu"};
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        titleFont.dispose();
        statsFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
    }
}
