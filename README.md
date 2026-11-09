# LetItRoll

An immersive, physical in-world slot machine plugin for **Paper 1.21.10** built with modern `ItemDisplay` entities, vanilla item economies, and zero client resource pack requirements.

---

## Features

- **Physical In-World Machines**: Built directly in the world using a Lodestone base and an attached Lever.
- **Client-Rendered Item Displays**: Displays 3 reels cleanly using Paper's lightweight `ItemDisplay` entities above the machine base.
- **Vanilla Diamond Economy**: Costs 1 Diamond per pull; automatically deducted from inventory on lever interaction.
- **Staggered Reel Spin Loop**: 70-tick (~3.5 seconds) animated spin with sequential reel locking (ticks 26, 46, 66), audio ticking, and lever reset.
- **Win Fanfare & Overflow Safe**: Audio-visual celebrations (particles, titles, sounds) scaled by tier. Payouts drop safely at player feet if inventory is full.
- **Machine Protection & Persistence**: Protects active machines from unauthorized breaking or explosions. Auto-saves and restores machines via `machines.yml`.

---

## Payout Table

Roll 3 matching icons to win:

| Tier | Icon | Material | Reel Weight | Payout | Reward Effect |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Jackpot** | Amethyst Shard | `AMETHYST_SHARD` | 10 | **64 Diamonds** | Totem particles, challenge fanfare, screen title |
| **Big Win** | Resin Clump | `RESIN_CLUMP` | 20 | **16 Diamonds** | Villager happy particles, level-up sound, screen title |
| **Win** | Piglin Head | `PIGLIN_HEAD` | 30 | **4 Diamonds** | Wax-on particles, orb pickup sound, screen title |
| **Consolation**| Pufferfish | `PUFFERFISH` | 40 | **1 Diamond** | Pufferfish inflation sound, action bar notification |

*Any non-matching combination results in a loss with a bass note sound effect.*

---

## Setup & Usage

### Creating a Slot Machine

1. Place a **Lodestone** block on the ground.
2. Attach a **Lever** to one of the horizontal sides of the Lodestone.
3. Run `/letitroll wand` to obtain the **Slot Machine Wand**.
4. Stand facing the side you want as the front of the machine and **right-click the Lodestone** with the wand.
5. The machine will register, facing toward you, and spawn 3 display icons above the Lodestone.

### Playing

1. Ensure you have at least **1 Diamond** in your inventory.
2. Right-click the attached lever.
3. Watch the reels spin and claim your payout!

---

## Commands & Permissions

**Permission**: `letitroll.admin` (Default: OP)  
**Aliases**: `/slot`, `/slots`, `/lir`

| Command | Description |
| :--- | :--- |
| `/letitroll wand` | Gives the slot machine creation wand (Blaze Rod). |
| `/letitroll list` | Lists all active registered machines and their coordinates. |
| `/letitroll remove` | Deletes the slot machine currently targeted by crosshair (within 5 blocks). |
| `/letitroll reload` | Reloads registered machines from `machines.yml`. |
| `/letitroll help` | Displays in-game command help. |

---

## Requirements & Building

- **Target Server**: Paper 1.21.10 (`io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT`)
- **Java**: Java 21+

### Gradle Commands

Use `.\gradlew.bat` on Windows or `./gradlew` on Linux/macOS:

```bash
# Compile source code
./gradlew compileJava

# Run unit tests
./gradlew test

# Build plugin jar (outputs to build/libs/LetItRoll-<version>.jar)
./gradlew build

# Launch local Paper test server with the plugin installed
./gradlew runServer
```
