package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;

public class ItemStackSerializer extends JsonSerializer<ItemStackSnapshot> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(ItemStackSnapshot value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(DataFormats.JSON.write(value.toContainer()));
        gen.writeTree(jsonNode);
    }
}
