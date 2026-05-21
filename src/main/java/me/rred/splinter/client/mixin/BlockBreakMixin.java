package me.rred.splinter.client.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class BlockBreakMixin {
    @Inject(method = "updateListeners", at = @At("HEAD"))
    public void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        if (newState.isAir() && !oldState.isAir()) {
//            BlockTargetHandler.onBlockBroken(pos);
//            replace with RouteHandler eventually
        }
    }
}
