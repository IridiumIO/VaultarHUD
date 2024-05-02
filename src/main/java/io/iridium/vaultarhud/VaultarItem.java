package io.iridium.vaultarhud;

import iskallia.vault.altar.RequiredItems;
import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static io.iridium.vaultarhud.util.SharedFunctions.getItemStackFromTag;

public class VaultarItem {
    private List<ItemStack> items;
    private int countTotal;
    private int countCompleted;

    public VaultarItem(List<ItemStack> items, int countCompleted, int countTotal) {
        this.items = items;
        this.countTotal = countTotal;
        this.countCompleted = countCompleted;
    }

    public VaultarItem(CompoundTag itemTag){
        this.items = new ArrayList<>();
        for (Tag rawSubItemTag : itemTag.getList("items", 10)) {
            CompoundTag subItemTag = (CompoundTag) rawSubItemTag;
            String id = subItemTag.getString("id");
            this.items.add(getItemStackFromTag(id));
        }
        this.countTotal = itemTag.getInt("amountRequired");;
        this.countCompleted = itemTag.getInt("currentAmount");;
    }


    public ItemStack getCurrentItem(int ticker) {return items.get(ticker % items.size());}
    public int getCountCompleted() {
        return countCompleted;
    }
    public int getCountTotal() {
        return countTotal;
    }


    public static List<VaultarItem> getVaultarItems(ServerPlayer player) {

        if (player == null || PlayerVaultAltarData.get(player.getLevel()) == null || PlayerVaultAltarData.get(player.getLevel()).getRecipe(player.getUUID()) == null) {
            return new ArrayList<>();
        }

        List<VaultarItem> vItems = new ArrayList<>();
        for (RequiredItems requiredItem : PlayerVaultAltarData.get(player.getLevel()).getRecipe(player.getUUID()).getRequiredItems()) {
            vItems.add(new VaultarItem(requiredItem.getItems(), requiredItem.getCurrentAmount(), requiredItem.getAmountRequired()));
        }
        return vItems;
    }



    // Add a constructor that accepts a FriendlyByteBuf
    public VaultarItem(FriendlyByteBuf buf) {
        this.countCompleted = buf.readInt();
        this.countTotal = buf.readInt();
        int itemSize = buf.readInt();
        this.items = new ArrayList<>();
        for (int i = 0; i < itemSize; i++) {
            this.items.add(buf.readItem());
        }
    }


    // Add a method to write a VaultarItem to a FriendlyByteBuf
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.countCompleted);
        buf.writeInt(this.countTotal);
        buf.writeInt(this.items.size());
        for (ItemStack item : this.items) {
            buf.writeItem(item);
        }
    }



//    //BROKEN CODE - I DON'T KNOW HOW IT WENT WRONG BUT NOW I HAVE TO USE THE TOSTRING METHOD TO COMPARE THE ITEMS INSTEAD.
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null || getClass() != obj.getClass()) {
//            return false;
//        }
//        VaultarItem vaultarItem = (VaultarItem) obj;
//        if (countCompleted != vaultarItem.countCompleted || countTotal != vaultarItem.countTotal) {
//            return false;
//        }
//
//        // Sort the item lists
//        this.items.sort(Comparator.comparing(item -> item.getItem().getRegistryName().toString()));
//        vaultarItem.items.sort(Comparator.comparing(item -> item.getItem().getRegistryName().toString()));
//
//        // Compare the sorted item lists by their registry names
//        return this.items.stream().map(item -> item.getItem().getRegistryName().toString())
//                .equals(vaultarItem.items.stream().map(item -> item.getItem().getRegistryName().toString()));
//    }
//    @Override
//    public int hashCode() {
//        return Objects.hash(items, countCompleted, countTotal);
//    }
//
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("VaultarItem{");
//        sb.append(", countCompleted=").append(countCompleted);
//        sb.append(", countTotal=").append(countTotal);
//        sb.append(", items=[");
//        for (int i = 0; i < items.size(); i++) {
//            if (i > 0) {
//                sb.append(", ");
//            }
//            sb.append(items.get(i).getItem().getRegistryName());
//        }
//        sb.append("]}");
//        return sb.toString();
//    }

}
