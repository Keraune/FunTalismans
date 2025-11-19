package ft.keraune.funtalismans.manager;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.TalismanRarity;

import java.util.HashMap;
import java.util.Map;

public class RarityManager {

    private final FunTalismans plugin;
    private final Map<String, TalismanRarity> rarities = new HashMap<>();

    public RarityManager(FunTalismans plugin) {
        this.plugin = plugin;
        loadRarities();
    }

    public void loadRarities() {
        rarities.clear();
        Config config = plugin.getConfigHandler().getConfig("rarities.conf");

        if (config == null || !config.hasPath("rarities")) {
            loadDefaultRarities();
            return;
        }

        Config raritiesSection = config.getConfig("rarities");
        for (String rarityId : raritiesSection.root().keySet()) {
            try {
                Config rarityConfig = raritiesSection.getConfig(rarityId);

                TalismanRarity rarity = new TalismanRarity(
                        rarityId,
                        rarityConfig.getString("name"),
                        rarityConfig.getString("color")
                );

                rarities.put(rarityId.toLowerCase(), rarity);
                // SILENCIADO: No mostrar logs individuales

            } catch (Exception e) {
                // SILENCIADO: Solo errores críticos
            }
        }
    }

    private void loadDefaultRarities() {
        rarities.put("common", new TalismanRarity("common", "&fComún", "&7"));
        rarities.put("uncommon", new TalismanRarity("uncommon", "&aPoco Común", "&a"));
        rarities.put("rare", new TalismanRarity("rare", "&9Raro", "&9"));
        rarities.put("epic", new TalismanRarity("epic", "&5Épico", "&5"));
        rarities.put("legendary", new TalismanRarity("legendary", "&6Legendario", "&6"));
        // SILENCIADO: No mostrar warning
    }

    public TalismanRarity getRarity(String id) {
        if (id == null) return rarities.get("common");
        return rarities.getOrDefault(id.toLowerCase(), rarities.get("common"));
    }

    public Map<String, TalismanRarity> getRarities() {
        return new HashMap<>(rarities);
    }

    public void reload() {
        // SILENCIADO: No mostrar logs
        loadRarities();
    }
}