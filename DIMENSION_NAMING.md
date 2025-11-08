# Dimension Naming Guide

## Understanding Minecraft Dimension IDs

Minecraft dimensions use a `namespace:path` format. Understanding the correct names is crucial for creating resource worlds.

## ❌ Common Mistakes

These dimension IDs **DO NOT EXIST** in vanilla Minecraft:
- `minecraft:nether` ❌ (Wrong!)
- `minecraft:end` ❌ (Wrong!)
- `minecraft:overworld` ❌ (Already exists as default world!)

## ✅ Correct Vanilla Dimension IDs

Use these for vanilla Minecraft dimensions:
- `minecraft:the_nether` ✓ (Vanilla Nether)
- `minecraft:the_end` ✓ (Vanilla End)
- `minecraft:overworld` ✓ (Vanilla Overworld - already exists)

## 🎯 Custom Dimension Naming

For custom resource worlds, use your own namespace:
- `timed_harvest:nether` ✓ (Custom nether-type world)
- `timed_harvest:end` ✓ (Custom end-type world)
- `timed_harvest:mining` ✓ (Custom mining world)
- `timed_harvest:farming` ✓ (Custom farming world)
- `myserver:resource_nether` ✓ (Your custom namespace)

## 📋 Create Command Examples

### Creating a Custom Nether World
```
/timedharvest create nether_world timed_harvest:nether 24 minecraft:the_nether
```
- **World ID**: `nether_world` (your internal name)
- **Dimension**: `timed_harvest:nether` (unique dimension ID)
- **Reset Hours**: `24` (resets every 24 hours)
- **World Type**: `minecraft:the_nether` (uses nether biomes)

### Creating a Custom End World
```
/timedharvest create end_world timed_harvest:end 48 minecraft:the_end
```
- **World ID**: `end_world`
- **Dimension**: `timed_harvest:end`
- **Reset Hours**: `48`
- **World Type**: `minecraft:the_end` (uses end biomes)

### Creating a Custom Mining World
```
/timedharvest create mining timed_harvest:mining 12 minecraft:overworld
```
- **World ID**: `mining`
- **Dimension**: `timed_harvest:mining`
- **Reset Hours**: `12`
- **World Type**: `minecraft:overworld` (uses overworld biomes)

### With All Options
```
/timedharvest create resource timed_harvest:resource 24 minecraft:overworld 12345 5000 true
```
- **Seed**: `12345` (custom seed, use `0` for random)
- **Border Size**: `5000` (5000 blocks, use `0` for infinite)
- **Structures**: `true` (enable structures)

## 🔧 World Types vs Dimension Names

**They are different!**

- **Dimension Name**: The unique ID for your custom dimension (e.g., `timed_harvest:nether`)
  - Must be unique across all dimensions
  - Can use any namespace (but avoid `minecraft:` for custom worlds)
  
- **World Type**: The biome preset and world generation (e.g., `minecraft:the_nether`)
  - Determines what biomes generate
  - Valid types: `minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`

## 🎮 In-Game Help

Use these commands for help:
- `/timedharvest help` - Show all commands
- `/timedharvest help troubleshooting` - Common issues and fixes

## ⚠️ Important Notes

1. **After creating a world**, you must **restart** the server/game
2. Then run `/timedharvest reset <worldId>` to generate the dimension
3. Dimensions cannot load with `/reload` - they require a restart
4. The mod will validate dimension names and show errors if you use invalid IDs

## 💡 Quick Reference

| World Type | Use This | Not This |
|------------|----------|----------|
| Nether | `timed_harvest:nether` + `minecraft:the_nether` | `minecraft:nether` ❌ |
| End | `timed_harvest:end` + `minecraft:the_end` | `minecraft:end` ❌ |
| Overworld | `timed_harvest:mining` + `minecraft:overworld` | `minecraft:overworld` as dimension ⚠️ |

**Note**: `minecraft:overworld` already exists as the default world, so create custom dimensions for resource worlds!
