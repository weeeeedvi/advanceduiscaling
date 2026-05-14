package net.rosemods.betteruiscale.mixin;

import net.minecraft.client.util.Window;
import net.rosemods.betteruiscale.ScaleFactorUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Window.class, priority = 1500)
public abstract class MixinWindow {
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;
    @Shadow private double scaleFactor;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    /**
     * @author Rose, Qendolin
     * @reason Redirects scale calculation to ScaleFactorUtil using Inject for better compatibility.
     */
    @Inject(method = "calculateScaleFactor", at = @At("HEAD"), cancellable = true)
    private void onCalculateScaleFactor(int guiScale, boolean forceUnicodeFont, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ScaleFactorUtil.calcScaleFactor(guiScale, forceUnicodeFont, this.framebufferWidth, this.framebufferHeight));
    }

    /**
     * @author Rose, Qendolin
     * @reason Updates scale factor fields while remaining compatible with other rendering mods.
     */
    @Inject(method = "setScaleFactor", at = @At("HEAD"), cancellable = true)
    private void onSetScaleFactor(double internalScaleFactor, CallbackInfo ci) {
        this.scaleFactor = ScaleFactorUtil.fromInternalScaleFactor(internalScaleFactor);
        this.scaledWidth = ScaleFactorUtil.scaleInternal(this.framebufferWidth, internalScaleFactor);
        this.scaledHeight = ScaleFactorUtil.scaleInternal(this.framebufferHeight, internalScaleFactor);
        
        // Cancel the original method so it doesn't overwrite our custom values
        ci.cancel();
    }
}