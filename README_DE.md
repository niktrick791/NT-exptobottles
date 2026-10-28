# NT-exptobottles ⚗️ - Minecraft Plugin
[![Donate with PayPal](https://img.shields.io/badge/Donate-PayPal-00457C.svg?logo=paypal&logoColor=white)](https://paypal.me/niktrick)

[🇬🇧 Switch to English Version](README.md) | [🇩🇪 Deutsche Version](README_DE.md)

Ein hochperformantes und modernes Spigot / Paper / Purpur Plugin (Minecraft 1.8.8 – 1.21.4+), mit dem Spieler ihre Erfahrungspunkte (XP) in Flaschen abfüllen, sicher lagern, handeln und jederzeit verlustfrei wieder aufnehmen oder werfen können.

---

## 🚀 Features

- **Exakte XP-Mathematik**:
  - Nutzt die offiziellen Minecraft XP-Formeln (Levels 0–15, 16–30, 31+).
  - Unterstützt Level-basierte und Punkt-basierte Umrechnung ohne Rundungsverluste.
- **Intuitives Abfüllen**:
  - **Per Befehl**: `/bottle`, `/bottle all`, `/bottle 500`, `/bottle 10l`, `/bottle until 30`.
  - **Per Rechtsklick**: Einfach mit einer leeren Glasflasche sneaken (`Shift + Rechtsklick`)!
- **Zwei Arten der Wiedergewinnung**:
  - **Direktes Trinken**: Rechtsklick mit der Erfahrungsflasche absorbiert die XP sofort (ohne Orb-Verlust).
  - **Werfen**: Flasche wie eine normale Wurfflasche werfen – beim Aufprall spawnt exakt die gespeicherte XP-Menge.
  - Leere Flasche wird beim Trinken zurückerstattet (in der Config konfigurierbar).
- **Schnelles XP-Einsammeln (Fast XP Vacuum für Farmen)**:
  - Löst das bekannte Problem, dass man an Enderman-, Gold- oder Raid-Farmen lange vor XP-Haufen warten muss.
  - Orbs im Radius (z. B. 7 Blöcke) werden sanft herangezogen und bei Annäherung blitzschnell absorbiert.
  - **Mending-Unterstützung**: Repariert beschädigte Rüstung/Werkzeuge mit Reparatur (Mending) wie in Vanilla (2 Haltbarkeit pro 1 XP), Rest geht in die Level-Leiste.
  - Kann per `/bottle vacuum` von jedem Spieler individuell an-/ausgeschaltet werden.
- **Zweisprachige Unterstützung (i18n)**:
  - Konfigurierbar in `config.yml`: `language: de` oder `language: en`.
  - In-Game umschaltbar: `/bottle lang <de|en>`.
- **Universelle Cross-Version-Kompatibilität**:
  - Kompiliert mit Java 8 Bytecode (`major version: 52`). Läuft auf Spigot, Paper und Purpur von 1.8.8 bis 1.21.4+.

---

## 📜 Befehle & Aliase

Hauptbefehl: `/bottle` (Aliase: `/bottlexp`, `/xpbottle`, `/expbottle`)

| Befehl | Berechtigung | Beschreibung |
| :--- | :--- | :--- |
| `/bottle` | `exptobottles.use` | Füllt 1 Level deiner Erfahrung in eine Flasche ab. |
| `/bottle all` | `exptobottles.use` | Füllt deine gesamten Erfahrungspunkte ab. |
| `/bottle <xp>` | `exptobottles.use` | Füllt eine bestimmte Punktzahl ab (z. B. `/bottle 500`). |
| `/bottle <level>l` | `exptobottles.use` | Füllt eine bestimmte Anzahl an Leveln ab (z. B. `/bottle 5l`). |
| `/bottle until <level>` | `exptobottles.use` | Füllt alle XP ab, bis du das Ziel-Level erreichst. |
| `/bottle vacuum` | `exptobottles.use` | Schaltet das schnelle XP-Einsammeln für dich ein oder aus. |
| `/bottle lang <de\|en>` | `exptobottles.admin` | Schaltet die Sprache des Plugins live im Spiel um. |
| `/bottle spawn <xp>` | `exptobottles.admin` | Spawnt Erfahrungspunkte am Standort (*Admin*). |
| `/bottle reload` | `exptobottles.admin` | Lädt die `config.yml` neu (*Admin*). |

---

## ⚙️ Konfiguration (`config.yml`)

```yaml
# Sprache: "de" (Deutsch) oder "en" (Englisch)
language: de

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

## 📦 Kompilierung

```bash
.\gradlew.bat jar
```
Die fertige JAR befindet sich im Projektordner:
- `build/libs/ExpToBottles-1.0.0.jar`
---

## 💖 Unterstützung & Spenden

Wenn dir dieses Plugin gefällt, freuen wir uns über deine freiwillige Unterstützung:
- [PayPal Spende](https://paypal.me/niktrick)
- Alle Spenden sind freiwillig und fließen direkt in die Wartung und Weiterentwicklung!
