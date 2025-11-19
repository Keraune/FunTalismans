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
    private final Map<NamespacedKey, Recipe> recipes;
    private final Map<String, AdvancedRecipe> advancedRecipes;

    public CraftingManager(FunTalismans plugin) {
        this.plugin = plugin;
        this.recipes = new HashMap<>();
        this.advancedRecipes = new HashMap<>();
        loadRecipes();
    }

    public void loadRecipes() {
        unregisterRecipes();
        advancedRecipes.clear();

        Config config = plugin.getConfigHandler().getConfig("crafting.conf");
        if (config == null || !config.hasPath("addRecipe")) {
            createDefaultRecipes();
            return;
        }

        List<? extends Config> recipeList = config.getConfigList("addRecipe");
        for (Config recipeConfig : recipeList) {
            try {
                registerAdvancedRecipe(recipeConfig);
            } catch (Exception e) {
                // Error silencioso
            }
        }
    }

    private void registerAdvancedRecipe(Config recipeConfig) {
        String key = recipeConfig.getString("key");
        List<String> shape = recipeConfig.getStringList("shape");
        Config ingredientsConfig = recipeConfig.getConfig("ingredients");
        Config resultConfig = recipeConfig.getConfig("result");

        // Determinar el tipo de resultado
        ItemStack result = createResultItem(resultConfig);
        if (result == null) return;

        // Crear clave única
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key.toLowerCase());

        // Crear receta con forma
        ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
        recipe.shape(shape.toArray(new String[0]));

        // NUEVO: Agregar categoría para que aparezca en el libro de recetas (compatible con versiones)
        try {
            // Intentar usar RecipeCategory si está disponible (versiones más recientes)
            Class<?> recipeCategoryClass = Class.forName("org.bukkit.inventory.RecipeCategory");
            Object miscCategory = Enum.valueOf((Class<Enum>) recipeCategoryClass, "MISC");
            recipe.getClass().getMethod("setCategory", recipeCategoryClass).invoke(recipe, miscCategory);

            // También intentar setGroup si está disponible
            recipe.getClass().getMethod("setGroup", String.class).invoke(recipe, "funtalismans_talismans");
        } catch (Exception e) {
            // Fallback para versiones antiguas - no hacer nada, las recetas funcionarán igual
        }

        // Configurar ingredientes (materiales y talismanes)
        for (String ingredientChar : ingredientsConfig.root().keySet()) {
            String ingredientValue = ingredientsConfig.getString(ingredientChar);
            RecipeChoice choice = createIngredientChoice(ingredientValue);

            if (choice != null) {
                recipe.setIngredient(ingredientChar.charAt(0), choice);
            }
        }

        // Registrar receta
        Bukkit.addRecipe(recipe);
        recipes.put(namespacedKey, recipe);

        // Guardar receta avanzada
        advancedRecipes.put(key, new AdvancedRecipe(key, shape, result));
    }

    private ItemStack createResultItem(Config resultConfig) {
        // Si tiene material, crear item normal
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
        }
        // Si tiene talisman, crear talismán
        else if (resultConfig.hasPath("talisman")) {
            String talismanId = resultConfig.getString("talisman");
            Talisman talisman = plugin.getTalismanManager().getTalisman(talismanId);
            return talisman != null ? TalismanItemBuilder.build(talisman) : null;
        }

        return null;
    }

    private RecipeChoice createIngredientChoice(String ingredientValue) {
        // Verificar si es un material normal
        Material material = Material.matchMaterial(ingredientValue);
        if (material != null) {
            return new RecipeChoice.MaterialChoice(material);
        }

        // Verificar si es un talismán
        Talisman talisman = plugin.getTalismanManager().getTalisman(ingredientValue);
        if (talisman != null) {
            return new RecipeChoice.ExactChoice(TalismanItemBuilder.build(talisman));
        }

        return null;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe instanceof ShapedRecipe shapedRecipe &&
                recipes.containsKey(shapedRecipe.getKey())) {

            // Verificar permisos
            if (event.getView().getPlayer() instanceof Player player) {
                if (!player.hasPermission("funtalismans.craft")) {
                    event.getInventory().setResult(null);
                    return;
                }
            }

            // Verificar que la receta sea exacta
            if (!isAdvancedRecipeValid(event.getInventory(), shapedRecipe)) {
                event.getInventory().setResult(null);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Descubrir recetas para el jugador que se conecta
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            discoverRecipesForPlayer(event.getPlayer());
        }, 20L);
    }

    private void discoverRecipesForPlayer(Player player) {
        for (NamespacedKey recipeKey : recipes.keySet()) {
            try {
                player.discoverRecipe(recipeKey);
            } catch (Exception e) {
                // Silencioso
            }
        }
    }

    public void discoverRecipesForOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            discoverRecipesForPlayer(player);
        }
    }

    private boolean isAdvancedRecipeValid(CraftingInventory inventory, ShapedRecipe recipe) {
        ItemStack[] matrix = inventory.getMatrix();
        String[] shape = recipe.getShape();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length(); j++) {
                int slot = i * 3 + j;
                char ingredientChar = shape[i].charAt(j);
                ItemStack itemInSlot = matrix[slot];

                RecipeChoice choice = recipe.getChoiceMap().get(ingredientChar);
                if (choice != null && !choice.test(itemInSlot)) {
                    return false;
                } else if (choice == null && itemInSlot != null && !itemInSlot.getType().isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createDefaultRecipes() {
        // Recetas por defecto si no hay configuración
        createDefaultTalismanRecipes();
    }

    private void createDefaultTalismanRecipes() {
        // Ejemplo: Combinar dos talismanes para crear uno más poderoso
        Talisman sphereTitan = plugin.getTalismanManager().getTalisman("sphereTitan");
        Talisman castigador = plugin.getTalismanManager().getTalisman("talismanCastigador");

        if (sphereTitan != null && castigador != null) {
            // Crear talismán de fusión (ejemplo)
            Talisman fusionTalisman = plugin.getTalismanManager().getTalisman("fusionTalisman");
            if (fusionTalisman != null) {
                registerFusionRecipe(fusionTalisman, sphereTitan, castigador);
            }
        }
    }

    private void registerFusionRecipe(Talisman result, Talisman... ingredients) {
        NamespacedKey key = new NamespacedKey(plugin, "fusion_" + result.getId().toLowerCase());
        ItemStack resultItem = TalismanItemBuilder.build(result);

        ShapedRecipe recipe = new ShapedRecipe(key, resultItem);
        recipe.shape("S C", " D ", "   ");

        try {
            // Intentar usar RecipeCategory si está disponible
            Class<?> recipeCategoryClass = Class.forName("org.bukkit.inventory.RecipeCategory");
            Object miscCategory = Enum.valueOf((Class<Enum>) recipeCategoryClass, "MISC");
            recipe.getClass().getMethod("setCategory", recipeCategoryClass).invoke(recipe, miscCategory);
            recipe.getClass().getMethod("setGroup", String.class).invoke(recipe, "funtalismans_fusion");
        } catch (Exception e) {
            // Fallback para versiones antiguas
        }

        recipe.setIngredient('S', new RecipeChoice.ExactChoice(TalismanItemBuilder.build(ingredients[0])));
        recipe.setIngredient('C', new RecipeChoice.ExactChoice(TalismanItemBuilder.build(ingredients[1])));
        recipe.setIngredient('D', Material.DRAGON_EGG);

        Bukkit.addRecipe(recipe);
        recipes.put(key, recipe);
    }

    public void reloadRecipes() {
        loadRecipes();
        // Rediscover recipes for online players
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            discoverRecipesForOnlinePlayers();
        }, 20L);
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : recipes.keySet()) {
            Bukkit.removeRecipe(key);
        }
        recipes.clear();
    }

    public List<String> getRecipeKeys() {
        return new ArrayList<>(advancedRecipes.keySet());
    }

    public Set<NamespacedKey> getRecipeNamespacedKeys() {
        return new HashSet<>(recipes.keySet());
    }

    /**
     * Método auxiliar para verificar si RecipeCategory está disponible
     */
    public boolean isRecipeCategoryAvailable() {
        try {
            Class.forName("org.bukkit.inventory.RecipeCategory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // Clase para recetas avanzadas
    private static class AdvancedRecipe {
        private final String key;
        private final List<String> shape;
        private final ItemStack result;

        public AdvancedRecipe(String key, List<String> shape, ItemStack result) {
            this.key = key;
            this.shape = shape;
            this.result = result;
        }
    }
}