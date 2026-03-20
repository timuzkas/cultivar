---
title: Getting Started
layout: default
---

## Installation

1. Download the latest `cultivar.jar` from the releases page
2. Place the JAR in your server's `plugins` folder
3. Restart the server
4. The plugin generates `plugins/Cultivar/config.yml` with default settings

## Server Requirements

Use Paper or Spigot 1.20.1 for best compatibility. Other server software may work but is untested.

## Configuration

Edit `plugins/Cultivar/config.yml` to customize:

- Growth timing per crop stage
- Water expiry windows
- Stress thresholds
- Reputation scores and rank thresholds
- Smoking effects and cooldowns
- World blacklist
- Recipe toggles

Run `/cv reload` after making changes.

## Permissions

Grant the `cultivar.use` permission to players who should have basic access. Admin commands require `cultivar.admin`.

## First Steps

1. Obtain seeds (see [Crops](/crops) for acquisition methods)
2. Find suitable farmland
3. Plant seeds on farmland
4. Keep crops watered
5. Tend to crop-specific needs as they grow
6. Harvest and process your yield

## Recommended Settings

If you want faster gameplay, adjust in `config.yml`:

```yaml
growth:
  cannabis:
    stage-1-minutes: 5
    stage-2-minutes: 5
    stage-3-minutes: 5
```

Default times are longer for a more realistic pacing.
