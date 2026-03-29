# RogueForge Run, World, and Meta Structure

## 1. Purpose

This document defines how RogueForge combines roguelite progression with open-world exploration, town growth, and player-built outposts.

Its job is to answer four questions clearly:

- What resets on death
- What persists in the world
- What is permanently retained as meta progression
- How quests, bases, and expeditions should interact

This is the primary reference for keeping the roguelite layer and the persistent world layer compatible.

Implementation companion:

- `docs/State_Implementation_Guide.md` for concrete rules on save-file fields, quest state channels, world flags, session state, and meta progression storage

## 2. Core Model

RogueForge should be structured as three connected progression layers:

- Run Layer
- World Layer
- Meta Layer

These layers must not carry the same stakes.

If they overlap too much, players will either:

- feel punished for building long-term systems
- or exploit death because it is too rewarding

The design goal is:

- runs create tension
- the world creates attachment and continuity
- meta progression creates long-tail identity between failed runs

## 3. Run Layer

The run layer is the current expedition.

This is the part of the game that should feel risky and roguelite-driven.

### 3.1 Run Layer Includes

- current expedition state
- current HP and consumable pressure
- temporary run buffs
- unbanked loot and materials
- current frontier push depth
- active dungeon attempt
- live combat risk
- raid pressure accumulated during the outing

### 3.2 Run Layer Rules

- The player leaves Ironhaven or an owned outpost to begin an expedition.
- The deeper the player pushes, the higher the value and risk.
- Valuable progress should usually need to be extracted or banked.
- Death should mostly punish unbanked run gains rather than destroy persistent settlement progress.

### 3.3 Run Success

A run is successful when the player:

- returns alive to Ironhaven
- returns alive to a secure owned outpost
- completes a major objective and banks the rewards

## 4. World Layer

The world layer is the persistent state of Mechara for that save.

This is the layer that supports open-world building and long-term investment.

### 4.1 World Layer Includes

- Ironhaven growth
- unlocked services and shops
- claimed frontier outposts
- placed structures
- defender assignments
- world seed
- discovered map state
- harvested frontier state
- cleared story gates
- completed quests
- unlocked routes and shortcuts

### 4.2 World Layer Rules

- World progress should survive death.
- Built bases should remain.
- Claimed land should remain.
- Long-term infrastructure should reduce friction for future runs.
- Persistent world growth should create more launch points, better recovery, and stronger logistics, but should not erase danger entirely.

## 5. Meta Layer

The meta layer sits above the current save-state run and expresses long-tail adaptation across failures.

### 5.1 Meta Layer Includes

- cybernetic augments
- active curses
- curse purge opportunities
- collapse streak
- death-judged reward history

### 5.2 Meta Layer Rules

- Meta rewards should be tied to the quality of a failed run, not just death count.
- Repeated shallow failures should become punitive through curses.
- Strong runs should allow higher-tier augments or curse removal.
- Meta progression should not replace the need for good play, extraction, or settlement planning.

## 6. Death Rules

Death must be meaningful without making long-term building feel pointless.

### 6.1 On Death, The Player Should Lose

- unbanked expedition loot
- unbanked temporary combat or run bonuses
- current field push momentum
- current active sortie state

### 6.2 On Death, The Player Should Keep

- Ironhaven progression
- claimed outposts
- built base structures
- reserve bot assignments where applicable
- unlocked routes
- completed quests
- persistent crafting unlocks
- world seed and discovered world state
- meta augments and curses

### 6.3 Design Intent

Death should mean:

- "I failed this expedition"

not:

- "my settlement investment was erased"

## 7. Extraction and Banking

Extraction is the bridge between the run layer and the world layer.

### 7.1 Banking Rule

Important rewards should fall into one of two categories:

- automatically persistent rewards
- rewards that must be banked by returning alive

### 7.2 Good Banked Rewards

- salvage
- frontier materials
- blueprint fragments
- rare crafting stock
- outpost supplies
- bot parts
- story artifacts recovered from dangerous zones

### 7.3 Good Automatically Persistent Rewards

- completed main-story quest flags
- defeated one-time bosses
- world unlock switches
- permanently opened routes

### 7.4 Safe Banking Points

The player should be able to bank rewards at:

- Ironhaven
- secure player outposts
- special major expedition checkpoints if designed explicitly

## 8. Outposts and Bases

Outposts are the main bridge between the persistent world and the roguelite expedition loop.

### 8.1 Outpost Role

Outposts should not be purely decorative. They should act as:

- forward launch points
- repair and recovery points
- stash nodes for banked field resources
- defender anchors
- regional strategic footholds

### 8.2 Outpost Benefits

Building an outpost can provide:

- shorter travel time back to danger
- safer extraction routes
- local storage
- healing or repair support
- reserve-bot defense coverage
- raid or event management

### 8.3 Outpost Limits

Outposts must not fully invalidate risk. They should:

- reduce danger locally
- improve recovery
- extend reach

but not:

- trivialize deep frontier exploration
- fully suppress raids everywhere
- remove the need to return with resources

## 9. Quests

Quests should be divided by layer so their stakes make sense.

### 9.1 World Quests

These should persist across death.

Examples:

- meet key NPCs
- unlock Ironhaven services
- claim an outpost site
- open a route
- defeat a one-time zone boss
- restore a regional machine

### 9.2 Expedition Quests

These are tied to a single outing or deep push.

Examples:

- survive a dangerous sweep
- recover an artifact from a ruin
- escort or protect a convoy during a sortie
- clear a raid wave before extraction
- reach a biome threshold and come back alive

### 9.3 Quest Rule

If a quest is about:

- changing the world, it should usually persist
- surviving a run, it should usually reset if the player dies before banking it

## 10. Recommended Gameplay Loop

RogueForge should feel like:

1. Prepare in Ironhaven
2. Travel to a frontier region or owned outpost
3. Push into dangerous territory
4. Gather resources, fight, build temporary momentum, and pursue objectives
5. Decide whether to push deeper or extract
6. Return to a safe bank point
7. Convert run gains into world growth
8. If death occurs, evaluate the run through the meta system

This preserves tension while making base building meaningful.

## 11. Content Placement Guide

Use this rule when adding new rewards or systems:

### 11.1 Ask Three Questions

- Is this for the current run
- Is this for the persistent world
- Is this for meta progression

### 11.2 Examples

- potion found in the field: run
- salvage carried home: world
- new outpost structure unlocked: world
- cybernetic augment drafted on death: meta
- curse from repeated poor runs: meta
- temporary combat buff from a ruin shrine: run
- route opened by powering an ancient gate: world

## 12. Sensible Failure States

To keep the hybrid structure coherent:

- failing a run should sting
- building a base should always matter
- deep exploration should remain risky
- returning alive should be clearly better than dying

The player should never feel that the smartest strategy is to intentionally die.

## 13. Implementation Guidance

When implementing systems, default to these rules:

- Store persistent settlement state in world/save data.
- Store expedition-only state in active run/session data.
- Store augments and curses in meta progression data.
- Make outposts improve launch, recovery, defense, and banking.
- Gate the highest-value rewards behind successful extraction or completion.

## 14. Current Build Alignment

The current build already has the foundations for this structure:

- persistent town and frontier world state
- persistent base and defender systems
- seeded frontier generation
- death-judged augment and curse drafts

The next design responsibility is to keep future content aligned with the layer model above so the hybrid structure remains sensible as the game grows.
