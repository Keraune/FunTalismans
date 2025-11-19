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
        // Este listener ahora solo maneja crafting no autorizado de talismanes
        // El crafting autorizado se maneja en CraftingManager

        // Si el resultado ya es un talismán (de nuestras recetas), permitirlo
        if (event.getRecipe() != null && event.getInventory().getResult() != null) {
            ItemStack result = event.getInventory().getResult();
            if (result.hasItemMeta()) {
                ItemMeta meta = result.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                // Si el resultado es un talismán de nuestro sistema, es una receta autorizada
                if (pdc.has(plugin.getConfigManager().talismansKey())) {
                    return; // Permitir crafting autorizado
                }
            }
        }

        // Prevenir crafting no autorizado que use talismanes como ingredientes
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                if (pdc.has(plugin.getConfigManager().talismansKey())) {
                    // Solo cancelar si no es una receta autorizada de nuestro sistema
                    if (!isAuthorizedTalismanCraft(event)) {
                        event.getInventory().setResult(null);
                        break;
                    }
                }
            }
        }
    }

    private boolean isAuthorizedTalismanCraft(PrepareItemCraftEvent event) {
        // Verificar si esta es una receta autorizada de nuestro sistema de crafting
        // Esto se maneja principalmente en CraftingManager
        return event.getRecipe() != null &&
                event.getRecipe() instanceof org.bukkit.inventory.ShapedRecipe;
    }
}