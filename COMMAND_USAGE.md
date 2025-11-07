# Command Usage Guide

## Important: Argument Formatting

When using commands with multiple arguments that contain special characters (like colons), you need to use **quotes** around arguments.

## Create World Command

### Correct Usage:
```
/timedharvest create mining_world "timed_harvest:mining" 168
```

### Why quotes are needed:
- The `dimensionName` parameter contains a colon (`:`) 
- Minecraft's command parser treats colons as special characters
- Wrapping it in quotes makes it a single argument

## All Commands with Correct Formatting

### World Management
```
/timedharvest create <worldId> "<namespace:dimension>" <hours>
/timedharvest create mining_world "timed_harvest:mining" 168
/timedharvest create farm_world "timed_harvest:farming" 24
/timedharvest create end_world "timed_harvest:end" 336
```

### Teleportation
```
/timedharvest tp <worldId>
/timedharvest tp mining_world
/timedharvest spawn
```

### Status and Control
```
/timedharvest status
/timedharvest status mining_world
/timedharvest reset mining_world
/timedharvest enable mining_world
/timedharvest disable mining_world
/timedharvest reload
```

## Quick Start Example

1. Create a new mining world:
   ```
   /timedharvest create mining_world "timed_harvest:mining" 168
   ```

2. Generate the world:
   ```
   /timedharvest reset mining_world
   ```

3. Teleport to it:
   ```
   /timedharvest tp mining_world
   ```

4. Check status:
   ```
   /timedharvest status mining_world
   ```

5. Return to spawn:
   ```
   /timedharvest spawn
   ```

## Command Completion

Use the **TAB** key to auto-complete commands and see available options!
