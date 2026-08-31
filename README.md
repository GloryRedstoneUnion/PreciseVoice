# PreciseVoice

PreciseVoice is a client-side Fabric 1.20.1 mod that applies a separate volume
multiplier to every sound event identifier.

## Usage

```text
/voice <type> <volume>
/voice all <volume>
/voice status
```

- `type` is a sound event identifier such as `minecraft:block.chest.open`.
- `volume` is a float. `0.0` mutes the sound, `1.0` keeps the original volume,
  and values above `1.0` amplify it.
- The default accepted range is `0.0` through `3.0`.
- Sound identifiers are suggested while typing the command.
- `all` applies an overall multiplier to every sound. It is multiplied by any
  per-sound value, with the final result capped at `maxVolume`.
- `status` shows the current `all` value and every individually modified sound.

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
