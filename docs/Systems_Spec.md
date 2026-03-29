# RogueForge Systems Specification

## 1. Purpose

This document expands the final-vision mechanical systems from the architecture document into a development-facing design reference.

Use this document for:

- combat implementation planning
- progression tuning
- equipment and economy balancing
- robot and party design
- system-level feature decomposition

Companion document:

- `docs/Run_World_Meta_Structure.md` for reset rules, extraction logic, outpost purpose, and the boundary between run progression, world persistence, and meta progression
- `docs/State_Implementation_Guide.md` for code-facing storage rules covering save data, world flags, quest progression channels, and meta progression boundaries

## 2. System Overview

RogueForge’s final design is built around six major system layers:

- Conditional Turn-Based combat
- Progression through use
- Robot collection and evolution
- Equipment, crafting, and fusion
- Open-world exploration and environmental interaction
- Hub and settlement growth

These systems must reinforce each other rather than exist in isolation.

## 3. Combat System

### 3.1 Battle Flow

1. Overworld enemy contact begins an encounter
2. Build the combatant list from player party and enemy group
3. Calculate initial turn order
4. Render battle UI, timeline, and combatants
5. Resolve actions through command menus or AI
6. Apply damage, healing, elements, status effects, and reactions
7. Update the timeline based on speed cost
8. Repeat until victory, defeat, or escape
9. Show battle results with progression feedback

### 3.2 Encounter Scale
Final support target:

- Player + up to 3 active robots
- 1-5 enemies

### 3.3 Core Commands

- Attack
- Defend
- Ability
- Item
- Analyze
- Flee

### 3.4 Design Goals
Combat should reward:

- scanning before committing
- party composition
- weakness exploitation
- speed management
- selective defense
- ability timing
- status pressure

## 4. Conditional Turn-Based Timeline

### 4.1 Core Concept
RogueForge uses an FFX-inspired Conditional Turn-Based system rather than fixed-round order.

### 4.2 Timeline Rules

- Every combatant has effective speed
- Every action has a speed cost
- Lower speed cost means faster return to the timeline
- Timeline UI shows the next 8-10 turns
- Turn order must update after every action

### 4.3 Tactical Purpose
The timeline is not only visual feedback. It is a primary strategic layer. Players should routinely choose weaker or defensive options when doing so gives better future positioning.

## 5. Core Command Specifications

### 5.1 Attack
Purpose:

- standard physical damage action

Characteristics:

- uses equipped weapon
- low-complexity command
- speed cost: 80
- affected by weapon proficiency
- selects a living enemy target

### 5.2 Defend
Purpose:

- short-term survival and tempo manipulation

Characteristics:

- physical damage taken: -50%
- magical damage taken: -25%
- speed cost: 40
- removed at the start of the next turn

### 5.3 Ability
Purpose:

- primary tactical command

Characteristics:

- opens learned ability list
- only abilities off cooldown can be selected
- abilities show proficiency, element, target type, and speed cost
- speed cost varies by spell class and power

### 5.4 Item
Purpose:

- reliable consumable use

Characteristics:

- speed cost: 70
- never misses
- includes healing, cure, revive, buff, and damage items

### 5.5 Analyze
Purpose:

- enemy knowledge and bestiary progression

Characteristics:

- speed cost: 50
- scan levels 1-3
- Scout-role advantage on scanning

### 5.6 Flee
Purpose:

- tactical retreat from non-boss battles

Characteristics:

- disabled in boss battles
- failed flee has high speed cost
- repeated attempts gain bonus chance

## 6. Damage Formulas

### 6.1 Physical Damage

Target design formula:

- `baseDamage = (ATK * weaponMultiplier) - (DEF * 0.5)`
- `weaponMultiplier = 1.0 + (weaponProficiency * 0.05)`
- apply crit
- apply defend mitigation
- apply small variance
- minimum final damage is 1

### 6.2 Ability Damage

Target design formula:

- `baseDamage = abilityPower * (ATK * 0.6 + INT * 0.4) / DEF`
- multiply by element result
- multiply by proficiency multiplier
- apply small variance

### 6.3 Healing

Target design formula:

- `healAmount = abilityPower * (INT * 0.8 + ATK * 0.2) * proficiencyMultiplier`

### 6.4 Critical Hit Model

- base chance: 5%
- bonus chance: +1% per 10 SPD
- crit multiplier: 1.5x

## 7. Elemental System

### 7.1 Elements

- Fire
- Ice
- Lightning
- Earth
- Wind
- Water

### 7.2 Element Outcomes

- weakness: 1.5x
- neutral: 1.0x
- resistance: 0.5x
- absorb: nullify or convert to benefit depending on implementation handling

### 7.3 Element Pairings

- Fire is strong against Ice and Wind, weak into Water and Earth
- Ice is strong against Wind and Lightning, weak into Fire and Earth
- Lightning is strong against Water and Wind, weak into Earth and Ice
- Earth is strong against Fire and Lightning, weak into Wind and Water
- Wind is strong against Earth and Water, weak into Fire and Ice
- Water is strong against Fire and Earth, weak into Lightning and Wind

### 7.4 Elemental Break
Three consecutive hits of the same element on one target trigger Elemental Break, reducing that target’s resistance tier for that element for the rest of the encounter.

## 8. Status Effect Framework

### 8.1 Status List

- Burn
- Poison
- Bleed
- Freeze
- Stun
- Paralyze
- Slow
- Blind
- Silence
- Weaken
- Haste
- Protect
- Shell
- Regen
- Berserk

### 8.2 Mechanical Roles

- Burn: DoT plus defense reduction
- Poison: heavier sustained DoT
- Bleed: punishes action-taking
- Freeze: turn denial plus fragility
- Stun: hard turn denial
- Paralyze: chance-based disruption
- Slow: speed and timeline suppression
- Blind: physical accuracy loss
- Silence: ability lockout
- Weaken: attack suppression
- Haste: speed acceleration
- Protect: physical mitigation
- Shell: magical mitigation
- Regen: sustained recovery
- Berserk: damage increase with command restriction

### 8.3 Stacking Rules

- damage-over-time effects stack independently
- control effects replace or override each other
- same-type buff/debuff applications refresh duration
- opposing statuses cancel where appropriate

## 9. Battle Results Screen

### 9.1 Purpose
The results screen is a progression celebration layer, not just a summary panel.

### 9.2 Reward Categories

- player XP
- gold
- ability proficiency gains
- weapon proficiency gains
- bestiary updates
- item drops
- equipment drops
- level-up notifications
- unlock notifications

### 9.3 UX Goal
Players should leave a battle understanding not only what they earned, but how their party changed.

## 10. Ability Proficiency System

### 10.1 Core Rule
Abilities gain proficiency by being used in battle.

### 10.2 Proficiency Levels

- Level 1 to Level 10

### 10.3 XP Thresholds

- 1: 0
- 2: 50
- 3: 120
- 4: 220
- 5: 380
- 6: 600
- 7: 900
- 8: 1300
- 9: 1800
- 10: 2500

### 10.4 Progression Benefits

- more damage or healing
- lower cooldowns
- secondary effects
- upgraded naming
- evolved forms
- final Unique Skill transformation

### 10.5 XP Formula Goal
Training on stronger enemies should be more valuable than farming weak ones.

Target rule:

- `baseXP = 10 + (enemyRank - abilityLevel) * 5`
- lower-rank enemies give reduced returns
- very high-rank enemies grant bonus growth

## 11. Unique Skill Evolution

### 11.1 Rule
At proficiency level 10, a mastered ability evolves into a Unique Skill.

### 11.2 Party Constraint
Only one instance of each Unique Skill can exist in the active party at once.

### 11.3 Example Evolutions

- Dash -> Phantom Step
- Scan -> Omniscience Eye
- Shield Wall -> Fortress Eternal
- Taunt -> Gravity Well
- Power Strike -> Obliteration
- Rapid Fire -> Bullet Storm
- Heal Pulse -> Life Spring
- Repair Aura -> Genesis Field

### 11.4 Design Function
Unique Skills are:

- specialization capstones
- major identity markers
- powerful late-build differentiators

## 12. Weapon Proficiency System

### 12.1 Weapon Types

- Sword
- Axe
- Lance
- Staff
- Bow
- Fist
- Gun
- Dual-Blade

### 12.2 Growth Model
Weapon proficiency increases through basic attacks and weapon-specific action usage.

### 12.3 Combat Arts
Each weapon unlocks Combat Arts at:

- Level 3
- Level 6
- Level 10

### 12.4 Weapon Identities

- Sword: balanced offense and multi-hit arts
- Axe: heavier power, lower speed
- Lance: armor piercing and jump-style attacks
- Staff: magic amplification and casting manipulation
- Bow: crit and ranged pressure
- Fist: speed and combo offense
- Gun: ranged burst and burn-capable tech
- Dual-Blade: multi-hit and evasion-heavy offense

## 13. Grade System

### 13.1 Grade Ranges

- G: levels 1-10
- F: levels 11-20
- E: levels 21-35
- D: levels 36-50
- C: levels 51-70
- B: levels 71-85
- A: levels 86-95
- S: levels 96-99

### 13.2 Grade Effects
Grades control:

- equipment tier access
- expected zone access
- active party size
- progression pacing

### 13.3 Party Slot Growth

- G: player + 1 robot
- F: player + 2 robots
- E: player + 2 robots
- D+: player + 3 robots

## 14. Robot System

### 14.1 Design Role
Robots are the core progression expression system.

Robots are defined by:

- model line
- role
- equipment loadout
- ability usage history
- weapon specialization
- evolution state

### 14.2 Robot Roles

- Tank
- DPS
- Support
- Scout

### 14.3 Robot Evolution
Target path:

- MK-I
- MK-II
- MK-III

Target effects:

- MK-II: about +20% stats, 3 ability slots
- MK-III: about +40% stats, 4 ability slots

Requirements:

- Forge Core level
- materials
- advanced upgrade access

### 14.4 Discoverable Robots
Final content scope includes 10 discoverable robots in addition to starting party foundations.

## 15. Exploration Systems

### 15.1 Overworld Encounters
Enemies exist in the world and trigger battles on contact.

### 15.2 Zone Structure
Zones contain:

- spawn points
- transitions
- enemy placements
- NPCs
- chests
- environmental objects
- secrets

### 15.3 Environmental Interaction
Abilities and robot functions should affect world traversal.

Examples:

- fire clears barriers
- scan reveals passages
- strength moves heavy obstacles

### 15.4 Traversal Upgrades
Dragon Riding serves as a major late-game traversal unlock.

## 16. Bestiary and Analyze Systems

### 16.1 Scan Levels

- level 1: basic info
- level 2: full combat stats
- level 3: drop tables and complete scan data

### 16.2 Archive Function
Bestiary data should feed:

- enemy mastery
- lore completion
- bounties
- completionist progression

## 17. Equipment System

### 17.1 Equipment Tiers

- Tier 1: Common
- Tier 2: Uncommon
- Tier 3: Rare
- Tier 4: Epic
- Tier 5: Legendary
- Mythic

### 17.2 Slot Structure

- Head
- Body
- Arms
- Legs
- Weapon
- Accessory

### 17.3 Slot Identity

- Head: HP, INT, status resistance, scan utility
- Body: HP and DEF anchor slot
- Arms: offense/defense balance
- Legs: speed and evasive identity
- Weapon: attack identity and proficiency ownership
- Accessory: flexible stat and passive boost slot

### 17.4 Tier Intent

- Tier 1: starter gear
- Tier 2: early specialization
- Tier 3: meaningful build identity
- Tier 4: elite progression and boss power
- Tier 5: late-game chase
- Mythic: one-of-a-kind challenge rewards

## 18. Unique Boost System

### 18.1 Purpose
Unique Boosts make high-tier gear feel build-defining instead of merely stronger.

### 18.2 Example Boosts

- Burn
- Freeze
- Shock
- Drain
- Reflect
- Regen
- Haste
- Counter
- elemental absorbs
- crit up
- gold/XP bonus
- Last Stand
- Auto-Revive

### 18.3 Distribution Goal
Unique Boosts should appear more often as tier rises, with Mythic gear guaranteeing highly distinctive identity.

## 19. Crafting System

### 19.1 Crafting Purpose
Crafting converts exploration and combat rewards into long-term progression rather than disposable loot churn.

### 19.2 Crafting Stations

- Ironhaven Workshop
- Ember Forge
- Frost Anvil
- Crystal Forge
- Sky Workshop
- Ancient Foundry
- Master Workshop

### 19.3 Station Logic
Stations gate:

- item tier
- element specialization
- recipe family
- robot evolution access

### 19.4 Recipe Inputs
Recipes combine:

- common field materials
- rare regional materials
- boss materials
- currency costs

## 20. Equipment Fusion

### 20.1 Core Rule
Kira can fuse two items of the same tier and slot into one item of the next tier.

### 20.2 Fusion Output

- inherits the best stat values from parents
- rolls for Unique Boost chance or outcome
- costs escalating gold by tier

### 20.3 Design Goal
Fusion creates a gear sink, a long-term chase loop, and a reason to care about duplicate gear.

## 21. Economy

### 21.1 Resource Layers

- gold
- crafting materials
- rare cores
- region-specific reagents
- boss-drop materials

### 21.2 Economy Goals

- early game should feel generous enough to explore
- mid-game should force trade-offs
- late-game should support big-ticket investment without trivializing decisions

### 21.3 Shops
Shops should reflect:

- grade progression
- zone progression
- world-state unlocks
- rotating special inventory

### 21.4 Wandering Merchant
Jax provides:

- rotating stock
- premium pricing
- exclusive access to hard-to-find goods

## 22. Hub Systems

### 22.1 Ironhaven Services

- robot management
- equipment services
- potion and status support
- quest and rumor intake
- bestiary and lore access
- training
- reserve storage

### 22.2 Hub Progression
Boss clears and settlement quests should unlock:

- new service depth
- stronger stock
- more NPCs
- visible structural change

## 23. Quest and Reward Systems

### 23.1 Main Story Rewards
Main story quests should unlock:

- key progression routes
- Forge Core levels
- major traversal features
- key systems like MK-III access

### 23.2 Side Quest Rewards
Side quests should give:

- gold
- blueprints
- new NPCs
- building upgrades
- robots
- shortcuts
- archive entries
- unique gear

## 24. Boss and Endgame Systems

### 24.1 Boss Design Goals
Bosses should:

- introduce or stress a mechanical idea
- feel distinct from normal encounters
- unlock world or hub progress
- create memorable reward spikes

### 24.2 Multi-Phase Bosses
Late-game bosses should use multi-phase encounters to escalate mechanical complexity and spectacle.

### 24.3 Final Boss
Origin Core’s target structure:

- Phase 1: physical + summons
- Phase 2: rotating elemental weakness
- Phase 3: status and time manipulation
- 10% HP trigger: Genesis Reset

### 24.4 Post-Game
Final vision post-game includes:

- Infinite Dungeon
- The Beyond
- superbosses
- Mythic chase content

## 25. Current Build vs Final Systems Vision

All five planned development phases are complete. The codebase now implements the full production scope of every major system layer.

### 25.1 Implemented Systems (Production-Complete)

| System | Implemented State |
|---|---|
| Combat engine | 28 combat classes, CTB-lite timeline, Attack/Defend/Ability/Item/Analyze/Flee commands, 1–5 enemy encounters, full results screen with XP/gold/proficiency/drops |
| Elemental system | Six elements (Fire, Ice, Thunder, Wind, Earth, Water), weakness/resistance/absorb resolution in CombatResolver.java |
| Status system | 15 status effects with stacking and counter rules in StatusEffectManager.java |
| Ability proficiency | Level 1–10 through use; evolution to Unique Skill via AbilityEvolutionManager.java |
| Weapon proficiency | 8 weapon families tracked via WeaponProficiencyTracker.java; 8-track mastery support present |
| Robot system | 5 families × 3 tiers = 15 robot variants; active party + reserve roster; deployment fully operational |
| Discoverable robots | 10 recruitment events wired to in-world chests, NPCs, and story events |
| Robot evolution | MK-I → MK-II → MK-III framework via RobotEvolutionManager.java |
| Crafting | Full multi-station pipeline: ForgeScreen.java, ForgeRecipeDefinition.java, ForgeComponentDefinition.java, ForgeIngredientDefinition.java |
| Equipment fusion | Fusion framework present in WorkshopScreen.java |
| Overworld exploration | 16 production zones with TMX maps, NPCs, chests, doors, story events, and enemy spawns |
| Environmental interaction | burn_barrier, strength_boulder, scan_hidden_path — all three types wired in GameScreen and present in 6 TMX maps |
| Dragon Riding | Traversal network across 7 TMX files; unlocked by dragon boss defeat; gated by `frontier.dragon_roosts_active` |
| Hub settlement | 4 facilities × 3 tiers = 12 upgrades; SettlementManager wired; all 7 Ironhaven NPCs implemented |
| Quest system | 34 quests: 10 main (4 acts) + 24 side (5 categories); all steps verified by Python cross-validation |
| Dialogue system | 112 entries across 28 NPCs; full priority-gating, quest-state conditions, and world-flag effects |
| Story event system | 30+ events: 9 zone intros, 11 boss defeats, 3 frontier-state triggers |
| Bestiary / Analyze | 3 scan levels, persistence via BestiaryManager.java |
| Save / load | Broad persistent state across all systems via SaveManager.java |
| Endgame content | The Void with Memory Lane, Gauntlet of Trials, Throne of Origin; Infinite Dungeon with procedural floor generation; 4 S-rank superbosses |

### 25.2 Remaining Gaps vs Final Vision

These items are aspirational scope beyond the five-phase plan — the difference between current production and the full final vision:

| Gap | Current State | Final Vision Target |
|---|---|---|
| Elemental Break mechanic | Not yet implemented | Breaks opponent elemental resistance for one round |
| CTB timeline depth | CTB-lite functional for current content | Full FFX-depth CTB with per-ability speed costs |
| Combat Arts | Generic placeholder actions | Weapon-specific real actions per weapon family |
| Quest count | 34 quests (5 categories) | 60+ quests (8 categories including Monster Bounty, Arena Challenge, NPC Rescue) |
| Zone count | 16 zones | 25+ world spaces |
| Equipment tiers | Tiers 1–4 in production | Tier 5 and Mythic tier |
| Fusion economy | Framework present | Full tier-to-tier fusion gating |
| Archive integration | BestiaryManager persists data | Archive screen hub integration |
| Forge Core milestones | Upgrade via quest chain | Boss-count gating (5/10/15 bosses → Lv2/3/4) |
| Enemy variety | Broad rank ladder, 4 S-rank superbosses | Larger per-zone enemy roster |

## 26. Roguelike Loop: Shard Runs + Forge Legacy

The name RogueForge promises a roguelike identity. The two-system design below delivers that identity while preserving the persistent open-world and quest content already built.

### 26.1 Design Philosophy

Two systems operate in tandem:

- **Shard Runs** provide the roguelike pressure — a stripped, high-stakes dungeon mode where every run is different and death is cheap.
- **Forge Legacy** provides the roguelike reward — a meta-progression layer where death permanently unlocks something, so every wipe moves the player forward.

Neither system requires touching the main overworld or quest content. Shard Runs use the existing Infinite Dungeon as their arena. Forge Legacy uses the existing Ironhaven settlement as its reward hub.

---

### 26.2 Shard Runs

#### What It Is

The player enters the Infinite Dungeon in Shard Run mode via a dedicated terminal in Ironhaven. On entry, their full equipment and robot loadout is stripped. They begin a run with a baseline kit and build from scratch by clearing floors.

#### Entry State

- Player starts with: starter weapon, one MK-I robot (chosen from 3 random options), 100 gold, no abilities evolved
- All Forge Legacy unlocks (see 26.3) are applied at entry as passive permanent bonuses
- Active quest progress, overworld items, and regular save state are untouched — Shard Runs exist in a parallel state layer

#### Floor Structure

Each floor has a fixed structure:

1. **Combat rooms** (3–5 per floor) — standard encounters scaled to floor depth
2. **Loot room** — chest with gear, crafting material, or a Shard Module (see below)
3. **Merchant room** — wandering trader offers 3 items for gold dropped in the run
4. **Boss room** — named floor boss; clearing it advances to the next floor

Floor count is unlimited. Difficulty scales continuously. Floors 1–5 are Starter, 6–15 are Frontier, 16–30 are Deep Forge, 31+ are Abyss.

#### Shard Modules

Shard Modules are run-only passive upgrades found in loot rooms or purchased from floor merchants. They stack within a run and are lost on death or exit.

Examples:

| Module | Effect |
|---|---|
| Tempered Core | +15% physical damage this run |
| Overclock Cell | Robot acts twice on turns where HP > 75% |
| Recycler Unit | Gain 10 gold whenever an enemy is defeated by a status effect |
| Breaker Circuit | First elemental hit each battle applies a free Elemental Break |
| Ghost Protocol | Once per run, revive with 25% HP instead of dying |
| Volatile Alloy | All crits deal +40% damage but player takes +10% damage |

Modules form the run-to-run variation that makes each attempt feel distinct. Players draft a build from random offerings rather than arriving with a fixed loadout.

#### Robot Drafting

During a run, the player can find or buy additional robots (MK-I only at start). Robot grade, species, and role rotate randomly per floor set. Evolving a robot within a run (MK-I → MK-II → MK-III) is possible using materials found in the dungeon — evolution does not require Ironhaven facilities mid-run.

#### Death

On death, the run ends immediately. The player returns to Ironhaven. They receive:

- **Forge Shards** — the meta currency of Forge Legacy, earned based on floors cleared (see 26.3)
- **Run Echo** — a summary card showing floors cleared, peak damage, modules collected, and cause of death
- No penalty to overworld progress, gold, or equipment

#### Successful Exit

The player can exit a Shard Run at any staircase by choosing "Seal Run" instead of descending. On exit:

- Forge Shards are awarded at 1.5× the death rate
- One **Sealed Cache** drops — a chest delivered to the player’s Ironhaven storage containing run-quality loot (scales with floor reached)
- The run is marked complete; a new run can be started immediately

---

### 26.3 Forge Legacy (Meta-Progression)

#### What It Is

Forge Legacy is a permanent unlock tree funded by Forge Shards. Every run — successful or not — contributes Shards. The tree represents the cumulative knowledge and hardware the player has salvaged across all their attempts.

Forge Legacy unlocks persist across all runs and across the full save file. They apply to both Shard Runs (as starting bonuses) and the overworld (as passive stat improvements).

#### Forge Shard Economy

| Outcome | Forge Shards Awarded |
|---|---|
| Each floor cleared | 10 Shards |
| Each boss defeated | 25 Shards |
| Successful exit (Seal Run) | 50 bonus Shards |
| Death on floors 1–5 | 0 bonus Shards |
| Death on floors 6–15 | 15 bonus Shards |
| Death on floors 16–30 | 35 bonus Shards |
| Death on floors 31+ | 60 bonus Shards |

#### Legacy Tree Structure

The tree has four branches. Each node costs an escalating Shard amount and unlocks permanently.

**Branch A — Forge Foundation (starting kit improvements)**

| Node | Cost | Effect |
|---|---|---|
| Better Alloy | 50 | Start with a Tier 2 weapon instead of Tier 1 |
| Reserve Cell | 100 | Start with one extra consumable |
| Crew of Two | 150 | Start with 2 robot choices instead of 3 random options (pick 1 of 2) |
| Veteran’s Edge | 250 | Start with one random Shard Module already equipped |
| Full Crew Draft | 400 | Start with 3 robot choices |
| Loaded Manifest | 600 | Start with 200 gold instead of 100 |

**Branch B — Echo Proficiency (carry-over of skills)**

| Node | Cost | Effect |
|---|---|---|
| Muscle Memory | 75 | Carry 1 Ability at Proficiency Lv1 into each run |
| Deep Conditioning | 200 | Proficiency carry-in increases to Lv3 |
| Weapon Echo | 300 | Carry 2 Weapon Proficiency levels (of most-used weapon type) into each run |
| Full Recall | 500 | Carry proficiency for 2 Abilities and 1 Weapon type |

**Branch C — Ironhaven Legacy (overworld permanent bonuses)**

| Node | Cost | Effect |
|---|---|---|
| Toughened Frame | 100 | +5% max HP in overworld and Shard Runs |
| Resonance Tuning | 150 | +5% elemental ability damage globally |
| Salvage Protocol | 200 | +15% gold from all enemy drops (overworld and runs) |
| Hardened Core | 350 | +10% physical damage globally |
| Adaptive Systems | 500 | All robots gain +1 base speed |
| Legacy Alloy | 800 | +1 to all equipment tiers found in the overworld (min. Tier 2) |

**Branch D — Module Inheritance (unlock rare module pool)**

| Node | Cost | Effect |
|---|---|---|
| Module Archive I | 100 | Adds 3 rare Shard Modules to the run loot pool |
| Module Archive II | 250 | Adds 4 more rare modules to the pool |
| Module Archive III | 500 | Adds the full Legendary module set to the pool |
| Cursed Catalogue | 400 | Unlocks Cursed Modules — powerful effects with a downside; required for the highest floor depths |

#### Ironhaven Integration

Forge Shards are spent at a new Ironhaven facility: the **Legacy Vault**, located adjacent to the Forge Core. The Legacy Vault NPC (working name: **Coda**) manages the tree and provides run history. The Vault becomes available after the player first attempts a Shard Run, regardless of floor reached.

---

### 26.4 Shard Run Modifiers (Advanced)

Once the player has cleared floor 15 at least once, the Shard Run terminal offers optional **Run Modifiers** — voluntary handicaps that multiply Shard rewards.

| Modifier | Shard Multiplier | Effect |
|---|---|---|
| Iron Pact | ×1.5 | No consumable items in the run |
| Stripped | ×1.5 | Module slots reduced from 6 to 3 |
| Solo | ×2.0 | No robots — player character only |
| Cursed Clock | ×2.0 | Each floor has a turn limit; exceeding it wipes the run |
| All Hazards | ×2.5 | All four hazard modifiers active simultaneously |

Modifiers stack multiplicatively. A full-hazard Solo Cursed Clock run is ×6.0 — designed for players who have exhausted the standard depth ceiling.

---

### 26.5 Integration Points with Existing Systems

| Existing System | Integration |
|---|---|
| Infinite Dungeon | Shard Runs use the same procedural floor generator; Shard Run mode is a flag passed at entry |
| SettlementManager | Legacy Vault is a fifth facility added to Ironhaven; follows the same tier-upgrade pattern |
| ForgeRecipeDefinition | Sealed Cache loot uses the existing chest/loot framework |
| AbilityEvolutionManager | Branch B carry-in applies proficiency at run start via a pre-combat hook |
| SaveManager | Forge Shard balance, Legacy tree state, and run history are new persistent save fields |
| Dialogue system | Coda (Legacy Vault NPC) uses the same NPC dialogue JSON format with run-history-aware flags |
| BestiaryManager | Shard Run kills count toward Bestiary completion |

---

### 26.6 New Flags and State

New world flags introduced by this system:

| Flag | Set By | Effect |
|---|---|---|
| `meta.shard_run_unlocked` | First entry into Shard Run terminal | Enables Legacy Vault in Ironhaven |
| `meta.floor15_cleared` | Floor 15 boss defeated in any run | Unlocks Run Modifier menu |
| `meta.floor30_cleared` | Floor 30 boss defeated in any run | Unlocks Cursed Module pool |
| `meta.legacy_branch_X_maxed` | All nodes in branch X purchased | Cosmetic reward + Coda dialogue chain |

---

## 27. Systems Summary

RogueForge’s final systems design is built around the idea that combat, progression, exploration, and hub growth must continuously feed one another. Battles produce mastery and materials, mastery unlocks deeper tactical expression, exploration yields better opportunities and hidden systems, and every major success returns to Ironhaven as visible progress.

The Shard Run / Forge Legacy layer adds a second axis: every death produces permanent progress. The roguelike loop and the open-world RPG loop reinforce each other — players who run the dungeon become stronger in the overworld, and players who push the overworld unlock better starting conditions for dungeon runs.
