# RogueForge Loop Alignment Plan

## 1. Purpose

This document checks the current playable game against the intended core loop and identifies the next implementation steps needed to make that loop feel deliberate, readable, and repeatable in live play.

Canonical loop:

1. Prepare in Ironhaven or an owned outpost
2. Travel to the frontier or deeper territory
3. Explore, fight, gather, and pursue quests
4. Decide whether to push deeper or extract
5. Bank resources and progress
6. Upgrade town, gear, robots, bases, and world state
7. Unlock new zones, systems, and opportunities
8. Repeat

This is a gameplay-structure document, not only a content document.

Companion documents:

- `docs/GDD.md`
- `docs/Systems_Spec.md`
- `docs/Run_World_Meta_Structure.md`
- `docs/State_Implementation_Guide.md`
- `docs/Narrative_Bible.md`
- `docs/Act_0_Arrival_Plan.md`
- `docs/Act_1_Frontier_Expansion_Plan.md`

## 2. Current Assessment

The current build already mostly follows the target loop.

Strongly implemented today:

- starting in Ironhaven
- frontier travel
- exploration and combat
- resource gathering
- unbanked versus banked haul
- outpost claims and base building
- return and banking behavior
- persistent world upgrades and base persistence
- authored quest progression

Still weaker than the rest of the loop:

- expedition preparation as a distinct phase
- post-return quest refresh as a systemic phase

The game structurally supports the loop, but the player does not always feel the loop as clearly as they should.

## 3. Step-by-Step Alignment

### 3.1 Prepare in Ironhaven or an Owned Outpost

Current status: strong

What works:

- the run begins in `town`
- town dialogue and early quests work
- shops, workshop, forge, and progression systems are town-anchored
- settlement upgrades visibly change town state
- outposts already exist as persistent forward positions

What still helps:

- make both town and owned outposts feel more like expedition staging grounds and less like only service nodes

### 3.2 Travel to the Frontier or Deeper Territory

Current status: strong

What works:

- town to Verdant Fields transition is established
- frontier sites and outposts exist
- claims and player structures create forward positions

What still helps:

- clearer travel identity between “launching from town” and “launching from a player outpost”

### 3.3 Explore, Fight, Gather, and Pursue Quests

Current status: strong

What works:

- seeded frontier exploration
- roaming encounters
- harvesting
- outpost claiming
- quest advancement during world play
- persistent frontier state

This is one of the healthiest loop steps in the build.

### 3.4 Decide Whether to Push Deeper or Extract

Current status: strong

What works:

- unbanked haul
- storage-backed extraction points
- death loss for unbanked gains
- safe versus unsafe world positions

What still helps:

- stronger UI feedback for current risk and extraction value
- clearer “you are carrying X unbanked” reminders during deeper frontier play

### 3.5 Bank Resources and Progress

Current status: strong

What works:

- town and storage structures bank haul
- save/load preserves unbanked versus banked state
- death clears unbanked haul

This step is already functionally clear.

### 3.6 Upgrade Town, Gear, Robots, Bases, and World State

Current status: good, but fragmented

What works:

- town upgrades
- robot progression
- equipment and forge loop
- base structures and defenders
- guild ownership foundation for future shared-world growth

What is missing:

- a more unified “post-expedition upgrade phase” feel
- stronger connection between what was brought back and what can now be improved

### 3.7 Unlock New Zones, Systems, and Opportunities

Current status: good for authored quests and unlocks, weaker for systemic refresh

What works:

- quest states advance
- world flags unlock new steps
- settlement/world consequences already feed authored content

What is missing:

- recurring quest refresh tied to:
  - outpost ownership
  - settlement upgrades
  - frontier discoveries
  - guild state
  - raid survival
- broader unlock surfacing for:
  - new routes
  - new services
  - new systems
  - new job types
  - new player-authored opportunities

### 3.8 Repeat

Current status: structurally strong

The player can already repeat the loop, but the handoff from “upgrade and unlock” back into “next expedition” needs stronger staging.

## 4. Main Gaps

The current game does not have a loop problem so much as a `loop readability` problem.

The two key gaps are:

### Gap A: Preparation Is Not Central Enough

The player can technically prepare, but the game does not yet frame preparation as an important, intentional phase.

### Gap B: Return-State Does Not Yet Generate Enough Fresh Work and Unlock Visibility

The player can return, bank, and upgrade, but the game does not yet create enough new objectives directly out of that return state.

## 5. Recommended Immediate Changes

## 5.1 Expedition Board

Add a dedicated expedition-prep screen or overlay in Ironhaven that summarizes:

- current objective
- active expedition quests
- recommended frontier route or zone
- current repair kits
- current party / reserve-bot status
- current banked materials
- current equipped player loadout
- active claim guild and outpost ownership mode

This should become the clear “before leaving town or an owned outpost” interaction point.

## 5.2 Extraction Summary on Return

When the player banks haul, show a stronger summary of:

- what was banked
- what upgrades became possible
- what quests progressed
- what new jobs or world responses unlocked

This makes the loop feel more rewarding and legible.

## 5.3 Dynamic Quest Refresh

After banking or returning alive, recalculate available quest hooks from:

- newly claimed outposts
- newly built structures
- newly defeated bosses
- newly reached biomes
- settlement upgrade thresholds
- raid survival events

This can begin with authored templates before full player-authored quest systems arrive.

## 5.4 Outpost Role Clarification

Outposts should create clearer gameplay differences from town:

- local banking
- defender safety
- regional launch point
- local services depending on built structures
- zone-specific jobs or warnings

That makes the loop naturally branch into:

- town-centered run
- outpost-centered run

## 5.5 Risk Feedback in Frontier

The HUD should more clearly surface:

- unbanked gold
- unbanked components
- unbanked shards
- nearest valid bank point
- current raid or danger pressure

That improves the push-versus-extract decision.

## 6. Recommended Implementation Order

This order should also align to the broader gameplay-act structure:

- Act 0 teaches the loop
- Act 1 establishes frontier travel, extraction, and first claims
- Act 2 makes return-and-upgrade stronger
- Act 3 deepens specialization and infrastructure identity
- Act 4 stress-tests the world layer through raids and defense
- Act 5 expands into mastery, spectacle, and long-tail systems

Related act planning documents:

- `docs/Act_0_Arrival_Plan.md`
- `docs/Act_1_Frontier_Expansion_Plan.md`
- `docs/Act_2_Crafting_and_Robots_Plan.md`

### Phase 1: Strengthen Prep and Return Readability

- expedition board
- stronger banked-haul summary
- clearer frontier risk HUD

### Phase 2: Stronger Post-Return Content Refresh

- settlement-triggered quest refresh
- outpost-triggered quest refresh
- raid-triggered town or guild work

### Phase 3: Distinct Launch Point Identities

- town launch benefits
- outpost launch benefits
- structure-based local services

### Phase 4: Fold Guild and Creator Systems Into the Loop

- guild jobs on return
- guild-owned outpost responsibilities
- player-authored contracts
- created NPC quest issuers

## 7. Success Criteria

The loop is properly aligned when a player can naturally answer:

- What am I preparing for right now?
- What do I risk losing if I stay out longer?
- What did returning alive just unlock?
- What is now better in town or in my outposts?
- What is the next sensible reason to head back out?

If those answers are obvious during play, the loop is healthy.

## 8. Near-Term Priority

If only one area is addressed next, it should be:

- `Prepare expedition`

because that is currently the least explicit part of the loop and the easiest place to make the entire game feel more deliberate.
