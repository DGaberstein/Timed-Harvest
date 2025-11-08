package com.timedharvest.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.timedharvest.TimedHarvestMod;
import com.timedharvest.config.ModConfig;
import com.timedharvest.gui.WorldSelectionGui;
import com.timedharvest.world.ResourceWorldManager;
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
import net.minecraft.command.argument.IdentifierArgumentType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registers and handles all commands for the Timed Harvest mod.
 */
public class TimedHarvestCommands {
    
    // Teleport cooldown tracking
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 seconds cooldown

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
            registerPlayerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        final int[] RESET_DAYS = new int[] {1, 2, 3, 4, 5, 6, 7, 14, 21, 28};
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
            // /timedharvest create <worldId> <dimensionName> <resetDays> [worldType] [seed] [borderSize] [structures]
            .then(CommandManager.literal("create")
                .then(CommandManager.argument("worldId", StringArgumentType.word())
                    .then(CommandManager.argument("dimensionName", IdentifierArgumentType.identifier())
                        .then(CommandManager.argument("resetDays", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                for (int d : RESET_DAYS) builder.suggest(Integer.toString(d));
                                return builder.buildFuture();
                            })
                            // Basic: /timedharvest create <worldId> <dimensionName> <resetDays>
                            .executes(TimedHarvestCommands::createWorld)
                            // With worldType: /timedharvest create <worldId> <dimensionName> <resetDays> <worldType>
                            .then(CommandManager.argument("worldType", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> {
                                    builder.suggest("minecraft:overworld");
                                    builder.suggest("minecraft:the_nether");
                                    builder.suggest("minecraft:the_end");
                                    return builder.buildFuture();
                                })
                                .executes(TimedHarvestCommands::createWorldWithType)
                                // With seed: /timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed>
                                .then(CommandManager.argument("seed", StringArgumentType.word())
                                    .executes(TimedHarvestCommands::createWorldWithSeed)
                                    // With border: /timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed> <borderSize>
                                    .then(CommandManager.argument("borderSize", StringArgumentType.word())
                                        .executes(TimedHarvestCommands::createWorldWithBorder)
                                        // Full: /timedharvest create <worldId> <dimensionName> <resetHours> <worldType> <seed> <borderSize> <structures>
                                        .then(CommandManager.argument("structures", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                builder.suggest("true");
                                                builder.suggest("false");
                                                return builder.buildFuture();
                                            })
                                            .executes(TimedHarvestCommands::createWorldFull)))))))))
            
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
                .executes(TimedHarvestCommands::showHelp)
                .then(CommandManager.literal("troubleshooting")
                    .executes(TimedHarvestCommands::showTroubleshooting)))
        );
    }

    /**
     * Manually resets a resource world.
     */
    private static int resetWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
            return 0;
        }

        if (!worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' is disabled!"));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§e⟳ §6Resetting resource world: §e§l" + worldId + "§6..."), true);
        
        TimedHarvestMod.getScheduler().manualReset(worldId, worldConfig);
        
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aResource world '§e§l" + worldId + "§a' has been reset!"), true);
        return 1;
    }

    /**
     * Shows status for all configured worlds.
     */
    private static int statusAll(CommandContext<ServerCommandSource> context) {
        ModConfig config = TimedHarvestMod.getConfig();
        
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬ §e§lTimed Harvest Status §6§l▬▬▬▬"), false);
        context.getSource().sendFeedback(() -> Text.literal("§eAuto-reset: " + (config.enableAutoReset ? "§a§lENABLED" : "§c§lDISABLED")), false);
        context.getSource().sendFeedback(() -> Text.literal("§eConfigured worlds: §f§l" + config.resourceWorlds.size()), false);
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
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
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬ §e§lStatus: §f" + worldId + " §6§l▬▬"), false);
        displayWorldStatus(context, worldConfig);

        return 1;
    }

    /**
     * Displays status information for a world.
     */
    private static void displayWorldStatus(CommandContext<ServerCommandSource> context, ModConfig.ResourceWorldConfig worldConfig) {
        String enabledStatus = worldConfig.enabled ? "§a§lENABLED" : "§c§lDISABLED";
        
        context.getSource().sendFeedback(() -> Text.literal("§6● §eWorld ID: §f§l" + worldConfig.worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("§6● §eStatus: " + enabledStatus), false);
        context.getSource().sendFeedback(() -> Text.literal("§6● §eDimension: §f" + worldConfig.dimensionName), false);
        context.getSource().sendFeedback(() -> Text.literal("§6● §eWorld Type: §f" + worldConfig.worldType), false);
        context.getSource().sendFeedback(() -> Text.literal("§6● §eReset Interval: §f" + worldConfig.resetIntervalHours + " §7hours"), false);

        if (worldConfig.enabled && TimedHarvestMod.getConfig().enableAutoReset) {
            long timeRemaining = TimedHarvestMod.getScheduler().getTimeUntilReset(worldConfig.worldId, worldConfig);
            String timeStr = ResetScheduler.formatTime(timeRemaining);
            context.getSource().sendFeedback(() -> Text.literal("§6● §eNext Reset: §a§l" + timeStr), false);
        }

        context.getSource().sendFeedback(() -> Text.literal(""), false);
    }

    /**
     * Reloads the configuration file.
     */
    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("§e⟳ §6Reloading configuration..."), false);
        
        TimedHarvestMod.reloadConfig();
        
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aConfiguration reloaded successfully!"), true);
        return 1;
    }

    /**
     * Shows help information.
     */
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        boolean hasPermission = context.getSource().hasPermissionLevel(2);
        
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬ §e§lTimed Harvest Commands §6§l▬▬▬▬▬▬▬"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        if (hasPermission) {
            context.getSource().sendFeedback(() -> Text.literal("§6§l● §eAdmin Commands:"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest reset §7<worldId> §8- §fManually reset a world"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest status §7[worldId] §8- §fShow reset status"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest reload §8- §fReload configuration"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest tp §7<worldId> §8- §fTeleport to world"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest spawn §8- §fTeleport to overworld"), false);
            context.getSource().sendFeedback(() -> Text.literal(""), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest create §7<worldId> <dimension> <days>"), false);
            context.getSource().sendFeedback(() -> Text.literal("                       §7[type] [seed] [border] [structures]"), false);
            context.getSource().sendFeedback(() -> Text.literal("    §8→ §fCreate new resource world"), false);
        context.getSource().sendFeedback(() -> Text.literal("    §7Days: §f1, 2, 3, 4, 5, 6, 7, 14, 21, 28"), false);
            context.getSource().sendFeedback(() -> Text.literal("    §7Example: §f/th create §enether §atimed_harvest:nether §67"), false);
            context.getSource().sendFeedback(() -> Text.literal("              §f          §e....... §aminecraft:the_nether §67"), false);
            context.getSource().sendFeedback(() -> Text.literal(""), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest enable §7<worldId> §8- §fEnable a world"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest disable §7<worldId> §8- §fDisable a world"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest delete §7<worldId> §8- §fDelete from config"), false);
            context.getSource().sendFeedback(() -> Text.literal(""), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest help §8- §fShow this help"), false);
            context.getSource().sendFeedback(() -> Text.literal("  §6/timedharvest help troubleshooting §8- §fCommon fixes"), false);
            context.getSource().sendFeedback(() -> Text.literal(""), false);
        }
        
        // Always show the player command
        context.getSource().sendFeedback(() -> Text.literal("§a§l● §ePlayer Commands:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a/th §8- §fOpen world teleporter GUI"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
        
        return 1;
    }
    
    /**
     * Shows common troubleshooting tips and fixes.
     */
    private static int showTroubleshooting(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬ §c§lTimed Harvest Troubleshooting §6§l▬▬▬"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        context.getSource().sendFeedback(() -> Text.literal("§c§l⚠ §c\"Dimension does not exist\" Error:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §8→ §fRun: §6§l/timedharvest reset §7<worldId>"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §8→ §7This creates the dimension and datapack"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        context.getSource().sendFeedback(() -> Text.literal("§e§l💡 §eCorrect Dimension Naming:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §c✗ Wrong: §7minecraft:nether §8(doesn't exist!)"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a✓ Right: §f§lminecraft:the_nether §8(vanilla nether)"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a✓ Right: §f§ltimed_harvest:nether §8(custom nether)"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        context.getSource().sendFeedback(() -> Text.literal("§e§l⚙ §eAfter Config Changes:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a1. §fRun: §6§l/timedharvest reload"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a2. §fRun: §6§l/timedharvest reset §7<worldId> §ffor each changed world"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aAuto-Fix Features:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §8• §7Missing namespace in dimension names"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §8• §7Missing worldType defaults to overworld"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §8• §7Check logs for auto-fix messages"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        
        context.getSource().sendFeedback(() -> Text.literal("§b§l📖 §bFull Guide: §f/TROUBLESHOOTING.md"), false);
        context.getSource().sendFeedback(() -> Text.literal("§8Tip: Check server logs for detailed error messages"), false);
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
        
        return 1;
    }

    /**
     * Teleports the player to a resource world.
     */
    private static int teleportToWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
            return 0;
        }

        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }

        // Check teleport cooldown
        if (!checkTeleportCooldown(player)) {
            long remainingMs = getRemainingCooldown(player);
            double remainingSec = remainingMs / 1000.0;
            player.sendMessage(Text.literal(String.format("§c§l⏱ §cPlease wait %.1f seconds before teleporting again!", remainingSec)), true);
            return 0;
        }

        // Get the dimension
        RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, 
            new Identifier(worldConfig.dimensionName));
        
        ServerWorld targetWorld = context.getSource().getServer().getWorld(dimensionKey);
        if (targetWorld == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cDimension '§e" + worldConfig.dimensionName + "§c' does not exist!"));
            context.getSource().sendError(Text.literal("§eUse §6§l/timedharvest reset " + worldId + " §eto create it."));
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
        
        // Set cooldown after successful teleport
        setTeleportCooldown(player);
        
        long seed = targetWorld.getSeed();
        String displayName = ResourceWorldManager.getDisplayNameForWorld(targetWorld);
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aTeleported to §e" + displayName + "§a!\n§7Seed: §a[§e" + seed + "§a]"), false);
        return 1;
    }

    /**
     * Teleports the player to the overworld spawn.
     */
    private static int teleportToSpawn(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }

        // Check teleport cooldown
        if (!checkTeleportCooldown(player)) {
            long remainingMs = getRemainingCooldown(player);
            double remainingSec = remainingMs / 1000.0;
            player.sendMessage(Text.literal(String.format("§c§l⏱ §cPlease wait %.1f seconds before teleporting again!", remainingSec)), true);
            return 0;
        }

        ServerWorld overworld = context.getSource().getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        
        // Set cooldown after successful teleport
        setTeleportCooldown(player);
        
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aTeleported to §e§lspawn§a!"), false);
        return 1;
    }

    /**
     * Creates a new resource world in the configuration.
     */
    /**
     * Creates a new world with default settings.
     */
    private static int createWorld(CommandContext<ServerCommandSource> context) {
        return createWorldInternal(context, "minecraft:overworld", 0, 0, true);
    }
    
    /**
     * Creates a new world with specified world type.
     */
    private static int createWorldWithType(CommandContext<ServerCommandSource> context) {
        Identifier worldTypeId = IdentifierArgumentType.getIdentifier(context, "worldType");
        String worldType = worldTypeId.toString();
        return createWorldInternal(context, worldType, 0, 0, true);
    }
    
    /**
     * Creates a new world with specified world type and seed.
     */
    private static int createWorldWithSeed(CommandContext<ServerCommandSource> context) {
        Identifier worldTypeId = IdentifierArgumentType.getIdentifier(context, "worldType");
        String worldType = worldTypeId.toString();
        String seedStr = StringArgumentType.getString(context, "seed");
        
        long seed;
        try {
            seed = Long.parseLong(seedStr);
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or 0 for random."));
            return 0;
        }
        
        return createWorldInternal(context, worldType, seed, 0, true);
    }
    
    /**
     * Creates a new world with specified world type, seed, and border size.
     */
    private static int createWorldWithBorder(CommandContext<ServerCommandSource> context) {
        Identifier worldTypeId = IdentifierArgumentType.getIdentifier(context, "worldType");
        String worldType = worldTypeId.toString();
        String seedStr = StringArgumentType.getString(context, "seed");
        String borderStr = StringArgumentType.getString(context, "borderSize");
        
        long seed;
        try {
            seed = Long.parseLong(seedStr);
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or 0 for random."));
            return 0;
        }
        
        int borderSize;
        try {
            borderSize = Integer.parseInt(borderStr);
            if (borderSize < 0) {
                context.getSource().sendError(Text.literal("§cBorder size must be 0 or positive!"));
                return 0;
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid border size format!"));
            return 0;
        }
        
        return createWorldInternal(context, worldType, seed, borderSize, true);
    }
    
    /**
     * Creates a new world with all options specified.
     */
    private static int createWorldFull(CommandContext<ServerCommandSource> context) {
        Identifier worldTypeId = IdentifierArgumentType.getIdentifier(context, "worldType");
        String worldType = worldTypeId.toString();
        String seedStr = StringArgumentType.getString(context, "seed");
        String borderStr = StringArgumentType.getString(context, "borderSize");
        String structuresStr = StringArgumentType.getString(context, "structures");
        
        long seed;
        try {
            seed = Long.parseLong(seedStr);
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or 0 for random."));
            return 0;
        }
        
        int borderSize;
        try {
            borderSize = Integer.parseInt(borderStr);
            if (borderSize < 0) {
                context.getSource().sendError(Text.literal("§cBorder size must be 0 or positive!"));
                return 0;
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid border size format!"));
            return 0;
        }
        
        boolean generateStructures = Boolean.parseBoolean(structuresStr);
        
        return createWorldInternal(context, worldType, seed, borderSize, generateStructures);
    }
    
    /**
     * Internal method to create a world with all options.
     */
    private static int createWorldInternal(CommandContext<ServerCommandSource> context, 
                                          String worldType, long seed, int borderSize, boolean generateStructures) {
        String worldId = StringArgumentType.getString(context, "worldId");
        Identifier dimensionNameId = IdentifierArgumentType.getIdentifier(context, "dimensionName");
        String dimensionName = dimensionNameId.toString();
        String daysStr = StringArgumentType.getString(context, "resetDays");

        // Check if world already exists
        if (findWorldConfig(worldId) != null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' already exists!"));
            return 0;
        }

        // Validate dimension naming - prevent conflicts with vanilla dimension IDs
        if (dimensionName.equals("minecraft:nether") || dimensionName.equals("minecraft:end") || dimensionName.equals("minecraft:overworld")) {
            context.getSource().sendError(Text.literal("§c§l✖ §cInvalid dimension name: §e" + dimensionName));
            context.getSource().sendError(Text.literal(""));
            context.getSource().sendError(Text.literal("§e§l⚠ §6These dimension IDs don't exist in vanilla:"));
            context.getSource().sendError(Text.literal("  §c✗ §7minecraft:nether"));
            context.getSource().sendError(Text.literal("  §c✗ §7minecraft:end"));
            context.getSource().sendError(Text.literal("  §c✗ §7minecraft:overworld"));
            context.getSource().sendError(Text.literal(""));
            context.getSource().sendError(Text.literal("§e§l💡 §6Use these instead:"));
            context.getSource().sendError(Text.literal("  §a✓ §f§lminecraft:the_nether §7(vanilla nether)"));
            context.getSource().sendError(Text.literal("  §a✓ §f§lminecraft:the_end §7(vanilla end)"));
            context.getSource().sendError(Text.literal("  §a✓ §f§lminecraft:overworld §7(vanilla overworld) - Already exists!"));
            context.getSource().sendError(Text.literal("  §a✓ §f§ltimed_harvest:nether §7(custom nether)"));
            context.getSource().sendError(Text.literal("  §a✓ §f§ltimed_harvest:end §7(custom end)"));
            context.getSource().sendError(Text.literal("  §a✓ §f§ltimed_harvest:mining §7(custom world)"));
            return 0;
        }

        // Parse reset days and validate
        int resetDays;
        try {
            resetDays = Integer.parseInt(daysStr);
            // Only allow specific day values
            if (resetDays != 1 && resetDays != 2 && resetDays != 3 && resetDays != 4 && 
                resetDays != 5 && resetDays != 6 && resetDays != 7 && resetDays != 14 && 
                resetDays != 21 && resetDays != 28) {
                context.getSource().sendError(Text.literal("§c§l✖ §cInvalid reset interval!"));
                context.getSource().sendError(Text.literal(""));
                context.getSource().sendError(Text.literal("§e§lAllowed values (in days):"));
                context.getSource().sendError(Text.literal("  §a● §f1, 2, 3, 4, 5, 6, 7 §7(daily to weekly)"));
                context.getSource().sendError(Text.literal("  §a● §f14 §7(bi-weekly)"));
                context.getSource().sendError(Text.literal("  §a● §f21 §7(tri-weekly)"));
                context.getSource().sendError(Text.literal("  §a● §f28 §7(monthly)"));
                return 0;
            }
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("§cInvalid number format for reset days!"));
            return 0;
        }

        // Convert days to hours
        double resetHours = resetDays * 24.0;

        // Create new world config
        ModConfig.ResourceWorldConfig newWorld = new ModConfig.ResourceWorldConfig();
        newWorld.worldId = worldId;
        newWorld.dimensionName = dimensionName;
        newWorld.resetIntervalHours = resetHours;
        newWorld.worldType = worldType;
        newWorld.seed = seed;
        newWorld.worldBorderSize = borderSize;
        newWorld.generateStructures = generateStructures;
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
        
        // Save config again to persist the generated seed
        TimedHarvestMod.getConfig().save();
        
        // Reload to apply changes
        TimedHarvestMod.reloadConfig();

        // Get the final seed (may have been generated randomly)
        final long finalSeed = newWorld.seed;
        
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ Successfully Created Resource World!"), true);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);
        context.getSource().sendFeedback(() -> Text.literal("§e§lWorld Settings:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eWorld ID: §f" + worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eDimension: §f" + dimensionName), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eWorld Type: §f" + worldType), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eSeed: §a§l" + finalSeed + (seed == 0 ? " §7(randomly generated)" : "")), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eWorld Border: §f" + (borderSize == 0 ? "§7None (Infinite)" : borderSize + " blocks")), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eStructures: " + (generateStructures ? "§a§lENABLED" : "§c§lDISABLED")), false);
        context.getSource().sendFeedback(() -> Text.literal("  §6● §eReset Interval: §f" + resetDays + " days §7(" + (int)resetHours + " hours)"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        context.getSource().sendFeedback(() -> Text.literal("§e§lNext Steps:"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a1. §f§lRestart §fthe server/game"), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a2. §fRun: §6§l/timedharvest reset " + worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("  §a3. §fAccess via §6§l/th §fgui or §6§l/timedharvest tp " + worldId), false);
        context.getSource().sendFeedback(() -> Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"), false);

        return 1;
    }

    /**
     * Enables a resource world.
     */
    private static int enableWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
            return 0;
        }

        if (worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§e§l⚠ §eWorld '§6" + worldId + "§e' is already enabled!"));
            return 0;
        }

        worldConfig.enabled = true;
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aEnabled world '§e§l" + worldId + "§a'!"), true);
        return 1;
    }

    /**
     * Disables a resource world.
     */
    private static int disableWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
            return 0;
        }

        if (!worldConfig.enabled) {
            context.getSource().sendError(Text.literal("§e§l⚠ §eWorld '§6" + worldId + "§e' is already disabled!"));
            return 0;
        }

        worldConfig.enabled = false;
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        context.getSource().sendFeedback(() -> Text.literal("§c§l✓ §cDisabled world '§e§l" + worldId + "§c'!"), true);
        return 1;
    }

    /**
     * Deletes a resource world from the configuration and removes its files.
     */
    private static int deleteWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        
        ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
        if (worldConfig == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
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
                player.sendMessage(Text.literal("§6§l[Timed Harvest] §eYou were teleported to spawn because world '§6§l" + worldId + "§e' is being deleted."));
            }
        }

        // Remove from configuration
        TimedHarvestMod.getConfig().resourceWorlds.remove(worldConfig);
        TimedHarvestMod.getConfig().save();
        TimedHarvestMod.reloadConfig();

        // Delete world files (will happen on next server restart or manual reset)
        context.getSource().sendFeedback(() -> Text.literal("§c§l✗ §cWorld '§e§l" + worldId + "§c' has been deleted from configuration!"), true);
        context.getSource().sendFeedback(() -> Text.literal("§7World files will be removed on next server restart."), true);
        
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
            .executes(TimedHarvestCommands::openWorldGui)
            
            // /th admin - Open admin dashboard (requires permission)
            .then(CommandManager.literal("admin")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(TimedHarvestCommands::openAdminDashboard)));
    }

    /**
     * Opens the world selection GUI for the player.
     */
    private static int openWorldGui(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayer();
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }
        
        if (player == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }

        try {
            // Open the GUI
            SimpleInventory inventory = new SimpleInventory(27);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new WorldSelectionGui(syncId, playerInventory, inventory, player),
                Text.literal("§6§lResource Worlds")
            ));
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.error("Error opening world GUI", e);
            context.getSource().sendError(Text.literal("§c§l✖ §cError opening GUI: " + e.getMessage()));
            return 0;
        }

        return 1;
    }

    /**
     * Opens the admin dashboard GUI for operators.
     */
    private static int openAdminDashboard(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player;
        try {
            player = context.getSource().getPlayer();
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }
        
        if (player == null) {
            context.getSource().sendError(Text.literal("§c§l✖ §cThis command can only be used by players!"));
            return 0;
        }

        if (!player.hasPermissionLevel(2)) {
            player.sendMessage(Text.literal("§c§l✖ §cYou don't have permission to access the admin dashboard!"));
            return 0;
        }

        try {
            // Open the admin dashboard GUI
            SimpleInventory inventory = new SimpleInventory(54);
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new com.timedharvest.gui.AdminDashboardGui(syncId, playerInventory, inventory, player),
                Text.literal("§6§lAdmin Dashboard")
            ));
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.error("Error opening admin dashboard", e);
            context.getSource().sendError(Text.literal("§c§l✖ §cError opening admin dashboard: " + e.getMessage()));
            return 0;
        }

        return 1;
    }
    
    /**
     * Checks if a player can teleport (cooldown expired).
     */
    private static boolean checkTeleportCooldown(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        
        if (!TELEPORT_COOLDOWNS.containsKey(playerId)) {
            return true;
        }
        
        long lastTeleport = TELEPORT_COOLDOWNS.get(playerId);
        return (currentTime - lastTeleport) >= COOLDOWN_MS;
    }
    
    /**
     * Gets the remaining cooldown time in milliseconds.
     */
    private static long getRemainingCooldown(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        
        if (!TELEPORT_COOLDOWNS.containsKey(playerId)) {
            return 0;
        }
        
        long lastTeleport = TELEPORT_COOLDOWNS.get(playerId);
        long elapsed = currentTime - lastTeleport;
        return Math.max(0, COOLDOWN_MS - elapsed);
    }
    
    /**
     * Sets the teleport cooldown for a player.
     */
    private static void setTeleportCooldown(ServerPlayerEntity player) {
        TELEPORT_COOLDOWNS.put(player.getUuid(), System.currentTimeMillis());
    }
}
