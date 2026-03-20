---
title: Crafting
layout: default
---

## Seeds

### Cannabis Seeds

Obtained from dungeon chests, village chests, or wandering traders. Not craftable.

### Tea Seeds

Crafted from:
- 1 Green Tea Leaf + 1 Wheat Seeds

### Tobacco Seeds

Crafted from:
- 1 Tobacco Leaf + 1 Wheat Seeds

---

## Smoking Pipes

Three pipe tiers with different durability and smoking effects.

### Wooden Pipe

- Durability: 64 uses
- Effects duration: 30 seconds
- Crafted from:
  - 3 Oak Planks (top row)
  - 1 Stick (center)

### Clay Pipe

- Durability: 128 uses
- Effects duration: 45 seconds
- Crafted from:
  - 3 Terracotta (top row)
  - 1 Stick (center)

### Meerschaum Pipe

- Durability: 256 uses
- Effects duration: 60 seconds
- Crafted from:
  - 3 White Terracotta (top row)
  - 1 Blaze Rod (center)

---

## Smoking Items

### Cigarette

- 1 Paper + 1 Tobacco Leaf
- Effects: Speed I (30s), Saturation (5s)

### Spliff

- 1 Paper + 1 Cannabis Bud + 1 Tobacco Leaf
- Combines effects of cannabis and tobacco

### Cannabis Joint

- 1 Paper + 2 Cannabis Buds
- Effects: Slowness I (45s), Hunger I (30s), Night Vision (60s), Nausea (on use)

---

## Pipe Usage

1. Hold the pipe in main hand
2. Hold the smoking item (cannabis, tobacco, cigarette) in off hand
3. Right-click to smoke
4. Pipe durability decreases by 1
5. Wait for cooldown before smoking again (configurable, default 30 seconds)

---

## Drying Rack

Used for drying tobacco leaves.

### Placement

The drying rack must be placed on a wall (not floor or ceiling). It attaches to solid blocks on its sides.

### Recipe

```
[Oak Planks] [Oak Planks] [Oak Planks]
[Stick    ] [             ] [         ]
[Stick    ] [             ] [         ]
```

Top row: 3 Oak Planks
Middle: 1 Stick
Bottom: 1 Stick

### Usage

1. Place the drying rack on a wall (attach to the side of a solid block)
2. Right-click the rack with fresh tobacco leaves
3. The rack shows a visual indicator of drying progress
4. Wait for drying time (configurable in config.yml)
5. Right-click the rack to collect dried tobacco leaves

Dried leaves are required for [tobacco curing](/mechanics#tobacco-curing). A single rack holds one batch at a time.

### Tips

- Place multiple racks for larger operations
- Drying time can be configured per curing method
- Hanging leaves from the rack for too long (beyond ready) does not ruin them

---

## Tea Brewing

Tea brewing uses a two-part setup: a teapot and a cauldron.

### Teapot

Crafted from:
```
[Copper Ingot] [Copper Ingot] [Copper Ingot]
[Copper Ingot] [Iron Ingot  ] [Copper Ingot]
[Copper Ingot] [Copper Ingot] [Copper Ingot]
```

The iron ingot forms the spout. Place the teapot on a cauldron to begin brewing.

### Tea Types

| Tea | Leaf Required | Brew Time | Effects |
|-----|---------------|-----------|---------|
| Green Tea | 1 Green Tea Leaf | 30s | Regeneration I (30s) |
| Black Tea | 1 Black Tea Leaf | 45s | Speed I (60s), Haste I (30s) |
| Herbal Tea | 1 Herbal Tea Leaf | 30s | Night Vision (120s), Slow Falling (60s) |

### Setup

1. Place a cauldron (on a furnace for water heating)
2. Place the teapot directly on top of the cauldron
3. Fill the cauldron with water (use water buckets)

### Brewing Process

1. Add tea leaves to the teapot (right-click with leaves)
2. The cauldron heats the water and brewing begins automatically
3. Watch for the brew timer — bottling at the exact moment gives a **Perfect Steep** bonus
4. Right-click the cauldron with a glass bottle to bottle the tea
5. Right-click with the tea mug to consume

### Perfect Steep

Bottling tea exactly when brewing completes gives:
- +50% effect duration
- +2 reputation points

### Notes

- The cauldron must have water and a heat source below (furnace, lava, soul fire, etc.)
- Multiple teapots on one cauldron each drain 1 water level
- Bottling too early produces weak tea; too late produces bitter tea with reduced effects
- Each teapot brews independently

---

## Fermentation Chamber

Cannabis buds can be fermented for enhanced potency.

### Setup

1. Place a composter at ground level
2. Fill it with fresh cannabis buds (right-click with buds)
3. Move the composter to a dark location (see below)
4. Wait for fermentation

### Requirements

- **Darkness**: The composter must be at Y < 40, or in a location with no sky access (underground room, enclosed structure)
- **Time**: 40 minutes of real time

### Fermented vs Regular

| Property | Regular | Fermented |
|----------|---------|-----------|
| Potency | Tier I-IV | Tier II-V (+1) |
| Yield | 100% | 80% (-20%) |
| Effect Duration | Base | +50% |

### Tips

- Enclosed underground rooms work well
- Place multiple composter-fermenters for larger batches
- Fermentation is per-batch, not per-item — ferment entire harvests at once
- Breaking the composter cancels fermentation and loses the batch

---

## Compost

Crafted from:
- 8 Wheat + 1 Composter (shaped recipe)

### Usage

1. Use compost on farmland
2. Soil enrichment levels: 0, 1, 2, 3
3. Higher levels speed up crop growth
4. Enriched soil depletes over multiple harvests

---

## Strain Journal

Crafted from:
- 1 Book + 1 Comparator

The journal tracks discovered strain genetics. When you harvest a crop with a new strain profile, it's automatically added.

### Usage

- Right-click to open and view discovered strains
- Strains show: Name, genetics profile, discovery date
- Share strain information with other players
