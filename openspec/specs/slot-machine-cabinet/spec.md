# slot-machine-cabinet Specification

## Purpose

Defines the physical slot machine cabinet architecture including base, shelf, and canopy blocks, surface-aligned display positions, and multiblock damage protection.

## Requirements

### Requirement: Automatic Cabinet Construction on Registration
The system SHALL construct the cabinet enclosure upon wand interaction according to the wand type in hand, mounting an attached wall `LEVER` on the right side of the Lodestone as viewed by the player facing the machine front if missing, requiring only a Lodestone from the administrator.

#### Scenario: Registration with Cabinet Wand
- **WHEN** an administrator interacts with a bare Lodestone using the Cabinet Wand and the spaces at Y+1 and Y+2 are unobstructed
- **THEN** the system places a Spruce Shelf at Y+1, a flat Spruce Trapdoor canopy at Y+2, an attached wall Lever on the player's right side, registers the machine with all cabinet locations, and spawns the display reels

#### Scenario: Registration with Base Wand
- **WHEN** an administrator interacts with a bare Lodestone using the Base Wand
- **THEN** the system ensures an attached wall Lever is placed on the player's right side, registers the machine without shelf or canopy blocks, and spawns the display reels

#### Scenario: Registration with pre-existing cabinet blocks
- **WHEN** an administrator interacts with a Lodestone using the wand that already has valid shelf, canopy, or lever blocks placed
- **THEN** the system adopts the existing blocks without overwriting and registers the machine

#### Scenario: Registration blocked by obstruction
- **WHEN** an administrator interacts with a Lodestone using the Cabinet Wand but blocks at Y+1 or Y+2 are occupied by non-cabinet, non-air blocks
- **THEN** the system cancels creation and informs the administrator that the space above the Lodestone must be clear

### Requirement: Administrative Plugin Reload Command
The system SHALL provide a `/letitroll reload` command accessible only to administrators that reloads plugin configuration and refreshes active slot machines and displays from storage.

#### Scenario: Admin executes reload command
- **WHEN** an administrator executes `/letitroll reload`
- **THEN** the system reloads plugin configuration, removes and respawns active ItemDisplay entities according to stored machine locations, and reports success to the administrator

#### Scenario: Non-admin attempts reload
- **WHEN** a player without administrator permissions executes `/letitroll reload`
- **THEN** the system denies execution and displays a permission error message

### Requirement: Surface Aligned Display Positioning
The system SHALL position the three ItemDisplay reel entities so they are centered and aligned within the Spruce Shelf block.

#### Scenario: Display entities spawn on shelf surface
- **WHEN** the machine spawns its three ItemDisplay entities
- **THEN** the entities are positioned horizontally centered over the Lodestone and vertically anchored within the Y+1 Spruce Shelf block aligned with the machine facing

### Requirement: Multiblock Cabinet Protection
The system SHALL protect the Lodestone, attached Lever, and any registered Spruce Shelf or canopy trapdoor blocks belonging to an active slot machine from breaking or explosion damage.

#### Scenario: Non-admin attempts to break shelf trapdoor
- **WHEN** a player without administrator permissions attempts to break a registered slot machine shelf or canopy block
- **THEN** the system cancels the break event and notifies the player that the block is protected

#### Scenario: Explosion damages cabinet blocks
- **WHEN** an explosion affects blocks belonging to an active slot machine cabinet
- **THEN** protected cabinet blocks are preserved from explosion destruction

#### Scenario: Admin breaks Lodestone base
- **WHEN** an administrator breaks the Lodestone base block of an active slot machine
- **THEN** the system removes all displays, clears the registered shelf, canopy, and lever blocks, and deregisters the machine
