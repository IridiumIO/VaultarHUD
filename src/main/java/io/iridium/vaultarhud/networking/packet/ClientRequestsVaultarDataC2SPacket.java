package io.iridium.vaultarhud.networking.packet;

import io.iridium.vaultarhud.VaultarHud;
import io.iridium.vaultarhud.VaultarItem;
import io.iridium.vaultarhud.networking.ModMessages;
import iskallia.vault.core.vault.Vault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static io.iridium.vaultarhud.VaultarItem.getVaultarItems;

public class ClientRequestsVaultarDataC2SPacket {

    private static long lastPacketTime = 0;
    private static long packetCooldown = 100;

    public ClientRequestsVaultarDataC2SPacket() {}

    public ClientRequestsVaultarDataC2SPacket(FriendlyByteBuf buf){}

    public void toBytes(FriendlyByteBuf buf){}

    public boolean handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // THIS IS ALL ON THE SERVER!!!!

            ServerPlayer player = context.getSender();

            List<VaultarItem> newItems = getVaultarItems(player);

//            List<VaultarItem> testItems = Arrays.asList(
//                    new VaultarItem(Arrays.asList(getItemStack("minecraft:stone"), getItemStack("minecraft:cobblestone"), getItemStack("minecraft:deepslate"), getItemStack("minecraft:cobbled_deepslate")), 25, 25),
//                    new VaultarItem(Arrays.asList(getItemStack("minecraft:cracked_polished_blackstone_bricks")), 14, 23),
//                    new VaultarItem(Arrays.asList(getItemStack("minecraft:birch_log"), getItemStack("minecraft:oak_log"), getItemStack("minecraft:spruce_log")), 42, 1056),
//                    new VaultarItem(Arrays.asList(getItemStack("minecraft:bamboo")), 217, 218)
//            );


            if (System.currentTimeMillis() - lastPacketTime < packetCooldown) {
                return;
            }

            ModMessages.sendToClient(new ServerReturnsVaultarDataS2CPacket(newItems), player);

//            ModMessages.sendToClient(new ServerReturnsVaultarDataS2CPacket(testItems), player);

            lastPacketTime = System.currentTimeMillis();
        });

        return true;
    }

    private static ItemStack getItemStack(String item) {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(item)));
    }

}
