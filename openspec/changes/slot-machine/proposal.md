# Proposal

## Why
Players want an immersive, interactive in-world casino minigame in Minecraft Paper 1.21.10. Instead of relying on chest GUIs or text-only commands, slot machines are physically built into the world (using a Lodestone base, lever, and wooden shelf display), where players can pull the lever to bet diamonds and watch reels spin in real-time.

## What Changes
- Add an Admin Setup tool (`/letitroll wand`) to register a slot machine on a physical Lodestone base block with an attached lever.
- Spawn and manage a 3x1 reel display on the wooden shelf using modern Paper `ItemDisplay` entities.
- Implement one-touch lever pull interaction: pulling the lever automatically checks and deducts 1 Diamond from the player's inventory to spin.
- Implement a synchronized reel spin animation with tick-staggered locks and audio-visual feedback.
- Implement win/loss evaluation with 4 distinct icon tiers: Pufferfish (Worst), Piglin Head (Bad), Resin Clump (Good), and Amethyst Shard (Best).
- Payout rewards directly to winners with celebration fanfare (titles, sounds, particles) or sound low buzzer on loss.
- Persist machine data across server restarts using Bukkit's `PersistentDataContainer` (PDC) and an in-memory runtime registry.

## Capabilities

### New Capabilities
- `slot-machine`: In-world slot machine registration, inventory credit charging on lever pull, reel display animation using `ItemDisplay`, outcome evaluation, and payout distribution.

### Modified Capabilities
*(None - this is the initial feature set)*

## Impact
- **APIs**: Paper 1.21.10 API (`ItemDisplay`, `PersistentDataContainer`, `TileState`, `Switch` block data, Adventure `Component`/`Title`/`Sound`).
- **Dependencies**: No external plugins required; built natively on Paper API.
- **Performance**: High tick performance using lightweight `ItemDisplay` entities and Bukkit/Paper regional schedulers.
