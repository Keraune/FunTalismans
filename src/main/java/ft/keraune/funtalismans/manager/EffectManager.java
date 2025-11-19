package ft.keraune.funtalismans.manager;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.api.TalismanEffect;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EffectManager {

    private final FunTalismans plugin;
    private final Map<UUID, Set<PotionEffectType>> playerTalismanEffects;

    public EffectManager(FunTalismans plugin) {
        this.plugin = plugin;
        this.playerTalismanEffects = new HashMap<>();
    }

    public void applyEffects(Player player, Talisman talisman) {
        UUID playerId = player.getUniqueId();

        // Registrar los efectos que aplicamos
        Set<PotionEffectType> effects = playerTalismanEffects.getOrDefault(playerId, new HashSet<>());

        for (TalismanEffect effect : talisman.getEffects()) {
            PotionEffectType type = effect.getType();
            if (type == null) continue;

            effects.add(type);

            PotionEffect potion = new PotionEffect(
                    type,
                    PotionEffect.INFINITE_DURATION,
                    effect.getAmplifier(),
                    true,
                    true,
                    true
            );

            player.addPotionEffect(potion, true);
        }

        playerTalismanEffects.put(playerId, effects);
    }

    public void removeEffects(Player player, Talisman talisman) {
        UUID playerId = player.getUniqueId();
        Set<PotionEffectType> effects = playerTalismanEffects.get(playerId);

        if (effects == null) return;

        for (TalismanEffect effect : talisman.getEffects()) {
            PotionEffectType type = effect.getType();
            if (type == null) continue;

            effects.remove(type);

            // Solo remover si ningún otro talismán provee este efecto
            if (!isEffectProvidedByOtherTalismans(player, type)) {
                player.removePotionEffect(type);
            }
        }

        if (effects.isEmpty()) {
            playerTalismanEffects.remove(playerId);
        } else {
            playerTalismanEffects.put(playerId, effects);
        }
    }

    public void removeAllEffects(Player player) {
        UUID playerId = player.getUniqueId();
        Set<PotionEffectType> effects = playerTalismanEffects.get(playerId);

        if (effects != null) {
            for (PotionEffectType effectType : effects) {
                player.removePotionEffect(effectType);
            }
            playerTalismanEffects.remove(playerId);
        }

        // Limpieza adicional por si acaso - remover efectos infinitos
        for (PotionEffect activeEffect : player.getActivePotionEffects()) {
            // Solo remover efectos infinitos que podrían ser de talismanes
            if (activeEffect.getDuration() > 1000000 || activeEffect.getDuration() == PotionEffect.INFINITE_DURATION) {
                player.removePotionEffect(activeEffect.getType());
            }
        }
    }

    private boolean isEffectProvidedByOtherTalismans(Player player, PotionEffectType type) {
        for (Talisman talisman : plugin.getTalismanManager().getTalismans().values()) {
            if (talisman.hasEffect(type) && plugin.getTalismanManager().isAnySlotHolding(player, talisman)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Limpia todos los efectos de todos los jugadores (para shutdown)
     */
    public void cleanupAllEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeAllEffects(player);
        }
        playerTalismanEffects.clear();
    }
}