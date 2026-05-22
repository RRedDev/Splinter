package me.rred.splinter.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.rred.splinter.client.EditSession;
import me.rred.splinter.client.SplinterClient;
import me.rred.splinter.client.SplinterStateMachine;
import net.minecraft.client.util.math.MatrixStack;

public class GlobalRenderer {
    public static final GlobalRenderer INSTANCE = new GlobalRenderer();
    private MatrixStack matrixStack = null;

    public void setMatrixStack(MatrixStack value) {
        matrixStack = value;
    }

    public void clearMatrixStack() {
        matrixStack = null;
    }

    public void render() {
        if (matrixStack == null) return;

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.peek().getModel());
        GlStateManager.disableTexture();
        GlStateManager.disableDepthTest();
        GlStateManager.disableCull();

        SplinterClient.routeHandler.render();

        if (SplinterClient.ssm.getState() == SplinterStateMachine.State.EDIT) {
            EditSession session = SplinterClient.ssm.getEditSession();
            if (session != null) session.renderOutlines();
        }

        GlStateManager.enableDepthTest();
        GlStateManager.enableCull();
        GlStateManager.enableTexture();
        RenderSystem.popMatrix();
    }
}
