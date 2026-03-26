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

- `Aligned`: broadly implemented and matches the docs at a meaningful level
- `Partial`: foundation exists, but scope or behavior falls short of the docs
- `Missing`: described in the docs but not represented in code/data in a meaningful way
- `Docs Ahead`: docs describe target-state content far beyond current production scope

## 3. Executive Summary

The current codebase is best described as:

- a strong early foundation for combat, world-state, quests, roster management, and save/load
- a partial implementation of the tactical combat and progression pillars
- a very early implementation of the final world/content scope

The most important conclusion is:

- The systems documents are usable as a target design
- The code does not yet match the full content and endgame promises in those documents
- The best implementation path is to finish core combat/progression architecture before scaling world/content

## 4. Gap Matrix

| Area | Doc Target | Current Code Status | Evidence | Gap Assessment | Recommended Priority |
|---|---|---|---|---|---|
| Combat command set | Attack, Defend, Ability, Item, Analyze, Flee | `Aligned` | [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java#L38) | All six root commands exist and are playable. | P2 polish |
| Conditional Turn-Based timeline | Visible CTB timeline with speed-cost turn control | `Partial` | [TurnTimeline.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/TurnTimeline.java#L9), [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java#L1094) | A CTB-lite timeline exists, but it is simpler than the final-vision design and still has timing quirks from the review. | P1 |
| Multi-enemy tactical combat | 1-5 enemies, individual targeting, strong tactical depth | `Partial` | [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java#L148), [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L1535) | Multi-enemy support exists, but encounter assembly and combat balance do not yet match the intended depth. | P1 |
| Elemental system | Six elements plus weakness/resistance/absorb and Elemental Break | `Partial` | [ElementalSystem.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/ElementalSystem.java), [CombatResolver.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/CombatResolver.java#L70) | Basic elemental resolution exists, but Elemental Break and full strategic payoff are missing. | P1 |
| Status system | 15 broad statuses with stacking/counter rules | `Partial` | [StatusEffectManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/StatusEffectManager.java#L10), [StatusEffectType.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/StatusEffectType.java) | Many status types and rules exist, but the final spec is wider and needs tighter balance/feedback. | P1 |
| Battle results celebration | XP, gold, proficiency, drops, bestiary updates, level-up messaging | `Partial` | [BattleResultsScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleResultsScreen.java), [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java#L699) | Results flow exists, but needs refinement to match full progression-celebration goals. | P2 |
| Bestiary / Analyze loop | 3 scan levels, archive role, persistent knowledge loop | `Partial` | [BestiaryManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/BestiaryManager.java), [GameState.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/core/GameState.java#L54) | Analyze and persistence exist, but Archive-level hub integration is not present yet. | P2 |
| Ability proficiency | Abilities level 1-10 through use and evolve | `Partial` | [AbilityProgressionState.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/progression/AbilityProgressionState.java), [BattleScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/BattleScreen.java#L736) | System exists and is wired into combat, but needs more balancing and target-state progression coverage. | P1 |
| Unique skill evolution | Mastered abilities transform into unique capstones with party uniqueness | `Partial` | [AbilityEvolutionManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/progression/AbilityEvolutionManager.java#L16), [abilities.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/abilities.json) | The framework exists, including `uniqueSkillId`, but current content breadth is much smaller than the docs. | P1 |
| Weapon proficiency | 8 weapon tracks with distinct Combat Arts | `Partial` | [WeaponType.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/combat/WeaponType.java#L6), [WeaponProficiencyTracker.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/progression/WeaponProficiencyTracker.java#L33) | 8 weapon families exist and proficiency exists, but Combat Arts are still generic labels rather than real weapon-specific actions. | P1 |
| Grade system | Grade controls equipment, access, and party size | `Partial` | [GameState.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/core/GameState.java#L23), [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L3036) | Grade logic exists, but final target rules for party gating and world gating are only partially enforced. | P2 |
| Robot collection and reserve management | Active party plus reserve handling | `Aligned` | [WorkshopScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/WorkshopScreen.java#L210), [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L3020) | Roster, reserve deployment, and party-slot management are already present in meaningful form. | P2 polish |
| Robot evolution | MK-I -> MK-II -> MK-III with progression requirements | `Partial` | [RobotEvolutionManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/progression/RobotEvolutionManager.java), [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L1655) | Evolution framework exists, but final gating through Forge Core/material economy is not there yet. | P1 |
| Starting robot families | Scout / Guardian / Striker / support lines | `Partial` | [robots.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/robots.json), [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L215) | Multiple robot lines exist in data, but discovery and full progression scope are still narrow. | P2 |
| Discoverable robots | 10 discoverable robots across the world | `Missing` | [recruitment.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/recruitment.json#L1) | Only 2 recruitment events exist. This is far short of the final-vision scope. | P3 |
| Overworld exploration | Zone traversal, houses, NPCs, chests, transitions | `Aligned` | [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L1826), [TmxWorldLoader.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/TmxWorldLoader.java) | The foundational exploration loop exists and is one of the stronger implemented areas. | P2 polish |
| Environmental interaction abilities | Fire burns barriers, scan reveals paths, strength moves obstacles | `Missing` | source search only returns docs | This is part of the intended world design, but there is no real implementation yet. | P3 |
| Dragon Riding | Late-game traversal unlock | `Missing` | source/data search only returns docs | No implementation or data support exists yet. | P4 |
| Hub settlement growth | Ironhaven changes as bosses and quests progress | `Partial` | [GameScreen.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/screen/GameScreen.java#L1914), [settlement_upgrades.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/settlement_upgrades.json) | Settlement progression exists in a compact form, but far below the final named facility/service vision. | P2 |
| Hub facilities and named service NPCs | Forge Core, Tavern, Archive, Hangar, Master Silas, Kira, Elena, Rex, Cogs, Bolt, Jax, Watcher | `Missing` | source/data search only finds current smaller cast | The docs are ahead of the implemented hub narrative and service layer. | P3 |
| Quest system | Main quest line + side quests with state/requirements | `Partial` | [QuestManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/QuestManager.java#L14), [quests.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/quests.json#L1) | Quest infrastructure is good, but content scale is far below the docs. | P2 |
| Dialogue system | Requirement-gated NPC dialogue with world-state effects | `Partial` | [DialogueSystem.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/world/DialogueSystem.java#L13), [dialogue.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/dialogue.json) | Dialogue resolution exists and is useful, but the full narrative cast and branching depth are not there yet. | P2 |
| Main story scope | 4 acts, ~18 main quests, Origin Core climax | `Docs Ahead` | [quests.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/quests.json#L1) | Current story content is a small early arc; the docs describe the long-term target. | P4 |
| Side quest scope | 60+ side quests across categories | `Docs Ahead` | [quests.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/quests.json#L1) | Current side content is modest and nowhere near target scale. | P4 |
| World scale | 16+ production zones / 25+ world spaces | `Docs Ahead` | [zones.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/zones.json#L1) | Only 3 zones exist now, so the docs are intentionally ahead of production. | P4 |
| Enemy compendium scale | full rank ladder and broad roster | `Docs Ahead` | [monsters.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/monsters.json) | Only 6 monsters exist in data; the final docs envision a much larger enemy database. | P4 |
| Crafting system | multi-station crafting using gathered materials | `Missing` | no `CraftingSystem` in source; source/data search returns docs only | This is a major missing production system. | P3 |
| Equipment fusion | Kira fuses same-tier gear into next-tier items | `Missing` | source/data search only returns docs | No fusion system exists yet. | P3 |
| Equipment tier ladder | Tier 1-5 + Mythic | `Partial` | [equipment.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/equipment.json) | Early tier gear exists, but final-tier and Mythic systems do not. | P3 |
| Dynamic shops / wandering merchant | restocking by state, Jax rotation | `Missing` | [shop_inventories.json](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/data/shop_inventories.json#L1), source search only returns docs for Jax | Shop system is static compared with target design. | P3 |
| Save/load breadth | persistent game state across multiple systems | `Aligned` | [SaveFile.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/data/SaveFile.java#L14), [SaveManager.java](/Users/grantbrooks/Documents/GitHub/RogueForge/core/src/main/java/com/rogueforge/game/persistence/SaveManager.java#L14) | Save architecture is already quite broad and is one of the strongest foundations. | P2 verification |
| Endgame / post-game | The Void, The Beyond, Infinite Dungeon, superbosses | `Missing` | source/data search only returns docs | Entirely future-facing at present. | P5 |

## 5. Recommended Build Order

### Phase 1: Core Combat and Progression Closure
Build these first because later content depends on them.

1. Fix and finish CTB combat behavior
2. Complete elemental payoff, including missing mechanics
3. Tighten status effect rules and feedback
4. Finish weapon proficiency and real Combat Arts
5. Finish ability mastery and unique-skill progression
6. Tighten battle rewards and progression presentation

Why first:

- Every later zone, boss, robot, and item depends on combat feeling correct
- Tuning content before the underlying progression systems are stable creates expensive rework

### Phase 2: Progression Infrastructure

1. Formalize grade/party-slot gating
2. Complete robot evolution requirements
3. Expand bestiary/archive integration
4. Strengthen hub systems that display progression state

Why second:

- This locks the player-growth model before major content expansion

### Phase 3: Economy and Equipment

1. Crafting system
2. Equipment fusion
3. Expanded equipment tiers
4. Shop progression and dynamic inventory

Why third:

- Crafting and fusion depend on a stable material/reward economy
- Those rewards depend on final combat/progression outputs

### Phase 4: World and Narrative Scale-Up

1. Expand zone count and world graph
2. Expand enemy roster
3. Expand main story into acts
4. Add side-quest categories
5. Add named Ironhaven NPC/service structure
6. Add discoverable robots

Why fourth:

- Content production scales best after systems are stable

### Phase 5: Traversal, Endgame, and Post-Game

1. Environmental interaction ability layer
2. Dragon Riding
3. The Void
4. Infinite Dungeon
5. The Beyond and superboss content

Why last:

- These are expensive and depend on nearly every earlier system

## 6. Best First Build Target

The best next implementation target is:

### Combat/Progression Closure Sprint

Recommended first chunk:

1. Correct and harden CTB behavior
2. Implement missing elemental payoff
3. Replace placeholder Combat Arts with real weapon-specific unlocks

This is the highest-leverage next step because it:

- directly closes the biggest system gap in the docs
- improves the fun of every future playtest
- reduces the risk of rebalancing later content twice

## 7. Immediate Implementation Candidates

If we start building right away, the best options are:

### Option A: Combat Foundations

- Fix remaining CTB/timeline issues
- Add Elemental Break
- clean up cooldown timing

Best if:

- we want to stabilize the tactical core first

### Option B: Weapon Mastery Expansion

- implement weapon-specific Combat Arts
- wire unlocks into battle actions and UI

Best if:

- we want a contained, high-value progression feature next

### Option C: Crafting/Economy Foundations

- add `CraftingSystem`
- define recipe data format
- add first crafting station flow

Best if:

- we want to begin content/economy expansion early

## 8. Recommendation

Start with `Option A: Combat Foundations`.

Reason:

- It sits on the critical path for almost every other gameplay system
- It directly aligns the current code with the strongest parts of the new docs
- It makes later robot, zone, item, and boss work easier to design and verify
