package ft.keraune.funtalismans;

import ft.keraune.funtalismans.commands.TalismanCommand;
import ft.keraune.funtalismans.commands.TalismanTabCompleter;
import ft.keraune.funtalismans.config.ConfigHandler;
import ft.keraune.funtalismans.config.ConfigManager;
import ft.keraune.funtalismans.listeners.CraftListener;
import ft.keraune.funtalismans.listeners.PlayerQuitListener;
import ft.keraune.funtalismans.manager.TalismanManager;
import ft.keraune.funtalismans.manager.EffectManager;
import ft.keraune.funtalismans.manager.RarityManager;
import ft.keraune.funtalismans.manager.MessageManager;
import ft.keraune.funtalismans.effects.EffectHandler;
import ft.keraune.funtalismans.listeners.BlockPlaceListener;
import ft.keraune.funtalismans.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FunTalismans extends JavaPlugin {

    private static FunTalismans instance;

    private TalismanManager talismanManager;
    private ConfigHandler configHandler;
    private ConfigManager configManager;
    private EffectManager effectManager;
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
        messageManager = new MessageManager(this); // Después de configHandler
        rarityManager = new RarityManager(this);
        talismanManager = new TalismanManager(this);
        talismanManager.loadTalismans();

        effectManager = new EffectManager(this);
        effectHandler = new EffectHandler(this);

        // Registrar comandos y eventos
        Bukkit.getScheduler().runTask(this, () -> {
            if (getCommand("talisman") != null) {
                getCommand("talisman").setExecutor(new TalismanCommand());
                getCommand("talisman").setTabCompleter(new TalismanTabCompleter());
            }
        });

        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this); // NUEVO

        getLogger().info("FunTalismans enabled!");
    }

    // MÉTODO PARA RELOAD COMPLETO
    public void reloadAll() {
        // 1. Recargar configuraciones
        configHandler.reloadAll();
        configManager.reload();

        // 2. Recargar mensajes
        messageManager.reload();

        // 3. Recargar managers
        rarityManager.reload();

        // 4. Recargar talismanes
        talismanManager.reloadTalismans();
    }

    private void silenceMojangLogs() {
        try {
            java.util.logging.Logger authLibLogger = java.util.logging.Logger.getLogger("com.mojang.authlib");
            authLibLogger.setLevel(java.util.logging.Level.SEVERE);

            java.util.logging.Logger.getLogger("com.mojang.authlib.yggdrasil").setLevel(java.util.logging.Level.SEVERE);
            java.util.logging.Logger.getLogger("com.mojang").setLevel(java.util.logging.Level.SEVERE);

        } catch (Exception e) {
            // Empty catch intencional
        }
    }

    @Override
    public void onDisable() {
        // LIMPIAR EFECTOS DE TODOS LOS JUGADORES ANTES DE DESACTIVAR
        cleanupAllPlayerEffects();

        getLogger().info("FunTalismans disabled!");
    }

    /**
     * Limpia los efectos de talismanes de todos los jugadores en línea
     */
    private void cleanupAllPlayerEffects() {
        getLogger().info("Cleaning up talisman effects from all players...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            cleanupPlayerEffects(player);
        }

        getLogger().info("Talisman effects cleanup completed!");
    }

    /**
     * Limpia los efectos de talismanes de un jugador específico
     */
    private void cleanupPlayerEffects(Player player) {
        try {
            if (talismanManager != null) {
                talismanManager.clearEffects(player);
                getLogger().info("Cleaned effects for player: " + player.getName());
            }
        } catch (Exception e) {
            getLogger().warning("Failed to clean effects for player: " + player.getName() + " - " + e.getMessage());
        }
    }

    public static FunTalismans getInstance() { return instance; }

    public TalismanManager getTalismanManager() { return talismanManager; }
    public ConfigHandler getConfigHandler() { return configHandler; }
    public ConfigManager getConfigManager() { return configManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public EffectHandler getEffectHandler() { return effectHandler; }
    public RarityManager getRarityManager() { return rarityManager; }
    public MessageManager getMessageManager() { return messageManager; }

    public String getPrefix() {
        return TextUtil.color(configHandler.getConfig("config.conf").getString("plugin.prefix"));
    }
}