package com.timuzkas.cultivar;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FermentationManager {
    private final List<FermentEntry> activeEntries = new ArrayList<>();
    private final Set<Location> watchedChests = new HashSet<>();

    public void register(FermentEntry entry) {
        activeEntries.add(entry);
        watchedChests.add(entry.chestLocation);
    }

    public void markDisturbed(Location chestLocation) {
        for (FermentEntry entry : activeEntries) {
            if (entry.chestLocation.equals(chestLocation)) {
                entry.disturbed = true;
            }
        }
    }

    public void removeEntriesAt(Location chestLocation) {
        activeEntries.removeIf(entry -> entry.chestLocation.equals(chestLocation));
        watchedChests.remove(chestLocation);
    }

    public List<FermentEntry> getDueEntries(long now) {
        List<FermentEntry> due = new ArrayList<>();
        for (FermentEntry entry : activeEntries) {
            if (entry.fermentDue <= now && !entry.disturbed) {
                due.add(entry);
            }
        }
        return due;
    }

    public void removeEntry(String id) {
        activeEntries.removeIf(entry -> entry.id.equals(id));
    }

    public boolean isWatched(Location location) {
        return watchedChests.contains(location);
    }

    public List<FermentEntry> getActiveEntries() {
        return activeEntries;
    }
}
