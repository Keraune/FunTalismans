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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EffectHandler implements Listener {

    private final FunTalismans plugin;
    private final TalismanManager manager;
    private final Map<UUID, Set<PotionEffectType>> activePlayerEffects;

    public EffectHandler(FunTalismans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getTalismanManager();
        this.activePlayerEffects = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Actualizar efectos cuando el jugador se conecta
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updatePlayerEffects(event.getPlayer());
        }, 10L);
    }

    public void updatePlayerEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // 1. Obtener efectos que DEBERÍAN estar activos según los talismanes equipados
        Set<PotionEffectType> expectedEffects = getExpectedEffects(player);

        // 2. Obtener efectos que están actualmente activos del jugador
        Set<PotionEffectType> currentEffects = activePlayerEffects.getOrDefault(playerId, new HashSet<>());

        // 3. Remover efectos que YA NO deberían estar activos
        for (PotionEffectType effectType : currentEffects) {
            if (!expectedEffects.contains(effectType)) {
                player.removePotionEffect(effectType);
            }
        }

        // 4. Aplicar efectos NUEVOS que deberían estar activos
        for (Talisman talisman : manager.getTalismans().values()) {
            if (isTalismanInCorrectSlots(player, talisman)) {
                applyTalismanEffects(player, talisman);
            }
        }

        // 5. Actualizar el registro de efectos activos
        activePlayerEffects.put(playerId, expectedEffects);
    }

    private Set<PotionEffectType> getExpectedEffects(Player player) {
        Set<PotionEffectType> expected = new HashSet<>();

        for (Talisman talisman : manager.getTalismans().values()) {
            if (isTalismanInCorrectSlots(player, talisman)) {
                for (TalismanEffect effect : talisman.getEffects()) {
                    if (effect.getType() != null) {
                        expected.add(effect.getType());
                    }
                }
            }
        }

        return expected;
    }

    private void applyTalismanEffects(Player player, Talisman talisman) {
        for (TalismanEffect effect : talisman.getEffects()) {
            PotionEffectType type = effect.getType();
            if (type == null) continue;

            // Verificar si ya tiene un efecto del mismo tipo pero diferente nivel
            PotionEffect currentEffect = player.getPotionEffect(type);
            if (currentEffect != null) {
                // Si el nivel actual es diferente (o mayor) al que debería tener, removerlo
                if (currentEffect.getAmplifier() != effect.getAmplifier()) {
                    player.removePotionEffect(type);
                } else {
                    // Si ya tiene el mismo nivel, saltar para no aplicar duplicado
                    continue;
                }
            }

            // Aplicar el efecto correcto
            PotionEffect potion = new PotionEffect(
                    type,
                    PotionEffect.INFINITE_DURATION,
                    effect.getAmplifier(),
                    true,
                    true,
                    true
            );

            player.addPotionEffect(potion);
        }
    }

    private boolean isTalismanInCorrectSlots(Player player, Talisman talisman) {
        for (EquipmentSlot slot : talisman.getEffectSlots()) {
            if (manager.isItemInSlot(player, slot, talisman)) {
                return true;
            }
        }
        return false;
    }

    // Eventos que requieren actualización de efectos
    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> updatePlayerEffects(event.getPlayer()));
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> updatePlayerEffects(event.getPlayer()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTask(plugin, () -> updatePlayerEffects(player));
        }
    }

    // Método para limpiar efectos cuando el jugador se desconecta
    public void cleanupPlayerEffects(Player player) {
        UUID playerId = player.getUniqueId();
        Set<PotionEffectType> effects = activePlayerEffects.get(playerId);

        if (effects != null) {
            for (PotionEffectType effectType : effects) {
                player.removePotionEffect(effectType);
            }
            activePlayerEffects.remove(playerId);
        }
    }
}