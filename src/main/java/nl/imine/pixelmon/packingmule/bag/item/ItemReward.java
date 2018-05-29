package nl.imine.pixelmon.packingmule.bag.item;

import org.spongepowered.api.item.ItemType;

public class ItemReward {

    private final ItemType itemType;

    public ItemReward(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }
}
