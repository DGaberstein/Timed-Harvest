package com.timedharvest;

import com.timedharvest.command.TimedHarvestCommands;
import com.timedharvest.config.ModConfig;
import com.timedharvest.scheduler.ResetScheduler;
import com.timedharvest.world.ResourceWorldManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Timed Harvest mod.
 * This mod combines resource world management with scheduled automatic resets.
 */
public class TimedHarvestMod implements ModInitializer {
    public static final String MOD_ID = "timed-harvest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ResourceWorldManager worldManager;
    private static ResetScheduler scheduler;
    private static ModConfig config;

    @Override
    public void onInitialize() {
        // Register world load event to apply pending world borders
        ServerWorldEvents.LOAD.register((server, world) -> {
            worldManager.onWorldLoad(world);
        });
        LOGGER.info("Initializing Timed Harvest Mod");

        // Load configuration
        config = ModConfig.load();

        // Initialize managers
        worldManager = new ResourceWorldManager();
        scheduler = new ResetScheduler();

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Server started - initializing resource worlds");
            worldManager.initialize(server);
            scheduler.initialize(server, worldManager, config);
            
            // Reload datapacks to ensure dimensions are registered
            server.execute(() -> {
                try {
                    server.getCommandManager().executeWithPrefix(
                        server.getCommandSource(),
                        "reload"
                    );
                    LOGGER.info("Automatically reloaded datapacks to register dimensions");
                } catch (Exception e) {
                    LOGGER.warn("Failed to auto-reload datapacks: {}", e.getMessage());
                }
            });
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Server stopping - saving scheduler state");
            scheduler.saveState();
        });

        // Register tick event for scheduler
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            scheduler.tick(server);
        });

        // Register commands
        TimedHarvestCommands.register();

        LOGGER.info("Timed Harvest Mod initialized successfully!");
    }

    public static ResourceWorldManager getWorldManager() {
        return worldManager;
    }

    public static ResetScheduler getScheduler() {
        return scheduler;
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void reloadConfig() {
        config = ModConfig.load();
        LOGGER.info("Configuration reloaded");
    }

    // Utility to delete a world folder recursively
    public static boolean deleteWorldFolder(java.io.File dir) {
        if (dir == null || !dir.exists()) return false;
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete();
    }
}
