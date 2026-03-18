package com.timuzkas.cultivar;

import java.sql.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GrowerManager {
    private final Database database;
    private final Plugin plugin;
    private final Map<UUID, Integer> playerScores = new HashMap<>();
    private final Map<UUID, String> playerTitles = new HashMap<>();

    public GrowerManager(Database database, Plugin plugin) {
        this.database = database;
        this.plugin = plugin;
    }

    public void loadPlayer(UUID playerUuid, String playerName) {
        try {
            int score = database.loadGrowScore(playerUuid);
            String title = database.loadGrowerTitle(playerUuid);
            playerScores.put(playerUuid, score);
            playerTitles.put(playerUuid, title);
            database.saveGrowerReputation(playerUuid, playerName, score, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getScore(UUID playerUuid) {
        return playerScores.getOrDefault(playerUuid, 0);
    }

    public String getTitle(UUID playerUuid) {
        return playerTitles.getOrDefault(playerUuid, "Apprentice");
    }

    public String addScore(UUID playerUuid, String playerName, int amount) {
        int oldScore = playerScores.getOrDefault(playerUuid, 0);
        String oldTitle = getTitle(playerUuid);
        
        int newScore = oldScore + amount;
        playerScores.put(playerUuid, newScore);
        
        String newTitle = calculateTitle(newScore);
        playerTitles.put(playerUuid, newTitle);
        
        try {
            database.updateGrowScore(playerUuid, newScore);
            if (!oldTitle.equals(newTitle)) {
                database.updateGrowTitle(playerUuid, newTitle);
            }
            database.saveGrowerReputation(playerUuid, playerName, newScore, newTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (!oldTitle.equals(newTitle)) {
            return newTitle;
        }
        return null;
    }

    public void setPlayerName(UUID playerUuid, String playerName) {
        try {
            int score = playerScores.getOrDefault(playerUuid, 0);
            String title = playerTitles.getOrDefault(playerUuid, "Apprentice");
            database.saveGrowerReputation(playerUuid, playerName, score, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String calculateTitle(int score) {
        Map<String, Integer> thresholds = getTitleThresholds();
        
        String bestTitle = "Apprentice";
        for (Map.Entry<String, Integer> entry : thresholds.entrySet()) {
            if (score >= entry.getValue()) {
                bestTitle = entry.getKey();
            }
        }
        return bestTitle;
    }

    private Map<String, Integer> getTitleThresholds() {
        Map<String, Integer> thresholds = new LinkedHashMap<>();
        thresholds.put("Apprentice", plugin.getConfig().getInt("cultivar.reputation.thresholds.apprentice", 0));
        thresholds.put("Cultivator", plugin.getConfig().getInt("cultivar.reputation.thresholds.cultivator", 50));
        thresholds.put("Master Grower", plugin.getConfig().getInt("cultivar.reputation.thresholds.master-grower", 200));
        thresholds.put("Botanist", plugin.getConfig().getInt("cultivar.reputation.thresholds.botanist", 500));
        return thresholds;
    }

    public int getEventScore(String event) {
        return plugin.getConfig().getInt("cultivar.reputation.scores." + event, 0);
    }

    public int getStressBonus(UUID playerUuid) {
        String title = getTitle(playerUuid);
        return switch (title) {
            case "Master Grower" -> 2;
            case "Cultivator" -> 1;
            default -> 0;
        };
    }

    public int getYieldFloorBonus(UUID playerUuid) {
        String title = getTitle(playerUuid);
        return switch (title) {
            case "Master Grower" -> 2;
            case "Cultivator" -> 1;
            default -> 0;
        };
    }

    public float getSeedQualityBonus(UUID playerUuid) {
        String title = getTitle(playerUuid);
        return switch (title) {
            case "Master Grower" -> 0.10f;
            case "Cultivator" -> 0.05f;
            default -> 0.0f;
        };
    }
}
