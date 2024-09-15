package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.iridium.vaultarhud.VaultarHud;
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
        private static MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        private static PoseStack blockPoseStack = new PoseStack();

        public static void render(ItemStack itemStack, Point renderOrigin, float scale){
                render(itemStack, renderOrigin, scale, false, false, false);
        }

        public static void render(ItemStack itemStack, Point renderOrigin, float scale, boolean isFloating, boolean isSpinning, boolean isShiny) {
                ItemRenderer renderer = minecraft.getItemRenderer();
                BakedModel pBakedModel = getBakedModel(itemStack, renderer);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.pushPose();

                int x = (int) renderOrigin.getX();
                double y = renderOrigin.getY();

                if (isFloating) {
                        // Add a sine wave function to the y coordinate to create an up and down motion
                        y += Math.sin(System.currentTimeMillis() % 4000.0 / 4000.0 * 2.0 * Math.PI) * 3.0;  // Change 5.0 to control the height of the motion
                }


                poseStack.translate(x , y, 300);
                poseStack.translate(8.0D * scale, 8.0D * scale, 0.0D);
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale);
                RenderSystem.applyModelViewMatrix();

                blockPoseStack.pushPose();


                if (isSpinning){
                        float angle = (float) (System.currentTimeMillis() / 15.0 % 340.0);
                        Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
                        blockPoseStack.mulPose(rotation);
                }


                boolean flag = !pBakedModel.usesBlockLight();
                if (flag) {
                        Lighting.setupForFlatItems();
                }

                if (isShiny){
                        itemStack.enchant(Enchantment.byId(1), 1);
                }

                renderer.render(itemStack, ItemTransforms.TransformType.GUI, false, blockPoseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, pBakedModel);
                bufferSource.endBatch();
                RenderSystem.enableDepthTest();
                if (flag) {
                        Lighting.setupFor3DItems();
                }
                blockPoseStack.popPose();
                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();

                // Reset buffer source state
                bufferSource = minecraft.renderBuffers().bufferSource();
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();

        }
}
