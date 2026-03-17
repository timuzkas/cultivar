package com.timuzkas.cultivar;

import java.sql.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlayerStrainManager {
    private final Database database;
    private final Map<UUID, Set<String>> playerStrains = new HashMap<>();

    public PlayerStrainManager(Database database, Plugin plugin) {
        this.database = database;
    }

    public void loadPlayerStrains(UUID playerUuid) {
        try {
            Set<String> strains = database.loadPlayerStrains(playerUuid);
            playerStrains.put(playerUuid, strains);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getDiscoveredStrains(UUID playerUuid) {
        return playerStrains.computeIfAbsent(playerUuid, k -> new HashSet<>());
    }

    public void addDiscoveredStrain(UUID playerUuid, String strainId) {
        Set<String> strains = playerStrains.computeIfAbsent(playerUuid, k -> new HashSet<>());
        if (strains.add(strainId)) {
            try {
                database.savePlayerStrain(playerUuid, strainId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearPlayerStrains(UUID playerUuid) {
        playerStrains.remove(playerUuid);
        try {
            database.clearPlayerStrains(playerUuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getAllKnownStrains() {
        Set<String> allStrains = new HashSet<>();
        for (Set<String> strains : playerStrains.values()) {
            allStrains.addAll(strains);
        }
        return allStrains;
    }
}
