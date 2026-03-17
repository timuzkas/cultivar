package com.timuzkas.cultivar;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
              water_source_bonus INTEGER DEFAULT 0
            );
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public List<CropRecord> loadAll() throws SQLException {
        List<CropRecord> records = new ArrayList<>();

        String sql = "SELECT * FROM crops";

        try (
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                CropRecord record = new CropRecord();

                record.id = rs.getString("id");
                record.ownerUuid = UUID.fromString(rs.getString("owner_uuid"));

                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");

                record.location = new Location(world, x, y, z);

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

                records.add(record);
            }
        }

        return records;
    }

    public void save(CropRecord record) throws SQLException {
        String sql = """
            INSERT OR REPLACE INTO crops (
            id, world, x, y, z, crop_type, stage, owner_uuid,
            planted_at, stage_advanced_at, last_watered, last_pruned,
            last_stripped, last_misted, stress, flags, heat_bonus, water_source_bonus, death_reason
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

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
}
