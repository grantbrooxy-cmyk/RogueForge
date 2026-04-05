# RogueForge Guild and Permissions Implementation Plan

## 1. Purpose

This document turns the player-created world vision into an engine-facing implementation plan for guilds, ownership, and permissions.

Its job is to define the first reusable system layer that later features will rely on:

- guild halls
- guild storage
- player-authored quest boards
- player-created NPCs
- settlement governance
- land-edit permissions
- dungeon publication rights

This document should be treated as the bridge between product vision and engine implementation.

Companion documents:

- `docs/Player_Created_World_Systems_Plan.md`
- `docs/Run_World_Meta_Structure.md`
- `docs/State_Implementation_Guide.md`

## 2. Why This Comes First

Guilds, player quests, created NPCs, and player-run settlements all depend on the same hidden question:

- who is allowed to do what to which world object

Without that foundation, later systems will drift into one-off logic that is difficult to reuse, secure, or move into multiplayer.

The first implementation step should therefore be:

- define ownership
- define permission actions
- define guild roles and memberships
- define reusable permission evaluation rules

## 3. Scope of Phase 1

This phase is not full guild gameplay.

This phase is the reusable engine foundation that later gameplay systems will call into.

Phase 1 should include:

- ownership record model
- permission-action catalog
- reusable permission set model
- guild rank model
- guild membership model
- guild definition model
- permission evaluation engine
- default rank templates

Phase 1 should not yet include:

- guild UI
- guild chat
- guild treasury screens
- guild halls as interactable gameplay
- quest authoring UI
- NPC creation UI
- multiplayer synchronization

## 4. Core Questions the Engine Must Answer

The system should be able to answer these consistently:

- Is this object player-owned, guild-owned, settlement-owned, or public?
- Which player or guild owns it?
- Which actions are allowed against it?
- Which guild role grants those actions?
- Does a player’s role permit the requested action?
- Is the player the owner, a guild member, or neither?

If a new system cannot express itself in those terms, it is probably not using the foundation correctly.

## 5. Core Data Models

The first engine pass should establish the following models.

### 5.1 `OwnershipRecord`

Represents the authoritative ownership of a world object.

Suggested fields:

- ownership scope
- owner player id
- owner guild id
- settlement id if applicable
- explicit editor allow-list
- public interaction toggle

Examples of objects that should eventually use this:

- a claimed outpost
- a guild hall
- a quest board
- a storage compound
- a created NPC
- a challenge dungeon beacon

### 5.2 `PermissionAction`

Represents actions the engine can authorize.

Initial suggested actions:

- build
- edit structures
- remove structures
- manage storage
- post quests
- manage quests
- create NPCs
- edit NPCs
- manage members
- manage ranks
- claim land
- publish challenges

### 5.3 `PermissionSet`

Represents the allowed action set for a role or object policy.

This should be reusable across guild ranks and later settlement roles.

### 5.4 `GuildRank`

Represents a named guild role and the permission set attached to it.

Initial role templates:

- guild master
- officer
- quartermaster
- architect
- member
- recruit

### 5.5 `GuildMembership`

Represents a player’s membership in a guild and the rank they hold.

Suggested fields:

- player id
- rank id
- active flag

### 5.6 `GuildDefinition`

Represents a persistent guild as a social and ownership entity.

Suggested fields:

- guild id
- guild name
- founder player id
- recruitment visibility
- hall zone id
- hall claimed-site id
- ranks
- memberships

### 5.7 `GuildPermissionsEngine`

Provides reusable permission evaluation.

Primary job:

- given a guild, player id, and action, answer whether the player may perform that action

Secondary job later:

- evaluate ownership-aware permissions on world objects

## 6. Default Permission Policy

The first default rank layout should be:

### Guild Master

- all actions

### Officer

- post quests
- manage quests
- create NPCs
- edit NPCs
- publish challenges

### Quartermaster

- manage storage
- post quests
- manage quests

### Architect

- build
- edit structures
- remove structures
- claim land

### Member

- no elevated management permissions by default

### Recruit

- no elevated permissions

These defaults should live in code as templates, not be hardcoded in UI logic.

## 7. Ownership Policy Rules

The engine should start with four ownership scopes:

- personal
- guild
- settlement
- public

### 7.1 Personal Ownership

The owner player has full edit rights unless an object-specific rule limits them.

### 7.2 Guild Ownership

A member’s rights depend on guild rank permissions.

### 7.3 Settlement Ownership

Reserved for later governance systems. Phase 1 only needs the scope defined so later systems do not need a breaking change.

### 7.4 Public Ownership

Reserved for protected or moderated objects. Public should mean publicly accessible, not freely editable.

## 8. Save and Persistence Implications

These systems belong to the persistent save/world domain, not the run or meta layer.

Future save data will need space for:

- guild definitions
- guild memberships
- ownership records for world objects
- settlement access rules

They should not be stored in:

- session-only expedition state
- death-judged meta progression

## 9. Multiplayer Implications

The design should assume future authoritative validation.

That means the permission engine must not rely on UI-only checks.

Any future online flow should validate on the host or server:

- guild rank
- object ownership
- requested action
- escrow or resource rights where relevant

## 10. Code Placement

The recommended location is a new reusable engine package:

- `core/src/main/java/com/rogueforge/game/engine/social`

This keeps guilds and permissions separate from:

- `engine/base`
- `engine/world`
- `engine/meta`

while staying reusable for multiple future systems.

## 11. Immediate Deliverables

The first code pass should add:

- `PermissionAction`
- `OwnershipScope`
- `PermissionSet`
- `GuildRank`
- `GuildMembership`
- `GuildDefinition`
- `OwnershipRecord`
- `GuildPermissionsEngine`

and a test suite proving:

- default guild-master permissions
- rank-specific restrictions
- ownership helper behavior
- safe denial for unknown or inactive members

## 12. Next Step After Phase 1

Once these models exist, the next implementation layer should be:

- persistent guild state in save data
- land ownership attached to claimed frontier sites
- guild-owned base and storage permissions

That will let the current base-building system begin transitioning from single-player ownership assumptions into reusable shared-world ownership rules.
