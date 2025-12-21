package ft.keraune.funtalismans;

import ft.keraune.funtalismans.commands.TalismanCommand;
import ft.keraune.funtalismans.commands.TalismanTabCompleter;
import ft.keraune.funtalismans.config.ConfigHandler;
import ft.keraune.funtalismans.config.ConfigManager;
import ft.keraune.funtalismans.listeners.PlayerQuitListener;
import ft.keraune.funtalismans.listeners.TalismanUpdateListeners;
import ft.keraune.funtalismans.manager.TalismanManager;
import ft.keraune.funtalismans.manager.RarityManager;
import ft.keraune.funtalismans.manager.MessageManager;
import ft.keraune.funtalismans.effects.EffectHandler;
import ft.keraune.funtalismans.listeners.BlockPlaceListener;
import ft.keraune.funtalismans.utils.TextUtil;
import ft.keraune.funtalismans.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FunTalismans extends JavaPlugin {

    private static FunTalismans instance;

    private TalismanManager talismanManager;
    private ConfigHandler configHandler;
    private ConfigManager configManager;
    private EffectHandler effectHandler;
    private RarityManager rarityManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        silenceMojangLogs();

        // Inicializar en orden correcto
        configHandler = new ConfigHandler(this);
        configHandler.init();

        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        rarityManager = new RarityManager(this);
        talismanManager = new TalismanManager(this);
        talismanManager.loadTalismans();

        effectHandler = new EffectHandler(this);

        // Registrar comandos
        if (getCommand("talisman") != null) {
            getCommand("talisman").setExecutor(new TalismanCommand());
            getCommand("talisman").setTabCompleter(new TalismanTabCompleter());
        }

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new TalismanUpdateListeners(this), this);

        new UpdateChecker(this, "funtalismans").checkForUpdates();

        getLogger().info("FunTalismans enabled!");
    }

    public void reloadAll() {
        configHandler.reloadAll();
        configManager.reload();
        messageManager.reload();
        rarityManager.reload();
        talismanManager.reloadTalismans();
    }

    private void silenceMojangLogs() {
        try {
            java.util.logging.Logger.getLogger("com.mojang.authlib").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.mojang.authlib.yggdrasil").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.mojang").setLevel(java.util.logging.Level.SEVERE);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDisable() {
        cleanupAllPlayerEffects();
        getLogger().info("FunTalismans disabled!");
    }

    private void cleanupAllPlayerEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            cleanupPlayerEffects(player);
        }
    }

    private void cleanupPlayerEffects(Player player) {
        try {
            if (effectHandler != null) {
                effectHandler.cleanupPlayerEffects(player);
            }
        } catch (Exception ignored) {}
    }

    public static FunTalismans getInstance() { return instance; }

    public TalismanManager getTalismanManager() { return talismanManager; }
    public ConfigHandler getConfigHandler() { return configHandler; }
    public ConfigManager getConfigManager() { return configManager; }
    public EffectHandler getEffectHandler() { return effectHandler; }
    public RarityManager getRarityManager() { return rarityManager; }
    public MessageManager getMessageManager() { return messageManager; }

    public String getPrefix() {
        return TextUtil.color(configHandler.getConfig("config.conf").getString("plugin.prefix"));
    }
}