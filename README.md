# ExpToBottles ⚗️ - Purpur MC 1.26.2 Plugin

Ein performantes und modernes Plugin für **Purpur / Paper Minecraft 1.26.2**, mit dem Spieler ihre Erfahrungspunkte (XP) in Flaschen abfüllen, sicher lagern, handeln und jederzeit verlustfrei wieder aufnehmen oder werfen können.

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
  - Leere Flasche wird beim Trinken zurückerstattet (in der Config ein-/ausschaltbar).
- **Modernes Adventure & MiniMessage Design**:
  - Wunderschöne Farbverläufe und Tooltips auf den Items.
  - NBT/PersistentDataContainer (PDC) Speicherung mit `NamespacedKey` – kompatibel mit Server-Neustarts.
- **Schnelles XP-Einsammeln (Fast XP Vacuum für Farmen)**:
  - Löst das bekannte Problem, dass man an Enderman-, Gold- oder Raid-Farmen minutenlang vor XP-Haufen herumsteht.
  - Orbs im Radius (z. B. 5 Blöcke) werden sanft herangezogen und bei Annäherung blitzschnell (10x pro Sekunde) in Batches absorbiert.
  - **Mending-Unterstützung**: Repariert beschädigte Rüstung/Werkzeuge mit Reparatur (Mending) wie in Vanilla (2 Haltbarkeit pro 1 XP), Rest geht in die Level-Leiste.
  - Kann per `/bottle vacuum` von jedem Spieler individuell an-/ausgeschaltet werden.
- **XP-Generierung / Spawn-Befehl**:
  - `/bottle spawn <menge>` spawnt XP-Orbs direkt vor dem Spieler (ideal zum Testen und für Server-Events).
- **Vollständig konfigurierbar**:
  - Alle Radien, Batch-Größen, Mending, Steuer-/Verlustgebühr, Sounds und Partikel in `config.yml` anpassbar.

---

## 📜 Befehle & Aliase

Hauptbefehl: `/bottle` (Aliase: `/bottlexp`, `/xpbottle`, `/expbottle`)

| Befehl | Beschreibung |
| :--- | :--- |
| `/bottle` | Füllt 1 Level deiner Erfahrung in eine Flasche ab |
| `/bottle all` | Füllt deine gesamten Erfahrungspunkte ab |
| `/bottle <xp>` | Füllt eine bestimmte Punktzahl ab (z. B. `/bottle 500`) |
| `/bottle <level>l` | Füllt eine bestimmte Anzahl an Leveln ab (z. B. `/bottle 5l`) |
| `/bottle until <level>` | Füllt alle XP ab, bis du das Ziel-Level erreichst (z. B. `/bottle until 30`) |
| `/bottle vacuum` | Schaltet das schnelle XP-Einsammeln für dich ein oder aus |
| `/bottle spawn <xp>` | Spawnt Erfahrungspunkte am Standort (*Admin*) |
| `/bottle reload` | Lädt die `config.yml` neu (*Admin*) |

---

## 🔑 Berechtigungen (Permissions)

| Permission | Standard | Beschreibung |
| :--- | :--- | :--- |
| `exptobottles.use` | Jeder (`true`) | Erlaubt das Abfüllen und Einlösen von Flaschen |
| `exptobottles.admin` | Operatoren (`op`) | Erlaubt `/bottle reload` |

---

## 📦 Installation

1. Kopiere die generierte Datei `build/libs/ExpToBottles-1.0.0.jar` in den `plugins/`-Ordner deines Purpur 1.26.2 Servers.
2. Starte den Server neu oder führe `/reload confirm` aus.
3. Die Konfigurationsdatei wird automatisch unter `plugins/ExpToBottles/config.yml` angelegt.

---

## ⚙️ Bauen aus dem Quellcode (Gradle)

```powershell
$env:JAVA_HOME = "e:\Antigravity Projects\MC\tools\jdk-25.0.4.1+1"
.\gradlew.bat test build
```
Die fertige JAR-Datei befindet sich in:
`build/libs/ExpToBottles-1.0.0.jar`
