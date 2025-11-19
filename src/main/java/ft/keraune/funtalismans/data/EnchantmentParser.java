package ft.keraune.funtalismans.data;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.TalismanEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnchantmentParser {

    public static List<TalismanEnchantment> parse(FunTalismans plugin, Config config, String talismanId) {
        List<TalismanEnchantment> enchantments = new ArrayList<>();

        if (config == null) return enchantments;

        for (String enchantmentName : config.root().keySet()) {
            try {
                // Obtener el encantamiento de Bukkit
                String normalizedName = enchantmentName.toUpperCase(Locale.ROOT);
                Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(normalizedName.toLowerCase(Locale.ROOT)));

                if (enchantment == null) {
                    plugin.getLogger().warning("Invalid enchantment '" + enchantmentName + "' in talisman '" + talismanId + "'");
                    continue;
                }

                // Obtener el nivel
                int level = config.getInt(enchantmentName);
                if (level < 1) {
                    plugin.getLogger().warning("Enchantment level must be at least 1 for '" + enchantmentName + "' in talisman '" + talismanId + "'");
                    continue;
                }

                enchantments.add(new TalismanEnchantment(enchantment, level));

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to parse enchantment '" + enchantmentName + "' in talisman '" + talismanId + "': " + e.getMessage());
            }
        }

        return enchantments;
    }
}