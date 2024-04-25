package io.iridium.vaultarhud.event;

import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.networking.ModMessages;
import io.iridium.vaultarhud.networking.packet.HandshakeCheckModIsOnServerC2SPacket;
import io.iridium.vaultarhud.util.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    public static ModMode mode = ModMode.CLIENTONLY;


    @Mod.EventBusSubscriber(modid = VaultarHud.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {


        @SubscribeEvent
        public static void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {

            mode = ModMode.CLIENTONLY;
            ModMessages.sendToServer(new HandshakeCheckModIsOnServerC2SPacket());


        }

        @SubscribeEvent
        public  static void onKeyInput(InputEvent.KeyInputEvent event) {

            if (KeyBindings.ENABLE_HUD.consumeClick()) {
                VaultarHUDOverlay.isEnabled = !VaultarHUDOverlay.isEnabled;
            }
        }


    }

    public enum ModMode {
        CLIENTONLY,
        CLIENTANDSERVER
    }


}
