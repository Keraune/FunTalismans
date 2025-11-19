package ft.keraune.funtalismans.manager;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.utils.TextUtil;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private final FunTalismans plugin;
    private final Map<String, String> messages = new HashMap<>();

    public MessageManager(FunTalismans plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messages.clear();

        Config config = plugin.getConfigHandler().getConfig("config.conf");
        if (config == null || !config.hasPath("messages")) {
            loadDefaultMessages();
            return;
        }

        Config messagesConfig = config.getConfig("messages");
        for (String key : messagesConfig.root().keySet()) {
            try {
                messages.put(key, messagesConfig.getString(key));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load message: " + key);
            }
        }

        ensureEssentialMessages();
    }

    private void loadDefaultMessages() {
        messages.putAll(getDefaultMessages());
    }

    private void ensureEssentialMessages() {
        Map<String, String> essential = getDefaultMessages();
        for (Map.Entry<String, String> entry : essential.entrySet()) {
            messages.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, String> getDefaultMessages() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("no_permission", "&cYou don't have permission.");
        defaults.put("player_not_found", "&cPlayer not found.");
        defaults.put("talisman_not_found", "&cTalisman not found.");
        defaults.put("inventory_full", "&cThe player's inventory is full!");
        defaults.put("reload_success", "&aAll configurations reloaded! &7(%ms%ms)");
        defaults.put("give_success", "&aGiven talisman &e%talisman% &ato &b%player%");
        defaults.put("unknown_subcommand", "&cUnknown subcommand.");
        defaults.put("usage_give", "&cUsage: /talisman give <player> <id>");
        defaults.put("usage_reload", "&e/talisman reload");
        defaults.put("usage_give_cmd", "&e/talisman give <player> <id>");
        return defaults;
    }

    public String getMessage(String key) {
        String message = messages.getOrDefault(key, "&cMessage not found: " + key);
        return TextUtil.color(message);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = messages.getOrDefault(key, "&cMessage not found: " + key);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return TextUtil.color(message);
    }

    public void reload() {
        loadMessages();
    }
}