package dev.minecraft.precisevoice.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public final class VoiceConfigManager {
    public static final float DEFAULT_MAX_VOLUME = 3.0F;

    private static final Logger LOGGER = LoggerFactory.getLogger("PreciseVoice/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;
    private volatile Snapshot snapshot;

    private VoiceConfigManager(Path path, Snapshot snapshot) {
        this.path = path;
        this.snapshot = snapshot;
    }

    public static VoiceConfigManager load(Path path) {
        Snapshot defaults = new Snapshot(DEFAULT_MAX_VOLUME, Map.of());
        VoiceConfigManager manager = new VoiceConfigManager(path, defaults);

        if (Files.notExists(path)) {
            try {
                manager.write(defaults);
            } catch (IOException exception) {
                LOGGER.error("Could not create config file {}", path, exception);
            }
            return manager;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Snapshot loaded = parse(JsonParser.parseReader(reader));
            manager.snapshot = loaded;
            manager.write(loaded);
        } catch (Exception exception) {
            LOGGER.error("Could not read config file {}; using defaults", path, exception);
        }

        return manager;
    }

    public float getMaxVolume() {
        return snapshot.maxVolume();
    }

    public float getMultiplier(Identifier soundId) {
        return snapshot.volumes().getOrDefault(soundId.toString(), 1.0F);
    }

    public synchronized void setMultiplier(Identifier soundId, float multiplier) throws IOException {
        Snapshot current = snapshot;
        if (!Float.isFinite(multiplier) || multiplier < 0.0F || multiplier > current.maxVolume()) {
            throw new IllegalArgumentException("Volume is outside the configured range");
        }

        Map<String, Float> updatedVolumes = new TreeMap<>(current.volumes());
        updatedVolumes.put(soundId.toString(), multiplier);
        Snapshot updated = new Snapshot(current.maxVolume(), updatedVolumes);
        write(updated);
        snapshot = updated;
    }

    private static Snapshot parse(JsonElement rootElement) {
        if (!rootElement.isJsonObject()) {
            throw new IllegalArgumentException("Config root must be a JSON object");
        }

        JsonObject root = rootElement.getAsJsonObject();
        float maxVolume = root.has("maxVolume")
            ? root.get("maxVolume").getAsFloat()
            : DEFAULT_MAX_VOLUME;
        if (!Float.isFinite(maxVolume) || maxVolume < 0.0F) {
            throw new IllegalArgumentException("maxVolume must be a finite, non-negative number");
        }

        Map<String, Float> volumes = new TreeMap<>();
        if (root.has("volumes")) {
            JsonObject volumeObject = root.getAsJsonObject("volumes");
            for (Map.Entry<String, JsonElement> entry : volumeObject.entrySet()) {
                Identifier id = Identifier.tryParse(entry.getKey());
                float volume = entry.getValue().getAsFloat();
                if (id == null || !Float.isFinite(volume) || volume < 0.0F) {
                    LOGGER.warn("Ignoring invalid volume entry {}", entry.getKey());
                    continue;
                }
                volumes.put(id.toString(), Math.min(volume, maxVolume));
            }
        }

        return new Snapshot(maxVolume, volumes);
    }

    private void write(Snapshot value) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        JsonObject root = new JsonObject();
        root.addProperty("maxVolume", value.maxVolume());
        JsonObject volumes = new JsonObject();
        value.volumes().forEach(volumes::addProperty);
        root.add("volumes", volumes);

        Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }

        try {
            Files.move(
                temporaryPath,
                path,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException atomicMoveFailure) {
            Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record Snapshot(float maxVolume, Map<String, Float> volumes) {
        private Snapshot {
            volumes = Collections.unmodifiableMap(new TreeMap<>(volumes));
        }
    }
}
