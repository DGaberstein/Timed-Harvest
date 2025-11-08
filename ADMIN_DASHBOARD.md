# Admin Dashboard Feature

## Overview

The Admin Dashboard is a **graphical user interface** (GUI) for managing resource worlds in the Timed Harvest mod. It provides an intuitive, visual way to manage worlds without typing commands.

## Accessing the Admin Dashboard

There are **two ways** to open the Admin Dashboard:

### Method 1: From World Selector
1. Open the world selector with `/th`
2. Click the **§6⚙ Admin Dashboard** button (Nether Star) in the bottom-right corner
3. Requires admin/operator permission (level 2)

### Method 2: Direct Command
```
/th admin
```
- Opens the Admin Dashboard directly
- Requires admin/operator permission (level 2)

## Dashboard Features

### 📊 Main View (6 rows × 9 slots)

#### **Row 1: Title Bar**
- Yellow glass pane border
- Shows "§6§l▬▬ Admin Dashboard ▬▬"

#### **Rows 2-4: World Management**
- Displays up to 7 worlds per page
- Each world shows:
  - **Status indicator**: §a● (enabled) or §c● (disabled)
  - **World name** in gold
  - **Details**:
    - Dimension name
    - World type (overworld/nether/end)
    - Reset interval (hours)
    - Seed (if custom)
    - World border size (if set)
    - Structures (ON/OFF)
    - Enabled status
    - Next reset time (if auto-reset enabled)

##### World Icons:
- 🟫 **Grass Block** = Overworld-type world
- 🟥 **Netherrack** = Nether-type world
- ⬜ **End Stone** = End-type world

##### World Interactions:
- **§e§lLEFT CLICK**: View management commands for that world
- **§c§lRIGHT CLICK**: Toggle enabled/disabled status

#### **Row 5: Bottom Action Bar**

| Slot | Item | Function | Description |
|------|------|----------|-------------|
| 45 | 📖 Writable Book | **Reload Config** | Reloads the configuration file |
| 46 | 💎 Emerald | **Create World** | Shows create command syntax |
| 47 | 📘 Book | **Help & Commands** | Lists all available commands |
| 48 | ➡️ Arrow | **Previous Page** | Navigate to previous page |
| 49 | 📄 Paper | **Page Indicator** | Shows current page / total pages |
| 50 | ➡️ Arrow | **Next Page** | Navigate to next page |
| 53 | 🚫 Barrier | **Close Dashboard** | Closes the dashboard |

## World Management Actions

When you **LEFT CLICK** on a world, the dashboard shows available commands:

```
§6§l▬▬▬▬▬ §e§lWorld: <worldId> §6§l▬▬▬▬▬

§e§lAvailable Commands:
  §a● §6/timedharvest reset §e<worldId>
    §7→ Manually reset this world

  §a● §6/timedharvest tp §e<worldId>
    §7→ Teleport to this world

  §a● §6/timedharvest status §e<worldId>
    §7→ View detailed status

  §a● §6/timedharvest enable/disable §e<worldId>
    §7→ Enable/Disable this world

  §c● §6/timedharvest delete §e<worldId>
    §7→ §cRemove from configuration
```

When you **RIGHT CLICK** on a world:
- Instantly toggles the world between **ENABLED** and **DISABLED**
- Updates the config automatically
- Shows confirmation message
- GUI updates immediately to reflect the change

## Quick Actions

### Reload Configuration
Click the **📖 Writable Book** to:
- Reload `timed-harvest.json`
- Apply any manual config changes
- See success message

### Create World
Click the **💎 Emerald** to see:
- Full command syntax
- Examples for nether, end, and overworld worlds
- Parameter explanations

### View Help
Click the **📘 Book** to see:
- All available commands
- Organized by category
- Command descriptions

## Navigation

### Pagination
- If you have **more than 7 worlds**, the dashboard automatically paginates
- Use **◀ Previous Page** and **Next Page ▶** buttons
- Current page indicator shows: `Page X / Y` with world count

## Permission Requirements

- **Required permission level**: 2 (Operator/Admin)
- Non-admins will see:
  ```
  §c§l✖ §cYou don't have permission to access the admin dashboard!
  ```

## Integration with World Selector

The Admin Dashboard is seamlessly integrated with the player-facing world selector:

1. **Players** (no admin) see:
   - `/th` → World Selector (teleport GUI)
   - Admin Dashboard button is visible but shows permission error on click

2. **Admins** (level 2+) see:
   - `/th` → World Selector with Admin Dashboard button
   - `/th admin` → Direct access to Admin Dashboard
   - Click Nether Star → Opens Admin Dashboard
   - Full access to all features

## Visual Design

### Color Scheme
- **§6 Gold**: Titles, headers, important labels
- **§e Yellow**: World IDs, highlighted text
- **§a Green**: Enabled status, success messages
- **§c Red**: Disabled status, warnings, delete actions
- **§7 Gray**: Descriptions, helper text
- **§f White**: Data values
- **§l Bold**: Important emphasis

### Layout
- **Centered design** with worlds spaced out for clarity
- **Color-coded status** (green = enabled, red = disabled)
- **Icon-based** identification (grass/netherrack/end stone)
- **Organized sections** with clear visual separation

## Use Cases

### Daily Administration
1. **Quick status check**: Open dashboard to see all worlds at a glance
2. **Toggle worlds**: Right-click to enable/disable for maintenance
3. **Check reset times**: See when each world will reset next

### World Management
1. **View details**: Left-click a world to see management commands
2. **Reset manually**: Copy/use the reset command from the popup
3. **Teleport**: Copy/use the tp command to visit the world

### Configuration
1. **Reload changes**: After editing config file, click Reload button
2. **Monitor status**: See which worlds are active/inactive
3. **Plan changes**: View all settings before making modifications

## Tips & Best Practices

1. **Use RIGHT CLICK** for quick enable/disable toggles
2. **Use LEFT CLICK** when you need to execute specific commands
3. **Check the page indicator** to see if you have more worlds than visible
4. **Reload config** after manual JSON edits
5. **Close dashboard** returns you to the game (or world selector if accessed from there)

## Example Workflow

### Scenario: Resetting a Nether World

1. Open dashboard: `/th admin`
2. Find your nether world (red netherrack icon)
3. Check next reset time in the world's lore
4. Left-click the world
5. Dashboard closes and shows commands
6. Run: `/timedharvest reset nether`
7. World resets!

### Scenario: Temporarily Disabling a World

1. Open dashboard: `/th admin`
2. Find the world you want to disable
3. **Right-click** the world item
4. World status changes from §a● to §c●
5. Config saves automatically
6. Players can no longer access this world

## Technical Details

- **GUI Type**: 6-row chest GUI (54 slots)
- **Inventory Type**: `GENERIC_9X6`
- **Screen Handler**: `AdminDashboardGui`
- **Permission Check**: On open and in `canUse()` method
- **Auto-save**: Config saves automatically on world toggle
- **Refresh**: GUI updates in real-time when toggling worlds

## Future Enhancements

Potential features for future updates:
- In-GUI world creation (form-based)
- Edit world settings directly in GUI
- Bulk enable/disable operations
- World sorting options
- Search/filter functionality
- Quick teleport from dashboard
- Direct reset button (with confirmation)

---

## Quick Reference

| Action | Command | Permission |
|--------|---------|------------|
| Open Dashboard | `/th admin` | Level 2 |
| Toggle World | Right-click world item | Level 2 |
| View Commands | Left-click world item | Level 2 |
| Reload Config | Click book button | Level 2 |
| Navigate Pages | Click arrow buttons | Level 2 |
| Close Dashboard | Click barrier button | Level 2 |

**Enjoy streamlined world management! 🎮⚙️**
