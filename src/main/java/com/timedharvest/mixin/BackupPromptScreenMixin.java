package com.timedharvest.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackupPromptScreen.class)
public class BackupPromptScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // Immediately skip this screen and go to the parent
        MinecraftClient client = MinecraftClient.getInstance();
        Screen parent = this.getParent();
        if (client != null && parent != null) {
            client.setScreen(parent);
            ci.cancel();
        }
    }

    // Helper to get the parent screen (reflection, as field is private)
    private Screen getParent() {
        try {
            java.lang.reflect.Field parentField = BackupPromptScreen.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            return (Screen) parentField.get(this);
        } catch (Exception e) {
            return null;
        }
    }
}
