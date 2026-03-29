# RogueForge End-to-End QA Checklist

Use this checklist for a full beginning-to-end gameplay verification pass.

## Test Setup

- [ ] Start from a fresh save.
- [ ] Confirm the run starts in `town` / Ironhaven rather than directly in hostile Verdant Fields.
- [ ] Confirm the game loads without missing textures, missing dialogue, or map transition errors.
- [ ] Confirm player movement, camera follow, interaction prompts, and pause/menu input all work at run start.

## Town Opening

- [ ] Speak to Bram and confirm the initial main quest appears.
- [ ] Speak to every required town NPC for the town-survey step.
- [ ] Confirm the town-survey quest advances after the required NPC conversations.
- [ ] Return to Bram and confirm the next main quest step triggers correctly.
- [ ] Confirm Mira's early quest text now clearly points the player from town into Verdant Fields.
- [ ] Confirm shops in town open correctly and show the correct renamed player equipment.
- [ ] Confirm robot equipment is not shown/equippable in player slots, and player equipment is not shown/equippable in robot slots.

## Town Map and Doors

- [ ] Confirm every building entrance in town can be approached and used by the player.
- [ ] Confirm every exit gate or passage in town has enough clear space to stand on and interact with.
- [ ] Confirm the east gate / thin passage into Verdant Fields is not blocked by harvest nodes, claim markers, or decoration.
- [ ] Confirm doors do not place the player into blocked tiles on arrival.

## Verdant Fields Frontier

- [ ] Exit town into Verdant Fields and confirm the map feels expansive rather than like a tiny fixed arena.
- [ ] Confirm the frontier uses seeded terrain variation rather than a flat repeated surface.
- [ ] Confirm the starter safe area around town exists and enemies do not pressure the player immediately at the gate.
- [ ] Move outward and confirm enemies begin appearing outside the safe area.
- [ ] Confirm authored early encounters near the entrance still spawn reliably.
- [ ] Confirm east Verdant Fields is traversable and no longer overfilled with blockers.
- [ ] Confirm resource nodes appear across multiple terrain types.
- [ ] Confirm claimable outpost/base sites appear outside the starter safe zone.
- [ ] Save, reload, and confirm the same save preserves frontier identity, harvested nodes, and claimed sites.

## Quests and Story Progression

- [ ] Progress through the main quest until combat is required and confirm every objective can be completed normally.
- [ ] Confirm NPC dialogue matches the split between Ironhaven and Verdant Fields.
- [ ] Confirm no quest still refers to Verdant Fields as the safe starter town.
- [ ] Confirm quest completion updates UI text, journal state, and objective markers properly.
- [ ] Confirm returning to a quest giver after fulfilling conditions always advances the next step.

## Combat and Exploration

- [ ] Win the first required combat encounter and confirm the correct progression flags trigger.
- [ ] Confirm roaming combat in Verdant Fields can still occur after frontier/worldgen changes.
- [ ] Confirm battle transitions in and out of exploration still behave correctly.
- [ ] Confirm enemy scaling or rank progression feels connected to zone progression.
- [ ] Confirm dungeon or later-zone transitions still work after the world-engine refactors.

## Extraction and Banking

- [ ] Earn gold, components, or shards in the frontier and confirm they are treated as unbanked expedition haul while away from safety.
- [ ] Reach Ironhaven or a valid storage-backed outpost position and confirm the haul banks into the persistent inventory.
- [ ] Save and reload mid-expedition with unbanked haul and confirm it persists as unbanked rather than silently becoming banked.
- [ ] Die while carrying unbanked haul and confirm the haul is lost.
- [ ] Confirm already banked gold, forge materials, and shards remain intact after death.

## Building and Base Claim

- [ ] Find and claim an outpost/base site in Verdant Fields.
- [ ] Enter build mode and confirm the overlay appears with controls and structure selection.
- [ ] Place each starter structure type at least once:
- [ ] `wall`
- [ ] `sentry post`
- [ ] `storage`
- [ ] `fabricator`
- [ ] `power pylon`
- [ ] Confirm placement rules reject invalid terrain, blocked tiles, overlapping placement, and safe-zone violations where intended.
- [ ] Confirm placed structures render properly in the world and remain interactable.
- [ ] Remove a placed structure and confirm materials are refunded correctly.
- [ ] Repair a damaged structure and confirm the repair action works and updates structure health.
- [ ] Save and reload after building and confirm all placed structures persist.

## Reserve Bots and Base Defense

- [ ] Assign a reserve bot to an eligible defense structure.
- [ ] Confirm the reserve bot becomes a live world defender at the base.
- [ ] Confirm the defender holds position or patrols correctly.
- [ ] Lure or wait for enemies near the base and confirm defenders engage them.
- [ ] Confirm defender state survives save/reload closely enough for current design.
- [ ] Confirm removing or destroying a defense post updates defender assignment cleanly.

## Raids and Structure Damage

- [ ] Stay active in the frontier long enough to build raid pressure.
- [ ] Confirm a raid wave can begin.
- [ ] Confirm raid enemies target the player base as expected.
- [ ] Confirm structures can take damage during combat.
- [ ] Confirm damaged structures show updated health feedback.
- [ ] Confirm destroyed structures are removed from the world cleanly.
- [ ] Confirm rebuilding after destruction works without corrupted state.

## Gear, Crafting, and Shops

- [ ] Confirm player gear names are fully rethemed and read like human/adventurer equipment.
- [ ] Confirm robot gear names remain distinct and mechanical.
- [ ] Confirm forge recipes, shop listings, and equipment UI all use the correct renamed player items.
- [ ] Confirm item fusion/upgrade logic stays within the correct player-vs-robot equipment family.

## Death, Meta Progression, and Curses

- [ ] Die after a weak early run and confirm the game-over draft can offer curse cards after repeated shallow failures.
- [ ] Confirm accepting a curse persists its penalty into the next run.
- [ ] Confirm curse penalties actually affect stats, harvest, XP, or starting resources as intended.
- [ ] Reach a stronger run and die with active curses.
- [ ] Confirm the game-over draft can offer purge cards on strong or exceptional runs.
- [ ] Confirm choosing a purge card removes the curse for future runs.
- [ ] Confirm positive augments still appear correctly on qualifying runs.
- [ ] Confirm the player cannot cheaply farm power by repeatedly dying early.

## Save/Load and Persistence

- [ ] Save from town and reload successfully.
- [ ] Save from Verdant Fields and reload successfully.
- [ ] Save with claimed sites, placed structures, defenders, raid state, and active curses, then reload and confirm persistence.
- [ ] Confirm `worldSeed` remains stable across reloads for the same save.
- [ ] Confirm a new fresh save creates a different world seed from a previous save.
- [ ] Confirm quest progression persists entirely through structured quest states and world flags, without any dependence on legacy quest-flag payloads.

## Map Access Audit

- [ ] Visit every zone transition currently reachable in the build.
- [ ] Confirm every map entrance and exit can be physically approached and used by the player.
- [ ] Confirm return gates from combat zones back to town or parent zones are never blocked.
- [ ] Confirm no NPC, chest, or important interactable is stranded behind impassable placement.

## Regression Smoke

- [ ] Start a new save after finishing the long-session test and confirm the opening still works cleanly.
- [ ] Confirm current-version saves reload cleanly after major quest, world-state, and building progress.
- [ ] Confirm no crashes, softlocks, or broken UI states occur during a full play session.

## Automated Coverage Reference

These suites currently cover the main logic and data integrity layers:

- [ ] [WorldSystemsIntegrationTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/world/WorldSystemsIntegrationTest.java)
- [ ] [QuestDialogueIntegrityTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/world/QuestDialogueIntegrityTest.java)
- [ ] [ZoneMapIntegrityTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/world/ZoneMapIntegrityTest.java)
- [ ] [FrontierZoneGeneratorTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/world/FrontierZoneGeneratorTest.java)
- [ ] [BaseBuildingEngineTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/engine/base/BaseBuildingEngineTest.java)
- [ ] [BaseDefenseDirectorTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/engine/base/BaseDefenseDirectorTest.java)
- [ ] [SaveFileTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/data/SaveFileTest.java)
- [ ] [CyberneticEnhancementEngineTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/engine/meta/CyberneticEnhancementEngineTest.java)
- [ ] [EquipmentCatalogDataTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/data/EquipmentCatalogDataTest.java)
- [ ] [ShopCoverageDataTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/data/ShopCoverageDataTest.java)
- [ ] [GameStateTest.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/test/java/com/rogueforge/game/core/GameStateTest.java)

## Suggested Test Order

1. New save opening and town quest flow
2. Town exits, Verdant Fields traversal, first combat
3. Frontier harvesting, site claim, build mode
4. Defender assignment, raid/damage/repair loop
5. Death draft, curse, and purge verification
6. Save/load persistence checks
7. Reachable map-door audit across the rest of the world
