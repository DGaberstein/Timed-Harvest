# Quick Start Guide

## First Time Setup

### 1. Create Your First Resource World

In-game, run:
```
/timedharvest create mining_world "timed_harvest:mining" 168
```

You'll see:
```
✓ Created new resource world 'mining_world'!
  Dimension: timed_harvest:mining
  Reset Interval: 168.0 hours
  IMPORTANT: Restart the server for the dimension to be loaded!
```

### 2. Restart the Server

**This is required!** Dimensions can only load on server startup.

- Close the game/stop the server
- Start it again
- The datapack will be loaded automatically

### 3. Generate the World

After restart, run:
```
/timedharvest reset mining_world
```

This creates the initial world terrain.

### 4. Teleport and Explore

```
/timedharvest tp mining_world
```

You're now in your resource world! Mine, gather, explore - it will reset in 168 hours (1 week).

### 5. Return to Spawn

```
/timedharvest spawn
```

## Common Workflows

### Create a Daily Farming World
```
/timedharvest create farming "timed_harvest:farming" 24
# Restart server
/timedharvest reset farming
/timedharvest tp farming
```

### Create a Test World (3-minute resets)
```
/timedharvest create test "timed_harvest:test" 0.05
# Restart server
/timedharvest reset test
/timedharvest tp test
# Wait 3 minutes - it will auto-reset!
```

### Check All Worlds Status
```
/timedharvest status
```

Output:
```
=== Timed Harvest Status ===
Auto-reset enabled: true
Configured worlds: 2

World ID: mining_world
Enabled: true
Dimension: timed_harvest:mining
Reset Interval: 168.0 hours
Next Reset: 6d 23h 45m

World ID: farming
Enabled: true  
Dimension: timed_harvest:farming
Reset Interval: 24.0 hours
Next Reset: 23h 12m
```

### Temporarily Disable a World
```
/timedharvest disable mining_world
```

The world still exists, but won't auto-reset.

### Re-enable Later
```
/timedharvest enable mining_world
```

### Manual Reset (Instant)
```
/timedharvest reset mining_world
```

Immediately resets the world (kicks players, deletes terrain, regenerates).

## Important Notes

### ⚠️ Always Use Quotes for Dimension Names
```
✓ /timedharvest create world "timed_harvest:mining" 168
✗ /timedharvest create world timed_harvest:mining 168  (FAILS!)
```

### ⚠️ Restart Required After Creating
Datapacks only load on server start. After creating a world, **you must restart**.

### ⚠️ Don't Forget Initial Reset
After creating and restarting, use `/timedharvest reset <worldId>` to generate the initial terrain.

### ⚠️ Players Get Kicked on Reset
When a world resets (automatic or manual), all players inside are teleported to overworld spawn.

## Testing Your Setup

1. Create test world: `/timedharvest create test "timed_harvest:test" 0.05`
2. Restart server
3. Generate: `/timedharvest reset test`
4. Teleport: `/timedharvest tp test`
5. Build something
6. Wait 3 minutes
7. Check it's gone: `/timedharvest tp test`

If everything disappeared, it works! 🎉

## Configuration File

All settings are in `config/timed-harvest.json`:

```json
{
  "resourceWorlds": [
    {
      "worldId": "mining_world",
      "dimensionName": "timed_harvest:mining",
      "resetIntervalHours": 168.0,
      "worldType": "minecraft:overworld",
      "seed": 0,
      "generateStructures": true,
      "enabled": true
    }
  ],
  "enableAutoReset": true,
  "notifyPlayersOnReset": true,
  "warningMinutesBeforeReset": 5,
  "kickPlayersOnReset": true
}
```

You can edit this file manually, then run `/timedharvest reload` in-game.

## Troubleshooting

**Problem:** "Dimension 'timed_harvest:mining' does not exist!"  
**Solution:** You forgot to restart after creating the world.

**Problem:** World is empty/void  
**Solution:** Run `/timedharvest reset <worldId>` to generate terrain.

**Problem:** Command not working  
**Solution:** Make sure you're OP level 2: `/op <yourname>`

**Problem:** Timer not counting down  
**Solution:** Check `/timedharvest status` - is the world enabled?

## Need Help?

- Read `HOW_IT_WORKS.md` for technical details
- Check `COMMANDS.md` for all available commands
- See `COMMAND_USAGE.md` for syntax examples
