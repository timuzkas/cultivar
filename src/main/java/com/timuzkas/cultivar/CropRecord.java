package com.timuzkas.cultivar;

import org.bukkit.Location;
import java.util.Set;
import java.util.UUID;
import java.util.EnumSet;

public class CropRecord {
    public String id;
    public UUID ownerUuid;
    public Location location;
    public CropType type;
    public int stage;
    public long plantedAt;
    public long stageAdvancedAt;
    public long lastWatered;
    public long lastPruned;
    public long lastStripped;
    public long lastMisted;
    public int stress;
    public Set<CropFlag> flags = EnumSet.noneOf(CropFlag.class);
    public boolean heatBonus;
    public boolean waterSourceBonus;
    public boolean dirty = false;
    public String deathReason = null;

    public CropRecord() {}

    public long getStageTimeMs(org.bukkit.configuration.Configuration config) {
        long baseMinutes = config.getLong("cultivar." + type.name().toLowerCase() + ".stage-minutes", 20);
        long stageTime = baseMinutes * 60000;
        double bonus = heatBonus ? config.getDouble("cultivar.tobacco.heat-bonus-percent", 20) / 100.0 : 0;
        if (type == CropType.TEA && waterSourceBonus) {
            bonus = config.getDouble("cultivar.tea.water-source-bonus-percent", 15) / 100.0;
        }
        return (long) (stageTime * (1 - bonus));
    }

    public CropRecord(String id, UUID ownerUuid, Location location, CropType type, int stage,
                      long plantedAt, long stageAdvancedAt, long lastWatered, long lastPruned,
                      long lastStripped, long lastMisted, int stress, Set<CropFlag> flags, boolean heatBonus, boolean waterSourceBonus, String deathReason) {
        this.id = id;
        this.ownerUuid = ownerUuid;
        this.location = location;
        this.type = type;
        this.stage = stage;
        this.plantedAt = plantedAt;
        this.stageAdvancedAt = stageAdvancedAt;
        this.lastWatered = lastWatered;
        this.lastPruned = lastPruned;
        this.lastStripped = lastStripped;
        this.lastMisted = lastMisted;
        this.stress = stress;
        this.flags = flags != null ? flags : EnumSet.noneOf(CropFlag.class);
        this.heatBonus = heatBonus;
        this.waterSourceBonus = waterSourceBonus;
        this.deathReason = deathReason;
    }
}