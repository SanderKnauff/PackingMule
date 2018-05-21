package nl.imine.pixelmon.packingmule.api;

import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.bag.BagContents;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import nl.imine.pixelmon.packingmule.service.PlayerInventoryRepository;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import java.util.*;

public class GiveItemAPI {

    private static GiveItemAPI giveItemAPI;

    private final PlayerInventoryRepository playerInventoryRepository;
    private final CategoryRepository categoryRepository;

    public GiveItemAPI(PlayerInventoryRepository playerInventoryRepository, CategoryRepository categoryRepository) {
        this.playerInventoryRepository = playerInventoryRepository;
        this.categoryRepository = categoryRepository;
    }

    public static void init(PlayerInventoryRepository playerInventoryRepository, CategoryRepository categoryRepository) {
        giveItemAPI = new GiveItemAPI(playerInventoryRepository, categoryRepository);
    }

    public static GiveItemAPI getGiveItemAPI() {
        return giveItemAPI;
    }

    public void giveItemToPlayer(Player player, ItemStack itemStack) {
        if (isItemInAnyCategory(itemStack))
            giveItemInBag(player, itemStack);
        else
            giveItemInInventory(player, itemStack);
    }

    public boolean playerHasItem(Player player, ItemType itemType) {
        return hasItemInInventory(player, itemType) || hasItemInBag(player, itemType);
    }

    private boolean hasItemInInventory(Player player, ItemType itemType) {
        return player.getInventory().contains(itemType);
    }

    private boolean hasItemInBag(Player player, ItemType itemType) {
        return playerInventoryRepository.getByPlayer(player)
                .map(inventory ->
                        inventory.getBags().stream().flatMap(bagContents -> bagContents.getItems().stream()).anyMatch(itemType::equals)
                ).orElse(false);
    }

    public void removeItemFromPlayer(Player player, ItemStack itemStack) {
        if (isItemInAnyCategory(itemStack))
            removeFromBagAndReplaceItem(player, itemStack);
        else
            removeFromInventory(player, itemStack);
    }

    private void removeFromBagAndReplaceItem(Player player, ItemStack itemStack) {
        getCategoryFromItemStack(itemStack).ifPresent(bagCategory -> playerInventoryRepository.findOne(player.getUniqueId()).ifPresent(playerInventory -> {
            List<BagContents> bagContents = new ArrayList<>(playerInventory.getBags());
            BagContents bagContent = playerInventory.getBagContents(bagCategory);
            Set<ItemType> items = new HashSet<>(bagContent.getItems());
            items.remove(itemStack.getType());
            bagContent.setItems(items);
            bagContents.removeIf(contents -> contents.getCategory().equals(bagCategory));
            bagContents.add(bagContent);
            playerInventory.setBags(bagContents);

            if (hasItemInInventory(player, itemStack.getType())) {
                removeFromInventory(player, itemStack);
                bagContent.getItems().stream().findAny().ifPresent(itemType ->
                        giveItemInInventory(player, ItemStack.builder().itemType(itemType).build())
                );
            }
        }));
    }

    private void removeFromInventory(Player player, ItemStack itemStack) {
        player.getInventory().query(QueryOperationTypes.ITEM_TYPE.of(itemStack.getType())).clear();
    }

    private boolean isItemInAnyCategory(ItemStack itemStack) {
        return categoryRepository.getAll().stream()
                .flatMap(bagCategory -> bagCategory.getAllowedItems().stream())
                .anyMatch(itemStack.getType()::equals);
    }

    private void giveItemInBag(Player player, ItemStack itemStack) {
        getCategoryFromItemStack(itemStack).ifPresent(bagCategory -> {
            if (!isAnyItemFromCategoryAlreadyInPlayerInventory(player, bagCategory))
                giveItemInInventory(player, itemStack);
            addItemToPlayerBag(player, bagCategory, itemStack);
        });
    }

    private Optional<BagCategory> getCategoryFromItemStack(ItemStack itemStack) {
        return categoryRepository.getAll().stream()
                .filter(category -> category.getAllowedItems().contains(itemStack.getType()))
                .findAny();
    }

    private boolean isAnyItemFromCategoryAlreadyInPlayerInventory(Player player, BagCategory bagCategory) {
        return bagCategory.getAllowedItems().stream()
                .anyMatch(player.getInventory()::contains);

    }

    private void addItemToPlayerBag(Player player, BagCategory bagCategory, ItemStack itemStack) {
        playerInventoryRepository.findOne(player.getUniqueId()).ifPresent(playerInventory -> {
            BagContents bagContents = playerInventory.getBagContents(bagCategory);
            Set<ItemType> items = new HashSet<>(bagContents.getItems());
            items.add(itemStack.getType());
            bagContents.setItems(items);
            playerInventoryRepository.addOne(playerInventory);
        });

    }

    private void giveItemInInventory(Player player, ItemStack itemStack) {
        getPlayerCombinedHotbarAndMainInventory(player).offer(itemStack);
    }

    private Inventory getPlayerCombinedHotbarAndMainInventory(Player player) {
        Inventory hotbarPlayerInventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        Inventory mainPlayerInventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
        return hotbarPlayerInventory.union(mainPlayerInventory);
    }
}
