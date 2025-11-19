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
    // APLICAR EFECTOS USANDO PotionEffect.INFINITE_DURATION (muestra ∞)
    // ===============================================================
    private void aplicarEfectos(Player jugador, Talisman talisman) {
        for (TalismanEffect efecto : talisman.getEffects()) {
            PotionEffectType tipo = efecto.getType();
            if (tipo == null) continue;

            // Usar la constante INFINITE_DURATION para mostrar ∞ en el cliente
            PotionEffect pocion = new PotionEffect(
                    tipo,
                    PotionEffect.INFINITE_DURATION, // Esto mostrará el símbolo ∞
                    efecto.getAmplifier(),
                    true,   // ambiental
                    true,   // partículas
                    true    // icono (debe ser true para mostrar el icono y ∞)
            );

            jugador.addPotionEffect(pocion, true);
        }
    }

    // ===============================================================
    // REMOVER EFECTOS SOLO SI NINGÚN OTRO TALISMÁN LOS PROVEE
    // ===============================================================
    private void removerEfectos(Player jugador, Talisman talisman) {
        for (TalismanEffect efecto : talisman.getEffects()) {
            PotionEffectType tipo = efecto.getType();
            if (tipo == null) continue;

            boolean todaviaProveido = false;

            // Verificar si algún talismán en los slots CORRECTOS provee este efecto
            for (Talisman otroTalisman : manager.getTalismans().values()) {
                if (otroTalisman.hasEffect(tipo) && estaTalismanEnSlotsCorrectos(jugador, otroTalisman)) {
                    todaviaProveido = true;
                    break;
                }
            }

            if (!todaviaProveido) {
                jugador.removePotionEffect(tipo);
            }
        }
    }

    // ===============================================================
    // VERIFICAR SI EL TALISMÁN ESTÁ EN SUS SLOTS CORRECTOS
    // ===============================================================
    private boolean estaTalismanEnSlotsCorrectos(Player jugador, Talisman talisman) {
        for (EquipmentSlot slot : talisman.getEffectSlots()) {
            if (manager.isItemInSlot(jugador, slot, talisman)) {
                return true;
            }
        }
        return false;
    }

    // ===============================================================
    // ACTUALIZAR EFECTOS (llamado al equipar/desequipar/cambio de inventario)
    // ===============================================================
    private void actualizar(Player jugador) {
        // Primero remover efectos de talismanes que ya no están en sus slots
        for (Talisman talisman : manager.getTalismans().values()) {
            removerEfectos(jugador, talisman);
        }

        // Luego aplicar efectos solo para talismanes que SÍ están en sus slots correctos
        for (Talisman talisman : manager.getTalismans().values()) {
            if (estaTalismanEnSlotsCorrectos(jugador, talisman)) {
                aplicarEfectos(jugador, talisman);
            }
        }
    }

    // ===============================================================
    // EVENTOS
    // ===============================================================
    @EventHandler
    public void alCambiarItemMano(PlayerItemHeldEvent evento) {
        Bukkit.getScheduler().runTask(plugin, () -> actualizar(evento.getPlayer()));
    }

    @EventHandler
    public void alIntercambiarManos(PlayerSwapHandItemsEvent evento) {
        Bukkit.getScheduler().runTask(plugin, () -> actualizar(evento.getPlayer()));
    }

    @EventHandler
    public void alClicInventario(InventoryClickEvent evento) {
        if (evento.getWhoClicked() instanceof Player jugador)
            Bukkit.getScheduler().runTask(plugin, () -> actualizar(jugador));
    }
}