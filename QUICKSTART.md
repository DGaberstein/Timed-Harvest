# Quick Start Guide - Timed Harvest

## 🎯 Goal
Set up a resource world that automatically resets every week to keep resources fresh for your server players.

## ⚡ 5-Minute Setup

### Step 1: Build the Mod
```bash
cd "c:\Users\Thicc_White\Desktop\Timed Harvest"
.\gradlew build
```

The mod JAR will be created at: `build\libs\timed-harvest-1.0.0.jar`

### Step 2: Install on Server

1. Copy `timed-harvest-1.0.0.jar` to your server's `mods/` folder
2. Make sure **Fabric API** is also installed in `mods/`
3. Start your server - it will generate default config

### Step 3: Configure Your Resource World

Edit `config/timed-harvest.json`:

```json
{
  "resourceWorlds": [
    {
      "worldId": "mining_world",
      "dimensionName": "timed_harvest:mining",
      "resetIntervalHours": 168,
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

**Change `"enabled": true`** to activate the world!

### Step 4: Reload Config
```
/timedharvest reload
```

### Step 5: Check Status
```
/timedharvest status
```

You should see:
```
=== Timed Harvest Status ===
Auto-reset enabled: true
Configured worlds: 1

World ID: mining_world
Enabled: true
Dimension: timed_harvest:mining
Reset Interval: 168 hours
Next Reset: 6d 23h 59m
```

## ✅ You're Done!

Your resource world will now:
- ✅ Reset automatically every 7 days (168 hours)
- ✅ Warn players 5 minutes before reset
- ✅ Safely teleport players out before reset
- ✅ Regenerate with a new seed
- ✅ Notify all players when reset is complete

## 🎮 Common Configurations

### Daily Reset (24 hours)
```json
"resetIntervalHours": 24
```

### Twice Weekly (84 hours = 3.5 days)
```json
"resetIntervalHours": 84
```

### Monthly (720 hours = 30 days)
```json
"resetIntervalHours": 720
```

### No Warning Before Reset
```json
"warningMinutesBeforeReset": 0
```

### 15 Minute Warning
```json
"warningMinutesBeforeReset": 15
```

## 🔧 Manual Reset

Need to reset immediately?
```
/timedharvest reset mining_world
```

## 📊 Check Time Until Reset

```
/timedharvest status mining_world
```

## 🔄 Multiple Resource Worlds

Want separate mining and nether resource worlds?

```json
{
  "resourceWorlds": [
    {
      "worldId": "mining",
      "dimensionName": "timed_harvest:mining",
      "resetIntervalHours": 168,
      "worldType": "minecraft:overworld",
      "enabled": true
    },
    {
      "worldId": "nether_resources",
      "dimensionName": "timed_harvest:nether",
      "resetIntervalHours": 336,
      "worldType": "minecraft:the_nether",
      "enabled": true
    }
  ]
}
```

## 🚨 Troubleshooting

### "World not found in configuration!"
- Make sure `worldId` matches exactly
- Check for typos in config file
- Run `/timedharvest reload` after editing

### World not resetting automatically
- Verify `"enabled": true` for the world
- Check `"enableAutoReset": true` is set
- Look at server logs for errors

### Players aren't being kicked
- Set `"kickPlayersOnReset": true`
- Ensure players have permission to be teleported

## 📖 Next Steps

- Read [README.md](README.md) for full documentation
- Read [DEVELOPMENT.md](DEVELOPMENT.md) for customization
- Join community for support

---

**That's it! Your resource world is now on autopilot.** 🚀
