package io.iridium.vaultarhud.networking;

import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.networking.packet.ClientRequestsVaultarDataC2SPacket;
import io.iridium.vaultarhud.networking.packet.HandshakeCheckModIsOnServerC2SPacket;
import io.iridium.vaultarhud.networking.packet.HandshakeRespondModIsOnServerS2CPacket;
import io.iridium.vaultarhud.networking.packet.ServerReturnsVaultarDataS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(VaultarHud.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(ClientRequestsVaultarDataC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClientRequestsVaultarDataC2SPacket::new)
                .encoder(ClientRequestsVaultarDataC2SPacket::toBytes)
                .consumer(ClientRequestsVaultarDataC2SPacket::handle)
                .add();

        net.messageBuilder(ServerReturnsVaultarDataS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ServerReturnsVaultarDataS2CPacket::new)
                .encoder(ServerReturnsVaultarDataS2CPacket::toBytes)
                .consumer(ServerReturnsVaultarDataS2CPacket::handle)
                .add();

        net.messageBuilder(HandshakeCheckModIsOnServerC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(HandshakeCheckModIsOnServerC2SPacket::new)
                .encoder(HandshakeCheckModIsOnServerC2SPacket::toBytes)
                .consumer(HandshakeCheckModIsOnServerC2SPacket::handle)
                .add();

        net.messageBuilder(HandshakeRespondModIsOnServerS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HandshakeRespondModIsOnServerS2CPacket::new)
                .encoder(HandshakeRespondModIsOnServerS2CPacket::toBytes)
                .consumer(HandshakeRespondModIsOnServerS2CPacket::handle)
                .add();

    }

    public static <MSG> void sendToServer(MSG message){
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}
