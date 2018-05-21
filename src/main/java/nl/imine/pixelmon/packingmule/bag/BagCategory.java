package nl.imine.pixelmon.packingmule.bag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.imine.pixelmon.packingmule.service.Identifyable;
import org.spongepowered.api.item.ItemType;

import java.util.Objects;
import java.util.Set;

public class BagCategory implements Identifyable<String> {

    private final String id;
    private final Set<ItemType> allowedItems;

    @JsonCreator
    public BagCategory(@JsonProperty("id") String id, @JsonProperty("allowedItems") Set<ItemType> allowedItems) {
        this.id = id;
        this.allowedItems = allowedItems;
    }

    public Set<ItemType> getAllowedItems() {
        return allowedItems;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BagCategory that = (BagCategory) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(allowedItems, that.allowedItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, allowedItems);
    }
}
