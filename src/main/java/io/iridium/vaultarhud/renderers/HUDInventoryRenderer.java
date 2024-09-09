package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.VaultarItem;
import io.iridium.vaultarhud.util.Point;
import io.iridium.vaultarhud.util.ScreenValidator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

import static io.iridium.vaultarhud.renderers.HUDInGameRenderer.GetPlayerInventoryItems;
import static io.iridium.vaultarhud.util.SharedFunctions.isMouseOverItem;
import static io.iridium.vaultarhud.util.SharedFunctions.renderBackground;

public class HUDInventoryRenderer {

        private static ResourceLocation hudInventoryTexture = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_inv.png");
        private static ResourceLocation hudSmallInventoryTexture = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_inv_small.png");

        private static Minecraft minecraft = Minecraft.getInstance();


        public static boolean ShowHoverHUD = false;

        public static boolean hasMouseClicked = false;

        public static void render(PoseStack poseStack, Point renderOrigin) {

                // Set the color and enable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                float scale = 1.0F;

                //Define the spacing between each rendered element
                int elementSpacing = Math.round(18 * scale);

                double GUISCALE = minecraft.getWindow().getGuiScale();

                Point offset = ScreenValidator.getScreenHUDCoordinates(minecraft.screen, new Point(32, 28));
                int x = (int) offset.getX();
                int y = (int) offset.getY();



                if (hasMouseClicked && isMouseOverItem(minecraft.mouseHandler.xpos(), minecraft.mouseHandler.ypos(), x * GUISCALE, (ShowHoverHUD ? y-59: y) * GUISCALE, 28, ShowHoverHUD ? 87 : 28, (float) GUISCALE)) {
                        VaultarHud.LOGGER.info("Mouse Clicked on HUD and SHowHoverHUD is " + ShowHoverHUD);
                        hasMouseClicked = false;
                        ShowHoverHUD = !ShowHoverHUD;
                }

                if (ShowHoverHUD) {
                        y -= 59;

                        // Draw the background image
                        renderBackground(poseStack, x, y, 28, 87, hudInventoryTexture);
                        RenderCrystal(poseStack, x + 6, y - 19, scale, true);
                        // Render each item

                        boolean isHovered = isMouseOverItem(minecraft.mouseHandler.xpos(), minecraft.mouseHandler.ypos(), x * GUISCALE, y * GUISCALE, 28, 87, (float) GUISCALE);

                        for (int i = 0; i < VaultarHUDOverlay.vaultarItems.size(); i++) {
                                VaultarItem item = VaultarHUDOverlay.vaultarItems.get(i);

                                List<ItemStack> suitableItems = item.items;

                                int totalSuitableItemsInInventory = 0;
                                Map<Item, Integer> inventoryItems = GetPlayerInventoryItems(minecraft.player);
                                for (ItemStack stack : suitableItems) {
                                        Item key = stack.getItem();
                                        if (inventoryItems.containsKey(key)) {
                                                totalSuitableItemsInInventory += inventoryItems.get(key);
                                        }
                                }

                                RenderInventoryHUDCompositeElement(poseStack, x+6, y + 10 + (elementSpacing * i), item.getCurrentItem(VaultarHUDOverlay.TICKER), item.getCountCompleted(), item.getCountTotal(), totalSuitableItemsInInventory, scale, isHovered);

                        }

                } else {
                        // Draw the background image
                        renderBackground(poseStack, x, y, 28, 28, hudSmallInventoryTexture);
                        RenderAltar(poseStack, x + 6, y + 8, 1.0F);
                        RenderCrystal(poseStack, x + 10, y + 3, 0.5F, false);
                }


                // Reset the color and disable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();


        }


        private static void RenderInventoryHUDCompositeElement(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal, int inventoryTotal, float scale, boolean isHovered){

                poseStack.pushPose();
                poseStack.translate(x, y, 100.0F);
                poseStack.scale(scale, scale, 1.0F);
                poseStack.translate(-x, -y, 0);

                // Draw the item icon

                if (countCompleted == countTotal){
                        itemStack.enchant(Enchantment.byId(1), 1);
                }


                ScalableItemRenderer.render(itemStack, new Point(x, y) , scale);


                poseStack.pushPose();

//                GuiComponent.fill(poseStack, x , y, x+16, y + 16, 0x60FFFFFF);

                poseStack.translate(0, 0, 110F);

                if (isHovered) {
                        String text = itemStack.getHoverName().getString();
                        int nameWidth = minecraft.font.width(text);
                        int xOffset = x - 8 - nameWidth;
                        int pColor = (countCompleted == countTotal) ? 0x00FF00 : 0xFFFFFF;
                        if (xOffset <= 0) {
                                poseStack.pushPose();
                                float scaleF = (float) (x - 8) / nameWidth;
                                poseStack.scale(scaleF, scaleF, 1.0f);
                                minecraft.font.drawShadow(poseStack, new TextComponent(text), (0) / scaleF, (y + 5) / scaleF, pColor);
                                poseStack.popPose();
                        } else {
                                minecraft.font.drawShadow(poseStack, new TextComponent(text), xOffset, y + 5, pColor);
                        }


                }else{
                        String text_count = countCompleted + "/" + countTotal;
                        String text_invCount = inventoryTotal > 0 && countCompleted < countTotal ? " (" + inventoryTotal + ")" : "";

                        int stringWidth = minecraft.font.width(text_count + text_invCount);
                        int xOffset = x - 8 - stringWidth;
                        int pColor = (countCompleted == countTotal) ? 0x00FF00 : 0xFFFFFF;

                        MutableComponent invCountT =  new TextComponent(text_invCount).withStyle(ChatFormatting.GOLD);
                        minecraft.font.drawShadow(poseStack, new TextComponent(text_count).append(invCountT), xOffset, y + 5, pColor);
                }

                poseStack.popPose();

                poseStack.popPose();


        }


        private static final ItemStack crystal = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("the_vault:vault_crystal")));
        private static final ItemStack altar = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("the_vault:vault_altar")));


        public static void RenderCrystal(PoseStack poseStack, int x, int y, float scale, boolean isFloating) {
                poseStack.pushPose();
                poseStack.translate(x, y, 200F);
                poseStack.scale(scale, scale, 1.0F);
                poseStack.translate(-x, -y, 0);

                ScalableItemRenderer.render(crystal, new Point(x, y), scale, isFloating, true, true);

                poseStack.popPose();

        }

        public static void RenderAltar(PoseStack poseStack, int x, int y, float scale) {
                poseStack.pushPose();
                poseStack.translate(x, y, 1F);
                poseStack.scale(scale, scale, 1.0F);
                poseStack.translate(-x, -y, 0);

                ScalableItemRenderer.render(altar, new Point(x, y), scale, false, false, false);

                poseStack.popPose();

        }


}
