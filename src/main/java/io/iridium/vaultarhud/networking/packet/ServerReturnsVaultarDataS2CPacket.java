package io.iridium.vaultarhud.networking.packet;


import io.iridium.vaultarhud.VaultarHUDOverlay;
import io.iridium.vaultarhud.VaultarItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ServerReturnsVaultarDataS2CPacket {

        private final List<VaultarItem> items;

        public ServerReturnsVaultarDataS2CPacket(List<VaultarItem> items) {
            this.items = items;
        }

        public ServerReturnsVaultarDataS2CPacket(FriendlyByteBuf buf){
            this.items = new ArrayList<>();
            int itemsSize = buf.readInt();
            for (int i = 0; i < itemsSize; i++) {
                this.items.add(new VaultarItem(buf));
            }
        }

        public void toBytes(FriendlyByteBuf buf){
            buf.writeInt(this.items.size());
            for (VaultarItem item : this.items) {
                item.write(buf);
            }
        }

        public boolean handle(Supplier<NetworkEvent.Context> supplier){
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> {
                /// THIS IS ALL ON THE CLIENT!!!!

                VaultarHUDOverlay.vaultarItems = this.items;

            });

            return true;
        }

    }

