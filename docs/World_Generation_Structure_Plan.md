## World Generation Structure Plan

This is the intended map strategy for RogueForge.

The game should not choose between `fully handcrafted` and `fully procedural`.
It should use a hybrid structure:

- handcrafted hubs and major landmarks
- seeded procedural wilderness
- authored prefab locations injected into the generated world

That keeps the game readable and memorable while still giving it scale, replay value, and discovery.

## Core Rule

Use the right world-building method for the right gameplay purpose.

- `Handcrafted` for places the player must understand clearly
- `Procedural` for traversal, exploration, and long-tail replay
- `Prefab injection` for reusable authored content placed differently each seed

## World Layers

### 1. Starter Hub Layer

Ironhaven should be handcrafted.

It is the player’s first impression and the anchor for the entire loop, so it should always be:
- visually strong
- readable
- quest-safe
- easy to navigate
- stable across saves and seeds

Ironhaven should always exist near the world origin or a fixed seeded anchor point.

This includes:
- town layout
- initial buildings
- first tutorial NPCs
- early service locations
- early quest staging

### 2. Safe Frontier Ring

The area immediately around Ironhaven should be semi-curated.

This ring teaches:
- first gathering
- first combat
- first extraction
- first outpost claim
- first dungeon entrance

This space can still live inside a seeded world, but it should be controlled enough that the player never gets a bad or confusing opening.

Best approach:
- fixed or lightly varied terrain bands
- controlled enemy density
- guaranteed route to first objectives
- guaranteed room for early outpost building

### 3. Seeded Overworld

Beyond the safe frontier ring, the world should expand through seeded generation.

This layer is where the game gets its scale and replay value.

The seeded overworld should determine:
- biome layout
- resource concentrations
- traversable routes
- open buildable plains
- hostile regions
- placement candidates for villages, ruins, caves, and landmarks

The current terrain/biome seed foundation already supports this direction well.

### 4. Prefab Landmark Layer

The generated overworld should not be empty noise.

It should place handcrafted prefab locations into valid generated regions.

Examples:
- ruined villages
- watchtowers
- abandoned depots
- shrines
- cave mouths
- dungeon entrances
- broken workshops
- neutral settlements
- guild camps
- raid nests

Each prefab should be authored manually once, then placed by generation rules.

This gives:
- handcrafted visual quality
- procedural replayability
- efficient content scaling

## Best Content Split

### Handcrafted Spaces

These should be manually built:
- Ironhaven
- major story hubs
- boss arenas
- first dungeon
- special late-game landmarks
- important interiors

### Procedural Spaces

These should be seeded:
- wilderness terrain
- biome spread
- resource belts
- travel space between landmarks
- optional exploration sectors
- long-range building territory

### Prefab-Injected Spaces

These should be authored once and placed by rules:
- minor villages
- camps
- ruins
- towers
- caves
- outposts
- shrines
- small dungeon entrances

## Village Strategy

Random villages should exist, but they should not be fully random tile soup.

They should come from village prefab sets.

Example structure:
- `village_small_a`
- `village_small_b`
- `village_farmstead_a`
- `ruined_village_a`
- `watchpost_a`
- `trader_camp_a`

The generator chooses:
- whether a village appears
- which prefab variant to use
- which biome it can appear in
- what condition it is in
- what NPC set or shop role it has

This is much stronger than generating villages from scratch.

## Dungeon Strategy

Dungeons should mostly be anchored to landmarks.

Best model:
- the entrance and surrounding landmark are handcrafted or prefab-authored
- the deeper internal layout can be procedural, semi-procedural, or room-template based

That gives you:
- recognizable world presence
- replayable internal structure

## Quest Strategy

Quests should respect the same world structure.

### Handcrafted Quest Content

Use this for:
- tutorial quests
- main story arcs
- boss progression
- settlement milestones
- named NPC arcs

### Systemic Quest Content

Use this for:
- bounty targets
- village requests
- guild tasks
- frontier contracts
- defense requests
- gathering orders

Systemic quests should target generated locations and prefab settlements without needing a unique authored map every time.

## Base Building Strategy

Base building works best in procedural space, not inside the handcrafted hub.

So:
- Ironhaven remains protected and curated
- player-built bases belong in the seeded overworld
- prefab settlements and ruins create reasons to expand outward

This preserves the fantasy:
- town is your anchor
- frontier is your canvas

## Recommended World Shape

The strongest structure for RogueForge is:

1. `Ironhaven`
Always handcrafted.

2. `Starter frontier ring`
Semi-curated and safe enough to teach the loop.

3. `Large seeded overworld`
Biome-based, expandable, and resource-rich.

4. `Injected prefab landmarks`
Villages, ruins, camps, and dungeon entrances.

5. `Deep danger bands`
Harder sectors, rare materials, raid pressure, major dungeons.

## Engine Direction

The engine should support three world-content types:

- `FixedMapZone`
For handcrafted maps like Ironhaven.

- `GeneratedFrontierZone`
For seeded overworld terrain and large-scale wilderness.

- `PrefabLocation`
For authored structures placed into generated space.

That should become the core mental model for future map work.

## Practical Recommendation

For now:

- repaint Ironhaven manually in Tiled
- keep it as the handcrafted starter hub
- keep Verdant Fields and the wider frontier seeded
- add village and landmark prefabs next instead of trying to hand-build the entire world

This gives the best blend of:
- visual quality
- replay value
- exploration
- quest clarity
- scalable content production

## Decision Rule

When adding a new world space, ask:

- Is this a place every player must understand clearly?
- Is this a place meant to support endless exploration?
- Is this a reusable authored landmark?

Then choose:

- `handcrafted`
- `procedural`
- `prefab-injected`

If a space serves more than one purpose, split its layers instead of forcing one method to do everything.
