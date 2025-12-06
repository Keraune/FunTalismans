# FunTalismans

**FunTalismans** is a lightweight and fully customizable plugin that allows server owners to create unique talismans with effects, attributes, stats, and item properties.  
All customization is done through clean and flexible **HOCON** configuration files.

# Features

**Highly Customizable Talismans**  
Create talismans with effects, attributes, stats, NBT tags, custom names, glow, and more.

**Perfect for:**  
• RPG servers  
• Survival progression  
• Custom equipment systems  
• Magic/Fantasy systems  
• Servers with unique combat and stat mechanics  

**Smart Reload System**  
FunTalismans includes a built-in reload system that applies configuration changes instantly.  
When running the reload command, the plugin automatically:

• Rebuilds all talismans from the updated configuration  
• Updates talismans already in players' inventories  
• Applies new effects, attributes, names, textures, NBT, and statistics in real time  
• Requires no server restart

# Commands

**/talisman reload**  
Reloads all plugin configurations and updates all existing talismans.  
Permission: `funtalismans.reload`

**/talisman give <player> <talisman-id>**  
Gives a specific talisman to a player.  
Permission: `funtalismans.give`

Example:  

# Requirements

• NBTAPI

# Example Talismans (HOCON)

```hocon
talismans {
  talismanCastigador {
    name: "&l&#990000&k111 <b><gradient:#990000:#9700A0:#5700A0>Punishment Talisman</gradient></b> &l&#5700A0&k111"
    material: "totem_of_undying"
    unbreakable: false
    glow: true

    effects {
      slot: [ "offhand", "mainhand" ]
      list: [
        {
          type: "STRENGTH"
          amplifier: 2
        },
        {
          type: SPEED
          amplifier: 2
        }
      ]
    }

    attributes: [
      {
        type: "attack_damage"
        amount: 7.0
        operation: "add_number"
        slot: [ "offhand" ]
      },
      {
        type: "max_health"
        amount: -4
        operation: "add_number"
        slot: "offhand"
      },
      {
        type: "movement_speed"
        amount: 0.10
        operation: "add_scalar"
        slot: "offhand"
      }
    ]

    nbt: {
      id: "talismanCastigador"
      rarity: "Legendary"
    }
  }
}
