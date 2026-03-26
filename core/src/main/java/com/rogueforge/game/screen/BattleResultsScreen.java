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
 *
 * <p>Two-column layout with a shared robot-progress footer:
 * <pre>
 *  ┌────────────────────────────────────────────────┐
 *  │                  * VICTORY *                   │
 *  ├─────────────────────┬──────────────────────────┤
 *  │ REWARDS             │ PROFICIENCY GAINS         │
 *  │  Gold: N G          │  - Rust: Scan +10 XP      │
 *  │  Experience: N      │  - Rust: SWORD +10 XP     │
 *  │  Level ups: N       ├──────────────────────────┤
 *  │  Bestiary: N new    │ UNLOCKS                   │
 *  ├─────────────────────┤  - Cleave (Tier 1)  [gold]│
 *  │ DROPS               │  - Scan reached Lv.4      │
 *  │  - Iron Shard x2    │                           │
 *  ├─────────────────────┴──────────────────────────┤
 *  │ ROBOT PROGRESS                                  │
 *  │  - Rust +1 level(s)                             │
 *  ├────────────────────────────────────────────────┤
 *  │        Press ENTER or click to return           │
 *  └────────────────────────────────────────────────┘
 * </pre>
 */
public class BattleResultsScreen implements Screen {

    // ── Color palette ──────────────────────────────────────────────────────
    private static final Color COL_TITLE    = new Color(1.00f, 0.90f, 0.30f, 1f); // bright gold
    private static final Color COL_HEADER   = new Color(1.00f, 0.80f, 0.48f, 1f); // warm amber
    private static final Color COL_BODY     = new Color(0.92f, 0.92f, 0.92f, 1f); // off-white
    private static final Color COL_UNLOCK   = new Color(1.00f, 0.90f, 0.20f, 1f); // yellow-gold
    private static final Color COL_ROBOT    = new Color(0.52f, 0.85f, 1.00f, 1f); // sky blue
    private static final Color COL_HINT     = new Color(0.60f, 0.60f, 0.60f, 1f); // gray
    private static final Color COL_PANEL    = new Color(0.09f, 0.10f, 0.16f, 1f); // dark panel
    private static final Color COL_DIVIDER  = new Color(0.22f, 0.25f, 0.36f, 1f); // muted blue-gray

    // ── Layout constants (fractions of screen dimensions) ─────────────────
    private static final float PANEL_X      = 0.12f;
    private static final float PANEL_Y      = 0.06f;
    private static final float PANEL_W      = 0.76f;
    private static final float PANEL_H      = 0.88f;
    private static final float COL_SPLIT    = 0.50f; // vertical divider X position

    // Y boundaries (from bottom)
    private static final float Y_TITLE      = 0.91f;
    private static final float Y_TOP_DIV    = 0.84f; // below title
    private static final float Y_MID_DIV    = 0.30f; // above robot footer
    private static final float Y_BOT_DIV    = 0.13f; // above hint
    private static final float Y_REWARDS    = 0.82f;
    private static final float Y_DROPS_DIV  = 0.54f; // within left column
    private static final float Y_PROF_DIV   = 0.54f; // within right column
    private static final float Y_DROPS_HDR  = 0.52f;
    private static final float Y_PROF_HDR   = 0.82f;
    private static final float Y_UNLOCK_HDR = 0.52f;
    private static final float Y_ROBOT_HDR  = 0.28f;
    private static final float Y_HINT       = 0.09f;

    private static final float LX           = 0.15f; // left column content x
    private static final float RX           = 0.53f; // right column content x

    private static final float LINE_H       = 0.040f; // line height (fraction of h)
    private static final int   MAX_LINES    = 5;      // max items before truncation

    // ── Fields ─────────────────────────────────────────────────────────────
    private final ScreenManager screenManager;
    private final GameScreen gameScreen;
    private final BattleScreen.BattleResult result;
    private final BattleResultSummary summary;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;
    private final OrthographicCamera camera;
    private boolean closed;

    public BattleResultsScreen(RogueForgeGame game, ScreenManager screenManager, GameScreen gameScreen,
                               BattleScreen.BattleResult result, BattleResultSummary summary) {
        this.screenManager = screenManager;
        this.gameScreen    = gameScreen;
        this.result        = result;
        this.summary       = summary;
        this.batch         = new SpriteBatch();
        this.shapes        = new ShapeRenderer();
        this.titleFont     = new BitmapFont();
        this.headerFont    = new BitmapFont();
        this.bodyFont      = new BitmapFont();
        this.camera        = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        titleFont.getData().setScale(2.5f);
        headerFont.getData().setScale(1.30f);
        bodyFont.getData().setScale(1.15f);
    }

    // ── Screen lifecycle ───────────────────────────────────────────────────

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

        Gdx.gl.glClearColor(0.06f, 0.07f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawChrome(w, h);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawContent(w, h);
        batch.end();
    }

    // ── Background / chrome ────────────────────────────────────────────────

    private void drawChrome(float w, float h) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Main panel
        shapes.setColor(COL_PANEL);
        shapes.rect(w * PANEL_X, h * PANEL_Y, w * PANEL_W, h * PANEL_H);

        shapes.setColor(COL_DIVIDER);

        // Horizontal: below title
        hline(w, h, PANEL_X, PANEL_X + PANEL_W, Y_TOP_DIV);
        // Horizontal: above robot footer
        hline(w, h, PANEL_X, PANEL_X + PANEL_W, Y_MID_DIV);
        // Horizontal: above hint
        hline(w, h, PANEL_X, PANEL_X + PANEL_W, Y_BOT_DIV);

        // Vertical: column split (between top and robot dividers)
        vline(w, h, COL_SPLIT, Y_MID_DIV, Y_TOP_DIV);

        // Left column internal divider (Rewards / Drops)
        hline(w, h, PANEL_X, COL_SPLIT, Y_DROPS_DIV);

        // Right column internal divider (Proficiency / Unlocks)
        hline(w, h, COL_SPLIT, PANEL_X + PANEL_W, Y_PROF_DIV);

        shapes.end();
    }

    /** Draws a 2px-tall horizontal divider line. */
    private void hline(float w, float h, float xFracStart, float xFracEnd, float yFrac) {
        shapes.rect(w * xFracStart, h * yFrac, w * (xFracEnd - xFracStart), 2f);
    }

    /** Draws a 2px-wide vertical divider line. */
    private void vline(float w, float h, float xFrac, float yFracBot, float yFracTop) {
        shapes.rect(w * xFrac, h * yFracBot, 2f, h * (yFracTop - yFracBot));
    }

    // ── Content ────────────────────────────────────────────────────────────

    private void drawContent(float w, float h) {
        float lh = h * LINE_H;
        float lx = w * LX;
        float rx = w * RX;

        // ── Title ──────────────────────────────────────────────────────────
        titleFont.setColor(COL_TITLE);
        titleFont.draw(batch, "* VICTORY *", w * 0.34f, h * Y_TITLE);

        // ── LEFT COLUMN ────────────────────────────────────────────────────

        // REWARDS
        sectionHeader("REWARDS", lx, h * Y_REWARDS);
        float ry = h * (Y_REWARDS - LINE_H);
        bodyLine("Gold:        " + summary.getGoldEarned() + " G",         lx, ry); ry -= lh;
        bodyLine("Experience:  " + summary.getExperienceEarned(),           lx, ry); ry -= lh;
        bodyLine("Level ups:   " + summary.getLevelUps(),                   lx, ry); ry -= lh;
        bodyLine("Bestiary:    " + summary.getBestiaryUpdates() + " new",   lx, ry);

        // DROPS
        sectionHeader("DROPS", lx, h * Y_DROPS_HDR);
        drawLines(summary.getDrops(), "None", lx, h * (Y_DROPS_HDR - LINE_H), lh, COL_BODY);

        // ── RIGHT COLUMN ───────────────────────────────────────────────────

        // PROFICIENCY GAINS (ability XP + weapon XP merged)
        sectionHeader("PROFICIENCY GAINS", rx, h * Y_PROF_HDR);
        String[] profLines = merge(summary.getAbilityProgress(), summary.getWeaponProgress());
        drawLines(profLines, "None", rx, h * (Y_PROF_HDR - LINE_H), lh, COL_BODY);

        // UNLOCKS (mastery level-ups + new Combat Arts) — rendered in gold
        sectionHeader("UNLOCKS", rx, h * Y_UNLOCK_HDR);
        drawLines(summary.getMasteryUnlocks(), "None", rx, h * (Y_UNLOCK_HDR - LINE_H), lh, COL_UNLOCK);

        // ── ROBOT PROGRESS (footer spanning both columns) ──────────────────
        sectionHeader("ROBOT PROGRESS", lx, h * Y_ROBOT_HDR);
        drawLines(summary.getRobotProgress(), "No changes this battle", lx, h * (Y_ROBOT_HDR - LINE_H), lh, COL_ROBOT);

        // ── Continue hint ──────────────────────────────────────────────────
        bodyFont.setColor(COL_HINT);
        bodyFont.draw(batch, "Press ENTER or click to return to the frontier.", w * 0.24f, h * Y_HINT);
    }

    // ── Rendering helpers ──────────────────────────────────────────────────

    private void sectionHeader(String text, float x, float y) {
        headerFont.setColor(COL_HEADER);
        headerFont.draw(batch, text, x, y);
    }

    private void bodyLine(String text, float x, float y) {
        bodyFont.setColor(COL_BODY);
        bodyFont.draw(batch, text, x, y);
    }

    /**
     * Draws up to {@link #MAX_LINES} items from {@code lines} then a "+ N more" overflow note.
     * Falls back to {@code emptyLabel} when the array is empty.
     */
    private void drawLines(String[] lines, String emptyLabel, float x, float startY, float lh, Color color) {
        if (lines == null || lines.length == 0) {
            bodyFont.setColor(COL_HINT);
            bodyFont.draw(batch, "  " + emptyLabel, x, startY);
            return;
        }
        int shown = Math.min(lines.length, MAX_LINES);
        bodyFont.setColor(color);
        for (int i = 0; i < shown; i++) {
            bodyFont.draw(batch, "- " + lines[i], x, startY - (i * lh));
        }
        if (lines.length > MAX_LINES) {
            bodyFont.setColor(COL_HINT);
            bodyFont.draw(batch, "  ... +" + (lines.length - MAX_LINES) + " more",
                x, startY - (shown * lh));
        }
    }

    /** Concatenates two non-null String arrays. */
    private static String[] merge(String[] a, String[] b) {
        if (a == null) a = new String[0];
        if (b == null) b = new String[0];
        String[] out = new String[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    // ── Lifecycle stubs ────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        titleFont.dispose();
        headerFont.dispose();
        bodyFont.dispose();
    }
}
