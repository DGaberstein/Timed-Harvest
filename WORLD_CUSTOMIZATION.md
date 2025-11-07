# World Customization Features

## Overview
Your Timed Harvest mod now supports custom seeds and world border sizes for resource worlds!

## New Configuration Options

### 1. Custom Seeds (`seed`)
Control the world generation seed for each resource world:

- **Default value**: `0` (automatic seed generation)
- **Behavior**:
  - `0` = Generates a unique seed based on the worldId hash code
  - Any non-zero value = Uses that specific seed for world generation
  
**Example**:
```json
{
  "worldId": "mining_world",
  "dimensionName": "timed_harvest:mining",
  "seed": 12345,
  "worldBorderSize": 5000,
  ...
}
```

### 2. World Border Size (`worldBorderSize`)
Set a world border to limit world size:

- **Default value**: `10000` (10,000 blocks diameter)
- **Measurement**: Diameter in blocks (total width of the border)
- **Behavior**:
  - `0` or negative = No world border
  - Positive value = Sets a circular border centered at 0,0
  
**Examples**:
- `1000` = Small world (500 blocks in each direction from spawn)
- `10000` = Default (5,000 blocks in each direction)
- `20000` = Large world (10,000 blocks in each direction)
- `0` = No border (infinite world)

## How It Works

### Seed Generation
1. When `seed = 0`, the system automatically generates a seed using `worldId.hashCode()`
2. This ensures each world with a different ID gets a unique terrain
3. If you want reproducible worlds, set a specific seed value

### World Border Application
The world border is applied:
1. **On server start** - When the mod initializes
2. **On teleport** - When a player uses `/timedharvest tp <worldId>`
3. **After reset** - When a world is reset (manually or automatically)

## Configuration Example

Here's a complete example showing different world configurations:

```json
{
  "enableAutoReset": true,
  "notifyPlayersOnReset": true,
  "warnBeforeResetMinutes": [60, 30, 10, 5, 1],
  "resourceWorlds": [
    {
      "worldId": "mining_world",
      "dimensionName": "timed_harvest:mining",
      "resetIntervalHours": 168,
      "worldType": "minecraft:overworld",
      "seed": 0,
      "generateStructures": true,
      "worldBorderSize": 5000,
      "enabled": true
    },
    {
      "worldId": "end_world",
      "dimensionName": "timed_harvest:end",
      "resetIntervalHours": 336,
      "worldType": "minecraft:the_end",
      "seed": 98765,
      "generateStructures": true,
      "worldBorderSize": 10000,
      "enabled": true
    },
    {
      "worldId": "creative_world",
      "dimensionName": "timed_harvest:creative",
      "resetIntervalHours": 720,
      "worldType": "minecraft:overworld",
      "seed": 11111,
      "generateStructures": false,
      "worldBorderSize": 0,
      "enabled": true
    }
  ]
}
```

## Testing Your Configuration

1. **Edit the config**: Update `run/config/timed-harvest.json`
2. **Set your desired seed**: Use `0` for auto-generation or a specific number
3. **Set your border size**: Choose diameter in blocks (or `0` for no border)
4. **Restart the server**: Changes apply on next server start
5. **Test teleportation**: Use `/timedharvest tp <worldId>` to visit worlds
6. **Check the border**: Walk to the edge - you should hit the world border

## Technical Details

- **Border shape**: Circular/square border centered at X=0, Z=0
- **Border warning**: Players get a red screen effect when approaching the edge
- **Border damage**: Players take damage if they try to pass through the border
- **Seed consistency**: Same seed = same terrain generation every reset
- **Seed uniqueness**: Different worldId with seed=0 = different terrain

## Common Use Cases

### Small Mining World
```json
{
  "worldId": "mini_mine",
  "seed": 0,
  "worldBorderSize": 1000,
  "resetIntervalHours": 24
}
```

### Specific Seed for Events
```json
{
  "worldId": "event_world",
  "seed": 424242,
  "worldBorderSize": 2000,
  "resetIntervalHours": 168
}
```

### Unlimited Exploration World
```json
{
  "worldId": "explore",
  "seed": 0,
  "worldBorderSize": 0,
  "resetIntervalHours": 720
}
```

## Notes

- World borders apply immediately when players teleport to a world
- Changing the seed requires a world reset to take effect
- Changing the border size applies next time a player visits the world
- A world border of `0` means unlimited world size
- Seed `0` generates unique worlds based on worldId, so changing worldId = new terrain
