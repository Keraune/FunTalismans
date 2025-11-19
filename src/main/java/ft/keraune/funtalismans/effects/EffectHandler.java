package ft.keraune.funtalismans.effects;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.api.TalismanEffect;
import ft.keraune.funtalismans.manager.TalismanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EffectHandler implements Listener {

    private final FunTalismans plugin;
    private final TalismanManager manager;

    public EffectHandler(FunTalismans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getTalismanManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ===============================================================
    // APPLY EFFECTS USING PotionEffect.INFINITE_DURATION (shows ∞)
    // ===============================================================
    private void applyEffects(Player player, Talisman t) {
        for (TalismanEffect eff : t.getEffects()) {
            PotionEffectType type = eff.getType();
            if (type == null) continue;

            // Usar la constante INFINITE_DURATION para mostrar ∞ en el cliente
            PotionEffect potion = new PotionEffect(
                    type,
                    PotionEffect.INFINITE_DURATION, // Esto mostrará el símbolo ∞
                    eff.getAmplifier(),
                    true,   // ambient
                    true,   // particles
                    true    // icon (debe ser true para mostrar el icono y ∞)
            );

            player.addPotionEffect(potion, true);
        }
    }

    // ===============================================================
    // REMOVE EFFECTS ONLY IF NO OTHER TALISMAN PROVIDES THEM
    // ===============================================================
    private void removeEffects(Player player, Talisman t) {
        for (TalismanEffect eff : t.getEffects()) {
            PotionEffectType type = eff.getType();
            if (type == null) continue;

            boolean stillProvided = false;

            // Verificar si algún talismán en los slots CORRECTOS provee este efecto
            for (Talisman otherTalisman : manager.getTalismans().values()) {
                if (otherTalisman.hasEffect(type) && isTalismanInCorrectSlots(player, otherTalisman)) {
                    stillProvided = true;
                    break;
                }
            }

            if (!stillProvided) {
                player.removePotionEffect(type);
            }
        }
    }

    // ===============================================================
    // VERIFICAR SI EL TALISMÁN ESTÁ EN SUS SLOTS CORRECTOS
    // ===============================================================
    private boolean isTalismanInCorrectSlots(Player player, Talisman talisman) {
        for (EquipmentSlot slot : talisman.getEffectSlots()) {
            if (manager.isItemInSlot(player, slot, talisman)) {
                return true;
            }
        }
        return false;
    }

    // ===============================================================
    // UPDATE EFFECTS (called on equip/unequip/inventory change)
    // ===============================================================
    private void update(Player p) {
        // Primero remover efectos de talismanes que ya no están en sus slots
        for (Talisman t : manager.getTalismans().values()) {
            removeEffects(p, t);
        }

        // Luego aplicar efectos solo para talismanes que SÍ están en sus slots correctos
        for (Talisman t : manager.getTalismans().values()) {
            if (isTalismanInCorrectSlots(p, t)) {
                applyEffects(p, t);
            }
        }
    }

    // ===============================================================
    // EVENTS
    // ===============================================================
    @EventHandler
    public void onHeldChange(PlayerItemHeldEvent e) {
        Bukkit.getScheduler().runTask(plugin, () -> update(e.getPlayer()));
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Bukkit.getScheduler().runTask(plugin, () -> update(e.getPlayer()));
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p)
            Bukkit.getScheduler().runTask(plugin, () -> update(p));
    }
}