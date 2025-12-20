package ft.keraune.funtalismans.items;

import de.tr7zw.nbtapi.NBTItem;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.api.TalismanAttribute;
import ft.keraune.funtalismans.api.TalismanEnchantment;
import ft.keraune.funtalismans.api.TalismanRarity;
import ft.keraune.funtalismans.manager.RarityManager;
import ft.keraune.funtalismans.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.logging.Logger;

public class TalismanItemBuilder {

    private static final Logger LOGGER = Bukkit.getLogger();

    public static ItemStack build(Talisman t) {
        Material mat = Material.matchMaterial(t.getMaterial());
        if (mat == null) mat = Material.STICK;

        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // APLICAR PROPIEDADES BÁSICAS PRIMERO
        meta.setDisplayName(TextUtil.color(t.getName()));
        meta.setLore(t.getLore().stream().map(TextUtil::color).toList());
        meta.setUnbreakable(t.isUnbreakable());

        // APLICAR COLOR DE RAREZA AL NOMBRE
        TalismanRarity rarity = getRarity(t);
        if (rarity != null) {
            String coloredName = rarity.color() + t.getName();
            meta.setDisplayName(TextUtil.color(coloredName));
        }

        // COLOR UNIVERSAL
        if (t.getColor() != null) {
            try {
                meta = ColorApplier.apply(meta, t.getColor());
            } catch (Exception e) {
                LOGGER.warning("Failed to apply color for talisman '" + t.getId() + "': " + e.getMessage());
            }
        } else if (t.getNbt().containsKey("color")) {
            try {
                meta = ColorApplier.apply(meta, t.getNbt().get("color").toString());
            } catch (Exception e) {
                LOGGER.warning("Failed to apply color from NBT for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // DAMAGE
        if (t.getDamage() >= 0 && meta instanceof Damageable dm) {
            try {
                dm.setDamage(t.getDamage());
                meta = dm;
            } catch (Exception e) {
                LOGGER.warning("Failed to set damage for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // FLAGS
        for (String flagName : t.getFlags()) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
            } catch (Exception e) {
                LOGGER.warning("Invalid item flag '" + flagName + "' for talisman '" + t.getId() + "'");
            }
        }

        // ENCHANTMENTS
        for (TalismanEnchantment enchant : t.getEnchantments()) {
            if (enchant.getEnchantment() != null) {
                try {
                    meta.addEnchant(enchant.getEnchantment(), enchant.getLevel(), true);
                } catch (Exception e) {
                    LOGGER.warning("Failed to apply enchantment " + enchant.getEnchantment() +
                            " for talisman '" + t.getId() + "': " + e.getMessage());
                }
            }
        }

        // GLOW (controlado individualmente por cada talismán)
        if (t.isGlow()) {
            try {
                applyGlow(meta);
            } catch (Exception e) {
                LOGGER.warning("Failed to apply glow for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // ATRIBUTOS (con UUID determinístico)
        for (TalismanAttribute attr : t.getAttributes()) {
            if (attr.getType() != null && attr.getSlot() != null) {
                try {
                    // UUID determinístico basado en el ID del talismán y el atributo
                    UUID deterministicUUID = UUID.nameUUIDFromBytes(
                            (t.getId() + ":" + attr.getType().getKey().getKey() + ":" + attr.getSlot().name()).getBytes()
                    );

                    AttributeModifier modifier = new AttributeModifier(
                            deterministicUUID, // UUID determinístico en lugar de random
                            t.getId(),
                            attr.getAmount(),
                            attr.getOperation(),
                            attr.getSlot()
                    );
                    meta.addAttributeModifier(attr.getType(), modifier);
                } catch (Exception e) {
                    LOGGER.warning("Failed to apply attribute " + attr.getType() +
                            " for talisman '" + t.getId() + "': " + e.getMessage());
                }
            }
        }

        // PERSISTENT DATA CONTAINER
        try {
            var cfg = FunTalismans.getInstance().getConfigManager();
            meta.getPersistentDataContainer().set(
                    cfg.talismansKey(),
                    cfg.stringType(),
                    t.getId()
            );
        } catch (Throwable e) {
            LOGGER.warning("Failed to set PDC for talisman '" + t.getId() + "': " + e.getMessage());
        }

        // APLICAR META PRIMERO
        try {
            item.setItemMeta(meta);
        } catch (Exception e) {
            LOGGER.warning("Failed to set item meta for talisman '" + t.getId() + "': " + e.getMessage());
            return new ItemStack(mat, 1);
        }

        // TEXTURA PARA CABEZAS - APLICAR DESPUÉS del meta básico
        if (item.getItemMeta() instanceof SkullMeta skullMeta && t.getNbt().containsKey("texture")) {
            try {
                String texture = t.getNbt().get("texture").toString();
                skullMeta = SkullUtil.applyTexture(skullMeta, texture);
                item.setItemMeta(skullMeta);
            } catch (Exception e) {
                LOGGER.warning("Failed to apply custom texture for talisman '" + t.getId() + "': " + e.getMessage());
            }
        }

        // NBT EXTRA — sin sobrescribir ni duplicar IDs internos
        try {
            NBTItem nbt = new NBTItem(item, true);

            // ID usado si el CONF tiene "id"
            if (t.getNbt().containsKey("id")) {
                String citId = t.getNbt().get("id").toString();
                nbt.setString("talisman_id", citId);  // Grabar SOLO en raíz
            }

            // Aplicar NBT extra sin sobrescribir los internos
            t.getNbt().forEach((key, value) -> {
                if (key.equalsIgnoreCase("id") ||
                        key.equalsIgnoreCase("talisman_id") ||
                        key.equalsIgnoreCase("publicbukkitvalues")) {
                    return;
                }

                try {
                    nbt.setObject(key, value);
                } catch (Exception ignored) {}
            });

            return nbt.getItem();

        } catch (Throwable e) {
            return item;
        }
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
            LOGGER.warning("Failed to apply glow fallback: " + e.getMessage());
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