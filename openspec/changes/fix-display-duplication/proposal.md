# Proposal

## Why

Slot machine displays duplicate on every server restart. `loadMachines()` unconditionally removes and respawns the three persistent `ItemDisplay` reels for each machine, but the removal runs at `POSTWORLD` enable time when the machine's chunk is not loaded, so `Bukkit.getEntity(uuid)` returns `null` and the removal silently no-ops. The subsequent spawn loads the chunk, materializing the disk-saved originals alongside the new entities, and `repository.save(machine)` overwrites the tracked UUIDs — permanently orphaning the originals. `SpinTask` only animates the tracked trio, so the orphaned amethyst reels remain visible and overlap the live ones. The damage compounds with each restart.

## What Changes

- `loadMachines()` becomes a pure in-memory load: no display removal, no display spawning, no per-machine `repository.save`.
- Add a display reconciliation routine that treats the persistent data container (`machine_id` + `slot_index`) as the source of truth, not the UUID array in `machines.yml`.
  - Keeps at most one display per slot, deleting duplicates.
  - Spawns a replacement only for a slot with no surviving display.
  - Persists the machine only when the UUID set actually changed.
- Reconcile on entity load (`EntitiesLoadEvent`), using the event's entity list rather than a radius scan.
- Reconcile machines whose chunk is already loaded immediately after `loadMachines()`, covering machines in spawn chunks whose `EntitiesLoadEvent` fired before the plugin enabled.
- Remove any loaded display whose `machine_id` is not a registered machine, cleaning orphans left by machines deleted while their chunk was unloaded.
- Reconcile on lever interaction and on `/letitroll reload` via a bounded box scan, since no entity event is available on those paths.
- **BREAKING** (behavioral): `/letitroll reload` no longer respawns displays, so it no longer resets reels to Amethyst Shard or causes a visual flicker.
- `removeMachine` purges displays by PDC `machine_id`, so deletion no longer depends on the stored UUIDs resolving.

## Capabilities

### New Capabilities

- `slot-machine-display-lifecycle`: Ownership, persistence, reconciliation, and cleanup of the `ItemDisplay` reel entities that belong to a registered slot machine.

### Modified Capabilities

- `slot-machine-cabinet`: The "Administrative Plugin Reload Command" requirement currently mandates removing and respawning active `ItemDisplay` entities; it must instead reconcile existing displays without destroying valid ones.

## Impact

- `display/DisplayManager.java`: reconciliation and PDC-based purge replace blind remove-then-spawn.
- `manager/SlotMachineManager.java`: `loadMachines()` semantics change; display lifecycle hooks added.
- `listener/`: new entity-load listener; lever listener triggers reconciliation.
- `storage/SlotMachineRepository.java`: no schema change; display UUIDs become a cache rather than the authority.
- `machines.yml`: no longer rewritten on every startup.
- Existing worlds: accumulated duplicate displays are cleaned the first time each affected chunk's entities load.
