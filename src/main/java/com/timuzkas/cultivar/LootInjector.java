package com.timuzkas.cultivar;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootInjector implements Listener {
    private final Plugin plugin;
    private final Random random = new Random();

    public LootInjector(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!plugin.getConfig().getBoolean("loot.inject-dungeon", true) &&
            !plugin.getConfig().getBoolean("loot.inject-village", true)) return;

        // Check loot table
        String table = event.getLootTable().getKey().toString();
        if ((table.contains("dungeon") && plugin.getConfig().getBoolean("loot.inject-dungeon", true)) ||
            (table.contains("village") && plugin.getConfig().getBoolean("loot.inject-village", true))) {
            if (random.nextDouble() < 0.1) { // 10% chance
                ItemStack item = getRandomSeed();
                event.getLoot().add(item);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.WANDERING_TRADER && plugin.getConfig().getBoolean("loot.inject-wandering-trader", true)) {
            if (event.getEntity() instanceof org.bukkit.entity.WanderingTrader trader) {
                List<MerchantRecipe> recipes = new ArrayList<>(trader.getRecipes());
                // Add seed trades
                MerchantRecipe recipe = new MerchantRecipe(ItemFactory.createCannabisSeed(), 1, 5, true);
                recipe.addIngredient(new ItemStack(org.bukkit.Material.EMERALD));
                recipes.add(recipe);
                trader.setRecipes(recipes);
            }
        }
    }

    private ItemStack getRandomSeed() {
        double r = random.nextDouble();
        if (r < 0.35) return ItemFactory.createCannabisSeed();
        else if (r < 0.6) return ItemFactory.createTobaccoSeed();
        else if (r < 0.8) return ItemFactory.createTeaSeed();
        else return ItemFactory.createSporeItem();
    }
}