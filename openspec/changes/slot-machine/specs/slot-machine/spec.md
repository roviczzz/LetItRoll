# Spec Delta

## Purpose
Provides an interactive in-world slot machine minigame where players bet diamonds by pulling a physical lever on a multiblock cabinet with dynamic display entities.

## ADDED Requirements

### Requirement: Machine Registration and Anchoring
The system SHALL allow authorized administrators to register a slot machine on a physical Lodestone base block, automatically locating the attached lever and spawning three ItemDisplay entities centered on the shelf.

#### Scenario: Successful registration with wand
- **WHEN** an admin right-clicks a Lodestone with the registration wand
- **THEN** the system validates the attached lever and shelf, registers the machine in persistent storage, and spawns three ItemDisplay entities displaying default icons

#### Scenario: Missing attached lever
- **WHEN** an admin clicks a Lodestone that lacks an attached lever
- **THEN** the system cancels registration and displays an error message requesting a lever

### Requirement: Automatic Diamond Deduction on Lever Pull
The system SHALL intercept interactions with the machine's lever, check the player's inventory for at least one Diamond, deduct exactly one Diamond, and initiate the spin.

#### Scenario: Player has at least one Diamond
- **WHEN** a player right-clicks the lever on an idle machine while holding or carrying at least 1 Diamond
- **THEN** the system deducts 1 Diamond from their inventory, animates the lever, and initiates the spinning sequence

#### Scenario: Player lacks Diamonds
- **WHEN** a player right-clicks the lever on an idle machine with 0 Diamonds in their inventory
- **THEN** the system cancels the lever action, plays a locked click sound, and sends a notice stating 1 Diamond is required

#### Scenario: Lever pulled while already spinning
- **WHEN** a player interacts with the lever while the machine is actively spinning
- **THEN** the system ignores the interaction and prevents duplicate bets

### Requirement: Staggered Reel Spin Animation
The system SHALL cycle the three ItemDisplay entities through the four designated icons with synchronized ticking audio and lock each reel sequentially.

#### Scenario: Sequential reel lock
- **WHEN** a spin is initiated
- **THEN** the reels cycle icons rapidly, Reel 1 locks after a short duration, Reel 2 locks next, and Reel 3 locks last with distinct audio feedback

### Requirement: Outcome Evaluation and Tiered Payouts
The system SHALL evaluate the final three icons upon reel completion, awarding tiered payouts if all three match and concluding as a loss otherwise.

#### Scenario: Jackpot match (3x Amethyst Shard)
- **WHEN** all three reels stop on Amethyst Shard
- **THEN** the system declares a jackpot, rewards 64 Diamonds, triggers celebration particles/fanfare, and returns to idle

#### Scenario: High win match (3x Resin Clump)
- **WHEN** all three reels stop on Resin Clump
- **THEN** the system rewards 16 Diamonds with celebratory chimes and returns to idle

#### Scenario: Low win match (3x Piglin Head)
- **WHEN** all three reels stop on Piglin Head
- **THEN** the system rewards 4 Diamonds with chime sounds and returns to idle

#### Scenario: Consolation match (3x Pufferfish)
- **WHEN** all three reels stop on Pufferfish
- **THEN** the system rewards 1 Diamond consolation and returns to idle

#### Scenario: No match loss
- **WHEN** the three reels do not all match
- **THEN** the system plays a low buzzer sound, displays a consolation message, and returns to idle

### Requirement: Safe Teardown and Cleanup
The system SHALL safely remove display entities and deregister the machine when the Lodestone is broken.

#### Scenario: Base block broken
- **WHEN** the Lodestone base block of an active slot machine is broken
- **THEN** the system removes all associated ItemDisplay entities and deletes the machine registration
