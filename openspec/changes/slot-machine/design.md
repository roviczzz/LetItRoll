# Design

## Context
Paper 1.21.10 plugin targeting Java 21. See `proposal.md` for user motivation and `specs/slot-machine/spec.md` for behavioral requirements.

## Goals / Non-Goals

**Goals:**
- Provide a responsive in-world physical slot machine using vanilla blocks and display entities.
- Direct lever interaction: pulling the lever immediately checks and consumes 1 Diamond from inventory and spins.
- Persist machine registrations across server reboots via `machines.yml` and `PersistentDataContainer` (PDC).
- Clean state machine lifecycle (`IDLE`, `SPINNING`, `EVALUATING`, `RESETTING`).
- Safe cleanup of `ItemDisplay` entities when machines are dismantled.

**Non-Goals:**
- Vault / virtual currency hooks (strictly uses vanilla Diamonds).
- Complex custom textures or client resource packs (uses vanilla icons: Pufferfish, Piglin Head, Resin Clump, Amethyst Shard).

## Decisions

### 1. Display Architecture: `ItemDisplay` Entities
- **Rationale**: In modern Paper (1.21.10), `ItemDisplay` entities are client-rendered, possess no hitboxes or physics tick overhead, and allow precise floating transformations (`ItemDisplayTransform.FIXED`, scale `0.5`, translation offsets).
- **Alternative Considered**: `ItemFrame` (dropped because players can knock items out or rotate them, and alignment with trapdoor shelves is clunky).

### 2. Relative Directional Coordinate Mapping
- When an admin registers a Lodestone with the wand, the plugin scans the 4 adjacent horizontal blocks for a `Lever` attached to the Lodestone.
- From the lever's relative position (e.g., on the East side), the machine facing is deduced as `NORTH`.
- The shelf display center is calculated as:
  `shelfCenter = lodestoneLocation.clone().add(0, 1.2, 0).add(backwardVector.multiply(0.35))`
- Displays 0, 1, 2 are spaced laterally along the perpendicular axis by `-0.35`, `0.0`, and `+0.35` blocks.

### 3. Lever Interaction & Inventory Deduction
- Listen for `PlayerInteractEvent` with `Action.RIGHT_CLICK_BLOCK` on the machine's lever.
- Cancel the vanilla lever redstone toggle if player has 0 Diamonds or if the machine is currently `SPINNING`.
- When valid:
  - Deduct 1 Diamond: `player.getInventory().removeItem(new ItemStack(Material.DIAMOND, 1))`
  - Manually flip the lever block data (`Switch.setPowered(true)`) and play lever click sound.
  - Transition state to `SPINNING`.

### 4. Reel Spin Animation & Timing Loop
- Run a 2-tick interval task (`BukkitRunnable`):
  - Total spin time: 70 ticks (~3.5 seconds).
  - Ticks 0 to 25: All 3 reels cycle through random icons every 2 ticks with `BLOCK_NOTE_BLOCK_HAT` clicks.
  - Tick 25: Reel 0 locks its final outcome + heavy click sound (`BLOCK_IRON_TRAPDOOR_CLOSE`).
  - Ticks 25 to 45: Reels 1 and 2 continue cycling.
  - Tick 45: Reel 1 locks its final outcome.
  - Ticks 45 to 65: Reel 2 continues cycling.
  - Tick 65: Reel 2 locks its final outcome.
  - Tick 70: Evaluation phase, reward distribution, reset lever (`Switch.setPowered(false)`), and return to `IDLE`.

### 5. Outcome Probability Table
- Reel strips are weighted:
  - `PUFFERFISH` (Worst): Weight 40 (Consolation: 1 Diamond)
  - `PIGLIN_HEAD` (Bad): Weight 30 (Win: 4 Diamonds)
  - `RESIN_CLUMP` (Good): Weight 20 (Win: 16 Diamonds)
  - `AMETHYST_SHARD` (Best): Weight 10 (Jackpot: 64 Diamonds)
- A win requires all 3 reels to match the same icon. Any mismatch results in a loss.

## Risks / Trade-offs

- **[Server Restart Mid-Spin]** → On shutdown/disable, active spins are aborted, lever state is unpowered, and displays are reset to default icons.
- **[Orphaned Entities]** → Each `ItemDisplay` has `letitroll:machine_id` in its PDC. On chunk load or machine cleanup, orphan checks clean up unregistered displays.
- **[Inventory Overflow on Payout]** → Rewards use `player.getInventory().addItem(...)`. Any items that do not fit in the inventory are spawned at the player's feet via `world.dropItem(...)`.
