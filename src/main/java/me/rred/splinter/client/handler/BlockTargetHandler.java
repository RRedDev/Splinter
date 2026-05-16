package me.rred.splinter.client.handler;

import me.rred.splinter.Splinter;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.rendering.BlockOutlineRenderer;
import net.minecraft.MinecraftVersion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BlockTargetHandler {

    public static void toggleOutline() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.crosshairTarget instanceof BlockHitResult hit) {
            BlockPos pos = hit.getBlockPos();

            // if the targeted block is an air block, then return
            Block block = client.world.getBlockState(pos).getBlock();
            if (block == Blocks.AIR) return;

            SplinterClient.tsm.onBlockSelected(pos);
        }
    }

    public static void onBlockBroken(BlockPos pos) {
        SplinterClient.tsm.onBlockBroken(pos);
    }

    public static void render() {
        BlockPos start = SplinterClient.tsm.getStartPos();
        BlockPos end = SplinterClient.tsm.getEndPos();

        if (start != null) {
            new BlockOutlineRenderer(start, Color.GREEN).render();
        }
        if (end != null) {
            new BlockOutlineRenderer(end, Color.RED).render();
        }

    }
}
