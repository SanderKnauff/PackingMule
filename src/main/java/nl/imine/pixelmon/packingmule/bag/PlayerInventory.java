package nl.imine.pixelmon.packingmule.bag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.imine.pixelmon.packingmule.service.Identifyable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerInventory implements Identifyable<UUID> {

    private final UUID playerId;
    private List<BagContents> bags;

    @JsonCreator
    public PlayerInventory(@JsonProperty("id") UUID playerId, @JsonProperty("bags") List<BagContents> bags) {
        this.playerId = playerId;
        this.bags = bags;
    }

    public UUID getId() {
        return playerId;
    }

    public List<BagContents> getBags() {
        return bags;
    }

    public void setBags(List<BagContents> bags) {
        this.bags = bags;
    }

    public BagContents getBagContents(BagCategory bagCategory) {
        return getBags().stream()
                .filter(bagContents -> bagContents.getCategory().equals(bagCategory))
                .findAny()
                .orElseGet(() -> {
                    BagContents newContents = new BagContents(bagCategory, Collections.emptySet());
                    ArrayList<BagContents> newBags = new ArrayList<>(getBags());
                    newBags.add(newContents);
                    setBags(newBags);
                    return newContents;
                });

    }
}
