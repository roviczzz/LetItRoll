# AGENTS.md

## Overview & Architecture
- **Type**: Minecraft Paper server plugin targeting Paper 1.21.10 (`io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT`).
- **Runtime/Toolchain**: Java 21 (`JavaLanguageVersion.of(21)` via Gradle toolchain).
- **Entrypoint**: `src/main/java/me/roviczzz/letItRoll/LetItRoll.java` (`JavaPlugin`).
- **Core Architecture (`me.roviczzz.letItRoll`)**:
  - `manager/SlotMachineManager`: Central coordinator; caches active machines in memory by UUID, Lodestone `Location`, and Lever `Location`.
  - `storage/SlotMachineRepository`: Saves/loads machine metadata (locations, facing, display UUIDs) to `machines.yml`.
  - `display/DisplayManager`: Spawns, aligns, and removes 3 client-rendered `ItemDisplay` entities above the Lodestone based on horizontal facing.
  - `game/SpinTask`: 70-tick `BukkitRunnable` handling staggered reel locks (ticks 26, 46, 66), audio, lever resetting, outcome evaluation, and diamond rewards.
  - `listener/`: `LeverInteractListener` handles bet deduction (1 Diamond) and spin initiation; `MachineProtectionListener` prevents breaking or explosion of active machines; `WandListener` creates machines via `/letitroll wand`.
- **Plugin Manifest**: `src/main/resources/plugin.yml`. Gradle `processResources` token-replaces `${version}` from `gradle.properties`—do not hardcode the version in `plugin.yml`.

## Build & Test Commands
Use the wrapper script (`.\gradlew.bat` on Windows, `./gradlew` on POSIX). System `gradle` is not on `PATH`.

- **Compile only**: `.\gradlew.bat compileJava`
- **Run all tests**: `.\gradlew.bat test`
- **Run focused test**: `.\gradlew.bat test --tests SlotIconTest` or `.\gradlew.bat test --tests SlotMachineTest.testSlotMachineCreationAndState`
- **Full verification**: `.\gradlew.bat check`
- **Build jar**: `.\gradlew.bat build` (outputs to `build/libs/LetItRoll-<version>.jar`)
- **Clean build**: `.\gradlew.bat clean`

## Testing Quirks
- **No Bukkit Mocking Environment**: The test suite uses pure JUnit 5 (`junit-jupiter:5.10.2`) without MockBukkit or Mockito. Tests cannot instantiate Bukkit server singletons (`Bukkit.getEntity`, `World`, `ItemDisplay`, etc.). Keep unit tests restricted to decoupled models, mathematics, or state machines (`model/`).

## Dev Server (`runServer`)
- **Plugin**: Configured via `xyz.jpenilla.run-paper` plugin.
- **Run Server**: `.\gradlew.bat runServer` launches a local Paper 1.21.10 server in `./run/` (gitignored) with the plugin automatically deployed.
- **Process Warning**: `runServer` is an interactive, blocking foreground process with a server console. Do not execute it blocking in automated agent sessions.
- **EULA**: Pre-accepted via JVM flag `-Dcom.mojang.eula.agree=true` in `build.gradle.kts`.

## Toolchain & Workflow Quirks
- **Gradle Caching**: `gradle.properties` sets `configuration-cache=true`, `parallel=true`, and `caching=true`. If build errors occur due to cache serialization or environment changes, append `--no-configuration-cache`.
- **OpenSpec**: Managed under `openspec/` with `schema: spec-driven`. Workflows and skill definitions live under `.agents/` and `.opencode/`.
