package ft.keraune.funtalismans.config;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigHandler {

    private final FunTalismans plugin;
    private final Map<String, Config> configs = new HashMap<>();

    public ConfigHandler(FunTalismans plugin) {
        this.plugin = plugin;
    }

    public void init() {
        loadConfig("config.conf");
        loadConfig("talismans.conf");
        loadConfig("rarities.conf");

        if (getConfig("config.conf") == null) {
            plugin.getLogger().severe("CRITICAL: config.conf failed to load!");
        }
    }

    public Config getConfig(String name) {
        return configs.get(name);
    }

    public void loadConfig(String name) {
        try {
            File file = new File(plugin.getDataFolder(), name);

            if (!file.exists()) {
                plugin.saveResource(name, false);
                plugin.getLogger().info("Created default " + name);
            }

            Config hocon = HoconLoader.load(file);
            configs.put(name, hocon);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load " + name + ": " + e.getMessage());
        }
    }

    public void reloadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }

        try {
            Config hocon = HoconLoader.load(file);
            configs.put(name, hocon);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reload " + name + ": " + e.getMessage());
        }
    }

    public void reloadAll() {
        for (String name : new ArrayList<>(configs.keySet())) {
            reloadConfig(name);
        }
    }
}