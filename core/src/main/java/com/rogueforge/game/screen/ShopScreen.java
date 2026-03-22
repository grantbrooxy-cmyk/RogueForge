package com.rogueforge.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rogueforge.game.core.RogueForgeGame;
import com.rogueforge.game.core.ScreenManager;
import com.rogueforge.game.data.EquipmentItem;
import com.rogueforge.game.economy.ShopInventory;
import java.util.List;

/**
 * Dedicated shop UI with buy and sell tabs.
 */
public class ShopScreen implements Screen {
    private static final String[] TABS = {"Buy", "Sell"};
    private static final float BACK_W = 140f;
    private static final float BACK_H = 42f;

    private final RogueForgeGame game;
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final String shopName;
    private final ShopInventory inventory;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont titleFont;
    private final BitmapFont bodyFont;
    private final GlyphLayout layout;
    private final Texture backgroundTexture;
    private final Texture uiTexture;

    private int currentTab = 0;
    private int hoveredTab = -1;
    private int hoveredRow = -1;
    private boolean hoveredBack;
    private String statusMessage = "Welcome.";

    public ShopScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen, String shopName, ShopInventory inventory) {
        this.game = game;
        this.screenManager = screenManager;
        this.gameScreen = gameScreen;
        this.shopName = shopName;
        this.inventory = inventory;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.bodyFont = new BitmapFont();
        this.layout = new GlyphLayout();
        this.backgroundTexture = loadTexture("Backgrounds/background 2/orig_big.png");
        this.uiTexture = createUiTexture();
        this.titleFont.getData().setScale(2.4f);
        this.bodyFont.getData().setScale(1.1f);
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
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        handleInput();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera.setToOrtho(false, w, h);

        Gdx.gl.glClearColor(0.07f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, w, h);
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(uiTexture, 20f, 20f, w - 40f, h - 40f);
        batch.setColor(Color.WHITE);
        titleFont.setColor(1f, 0.88f, 0.55f, 1f);
        titleFont.draw(batch, shopName, 48f, h - 42f);
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, "Gold: " + gameScreen.getGold() + "   Repair Kits: " + gameScreen.getHealingPotions(), 52f, h - 86f);
        batch.end();

        drawTabs(w, h);
        drawRows(w, h);
        drawFooter(w, h);
    }

    private void handleInput() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();

        hoveredTab = -1;
        hoveredRow = -1;
        hoveredBack = isBackButtonHit(mx, my, w, h);

        for (int i = 0; i < TABS.length; i++) {
            float tx = 48f + i * 150f;
            if (mx >= tx && mx <= tx + 130f && my >= h - 148f && my <= h - 104f) {
                hoveredTab = i;
            }
        }

        int rowCount = getCurrentRowCount();
        for (int i = 0; i < rowCount; i++) {
            float rowY = h - 210f - (i * 54f);
            if (mx >= 48f && mx <= w - 48f && my >= rowY && my <= rowY + 44f) {
                hoveredRow = i;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            closeMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            currentTab = (currentTab + 1) % TABS.length;
            return;
        }

        if (Gdx.input.justTouched()) {
            if (hoveredBack) {
                closeMenu();
                return;
            }
            if (hoveredTab >= 0) {
                currentTab = hoveredTab;
                return;
            }
            if (hoveredRow >= 0) {
                activateRow(hoveredRow);
                return;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) activateRow(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) activateRow(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) activateRow(2);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) activateRow(3);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) activateRow(4);
    }

    private void activateRow(int index) {
        if (currentTab == 0) {
            ShopInventory.ShopEntry entry = inventory.getItem(index);
            if (entry != null) {
                statusMessage = gameScreen.buyShopEntry(entry);
            }
            return;
        }
        List<EquipmentItem> sellable = gameScreen.getSellableEquipment();
        if (index >= 0 && index < sellable.size()) {
            statusMessage = gameScreen.sellEquipment(sellable.get(index).getId());
        }
    }

    private int getCurrentRowCount() {
        return currentTab == 0 ? inventory.getItemCount() : gameScreen.getSellableEquipment().size();
    }

    private void drawTabs(float w, float h) {
        batch.begin();
        for (int i = 0; i < TABS.length; i++) {
            float tx = 48f + i * 150f;
            batch.setColor(i == currentTab ? new Color(0.72f, 0.46f, 0.2f, 1f)
                : (i == hoveredTab ? new Color(0.3f, 0.34f, 0.46f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f)));
            batch.draw(uiTexture, tx, h - 148f, 130f, 44f);
            batch.setColor(Color.WHITE);
            layout.setText(bodyFont, TABS[i]);
            bodyFont.draw(batch, TABS[i], tx + (130f - layout.width) / 2f, h - 118f);
        }
        float backX = backButtonX(w);
        float backY = h - 148f;
        batch.setColor(hoveredBack ? new Color(0.68f, 0.28f, 0.24f, 1f) : new Color(0.32f, 0.18f, 0.18f, 1f));
        batch.draw(uiTexture, backX, backY, BACK_W, BACK_H);
        batch.setColor(Color.WHITE);
        layout.setText(bodyFont, "Back");
        bodyFont.draw(batch, "Back", backX + (BACK_W - layout.width) / 2f, h - 118f);
        batch.end();
    }

    private void drawRows(float w, float h) {
        batch.begin();
        if (currentTab == 0) {
            for (int i = 0; i < inventory.getItemCount(); i++) {
                ShopInventory.ShopEntry entry = inventory.getItem(i);
                float rowY = h - 210f - (i * 54f);
                drawRowBackground(w, rowY, i == hoveredRow);
                String label = (i + 1) + ". " + entry.getLabel() + " - " + entry.getCost() + "g";
                bodyFont.draw(batch, label, 60f, rowY + 30f);
                if (entry.getEquipment() != null) {
                    EquipmentItem item = entry.getEquipment();
                    bodyFont.draw(batch, compactStatLine(item), 440f, rowY + 30f);
                }
            }
        } else {
            List<EquipmentItem> sellable = gameScreen.getSellableEquipment();
            for (int i = 0; i < sellable.size(); i++) {
                EquipmentItem item = sellable.get(i);
                float rowY = h - 210f - (i * 54f);
                drawRowBackground(w, rowY, i == hoveredRow);
                bodyFont.draw(batch, (i + 1) + ". " + item.getName() + " - " + gameScreen.getSellPrice(item) + "g", 60f, rowY + 30f);
                bodyFont.draw(batch, compactStatLine(item), 440f, rowY + 30f);
            }
            if (sellable.isEmpty()) {
                bodyFont.draw(batch, "No unequipped gear available to sell.", 60f, h - 210f + 30f);
            }
        }
        batch.end();
    }

    private void drawRowBackground(float w, float rowY, boolean hovered) {
        batch.setColor(hovered ? new Color(0.3f, 0.34f, 0.46f, 1f) : new Color(0.18f, 0.2f, 0.28f, 1f));
        batch.draw(uiTexture, 48f, rowY, w - 96f, 44f);
        batch.setColor(Color.WHITE);
    }

    private String compactStatLine(EquipmentItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getHpBonus() != 0) sb.append("HP ").append(item.getHpBonus()).append("  ");
        if (item.getAttackBonus() != 0) sb.append("STR ").append(item.getAttackBonus()).append("  ");
        if (item.getDefenseBonus() != 0) sb.append("STA ").append(item.getDefenseBonus()).append("  ");
        if (item.getSpeedBonus() != 0) sb.append("AGI ").append(item.getSpeedBonus()).append("  ");
        if (item.getIntelligenceBonus() != 0) sb.append("INT ").append(item.getIntelligenceBonus()).append("  ");
        sb.append("Grade ").append(item.getGradeRequirement());
        return sb.toString();
    }

    private void drawFooter(float w, float h) {
        batch.begin();
        bodyFont.setColor(Color.WHITE);
        bodyFont.draw(batch, statusMessage, 52f, 74f);
        bodyFont.draw(batch, "Tab: switch buy/sell   Esc: back", w - 290f, 74f);
        batch.end();
    }

    private float backButtonX(float width) {
        return width - 48f - BACK_W;
    }

    private boolean isBackButtonHit(float mx, float my, float width, float height) {
        float x = backButtonX(width);
        float y = height - 148f;
        return mx >= x && mx <= x + BACK_W && my >= y && my <= y + BACK_H;
    }

    private void closeMenu() {
        screenManager.pop();
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
        backgroundTexture.dispose();
        uiTexture.dispose();
    }
}
