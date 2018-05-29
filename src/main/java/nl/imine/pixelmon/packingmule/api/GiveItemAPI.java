package nl.imine.pixelmon.packingmule.api;

import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.bag.BagContents;
import nl.imine.pixelmon.packingmule.bag.item.ItemReward;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import nl.imine.pixelmon.packingmule.service.PlayerInventoryRepository;
import org.spongepowered.api.Sponge;
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

    public void giveItemToPlayer(Player player, String rewardId) {
        ItemReward itemReward = getItemRewardFromString(rewardId);
        if (isItemInAnyCategory(rewardId))
            giveItemInBag(player, (SpecializedItemReward) itemReward);
        else
            giveItemInInventory(player, ItemStack.builder().itemType(itemReward.getItemType()).build());
    }

    private ItemReward getItemRewardFromString(String rewardId) {
        if (rewardId.contains(":"))
            return new ItemReward(Sponge.getRegistry().getType(ItemType.class, rewardId).orElseThrow(() -> new IllegalArgumentException("ItemType '" + rewardId + "' was not registered in sponge!")));
        else
            return categoryRepository.getAll().stream().flatMap(bagCategory -> bagCategory.getAllowedItems().stream())
                    .filter(specializedItemReward -> specializedItemReward.getId().equals(rewardId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Reward '" + rewardId + "' was not registered in any catagory!"));
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

    public void removeItemFromPlayer(Player player, SpecializedItemReward specializedItemReward) {
        if (isItemInAnyCategory(specializedItemReward.getId()))
            removeFromBagAndReplaceItem(player, specializedItemReward);
        else
            removeFromInventory(player, specializedItemReward.getItemType());
    }

    private void removeFromBagAndReplaceItem(Player player, SpecializedItemReward specializedItemReward) {
        getCategoryFromRewardId(specializedItemReward.getId()).ifPresent(bagCategory -> playerInventoryRepository.findOne(player.getUniqueId()).ifPresent(playerInventory -> {
            List<BagContents> bagContents = new ArrayList<>(playerInventory.getBags());
            BagContents bagContent = playerInventory.getBagContents(bagCategory);
            Set<ItemType> items = new HashSet<>(bagContent.getItems());
            items.remove(specializedItemReward.getItemType());
            bagContent.setItems(items);
            bagContents.removeIf(contents -> contents.getCategory().equals(bagCategory));
            bagContents.add(bagContent);
            playerInventory.setBags(bagContents);

            if (hasItemInInventory(player, specializedItemReward.getItemType())) {
                removeFromInventory(player, specializedItemReward.getItemType());
                bagContent.getItems().stream().findAny().ifPresent(itemType ->
                        giveItemInInventory(player, ItemStack.builder().itemType(itemType).build())
                );
            }
        }));
    }

    private void removeFromInventory(Player player, ItemType itemType) {
        player.getInventory().query(QueryOperationTypes.ITEM_TYPE.of(itemType)).clear();
    }

    private boolean isItemInAnyCategory(String rewardId) {
        return categoryRepository.getAll().stream()
                .flatMap(bagCategory -> bagCategory.getAllowedItems().stream())
                .map(SpecializedItemReward::getId)
                .anyMatch(rewardId::equals);
    }

    private void giveItemInBag(Player player, SpecializedItemReward itemReward) {
        getCategoryFromRewardId(itemReward.getId()).ifPresent(bagCategory -> {
            if (!isAnyItemFromCategoryAlreadyInPlayerInventory(player, bagCategory))
                giveItemInInventory(player, itemReward.createItemStack());
            addItemToPlayerBag(player, bagCategory, itemReward.createItemStack());
        });
    }

    private Optional<BagCategory> getCategoryFromRewardId(String rewardId) {
        return categoryRepository.getAll().stream()
                .filter(category -> category.getAllowedItems().stream().anyMatch(specializedItemReward -> specializedItemReward.getId().equals(rewardId)))
                .findAny();
    }

    private boolean isAnyItemFromCategoryAlreadyInPlayerInventory(Player player, BagCategory bagCategory) {
        return bagCategory.getAllowedItems().stream()
                .map(SpecializedItemReward::getItemType)
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
