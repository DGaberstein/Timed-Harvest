package com.timedharvest.gui;

import com.timedharvest.TimedHarvestMod;
import com.timedharvest.config.ModConfig;
import com.timedharvest.scheduler.ResetScheduler;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
/**
 * Admin Dashboard GUI for managing resource worlds.
 */
public class AdminDashboardGui extends ScreenHandler {
    // --- Static inner classes for GUI factories and confirmation dialogs ---
    public static class AdminDashboardGuiFactory implements net.minecraft.screen.NamedScreenHandlerFactory {
        @Override
        public net.minecraft.text.Text getDisplayName() {
            return net.minecraft.text.Text.literal("Admin Dashboard");
        }
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new AdminDashboardGui(syncId, inv);
        }
    }

    public static class WorldCommandGuiFactory implements net.minecraft.screen.NamedScreenHandlerFactory {
        private final ModConfig.ResourceWorldConfig worldConfig;
        public WorldCommandGuiFactory(ModConfig.ResourceWorldConfig worldConfig) {
            this.worldConfig = worldConfig;
        }
        @Override
        public net.minecraft.text.Text getDisplayName() {
            return net.minecraft.text.Text.literal("World Command");
        }
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new WorldCommandGui(syncId, inv, worldConfig, (ServerPlayerEntity) player);
        }
    }

    public static class ConfirmationGuiFactory implements net.minecraft.screen.NamedScreenHandlerFactory {
        private final boolean approved;
        public ConfirmationGuiFactory(boolean approved) {
            this.approved = approved;
        }
        @Override
        public net.minecraft.text.Text getDisplayName() {
            return net.minecraft.text.Text.literal(approved ? "Approve Action" : "Cancel Action");
        }
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new ConfirmationGui(syncId, inv, (ServerPlayerEntity) player, approved);
        }
    }

    public static class ConfirmationGui extends ScreenHandler {
        private final Inventory inventory;
    // Removed unused fields: player, approved
        public ConfirmationGui(int syncId, PlayerInventory playerInventory, ServerPlayerEntity player, boolean approved) {
            super(ScreenHandlerType.GENERIC_9X1, syncId);
            this.inventory = new SimpleInventory(9);
            // Add slots
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col, 8 + col * 18, 18));
            }
            // Populate confirmation button
            ItemStack confirm = new ItemStack(approved ? Items.LIME_CONCRETE : Items.RED_CONCRETE);
            setItemNameAndLore(confirm, approved ? "§aCONFIRM" : "§cCONFIRM CANCEL", approved ? "§7Confirm this action" : "§7Confirm the CANCEL action");
            inventory.setStack(4, confirm);
        }
        @Override
        public boolean canUse(PlayerEntity player) { return true; }
        @Override
        public ItemStack quickMove(PlayerEntity player, int index) { return ItemStack.EMPTY; }
        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
            if (slotIndex == 4 && actionType == SlotActionType.PICKUP) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                serverPlayer.closeHandledScreen();
                serverPlayer.openHandledScreen(new AdminDashboardGui.AdminDashboardGuiFactory());
            }
        }
        private void setItemNameAndLore(ItemStack item, String name, String lore) {
            setItemNameAndLore(item, name, new String[]{lore});
        }
        private void setItemNameAndLore(ItemStack item, String name, String... loreLines) {
            NbtCompound nbt = item.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", Text.Serializer.toJson(Text.literal(name)));
            NbtList loreList = new NbtList();
            for (String line : loreLines) {
                loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal(line))));
            }
            display.put("Lore", loreList);
            nbt.put("display", display);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.hasPermissionLevel(2); // Admin only;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY; // Disable shift-clicking;
    }
    private final Inventory inventory;
    private int currentPage = 0;
    private static final int WORLDS_PER_PAGE = 7; // Leave room for navigation and actions

    public AdminDashboardGui(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(54), (ServerPlayerEntity) playerInventory.player);
    }

    public AdminDashboardGui(int syncId, PlayerInventory playerInventory, Inventory inventory, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        this.inventory = inventory;

        // Add inventory slots (6 rows of 9)
        for (int row = 0; row < 6; row++) {
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
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        // Populate the GUI
        populateInventory();
    }

    private void populateInventory() {
        inventory.clear();

        List<ModConfig.ResourceWorldConfig> worlds = TimedHarvestMod.getConfig().resourceWorlds;
        int totalWorlds = worlds.size();
        int totalPages = (int) Math.ceil((double) totalWorlds / WORLDS_PER_PAGE);

        // Title bar (row 0)
        for (int i = 0; i < 9; i++) {
            ItemStack pane = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
            NbtCompound nbt = pane.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", Text.Serializer.toJson(Text.literal("§6§l▬▬ Admin Dashboard ▬▬")));
            nbt.put("display", display);
            inventory.setStack(i, pane);
        }

        // World list (rows 1-4, slots 10-44, only odd columns 1,3,5,7)
        int startIndex = currentPage * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, totalWorlds);
        int worldsPlaced = 0;
        outer:
        for (int row = 0; row < 4; row++) {
            for (int col = 1; col <= 7; col += 2) { // Only odd columns
                int slot = 10 + row * 9 + col;
                int worldIdx = startIndex + worldsPlaced;
                if (worldIdx >= endIndex) break outer;
                ModConfig.ResourceWorldConfig worldConfig = worlds.get(worldIdx);
                ItemStack worldItem = createWorldManagementItem(worldConfig);
                inventory.setStack(slot, worldItem);
                worldsPlaced++;
            }
        }

        // Bottom action bar (row 5, slots 45-53)
        // Reload Config button
        ItemStack reloadItem = new ItemStack(Items.WRITABLE_BOOK);
        setItemNameAndLore(reloadItem, "§e§lReload Config", 
            "§7Click to reload the",
            "§7configuration file");
        inventory.setStack(45, reloadItem);
        
        // Create World button
        ItemStack createItem = new ItemStack(Items.EMERALD);
        setItemNameAndLore(createItem, "§a§lCreate World", 
            "§7Click to create a new resource world with a random seed.",
            "§6/timedharvest create <worldId> <dimension> <resetDays> <worldType> <seed>");
        inventory.setStack(46, createItem);
        
        // Help button
        ItemStack helpItem = new ItemStack(Items.BOOK);
        setItemNameAndLore(helpItem, "§b§lHelp & Commands", 
            "§7Click to view all",
            "§7available commands");
        inventory.setStack(47, helpItem);
        
        // Previous Page
        if (currentPage > 0) {
            ItemStack prevItem = new ItemStack(Items.ARROW);
            setItemNameAndLore(prevItem, "§e◀ Previous Page", 
                "§7Page " + currentPage + " of " + totalPages);
            inventory.setStack(48, prevItem);
        }
        
        // Page indicator (center)
        ItemStack pageItem = new ItemStack(Items.PAPER);
        setItemNameAndLore(pageItem, "§6§lPage " + (currentPage + 1) + " / " + totalPages,
            "§7Showing " + (endIndex - startIndex) + " of " + totalWorlds + " worlds");
        inventory.setStack(49, pageItem);
        
        // Next Page
        if (currentPage < totalPages - 1) {
            ItemStack nextItem = new ItemStack(Items.ARROW);
            setItemNameAndLore(nextItem, "§eNext Page ▶", 
                "§7Page " + (currentPage + 2) + " of " + totalPages);
            inventory.setStack(50, nextItem);
        }
        
        // Back button (arrow)
        ItemStack backItem = new ItemStack(Items.ARROW);
        setItemNameAndLore(backItem, "§e§lBack", "§7Return to dashboard");
        inventory.setStack(53, backItem);
        }

    private ItemStack createWorldManagementItem(ModConfig.ResourceWorldConfig worldConfig) {
        ItemStack item;
        
        // Choose icon based on world type
        String worldType = worldConfig.worldType != null ? worldConfig.worldType : "minecraft:overworld";
        if (worldType.contains("nether")) {
            item = new ItemStack(Items.NETHERRACK);
        } else if (worldType.contains("end")) {
            item = new ItemStack(Items.END_STONE);
        } else {
            item = new ItemStack(Items.GRASS_BLOCK);
        }
        
        NbtCompound nbt = item.getOrCreateNbt();
        NbtCompound display = new NbtCompound();
        
    // Title - world ID with status
    String status = worldConfig.enabled ? "§a●" : "§c●";
    boolean isMain = TimedHarvestMod.getConfig().mainWorldId != null && TimedHarvestMod.getConfig().mainWorldId.equals(worldConfig.worldId);
    String mainTag = isMain ? " §e★" : "";
    display.putString("Name", Text.Serializer.toJson(Text.literal(status + " §e§l" + worldConfig.worldId + mainTag)));
        
        // Lore with details
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬").formatted(Formatting.DARK_GRAY))));
        if (isMain) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§e★ Main World for evacuation after reset"))));
        }
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Dimension: §f" + worldConfig.dimensionName))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6World Type: §f" + worldType))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Reset Interval: §f" + worldConfig.resetIntervalHours + "h"))));

        if (worldConfig.seed != 0) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Seed: §f" + worldConfig.seed))));
        }

        if (worldConfig.worldBorderSize > 0) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Border: §f" + worldConfig.worldBorderSize + " blocks"))));
        }

        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Structures: " + (worldConfig.generateStructures ? "§a§lON" : "§c§lOFF")))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Status: " + (worldConfig.enabled ? "§a§lENABLED" : "§c§lDISABLED")))));

        // Show pending delete if world folder exists but not in config
        java.io.File worldFolder = null;
        try {
            String worldName = TimedHarvestMod.getConfig().mainWorldId; // fallback to main world name
            if (worldName == null || worldName.isEmpty()) {
                worldName = "world";
            }
            java.io.File savesDir = new java.io.File("saves");
            worldFolder = new java.io.File(savesDir, worldConfig.worldId);
        } catch (Exception e) {
            // ignore
        }
        boolean pendingDelete = false;
        if (worldFolder != null && worldFolder.exists() && !TimedHarvestMod.getConfig().resourceWorlds.contains(worldConfig)) {
            pendingDelete = true;
        }
        if (pendingDelete) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§c§lPENDING DELETE: World files will be removed on next restart!"))));
        }

        // Next reset time if enabled
        if (worldConfig.enabled && TimedHarvestMod.getConfig().enableAutoReset) {
            long timeRemaining = TimedHarvestMod.getScheduler().getTimeUntilReset(worldConfig.worldId, worldConfig);
            String timeStr = ResetScheduler.formatTime(timeRemaining);
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§6Next Reset: §a" + timeStr))));
        }

        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal(""))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§e§l▶ LEFT CLICK §7to manage"))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§c§l▶ RIGHT CLICK §7to toggle"))));

        display.put("Lore", lore);
        nbt.put("display", display);

        return item;
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

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // Handle Reload Config button (slot 45)
        if (slotIndex == 45 && actionType == SlotActionType.PICKUP) {
            TimedHarvestMod.reloadConfig();
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.sendMessage(Text.literal("§a[Timed Harvest] Config reloaded!"));
            populateInventory();
            return;
        }
        // Handle Help & Commands button (slot 47)
        if (slotIndex == 47 && actionType == SlotActionType.PICKUP) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.closeHandledScreen();
            // Show help (could open a new GUI or send chat message)
            serverPlayer.sendMessage(Text.literal("§b[Timed Harvest] See /th help or /timedharvest help for all commands."));
            return;
        }
        // Handle Back button (slot 53)
        if (slotIndex == 53 && actionType == SlotActionType.PICKUP) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            serverPlayer.closeHandledScreen();
            // Return to Resource Worlds GUI
            serverPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new com.timedharvest.gui.WorldSelectionGui(syncId, inv, new net.minecraft.inventory.SimpleInventory(27), serverPlayer),
                Text.literal("§e§lResource Worlds")
            ));
            return;
        }
        // Handle Delete World button (slot 24)
        if (slotIndex == 24 && actionType == SlotActionType.PICKUP) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            // Find the worldId for this slot
            int worldIdx = (currentPage * WORLDS_PER_PAGE) + ((24 - 10) / 2);
            List<ModConfig.ResourceWorldConfig> worlds = TimedHarvestMod.getConfig().resourceWorlds;
            if (worldIdx >= 0 && worldIdx < worlds.size()) {
                String worldId = worlds.get(worldIdx).worldId;
                serverPlayer.closeHandledScreen();
                String cmd = String.format("timedharvest delete %s", worldId);
                serverPlayer.getServer().getCommandManager().executeWithPrefix(serverPlayer.getCommandSource(), cmd);
            }
            return;
        }
        // Handle Create World button (slot 46)
        if (slotIndex == 46 && actionType == SlotActionType.PICKUP) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            // Generate a random worldId and seed
            String worldId = "world_" + System.currentTimeMillis();
            String dimension = "timed_harvest:custom_" + worldId;
            int resetDays = 7; // Default to 7 days, or you could prompt/select
            String worldType = "minecraft:overworld";
            long seed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
            serverPlayer.closeHandledScreen();
            String cmd = String.format("timedharvest create %s %s %d %s %d", worldId, dimension, resetDays, worldType, seed);
            serverPlayer.getServer().getCommandManager().executeWithPrefix(serverPlayer.getCommandSource(), cmd);
            return;
        }
        // Prevent default behavior for GUI slots
        if (slotIndex < 0 || slotIndex >= 54) {
            super.onSlotClick(slotIndex, button, actionType, player);
            return;
        }
        
        // Ignore pickup actions, only respond to direct clicks
        if (actionType != SlotActionType.PICKUP) {
            return;
        }
        
        ItemStack clickedItem = inventory.getStack(slotIndex);
        if (clickedItem == null || clickedItem.isEmpty()) {
            return;
        }
        
        // Only handle navigation to submenu or toggling enabled/disabled
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        List<ModConfig.ResourceWorldConfig> worlds = TimedHarvestMod.getConfig().resourceWorlds;
        int startIndex = currentPage * WORLDS_PER_PAGE;
        if (slotIndex < 10 || slotIndex > 44) return;
        int row = (slotIndex - 10) / 9;
        int col = (slotIndex - 10) % 9;
        if (col < 1 || col > 7 || col % 2 == 0) return;
        int indexInRow = (col - 1) / 2;
        int worldIndex = startIndex + (row * 4) + indexInRow;
        if (worldIndex < 0 || worldIndex >= worlds.size()) return;
        ModConfig.ResourceWorldConfig worldConfig = worlds.get(worldIndex);
        if (button == 0) { // Left click - show management options
            serverPlayer.closeHandledScreen();
            showWorldManagementOptions(serverPlayer, worldConfig);
        } else if (button == 1) { // Right click - toggle enabled/disabled
            worldConfig.enabled = !worldConfig.enabled;
            TimedHarvestMod.getConfig().save();
            populateInventory();
            String status = worldConfig.enabled ? "§a§lENABLED" : "§c§lDISABLED";
            serverPlayer.sendMessage(Text.literal("§6World '§e§l" + worldConfig.worldId + "§6' is now " + status));
        }
    }


    private void showWorldManagementOptions(ServerPlayerEntity player, ModConfig.ResourceWorldConfig worldConfig) {
        // Open a new GUI with command buttons for this world
    player.openHandledScreen(new AdminDashboardGui.WorldCommandGuiFactory(worldConfig));
    }

    // Inner class for the world command submenu GUI
    private static class WorldCommandGui extends ScreenHandler {
        /**
         * Returns the name of the player who opened this GUI.
         */
        public String getPlayerName() {
            return player.getName().getString();
        }
        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int index) {
            return ItemStack.EMPTY;
        }
        private final ModConfig.ResourceWorldConfig worldConfig;
        private final Inventory inventory;
        private final ServerPlayerEntity player;

        public WorldCommandGui(int syncId, PlayerInventory playerInventory, ModConfig.ResourceWorldConfig worldConfig, ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X3, syncId);
            this.worldConfig = worldConfig;
            this.player = player;
            this.inventory = new SimpleInventory(27);

            // Add slots
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18) {
                        @Override
                        public boolean canInsert(ItemStack stack) { return false; }
                    });
                }
            }

            // Use the player field: send a message when the GUI is opened
            player.sendMessage(Text.literal("§7World Command Menu opened for: §e" + worldConfig.worldId));

            // Populate command buttons
            populateButtons();
        }

        private void populateButtons() {
            // Restart World (reset with new seed)
            ItemStack restartWorld = new ItemStack(Items.RESPAWN_ANCHOR);
            setItemNameAndLore(restartWorld, "§eRestart World (New Seed)", "§7Reset this world with a new random seed (same as /timedharvest restart <worldId>)");
            inventory.setStack(24, restartWorld);

            // Delete World button (in world management submenu)
            ItemStack deleteWorld = new ItemStack(Items.BARRIER);
            setItemNameAndLore(deleteWorld, "§cDelete World Files", "§cDelete world files and remove from config");
            inventory.setStack(20, deleteWorld);
            // Reset with New Seed
            ItemStack resetNewSeed = new ItemStack(Items.GLOWSTONE_DUST);
            setItemNameAndLore(resetNewSeed, "§eReset with New Seed", "§7Reset this world and generate a new random seed");
            inventory.setStack(11, resetNewSeed);
            // Removed misplaced code for reset with new seed
            // Reset
            ItemStack reset = new ItemStack(Items.REDSTONE_BLOCK);
            setItemNameAndLore(reset, "§e/timedharvest reset", "§7Manually reset this world");
            inventory.setStack(10, reset);

            // TP
            ItemStack tp = new ItemStack(Items.ENDER_PEARL);
            setItemNameAndLore(tp, "§e/timedharvest tp", "§7Teleport to this world");
            inventory.setStack(12, tp);

            // Status
            ItemStack status = new ItemStack(Items.PAPER);
            setItemNameAndLore(status, "§e/timedharvest status", "§7View detailed status", "§8Opened by: §b" + getPlayerName());
            inventory.setStack(14, status);

            // Enable/Disable
            ItemStack toggle = new ItemStack(worldConfig.enabled ? Items.RED_DYE : Items.LIME_DYE);
            setItemNameAndLore(toggle, worldConfig.enabled ? "§c/timedharvest disable" : "§a/timedharvest enable", worldConfig.enabled ? "§7Disable this world" : "§7Enable this world");
            inventory.setStack(16, toggle);

            // Set as Main World (only for highest-level user)
            if (player.hasPermissionLevel(4)) {
                ItemStack mainWorld = new ItemStack(Items.NETHER_STAR);
                boolean isMain = TimedHarvestMod.getConfig().mainWorldId != null && TimedHarvestMod.getConfig().mainWorldId.equals(worldConfig.worldId);
                setItemNameAndLore(mainWorld,
                    isMain ? "§e§lMain World (Current)" : "§eSet as Main World",
                    isMain ? "§aThis is the current main world for evacuation." : "§7Click to set this world as the main world for evacuation after reset.");
                inventory.setStack(18, mainWorld);
            }

            // Delete
            ItemStack delete = new ItemStack(Items.BARRIER);
            setItemNameAndLore(delete, "§c/timedharvest delete", "§cRemove from configuration");
            inventory.setStack(22, delete);

            // Back button
            ItemStack back = new ItemStack(Items.ARROW);
            setItemNameAndLore(back, "§eBack", "§7Return to dashboard");
            inventory.setStack(26, back);
        }

        private void setItemNameAndLore(ItemStack item, String name, String lore) {
            setItemNameAndLore(item, name, new String[]{lore});
        }

        // Overload to support multiple lore lines
        private void setItemNameAndLore(ItemStack item, String name, String... loreLines) {
            NbtCompound nbt = item.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", Text.Serializer.toJson(Text.literal(name)));
            NbtList loreList = new NbtList();
            for (String line : loreLines) {
                loreList.add(NbtString.of(Text.Serializer.toJson(Text.literal(line))));
            }
            display.put("Lore", loreList);
            nbt.put("display", display);
        }

        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            // Only handle approve/cancel for pending delete
            if (!TimedHarvestMod.getConfig().resourceWorlds.contains(worldConfig)) {
                if (slotIndex == 15 && actionType == SlotActionType.PICKUP) { // APPROVE DELETE
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Are you sure you want to approve delete?", () -> {
                        // Actually delete the world folder
                        java.io.File worldFolder = new java.io.File("saves", worldConfig.worldId);
                        if (worldFolder.exists()) {
                            TimedHarvestMod.deleteWorldFolder(worldFolder);
                        }
                        // Remove from config if present
                        TimedHarvestMod.getConfig().resourceWorlds.remove(worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld files deleted and removed from config."));
                    }, () -> {
                        serverPlayer.sendMessage(Text.literal("§eDelete cancelled."));
                    }));
                    return;
                }
                if (slotIndex == 17 && actionType == SlotActionType.PICKUP) { // CANCEL DELETE
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Are you sure you want to cancel delete?", () -> {
                        // Restore world to config (if not present)
                        if (!TimedHarvestMod.getConfig().resourceWorlds.contains(worldConfig)) {
                            TimedHarvestMod.getConfig().resourceWorlds.add(worldConfig);
                            TimedHarvestMod.getConfig().save();
                        }
                        serverPlayer.sendMessage(Text.literal("§aWorld restored to config."));
                    }, null));
                    return;
                }
            }
            if (slotIndex < 0 || slotIndex >= 27 || actionType != SlotActionType.PICKUP) {
                super.onSlotClick(slotIndex, button, actionType, player);
                return;
            }
            switch (slotIndex) {
                case 24: // Restart World (New Seed)
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Restart world with a new random seed?", () -> {
                        // Set a new random seed and reset
                        worldConfig.seed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
                        ResetScheduler.resetWorld(worldConfig.worldId, worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld restarted with a new random seed."));
                    }, null));
                    break;
                case 20: // Delete World Files (new button)
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Delete world files and remove from config?", () -> {
                        java.io.File worldFolder = new java.io.File("saves", worldConfig.worldId);
                        if (worldFolder.exists()) {
                            TimedHarvestMod.deleteWorldFolder(worldFolder);
                        }
                        TimedHarvestMod.getConfig().resourceWorlds.remove(worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld files deleted and removed from config."));
                    }, null));
                    break;
                case 10: // Reset
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Reset this world?", () -> {
                        ResetScheduler.resetWorld(worldConfig.worldId, worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld reset."));
                    }, null));
                    break;
                case 11: // Reset with New Seed
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Reset with a new random seed?", () -> {
                        worldConfig.seed = com.timedharvest.world.ResourceWorldManager.generateRandomSeed();
                        ResetScheduler.resetWorld(worldConfig.worldId, worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld reset with a new random seed."));
                    }, null));
                    break;
                case 12: // TP
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Teleport to this world?", () -> {
                        com.timedharvest.gui.WorldSelectionGui.teleportToWorld(serverPlayer, worldConfig);
                    }, null));
                    break;
                case 14: // Status
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("View detailed status?", () -> {
                        serverPlayer.sendMessage(Text.literal(worldConfig.toString()));
                    }, null));
                    break;
                case 16: // Enable/Disable
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui((worldConfig.enabled ? "Disable" : "Enable") + " this world?", () -> {
                        worldConfig.enabled = !worldConfig.enabled;
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld " + (worldConfig.enabled ? "enabled" : "disabled") + "."));
                    }, null));
                    break;
                case 18: // Set as Main World (Nether Star)
                    if (serverPlayer.hasPermissionLevel(4)) {
                        serverPlayer.closeHandledScreen();
                        serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Set this world as the main world for evacuation?", () -> {
                            TimedHarvestMod.getConfig().mainWorldId = worldConfig.worldId;
                            TimedHarvestMod.getConfig().save();
                            serverPlayer.sendMessage(Text.literal("§aSet as main world for evacuation."));
                        }, null));
                    }
                    break;
                case 22: // Delete
                    serverPlayer.closeHandledScreen();
                    serverPlayer.openHandledScreen(new ConfirmAndOrCancelGui("Remove from configuration?", () -> {
                        TimedHarvestMod.getConfig().resourceWorlds.remove(worldConfig);
                        TimedHarvestMod.getConfig().save();
                        serverPlayer.sendMessage(Text.literal("§aWorld removed from config."));
                    }, null));
                    break;
                case 26: // Back
                    serverPlayer.closeHandledScreen();
                    // Return to Resource Worlds GUI
                    serverPlayer.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, inv, p) -> new com.timedharvest.gui.WorldSelectionGui(syncId, inv, new net.minecraft.inventory.SimpleInventory(27), serverPlayer),
                        Text.literal("§e§lResource Worlds")
                    ));
                    break;
                default:
                    break;
            }
        }
    }
}