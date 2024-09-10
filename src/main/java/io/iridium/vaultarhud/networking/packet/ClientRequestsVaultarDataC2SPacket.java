package io.iridium.vaultarhud.networking.packet;

import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.VaultarItem;
import io.iridium.vaultarhud.networking.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static io.iridium.vaultarhud.VaultarItem.getVaultarItems;
import static io.iridium.vaultarhud.util.SharedFunctions.getItemStack;

public class ClientRequestsVaultarDataC2SPacket {

    private static long lastPacketTime = 0;
    private static final long PACKET_COOLDOWN = 100;

    public ClientRequestsVaultarDataC2SPacket() {}

    public ClientRequestsVaultarDataC2SPacket(FriendlyByteBuf buf){}

    public void toBytes(FriendlyByteBuf buf){}

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // THIS IS ALL ON THE SERVER!!!!

            ServerPlayer player = context.getSender();

            List<VaultarItem> newItems = VaultarHud.ISDEBUG ?
                    Arrays.asList(
                            new VaultarItem(Arrays.asList(getItemStack("minecraft:stone"), getItemStack("minecraft:cobblestone"), getItemStack("minecraft:deepslate"), getItemStack("minecraft:cobbled_deepslate")), 5, 25),
                            new VaultarItem(Arrays.asList(getItemStack("minecraft:cracked_polished_blackstone_bricks")), 14, 232),
                            new VaultarItem(Arrays.asList(getItemStack("minecraft:birch_log"), getItemStack("minecraft:oak_log"), getItemStack("minecraft:spruce_log")), 4346, 4356),
                            new VaultarItem(Arrays.asList(getItemStack("minecraft:bamboo")), 128, 2118)
                    ) :
                    getVaultarItems(player);


            if (System.currentTimeMillis() - lastPacketTime < PACKET_COOLDOWN) return;

            ModMessages.sendToClient(new ServerReturnsVaultarDataS2CPacket(newItems), player);
            lastPacketTime = System.currentTimeMillis();
        });

        return true;
    }


}
