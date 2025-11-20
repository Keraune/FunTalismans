package ft.keraune.funtalismans.crafting;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.manager.TalismanManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.Objects;

public class TalismanRecipeChoice implements RecipeChoice {

    private final Talisman talisman;
    private final ItemStack representativeItem;

    public TalismanRecipeChoice(Talisman talisman) {
        this.talisman = talisman;
        this.representativeItem = ft.keraune.funtalismans.items.TalismanItemBuilder.build(talisman);
    }

    @Override
    public boolean test(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;

        TalismanManager manager = FunTalismans.getInstance().getTalismanManager();
        Talisman itemTalisman = manager.getFromItem(item);

        return itemTalisman != null && itemTalisman.getId().equalsIgnoreCase(talisman.getId());
    }

    @Override
    public ItemStack getItemStack() {
        return representativeItem.clone();
    }

    @Override
    public RecipeChoice clone() {
        try {
            TalismanRecipeChoice clone = (TalismanRecipeChoice) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TalismanRecipeChoice other)) return false;
        return Objects.equals(talisman.getId(), other.talisman.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(talisman.getId());
    }
}