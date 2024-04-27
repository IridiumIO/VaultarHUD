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
import io.iridium.vaultarhud.util.Point;
import iskallia.vault.client.ClientPartyData;
import iskallia.vault.world.data.VaultPartyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import com.refinedmods.refinedstorage.screen.grid.GridScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.CraftingTermScreen;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
public class VaultarHUDOverlay {

    private static List<VaultarItem> vaultarItems = new CopyOnWriteArrayList<>(Arrays.asList());
    private static long lastChangeTime = 0;
    private static ResourceLocation hudTexture2 = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_2.png");
    private static ResourceLocation hudInventoryTexture = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_inv.png");
    private static PoseStack fallbackStack = new PoseStack();
    private static int ticker = 0;

    public static boolean isVisible = false;

    // 0 = Off, 1 = Regular Inventory HUD on left side, 2 = Small Inventory HUD
    public static int visibilityMode = 1;


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

        if (visibilityMode == 0) {
            return;
        }

        // Check if the TAB key is down
        if (!isVisible) {
            return;
        }

        // Check if it's the right type of event
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }


        // Get the Minecraft instance
        Minecraft minecraft = Minecraft.getInstance();



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

        RenderView1(minecraft, event.getMatrixStack(), -1);

    }


    public static void RenderView1(Minecraft minecraft, PoseStack poseStack, int x) {

        // Set the color and enable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

//        float scale = isInVault() ? 0.75F : 1.0F;

        float scale = 1.0F;

        //Define the spacing between each rendered element
        int elementSpacing = Math.round(42 * scale);

        // Calculate starting coordinates for rendering; in this case, on the right side of the screen centered vertically
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        x = (x == -1)? screenWidth - Math.round(135 * scale) : x;
        int y = screenHeight / 2 - (elementSpacing * vaultarItems.size() / 2);
        int offset = 0;


        LocalPlayer player = minecraft.player;
        VaultPartyData.Party party = ClientPartyData.getParty(player.getUUID());

        if (party != null){
            int partySize = party.getMembers().size();
            y = (int)Math.max(screenHeight/3.0F, 42.0F);
            offset = (partySize) * 12 + 14;
        }

        //Sanity check so that the HUD doesn't render off the screen if the party is too large
        y = Math.min(y + offset, screenHeight - (elementSpacing * vaultarItems.size()) - 20);


        // Render each item
        for (int i = 0; i < vaultarItems.size(); i++) {
            VaultarItem item = vaultarItems.get(i);
            RenderCompositeObjectForView1(poseStack, x, y + (elementSpacing * i), item.getCurrentItem(ticker), item.getCountCompleted(), item.getCountTotal(), scale);
        }

        // Reset the color and disable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

    }


    private static boolean isValidVanillaScreen(Screen screen){
        List<Class<? extends Screen>> Screens = Arrays.asList(
                InventoryScreen.class,
                ContainerScreen.class,
                CraftingScreen.class,
                FurnaceScreen.class
        );

        return Screens.stream().anyMatch(cls -> cls.isInstance(screen));
    }


    private static int curHeight = 0;
    private static boolean isValidRSScreen(Screen screen){
        List<Class<? extends Screen>> Screens = Arrays.asList(
                GridScreen.class
        );
        return Screens.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    private static boolean isValidSophisticatedBackpackScreen(Screen screen){

        List<Class<? extends Screen>> Screens = Arrays.asList(
                StorageScreenBase.class
        );
        return Screens.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    private static boolean isValidAE2Screen(Screen screen){

        List<Class<? extends Screen>> Screens = Arrays.asList(
                MEStorageScreen.class,
                CraftingTermScreen.class
        );

        if (Screens.stream().anyMatch(cls -> cls.isInstance(screen))){
            MEStorageScreen gridScreen = (MEStorageScreen) screen;

            if (gridScreen == null){
                gridScreen = (CraftingTermScreen) screen;
            }


            if (gridScreen.getYSize() != curHeight) {
                curHeight = gridScreen.getYSize();
                VaultarHud.LOGGER.info("Screen Height: " + curHeight);
            }

        }



        return Screens.stream().anyMatch(cls -> cls.isInstance(screen));
    }

    private static Point getScreenHUDCoordinates(Screen screen){
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = 0;
        int y = 0;
        int offset = 0;

        String screenName = screen.getClass().getName();

        if (isValidVanillaScreen(screen)){
            x = ((screenWidth - 176) / 2) - 27;
            y = ((screenHeight + 166) / 2) - 87;

        }

        if (isValidRSScreen(screen)){

            GridScreen gs = (GridScreen) screen;

            x = ((screenWidth - 227) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 88;
        }


        if (isValidSophisticatedBackpackScreen(screen)){

            StorageScreenBase gs = (StorageScreenBase) screen;

            x = ((screenWidth - 176) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 87;
        }

        if (isValidAE2Screen(screen)){

            MEStorageScreen gs = (MEStorageScreen) screen;

            if (gs == null){
                gs = (CraftingTermScreen) screen;
            }

            x = ((screenWidth - 195) / 2) - 27;
            y = ((screenHeight + gs.getYSize()) / 2) - 88;
        }

        return new Point(x, y);

    }

    @SubscribeEvent
    public static void onGUIScreenDraw(ScreenEvent.DrawScreenEvent.Post event){

        if (visibilityMode == 0) {
            return;
        }

        if (!isValidVanillaScreen(event.getScreen()) && !isValidRSScreen(event.getScreen()) && !isValidSophisticatedBackpackScreen(event.getScreen()) && !isValidAE2Screen(event.getScreen())) {
            return;
        }


        // Get the Minecraft instance
        Minecraft minecraft = Minecraft.getInstance();

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

        if (visibilityMode == 2){
            RenderInventoryView(minecraft, event.getPoseStack(), isValidVanillaScreen(event.getScreen())? true : false);
            return;
        }
        RenderView1(minecraft, event.getPoseStack(), 2);

    }


    public static void RenderInventoryView(Minecraft minecraft, PoseStack poseStack, boolean showCrystal) {

        // Set the color and enable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        float scale = 1.0F;

        //Define the spacing between each rendered element
        int elementSpacing = Math.round(18 * scale);

        // Calculate starting coordinates for rendering; in this case, on the right side of the screen centered vertically

        Point offset = getScreenHUDCoordinates(minecraft.screen);

        int x = (int) offset.getX();
        int y = (int) offset.getY();

        poseStack.pushPose();
        //Render Background
        RenderSystem.setShaderTexture(0, hudInventoryTexture);
        GuiComponent.blit(poseStack, x, y, 28, 87, 28, 87, 28, 87);
        poseStack.popPose();


        if (showCrystal) {
            RenderCrystal(poseStack, x + 6, y - 21, scale);
        }

        // Render each item
        for (int i = 0; i < vaultarItems.size(); i++) {
            VaultarItem item = vaultarItems.get(i);
            RenderCompositeObjectForInvView(poseStack, x+6, y + 10 + (elementSpacing * i), item.getCurrentItem(ticker), item.getCountCompleted(), item.getCountTotal(), scale);
        }

        // Reset the color and disable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

    }


    public static void RenderCompositeObjectForView1(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal,  float scale){

        Minecraft minecraft = Minecraft.getInstance();

        poseStack.pushPose();
        poseStack.translate(x, y, 100.0F);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        //Draw the background image
        RenderSystem.setShaderTexture(0, hudTexture2);
        GuiComponent.blit(poseStack, x, y, 130, 38, 130, 38, 130, 38);


        // Draw the item icon

       ScalableItemRenderer(itemStack, x + 6 * scale , y + 5 * scale, scale, false);


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


    public static void RenderCompositeObjectForInvView(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal,  float scale){

        Minecraft minecraft = Minecraft.getInstance();

        poseStack.pushPose();
        poseStack.translate(x, y, 100.0F);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        // Draw the item icon

        if (countCompleted == countTotal){
            itemStack.enchant(Enchantment.byId(1), 1);
        }



        double guiScale = minecraft.getWindow().getGuiScale();

        ScalableItemRenderer(itemStack, x, y , scale, false);

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

    public static  void RenderCrystal(PoseStack poseStack, int x, int y, float scale) {
        poseStack.pushPose();
        poseStack.translate(x, y, 100F);
        poseStack.scale(scale, scale, 1.0F);
        poseStack.translate(-x, -y, 0);

        ItemStack crystal = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("the_vault:vault_crystal")));

        ScalableItemRenderer(crystal, x , y , scale, true);

        poseStack.popPose();

    }


    public static void ScalableItemRenderer(ItemStack itemStack, double x, double y, float scale, boolean isAnimated) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        BakedModel pBakedModel = getBakedModel(itemStack);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();

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


        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
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

}
