package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class DryingRackListener implements Listener {
    private final DryingRackManager rackManager;
    private final ActionBarAnimator animator;

    public DryingRackListener(DryingRackManager rackManager, ActionBarAnimator animator) {
        this.rackManager = rackManager;
        this.animator = animator;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();
        
        if (block == null) return;
        
        boolean isFence = isFence(block.getType());
        
        if (item != null && item.getType() == Material.STRING && isFence) {
            if (player.isSneaking()) {
                if (rackManager.registerRack(block.getLocation())) {
                    item.setAmount(item.getAmount() - 1);
                    player.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOL_PLACE, 1.0f, 1.0f);
                    player.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(0.5, 1, 0.5), 8, 0.3, 0.3, 0.3, 0);
                    animator.reveal(player, "§7Drying rack registered", null);
                    event.setCancelled(true);
                    return;
                } else {
                    animator.reveal(player, "§cAlready a drying rack here", null);
                    event.setCancelled(true);
                    return;
                }
            }
        }
        
        if (rackManager.isRack(block.getLocation())) {
            DryingRackData rack = rackManager.getRack(block.getLocation());
            
            if (item != null && (ItemFactory.isWetTobaccoLeaf(item) || ItemFactory.isFreshTeaLeaf(item))) {
                if (rack.hasSpace()) {
                    item.setAmount(item.getAmount() - 1);
                    rack.addItem(item.clone());
                    player.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOL_PLACE, 0.8f, 1.0f);
                    player.getWorld().spawnParticle(Particle.CLOUD, block.getLocation().add(0.5, 1, 0.5), 5, 0.2, 0.2, 0.2, 0);
                    animator.reveal(player, "§7Hanging to dry... (" + rack.getSlotCount() + "/" + rack.maxSlots + ")", null);
                    event.setCancelled(true);
                    return;
                } else {
                    animator.reveal(player, "§cRack is full", null);
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (item == null || item.getType() == Material.AIR) {
                if (player.isSneaking()) {
                    if (!rack.hanging.isEmpty()) {
                        for (ItemStack hanging : rack.hanging) {
                            player.getInventory().addItem(hanging);
                        }
                        rack.hanging.clear();
                        rack.hungAt.clear();
                        player.getWorld().playSound(block.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.0f);
                        animator.reveal(player, "§7Retrieved all items", null);
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    rack.getReadyItems();
                    if (!rack.getReadyItems().isEmpty()) {
                        int count = rack.getReadyItems().size();
                        for (ItemStack ready : rack.getReadyItems()) {
                            player.getInventory().addItem(convertToDried(ready));
                        }
                        rack.removeReadyItems();
                        player.getWorld().playSound(block.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                        animator.reveal(player, "§aCollected " + count + " dried items", null);
                        event.setCancelled(true);
                        return;
                    } else {
                        animator.reveal(player, "§7Nothing ready yet (" + rack.getSlotCount() + "/" + rack.maxSlots + ")", null);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
    
    private boolean isFence(Material type) {
        return type.name().contains("FENCE") && !type.name().contains("GATE");
    }
    
    private ItemStack convertToDried(ItemStack wet) {
        if (ItemFactory.isWetTobaccoLeaf(wet)) {
            return ItemFactory.createAirCuredLeaf();
        } else if (ItemFactory.isFreshTeaLeaf(wet)) {
            return ItemFactory.createRackDriedTeaLeaf();
        }
        return wet;
    }
}
