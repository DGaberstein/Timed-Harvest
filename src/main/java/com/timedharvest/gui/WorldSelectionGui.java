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
import java.util.List;

/**
 * GUI for selecting and teleporting to resource worlds.
 */
public class WorldSelectionGui extends ScreenHandler {
    private final Inventory inventory;
    private final List<ModConfig.ResourceWorldConfig> enabledWorlds;
    private int currentPage = 0;
    private static final int WORLDS_PER_PAGE = 9; // One row of worlds

    public WorldSelectionGui(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(27), (ServerPlayerEntity) playerInventory.player);
    }

    public WorldSelectionGui(int syncId, PlayerInventory playerInventory, Inventory inventory, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, syncId);
        this.inventory = inventory;
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
        
        // Add "Return to Spawn" button at bottom left (slot 18)
        ItemStack spawnItem = new ItemStack(Items.RED_BED);
        NbtCompound nbt = spawnItem.getOrCreateNbt();
        NbtCompound display = new NbtCompound();
        display.putString("Name", Text.Serializer.toJson(Text.literal("Return to Spawn").formatted(Formatting.GREEN)));
        
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Click to teleport to").formatted(Formatting.GRAY))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("the overworld spawn").formatted(Formatting.GRAY))));
        
        display.put("Lore", lore);
        nbt.put("display", display);
        
        inventory.setStack(18, spawnItem);
        
        // Add Previous Page button at bottom left corner if not on first page (slot 19)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(Items.COMMAND_BLOCK);
            NbtCompound prevNbt = prevButton.getOrCreateNbt();
            NbtCompound prevDisplay = new NbtCompound();
            prevDisplay.putString("Name", Text.Serializer.toJson(Text.literal("◀ Previous Page").formatted(Formatting.YELLOW)));
            
            NbtList prevLore = new NbtList();
            prevLore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Page " + currentPage + "/" + totalPages).formatted(Formatting.GRAY))));
            
            prevDisplay.put("Lore", prevLore);
            prevNbt.put("display", prevDisplay);
            
            inventory.setStack(19, prevButton);
        }
        
        // Add Next Page button at bottom right corner if there are more pages (slot 25)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Items.COMMAND_BLOCK);
            NbtCompound nextNbt = nextButton.getOrCreateNbt();
            NbtCompound nextDisplay = new NbtCompound();
            nextDisplay.putString("Name", Text.Serializer.toJson(Text.literal("Next Page ▶").formatted(Formatting.YELLOW)));
            
            NbtList nextLore = new NbtList();
            nextLore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Page " + (currentPage + 2) + "/" + totalPages).formatted(Formatting.GRAY))));
            
            nextDisplay.put("Lore", nextLore);
            nextNbt.put("display", nextDisplay);
            
            inventory.setStack(25, nextButton);
        }
        
        // Add help/info command block at bottom right (slot 26)
        ItemStack infoBlock = new ItemStack(Items.COMMAND_BLOCK);
        NbtCompound infoNbt = infoBlock.getOrCreateNbt();
        NbtCompound infoDisplay = new NbtCompound();
        infoDisplay.putString("Name", Text.Serializer.toJson(Text.literal("Admin Commands").formatted(Formatting.GOLD)));
        
        NbtList infoLore = new NbtList();
        infoLore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Use: /timedharvest").formatted(Formatting.GRAY))));
        infoLore.add(NbtString.of(Text.Serializer.toJson(Text.literal("For admin commands").formatted(Formatting.GRAY))));
        
        infoDisplay.put("Lore", infoLore);
        infoNbt.put("display", infoDisplay);
        
        inventory.setStack(26, infoBlock);
    }

    private ItemStack createWorldItem(ModConfig.ResourceWorldConfig worldConfig) {
        // Choose icon based on world type
        ItemStack item;
        if (worldConfig.worldType.contains("nether")) {
            item = new ItemStack(Items.NETHERRACK);
        } else if (worldConfig.worldType.contains("end")) {
            item = new ItemStack(Items.END_STONE);
        } else {
            item = new ItemStack(Items.GRASS_BLOCK);
        }
        
        // Set custom name using NBT
        NbtCompound nbt = item.getOrCreateNbt();
        NbtCompound display = new NbtCompound();
        display.putString("Name", Text.Serializer.toJson(Text.literal(worldConfig.worldId).formatted(Formatting.YELLOW)));
        
        // Add lore
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("World Type: " + worldConfig.worldType).formatted(Formatting.GRAY))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Reset: " + worldConfig.resetIntervalHours + " hours").formatted(Formatting.GRAY))));
        
        if (worldConfig.worldBorderSize > 0) {
            lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Border: " + worldConfig.worldBorderSize + " blocks").formatted(Formatting.GRAY))));
        }
        
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal(""))));
        lore.add(NbtString.of(Text.Serializer.toJson(Text.literal("Click to teleport!").formatted(Formatting.GREEN))));
        
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
        
        // Check if it's the spawn button (slot 18)
        if (slotIndex == 18 && clickedItem.getItem() == Items.RED_BED) {
            teleportToSpawn(serverPlayer);
            serverPlayer.closeHandledScreen();
            return;
        }
        
        // Check if it's the Previous Page button (slot 19)
        if (slotIndex == 19 && clickedItem.getItem() == Items.COMMAND_BLOCK) {
            if (currentPage > 0) {
                currentPage--;
                populateInventory();
            }
            return;
        }
        
        // Check if it's the Next Page button (slot 25)
        if (slotIndex == 25 && clickedItem.getItem() == Items.COMMAND_BLOCK) {
            int totalPages = (int) Math.ceil((double) enabledWorlds.size() / WORLDS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                populateInventory();
            }
            return;
        }
        
        // Check if it's the info command block (slot 26)
        if (slotIndex == 26 && clickedItem.getItem() == Items.COMMAND_BLOCK) {
            serverPlayer.closeHandledScreen();
            serverPlayer.sendMessage(Text.literal("§6Use §e/timedharvest §6for admin commands!"));
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
        RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, 
            new Identifier(worldConfig.dimensionName));
        
        ServerWorld targetWorld = player.getServer().getWorld(dimensionKey);
        if (targetWorld == null) {
            player.sendMessage(Text.literal("§cDimension '" + worldConfig.dimensionName + "' does not exist!"));
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
        player.sendMessage(Text.literal("§aTeleported to " + worldConfig.worldId + "!"));
    }

    private void teleportToSpawn(ServerPlayerEntity player) {
        ServerWorld overworld = player.getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        player.sendMessage(Text.literal("§aTeleported to spawn!"));
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
