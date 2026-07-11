package io.iridium.vaultarhud.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.VaultarItem;
import io.iridium.vaultarhud.util.Point;
import io.iridium.vaultarhud.util.SharedFunctions;
import iskallia.vault.client.data.ClientPartyData;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.iridium.vaultarhud.util.SharedFunctions.*;

public class HUDInGameRenderer{

        private static Minecraft minecraft = Minecraft.getInstance();

        private static final ResourceLocation HUD_MAIN_TEX = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_main.png");
        private static final ResourceLocation HUD_MAIN_DARK_TEX = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_main_dark.png");


    public static ResourceLocation getHudTexture() {
                return SharedFunctions.isDarkModeEnabled() ? HUD_MAIN_DARK_TEX : HUD_MAIN_TEX;
        }

        public static void render(PoseStack poseStack, Point origin) {

                // Set the color and enable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();

                float scale = 1.0F;

                //Define the spacing between each rendered element
                int elementSpacing = Math.round(42 * scale);

                // Calculate starting coordinates for rendering; in this case, on the right side of the screen centered vertically when outside the inventory, or on the left side when inside the inventory (origin.X = -1)
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                int screenHeight = minecraft.getWindow().getGuiScaledHeight();
                double x = (origin.getX() == -1)? screenWidth - Math.round(135 * scale) : origin.getX();

                boolean isLeftSideRender = origin.getX() == 2;

                LocalPlayer player = minecraft.player;


                 Map<Item, Integer> inventoryItems = GetPlayerInventoryItems(player, 200);


                int itemsToRender = (int) VaultarHUDOverlay.vaultarItems.stream()
                        .filter(item -> item.getCountCompleted() != item.getCountTotal())
                        .count();

                int y = CalculateYOffsetIfPartyVisible(screenHeight, elementSpacing, isLeftSideRender, itemsToRender);


                // Render each item
                for (VaultarItem item : VaultarHUDOverlay.vaultarItems) {

                        List<ItemStack> suitableItems = item.items;

                        int totalSuitableItemsInInventory = 0;

                        for (ItemStack stack : suitableItems) {
                                Item key = stack.getItem();
                                if (inventoryItems.containsKey(key)) {
                                        totalSuitableItemsInInventory += inventoryItems.get(key);
                                }
                        }

                        if (item.getCountCompleted() != item.getCountTotal()){
                                RenderMainHUDCompositeElement(poseStack, (int) x, y, item.getCurrentItem(VaultarHUDOverlay.TICKER), item.getCountCompleted(), item.getCountTotal(), totalSuitableItemsInInventory,1.0F);
                                y += elementSpacing;
                        }

                }


                // Reset the color and disable transparency
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
        }


        private static void RenderMainHUDCompositeElement(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal, int inventoryTotal, float scale){


                if (countCompleted == countTotal){
                        itemStack.enchant(Enchantment.byId(1), 1);
                        return;
                }

                poseStack.pushPose();
                poseStack.translate(x, y, 100.0F);
                poseStack.scale(scale, scale, 1.0F);
                poseStack.translate(-x, -y, 0);

                //Draw the background image
                renderBackground(poseStack, x, y, 130, 38, getHudTexture());

                // Draw the item icon
                ScalableItemRenderer.render(itemStack, new Point(x + 6 * scale , y + 5 * scale), scale );


                // Center the item count text and draw it and the progress bar
                String text_count =  countCompleted + "/" + countTotal;
                int stringWidth = minecraft.font.width(text_count);
                int xOffset = x + 65 - stringWidth / 2;

                float progressOrange = (countTotal != 0) ? ((float) Math.min(countCompleted + inventoryTotal, countTotal) / countTotal * 118) : 0;
                float progressGreen = (countTotal != 0) ? ((float) countCompleted / countTotal * 118) : 118;

                GuiComponent.fill(poseStack, x + 6, y + 29, (int) Math.floor((x + 6) + progressOrange), y + 34, 0xFFd38b06);
                GuiComponent.fill(poseStack, x + 6, y + 29, (int) Math.floor((x + 6) + progressGreen), y + 34, 0xFF00FF00);

                minecraft.font.drawShadow(poseStack, new TextComponent(text_count), xOffset, y + 27, 0xFFFFFF);

                if (inventoryTotal > 0 && countCompleted < countTotal){
                        String text_inv = "(" + inventoryTotal + ")";
                        minecraft.font.drawShadow(poseStack, new TextComponent(text_inv), x+6, y + 27, 0xFFFFFF);
                }

                // Draw the item name, shrinking its size if it is too long
                String text_name = itemStack.getHoverName().getString();
                int nameWidth = minecraft.font.width(text_name);
                if (nameWidth > 100) {
                        poseStack.pushPose();
                        float scaleF = 100.0f / nameWidth;
                        poseStack.scale(scaleF, scaleF, 1.0f);
                        minecraft.font.drawShadow(poseStack, new TextComponent(text_name), (x + 26) / scaleF, (y + 10) / scaleF, 0x54FC54);
                        poseStack.popPose();
                } else {
                        minecraft.font.drawShadow(poseStack, new TextComponent(text_name), x + 26, y + 9, 0x54FC54);
                }

                poseStack.popPose();


        }




        private static int CalculateYOffsetIfPartyVisible(int screenHeight, int elementSpacing, boolean ignoreParty, int itemsToRender){
                int y = screenHeight / 2 - (elementSpacing * VaultarHUDOverlay.vaultarItems.size() / 2);

                VaultPartyData.Party party = VaultarHud.ISDEBUG ? null : ClientPartyData.getParty(minecraft.player.getUUID());

                if (party != null && !ignoreParty){
                        int partySize = party.getMembers().size();
                        y = (int)Math.max(screenHeight/3.0F, 42.0F);
                        y += (partySize) * 12 + 14;
                }else {
                        y += (elementSpacing * (4-itemsToRender) / 2);
                }


                //Sanity check so that the HUD doesn't render off the screen if the party is too large
                return Math.min(y, screenHeight - (elementSpacing * VaultarHUDOverlay.vaultarItems.size()) - 20);


        }

}
