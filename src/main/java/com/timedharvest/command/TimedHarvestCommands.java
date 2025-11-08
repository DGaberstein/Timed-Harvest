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
import java.nio.file.Path;

/**
 * Registers and handles all commands for the Timed Harvest mod.
 */
public class TimedHarvestCommands {
    
    // Teleport cooldown tracking
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 seconds cooldown
    private static final int[] RESET_DAYS = new int[] {1, 2, 3, 4, 5, 6, 7, 14, 21, 28};

    // Helper for resetDays suggestions
    private static com.mojang.brigadier.suggestion.SuggestionProvider<ServerCommandSource> resetDaysSuggester = (context, builder) -> {
        for (int d : RESET_DAYS) builder.suggest(Integer.toString(d));
        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
            registerPlayerCommands(dispatcher);

            // Register /th restart [worldId] for convenience (alias)
            dispatcher.register(CommandManager.literal("th")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("restart")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(ctx -> restartServerOrWorld(ctx, null))
                    .then(CommandManager.argument("worldId", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            ModConfig config = TimedHarvestMod.getConfig();
                            for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                                builder.suggest(worldConfig.worldId);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> restartServerOrWorld(ctx, StringArgumentType.getString(ctx, "worldId")))
                    )
                )
            );
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Build the root command with all subcommands chained directly
        com.mojang.brigadier.builder.LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("timedharvest")
            .requires(source -> source.hasPermissionLevel(2));

        root = root.then(CommandManager.literal("restart")
            .requires(source -> source.hasPermissionLevel(4))
            .executes(ctx -> restartServerOrWorld(ctx, null))
            .then(CommandManager.argument("worldId", StringArgumentType.string())
                .suggests((context, builder) -> {
                    ModConfig config = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                        builder.suggest(worldConfig.worldId);
                    }
                    return builder.buildFuture();
                })
                .executes(ctx -> restartServerOrWorld(ctx, StringArgumentType.getString(ctx, "worldId")))
            )
        );

        root = root.then(CommandManager.literal("reset")
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
                .then(CommandManager.argument("newseed", StringArgumentType.word())
                    .executes(TimedHarvestCommands::resetWorldWithNewSeed)
                )
                .executes(TimedHarvestCommands::resetWorld)
            )
        );

        root = root.then(CommandManager.literal("status")
            .executes(TimedHarvestCommands::statusAll)
            .then(CommandManager.argument("worldId", StringArgumentType.string())
                .suggests((context, builder) -> {
                    ModConfig config2 = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config2.resourceWorlds) {
                        builder.suggest(worldConfig.worldId);
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::statusSingle))
        );

        root = root.then(CommandManager.literal("reload")
            .executes(TimedHarvestCommands::reloadConfig));

        root = root.then(CommandManager.literal("tp")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .suggests((context, builder) -> {
                    ModConfig config3 = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config3.resourceWorlds) {
                        if (worldConfig.enabled) {
                            builder.suggest(worldConfig.worldId);
                        }
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::teleportToWorld))
        );

        root = root.then(CommandManager.literal("spawn")
            .executes(TimedHarvestCommands::teleportToSpawn));

        root = root.then(CommandManager.literal("create")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .then(CommandManager.argument("dimensionName", IdentifierArgumentType.identifier())
                    .then(CommandManager.argument("resetDays", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 28))
                        .suggests(resetDaysSuggester)
                        .executes(TimedHarvestCommands::createWorld)
                        .then(CommandManager.argument("worldType", IdentifierArgumentType.identifier())
                            .suggests((context, builder) -> {
                                builder.suggest("minecraft:overworld");
                                builder.suggest("minecraft:the_nether");
                                builder.suggest("minecraft:the_end");
                                return builder.buildFuture();
                            })
                            .executes(TimedHarvestCommands::createWorldWithType)
                            .then(CommandManager.argument("seed", StringArgumentType.word())
                                .executes(TimedHarvestCommands::createWorldWithSeed)
                                .then(CommandManager.argument("borderSize", StringArgumentType.word())
                                    .executes(TimedHarvestCommands::createWorldWithBorder)
                                    .then(CommandManager.argument("structures", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("true");
                                            builder.suggest("false");
                                            return builder.buildFuture();
                                        })
                                        .executes(TimedHarvestCommands::createWorldFull)
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );

        root = root.then(CommandManager.literal("enable")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .suggests((context, builder) -> {
                    ModConfig config4 = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config4.resourceWorlds) {
                        builder.suggest(worldConfig.worldId);
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::enableWorld))
        );

        root = root.then(CommandManager.literal("disable")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .suggests((context, builder) -> {
                    ModConfig config5 = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config5.resourceWorlds) {
                        builder.suggest(worldConfig.worldId);
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::disableWorld))
        );

        root = root.then(CommandManager.literal("delete")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .suggests((context, builder) -> {
                    ModConfig config6 = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config6.resourceWorlds) {
                        builder.suggest(worldConfig.worldId);
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::deleteWorld))
        );

        root = root.then(CommandManager.literal("help")
            .executes(TimedHarvestCommands::showHelp)
            .then(CommandManager.literal("troubleshooting")
                .executes(TimedHarvestCommands::showTroubleshooting))
        );

        root = root.then(CommandManager.literal("setmain")
            .then(CommandManager.argument("worldId", StringArgumentType.word())
                .suggests((context, builder) -> {
                    ModConfig config = TimedHarvestMod.getConfig();
                    for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
                        if (worldConfig.enabled) {
                            builder.suggest(worldConfig.worldId);
                        }
                    }
                    return builder.buildFuture();
                })
                .executes(TimedHarvestCommands::setMainWorld)
            )
        );

        dispatcher.register(root);
    }

    /**
     * Attempts to gracefully stop the server. If the host is configured to auto-restart, this will restart the server.
     */
    /**
     * Handles /timedharvest restart [worldId] and /th restart [worldId].
     * If worldId is provided, resets that world with a new seed. Otherwise, restarts the server.
     */
    private static int restartServerOrWorld(CommandContext<ServerCommandSource> context, String worldId) {
        if (worldId == null || worldId.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("§e⟳ §6Attempting to restart the server..."), true);
            try {
                context.getSource().getServer().stop(false); // false = not emergency, graceful shutdown
            } catch (Exception e) {
                context.getSource().sendError(Text.literal("§c§l✖ §cFailed to stop/restart the server: " + e.getMessage()));
                return 0;
            }
            return 1;
        } else {
            ModConfig.ResourceWorldConfig worldConfig = findWorldConfig(worldId);
            if (worldConfig == null) {
                context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found in configuration!"));
                return 0;
            }
            if (!worldConfig.enabled) {
                context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' is disabled!"));
                return 0;
            }
            context.getSource().sendFeedback(() -> Text.literal("§e⟳ §6Resetting resource world: §e§l" + worldId + "§6 with new random seed..."), true);
            TimedHarvestMod.getScheduler().manualResetWithNewSeed(worldId, worldConfig);
            context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aResource world '§e§l" + worldId + "§a' has been reset with a new random seed!"), true);
            return 1;
        }
    }

    /**
     * Manually resets a resource world with a new random seed.
     */
    private static int resetWorldWithNewSeed(CommandContext<ServerCommandSource> context) {
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
        context.getSource().sendFeedback(() -> Text.literal("§e⟳ §6Resetting resource world: §e§l" + worldId + "§6 with new random seed..."), true);
        TimedHarvestMod.getScheduler().manualResetWithNewSeed(worldId, worldConfig);
        context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aResource world '§e§l" + worldId + "§a' has been reset with a new random seed!"), true);
        return 1;
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

        final long finalSeed;
        boolean usedRandomSeed = false;
        if (seedStr == null || seedStr.isEmpty() || seedStr.equals("0")) {
            finalSeed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
            usedRandomSeed = true;
        } else {
            long parsedSeed;
            try {
                parsedSeed = Long.parseLong(seedStr);
            } catch (NumberFormatException e) {
                context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or leave blank for random."));
                return 0;
            }
            if (parsedSeed == 0) {
                finalSeed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
                usedRandomSeed = true;
            } else {
                finalSeed = parsedSeed;
            }
        }
        int result = createWorldInternal(context, worldType, finalSeed, 0, true);
        if (usedRandomSeed && result == 1) {
            context.getSource().sendFeedback(() -> Text.literal("§7A random seed was generated: §a" + finalSeed), false);
        }
        return result;
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
        if (seedStr.isEmpty() || seedStr.equals("0")) {
            seed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
        } else {
            try {
                seed = Long.parseLong(seedStr);
            } catch (NumberFormatException e) {
                context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or leave blank for random."));
                return 0;
            }
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
        if (seedStr.isEmpty() || seedStr.equals("0")) {
            seed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
        } else {
            try {
                seed = Long.parseLong(seedStr);
            } catch (NumberFormatException e) {
                context.getSource().sendError(Text.literal("§cInvalid seed format! Use a number or leave blank for random."));
                return 0;
            }
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
    int resetDays = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "resetDays");


        // Check if world already exists in config
        if (findWorldConfig(worldId) != null) {
            context.getSource().sendError(Text.literal("§cWorld '" + worldId + "' already exists!"));
            return 0;
        }

        // Check if a world folder already exists for this dimension, and delete it if so
        try {
            String worldName = context.getSource().getServer().getSaveProperties().getLevelName();
            java.nio.file.Path worldPath = context.getSource().getServer().getRunDirectory().toPath()
                .resolve("saves")
                .resolve(worldName)
                .resolve("dimensions")
                .resolve(new Identifier(dimensionName).getNamespace())
                .resolve(new Identifier(dimensionName).getPath());
            TimedHarvestMod.LOGGER.info("[TH] Checking for existing world folder at: {}", worldPath);
            if (java.nio.file.Files.exists(worldPath)) {
                TimedHarvestMod.LOGGER.info("[TH] Deleting existing world folder for dimension '{}': {}", dimensionName, worldPath);
                java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(worldPath);
                walk.sorted(java.util.Comparator.reverseOrder()).map(java.nio.file.Path::toFile).forEach(file -> {
                    TimedHarvestMod.LOGGER.info("[TH] Deleting file/folder: {}", file.getAbsolutePath());
                    file.delete();
                });
                walk.close();
                context.getSource().sendFeedback(() -> Text.literal("§e§l⚠ §6Old world folder for dimension '" + dimensionName + "' was deleted to allow new seed to take effect."), false);
            } else {
                TimedHarvestMod.LOGGER.info("[TH] No existing world folder found for dimension '{}'.", dimensionName);
            }
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.error("[TH] Failed to check/delete old world folder for dimension '{}': {}", dimensionName, e.getMessage(), e);
            context.getSource().sendError(Text.literal("§cFailed to check/delete old world folder: " + e.getMessage()));
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

        // resetDays is already validated by the argument type and suggestions

        // Convert days to hours
        double resetHours = resetDays * 24.0;

        // Create new world config
        ModConfig.ResourceWorldConfig newWorld = new ModConfig.ResourceWorldConfig();
        newWorld.worldId = worldId;
        newWorld.dimensionName = dimensionName;
        newWorld.resetIntervalHours = resetHours;
        newWorld.worldType = worldType;
    // Always use the provided seed (never 0)
    newWorld.seed = seed;
    TimedHarvestMod.LOGGER.info("[TH] Creating world '{}', dimension '{}', worldType '{}', seed {}, border {}, structures {}", worldId, dimensionName, worldType, seed, borderSize, generateStructures);
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
    TimedHarvestMod.LOGGER.info("[TH] Resource world '{}' created with seed {} (final seed: {})", worldId, seed, finalSeed);
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
        context.getSource().sendFeedback(() -> Text.literal("  §a1. §f§l§e§lRESTART REQUIRED!"), false);
        context.getSource().sendFeedback(() -> Text.literal("    §7You must restart the server/game to load the new dimension."), false);
        context.getSource().sendFeedback(() -> Text.literal("    §6§lWhy? §fMinecraft only loads new dimensions on startup."), false);
        context.getSource().sendFeedback(() -> Text.literal("    §eIf you do not restart, the new world will NOT be accessible!"), false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
        // Add clickable /th restart reminder for admins
        net.minecraft.text.ClickEvent clickRestart = new net.minecraft.text.ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, "/timedharvest restart");
        net.minecraft.text.Text restartText = net.minecraft.text.Text.literal("§a[Click here to restart the server now]")
            .styled(style -> style.withColor(net.minecraft.util.Formatting.GREEN).withBold(true).withClickEvent(clickRestart).withHoverEvent(new net.minecraft.text.HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, net.minecraft.text.Text.literal("§eRun /timedharvest restart (OP only)"))));
        context.getSource().sendFeedback(() -> restartText, false);
        context.getSource().sendFeedback(() -> Text.literal(""), false);
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

        // Actually delete world files from disk now
        try {
            Path worldPath = context.getSource().getServer().getRunDirectory().toPath()
                .resolve("saves")
                .resolve(context.getSource().getServer().getSaveProperties().getLevelName())
                .resolve("dimensions")
                .resolve(dimensionKey.getValue().getNamespace())
                .resolve(dimensionKey.getValue().getPath());
            TimedHarvestMod.LOGGER.info("[TH] Attempting to delete world folder for '{}': {}", worldId, worldPath);
            if (java.nio.file.Files.exists(worldPath)) {
                java.nio.file.Files.walk(worldPath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(file -> {
                        TimedHarvestMod.LOGGER.info("[TH] Deleting file/folder: {}", file.getAbsolutePath());
                        file.delete();
                    });
                if (!java.nio.file.Files.exists(worldPath)) {
                    TimedHarvestMod.LOGGER.info("[TH] Successfully deleted world folder: {}", worldPath);
                    context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aWorld files deleted from disk: " + worldPath), true);
                } else {
                    TimedHarvestMod.LOGGER.error("[TH] Failed to fully delete world folder: {}", worldPath);
                    context.getSource().sendError(Text.literal("§c§l✗ §cFailed to fully delete world folder: " + worldPath));
                }
            } else {
                TimedHarvestMod.LOGGER.info("[TH] World folder did not exist: {}", worldPath);
                context.getSource().sendFeedback(() -> Text.literal("§7World folder did not exist: " + worldPath), true);
            }
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.error("[TH] Error deleting world files for '{}': {}", worldId, e.getMessage(), e);
            context.getSource().sendError(Text.literal("§c§l✗ §cError deleting world files: " + e.getMessage()));
        }
        context.getSource().sendFeedback(() -> Text.literal("§c§l✗ §cWorld '§e§l" + worldId + "§c' has been deleted from configuration!"), true);
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
    /**
     * Sets the main world for evacuation after reset.
     */
    private static int setMainWorld(CommandContext<ServerCommandSource> context) {
        String worldId = StringArgumentType.getString(context, "worldId");
        ModConfig config = TimedHarvestMod.getConfig();
        for (ModConfig.ResourceWorldConfig worldConfig : config.resourceWorlds) {
            if (worldConfig.worldId != null && worldConfig.worldId.equals(worldId) && worldConfig.enabled) {
                config.mainWorldId = worldId;
                config.save();
                context.getSource().sendFeedback(() -> Text.literal("§a§l✓ §aSet '" + worldId + "' as the main world for evacuation."), false);
                return 1;
            }
        }
        context.getSource().sendError(Text.literal("§c§l✖ §cWorld '§e" + worldId + "§c' not found or not enabled!"));
        return 0;
    }
}
