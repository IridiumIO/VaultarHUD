package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.VaultarItem;
import io.iridium.vaultarhud.util.Point;
import io.iridium.vaultarhud.util.ScreenValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import static io.iridium.vaultarhud.util.SharedFunctions.isMouseOverItem;
import static io.iridium.vaultarhud.util.SharedFunctions.renderBackground;

public class HUDInventoryRenderer implements IVaultarHUDRenderer{

        private boolean renderCrystal = true;
        private static ResourceLocation hudInventoryTexture = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_inv.png");

        private static Minecraft minecraft = Minecraft.getInstance();

        public HUDInventoryRenderer(boolean renderCrystal) {
            this.renderCrystal = renderCrystal;
        }

        @Override
        public void render(PoseStack poseStack, Point renderOrigin) {

                // Set the color and enable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                float scale = 1.0F;

                //Define the spacing between each rendered element
                int elementSpacing = Math.round(18 * scale);

                // Calculate starting coordinates for rendering
                Point offset = ScreenValidator.getScreenHUDCoordinates(minecraft.screen);
                int x = (int) offset.getX();
                int y = (int) offset.getY();

                // Draw the background image
                renderBackground(poseStack, x, y, 28, 87, hudInventoryTexture);


                if (renderCrystal) {
                        VaultarHUDOverlay.RenderCrystal(poseStack, x + 6, y - 21, scale);
                }

                // Render each item
                for (int i = 0; i < VaultarHUDOverlay.vaultarItems.size(); i++) {
                        VaultarItem item = VaultarHUDOverlay.vaultarItems.get(i);
                        RenderInventoryHUDCompositeElement(poseStack, x+6, y + 10 + (elementSpacing * i), item.getCurrentItem(VaultarHUDOverlay.TICKER), item.getCountCompleted(), item.getCountTotal(), scale);
                }

                // Reset the color and disable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();


        }


        private static void RenderInventoryHUDCompositeElement(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal, float scale){

                poseStack.pushPose();
                poseStack.translate(x, y, 100.0F);
                poseStack.scale(scale, scale, 1.0F);
                poseStack.translate(-x, -y, 0);

                // Draw the item icon

                if (countCompleted == countTotal){
                        itemStack.enchant(Enchantment.byId(1), 1);
                }



                double guiScale = minecraft.getWindow().getGuiScale();

                ScalableItemRenderer.render(itemStack, new Point(x, y) , scale, false);

                MouseHandler mouseHandler = minecraft.mouseHandler;
                double mouseX = (double)mouseHandler.xpos();
                double mouseY = (double)mouseHandler.ypos();
                if(isMouseOverItem(mouseX, mouseY, x * guiScale, y * guiScale, (float) guiScale)){

                        poseStack.pushPose();

                        GuiComponent.fill(poseStack, x , y, x+16, y + 16, 0x60FFFFFF);

                        poseStack.translate(0, 0, 110F);
                        String text_count = countCompleted + "/" + countTotal;
                        int stringWidth = minecraft.font.width(text_count);
                        int xOffset = x - 8 - stringWidth;

                        int pColor = (countCompleted == countTotal) ? 0x00FF00 : 0xFFFFFF;

                        minecraft.font.drawShadow(poseStack, new TextComponent(text_count), xOffset, y + 5, pColor);
                        poseStack.popPose();

                }


                // Center the item count text and draw it and the progress bar



                poseStack.popPose();


        }


}
