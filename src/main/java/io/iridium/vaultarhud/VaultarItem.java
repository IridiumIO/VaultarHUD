package io.iridium.vaultarhud;

import iskallia.vault.altar.AltarInfusionRecipe;
import iskallia.vault.altar.RequiredItems;
import iskallia.vault.world.data.PlayerVaultAltarData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class VaultarItem {
    private List<ItemStack> items;
//    private int currentTagIndex;
    private int countCompleted;
    private int countTotal;

    public VaultarItem(List<ItemStack> items, int countCompleted, int countTotal) {
//        this.currentTagIndex = 0;
        this.countCompleted = countCompleted;
        this.countTotal = countTotal;
        this.items = items;
    }

    public ItemStack getCurrentItem(int ticker) {return items.get(ticker % items.size());}
//    public void updateIndex() {
//        currentTagIndex = (currentTagIndex + 1) % items.size();
//    }
    public int getCountCompleted() {
        return countCompleted;
    }
    public int getCountTotal() {
        return countTotal;
    }


    public static List<VaultarItem> getVaultarItems(ServerPlayer player) {

        if (player == null) {
            return new ArrayList<>();
        }
        ServerLevel level = player.getLevel();


        PlayerVaultAltarData altarData = PlayerVaultAltarData.get(level);

        if (altarData == null) {
            return new ArrayList<>();
        }

        AltarInfusionRecipe recipe = altarData.getRecipe(player.getUUID());

        if (recipe == null) {
            return new ArrayList<>();
        }

        List<RequiredItems> requiredItems = recipe.getRequiredItems();

        if (requiredItems.size() == 0) {
            return new ArrayList<>();
        }

        List<VaultarItem> vItems = new ArrayList<>();

        for (RequiredItems requiredItem : requiredItems) {
            int amountRequired = requiredItem.getAmountRequired();
            int currentAmount = requiredItem.getCurrentAmount();
            List<ItemStack> reqItems = requiredItem.getItems();

            VaultarItem item = new VaultarItem(reqItems, currentAmount, amountRequired);
            vItems.add(item);

        }
        return vItems;
    }



    // Add a constructor that accepts a FriendlyByteBuf
    public VaultarItem(FriendlyByteBuf buf) {
//        this.currentTagIndex = buf.readInt();
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
//        buf.writeInt(this.currentTagIndex);
        buf.writeInt(this.countCompleted);
        buf.writeInt(this.countTotal);
        buf.writeInt(this.items.size());
        for (ItemStack item : this.items) {
            buf.writeItem(item);
        }
    }



    //BROKEN CODE - I DON'T KNOW HOW IT WENT WRONG BUT NOW I HAVE TO USE THE TOSTRING METHOD TO COMPARE THE ITEMS INSTEAD.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VaultarItem vaultarItem = (VaultarItem) obj;
        if (countCompleted != vaultarItem.countCompleted || countTotal != vaultarItem.countTotal) {
            return false;
        }

        // Sort the item lists
        this.items.sort(Comparator.comparing(item -> item.getItem().getRegistryName().toString()));
        vaultarItem.items.sort(Comparator.comparing(item -> item.getItem().getRegistryName().toString()));

        // Compare the sorted item lists by their registry names
        return this.items.stream().map(item -> item.getItem().getRegistryName().toString())
                .equals(vaultarItem.items.stream().map(item -> item.getItem().getRegistryName().toString()));
    }
    @Override
    public int hashCode() {
        return Objects.hash(items, countCompleted, countTotal);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VaultarItem{");
        sb.append(", countCompleted=").append(countCompleted);
        sb.append(", countTotal=").append(countTotal);
        sb.append(", items=[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i).getItem().getRegistryName());
        }
        sb.append("]}");
        return sb.toString();
    }

}
