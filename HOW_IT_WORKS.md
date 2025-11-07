# How Resource Worlds Work - Technical Guide

## Overview

The Timed Harvest mod now automatically generates **Minecraft datapacks** to create custom dimensions. This is the proper, official way to add dimensions to Minecraft.

## How It Works

### 1. Creating a World

When you run:
```
/timedharvest create mining_world "timed_harvest:mining" 168
```

The mod does the following:
1. **Adds the world to config** (`config/timed-harvest.json`)
2. **Generates a datapack** in `world/datapacks/timed_harvest/` with:
   - `pack.mcmeta` - Datapack metadata
   - `data/timed_harvest/dimension/mining.json` - Dimension definition
   - `data/timed_harvest/dimension_type/mining_type.json` - Dimension properties

3. **Notifies you** that a server restart is required

### 2. Server Restart Required

**IMPORTANT:** Custom dimensions can only be loaded when the server starts. After creating a world:
1. Stop the server/close the game
2. Restart it
3. The dimension will now be loaded and accessible

### 3. Using the World

After restart:
```
# Generate the world terrain
/timedharvest reset mining_world

# Teleport to it
/timedharvest tp mining_world

# Check status
/timedharvest status mining_world

# Return to spawn
/timedharvest spawn
```

## Datapack Structure

```
world/
└── datapacks/
    └── timed_harvest/
        ├── pack.mcmeta
        └── data/
            └── timed_harvest/          (or your custom namespace)
                ├── dimension/
                │   └── mining.json      (dimension definition)
                └── dimension_type/
                    └── mining_type.json (dimension properties)
```

## Dimension Configuration

Each dimension is configured as an overworld-like world with:
- Normal sky and lighting
- Day/night cycle
- Weather
- Mob spawning
- Structures (villages, etc.)
- Standard world generation
- Height: -64 to +320 (same as overworld)

## Reset Behavior

When a world resets:
1. **Players are kicked** and teleported to overworld spawn
2. **World files are deleted** (`world/dimensions/timed_harvest/mining/`)
3. **Datapack remains** (dimension still exists, just empty)
4. **Next visit regenerates** the world with new terrain

## Automatic Reset Schedule

The scheduler tracks each world independently:
- Countdown timer based on `resetIntervalHours`
- Warnings at configured intervals (default: 5 minutes before)
- Automatic reset when timer expires
- State persists across server restarts

## Multiple Worlds

You can create as many resource worlds as you want:

```
/timedharvest create mining "timed_harvest:mining" 168    # Weekly
/timedharvest create farming "timed_harvest:farming" 24   # Daily  
/timedharvest create nether_mining "timed_harvest:nether" 336  # Bi-weekly
```

Each gets its own:
- Dimension
- Reset schedule
- World files
- Datapack entry

## Troubleshooting

### "Dimension does not exist"
**Solution:** You forgot to restart after creating the world. Datapacks only load on server start.

### World not generating
**Solution:** Use `/timedharvest reset <worldId>` to trigger initial generation.

### Can't teleport
**Solutions:**
1. Restart server if just created
2. Check `/timedharvest status <worldId>` to verify it's enabled
3. Make sure you used quotes: `/timedharvest create test "timed_harvest:test" 24`

### Datapack not working
**Check:**
1. File exists: `world/datapacks/timed_harvest/pack.mcmeta`
2. Dimension files exist in correct namespace folder
3. Server logs for datapack loading errors
4. Run `/reload` after manual datapack changes

## Best Practices

1. **Use meaningful names:** `mining_world`, `farming_world`, etc.
2. **Namespace convention:** Use `timed_harvest:` or your mod ID
3. **Test short intervals:** Use `0.05` hours (3 min) for testing
4. **Production intervals:** Weekly (168) or daily (24) for actual use
5. **Disable unused worlds:** `/timedharvest disable <worldId>` to pause resets

## Advanced: Manual Datapack Editing

You can manually edit the generated datapacks:

1. Edit `world/datapacks/timed_harvest/data/*/dimension/*.json`
2. Change generator settings, biome sources, etc.
3. Run `/reload` in-game
4. Restart server for dimension type changes

See [Minecraft Wiki - Custom Dimensions](https://minecraft.wiki/w/Custom_dimension) for full options.
