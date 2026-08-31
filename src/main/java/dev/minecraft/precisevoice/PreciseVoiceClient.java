package dev.minecraft.precisevoice;

import dev.minecraft.precisevoice.command.VoiceCommand;
import dev.minecraft.precisevoice.config.VoiceConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PreciseVoiceClient implements ClientModInitializer {
    public static final String MOD_ID = "precisevoice";
    public static final String MOD_NAME = "PreciseVoice";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static volatile VoiceConfigManager config;

    @Override
    public void onInitializeClient() {
        config = VoiceConfigManager.load(
            FabricLoader.getInstance().getGameDir().resolve(MOD_NAME + ".json")
        );
        ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> VoiceCommand.register(dispatcher, config)
        );
    }

    public static float getVolumeMultiplier(Identifier soundId) {
        VoiceConfigManager currentConfig = config;
        return currentConfig == null ? 1.0F : currentConfig.getMultiplier(soundId);
    }
}
