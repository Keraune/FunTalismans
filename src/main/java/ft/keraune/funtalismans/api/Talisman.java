package ft.keraune.funtalismans.api;

import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.Map;

public class Talisman {

    private final String id;
    private final String name;
    private final String material;
    private final boolean glow;
    private final boolean unbreakable;
    private final List<String> lore;

    private final List<TalismanAttribute> attributes;
    private final Map<String, Object> nbt;

    private final String color;
    private final int count;
    private final int damage;
    private final List<String> flags;

    // ---- NUEVO ----
    private final List<EquipmentSlot> effectSlots;
    private final List<TalismanEffect> effects;
    private final List<TalismanEnchantment> enchantments; // ← NUEVO

    public Talisman(
            String id,
            String name,
            String material,
            boolean glow,
            boolean unbreakable,
            List<String> lore,
            List<TalismanAttribute> attributes,
            Map<String, Object> nbt,
            String color,
            int count,
            int damage,
            List<String> flags,
            List<EquipmentSlot> effectSlots,
            List<TalismanEffect> effects,
            List<TalismanEnchantment> enchantments // ← NUEVO
    ) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.glow = glow;
        this.unbreakable = unbreakable;
        this.lore = List.copyOf(lore);
        this.attributes = List.copyOf(attributes);
        this.nbt = Map.copyOf(nbt);

        this.color = color;
        this.count = count;
        this.damage = damage;
        this.flags = List.copyOf(flags);

        this.effectSlots = List.copyOf(effectSlots);
        this.effects = List.copyOf(effects);
        this.enchantments = List.copyOf(enchantments); // ← NUEVO
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMaterial() { return material; }
    public boolean isGlow() { return glow; }
    public boolean isUnbreakable() { return unbreakable; }
    public List<String> getLore() { return lore; }
    public List<TalismanAttribute> getAttributes() { return attributes; }
    public Map<String, Object> getNbt() { return nbt; }
    public String getColor() { return color; }
    public int getCount() { return count; }
    public int getDamage() { return damage; }
    public List<String> getFlags() { return flags; }

    public List<EquipmentSlot> getEffectSlots() { return effectSlots; }
    public List<TalismanEffect> getEffects() { return effects; }
    public List<TalismanEnchantment> getEnchantments() { return enchantments; } // ← NUEVO

    public boolean hasEffect(org.bukkit.potion.PotionEffectType type) {
        for (TalismanEffect eff : effects) {
            if (eff.getType() == type) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Talisman{id='" + id + "'}";
    }
}