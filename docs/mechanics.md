---
title: Mechanics
layout: default
---

## Watering System

Crops require consistent watering to grow properly.

### How It Works

1. When you plant a seed, farmland starts dry
2. Right-click with water bucket on the farmland to water it
3. Water expires after a configurable time (default: 5 minutes per stage)
4. Re-water before expiry to maintain growth

### Water Expiry

If a crop stage completes without water, the crop enters a "thirsty" state:
- Growth pauses until watered
- Extended thirst (2+ stages missed) causes stress
- Very extended thirst kills the crop

### Detection

The plugin checks for water within a 1-block radius of the farmland. Water blocks persist; water in buckets does not.

---

## Stress System

Stress accumulates from neglect and environmental factors.

### Stress Sources

| Source | Crop | Effect |
|--------|------|--------|
| Missed watering | All | +1 stress per missed stage |
| Midday sun | Cannabis | +1 stress per sunny period |
| Overcrowding | Cannabis | +2 stress if within 2 blocks of another |
| Overdue pruning | Cannabis | +1 stress per overdue prune |
| Failed stripping | Tobacco | +2 stress per failed stage |
| Wrong light level | Tea | +1 stress per incorrect light period |
| Light exposure | Mushrooms | +2 stress if light > 6 |

### Stress Effects

- **0-2 stress**: No yield penalty
- **3-5 stress**: -20% yield
- **6-10 stress**: -50% yield
- **11+ stress**: -80% yield, possible death

### Reducing Stress

- Harvest with zero stress for bonus reputation (+3 points)
- Some strains have stress resistance genetics
- Proper care prevents accumulation

---

## Strain Genetics

Each crop has a procedurally generated strain with unique properties.

### How Strains Work

When you plant a seed, the game generates a deterministic strain based on the seed's metadata. This means the same seed always produces the same strain.

### Strain Properties

**Cannabis**
- Yield Bonus: -20% to +40%
- Speed Multiplier: 0.8x to 1.3x
- Stress Resistance: Low, Medium, High
- Potency Tier: I, II, III, IV

**Tobacco**
- Curability Bonus: affects curing quality
- Leaf Yield: number of leaves per harvest
- Aroma Profile: Mild, Standard, Rich, Bold

**Tea**
- Brew Strength: affects effect duration
- Rarity Tag: Common, Rare, Legendary

**Mushrooms**
- Potency Level: I, II, III, IV
- Light Tolerance: how much light before stress
- Spore Density: affects yield

### Discovering Strains

Harvest crops to discover strains. New strains appear in your Strain Journal. You can share strains by giving seeds to other players.

---

## Fermentation

Cannabis buds can be fermented for enhanced quality.

### Process

1. Place cannabis buds in a composter
2. Store the composter in a dark location (Y < 40, no sky access)
3. Wait 40 minutes
4. Collect fermented buds

### Fermented vs Regular

| Property | Regular | Fermented |
|----------|---------|-----------|
| Base potency | Potency Tier | +1 Tier |
| Yield | Strain-based | -20% |
| Effects | Standard | Enhanced duration |

---

## Tobacco Curing

Cured tobacco produces higher quality leaves with better effects.

### Curing Methods

**Light-Cured**
- Dried leaves in sunlight for 20 minutes
- Mild flavor, standard effects

**Dark-Cured**
- Dried leaves in darkness for 40 minutes
- Rich flavor, enhanced effects

**Fire-Cured**
- Dried leaves near fire (3+ blocks away) for 30 minutes
- Bold aroma, +20% saturation in cigarettes

**Air-Cured**
- Dried leaves in open air for 60 minutes
- Maximum leaf preservation, +10% yield

**Aged**
- Any cured leaf + 20 minutes
- All stats improved by 10%

### Curing Process

1. Dry leaves on drying rack
2. Place dried leaves in composter
3. Apply curing method (light, fire proximity, darkness, open air)
4. Wait for cure time
5. Collect cured leaves

---

## Tea Brewing

See the [Crafting](/crafting) page for tea types and recipes.

### Perfect Steep Bonus

Bottling tea at the exact moment it's ready gives:
- +50% effect duration
- +2 reputation points

### Brewing in Bulk

Place multiple teapots on a cauldron. Each teapot:
- Requires its own tea leaves
- Has independent brew timers
- Drains 1 water level from cauldron per teapot

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
| Prune cannabis | +1 |
| Strip tobacco leaves | +1 |
| Mist tea leaves | +1 |
| Cross-breed strains | +10 |
| Zero-stress harvest | +3 |
| Perfect steep (tea) | +2 |
| Harvest new strain | +3 |
| Craft pipe | +1 |

### Rank Benefits

Higher ranks unlock:
- Potential for new seed varieties (future feature)
- Reduced cooldowns on smoking
- Access to special recipes

---

## Soil Enrichment

Improve farmland productivity with compost.

### Levels

| Level | Growth Speed Bonus |
|-------|-------------------|
| 0 | 1.0x (baseline) |
| 1 | 1.1x |
| 2 | 1.2x |
| 3 | 1.3x |

### Depletion

Each level depletes by 0.1 per harvest. Level 3 farmland becomes level 2 after one harvest, eventually depleting to level 0.

### Commands

- `/cv soil get` - Check soil enrichment level
- `/cv soil set <level>` - Set enrichment level (admin)
- `/cv soil clear` - Reset to level 0

---

## Proximity Notifications

When you stand near your crops, the plugin shows:

- Current growth stage
- Water status (time until expiry)
- Pending care actions (prune, strip, mist)
- Current stress level

This helps you manage multiple crops without checking each one individually.
