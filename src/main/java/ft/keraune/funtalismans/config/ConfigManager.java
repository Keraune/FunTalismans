package ft.keraune.funtalismans.config;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class ConfigManager {

    private final FunTalismans plugin;

    private String prefix;
    private NamespacedKey talismansKey;

    public ConfigManager(FunTalismans plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        Config c = plugin.getConfigHandler().getConfig("config.conf");

        prefix = c.getString("plugin.prefix");

        // Inicializar la key usada por PDC para identificar talismans en ItemMeta
        talismansKey = new NamespacedKey(plugin, "talisman_id");
    }

    public void reload() {
        load();
    }

    public String getPrefix() {
        return prefix;
    }

    public NamespacedKey talismansKey() {
        return talismansKey;
    }

    public PersistentDataType<String, String> stringType() {
        return PersistentDataType.STRING;
    }
}