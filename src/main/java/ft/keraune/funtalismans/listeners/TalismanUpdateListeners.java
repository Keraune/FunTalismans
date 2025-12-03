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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class TalismanUpdateListeners implements Listener {

    private final TalismanManager talismanManager;
    private final FunTalismans plugin;

    public TalismanUpdateListeners(FunTalismans plugin) {
        this.plugin = plugin;
        this.talismanManager = plugin.getTalismanManager();
    }

    // ==========================================
    // PLAYER JOIN → update inv, ender chest, open inventories, cursor
    // ==========================================
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateAllForPlayer(player);
        }, 20L);
    }

    // ==========================================
    // OPEN CONTAINER (chest, barrel, shulker, etc.)
    // ==========================================
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (inv == null) return;

        Object holder = inv.getHolder();

        // Caso 1: Contenedor del mundo
        if (holder instanceof BlockState blockState && blockState instanceof Container) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    talismanManager.updateInventoryTalismans(inv)
            );
            return;
        }

        // Caso 2: Ender chest del jugador
        if (holder instanceof EnderChest) {
            Player player = (Player) event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () ->
                    talismanManager.updateInventoryTalismans(player.getEnderChest())
            );
        }

        // Caso 3: Inventarios especiales (crafting, anvil, smithing, etc.)
        Bukkit.getScheduler().runTask(plugin, () ->
                updateOpenInventory((Player) event.getPlayer())
        );
    }

    // ==========================================
    // PICKUP → update before adding to inventory
    // ==========================================
    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();
        if (stack == null || stack.getType() == Material.AIR) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            // Usar el método seguro
            ItemStack updated = talismanManager.updateItemStackSafely(stack);
            if (updated != stack) { // Si cambió
                item.setItemStack(updated);
            }
        });
    }

    // ==========================================
    // ACTUALIZAR DESPUÉS DE UN CLICK - CORREGIDO
    // ==========================================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // NO ACTUALIZAR DURANTE EL CLICK - solo después
        // Esto previene que los items desaparezcan
    }

    // ==========================================
    // ACTUALIZAR DESPUÉS DE UN DRAG - CORREGIDO
    // ==========================================
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // NO ACTUALIZAR DURANTE EL DRAG - solo después
    }

    // ==========================================
    // ACTUALIZAR DESPUÉS DE CERRAR INVENTARIO
    // ==========================================
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Esperar 1 tick para que todo se estabilice
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateAllForPlayer(player);
        }, 1L);
    }

    // ==========================================
    // ACTUALIZAR AL CAMBIAR DE ITEM EN LA MANO
    // ==========================================
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Esperar 1 tick para que el cambio se complete
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Solo actualizar el item en la mano
            ItemStack inHand = player.getInventory().getItem(event.getNewSlot());
            if (inHand != null && !inHand.getType().isAir()) {
                ItemStack updated = talismanManager.updateItemStackSafely(inHand);
                if (updated != inHand) {
                    player.getInventory().setItem(event.getNewSlot(), updated);
                    player.updateInventory();
                }
            }
        }, 1L);
    }

    // ===========================================================
    // MÉTODO CENTRAL: Actualiza absolutamente TODO
    // ===========================================================
    private void updateAllForPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        try {
            // Inventario normal
            talismanManager.updateInventoryTalismans(player.getInventory());

            // Ender chest
            talismanManager.updateInventoryTalismans(player.getEnderChest());

            // Cursor item - usar método seguro
            ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                ItemStack updated = talismanManager.updateItemStackSafely(cursor);
                if (updated != cursor) {
                    player.setItemOnCursor(updated);
                }
            }

            // Crafting grid + cualquier menú abierto
            updateOpenInventory(player);

        } catch (Exception e) {
            plugin.getLogger().warning("Error updating player " + player.getName() + ": " + e.getMessage());
        }
    }

    // ===========================================================
    // Cursor item - método auxiliar
    // ===========================================================
    private void updateCursorItem(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            ItemStack updated = talismanManager.updateItemStackSafely(cursor);
            if (updated != cursor) {
                player.setItemOnCursor(updated);
            }
        }
    }

    // ===========================================================
    // Actualizar inventarios abiertos (crafting, anvil, smithing…)
    // ===========================================================
    private void updateOpenInventory(Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return;

        try {
            // Inventario superior (crafting, anvil, etc.)
            Inventory top = view.getTopInventory();
            if (top != null) {
                talismanManager.updateInventoryTalismans(top);
            }

            // Inventario inferior (player inventory shown inside the UI)
            Inventory bottom = view.getBottomInventory();
            if (bottom != null) {
                talismanManager.updateInventoryTalismans(bottom);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating open inventory for " + player.getName() + ": " + e.getMessage());
        }
    }
}