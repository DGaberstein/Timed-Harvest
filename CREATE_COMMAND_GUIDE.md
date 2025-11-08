# Create Command Guide

## Overview
The `/timedharvest create` command allows you to create new resource worlds with full customization options including world type, seed, border size, and structure generation.

## Command Syntax

### Basic Usage (Minimal)
```
/timedharvest create <worldId> <dimensionName> <resetHours>
```
Creates a world with default settings:
- World Type: `minecraft:overworld`
- Seed: Random (hash-based)
- Border Size: None (0)
- Structures: Enabled

**Example:**
```
/timedharvest create mining timed_harvest:mining 168
```

---

### With World Type
```
/timedharvest create <worldId> <dimensionName> <resetHours> <worldType>
```
Specify the world type (overworld, nether, or end).

**Example:**
```
/timedharvest create nether_res timed_harvest:nether 72 minecraft:the_nether
```

---

### With World Type + Seed
```
/timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed>
```
Set a specific seed for consistent world generation.

**Example:**
```
/timedharvest create survival timed_harvest:survival 168 minecraft:overworld 12345678
```

Use `0` for seed to get a random hash-based seed:
```
/timedharvest create random_world timed_harvest:random 24 minecraft:overworld 0
```

---

### With World Type + Seed + Border
```
/timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed> <borderSize>
```
Add a world border for performance and containment.

**Example:**
```
/timedharvest create pvp timed_harvest:pvp 24 minecraft:overworld 999 5000
```
This creates a 5000-block diameter world border.

Use `0` for no border:
```
/timedharvest create unlimited timed_harvest:unlimited 168 minecraft:overworld 12345 0
```

---

### Full Command (All Options)
```
/timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed> <borderSize> <structures>
```
Complete control over all world settings.

**Example:**
```
/timedharvest create speedrun timed_harvest:speedrun 6 minecraft:overworld 42 10000 false
```

**Parameters:**
- `structures`: `true` or `false` - Enable/disable structure generation (villages, temples, etc.)

---

## Parameter Details

### `<worldId>`
- **Type:** Single word (no spaces)
- **Purpose:** Unique identifier for the world
- **Examples:** `mining`, `nether_farm`, `end_resources`
- **Used in:** Commands and GUI

### `<dimensionName>`
- **Type:** Namespaced string
- **Format:** `namespace:name`
- **Examples:** 
  - `timed_harvest:mining`
  - `timed_harvest:nether`
  - `mymod:custom_world`
- **Important:** Must include namespace!

### `<resetHours>`
- **Type:** Number (can be decimal)
- **Purpose:** How often the world resets
- **Examples:**
  - `168` - Weekly (7 days)
  - `24` - Daily
  - `0.5` - Every 30 minutes
  - `720` - Monthly (30 days)

### `<worldType>` (Optional)
- **Type:** Namespaced world type
- **Default:** `minecraft:overworld`
- **Valid Values:**
  - `minecraft:overworld` - Normal terrain
  - `minecraft:the_nether` - Nether dimension
  - `minecraft:the_end` - End dimension
- **Tab Completion:** Available in-game

### `<seed>` (Optional)
- **Type:** Long number
- **Default:** `0` (random hash-based)
- **Purpose:** World generation seed
- **Examples:**
  - `0` - Random (uses worldId hash)
  - `12345678` - Specific seed
  - `-999999` - Negative seeds work too
- **Note:** Use `0` for unique random worlds

### `<borderSize>` (Optional)
- **Type:** Integer (positive or 0)
- **Default:** `0` (no border)
- **Purpose:** World border diameter in blocks
- **Examples:**
  - `0` - No border (infinite)
  - `5000` - 5000 block diameter
  - `10000` - 10000 block diameter
  - `1000` - Small 1000 block world
- **Performance Tip:** Smaller borders = better performance

### `<structures>` (Optional)
- **Type:** Boolean
- **Default:** `true`
- **Valid Values:** `true` or `false`
- **Purpose:** Enable/disable structure generation
- **Structures Include:**
  - Villages
  - Temples
  - Strongholds
  - Mansions
  - Nether Fortresses
  - End Cities
- **Use `false` for:** Speedruns, PvP arenas, clean worlds

---

## Common Use Cases

### 1. Weekly Mining World
```
/timedharvest create mining timed_harvest:mining 168 minecraft:overworld 0 10000 true
```
- Resets weekly
- Random seed
- 10km border
- All structures

### 2. Daily Nether Farm
```
/timedharvest create nether_farm timed_harvest:nether 24 minecraft:the_nether 555 5000 true
```
- Resets daily
- Fixed seed (555)
- 5km border
- Fortresses enabled

### 3. Hourly PvP Arena
```
/timedharvest create pvp timed_harvest:pvp 1 minecraft:overworld 0 1000 false
```
- Resets hourly
- Random seed
- 1km border
- No structures (flat-ish terrain)

### 4. Monthly End Resource World
```
/timedharvest create end_world timed_harvest:end 720 minecraft:the_end 0 0 true
```
- Resets monthly
- Random seed
- No border
- End cities enabled

### 5. Speedrun Practice World
```
/timedharvest create speedrun timed_harvest:speedrun 0.25 minecraft:overworld 42 0 false
```
- Resets every 15 minutes
- Fixed seed (42)
- No border
- No structures for pure challenge

---

## Output Information

After creating a world, you'll see:

```
✓ Created new resource world 'mining'!

World Settings:
Dimension: timed_harvest:mining
World Type: minecraft:overworld
Seed: Random (hash-based)
World Border: 10000 blocks
Structures: Enabled
Reset Interval: 168.0 hours

Next steps:
1. Restart the server/game
2. Run: /timedharvest reset mining
3. Access via /th gui or /timedharvest tp mining
```

---

## Important Notes

### ⚠️ Restart Required
- New dimensions **MUST** be loaded on startup
- `/reload` is **NOT** sufficient
- Datapacks are created but need a full restart

### 🔄 After Restart
1. Run `/timedharvest reset <worldId>` to initialize the world
2. The world will then appear in the `/th` GUI
3. Players can now access it

### 📝 Configuration
- All settings are saved to `config/timed-harvest.json`
- You can manually edit this file for advanced customization
- Run `/timedharvest reload` after manual edits

---

## Examples by World Type

### Overworld Worlds
```bash
# Basic mining world
/timedharvest create mining timed_harvest:mining 168

# Survival challenge with seed
/timedharvest create survival timed_harvest:survival 720 minecraft:overworld 123456

# Small bordered world
/timedharvest create small timed_harvest:small 24 minecraft:overworld 0 3000 true
```

### Nether Worlds
```bash
# Nether resource farm
/timedharvest create nether_res timed_harvest:nether 48 minecraft:the_nether

# Specific seed nether
/timedharvest create nether_fixed timed_harvest:nether2 72 minecraft:the_nether 777 8000 true
```

### End Worlds
```bash
# End city farming
/timedharvest create end_farm timed_harvest:end 168 minecraft:the_end

# Clean end (no structures)
/timedharvest create end_clean timed_harvest:end2 24 minecraft:the_end 0 0 false
```

---

## Troubleshooting

### "World already exists"
```
/timedharvest list
```
Check if the worldId is already in use. Use a different name or delete the existing world first.

### "Invalid number format"
- Ensure hours, seed, and border are valid numbers
- Use `0` for defaults
- Decimals are allowed for hours (e.g., `0.5` = 30 minutes)

### "Dimension doesn't exist after restart"
1. Check that you restarted (not just `/reload`)
2. Run `/timedharvest reset <worldId>`
3. Check logs for datapack generation errors

### World doesn't appear in GUI
1. Ensure `enabled: true` in config
2. Run `/timedharvest reload`
3. Restart if it's a new world

---

## See Also

- **[README.md](README.md)** - General mod information
- **[COMMANDS.md](COMMANDS.md)** - All commands reference
- **[WORLD_CUSTOMIZATION.md](WORLD_CUSTOMIZATION.md)** - Manual config editing
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Common issues and fixes
