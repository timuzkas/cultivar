---
title: Crops
layout: default
---

## Overview

Each crop type has unique growth stages, care requirements, and environmental preferences. Understanding these is key to maximizing yield quality.

## Cannabis

Cannabis plants grow through 5 stages over approximately 50 minutes (configurable).

### Acquisition

- Crafted from seeds found in dungeon chests, village chests, or from wandering traders
- See [Loot Tables](#loot-injection) for spawn locations

### Growth Stages

| Stage | Duration | Action Required |
|-------|----------|-----------------|
| Seedling | 10 min | Keep watered |
| Vegetative | 10 min | Keep watered; midday sun causes stress |
| Early Flower | 10 min | Prune 3-4 times per day |
| Late Flower | 10 min | Prune 2-3 times per day |
| Harvest | - | Harvest when ready |

### Care Requirements

**Pruning**: Examine the crop with `/cv inspect` to check pruning needs. Click the crop while holding shears when prompted. Plants need pruning at stages 3-4. Overdue pruning causes stress.

**Sunlight**: Midday sun (light level 15) causes stress. Plant in shaded areas or use partial blocks above to reduce light.

**Spacing**: Overcrowded crops (within 2 blocks of another cannabis plant) suffer stress penalties.

**Watering**: Keep the farmland moist. Water expires after a configurable window.

### Harvesting

Harvest by breaking the mature plant. Yield depends on:
- Strain genetics (yield bonus)
- Stress accumulated during growth
- Watering consistency

Harvested as buds, which can be [fermented](/mechanics#fermentation) or smoked.

---

## Tobacco

Tobacco plants grow through 6 stages over approximately 60 minutes.

### Acquisition

- Start with cured tobacco leaves or tobacco seeds

### Growth Stages

| Stage | Duration | Action Required |
|-------|----------|-----------------|
| Seedling | 10 min | Keep watered |
| Early Growth | 10 min | Strip lower leaves |
| Mid Growth | 10 min | Keep watered |
| Late Growth | 10 min | Strip leaves again |
| Maturation | 10 min | Final leaf strip |
| Harvest | - | Harvest leaves |

### Care Requirements

**Stripping**: At stages 2 and 4, strip lower leaves by clicking the plant with an empty hand. Failure to strip causes stress.

**Heat**: Nearby fire (within 3 blocks, not more than 1 block above) provides a growth speed bonus.

**Watering**: Standard watering requirements apply.

### Harvesting

Harvest leaves by breaking the mature plant. Leaves can be:
- Cured using one of four curing methods
- Dried on drying racks for basic use
- Used to roll cigarettes

---

## Tea

Tea plants grow through 4 stages over approximately 40 minutes.

### Acquisition

- Tea seeds or fresh tea leaves

### Growth Stages

| Stage | Duration | Action Required |
|-------|----------|-----------------|
| Seedling | 10 min | Keep watered |
| Early Growth | 10 min | Mist periodically |
| Late Growth | 10 min | Mist when needed |
| Harvest | - | Harvest leaves |

### Care Requirements

**Misting**: Spray water on leaves periodically using a water bottle. Tea prefers humidity. Mist 3-4 times during growth.

**Light**: Tea prefers partial shade (light level 7-13). Too much or too little light causes stress.

**Water Source**: Growth speed bonus when planted within 2 blocks of a water source block.

### Harvesting

Harvest fresh leaves. Use in [tea brewing](/mechanics#tea-brewing) for beverages with various effects.

---

## Mushrooms

Mushrooms grow through 4 stages over approximately 30 minutes.

### Acquisition

- Mushroom spores from harvesting mature mushrooms

### Growth Stages

| Stage | Duration | Action Required |
|-------|----------|-----------------|
| Spore | 8 min | Darkness required |
| Developing | 8 min | Maintain darkness |
| Maturing | 8 min | Keep dark |
| Harvest | - | Harvest cap |

### Care Requirements

**Light**: Mushrooms require darkness (light level 0-6). Any light above 6 causes stress and stops growth.

**Soil**: Must be planted on mycelium or podzol. Regular dirt or farmland does not work.

**Underground**: Best grown in caves, mineshafts, or underground farms with proper lighting隔离.

### Harvesting

Break the mature mushroom cap to harvest. Mushrooms can be smoked or used as consumables.

---

## Loot Injection

Cannabis seeds spawn in:
- Dungeon chests (stronghold, mineshaft, desert pyramid, jungle temple)
- Village chests (savanna and taiga villages)
- Wandering trader offers

This ensures seeds are obtainable without commands.
