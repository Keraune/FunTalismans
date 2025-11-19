package ft.keraune.funtalismans.api;

import org.bukkit.potion.PotionEffectType;

public class TalismanEffect {

    private final PotionEffectType type;
    private final int amplifier;

    public TalismanEffect(PotionEffectType type, int amplifier) {
        this.type = type;
        this.amplifier = amplifier;
    }

    public PotionEffectType getType() {
        return type;
    }

    public int getAmplifier() {
        return amplifier;
    }
}
