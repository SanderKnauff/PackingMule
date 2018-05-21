package nl.imine.pixelmon.packingmule;

import nl.imine.pixelmon.packingmule.api.GiveItemAPI;
import nl.imine.pixelmon.packingmule.listener.InventoryListener;
import nl.imine.pixelmon.packingmule.service.PlayerInventoryRepository;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.nio.file.Path;

@Plugin(id="packingmule", name = "Packing Mule", version = "1.0", description = "Inventory management plugin")
public class PackingMulePlugin {

    private static final Logger logger = LoggerFactory.getLogger(PackingMulePlugin.class);

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configPath;

    @Listener
    public void onGameServerStartedEvent(GameStartedServerEvent event) {
        CategoryRepository categoryService = new CategoryRepository(configPath);
        categoryService.loadAll();
        PlayerInventoryRepository playerInventoryService = new PlayerInventoryRepository(configPath, categoryService);
        playerInventoryService.loadAll();
        categoryService.getAll().forEach(category -> logger.info("{}", category));
        Sponge.getEventManager().registerListeners(this, new InventoryListener(categoryService, playerInventoryService, this));
        GiveItemAPI.init(playerInventoryService, categoryService);
    }

}
