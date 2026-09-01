# PreciseVoice

PreciseVoice is a client-side Fabric mod for Minecraft 1.20.1 that provides
fine-grained, persistent volume controls for individual sound events.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.15.0 or later
- Fabric API
- Java 17 or later

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.20.1.
2. Download the latest mod JAR from the
   [Releases](https://github.com/GloryRedstoneUnion/PreciseVoice/releases) page.
3. Place the JAR in the client instance's `mods` directory.

PreciseVoice is client-only and does not need to be installed on a server.

## Commands

```text
/voice <type> <volume>
/voice all <volume>
/voice status
```

- `type` is a sound event identifier such as `minecraft:block.chest.open`.
  The default `minecraft` namespace may be omitted.
- `volume` is a float. `0.0` mutes the sound, `1.0` keeps the original volume,
  and values above `1.0` amplify it.
- The default accepted range is `0.0` through `3.0`.
- Sound identifiers are suggested while typing the command.
- `all` applies an overall multiplier to every sound. It is multiplied by any
  per-sound value, with the final result capped at `maxVolume`.
- `status` shows only volume options that differ from their default multiplier
  of `1.0`.
- Setting an individual sound or `all` to `1.0` resets that option. Individual
  sound overrides reset this way are removed from the configuration file.

## Configuration

The configuration is saved as `PreciseVoice.json` in the game instance root:

```json
{
  "maxVolume": 3.0,
  "allVolume": 0.8,
  "volumes": {
    "minecraft:block.chest.open": 0.35,
    "minecraft:entity.creeper.primed": 1.75
  }
}
```

Edit `maxVolume` while the game is closed to change the command's upper limit.
Values above `1.0` can cause clipping depending on the sound and audio device.

## Build

```powershell
.\gradlew.bat build
```

The remapped mod jar is generated in `build/libs`.

## License

PreciseVoice is available under the [MIT License](LICENSE).
