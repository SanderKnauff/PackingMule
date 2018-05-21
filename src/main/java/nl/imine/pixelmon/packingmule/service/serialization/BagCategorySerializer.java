package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nl.imine.pixelmon.packingmule.bag.BagCategory;

import java.io.IOException;

public class BagCategorySerializer extends JsonSerializer<BagCategory> {

    @Override
    public void serialize(BagCategory value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getId());
    }
}
