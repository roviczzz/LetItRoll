# Proposal

## Why

The current slot machine implementation renders spinning `ItemDisplay` icons floating mid-air above a bare Lodestone base without a physical shelf or decorative cabinet. Furthermore, inspecting in-game builds directly from external tools or developer chat currently requires manual coordinate guesswork. Providing an in-game dump command allows immediate export of in-world block structures to inspectable JSON files, while introducing a spruce shelf and canopy cabinet structure grounds the item displays on realistic physical surfaces and protects the machine's full multiblock enclosure.

## What Changes

- Add `/letitroll dump [radius]` command for administrators to scan targeted block volumes and export block types, relative coordinates, and block data states to `plugins/LetItRoll/captures/build-dump.json` as well as outputting ASCII cross-sections to the console.
- Add support for distinct wand variants via `/letitroll wand [cabinet|base]`:
  - **Cabinet Wand** (default): Requires only a Lodestone base; interaction automatically places a Spruce Shelf (`SPRUCE_SHELF`) at `Y + 1`, a flat Spruce Trapdoor canopy (`SPRUCE_TRAPDOOR`) at `Y + 2`, and an attached wall `LEVER` on the right side of the Lodestone from the perspective of a player facing the machine front.
  - **Base Wand**: Creates a minimal slot machine requiring only a Lodestone base and mounting an attached wall `LEVER` on the right side if missing, without adding shelf or canopy blocks.
  - Adopts pre-existing cabinet blocks if already placed and verifies space clearance if blocks need to be constructed.
  - Calibrate `DisplayManager` ItemDisplay positions so the 3 reel items align properly within the spruce shelf block (`Y + 1`).
- Add `/letitroll reload` administrative command to reload configuration and refresh active machines and displays from storage.
- Enhance `MachineProtectionListener` to protect constructed shelf (`SPRUCE_SHELF`) and canopy (`SPRUCE_TRAPDOOR`) blocks belonging to an active slot machine from breaking or explosion damage.
- Automatically tear down and clean up constructed cabinet blocks when an active slot machine is unregistered or destroyed.

## Capabilities

### New Capabilities
- `slot-machine-cabinet`: Multi-variant wand creation (cabinet vs base), player-right lever mounting, automatic cabinet construction, calibrated display alignment, multiblock protection, cleanup upon removal, and configuration reload.
- `in-game-structure-dump`: In-game administrative block dumping command (`/letitroll dump [radius]`) exporting targeted block volumes into JSON and ASCII diagnostics.

### Modified Capabilities
<!-- None: openspec/specs/ is currently empty. -->

## Impact

- **Commands**: New sub-commands `/letitroll dump [radius]`, `/letitroll reload`, and expanded `/letitroll wand [cabinet|base]` with tab completion added to `LetItRollCommand`.
- **Display**: `DisplayManager` slot coordinate calculation adjusted to align item icons within the Spruce Shelf block at Y+1.
- **Protection**: `MachineProtectionListener` extended to track and protect cabinet shelf (`SPRUCE_SHELF`) and canopy (`SPRUCE_TRAPDOOR`) blocks in addition to base Lodestone and Lever blocks.
- **Storage**: Optional cabinet block locations stored in `SlotMachine` model and persisted to `machines.yml`.
