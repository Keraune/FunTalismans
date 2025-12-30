# FunTalismans

**Advanced Custom Talismans Plugin for Minecraft**

FunTalismans is a **lightweight, powerful, and highly customizable** Minecraft plugin that allows server owners to create **unique talismans** with special effects, attributes, stats, and advanced item properties.

All customization is handled through clean, readable, and flexible **HOCON configuration files**, making it easy to design complex systems without touching code.

This plugin is ideal for servers that want **deep progression**, **custom equipment**, or **RPG-style mechanics**.

Spanish `talismans.conf` configuration example:  
https://www.mediafire.com/file/8o6x85b39ucz6ep/talismans.conf/file

---

## Overview

FunTalismans gives you full control over how talismans behave, look, and interact with players.  
Each talisman can apply effects, modify attributes, and carry custom metadata while remaining fully configurable and reloadable at runtime.

No server restart is required when making changes.

---

## Features

**Highly customizable talismans**, including support for:

- Potion effects  
- Custom attributes  
- Advanced statistics  
- NBT tags  
- Custom names (legacy colors, hex, gradients)  
- Visual glow  
- Item properties such as unbreakable items and equipment slots  

---

## Ideal use cases

FunTalismans is perfect for:

- RPG servers  
- Survival progression systems  
- Custom equipment setups  
- Magic or fantasy gameplay  
- Servers with unique combat and stat mechanics  

---

## Smart reload system

FunTalismans includes a **real-time reload system** that applies configuration changes instantly.

When using the reload command, the plugin will:

- Rebuild all talismans from the updated configuration  
- Update talismans already present in player inventories  
- Apply new effects, attributes, names, textures, NBT, and stats live  
- Work without requiring a server restart  

This allows fast iteration and balancing without interrupting gameplay.

---

## Commands

**`/talisman reload`**  
Reloads all plugin configurations and updates every existing talisman.

Permission:

```
funtalismans.reload
```

---


**`/talisman give <player> <talisman-id> <amount>`**  
Gives a specific talisman to a player.

Permission:

```
funtalismans.give
```

---

## Requirements

- NBTAPI

---

## Example Talisman Configuration (HOCON)

```hocon
###########################################################################################
#                                                                                         #
#                    TALISMANS CONFIGURATION GUIDE                                        #
#                                                                                         #
#  ITEM STRUCTURE:                                                                        #
#  - name: Supports Legacy codes (&), Hexadecimal (&#), and Gradients.                    #
#  - material: Minecraft Item ID (e.g., "totem_of_undying", "player_head").               #
#  - texture: Only for "player_head". Supports 3 formats:                                 #
#      1. MINECRAFT URL: "http://textures.minecraft.net/texture/..."                      #
#      2. Base64 with prefix: "base64:eyJ0ZXh0dXJlcyI6..."                                #
#      3. Raw Value (Base64 only): "eyJ0ZXh0dXJlcyI6..."                                  #
#  - unbreakable: 'true' to prevent the item from ever breaking.                          #
#  - glow: 'true' to add the enchantment glint effect.                                    #
#                                                                                         #
#  ENCHANTMENTS (enchantments):                                                           #
#  - List of visual or functional enchantments. E.g., SHARPNESS: 5                        #
#                                                                                         #
#  VALID SLOTS (for effects & attributes):                                                #
#  The item must be equipped in one of these slots to trigger its power:                  #
#    - "mainhand" : Main hand weapon slot.                                                #
#    - "offhand"  : Off-hand / Shield slot.                                               #
#    - "head"     : Helmet slot.                                                          #
#    - "chest"    : Chestplate slot.                                                      #
#    - "legs"     : Leggings slot.                                                        #
#    - "feet"     : Boots slot.                                                           #
#                                                                                         #
#  EFFECTS (effects):                                                                     #
#  - slot: Use one of the valid slots listed above.                                       #
#  - list: List of constant potion effects.                                               #
#    - type: Effect name (SPEED, INCREASE_DAMAGE, REGENERATION, etc).                     #
#    - amplifier: Level of the effect (0 is level I, 1 is level II).                      #
#                                                                                         #
#  ATTRIBUTES (attributes):                                                                #
#  - slot: Use one of the valid slots listed above.                                       #
#  - type: Statistic to modify (max_health, attack_damage, movement_speed, etc).          #
#  - operation:                                                                           #
#    - "add_number": Adds the value directly (e.g., 2.0 = 1 full heart).                  #
#    - "add_scalar": Adds a percentage (e.g., 0.10 = +10% of the base stat).              #
#                                                                                         #
###########################################################################################

talismans {
  // Template for creating new items:
  // TemplateID {
  //   name: "<gradient:#FF5555:#FFCC00>Epic Talisman</gradient>"
  //   material: "player_head"
  //   texture: "base64:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM0YzI3MTZhNTY1NTdjYzc5MzMzYWI1MDM5MWNhN2EwNGJiMjczODkxOThiNTdmNDFiMjQ0YjAwMWRmZjA1MCJ9fX0="
  //   unbreakable: true
  //   glow: false
  //   enchantments {
  //     UNBREAKING: 1
  //   }
  //   effects {
  //     slot: "offhand"
  //     list: [
  //       { type: "SPEED", amplifier: 1 }
  //     ]
  //   }
  //   lore: [
  //     "&7Custom head using Base64"
  //   ]
  //   attributes: [
  //     { type: "max_health", amount: 2.0, operation: "add_number", slot: "offhand" }
  //   ]
  //   nbt: { id: "TemplateID", rarity: "legendary" }
  // }

  // ===================
  // Talisman Sections
  // ===================

  GraniteTalisman {
    name: "&x&f&b&0&0&0&0[★]&x&f&f&6&3&0&0 ɢʀᴀɴɪᴛᴇ ᴛᴀʟɪꜱᴍᴀɴ"
    material: "totem_of_undying"
    unbreakable: false
    glow: true
    lore: [
      "&7ᴛʜᴇ ɢʀᴀɴɪᴛᴇ ᴛᴀʟɪꜱᴍᴀɴ - ɪꜱ",
      "&7ᴛʜᴇ ɪɴꜰɪɴɪᴛʏ ᴏꜰ ꜱᴛʀᴇɴɢᴛʜ",
      "&7ᴀɴᴅ ᴛʜᴇ ꜱᴘɪʀɪᴛ ᴏꜰ ꜰʀᴇᴇᴅᴏᴍ"
    ]
    attributes: [
      {
        type: "max_health"
        amount: -4
        operation: "add_number"
        slot: "offhand"
      },
      {
        type: "movement_speed"
        amount: 0.15
        operation: "add_scalar"
        slot: "offhand"
      },
      {
        type: "attack_damage"
        amount: 3.0
        operation: "add_number"
        slot: "offhand"
      },
    ]
    nbt: {
      id: "GraniteTalisman"
      rarity: "epic"
    }
  }
  // ===================
  // Sphere Sections
  // ===================

  AndromedaSphere {
    name: "&x&f&b&0&0&0&0[★] <#ff6300>ᴀɴᴅʀᴏᴍᴇᴅᴀ ꜱᴘʜᴇʀᴇ"
    material: "player_head"
    texture: "http://textures.minecraft.net/texture/44ffe3f358f209bad8fff4dc48245d9baf0a031b3c1ee6b758460a339b1519e2"
    unbreakable: false
    glow: false
    lore: [
      "&7ᴛʜᴇ ꜱᴘʜᴇʀᴇ ɢᴜᴀʀᴅꜱ ᴛʜᴇ ɢᴀᴢᴇ",
      "&7ᴏꜰ ᴀɴᴅʀᴏᴍᴇᴅᴀ ɢᴜɪᴅɪɴɢ ᴛʜʀᴏᴜɢʜ",
      "&7ᴛʜᴇ ᴅᴀʀᴋɴᴇꜱᴇ ᴀɴᴅ ᴛʜᴇ ꜱᴛᴀʀꜱ"
    ]
    attributes: [
      {
        type: "max_health"
        amount: -4
        operation: "add_number"
        slot: "offhand"
      },
      {
        type: "movement_speed"
        amount: 0.15
        operation: "add_scalar"
        slot: "offhand"
      },
      {
        type: "attack_damage"
        amount: 3.0
        operation: "add_number"
        slot: "offhand"
      },
      {
        type: "armor"
        amount: 2.0
        operation: "add_number"
        slot: "offhand"
      },
    ]
    nbt: {
      id: "AndromedaSphere"
      rarity: "legendary"
    }
  }
}
```

---

## Notes

- Designed for performance and scalability  
- Fully compatible with custom systems  
- Easy to maintain and extend  
- Ideal foundation for advanced RPG plugins  

---

**FunTalismans** gives you full control over how talismans behave, look, and evolve —  
all without restarting your server.

---
