# Timed Harvest - Fabric Mod

A Minecraft Fabric mod that creates automatically resetting resource worlds with configurable dimensions, custom seeds, and world borders. Players can teleport to dedicated mining, nether, or end worlds that regenerate on scheduled intervals to provide fresh resources while preserving the main world.

## 🎯 Features

- ✅ **Automatic World Resets** - Schedule resource worlds to reset at configurable intervals
- ✅ **Player-Friendly GUI** - Simple `/th` command opens a visual world selector
- ✅ **Custom Seeds** - Set specific seeds or auto-generate unique worlds
- ✅ **World Borders** - Configure world size limits for better performance
- ✅ **Pagination** - Handle unlimited worlds with page navigation
- ✅ **Player Management** - Safely kicks players from worlds before reset
- ✅ **Countdown Warnings** - Configurable warnings before resets occur
- ✅ **Tab Completion** - All admin commands support tab completion
- ✅ **Persistent State** - Survives server restarts with saved reset schedules
- ✅ **JSON Configuration** - Easy-to-edit configuration files
- ✅ **Multiple Worlds** - Support for multiple resource worlds with independent schedules

## 📦 Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
2. Download [Fabric API](https://https://modrinth.com/mod/fabric-api) and place in `mods/` folder
3. Download **Timed Harvest** from [Modrinth](https://modrinth.com/mod/timed-harvest) and place in `mods/` folder
4. Start your server/client
5. Edit the generated config file at `config/timed-harvest.json`
6. Restart or use `/timedharvest reload`

## ⚙️ Configuration

The mod generates a configuration file at `config/timed-harvest.json`:

```json
{
  "resourceWorlds": [
    {
      "worldId": "resource_world",
      "dimensionName": "timed_harvest:resource",
      "resetIntervalHours": 168,
      "worldType": "minecraft:overworld",
      "seed": 0,
      "generateStructures": true,
      "worldBorderSize": 10000,
      "enabled": false
    }
  ],
  "enableAutoReset": true,
  "notifyPlayersOnReset": true,
  "warnBeforeResetMinutes": [60, 30, 10, 5, 1],
  "kickPlayersOnReset": true
}
```

### Configuration Options

| Field | Type | Description |
|-------|------|-------------|
| `worldId` | String | Unique identifier for the world |
| `dimensionName` | String | Dimension identifier (e.g., `timed_harvest:resource`) |
| `resetIntervalHours` | Number | Hours between automatic resets |
| `worldType` | String | World type (`minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`) |
| `seed` | Number | World seed (0 = auto-generate unique seed based on worldId) |
| `generateStructures` | Boolean | Whether to generate structures (villages, temples, etc.) |
| `worldBorderSize` | Number | World border diameter in blocks (0 = no border) |
| `enabled` | Boolean | Whether this world is active |
| `enableAutoReset` | Boolean | Global toggle for automatic resets |
| `notifyPlayersOnReset` | Boolean | Send broadcast messages on reset |
| `warnBeforeResetMinutes` | Array | List of times (in minutes) to warn before reset |
| `kickPlayersOnReset` | Boolean | Teleport players out before reset |

## 🎮 Commands

### Player Commands (No Permissions Required)

#### `/th`
Opens a visual GUI to teleport to resource worlds. Features:
- Click world icons to teleport instantly
- View world info (type, reset time, border size)
- "Return to Spawn" button
- Navigation buttons for multiple pages (9 worlds per page)
- World buttons centered in middle row for easy access

**Example:**
```
/th
```

### Admin Commands (Requires OP Level 2)

#### `/timedharvest reset <worldId>`
Manually resets the specified resource world immediately.

**Example:**
```
/timedharvest reset mining_world
```

#### `/timedharvest status [worldId]`
Shows reset status for all worlds or a specific world.

**Examples:**
```
/timedharvest status
/timedharvest status mining_world
```

#### `/timedharvest tp <worldId>`
Teleport to a specific resource world (admin version).

**Example:**
```
/timedharvest tp mining_world
```

#### `/timedharvest spawn`
Teleport back to the overworld spawn.

**Example:**
```
/timedharvest spawn
```

#### `/timedharvest create <worldId> <dimensionName> <resetHours>`
Create a new resource world configuration.

**Example:**
```
/timedharvest create end_world timed_harvest:end 168
```

#### `/timedharvest enable <worldId>`
Enable a disabled world.

**Example:**
```
/timedharvest enable mining_world
```

#### `/timedharvest disable <worldId>`
Disable a world without deleting its configuration.

**Example:**
```
/timedharvest disable mining_world
```

#### `/timedharvest delete <worldId>`
Delete a world from the configuration.

**Example:**
```
/timedharvest delete old_world
```

#### `/timedharvest reload`
Reloads the configuration file without restarting the server.

**Example:**
```
/timedharvest reload
```

#### `/timedharvest help`
Displays command help information (shows different commands based on permission level).

**Example:**
```
/timedharvest help
```

## 🌍 World Customization

### Custom Seeds

Control world generation with seeds:
- **`seed: 0`** - Automatically generates a unique seed based on worldId (different worlds = different terrain)
- **`seed: 12345`** - Uses specific seed for reproducible world generation

### World Borders

Set borders to limit world size and improve performance:
- **`worldBorderSize: 10000`** - 10,000 block diameter (5,000 blocks from center)
- **`worldBorderSize: 5000`** - Smaller world for faster exploration
- **`worldBorderSize: 0`** - No border (unlimited world)

Borders are applied automatically when players teleport to worlds.

## 🔧 How It Works

### Technical Architecture

1. **World Management Module** - Handles dimension creation via datapacks, deletion, and file management
2. **Scheduler Module** - Tick-based timer system that tracks reset intervals
3. **GUI System** - Visual world selector with pagination support
4. **Configuration System** - JSON-based config with hot-reload support
5. **Command Interface** - Brigadier commands with tab completion

### Reset Process Flow

```
Server Tick → Check Scheduler → Time Reached?
                                      ↓ Yes
                      Send Warnings (60, 30, 10, 5, 1 min before)
                                      ↓
                      Kick Players from Resource World
                                      ↓
                      Save & Unload World
                                      ↓
                      Delete World Files
                                      ↓
                      World Regenerates on Next Access
                                      ↓
                      Apply World Border (if configured)
                                      ↓
                      Update Next Reset Time
                                      ↓
                      Broadcast Notification
```

## 📅 Example Configurations

### Weekly Mining World (Small Border)
```json
{
  "worldId": "mining_world",
  "dimensionName": "timed_harvest:mining",
  "resetIntervalHours": 168,
  "worldType": "minecraft:overworld",
  "seed": 0,
  "generateStructures": true,
  "worldBorderSize": 5000,
  "enabled": true
}
```

### Daily Resource Dimension (Specific Seed)
```json
{
  "worldId": "daily_resources",
  "dimensionName": "timed_harvest:daily",
  "resetIntervalHours": 24,
  "worldType": "minecraft:overworld",
  "seed": 424242,
  "generateStructures": true,
  "worldBorderSize": 3000,
  "enabled": true
}
```

### Monthly End World (No Border)
```json
{
  "worldId": "end_resources",
  "dimensionName": "timed_harvest:end",
  "resetIntervalHours": 720,
  "worldType": "minecraft:the_end",
  "seed": 0,
  "generateStructures": true,
  "worldBorderSize": 0,
  "enabled": true
}
```

## 🛠️ Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/DGaberstein/Timed-Harvest.git
cd Timed-Harvest

# Build the mod
./gradlew build

# Find the .jar in build/libs/
```

### Project Structure

```
src/main/java/com/timedharvest/
├── TimedHarvestMod.java          # Main entry point
├── command/
│   └── TimedHarvestCommands.java # Command handlers with tab completion
├── config/
│   └── ModConfig.java            # Configuration management
├── gui/
│   └── WorldSelectionGui.java    # Visual world selector GUI
├── scheduler/
│   └── ResetScheduler.java       # Tick-based scheduling
└── world/
    ├── ResourceWorldManager.java # World lifecycle management
    └── DatapackGenerator.java    # Dynamic datapack creation
```

### Key Features Implementation

- **Datapack System**: Dynamically generates dimension datapacks for custom worlds
- **GUI System**: ScreenHandler-based inventory GUI with pagination
- **Auto-reload**: Automatically reloads datapacks on server start
- **Seed Generation**: Unique seeds using worldId.hashCode() for consistent uniqueness
- **World Borders**: Applied on-the-fly when players access worlds

## 📜 License

MIT License - See LICENSE file for details

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/DGaberstein/Timed-Harvest/issues)
- **Wiki**: [GitHub Wiki](https://github.com/DGaberstein/Timed-Harvest/wiki)

## 🙏 Credits

Inspired by:
- **Resource World** mod - World management concepts
- **Command Scheduler** mod - Scheduling functionality

Built with ❤️ using [Fabric](https://fabricmc.net/)

---

## 🎨 GUI Preview

The `/th` command opens a clean, centered GUI:

```
┌─────────────────────────────────┐
│     Resource Worlds             │
├─────────────────────────────────┤
│    [Empty Top Row]              │
│                                 │
│   🟩 Mining  🔴 Nether  ⬜ End  │ ← Worlds (centered)
│                                 │
│ 🛏️ Spawn  ◀ Prev  Next ▶  📦   │ ← Actions
└─────────────────────────────────┘
```

**Features:**
- World icons change based on world type
- Hover to see reset time and border size
- Click to teleport instantly
- Pagination for 10+ worlds
- No permissions required

---

**Note**: This mod is server-side focused but works in single-player. Resource worlds are managed separately from your main world and will reset independently. Dimensions are created using Minecraft's datapack system for maximum compatibility.

**Requirements:**
- Minecraft 1.20.1
- Fabric Loader 0.15.11+
- Fabric API 0.92.2+
- Java 21
