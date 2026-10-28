# ExpToBottles ⚗️ - Minecraft Plugin

[🇩🇪 Zur deutschen Version wechseln](README_DE.md) | [🇬🇧 English Version](README.md)

A high-performance and lightweight Spigot / Paper / Purpur plugin (Minecraft 1.8.8 – 1.21.4+) that allows players to bottle their experience points (XP), store them safely, trade with others, and instantly consume or throw them without any XP loss.

---

## 🚀 Features

- **Exact Experience Mathematics**:
  - Uses official Minecraft XP formulas across all level brackets (0–15, 16–30, 31+).
  - Accurate point-to-level conversions with zero rounding losses.
- **Intuitive Bottling**:
  - **By Command**: `/bottle`, `/bottle all`, `/bottle 500`, `/bottle 10l`, `/bottle until 30`.
  - **By Sneak-Right-Click**: Simply sneak with an empty glass bottle in hand (`Shift + Right Click`)!
- **Two Ways to Retrieve XP**:
  - **Direct Consumption**: Right-click the bottled XP to drink it instantly (no floating orb loss).
  - **Throwing**: Throw it like a regular splash bottle – exact XP drops on impact.
  - Option to return an empty bottle upon drinking.
- **Fast XP Vacuum (Instant Farm Pickup)**:
  - Eliminates waiting at mob farms (Enderman, gold, raid farms) for slow XP orbs to arrive.
  - Nearby orbs are smoothly drawn toward the player and absorbed at accelerated rates.
  - **Full Mending Support**: Repairs damaged mending items at vanilla rates (2 durability per 1 XP), remainder goes straight into the XP bar.
  - Individual toggle via `/bottle vacuum`.
- **Bilingual & Multi-Language Support**:
  - Configurable language in `config.yml`: `language: en` or `language: de`.
  - In-game language switching: `/bottle lang <de|en>`.
- **Universal Cross-Version Support**:
  - Compiled targeting Java 8 bytecode (`major version: 52`). Compatible with Minecraft 1.8.8 through 1.21.4+.

---

## 📜 Commands & Permissions

Primary command: `/bottle` (Aliases: `/bottlexp`, `/xpbottle`, `/expbottle`)

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/bottle` | `exptobottles.use` | Bottles 1 level of your experience. |
| `/bottle all` | `exptobottles.use` | Bottles all of your current experience. |
| `/bottle <xp>` | `exptobottles.use` | Bottles an exact amount of XP (e.g. `/bottle 500`). |
| `/bottle <level>l` | `exptobottles.use` | Bottles an exact number of levels (e.g. `/bottle 5l`). |
| `/bottle until <level>` | `exptobottles.use` | Bottles experience down to target level (e.g. `/bottle until 30`). |
| `/bottle vacuum` | `exptobottles.use` | Toggles the fast XP vacuum on/off for you. |
| `/bottle lang <de\|en>` | `exptobottles.admin` | Switches plugin language live in-game. |
| `/bottle spawn <xp>` | `exptobottles.admin` | Spawns experience orbs at your location (*Admin*). |
| `/bottle reload` | `exptobottles.admin` | Reloads `config.yml` (*Admin*). |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Language: "en" (English) or "de" (German)
language: en

bottling:
  require-empty-bottle: true
  return-empty-bottle-on-consume: true
  min-xp: 1
  max-xp-per-bottle: -1
  tax-percent: 0.0
  sneak-right-click-to-fill: true
  sneak-fill-mode: "LEVEL"
  direct-consume-on-right-click: true
  allow-throw: true

vacuum:
  enabled: true
  radius: 7.0
  instant-pickup-radius: 1.8
  interval-ticks: 2
  max-orbs-per-batch: 32
  pull-effect: true
  repair-mending: true
```

---

## 📦 Building

```bash
.\gradlew.bat jar
```
The compiled JAR is generated at:
- `build/libs/ExpToBottles-1.0.0.jar`
