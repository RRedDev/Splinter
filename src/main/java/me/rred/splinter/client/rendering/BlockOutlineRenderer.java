package me.rred.splinter.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL32;

import java.awt.*;

public class BlockOutlineRenderer {
    private static final float LINE_WIDTH = 2.5f;
    private static final int DRAW_MODE = 1;
    private final Vec3d[] corners;
    private static final int[][] edges = new int[][]{
        {0,1},{1,2},{2,3},{3,0}, // bottom
        {4,5},{5,6},{6,7},{7,4}, // top
        {0,4},{1,5},{2,6},{3,7}  // verticals
    };
    private Color color;

    public BlockOutlineRenderer(BlockPos blockPos, Color color, float padding) {
        this.color = color;
        this.corners = new Vec3d[8];
        Vec3d center = blockToVec(blockPos);

        double minX = center.getX() - 0.5 - padding;
        double maxX = center.getX() + 0.5 + padding;
        double minY = center.getY() - 0.5 - padding;
        double maxY = center.getY() + 0.5 + padding;
        double minZ = center.getZ() - 0.5 - padding;
        double maxZ = center.getZ() + 0.5 + padding;

        corners[0] = new Vec3d(minX, minY, minZ);
        corners[1] = new Vec3d(maxX, minY, minZ);
        corners[2] = new Vec3d(maxX, minY, maxZ);
        corners[3] = new Vec3d(minX, minY, maxZ);
        corners[4] = new Vec3d(minX, maxY, minZ);
        corners[5] = new Vec3d(maxX, maxY, minZ);
        corners[6] = new Vec3d(maxX, maxY, maxZ);
        corners[7] = new Vec3d(minX, maxY, maxZ);
    }

    public BlockOutlineRenderer(BlockPos blockPos, Color color) {
        this(blockPos, color, 0f);
    }


    public void setColor(Color value) {
        color = value;
    }

    public void render() {
        enableTransparency();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.lineWidth(LINE_WIDTH);
        buffer.begin(DRAW_MODE, VertexFormats.POSITION_COLOR);

        for (int[] edge : edges) {
            addVertex(buffer, corners[edge[0]], color);
            addVertex(buffer, corners[edge[1]], color);
        }

        tessellator.draw();
        disableTransparency();
    }

    /*
    public void render() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.lineWidth(5.0f);
        buffer.begin(1, VertexFormats.POSITION_COLOR);

        // Draw a single hardcoded line at world origin
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        buffer.vertex(0 - cam.x, 65 - cam.y, 0 - cam.z).color(255, 0, 0, 255).next();
        buffer.vertex(10 - cam.x, 65 - cam.y, 0 - cam.z).color(255, 0, 0, 255).next();

        tessellator.draw();
    }
    */

    private Vec3d blockToVec(@NotNull BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.5, 0.5);
    }

    private void addVertex(@NotNull BufferBuilder buffer, @NotNull Vec3d pos, Color color) {
        Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        float[] comps = color.getComponents(null);
        buffer.vertex(
                pos.x - cam.x, pos.y - cam.y, pos.z - cam.z
        ).color(
                comps[0], comps[1], comps[2], comps[3]
        ).next();
    }

    private void enableTransparency() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void disableTransparency() {
        RenderSystem.disableBlend();
    }
}

