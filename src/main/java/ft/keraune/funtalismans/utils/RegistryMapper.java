package ft.keraune.funtalismans.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;

public class RegistryMapper {

    public static Attribute getAttribute(String name) {
        if (name == null) return null;
        NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
        return Registry.ATTRIBUTE.get(key);
    }

    public static AttributeModifier.Operation getOperation(String name) {
        // Paper 1.21.9: Operation still uses enum
        try {
            return AttributeModifier.Operation.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AttributeModifier.Operation.ADD_NUMBER;
        }
    }

    public static EquipmentSlot getSlot(String name) {
        try {
            return EquipmentSlot.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
