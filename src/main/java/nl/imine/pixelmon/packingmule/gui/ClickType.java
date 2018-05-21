package nl.imine.pixelmon.packingmule.gui;

import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;

import java.util.Optional;

/**
 * Simple ClickType. Does not allow the usage of all sponge's features
 */
public class ClickType {

    private final boolean isPrimary;
    private final boolean isShift;

    public ClickType(boolean isPrimary, boolean isShift) {
        this.isPrimary = isPrimary;
        this.isShift = isShift;
    }

    public static Optional<ClickType> getFromEvent(ClickInventoryEvent evt) {
        if (evt instanceof ClickInventoryEvent.Primary
                || evt instanceof ClickInventoryEvent.Secondary
                || evt instanceof ClickInventoryEvent.Shift.Primary
                || evt instanceof ClickInventoryEvent.Shift.Secondary) {
            return Optional.of(new ClickType(evt instanceof ClickInventoryEvent.Primary, evt instanceof ClickInventoryEvent.Shift));
        } else {
            return Optional.empty();
        }
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isShift() {
        return isShift;
    }

    @Override
    public String toString() {
        return "ClickType{" +
                "isPrimary=" + isPrimary +
                ", isShift=" + isShift +
                '}';
    }
}
