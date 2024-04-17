package io.iridium.vaultarhud.networking.packet;

import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.event.ClientEvents;
import io.iridium.vaultarhud.networking.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HandshakeRespondModIsOnServerS2CPacket {


    public HandshakeRespondModIsOnServerS2CPacket() {}

    public HandshakeRespondModIsOnServerS2CPacket(FriendlyByteBuf buf){}

    public void toBytes(FriendlyByteBuf buf){}

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // THIS IS ALL ON THE CLIENT!!!!

            ClientEvents.mode = ClientEvents.ModMode.CLIENTANDSERVER;

        });

        return true;
    }

}
