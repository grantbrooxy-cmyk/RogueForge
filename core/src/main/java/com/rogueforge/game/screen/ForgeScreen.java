package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.ForgeRecipeDefinition;
import java.util.List;

/**
 * Dedicated town forge screen for crafting weapons and robot parts.
 */
public class ForgeScreen implements Screen {
    private static final float EXIT_W = 140f;
    private static final float EXIT_H = 42f;

    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont buttonFont;
    private final GlyphLayout layout;
    private final OrthographicCamera camera;
    private final Texture backgroundTexture;
    private final Texture uiTexture;

    private int hoveredRecipe = -1;
    private int hoveredShard = -1;
    private int hoveredComponent = -1;
    private boolean hoveredExit;
    private String statusMessage;
    private float closeInputBlockTimer;

    public ForgeScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen) {
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.batch = new SpriteBatch();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.buttonFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backgroundTexture = loadTexture("Backgrounds/background 2/orig_big.png");
        this.uiTexture = createUiTexture();
        this.titleFont.getData().setScale(3f);
        this.bodyFont.getData().setScale(1.2f);
        this.buttonFont.getData().setScale(1.15f);
    }

    private Texture loadTexture(String relativePath) {
        Texture texture = new Texture(Gdx.files.internal(relativePath));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    private Texture createUiTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture texture = new Texture(pm);
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        pm.dispose();
        return texture;
    }

    @Override
    public void show() {
        closeInputBlockTimer = 0.18f;
    }

    @Override
    public void render(float delta) {
        handleInput();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();
        hoveredExit = isExitButtonHit(mx, my, w, h);

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0.09f, 0.09f, 0.12f, 0.92f);
        batch.draw(uiTexture, 28f, 24f, w - 56f, h - 48f);
        batch.setColor(Color.WHITE);
        drawHeader(w, h);
        drawForgeContent(w, h, mx, my);
        batch.end();
    }

    private void handleInput() {
        closeInputBlockTimer = Math.max(0f, closeInputBlockTimer - Gdx.graphics.getDeltaTime());
        if (closeInputBlockTimer <= 0f
            && (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.I))) {
            close();
            return;
        }

        if (!Gdx.input.justTouched()) {
            return;
        }

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        if (isExitButtonHit(mx, my, w, h)) {
            close();
            return;
        }

        List<ForgeRecipeDefinition> recipes = gameScreen.getForgeRecipes();
        String[] sellableGrades = visibleShardGrades();
        String[] sellableComponents = gameScreen.getForgeSellableComponentIds().toArray(new String[0]);
        for (int i = 0; i < recipes.size(); i++) {
            float cardX = recipeCardX();
            float cardY = recipeCardY(h, i);
            float cardW = recipeCardWidth(w);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 78f) {
                statusMessage = gameScreen.forgeRecipe(i);
                return;
            }
        }
        for (int i = 0; i < sellableGrades.length; i++) {
            float lineY = shardLineY(h, i);
            if (mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                statusMessage = gameScreen.sellShard(sellableGrades[i]);
                return;
            }
        }
        for (int i = 0; i < sellableComponents.length; i++) {
            float lineY = componentLineY(h, i);
            if (mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                statusMessage = gameScreen.sellForgeComponent(sellableComponents[i]);
                return;
            }
        }
    }

    private void drawHeader(float w, float h) {
        float exitX = exitButtonX(w);
        float exitY = h - 140f;
        batch.setColor(hoveredExit ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.32f, 0.18f, 0.18f, 1f));
        batch.draw(uiTexture, exitX, exitY, EXIT_W, EXIT_H);
        batch.setColor(Color.WHITE);
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, "TOWN FORGE", 48f, h - 52f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Toma can rebuild enemy salvage into weapons and frame parts.", 48f, h - 86f);
        layout.setText(buttonFont, "Exit");
        buttonFont.draw(batch, "Exit", exitX + (EXIT_W - layout.width) / 2f, h - 112f);
    }

    private void drawForgeContent(float w, float h, float mx, float my) {
        List<String> shardLines = gameScreen.getShardInventoryLines();
        String[] sellableGrades = visibleShardGrades();
        List<String> componentLines = gameScreen.getForgeInventoryLines();
        String[] sellableComponents = gameScreen.getForgeSellableComponentIds().toArray(new String[0]);
        List<ForgeRecipeDefinition> recipes = gameScreen.getForgeRecipes();
        hoveredRecipe = -1;
        hoveredShard = -1;
        hoveredComponent = -1;

        float leftX = 56f;
        float topY = h - 190f;

        batch.setColor(new Color(0.12f, 0.14f, 0.2f, 0.96f));
        batch.draw(uiTexture, 44f, 80f, 300f, h - 260f);
        batch.draw(uiTexture, 370f, 80f, w - 414f, h - 260f);
        batch.setColor(Color.WHITE);

        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Shard Stock", leftX, topY);
        bodyFont.draw(batch, "Gold: " + gameScreen.getGold(), leftX, topY - 30f);
        for (int i = 0; i < shardLines.size() && i < 8; i++) {
            float lineY = shardLineY(h, i);
            if (i < sellableGrades.length && mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                hoveredShard = i;
            }
            bodyFont.setColor(i == hoveredShard ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            bodyFont.draw(batch, shardLines.get(i), leftX, lineY);
        }
        bodyFont.setColor(Color.WHITE);
        float componentTitleY = topY - 280f;
        bodyFont.draw(batch, "Forge Components", leftX, componentTitleY);
        for (int i = 0; i < componentLines.size() && i < 8; i++) {
            float lineY = componentLineY(h, i);
            if (i < sellableComponents.length && mx >= 56f && mx <= 328f && my >= lineY - 18f && my <= lineY + 6f) {
                hoveredComponent = i;
            }
            bodyFont.setColor(i == hoveredComponent ? new Color(1f, 0.9f, 0.55f, 1f) : Color.WHITE);
            bodyFont.draw(batch, componentLines.get(i), leftX, lineY);
        }
        bodyFont.setColor(Color.WHITE);

        float recipeX = 392f;
        bodyFont.draw(batch, "Forge Patterns", recipeX, topY);
        if (statusMessage != null) {
            bodyFont.setColor(1f, 0.9f, 0.55f, 1f);
            bodyFont.draw(batch, statusMessage, recipeX, topY - 30f);
            bodyFont.setColor(Color.WHITE);
        } else {
            bodyFont.setColor(Color.LIGHT_GRAY);
            bodyFont.draw(batch, "Select a pattern to craft it instantly.", recipeX, topY - 30f);
            bodyFont.setColor(Color.WHITE);
        }

        for (int i = 0; i < recipes.size(); i++) {
            ForgeRecipeDefinition recipe = recipes.get(i);
            float cardX = recipeCardX();
            float cardY = recipeCardY(h, i);
            float cardW = recipeCardWidth(w);
            if (mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + 78f) {
                hoveredRecipe = i;
            }
            boolean craftable = gameScreen.canForgeRecipe(recipe);
            batch.setColor(i == hoveredRecipe
                ? new Color(0.3f, 0.34f, 0.46f, 1f)
                : (craftable ? new Color(0.2f, 0.28f, 0.24f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f)));
            batch.draw(uiTexture, cardX, cardY, cardW, 78f);
            batch.setColor(Color.WHITE);
            bodyFont.draw(batch, recipe.getName() + "  [" + recipe.getCategory() + "]", cardX + 12f, cardY + 58f);
            bodyFont.draw(batch, recipe.getDescription(), cardX + 12f, cardY + 38f);
            bodyFont.draw(batch, gameScreen.buildForgeRequirementLine(recipe)
                + (craftable ? "  READY" : "  LOCKED"), cardX + 12f, cardY + 16f);
        }
    }

    private void close() {
        screenManager.pop();
    }

    private float exitButtonX(float width) {
        return width - 48f - EXIT_W;
    }

    private boolean isExitButtonHit(float mx, float my, float width, float height) {
        float x = exitButtonX(width);
        float y = height - 140f;
        return mx >= x && mx <= x + EXIT_W && my >= y && my <= y + EXIT_H;
    }

    private float recipeCardWidth(float w) {
        return w - 438f;
    }

    private float recipeCardX() {
        return 392f;
    }

    private float recipeCardY(float h, int index) {
        return h - 320f - (index * 86f);
    }

    private float shardLineY(float h, int index) {
        return h - 262f - (index * 24f);
    }

    private float componentLineY(float h, int index) {
        return h - 542f - (index * 24f);
    }

    private String[] visibleShardGrades() {
        String[] ordered = {"S", "A", "B", "C", "D", "E", "F", "G"};
        java.util.List<String> visible = new java.util.ArrayList<>();
        for (String grade : ordered) {
            if (gameScreen.getShardCount(grade) > 0) {
                visible.add(grade);
            }
        }
        return visible.toArray(new String[0]);
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
        titleFont.dispose();
        bodyFont.dispose();
        buttonFont.dispose();
        backgroundTexture.dispose();
        uiTexture.dispose();
    }
}
