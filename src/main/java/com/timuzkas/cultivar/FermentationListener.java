package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.block.Block;

public class FermentationListener implements Listener {
    private final FermentationManager fermentationManager;
    private final Plugin plugin;

    public FermentationListener(FermentationManager fermentationManager, Plugin plugin) {
        this.fermentationManager = fermentationManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;
        
        ItemStack cursor = event.getCursor();
        if (cursor == null || cursor.getType() == Material.AIR) return;
        
        boolean isBud = ItemFactory.isCannabisBud(cursor) && !ItemFactory.isFermented(cursor);
        boolean isTobacco = ItemFactory.isDryTobaccoLeaf(cursor) && !ItemFactory.isAgedTobacco(cursor);
        if (!isBud && !isTobacco) return;
        
        Location loc = chest.getLocation();
        if (loc.getBlockY() >= plugin.getConfig().getInt("cultivar.fermentation.min-y-level", 40)) return;
        
        Block block = loc.getBlock();
        if (block.getLightLevel() > plugin.getConfig().getInt("cultivar.fermentation.required-light-level", 0)) return;
        
        long duration = plugin.getConfig().getLong("cultivar.fermentation.duration-minutes", 40) * 60000L;
        FermentEntry entry = new FermentEntry();
        entry.id = java.util.UUID.randomUUID().toString();
        entry.chestLocation = loc;
        entry.slotIndex = event.getSlot();
        entry.itemType = isBud ? "bud" : "tobacco";
        entry.startedAt = System.currentTimeMillis();
        entry.fermentDue = entry.startedAt + duration;
        
        fermentationManager.register(entry);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;
        
        for (org.bukkit.inventory.ItemStack item : event.getNewItems().values()) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            boolean isBud = ItemFactory.isCannabisBud(item) && !ItemFactory.isFermented(item);
            boolean isTobacco = ItemFactory.isDryTobaccoLeaf(item) && !ItemFactory.isAgedTobacco(item);
            if (!isBud && !isTobacco) continue;
            
            Location loc = chest.getLocation();
            if (loc.getBlockY() >= plugin.getConfig().getInt("cultivar.fermentation.min-y-level", 40)) continue;
            
            Block block = loc.getBlock();
            if (block.getLightLevel() > plugin.getConfig().getInt("cultivar.fermentation.required-light-level", 0)) continue;
            
            for (int slot : event.getRawSlots()) {
                if (slot >= 0 && slot < chest.getInventory().getSize()) {
                    long duration = plugin.getConfig().getLong("cultivar.fermentation.duration-minutes", 40) * 60000L;
                    FermentEntry entry = new FermentEntry();
                    entry.id = java.util.UUID.randomUUID().toString();
                    entry.chestLocation = loc;
                    entry.slotIndex = slot;
                    entry.itemType = isBud ? "bud" : "tobacco";
                    entry.startedAt = System.currentTimeMillis();
                    entry.fermentDue = entry.startedAt + duration;
                    
                    fermentationManager.register(entry);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;
        Location loc = chest.getLocation();
        if (fermentationManager.isWatched(loc)) {
            fermentationManager.markDisturbed(loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type != Material.CHEST && type != Material.TRAPPED_CHEST) return;
        Location loc = event.getBlock().getLocation();
        if (fermentationManager.isWatched(loc)) {
            fermentationManager.removeEntriesAt(loc);
        }
    }
}
