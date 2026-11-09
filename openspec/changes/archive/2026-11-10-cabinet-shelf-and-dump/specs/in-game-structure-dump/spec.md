# Spec Delta

## Purpose

Provides an administrative command to scan and export in-game Minecraft block structures to formatted JSON files and console ASCII diagrams for inspection.

## ADDED Requirements

### Requirement: Admin In-Game Dump Command
The system SHALL provide a `/letitroll dump [radius]` command accessible only to administrators that scans a bounding box around the targeted block.

#### Scenario: Admin executes dump on targeted block
- **WHEN** an administrator targeting a block runs `/letitroll dump` with an optional radius between 1 and 5
- **THEN** the system scans the bounding box volume centered on the target block and outputs the results

#### Scenario: Non-admin attempts dump command
- **WHEN** a player without administrator permissions runs `/letitroll dump`
- **THEN** the system rejects the execution and displays an error message

#### Scenario: No target block in range
- **WHEN** an administrator runs `/letitroll dump` without looking at a valid block within line-of-sight range
- **THEN** the system prompts the administrator to look at a target block

### Requirement: Structured Block Serialization and Export
The system SHALL serialize scanned blocks into a JSON file containing relative coordinates, material names, and block state data, while outputting a readable ASCII layer slice to the console.

#### Scenario: Successful file write and console output
- **WHEN** a dump command successfully scans a block volume
- **THEN** the system writes the serialized block data to `plugins/LetItRoll/captures/build-dump.json` and prints an ASCII diagram of the layers to the server console
