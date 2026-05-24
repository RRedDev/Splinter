package me.rred.splinter.mixin;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {
    @Inject(method = "updateScore", at = @At("HEAD"))
    private void onScoreUpdate(ScoreboardPlayerScore score, CallbackInfo ci) {
        if (!score.getObjective().getName().equals("timer.temp")) return;
        if (!score.getPlayerName().equals("tick")) return;

        int value = score.getScore();
        SplinterClient.routeHandler.onMapTickUpdated(value);

        boolean inMap = value > 0;
        if (inMap != SplinterClient.ssm.isInMap()) {
            SplinterClient.ssm.setInMap(inMap);
        }
    }

}