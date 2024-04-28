package io.iridium.vaultarhud;

import com.mojang.blaze3d.vertex.PoseStack;
import io.iridium.vaultarhud.event.ClientEvents;
import io.iridium.vaultarhud.networking.ModMessages;
import io.iridium.vaultarhud.networking.packet.ClientRequestsVaultarDataC2SPacket;
import io.iridium.vaultarhud.renderers.HUDInGameRenderer;
import io.iridium.vaultarhud.renderers.HUDInventoryRenderer;
import io.iridium.vaultarhud.renderers.ScalableItemRenderer;
import io.iridium.vaultarhud.util.Point;
import io.iridium.vaultarhud.util.ScreenValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
public class VaultarHUDOverlay {

    public static List<VaultarItem> vaultarItems = new CopyOnWriteArrayList<>(Arrays.asList());
    private static long lastChangeTime = 0;

    public static int TICKER = 0;

    public static boolean isVisible = false;

    // 0 = Off, 1 = Regular Inventory HUD on left side, 2 = Small Inventory HUD
    public static int visibilityMode = 1;

    private static Minecraft minecraft = Minecraft.getInstance();



    @SubscribeEvent
    public static synchronized void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){

        if(!ClientEvents.mode.equals(ClientEvents.ModMode.CLIENTONLY)){
            return;
        }

        BlockPos pos = event.getPos();
        BlockState blockState = event.getWorld().getBlockState(pos);
        Block block = blockState.getBlock();
        BlockEntity blockEntity = event.getWorld().getBlockEntity(pos);

        if (!block.getRegistryName().toString().equals("the_vault:vault_altar") || blockEntity == null){
            return;
        }
        vaultarItems.clear();

        ListTag requiredItemsList = blockEntity.saveWithFullMetadata().getCompound("Recipe").getList("requiredItems", 10);

        for (Tag rawItemTag : requiredItemsList) {
            vaultarItems.add(new VaultarItem((CompoundTag) rawItemTag));
        }

    }


    @SubscribeEvent
    public static synchronized void onRenderGameOverlay(RenderGameOverlayEvent event) {

        // Check if the HUD is enabled, if the visibility mode is set to 0, or if the event type is not ALL
        if (visibilityMode == 0 || !isVisible || (event.getType() != RenderGameOverlayEvent.ElementType.ALL)) {
            return;
        }

        ModMessages.sendToServer(new ClientRequestsVaultarDataC2SPacket());

        // Check if the list is empty after getting the items
        if (vaultarItems.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChangeTime >= 1000) {
            TICKER = (TICKER + 1) % 128;  //Increments the ticker but keeps it within the range of 0-127 to avoid calculations getting too expensive
            lastChangeTime = currentTime;
        }

        HUDInGameRenderer.render(event.getMatrixStack(), new Point(-1, 0));

    }

    @SubscribeEvent
    public static void onGUIScreenDraw(ScreenEvent.DrawScreenEvent.Post event){

        if (visibilityMode == 0 || !ScreenValidator.isValidScreen(event.getScreen())){
            return;
        }

        ModMessages.sendToServer(new ClientRequestsVaultarDataC2SPacket());

        // Check if the list is empty after getting the items
        if (vaultarItems.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChangeTime >= 1000) {
            TICKER = (TICKER + 1) % 128;  //Increments the ticker but keeps it within the range of 0-127 to avoid calculations getting too expensive
            lastChangeTime = currentTime;
        }

        if (visibilityMode == 2){
            HUDInventoryRenderer.render(event.getPoseStack(), null);
            return;
        }

        HUDInGameRenderer.render(event.getPoseStack(), new Point(2, 0));

    }







}
