## Tiled Repaint Workflow

Use this when repainting gameplay maps like [town.tmx](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps/town.tmx) so the visuals can change without breaking the game.

### Safe rule

Only repaint tile layers.

Do not rename or delete these object layers:
- `collisions`
- `doors`
- `spawn_points`
- `npcs`
- `features`
- `chests`

Those layers drive gameplay, not just visuals.

### Ironhaven setup

[town.tmx](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps/town.tmx) now includes:
- the old placeholder tilesets
- [serene_village.tsx](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps/serene_village.tsx)
- [serene_village_animated.tsx](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps/serene_village_animated.tsx)

That means you can repaint Ironhaven in Tiled using the new village art without changing the map path or the gameplay markers.

### Recommended process

1. Open [town.tmx](/Users/grantbrooks/Documents/GitHub/RogueForge/assets/maps/town.tmx) in Tiled.
2. Leave all object layers alone.
3. Repaint `01_Ground` first.
4. Repaint structure/detail layers after the ground is readable.
5. Keep paths visually distinct from grass.
6. Keep doors visually aligned with the `doors` objects.
7. Keep NPC standing areas on walkable tiles.
8. Save and run the map integrity tests.

### Before saving a big repaint

Check these against the object layers:
- player spawn points still land on walkable ground
- the east gate still looks reachable
- house doors still visually match the interaction points
- annex NPCs still stand on sensible tiles

### Verification

Run:

```bash
./gradlew core:test --tests com.rogueforge.game.world.ZoneMapIntegrityTest --tests com.rogueforge.game.world.WorldSystemsIntegrationTest
```

If those pass, the map should still be playable at the systems level.
