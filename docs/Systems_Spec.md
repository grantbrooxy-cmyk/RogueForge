# RogueForge Systems Specification

## 1. Purpose

This document expands the final-vision mechanical systems from the architecture document into a development-facing design reference.

Use this document for:

- combat implementation planning
- progression tuning
- equipment and economy balancing
- robot and party design
- system-level feature decomposition

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

The current codebase already includes foundations for:

- combat flow
- screens
- save/load
- robot rosters
- equipment data
- world zones

The final systems vision is substantially larger and includes:

- full CTB behavior
- complete elemental wheel
- full status framework
- complete proficiency-through-use model
- weapon Combat Arts
- 8-weapon mastery support
- tiered crafting stations
- full fusion economy
- endgame and endless modes

## 26. Systems Summary

RogueForge’s final systems design is built around the idea that combat, progression, exploration, and hub growth must continuously feed one another. Battles produce mastery and materials, mastery unlocks deeper tactical expression, exploration yields better opportunities and hidden systems, and every major success returns to Ironhaven as visible progress.
