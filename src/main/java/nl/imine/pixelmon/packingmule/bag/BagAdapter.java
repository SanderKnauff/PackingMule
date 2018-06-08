package nl.imine.pixelmon.packingmule.bag;

import nl.imine.pixelmon.packingmule.PackingMulePlugin;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.*;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BagAdapter {

    private static final Logger logger = LoggerFactory.getLogger(BagAdapter.class);

    private static final int INVENTORY_WIDTH = 9;
    private static final int INVENTORY_HEIGHT = 6;
    private static final int NAVIGATION_ROW_COUNT = 1;

    private final BagContents bagContents;

    private final PackingMulePlugin pluginInstance;

    private int topRowIndex = 0;

    public BagAdapter(BagContents bagContents, PackingMulePlugin pluginInstance) {
        this.bagContents = bagContents;
        this.pluginInstance = pluginInstance;
    }

    public void openInventory(Player player) {
        Inventory inventory = createBaseInventory();
        updateInventory(inventory);
        player.openInventory(inventory);
    }

    private Inventory createBaseInventory() {
        return Inventory.builder()
                .property(InventoryDimension.of(INVENTORY_WIDTH, INVENTORY_HEIGHT))
                .property(InventoryTitle.of(Text.of(bagContents.getCategory().getId())))
                .listener(ClickInventoryEvent.class, clickInventoryEvent -> {
                    clickInventoryEvent.getTransactions().forEach(slotTransaction -> slotTransaction.getSlot().getInventoryProperty(SlotIndex.class).ifPresent(slotIndex -> {
                        if (slotIndex.getValue() != null) {
                            switch (slotIndex.getValue()) {
                                case 8:
                                    moveRow(slotTransaction.getSlot().parent(), -1);
                                    break;
                                case 53:
                                    moveRow(slotTransaction.getSlot().parent(), 1);
                                    break;
                                default:
                                    clickInventoryEvent.getCause().first(Player.class).ifPresent(player -> {
                                        setPlayerActiveItem(player, bagContents.getItems().get(getArrayIndexFromListSlot(slotIndex.getValue())).createItemStack());
                                    });
                            }
                        }
                    }));
                    clickInventoryEvent.setCancelled(true);
                })
                .build(pluginInstance);
    }

    private void moveRow(Inventory inventory, int i) {
        topRowIndex = Math.max(topRowIndex + i, 0);
        topRowIndex = Math.min(topRowIndex, calculateMaxRow());
        updateInventory(inventory);
    }

    private int calculateMaxRow() {
        int containerWidth = INVENTORY_WIDTH - NAVIGATION_ROW_COUNT;
        return bagContents.getItems().size() / containerWidth;
    }

    private void setPlayerActiveItem(Player player, ItemStack itemStack) {
        if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && bagContents.getCategory().getAllowedItems().stream().anyMatch(specializedItemReward -> specializedItemReward.createItemStack().equalTo(player.getItemInHand(HandTypes.MAIN_HAND).get()))) {
            player.setItemInHand(HandTypes.MAIN_HAND, itemStack);
        }
    }

    private void updateInventory(Inventory inventory) {
        inventory.first().clear();
        addControlButtons(inventory);
        List<SpecializedItemReward> bagEntries = new ArrayList<>(bagContents.getItems());
        for (int i = 0; i < bagEntries.size(); i++) {
            if (i < topRowIndex * 8) {
                //Skip elements if we don't need it for this row
                continue;
            }
            int rowIndex = (i - (topRowIndex * 8)) / 8;
            int columnIndex = (i - (topRowIndex * 8)) % 8;

            inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(columnIndex, rowIndex)))
                    .offer(bagEntries.get(i).createItemStack());

        }
    }

    private void addControlButtons(Inventory inventory) {
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 0)))
                .offer(ItemStack.builder().itemType(ItemTypes.STONE_BUTTON).build());
        inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(8, 5)))
                .offer(ItemStack.builder().itemType(ItemTypes.STONE_BUTTON).build());
    }


    private static int getListSlotFromArrayIndex(int i) {
        return i + (i / 8);
    }

    private static int getArrayIndexFromListSlot(int i) {
        return i - (i / 9);
    }

}
