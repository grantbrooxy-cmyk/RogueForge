# RogueForge – Visual Direction: Ironhaven & Art Style Overhaul

## The Goal

Ironhaven should feel like the best parts of three references blended together:

- **Stardew Valley** — warmth, coziness, earthy tones, lived-in town with personality
- **Pokémon (Gen 3/4)** — clean readable tiles, strong color separation, no visual clutter
- **Terraria** — layered environmental detail, strong texture variety, sense of depth

The current "Cute Fantasy Free" pack is fine as a placeholder, but its pastel-chibi style reads more like a mobile game than an indie adventure RPG. The replacement direction should feel hand-crafted and rich without being noisy.

---

## Immediate Asset Replacements

### 1. Town / Exterior Tiles (Ironhaven overworld)

**Priority: HIGH — this is the most visible first impression**

#### Best Free Option: LimeZu – Serene Village (Revamped)
- **Link:** https://limezu.itch.io/serenevillagerevamped
- **License:** CC-BY 4.0 (credit required, free to use commercially)
- **Size:** 16×16 px — drop-in compatible with your current Tiled maps
- **Why it works:** Warm earthy greens, wood-and-stone buildings, varied terrain transitions, animated objects. This is the closest free tileset to Stardew Valley's aesthetic. 24 house variations means Ironhaven can have real visual diversity per building.
- **Includes:** Terrain, trees, rocks, mushrooms, 24 houses, fences, paths, water, animated objects

**Credit line to add to your credits screen:**
> Serene Village (Revamped) by LimeZu — limezu.itch.io — CC-BY 4.0

---

#### Strong Runner-Up: OpenGameArt – RPG Town Pixel Art Assets
- **Link:** https://opengameart.org/content/rpg-town-pixel-art-assets
- **License:** CC0 / Public Domain (no credit needed)
- **Why it works:** 16×16, heavily inspired by Final Fantasy 6's town tiles — clean outlines, warm stone/wood palette. Very Pokémon-readable.

---

### 2. Interior Tiles (houses, shops, forge interior)

**Priority: MEDIUM**

#### Best Option: LimeZu – Modern Interiors (Free Sample)
- **Link:** https://limezu.itch.io/moderninteriors
- The free tier includes a solid set of floor/wall tiles and furniture sprites. The full pack is paid but affordable (~$5–10).
- **Why it works:** Reads immediately, warm wood floors, clean walls. Feels Stardew-like for indoor spaces.

---

### 3. Characters & NPCs

**Priority: MEDIUM-HIGH**

Your current character sprites in `assets/1 Characters/` use a generic fantasy style. For Pokémon-style readability:

#### Recommended Free Pack: Anokolisa – Free Pixel Art Asset Pack
- **Link:** https://anokolisa.itch.io/free-pixel-art-asset-pack-topdown-tileset-rpg-16x16-sprites
- Includes 3 hero characters, 8 enemies, 50 weapons — all 16×16 and top-down compatible

#### For a Pokémon-inspired player character:
- **Link:** https://aarya-yt.itch.io/16x16-tileset-pokemon-inspired — free Pokémon-style tileset + characters that pair well with the LimeZu village tiles

---

### 4. Dungeon / Ironhaven Underground / Combat Zone Tiles

**Priority: MEDIUM**

#### Recommended: LPC 16x16 Tiles Extended (OpenGameArt)
- **Link:** https://opengameart.org/content/lpc-16x16-tiles-extended
- **License:** CC-BY-SA 3.0
- **Why it works:** Huge variety of cave, dungeon, underground, forest, snow, and water tiles. Gives the Terraria-style layered detail for non-town zones. All 16×16 and designed for top-down maps.

---

## How to Swap Tilesets in Tiled

1. Download the new tileset PNG (e.g., LimeZu's Serene Village sprite sheet)
2. Drop it into `assets/maps/` or a new `assets/tilesets/` folder
3. In Tiled: open `town.tmx` → open the Tilesets panel → click the wrench icon on your current tileset → update the image source to the new file
4. Re-map tiles layer by layer. Since the new tiles will be 16×16 like the current ones, the grid structure stays the same — only tile IDs change.
5. Update `tileset.tsx` to point to the new source image path.

**Tip:** Do one layer at a time (ground first, then objects, then details). Don't try to remap everything at once.

---

## Color Direction for Ironhaven

### Palette Reference (Stardew/Pokémon blend)

| Element | Current | Target |
|---|---|---|
| Grass | Bright/cool green | Warm medium green `#6A9E44` |
| Dirt path | N/A | Warm sandy tan `#C8A86B` |
| Stone buildings | Generic gray | Warm slate `#7A7065` |
| Wood buildings | N/A | Rich cedar `#8B5E3C` |
| Water | Generic blue | Teal-aqua `#4A9BAE` |
| Sky/ambient | Cold | Warm afternoon `#F2C97A` |

### Key Principle from Reference Games
All three target games use **muted, slightly desaturated colors for large areas** (grass, ground, walls) and **brighter saturated colors as accents** (flowers, signs, player character, important items). The contrast guides the eye naturally.

Avoid pure RGB values like `(0, 255, 0)` for grass — they look garish. Always slightly shift greens toward yellow, blues toward teal.

---

## UI Theme Recommendations

The current `UITheme.java` uses cold colors (bright blue primary, pure greens/cyans). Warmer alternatives:

| Role | Current | Stardew-like Replacement |
|---|---|---|
| BACKGROUND | `#1A1A1A` cold dark | `#1C1610` warm dark brown-black |
| PRIMARY | `#3399FF` cold blue | `#5B8C5A` muted sage green |
| ACCENT | `#FFD900` gold | `#E8A930` warm amber gold (keep similar) |
| HEALTH_BAR | `#00FF00` pure green | `#52B256` warm forest green |
| MANA_BAR | `#33CCFF` cold cyan | `#5A9EC8` muted sky blue |
| TEXT | `#FFFFFF` pure white | `#F5EED8` warm cream |

---

## Map Layout Principles for Ironhaven (Pokémon-style readability)

Pokémon towns are instantly readable because of a few rules:

1. **Paths are always distinct from grass** — use a clearly different tile, not just a color variation
2. **Buildings have consistent footprints** — 2-tile-tall walls + 1-tile roof overhang is the classic formula
3. **3-tile variety rule** — no single tile repeats more than 3 times in a row without a break (rock, flower, variation)
4. **Water always has an animated border** — static water reads as ice; animated edges read as liquid
5. **NPCs stand on paths or inside buildings** — never floating in grass

Stardew adds: **seasonal decoration layers** (flowers, leaves), **shadow under every object**, and **interior lighting warmth**.

Terraria adds: **background tile variety** — use 2-3 grass variants scattered procedurally so the ground doesn't look like a checkerboard.

---

## Quick Wins You Can Do Today

1. **Swap the tileset PNG** in `town.tmx` to LimeZu Serene Village — even without remapping all tiles, just seeing warmer colors will feel like a big improvement
2. **Update UITheme.java** with the warmer color values above — this affects HUD, menus, and battle screen immediately
3. **Add a simple vignette or ambient color tint** in your WorldRenderer — a warm amber tint (0.03f, 0.02f, 0.0f additive) on the camera gives an "afternoon light" feel for no art cost
4. **Scale up your pixel art correctly** — ensure `TextureFilter.Nearest` is set everywhere (not `Linear`) so pixels stay crisp; check your SpriteBatch and Texture loads

---

## Asset Credits Template

If using the recommended packs, add to your credits screen:

```
Art Assets:
- Serene Village (Revamped) by LimeZu (limezu.itch.io) — CC-BY 4.0
- LPC 16x16 Tiles Extended (OpenGameArt) — CC-BY-SA 3.0
- Anokolisa Free Asset Pack (anokolisa.itch.io)
```
