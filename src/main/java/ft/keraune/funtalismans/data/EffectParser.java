package ft.keraune.funtalismans.data;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.TalismanEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EffectParser {

    /**
     *  Parsea una lista de Configs (effects.list)
     */
    public static List<TalismanEffect> parseList(FunTalismans plugin, List<? extends Config> list, String id) {
        List<TalismanEffect> effects = new ArrayList<>();

        for (Config section : list) {
            try {
                String typeName = section.getString("type").toUpperCase();
                PotionEffectType type = PotionEffectType.getByName(typeName);

                if (type == null) {
                    plugin.getLogger().warning("Invalid effect type '" + typeName +
                            "' in talisman '" + id + "'");
                    continue;
                }

                int amplifier = section.hasPath("amplifier")
                        ? section.getInt("amplifier")
                        : 0;

                effects.add(new TalismanEffect(type, amplifier));

            } catch (Exception e) {
                plugin.getLogger().warning("Invalid effect entry in talisman '" + id + "'");
            }
        }

        return effects;
    }

    /**
     *  Parseo alternativo cuando effects es un objeto completo (no lista directa)
     */
    public static List<TalismanEffect> parse(FunTalismans plugin, Config root, String id) {
        if (!root.hasPath("effects.list"))
            return new ArrayList<>();

        return parseList(plugin, root.getConfigList("effects.list"), id);
    }
}
