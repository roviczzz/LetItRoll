# Design

## Context

See `proposal.md` for motivation and `specs/` for normative requirements.

Current state that shapes the approach:

- `DisplayManager.spawnDisplays` marks reel entities `setPersistent(true)` and stamps each with `machine_id` (string UUID) and `slot_index` (integer) in its persistent data container. Those entities are saved in chunk data and survive restarts with their UUIDs intact.
- `SlotMachineRepository` persists the three display UUIDs per machine in `machines.yml`. Those UUIDs therefore duplicate information that already lives on the entities, and can go stale.
- `SlotMachineManager.loadMachines()` is called from both `onEnable()` (`plugin.yml` sets `load: POSTWORLD`) and `/letitroll reload`. It currently removes and respawns displays for every machine.
- `Bukkit.getEntity(uuid)` resolves only entities in loaded chunks. At `POSTWORLD` enable, machine chunks are not loaded, so removal silently no-ops while `world.spawn` subsequently loads the chunk and materializes the saved originals.
- The test suite is pure JUnit 5 with no Bukkit mocking, so reconciliation decisions must be separable from Bukkit API calls to be testable (`AGENTS.md`).

## Goals / Non-Goals

**Goals:**

- Make startup a pure in-memory load with no display side effects.
- Make reconciliation idempotent and safe to run at any time, from any trigger, including with unloaded chunks.
- Clean up damage already present in existing worlds.
- Keep the reconciliation decision logic unit-testable without a Bukkit server.

**Non-Goals:**

- Changing display geometry, reel probabilities, icons, or payout logic.
- Removing the `displays` field from `machines.yml` (kept as a cache for backward compatibility).
- Guaranteeing instant cleanup of orphaned displays in chunks that are never loaded.
- Reworking the lever or cabinet block lifecycle.

## Decisions

### Decision 1: Persistent data container is the authority

Identify a display by `(machine_id, slot_index)` read from its PDC, not by the UUID array in `machines.yml`. The UUID array becomes a cache/hint used for a fast path and for preferring which duplicate to keep.

**Rationale:** PDC data travels with the entity, survives restarts, and describes every display that exists, including ones the plugin has lost track of. UUID lists can only describe what the plugin last knew about.

**Alternatives considered:**
- *Trust the UUID list*: cannot detect or remove duplicates — this is the current bug.
- *Name or tag by scoreboard/team*: heavier, no better than PDC.
- *Store a single marker entity per machine*: adds a second entity lifecycle to keep consistent for no gain.

### Decision 2: Reconcile, never blindly respawn

Introduce a reconcile operation per machine. For each of slots 0..2, group the machine's found displays by `slot_index`, keep exactly one, delete the rest, and spawn a replacement only when the group is empty. Choose the survivor by preferring the UUID currently recorded for that slot, otherwise any member of the group.

**Rationale:** Reconciliation converges to a correct state from any starting state, which is what makes it safe on startup, on reload, and on chunk load alike.

**Alternatives considered:**
- *Delete-all-then-spawn*: correct output but causes flicker, resets icons, and requires the chunk to be loaded.
- *Spawn-only-if-missing, no dedupe*: leaves the existing duplicates in place.

### Decision 3: Trigger on `EntitiesLoadEvent`, not `ChunkLoadEvent`

Reconcile from `EntitiesLoadEvent`, which fires after a chunk's entities are added to the world and exposes the loaded entity list. `ChunkLoadEvent` fires before entities exist and would reintroduce the chunk-loading race.

**Rationale:** The event hands over the entities directly, eliminating both the "is the chunk loaded" guard and the need for a radius scan. It also gives a natural place to delete displays whose `machine_id` is unregistered.

**Alternatives considered:**
- *`ChunkLoadEvent` + `getChunkAt().getEntities()`*: works but forces chunk access and does more work than the event's own list.
- *Periodic repeating task sweeping all machines*: wasteful and still misses unloaded chunks.

### Decision 4: Boot sweep for already-loaded chunks

After `loadMachines()` completes in `onEnable()`, iterate registered machines and reconcile those whose chunk is already loaded. Do not load any chunk.

**Rationale:** Spawn chunks are loaded during world initialization, before `POSTWORLD` enable, so their `EntitiesLoadEvent` has already fired and cannot be observed. At `POSTWORLD` only a handful of chunks are loaded, so the sweep is cheap.

**Alternatives considered:**
- *Force-load all machine chunks at enable*: unbounded memory and startup cost on a large world.
- *Do nothing and rely on lever use*: machines in spawn chunks stay duplicated until touched.

### Decision 5: Box scan only where no event exists

Lever interaction and `/letitroll reload` have no entity list available, so they use a bounded scan around the machine's calculated slot positions, filtered by PDC `machine_id`. The `machine_id` filter is mandatory, not an optimization: two adjacent machines can place displays within each other's search box.

**Rationale:** Keeps the fast path event-driven while giving interactive paths a deterministic way to self-heal.

**Alternatives considered:**
- *Scan the whole chunk*: correct but heavier, and still needs the same PDC filter.
- *Scan only exact UUIDs*: cannot find duplicates.

### Decision 6: Persist only on change

`reconcile` returns whether the UUID set changed; `SlotMachineManager` calls `repository.save` only in that case. `loadMachines()` never saves.

**Rationale:** Eliminates a full `machines.yml` rewrite on every boot, and avoids writing a file that the reconciliation did not actually alter.

### Decision 7: Extract the pure decision function

Split reconciliation into a pure function that takes the recorded UUIDs plus a list of `(uuid, slotIndex)` descriptors of found displays and returns a plan: survivors, UUIDs to delete, slots to spawn. A thin Bukkit-facing method applies the plan.

**Rationale:** `AGENTS.md` forbids Bukkit mocking in tests. The grouping, duplicate selection, and missing-slot detection are the parts most likely to regress, and they are all expressible as plain data. The plan type also makes the "no writes when already correct" requirement directly assertable.

**Alternatives considered:**
- *Test via the Bukkit-facing method*: not possible without a server.
- *No extraction*: leaves the bug's logic untested.

### Decision 8: Deletion purges by identity, not by UUID

`removeMachine` removes displays by PDC `machine_id` using the same lookup as reconciliation, instead of only resolving recorded UUIDs.

**Rationale:** Recorded UUIDs may be stale or in an unloaded chunk; identity-based purge is the only reliable way to avoid leaving orphans behind.

## Risks / Trade-offs

- **[Risk] `EntitiesLoadEvent` fires for every chunk with entities, so the handler runs often.** → Filter early on "does this entity carry our `machine_id` key" and return immediately otherwise; the handler does no work for chunks without slot machine displays.
- **[Risk] Adjacent machines place displays inside each other's scan box.** → Always filter by PDC `machine_id`; never delete an entity whose identity does not match.
- **[Risk] A reconcile triggered mid-spin could race `SpinTask`.** → `SpinTask` re-reads `machine.getDisplayUuids()` every tick (`SpinTask.java:58`), so a UUID change is picked up on the next tick. Reconciliation keeps the existing display whenever one is valid, so a normal spin is untouched.
- **[Risk] Orphans in never-loaded chunks persist indefinitely.** → Accepted; they are invisible to players until the chunk loads, at which point the event handler removes them.
- **[Risk] Removing and respawning on a legitimate missing slot changes the visible icon to the default.** → Accepted; a slot with no display has nothing to preserve. Reconciliation never resets slots that already have a valid display.
- **[Risk] The boot sweep's "chunk already loaded" check could be wrong for a chunk in the middle of loading.** → The check is a cheap predicate; if it reports not-loaded, the `EntitiesLoadEvent` handler covers it moments later. Reconciliation is idempotent, so a double run is harmless.

## Migration Plan

No schema migration and no manual world edits.

1. Deploy the plugin build; restart the server.
2. On enable, no displays are spawned or removed. Existing duplicates remain until their chunk's entities load.
3. As players visit machines, `EntitiesLoadEvent` collapses each slot to a single display; the first lever pull or `/letitroll reload` also reconciles on demand.
4. Orphaned displays from machines deleted while unloaded are removed on the same event.

Rollback: reinstating the previous behavior requires no data changes; `machines.yml` retains the `displays` field and remains readable by the old code. Note that rolling back reintroduces the duplication bug on the next restart.

## Open Questions

None.
