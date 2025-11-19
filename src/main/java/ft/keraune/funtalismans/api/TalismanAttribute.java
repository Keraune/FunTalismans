package ft.keraune.funtalismans.api;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

public class TalismanAttribute {

    private final Attribute type;
    private final double amount;
    private final AttributeModifier.Operation operation;
    private final EquipmentSlot slot;

    public TalismanAttribute(Attribute type, double amount, AttributeModifier.Operation operation, EquipmentSlot slot) {
        this.type = type;
        this.amount = amount;
        this.operation = operation;
        this.slot = slot;
    }

    public Attribute getType() { return type; }
    public double getAmount() { return amount; }
    public AttributeModifier.Operation getOperation() { return operation; }
    public EquipmentSlot getSlot() { return slot; }

    @Override
    public String toString() {
        return "TalismanAttribute{type=" + type + ", amount=" + amount + "}";
    }
}
