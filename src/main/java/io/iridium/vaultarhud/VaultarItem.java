package io.iridium.vaultarhud;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

class VaultarItem {
//    private List<String> tags;
    private List<ItemStack> items;
    private int currentTagIndex;
    private int countCompleted;
    private int countTotal;

//    public VaultarItem(List<String> tags, int countCompleted, int countTotal) {
//        this.tags = tags;
//        this.currentTagIndex = 0;
//        this.countCompleted = countCompleted;
//        this.countTotal = countTotal;
//        for (String tag : tags) {
//            items.add(getItemStackFromTag(tag));
//        }
//    }

    public VaultarItem(List<ItemStack> items, int countCompleted, int countTotal) {
        this.currentTagIndex = 0;
        this.countCompleted = countCompleted;
        this.countTotal = countTotal;
        this.items = items;
    }

//    public String getCurrentTag() {
//        return tags.get(currentTagIndex);
//    }

    public ItemStack getCurrentItem() {return items.get(currentTagIndex);}

    public void updateIndex() {
        currentTagIndex = (currentTagIndex + 1) % items.size();
    }

    public int getCountCompleted() {
        return countCompleted;
    }

    public int getCountTotal() {
        return countTotal;
    }

    private static ItemStack getItemStackFromTag(String tag){
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag));
        return new ItemStack(item);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        VaultarItem vaultarItem = (VaultarItem) obj;
        return countCompleted == vaultarItem.countCompleted &&
                countTotal == vaultarItem.countTotal &&
                items.equals(vaultarItem.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, currentTagIndex, countCompleted, countTotal);
    }

}
