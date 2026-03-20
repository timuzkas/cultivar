---
title: Commands
layout: default
---

## Player Commands

### `/cv` - Cultivar

View your profile, reputation, and active crops.

```
/cv
```

Shows:
- Current rank
- Total reputation points
- Number of active crops
- Nearby crop status

---

### `/cv inspect`

Inspect the crop you're looking at.

```
/cv inspect
```

Shows:
- Crop type and current stage
- Time to next stage
- Water status
- Stress level
- Pending care actions

---

### `/cv strains`

Manage your strain journal.

```
/cv strains list
/cv strains info <strain-name>
```

- `list` - Show all discovered strains
- `info` - Detailed info on a specific strain

---

### `/cv journal`

Get your personal strain journal book.

```
/cv journal
```

---

### `/cv soil`

Check soil enrichment status.

```
/cv soil get
```

Shows the enrichment level of the farmland you're standing on (0-3).

---

## Admin Commands

Requires `cultivar.admin` permission.

---

### `/cv give`

Give items to players.

```
/cv give <player> <item> [amount]
```

Valid items:
- `cannabis_seed`
- `tobacco_seed`
- `tea_seed`
- `mushroom_seed`
- `spore` (Mushroom spores)
- `cannabis_bud`
- `tea_leaf` (Fresh)
- `dried_tea_leaf`
- `dried_mushroom`
- `light_cured_leaf`
- `dark_cured_leaf`
- `fire_cured_leaf`
- `compost`
- `pipe` (Wooden)
- `clay_pipe`
- `meerschaum_pipe`
- `teapot`
- `basket` (Harvest Basket)
- `tea_bottle:green`
- `tea_bottle:black`
- `tea_bottle:herbal`

---

### `/cv remove`

Remove the crop you're looking at.

```
/cv remove
```

Permanently deletes the crop and its data.

---

### `/cv reload`

Reload configuration from disk.

```
/cv reload
```

---

### `/cv force`

Force crop properties (for debugging/admin).

```
/cv force <property> <value>
```

Properties:
- `stage <0-5>` - Set growth stage
- `stress <value>` - Set stress level
- `flag <flag-name>` - Set specific flag
- `advance` - Skip current stage
- `strain <strain-id>` - Set strain
- `cure <light|dark|fire|generic>` - Set tobacco cure type (held item)
- `steep <weak|perfect|bitter>` - Set tea quality (held item)
- `mushroom <0-5>` - Mushroom-specific stage set

---

### `/cv clear`

Clear crops in an area.

```
/cv clear [radius]
```

- No radius: Clear all crops on the server
- With radius: Clear crops within X blocks of your position

---

### `/cv soil`

Manage soil enrichment (admin override).

```
/cv soil set <0-3>
/cv soil clear
```

- `set` - Set enrichment level for current farmland
- `clear` - Reset to level 0
