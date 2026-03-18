package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import java.util.*;

public class DryingRackData {
    public Location location;
    public List<ItemStack> hanging = new ArrayList<>();
    public Map<ItemStack, Long> hungAt = new HashMap<>();
    public int maxSlots = 4;

    public DryingRackData(Location location) {
        this.location = location;
    }

    public boolean hasSpace() {
        return hanging.size() < maxSlots;
    }

    public boolean addItem(ItemStack item) {
        if (!hasSpace()) return false;
        hanging.add(item);
        hungAt.put(item, System.currentTimeMillis());
        return true;
    }

    public List<ItemStack> getReadyItems() {
        List<ItemStack> ready = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (ItemStack item : hanging) {
            Long hungAtTime = hungAt.get(item);
            if (hungAtTime != null) {
                long elapsed = now - hungAtTime;
                long dryTime = getDryTime();
                if (elapsed >= dryTime) {
                    ready.add(item);
                }
            }
        }
        return ready;
    }

    public void removeReadyItems() {
        List<ItemStack> ready = getReadyItems();
        for (ItemStack item : ready) {
            hanging.remove(item);
            hungAt.remove(item);
        }
    }

    public long getDryTime() {
        return 15 * 60 * 1000;
    }

    public int getSlotCount() {
        return hanging.size();
    }
}
