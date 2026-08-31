package dev.minecraft.precisevoice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.minecraft.precisevoice.PreciseVoiceClient;
import dev.minecraft.precisevoice.config.VoiceConfigManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;
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
            .then(literal("status")
                .executes(context -> showStatus(context.getSource(), config)))
            .then(literal("all")
                .then(argument("volume", FloatArgumentType.floatArg(0.0F))
                    .executes(context -> setAllVolume(
                        context.getSource(),
                        FloatArgumentType.getFloat(context, "volume"),
                        config
                    ))))
            .then(argument("type", IdentifierArgumentType.identifier())
                .suggests((context, builder) -> CommandSource.suggestIdentifiers(soundIds(), builder))
                .then(argument("volume", FloatArgumentType.floatArg(0.0F))
                    .executes(context -> setVolume(
                        context.getSource(),
                        context.getArgument("type", Identifier.class),
                        FloatArgumentType.getFloat(context, "volume"),
                        config
                    ))))
        );
    }

    private static int showStatus(
        FabricClientCommandSource source,
        VoiceConfigManager config
    ) {
        source.sendFeedback(Text.translatable(
            "command.precisevoice.status.header",
            Float.toString(config.getMaxVolume())
        ));
        source.sendFeedback(Text.translatable(
            "command.precisevoice.status.all",
            Float.toString(config.getAllMultiplier())
        ));

        Map<String, Float> soundMultipliers = config.getSoundMultipliers();
        if (soundMultipliers.isEmpty()) {
            source.sendFeedback(Text.translatable("command.precisevoice.status.none"));
        } else {
            soundMultipliers.forEach((soundId, multiplier) -> source.sendFeedback(Text.translatable(
                "command.precisevoice.status.entry",
                soundId,
                Float.toString(multiplier)
            )));
        }
        return soundMultipliers.size() + 1;
    }

    private static int setAllVolume(
        FabricClientCommandSource source,
        float volume,
        VoiceConfigManager config
    ) {
        if (!validateVolume(source, volume, config)) {
            return 0;
        }

        try {
            config.setAllMultiplier(volume);
        } catch (IOException | IllegalArgumentException exception) {
            PreciseVoiceClient.LOGGER.error("Could not persist all-sound volume", exception);
            source.sendError(Text.translatable("command.precisevoice.error.save"));
            return 0;
        }

        refreshPlayingSounds();
        source.sendFeedback(Text.translatable(
            "command.precisevoice.success.all",
            Float.toString(volume),
            Float.toString(config.getMaxVolume())
        ));
        return 1;
    }

    private static int setVolume(
        FabricClientCommandSource source,
        Identifier soundId,
        float volume,
        VoiceConfigManager config
    ) {
        if (!soundIds().contains(soundId)) {
            source.sendError(Text.translatable("command.precisevoice.error.unknown_sound", soundId));
            return 0;
        }
        if (!validateVolume(source, volume, config)) {
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
            Float.toString(config.getMaxVolume())
        ));
        return 1;
    }

    private static boolean validateVolume(
        FabricClientCommandSource source,
        float volume,
        VoiceConfigManager config
    ) {
        if (!Float.isFinite(volume)) {
            source.sendError(Text.translatable("command.precisevoice.error.not_finite"));
            return false;
        }

        float maxVolume = config.getMaxVolume();
        if (volume > maxVolume) {
            source.sendError(Text.translatable(
                "command.precisevoice.error.too_high",
                Float.toString(maxVolume)
            ));
            return false;
        }
        return true;
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
