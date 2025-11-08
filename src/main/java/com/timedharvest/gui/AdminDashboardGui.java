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
        
        // World list (rows 1-4, slots 9-44)
        int startIndex = currentPage * WORLDS_PER_PAGE;
        int endIndex = Math.min(startIndex + WORLDS_PER_PAGE, totalWorlds);
        
        int slot = 10; // Start at slot 10 (row 2, col 2) for better layout
        for (int i = startIndex; i < endIndex; i++) {
            ModConfig.ResourceWorldConfig worldConfig = worlds.get(i);
            ItemStack worldItem = createWorldManagementItem(worldConfig);
            inventory.setStack(slot, worldItem);
            slot += 2; // Space out items for better visibility
            
            // Move to next row after 4 items
            if (slot % 9 >= 8) {
                slot = slot - (slot % 9) + 9 + 1;
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
            "§7Use command:",
            "§6/timedharvest create",
            "§7<worldId> <dimension> <hours>");
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
        
        // Close/Back button
        ItemStack closeItem = new ItemStack(Items.BARRIER);
        setItemNameAndLore(closeItem, "§c§lClose Dashboard", 
            "§7Return to world selector");
        inventory.setStack(53, closeItem);
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
        display.putString("Name", Text.Serializer.toJson(Text.literal(status + " §e§l" + worldConfig.worldId)));
        
        // Lore with details
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("§7▬▬▬▬▬▬▬▬▬▬▬▬▬▬").formatted(Formatting.DARK_GRAY))));
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
        if (slotIndex < 0 || slotIndex >= inventory.size()) {
            return;
        }
        
        ItemStack clickedItem = inventory.getStack(slotIndex);
        if (clickedItem == null || clickedItem.isEmpty()) {
            return;
        }
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        // Handle different button clicks
        switch (slotIndex) {
            case 45: // Reload Config
                handleReloadConfig(serverPlayer);
                break;
            case 46: // Create World
                handleCreateWorld(serverPlayer);
                break;
            case 47: // Help
                handleHelp(serverPlayer);
                break;
            case 48: // Previous Page
                if (currentPage > 0) {
                    currentPage--;
                    populateInventory();
                }
                break;
            case 50: // Next Page
                List<ModConfig.ResourceWorldConfig> worlds = TimedHarvestMod.getConfig().resourceWorlds;
                int totalPages = (int) Math.ceil((double) worlds.size() / WORLDS_PER_PAGE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    populateInventory();
                }
                break;
            case 53: // Close
                serverPlayer.closeHandledScreen();
                // Optionally reopen world selector
                break;
            default:
                // Check if it's a world management item
                handleWorldClick(serverPlayer, slotIndex, button);
                break;
        }
    }

    private void handleWorldClick(ServerPlayerEntity player, int slotIndex, int button) {
        // World items are in slots 10-44 area, spaced out
        List<ModConfig.ResourceWorldConfig> worlds = TimedHarvestMod.getConfig().resourceWorlds;
        int startIndex = currentPage * WORLDS_PER_PAGE;
        
        // Calculate which world this slot represents
        int worldIndex = getWorldIndexFromSlot(slotIndex, startIndex);
        if (worldIndex >= 0 && worldIndex < worlds.size()) {
            ModConfig.ResourceWorldConfig worldConfig = worlds.get(worldIndex);
            
            if (button == 0) { // Left click - show management options
                player.closeHandledScreen();
                showWorldManagementOptions(player, worldConfig);
            } else if (button == 1) { // Right click - toggle enabled/disabled
                worldConfig.enabled = !worldConfig.enabled;
                TimedHarvestMod.getConfig().save();
                populateInventory();
                
                String status = worldConfig.enabled ? "§a§lENABLED" : "§c§lDISABLED";
                player.sendMessage(Text.literal("§6World '§e§l" + worldConfig.worldId + "§6' is now " + status));
            }
        }
    }

    private int getWorldIndexFromSlot(int slot, int startIndex) {
        // Worlds start at slot 10 and are spaced 2 slots apart
        // Pattern: 10, 12, 14, 16 (row 2)
        //          19, 21, 23, 25 (row 3)
        //          etc.
        
        if (slot < 10 || slot > 44) return -1;
        
        int row = (slot - 10) / 9;
        int col = (slot - 10) % 9;
        
        // Only even columns starting from 1
        if (col < 1 || col > 7 || col % 2 == 0) return -1;
        
        int indexInRow = (col - 1) / 2;
        int worldIndex = startIndex + (row * 4) + indexInRow;
        
        return worldIndex;
    }

    private void showWorldManagementOptions(ServerPlayerEntity player, ModConfig.ResourceWorldConfig worldConfig) {
        player.sendMessage(Text.literal("§6§l▬▬▬▬▬ §e§lWorld: " + worldConfig.worldId + " §6§l▬▬▬▬▬"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§e§lAvailable Commands:"));
        player.sendMessage(Text.literal("  §a● §6/timedharvest reset §e" + worldConfig.worldId));
        player.sendMessage(Text.literal("    §7→ Manually reset this world"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §a● §6/timedharvest tp §e" + worldConfig.worldId));
        player.sendMessage(Text.literal("    §7→ Teleport to this world"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §a● §6/timedharvest status §e" + worldConfig.worldId));
        player.sendMessage(Text.literal("    §7→ View detailed status"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §a● §6/timedharvest " + (worldConfig.enabled ? "disable" : "enable") + " §e" + worldConfig.worldId));
        player.sendMessage(Text.literal("    §7→ " + (worldConfig.enabled ? "Disable" : "Enable") + " this world"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §c● §6/timedharvest delete §e" + worldConfig.worldId));
        player.sendMessage(Text.literal("    §7→ §cRemove from configuration"));
        player.sendMessage(Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }

    private void handleReloadConfig(ServerPlayerEntity player) {
        player.closeHandledScreen();
        TimedHarvestMod.reloadConfig();
        player.sendMessage(Text.literal("§a§l✓ §aConfiguration reloaded successfully!"));
    }

    private void handleCreateWorld(ServerPlayerEntity player) {
        player.closeHandledScreen();
        player.sendMessage(Text.literal("§6§l▬▬▬▬ §e§lCreate World Command §6§l▬▬▬▬"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§e§lUsage:"));
        player.sendMessage(Text.literal("  §6/timedharvest create §7<worldId> <dimension> <hours>"));
        player.sendMessage(Text.literal("                       §7[type] [seed] [border] [structures]"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§e§lExamples:"));
        player.sendMessage(Text.literal("  §7Nether: §f/th create §enether §atimed_harvest:nether §624"));
        player.sendMessage(Text.literal("           §f          §e...... §aminecraft:the_nether §624"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §7End: §f/th create §eend §atimed_harvest:end §648"));
        player.sendMessage(Text.literal("        §f          §e... §aminecraft:the_end §648"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("  §7Mining: §f/th create §emining §atimed_harvest:mining §612"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§8Tip: See §7/timedharvest help §8for more info"));
        player.sendMessage(Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }

    private void handleHelp(ServerPlayerEntity player) {
        player.closeHandledScreen();
        player.sendMessage(Text.literal("§6§l▬▬▬▬▬▬▬ §e§lTimed Harvest Commands §6§l▬▬▬▬▬▬▬"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§6§l● §eWorld Management:"));
        player.sendMessage(Text.literal("  §6/th admin §8- §fOpen this dashboard"));
        player.sendMessage(Text.literal("  §6/timedharvest reset §7<worldId> §8- §fManually reset a world"));
        player.sendMessage(Text.literal("  §6/timedharvest status §7[worldId] §8- §fShow reset status"));
        player.sendMessage(Text.literal("  §6/timedharvest tp §7<worldId> §8- §fTeleport to world"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§6§l● §eConfiguration:"));
        player.sendMessage(Text.literal("  §6/timedharvest reload §8- §fReload configuration"));
        player.sendMessage(Text.literal("  §6/timedharvest enable §7<worldId> §8- §fEnable a world"));
        player.sendMessage(Text.literal("  §6/timedharvest disable §7<worldId> §8- §fDisable a world"));
        player.sendMessage(Text.literal("  §6/timedharvest delete §7<worldId> §8- §fDelete from config"));
        player.sendMessage(Text.literal(""));
        player.sendMessage(Text.literal("§6§l● §eHelp:"));
        player.sendMessage(Text.literal("  §6/timedharvest help troubleshooting §8- §fCommon fixes"));
        player.sendMessage(Text.literal("§6§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY; // Disable shift-clicking
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.hasPermissionLevel(2); // Admin only
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
    }
}
