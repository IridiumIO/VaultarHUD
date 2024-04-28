package io.iridium.vaultarhud.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SharedFunctions {


    public static String formatNumber(int number) {
        if (number > 1000) {
            return round(number / 1000, 1) + "k";
        } else {
            return String.valueOf(number);
        }
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


    public static boolean isBlock(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem;
    }

    public static BakedModel getBakedModel(ItemStack itemStack) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        return itemRenderer.getItemModelShaper().getItemModel(itemStack);
    }

    public static boolean isInVault() {
        ClientLevel level = Minecraft.getInstance().level;
        String dimension = level.dimension().location().toString();
        return !(dimension.equals("minecraft:overworld") || dimension.equals("minecraft:the_nether") || dimension.equals("minecraft:the_end"));
    }

    public static boolean isMouseOverItem(double mouseX, double mouseY, double itemX, double itemY, float scale) {
        // Calculate the bounds of the item
        int itemWidth = (int) (16 * scale);
        int itemHeight = (int) (16 * scale);

        // Check if the mouse's position is within the item's bounds
        return mouseX >= itemX && mouseY >= itemY && mouseX < itemX + itemWidth && mouseY < itemY + itemHeight;
    }


    public static ItemStack getItemStackFromTag(String tag){
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag));
        return new ItemStack(item);
    }



    public static void renderBackground(PoseStack poseStack, int x, int y, int width, int height, ResourceLocation texture) {
        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(poseStack, x, y, width, height, width, height, width, height);
        poseStack.popPose();
    }


}
