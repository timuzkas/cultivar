package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CropBreakListener implements Listener {
    private final CropManager cropManager;

    public CropBreakListener(CropManager cropManager) {
        this.cropManager = cropManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        CropRecord crop = cropManager.getByLocation(event.getBlock().getLocation());
        if (crop != null) {
            event.setDropItems(false);
            event.setCancelled(true);
            
            revertBlock(crop);
            
            try {
                cropManager.remove(event.getBlock().getLocation());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void revertBlock(CropRecord crop) {
        crop.location.getBlock().setType(Material.AIR);
    }
}