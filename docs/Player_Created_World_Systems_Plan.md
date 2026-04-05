# RogueForge Player-Created World Systems Plan

## 1. Purpose

This document defines how RogueForge can evolve from a developer-authored RPG with persistent frontier building into a player-shaped sandbox RPG where players create meaningful gameplay for each other.

It covers:

- guild master gameplay
- player-authored quest systems
- player-created NPC systems
- player-run settlements and outposts
- dungeon architect systems
- permissions and anti-abuse rules
- engine and data-model implications
- staged rollout order

Companion documents:

- `docs/GDD.md` for overall product vision
- `docs/Systems_Spec.md` for combat, economy, and progression systems
- `docs/Run_World_Meta_Structure.md` for run, world, and meta boundaries
- `docs/State_Implementation_Guide.md` for persistence and state-channel rules
- `docs/Guild_and_Permissions_Implementation_Plan.md` for the first engine-facing ownership and guild foundation

## 2. Vision

RogueForge should not only offer content to players. It should gradually let players become in-world creators who can shape the frontier, establish social structures, and generate objectives for other players inside persistent worlds.

The target fantasy is:

- adventurer
- builder
- guild master
- settlement ruler
- dungeon architect
- NPC patron or steward
- quest author

The core rule is:

- players should be able to create gameplay for other players

without turning the game into an unbounded scripting sandbox too early.

## 3. Design Principles

### 3.1 Guided Creation Before Freeform Creation

Player creativity should first be expressed through strong templates and simulation hooks, not raw unrestricted scripting.

This keeps systems:

- understandable
- testable
- abuse-resistant
- multiplayer-friendly

### 3.2 Simulation First

Player-created content should attach to persistent world systems rather than exist as disconnected UI records.

Examples:

- a guild hall is a real built place
- a guild quest board belongs to a real location
- a created NPC occupies a real point in the world
- a dungeon challenge is tied to a real claimed structure or site

### 3.3 Permissions Are Core Infrastructure

Creative freedom only works if ownership and edit authority are explicit.

Every player-created object should answer:

- who owns it
- who can edit it
- who can interact with it
- who can publish it
- who can delete it
- who receives its rewards or taxes

### 3.4 Player Creation Should Feed the Main Loop

Player-authored systems should reinforce:

- exploration
- extraction
- building
- guild cooperation
- defense
- economy
- social identity

They should not bypass progression or trivialize authored content.

## 4. System Layers

RogueForge should support four connected layers of authored and player-driven play:

- developer-authored world layer
- player-built world layer
- player-governed social layer
- player-authored activity layer

### 4.1 Developer-Authored World Layer

This is the hand-authored backbone:

- main story
- major factions
- unique bosses
- starter town
- fixed progression landmarks
- tutorial rails

### 4.2 Player-Built World Layer

This is the persistent construction and frontier reshaping layer:

- bases
- outposts
- guild halls
- storage compounds
- patrol routes
- defenses
- regional service hubs

### 4.3 Player-Governed Social Layer

This is the role and authority layer:

- guilds
- guild ranks
- land permissions
- settlement governance
- shared storage permissions
- public versus private services

### 4.4 Player-Authored Activity Layer

This is where players create repeatable gameplay:

- guild quests
- public bounties
- player-made NPC roles
- settlement service loops
- challenge dungeons
- raid contracts
- delivery requests

## 5. Guild Master System

## 5.1 Fantasy

A player can found a guild, establish a guild identity, recruit members, assign ranks, create internal goals, and publish quests or contracts to other players.

## 5.2 Guild Core Functions

Guild systems should support:

- guild creation
- guild hall placement or claiming
- guild identity
- ranks and permissions
- guild storage
- guild quest board
- guild treasury
- guild task rewards
- guild territory rights where relevant

## 5.3 Guild Roles

Suggested starting roles:

- Guild Master
- Officer
- Quartermaster
- Architect
- Recruit
- Member

Each role should define permissions for:

- inviting or removing members
- editing guild structures
- withdrawing from guild storage
- posting quests
- setting rewards
- creating guild NPCs
- publishing public jobs

## 5.4 Guild Quest Types

Guild leaders and permitted officers should be able to create structured quests such as:

- gather resources
- craft supplies
- hunt specified enemies
- clear a marked site
- defend a named outpost
- escort a caravan or bot unit
- deliver goods to a location
- survive a raid window
- build required structures at a site

## 5.5 Guild Reward Types

Guild rewards should use controlled pools:

- gold
- banked materials
- guild reputation
- access rights
- rank advancement
- item bundles

Direct arbitrary reward scripting should be avoided early.

## 6. Player-Created Quest System

## 6.1 Authoring Model

Players should create quests from templates, not code.

Each quest should be built from:

- issuer
- objective type
- target
- location
- quantity or threshold
- eligibility
- reward
- visibility
- expiry

## 6.2 Initial Quest Templates

The first safe set should include:

- Gather
- Deliver
- Hunt
- Defend
- Escort
- Explore
- Craft
- Build

## 6.3 Quest Constraints

Player-authored quests should obey strict rules:

- rewards come from actual stored or reserved resources
- objective types must resolve against existing game systems
- quests cannot grant developer-only items
- quests cannot write arbitrary world flags
- quests cannot modify story progression

## 6.4 Discovery and Surfacing

Player-created quests should surface through:

- guild boards
- public contract boards
- settlement stewards
- created NPCs
- outpost terminals

## 6.5 Completion Rules

Quest completion should resolve through system-tracked events, not manual trust:

- item delivery checked against inventory
- kills checked against enemy families or target ids
- structures checked against placed-structure state
- exploration checked against zone entry or beacon reach
- defense checked against raid survival state

## 7. Player-Created NPC System

## 7.1 Purpose

NPC creation lets players turn their settlements, guild halls, and outposts into social and functional spaces rather than static structure clusters.

## 7.2 Initial NPC Roles

The starting role set should be constrained:

- Merchant
- Quartermaster
- Guild Clerk
- Guard Captain
- Healer
- Crafter
- Bounty Steward
- Caretaker

## 7.3 NPC Definition Fields

Each created NPC should include:

- NPC id
- owner id or guild id
- display name
- role
- home location
- assigned structure or station
- dialogue package
- schedule template
- faction alignment
- service settings
- quest board linkage where relevant

## 7.4 Dialogue Model

Dialogue should begin with templates plus configurable text fragments:

- greeting
- role statement
- current task line
- guild affiliation line
- quest handoff line

Avoid fully open dialogue authoring at first. Use constrained fields with optional custom flavor text length limits.

## 7.5 Simulation Hooks

Created NPCs should eventually support:

- standing post
- patrol route
- shop inventory role
- quest issuance
- repair service
- banking service
- crafting service
- defense alerts

## 8. Dungeon Architect System

## 8.1 Purpose

Players should be able to create challenge spaces that extend the life of the game beyond authored dungeon content.

## 8.2 First Safe Version

Do not begin with unrestricted map editing.

Start with:

- claimable challenge sites
- room or node templates
- trap modules
- defender assignments
- reward chest rules
- access policies

## 8.3 Challenge Publishing

Player-made challenge spaces can be:

- private
- guild-only
- friends-only
- public

## 8.4 Reward Rules

To prevent abuse:

- player-made dungeons must draw from bounded reward tables
- creator-provided rewards must come from stored resources
- repeat clears should have diminishing payout
- self-farming rules must restrict owners and alts as needed

## 9. Permissions and Anti-Abuse

## 9.1 Ownership Model

Every player-authored object should resolve to one of:

- personal ownership
- guild ownership
- shared settlement ownership
- world-public but moderated ownership

## 9.2 Permission Domains

Permission checks are needed for:

- build
- edit
- remove
- publish
- assign rewards
- withdraw resources
- recruit NPCs
- post quests
- claim land
- change access policy

## 9.3 Anti-Abuse Requirements

At minimum the system should prevent:

- infinite reward loops
- self-issued exploit quests with fake payouts
- public grief edits
- unrestricted NPC spam
- settlement storage theft without permission
- terrain griefing in shared hubs
- dungeon reward duplication

## 9.4 Safe Rule Set

Use these baseline rules:

- rewards must be escrowed before publication
- quests use only approved templates
- build rights exist only in owned or permitted land
- public spaces have tighter placement limits
- NPC counts are capped per settlement tier
- dungeon publication requires validation
- creator objects can be rate-limited per player or guild

## 10. Engine and Data Model Changes

## 10.1 New Core Domains

The engine will eventually need dedicated models for:

- `GuildDefinition`
- `GuildMembership`
- `GuildPermissionSet`
- `PlayerAuthoredQuestDefinition`
- `QuestBoardEntry`
- `PlayerCreatedNpcDefinition`
- `NpcRoutineDefinition`
- `SettlementPermissionState`
- `DungeonBlueprint`
- `PublishedChallengeDefinition`

## 10.2 World-State Responsibilities

Persistent world state should carry:

- ownership
- placements
- guild structures
- authored NPC definitions
- published quest boards
- settlement permissions
- challenge-site state

Meta progression should not hold these.

## 10.3 Runtime Responsibilities

Runtime systems should handle:

- quest validation
- NPC routine ticking
- permission checks
- structure and land edit validation
- payout escrow and release
- publication visibility

## 10.4 Multiplayer Implications

If RogueForge moves into online shared worlds, these systems must assume authoritative world state rather than trusting clients.

That means:

- host or server ownership of world truth
- validated placement and quest publication
- validated reward escrow
- validated NPC creation
- validated challenge completion

Do not design these systems around client trust.

## 11. Rollout Roadmap

The safest order is:

### Phase 1: Strong Sandbox Foundation

- persistent base and outpost building
- storage and logistics
- defender AI
- land ownership and permissions

### Phase 2: Guild Layer

- guild creation
- guild ranks
- guild storage
- guild hall designation

### Phase 3: Structured Quest Authoring

- guild quest board
- public contract board
- escrowed rewards
- template-based objectives

### Phase 4: NPC Authoring

- created role-based NPCs
- schedule templates
- service assignments
- guild clerks and quartermasters

### Phase 5: Challenge Spaces

- challenge-site claims
- dungeon room templates
- defender placements
- publishable challenge runs

### Phase 6: Advanced Creator Tools

- richer NPC logic
- event chains
- settlement governance rules
- creator analytics and moderation tools

## 12. Product Positioning

RogueForge should not try to become an unrestricted scripting platform immediately.

Its strongest identity is:

- a persistent sandbox frontier RPG
- with strong progression and tactical combat
- where players can become rulers, guild masters, and content authors inside the world

The goal is not infinite raw freedom.

The goal is structured creative power that produces lasting social play.
