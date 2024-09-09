package io.iridium.vaultarhud.event;

import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.networking.ModMessages;
import io.iridium.vaultarhud.networking.packet.HandshakeCheckModIsOnServerC2SPacket;
import io.iridium.vaultarhud.renderers.HUDInventoryRenderer;
import io.iridium.vaultarhud.util.KeyBindings;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientEvents {
    public static ModMode mode = ModMode.CLIENTONLY;

    private static DoubleClickDetector doubleClickDetector = new DoubleClickDetector();
    private static boolean isProcessingKeyInput = false;

    @Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


        @SubscribeEvent
        public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {

            mode = ModMode.CLIENTONLY;
            ModMessages.sendToServer(new HandshakeCheckModIsOnServerC2SPacket());


        }



        @SubscribeEvent
        public static void onKeyInput(InputEvent.KeyInputEvent event) {

            if (KeyBindings.ENABLE_HUD.consumeClick()) {

                VaultarHUDOverlay.isHUDEnabled = !VaultarHUDOverlay.isHUDEnabled;

            }

            if (KeyBindings.TOGGLE_HUD.isDown()) {
                VaultarHUDOverlay.isVisible = true;
            }  else {
                VaultarHUDOverlay.isVisible = false;
            }


        }

        @SubscribeEvent
        public static void onGUIKeyInput(ScreenEvent.KeyboardKeyPressedEvent.Post event) {

            if (event.getScreen() instanceof InventoryScreen){
                return;
            }

            if (KeyBindings.ENABLE_HUD.matches(event.getKeyCode(), 0)) {

                VaultarHUDOverlay.isHUDEnabled = !VaultarHUDOverlay.isHUDEnabled;

            }
        }


        @SubscribeEvent
        public static void onMouseEvent(ScreenEvent.MouseClickedEvent event) {
            if(event.getButton() == 0 ) {
                HUDInventoryRenderer.hasMouseClicked = true;

                scheduler.schedule(() -> HUDInventoryRenderer.hasMouseClicked = false, 100, TimeUnit.MILLISECONDS);
            }
        }


    }

    public enum ModMode {
        CLIENTONLY,
        CLIENTANDSERVER
    }




}

class DoubleClickDetector {
    private static final long DOUBLE_CLICK_TIME = 500; // Time for a double click in milliseconds
    private long lastPressTime;

    public boolean isDoubleClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPressTime <= DOUBLE_CLICK_TIME) {
            lastPressTime = 0; // Reset the lastPressTime
            return true;
        } else {
            lastPressTime = currentTime;
            return false;
        }
    }
}