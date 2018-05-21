package nl.imine.pixelmon.packingmule.listener;

import nl.imine.pixelmon.packingmule.PackingMulePlugin;
import nl.imine.pixelmon.packingmule.api.GiveItemAPI;
import nl.imine.pixelmon.packingmule.bag.BagAdapter;
import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.bag.BagContents;
import nl.imine.pixelmon.packingmule.bag.PlayerInventory;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import nl.imine.pixelmon.packingmule.service.PlayerInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.sql.ConnectionEvent;
import java.util.Collections;
import java.util.Optional;

public class InventoryListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryListener.class);

    private final CategoryRepository categoryService;
    private final PlayerInventoryRepository playerInventoryRepository;
    private final PackingMulePlugin pluginInstance;

    public InventoryListener(CategoryRepository categoryService, PlayerInventoryRepository playerInventoryRepository, PackingMulePlugin pluginInstance) {
        this.categoryService = categoryService;
        this.playerInventoryRepository = playerInventoryRepository;
        this.pluginInstance = pluginInstance;
    }

    @Listener
    @Include(InteractItemEvent.Secondary.MainHand.class)
    public void onItemUseEvent(InteractItemEvent evt, @Root Player player) {
        if (player.get(Keys.IS_SNEAKING).orElse(false))
            getCatagoryFromItemType(evt.getItemStack().getType()).ifPresent(bagCategory -> {
                Optional<PlayerInventory> oPlayerInventory = playerInventoryRepository.getByPlayer(player);
                PlayerInventory playerInventory = oPlayerInventory.orElseGet(() -> {
                    PlayerInventory object = new PlayerInventory(player.getUniqueId(), Collections.emptyList());
                    playerInventoryRepository.addOne(object);
                    return object;
                });

                new BagAdapter(new BagContents(bagCategory, playerInventory.getBagContents(bagCategory).getItems()), pluginInstance)
                        .openInventory(player);
            });
    }

    private Optional<BagCategory> getCatagoryFromItemType(ItemType itemType) {
        return categoryService.getAll().parallelStream().filter(bagCategory -> bagCategory.getAllowedItems().contains(itemType)).findFirst();
    }

    @Listener
    @Include(ClientConnectionEvent.Join.class)
    public void login(ClientConnectionEvent connectionEvent, @Root Player player) {
        GiveItemAPI.getGiveItemAPI().giveItemToPlayer(player, ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm70").get()).build());
        GiveItemAPI.getGiveItemAPI().giveItemToPlayer(player, ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:hm2").get()).build());
        System.out.println(GiveItemAPI.getGiveItemAPI().playerHasItem(player, Sponge.getRegistry().getType(ItemType.class, "pixelmon:hm2").get()));
        System.out.println(GiveItemAPI.getGiveItemAPI().playerHasItem(player, Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm10").get()));
        GiveItemAPI.getGiveItemAPI().removeItemFromPlayer(player, ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm10").get()).build());
        GiveItemAPI.getGiveItemAPI().removeItemFromPlayer(player, ItemStack.builder().itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:hm2").get()).build());
        System.out.println(GiveItemAPI.getGiveItemAPI().playerHasItem(player, Sponge.getRegistry().getType(ItemType.class, "pixelmon:hm2").get()));
        System.out.println(GiveItemAPI.getGiveItemAPI().playerHasItem(player, Sponge.getRegistry().getType(ItemType.class, "pixelmon:tm10").get()));
    }

}
