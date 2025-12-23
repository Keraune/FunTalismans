# ✨ FunTalismans ✨  
### 🔮 Advanced Custom Talismans Plugin for Minecraft

📥 **Spanish `talismans.conf` configuration download**  
👉 https://www.mediafire.com/file/8o6x85b39ucz6ep/talismans.conf/file  

---

## 📖 Overview

**FunTalismans** is a **lightweight, powerful, and highly customizable** Minecraft plugin that allows server owners to create **unique talismans** with special effects, attributes, stats, and advanced item properties.

All customization is handled through clean, readable, and flexible **HOCON configuration files**, making it easy to design complex systems without touching code.

Perfect for servers that want **deep progression**, **custom equipment**, or **RPG-style mechanics**.

---

## 🚀 Features

### 🧿 Highly Customizable Talismans
Create talismans with full control over:

- ✨ Potion effects  
- ⚔️ Custom attributes  
- 📊 Advanced statistics  
- 🧬 NBT tags  
- 🏷️ Custom names (colors, gradients, styles)  
- 🌟 Visual glow  
- 🛡️ Item properties (unbreakable, slots, etc.)  

---

### 🎯 Ideal For

✔ RPG servers  
✔ Survival progression systems  
✔ Custom equipment setups  
✔ Magic / Fantasy gameplay  
✔ Servers with unique combat & stat mechanics  

---

## ♻️ Smart Reload System

FunTalismans includes a **real-time reload system** that applies configuration changes instantly.

When running the reload command, the plugin automatically:

- 🔄 Rebuilds all talismans from the updated configuration  
- 🎒 Updates talismans already present in player inventories  
- ⚡ Applies new effects, attributes, names, textures, NBT, and stats live  
- 🚫 Requires **no server restart**

This allows fast iteration and balancing without interrupting gameplay.

---

## 🧾 Commands

### 🔄 `/talisman reload`
Reloads all plugin configurations and updates every existing talisman.

**Permission:**  
```
funtalismans.reload
```

---

### 🎁 `/talisman give <player> <talisman-id> <amount>`
Gives a specific talisman to a player.

**Permission:**  
```
funtalismans.give
```

---

## 📦 Requirements

- ✅ **NBTAPI**

---

## 🧪 Example Talisman Configuration (HOCON)

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
```

---

## 🛠️ Notes

- Designed for performance and scalability  
- Fully compatible with custom systems  
- Easy to maintain and extend  
- Ideal foundation for advanced RPG plugins  

---

**FunTalismans** gives you full control over how talismans behave, look, and evolve —  
all without restarting your server.

---
