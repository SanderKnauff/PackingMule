package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;
import java.util.Optional;

public class ItemStackDeserializer extends JsonDeserializer<ItemStackSnapshot> {

    private static final Logger logger = LoggerFactory.getLogger(ItemStackDeserializer.class);

    @Override
    public ItemStackSnapshot deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        DataContainer dataContainer = DataFormats.JSON.read(p.readValueAsTree().toString());
        Optional<ItemStackSnapshot> oItemStackSnapshot = Sponge.getDataManager().deserialize(ItemStackSnapshot.class, dataContainer);
        if(oItemStackSnapshot.isPresent()) {
            return oItemStackSnapshot.get();
        } else {
            logger.warn("Unable to deserialize ItemStackSnapshot from JSON String: {}", p.getText());
            return null;
        }
    }
}
