package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import nl.imine.pixelmon.packingmule.bag.BagCategory;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class BagCategoryDeserializer extends JsonDeserializer<BagCategory> {

    private final CategoryRepository categoryService;

    public BagCategoryDeserializer(CategoryRepository categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public BagCategory deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String categoryName = p.getText();
        return categoryService.getAll().stream().filter(c -> categoryName.equals(c.getId())).findFirst().orElseGet(() -> {
            BagCategory bagCategory = new BagCategory(categoryName, new ArrayList<>());
            categoryService.addOne(bagCategory);
            return bagCategory;
        });
    }
}
