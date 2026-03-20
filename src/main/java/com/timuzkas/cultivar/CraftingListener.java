package com.timuzkas.cultivar;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class CraftingListener implements Listener {

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getMatrix() == null) return;

        ItemStack[] matrix = event.getInventory().getMatrix();
        
        ItemStack cannabisBud = null;
        ItemStack tobaccoLeaf = null;
        ItemStack driedMushroom = null;
        ItemStack driedTeaLeaf = null;
        ItemStack freshTeaLeaf = null;
        ItemStack cannabisTrim = null;

        for (ItemStack item : matrix) {
            if (item == null || item.getItemMeta() == null) continue;

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            
            boolean hasCultivarKey = pdc.getKeys().stream()
                .anyMatch(key -> key.getNamespace().equals("cultivar"));
            
            if (hasCultivarKey) {
                if (ItemFactory.isCannabisBud(item)) {
                    cannabisBud = item;
                } else if (ItemFactory.isCannabisTrim(item)) {
                    cannabisTrim = item;
                } else if (ItemFactory.isWetTobaccoLeaf(item) || ItemFactory.isDryTobaccoLeaf(item)) {
                    tobaccoLeaf = item;
                } else if (ItemFactory.isDriedMushroom(item)) {
                    driedMushroom = item;
                } else if (ItemFactory.isDriedTeaLeaf(item)) {
                    driedTeaLeaf = item;
                } else if (ItemFactory.isFreshTeaLeaf(item)) {
                    freshTeaLeaf = item;
                }
            }
        }

        ItemStack result = null;

        if (cannabisBud != null && tobaccoLeaf != null) {
            String strainId = ItemFactory.getStrainId(cannabisBud);
            String strainName = ItemFactory.getStrainName(cannabisBud);
            String cureType = null;
            if (ItemFactory.isDryTobaccoLeaf(tobaccoLeaf)) {
                ItemMeta meta = tobaccoLeaf.getItemMeta();
                cureType = meta.getPersistentDataContainer().get(
                    new NamespacedKey("cultivar", "cure_type"),
                    PersistentDataType.STRING
                );
            }
            result = ItemFactory.createSpliff(strainId, strainName, cureType);
        } else if (driedMushroom != null && driedTeaLeaf != null) {
            result = ItemFactory.createHerbalFill();
        } else if (freshTeaLeaf != null && cannabisTrim != null) {
            String strainId = ItemFactory.getStrainId(cannabisTrim);
            String strainName = ItemFactory.getStrainName(cannabisTrim);
            result = ItemFactory.createSpicedTeaLeaf(strainId, strainName);
        } else if (cannabisTrim != null) {
            boolean hasDirt = false;
            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.DIRT) {
                    hasDirt = true;
                    break;
                }
            }
            if (hasDirt) {
                result = ItemFactory.createCompost();
            }
        } else {
            for (ItemStack item : matrix) {
                if (item == null || item.getItemMeta() == null) continue;

                boolean isCultivarItem = item.getItemMeta().getPersistentDataContainer().getKeys().stream()
                    .anyMatch(key -> key.getNamespace().equals("cultivar"));

                if (isCultivarItem) {
                    event.getInventory().setResult(null);
                    return;
                }
            }
            return;
        }

        event.getInventory().setResult(result);
    }
}
