package me.rred.splinter.client.mixin;

import me.rred.splinter.client.rendering.GlobalRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
            shift = At.Shift.AFTER
    ))
    private void afterWorldRender(float delta, long time, MatrixStack matrixStack, CallbackInfo ci) {
        GlobalRenderer.INSTANCE.setMatrixStack(matrixStack);
        GlobalRenderer.INSTANCE.render();
        GlobalRenderer.INSTANCE.clearMatrixStack();
    }
}
