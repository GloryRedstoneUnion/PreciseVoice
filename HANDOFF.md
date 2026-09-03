# PreciseVoice Handoff

Generated: 2026-09-03

## Current Focus

The current task is to publish the handoff context, link it from the README,
and push the documentation update to GitHub. Do not create a new GitHub
Release for this documentation-only change.

## Repository

- Local path: `D:\MinecraftDev\Project\PreciseVoice`
- Remote: `https://github.com/GloryRedstoneUnion/PreciseVoice.git`
- Branch: `main`
- Latest feature release: [`v1.1.0`](https://github.com/GloryRedstoneUnion/PreciseVoice/releases/tag/v1.1.0)
- Release assets: the remapped mod JAR and the sources JAR are attached to the release.

## Implemented Behavior

PreciseVoice is a client-only Fabric mod for Minecraft 1.20.1. The command
surface and persistent configuration are implemented in
[`VoiceCommand.java`](src/main/java/dev/minecraft/precisevoice/command/VoiceCommand.java)
and
[`VoiceConfigManager.java`](src/main/java/dev/minecraft/precisevoice/config/VoiceConfigManager.java).

- `/voice <type> <volume>` sets a sound-event multiplier.
- `/voice all <volume>` sets the overall multiplier.
- `/voice status` lists only non-default settings.
- `/voice resetall` restores the overall multiplier and all per-sound
  multipliers to `1.0`, clears per-sound overrides, preserves `maxVolume`,
  persists the result, and refreshes active playback.
- Configuration is stored as `PreciseVoice.json` in the game root.
- Sound multipliers are capped by the configurable `maxVolume`.

## Verification

Run the following from the repository root:

```powershell
.\gradlew.bat clean build --offline
```

The build and the complete automated test suite passed before this handoff
update. Tests cover command parsing, status filtering, reset behavior,
configuration persistence, migration, and volume clamping. Keep the client-only
environment in `fabric.mod.json` unchanged.

## Suggested Skills

- `handoff`: create the next session's handoff document when work needs to be transferred again.
- `code-review`: review changes against the current specification and repository standards.
- `diagnosing-bugs`: investigate any reported runtime or sound-volume regression.

## Change Policy

Use Git for all changes and commit completed work. Release tags and GitHub
Release pages should only be changed when the user explicitly requests a new
release.
