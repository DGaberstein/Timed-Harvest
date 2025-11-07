# Timed Harvest - Fabric Mod

A unified Minecraft Fabric mod that manages resource worlds with **automatic scheduled resets**. No need for multiple mods or complex command schedulers - everything is built-in!

## 🎯 Features

- ✅ **Automatic World Resets** - Schedule resource worlds to reset at configurable intervals
- ✅ **Player Management** - Safely kicks players from worlds before reset
- ✅ **Countdown Warnings** - Warns players before resets occur
- ✅ **Server-Side Tick System** - Uses Minecraft's native tick system for precise scheduling
- ✅ **Persistent State** - Survives server restarts with saved reset schedules
- ✅ **JSON Configuration** - Easy-to-edit configuration files
- ✅ **Admin Commands** - Manual controls and status checking
- ✅ **Multiple Worlds** - Support for multiple resource worlds with independent schedules

## 📦 Installation

1. Download and install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
2. Download [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and place in `mods/` folder
3. Download Timed Harvest mod and place in `mods/` folder
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
      "enabled": false
    }
  ],
  "enableAutoReset": true,
  "notifyPlayersOnReset": true,
  "warningMinutesBeforeReset": 5,
  "kickPlayersOnReset": true
}
```

### Configuration Options

| Field | Type | Description |
|-------|------|-------------|
| `worldId` | String | Unique identifier for the world |
| `dimensionName` | String | Dimension identifier (e.g., `timed_harvest:resource`) |
| `resetIntervalHours` | Number | Hours between automatic resets |
| `worldType` | String | World type (`minecraft:overworld`, `minecraft:the_nether`, etc.) |
| `seed` | Number | World seed (0 = random) |
| `generateStructures` | Boolean | Whether to generate structures |
| `enabled` | Boolean | Whether this world is active |
| `enableAutoReset` | Boolean | Global toggle for automatic resets |
| `notifyPlayersOnReset` | Boolean | Send broadcast messages on reset |
| `warningMinutesBeforeReset` | Number | Minutes before reset to send warning |
| `kickPlayersOnReset` | Boolean | Teleport players out before reset |

## 🎮 Commands

All commands require OP level 2 (operator permissions):

### `/timedharvest reset <worldId>`
Manually resets the specified resource world immediately.

**Example:**
```
/timedharvest reset resource_world
```

### `/timedharvest status [worldId]`
Shows reset status for all worlds or a specific world.

**Examples:**
```
/timedharvest status
/timedharvest status resource_world
```

### `/timedharvest reload`
Reloads the configuration file without restarting the server.

**Example:**
```
/timedharvest reload
```

### `/timedharvest help`
Displays command help information.

## 🔧 How It Works

### Technical Architecture

1. **World Management Module** - Handles dimension creation, deletion, and file management
2. **Scheduler Module** - Tick-based timer system that tracks reset intervals
3. **Configuration System** - JSON-based config with hot-reload support
4. **Command Interface** - Brigadier commands for manual control

### Reset Process Flow

```
Server Tick → Check Scheduler → Time Reached?
                                      ↓ Yes
                      Send Warning (5 min before)
                                      ↓
                      Kick Players from Resource World
                                      ↓
                      Save & Unload World
                                      ↓
                      Delete World Files
                                      ↓
                      World Regenerates on Next Access
                                      ↓
                      Update Next Reset Time
                                      ↓
                      Broadcast Notification
```

## 📅 Example Use Cases

### Weekly Mining World
Reset every Sunday (168 hours):
```json
{
  "worldId": "mining_world",
  "resetIntervalHours": 168,
  "enabled": true
}
```

### Daily Resource Dimension
Reset every 24 hours:
```json
{
  "worldId": "daily_resources",
  "resetIntervalHours": 24,
  "enabled": true
}
```

### Bi-Weekly Nether
Reset every 2 weeks (336 hours):
```json
{
  "worldId": "nether_resources",
  "worldType": "minecraft:the_nether",
  "resetIntervalHours": 336,
  "enabled": true
}
```

## 🛠️ Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/timed-harvest.git
cd timed-harvest

# Build the mod
./gradlew build

# Find the .jar in build/libs/
```

### Project Structure

```
src/main/java/com/timedharvest/
├── TimedHarvestMod.java          # Main entry point
├── command/
│   └── TimedHarvestCommands.java # Command handlers
├── config/
│   └── ModConfig.java            # Configuration management
├── scheduler/
│   └── ResetScheduler.java       # Tick-based scheduling
└── world/
    └── ResourceWorldManager.java # World lifecycle management
```

## 📜 License

MIT License - See LICENSE file for details

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/timed-harvest/issues)
- **Wiki**: [GitHub Wiki](https://github.com/yourusername/timed-harvest/wiki)

## 🙏 Credits

Inspired by:
- **Resource World** mod - World management concepts
- **Command Scheduler** mod - Scheduling functionality

Built with ❤️ using [Fabric](https://fabricmc.net/) based on the tutorial at [horus.dev](https://horus.dev/blog/creating-a-minecraft-mod-using-java-and-fabric)

---

**Note**: This mod is server-side focused but works in single-player. Resource worlds are managed separately from your main world and will reset independently.
