package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class FurnaceListener implements Listener {

    private static final NamespacedKey CURE_TYPE = new NamespacedKey("cultivar", "cure_type");

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack input = event.getSource();
        if (ItemFactory.isWetTobaccoLeaf(input)) {
            ItemStack result = ItemFactory.createDryTobaccoLeaf();
            
            org.bukkit.block.Block furnaceBlock = event.getBlock();
            ItemStack fuel = null;
            
            if (furnaceBlock.getState() instanceof org.bukkit.block.Furnace furnace) {
                fuel = furnace.getInventory().getFuel();
            }
            
            String cureType = "default";
            if (fuel != null) {
                if (fuel.getType() == Material.OAK_LOG) {
                    cureType = "light";
                } else if (fuel.getType() == Material.JUNGLE_LOG) {
                    cureType = "dark";
                } else if (fuel.getType() == Material.SOUL_SAND) {
                    cureType = "fire";
                }
            }
            
            if (!cureType.equals("default")) {
                ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(CURE_TYPE, PersistentDataType.STRING, cureType);
                    
                    String displayName = switch (cureType) {
                        case "light" -> "§7Light-cured Leaf";
                        case "dark" -> "§6Dark-cured Leaf";
                        case "fire" -> "§cFire-cured Leaf";
                        default -> "§7Dry Tobacco Leaf";
                    };
                    meta.setDisplayName(displayName);
                    result.setItemMeta(meta);
                }
            }
            
            event.setResult(result);
        }
    }
    
    public static String getCureType(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return null;
        return item.getItemMeta().getPersistentDataContainer().get(CURE_TYPE, PersistentDataType.STRING);
    }
}