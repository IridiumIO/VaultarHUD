package io.iridium.vaultarhud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.iridium.vaultarhud.event.ClientEvents;
import io.iridium.vaultarhud.networking.ModMessages;
import io.iridium.vaultarhud.networking.packet.ClientRequestsVaultarDataC2SPacket;
import io.iridium.vaultarhud.util.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


@Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
public class VaultarHUDOverlay {

    private static List<VaultarItem> vaultarItems = new CopyOnWriteArrayList<>(Arrays.asList());
    private static long lastChangeTime = 0;
    private static ResourceLocation hudTexture2 = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_2.png");
    private static PoseStack poseStack = new PoseStack();
    private static int ticker = 0;
    public static  boolean isEnabled = true;

    public static synchronized void setVaultarItems(List<VaultarItem> items) {
        vaultarItems = items;
    }

    @SubscribeEvent
    public static synchronized void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){

        if(!ClientEvents.mode.equals(ClientEvents.ModMode.CLIENTONLY)){
            return;
        }

        BlockPos pos = event.getPos();


        BlockState blockState = event.getWorld().getBlockState(pos);
        Block block = blockState.getBlock();

        if (!block.getRegistryName().toString().equals("the_vault:vault_altar")){
            return;
        }
        BlockEntity blockEntity = event.getWorld().getBlockEntity(pos);
        if (blockEntity == null){
            return;
        }

        vaultarItems.clear();

        CompoundTag nbtData = blockEntity.saveWithFullMetadata();

        CompoundTag recipeTag = nbtData.getCompound("Recipe");
        ListTag requiredItemsList = recipeTag.getList("requiredItems", 10);

        for (Tag rawItemTag : requiredItemsList) {
            CompoundTag itemTag = (CompoundTag) rawItemTag;
            int amountRequired = itemTag.getInt("amountRequired");
            int currentAmount = itemTag.getInt("currentAmount");
            ListTag itemsList = itemTag.getList("items", 10);

            List<ItemStack> requiredItems = new ArrayList<>();

            for (Tag rawSubItemTag : itemsList) {
                CompoundTag subItemTag = (CompoundTag) rawSubItemTag;
                String id = subItemTag.getString("id");
                requiredItems.add(getItemStackFromTag(id));
            }

            VaultarItem item = new VaultarItem(requiredItems, currentAmount, amountRequired);

            vaultarItems.add(item);

        }


    }

    public static ItemStack getItemStackFromTag(String tag){
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag));
        return new ItemStack(item);
    }

    @SubscribeEvent
    public static synchronized void onRenderGameOverlay(RenderGameOverlayEvent event) {

        if (!isEnabled) {
            return;
        }

        // Check if it's the right type of event
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }


        // Get the Minecraft instance
        Minecraft minecraft = Minecraft.getInstance();


        // Check if the TAB key is down
        if (!KeyBindings.TOGGLE_HUD.isDown()) {
            return;
        }

//        // Check if the HUD_OVERLAY key is down
//        if(!KeyBindings.TOGGLE_HUD.isDown()){
//
////            if(minecraft.screen == null){
//                return;
////            }
//
//        }

        ModMessages.sendToServer(new ClientRequestsVaultarDataC2SPacket());


        // Check if the list is empty after getting the items
        if (vaultarItems.size() == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChangeTime >= 1000) {
            ticker = (ticker + 1) % 128;  //Increments the ticker but keeps it within the range of 0-127 to avoid calculations getting too expensive
            lastChangeTime = currentTime;
        }

        RenderView1(minecraft);

    }


    public static void RenderView1(Minecraft minecraft) {

        // Set the color and enable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        float scale = 1.0F;

        //Define the spacing between each rendered element
        int elementSpacing = Math.round(42 * scale);

        // Calculate starting coordinates for rendering; in this case, on the right side of the screen centered vertically
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = screenWidth - Math.round(135 * scale);
        int y = screenHeight / 2 - (elementSpacing * vaultarItems.size() / 2);

        // Render each item
        for (int i = 0; i < vaultarItems.size(); i++) {
            VaultarItem item = vaultarItems.get(i);
            RenderCompositeObjectForView1(poseStack, x, y + (elementSpacing * i), item.getCurrentItem(ticker), item.getCountCompleted(), item.getCountTotal(), scale);
        }

        // Reset the color and disable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

    }

    public static void RenderCompositeObjectForView1(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal,  float scale){

        Minecraft minecraft = Minecraft.getInstance();

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        //Draw the background image
        RenderSystem.setShaderTexture(0, hudTexture2);
        GuiComponent.blit(poseStack, x, y, 130, 38, 130, 38, 130, 38);


        // Draw the item icon

       ScalableItemRenderer(itemStack, x , y, scale);


        // Center the item count text and draw it and the progress bar
        String text_count = countCompleted + "/" + countTotal;
        int stringWidth = minecraft.font.width(text_count);
        int xOffset = x + 65 - stringWidth / 2;
        GuiComponent.fill(poseStack, x + 6, y + 29, (int) Math.floor((x + 6) + ((float) countCompleted / countTotal * 118)), y + 34, 0xFF00FF00);
        minecraft.font.drawShadow(poseStack, new TextComponent(text_count), xOffset, y + 27, 0xFFFFFF);

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

    public static void ScalableItemRenderer(ItemStack itemStack, int x, int y, float scale) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        BakedModel pBakedModel = getBakedModel(itemStack);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();

        poseStack.translate((x + 6 * scale) , (y+5 * scale), (double)(100.0F));
        poseStack.translate(8.0D * scale, 8.0D * scale, 0.0D);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F * scale, 16.0F * scale, 16.0F * scale);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !pBakedModel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        renderer.render(itemStack, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, pBakedModel);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();

    }

    public static boolean isBlock(ItemStack itemStack) {
        return itemStack.getItem() instanceof BlockItem;
    }

    public static BakedModel getBakedModel(ItemStack itemStack) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        return itemRenderer.getItemModelShaper().getItemModel(itemStack);
    }
}
