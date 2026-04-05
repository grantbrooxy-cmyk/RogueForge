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
import com.badlogic.gdx.utils.Align;
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
    private final BitmapFont cardTitleFont;
    private final BitmapFont buttonFont;
    private final OrthographicCamera camera;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;

    private static final float BTN_W = 240f;
    private static final float BTN_H = 50f;
    private static final float BTN_GAP = 16f;
    private static final float CARD_MAX_W = 280f;
    private static final float CARD_MIN_W = 190f;
    private static final float CARD_H = 170f;
    private static final float CARD_GAP = 18f;
    private static final float CARD_PADDING = 16f;

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

        cardTitleFont = new BitmapFont();
        cardTitleFont.getData().setScale(1.2f);
        cardTitleFont.setColor(Color.WHITE);

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
        CardLayout cardLayout = buildCardLayout(w, startY, totalBtnHeight);

        // Detect hover
        hoveredButton = -1;
        hoveredChoice = -1;
        for (int i = 0; i < offeredChoices.size(); i++) {
            CardSlot slot = cardLayout.slots.get(i);
            if (mx >= slot.x && mx <= slot.x + slot.width && my >= slot.y && my <= slot.y + slot.height) {
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
        shapeRenderer.rect(56f, startY - 150f, w - 112f, totalBtnHeight + 220f + cardLayout.totalHeight);
        for (int i = 0; i < offeredChoices.size(); i++) {
            CardSlot slot = cardLayout.slots.get(i);
            DeathDraftChoice choice = offeredChoices.get(i);
            Color fill = getChoiceFillColor(choice, i == hoveredChoice);
            shapeRenderer.setColor(fill);
            shapeRenderer.rect(slot.x, slot.y, slot.width, slot.height);
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
            statsFont.draw(batch, layout, (w - layout.width) / 2f, cardLayout.topY + cardLayout.totalHeight + 34f);
        } else {
            layout.setText(statsFont, "No draft cards available. Start another run.");
            statsFont.draw(batch, layout, (w - layout.width) / 2f, statsY - 140f);
        }

        for (int i = 0; i < offeredChoices.size(); i++) {
            DeathDraftChoice choice = offeredChoices.get(i);
            CardSlot slot = cardLayout.slots.get(i);
            drawChoiceCardText(choice, slot.x, slot.y, slot.width, slot.height);
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

    private void drawChoiceCardText(DeathDraftChoice choice, float cardX, float cardY, float cardWidth, float cardHeight) {
        if (choice == null) {
            return;
        }
        float innerX = cardX + CARD_PADDING;
        float innerWidth = cardWidth - CARD_PADDING * 2f;
        float topY = cardY + cardHeight - CARD_PADDING;

        GlyphLayout headingLayout = new GlyphLayout();
        cardTitleFont.setColor(Color.WHITE);
        headingLayout.setText(cardTitleFont, getChoiceHeading(choice), Color.WHITE, innerWidth, Align.left, true);
        cardTitleFont.draw(batch, headingLayout, innerX, topY);

        GlyphLayout actionLayout = new GlyphLayout();
        statsFont.setColor(new Color(0.88f, 0.96f, 1f, 1f));
        actionLayout.setText(statsFont, getChoiceActionText(choice), statsFont.getColor(), innerWidth, Align.left, true);
        float actionTopY = cardY + CARD_PADDING + actionLayout.height;
        statsFont.draw(batch, actionLayout, innerX, actionTopY);

        float descriptionTopY = topY - headingLayout.height - 12f;
        float availableDescriptionHeight = descriptionTopY - actionTopY - 10f;
        String description = fitWrappedText(statsFont, choice.getDescription(), innerWidth, availableDescriptionHeight);

        GlyphLayout descriptionLayout = new GlyphLayout();
        statsFont.setColor(new Color(0.9f, 0.95f, 1f, 1f));
        descriptionLayout.setText(statsFont, description, statsFont.getColor(), innerWidth, Align.left, true);
        statsFont.draw(batch, descriptionLayout, innerX, descriptionTopY);
    }

    private String fitWrappedText(BitmapFont font, String text, float width, float maxHeight) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        GlyphLayout measure = new GlyphLayout();
        measure.setText(font, text, Color.WHITE, width, Align.left, true);
        if (measure.height <= maxHeight) {
            return text;
        }

        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String candidate = builder.length() == 0 ? words[i] : builder + " " + words[i];
            measure.setText(font, candidate + "...", Color.WHITE, width, Align.left, true);
            if (measure.height > maxHeight) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(words[i]);
        }
        return builder.length() == 0 ? "..." : builder + "...";
    }

    private CardLayout buildCardLayout(float screenWidth, float startY, float totalBtnHeight) {
        CardLayout layout = new CardLayout();
        layout.topY = startY + totalBtnHeight + 30f;
        if (offeredChoices.isEmpty()) {
            layout.totalHeight = 0f;
            return layout;
        }

        float availableWidth = Math.max(CARD_MIN_W, screenWidth - 112f);
        int maxColumns = Math.max(1, (int) Math.floor((availableWidth + CARD_GAP) / (CARD_MIN_W + CARD_GAP)));
        int columns = Math.min(Math.max(1, offeredChoices.size()), maxColumns);
        float cardWidth = Math.min(CARD_MAX_W, (availableWidth - (columns - 1) * CARD_GAP) / columns);
        if (cardWidth < CARD_MIN_W && columns > 1) {
            columns--;
            cardWidth = Math.min(CARD_MAX_W, (availableWidth - (columns - 1) * CARD_GAP) / columns);
        }
        cardWidth = Math.max(CARD_MIN_W, cardWidth);
        int rows = (int) Math.ceil(offeredChoices.size() / (float) columns);
        layout.totalHeight = rows * CARD_H + Math.max(0, rows - 1) * CARD_GAP;

        for (int index = 0; index < offeredChoices.size(); index++) {
            int row = index / columns;
            int column = index % columns;
            int itemsInRow = Math.min(columns, offeredChoices.size() - row * columns);
            float rowWidth = itemsInRow * cardWidth + Math.max(0, itemsInRow - 1) * CARD_GAP;
            float rowStartX = (screenWidth - rowWidth) / 2f;
            float x = rowStartX + column * (cardWidth + CARD_GAP);
            float y = layout.topY + (rows - 1 - row) * (CARD_H + CARD_GAP);
            layout.slots.add(new CardSlot(x, y, cardWidth, CARD_H));
        }
        return layout;
    }

    private static class CardLayout {
        private final List<CardSlot> slots = new ArrayList<>();
        private float topY;
        private float totalHeight;
    }

    private static class CardSlot {
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        private CardSlot(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
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
        cardTitleFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
    }
}
