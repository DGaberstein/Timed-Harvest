# Development Guide - Timed Harvest

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher (JDK)
- **Gradle** (wrapper included)
- **Minecraft 1.20.1** with Fabric Loader
- **Fabric API** mod

### Setup Development Environment

1. **Clone/Open the project**
   ```bash
   cd "c:\Users\Thicc_White\Desktop\Timed Harvest"
   ```

2. **Generate IDE files** (optional)
   ```bash
   # For IntelliJ IDEA
   .\gradlew idea
   
   # For Eclipse
   .\gradlew eclipse
   ```

3. **Run the client for testing**
   ```bash
   .\gradlew runClient
   ```

4. **Run the server for testing**
   ```bash
   .\gradlew runServer
   ```

## 🏗️ Project Architecture

### Core Components

#### 1. **TimedHarvestMod.java** (Entry Point)
- Initializes all managers and systems
- Registers event listeners
- Provides static access to core components

#### 2. **ResourceWorldManager.java** (World Management)
- Creates and registers custom dimensions
- Handles world deletion and file cleanup
- Manages player teleportation
- Provides world existence checks

**Key Methods:**
- `resetWorld()` - Complete reset workflow
- `kickPlayersFromWorld()` - Safe player evacuation
- `deleteWorldFiles()` - File system cleanup

#### 3. **ResetScheduler.java** (Scheduling System)
- Tick-based timing system (runs every server tick)
- Persistent state management
- Warning system before resets
- Manual reset support

**Key Methods:**
- `tick()` - Called every server tick
- `manualReset()` - Trigger immediate reset
- `getTimeUntilReset()` - Query remaining time
- `saveState()` / `loadState()` - Persistence

#### 4. **ModConfig.java** (Configuration)
- JSON-based configuration
- Hot-reload support
- Type-safe config objects
- Default value generation

#### 5. **TimedHarvestCommands.java** (Commands)
- Brigadier command registration
- Permission checking (OP level 2)
- User-friendly feedback messages

## 🔧 Building the Mod

### Development Build
```bash
.\gradlew build
```

Output: `build/libs/timed-harvest-1.0.0.jar`

### Testing Build
```bash
# Clean previous builds
.\gradlew clean

# Build and run client
.\gradlew runClient
```

## 📝 Adding Features

### Adding a New Command

1. Open `TimedHarvestCommands.java`
2. Add command registration in `registerCommands()`:
   ```java
   .then(CommandManager.literal("mycommand")
       .executes(MyClass::myMethod))
   ```
3. Implement the command handler method

### Adding Configuration Options

1. Open `ModConfig.java`
2. Add new field to `ModConfig` class or `ResourceWorldConfig`
3. Update `createDefault()` to set default value
4. Access via `TimedHarvestMod.getConfig().yourField`

### Adding a Mixin

1. Create new class in `com.timedharvest.mixin` package
2. Add `@Mixin` annotation
3. Register in `timed-harvest.mixins.json`:
   ```json
   "mixins": ["YourMixinClass"]
   ```

## 🧪 Testing

### Manual Testing Checklist

- [ ] Config file generates correctly on first run
- [ ] Commands execute with proper permissions
- [ ] World resets complete successfully
- [ ] Players are safely teleported before reset
- [ ] Warnings appear at correct times
- [ ] State persists across server restarts
- [ ] Multiple worlds can be managed independently
- [ ] Config reload works without restart

### Testing Commands

```bash
# Start server
.\gradlew runServer

# In Minecraft console:
/op Player
/timedharvest status
/timedharvest reset resource_world
/timedharvest reload
```

## 📦 Publishing

### Building for Release

1. Update version in `gradle.properties`:
   ```properties
   mod_version=1.0.1
   ```

2. Build the mod:
   ```bash
   .\gradlew clean build
   ```

3. Test the built JAR:
   - Copy from `build/libs/timed-harvest-1.0.1.jar`
   - Place in test server's `mods/` folder
   - Verify functionality

4. Create release on GitHub with JAR attached

## 🐛 Debugging

### Enable Debug Logging

Add to `src/main/resources/log4j2.xml`:
```xml
<Logger name="com.timedharvest" level="debug"/>
```

### Common Issues

**Issue: World doesn't reset**
- Check `config/timed-harvest.json` - is world enabled?
- Check logs for errors during reset
- Verify `enableAutoReset` is `true`

**Issue: Players not kicked**
- Check `kickPlayersOnReset` setting
- Verify world dimension name matches

**Issue: Config not loading**
- Check JSON syntax validity
- Check file permissions
- Review server logs for parse errors

## 🎨 Code Style

- Use **descriptive variable names**
- Add **JavaDoc** comments to public methods
- Follow **Java naming conventions**
- Keep methods **focused and single-purpose**
- Use **logger** instead of `System.out.println()`

## 📚 Useful Resources

- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Yarn Mappings](https://maven.fabricmc.net/net/fabricmc/yarn/)
- [Brigadier Commands](https://github.com/Mojang/brigadier)
- [Minecraft Wiki](https://minecraft.wiki/)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📞 Need Help?

- Check existing [GitHub Issues](https://github.com/yourusername/timed-harvest/issues)
- Review the [Fabric Wiki](https://fabricmc.net/wiki/)
- Ask in the [Fabric Discord](https://discord.gg/v6v4pMv)

---

Happy modding! 🎮
