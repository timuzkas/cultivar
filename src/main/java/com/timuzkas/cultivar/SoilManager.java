package com.timuzkas.cultivar;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;

public class SoilManager {
    private final Map<Location, Integer> enrichmentCache = new HashMap<>();
    private final Database database;

    public SoilManager(Database database) {
        this.database = database;
    }

    public int getEnrichment(Location farmlandLocation) {
        return enrichmentCache.getOrDefault(farmlandLocation, 0);
    }

    public void setEnrichment(Location farmlandLocation, int level) {
        int clampedLevel = Math.max(0, Math.min(3, level));
        enrichmentCache.put(farmlandLocation, clampedLevel);
        try {
            database.saveSoilEnrichment(farmlandLocation, clampedLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void degrade(Location farmlandLocation) {
        int current = getEnrichment(farmlandLocation);
        if (current > 0) {
            setEnrichment(farmlandLocation, current - 1);
        }
    }

    public void loadFromDatabase() {
        try {
            Map<Location, Integer> data = database.loadAllSoilEnrichment();
            enrichmentCache.putAll(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache(Location location) {
        enrichmentCache.remove(location);
    }
}
