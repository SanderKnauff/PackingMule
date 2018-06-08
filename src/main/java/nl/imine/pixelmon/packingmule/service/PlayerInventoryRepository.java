package nl.imine.pixelmon.packingmule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.bag.PlayerInventory;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;
import nl.imine.pixelmon.packingmule.service.serialization.BagCategoryDeserializer;
import nl.imine.pixelmon.packingmule.service.serialization.BagCategorySerializer;
import nl.imine.pixelmon.packingmule.service.serialization.SpecializedItemRewardDeserializer;
import nl.imine.pixelmon.packingmule.service.serialization.SpecializedItemRewardSerializer;
import org.spongepowered.api.entity.living.player.Player;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public class PlayerInventoryRepository extends AbstractJsonRepository<UUID, PlayerInventory> {

    private static final String CATEGORY_STORAGE_PATH = "playerInventories.json";

    private final CategoryRepository categoryService;

    public PlayerInventoryRepository(Path configPath, CategoryRepository categoryService) {
        super(configPath.resolve(CATEGORY_STORAGE_PATH));
        this.categoryService = categoryService;
    }

    @Override
    protected ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = super.createObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BagCategory.class, new BagCategorySerializer());
        simpleModule.addDeserializer(BagCategory.class, new BagCategoryDeserializer(categoryService));
        simpleModule.addSerializer(SpecializedItemReward.class, new SpecializedItemRewardSerializer());
        simpleModule.addDeserializer(SpecializedItemReward.class, new SpecializedItemRewardDeserializer(categoryService));
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }

    public Optional<PlayerInventory> getByPlayer(Player player) {
        return getAll().stream()
                .filter(contents -> contents.getId().equals(player.getUniqueId()))
                .findAny();
    }
}
