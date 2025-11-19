package ft.keraune.funtalismans.api;

import org.bukkit.enchantments.Enchantment;

public class TalismanEnchantment {

    private final Enchantment enchantment;
    private final int level;

    public TalismanEnchantment(Enchantment enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "TalismanEnchantment{enchantment=" + enchantment + ", level=" + level + "}";
    }
}