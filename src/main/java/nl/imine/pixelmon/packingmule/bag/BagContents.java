package nl.imine.pixelmon.packingmule.bag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;

import java.util.List;

public class BagContents {

    private final BagCategory category;
    private List<SpecializedItemReward> items;

    @JsonCreator
    public BagContents(@JsonProperty("category") BagCategory category, @JsonProperty("items") List<SpecializedItemReward> items) {
        this.category = category;
        this.items = items;
    }

    public BagCategory getCategory() {
        return category;
    }

    public List<SpecializedItemReward> getItems() {
        return items;
    }

    public void setItems(List<SpecializedItemReward> items) {
        this.items = items;
    }
}
