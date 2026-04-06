# RogueForge

RogueForge is a 2D pixel-art action RPG / tactics hybrid built with LibGDX. You explore connected zones, fight real-time and turn-based encounters, recruit robots, craft upgrades, and expand your foothold across a frontier full of monsters, contracts, and faction pressure.

## Gameplay Preview

Gameplay screenshot / GIF placeholder: add a capture at `docs/gameplay.gif` or `docs/gameplay.png` and link it here once you export a representative run.

## What Is RogueForge?

The current project combines:

- Overworld exploration across frontier zones and settlements
- Party-building with recruitable robot allies
- Turn-based battle encounters with abilities, elemental interactions, and status effects
- Gear, crafting, shops, and progression data loaded from JSON in `assets/data/`
- Base-building, guild systems, and world-state-driven content layers

## Run

macOS / Linux:
```bash
./run.sh
```

Windows:
```bat
run.bat
```

The launch scripts verify Java, make sure the Gradle wrapper exists, and start the desktop target with `:lwjgl3:run`.

## Controls

Overworld:

- `W A S D` or arrow keys: move
- `E`: interact / confirm in world interactions
- `Enter`: dismiss message prompts
- `Esc`: pause
- `I`: open workshop / inventory flow
- `Q`: open the quest menu
- `P`: open expedition/contract-related panels
- `G`: open guild management
- `B`: open build mode

Build mode:

- `B` or `Esc`: close build mode
- `Left` / `Right`: cycle structures
- `E`: place selected structure
- `R`: remove structure
- `T`: repair structure
- `F`: assign a reserve bot

Battle:

- `Up` / `Down`: change selection
- `Enter` or `Space`: confirm action
- `Esc`: go back
- `1`-`6`: quick-pick menu options

Menus and modal screens also support mouse input in several places.

## Data-Driven Content

Most gameplay definitions already live in [`assets/data/`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data), so balance and content tweaks can be made without recompiling code.

Useful files include:

- [`assets/data/monsters.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/monsters.json): monster stats, ranks, elemental traits, and loot values
- [`assets/data/zones.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/zones.json): zone IDs, tilemaps, rank ranges, bosses, and monster pools
- [`assets/data/abilities.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/abilities.json): combat abilities and status payloads
- [`assets/data/robots.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/robots.json): robot archetypes, stats, and ability loadouts
- [`assets/data/equipment.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/equipment.json): player/robot gear and stat bonuses

## How To Add New Monsters

1. Add a new monster entry to [`assets/data/monsters.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/monsters.json).
2. Give it a unique `id`, a readable `name`, combat stats, `rank`, `aiProfile`, and any `weaknesses`, `resistances`, or `absorbs`.
3. If the monster should appear in the overworld, reference that `id` from a zone in [`assets/data/zones.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/zones.json).
4. If the monster needs custom battle behavior or presentation, follow up in the combat / screen code after the data entry exists.

## How To Add New Zones

1. Add a new zone entry to [`assets/data/zones.json`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/zones.json).
2. Set a unique `id`, `name`, `tilemapPath`, rank floor/ceiling, and `monsterIds`.
3. Create or reference the matching TMX map and tileset assets under `assets/`.
4. If the zone participates in frontier generation, war systems, or custom event flows, wire those rules in after the base definition is loading correctly.

For related world-content guidance, see [`docs/TMX_Mapping_Spec.md`](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/TMX_Mapping_Spec.md) and [`docs/Systems_Spec.md`](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/Systems_Spec.md).

## Portable Windows Build

Windows app image folder:
```bat
gradlew.bat :lwjgl3:packageAppImage
```

Windows helper that builds the app image and zips it:
```bat
portable.bat
```

The portable app image is written to `lwjgl3/build/app-image/RogueForge` and the zipped archive is written to `lwjgl3/build/portable/RogueForge-portable.zip`.

## Package Installers

macOS `.dmg`:
```bash
./gradlew :lwjgl3:packageDmg
```

Windows `.exe`:
```bat
gradlew.bat :lwjgl3:packageExe
```

Both packaging tasks output installers into `lwjgl3/build/package` and require `jpackage` from a JDK that includes it.
