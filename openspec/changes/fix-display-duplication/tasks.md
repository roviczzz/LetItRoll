# Tasks

## 1. Pure reconciliation planning

- [x] 1.1 Add a plain-data `DisplayPlan` type describing the surviving UUID per slot, the UUIDs to delete, and the slots needing a spawn; verify it compiles and carries no Bukkit types (`.\gradlew.bat compileJava`)
- [x] 1.2 Implement the pure planner that takes the recorded UUID array plus a list of `(uuid, slotIndex)` descriptors and produces a `DisplayPlan`; verify a unit test covers an already-correct machine producing an empty plan
- [x] 1.3 Add unit tests for duplicate slots asserting one survivor per slot and the extras in the delete set, including the case where the recorded UUID is present and the case where it is absent
- [x] 1.4 Add unit tests for missing slots asserting only the empty slot appears in the spawn set, and for descriptors outside slots 0..2 being reported as deletable; verify `.\gradlew.bat test --tests *DisplayPlan*` passes

## 2. Display identity and application in `DisplayManager`

- [x] 2.1 Add helpers to read `machine_id` and `slot_index` from an `ItemDisplay` persistent data container and to test whether an entity carries the machine identity key; verify `.\gradlew.bat compileJava`
- [x] 2.2 Implement the Bukkit-facing apply step that deletes the plan's UUIDs and spawns the plan's slots with the default icon, stamping PDC identity and recording the new UUIDs on the machine; verify `.\gradlew.bat compileJava`
- [x] 2.3 Implement the box-scan lookup around the machine's calculated slot positions, filtered by PDC `machine_id`, returning descriptors for the planner; verify `.\gradlew.bat compileJava`
- [x] 2.4 Replace the blind remove-then-spawn in `spawnDisplays` usage with a single reconcile entry point that returns whether the UUID set changed; verify no call site still spawns unconditionally (`rg "spawnDisplays" src/main`)
- [x] 2.5 Implement identity-based purge for a machine identifier and route `removeDisplays` through it; verify `.\gradlew.bat compileJava`

## 3. Manager lifecycle

- [x] 3.1 Change `loadMachines()` to load into memory only: no `removeDisplays`, no `spawnDisplays`, no `repository.save`; verify by inspection that the method body contains no display or save calls
- [x] 3.2 After loading, reconcile only machines whose chunk is already loaded, without loading any chunk; verify `.\gradlew.bat compileJava`
- [x] 3.3 Add a reconcile entry point on the manager that persists the machine only when the UUID set changed; verify the existing `SlotMachineManagerTest` still passes (`.\gradlew.bat test --tests SlotMachineManagerTest`)
- [x] 3.4 Route `removeMachine` through the identity-based purge; verify `.\gradlew.bat test` passes

## 4. Event and interaction wiring

- [x] 4.1 Add an `EntitiesLoadEvent` listener that returns immediately for entities without the machine identity key, reconciles machines owning the loaded displays, and deletes displays whose `machine_id` is unregistered; verify `.\gradlew.bat compileJava`
- [x] 4.2 Register the new listener in `onEnable`; verify the plugin registers four listeners without error at enable time
- [x] 4.3 Trigger reconcile from `LeverInteractListener` before the spin starts; verify `.\gradlew.bat compileJava`
- [x] 4.4 Confirm `/letitroll reload` reaches the non-destructive path and no longer resets displayed icons; verify `.\gradlew.bat compileJava`

## 5. Integration verification

- [x] 5.1 Run the full suite (`.\gradlew.bat test`) and confirm all tests pass, including the new planner tests
- [x] 5.2 Start the dev server (`.\gradlew.bat runServer`), create a machine, spin it, restart the server, and confirm exactly three displays remain showing the last spin result
- [x] 5.3 On a world with pre-existing duplicates, confirm the duplicates collapse to three when the machine's chunk loads, and that `/letitroll reload` leaves the displayed icons unchanged

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result updates `openspec/specs/slot-machine-display-lifecycle/spec.md` and the `slot-machine-cabinet` reload requirement.
