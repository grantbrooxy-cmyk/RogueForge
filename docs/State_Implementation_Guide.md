# RogueForge State Implementation Guide

## 1. Purpose

This document translates the layer model from [Run_World_Meta_Structure.md](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/Run_World_Meta_Structure.md) into implementation rules for code and save data.

Use it when adding:

- save-file fields
- world-state flags
- quest logic
- expedition state
- meta progression data

The goal is to stop run-state, world-state, and meta-state from drifting into each other.

## 2. The Three Storage Domains

RogueForge should treat state as belonging to one of three domains:

- Save domain
- Session domain
- Meta domain

### 2.1 Save Domain

Stored in the main save file.

Use this for:

- persistent world state
- persistent character progression
- persistent base/outpost state
- persistent quest progression

### 2.2 Session Domain

Lives only during the current expedition or active runtime session unless intentionally banked into the save.

Use this for:

- current encounter state
- temporary run buffs
- temporary expedition modifiers
- current sortie-specific counters
- unbanked resource haul

### 2.3 Meta Domain

Stored separately from the main save and applied across failed runs.

Use this for:

- cybernetic augments
- active curses
- collapse streak
- death-draft progression

## 3. Current Code Seams

These are the main code locations that already map to the three domains.

### 3.1 Save Domain

- [SaveFile.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/data/SaveFile.java)
- [SaveManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/persistence/SaveManager.java)
- [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L2281)

### 3.2 World and Quest Logic

- [QuestManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/QuestManager.java)
- [WorldStateManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/WorldStateManager.java)
- [DialogueSystem.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/DialogueSystem.java)

### 3.3 Meta Domain

- [MetaProgressionManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/persistence/MetaProgressionManager.java)
- [MetaProgressionState.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/data/MetaProgressionState.java)
- [CyberneticEnhancementEngine.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/engine/meta/CyberneticEnhancementEngine.java)

## 4. Save Data Rules

When adding a new field to [SaveFile.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/data/SaveFile.java), first classify it.

### 4.1 Persist in SaveFile If It Represents

- the state of the world after the player returns
- permanent unlocks
- stable inventory or equipment
- base ownership or placed structures
- claimed land
- durable route or story changes
- quest line progress that should survive death

### 4.2 Do Not Persist in SaveFile If It Represents

- a one-expedition temporary buff
- an unbanked run-only reward
- a temporary room or event modifier that should disappear on death
- a death-draft option list for the current game-over screen

### 4.3 Persist Separately in Meta If It Represents

- progression earned from judged failure
- augment ownership
- curse ownership
- curse-clearing history
- anti-suicide punishment state

## 5. Quest State Rules

Quest logic now uses two primary channels:

- `questStates`
- `worldStateFlags`

These should have distinct jobs.

### 5.1 `questStates`

Use for:

- current structured step of a quest line
- the explicit stage of a multi-step main or side quest

Good examples:

- `ironhaven_arrival -> survey_town`
- `shard_hunt -> return_shard`

Do not use `questStates` for:

- tiny one-off world facts
- generic unlocks
- NPC visibility switches that are not truly quest-stage data

### 5.2 `worldStateFlags`

Use for:

- world facts
- unlock conditions
- dialogue conditions
- map access switches
- facility upgrades
- NPC visibility conditions
- one-time story or settlement state changes

Examples:

- `arrival.spoke_mira`
- `settlement.workshop_tools`
- `arrival.first_battle_won`
- route gates, powered systems, and revealed shortcuts

## 6. Recommended Quest Implementation Pattern

For new quests, use this pattern:

1. Create a structured quest line in `quests.json`
2. Track the current step in `questStates`
3. Use `worldStateFlags` for completion conditions and world consequences
### 6.1 Example

For a quest to restore a radio tower:

- `questState`: `restore_radio -> gather_parts`
- `worldStateFlag`: `world.radio_parts_collected`
- `worldStateFlag`: `world.radio_tower_online`
- `worldStateFlag`: `route.north_signal_open`

This keeps:

- quest step progression readable
- world consequences reusable by dialogue, doors, and encounters

## 7. Death and Reset Rules in Code

When implementing death handling, ask:

- should this survive death in the same save
- should this disappear unless banked
- should this live in meta progression instead

### 7.1 Survives Death

- `worldSeed`
- world unlocks
- quest line progression
- claimed sites
- base structures
- settlement upgrades
- discovered persistent routes

### 7.2 Lost on Death Unless Banked

- unbanked field haul
- temporary expedition modifiers
- temporary active run boosts
- unfinished sortie progress intended to be risky

### 7.3 Moves to Meta Progression

- judged death rewards
- augments
- curses
- curse purge state

## 8. Naming Rules

Use names that reveal the domain.

### 8.1 World Flags

Prefer prefixes like:

- `arrival.`
- `settlement.`
- `route.`
- `recruit.`
- `boss.`
- `world.`
- `zone.`

### 8.2 Run or Expedition Data

Prefer prefixes or types like:

- `run.`
- `expedition.`
- `sortie.`
- `raidSession.`

These should usually not be serialized into persistent save data unless explicitly banked.

### 8.3 Meta Data

Prefer prefixes or types like:

- `meta.`
- `augment.`
- `curse.`
- `collapse.`

## 9. Practical Guidance for New Features

### 9.1 If You Add a New Reward

Decide whether it belongs to:

- current run
- persistent world
- meta progression

Then store it in the matching domain.

### 9.2 If You Add a New Quest

Use:

- `questStates` for step progression
- `worldStateFlags` for side effects and conditions

Avoid:

- inventing multiple new boolean quest flags unless absolutely necessary

### 9.3 If You Add a New Base Mechanic

Store:

- ownership
- placement
- assignments
- damage that should persist

in the save domain

Store:

- temporary combat state during a current raid encounter

in the session domain

### 9.4 If You Add a New Roguelite Mechanic

Store:

- permanent judged-failure progression in meta

Do not store it as ordinary world progress unless it is meant to reshape that save permanently.

## 10. Recommended Refactor Direction

The current codebase is workable, but future cleanup should move toward:

1. treating `questStates` as the primary quest-step source of truth
2. keeping structured quest steps and world flags as the only supported quest progression channels
3. reserving `worldStateFlags` for reusable world facts
4. keeping expedition-only state out of long-term save structures unless intentionally banked
5. keeping all death-judged progression in the meta system

## 11. Decision Checklist

Before adding new state, answer:

1. Is this for the current expedition only
2. Is this meant to change the persistent save world
3. Is this earned through failure and meant to persist above the current run

If the answer is unclear, the feature is probably mixing layers and should be redesigned before implementation.
