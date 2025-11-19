package ft.keraune.funtalismans.listeners;

import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class CraftListener implements Listener {

    private final FunTalismans plugin;

    public CraftListener(FunTalismans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                if (pdc.has(plugin.getConfigManager().talismansKey())) {
                    event.getInventory().setResult(null);
                    break;
                }
            }
        }
    }
}