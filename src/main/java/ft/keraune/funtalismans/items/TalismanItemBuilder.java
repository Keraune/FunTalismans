package ft.keraune.funtalismans.items;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.api.TalismanAttribute;
import ft.keraune.funtalismans.api.TalismanEnchantment;
import ft.keraune.funtalismans.api.TalismanRarity;
import ft.keraune.funtalismans.manager.RarityManager;
import ft.keraune.funtalismans.utils.TextUtil;
// IMPORTS NATIVOS DE PAPER (1.21.4+)
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.logging.Logger;

public class TalismanItemBuilder {

    // Logger seguro desde la instancia principal
    private static Logger getLogger() {
        return FunTalismans.getInstance().getLogger();
    }

    public static ItemStack build(Talisman t) {
        Material mat = Material.matchMaterial(t.getMaterial());
        if (mat == null) mat = Material.STICK;

        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // 1. PROPIEDADES BÁSICAS
        meta.setDisplayName(TextUtil.color(t.getName()));
        meta.setLore(t.getLore().stream().map(TextUtil::color).toList());
        meta.setUnbreakable(t.isUnbreakable());

        // 2. CUSTOM MODEL DATA (Enteros)
        // La parte de Strings la aplicamos al final usando DataComponents
        Object cmd = t.getCustomModelData();
        if (cmd instanceof Integer i) {
            try {
                meta.setCustomModelData(i);
            } catch (Throwable ignored) {}
        }

        // 3. RAREZA
        TalismanRarity rarity = getRarity(t);
        if (rarity != null) {
            String coloredName = rarity.color() + t.getName();
            meta.setDisplayName(TextUtil.color(coloredName));
        }

        // 4. COLOR
        if (t.getColor() != null) {
            try {
                meta = ColorApplier.apply(meta, t.getColor());
            } catch (Exception e) {
                getLogger().warning("Failed to apply color for talisman '" + t.getId() + "': " + e.getMessage());
            }
        } else if (t.getNbt().containsKey("color")) {
            try {
                meta = ColorApplier.apply(meta, t.getNbt().get("color").toString());
            } catch (Exception e) {
                getLogger().warning("Failed to apply color from NBT for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // 5. DAÑO
        if (t.getDamage() >= 0 && meta instanceof Damageable dm) {
            try {
                dm.setDamage(t.getDamage());
                meta = dm;
            } catch (Exception e) {
                getLogger().warning("Failed to set damage for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // 6. FLAGS
        for (String flagName : t.getFlags()) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
            } catch (Exception e) {
                getLogger().warning("Invalid item flag '" + flagName + "' for talisman '" + t.getId() + "'");
            }
        }

        // 7. ENCANTAMIENTOS
        for (TalismanEnchantment enchant : t.getEnchantments()) {
            if (enchant.getEnchantment() != null) {
                try {
                    meta.addEnchant(enchant.getEnchantment(), enchant.getLevel(), true);
                } catch (Exception e) {
                    getLogger().warning("Failed to apply enchantment " + enchant.getEnchantment() +
                            " for talisman '" + t.getId() + "': " + e.getMessage());
                }
            }
        }

        // 8. GLOW
        if (t.isGlow()) {
            try {
                applyGlow(meta);
            } catch (Exception e) {
                getLogger().warning("Failed to apply glow for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // 9. ATRIBUTOS
        for (TalismanAttribute attr : t.getAttributes()) {
            if (attr.getType() != null && attr.getSlot() != null) {
                try {
                    UUID deterministicUUID = UUID.nameUUIDFromBytes(
                            (t.getId() + ":" + attr.getType().getKey().getKey() + ":" + attr.getSlot().name()).getBytes()
                    );

                    AttributeModifier modifier = new AttributeModifier(
                            deterministicUUID,
                            t.getId(),
                            attr.getAmount(),
                            attr.getOperation(),
                            attr.getSlot()
                    );
                    meta.addAttributeModifier(attr.getType(), modifier);
                } catch (Exception e) {
                    getLogger().warning("Failed to apply attribute " + attr.getType() +
                            " for talisman '" + t.getId() + "': " + e.getMessage());
                }
            }
        }

        // 10. TEXTURAS (CABEZAS)
        // Nota: Asumimos que SkullUtil maneja la textura internamente
        if (meta instanceof SkullMeta skullMeta && t.getNbt().containsKey("texture")) {
            try {
                String texture = t.getNbt().get("texture").toString();
                skullMeta = SkullUtil.applyTexture(skullMeta, texture);
                meta = skullMeta; // Importante actualizar referencia
            } catch (Exception e) {
                getLogger().warning("Failed to apply custom texture for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // 11. DATOS PERSISTENTES (Reemplazo Nativo de NBT)
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        var cfg = FunTalismans.getInstance().getConfigManager();

        // ID Principal
        pdc.set(cfg.talismansKey(), cfg.stringType(), t.getId());

        // ID Extra (compatibilidad)
        if (t.getNbt().containsKey("id")) {
            String citId = t.getNbt().get("id").toString();
            NamespacedKey citKey = new NamespacedKey(FunTalismans.getInstance(), "talisman_id");
            pdc.set(citKey, PersistentDataType.STRING, citId);
        }

        // Otros datos extra definidos en la config (convertidos a PDC)
        t.getNbt().forEach((key, value) -> {
            if (key.equalsIgnoreCase("id") ||
                    key.equalsIgnoreCase("texture") ||
                    key.equalsIgnoreCase("rarity") ||
                    key.equalsIgnoreCase("color")) {
                return;
            }

            try {
                // Creamos una key para el dato extra
                NamespacedKey extraKey = new NamespacedKey(FunTalismans.getInstance(), key.toLowerCase());

                // Detectamos tipo básico para guardarlo correctamente
                if (value instanceof String s) {
                    pdc.set(extraKey, PersistentDataType.STRING, s);
                } else if (value instanceof Integer i) {
                    pdc.set(extraKey, PersistentDataType.INTEGER, i);
                } else if (value instanceof Double d) {
                    pdc.set(extraKey, PersistentDataType.DOUBLE, d);
                } else if (value instanceof Boolean b) {
                    // PDC no tiene Boolean nativo, usamos Byte 0/1
                    pdc.set(extraKey, PersistentDataType.BYTE, (byte) (b ? 1 : 0));
                }
            } catch (Exception ignored) {}
        });

        // APLICAR META FINAL
        item.setItemMeta(meta);

        // ========================================================================
        // 12. CUSTOM MODEL DATA STRING (API NATIVA DE PAPER)
        // ========================================================================
        if (cmd instanceof String s) {
            try {
                // Usamos la API de DataComponents de Paper (1.20.5+)
                // Esto inyecta el valor directamente en el componente minecraft:custom_model_data
                item.setData(DataComponentTypes.CUSTOM_MODEL_DATA,
                        CustomModelData.customModelData().addString(s).build()
                );
            } catch (Throwable e) {
                // Si esto falla, probablemente el servidor no es Paper o es una versión antigua
                getLogger().warning("Failed to apply String CustomModelData (Require Paper 1.21.4+): " + e.getMessage());
            }
        }

        return item;
    }

    private static TalismanRarity getRarity(Talisman t) {
        RarityManager rarityManager = FunTalismans.getInstance().getRarityManager();
        String rarityId = (String) t.getNbt().get("rarity");
        return rarityManager.getRarity(rarityId);
    }

    private static void applyGlow(ItemMeta meta) {
        boolean isPaper = isPaperServer();

        if (isPaper) {
            try {
                meta.setEnchantmentGlintOverride(true);
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            } catch (Throwable e) {
                applySpigotGlow(meta);
            }
        } else {
            applySpigotGlow(meta);
        }
    }

    private static void applySpigotGlow(ItemMeta meta) {
        try {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Throwable e) {
            getLogger().warning("Failed to apply glow fallback: " + e.getMessage());
        }
    }

    private static boolean isPaperServer() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}