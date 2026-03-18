package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FarmlandInteractListener implements Listener {
    private final SoilManager soilManager;
    private final ActionBarAnimator animator;

    public FarmlandInteractListener(SoilManager soilManager, ActionBarAnimator animator) {
        this.soilManager = soilManager;
        this.animator = animator;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.FARMLAND) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || !ItemFactory.isCompost(item)) return;
        
        event.setCancelled(true);
        
        if (soilManager == null) {
            animator.reveal(player, "§cSoil enrichment not available", null);
            return;
        }

        int currentLevel = soilManager.getEnrichment(block.getLocation());

        if (currentLevel >= 3) {
            animator.reveal(player, "§eSoil already max enriched", null);
            return;
        }

        soilManager.setEnrichment(block.getLocation(), currentLevel + 1);
        item.setAmount(item.getAmount() - 1);

        int newLevel = currentLevel + 1;
        String enrichmentStr = "§a" + "✦".repeat(newLevel) + "✧".repeat(3 - newLevel) + " Soil enriched";

        player.getWorld().playSound(block.getLocation(), org.bukkit.Sound.BLOCK_GRAVEL_PLACE, 1.0f, 1.0f);
        player.getWorld().playSound(block.getLocation(), org.bukkit.Sound.BLOCK_COMPOSTER_FILL, 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1, 0.5), 8, 0.3, 0.3, 0.3, 0);

        animator.reveal(player, enrichmentStr, null);
    }
}
