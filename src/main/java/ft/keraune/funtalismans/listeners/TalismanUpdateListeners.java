package ft.keraune.funtalismans.listeners;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.manager.TalismanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.Inventory;

public class TalismanUpdateListeners implements Listener {

    private final TalismanManager talismanManager;

    public TalismanUpdateListeners(FunTalismans plugin) {
        this.talismanManager = plugin.getTalismanManager();
    }

    // Jugadores que entran → actualizar talismanes en inventario + EnderChest
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(FunTalismans.getInstance(), () -> {
            talismanManager.updateInventoryTalismans(player.getInventory());
            talismanManager.updateInventoryTalismans(player.getEnderChest());
        }, 20L);
    }

    // Contenedores abiertos → actualizar talismanes dentro del inventario del contenedor
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        Inventory inv = event.getInventory();
        if (inv == null) return;

        Object holder = inv.getHolder();

        // Caso 1: Bloques contenedores del mundo (cofres, barriles, shulkers, etc.)
        if (holder instanceof BlockState blockState && blockState instanceof Container) {
            Bukkit.getScheduler().runTask(FunTalismans.getInstance(), () ->
                    talismanManager.updateInventoryTalismans(inv)
            );
            return;
        }

        // Caso 2: Ender Chest personal del jugador
        if (holder instanceof EnderChest) {
            Player player = (Player) event.getPlayer();
            Bukkit.getScheduler().runTask(FunTalismans.getInstance(), () ->
                    talismanManager.updateInventoryTalismans(player.getEnderChest())
            );
        }
    }

    // Ítems recogidos → actualizar talismán antes de darlo al jugador
    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        Item item = event.getItem();
        if (item.getItemStack() == null || item.getItemStack().getType() == Material.AIR)
            return;

        Bukkit.getScheduler().runTask(FunTalismans.getInstance(), () ->
                talismanManager.updateItemStack(item.getItemStack())
        );
    }
}
