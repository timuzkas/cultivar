package com.timuzkas.cultivar;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class DryingRackManager {
    private final Map<Location, DryingRackData> racks = new HashMap<>();
    private final Plugin plugin;

    public DryingRackManager(Plugin plugin) {
        this.plugin = plugin;
        startDryingTask();
    }

    public boolean registerRack(Location location) {
        if (racks.containsKey(location)) {
            return false;
        }
        racks.put(location, new DryingRackData(location));
        return true;
    }

    public boolean isRack(Location location) {
        return racks.containsKey(location);
    }

    public DryingRackData getRack(Location location) {
        return racks.get(location);
    }

    public void unregisterRack(Location location) {
        racks.remove(location);
    }

    public Collection<DryingRackData> getAllRacks() {
        return racks.values();
    }

    private void startDryingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (DryingRackData rack : racks.values()) {
                    rack.getReadyItems();
                }
            }
        }.runTaskTimer(plugin, 0, 200);
    }
}
