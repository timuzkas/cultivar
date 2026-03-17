package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

public class RecipeRegistry {

    private final Plugin plugin;

    public RecipeRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // Cannabis Seed: GRASS + GREEN_DYE → 1
        if (plugin.getConfig().getBoolean("recipes.cannabis-seed", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "cannabis_seed");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createCannabisSeed()
            );
            recipe.addIngredient(Material.GRASS);
            recipe.addIngredient(Material.GREEN_DYE);
            plugin.getServer().addRecipe(recipe);
        }

        // Tobacco Seed: DEAD_BUSH + BROWN_DYE → 1
        if (plugin.getConfig().getBoolean("recipes.tobacco-seed", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "tobacco_seed");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createTobaccoSeed()
            );
            recipe.addIngredient(Material.DEAD_BUSH);
            recipe.addIngredient(Material.BROWN_DYE);
            plugin.getServer().addRecipe(recipe);
        }

        // Tea Seed: FERN + CYAN_DYE → 1
        if (plugin.getConfig().getBoolean("recipes.tea-seed", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "tea_seed");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createTeaSeed()
            );
            recipe.addIngredient(Material.FERN);
            recipe.addIngredient(Material.CYAN_DYE);
            plugin.getServer().addRecipe(recipe);
        }

        // Compost: 3x BONE_MEAL + DIRT → 2
        if (plugin.getConfig().getBoolean("recipes.compost", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "compost");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createCompost()
            );
            recipe.addIngredient(3, Material.BONE_MEAL);
            recipe.addIngredient(Material.DIRT);
            plugin.getServer().addRecipe(recipe);
        }

        // Blank Pipe: STICK + COAL + IRON_INGOT → 1
        if (plugin.getConfig().getBoolean("recipes.blank-pipe", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "blank_pipe");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createBlankPipe()
            );
            recipe.addIngredient(Material.STICK);
            recipe.addIngredient(Material.COAL);
            recipe.addIngredient(Material.IRON_INGOT);
            plugin.getServer().addRecipe(recipe);
        }

        // Teapot: IRON_INGOT + BUCKET → 1
        if (plugin.getConfig().getBoolean("recipes.teapot", true)) {
            NamespacedKey key = new NamespacedKey(plugin, "teapot");
            plugin.getServer().removeRecipe(key);
            ShapelessRecipe recipe = new ShapelessRecipe(
                key,
                ItemFactory.createEmptyTeapot()
            );
            recipe.addIngredient(Material.IRON_INGOT);
            recipe.addIngredient(Material.BUCKET);
            plugin.getServer().addRecipe(recipe);
        }
    }
}
