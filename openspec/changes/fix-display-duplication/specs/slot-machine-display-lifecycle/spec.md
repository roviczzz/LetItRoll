# Spec Delta

## Purpose

Owns the lifecycle of the `ItemDisplay` reel entities belonging to a registered slot machine: how they are identified, persisted across restarts, reconciled with world state, and removed.

## ADDED Requirements

### Requirement: Displays Persist Across Server Restarts

The system SHALL keep each slot machine's three reel displays in the world across server restarts without recreating them, preserving the icon shown at the time of shutdown.

#### Scenario: Restart with displays already saved

- **WHEN** the server restarts and a registered machine's displays were saved in the world data
- **THEN** the system does not spawn new displays for that machine and the existing displays remain as the only reels present

#### Scenario: Startup performs no display writes

- **WHEN** the plugin enables and loads registered machines
- **THEN** the system performs no display spawn, no display removal, and no machine persistence write

#### Scenario: Last spin result survives restart

- **WHEN** a machine's displays show a spin result and the server restarts
- **THEN** the displays show that same result after restart rather than being reset to a default icon

### Requirement: Displays Are Identified by Persistent Data

The system SHALL identify a display as belonging to a machine and slot by the machine identifier and slot index stored on the entity, and SHALL treat that data rather than the recorded UUID list as the authoritative link.

#### Scenario: Display carries machine and slot identity

- **WHEN** the system spawns a reel display
- **THEN** the entity is stamped with the owning machine's identifier and its slot index

#### Scenario: Recorded UUID is stale

- **WHEN** a stored display UUID no longer resolves to a live display
- **THEN** the system still finds the machine's displays by their stored identity data and reconciles them

### Requirement: Reconciliation Preserves Valid Displays

The system SHALL reconcile a machine's displays against the machine's slot positions and SHALL keep an existing display unchanged when it is valid for its slot.

#### Scenario: Machine is already correct

- **WHEN** reconciliation runs for a machine that has exactly one valid display in each of its three slots
- **THEN** the system keeps all three displays, removes none, spawns none, and does not modify the machine's stored display UUIDs

#### Scenario: Reconciliation is repeatable

- **WHEN** reconciliation runs repeatedly against unchanged world state
- **THEN** each run produces the same result and no additional displays accumulate

### Requirement: Duplicate Displays Are Removed

The system SHALL keep at most one display per machine slot and SHALL remove any additional displays that claim the same slot.

#### Scenario: Duplicates from prior restarts exist

- **WHEN** a machine's slot contains more than one display carrying that machine's identifier and slot index
- **THEN** the system keeps one display for that slot and removes the others

#### Scenario: Duplicates overlap visually

- **WHEN** duplicate displays occupy the same slot position
- **THEN** after reconciliation only a single display renders at that position

### Requirement: Missing Displays Are Replaced

The system SHALL spawn a replacement display for a slot that has no surviving display.

#### Scenario: Display was destroyed externally

- **WHEN** reconciliation finds a machine slot with no display carrying that machine's identifier and slot index
- **THEN** the system spawns a new display for that slot, stamps its identity data, and records its UUID on the machine

#### Scenario: Only the affected slot is replaced

- **WHEN** a machine has a missing display in one slot and valid displays in the other two
- **THEN** the system spawns a display only for the missing slot and leaves the other two untouched

### Requirement: Orphaned Displays Are Removed

The system SHALL remove a loaded display that carries a slot machine identity which does not correspond to a registered machine.

#### Scenario: Machine was deleted while its chunk was unloaded

- **WHEN** a chunk loads containing displays whose machine identifier is not registered
- **THEN** the system removes those displays

#### Scenario: Foreign entity is ignored

- **WHEN** a loaded item display does not carry the slot machine identity data
- **THEN** the system leaves it untouched

### Requirement: Reconciliation Is Safe When the Chunk Is Not Loaded

The system SHALL skip display reconciliation for a machine whose chunk is not loaded, and SHALL NOT load the chunk for the purpose of reconciliation.

#### Scenario: Machine chunk unloaded at startup

- **WHEN** reconciliation is requested for a machine whose chunk is not loaded
- **THEN** the system makes no changes to displays or stored UUIDs and defers to a later trigger

#### Scenario: Reconciliation resumes on entity load

- **WHEN** a chunk containing machine displays loads
- **THEN** the system reconciles the machines belonging to that chunk

### Requirement: Machine Deletion Purges Its Displays

The system SHALL remove every display carrying the deleted machine's identifier when the machine is unregistered.

#### Scenario: Admin removes a machine

- **WHEN** a machine is removed through the administrative removal path or by breaking its Lodestone
- **THEN** the system removes all displays carrying that machine's identifier and deletes the machine's stored record

#### Scenario: Removal with stale recorded UUIDs

- **WHEN** a machine is removed and its stored display UUIDs do not resolve
- **THEN** the system still removes the machine's displays by their stored identity data
