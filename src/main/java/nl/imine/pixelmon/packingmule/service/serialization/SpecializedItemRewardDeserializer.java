package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;
import nl.imine.pixelmon.packingmule.service.CategoryRepository;
import org.spongepowered.api.item.ItemTypes;

import java.io.IOException;
import java.util.Collections;

public class SpecializedItemRewardDeserializer extends JsonDeserializer<SpecializedItemReward> {

    private static final SpecializedItemReward NO_ITEM_FOUND_FALLBACK = new SpecializedItemReward("UNKNOWN_ITEM", ItemTypes.AIR, "Unknown Item", Collections.emptyList());
    private final CategoryRepository categoryRepository;

    public SpecializedItemRewardDeserializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public SpecializedItemReward deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String rewardId = parser.getText();
        return categoryRepository.getAll().stream()
                .flatMap(bagCategory -> bagCategory.getAllowedItems().stream())
                .filter(specializedItemReward -> specializedItemReward.getId().equals(rewardId))
                .findAny()
                .orElse(NO_ITEM_FOUND_FALLBACK);
    }
}
