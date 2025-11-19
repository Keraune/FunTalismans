package ft.keraune.funtalismans.manager;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.items.TalismanItemBuilder;
import ft.keraune.funtalismans.items.SkullUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class TalismanManager {

    private final FunTalismans plugin;
    private Map<String, Talisman> talismans = new HashMap<>();
    private int lastReloadUpdatedPlayers = 0;

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
    }

    public int reloadTalismans() {
        Map<String, Talisman> oldTalismans = new HashMap<>(talismans);

        talismans.clear();
        loadTalismans();

        SkullUtil.resetWarning();

        lastReloadUpdatedPlayers = updateAllPlayerItems(oldTalismans);

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

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                Talisman oldTalisman = getFromItem(item);
                if (oldTalisman != null) {
                    Talisman newTalisman = getTalisman(oldTalisman.getId());
                    if (newTalisman != null) {
                        ItemStack newItem = TalismanItemBuilder.build(newTalisman);
                        inventory.setItem(i, newItem);
                        updated = true;
                    }
                }
            }
        }

        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = armor[i];
            if (item != null) {
                Talisman oldTalisman = getFromItem(item);
                if (oldTalisman != null) {
                    Talisman newTalisman = getTalisman(oldTalisman.getId());
                    if (newTalisman != null) {
                        armor[i] = TalismanItemBuilder.build(newTalisman);
                        updated = true;
                    }
                }
            }
        }
        inventory.setArmorContents(armor);

        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand != null) {
            Talisman oldTalisman = getFromItem(mainHand);
            if (oldTalisman != null) {
                Talisman newTalisman = getTalisman(oldTalisman.getId());
                if (newTalisman != null) {
                    inventory.setItemInMainHand(TalismanItemBuilder.build(newTalisman));
                    updated = true;
                }
            }
        }

        ItemStack offHand = inventory.getItemInOffHand();
        if (offHand != null) {
            Talisman oldTalisman = getFromItem(offHand);
            if (oldTalisman != null) {
                Talisman newTalisman = getTalisman(oldTalisman.getId());
                if (newTalisman != null) {
                    inventory.setItemInOffHand(TalismanItemBuilder.build(newTalisman));
                    updated = true;
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
        return talismans;
    }

    public List<String> getTalismanIds() {
        return new ArrayList<>(talismans.keySet());
    }

    public Talisman getFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        if (!pdc.has(plugin.getConfigManager().talismansKey()))
            return null;

        String id = pdc.get(plugin.getConfigManager().talismansKey(), plugin.getConfigManager().stringType());
        return getTalisman(id);
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

        if (item == null) return false;

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

    public void clearEffects(Player p) {
        plugin.getEffectManager().removeAllEffects(p);
    }
}