# Tasks

## 1. Domain Models and Configuration

- [x] 1.1 Implement `SlotIcon` enum with materials (`PUFFERFISH`, `PIGLIN_HEAD`, `RESIN_CLUMP`, `AMETHYST_SHARD`), weights, and diamond payouts; verify with unit tests.
- [x] 1.2 Implement `SlotMachine` model and `MachineState` enum (`IDLE`, `SPINNING`) tracking machine UUID, Lodestone location, Lever location, facing direction, and reel display UUIDs.
- [x] 1.3 Implement `SlotMachineRepository` for YAML/PDC persistence to `machines.yml` and verify load/save lifecycle.

## 2. Display and World Management

- [x] 2.1 Implement `DisplayManager` to calculate vector offsets based on machine facing, spawn/update `ItemDisplay` entities, and remove displays cleanly.
- [x] 2.2 Implement `SlotMachineManager` for in-memory caching, machine registration, and block lookup.

## 3. Game Loop and Animation Engine

- [x] 3.1 Implement `SpinTask` handling tick-scheduled reel cycling, audio effects, staggered reel locking, and physical lever toggle.
- [x] 3.2 Implement outcome evaluation to dispense diamonds (with inventory overflow fallback), trigger titles/particles on win, or play low buzzer on loss.

## 4. Interaction, Commands, and Listeners

- [x] 4.1 Implement `LeverInteractListener` to intercept lever right-clicks on registered machines, verify/deduct 1 Diamond from inventory, and trigger `SpinTask`.
- [x] 4.2 Implement `BlockBreakListener` to safely destroy machines and remove linked `ItemDisplay` entities when a Lodestone is broken.
- [x] 4.3 Implement `WandListener` and `/letitroll` command (`wand`, `reload`, `list`) for admin machine creation and management.

## 5. Build Verification

- [x] 5.1 Run Gradle compile and build to verify zero errors and valid plugin jar generation.
