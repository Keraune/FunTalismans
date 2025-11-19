package ft.keraune.funtalismans.data;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.TalismanAttribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class AttributeParser {

    public static List<TalismanAttribute> parse(FunTalismans plugin, Config entry, String talismanId) {

        List<TalismanAttribute> list = new ArrayList<>();

        // ------------------------
        // ATTRIBUTE TYPE
        // ------------------------
        String typeName = entry.getString("type").toLowerCase(Locale.ROOT);

        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(typeName));
        if (attribute == null) {
            plugin.getLogger().warning("Invalid attribute '" + typeName + "' in talisman '" + talismanId + "'");
            return list;
        }

        // ------------------------
        // AMOUNT
        // ------------------------
        double amount;
        try {
            amount = entry.getDouble("amount");
        } catch (Exception e) {
            plugin.getLogger().warning("Missing/invalid 'amount' in attribute of talisman '" + talismanId + "'");
            return list;
        }

        // ------------------------
        // OPERATION
        // Default: ADD_NUMBER
        // ------------------------
        AttributeModifier.Operation op = AttributeModifier.Operation.ADD_NUMBER;

        if (entry.hasPath("operation")) {
            try {
                op = AttributeModifier.Operation.valueOf(
                        entry.getString("operation").toUpperCase(Locale.ROOT)
                );
            } catch (Exception e) {
                plugin.getLogger().warning(
                        "Invalid operation '" + entry.getString("operation") +
                                "' in talisman '" + talismanId + "', using ADD_NUMBER"
                );
            }
        }

        // ------------------------
        // SLOT
        // Puede ser:
        // slot = "HEAD"
        // slot = ["HEAD", "OFF_HAND"]
        // ------------------------
        if (!entry.hasPath("slot")) {
            plugin.getLogger().warning("Missing slot in attribute of talisman '" + talismanId + "'");
            return list;
        }

        List<EquipmentSlot> slots = new ArrayList<>();

        ConfigValueType slotType = entry.getValue("slot").valueType();
        if (slotType == ConfigValueType.STRING) {

            EquipmentSlot mapped = SlotMapper.map(entry.getString("slot"));
            if (mapped != null) slots.add(mapped);

        } else if (slotType == ConfigValueType.LIST) {

            for (String s : entry.getStringList("slot")) {
                EquipmentSlot mapped = SlotMapper.map(s.trim());
                if (mapped != null) slots.add(mapped);
            }

        } else {
            plugin.getLogger().warning("Invalid slot type for talisman '" + talismanId + "'");
            return list;
        }

        if (slots.isEmpty()) {
            plugin.getLogger().warning("No valid equipment slots for talisman '" + talismanId + "'");
            return list;
        }

        // ------------------------
        // GENERATE ATTRIBUTES
        // ------------------------
        for (EquipmentSlot slot : slots) {

            list.add(new TalismanAttribute(
                    attribute,
                    amount,
                    op,
                    slot
            ));
        }

        return list;
    }
}
