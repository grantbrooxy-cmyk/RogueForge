# RogueForge Act 2 Plan: Crafting and Robots

## 1. Purpose

This document defines Act 2 as the phase where RogueForge shifts from simple frontier survival into deeper economy, team building, and settlement specialization.

Act 2 should make the player feel that returning from expeditions no longer only restores the town. It now grows the player’s economy, expands the roster, and opens meaningful build and equipment choices.

Core loop Act 2 reinforces:

1. Prepare in Ironhaven or an owned outpost
2. Travel to the frontier or deeper territory
3. Explore, fight, gather, and pursue quests
4. Decide whether to push deeper or extract
5. Bank resources and progress
6. Upgrade town, gear, robots, bases, and world state
7. Unlock new zones, systems, and opportunities
8. Repeat

## 2. Act Identity

### Act

- Act 2

### Name

- Crafting and Robots

### Function

- economy-deepening phase
- roster-building phase
- specialization phase

### Goal

- player builds economy and team

## 3. Unlocks

Act 2 should unlock:

- crafting system depth
- Forge Core upgrades
- Robot evolution MK-II
- equipment tiers
- blueprint fragments
- new zones
- side quests
- recruitment quests
- settlement facilities

These are the systems that turn success in the field into long-term progression choices.

## 4. Ironhaven State

By the end of early-to-mid Act 2, Ironhaven should meaningfully function as a full progression hub.

Town facilities now expected:

- Forge Core
- Workshop
- Apothecary
- Tavern
- Hangar
- Archive
- Training Grounds

Not all of these need fully bespoke screens immediately, but they should have clear mechanical purpose in the act structure.

## 5. Player Progression Focus

Act 2 player progression should emphasize:

- craft gear
- upgrade robots
- recruit robots
- expand settlement
- build better outposts
- push into harder zones

The key shift is:

- Act 1 teaches footholds
- Act 2 teaches investment and specialization

## 6. Current Build Mapping

The current build already supports large parts of this phase.

### Already Present

- Forge component economy
- forge recipes and crafting support
- Workshop and Forge screens
- Forge Core progression state in [GameState.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/core/GameState.java)
- Robot evolution framework in [RobotEvolutionManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/progression/RobotEvolutionManager.java)
- MK-II and MK-III robot definitions in [robots.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/robots.json)
- recruitment events in [recruitment.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/recruitment.json)
- archive panel support in [WorkshopScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/WorkshopScreen.java)
- settlement upgrades and visible town changes
- side-quest and world-state scaffolding
- stronger outpost and base systems from Act 1

### Current Best-Fit Content

The existing content that most closely maps to Act 2 is:

- post-annex settlement growth
- Forge Core boss-count milestone hooks
- robot recruitment events in deeper zones
- Workshop and Forge progression loops
- equipment tier growth and fusion support

## 7. Current Gaps

Act 2 systems exist, but the phase is not yet clearly presented as the “economy and team building” act.

Main gaps:

- crafting exists mechanically, but is not yet introduced as a clean post-Act-1 progression phase
- blueprint fragments are not yet clearly surfaced as a distinct progression currency or discovery loop
- recruitment quests exist as events, but not yet as a strong named Act 2 quest pillar
- Forge Core upgrades are implemented, but not yet staged as the central act progression ladder
- several desired facilities exist more as design targets than as clearly unlocked settlement beats
- the transition from “frontier footholds” to “team/economy mastery” is still implicit

## 8. Recommended Act 2 Structure

The clean Act 2 flow should be:

### Step 1: Settlement Stabilization

- Act 1 footholds are now reliable
- Ironhaven supports repeated returns and upgrades
- player understands the town is now a real progression machine

### Step 2: Crafting Becomes Core

- player starts crafting meaningful gear upgrades
- better materials and fragments become worth chasing
- equipment choices begin to affect build identity

### Step 3: Forge Core Progression Matters

- player reaches or chases Forge Core Lv2
- robot evolution and extra team options become visible goals
- town progression starts to feel layered rather than linear

### Step 4: Recruitment and Team Expansion

- new robot recruits become major rewards
- reserve roster grows
- player starts thinking in roles and team composition, not just survival

### Step 5: New Facilities Reinforce Specialization

- Hangar supports reserve and deployment identity
- Archive supports knowledge and bestiary identity
- Training Grounds supports mastery identity
- Tavern supports side-quest and social-world identity

### Step 6: Harder Zones Open

- new zones now matter because they feed crafting, recruitment, and evolution
- the player pushes outward for better materials and stronger robot paths

### Step 7: Repeat with More Agency

- every return can now improve gear, bots, facilities, and long-term world position

## 9. Success Criteria

Act 2 is successful when the player clearly understands:

- field resources now feed real gear progression
- Forge Core upgrades gate meaningful new robot growth
- recruitment is a major long-term reward path
- settlement growth now supports economy and team building, not only survival
- harder zones are worth entering because they power stronger builds and rosters

If those lessons land, the player is ready for the stronger infrastructure and industrial specialization of Act 3.

## 10. Immediate Recommendation

Do not treat Act 2 as only “bigger numbers after Act 1.”

Treat it as the phase where the player learns:

- my economy matters
- my roster matters
- my crafting choices matter
- Ironhaven is becoming a true command center

The next practical cleanup after this definition is:

1. make Forge Core progression the explicit spine of Act 2
2. surface blueprint fragments and recruitment as named progression loops
3. make the missing facilities feel like visible settlement unlocks rather than only design intent
