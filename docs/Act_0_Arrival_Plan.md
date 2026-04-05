# RogueForge Act 0 Plan: Arrival

## 1. Purpose

This document defines Act 0 as the dedicated tutorial and onboarding phase of RogueForge.

Act 0 should exist to teach the player the core gameplay loop in a controlled, readable form before the broader frontier and progression layers fully open up.

Core loop Act 0 teaches:

1. Prepare in Ironhaven
2. Travel to the frontier
3. Explore, fight, gather, and pursue quests
4. Decide whether to push or return
5. Bank resources
6. Upgrade and unlock
7. Go out again

## 2. Act Identity

### Act

- Act 0

### Name

- Arrival

### Function

- tutorial phase
- onboarding phase
- first-loop teaching phase

### Goal

- teach the player the game loop

## 3. Player Learns

During Act 0, the player should learn:

- movement
- basic combat
- shops
- basic quests
- the first zone
- banking resources

These are the minimum literacy goals for the rest of the game.

If the player reaches the end of Act 0 and still does not understand one of these, the tutorial phase has failed.

## 4. Unlocks

Act 0 should unlock:

- town shops
- first weapon
- first abilities
- first robot
- Verdant Fields

These are not late rewards. They are the minimum starting kit needed to make the first real expedition loop meaningful.

## 5. Main Quest Shape

Act 0 main quests should cover:

- speak to town NPCs
- first combat
- gather materials
- repair workshop
- scout frontier

These quests should not feel like detached errands. Each one should teach one piece of the loop.

## 6. Current Build Mapping

The current build already covers large parts of this structure.

### Already Present

- `ironhaven_arrival`
  - meet Bram
  - speak to town NPCs
  - return to Bram
- `first_steps`
  - speak to Mira
  - survive Verdant Fields
  - return to Mira
- early town shops and workshop access
- first robot and early combat onboarding
- Verdant Fields as the first live frontier zone
- banking behavior through town safety and storage rules
- workshop restoration path through `workshop_tools`

### Current Best-Fit Content

The existing content that most closely maps to Act 0 is:

- `ironhaven_arrival`
- `first_steps`
- the earliest stretch of `workshop_tools`

## 7. Current Gaps

The current build does not yet present Act 0 as a clean, explicit tutorial act.

Main gaps:

- the quest data currently tags early main quests as `act: 1` rather than a distinct `act: 0`
- “gather materials” exists mechanically, but is not yet framed as a formal first-loop teaching quest
- “banking resources” is implemented, but not yet taught explicitly as part of onboarding
- workshop repair exists, but sits slightly later and is not yet clearly presented as an Act 0 capstone
- the phase transition from “arrival tutorial” into “frontier play” is still more implicit than it should be

## 8. Recommended Act 0 Structure

The clean Act 0 flow should be:

### Step 1: Arrival in Ironhaven

- wake in town
- meet Bram
- establish immediate purpose

### Step 2: Learn the Settlement

- speak to required NPCs
- see shop, workshop, and support functions
- understand that Ironhaven is the preparation space

### Step 3: First Frontier Push

- enter Verdant Fields
- survive first combat
- learn that the frontier is dangerous but manageable

### Step 4: First Material Recovery

- gather a simple set of early resources
- connect resources to repair, crafting, or town progression

### Step 5: First Return and Banking Lesson

- return to town or other safe banking point
- see resources become persistent
- understand the difference between carrying risk and securing gains

### Step 6: Workshop Repair / Restoration Beat

- use returned progress to unlock or restore a core service
- show that bringing resources home changes Ironhaven

### Step 7: Scout Frontier Transition

- receive the next outward-facing objective
- make it clear that the real frontier loop now begins

## 9. Success Criteria

Act 0 is successful when the player clearly understands:

- Ironhaven is where I prepare
- Verdant Fields is where I take risk
- loot should be brought back and banked
- returning home changes what I can do next
- new quests and routes come from successful returns

If those lessons are clear, the rest of the game can build on them.

## 10. Immediate Recommendation

Do not treat Act 0 as only a story introduction.

Treat it as a gameplay contract with the player:

- this is how RogueForge works

The next practical cleanup after this definition is:

1. decide whether to formally retag early quest data as `act: 0`
2. add a clearer first banking tutorial moment
3. make workshop restoration the visible payoff of the first full loop
