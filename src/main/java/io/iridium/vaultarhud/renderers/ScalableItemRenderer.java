package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.iridium.vaultarhud.util.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import static io.iridium.vaultarhud.util.SharedFunctions.getBakedModel;

public class ScalableItemRenderer {

        private static Minecraft minecraft = Minecraft.getInstance();

        public static void render(ItemStack itemStack, Point renderOrigin, float scale, boolean isAnimated) {
                ItemRenderer renderer = minecraft.getItemRenderer();

                BakedModel pBakedModel = getBakedModel(itemStack);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.pushPose();

                int x = (int) renderOrigin.getX();
                int y = (int) renderOrigin.getY();

                if (isAnimated) {
                        // Add a sine wave function to the y coordinate to create an up and down motion
                        y += Math.sin(System.currentTimeMillis() % 4000.0 / 4000.0 * 2.0 * Math.PI) * 5.0;  // Change 10.0 to control the height of the motion
                }

                poseStack.translate(x , y, 300.0F);
                poseStack.translate(8.0D * scale, 8.0D * scale, 0.0D);
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale);
                RenderSystem.applyModelViewMatrix();

                PoseStack blockPoseStack = new PoseStack();
                blockPoseStack.pushPose();


                if (isAnimated){
                        float angle = System.currentTimeMillis() / 20 % 340;
                        Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
                        blockPoseStack.mulPose(rotation);
                }


                MultiBufferSource.BufferSource multibuffersource$buffersource = minecraft.renderBuffers().bufferSource();
                boolean flag = !pBakedModel.usesBlockLight();
                if (flag) {
                        Lighting.setupForFlatItems();
                }

                if (isAnimated){
                        itemStack.enchant(Enchantment.byId(1), 1);
                }

                renderer.render(itemStack, ItemTransforms.TransformType.GUI, false, blockPoseStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, pBakedModel);
                multibuffersource$buffersource.endBatch();
                RenderSystem.enableDepthTest();
                if (flag) {
                        Lighting.setupFor3DItems();
                }
                blockPoseStack.popPose();

                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();

        }
}