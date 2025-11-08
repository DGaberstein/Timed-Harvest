# Timed Harvest - Command Reference

All commands require OP level 2 (operator permissions).

## Basic Commands

### Help
```
/timedharvest help
```
Shows all available commands.

### Status
```
/timedharvest status
```
Shows status of all configured worlds.

```
/timedharvest status <worldId>
```
Shows detailed status for a specific world.

### Reload Configuration
```
/timedharvest reload
```
Reloads the configuration file without restarting the server.

## World Management

### Create New World
```
/timedharvest create <worldId> <dimensionName> <resetHours> [worldType] [seed] [borderSize] [structures]
```
Creates a new resource world with customizable options.

**Required Parameters:**
- `worldId` - Unique identifier for the world
- `dimensionName` - Namespaced dimension name (e.g., `timed_harvest:mining`)
- `resetHours` - Reset interval in hours (can be decimal)

**Optional Parameters:**
- `worldType` - World dimension type (default: `minecraft:overworld`)
  - Options: `minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`
- `seed` - World generation seed (default: 0 for random)
- `borderSize` - World border diameter in blocks (default: 0 for no border)
- `structures` - Enable structures (default: true)

**Examples:**
- `/timedharvest create mining timed_harvest:mining 168`
  - Weekly mining world with defaults
  
- `/timedharvest create nether timed_harvest:nether 72 minecraft:the_nether`
  - Nether world that resets every 3 days
  
- `/timedharvest create pvp timed_harvest:pvp 24 minecraft:overworld 12345 5000`
  - Daily PvP world with specific seed and 5km border
  
- `/timedharvest create speedrun timed_harvest:speedrun 1 minecraft:overworld 999 10000 false`
  - Hourly speedrun world with no structures

**Note:** After creating a world, **restart the server/game** then use `/timedharvest reset <worldId>` to generate it.

📖 **See [CREATE_COMMAND_GUIDE.md](CREATE_COMMAND_GUIDE.md) for detailed examples and use cases.**

### Enable/Disable World
```
/timedharvest enable <worldId>
```
Enables automatic resets for a world.

```
/timedharvest disable <worldId>
```
Disables automatic resets for a world (keeps the world but stops auto-reset).

### Manual Reset
```
/timedharvest reset <worldId>
```
Immediately resets a resource world. This will:
- Kick all players from the world
- Delete the world files
- Regenerate the world from scratch
- Reset the countdown timer

## Teleportation

### Teleport to Resource World
```
/timedharvest tp <worldId>
```
Teleports you to the spawn point of a resource world.

**Examples:**
- `/timedharvest tp resource_world`
- `/timedharvest tp mining_world`

### Teleport to Spawn
```
/timedharvest spawn
```
Teleports you back to the overworld spawn point.

## Common Workflows

### Creating and Setting Up a New World
1. Create the world configuration:
   ```
   /timedharvest create my_world timed_harvest:my_world 168
   ```

2. Generate the world:
   ```
   /timedharvest reset my_world
   ```

3. Teleport to the new world:
   ```
   /timedharvest tp my_world
   ```

4. Check the status:
   ```
   /timedharvest status my_world
   ```

### Testing with Short Reset Times
For testing purposes, you can create worlds with very short reset intervals:

```
/timedharvest create test_world timed_harvest:test 0.05
```
This creates a world that resets every 3 minutes (0.05 hours).

### Managing Multiple Worlds
View all worlds:
```
/timedharvest status
```

Disable a specific world temporarily:
```
/timedharvest disable old_world
```

Re-enable it later:
```
/timedharvest enable old_world
```

## Configuration File

The configuration is stored in `config/timed-harvest.json`. You can also edit this file directly, but remember to run `/timedharvest reload` afterward.

Example configuration:
```json
{
  "resourceWorlds": [
    {
      "worldId": "resource_world",
      "dimensionName": "timed_harvest:resource",
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

## Tips

- Use `/timedharvest spawn` to quickly return to safety before a reset
- Check `/timedharvest status` regularly to see when the next reset is scheduled
- Dimension names should follow the format `namespace:path` (e.g., `timed_harvest:mining`)
- Each world should have a unique `worldId` and `dimensionName`
- Reset intervals are in hours and support decimals (e.g., 0.5 = 30 minutes)
