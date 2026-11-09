# Design

## Context

The plugin currently manages machines through `SlotMachineManager` mapping Lodestone and Lever locations. In `DisplayManager`, reel ItemDisplays are positioned above the Lodestone. In the game world (verified via `build-dump.json`), the arcade cabinet structure consists of a Lodestone base at Y+0 with an attached wall lever, a `SPRUCE_SHELF` (`minecraft:spruce_shelf`) at Y+1, and a flat `SPRUCE_TRAPDOOR` canopy (`minecraft:spruce_trapdoor[half=bottom,open=false]`) at Y+2, all oriented toward the front facing. Additionally, debugging and inspecting in-game builds from developer chat or log analysis requires a dump command that exports relative block data to disk.

See `proposal.md` for motivation and `specs/` for normative requirements.

## Goals / Non-Goals

**Goals:**
- Provide `/letitroll dump [radius]` to export targeted block volumes (materials, relative offsets, block data states) to `plugins/LetItRoll/captures/build-dump.json` and print ASCII layers to console.
- Calibrate `DisplayManager.calculateSlotLocations` so the three ItemDisplay reel items sit aligned within the `SPRUCE_SHELF` block at Y+1.
- Extend `SlotMachine` model and `SlotMachineRepository` to support optional `shelfLocation` and `canopyLocation` fields.
- Provide two wand variants via `/letitroll wand [cabinet|base]`:
  - `cabinet` (default): builds the full cabinet structure (`SPRUCE_SHELF` at Y+1, flat `SPRUCE_TRAPDOOR` at Y+2, and attached wall `LEVER`).
  - `base`: creates a minimal machine (Lodestone + attached wall `LEVER` only, without shelf or canopy blocks).
- Orient automatic wall lever placement to the player's right-hand side when viewing the front of the machine.
- Provide `/letitroll reload` command to reload plugin config and refresh registered slot machines and displays from disk.
- Extend `MachineProtectionListener` to protect registered shelf (`SPRUCE_SHELF`) and canopy (`SPRUCE_TRAPDOOR`) blocks from player breaking and explosions.
- Clean up constructed cabinet blocks (shelf, canopy, and lever) when a machine is unregistered or destroyed.

**Non-Goals:**
- Requiring administrators to manually place shelf, canopy, or lever blocks prior to machine creation.
- General-purpose schematic copying, pasting, or WorldEdit replacement.
- Modifying reel probabilities, item icons, or payout mechanics.

## Decisions

### Decision 1: Dedicated `StructureDumper` Utility
Create `me.roviczzz.letItRoll.util.StructureDumper` to handle block volume scanning, relative coordinate normalization (relative to the target block), JSON file writing, and console ASCII visualization.
- **Rationale**: Keeps `LetItRollCommand` clean and provides reusable logic for capturing world structures.
- **Alternatives Considered**: Direct inline dumping in command executor (rejected due to clutter and poor testability).

### Decision 2: Display Alignment Calculation
As captured in `build-dump.json`, the shelf at Y+1 is a `SPRUCE_SHELF` block (`minecraft:spruce_shelf[facing=south,powered=false,side_chain=unconnected,waterlogged=false]`).
- ItemDisplays with transformation scale `(0.42, 0.42, 0.42)` and `ItemDisplayTransform.FIXED` are positioned within the shelf block enclosure at `Y + 1.50` (or calibrated center of the shelf opening) with slot offsets along the machine's right vector (-0.33, 0.0, +0.33) and depth inset matching machine facing.
- **Rationale**: Aligns reel icons naturally with the shelf openings rather than assuming a thin trapdoor slab.

### Decision 3: Extended Model and O(1) Protection Lookup
Update `SlotMachine` with:
```java
private final Location shelfLocation; // nullable (SPRUCE_SHELF)
private final Location canopyLocation; // nullable (SPRUCE_TRAPDOOR)
```
Update `SlotMachineManager` with lookup maps:
```java
private final Map<Location, SlotMachine> machinesByCabinetBlock = new ConcurrentHashMap<>();
```
When a machine is registered or loaded:
- If `shelfLocation != null`, map it in `machinesByCabinetBlock`.
- If `canopyLocation != null`, map it in `machinesByCabinetBlock`.
- `MachineProtectionListener` checks `machinesByLodestone`, `machinesByLever`, and `machinesByCabinetBlock` to cancel break events and protect blocks from explosion.

### Decision 4: Backward-Compatible Repository Schema
In `machines.yml`:
```yaml
machines:
  <uuid>:
    lodestone: ...
    lever: ...
    facing: NORTH
    shelf: <optional Location map>
    canopy: <optional Location map>
    displays: ...
```
If `shelf` or `canopy` keys are missing when loading existing configurations, they default to `null`.

### Decision 5: Multi-Variant Wand Construction and Player-Right Lever Mounting
In `WandUtil`:
- Add `WandType` enum (`CABINET`, `BASE`).
- Store `wand_type` as a string in the item's PersistentDataContainer.
- Default `/letitroll wand` to `WandType.CABINET`, support `/letitroll wand cabinet` and `/letitroll wand base`.

In `WandListener`:
- When an administrator right-clicks a Lodestone with a wand:
  1. Determine `playerFacing = player.getFacing()`. The machine faces the player: `machineFacing = playerFacing.getOppositeFace()`.
  2. If the wand is `WandType.CABINET`:
     - Validate that `Y + 1` and `Y + 2` are clear (air or matching shelf/trapdoor). If obstructed, abort with a descriptive error.
     - Place `SPRUCE_SHELF` at `Y + 1` with facing matching `machineFacing`.
     - Place `SPRUCE_TRAPDOOR` at `Y + 2` with `Half.BOTTOM`, `open = false`, facing `machineFacing`.
  3. If the wand is `WandType.BASE`:
     - Do not place or bind shelf or canopy blocks (`shelfLocation = null`, `canopyLocation = null`).
  4. Automatic lever placement:
     - Check if an attached wall lever is already present.
     - If not, mount a wall `LEVER` on the player's right side (`playerFacing.right`, which is `BlockFace.WEST` for South-facing players / North-facing machines). If obstructed, try the player's left side or the machine's rear.
  5. Pass resolved locations to `SlotMachineManager.registerMachine`.
- In `SlotMachineManager.removeMachine`:
  - When removing displays and deleting the machine, clear the registered `shelfLocation`, `canopyLocation`, and `leverLocation` blocks from the world (resetting to air) so no orphaned physical blocks remain.

### Decision 6: Administrative Plugin and Display Reload
In `LetItRollCommand`:
- When an administrator runs `/letitroll reload`:
  1. Reload plugin config using `plugin.reloadConfig()`.
  2. In `SlotMachineManager.loadMachines()`:
     - Clear existing in-world `ItemDisplay` entities for currently loaded machines so orphaned displays do not duplicate.
     - Clear in-memory maps.
     - Load all machines from `machines.yml` repository.
     - Spawn fresh `ItemDisplay` entities for each loaded machine.
  3. Send success confirmation message to the sender.

## Risks / Trade-offs

- **[Risk] High dump radius causing server lag**: An admin specifying a huge radius could scan tens of thousands of blocks.
  - **Mitigation**: Constrain the radius argument between 1 and 5 (maximum bounding volume of 11x11x11 = 1,331 blocks).
- **[Risk] Obstructions during automatic cabinet construction**: Blocks above or beside the Lodestone might prevent safe placement.
  - **Mitigation**: Verify that `Y + 1` and `Y + 2` are either air or valid cabinet blocks, and verify a valid wall position exists for the lever before modifying world blocks. If obstructed, abort registration and send a descriptive message.
