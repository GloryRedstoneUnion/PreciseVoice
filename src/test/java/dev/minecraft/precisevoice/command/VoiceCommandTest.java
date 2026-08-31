package dev.minecraft.precisevoice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoiceCommandTest {
    @Test
    void parsesNamespacedSoundIdentifier() {
        assertParses("voice minecraft:entity.generic.explode 0.3");
    }

    @Test
    void parsesDefaultNamespaceSoundIdentifier() {
        assertParses("voice entity.generic.explode 0.3");
    }

    private static void assertParses(String command) {
        CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
        VoiceCommand.register(dispatcher, null);

        ParseResults<FabricClientCommandSource> result = dispatcher.parse(command, null);

        assertFalse(
            result.getReader().canRead(),
            () -> "Unparsed input: " + result.getReader().getRemaining()
        );
        assertTrue(
            result.getExceptions().isEmpty(),
            () -> "Parse errors: " + result.getExceptions().values()
        );
    }
}
