# RogueForge TMX Mapping Specification

## 1. Purpose

This document defines the standard TMX structure for RogueForge world maps.

Use it when:

- creating a new world-zone map
- refactoring an existing TMX file
- adding doors, spawns, NPCs, chests, or world interactions
- reviewing map consistency across the project

The goal is one predictable format across all production maps in [`assets/maps`](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps).

## 2. Core Standard

Every production world-zone TMX should use the same base layer stack.

### 2.1 Required Tile Layers

From bottom to top:

1. `01_Ground`
2. `02_Detail`
3. `03_Structures`
4. `04_Props`
5. `05_Foreground`

### 2.2 Required Object Layers

1. `features`
2. `collisions`
3. `spawn_points`
4. `doors`
5. `npcs`
6. `chests`

These names are intentionally aligned to the current loader behavior in [`TmxWorldLoader.java`](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/TmxWorldLoader.java).

## 3. Optional Layers

Only add these when the zone benefits from them:

- `06_OverlayFX`
- `07_Hazards`
- `waypoints`
- `triggers`
- `audio_zones`

Do not add optional layers by default if the map has no gameplay need for them.

## 4. Layer Responsibilities

### 4.1 Tile Layers

`01_Ground`

- terrain base
- grass, dirt, cave floor, snow, lava rock, void stone, water base

`02_Detail`

- paths
- cracks
- flowers
- puddles
- edge blends
- shallow terrain accents

`03_Structures`

- walls
- building bases
- fences
- large ruins
- pillars
- bridges

`04_Props`

- crates
- benches
- shrubs
- signs
- lamps
- barrels
- small static decoration

`05_Foreground`

- roofs
- canopy tops
- awnings
- hanging signs
- arch overlays
- anything meant to draw above the player

### 4.2 Object Layers

`features`

- interactable regions and notable landmarks
- houses
- shop stalls
- shrines
- gates
- special world interaction spots

Common properties:

- `kind`
- `label`
- `houseId`
- `shopId`
- `requiredWorldFlag`

`collisions`

- all movement blockers
- use broad rectangles and simple polygons
- prefer readable collision over tile-perfect collision

Common properties:

- `kind`

`spawn_points`

- player arrival anchors only
- every inbound route should have a matching spawn

Common properties:

- `spawnId`

`doors`

- map transitions
- door trigger rectangles

Common properties:

- `targetZone`
- `targetSpawnId`
- `requiredWorldFlag`

`npcs`

- talkable NPC placement points
- roaming anchors when needed

Common properties:

- `npcId`
- `facing`
- `label`
- `requiredWorldFlag`

`chests`

- loot
- hidden caches
- quest reward containers
- discoverable robot recovery spots

Common properties:

- `chestId`
- `requiredWorldFlag`
- `hidden`
- `recruitmentEventId`

## 5. Naming Rules

### 5.1 Tile Layers

Use the exact numeric prefixes:

- `01_Ground`
- `02_Detail`
- `03_Structures`
- `04_Props`
- `05_Foreground`

This keeps Tiled ordering stable and visually obvious.

### 5.2 Object Layers

Use the exact lowercase names expected by the current project:

- `features`
- `collisions`
- `spawn_points`
- `doors`
- `npcs`
- `chests`

Do not introduce alternate names such as `OBJ_Collision` or `PlayerSpawn` unless the loader is explicitly extended to support them.

## 6. Property Conventions

Use consistent property names across the whole project.

Recommended standard properties:

- `spawnId`
- `targetZone`
- `targetSpawnId`
- `npcId`
- `facing`
- `kind`
- `label`
- `houseId`
- `shopId`
- `chestId`
- `requiredWorldFlag`
- `hidden`
- `recruitmentEventId`

If an object needs gameplay metadata, it belongs on an object layer, not embedded in tile art.

## 7. Collision Guidance

Best practices:

- use larger rectangles wherever possible
- use simple polygons only for clearly irregular boundaries
- keep collision slightly simpler than visuals
- do not trace every tile edge
- let roofs and overhangs extend beyond collision if it feels better in play

Examples:

- house collision should cover the wall footprint, not the full roof art
- cliff collision should block the ledge, not every decorative stone
- fence collision can often be one long rectangle

## 8. Doors And Spawns

Door and spawn naming must be consistent.

Each travel direction should have:

- a door object in the source map
- a matching spawn point in the destination map

Example:

- door in `verdant_fields`
  - `targetZone = shadow_caves`
  - `targetSpawnId = from_fields`
- spawn point in `shadow_caves`
  - `spawnId = from_fields`

Minimum rule:

- every production map should have at least 2 player spawn points

## 9. Zone-Type Templates

### 9.1 Settlement / Home Village

Use:

- all 5 tile layers
- all 6 object layers

Focus:

- houses
- merchants
- named NPC hubs
- interior doors
- signs
- plaza landmarks

### 9.2 Fields / Forest / Coast

Use:

- `01_Ground`
- `02_Detail`
- `03_Structures`
- `04_Props`
- `05_Foreground`

Focus:

- terrain readability
- path routing
- hidden side routes
- signposts
- camps
- exploration chests

### 9.3 Cave / Ruin / Depths

Use:

- all 5 tile layers
- all 6 object layers

Focus:

- pillars
- blocked passages
- hidden seams
- quest caches
- tighter collision volumes

### 9.4 Hazard Regions

Examples:

- lava
- ice
- high mountain

Use:

- all 5 tile layers
- optional `07_Hazards` if needed later

Focus:

- readable traversal lanes
- hazard silhouettes
- boss arena space

### 9.5 Fortress / Endgame Zones

Use:

- all 5 tile layers
- all 6 object layers
- optional `triggers` for story beats

Focus:

- gates
- command spaces
- ritual rooms
- boss approach lines
- strong foreground silhouettes

### 9.6 Infinite Dungeon

Use:

- `01_Ground`
- `02_Detail`
- `03_Structures`
- `04_Props`

Object layers:

- `collisions`
- `spawn_points`
- `doors`
- `chests`
- `features`

`npcs` is usually unnecessary in the generated dungeon layout.

## 10. Per-Zone Template Table

The following table defines the intended map type and recommended usage pattern for each current production zone.

| Zone | TMX File | Template Type | Required Notes |
| --- | --- | --- | --- |
| Verdant Fields | `verdant_fields.tmx` | Settlement-adjacent field hub | Strong village-edge presentation, multiple exits, shops, NPC hub, hidden path content |
| Whispering Forest | `whispering_forest.tmx` | Field / forest | Hidden glade features, recruitable robot cache support, layered canopy foreground |
| Rusty Quarry | `rusty_quarry.tmx` | Field / ruin | Machinery debris, quarry lanes, cache chest coverage, heavy-frame interaction spaces |
| Coastal Shallows | `coastal_shallows.tmx` | Field / coast | Water-edge readability, tidepool route features, proper return spawns |
| Shadow Caves | `shadow_caves.tmx` | Cave / ruin | Hidden seam feature support, ruin pillars, cave wall collision clarity |
| Scorched Plateau | `scorched_plateau.tmx` | Hazard region | Heat-scarred terrain, clear lane routing, forge-adjacent landmarking |
| Frozen Vale | `frozen_vale.tmx` | Hazard region | Snow/ice readability, shrine/settlement features, clean choke-point collision |
| Crystal Depths | `crystal_depths.tmx` | Cave / ruin | Crystal structure silhouettes, scholar NPC space, cache placement |
| Dragon Peak | `dragon_peak.tmx` | Hazard region | Boss approach readability, ridge transitions, large landmark features |
| Sunken Abyss | `sunken_abyss.tmx` | Cave / depths | Undersea ruin layout, archive search space, at least two spawns |
| Sky Fortress | `sky_fortress.tmx` | Fortress / endgame | Command rail spaces, strong foreground overlay, military NPC anchors |
| Volcanic Core | `volcanic_core.tmx` | Hazard region | Furnace/boss arena readability, core landmarking, at least two spawns |
| Clockwork Sanctum | `clockwork_sanctum.tmx` | Fortress / endgame | Mechanical chambers, ritual/command features, clockwork route clarity |
| Abyssal Rift | `abyssal_rift.tmx` | Fortress / endgame | Breach landmarks, void gate approach, strict CSV/Tiled-safe formatting |
| The Void | `the_void.tmx` | Fortress / endgame | Final-approach spatial clarity, minimal clutter, strong foreground silhouettes |
| Infinite Dungeon | `infinite_dungeon.tmx` | Infinite dungeon | Keep generator-friendly, avoid hand-authored NPC dependence |

## 11. Non-Production Map

`verdant_fields_v2.tmx` currently exists as an editor-side variant and should not be treated as a production runtime map unless it is explicitly wired into zone loading and validated the same way as the main map set.

## 12. Review Checklist

Before considering a TMX map complete, verify:

1. It uses the standard tile-layer stack.
2. It contains the core object layers.
3. Every door points at a valid zone and destination spawn.
4. Every inbound route has a matching `spawnId`.
5. Collision is simple and readable.
6. Interactable landmarks live in `features`, not only in art.
7. Quest or discovery content is represented in `features`, `npcs`, or `chests`.
8. The foreground layer is used for roofs, canopy, and over-player visuals.
9. CSV tile data remains Tiled-safe.

## 13. Recommendation

Going forward, all new world maps should start from this spec rather than being improvised per zone.

That gives the project:

- more reliable Tiled editing
- easier map reviews
- cleaner loader expectations
- less door/spawn drift
- more consistent gameplay markup across the world
