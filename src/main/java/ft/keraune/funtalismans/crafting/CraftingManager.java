package ft.keraune.funtalismans.crafting;

import com.typesafe.config.Config;
import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.items.TalismanItemBuilder;
import ft.keraune.funtalismans.manager.TalismanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CraftingManager implements Listener {

    private final FunTalismans plugin;
    private final Map<String, CustomRecipe> recipes;

    public CraftingManager(FunTalismans plugin) {
        this.plugin = plugin;
        this.recipes = new HashMap<>();
        loadRecipes();
    }

    public void loadRecipes() {
        recipes.clear();

        Config config = plugin.getConfigHandler().getConfig("crafting.conf");
        if (config == null || !config.hasPath("addRecipe")) {
            plugin.getLogger().warning("No crafting recipes found!");
            return;
        }

        List<? extends Config> recipeList = config.getConfigList("addRecipe");
        plugin.getLogger().info("Loading " + recipeList.size() + " custom recipes...");

        for (Config recipeConfig : recipeList) {
            try {
                registerCustomRecipe(recipeConfig);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register recipe: " + e.getMessage());
            }
        }

        plugin.getLogger().info("Successfully loaded " + recipes.size() + " custom recipes");
    }

    private void registerCustomRecipe(Config recipeConfig) {
        String key = recipeConfig.getString("key");
        List<String> shapeList = recipeConfig.getStringList("shape");
        Config ingredientsConfig = recipeConfig.getConfig("ingredients");
        Config resultConfig = recipeConfig.getConfig("result");

        // Crear resultado
        ItemStack result = createResultItem(resultConfig);
        if (result == null) {
            plugin.getLogger().warning("Failed to create result for recipe: " + key);
            return;
        }

        // Crear nuestra receta personalizada
        CustomRecipe recipe = new CustomRecipe(key, shapeList, ingredientsConfig, result);
        recipes.put(key.toLowerCase(), recipe);

        plugin.getLogger().info("Registered custom recipe: " + key);
    }

    private ItemStack createResultItem(Config resultConfig) {
        if (resultConfig.hasPath("material")) {
            Material material = Material.matchMaterial(resultConfig.getString("material"));
            if (material == null) return null;

            ItemStack result = new ItemStack(material);
            ItemMeta meta = result.getItemMeta();

            if (resultConfig.hasPath("name")) {
                meta.setDisplayName(ft.keraune.funtalismans.utils.TextUtil.color(
                        resultConfig.getString("name")
                ));
            }

            if (resultConfig.hasPath("lore")) {
                List<String> lore = resultConfig.getStringList("lore").stream()
                        .map(ft.keraune.funtalismans.utils.TextUtil::color)
                        .toList();
                meta.setLore(lore);
            }

            result.setItemMeta(meta);
            return result;
        } else if (resultConfig.hasPath("talisman")) {
            String talismanId = resultConfig.getString("talisman");
            Talisman talisman = plugin.getTalismanManager().getTalisman(talismanId);
            return talisman != null ? TalismanItemBuilder.build(talisman) : null;
        }

        return null;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory() == null) return;

        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        // Ignorar si la matriz está vacía
        if (isEmptyMatrix(matrix)) return;

        // Buscar si alguna de nuestras recetas personalizadas coincide
        CustomRecipe matchedRecipe = findMatchingCustomRecipe(matrix);

        if (matchedRecipe != null) {
            plugin.getLogger().info("Custom recipe matched: " + matchedRecipe.getKey());

            // Verificar permisos
            if (event.getView().getPlayer() instanceof Player player) {
                if (!player.hasPermission("funtalismans.craft")) {
                    plugin.getLogger().info("No permission - cancelling");
                    inventory.setResult(null);
                    return;
                }
            }

            // Establecer el resultado
            inventory.setResult(matchedRecipe.getResult());
            plugin.getLogger().info("Setting result for custom recipe: " + matchedRecipe.getKey());
        } else {
            // Si no coincide con ninguna de nuestras recetas, pero contiene talismanes, cancelar
            // para evitar que se usen talismanes en recetas normales de Minecraft
            if (containsTalismans(matrix)) {
                inventory.setResult(null);
            }
        }
    }

    private boolean isEmptyMatrix(ItemStack[] matrix) {
        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    private CustomRecipe findMatchingCustomRecipe(ItemStack[] matrix) {
        for (CustomRecipe recipe : recipes.values()) {
            if (matchesCustomRecipe(matrix, recipe)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesCustomRecipe(ItemStack[] matrix, CustomRecipe recipe) {
        String[] shape = recipe.getShape();
        Config ingredients = recipe.getIngredients();

        // Verificar cada slot del crafting
        for (int i = 0; i < matrix.length; i++) {
            ItemStack currentItem = matrix[i];
            int row = i / 3;
            int col = i % 3;

            // Verificar si estamos dentro de la forma definida
            if (row < shape.length && col < shape[row].length()) {
                char expectedChar = shape[row].charAt(col);

                // Si es espacio, debe estar vacío
                if (expectedChar == ' ') {
                    if (currentItem != null && !currentItem.getType().isAir()) {
                        return false;
                    }
                    continue;
                }

                // Obtener el ingrediente esperado
                String ingredientValue = ingredients.getString(String.valueOf(expectedChar));

                // Verificar si coincide
                if (!matchesIngredient(currentItem, ingredientValue)) {
                    return false;
                }
            } else {
                // Fuera de la forma, debe estar vacío
                if (currentItem != null && !currentItem.getType().isAir()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean matchesIngredient(ItemStack item, String ingredientValue) {
        // Si el slot está vacío
        if (item == null || item.getType().isAir()) {
            return false;
        }

        Material material = Material.matchMaterial(ingredientValue);
        if (material != null) {
            // Es un material normal - comparar tipo
            return item.getType() == material;
        } else {
            // Es un talismán - verificar por ID
            TalismanManager manager = plugin.getTalismanManager();
            Talisman talisman = manager.getFromItem(item);

            if (talisman != null) {
                plugin.getLogger().info("Found talisman: " + talisman.getId() + " expected: " + ingredientValue);
            }

            return talisman != null && talisman.getId().equalsIgnoreCase(ingredientValue);
        }
    }

    private boolean containsTalismans(ItemStack[] matrix) {
        TalismanManager manager = plugin.getTalismanManager();
        for (ItemStack item : matrix) {
            if (item != null && !item.getType().isAir() && manager.getFromItem(item) != null) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // No necesitamos descubrir recetas ya que no usamos el sistema de Bukkit
    }

    public void discoverRecipesForOnlinePlayers() {
        // No necesitamos descubrir recetas ya que no usamos el sistema de Bukkit
    }

    public void reloadRecipes() {
        loadRecipes();
    }

    public void unregisterRecipes() {
        recipes.clear();
    }

    public List<String> getRecipeKeys() {
        return new ArrayList<>(recipes.keySet());
    }

    // Clase interna para nuestras recetas personalizadas
    private static class CustomRecipe {
        private final String key;
        private final String[] shape;
        private final Config ingredients;
        private final ItemStack result;

        public CustomRecipe(String key, List<String> shapeList, Config ingredients, ItemStack result) {
            this.key = key;
            this.shape = shapeList.toArray(new String[0]);
            this.ingredients = ingredients;
            this.result = result;
        }

        public String getKey() {
            return key;
        }

        public String[] getShape() {
            return shape;
        }

        public Config getIngredients() {
            return ingredients;
        }

        public ItemStack getResult() {
            return result.clone();
        }
    }
}