package dev.minecraft.precisevoice.config;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoiceConfigManagerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void createsDefaultConfigInRequestedDirectory() {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");

        VoiceConfigManager config = VoiceConfigManager.load(path);

        assertTrue(Files.exists(path));
        assertEquals(3.0F, config.getMaxVolume());
        assertEquals(1.0F, config.getAllMultiplier());
        assertEquals(1.0F, config.getMultiplier(new Identifier("minecraft:block.chest.open")));
    }

    @Test
    void persistsSoundVolume() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Identifier soundId = new Identifier("minecraft:entity.creeper.primed");
        VoiceConfigManager config = VoiceConfigManager.load(path);

        config.setMultiplier(soundId, 2.25F);
        VoiceConfigManager reloaded = VoiceConfigManager.load(path);

        assertEquals(2.25F, reloaded.getMultiplier(soundId));
    }

    @Test
    void settingDefaultSoundVolumeRemovesOverride() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Identifier soundId = new Identifier("minecraft:entity.villager.no");
        VoiceConfigManager config = VoiceConfigManager.load(path);

        config.setMultiplier(soundId, 0.2F);
        config.setMultiplier(soundId, 1.0F);
        VoiceConfigManager reloaded = VoiceConfigManager.load(path);

        assertTrue(reloaded.getSoundMultipliers().isEmpty());
        assertEquals(1.0F, reloaded.getMultiplier(soundId));
        assertTrue(Files.readString(path).contains("\"volumes\": {}"));
    }

    @Test
    void loadingLegacyConfigRemovesDefaultSoundOverrides() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Files.writeString(path, """
            {
              "maxVolume": 3.0,
              "allVolume": 1.0,
              "volumes": {
                "minecraft:entity.generic.explode": 0.2,
                "minecraft:entity.villager.no": 1.0
              }
            }
            """);

        VoiceConfigManager config = VoiceConfigManager.load(path);

        assertEquals(1, config.getSoundMultipliers().size());
        assertEquals(0.2F, config.getSoundMultipliers().get(
            "minecraft:entity.generic.explode"
        ));
        assertFalse(Files.readString(path).contains("minecraft:entity.villager.no"));
    }

    @Test
    void persistsAndCombinesAllSoundVolume() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Identifier soundId = new Identifier("minecraft:entity.creeper.primed");
        VoiceConfigManager config = VoiceConfigManager.load(path);

        config.setAllMultiplier(0.5F);
        config.setMultiplier(soundId, 2.0F);
        VoiceConfigManager reloaded = VoiceConfigManager.load(path);

        assertEquals(0.5F, reloaded.getAllMultiplier());
        assertEquals(2.0F, reloaded.getSoundMultipliers().get(soundId.toString()));
        assertEquals(1.0F, reloaded.getMultiplier(soundId));
        assertEquals(
            0.5F,
            reloaded.getMultiplier(new Identifier("minecraft:block.chest.open"))
        );
    }

    @Test
    void clampsStoredValuesWhenMaximumIsLowered() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Files.writeString(path, """
            {
              "maxVolume": 1.5,
              "volumes": {
                "minecraft:block.anvil.land": 2.5
              }
            }
            """);

        VoiceConfigManager config = VoiceConfigManager.load(path);

        assertEquals(1.5F, config.getMultiplier(new Identifier("minecraft:block.anvil.land")));
        assertThrows(
            IllegalArgumentException.class,
            () -> config.setMultiplier(new Identifier("minecraft:block.anvil.land"), 1.75F)
        );
    }

    @Test
    void capsCombinedMultiplierAtConfiguredMaximum() throws IOException {
        Path path = temporaryDirectory.resolve("PreciseVoice.json");
        Files.writeString(path, """
            {
              "maxVolume": 1.5,
              "allVolume": 1.5,
              "volumes": {
                "minecraft:block.anvil.land": 1.5
              }
            }
            """);

        VoiceConfigManager config = VoiceConfigManager.load(path);

        assertEquals(1.5F, config.getMultiplier(new Identifier("minecraft:block.anvil.land")));
    }
}
