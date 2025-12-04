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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TalismanUpdateListeners implements Listener {

    private final TalismanManager talismanManager;
    private final FunTalismans plugin;

    // Cooldown para evitar múltiples actualizaciones rápidas
    private final Map<UUID, Long> updateCooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 100; // 100ms de cooldown
    private static final long COOLDOWN_MS_FAST = 50; // 50ms para acciones rápidas
    private static final long FORCE_UPDATE_COOLDOWN_MS = 10; // 10ms para force update

    public TalismanUpdateListeners(FunTalismans plugin) {
        this.plugin = plugin;
        this.talismanManager = plugin.getTalismanManager();
    }

    // ==========================================
    // PLAYER JOIN → update solo si es necesario
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Esperar para que todo se cargue completamente
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateAllForPlayerQuietly(player);
            }
        }, 40L); // 2 segundos
    }

    // ==========================================
    // OPEN CONTAINER (chest, barrel, shulker, etc.)
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS)) return;

        Inventory inv = event.getInventory();
        Object holder = inv.getHolder();

        // Caso 1: Contenedor del mundo
        if (holder instanceof BlockState blockState && blockState instanceof Container) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    talismanManager.updateInventoryTalismansQuietly(inv);
                }
            }, 1L);
            if (!forceUpdate) updateCooldown(player);
            return;
        }

        // Caso 2: Ender chest del jugador
        if (holder instanceof EnderChest) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    talismanManager.updateInventoryTalismansQuietly(player.getEnderChest());
                }
            }, 1L);
            if (!forceUpdate) updateCooldown(player);
            return;
        }

        // Caso 3: Inventarios especiales
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateOpenInventoryQuietly(player);
            }
        }, 1L);
        if (!forceUpdate) updateCooldown(player);
    }

    // ==========================================
    // PICKUP → update solo si es necesario
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled()) return;

        Item item = event.getItem();
        ItemStack stack = item.getItemStack();
        if (stack == null || stack.getType() == Material.AIR) return;

        // Solo procesar si es un talismán
        if (talismanManager.getFromItem(stack) == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Usar el método seguro solo si es necesario
            if (talismanManager.needsUpdate(stack)) {
                ItemStack updated = talismanManager.updateItemStackSafely(stack);
                if (!talismanManager.areItemsEqual(stack, updated)) {
                    item.setItemStack(updated);
                }
            }
        }, 1L);
    }

    // ==========================================
    // NUEVO: Pickup completado (evento más específico)
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPickupItem(org.bukkit.event.entity.EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack stack = event.getItem().getItemStack();
        if (talismanManager.getFromItem(stack) == null) return;

        // Solo actualizar después del pickup si es necesario
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                boolean forceUpdate = isForceUpdateMode();
                if (forceUpdate || canUpdate(player, COOLDOWN_MS_FAST)) {
                    updateAllForPlayerQuietly(player);
                    if (!forceUpdate) updateCooldown(player);
                }
            }
        }, 1L);
    }

    // ==========================================
    // INVENTORY CLICK - solo si es necesario
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Solo procesar si los items involucrados son talismanes
        boolean hasTalisman = false;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (current != null && talismanManager.getFromItem(current) != null) {
            hasTalisman = true;
        }
        if (cursor != null && talismanManager.getFromItem(cursor) != null) {
            hasTalisman = true;
        }

        if (!hasTalisman) return;

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS_FAST)) return;

        // Actualizar después del click solo si es necesario
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateAllForPlayerQuietly(player);
            }
        }, 1L);

        if (!forceUpdate) updateCooldown(player);
    }

    // ==========================================
    // INVENTORY DRAG - solo si es necesario
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Verificar si algún item en el drag es un talismán
        boolean hasTalisman = false;
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && talismanManager.getFromItem(item) != null) {
                hasTalisman = true;
                break;
            }
        }

        if (!hasTalisman) return;

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS)) return;

        // Actualizar después del drag solo si es necesario
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateAllForPlayerQuietly(player);
            }
        }, 1L);

        if (!forceUpdate) updateCooldown(player);
    }

    // ==========================================
    // INVENTORY CLOSE - optimizado
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS)) return;

        // Esperar un tick para que todo se estabilice
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateAllForPlayerQuietly(player);
            }
        }, 1L);

        if (!forceUpdate) updateCooldown(player);
    }

    // ==========================================
    // PLAYER ITEM HELD - optimizado
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS_FAST)) return;

        // Solo actualizar si el nuevo item es un talismán
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                ItemStack inHand = player.getInventory().getItem(event.getNewSlot());
                if (inHand != null && !inHand.getType().isAir()) {
                    if (talismanManager.getFromItem(inHand) != null) {
                        // Solo actualizar este item si es necesario
                        if (talismanManager.needsUpdate(inHand)) {
                            ItemStack updated = talismanManager.updateItemStackSafely(inHand);
                            if (!talismanManager.areItemsEqual(inHand, updated)) {
                                player.getInventory().setItem(event.getNewSlot(), updated);
                                if (!forceUpdate) player.updateInventory();
                            }
                        }
                    }
                }
            }
        }, 1L);

        if (!forceUpdate) updateCooldown(player);
    }

    // ==========================================
    // NUEVO: Player swap hand items (para F)
    // ==========================================
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();

        // Verificar si alguno de los items es un talismán
        boolean hasTalisman = false;
        if (talismanManager.getFromItem(event.getMainHandItem()) != null ||
                talismanManager.getFromItem(event.getOffHandItem()) != null) {
            hasTalisman = true;
        }

        if (!hasTalisman) return;

        // IMPORTANTE: Si estamos en modo force update, ignorar cooldowns
        boolean forceUpdate = isForceUpdateMode();

        if (!forceUpdate && !canUpdate(player, COOLDOWN_MS_FAST)) return;

        // Actualizar después del swap
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                updateAllForPlayerQuietly(player);
            }
        }, 1L);

        if (!forceUpdate) updateCooldown(player);
    }

    // ===========================================================
    // MÉTODO OPTIMIZADO: Actualiza solo si es necesario
    // ===========================================================
    private void updateAllForPlayerQuietly(Player player) {
        if (player == null || !player.isOnline()) return;

        try {
            boolean forceUpdate = isForceUpdateMode();

            // Si estamos en modo force update, usar cooldown más corto
            if (!forceUpdate && !canUpdate(player, forceUpdate ? FORCE_UPDATE_COOLDOWN_MS : COOLDOWN_MS)) {
                return;
            }

            // Inventario normal
            talismanManager.updateInventoryTalismansQuietly(player.getInventory());

            // Cursor item
            ItemStack cursor = player.getItemOnCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (forceUpdate || (talismanManager.getFromItem(cursor) != null &&
                        talismanManager.needsUpdate(cursor))) {
                    ItemStack updated = talismanManager.updateItemStackSafely(cursor);
                    if (!talismanManager.areItemsEqual(cursor, updated)) {
                        player.setItemOnCursor(updated);
                    }
                }
            }

            // Crafting grid + cualquier menú abierto
            updateOpenInventoryQuietly(player);

            // Solo actualizar cooldown si no es force update
            if (!forceUpdate) {
                updateCooldown(player);
            }

        } catch (Exception e) {
            // Ignorar errores silenciosamente para no spammear logs
        }
    }

    // ===========================================================
    // Método optimizado para inventarios abiertos
    // ===========================================================
    private void updateOpenInventoryQuietly(Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return;

        try {
            // Inventario superior (crafting, anvil, etc.)
            Inventory top = view.getTopInventory();
            if (top != null) {
                talismanManager.updateInventoryTalismansQuietly(top);
            }

            // Inventario inferior (player inventory shown inside the UI)
            Inventory bottom = view.getBottomInventory();
            if (bottom != null) {
                talismanManager.updateInventoryTalismansQuietly(bottom);
            }
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    // ===========================================================
    // Cursor item - método auxiliar optimizado
    // ===========================================================
    private void updateCursorItemQuietly(Player player) {
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            if (talismanManager.getFromItem(cursor) != null &&
                    talismanManager.needsUpdate(cursor)) {
                ItemStack updated = talismanManager.updateItemStackSafely(cursor);
                if (!talismanManager.areItemsEqual(cursor, updated)) {
                    player.setItemOnCursor(updated);
                }
            }
        }
    }

    // ===========================================================
    // Métodos de cooldown
    // ===========================================================

    /**
     * Verifica si un jugador puede ser actualizado
     * @param player El jugador
     * @param cooldownMs El cooldown requerido en milisegundos
     * @return true si puede actualizar, false si está en cooldown
     */
    private boolean canUpdate(Player player, long cooldownMs) {
        UUID uuid = player.getUniqueId();
        Long lastUpdate = updateCooldowns.get(uuid);

        if (lastUpdate == null) {
            return true;
        }

        long timeSince = System.currentTimeMillis() - lastUpdate;
        return timeSince >= cooldownMs;
    }

    /**
     * Actualiza el tiempo de última actualización
     * @param player El jugador
     */
    private void updateCooldown(Player player) {
        updateCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        // Limpiar entradas antiguas periódicamente
        if (updateCooldowns.size() > 100) {
            long now = System.currentTimeMillis();
            updateCooldowns.entrySet().removeIf(entry ->
                    (now - entry.getValue()) > 300000); // Remover entradas de más de 5 minutos
        }
    }

    /**
     * Verifica si estamos en modo force update
     * @return true si estamos en modo force update
     */
    private boolean isForceUpdateMode() {
        try {
            // Intentar acceder al método isForceUpdateMode si existe
            java.lang.reflect.Method method = talismanManager.getClass().getMethod("isForceUpdateMode");
            return (boolean) method.invoke(talismanManager);
        } catch (Exception e) {
            return false; // Si no existe el método, retornar false
        }
    }
}