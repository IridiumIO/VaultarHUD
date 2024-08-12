package io.iridium.vaultarhud;

import io.iridium.vaultarhud.config.VaultarHudClientConfigs;
import io.iridium.vaultarhud.event.ClientEvents;
import io.iridium.vaultarhud.networking.ModMessages;
import io.iridium.vaultarhud.networking.packet.ClientRequestsVaultarDataC2SPacket;
import io.iridium.vaultarhud.renderers.HUDInGameRenderer;
import io.iridium.vaultarhud.renderers.HUDInventoryRenderer;
import io.iridium.vaultarhud.util.Point;
import io.iridium.vaultarhud.util.ScreenValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
public class VaultarHUDOverlay {

    public static List<VaultarItem> vaultarItems = new CopyOnWriteArrayList<>(Arrays.asList());
    private static long lastChangeTime = 0;
    public static int TICKER = 0;
    private static int CLIENTLASTCHANGEDTIME = 0;
    public static boolean isVisible = false;
    //public static int visibilityMode = 1; // 0 = Off, 1 = Regular Inventory HUD on left side, 2 = Small Inventory HUD
    public static boolean isHUDEnabled = true;

    private static Minecraft minecraft = Minecraft.getInstance();

    private static BlockPos AltarLocation = null;

    @SubscribeEvent
    public static synchronized void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){

        if(!ClientEvents.mode.equals(ClientEvents.ModMode.CLIENTONLY)) return;
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
        BlockEntity blockEntity = event.getWorld().getBlockEntity(event.getPos());

        long windowHandle = minecraft.getWindow().getWindow();
        boolean isShiftDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        boolean isAltDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
        boolean isCtrlDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS;

        if (isShiftDown && isAltDown && isCtrlDown) {
            //unhook from the altar
            AltarLocation = null;
            vaultarItems.clear();
            return;
        }

        if (blockEntity == null || !block.getRegistryName().toString().equals("the_vault:vault_altar")) return;
        vaultarItems.clear();
        AltarLocation = event.getPos();
        ListTag requiredItemsList = blockEntity.saveWithFullMetadata().getCompound("Recipe").getList("requiredItems", 10);
        for (Tag rawItemTag : requiredItemsList) vaultarItems.add(new VaultarItem((CompoundTag) rawItemTag));

    }

    @SubscribeEvent
    public static synchronized void onRenderGameOverlay(RenderGameOverlayEvent event) {

        if (!isHUDEnabled || !isVisible || (event.getType() != RenderGameOverlayEvent.ElementType.ALL)) return;
        updateVaultarItems();
        if (vaultarItems.isEmpty()) return;
        HUDInGameRenderer.render(event.getMatrixStack(), new Point(-1, 0));

    }


    @SubscribeEvent
    public static void onGUIScreenDraw(ScreenEvent.DrawScreenEvent.Post event){

        if (!isHUDEnabled|| !ScreenValidator.isValidScreen(event.getScreen())) return;
        updateVaultarItems();
        if (vaultarItems.isEmpty()) return;

        if(!VaultarHudClientConfigs.ENABLE_INVENTORY_HUD.get()) return;

        if (VaultarHudClientConfigs.USE_SMALL_INVENTORY_HUD.get()){
            HUDInventoryRenderer.render(event.getPoseStack(), null);
        }else {
            HUDInGameRenderer.render(event.getPoseStack(), new Point(2, 0));
        }

    }


    private static void updateVaultarItems(){

        if(ClientEvents.mode.equals(ClientEvents.ModMode.CLIENTONLY) && (System.currentTimeMillis() - lastChangeTime >= 1000)){
            lastChangeTime = System.currentTimeMillis();

            Level world = minecraft.player.level;

            if (world.dimension() != Level.OVERWORLD && world.dimension() != Level.NETHER && world.dimension() != Level.END){
                //VaultarHud.LOGGER.info("VaultarHUDOverlay: Player is not in the overworld, nether, or end dimension. Cancelling update.");
                return;
            }

            Block blockToFind = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:vault_altar"));

            if (AltarLocation == null){
                BlockPos pos = minecraft.player.getOnPos();
                AltarLocation = isBlockNearby(world, pos, blockToFind, 10);
            }

            if (AltarLocation != null){

                Block block = world.getBlockState(AltarLocation).getBlock();
                BlockEntity blockEntity = world.getBlockEntity(AltarLocation);

                if (blockEntity == null || !block.getRegistryName().toString().equals("the_vault:vault_altar")) return;
                vaultarItems.clear();
                ListTag requiredItemsList = blockEntity.saveWithFullMetadata().getCompound("Recipe").getList("requiredItems", 10);
                for (Tag rawItemTag : requiredItemsList) vaultarItems.add(new VaultarItem((CompoundTag) rawItemTag));

            }

        }else if(ClientEvents.mode.equals(ClientEvents.ModMode.CLIENTANDSERVER)){

            ModMessages.sendToServer(new ClientRequestsVaultarDataC2SPacket());
            if (vaultarItems.isEmpty()) return;
            if (System.currentTimeMillis() - lastChangeTime >= 1000) {
                TICKER = (TICKER + 1) % 128;  //Increments the ticker but keeps it within the range of 0-127 to avoid calculations getting too expensive
                lastChangeTime = System.currentTimeMillis();
            }

        }



    }


    public static BlockPos isBlockNearby(Level world, BlockPos center, Block blockToFind, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (world.getBlockState(pos).getBlock() == blockToFind) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }


}
