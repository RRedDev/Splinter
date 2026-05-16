package me.rred.splinter.mixin;

import me.rred.splinter.client.timer.TimerHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    public abstract TextRenderer getFontRenderer();

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("TAIL"))
    void afterRender(MatrixStack matrixStack, float tickDelta, CallbackInfo ci) {
        if (this.client.options.hudHidden) return;

        TimerHud.render(matrixStack, getFontRenderer());
    }
}
