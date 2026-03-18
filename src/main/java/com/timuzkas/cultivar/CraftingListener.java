package com.timuzkas.cultivar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getMatrix() == null) return;

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null || item.getItemMeta() == null) continue;

            // Check if it's a Cultivar item by checking any of our keys in PDC
            // We can just check for "cultivar" namespace in keys
            boolean isCultivarItem = item.getItemMeta().getPersistentDataContainer().getKeys().stream()
                .anyMatch(key -> key.getNamespace().equals("cultivar"));

            if (isCultivarItem) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
