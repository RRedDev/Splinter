package me.rred.splinter.client.handler;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import net.minecraft.MinecraftVersion;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BlockTargetHandler {
    private static BlockOutlineRenderer renderer = null;

    public static void toggleOutline() {
        MinecraftClient client = MinecraftClient.getInstance();

        // If already highlighted, clear it
        if (renderer != null) {
            renderer = null;
            return;
        }

        // Otherwise highlight targeted block
        if (client.crosshairTarget instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();
            Splinter.LOGGER.info("Creating outline at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            renderer = new BlockOutlineRenderer(hit.getBlockPos(), Color.WHITE);
        }
    }

    public static void render() {
        if (renderer != null) {
            renderer.render();
        }
    }
}
