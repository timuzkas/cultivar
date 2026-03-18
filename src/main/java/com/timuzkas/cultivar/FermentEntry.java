package com.timuzkas.cultivar;

import org.bukkit.Location;

public class FermentEntry {
    public String id;
    public Location chestLocation;
    public int slotIndex;
    public String itemType;
    public long startedAt;
    public long fermentDue;
    public boolean disturbed;

    public FermentEntry() {}
}
