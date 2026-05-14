package net.rosemods.betteruiscale.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VideoOptionsScreen.class, priority = 1500)
public abstract class MixinVideoOptionsScreen extends GameOptionsScreen {
    @Unique
    private int prevGuiScale = 0;

    public MixinVideoOptionsScreen(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    /**
     * Instead of @Redirect, which can be brittle with VulkanMod/Sodium, 
     * we use a HEAD injection to capture the state.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void captureGuiScale(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.gameOptions != null) {
            this.prevGuiScale = this.gameOptions.getGuiScale().getValue();
        }
    }

    /**
     * In 1.21.1, we manually trigger the resolution update only when the scale actually changes.
     * This avoids the "double-flash" or crashes when VulkanMod tries to re-initialize the swapchain.
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean result = super.mouseReleased(mouseX, mouseY, button);
        
        if (this.client != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            int currentScale = this.gameOptions.getGuiScale().getValue();
            if (currentScale != prevGuiScale) {
                // Manually trigger the update that we prevented earlier
                this.client.onResolutionChanged();
            }
        }
        return result;
    }
    
    /**
     * Optional: Fix for keyboard controls (Enter/Space) changing the scale.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            this.prevGuiScale = this.gameOptions.getGuiScale().getValue();
        }
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        
        if (this.client != null && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE)) {
            if (this.gameOptions.getGuiScale().getValue() != prevGuiScale) {
                this.client.onResolutionChanged();
            }
        }
        return result;
    }
}