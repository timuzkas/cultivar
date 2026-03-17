package com.timuzkas.cultivar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

public class FurnaceListener implements Listener {

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack input = event.getSource();
        if (ItemFactory.isWetTobaccoLeaf(input)) {
            event.setResult(ItemFactory.createDryTobaccoLeaf());
        }
    }
}