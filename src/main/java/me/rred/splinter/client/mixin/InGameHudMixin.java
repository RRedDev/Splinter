package me.rred.splinter.client.mixin;

import me.rred.splinter.client.EditSession;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
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

        String stateText = SplinterClient.ssm.getState().toString();
        String setText = SplinterClient.setManager.getActiveSet().getName();
        if (!stateText.equals("IDLE")) {
            int color = (stateText.equals("ACTIVE")) ? 0x55FF55 : 0xFFAA00;
            String setLabel = setText + " - ";
            int setTextWidth = getFontRenderer().getWidth(setLabel);
            getFontRenderer().drawWithShadow(matrixStack, setLabel, 10, 10, 0xFFFFFF);
            getFontRenderer().drawWithShadow(matrixStack, stateText, 10 + setTextWidth, 10, color);
            if (stateText.equals("EDIT")) {
                EditSession session = SplinterClient.ssm.getEditSession();
                if (session != null) {
                    session.renderHud(matrixStack, getFontRenderer());
                }
            }
        }

    }
}
