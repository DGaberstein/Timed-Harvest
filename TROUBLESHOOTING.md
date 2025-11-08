# Troubleshooting Guide

## Common Issues and Solutions

### 🔴 "Dimension does not exist" Error

**Problem:** When clicking a world in the GUI, you see: `Dimension 'worldname' does not exist!`

**Cause:** The dimension hasn't been created yet, or the datapack wasn't loaded properly.

**Solutions:**

1. **For Admins (Recommended):**
   ```
   /timedharvest reset <worldId>
   ```
   This will create the dimension and generate the required datapack.

2. **Manual Fix:**
   - Open the config file: `config/timed-harvest.json`
   - Find the world with the issue
   - Ensure `dimensionName` has format: `namespace:name` (e.g., `"timed_harvest:mining"`)
   - Ensure `worldType` is set (e.g., `"minecraft:overworld"`)
   - Run `/timedharvest reload` in-game
   - Run `/timedharvest reset <worldId>` to create the dimension

---

### ⚠️ Red "NOT CREATED YET" World in GUI

**Problem:** A world shows as a red barrier block with "NOT CREATED YET" text.

**Cause:** The dimension configuration is incomplete or hasn't been initialized.

**Solution:**
- Click the world in the GUI to see detailed instructions
- Admins can run: `/timedharvest reset <worldId>`
- After creation, the world will show with a proper icon

---

### 🔧 Auto-Fix System

The mod automatically fixes common configuration errors:

✅ **Auto-fixed issues:**
- Missing namespace in dimension names (adds `timed_harvest:` prefix)
- Missing `worldType` field (defaults to `minecraft:overworld`)
- Missing namespace in `worldType` (adds `minecraft:` prefix)

**When auto-fix runs:**
- During mod startup (server start)
- After running `/timedharvest reload`

**Check the logs** for messages like:
```
[timed-harvest] Auto-fixed dimension name: 'test' -> 'timed_harvest:test'
```

---

### 📝 Configuration Format

**Correct world configuration:**
```json
{
  "worldId": "mining",
  "dimensionName": "timed_harvest:mining",
  "resetIntervalHours": 168.0,
  "worldType": "minecraft:overworld",
  "seed": 0,
  "generateStructures": true,
  "enabled": true,
  "worldBorderSize": 10000
}
```

**Required fields:**
- `dimensionName` - MUST include namespace (e.g., `timed_harvest:mining`)
- `worldType` - MUST include namespace (e.g., `minecraft:overworld`)

---

### 🌍 Supported World Types

Valid `worldType` values:
- `minecraft:overworld` - Standard overworld terrain
- `minecraft:the_nether` - Nether dimension
- `minecraft:the_end` - End dimension

---

### 🛠️ Helpful Commands

**For Players:**
- `/th` - Open world selection GUI

**For Admins:**
- `/timedharvest list` - Show all configured worlds
- `/timedharvest reset <worldId>` - Reset/create a world
- `/timedharvest reload` - Reload configuration from file
- `/timedharvest tp <worldId>` - Teleport to a world
- `/timedharvest delete <worldId>` - Delete a world permanently

---

### 🔍 Understanding Error Messages

**In GUI:**
```
✘ Dimension 'test' does not exist!

⚠ To fix this issue:
1. Run: /timedharvest reset test
2. This will create the dimension and its datapack

Tip: Use /reload after creating new worlds
```

**In Logs:**
```
[CONFIG WARNING] World 'test' dimension name 'test' should include namespace
```
→ The config will be auto-fixed. Run `/timedharvest reload` to apply.

---

### 📂 File Locations

- **Config:** `config/timed-harvest.json`
- **Datapacks:** `saves/<world_name>/datapacks/timed_harvest/`
- **Dimensions:** `saves/<world_name>/dimensions/timed_harvest/<dimension_name>/`

---

### 🚨 Server Startup Warnings

If you see warnings on server start:
```
Found X configuration issue(s)!
These have been auto-fixed in config.
Use '/timedharvest reload' to apply fixes.
```

**What to do:**
1. The issues have been automatically corrected in the config file
2. Run `/timedharvest reload` in-game to apply the fixes
3. Run `/timedharvest reset <worldId>` for each affected world
4. Check `config/timed-harvest.json` to verify the corrections

---

### 💡 Best Practices

1. **Always use namespaced names:**
   - ✅ `"dimensionName": "timed_harvest:mining"`
   - ❌ `"dimensionName": "mining"`

2. **Test worlds after creation:**
   - Create the world: `/timedharvest reset mining`
   - Check the GUI: `/th`
   - Try teleporting to it

3. **After config changes:**
   - Save the file
   - Run `/timedharvest reload`
   - Reset affected worlds if needed

4. **Before adding to production:**
   - Test in single-player or a test server
   - Verify all worlds appear correctly in `/th` GUI
   - Ensure teleportation works

---

### ❓ Still Having Issues?

1. **Check the logs** in `logs/latest.log` for error details
2. **Verify your config** matches the format above
3. **Try the auto-fix** by running `/timedharvest reload`
4. **Reset the world** with `/timedharvest reset <worldId>`
5. **Report bugs** with log files and config file attached

---

### 📖 Related Documentation

- [README.md](README.md) - Main documentation
- [COMMANDS.md](COMMANDS.md) - Full command reference
- [WORLD_CUSTOMIZATION.md](WORLD_CUSTOMIZATION.md) - World customization guide
