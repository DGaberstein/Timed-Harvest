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

## � Documentation & Wiki

Timed Harvest now uses a dedicated [GitHub Wiki](https://github.com/DGaberstein/Timed-Harvest/wiki) for all major documentation and guides. For the latest instructions, guides, and troubleshooting, please visit:

- [Installation Guide](https://github.com/DGaberstein/Timed-Harvest/wiki/Installation)
- [Configuration Guide](https://github.com/DGaberstein/Timed-Harvest/wiki/Configuration)
- [Commands Reference](https://github.com/DGaberstein/Timed-Harvest/wiki/Commands)
- [GUI Guide](https://github.com/DGaberstein/Timed-Harvest/wiki/GUI-Guide)
- [Troubleshooting & FAQ](https://github.com/DGaberstein/Timed-Harvest/wiki/Troubleshooting-FAQ)
- [Changelog](https://github.com/DGaberstein/Timed-Harvest/wiki/Changelog)

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

See the [Configuration Guide](https://github.com/DGaberstein/Timed-Harvest/wiki/Configuration) for full details, example configs, and option explanations.


## 🎮 Commands

See the [Commands Reference](https://github.com/DGaberstein/Timed-Harvest/wiki/Commands) for a full list of commands, usage examples, and admin features.


## 🌍 World Customization

See the [Configuration Guide](https://github.com/DGaberstein/Timed-Harvest/wiki/Configuration) for details on seeds, world types, borders, and more.

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

## 📚 Additional Documentation

The following Markdown files provide extra technical and implementation details:

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
│ 🟨🟨🟨 Resource Worlds 🟨🟨🟨          │ ← Title Bar (Yellow Glass)
├───────────────────────────────────────────┤
│                                           │
│   🟩 Mining  🔴 Nether  ⬜ End           │ ← Worlds (Row 1)
│                                           │
│ 🛏️ Spawn  ◀ Prev  📄 1/2  Next ▶  ⭐    │ ← Actions (Row 2)
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
│ 🟨🟨🟨 Admin Dashboard 🟨🟨🟨          │ ← Title Bar
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
│ 🔄 Reload  ➕ Create  ❓ Help  ◀ ▶  ❌ │ ← Quick Actions
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
