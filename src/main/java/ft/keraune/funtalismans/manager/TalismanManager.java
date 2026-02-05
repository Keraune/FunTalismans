package ft.keraune.funtalismans.manager;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.items.TalismanItemBuilder;
import ft.keraune.funtalismans.items.SkullUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TalismanManager {

    private final FunTalismans plugin;
    private Map<String, Talisman> talismans = new HashMap<>();
    private int lastReloadUpdatedPlayers = 0;

    // NUEVO: Cache de items de talismanes
    private final Map<String, ItemStack> talismanItemCache = new HashMap<>();
    private boolean cacheNeedsRefresh = true;

    private boolean forceUpdateMode = false;

    public TalismanManager(FunTalismans plugin) {
        this.plugin = plugin;
    }

    public void loadTalismans() {
        var loader = new ft.keraune.funtalismans.data.TalismanLoader(plugin);
        talismans = loader.load();

        Map<String, Talisman> normalized = new HashMap<>();
        for (Map.Entry<String, Talisman> entry : talismans.entrySet()) {
            normalized.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        talismans = normalized;

        // Marcar cache como obsoleto
        cacheNeedsRefresh = true;

        // Refrescar cache inmediatamente
        refreshTalismanCache();
    }

    public int reloadTalismans() {
        Map<String, Talisman> oldTalismans = new HashMap<>(talismans);

        talismans.clear();
        loadTalismans();

        SkullUtil.resetWarning();

        // NUEVO: Forzar actualización después de reload
        forceUpdateMode = true;

        // Desactivar modo forzado después de un tiempo razonable
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            forceUpdateMode = false;
        }, 600L); // 30 segundos (600 ticks)

        lastReloadUpdatedPlayers = updateAllPlayerItems(oldTalismans);
        updateAllEnderChests();
        updateAllPlayerCursorsAndOpenInventories();
        updateTalismanContainers();

        return lastReloadUpdatedPlayers;
    }

    public int getLastReloadUpdatedPlayers() {
        return lastReloadUpdatedPlayers;
    }

    private int updateAllPlayerItems(Map<String, Talisman> oldTalismans) {
        int updatedPlayers = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean updated = updatePlayerItems(player, oldTalismans);
            if (updated) {
                updatedPlayers++;
            }
        }

        return updatedPlayers;
    }

    private boolean updatePlayerItems(Player player, Map<String, Talisman> oldTalismans) {
        PlayerInventory inventory = player.getInventory();
        boolean updated = false;

        // Actualizar inventario principal
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && !item.getType().isAir()) {
                Talisman oldTalisman = getFromItem(item);
                if (oldTalisman != null) {
                    Talisman newTalisman = getTalisman(oldTalisman.getId());
                    if (newTalisman != null) {
                        // IMPORTANTE: Después de reload SIEMPRE actualizar
                        if (forceUpdateMode || needsUpdate(item)) {
                            int originalAmount = item.getAmount();
                            ItemStack newItem = getCachedTalismanItem(newTalisman.getId());
                            if (newItem == null) {
                                newItem = TalismanItemBuilder.build(newTalisman);
                            }
                            newItem.setAmount(originalAmount);
                            inventory.setItem(i, newItem);
                            updated = true;
                        }
                    }
                }
            }
        }

        // Actualizar armadura
        ItemStack[] armor = inventory.getArmorContents();
        boolean armorUpdated = false;
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item != null && !item.getType().isAir()) {
                Talisman oldTalisman = getFromItem(item);
                if (oldTalisman != null) {
                    Talisman newTalisman = getTalisman(oldTalisman.getId());
                    if (newTalisman != null) {
                        if (forceUpdateMode || needsUpdate(item)) {
                            int originalAmount = item.getAmount();
                            ItemStack newItem = getCachedTalismanItem(newTalisman.getId());
                            if (newItem == null) {
                                newItem = TalismanItemBuilder.build(newTalisman);
                            }
                            newItem.setAmount(originalAmount);
                            armor[i] = newItem;
                            armorUpdated = true;
                        }
                    }
                }
            }
        }
        if (armorUpdated) {
            inventory.setArmorContents(armor);
            updated = true;
        }

        // Actualizar manos
        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand != null && !mainHand.getType().isAir()) {
            Talisman oldTalisman = getFromItem(mainHand);
            if (oldTalisman != null) {
                Talisman newTalisman = getTalisman(oldTalisman.getId());
                if (newTalisman != null) {
                    if (forceUpdateMode || needsUpdate(mainHand)) {
                        int originalAmount = mainHand.getAmount();
                        ItemStack newItem = getCachedTalismanItem(newTalisman.getId());
                        if (newItem == null) {
                            newItem = TalismanItemBuilder.build(newTalisman);
                        }
                        newItem.setAmount(originalAmount);
                        inventory.setItemInMainHand(newItem);
                        updated = true;
                    }
                }
            }
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand != null && !offHand.getType().isAir()) {
            Talisman oldTalisman = getFromItem(offHand);
            if (oldTalisman != null) {
                Talisman newTalisman = getTalisman(oldTalisman.getId());
                if (newTalisman != null) {
                    if (forceUpdateMode || needsUpdate(offHand)) {
                        int originalAmount = offHand.getAmount();
                        ItemStack newItem = getCachedTalismanItem(newTalisman.getId());
                        if (newItem == null) {
                            newItem = TalismanItemBuilder.build(newTalisman);
                        }
                        newItem.setAmount(originalAmount);
                        inventory.setItemInOffHand(newItem);
                        updated = true;
                    }
                }
            }
        }

        if (updated) {
            player.updateInventory();
        }

        return updated;
    }

    public Talisman getTalisman(String id) {
        if (id == null) return null;
        return talismans.get(id.toLowerCase());
    }

    public Map<String, Talisman> getTalismans() {
        return Collections.unmodifiableMap(talismans);
    }

    public List<String> getTalismanIds() {
        return new ArrayList<>(talismans.keySet());
    }

    public Talisman getFromItem(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return null;

        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        if (!pdc.has(plugin.getConfigManager().talismansKey()))
            return null;

        String id = pdc.get(plugin.getConfigManager().talismansKey(), plugin.getConfigManager().stringType());
        return getTalisman(id);
    }

    // NUEVO: Verificar versión del talismán
    private int getTalismanVersion(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return -1;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Usar una clave diferente para la versión
        return pdc.getOrDefault(
                new org.bukkit.NamespacedKey(plugin, "talisman_version"),
                PersistentDataType.INTEGER,
                -1
        );
    }

    // NUEVO: Verificar si un item necesita actualización
    public boolean needsUpdate(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return false;
        }

        Talisman talisman = getFromItem(item);
        if (talisman == null) {
            return false; // No es un talismán
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String currentId = pdc.get(plugin.getConfigManager().talismansKey(),
                plugin.getConfigManager().stringType());

        // Si no tiene ID o el ID no coincide
        if (currentId == null || !currentId.equalsIgnoreCase(talisman.getId())) {
            return true;
        }

        // Obtener el item esperado para este talismán
        ItemStack expectedItem = getCachedTalismanItem(talisman.getId());
        if (expectedItem == null) {
            expectedItem = TalismanItemBuilder.build(talisman);
        }

        // Comparar tipo de material
        if (item.getType() != expectedItem.getType()) {
            return true;
        }

        // --- CUSTOM MODEL DATA CHECK (NATIVO PAPER) ---
        Object expectedCMD = talisman.getCustomModelData();

        if (expectedCMD instanceof Integer i) {
            // Caso Entero: Usamos la API estándar de Meta
            if (!meta.hasCustomModelData() || meta.getCustomModelData() != i) return true;
        }
        else if (expectedCMD instanceof String s) {
            // Caso String: Usamos la API de Componentes de Paper
            if (!item.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
                return true; // No tiene componente, debe actualizarse
            }

            CustomModelData currentCmdData = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            // Verificamos si la lista de strings contiene el valor esperado
            if (currentCmdData == null || currentCmdData.strings() == null || !currentCmdData.strings().contains(s)) {
                return true;
            }
        }
        else if (expectedCMD == null) {
            // Si no esperamos nada, pero el item tiene data (Integer o Componente), actualizar para limpiar
            if (meta.hasCustomModelData()) return true;
            if (item.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) return true;
        }
        // -----------------------------------------------

        // Para custom heads, verificar también el display name
        if (expectedItem.hasItemMeta() && expectedItem.getItemMeta().hasDisplayName()) {
            String expectedName = expectedItem.getItemMeta().getDisplayName();
            if (meta.hasDisplayName()) {
                if (!meta.getDisplayName().equals(expectedName)) {
                    return true;
                }
            } else {
                return true; // Falta el display name
            }
        }

        return false; // No necesita actualización
    }

    // NUEVO: Obtener item de talismán desde cache
    private ItemStack getCachedTalismanItem(String id) {
        if (cacheNeedsRefresh) {
            refreshTalismanCache();
        }
        return talismanItemCache.get(id.toLowerCase());
    }

    // NUEVO: Refrescar cache con versión
    private void refreshTalismanCache() {
        talismanItemCache.clear();
        for (Map.Entry<String, Talisman> entry : talismans.entrySet()) {
            ItemStack item = TalismanItemBuilder.build(entry.getValue());
            talismanItemCache.put(entry.getKey().toLowerCase(), item);
        }
        cacheNeedsRefresh = false;
    }

    public boolean isItemInSlot(Player p, EquipmentSlot slot, Talisman t) {
        ItemStack item = switch (slot) {
            case HAND -> p.getInventory().getItemInMainHand();
            case OFF_HAND -> p.getInventory().getItemInOffHand();
            case HEAD -> p.getInventory().getHelmet();
            case CHEST -> p.getInventory().getChestplate();
            case LEGS -> p.getInventory().getLeggings();
            case FEET -> p.getInventory().getBoots();
            default -> null;
        };

        if (item == null || item.getType().isAir()) return false;

        Talisman found = getFromItem(item);
        return found != null && found.getId().equalsIgnoreCase(t.getId());
    }

    public boolean isAnySlotHolding(Player p, Talisman t) {
        for (EquipmentSlot slot : t.getEffectSlots()) {
            if (isItemInSlot(p, slot, t))
                return true;
        }
        return false;
    }

    public Talisman findEquippedTalisman(Player p) {
        for (Talisman t : talismans.values()) {
            if (isAnySlotHolding(p, t))
                return t;
        }
        return null;
    }

    /**
     * Encuentra todos los talismanes equipados por el jugador
     */
    public List<Talisman> findAllEquippedTalismans(Player p) {
        List<Talisman> equipped = new ArrayList<>();
        for (Talisman t : talismans.values()) {
            if (isAnySlotHolding(p, t)) {
                equipped.add(t);
            }
        }
        return equipped;
    }

    /**
     * Limpia los efectos del jugador - ahora delega al EffectHandler
     */
    public void clearEffects(Player p) {
        // Delegar la limpieza de efectos al EffectHandler para consistencia
        if (plugin.getEffectHandler() != null) {
            plugin.getEffectHandler().cleanupPlayerEffects(p);
        } else {
            // Fallback por si EffectHandler no está disponible
            cleanupEffectsFallback(p);
        }
    }

    /**
     * Método de respaldo para limpiar efectos si EffectHandler no está disponible
     */
    private void cleanupEffectsFallback(Player p) {
        // Este método se mantiene por compatibilidad pero ya no es el principal
        p.getActivePotionEffects().stream()
                .filter(effect -> effect.getDuration() > 1000000 ||
                        effect.getDuration() == org.bukkit.potion.PotionEffect.INFINITE_DURATION)
                .forEach(effect -> p.removePotionEffect(effect.getType()));
    }

    public void updateTalismanContainers() {
        for (World world : Bukkit.getWorlds()) {
            // Actualizar items tirados
            for (Item entityItem : world.getEntitiesByClass(Item.class)) {
                ItemStack stack = entityItem.getItemStack();
                Talisman t = getFromItem(stack);
                if (t != null) {
                    if (forceUpdateMode || needsUpdate(stack)) {
                        int amount = stack.getAmount();
                        ItemStack newItem = getCachedTalismanItem(t.getId());
                        if (newItem == null) {
                            newItem = TalismanItemBuilder.build(t);
                        }
                        newItem.setAmount(amount);
                        entityItem.setItemStack(newItem);
                    }
                }
            }

            // Actualizar contenedores
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof InventoryHolder holder) {
                        Inventory inv = holder.getInventory();
                        updateInventoryTalismansQuietly(inv);
                    }
                }
            }
        }
    }

    // NUEVO: Método optimizado para actualizar inventarios sin parpadeo
    public void updateInventoryTalismansQuietly(Inventory inv) {
        if (inv == null) return;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                Talisman t = getFromItem(item);
                if (t != null) {
                    // IMPORTANTE: Verificar forceUpdateMode también
                    if (forceUpdateMode || needsUpdate(item)) {
                        int amount = item.getAmount();
                        ItemStack newItem = getCachedTalismanItem(t.getId());
                        if (newItem == null) {
                            newItem = TalismanItemBuilder.build(t);
                        }
                        newItem.setAmount(amount);

                        // Solo setear si realmente cambió algo
                        if (!areItemsEqual(item, newItem)) {
                            inv.setItem(i, newItem);
                        }
                    }
                }
            }
        }
    }

    // Mantener compatibilidad
    public void updateInventoryTalismans(Inventory inv) {
        updateInventoryTalismansQuietly(inv);
    }

    // Actualizar un solo itemStack
    public void updateItemStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return;

        Talisman t = getFromItem(stack);
        if (t != null) {
            if (forceUpdateMode || needsUpdate(stack)) {
                int amount = stack.getAmount();
                ItemStack newItem = getCachedTalismanItem(t.getId());
                if (newItem == null) {
                    newItem = TalismanItemBuilder.build(t);
                }
                newItem.setAmount(amount);

                // Transferir metadata
                stack.setItemMeta(newItem.getItemMeta().clone());

                if (stack.getType() != newItem.getType()) {
                    stack.setType(newItem.getType());
                }

                stack.setAmount(amount);
            }
        }
    }

    // NUEVO MÉTODO: Actualizar itemStack de forma segura (para uso en eventos)
    public ItemStack updateItemStackSafely(ItemStack original) {
        if (original == null || original.getType().isAir()) return original;

        Talisman t = getFromItem(original);
        if (t != null) {
            if (forceUpdateMode || needsUpdate(original)) {
                int amount = original.getAmount();
                ItemStack updated = getCachedTalismanItem(t.getId());
                if (updated == null) {
                    updated = TalismanItemBuilder.build(t);
                }
                updated.setAmount(amount);
                return updated;
            }
        }

        return original;
    }

    // Método auxiliar para verificar si un item ya está actualizado
    private boolean isUpdated(ItemStack stack, Talisman talisman) {
        if (!stack.hasItemMeta()) return false;

        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Verificar si ya tiene la metadata correcta
        String currentId = pdc.get(plugin.getConfigManager().talismansKey(), plugin.getConfigManager().stringType());
        return talisman.getId().equals(currentId);
    }

    // MODIFICADO: Actualizar todos los enderchests
    public void updateAllEnderChests() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory ender = player.getEnderChest();
            updateInventoryTalismansQuietly(ender);
        }
    }

    // MODIFICADO: Actualizar cursores e inventarios abiertos
    public void updateAllPlayerCursorsAndOpenInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;

            try {
                // Actualizar cursor
                ItemStack cursor = player.getItemOnCursor();
                if (cursor != null && !cursor.getType().isAir() && getFromItem(cursor) != null) {
                    if (forceUpdateMode || needsUpdate(cursor)) {
                        ItemStack updated = updateItemStackSafely(cursor);
                        if (!areItemsEqual(cursor, updated)) {
                            player.setItemOnCursor(updated);
                        }
                    }
                }

                // Actualizar inventarios abiertos
                InventoryView view = player.getOpenInventory();
                if (view != null) {
                    Inventory top = view.getTopInventory();
                    if (top != null) {
                        updateInventoryTalismansQuietly(top);
                    }

                    Inventory bottom = view.getBottomInventory();
                    if (bottom != null) {
                        updateInventoryTalismansQuietly(bottom);
                    }
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Error updating cursor/open inv for " +
                        player.getName() + ": " + e.getMessage());
            }
        }
    }

    // Método optimizado para actualizar inventarios de forma segura
    private void updateInventoryTalismansSafely(Inventory inv) {
        if (inv == null) return;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir()) {
                Talisman t = getFromItem(item);
                if (t != null) {
                    // Solo actualizar si es necesario
                    if (forceUpdateMode || needsUpdate(item)) {
                        ItemStack updated = updateItemStackSafely(item);
                        if (!areItemsEqual(item, updated)) {
                            inv.setItem(i, updated);
                        }
                    }
                }
            }
        }
    }

    // Método para comparar si dos items son iguales (excepto cantidad)
    public boolean areItemsEqual(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        return a.getType() == b.getType() &&
                a.hasItemMeta() == b.hasItemMeta() &&
                (!a.hasItemMeta() || a.getItemMeta().equals(b.getItemMeta()));
    }

    // NUEVO: Método para comparar si dos items son visualmente iguales (para custom heads)
    public boolean areItemsVisuallyEqual(ItemStack a, ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getType() != b.getType()) return false;

        // Para custom heads, la metadata debe ser idéntica
        if (!a.hasItemMeta() || !b.hasItemMeta()) {
            return a.hasItemMeta() == b.hasItemMeta();
        }

        ItemMeta metaA = a.getItemMeta();
        ItemMeta metaB = b.getItemMeta();

        // Comparar display name
        if (metaA.hasDisplayName() != metaB.hasDisplayName()) return false;
        if (metaA.hasDisplayName() && !metaA.getDisplayName().equals(metaB.getDisplayName())) return false;

        // Comparar lore
        if (metaA.hasLore() != metaB.hasLore()) return false;
        if (metaA.hasLore() && !metaA.getLore().equals(metaB.getLore())) return false;

        // Comparar custom model data
        if (metaA.hasCustomModelData() != metaB.hasCustomModelData()) return false;
        if (metaA.hasCustomModelData() && metaA.getCustomModelData() != metaB.getCustomModelData()) return false;

        return true;
    }

    // NUEVO: Verificar si estamos en modo force update
    public boolean isForceUpdateMode() {
        return forceUpdateMode;
    }

    // NUEVO: Método para forzar actualización manual
    public void forceUpdateAll() {
        forceUpdateMode = true;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            forceUpdateMode = false;
        }, 600L);
    }
}