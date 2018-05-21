package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.io.IOException;

public class ItemTypeDeserializer extends JsonDeserializer<ItemType> {

    @Override
    public ItemType deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return Sponge.getRegistry().getType(ItemType.class, parser.getText()).orElse(null);
    }
}
