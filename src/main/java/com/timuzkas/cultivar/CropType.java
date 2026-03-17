package com.timuzkas.cultivar;

import org.bukkit.Material;

public enum CropType {
    CANNABIS,
    TOBACCO,
    TEA,
    MUSHROOM;

    public Material getVisualBlock(int stage) {
        return switch (this) {
            case CANNABIS -> stage < 2 ? Material.POTTED_FERN : Material.FERN;
            case TOBACCO -> stage < 2 ? Material.POTTED_DEAD_BUSH : Material.DEAD_BUSH;
            case TEA -> stage < 2 ? Material.POTTED_OAK_SAPLING : Material.AZALEA;
            case MUSHROOM -> {
                if (stage == 0) yield Material.BROWN_MUSHROOM_BLOCK;
                else if (stage < 3) yield Material.RED_MUSHROOM_BLOCK;
                else yield Material.SHROOMLIGHT;
            }
        };
    }

    public int getMaxStage() {
        return switch (this) {
            case CANNABIS -> 4;
            case TOBACCO -> 5;
            case TEA -> 3;
            case MUSHROOM -> 3;
        };
    }
}