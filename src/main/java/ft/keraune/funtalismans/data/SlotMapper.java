package ft.keraune.funtalismans.data;

import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlotMapper {

    /**
     * Convierte un nombre de slot en EquipmentSlot.
     * Solo acepta los nombres estrictos.
     */
    public static EquipmentSlot map(String rawSlot) {
        if (rawSlot == null) return null;

        String slot = rawSlot
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");

        switch (slot) {
            case "head":
                return EquipmentSlot.HEAD;

            case "chest":
                return EquipmentSlot.CHEST;

            case "legs":
                return EquipmentSlot.LEGS;

            case "feet":
                return EquipmentSlot.FEET;

            case "mainhand":
                return EquipmentSlot.HAND;

            case "offhand":
                return EquipmentSlot.OFF_HAND;

            default:
                return null;
        }
    }

    /**
     * Convierte una lista de slots separados por coma en una lista de EquipmentSlot.
     * Ejemplo: "mainhand, offhand, chest"
     */
    public static List<EquipmentSlot> mapList(String raw) {
        List<EquipmentSlot> slots = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return slots;

        String[] parts = raw.split(",");
        for (String part : parts) {
            EquipmentSlot slot = map(part);
            if (slot != null)
                slots.add(slot);
        }

        return slots;
    }
}