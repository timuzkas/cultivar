package com.timuzkas.cultivar;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class Database {

    private final Plugin plugin;
    private Connection connection;

    public Database(Plugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        connection = DriverManager.getConnection(
            "jdbc:sqlite:" + plugin.getDataFolder() + "/cultivar.db"
        );

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
            stmt.execute("PRAGMA synchronous=NORMAL;");
        }

        createTables();
    }

    private void createTables() throws SQLException {
        // Snapshot existing columns before CREATE IF NOT EXISTS runs
        Set<String> existingColumns = new HashSet<>();
        try (
            ResultSet rs = connection
                .getMetaData()
                .getColumns(null, null, "crops", null)
        ) {
            while (rs.next()) {
                existingColumns.add(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            // Table doesn't exist yet — will be created with all columns below
        }

        String sql = """
            CREATE TABLE IF NOT EXISTS crops (
              id TEXT PRIMARY KEY,
              world TEXT NOT NULL,
              x INTEGER NOT NULL,
              y INTEGER NOT NULL,
              z INTEGER NOT NULL,
              crop_type TEXT NOT NULL,
              stage INTEGER DEFAULT 0,
              owner_uuid TEXT NOT NULL,
              planted_at INTEGER NOT NULL,
              stage_advanced_at INTEGER NOT NULL,
              last_watered INTEGER DEFAULT 0,
              last_pruned INTEGER DEFAULT 0,
              last_stripped INTEGER DEFAULT 0,
              last_misted INTEGER DEFAULT 0,
              stress INTEGER DEFAULT 0,
              flags TEXT DEFAULT '',
              heat_bonus INTEGER DEFAULT 0,
              water_source_bonus INTEGER DEFAULT 0,
              death_reason TEXT,
              strain_id TEXT,
              strain_name TEXT,
              cold_biome INTEGER DEFAULT 0,
              last_stress_check INTEGER DEFAULT 0
            );
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);

            // Migrate older databases that are missing newer columns.
            // Only run ALTER TABLE when the table already existed (existingColumns non-empty).
            if (!existingColumns.isEmpty()) {
                if (!existingColumns.contains("death_reason")) {
                    try {
                        stmt.execute(
                            "ALTER TABLE crops ADD COLUMN death_reason TEXT"
                        );
                    } catch (SQLException ignored) {}
                }
                if (!existingColumns.contains("strain_id")) {
                    try {
                        stmt.execute(
                            "ALTER TABLE crops ADD COLUMN strain_id TEXT"
                        );
                    } catch (SQLException ignored) {}
                }
                if (!existingColumns.contains("strain_name")) {
                    try {
                        stmt.execute(
                            "ALTER TABLE crops ADD COLUMN strain_name TEXT"
                        );
                    } catch (SQLException ignored) {}
                }
                if (!existingColumns.contains("cold_biome")) {
                    try {
                        stmt.execute(
                            "ALTER TABLE crops ADD COLUMN cold_biome INTEGER DEFAULT 0"
                        );
                    } catch (SQLException ignored) {}
                }
                if (!existingColumns.contains("last_stress_check")) {
                    try {
                        stmt.execute(
                            "ALTER TABLE crops ADD COLUMN last_stress_check INTEGER DEFAULT 0"
                        );
                    } catch (SQLException ignored) {}
                }
            }
        }

        createPlayerStrainsTable();
        createSoilTable();
        createGrowerReputationTable();
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public List<CropRecord> loadAll() throws SQLException {
        List<CropRecord> records = new ArrayList<>();

        try (
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM crops"
            );
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                CropRecord record = new CropRecord();

                record.id = rs.getString("id");
                record.ownerUuid = UUID.fromString(rs.getString("owner_uuid"));

                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                record.location = new Location(
                    world,
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z")
                );

                record.type = CropType.valueOf(rs.getString("crop_type"));
                record.stage = rs.getInt("stage");

                record.plantedAt = rs.getLong("planted_at");
                record.stageAdvancedAt = rs.getLong("stage_advanced_at");

                record.lastWatered = rs.getLong("last_watered");
                record.lastPruned = rs.getLong("last_pruned");
                record.lastStripped = rs.getLong("last_stripped");
                record.lastMisted = rs.getLong("last_misted");

                record.stress = rs.getInt("stress");

                record.flags = EnumSet.noneOf(CropFlag.class);
                String flagsStr = rs.getString("flags");
                if (flagsStr != null && !flagsStr.isEmpty()) {
                    for (String flag : flagsStr.split(",")) {
                        try {
                            record.flags.add(CropFlag.valueOf(flag));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }

                record.heatBonus = rs.getInt("heat_bonus") == 1;
                record.waterSourceBonus = rs.getInt("water_source_bonus") == 1;
                record.deathReason = rs.getString("death_reason");
                record.strainId = rs.getString("strain_id");
                record.strainName = rs.getString("strain_name");
                record.coldBiome = rs.getInt("cold_biome") == 1;
                record.lastStressCheck = rs.getLong("last_stress_check");

                records.add(record);
            }
        }

        return records;
    }

    public void save(CropRecord record) throws SQLException {
        String sql = "INSERT OR REPLACE INTO crops (" +
            "id, world, x, y, z, crop_type, stage, owner_uuid, " +
            "planted_at, stage_advanced_at, last_watered, last_pruned, " +
            "last_stripped, last_misted, stress, flags, heat_bonus, " +
            "water_source_bonus, death_reason, strain_id, strain_name, " +
            "cold_biome, last_stress_check" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, record.id);
            stmt.setString(2, record.location.getWorld().getName());
            stmt.setInt(3, record.location.getBlockX());
            stmt.setInt(4, record.location.getBlockY());
            stmt.setInt(5, record.location.getBlockZ());

            stmt.setString(6, record.type.name());
            stmt.setInt(7, record.stage);

            stmt.setString(8, record.ownerUuid.toString());

            stmt.setLong(9, record.plantedAt);
            stmt.setLong(10, record.stageAdvancedAt);

            stmt.setLong(11, record.lastWatered);
            stmt.setLong(12, record.lastPruned);
            stmt.setLong(13, record.lastStripped);
            stmt.setLong(14, record.lastMisted);

            stmt.setInt(15, record.stress);

            StringBuilder flags = new StringBuilder();
            for (CropFlag flag : record.flags) {
                if (flags.length() > 0) flags.append(",");
                flags.append(flag.name());
            }
            stmt.setString(16, flags.toString());

            stmt.setInt(17, record.heatBonus ? 1 : 0);
            stmt.setInt(18, record.waterSourceBonus ? 1 : 0);
            stmt.setString(19, record.deathReason);
            stmt.setString(20, record.strainId);
            stmt.setString(21, record.strainName);
            stmt.setInt(22, record.coldBiome ? 1 : 0);
            stmt.setLong(23, record.lastStressCheck);

            stmt.executeUpdate();
        }

        record.dirty = false;
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM crops WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Soil enrichment
    // -------------------------------------------------------------------------

    public void createSoilTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS soil_enrichment (
              world TEXT NOT NULL,
              x INTEGER NOT NULL,
              y INTEGER NOT NULL,
              z INTEGER NOT NULL,
              level INTEGER DEFAULT 0,
              PRIMARY KEY (world, x, y, z)
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveSoilEnrichment(Location location, int level)
        throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO soil_enrichment (world, x, y, z, level)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            stmt.setInt(5, level);
            stmt.executeUpdate();
        }
    }

    public Map<Location, Integer> loadAllSoilEnrichment() throws SQLException {
        Map<Location, Integer> data = new HashMap<>();
        try (
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM soil_enrichment"
            );
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                data.put(
                    new Location(
                        world,
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z")
                    ),
                    rs.getInt("level")
                );
            }
        }
        return data;
    }

    public void deleteSoilEnrichment(Location location) throws SQLException {
        String sql =
            "DELETE FROM soil_enrichment WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, location.getWorld().getName());
            stmt.setInt(2, location.getBlockX());
            stmt.setInt(3, location.getBlockY());
            stmt.setInt(4, location.getBlockZ());
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Player strains
    // -------------------------------------------------------------------------

    public void createPlayerStrainsTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS player_strains (
                  player_uuid TEXT NOT NULL,
                  strain_id TEXT NOT NULL,
                  crop_type TEXT NOT NULL,
                  PRIMARY KEY (player_uuid, strain_id)
                );
                """;
            stmt.execute(sql);
            
            try {
                stmt.execute("ALTER TABLE player_strains ADD COLUMN crop_type TEXT DEFAULT 'CANNABIS'");
            } catch (SQLException e) {
                // Column already exists
            }
        }
    }

    public Map<String, CropType> loadPlayerStrainsWithTypes(UUID playerUuid) throws SQLException {
        Map<String, CropType> strains = new HashMap<>();
        String sql =
            "SELECT strain_id, crop_type FROM player_strains WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String strainId = rs.getString("strain_id");
                    String cropTypeStr = rs.getString("crop_type");
                    try {
                        CropType type = CropType.valueOf(cropTypeStr);
                        strains.put(strainId, type);
                    } catch (Exception e) {
                        strains.put(strainId, CropType.CANNABIS);
                    }
                }
            }
        }
        return strains;
    }

    public void savePlayerStrain(UUID playerUuid, String strainId, CropType cropType)
        throws SQLException {
        String sql =
            "INSERT OR IGNORE INTO player_strains (player_uuid, strain_id, crop_type) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, strainId);
            stmt.setString(3, cropType.name());
            stmt.executeUpdate();
        }
    }

    public void clearPlayerStrains(UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM player_strains WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Grower reputation
    // -------------------------------------------------------------------------

    public void createGrowerReputationTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS grower_reputation (
              player_uuid TEXT PRIMARY KEY,
              player_name TEXT NOT NULL,
              grow_score INTEGER DEFAULT 0,
              title TEXT DEFAULT 'Apprentice'
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public int loadGrowScore(UUID playerUuid) throws SQLException {
        String sql = "SELECT grow_score FROM grower_reputation WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("grow_score");
                }
            }
        }
        return 0;
    }

    public String loadGrowerTitle(UUID playerUuid) throws SQLException {
        String sql = "SELECT title FROM grower_reputation WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title");
                }
            }
        }
        return "Apprentice";
    }

    public void saveGrowerReputation(UUID playerUuid, String playerName, int score, String title) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO grower_reputation (player_uuid, player_name, grow_score, title)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.setString(2, playerName);
            stmt.setInt(3, score);
            stmt.setString(4, title);
            stmt.executeUpdate();
        }
    }

    public void updateGrowScore(UUID playerUuid, int score) throws SQLException {
        String sql = "UPDATE grower_reputation SET grow_score = ? WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    public void updateGrowTitle(UUID playerUuid, String title) throws SQLException {
        String sql = "UPDATE grower_reputation SET title = ? WHERE player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    public void createFermentationTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS fermentation (
              id TEXT PRIMARY KEY,
              world TEXT NOT NULL,
              chest_x INTEGER NOT NULL,
              chest_y INTEGER NOT NULL,
              chest_z INTEGER NOT NULL,
              slot_index INTEGER NOT NULL,
              item_type TEXT NOT NULL,
              started_at INTEGER NOT NULL,
              ferment_due INTEGER NOT NULL,
              disturbed INTEGER DEFAULT 0
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveFermentEntry(FermentEntry entry) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO fermentation 
            (id, world, chest_x, chest_y, chest_z, slot_index, item_type, started_at, ferment_due, disturbed)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entry.id);
            stmt.setString(2, entry.chestLocation.getWorld().getName());
            stmt.setInt(3, entry.chestLocation.getBlockX());
            stmt.setInt(4, entry.chestLocation.getBlockY());
            stmt.setInt(5, entry.chestLocation.getBlockZ());
            stmt.setInt(6, entry.slotIndex);
            stmt.setString(7, entry.itemType);
            stmt.setLong(8, entry.startedAt);
            stmt.setLong(9, entry.fermentDue);
            stmt.setInt(10, entry.disturbed ? 1 : 0);
            stmt.executeUpdate();
        }
    }

    public void deleteFermentEntry(String id) throws SQLException {
        String sql = "DELETE FROM fermentation WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public List<FermentEntry> loadAllFermentEntries() throws SQLException {
        List<FermentEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM fermentation";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                FermentEntry entry = new FermentEntry();
                entry.id = rs.getString("id");
                String worldName = rs.getString("world");
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                if (world != null) {
                    entry.chestLocation = new org.bukkit.Location(
                        world,
                        rs.getInt("chest_x"),
                        rs.getInt("chest_y"),
                        rs.getInt("chest_z")
                    );
                }
                entry.slotIndex = rs.getInt("slot_index");
                entry.itemType = rs.getString("item_type");
                entry.startedAt = rs.getLong("started_at");
                entry.fermentDue = rs.getLong("ferment_due");
                entry.disturbed = rs.getInt("disturbed") == 1;
                entries.add(entry);
            }
        }
        return entries;
    }
}
