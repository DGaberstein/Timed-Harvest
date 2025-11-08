package com.timedharvest.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ConfirmAndOrCancelGui implements NamedScreenHandlerFactory {
    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmAndOrCancelGui(String message, Runnable onConfirm, Runnable onCancel) {
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Confirmation");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, net.minecraft.entity.player.PlayerEntity player) {
        return new ConfirmAndOrCancelGuiHandler(syncId, playerInventory, message, onConfirm, onCancel);
    }

    // Inner handler class for the actual GUI logic
    public static class ConfirmAndOrCancelGuiHandler extends ScreenHandler {
    private final Inventory inventory;
    private final Runnable onConfirm;
    private final Runnable onCancel;

        public ConfirmAndOrCancelGuiHandler(int syncId, PlayerInventory playerInventory, String message, Runnable onConfirm, Runnable onCancel) {
            super(ScreenHandlerType.GENERIC_9X1, syncId);
            this.inventory = new SimpleInventory(9);
            this.onConfirm = onConfirm;
            this.onCancel = onCancel;
            // Add slots
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col, 8 + col * 18, 18));
            }
            // Center message (slot 2-6)
            ItemStack msg = new ItemStack(Items.PAPER);
            setItemNameAndLore(msg, "§eConfirm Action", message, "", "§7Click §aCONFIRM §7or §cCANCEL");
            inventory.setStack(2, msg);
            // Confirm button (slot 4)
            ItemStack confirm = new ItemStack(Items.LIME_CONCRETE);
            setItemNameAndLore(confirm, "§aCONFIRM", "§7Confirm this action");
            inventory.setStack(4, confirm);
            // Cancel button (slot 6)
            ItemStack cancel = new ItemStack(Items.RED_CONCRETE);
            setItemNameAndLore(cancel, "§cCANCEL", "§7Cancel this action");
            inventory.setStack(6, cancel);
        }

        @Override
        public boolean canUse(net.minecraft.entity.player.PlayerEntity player) { return true; }
        @Override
        public ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int index) { return ItemStack.EMPTY; }
        @Override
        public void onSlotClick(int slotIndex, int button, SlotActionType actionType, net.minecraft.entity.player.PlayerEntity player) {
            if (actionType != SlotActionType.PICKUP) return;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (slotIndex == 4) { // Confirm
                serverPlayer.closeHandledScreen();
                if (onConfirm != null) onConfirm.run();
            } else if (slotIndex == 6) { // Cancel
                serverPlayer.closeHandledScreen();
                if (onCancel != null) onCancel.run();
            }
        }
        private void setItemNameAndLore(ItemStack item, String name, String... loreLines) {
            NbtCompound nbt = item.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", net.minecraft.text.Text.Serializer.toJson(net.minecraft.text.Text.literal(name)));
            if (loreLines.length > 0) {
                NbtList lore = new NbtList();
                for (String line : loreLines) {
                    lore.add(NbtString.of(net.minecraft.text.Text.Serializer.toJson(net.minecraft.text.Text.literal(line))));
                }
                display.put("Lore", lore);
            }
            nbt.put("display", display);
        }
    }
}
