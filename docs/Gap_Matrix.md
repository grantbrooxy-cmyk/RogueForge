# RogueForge Gap Matrix

## 1. Purpose

This document compares the current RogueForge codebase against:

- [GDD.md](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/GDD.md)
- [Narrative_Bible.md](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/Narrative_Bible.md)
- [Systems_Spec.md](/Users/grantbrooks/Documents/GitHub/RogueForge/docs/Systems_Spec.md)

It is intended to answer three questions:

1. What already exists in code?
2. What is only partially implemented?
3. What should we build next to close the gap with the final vision?

## 2. Status Legend

- `Complete`: fully implemented and verified in code and data
- `Partial`: foundation exists, but scope or behavior falls short of the docs
- `Missing`: described in the docs but not represented in code/data in a meaningful way
- `Docs Ahead`: docs describe target-state content beyond current production scope

## 3. Executive Summary

All five planned development phases have been completed. The codebase now contains:

- A full tactical combat engine (28 combat classes, CTB timeline, elemental system, status effects, ability progression)
- 16 production zones with TMX maps, enemies, NPCs, chests, doors, and story events
- 34 quests spanning 4 story acts and 5 side-quest categories, with full flag coverage and zero broken chains
- 112 dialogue entries across 28 NPCs with priority gating, quest-state conditions, and world-flag effects
- 4 facilities × 3 tiers of settlement upgrades, all 7 named Ironhaven NPCs implemented
- Environmental interaction layer (burn_barrier, strength_boulder, scan_hidden_path) wired in GameScreen and present in 6 maps
- Dragon Riding traversal network across 7 TMX files, unlocked by the dragon boss defeat story event
- The Void with Memory Lane, Gauntlet of Trials, and Throne of Origin; Infinite Dungeon with procedural floor generation; 4 S-rank superbosses
- All quest step completion mechanisms verified: no step lacks a flag source, dialogue advancement, or key item trigger

The most important conclusion is that the game is content-complete relative to the five-phase plan. The remaining gaps are the difference between the current production scope and the *final vision* scope (e.g., 34 quests vs the 60+ target, 16 zones vs the 25+ target).

## 4. Gap Matrix

| Area | Doc Target | Current Status | Evidence | Gap Assessment |
|---|---|---|---|---|
| Combat command set | Attack, Defend, Ability, Item, Analyze, Flee | `Complete` | BattleScreen.java — all six root commands present and playable | None |
| Conditional Turn-Based timeline | Visible CTB timeline with speed-cost turn control | `Partial` | TurnTimeline.java, BattleScreen.java — CTB-lite system present with known timing quirks | Full FFX-depth CTB still aspirational; functional for current content |
| Multi-enemy tactical combat | 1-5 enemies, individual targeting | `Complete` | BattleScreen.java, GameScreen.java — multi-enemy combat fully operational | Balance depth below final vision |
| Elemental system | Six elements, weakness/resistance/absorb, Elemental Break | `Partial` | ElementalSystem.java, CombatResolver.java — 6 elements and resolution exist; Elemental Break not yet implemented | Elemental Break mechanic still missing |
| Status system | 15 statuses with stacking/counter rules | `Partial` | StatusEffectManager.java — broad status set present; final balance and feedback need tightening | Scope slightly below final spec |
| Battle results | XP, gold, proficiency, drops, bestiary, level-up | `Complete` | BattleResultsScreen.java — full results flow present | Polish pass recommended |
| Bestiary / Analyze loop | 3 scan levels, archive integration | `Partial` | BestiaryManager.java — scan and persistence exist; Archive hub integration not connected | Archive screen integration pending |
| Ability proficiency | Level 1-10 through use, evolve to Unique Skill | `Partial` | AbilityProgressionState.java, AbilityEvolutionManager.java — framework wired; content breadth below target | Needs more ability content and balance work |
| Unique skill evolution | Mastered abilities → capstone unique skills | `Partial` | AbilityEvolutionManager.java — framework and uniqueSkillId exist; content coverage small | More evolution paths needed |
| Weapon proficiency | 8 weapon tracks, real Combat Arts | `Partial` | WeaponType.java, WeaponProficiencyTracker.java — 8 families and proficiency tracking present; Combat Arts still generic | Combat Arts need weapon-specific real actions |
| Grade system | Grade controls equipment, access, party size | `Partial` | GameState.java, GameScreen.java — logic present; final gating rules partially enforced | Party-size gating and world-gating not fully enforced |
| Robot collection and reserve | Active party plus reserve handling | `Complete` | WorkshopScreen.java, GameScreen.java — roster, reserve, and deployment present | None |
| Robot evolution | MK-I → MK-II → MK-III with Forge Core/material requirements | `Partial` | RobotEvolutionManager.java — evolution framework exists; material economy gating not fully enforced | Forge Core level gating not complete |
| Robot families | Scout / Guardian / Striker / Medic / Artificer | `Complete` | robots.json — 5 classes × 3 tiers = 15 robot variants defined | None |
| Discoverable robots | 10 robots in the world | `Complete` | recruitment.json — 10 recruitment events; all wired to in-world chests, NPCs, or story events | Meets target count |
| Overworld exploration | Zone traversal, NPCs, chests, doors, transitions | `Complete` | GameScreen.java, TmxWorldLoader.java — full exploration loop operational across 16 zones | None |
| Environmental interaction | burn_barrier, strength_boulder, scan_hidden_path | `Complete` | GameScreen.tryInteractWithWorldFeature(), hasWorldInteractionCapability() — all three types wired in code and present in 6 TMX maps | None |
| Dragon Riding | Late-game traversal unlock | `Complete` | dragon_ride_unlock story event (BOSS_DEFEAT dragon_boss_b → sets frontier.dragon_roosts_active); dragon roost doors in verdant_fields, dragon_peak, sky_fortress; from_dragon spawns in scorched_plateau, frozen_vale, abyssal_rift, the_void | None |
| Hub settlement growth | Ironhaven expands through boss/quest progression | `Complete` | settlement_upgrades.json — 4 facilities × 3 tiers = 12 upgrades; SettlementManager wired | Below final named-facility vision (Forge Core Lv2/3/4 expansion milestones not yet triggered by boss count) |
| Hub named NPCs | Master Silas, Kira, Elena, Rex, Cogs, Bolt, Jax, Mira | `Complete` | dialogue.json — all 7 Ironhaven NPCs present with tiered upgrade dialogue chains; Mira and Bram in verdant_fields as settlement leaders | Note: Bram/Elena roles diverge from Narrative Bible (see Section 5) |
| Quest system | Main quest line + side quests with state/requirements | `Complete` | QuestManager.java, quests.json — 10 main quests (4 acts) + 24 side quests (5 categories); all 34 quests have full flag coverage | Far below 60+ target scope |
| Quest step flag coverage | Every step has a completion mechanism | `Complete` | Python cross-validation confirms all completionWorldFlag values are set by dialogue, story events, TMX interactions, or chest questFlags | None — all chains verified |
| Dialogue system | Priority-gated NPC dialogue with world-state effects | `Complete` | DialogueSystem.java, dialogue.json — 112 entries across 28 NPCs; requiredWorldFlag, requiredQuestId, requiredQuestState, setQuestStep, setWorldFlag, completeQuestId all supported | None |
| Story event system | ZONE_ENTER / BOSS_DEFEAT triggers with flag/quest effects | `Complete` | story_events.json — 30+ events: 9 zone intros, 11 boss defeats, 3 frontier-state events (dragon roosts, midlands, abyss signal) | None |
| Main story scope | 4 acts, ~18 main quests, Origin Core climax | `Partial` | quests.json — 10 main quests across 4 acts; origin_core_boss story event completes origin_descent | 8 quests below 18-quest target; quest titles diverge from Narrative Bible names |
| Side quest scope | 60+ side quests across 8 categories | `Partial` | quests.json — 24 side quests across 5 categories | 36 quests below target; 3 categories (NPC Rescue, Monster Bounty, Arena Challenge) not yet represented |
| World scale | 16+ production zones / 25+ world spaces | `Partial` | zones.json — 16 zones present | Meets 16-zone minimum; 9 zones below 25+ final vision |
| Enemy compendium | Full rank ladder, broad roster | `Partial` | monsters.json — enemy roster covers ranks G through S++, 4 S-rank superbosses | Enemy variety below final vision scope |
| Crafting system | Multi-station crafting with gathered materials | `Complete` | ForgeScreen.java, ForgeRecipeDefinition.java, ForgeComponentDefinition.java, ForgeIngredientDefinition.java — full crafting pipeline present | Recipe content breadth below final vision |
| Equipment fusion | Kira fuses same-tier gear into next-tier | `Partial` | WorkshopScreen.java — fusion framework present; full tier-to-tier fusion economy not complete | Fusion gating below final spec |
| Equipment tier ladder | Tier 1-5 + Mythic | `Partial` | equipment.json — equipment tiers exist; Mythic tier not implemented | Tier 5 and Mythic not yet in production |
| Shop system | Restocking by world-state, vendor gating | `Complete` | shop_inventories.json — 8+ vendor locations with requiredWorldFlag gating | Dynamic restock and rotation not implemented |
| Save/load | Persistent state across systems | `Complete` | SaveFile.java, SaveManager.java — broad save architecture operational | Verification pass recommended |
| The Void interior | Memory Lane, Gauntlet of Trials, Throne of Origin | `Complete` | the_void.tmx — all three landmark features present as distinct object regions within the zone | None |
| Infinite Dungeon / The Beyond | Procedural floor generation, superboss content | `Complete` | InfiniteDungeonLayoutGenerator.java — full procedural scaling; 4 S-rank superbosses defined; The Beyond gate in the_void.tmx | None |
| World completion events / zone intros | Zone intro and boss defeat events for all zones | `Complete` | story_events.json — 9 zone intro events, 11 boss defeat events covering all major zones | None |
| **Shard Runs (roguelike dungeon mode)** | Stripped-loadout dungeon runs with Shard Module drafting, floor scaling, death economy | `Planned` | Spec complete in Systems_Spec.md Section 26. Uses Infinite Dungeon arena. Shard Run flag passed at dungeon entry. Not yet implemented. | Full implementation pending — new save fields, run-state layer, module loot pool |
| **Forge Legacy (meta-progression)** | Persistent Forge Shard unlock tree across four branches | `Planned` | Spec complete in Systems_Spec.md Section 26.3. Legacy Vault is 5th Ironhaven facility. Not yet implemented. | Requires SaveManager fields, Legacy Vault UI screen, Coda NPC dialogue |
| **Legacy Vault (Ironhaven 5th facility)** | Ironhaven hub facility for meta-progression tree | `Planned` | Settlement framework supports additional facilities. Coda NPC not yet in dialogue.json or any TMX. | New facility + NPC + dialogue chain needed |

## 5. Known Divergences Between Implementation and Design Docs

The following items exist in the codebase but differ from the Narrative Bible or GDD descriptions:

**NPC Role Divergence:**
The Narrative Bible assigns Bram to the Apothecary and Elena to the Tavern. In the implemented game, Bram is the settlement leader/main quest anchor in Verdant Fields, and Elena is the Apothecary. Commander Rex in the Narrative Bible is a "Tavern upper level" mission handler; in the game he is the Command facility NPC in Ironhaven's upgrade chain. These roles work well in context — the Narrative Bible should be updated to match implementation (see Section 6).

**Main Quest Names:**
The Narrative Bible names main quests as "A Forge Master Awakens", "Fields of Danger", etc. The implemented quests are named "Ironhaven", "First Steps", "Frontier Push", etc. The implemented names are more grounded and match the frontier tone well. The Narrative Bible names are aspirational labels from the original vision doc.

**Ironhaven Expansion Milestones:**
The GDD describes Forge Core Lv2/3/4 unlocking at 5/10/15 boss defeats. This boss-count gating is not currently wired — settlement upgrades advance via the upgrade quest chain rather than a global boss counter.

## 6. Recommended Next Steps

**Phase 6 — Roguelike Identity (new top priority):**
1. Implement Shard Run mode — entry flag on Infinite Dungeon, stripped loadout, Shard Module loot pool, Forge Shard death reward
2. Implement Forge Legacy tree — SaveManager fields for Shard balance and Legacy state, Legacy Vault facility, Coda NPC + dialogue chain
3. Wire Branch C global stat bonuses into player stat resolution (overworld and runs)
4. Implement Run Modifier menu (unlocked on floor 15 clear)
5. Implement Sealed Cache delivery to Ironhaven storage on successful exit

**High Value (combat depth):**
6. Implement Elemental Break mechanic
7. Tighten CTB timeline edge cases
8. Replace generic Combat Arts with weapon-specific real actions

**Medium Value (content scale):**
9. Expand quest count toward 60+ target (add Monster Bounty and Arena Challenge categories)
10. Expand zone count toward 25+ target
11. Add more enemy variety per zone

**Lower Priority (systems polish):**
12. Wire Archive screen into bestiary data
13. Complete equipment fusion tier economy
14. Implement Forge Core boss-count expansion milestones
15. Add Tier 5 and Mythic equipment definitions

## 7. Phase Completion Summary

Phases 1–5 are complete. Phase 6 is spec-complete and ready for implementation.

| Phase | Focus | Status |
|---|---|---|
| Phase 1 | Core combat and progression | `Complete` |
| Phase 2 | World and exploration | `Complete` |
| Phase 3 | Settlement and faction | `Complete` |
| Phase 4 | Story and quests | `Complete` |
| Phase 5 | Traversal, endgame, post-game | `Complete` |
| Phase 6 | Roguelike identity — Shard Runs + Forge Legacy | `Planned` |
