package ft.keraune.funtalismans.listeners;

import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class BlockPlaceListener implements Listener {

    private final FunTalismans plugin;

    public BlockPlaceListener(FunTalismans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (pdc.has(plugin.getConfigManager().talismansKey())) {
            event.setCancelled(true);
        }
    }
}