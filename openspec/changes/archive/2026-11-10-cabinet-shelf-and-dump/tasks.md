# Tasks

## 1. In-Game Structure Dump Command

- [x] 1.1 Create `StructureDumper` utility to scan a block volume around a target location, serialize relative offsets, material names, and block states to JSON, and render ASCII cross-sections; verify via unit tests.
- [x] 1.2 Implement `/letitroll dump [radius]` subcommand and tab completion in `LetItRollCommand` writing to `plugins/LetItRoll/captures/build-dump.json` and logging ASCII to console; verify via compilation and command handling test.

## 2. Slot Machine Cabinet Model and Storage

- [x] 2.1 Extend `SlotMachine` model with optional `shelfLocation` and `canopyLocation` fields and update constructor and accessors; verify via updated unit tests in `SlotMachineTest`.
- [x] 2.2 Update `SlotMachineRepository` to serialize and deserialize `shelf` and `canopy` locations in `machines.yml` preserving backward compatibility with legacy entries; verify via repository unit tests.
- [x] 2.3 Update `SlotMachineManager` to index optional shelf and canopy locations in a fast lookup map and clear them upon machine removal; verify via unit tests.

## 3. Cabinet Construction, Display Alignment, and Protection

- [x] 3.1 Calibrate `DisplayManager.calculateSlotLocations` so ItemDisplay entities sit aligned within the `SPRUCE_SHELF` block at Y+1; verify slot location mathematical tests.
- [x] 3.2 Update `WandUtil` and `LetItRollCommand` to support `/letitroll wand [cabinet|base]` with persistent `WandType` metadata and tab completion; verify via compilation.
- [x] 3.3 Update `WandListener` to branch on `WandType` (Cabinet vs Base) and orient automatic wall lever placement to the player's right side; verify via compilation.
- [x] 3.4 Update `SlotMachineManager.removeMachine` to clear registered shelf, canopy, and lever blocks from the world upon machine deregistration; verify via compilation.
- [x] 3.5 Enhance `/letitroll reload` in `LetItRollCommand` and `SlotMachineManager` to reload config, refresh displays, and reload repository machines; verify via compilation.
- [x] 3.6 Update `MachineProtectionListener` to intercept and cancel break events and explosion damage on registered `SPRUCE_SHELF` and `SPRUCE_TRAPDOOR` canopy blocks while the machine is active; verify via compilation.

## 4. Verification and Quality Checks

- [x] 4.1 Run `.\gradlew.bat test` and `.\gradlew.bat check` to ensure all tests pass and code compiles cleanly.
