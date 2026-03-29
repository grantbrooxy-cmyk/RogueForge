# RogueForge Game Design Document

## 1. Document Purpose

This GDD reflects the intended final vision of RogueForge based on the Version 3 architecture document, not just the current playable build.

It is meant to serve as:

- A high-level design reference for the full game
- A content and systems alignment document
- A product-vision companion to the technical architecture and milestone roadmap

Companion documents:

- `docs/Narrative_Bible.md` for story, world, zones, factions, NPCs, and quest structure
- `docs/Systems_Spec.md` for combat, progression, economy, equipment, and hub systems
- `docs/Run_World_Meta_Structure.md` for the relationship between roguelite expeditions, persistent world building, and meta progression

Where useful, this document distinguishes between:

- Final Vision: the target shipped game
- Current Build Snapshot: what exists today in the codebase

## 2. High Concept

### Title
RogueForge

### Genre
Turn-based dungeon crawler RPG with robot companions, open-world exploration, hub progression, and layered mastery systems.

### Platform
Desktop PC, built in Java 11 with LibGDX and Gradle.

### Core Fantasy
The player is a human Forge Master who discovers, repairs, customizes, and commands a party of robot companions to explore Mechara, a vast post-technological world full of buried machinery, automated forges, ruined civilizations, giant robotic constructs, and interconnected dungeon complexes.

### Elevator Pitch
RogueForge combines Final Fantasy-style tactical turn-based combat, SAO-style progression-through-use, and open-world dungeon exploration into a game about rebuilding a lost world through mastery, discovery, and machine companionship.

## 3. Vision Statement

RogueForge is a tactical open-world RPG where every battle deepens mastery, every zone hides new secrets, every robot develops differently based on player use, and every major victory visibly restores the world around Ironhaven.

## 4. Design Pillars

### 4.1 Final Fantasy Combat Depth
Combat should feel like a tactical puzzle, not a stat check.

Key principles:

- Conditional Turn-Based timeline inspired by FFX
- Multi-enemy combat with individual targeting
- Turn manipulation through speed cost
- Front-row / back-row battle readability
- Elemental matchups and chain pressure
- Status effects as real tactical tools, not minor modifiers
- Party composition and command choice mattering every encounter

### 4.2 Progression Through Use
Robots do not simply buy skills from trees. They develop through repeated use.

Key principles:

- Each ability gains proficiency from use
- Each weapon type gains its own proficiency track
- Repeated specialization creates distinct robot builds
- Mastered abilities evolve into unique signature skills
- Discovery-based unlocks are more important than passive point spending

### 4.3 Open-World Exploration
The world should invite wandering, experimentation, and route planning.

Key principles:

- Non-linear world structure
- Natural difficulty gating through enemy strength and zone threat
- Hidden areas, rare materials, and optional bosses
- Ability-based environmental interactions
- Dynamic world-state changes tied to boss clears and quest completion

## 5. Core Experience Goals

The player should feel like:

- A tactical commander making meaningful combat decisions
- A master mechanic gradually assembling a legendary machine party
- An explorer uncovering ancient systems and buried history
- A restorer whose victories materially transform Ironhaven and the wider frontier

## 6. Core Gameplay Loop

### 6.1 Macro Loop

1. Leave Ironhaven or a field hub
2. Explore zones, dungeons, and world routes
3. Trigger tactical turn-based battles against enemy groups
4. Win rewards: XP, gold, materials, equipment, proficiency growth, bestiary data
5. Unlock new abilities, weapon arts, and robot evolution paths
6. Return to Ironhaven to manage robots, craft, fuse, shop, quest, and upgrade
7. Push into harder zones and story acts

### 6.2 Moment-to-Moment Loop

1. Navigate an overworld zone
2. Search for side paths, chests, NPCs, and combat encounters
3. Enter battle from overworld contact
4. Choose commands from a tactical menu
5. Exploit weaknesses, statuses, and timeline positioning
6. Resolve the encounter and review results
7. Continue forward, retreat, or re-route

## 7. World Overview

### 7.1 Setting: Mechara
Mechara is a reclaimed technological world where nature has grown over the ruins of a lost machine civilization. Ancient forges, massive constructs, floating citadels, automated facilities, and buried archives remain scattered across the landscape.

The tone blends:

- Lush wilderness
- Ruined technology
- Mechanical mysticism
- Frontier rebuilding
- Large-scale ancient mystery

### 7.2 World Structure
The world is arranged in a hub-and-spoke layout centered on Ironhaven, with outward branches and lateral loop connections that create shortcuts and non-linear progression opportunities.

Final vision scope:

- 25+ distinct zones
- 5 difficulty tiers
- Main-path progression plus optional detours
- Hidden routes, secret bosses, and revisitable gated areas

### 7.3 Exploration Philosophy
The player is encouraged, not forced, to follow the main quest path.

The world should support:

- Suggested progression
- Early peeks into dangerous regions
- Ability-gated or knowledge-gated secrets
- Retroactive discovery after gaining new traversal or interaction tools

## 8. Hub Settlement: Ironhaven

### 8.1 Narrative Role
Ironhaven is the player’s home base and the central symbol of the rebuilding theme. It is built around an ancient Forge Core and expands as the player defeats major bosses and restores world systems.

### 8.2 Final Vision Facilities

- Forge Core
- Workshop
- Apothecary
- Tavern
- Residential Quarter
- Training Grounds
- Archive
- Hangar

### 8.3 Functional Roles

- Forge Core: robot crafting, upgrading, evolution, advanced blueprints
- Workshop: equipment crafting, fusion, and gear services
- Apothecary: consumables and status recovery items
- Tavern: quests, rumors, character stories, and community texture
- Training Grounds: combat practice and mastery support
- Archive: bestiary and lore records
- Hangar: robot storage, reserve management, and deployment

### 8.4 Expansion Triggers
Ironhaven visually and functionally expands as major bosses are defeated.

Key milestones:

- 5 bosses defeated: Forge Core Lv2
- 10 bosses defeated: Forge Core Lv3
- 15 bosses defeated: Forge Core Lv4

Each expansion should unlock new services, NPCs, or progression pathways.

## 9. Narrative Structure

### 9.1 Story Premise
The player awakens as a Forge Master in a world whose automated systems have collapsed or become corrupted. As the journey unfolds, it becomes clear that the ancient world’s fall is tied to the Origin Core, a catastrophic machine intelligence at the center of Mechara’s history.

### 9.2 Story Format
The main story is structured across four acts and approximately 18 primary story quests.

### 9.3 Main Story Arc

#### Act 1: The Awakening
Level range: 1-20

Focus:

- Learn core systems
- Establish Ironhaven
- Build the first robot party
- Defeat early regional threats
- Obtain the first Seal Fragment

Key beats:

- The Forge Master Awakens
- First robot creation
- Early exploration near Ironhaven
- First major dungeon victories
- First Seal Fragment

#### Act 2: The Corruption Spreads
Level range: 20-50

Focus:

- Expand beyond starter territories
- Clear stronger dungeons
- Recover the remaining Seal Fragments
- Unlock stronger crafting and support systems

Key beats:

- Shadow Caves and Crystal Depths
- Flame Spire and Inferno Djinn
- Frozen Monastery and Abbot Permafrost
- Seal restoration
- Forge Core Lv2

#### Act 3: The Ancient Truth
Level range: 50-80

Focus:

- Discover the truth behind the fallen civilization
- Uncover the Origin Core’s history
- Reach higher-tech and more dangerous regions
- Unlock Dragon Riding and MK-III systems

Key beats:

- Sunken Abyss data archive
- Sky Fortress command center
- Dragon’s Wisdom and Dragon Trials
- Clockwork Sanctum and The Clockmaker
- Obtain the Void Key

#### Act 4: Endgame
Level range: 80-99

Focus:

- Open the final routes
- Forge mythic power
- Enter The Void
- Survive the gauntlet and defeat Origin Core

Key beats:

- Abyssal Rift and The Corruptor
- Volcanic Core and Mythic forging
- The Final Gate
- Memory Lane
- Gauntlet of Trials
- Origin Core final battle

## 10. Major World Regions and Example Zone Path

The final game envisions a much larger world than the current build. The architecture document calls for 16+ major zones in content production, with 25+ total distinct zones and sub-areas in the full vision.

### 10.1 Starter and Early Regions

#### Ironhaven
Starting settlement and progression hub.

#### Verdant Fields
Tutorial region of rolling green hills south of Ironhaven.

Sub-areas include:

- Sunlit Meadow
- Ancient Aqueduct
- Shepherd's Rest
- Windmill Hill
- Collapsed Bridge

Boss:

- Goblin Chief (Rank F)

Secret:

- Hidden cave behind a waterfall containing a rare robot part

#### Whispering Forest
Dense eastern woodland.

Sub-areas include:

- Outer Canopy
- Mushroom Grotto
- Spider's Nest
- Hollow Oak
- Ranger's Lookout
- Moonlit Glade

Bosses:

- Widow Queen (Rank F)
- Arachnid Mother (Rank E)

Secret:

- Buried robot companion in Mushroom Grotto

#### Shadow Caves
Early dungeon route beneath the frontier.

Associated with:

- Crystal Depths
- Mid-early fragment progression
- Hidden machinery and corrupted structures

### 10.2 Mid-Game Regions

#### Scorched Plateau
Volcanic highland with heat hazards and fire enemies.

Sub-areas include:

- Ashen Path
- Lava Flats
- Ember Forge
- Caldera Lake
- Flame Spire

Boss:

- Inferno Djinn (Rank D)

Secret:

- Cooled lava pool hiding Tier 3 weapon schematics

#### Dragon Peak
Towering mountain range with dragons, cliffs, and summit dungeons.

Sub-areas include:

- Foothills
- Wyvern Nests
- Cliffside Path
- Dragon's Roost
- Dragon Sanctum
- Summit Overlook

Boss:

- Elder Drake (Rank B)

Secret:

- Befriending the Elder Drake unlocks Dragon Riding

#### Frozen Monastery
A later mid-game route associated with ice threats, ancient ritual spaces, and one of the Seal Fragment quests.

### 10.3 Late-Game Regions

#### Sky Fortress
Floating citadel reached by elevator or Dragon Riding.

Sub-areas include:

- Landing Dock
- Outer Ramparts
- Inner Sanctum
- Power Core
- Command Bridge
- Observatory
- Armory

Boss:

- High Commander (Rank B)

Secret:

- Star chart revealing the Void Rift

#### Volcanic Core
The molten heart of the world.

Sub-areas include:

- Descent Shaft
- Magma Sea
- Ancient Foundry
- Obsidian Palace
- Core Chamber
- Phoenix Nest

Bosses:

- Molten Emperor (Rank A)
- Ancient Phoenix (optional/farmable Rank A)

Secret:

- Tier 5 legendary weapon crafting
- Unique fire robot

#### Clockwork Sanctum
Ancient automated facility with gears, time distortions, and assembly architecture.

Sub-areas include:

- Main Gate
- Gear Works
- Assembly Line
- Control Room
- Archive
- Master Workshop
- Clock Tower

Boss:

- The Architect (Rank A)

Secret:

- Ancient-history reveals
- MK-III upgrade path via the Clockmaker

### 10.4 Final and Post-Game Regions

#### The Void
Final zone between dimensions.

Requirements:

- Void Key
- Four Seal Fragments

Sub-areas include:

- Void Gate
- Memory Lane
- Gauntlet of Trials
- Throne of Origin
- The Beyond

Final boss:

- Origin Core (Rank S, 3 phases)

#### Infinite Dungeon
Post-game endless dungeon with:

- Boss every 10 floors
- Superboss every 25 floors
- Legendary reward every 50 floors
- Floor modifiers for long-term replayability

## 11. Player Role and Party Structure

### 11.1 Player Character
The player is a human Forge Master and a full combatant in battle.

The player serves as:

- Front-facing protagonist
- Core party member
- Robot commander
- Restorer of the Forge Core network

### 11.2 Party Composition
Final party structure:

- Player
- Up to 3 active robots, depending on grade progression

### 11.3 Party Slot Growth by Grade

- Grade G: Player + 1 robot
- Grade F: Player + 2 robots
- Grade E: Player + 2 robots
- Grade D and above: Player + 3 robots

## 12. Combat Vision

### 12.1 Combat Format
Combat begins from overworld enemy contact and transitions to a dedicated BattleScreen.

Combat flow:

1. Build party and enemy combatant list
2. Calculate initial timeline
3. Resolve turns through commands or AI
4. Apply damage, healing, statuses, elemental effects, and reactions
5. Update timeline after each action
6. End in victory, defeat, or escape

### 12.2 Encounter Size
Final combat supports:

- 1-5 enemies
- Individual targeting
- Front/back-row readability
- Multi-target and AoE abilities

### 12.3 Turn Timeline
The game uses a Conditional Turn-Based system inspired by FFX.

Key behavior:

- Visible timeline showing the next 8-10 turns
- Every action has a speed cost
- Heavy actions delay the next turn
- Quick actions allow more frequent turns
- Speed stat directly influences timeline position

### 12.4 Core Command Menu

- Attack
- Defend
- Ability
- Item
- Analyze
- Flee

### 12.5 Command Roles

- Attack: reliable physical action
- Defend: damage mitigation and turn-order control
- Ability: core source of specialization and tactical depth
- Item: consumables and recovery
- Analyze: bestiary progression and enemy intel
- Flee: risk-managed disengagement

## 13. Damage, Speed, and Tactical Math

### 13.1 Physical Damage
Final design formula:

- `baseDamage = (ATK * weaponMultiplier) - (DEF * 0.5)`
- `weaponMultiplier = 1.0 + (weaponProficiency * 0.05)`
- Apply crit, defense stance, and small variance
- Minimum damage is always 1

### 13.2 Critical Hits
Final design:

- Base crit chance: 5%
- Plus 1% per 10 SPD
- Crit multiplier: 1.5x

### 13.3 Ability Damage
Final design formula:

- Ability power scales off mixed ATK and INT
- Element multiplier applies after base damage
- Proficiency multiplier scales with ability level
- Ability variance remains small for readability

### 13.4 Healing
Healing scales mainly from INT with some ATK contribution and benefits from proficiency multipliers.

### 13.5 Speed Cost Reference
Target pacing:

- Defend: 40
- Analyze: 50
- Quick ability: 60
- Item: 70
- Basic attack: 80
- Standard ability: 80-100
- Heavy ability: 110-130
- Failed flee: 100

## 14. Elemental System

### 14.1 Elements
Final vision includes six combat elements:

- Fire
- Ice
- Lightning
- Earth
- Wind
- Water

### 14.2 Element Rules

- Weakness: 1.5x
- Neutral: 1.0x
- Resistance: 0.5x
- Absorb: 0.0x or healing-style nullification behavior depending on implementation layer

### 14.3 Elemental Chain Bonus
Three or more consecutive hits of the same element on one target trigger an Elemental Break, reducing that target's resistance tier to that element for the rest of the battle.

### 14.4 Strategic Goal
Elements should:

- Encourage varied party composition
- Reward scanning and planning
- Make multi-turn setups worthwhile
- Differentiate robots and weapons more sharply

## 15. Status Effect System

Final combat design includes a broad suite of statuses:

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

### 15.1 Status Design Intent
Statuses should create:

- Turn denial
- attrition pressure
- tempo control
- setup-and-payoff combat decisions
- strong support-role utility

### 15.2 Stacking Rules

- Damage-over-time statuses stack independently
- Control statuses replace each other
- Same-category buffs/debuffs refresh rather than stack infinitely
- Opposing statuses cancel when appropriate

## 16. Ability System

### 16.1 Ability Structure
Abilities are data-driven and defined by:

- Name
- Element
- Target pattern
- Cooldown
- Power
- Speed cost
- Proficiency bonuses
- Learnable role/model access
- Unique skill evolution path

### 16.2 Target Types

- Self
- Single enemy
- All enemies
- Single ally
- All allies

### 16.3 Role in Combat
Abilities are the primary source of:

- Elemental offense
- Healing
- Buffs
- Debuffs
- control effects
- battle identity

## 17. Progression Through Use

### 17.1 Ability Proficiency
Every ability has its own proficiency level from 1 to 10.

Key design goals:

- Repeated use drives power growth
- Weak enemies give reduced training value
- Higher-level enemies accelerate mastery
- Mastery should take effort but remain exciting

### 17.2 Proficiency Benefits
Across the track, abilities gain:

- Power increases
- Cooldown reductions
- Secondary effects
- Name upgrades
- Improved utility
- Final evolution into a Unique Skill

### 17.3 Mastery Milestone
At proficiency level 10, an ability transforms into its Unique Skill form.

## 18. Unique Skill Evolution

Only one instance of a given Unique Skill can exist in the party at a time.

Example final-vision evolutions:

- Dash -> Phantom Step
- Scan -> Omniscience Eye
- Shield Wall -> Fortress Eternal
- Taunt -> Gravity Well
- Power Strike -> Obliteration
- Rapid Fire -> Bullet Storm
- Heal Pulse -> Life Spring
- Repair Aura -> Genesis Field

Unique Skills should feel like:

- breakthrough moments
- capstone rewards for specialization
- build-defining power spikes

## 19. Weapon Proficiency and Combat Arts

### 19.1 Weapon Types
Final weapon mastery tracks:

- Sword
- Axe
- Lance
- Staff
- Bow
- Fist
- Gun
- Dual-Blade

### 19.2 Weapon Identity
Each weapon type has:

- Its own proficiency track
- Its own stat scaling emphasis
- Three unlockable Combat Arts at key levels

### 19.3 Combat Art Philosophy
Combat Arts add a second axis of specialization alongside abilities, allowing robots with similar base roles to diverge through weapon mastery.

## 20. Grade System

### 20.1 Grade Ladder

- G
- F
- E
- D
- C
- B
- A
- S

### 20.2 Grade Functions
Grade affects:

- Equipment tier access
- Zone access expectation
- Active party size
- Progression pacing
- Long-term aspirational status

### 20.3 Grade Ranges
Target progression:

- G: 1-10
- F: 11-20
- E: 21-35
- D: 36-50
- C: 51-70
- B: 71-85
- A: 86-95
- S: 96-99

## 21. Robot System

### 21.1 Robot Philosophy
Robots are collectible, customizable companions that function as the player's primary expression layer in combat.

Robots should differ through:

- Base model identity
- Role
- Equipment loadout
- Ability growth
- Weapon mastery
- Unique skill path
- Evolution state

### 21.2 Robot Lifecycle

1. Discover or obtain a robot
2. Repair / activate it
3. Equip and deploy it
4. Specialize it through battle use
5. Evolve it to stronger MK tiers
6. Build a unique role in the party

### 21.3 Robot Evolution
Final target path:

- MK-I
- MK-II
- MK-III

Target effects:

- MK-II: roughly +20% stats, 3 ability slots
- MK-III: roughly +40% stats, 4 ability slots

Requirements:

- Forge Core level
- Materials
- Advanced workshop or related upgrade path

### 21.4 Discoverable Robots
The full game vision includes 10 discoverable robot companions in the world, each tied to exploration, quests, or special conditions.

## 22. Character and NPC Design

### 22.1 Core Hub NPC Roles

- Mentor / Forge Core guide
- Kira: fusion specialist
- Elena: meal buffs and support services
- Bolt: training and combat practice
- Jax: wandering merchant with rotating inventory

### 22.2 Narrative NPC Function
NPCs should provide:

- Story progression
- Rumors and secrets
- Quests
- Services
- faction/world lore
- emotional grounding for Ironhaven

### 22.3 Dialogue Philosophy
Dialogue is meant to make the world feel inhabited and reactive, not merely functional.

The system should support:

- branching requirements
- quest-state gating
- inventory and world-state checks
- service unlocks
- relationship and lore delivery

## 23. Quest Structure

### 23.1 Main Quest Content
Final scope targets:

- 18 main story quests
- 4 acts
- boss-driven world-state progression

### 23.2 Side Quest Content
Final side content target:

- 60+ side quests

Quest categories include:

- NPC rescue
- Monster bounty
- Exploration
- Crafting chain
- Robot recovery
- Lore collection
- Arena challenge
- Settlement building

### 23.3 Quest Design Goals
Quests should:

- introduce mechanics naturally
- drive players into underexplored areas
- unlock world changes and services
- support robot acquisition and equipment growth
- deepen Ironhaven as a living settlement

## 24. Bestiary and Knowledge Systems

### 24.1 Analyze and Scan
Enemies can be permanently researched through scan-based actions.

Three scan levels:

- Basic info
- Full stats
- Drop tables and deeper knowledge

### 24.2 Archive Role
The Archive in Ironhaven should eventually become a progression hub for:

- Bestiary records
- lore fragments
- enemy weaknesses
- optional completionist goals

## 25. Equipment Vision

### 25.1 Equipment Tiers

- Tier 1: Common
- Tier 2: Uncommon
- Tier 3: Rare
- Tier 4: Epic
- Tier 5: Legendary
- Mythic

### 25.2 Equipment Slots

- Head
- Body
- Arms
- Legs
- Weapon
- Accessory

### 25.3 Equipment Goals
Equipment should support:

- clear stat identity by slot
- weapon-type ownership
- build specialization
- long-term crafting and fusion loops
- unique passive boosts on higher tiers

## 26. Unique Boosts

The final equipment system includes special passive effects such as:

- Burn
- Freeze
- Shock
- Drain
- Reflect
- Regen
- Haste
- Counter
- Elemental absorb effects
- Crit-up effects
- Gold/XP bonuses
- Last Stand
- Auto-Revive

These should turn endgame loot into build-defining choices rather than simple stat upgrades.

## 27. Crafting and Fusion

### 27.1 Crafting Philosophy
Crafting is a long-term reward structure for exploration and enemy farming. Materials from zones should feed a meaningful equipment progression pipeline.

### 27.2 Crafting Stations

- Ironhaven Workshop
- Ember Forge
- Frost Anvil
- Crystal Forge
- Sky Workshop
- Ancient Foundry
- Master Workshop

### 27.3 Station Roles
Different stations gate different tiers and specializations of crafting, ensuring that world progression and crafting progression remain connected.

### 27.4 Equipment Fusion
At the Workshop, Kira can fuse two items of the same tier and slot into one item of the next tier.

Fusion rules:

- Result inherits the best stats of both parents
- Result can roll for a Unique Boost
- Cost escalates sharply by tier

## 28. Economy

### 28.1 Core Currencies and Resources
The game economy is built around:

- Gold
- Crafting materials
- boss-drop materials
- high-tier cores and rare components

### 28.2 Economy Goals
The target feel is:

- modest abundance early
- some pressure in mid-game
- late-game access to major purchases without eliminating meaningful decisions

### 28.3 Shops
Shops should:

- restock based on world state
- reflect player grade
- include rotating special inventory
- create reasons to revisit Ironhaven and outpost hubs

## 29. Enemy and Boss Design

### 29.1 Regular Enemies
Enemy groups should challenge:

- elemental coverage
- status management
- formation planning
- AoE vs single-target choice

### 29.2 Boss Philosophy
Bosses should be:

- mechanically distinct
- world-defining
- tied to hub progression
- major reward spikes

### 29.3 Multi-Phase Bosses
Late-game and key story bosses should feature unique phase logic.

Example final boss vision:

- Origin Core Phase 1: physical pressure and summons
- Phase 2: rotating elemental weakness
- Phase 3: time manipulation and severe status disruption
- 10% HP trigger: Genesis Reset

### 29.4 Superbosses
Post-game superboss plans include:

- Omega Slime
- Shadow of the Forge Master

## 30. Traversal and Environmental Interaction

### 30.1 Environmental Abilities
Specific abilities or robot capabilities should interact with the world.

Examples:

- Fire burns barriers
- Scan reveals hidden passages
- Strength-based actions move boulders

### 30.2 Dragon Riding
Dragon Riding is a planned traversal unlock that opens aerial shortcuts and new access routes after the Dragon Elder quest chain.

### 30.3 Exploration Goal
Environmental interaction should create a Metroidvania-lite feeling without turning the game into a pure puzzle platformer.

## 31. Endgame and Post-Game

### 31.1 Endgame
The endgame culminates in:

- The Void
- Memory Lane
- Gauntlet of Trials
- Origin Core

### 31.2 Post-Game
Post-game systems include:

- Infinite Dungeon (accessible as standard exploration or as a Shard Run — see 31.4)
- The Beyond
- superbosses
- Mythic gear chase
- cosmetic or prestige rewards

### 31.3 Replayability Goals
Replayability should come from:

- mastery builds and weapon proficiency specialization
- robot composition differences
- item and Unique Boost hunting
- Shard Run module drafting — each run builds a different loadout from random offerings
- Forge Legacy meta-progression — accumulated unlocks change run feel over time
- post-game boss challenges
- Run Modifier escalation — voluntary handicaps for higher Shard multipliers

### 31.4 Roguelike Loop: Shard Runs and Forge Legacy

RogueForge delivers on its name through two interlocking roguelike systems that coexist with the persistent open world.

**Shard Runs** use the Infinite Dungeon as their arena. The player enters stripped of their regular loadout and builds a run from scratch — drafting a robot, collecting Shard Modules (run-only passive upgrades), and descending as far as possible. Death ends the run but is never punishing: the overworld, quests, and regular equipment are untouched. Floor depth and boss kills earn Forge Shards on every attempt, successful or not.

**Forge Legacy** is the permanent meta-progression tree funded by those Forge Shards. It lives in Ironhaven at a new facility called the Legacy Vault. The tree has four branches — starting kit improvements, proficiency carry-ins, global stat bonuses, and rare module pool unlocks. Every run deposits something into the Legacy tree, so every death makes the next attempt meaningfully different.

The two loops feed each other: stronger Legacy unlocks let players push deeper in Shard Runs, deeper runs earn more Shards, and more Shards open more of the tree. Players who prefer the open world benefit from the passive stat branches of Forge Legacy without needing to run the dungeon obsessively.

For full mechanical spec see Systems_Spec.md Section 26.

## 32. UI and Presentation

### 32.1 Combat UI Priorities

- Clear command menu
- Readable turn timeline
- Strong elemental and status indicators
- Results screen that celebrates progression gains

### 32.2 Exploration UI Priorities

- Quest clarity
- zone and route readability
- world-state feedback
- accessible robot and equipment management

### 32.3 Hub UI Priorities

- party management
- crafting and fusion access
- settlement growth visibility
- bestiary and archive presentation

## 33. Audio and Visual Direction

### 33.1 Visual Direction
The game uses pixel art with:

- readable combat silhouettes
- strong elemental color coding
- consistent zone-specific tilesets
- expressive UI icons for statuses, elements, weapons, and progression

### 33.2 Audio Direction
The architecture document positions audio as a major mood-builder and supports:

- battle themes
- overworld themes
- boss themes
- victory jingles
- evolution fanfares
- crafting and UI feedback

Target tone can blend:

- chiptune sensibilities
- orchestral momentum
- mechanical ambience

## 34. Current Build Snapshot

The current codebase is a fully content-complete implementation of the five-phase development plan. All major systems, world zones, story content, and endgame features are in production.

Broadly, the current build includes:

- Full tactical combat engine: 28 combat classes covering CTB timeline, elemental resolution, status effects, ability execution, loot, and bestiary
- 16 production zones, each with a TMX map, enemies, named NPCs, chests, doors, story events, and environmental interaction features
- 10 main quests across 4 story acts, 24 side quests across 5 categories — all with verified flag coverage and zero broken completion chains
- 112 NPC dialogue entries across 28 characters with priority gating, world-flag conditions, quest-state conditions, and settlement upgrade triggers
- 4 facilities × 3 tiers = 12 settlement upgrades; all 7 named Ironhaven NPCs with full upgrade dialogue chains
- Environmental interaction layer: burn_barrier, strength_boulder, and scan_hidden_path implemented in GameScreen and present across 6 zone maps
- Dragon Riding traversal network: unlocked by the dragon boss defeat story event, connecting 7 maps via requiredWorldFlag-gated dragon roost doors
- Endgame content: The Void (Memory Lane, Gauntlet of Trials, Throne of Origin), Infinite Dungeon with procedural floor scaling, 4 S-rank superbosses
- 10 discoverable robot companions wired to in-world chests, NPCs, and story events
- Save/load, economy, shop system with world-state gating, and full quest/dialogue flag consistency verified by cross-reference validation

Key final-vision areas that remain broader than the current build:

- Full CTB timeline depth (Elemental Break mechanic, CTB edge-case hardening, weapon-specific Combat Arts)
- Quest and zone content scale (34 quests vs 60+ target, 16 zones vs 25+ target)
- Equipment tier ladder (Tier 5 and Mythic not yet defined)
- Archive screen integration with bestiary data
- Forge Core boss-count expansion milestones (Lv2/3/4 tied to boss count, not yet wired)

## 35. Production Scope Summary

The Version 3 architecture document frames the target shipped product. The table below shows current delivery status against each target item.

| Target | Status |
|---|---|
| Multi-enemy tactical battle system | ✅ Delivered |
| Visible CTB turn timeline | ✅ Delivered (depth below final vision) |
| 6-element weakness/resistance model | ✅ Delivered (Elemental Break still pending) |
| 15 major status effects | ✅ Delivered (balance polish needed) |
| Progression-through-use ability system | ✅ Delivered (content breadth below target) |
| 8 weapon mastery tracks with Combat Arts | ✅ Tracks delivered; Combat Arts still generic |
| Grade-gated progression | ✅ Partial — logic present, not fully enforced |
| MK-I to MK-III robot evolution | ✅ Framework delivered; material gating incomplete |
| 16+ production zones | ✅ Delivered — 16 zones in production |
| 25+ total world spaces | ⚠️ Not yet — 16 of 25+ target |
| 4 acts of main story content | ✅ Delivered — 10 main quests across 4 acts |
| 60+ side quests | ⚠️ Not yet — 24 of 60+ target |
| 10 discoverable robots | ✅ Delivered — 10 recruitment events wired |
| Ironhaven hub expansion | ✅ Delivered — 4 facilities × 3 tiers |
| Tiered crafting and equipment fusion | ✅ Crafting delivered; fusion economy partial |
| Endgame final zone and post-game content | ✅ Delivered — The Void, Infinite Dungeon, 4 superbosses |
| Shard Run roguelike dungeon mode | ⚠️ Planned — spec complete, implementation pending |
| Forge Legacy meta-progression tree | ⚠️ Planned — spec complete, implementation pending |
| Legacy Vault (Ironhaven 5th facility) | ⚠️ Planned — Coda NPC + Shard tree UI |

## 36. One-Sentence Product Vision

RogueForge is a turn-based open-world machine-fantasy RPG where a Forge Master rebuilds a fallen civilization by mastering tactical combat, evolving a personalized robot party, restoring Ironhaven, and uncovering the truth behind the Origin Core at the heart of Mechara.
