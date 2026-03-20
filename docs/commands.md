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

Get a physical book containing your strain journal.

```
/cv journal
```

Useful for sharing strain discoveries with other players.

---

### `/cv soil`

Check soil enrichment status.

```
/cv soil get
```

Shows the enrichment level of the farmland you're standing on.

---

## Admin Commands

Requires `cultivar.admin` permission.

---

### `/cv give`

Give items to players.

```
/cv give <player> <item> [amount]
```

Items:
- `cannabis-seeds`
- `tobacco-seeds`
- `tea-seeds`
- `cannabis-bud`
- `tobacco-leaf`
- `tea-leaf`
- `pipe-wood`
- `pipe-clay`
- `pipe-meerschaum`
- `teapot`
- `drying-rack`
- `strain-journal`
- `compost`

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

Use after editing `config.yml` to apply changes without restarting.

---

### `/cv force`

Force crop properties (for debugging/admin).

```
/cv force <property> <value>
```

Properties:
- `stage <0-6>` - Set growth stage
- `stress <0-100>` - Set stress level
- `watered` - Mark as watered
- `dry` - Mark as dry
- `flag <flag-name>` - Set specific flag

**Use with caution.** This bypasses normal crop mechanics.

---

### `/cv clear`

Clear crops in an area.

```
/cv clear [radius]
```

- No radius: Clear all crops on the server
- With radius: Clear crops within X blocks of your position

Requires confirmation.

---

### `/cv soil`

Manage soil enrichment (admin override).

```
/cv soil set <0-3>
/cv soil clear
```

- `set` - Set enrichment level for current farmland
- `clear` - Reset to level 0
