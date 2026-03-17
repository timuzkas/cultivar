package com.timuzkas.cultivar;

import org.bukkit.Chunk;
import org.bukkit.Location;
import java.util.*;
import java.util.stream.Collectors;

public class CropManager {
    private final Database database;
    private final Map<Location, CropRecord> crops = new HashMap<>();

    public CropManager(Database database) {
        this.database = database;
    }

    public void loadAll() throws Exception {
        List<CropRecord> records = database.loadAll();
        for (CropRecord record : records) {
            crops.put(record.location, record);
        }
    }

    public void saveAll() throws Exception {
        for (CropRecord record : crops.values()) {
            if (record.dirty) {
                database.save(record);
            }
        }
    }

    public void saveDirty() throws Exception {
        saveAll();
    }

    public void save(CropRecord record) throws Exception {
        database.save(record);
    }

    public void register(CropRecord record) throws Exception {
        crops.put(record.location, record);
        database.save(record);
    }

    public void remove(Location location) throws Exception {
        CropRecord record = crops.remove(location);
        if (record != null) {
            database.delete(record.id);
        }
    }

    public CropRecord getByLocation(Location location) {
        return crops.get(location);
    }

    public List<CropRecord> getAllInChunk(Chunk chunk) {
        return crops.values().stream()
                .filter(record -> record.location.getChunk().equals(chunk))
                .collect(Collectors.toList());
    }

    public Collection<CropRecord> getAll() {
        return crops.values();
    }
}