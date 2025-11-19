package ft.keraune.funtalismans.data;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;

import java.util.HashMap;
import java.util.Map;

public class TalismanLoader {

    private final FunTalismans plugin;

    public TalismanLoader(FunTalismans plugin) {
        this.plugin = plugin;
    }

    public Map<String, Talisman> load() {

        Map<String, Talisman> talismans = new HashMap<>();
        Config config = plugin.getConfigHandler().getConfig("talismans.conf");

        if (config == null) {
            plugin.getLogger().severe("talismans.conf NOT loaded! Cannot load talismans.");
            return talismans;
        }

        Config section;
        try {
            section = config.getConfig("talismans");
        } catch (ConfigException.Missing e) {
            plugin.getLogger().severe("Section 'talismans' NOT found in talismans.conf");
            return talismans;
        }

        for (String id : section.root().keySet()) {
            try {
                Config t = section.getConfig(id);
                Talisman talisman = TalismanParser.parse(plugin, id, t);
                talismans.put(id, talisman);

            } catch (Exception ex) {
                plugin.getLogger().severe("Failed to load talisman '" + id + "': " + ex.getMessage());
            }
        }

        return talismans;
    }
}
