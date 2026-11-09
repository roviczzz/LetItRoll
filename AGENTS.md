# AGENTS.md

## Overview & Architecture
- **Type**: Minecraft Paper server plugin targeting Paper 1.21.10 (`io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT`).
- **Runtime/Toolchain**: Java 21 (`JavaLanguageVersion.of(21)` via Gradle toolchain).
- **Entrypoint**: `src/main/java/me/roviczzz/letItRoll/LetItRoll.java` (`JavaPlugin`).
- **Plugin Manifest**: `src/main/resources/plugin.yml`. Gradle `processResources` expands `${version}` token from project version (`gradle.properties`)—do not hardcode version in `plugin.yml`.

## Build & Test Commands
Use the wrapper script (`.\gradlew.bat` on Windows, `./gradlew` on POSIX). System `gradle` is not on `PATH`.

- **Compile only**: `.\gradlew.bat compileJava`
- **Build jar**: `.\gradlew.bat build` (outputs to `build/libs/LetItRoll-<version>.jar`)
- **Run tests**: `.\gradlew.bat test` (run focused test: `.\gradlew.bat test --tests <ClassName>.<methodName>`)
- **Full verification**: `.\gradlew.bat check`
- **Clean build**: `.\gradlew.bat clean`

## Dev Server (`runServer`)
- **Plugin**: Configured via `xyz.jpenilla.run-paper` plugin.
- **Run Server**: `.\gradlew.bat runServer` launches a local Paper 1.21.10 server in `./run/` (gitignored) with the plugin automatically deployed.
- **Process Warning**: `runServer` is an interactive, long-running process with a server console. Do not execute it blocking in automated agent sessions.
- **EULA**: Pre-accepted via JVM flag `-Dcom.mojang.eula.agree=true` in `build.gradle.kts`.

## Toolchain & Workflow Quirks
- **Gradle Caching**: `gradle.properties` enables `org.gradle.configuration-cache=true`, `org.gradle.parallel=true`, and `org.gradle.caching=true`. If build configuration changes cause cache issues, append `--no-configuration-cache`.
- **OpenSpec**: Configured at `openspec/` with `schema: spec-driven`. Workflows and skill definitions live under `.agents/` and `.opencode/`.
