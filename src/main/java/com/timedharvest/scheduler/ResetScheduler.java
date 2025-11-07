package com.timedharvest.scheduler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.timedharvest.TimedHarvestMod;
import com.timedharvest.config.ModConfig;
import com.timedharvest.config.ModConfig.ResourceWorldConfig;
import com.timedharvest.world.ResourceWorldManager;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles scheduled automatic resets for resource worlds.
 * Uses server tick system to track time and trigger resets.
 */
public class ResetScheduler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File STATE_FILE = new File("config/timed-harvest-state.json");
    private static final int TICKS_PER_SECOND = 20;

    private ResourceWorldManager worldManager;
    private ModConfig config;
    
    // Tracks when each world was last reset (in milliseconds)
    private Map<String, Long> lastResetTimes = new HashMap<>();
    
    // Tracks if warning has been sent for this cycle
    private Map<String, Boolean> warningSent = new HashMap<>();
    
    private int tickCounter = 0;

    /**
     * Initializes the scheduler with server and configuration.
     */
    public void initialize(MinecraftServer server, ResourceWorldManager worldManager, ModConfig config) {
        this.worldManager = worldManager;
        this.config = config;
        
        loadState();
        
        // Initialize tracking for configured worlds
        for (ResourceWorldConfig worldConfig : config.resourceWorlds) {
            if (worldConfig.enabled) {
                lastResetTimes.putIfAbsent(worldConfig.worldId, System.currentTimeMillis());
                warningSent.putIfAbsent(worldConfig.worldId, false);
            }
        }
        
        TimedHarvestMod.LOGGER.info("ResetScheduler initialized with {} worlds", lastResetTimes.size());
    }

    /**
     * Called every server tick to check if any resets are due.
     */
    public void tick(MinecraftServer server) {
        if (!config.enableAutoReset) return;

        // Only check once per second to reduce overhead
        tickCounter++;
        if (tickCounter < TICKS_PER_SECOND) return;
        tickCounter = 0;

        long currentTime = System.currentTimeMillis();

        for (ResourceWorldConfig worldConfig : config.resourceWorlds) {
            if (!worldConfig.enabled) continue;

            String worldId = worldConfig.worldId;
            long lastReset = lastResetTimes.getOrDefault(worldId, currentTime);
            long timeSinceReset = currentTime - lastReset;
            long resetInterval = (long)(worldConfig.resetIntervalHours * 60 * 60 * 1000); // Convert hours to milliseconds

            // Check if warning should be sent
            if (config.notifyPlayersOnReset && config.warningMinutesBeforeReset > 0) {
                long warningTime = resetInterval - (config.warningMinutesBeforeReset * 60 * 1000);
                if (timeSinceReset >= warningTime && !warningSent.getOrDefault(worldId, false)) {
                    worldManager.warnPlayersAboutReset(worldId, config.warningMinutesBeforeReset);
                    warningSent.put(worldId, true);
                }
            }

            // Check if reset is due
            if (timeSinceReset >= resetInterval) {
                TimedHarvestMod.LOGGER.info("Scheduled reset triggered for world: {}", worldId);
                
                if (worldManager.resetWorld(worldId, worldConfig)) {
                    lastResetTimes.put(worldId, currentTime);
                    warningSent.put(worldId, false);
                    saveState();
                }
            }
        }
    }

    /**
     * Manually resets a world and updates the schedule.
     */
    public void manualReset(String worldId, ResourceWorldConfig config) {
        if (worldManager.resetWorld(worldId, config)) {
            lastResetTimes.put(worldId, System.currentTimeMillis());
            warningSent.put(worldId, false);
            saveState();
        }
    }

    /**
     * Gets the time remaining until the next reset for a world.
     */
    public long getTimeUntilReset(String worldId, ResourceWorldConfig config) {
        long lastReset = lastResetTimes.getOrDefault(worldId, System.currentTimeMillis());
        long timeSinceReset = System.currentTimeMillis() - lastReset;
        long resetInterval = (long)(config.resetIntervalHours * 60 * 60 * 1000);
        return Math.max(0, resetInterval - timeSinceReset);
    }

    /**
     * Formats milliseconds into a human-readable time string.
     */
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Saves the scheduler state to disk.
     */
    public void saveState() {
        try {
            STATE_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(STATE_FILE)) {
                SchedulerState state = new SchedulerState();
                state.lastResetTimes = this.lastResetTimes;
                GSON.toJson(state, writer);
            }
        } catch (IOException e) {
            TimedHarvestMod.LOGGER.error("Failed to save scheduler state", e);
        }
    }

    /**
     * Loads the scheduler state from disk.
     */
    private void loadState() {
        if (!STATE_FILE.exists()) return;

        try (FileReader reader = new FileReader(STATE_FILE)) {
            SchedulerState state = GSON.fromJson(reader, SchedulerState.class);
            if (state != null && state.lastResetTimes != null) {
                this.lastResetTimes = state.lastResetTimes;
                TimedHarvestMod.LOGGER.info("Loaded scheduler state for {} worlds", lastResetTimes.size());
            }
        } catch (IOException e) {
            TimedHarvestMod.LOGGER.error("Failed to load scheduler state", e);
        }
    }

    /**
     * State object for JSON serialization.
     */
    private static class SchedulerState {
        Map<String, Long> lastResetTimes;
    }
}
