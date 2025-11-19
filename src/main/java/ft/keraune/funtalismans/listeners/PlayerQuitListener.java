package ft.keraune.funtalismans.listeners;

import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final FunTalismans plugin;

    public PlayerQuitListener(FunTalismans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Limpiar efectos cuando el jugador se desconecta
        plugin.getTalismanManager().clearEffects(player);
    }
}