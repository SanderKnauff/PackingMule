package nl.imine.pixelmon.packingmule.api;

import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.bag.BagContents;
import nl.imine.pixelmon.packingmule.bag.PlayerInventory;
import nl.imine.pixelmon.packingmule.bag.item.ItemReward;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import nl.imine.pixelmon.packingmule.service.PlayerInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GiveItemAPI.class);

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
        logger.info("Giving item '{}' to player '{}'", rewardId, player.getName());
        ItemReward itemReward = getItemRewardFromString(rewardId);
        if (isItemInAnyCategory(rewardId)) {
            logger.info("Item '{}' is not in a category", rewardId);
            giveItemInBag(player, (SpecializedItemReward) itemReward);
        } else {
            logger.info("Item '{}' was not in a category and will be given directly into the inventory", rewardId);
            giveItemInInventory(player, ItemStack.builder().itemType(itemReward.getItemType()).build());
        }
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


    private Optional<SpecializedItemReward> getSpecializedItemRewardFromReward(ItemReward itemReward) {
        return Optional.of(itemReward).filter(SpecializedItemReward.class::isInstance).map(SpecializedItemReward.class::cast);
    }

    public boolean playerHasItem(Player player, String rewardId) {
        Optional<SpecializedItemReward> oSpecializedItemReward = getSpecializedItemRewardFromReward(getItemRewardFromString(rewardId));
        return oSpecializedItemReward.map(specializedItemReward -> hasItemInInventory(player, specializedItemReward) || hasItemInBag(player, specializedItemReward)).orElse(false);
    }

    private boolean hasItemInInventory(Player player, SpecializedItemReward itemType) {
        return player.getInventory().containsAny(itemType.createItemStack());
    }

    private boolean hasItemInBag(Player player, SpecializedItemReward itemReward) {
        return playerInventoryRepository.getByPlayer(player)
                .map(inventory ->
                        inventory.getBags().stream().flatMap(bagContents -> bagContents.getItems().stream()).anyMatch(itemReward::equals)
                ).orElse(false);
    }

    public void removeItemFromPlayer(Player player, String rewardId) {
        getSpecializedItemRewardFromReward(getItemRewardFromString(rewardId)).ifPresent(itemReward -> {
            removeFromBagAndReplaceItem(player, itemReward);
            removeFromInventory(player, itemReward);
        });
    }

    private void removeFromBagAndReplaceItem(Player player, SpecializedItemReward specializedItemReward) {
        getCategoryFromRewardId(specializedItemReward.getId()).ifPresent(bagCategory -> playerInventoryRepository.findOne(player.getUniqueId()).ifPresent(playerInventory -> {
            List<BagContents> bagContents = new ArrayList<>(playerInventory.getBags());
            BagContents bagContent = playerInventory.getBagContents(bagCategory);
            List<SpecializedItemReward> items = new ArrayList<>(bagContent.getItems());
            items.removeIf(rewardInList -> specializedItemReward.getId().equals(rewardInList.getId()));
            bagContent.setItems(items);
            bagContents.removeIf(contents -> contents.getCategory().equals(bagCategory));
            bagContents.add(bagContent);
            playerInventory.setBags(bagContents);

            if (hasItemInInventory(player, specializedItemReward)) {
                removeFromInventory(player, specializedItemReward);
                bagContent.getItems().stream().findAny().ifPresent(itemType ->
                        giveItemInInventory(player, itemType.createItemStack())
                );
            }
        }));
    }

    private void removeFromInventory(Player player, SpecializedItemReward specializedItemReward) {
        player.getInventory().query(QueryOperationTypes.ITEM_STACK_IGNORE_QUANTITY.of(specializedItemReward.createItemStack())).clear();
    }

    private boolean isItemInAnyCategory(String rewardId) {
        return categoryRepository.getAll().stream()
                .flatMap(bagCategory -> bagCategory.getAllowedItems().stream())
                .map(SpecializedItemReward::getId)
                .anyMatch(rewardId::equals);
    }

    private void giveItemInBag(Player player, SpecializedItemReward itemReward) {
        logger.info("Adding {} to {}'s bag", itemReward.createItemStack(), player.getName());
        getCategoryFromRewardId(itemReward.getId()).ifPresent(bagCategory -> {
            logger.info("Item {}'s  BagCategory is {}", itemReward.createItemStack(), bagCategory.getId());
            if (!isAnyItemFromCategoryAlreadyInPlayerInventory(player, bagCategory)) {
                logger.info("Player {} has no item of BagCategory {} in it's inventory yet. Giving the current one", player.getName(), bagCategory.getId());
                giveItemInInventory(player, itemReward.createItemStack());
            }
            addItemToPlayerBag(player, bagCategory, itemReward);
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

    private void addItemToPlayerBag(Player player, BagCategory bagCategory, SpecializedItemReward specializedItemReward) {
        logger.info("Adding {} to {}'s {} bag", specializedItemReward.createItemStack(), player.getName(), bagCategory.getId());
        PlayerInventory playerInventory = playerInventoryRepository.findOne(player.getUniqueId()).orElse(new PlayerInventory(player.getUniqueId(), new ArrayList<>()));
        logger.info("Adding {} to {}'s player inventory {}", specializedItemReward.createItemStack(), player.getName(), playerInventory);
        BagContents bagContents = playerInventory.getBagContents(bagCategory);
        List<SpecializedItemReward> items = new ArrayList<>(bagContents.getItems());
        items.add(specializedItemReward);
        bagContents.setItems(items);
        playerInventoryRepository.addOne(playerInventory);

    }

    private void giveItemInInventory(Player player, ItemStack itemStack) {
        logger.info("Adding {} to {}'s inventory", itemStack, player.getName());
        getPlayerCombinedHotbarAndMainInventory(player).offer(itemStack);
    }

    private Inventory getPlayerCombinedHotbarAndMainInventory(Player player) {
        Inventory hotbarPlayerInventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        Inventory mainPlayerInventory = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class));
        return hotbarPlayerInventory.union(mainPlayerInventory);
    }
}
