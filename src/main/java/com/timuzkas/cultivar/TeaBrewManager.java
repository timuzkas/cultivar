package com.timuzkas.cultivar;

import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeaBrewManager {
    public final Map<Location, BrewSession> activeSessions = new HashMap<>();

    public enum SteepQuality {
        WEAK,
        PERFECT,
        BITTER
    }

    public static class BrewSession {
        public long startedAt;
        public long minBrewTime;
        public long maxBrewTime;
        public String variant;
        public boolean complete;
        public UUID startedBy;
        public SteepQuality quality;
        public String blendIngredient;
        public String strainId;
        public String strainName;
    }
}