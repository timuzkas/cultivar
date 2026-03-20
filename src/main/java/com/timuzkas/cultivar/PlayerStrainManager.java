package com.timuzkas.cultivar;

import java.util.*;
import org.bukkit.plugin.Plugin;

public class PlayerStrainManager {
    private final Database database;
    private final Map<UUID, Set<String>> playerStrains = new HashMap<>();
    private final Map<UUID, Map<String, CropType>> playerStrainTypes = new HashMap<>();

    public PlayerStrainManager(Database database, Plugin plugin) {
        this.database = database;
    }

    public void loadPlayerStrains(UUID playerUuid) {
        try {
            Map<String, CropType> strainsWithTypes = database.loadPlayerStrainsWithTypes(playerUuid);
            playerStrains.put(playerUuid, new HashSet<>(strainsWithTypes.keySet()));
            playerStrainTypes.put(playerUuid, new HashMap<>(strainsWithTypes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getDiscoveredStrains(UUID playerUuid) {
        return playerStrains.computeIfAbsent(playerUuid, k -> new HashSet<>());
    }

    public CropType getStrainCropType(UUID playerUuid, String strainId) {
        Map<String, CropType> types = playerStrainTypes.get(playerUuid);
        if (types != null && types.containsKey(strainId)) {
            return types.get(strainId);
        }
        return CropType.CANNABIS;
    }

    public void addDiscoveredStrain(UUID playerUuid, String strainId, CropType cropType) {
        Set<String> strains = playerStrains.computeIfAbsent(playerUuid, k -> new HashSet<>());
        if (strains.add(strainId)) {
            try {
                database.savePlayerStrain(playerUuid, strainId, cropType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        Map<String, CropType> types = playerStrainTypes.computeIfAbsent(playerUuid, k -> new HashMap<>());
        types.put(strainId, cropType);
    }

    public void clearPlayerStrains(UUID playerUuid) {
        playerStrains.remove(playerUuid);
        playerStrainTypes.remove(playerUuid);
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
