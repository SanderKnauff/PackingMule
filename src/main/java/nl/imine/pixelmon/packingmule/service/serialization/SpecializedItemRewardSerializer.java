package nl.imine.pixelmon.packingmule.service.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nl.imine.pixelmon.packingmule.bag.item.SpecializedItemReward;

import java.io.IOException;

public class SpecializedItemRewardSerializer extends JsonSerializer<SpecializedItemReward> {

    public SpecializedItemRewardSerializer() {
    }

    @Override
    public void serialize(SpecializedItemReward value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.getId());
    }
}
