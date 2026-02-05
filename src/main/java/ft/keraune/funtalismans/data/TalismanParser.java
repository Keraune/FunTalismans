package ft.keraune.funtalismans.data;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValueType;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.api.TalismanAttribute;
import ft.keraune.funtalismans.api.TalismanEffect;
import ft.keraune.funtalismans.api.TalismanEnchantment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class TalismanParser {

    public static Talisman parse(FunTalismans plugin, String id, Config t) {

        String name = getStringSafe(t, "name", id);
        String material = getStringSafe(t, "material", "STICK");

        boolean glow = getBooleanSafe(t, "glow", false);
        boolean unbreakable = getBooleanSafe(t, "unbreakable", false);

        // --- CUSTOM MODEL DATA PARSING ---
        Object customModelData = null;
        if (t.hasPath("custom_model_data")) {
            Object rawValue = t.getAnyRef("custom_model_data");
            if (rawValue instanceof Number) {
                customModelData = ((Number) rawValue).intValue();
            } else {
                String strVal = rawValue.toString();
                try {
                    customModelData = Integer.parseInt(strVal);
                } catch (NumberFormatException e) {
                    customModelData = strVal; // Es un String tipo "tali"
                }
            }
        }
        // -----------------------------------------------

        List<String> lore = getStringListSafe(t, "lore");

        int damage = getIntSafe(t, "damage", -1);

        // FLAGS
        List<String> flags = new ArrayList<>();
        if (t.hasPath("flags")) {
            try {
                if (t.getValue("flags").valueType().name().equalsIgnoreCase("LIST"))
                    flags.addAll(t.getStringList("flags"));
                else flags.add(t.getString("flags"));
            } catch (Exception ignored) {}
        }

        // ENCHANTMENTS - NUEVO
        List<TalismanEnchantment> enchantments = new ArrayList<>();
        if (t.hasPath("enchantments")) {
            try {
                Config enchantmentsConfig = t.getConfig("enchantments");
                enchantments.addAll(EnchantmentParser.parse(plugin, enchantmentsConfig, id));
            } catch (Exception e) {
                plugin.getLogger().severe("Invalid enchantments in talisman '" + id + "': " + e.getMessage());
            }
        }

        // ATTRIBUTES
        List<TalismanAttribute> attrs = new ArrayList<>();
        if (t.hasPath("attributes")) {
            try {
                for (Config entry : t.getConfigList("attributes"))
                    attrs.addAll(AttributeParser.parse(plugin, entry, id));
            } catch (ConfigException e) {
                plugin.getLogger().severe("Invalid attribute list in talisman '" + id + "'");
            }
        }

        // NBT
        Map<String, Object> nbt = new HashMap<>();
        if (t.hasPath("nbt")) {
            Config n = t.getConfig("nbt");
            for (String key : n.root().keySet())
                nbt.put(key, n.getAnyRef(key));
        }

        // TEXTURE (HEAD)
        if (material.equalsIgnoreCase("PLAYER_HEAD") && t.hasPath("texture"))
            nbt.put("texture", t.getString("texture"));

        // COLOR
        String color = t.hasPath("color") ? t.getString("color") : null;

        // -----------------------------
        // MULTI-SLOT EFFECTS - CORREGIDO
        // -----------------------------
        List<EquipmentSlot> effectSlots = new ArrayList<>();

        if (t.hasPath("effects.slot")) {
            try {
                ConfigValueType slotType = t.getValue("effects.slot").valueType();

                if (slotType == ConfigValueType.STRING) {
                    // Formato: slot: "offhand, mainhand"
                    String raw = t.getString("effects.slot");
                    effectSlots = SlotMapper.mapList(raw);
                } else if (slotType == ConfigValueType.LIST) {
                    // Formato: slot: ["offhand", "mainhand"]
                    List<String> slotStrings = t.getStringList("effects.slot");
                    for (String slotStr : slotStrings) {
                        EquipmentSlot slot = SlotMapper.map(slotStr.trim());
                        if (slot != null) effectSlots.add(slot);
                    }
                }

                if (effectSlots.isEmpty()) {
                    plugin.getLogger().warning("No valid effect slots found in talisman '" + id + "'");
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Invalid effects.slot format in talisman '" + id + "': " + e.getMessage());
            }
        } else {
            effectSlots.add(EquipmentSlot.HAND); // default
        }

        // -----------------------------
        // EFFECTS LIST
        // -----------------------------
        List<TalismanEffect> effects = new ArrayList<>();

        if (t.hasPath("effects.list")) {
            try {
                effects.addAll(EffectParser.parseList(plugin, t.getConfigList("effects.list"), id));
            } catch (Exception ex) {
                plugin.getLogger().severe("Invalid effects.list in talisman '" + id + "': " + ex.getMessage());
            }
        }

        return new Talisman(
                id, name, material, glow, unbreakable, lore,
                attrs, nbt, color, damage, flags,
                effectSlots, effects, enchantments, customModelData
        );
    }

    private static String getStringSafe(Config c, String path, String def) {
        try { return c.getString(path); }
        catch (Exception e) { return def; }
    }

    private static boolean getBooleanSafe(Config c, String path, boolean def) {
        try { return c.getBoolean(path); }
        catch (Exception e) { return def; }
    }

    private static int getIntSafe(Config c, String path, int def) {
        try { return c.getInt(path); }
        catch (Exception e) { return def; }
    }

    private static List<String> getStringListSafe(Config c, String path) {
        try { return c.getStringList(path); }
        catch (Exception e) { return new ArrayList<>(); }
    }
}