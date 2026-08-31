package dev.minecraft.precisevoice.config;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
