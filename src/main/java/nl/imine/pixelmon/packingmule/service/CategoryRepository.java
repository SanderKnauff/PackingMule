package nl.imine.pixelmon.packingmule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.service.serialization.ItemTypeDeserializer;
import org.spongepowered.api.item.ItemType;

import java.nio.file.Path;

public class CategoryRepository extends AbstractJsonRepository<String, BagCategory> {

    private static final String CATEGORY_STORAGE_PATH = "itemRegistry.json";

    public CategoryRepository(Path configPath) {
        super(configPath.resolve(CATEGORY_STORAGE_PATH));
    }

    @Override
    public ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = super.createObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(ItemType.class, new ItemTypeDeserializer());
        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}