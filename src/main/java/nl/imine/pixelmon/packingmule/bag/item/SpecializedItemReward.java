package nl.imine.pixelmon.packingmule.bag.item;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpecializedItemReward extends ItemReward {

    private final String id;
    private final String name;
    private final List<String> lore;

    @JsonCreator
    public SpecializedItemReward(@JsonProperty("id") String id,
                                 @JsonProperty("itemType") ItemType itemType,
                                 @JsonProperty("name") String name,
                                 @JsonProperty("lore") List<String> lore) {
        super(itemType);
        this.id = id;
        this.name = name;
        this.lore = lore;
    }

    public ItemStack createItemStack() {
        ItemStack.Builder builder = ItemStack.builder()
                .itemType(getItemType());
        if (name != null)
            builder.add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(name));
        if (lore != null && !lore.isEmpty())
            builder.add(Keys.ITEM_LORE, lore.stream().map(TextSerializers.FORMATTING_CODE::deserialize).collect(Collectors.toList()));
        return builder.build();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean matches(ItemStack itemStack) {
        return isItemTypeMatching(itemStack) && isNameMatching(itemStack) && isLoreMatching(itemStack);
    }

    private boolean isItemTypeMatching(ItemStack itemStack) {
        return itemStack.getType().equals(this.getItemType());
    }

    private boolean isNameMatching(ItemStack itemStack) {
        return name == null || itemStack.get(Keys.DISPLAY_NAME).map(Text::toString).orElse(itemStack.getTranslation().get()).equals(name);
    }

    private boolean isLoreMatching(ItemStack itemStack) {
        return lore == null || itemStack.get(Keys.ITEM_LORE).map(text -> text.stream().map(Text::toString).collect(Collectors.toList())).orElse(Collections.emptyList()).equals(lore);

    }

}
