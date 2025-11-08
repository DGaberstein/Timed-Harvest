# Admin Dashboard - Visual Layout

```
╔═══════════════════════════════════════════════════════════════════════╗
║                     §6§l▬▬ ADMIN DASHBOARD ▬▬                          ║
╠═══════════════════════════════════════════════════════════════════════╣
║  ROW 1: Title Bar (Yellow Glass Panes)                                ║
║  ┌───┬───┬───┬───┬───┬───┬───┬───┬───┐                               ║
║  │ Y │ Y │ Y │ Y │ Y │ Y │ Y │ Y │ Y │  Y = Yellow Stained Glass    ║
║  └───┴───┴───┴───┴───┴───┴───┴───┴───┘                               ║
╠═══════════════════════════════════════════════════════════════════════╣
║  ROWS 2-4: World Management (up to 7 worlds)                          ║
║  ┌───┬───┬───┬───┬───┬───┬───┬───┬───┐                               ║
║  │   │ 🟫│   │ 🟥│   │ ⬜│   │   │   │  World Icons (spaced)         ║
║  │   │ W1│   │ W2│   │ W3│   │   │   │                               ║
║  ├───┼───┼───┼───┼───┼───┼───┼───┼───┤                               ║
║  │   │ 🟫│   │ 🟥│   │   │   │   │   │  More worlds if needed       ║
║  │   │ W4│   │ W5│   │   │   │   │   │                               ║
║  ├───┼───┼───┼───┼───┼───┼───┼───┼───┤                               ║
║  │   │ 🟫│   │ 🟥│   │   │   │   │   │  (Empty slots = no world)    ║
║  │   │ W6│   │ W7│   │   │   │   │   │                               ║
║  └───┴───┴───┴───┴───┴───┴───┴───┴───┘                               ║
║                                                                        ║
║  World Item Tooltip Example:                                          ║
║  ┌─────────────────────────────────┐                                  ║
║  │ §a● §e§lNether World            │  (Green = Enabled)               ║
║  │ §7▬▬▬▬▬▬▬▬▬▬▬▬▬▬                │                                  ║
║  │ §6Dimension: §ftimed_harvest:ne │                                  ║
║  │ §6World Type: §fminecraft:the_n │                                  ║
║  │ §6Reset Interval: §f24h         │                                  ║
║  │ §6Seed: §f123456789             │                                  ║
║  │ §6Border: §f5000 blocks         │                                  ║
║  │ §6Structures: §a§lON            │                                  ║
║  │ §6Status: §a§lENABLED           │                                  ║
║  │ §6Next Reset: §a23h 45m         │                                  ║
║  │                                  │                                  ║
║  │ §e§l▶ LEFT CLICK §7to manage    │                                  ║
║  │ §c§l▶ RIGHT CLICK §7to toggle   │                                  ║
║  └─────────────────────────────────┘                                  ║
╠═══════════════════════════════════════════════════════════════════════╣
║  ROW 5: Bottom Action Bar                                             ║
║  ┌───┬───┬───┬───┬───┬───┬───┬───┬───┐                               ║
║  │📖 │💎 │📘 │◀  │📄 │▶  │   │   │🚫 │                              ║
║  │ R │ C │ H │ P │ P │ N │   │   │ X │                              ║
║  └───┴───┴───┴───┴───┴───┴───┴───┴───┘                               ║
║                                                                        ║
║  📖 R = Reload Config        💎 C = Create World                      ║
║  📘 H = Help                  ◀ P = Previous Page                     ║
║  📄 P = Page Indicator        ▶ N = Next Page                        ║
║  🚫 X = Close                                                         ║
╠═══════════════════════════════════════════════════════════════════════╣
║  ROW 6: Player Inventory (3 rows)                                     ║
║  ┌───────────────────────────────────────────────────┐                ║
║  │  [Player's Inventory - 27 slots]                  │                ║
║  └───────────────────────────────────────────────────┘                ║
║  ┌───────────────────────────────────────────────────┐                ║
║  │  [Player's Hotbar - 9 slots]                      │                ║
║  └───────────────────────────────────────────────────┘                ║
╚═══════════════════════════════════════════════════════════════════════╝
```

## Color Legend

### Status Indicators
- **§a●** Green dot = World ENABLED
- **§c●** Red dot = World DISABLED

### World Type Icons
- **🟫 Grass Block** = Overworld world (minecraft:overworld)
- **🟥 Netherrack** = Nether world (minecraft:the_nether)
- **⬜ End Stone** = End world (minecraft:the_end)

### Text Colors
- **§6 Gold** = Section headers, labels, borders
- **§e Yellow** = World names, important text
- **§a Green** = Enabled status, success, positive values
- **§c Red** = Disabled status, warnings, delete
- **§7 Gray** = Descriptions, helper text
- **§f White** = Data values
- **§l Bold** = Emphasis on important items

## Button Tooltips

### Reload Config Button
```
┌─────────────────────────┐
│ §e§lReload Config        │
│ §7Click to reload the   │
│ §7configuration file    │
└─────────────────────────┘
```

### Create World Button
```
┌──────────────────────────────┐
│ §a§lCreate World              │
│ §7Use command:               │
│ §6/timedharvest create       │
│ §7<worldId> <dimension> <hr> │
└──────────────────────────────┘
```

### Help Button
```
┌─────────────────────────┐
│ §b§lHelp & Commands      │
│ §7Click to view all     │
│ §7available commands    │
└─────────────────────────┘
```

### Previous Page Button
```
┌─────────────────────┐
│ §e◀ Previous Page    │
│ §7Page 1 of 3       │
└─────────────────────┘
```

### Page Indicator
```
┌──────────────────────────┐
│ §6§lPage 2 / 3            │
│ §7Showing 7 of 18 worlds │
└──────────────────────────┘
```

### Next Page Button
```
┌─────────────────────┐
│ §eNext Page ▶        │
│ §7Page 3 of 3       │
└─────────────────────┘
```

### Close Button
```
┌───────────────────────────┐
│ §c§lClose Dashboard        │
│ §7Return to world selector│
└───────────────────────────┘
```

## Interaction Flow

### Left-Click World Item
```
[Dashboard] → [Closes] → [Chat Messages]

§6§l▬▬▬▬▬ §e§lWorld: nether §6§l▬▬▬▬▬

§e§lAvailable Commands:
  §a● §6/timedharvest reset §enether
    §7→ Manually reset this world

  §a● §6/timedharvest tp §enether
    §7→ Teleport to this world

  §a● §6/timedharvest status §enether
    §7→ View detailed status

  §a● §6/timedharvest disable §enether
    §7→ Disable this world

  §c● §6/timedharvest delete §enether
    §7→ §cRemove from configuration
§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
```

### Right-Click World Item
```
[Dashboard] → [Toggles Status] → [Updates GUI] → [Shows Message]

§6World '§e§lnether§6' is now §a§lENABLED

[World item updates from §c● to §a●]
[Config saves automatically]
```

## Access Methods

### Method 1: From World Selector
```
/th
  ↓
[World Selector GUI opens]
  ↓
[Click Nether Star in bottom-right]
  ↓
[Admin Dashboard opens]
```

### Method 2: Direct Command
```
/th admin
  ↓
[Admin Dashboard opens immediately]
```

## Example Scenarios

### Scenario: Check World Status
```
1. Run: /th admin
2. View dashboard
3. See all worlds with:
   - Green ● = enabled
   - Red ● = disabled
   - Next reset times
4. No command typing needed!
```

### Scenario: Disable Mining World
```
1. Open: /th admin
2. Find "Mining World" (🟫 grass block)
3. Right-click the item
4. Status changes: §a● → §c●
5. Message: "World 'mining' is now §c§lDISABLED"
6. Done! (auto-saved)
```

### Scenario: View World Commands
```
1. Open: /th admin
2. Find "Nether World" (🟥 netherrack)
3. Left-click the item
4. Dashboard closes
5. See list of commands in chat
6. Copy and run desired command
```

### Scenario: Navigate Many Worlds
```
1. Open: /th admin
2. See "Page 1 / 4" and 7 worlds
3. Click "Next Page ▶"
4. See "Page 2 / 4" and 7 more worlds
5. Continue browsing
6. Click "◀ Previous Page" to go back
```

## Desktop/Mobile Comparison

### Desktop Player
- Full view of 6 rows
- Clear button positioning
- Easy mouse navigation
- Smooth clicking

### Mobile/Bedrock (if compatible)
- Touch-friendly large buttons
- Spaced world items
- Clear visual feedback
- Pagination for many worlds

---

**Visual design optimized for clarity and efficiency! 🎨✨**
