# RogueForge Act 3 Plan: Fusion and Advanced Systems

## 1. Purpose

This document defines Act 3 as the point where RogueForge stops being primarily about surviving and stabilizing the frontier and starts becoming about mastering ancient infrastructure.

Act 3 should make the player feel powerful, fast, and strategically expressive.

The player is no longer only reacting to dangerous terrain or harder bosses. They are now:

1. fusing gear into stronger forms
2. evolving robots into MK-III roles
3. using advanced abilities and elemental pressure
4. exploiting mobility routes the early game could not reach
5. building specialized frontier infrastructure that supports deeper pushes

## 2. Act Identity

### Act

- Act 3

### Name

- Fusion and Advanced Systems

### Function

- power-spike phase
- mobility phase
- infrastructure-mastery phase

### Goal

- player becomes powerful and mobile

## 3. Unlocks

Act 3 should clearly unlock or foreground:

- equipment fusion as a major long-term sink
- MK-III robot evolution and reserve roster upgrades
- advanced and unique robot abilities
- elemental break pressure as a readable combat payoff
- environmental interaction abilities in more dangerous regions
- special outpost structures with stronger utility
- flying mounts and dragon-riding routes
- air routes into elevated or isolated zones
- advanced crafting stations and high-tier forge recipes

## 4. World Changes

The world should feel materially different once Act 3 begins.

Expected changes:

- travel becomes faster
- dragon roosts and air routes shorten route friction
- special zones become reachable through sky-route or command-rail progress
- dangerous regions stop feeling like distant landmarks and become active destinations
- world-state events start reinforcing that the old machine network is waking up

## 5. Current Build Mapping

The current build already contains a strong base for this act.

### Already Present

- Act 3 main quest chain in [quests.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/quests.json)
- dragon-riding unlock event in [story_events.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/story_events.json)
- MK-III robot definitions in [robots.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/robots.json)
- unique robot ability definitions in [abilities.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/abilities.json)
- elemental break handling in [CombatResolver.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/CombatResolver.java) and [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java)
- fusion support in [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java) and [ForgeScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/ForgeScreen.java)
- advanced settlement upgrades in [settlement_upgrades.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/settlement_upgrades.json)
- advanced zone routing through [dialogue.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/dialogue.json) and [zones.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/zones.json)

### Content Added In This Pass

- missing Act 3 and late-game world-state defaults were registered in [world_state.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/world_state.json)
- advanced forge recipes were added in [forge_recipes.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/forge_recipes.json)
- advanced outpost structures were added in [StructureDefinitionRegistry.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/engine/base/StructureDefinitionRegistry.java)

## 6. Intended Player Experience

The player journey through Act 3 should feel like this:

### Step 1: The Frontier Opens Vertically

- the player recovers route intelligence
- dragon roosts and air lanes stop the world from feeling flat
- remote zones become practical destinations instead of long walks

### Step 2: Builds Become Expressive

- fusion and high-tier forging create real build identity
- unique robot skills and weapon arts make party composition matter more
- elemental sequencing gains more tactical value

### Step 3: Infrastructure Becomes Specialized

- command, research, and workshop upgrades stop being generic economy growth
- they now support specific endgame ambitions such as route control, prototype design, and MK-III operations

### Step 4: Dangerous Regions Become Routine Targets

- the player is expected to enter extreme zones
- mobility and system mastery reduce friction enough that danger comes from encounters and decisions, not just distance

## 7. Success Criteria

Act 3 is successful when the player clearly understands:

- I can move across the world much faster than before
- my gear progression is now about fusion and high-tier crafting, not just buying upgrades
- my robots are entering their elite forms and unique-skill phase
- advanced routes and structures make the world feel conquered in layers
- the game is preparing me for final-zone pressure, not just bigger numbers

If those lessons land, the player is ready for Act 4's mythic-forging and final-route demands.
