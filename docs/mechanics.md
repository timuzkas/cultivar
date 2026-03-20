---
title: Mechanics
layout: default
---

## Watering System

Crops require consistent watering to grow properly.

### How It Works

1. Right-click with a water bucket on the farmland to water it.
2. Water expires after a configurable time:
   - **Cannabis/Tobacco**: 45 minutes
   - **Tea**: 60 minutes
   - **Mushrooms**: 30 minutes
3. Re-water before expiry to maintain growth. Rain also replenishes water.

### Water Expiry

If water expires, growth pauses until re-watered or it rains. Extended neglect causes stress.

---

## Stress System

Stress accumulates from neglect and environmental factors.

### Stress Sources

| Source | Crop | Effect |
|--------|------|--------|
| Midday sun (>13) | Cannabis | +1 stress during midday |
| Overcrowding (<1r) | Cannabis | +1 stress if too close to another |
| Wrong light level | Tea | +1 stress per check |
| Light exposure (>7) | Mushrooms | +1 stress per check |
| Thundering storm | Cannabis | +1 stress to seedlings |
| Missed care | All | Stress increases if pruning/stripping/misting is ignored |

### Stress Effects

High stress slows growth and can lead to **Death**. Plants reaching a high stress threshold will die and be replaced by a wither rose.

### Reducing Stress

Stress can be prevented by maintaining proper environments and timely care. Some strains have **Stress Resistance** genetics.

---

## Strain Genetics

Each crop has a procedurally generated strain with unique properties.

### Properties

- **Yield Bonus**: Affects harvest quantity.
- **Speed Multiplier**: Speeds up or slows down growth.
- **Stress Resistance**: Increases the amount of stress a plant can handle before death.
- **Potency/Aroma**: Higher tiers provide better effects when consumed.
- **Light Tolerance** (Mushrooms): Allows growth in slightly brighter areas.
- **Brew Strength** (Tea): Increases duration of tea effects.

---

## Fermentation & Aging

Controlled aging in a dark, quiet environment enhances product quality.

### Process

1. Place **Cannabis Buds** or **Dry Tobacco** into a **Chest**.
2. The chest must be located below **Y level 40**.
3. The light level must be **0**.
4. Wait for **40 minutes**.

### Restrictions

**DO NOT OPEN** the chest during fermentation. Opening it disturbs the environment and ruins the process.

---

## Tobacco Curing

Tobacco curing is achieved through furnace smelting with specific fuel types.

### Curing Methods

| Fuel | Cure Type |
|------|-----------|
| **Oak Log** | Light-cured |
| **Jungle Log** | Dark-cured |
| **Soul Sand** | Fire-cured |
| **None (Drying Rack)** | Air-cured |
| **Any Other** | Regular Dry Tobacco |

Smelting **Wet Tobacco Leaves** with the logs/sand in a furnace produces cured leaves. Leaving them on a **Drying Rack** for 15 minutes produces Air-cured leaves.

---

## Tea Brewing

Tea is brewed in a cauldron and collected with a teapot.

### Setup

1. Fill a cauldron with water.
2. Place a **Campfire** or **Soul Campfire** directly underneath.
3. Add **2 Tea Leaves** to start the brew.

### Brewing Time & Quality

You can collect tea at any time after brewing starts, but timing affects the final quality:
- **Weak**: Collected before 60 seconds.
- **Perfect**: Collected between 60 and 90 seconds.
- **Bitter**: Collected after 90 seconds.

### Blending

Adding ingredients to your off-hand while starting the brew can create blends:
- **Sweet Berries**: Adds a regeneration bonus.
- **Sugar**: Smoother flavor.

---

## Reputation System

Track your cultivation progress through four ranks.

### Ranks

| Rank | Points Required |
|------|-----------------|
| Apprentice | 0 |
| Cultivator | 50 |
| Master Grower | 200 |
| Botanist | 500 |

### Earning Points

| Action | Points |
|--------|--------|
| Harvest any crop | +2 |
| Prune/Strip/Mist | +1 |
| Perfect steep (Tea) | +2 |
| Zero-stress harvest | +3 |
| Harvest new strain | +3 |
| Cross-breed strains | +10 |

---

## Soil Enrichment

Improve farmland productivity with compost.

### Levels

- **Level 0**: 1.0x speed (standard)
- **Level 1**: 1.1x speed
- **Level 2**: 1.2x speed
- **Level 3**: 1.3x speed

Soil level decreases by 1 after each harvest. Check level with `/cv soil get`.

---

## Proximity Notifications

When you are within 4 blocks of your crops, you will see a status update (ActionBar) showing:
- Growth stage and time remaining.
- Water status.
- Pending care actions.
- Stress level.
