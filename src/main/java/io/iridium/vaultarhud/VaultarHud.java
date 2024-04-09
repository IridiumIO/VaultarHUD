package io.iridium.vaultarhud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.altar.RequiredItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import io.iridium.vaultarhud.VaultarItem;

import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;


@Mod(VaultarHud.MOD_ID)
public class VaultarHud {

    public static final String MOD_ID = "vaultarhud";

    public VaultarHud() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }


}


@Mod.EventBusSubscriber
class VaultarHUDOverlay {

    private static final Logger LOGGER = Logger.getLogger(VaultarHud.class.getName());

    private static List<VaultarItem> vaultarItems = new CopyOnWriteArrayList<>(Arrays.asList());
    private static long lastChangeTime = 0;

    private static ResourceLocation hudTexture = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud.png");
    private static ResourceLocation hudTexture2 = new ResourceLocation(VaultarHud.MOD_ID, "textures/hud_2.png");


    private static PoseStack poseStack = new PoseStack();

    private static ServerPlayer player;

    @SubscribeEvent
    public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        player = (ServerPlayer) event.getPlayer();
    }


    public static List<VaultarItem> getVaultarItems(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        PlayerVaultAltarData altarData = PlayerVaultAltarData.get(level);

        if(altarData == null) {
            return new ArrayList<>();
        }

        AltarInfusionRecipe recipe = altarData.getRecipe(player.getUUID());

        if(recipe == null) {
            return new ArrayList<>();
        }

        List<RequiredItems> requiredItems = recipe.getRequiredItems();

        if(requiredItems == null) {
            return new ArrayList<>();
        }

        List<VaultarItem> vItems = new ArrayList<>();

        for(RequiredItems requiredItem : requiredItems) {
            int amountRequired = requiredItem.getAmountRequired();
            int currentAmount = requiredItem.getCurrentAmount();
            List<ItemStack> reqItems = requiredItem.getItems();

            VaultarItem item = new VaultarItem(reqItems, currentAmount, amountRequired);
            vItems.add(item);

        }
        return vItems;
    }


//    @SubscribeEvent
//    public static synchronized void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
//
//        BlockPos pos = event.getPos();
//        BlockState blockState = event.getWorld().getBlockState(pos);
//        Block block = blockState.getBlock();
//
//        if (!block.getRegistryName().toString().equals("the_vault:vault_altar")){
//            return;
//        }
//        BlockEntity blockEntity = event.getWorld().getBlockEntity(pos);
//        if (blockEntity == null){
//            return;
//        }
//
//        vaultarItems.clear();
//
//        CompoundTag nbtData = blockEntity.saveWithFullMetadata();
//
//        CompoundTag recipeTag = nbtData.getCompound("Recipe");
//        ListTag requiredItemsList = recipeTag.getList("requiredItems", 10);
//
//        for (Tag rawItemTag : requiredItemsList) {
//            CompoundTag itemTag = (CompoundTag) rawItemTag;
//            int amountRequired = itemTag.getInt("amountRequired");
//            int currentAmount = itemTag.getInt("currentAmount");
//            ListTag itemsList = itemTag.getList("items", 10);
//
//            List<ItemStack> requiredItems = new ArrayList<>();
//
//            for (Tag rawSubItemTag : itemsList) {
//                CompoundTag subItemTag = (CompoundTag) rawSubItemTag;
//                String id = subItemTag.getString("id");
//                requiredItems.add(getItemStackFromTag(id));
//            }
//
//            VaultarItem item = new VaultarItem(requiredItems, currentAmount, amountRequired);
//
//            vaultarItems.add(item);
//
//        }
//
//
//    }

    @SubscribeEvent
    public static synchronized void onRenderGameOverlay(RenderGameOverlayEvent event) {
        // Check if it's the right type of event
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        // Get the Minecraft instance
        Minecraft minecraft = Minecraft.getInstance();


        // Check if the TAB key is down
        if (!minecraft.options.keyPlayerList.isDown()) {
            return;
        }


        List<VaultarItem> newItems = getVaultarItems(player);
        if (!newItems.equals(vaultarItems)){
            vaultarItems = newItems;
        }

//        if (vaultarItems.size() == 0){
//            vaultarItems = getVaultarItems(player);
//        } else {
//            List<VaultarItem> newItems = getVaultarItems(player);
//            if (!newItems.equals(vaultarItems)){
//                vaultarItems = newItems;
//            }
//        }

        // Still check if the list is empty after getting the items
        if (vaultarItems.size() == 0){
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChangeTime >= 1000) {
            for (VaultarItem item : vaultarItems) {
                item.updateIndex();
            }
            lastChangeTime = currentTime;
        }

        RenderView1(minecraft);

    }


    public static void RenderView1(Minecraft minecraft){

        // Set the color and enable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();


        //Define the spacing between each rendered element
        int elementSpacing = 42;

        // Calculate starting coordinates for rendering; in this case, on the right side of the screen centered vertically
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int x = screenWidth - 135;
        int y = screenHeight /2 - (elementSpacing * vaultarItems.size() / 2);

        // Render each item
        for (int i = 0; i < vaultarItems.size(); i++) {
            VaultarItem item = vaultarItems.get(i);
            RenderCompositeObjectForView1(poseStack, x, y + (elementSpacing * i), item.getCurrentItem(), item.getCountCompleted(), item.getCountTotal());
        }

        // Reset the color and disable transparency
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

    }

    public static void RenderCompositeObjectForView1(PoseStack poseStack, int x, int y, ItemStack itemStack, int countCompleted, int countTotal){

        Minecraft minecraft = Minecraft.getInstance();

        //Draw the background image
        RenderSystem.setShaderTexture(0, hudTexture2);
        GuiComponent.blit(poseStack, x, y, 130, 38, 130, 38, 130, 38);

        // Draw the item icon
        minecraft.getItemRenderer().renderAndDecorateItem(itemStack, x + 6, y + 5);

        // Center the item count text and draw it and the progress bar
        String text_count = countCompleted + "/" + countTotal;
        int stringWidth = minecraft.font.width(text_count);
        int xOffset = x + 65 - stringWidth / 2;
        GuiComponent.fill(poseStack, x+6, y+29, (int) Math.floor((x+6) + ((float)countCompleted/countTotal * 118)), y+34, 0xFF00FF00);
        minecraft.font.drawShadow(poseStack, new TextComponent(text_count), xOffset, y + 27, 0xFFFFFF);

        // Draw the item name, shrinking its size if it is too long
        String text_name = itemStack.getHoverName().getString();
        int nameWidth = minecraft.font.width(text_name);
        if (nameWidth > 100){
            poseStack.pushPose();
            float scaleF = 100.0f / nameWidth;
            poseStack.scale(scaleF, scaleF, 1.0f);
            minecraft.font.drawShadow(poseStack, new TextComponent(text_name), (x + 26) /scaleF, (y + 10) /scaleF, 0x54FC54);
            poseStack.popPose();
        }else {
            minecraft.font.drawShadow(poseStack, new TextComponent(text_name), x +26, y + 9, 0x54FC54);
        }


    }

    public static ItemStack getItemStackFromTag(String tag){
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag));
        return new ItemStack(item);
    }




















    @SubscribeEvent
    public static void onKeyInput(InputEvent.MouseInputEvent event){

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null){
            return;
        }

        //1 = keyDown, 0 = keyUp, 2 = keyRepeat
        if(!isInventoryOpen() || event.getAction() != 1){
            return;
        }


        int eventKey = event.getButton();

        KeyMapping inventoryKeybind = Minecraft.getInstance().options.keyInventory;

        int boundKey = inventoryKeybind.getKey().getValue();

        if(eventKey == boundKey){
            player.closeContainer();
        }


    }



    public static boolean isInventoryOpen(){
        Screen screen = Minecraft.getInstance().screen;
        return screen instanceof AbstractContainerScreen;

    }


}
