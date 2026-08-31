package dev.minecraft.precisevoice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.minecraft.precisevoice.PreciseVoiceClient;
import dev.minecraft.precisevoice.config.VoiceConfigManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class VoiceCommand {
    private VoiceCommand() {
    }

    public static void register(
        CommandDispatcher<FabricClientCommandSource> dispatcher,
        VoiceConfigManager config
    ) {
        dispatcher.register(literal("voice")
            .then(argument("type", StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestIdentifiers(soundIds(), builder))
                .then(argument("volume", FloatArgumentType.floatArg(0.0F))
                    .executes(context -> setVolume(
                        context.getSource(),
                        StringArgumentType.getString(context, "type"),
                        FloatArgumentType.getFloat(context, "volume"),
                        config
                    ))))
        );
    }

    private static int setVolume(
        FabricClientCommandSource source,
        String rawId,
        float volume,
        VoiceConfigManager config
    ) {
        Identifier soundId = Identifier.tryParse(rawId);
        if (soundId == null) {
            source.sendError(Text.translatable("command.precisevoice.error.invalid_id", rawId));
            return 0;
        }
        if (!soundIds().contains(soundId)) {
            source.sendError(Text.translatable("command.precisevoice.error.unknown_sound", soundId));
            return 0;
        }
        if (!Float.isFinite(volume)) {
            source.sendError(Text.translatable("command.precisevoice.error.not_finite"));
            return 0;
        }

        float maxVolume = config.getMaxVolume();
        if (volume > maxVolume) {
            source.sendError(Text.translatable(
                "command.precisevoice.error.too_high",
                Float.toString(maxVolume)
            ));
            return 0;
        }

        try {
            config.setMultiplier(soundId, volume);
        } catch (IOException | IllegalArgumentException exception) {
            PreciseVoiceClient.LOGGER.error("Could not persist volume for {}", soundId, exception);
            source.sendError(Text.translatable("command.precisevoice.error.save"));
            return 0;
        }

        refreshPlayingSounds();
        source.sendFeedback(Text.translatable(
            "command.precisevoice.success",
            soundId,
            Float.toString(volume),
            Float.toString(maxVolume)
        ));
        return 1;
    }

    private static Set<Identifier> soundIds() {
        Set<Identifier> ids = new TreeSet<>();
        ids.addAll(Registries.SOUND_EVENT.getIds());
        ids.addAll(MinecraftClient.getInstance().getSoundManager().getKeys());
        return ids;
    }

    private static void refreshPlayingSounds() {
        MinecraftClient client = MinecraftClient.getInstance();
        float blockVolume = client.options.getSoundVolume(SoundCategory.BLOCKS);
        client.getSoundManager().updateSoundVolume(SoundCategory.BLOCKS, blockVolume);
    }
}
