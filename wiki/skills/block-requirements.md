---
description: Guide to block requirements
---

# Block requirements

Block requirements are customizable restrictions on breaking, placing, or harvesting blocks. For requirements on
using items, see [Item Requirements](item-requirements.md).

## Adding block requirements

Block requirements are defined under the `requirement.blocks.list` in `config.yml`. For example, adding a requirement of Mining level 5 to break iron ore:

```yaml
requirement:
  block:
    list:
    - material: iron_ore
      allow_break: false
      allow_place: true
      requirements:
      - type: skill_level
        skill: mining
        level: 5
```

Since the default config has an empty list defined as `list: []`, you must
remove these brackets when adding requirements.

### Requirement section keys

Each element in `list` represents a group of one or more requirements nodes for a single block type.
The following keys can be defined:
* `material` - The name of the block to add requirements for (required)
* `allow_break` - Whether to ignore requirements on block break (defaults to false)
* `allow_place` - Whether to ignore requirements on block place (defaults to false)
* `allow_harvest` - Whether to ignore requirements on block harvest. This is for crops that drop items but are not broken. (defaults to false)

When none of the above allow options are defined, the block cannot be broken, placed, or harvested when requirements are not met.

* `requirements` - A map list of the requirement nodes for this block. All the requirements nodes must be met to be able to break/place
  the block defined by `material`. The keys for each node are listed below.

## Requirement types

The following keys are defined in each mapping of the `requirements` list of a requirement section.

* `type` - The type of requirement, which can be `skill_level`, `permission`, `world`, `excluded_world`, `stat`, `biome`, `region`, `item`, or `enchantment`.
* `message` - The error message to send to the player when the requirement is not met. Supports MiniMessage and PlaceholderAPI (optional).

Each type has specific keys below that must be added to define type behavior. These keys are added in the same indent level as `type`.

### Skill level

The `skill_level` type requires the player to be at least a specific level in a skill.

Keys:
  * `skill` - The name of the skill to add a level requirement for
  * `level` - The minimum skill level the player must be

Example:

```yaml
- type: skill_level
  skill: farming
  level: 10
```

### Permission

The `permission` type requires the player to have a specific permission node.

Keys:
  * `permission` - The permission node required

Example:

```yaml
- type: permission
  permission: some.permission.node
```

### World

The `world` type requires the player to be in a specific world.

Keys:
  * `world` - The name of the world the player is required to be in

Example:

```yaml
- type: world
  world: world_nether
```

### Excluded world

The `excluded_world` type defines a list of worlds that will make the requirement fail if the player is in one of them.

Keys:
  * `worlds` - The list of worlds to not allow the player to be in

Example:

```yaml
- type: excluded_world
  worlds:
    - world_the_end
```

### Stat

The `stat` type requires the player to be at least a specific stat level.

Keys:
  * `stat` - The name of the stat to add a level requirement for
  * `value` - The minimum stat value that the player must be

Example:

```yaml
- type: stat
  stat: toughness
  value: 15
```

### Biome

The `biome` type requires the player to be in the specified biome

Keys:
  * `biome` - The name of the biome the player is required to be in

Example:

```yaml
- type: biome
  biome: swamp
```

### Region

The `region` type requires the player to be in the specified WorldGuard region.

Keys:
  * `region` - The name of the WorldGuard region the player is required to be in

Example:

```yaml
- type: region
  region: spawn
```

### Item

The `item` type requires the player to hold a specific item.

Keys:
  * `item` - The material name of the item the player is required to hold

Examples:

```yaml
- type: item
  item: diamond_pickaxe
```

### Enchantment

The `enchantment` type requires the player to have a specific enchantment on their held item.

Keys:
  * `enchantment` - The name of the enchantment to require
  * `level` - THe required enchantment level, either a singular value or a min-max (optional)
    * Examples of valid levels are `1` and `1-3`

Examples:

```yaml
- type: enchantment
  enchantment: sharpness
  level: 4-5
```

## General options

The `requirement.blocks` section in `config.yml` contains general options related to the block requirement system:
* `enabled` - Whether block requirements are checked at all
* `bypass_in_creative_mode` - Whether to ignore block requirements for players in creative mode (defaults to true)
* `bypass_if_op` - Whether to ignore block requirements for players that are op (defaults to false)

## Example

Example of multiple requirement sections using many requirement node types:

```yaml
requirement:
  block:
    list:
    - material: iron_ore
      allow_break: false
      allow_place: true
      requirements:
      - type: skill_level
        skill: mining
        level: 5
      - type: permission
        permission: some.permission.node
    - material: sweet_berry_bush
      allow_place: true
      allow_harvest: true
      requirements:
      - type: excluded_world
        worlds:
        - world_nether
      - type: stat
        stat: regeneration
        value: 100
```
