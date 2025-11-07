package com.timedharvest.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timedharvest.TimedHarvestMod;
import com.timedharvest.config.ModConfig;
import com.timedharvest.gui.WorldSelectionGui;
import com.timedharvest.scheduler.ResetScheduler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

/**
 * Registers and handles all commands for the Timed Harvest mod.
 */
public class TimedHarvestCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
            registerPlayerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("timedharvest")
            .requires(source -> source.hasPermissionLevel(2)) // Requires OP level 2
            
            // /timedharvest reset <worldId>
            .then(CommandManager.literal("reset")
                .then(CommandManager.argument("worldId", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            if (worldConfig.enabled) {
                                builder.suggest(worldConfig.worldId);
                            }
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::resetWorld)))
            
            // /timedharvest status [worldId]
            .then(CommandManager.literal("status")
                .executes(TimedHarvestCommands::statusAll)
                .then(CommandManager.argument("worldId", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            builder.suggest(worldConfig.worldId);
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::statusSingle)))
            
            // /timedharvest reload
            .then(CommandManager.literal("reload")
                .executes(TimedHarvestCommands::reloadConfig))
            
            // /timedharvest tp <worldId> - Teleport to resource world
            .then(CommandManager.literal("tp")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        // Add all enabled world IDs as suggestions
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            if (worldConfig.enabled) {
                                builder.suggest(worldConfig.worldId);
                            }
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::teleportToWorld)))
            
            // /timedharvest spawn - Teleport to overworld spawn
            .then(CommandManager.literal("spawn")
                .executes(TimedHarvestCommands::teleportToSpawn))
            
            // /timedharvest create <worldId> <dimensionName> <resetHours>
            .then(CommandManager.literal("create")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .then(CommandManager.argument("dimensionName", StringArgumentType.string())
                        .then(CommandManager.argument("resetHours", StringArgumentType.word())
                            .executes(TimedHarvestCommands::createWorld)))))
            
            // /timedharvest enable <worldId>
            .then(CommandManager.literal("enable")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            builder.suggest(worldConfig.worldId);
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::enableWorld)))
            
            // /timedharvest disable <worldId>
            .then(CommandManager.literal("disable")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            builder.suggest(worldConfig.worldId);
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::disableWorld)))
            
            // /timedharvest delete <worldId>
            .then(CommandManager.literal("delete")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        ModConfig config = TimedHarvestMod.getConfig();
                        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                            builder.suggest(worldConfig.worldId);
                        }
                        return builder.buildFuture();
                    })
                    .executes(TimedHarvestCommands::deleteWorld)))
            
            // /timedharvest help
            .then(CommandManager.literal("help")
                .executes(TimedHarvestCommands::showHelp))
        );
    }

    /**
     * Manually resets a resource world.
     */
    private static int resetWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        if (!worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' is disabled!"));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§eResetting resource world: " + worldId + "..."), true);
        
        TimedHarvestMod.getScheduler().manualReset(worldId, worldConfig);
        
        context.getSource().sendFeedback(() -> Text.literal("§aResource world '" + worldId + "' has been reset!"), true);
        return 1;
    }

    /**
     * Shows status for all configured worlds.
     */
    private static int statusAll(CommandContext<ServerCommandSource> context) {
        ModConfig config = TimedHarvestMod.getConfig();
        
        context.getSource().sendFeedback(() -> Text.literal("§6=== Timed Harvest Status ==="), false);
        context.getSource().sendFeedback(() -> Text.literal("§eAuto-reset enabled: §f" + config.enableAutoReset), false);
        context.getSource().sendFeedback(() -> Text.literal("§eConfigured worlds: §f" + config.resourceWorlds.size()), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);

        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
            displayWorldStatus(context, worldConfig);
        }

        return 1;
    }

    /**
     * Shows status for a specific world.
     */
    private static int statusSingle(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§6=== Status for '" + worldId + "' ==="), false);
        displayWorldStatus(context, worldConfig);

        return 1;
    }

    /**
     * Displays status information for a world.
     */
    private static void displayWorldStatus(CommandContext<ServerCommandSource> context, ModConfig.ResourceWorldConfig worldConfig) {
        context.getSource().sendFeedback(() -> Text.literal("§eWorld ID: §f" + worldConfig.worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("§eEnabled: §f" + worldConfig.enabled), false);
        context.getSource().sendFeedback(() -> Text.literal("§eDimension: §f" + worldConfig.dimensionName), false);
        context.getSource().sendFeedback(() -> Text.literal("§eReset Interval: §f" + worldConfig.resetIntervalHours + " hours"), false);

        if (worldConfig.enabled && TimedHarvestMod.getConfig().enableAutoReset) {
            long timeRemaining = TimedHarvestMod.getScheduler().getTimeUntilReset(worldConfig.worldId, worldConfig);
            String timeStr = ResetScheduler.formatTime(timeRemaining);
            context.getSource().sendFeedback(() -> Text.literal("§eNext Reset: §f" + timeStr), false);
        }

        context.getSource().sendFeedback(() -> Text.literal(""), false);
    }

    /**
     * Reloads the configuration file.
     */
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("§eReloading configuration..."), false);
        
        TimedHarvestMod.reloadConfig();
        
        context.getSource().sendFeedback(() -> Text.literal("§aConfiguration reloaded successfully!"), true);
        return 1;
    }

    /**
     * Shows help information.
     */
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        boolean hasPermission = context.getSource().hasPermissionLevel(2);
        
        context.getSource().sendFeedback(() -> Text.literal("§6=== Timed Harvest Commands ==="), false);
        
        if (hasPermission) {
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest reset <worldId> §f- Manually reset a resource world"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest status [worldId] §f- Show reset status"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest reload §f- Reload configuration"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest tp <worldId> §f- Teleport to resource world"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest spawn §f- Teleport to overworld spawn"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest create <worldId> <dimensionName> <hours> §f- Create new world"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest enable <worldId> §f- Enable a world"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest disable <worldId> §f- Disable a world"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest delete <worldId> §f- Delete a world from config"), false);
            context.getSource().sendFeedback(() -> Text.literal("§e/timedharvest help §f- Show this help message"), false);
        }
        
        // Always show the player command
        context.getSource().sendFeedback(() -> Text.literal("§e/th §f- Open world teleporter GUI"), false);
        
        return 1;
    }

    /**
     * Teleports the player to a resource world.
     */
    private static int teleportToWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("§cThis command can only be used by players!"));
            return 0;
        }

        // Get the dimension
        RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, 
            new Identifier(worldConfig.dimensionName));
        
        ServerWorld targetWorld = context.getSource().getServer().getWorld(dimensionKey);
        if (targetWorld == null) {
            context.getSource().sendError(Text.literal("§cDimension '" + worldConfig.dimensionName + "' does not exist! Use /timedharvest reset " + worldId + " to create it."));
            return 0;
        }

        // Apply world border if configured
        if (worldConfig.worldBorderSize > 0) {
            net.minecraft.world.border.WorldBorder border = targetWorld.getWorldBorder();
            border.setCenter(0.0, 0.0);
            border.setSize(worldConfig.worldBorderSize);
        }

        // Teleport to spawn point of the target world
        BlockPos spawnPos = targetWorld.getSpawnPos();
        player.teleport(targetWorld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        
        context.getSource().sendFeedback(() -> Text.literal("§aTeleported to " + worldId + "!"), false);
        return 1;
    }

    /**
     * Teleports the player to the overworld spawn.
     */
    private static int teleportToSpawn(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("§cThis command can only be used by players!"));
            return 0;
        }

        ServerWorld overworld = context.getSource().getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        
        context.getSource().sendFeedback(() -> Text.literal("§aTeleported to spawn!"), false);
        return 1;
    }

    /**
     * Creates a new resource world in the configuration.
     */
    private static int createWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        String dimensionName = StringArgumentType.getString(context, "dimensionName");
        String hoursStr = StringArgumentType.getString(context, "resetHours");

        // Check if world already exists
        if (findWorldConfig(worldId) != null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' already exists!"));
            return 0;
        }

        // Parse reset hours
        double resetHours;
        try {
            resetHours = Double.parseDouble(hoursStr);
            if (resetHours <= 0) {
                context.getSource().sendError(Text.literal("§cReset hours must be positive!"));
                return 0;
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid number format for reset hours!"));
            return 0;
        }

        // Create new world config
        ModConfig.ResourceWorldConfig newWorld = new ModConfig.ResourceWorldConfig();
        newWorld.worldId = worldId;
        newWorld.dimensionName = dimensionName;
        newWorld.resetIntervalHours = resetHours;
        newWorld.enabled = true;

        // Add to config
        TimedHarvestMod.getConfig().resourceWorlds.add(newWorld);
        
        // Save config
        TimedHarvestMod.getConfig().save();
        
        // Generate datapack for the dimension
        try {
            String worldName = context.getSource().getServer().getSaveProperties().getLevelName();
            java.io.File worldSaveDir = context.getSource().getServer().getRunDirectory().toPath()
                .resolve("saves")
                .resolve(worldName)
                .toFile();
            
            com.timedharvest.world.DatapackGenerator.createDimensionDatapack(worldSaveDir, newWorld);
            context.getSource().sendFeedback(() -> Text.literal("§aGenerated datapack in saves/" + worldName + "/datapacks/"), false);
            context.getSource().sendFeedback(() -> Text.literal("§6§lIMPORTANT: §cYou must restart the game/server!"), false);
            context.getSource().sendFeedback(() -> Text.literal("§eDimensions can only load on startup, not with /reload"), false);
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§cFailed to generate datapack: " + e.getMessage()));
        }
        
        // Reload to apply changes
        TimedHarvestMod.reloadConfig();

        context.getSource().sendFeedback(() -> Text.literal("§aCreated new resource world '" + worldId + "'!"), true);
        context.getSource().sendFeedback(() -> Text.literal("§eDimension: §f" + dimensionName), false);
        context.getSource().sendFeedback(() -> Text.literal("§eReset Interval: §f" + resetHours + " hours"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        context.getSource().sendFeedback(() -> Text.literal("§eNext steps:"), false);
        context.getSource().sendFeedback(() -> Text.literal("§e1. Close and restart Minecraft"), false);
        context.getSource().sendFeedback(() -> Text.literal("§e2. Run §f/timedharvest reset " + worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("§e3. Run §f/timedharvest tp " + worldId), false);
        
        return 1;
    }

    /**
     * Enables a resource world.
     */
    private static int enableWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        if (worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' is already enabled!"));
            return 0;
        }

        worldConfig.enabled = true;
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        context.getSource().sendFeedback(() -> Text.literal("§aEnabled world '" + worldId + "'!"), true);
        return 1;
    }

    /**
     * Disables a resource world.
     */
    private static int disableWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        if (!worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' is already disabled!"));
            return 0;
        }

        worldConfig.enabled = false;
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        context.getSource().sendFeedback(() -> Text.literal("§aDisabled world '" + worldId + "'!"), true);
        return 1;
    }

    /**
     * Deletes a resource world from the configuration and removes its files.
     */
    private static int deleteWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' not found in configuration!"));
            return 0;
        }

        // Kick all players from the world first
        RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, 
            new Identifier(worldConfig.dimensionName));
        ServerWorld targetWorld = context.getSource().getServer().getWorld(dimensionKey);
        
        if (targetWorld != null) {
            for (ServerPlayerEntity player : targetWorld.getPlayers()) {
                ServerWorld overworld = context.getSource().getServer().getOverworld();
                BlockPos spawnPos = overworld.getSpawnPos();
                player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
                player.sendMessage(Text.literal("§6[Timed Harvest] §eYou were teleported to spawn because world '" + worldId + "' is being deleted."));
            }
        }

        // Remove from configuration
        TimedHarvestMod.getConfig().resourceWorlds.remove(worldConfig);
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        // Delete world files (will happen on next server restart or manual reset)
        context.getSource().sendFeedback(() -> Text.literal("§aWorld '" + worldId + "' has been deleted from configuration!"), true);
        context.getSource().sendFeedback(() -> Text.literal("§eWorld files will be removed on next server restart."), true);
        
        return 1;
    }

    /**
     * Helper method to find a world config by ID.
     */
    private static ModConfig.ResourceWorldConfig findWorldConfig(String worldId) {
        for (ModConfig.ResourceWorldConfig config : TimedHarvestMod.getConfig().resourceWorlds) {
            if (config.worldId.equals(worldId)) {
                return config;
            }
        }
        return null;
    }

    /**
     * Registers player commands that don't require OP permissions.
     */
    private static void registerPlayerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("th")
            .executes(TimedHarvestCommands::openWorldGui));
    }

    /**
     * Opens the world selection GUI for the player.
     */
    private static int openWorldGui(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayer();
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§cThis command can only be used by players!"));
            return 0;
        }
        
        if (player == null) {
            context.getSource().sendError(Text.literal("§cThis command can only be used by players!"));
            return 0;
        }

        try {
            // Open the GUI
            SimpleInventory inventory = new SimpleInventory(27);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new WorldSelectionGui(syncId, playerInventory, inventory, player),
                Text.literal("§6Resource Worlds")
            ));
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.error("Error opening world GUI", e);
            context.getSource().sendError(Text.literal("§cError opening GUI: " + e.getMessage()));
            return 0;
        }

        return 1;
    }
}

