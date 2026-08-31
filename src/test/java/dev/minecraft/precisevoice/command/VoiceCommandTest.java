package dev.minecraft.precisevoice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import dev.minecraft.precisevoice.config.VoiceConfigManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoiceCommandTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void parsesNamespacedSoundIdentifier() {
        assertParses("voice minecraft:entity.generic.explode 0.3");
    }

    @Test
    void parsesDefaultNamespaceSoundIdentifier() {
        assertParses("voice entity.generic.explode 0.3");
    }

    @Test
    void parsesAllSoundVolume() {
        assertParses("voice all 0.3");
    }

    @Test
    void parsesStatusWithoutVolume() {
        assertParses("voice status");
    }

    @Test
    void statusListsAllAndIndividualSoundVolumes() throws Exception {
        VoiceConfigManager config = VoiceConfigManager.load(
            temporaryDirectory.resolve("PreciseVoice.json")
        );
        config.setAllMultiplier(0.5F);
        config.setMultiplier(new Identifier("minecraft:entity.generic.explode"), 2.0F);
        List<Text> feedback = new ArrayList<>();
        FabricClientCommandSource source = recordingSource(feedback);
        CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
        VoiceCommand.register(dispatcher, config);

        int result = dispatcher.execute("voice status", source);

        assertEquals(2, result);
        assertEquals(3, feedback.size());
        assertTranslation(
            feedback.get(0),
            "command.precisevoice.status.header",
            "3.0"
        );
        assertTranslation(
            feedback.get(1),
            "command.precisevoice.status.all",
            "0.5"
        );
        assertTranslation(
            feedback.get(2),
            "command.precisevoice.status.entry",
            "minecraft:entity.generic.explode",
            "2.0"
        );
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

    private static FabricClientCommandSource recordingSource(List<Text> feedback) {
        return (FabricClientCommandSource) Proxy.newProxyInstance(
            FabricClientCommandSource.class.getClassLoader(),
            new Class<?>[] {FabricClientCommandSource.class},
            (proxy, method, args) -> {
                if (method.getName().equals("sendFeedback")) {
                    feedback.add((Text) args[0]);
                    return null;
                }
                if (method.getReturnType() == boolean.class) {
                    return false;
                }
                if (method.getReturnType() == int.class) {
                    return 0;
                }
                return null;
            }
        );
    }

    private static void assertTranslation(Text text, String key, Object... args) {
        TranslatableTextContent content = assertInstanceOf(
            TranslatableTextContent.class,
            text.getContent()
        );
        assertEquals(key, content.getKey());
        assertArrayEquals(args, content.getArgs());
    }
}
