# Timed Harvest - Fabric Mod

A Minecraft Fabric mod that creates automatically resetting resource worlds with configurable dimensions, custom seeds, and world borders. Players can teleport to dedicated mining, nether, or end worlds that regenerate on scheduled intervals to provide fresh resources while preserving the main world.

## 🎯 Features

- ✅ **Automatic World Resets** - Schedule resource worlds to reset at configurable intervals
- ✅ **Player-Friendly GUI** - Simple `/th` command opens a professional world selector with title bar
- ✅ **Admin Dashboard GUI** - Visual management interface for operators (`/th admin`), now with confirmation dialogs for all critical actions (delete, reset, teleport, enable/disable, set main, status)
- ✅ **Confirmation Dialogs** - All admin actions now require confirmation via a dedicated GUI, preventing accidental changes
- ✅ **Consistent Navigation** - Back buttons and navigation always return you to the correct previous screen (e.g., Resource Worlds GUI)
- ✅ **Custom Seeds** - Set specific seeds or auto-generate truly random worlds
- ✅ **World Borders** - Configure world size limits for better performance
- ✅ **Pagination** - Handle unlimited worlds with arrow-based navigation
- ✅ **Player Management** - Safely kicks players from worlds before reset
- ✅ **Countdown Warnings** - Configurable warnings before resets occur
- ✅ **Tab Completion** - All admin commands support intelligent tab completion
- ✅ **Persistent State** - Survives server restarts with saved reset schedules
- ✅ **JSON Configuration** - Easy-to-edit configuration files with auto-validation
- ✅ **Multiple Worlds** - Support for unlimited resource worlds with independent schedules
- ✅ **Permission-Based UI** - Admin features only visible to operators
- ✅ **Professional Styling** - Consistent gold/yellow theme across all interfaces
- ✅ **Enhanced Create Command** - Full customization with worldType, seed, border, and structures
- ✅ **Dimension Validation** - Prevents invalid dimension names with helpful error messages

## 📦 Installation & Requirements

1. **Java 21 LTS required!**
  - Download and install [Java 21](https://www.oracle.com/java/technologies/downloads/) (JDK 21)
  - Set `JAVA_HOME` to your JDK 21 install (e.g., `C:\Program Files\Java\jdk-21`)
2. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place in `mods/` folder
4. Download **Timed Harvest** from [Modrinth](https://modrinth.com/mod/timed-harvest) and place in `mods/` folder

---

## 🆕 Recent Changes
- **Confirmation Dialogs**: All admin actions now require confirmation via a dedicated GUI, preventing accidental changes.
- **Consistent Navigation**: Back buttons and navigation always return you to the correct previous screen (e.g., Resource Worlds GUI).

- **Java 21 LTS**: Project now requires Java 21 for building and running.
- **Modern Gradle**: Uses Gradle 8.8+ for full Java 21 support.
- **Enhanced GUIs**: World Selector and Admin Dashboard now have professional styling, permission-based features, and improved navigation.
- **Create Command**: Fully customizable with world type, seed, border, and structures.
- **Auto-Fix & Validation**: Automatic config validation and in-game troubleshooting help.
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
Opens a professional visual GUI to teleport to resource worlds. Features:
- **Title Bar**: Yellow glass pane header with "Resource Worlds" label
- **World Icons**: Click grass blocks, netherrack, or end stone to teleport instantly
- **World Info**: Hover to see world type, reset time, border size, and structures
- **Return to Spawn**: Red bed button to teleport to overworld spawn
- **Navigation**: Arrow buttons for previous/next pages with page indicator
- **Admin Access**: Nether star button (operators only) to open Admin Dashboard
- **Centered Layout**: Worlds displayed in middle row for easy access
- **Permission-Aware**: Admin features only visible to operators

**Example:**
```
/th
```

#### `/th admin`
Opens the **Admin Dashboard GUI** for operators. Features:
- **Visual World Management**: See all worlds with status indicators (🟢 enabled / 🔴 disabled)
- **World Details**: View dimension, type, reset interval, seed, border, and next reset time
- **Quick Actions**: 
  - Left-click world → Show management commands
  - Right-click world → Toggle enabled/disabled instantly
- **Action Buttons**:
  - 📖 Reload Config
  - 💎 Create World (shows command syntax)
  - 📘 Help & Commands
  - ➡️ Page Navigation
- **Professional Layout**: 6-row interface with title bar and organized sections
- **Real-Time Updates**: GUI refreshes when toggling world status

**Requires**: Operator permission (level 2)

**Example:**
```
/th admin
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

#### `/timedharvest create <worldId> <dimensionName> <resetHours> [worldType] [seed] [borderSize] [structures]`
Create a new resource world with full customization options.

**Parameters:**
- `worldId` - Unique identifier (e.g., `nether`, `mining`)
- `dimensionName` - Dimension ID with namespace (e.g., `timed_harvest:nether`, `minecraft:the_nether`)
- `resetHours` - Hours between resets (e.g., `24`, `168`)
- `worldType` (optional) - World generation type: `minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`
- `seed` (optional) - Custom seed (0 = random, default: 0)
- `borderSize` (optional) - Border diameter in blocks (0 = infinite, default: 0)
- `structures` (optional) - Generate structures: `true` or `false` (default: true)

**Dimension Naming Important:**
- ❌ **Wrong**: `minecraft:nether` (doesn't exist!)
- ✅ **Right**: `minecraft:the_nether` (vanilla nether)
- ✅ **Right**: `timed_harvest:nether` (custom nether world)

**Examples:**
```bash
# Basic: Custom nether world, 24-hour reset
/timedharvest create nether timed_harvest:nether 24

# With world type: Use nether biomes
/timedharvest create nether timed_harvest:nether 24 minecraft:the_nether

# With seed: Specific terrain
/timedharvest create mining timed_harvest:mining 12 minecraft:overworld 123456

# With border: Limited 5000-block world
/timedharvest create end timed_harvest:end 48 minecraft:the_end 0 5000

# Full options: Everything customized
/timedharvest create resource timed_harvest:resource 168 minecraft:overworld 424242 10000 true
```

**After Creating:**
1. Restart the server/game (dimensions load on startup only)
2. Run `/timedharvest reset <worldId>` to generate the world
3. Access via `/th` GUI or `/timedharvest tp <worldId>`

**Tab Completion:**
- Automatically suggests valid dimension names
- Suggests valid world types
- Suggests boolean values for structures

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

#### `/timedharvest help troubleshooting`
Shows common troubleshooting tips and fixes, including:
- How to fix "Dimension does not exist" errors
- Correct dimension naming guide
- Steps after config changes
- Auto-fix features explanation

**Example:**
```
/timedharvest help troubleshooting
```

## 🌍 World Customization

### Custom Seeds

Control world generation with seeds:
- **`seed: 0`** - Automatically generates a truly random seed using `new Random().nextLong()` (saved to config for reproducibility)
- **`seed: 12345`** - Uses specific seed for reproducible world generation
- **Auto-saved**: Generated random seeds are automatically saved to config for world regeneration

### World Types

Choose the correct world type for proper biome generation:
- **`minecraft:overworld`** - Normal overworld biomes (plains, forests, mountains, etc.)
- **`minecraft:the_nether`** - Nether biomes (nether wastes, crimson forest, warped forest, etc.)
- **`minecraft:the_end`** - End biomes (end highlands, end midlands, end barrens, etc.)

**Important**: World type affects biome generation:
- Nether worlds need `minecraft:the_nether` to generate nether biomes
- End worlds need `minecraft:the_end` to generate end terrain
- Using wrong type will generate overworld biomes in all dimensions

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
3. **GUI System** - Professional ScreenHandler-based interfaces with permission-aware features:
   - **World Selector GUI** - Player teleportation interface with title bar and navigation
   - **Admin Dashboard GUI** - Visual world management with left/right click actions
4. **Configuration System** - JSON-based config with hot-reload and auto-validation
5. **Command Interface** - Brigadier commands with intelligent tab completion and dimension validation
6. **Biome Generation** - Proper biome source configuration for overworld, nether, and end dimensions

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

### Daily Nether Resources (Custom Seed)
```json
{
  "worldId": "nether_resources",
  "dimensionName": "timed_harvest:nether",
  "resetIntervalHours": 24,
  "worldType": "minecraft:the_nether",
  "seed": 123456789,
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
  "seed": -987654321,
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
│   └── TimedHarvestCommands.java # Command handlers with tab completion & validation
├── config/
│   └── ModConfig.java            # Configuration management with auto-validation
├── gui/
│   ├── WorldSelectionGui.java    # Player world selector (permission-aware)
│   └── AdminDashboardGui.java    # Admin management interface (NEW!)
├── scheduler/
│   └── ResetScheduler.java       # Tick-based scheduling
└── world/
    ├── ResourceWorldManager.java # World lifecycle management
    └── DatapackGenerator.java    # Dynamic datapack creation with biome sources
```

### Key Features Implementation

- **Datapack System**: Dynamically generates dimension datapacks with proper biome sources
- **GUI System**: Dual-interface design - player selector + admin dashboard
- **Permission System**: Permission-based UI visibility (admin features only for operators)
- **Auto-reload**: Automatically reloads datapacks on server start
- **Seed Generation**: Truly random seeds using `new Random().nextLong()` with auto-save
- **World Borders**: Applied on-the-fly when players access worlds
- **Dimension Validation**: Prevents creation of invalid dimension names with helpful errors
- **Biome Generation**: Correct biome sources for overworld (multi_noise), nether (multi_noise + preset), and end (the_end type)

## 📜 License

MIT License - See LICENSE file for details

## 📚 Documentation

### Feature Documentation
- **[ADMIN_DASHBOARD.md](ADMIN_DASHBOARD.md)** - Complete Admin Dashboard GUI guide
- **[DIMENSION_NAMING.md](DIMENSION_NAMING.md)** - Dimension naming conventions and validation

### User Guides
- **[CREATE_COMMAND_GUIDE.md](CREATE_COMMAND_GUIDE.md)** - Complete guide to creating worlds with all options
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Common issues and solutions
- **[COMMANDS.md](COMMANDS.md)** - Full command reference
- **[WORLD_CUSTOMIZATION.md](WORLD_CUSTOMIZATION.md)** - World customization guide

### Technical Documentation
- **[ADMIN_DASHBOARD_SUMMARY.md](ADMIN_DASHBOARD_SUMMARY.md)** - Implementation overview
- **[ADMIN_DASHBOARD_VISUAL.md](ADMIN_DASHBOARD_VISUAL.md)** - ASCII art visual reference
- **[WORLD_SELECTOR_UPDATE.md](WORLD_SELECTOR_UPDATE.md)** - World Selection GUI styling update
- **[DIMENSION_NAMING_FIX.md](DIMENSION_NAMING_FIX.md)** - Dimension validation fix summary

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

### World Selection GUI (`/th`)

The `/th` command opens a professional GUI with title bar and navigation:

```
┌───────────────────────────────────────────┐
│ 🟨🟨🟨 Resource Worlds 🟨🟨🟨              │ ← Title Bar (Yellow Glass)
├───────────────────────────────────────────┤
│                                           │
│   🟩 Mining  🔴 Nether  ⬜ End            │ ← Worlds (Row 1)
│                                           │
│ 🛏️ Spawn  ◀ Prev  📄 1/2  Next ▶  ⭐     │ ← Actions (Row 2)
└───────────────────────────────────────────┘
```

**Features:**
- Professional title bar with yellow glass panes
- World icons change based on world type
- Hover to see reset time and border size
- Click to teleport instantly
- Arrow-based pagination for 10+ worlds
- Page indicator shows current/total pages
- ⭐ Admin button (only visible to operators)

### Admin Dashboard GUI (`/th admin`)

Operators see a powerful 6-row management interface. All critical actions (delete, reset, teleport, enable/disable, set main, status) now require confirmation via a dedicated GUI, making admin actions safer and more user-friendly:

```
┌───────────────────────────────────────────┐
│ 🟨🟨🟨 Admin Dashboard 🟨🟨🟨               │ ← Title Bar
├───────────────────────────────────────────┤
│                                           │
│   🟩 Mining World        ⏱️ 60 min       │ ← World 1
│   Left-click: Commands  Right-click: OFF  │
│                                           │
│   🔴 Nether Resources    ⏱️ 120 min      │ ← World 2
│   Left-click: Commands  Right-click: ON   │
│                                           │
│   [More worlds...]                        │
│                                           │
├───────────────────────────────────────────┤
│ 🔄 Reload  ➕ Create  ❓ Help  ◀ ▶  ❌    │ ← Quick Actions
└───────────────────────────────────────────┘
```

**Features:**
- Left-click worlds to see commands
- Right-click worlds to toggle enabled/disabled
- Real-time updates when toggling
- Quick action buttons for common tasks
- Pagination for unlimited worlds
- Permission-based access (operators only)

---

**Note**: This mod is server-side focused but works in single-player. Resource worlds are managed separately from your main world and will reset independently. Dimensions are created using Minecraft's datapack system for maximum compatibility.

**Requirements:**
- Minecraft 1.20.1
- Fabric Loader 0.15.11+
- Fabric API 0.92.2+
- Java 21
