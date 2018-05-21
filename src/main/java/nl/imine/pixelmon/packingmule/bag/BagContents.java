package nl.imine.pixelmon.packingmule.bag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BagContents {

    private final BagCategory category;
    private Set<ItemType> items;

    @JsonCreator
    public BagContents(@JsonProperty("category") BagCategory category, @JsonProperty("items") Set<ItemType> items) {
        this.category = category;
        this.items = items;
    }

    public BagCategory getCategory() {
        return category;
    }

    public Set<ItemType> getItems() {
        return items;
    }

    public void setItems(Set<ItemType> items) {
        this.items = items;
    }
}
