package com.timuzkas.cultivar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FermentationTask extends BukkitRunnable {
    private final FermentationManager fermentationManager;
    private final org.bukkit.plugin.Plugin plugin;

    public FermentationTask(FermentationManager fermentationManager, org.bukkit.plugin.Plugin plugin) {
        this.fermentationManager = fermentationManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        java.util.List<FermentEntry> due = fermentationManager.getDueEntries(now);
        
        for (FermentEntry entry : due) {
            processEntry(entry);
        }
    }

    private void processEntry(FermentEntry entry) {
        World world = Bukkit.getWorld(entry.chestLocation.getWorld().getName());
        if (world == null) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        
        if (!entry.chestLocation.getChunk().isLoaded()) return;
        
        Block block = entry.chestLocation.getBlock();
        
        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        
        if (block.getLightLevel() > plugin.getConfig().getInt("cultivar.fermentation.required-light-level", 0)) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        
        Chest chest = (Chest) block.getState();
        ItemStack current = chest.getInventory().getItem(entry.slotIndex);
        
        if (current == null) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        
        if (entry.itemType.equals("bud") && !ItemFactory.isCannabisBud(current)) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        if (entry.itemType.equals("tobacco") && !ItemFactory.isDryTobaccoLeaf(current)) {
            fermentationManager.removeEntry(entry.id);
            return;
        }
        
        int amount = current.getAmount();
        ItemStack transformed = entry.itemType.equals("bud")
            ? ItemFactory.createFermentedBud()
            : ItemFactory.createAgedTobacco();
        transformed.setAmount(amount);
        chest.getInventory().setItem(entry.slotIndex, transformed);
        
        fermentationManager.removeEntry(entry.id);
        
        world.spawnParticle(Particle.SMOKE_NORMAL, 
            entry.chestLocation.clone().add(0.5, 1.1, 0.5), 
            6, 0.2, 0.1, 0.2, 0.01);
    }
}
