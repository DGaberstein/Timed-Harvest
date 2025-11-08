package com.timedharvest.gui;

import com.timedharvest.TimedHarvestMod;
import com.timedharvest.config.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for selecting and teleporting to resource worlds.
 */
public class WorldSelectionGui extends ScreenHandler {
    private final Inventory inventory;
    private final List<ModConfig.ResourceWorldConfig> enabledWorlds;
    private final ServerPlayerEntity player;
    private int currentPage = 0;
    private static final int WORLDS_PER_PAGE = 9; // One row of worlds
    
    // Teleport cooldown tracking
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3 seconds cooldown

    public WorldSelectionGui(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27), (ServerPlayerEntity) playerInventory.player);
    }

    public WorldSelectionGui(int syncId, PlayerInventory playerInventory, Inventory inventory, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, syncId);
        this.inventory = inventory;
        this.player = player;
        this.enabledWorlds = new ArrayList<>();
        
        // Get all enabled worlds
        for (ModConfig.ResourceWorldConfig config : TimedHarvestMod.getConfig().resourceWorlds) {
            if (config.enabled) {
                enabledWorlds.add(config);
            }
        }

        // Add inventory slots (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false; // No inserting items
                    }
                });
            }
        }

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        // Populate the GUI with world items
        populateInventory();
    }

    private void populateInventory() {
        inventory.clear();
        
        int totalWorlds = enabledWorlds.size();
        int totalPages = (int) Math.ceil((double) totalWorlds / WORLDS_PER_PAGE);
        
        // Title bar (row 0) - Yellow glass panes like Admin Dashboard
        for (int i = 0; i < 9; i++) {
            ItemStack pane = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
            NbtCompound nbt = pane.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", Text.Serializer.toJson(Text.literal("§6§l▬▬ Resource Worlds ▬▬")));
            nbt.put("display", display);
            inventory.setStack(i, pane);
        }
        
        // Calculate which worlds to show on this page
        int startIndex = currentPage * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, totalWorlds);
        int worldsOnThisPage = endIndex - startIndex;
        
        // Center worlds in the SECOND row (slots 9-17)
        int startSlot = 9 + (9 - worldsOnThisPage) / 2; // Center in second row
        
        int slot = startSlot;
        for (int i = startIndex; i < endIndex; i++) {
            ModConfig.ResourceWorldConfig worldConfig = enabledWorlds.get(i);
            ItemStack item = createWorldItem(worldConfig);
            inventory.setStack(slot, item);
            slot++;
        }
        
        // Bottom action bar (row 3, slots 18-26)
        
        // Return to Spawn button (slot 18)
        ItemStack spawnItem = new ItemStack(Items.RED_BED);
        setItemNameAndLore(spawnItem, "§a§lReturn to Spawn",
            "§7Click to teleport to",
            "§7the overworld spawn");
        inventory.setStack(18, spawnItem);
        
        // Previous Page button (slot 19)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Items.ARROW);
            setItemNameAndLore(prevButton, "§e◀ Previous Page",
                "§7Page " + currentPage + " of " + totalPages);
            inventory.setStack(19, prevButton);
        }
        
        // Page Indicator (slot 22 - center)
        if (totalPages > 1) {
            ItemStack pageItem = new ItemStack(Items.PAPER);
            setItemNameAndLore(pageItem, "§6§lPage " + (currentPage + 1) + " / " + totalPages,
                "§7Showing " + worldsOnThisPage + " of " + totalWorlds + " worlds");
            inventory.setStack(22, pageItem);
        }
        
        // Next Page button (slot 25)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Items.ARROW);
            setItemNameAndLore(nextButton, "§eNext Page ▶",
                "§7Page " + (currentPage + 2) + " of " + totalPages);
            inventory.setStack(25, nextButton);
        }
        
        // Admin Dashboard button (slot 26) - ONLY show if player has permission
        if (player != null && player.hasPermissionLevel(2)) {
            ItemStack adminButton = new ItemStack(Items.NETHER_STAR);
            setItemNameAndLore(adminButton, "§6§l⚙ Admin Dashboard",
                "§7Click to open the",
                "§7admin management panel",
                "",
                "§e§lManage all worlds");
            inventory.setStack(26, adminButton);
        }
    }

    private void setItemNameAndLore(ItemStack item, String name, String... loreLines) {
        NbtCompound nbt = item.getOrCreateNbt();
        NbtCompound display = new NbtCompound();
        display.putString("Name", Text.Serializer.toJson(Text.literal(name)));
        
        if (loreLines.length > 0) {
            NbtList lore = new NbtList();
            for (String line : loreLines) {
                lore.add(NbtString.of(Text.Serializer.toJson(Text.literal(line))));
            }
            display.put("Lore", lore);
        }
        
        nbt.put("display", display);
    }

    private ItemStack createWorldItem(ModConfig.ResourceWorldConfig worldConfig) {
        // Null check
        if (worldConfig == null) {
            TimedHarvestMod.LOGGER.warn("Null world config in createWorldItem");
            return new ItemStack(Items.BARRIER);
        }
        
        // Check if dimension actually exists
        boolean dimensionExists = false;
        try {
            // Try to create the identifier - if it fails, dimension name is malformed
            new Identifier(worldConfig.dimensionName);
            dimensionExists = true; // If we got here, the identifier is valid
        } catch (Exception e) {
            TimedHarvestMod.LOGGER.warn("Invalid dimension name: {}", worldConfig.dimensionName);
        }
        
        // Choose icon based on world type and existence
        ItemStack item;
        String worldType = worldConfig.worldType != null ? worldConfig.worldType : "minecraft:overworld";
        
        if (!dimensionExists) {
            // Use barrier block for non-existent dimensions
            item = new ItemStack(Items.BARRIER);
        } else if (worldType.contains("nether")) {
            item = new ItemStack(Items.NETHERRACK);
        } else if (worldType.contains("end")) {
            item = new ItemStack(Items.END_STONE);
        } else {
            item = new ItemStack(Items.GRASS_BLOCK);
        }
        
        // Set custom name using NBT
        NbtCompound nbt = item.getOrCreateNbt();
        NbtCompound display = new NbtCompound();
        String worldId = worldConfig.worldId != null ? worldConfig.worldId : "Unknown World";
        
        // Add warning color if dimension doesn't exist
        Formatting nameColor = dimensionExists ? Formatting.YELLOW : Formatting.RED;
        display.putString("Name", Text.Serializer.toJson(Text.literal(worldId).formatted(nameColor)));
        
        // Add lore
        NbtList lore = new NbtList();
        
        if (!dimensionExists) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("⚠ NOT CREATED YET").formatted(Formatting.RED, Formatting.BOLD))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal(""))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Click for instructions").formatted(Formatting.GRAY))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("on how to create it").formatted(Formatting.GRAY))));
        } else {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("World Type: " + worldType).formatted(Formatting.GRAY))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Reset: " + worldConfig.resetIntervalHours + " hours").formatted(Formatting.GRAY))));
            
            if (worldConfig.worldBorderSize > 0) {
                lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Border: " + worldConfig.worldBorderSize + " blocks").formatted(Formatting.GRAY))));
            }
            
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal(""))));
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Click to teleport!").formatted(Formatting.GREEN))));
        }
        
        display.put("Lore", lore);
        nbt.put("display", display);
        
        return item;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= inventory.size()) {
            return;
        }
        
        ItemStack clickedItem = inventory.getStack(slotIndex);
        if (clickedItem == null || clickedItem.isEmpty()) {
            return;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        // Ignore clicks on title bar (slots 0-8)
        if (slotIndex >= 0 && slotIndex <= 8) {
            return;
        }
        
        // Check if it's the spawn button (slot 18)
        if (slotIndex == 18 && clickedItem.getItem() == Items.RED_BED) {
            teleportToSpawn(serverPlayer);
            serverPlayer.closeHandledScreen();
            return;
        }
        
        // Check if it's the Previous Page button (slot 19)
        if (slotIndex == 19 && clickedItem.getItem() == Items.ARROW) {
            if (currentPage > 0) {
                currentPage--;
                populateInventory();
            }
            return;
        }
        
        // Check if it's the page indicator (slot 22) - do nothing
        if (slotIndex == 22 && clickedItem.getItem() == Items.PAPER) {
            return;
        }
        
        // Check if it's the Next Page button (slot 25)
        if (slotIndex == 25 && clickedItem.getItem() == Items.ARROW) {
            int totalPages = (int) Math.ceil((double) enabledWorlds.size() / WORLDS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                populateInventory();
            }
            return;
        }
        
        // Check if it's the admin dashboard button (slot 26)
        if (slotIndex == 26 && clickedItem.getItem() == Items.NETHER_STAR) {
            // Check permission (double-check even though button shouldn't be visible)
            if (!serverPlayer.hasPermissionLevel(2)) {
                serverPlayer.sendMessage(Text.literal("§c§l✖ §cYou don't have permission to access the admin dashboard!"));
                serverPlayer.closeHandledScreen();
                return;
            }
            
            // Open admin dashboard
            serverPlayer.closeHandledScreen();
            serverPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new AdminDashboardGui(syncId, inv, new net.minecraft.inventory.SimpleInventory(54), serverPlayer),
                Text.literal("§6§lAdmin Dashboard")
            ));
            return;
        }
        
        // Otherwise, check if it's a world teleport button in second row (slots 9-17)
        if (slotIndex >= 9 && slotIndex <= 17) {
            int worldIndex = findWorldIndexForSlot(slotIndex);
            if (worldIndex >= 0 && worldIndex < enabledWorlds.size()) {
                ModConfig.ResourceWorldConfig worldConfig = enabledWorlds.get(worldIndex);
                teleportToWorld(serverPlayer, worldConfig);
                serverPlayer.closeHandledScreen();
            }
        }
    }
    
    /**
     * Finds which world index a slot corresponds to, accounting for centering and pagination.
     */
    private int findWorldIndexForSlot(int slotIndex) {
        int startIndex = currentPage * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, enabledWorlds.size());
        int worldsOnThisPage = endIndex - startIndex;
        
        // Second row starts at slot 9, worlds are centered
        int startSlot = 9 + (9 - worldsOnThisPage) / 2;
        
        // Check if this slot is within the world button range
        if (slotIndex >= startSlot && slotIndex < startSlot + worldsOnThisPage) {
            return startIndex + (slotIndex - startSlot);
        }
        
        return -1; // Not a world slot
    }

    private void teleportToWorld(ServerPlayerEntity player, ModConfig.ResourceWorldConfig worldConfig) {
        // Check teleport cooldown
        if (!checkTeleportCooldown(player)) {
            long remainingMs = getRemainingCooldown(player);
            double remainingSec = remainingMs / 1000.0;
            player.sendMessage(Text.literal(String.format("§c§l⏱ §cPlease wait %.1f seconds before teleporting again!", remainingSec)), true);
            player.closeHandledScreen();
            return;
        }
        
        RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, 
            new Identifier(worldConfig.dimensionName));
        
        ServerWorld targetWorld = player.getServer().getWorld(dimensionKey);
        if (targetWorld == null) {
            // Check if player has permission
            boolean hasPermission = player.hasPermissionLevel(2);
            
            // Send helpful error message with fix instructions
            player.sendMessage(Text.literal("§c§l✖ §cDimension '§e" + worldConfig.dimensionName + "§c' does not exist!"));
            player.sendMessage(Text.literal(""));
            player.sendMessage(Text.literal("§e§l⚙ To fix this issue:"));
            
            if (hasPermission) {
                player.sendMessage(Text.literal("  §a1. §fRun: §6§l/timedharvest reset §e" + worldConfig.worldId));
                player.sendMessage(Text.literal("  §a2. §7This will create the dimension and its datapack"));
            } else {
                player.sendMessage(Text.literal("  §7Ask an admin to run: §6§l/timedharvest reset §e" + worldConfig.worldId));
            }
            
            player.sendMessage(Text.literal(""));
            player.sendMessage(Text.literal("§8Tip: Use §7/reload §8after creating new worlds"));
            return;
        }

        // Apply world border if configured
        if (worldConfig.worldBorderSize > 0) {
            net.minecraft.world.border.WorldBorder border = targetWorld.getWorldBorder();
            border.setCenter(0.0, 0.0);
            border.setSize(worldConfig.worldBorderSize);
        }

        // Teleport to spawn point
        BlockPos spawnPos = targetWorld.getSpawnPos();
        player.teleport(targetWorld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        
        // Set cooldown after successful teleport
        setTeleportCooldown(player);
        
            // The server will send the correct feedback with world name and seed, so no need to send a local message here.
    }

    private void teleportToSpawn(ServerPlayerEntity player) {
        // Check teleport cooldown
        if (!checkTeleportCooldown(player)) {
            long remainingMs = getRemainingCooldown(player);
            double remainingSec = remainingMs / 1000.0;
            player.sendMessage(Text.literal(String.format("§c§l⏱ §cPlease wait %.1f seconds before teleporting again!", remainingSec)), true);
            player.closeHandledScreen();
            return;
        }
        
        ServerWorld overworld = player.getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        
        // Set cooldown after successful teleport
        setTeleportCooldown(player);

        player.sendMessage(Text.literal("§a§l✓ §aTeleported to §e§lspawn§a!"));
    }
    
    /**
     * Checks if a player can teleport (cooldown expired).
     */
    private boolean checkTeleportCooldown(ServerPlayerEntity player) {
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
    private long getRemainingCooldown(ServerPlayerEntity player) {
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
    private void setTeleportCooldown(ServerPlayerEntity player) {
        TELEPORT_COOLDOWNS.put(player.getUuid(), System.currentTimeMillis());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY; // Disable shift-clicking
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
    }
}
