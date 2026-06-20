package com.bixis.bixismod.config;

import com.bixis.bixismod.BixisMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * config/bixis-arena-spawns.json dosyasından arena/PVP başlangıç noktalarını okur/yazar.
 * Format: {"1": {"x":..,"y":..,"z":..,"yaw":..,"dimension":"minecraft:overworld"}, ...}
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.5.
 */
public final class BixisArenaSpawnsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configFile;
    private static final Map<Integer, SpawnPoint> spawns = new HashMap<>();

    /** @param configDir Forge config dizini */
    public static void init(Path configDir) {
        configFile = configDir.resolve("bixis-arena-spawns.json");
        load();
    }

    /** @return belirtilen takımın arena spawn noktası */
    public static Optional<SpawnPoint> getSpawn(int teamNum) {
        return Optional.ofNullable(spawns.get(teamNum));
    }

    /** Belirtilen takımın arena spawn noktasını kaydeder ve dosyaya yazar. */
    public static void setSpawn(int teamNum, double x, double y, double z, float yaw, String dimension) {
        spawns.put(teamNum, new SpawnPoint(x, y, z, yaw, dimension));
        save();
    }

    /** Tüm arena spawn noktalarını siler ve dosyayı günceller. */
    public static void clearAll() {
        spawns.clear();
        save();
    }

    /** @return tüm kayıtlı arena spawn noktaları */
    public static Map<Integer, SpawnPoint> getAll() {
        return java.util.Collections.unmodifiableMap(spawns);
    }

    // -------------------------------------------------------------------------

    public static void loadFrom(Path file) {
        configFile = file;
        load();
    }

    private static void load() {
        spawns.clear();
        if (configFile == null || !Files.exists(configFile)) return;
        try (Reader r = Files.newBufferedReader(configFile)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            for (int i = 1; i <= 4; i++) {
                String key = String.valueOf(i);
                if (!root.has(key)) continue;
                spawns.put(i, parseSpawnPoint(root.getAsJsonObject(key)));
            }
            BixisMod.LOGGER.info("[Bixis] bixis-arena-spawns.json yüklendi ({} takım).", spawns.size());
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-arena-spawns.json okunamadı.", e);
        }
    }

    private static void save() {
        if (configFile == null) return;
        JsonObject root = new JsonObject();
        for (Map.Entry<Integer, SpawnPoint> entry : spawns.entrySet()) {
            root.add(String.valueOf(entry.getKey()), spawnPointToJson(entry.getValue()));
        }
        try (Writer w = Files.newBufferedWriter(configFile)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-arena-spawns.json yazılamadı.", e);
        }
    }

    static SpawnPoint parseSpawnPoint(JsonObject obj) {
        return new SpawnPoint(
            obj.get("x").getAsDouble(),
            obj.get("y").getAsDouble(),
            obj.get("z").getAsDouble(),
            obj.get("yaw").getAsFloat(),
            obj.has("dimension") ? obj.get("dimension").getAsString() : "minecraft:overworld"
        );
    }

    static JsonObject spawnPointToJson(SpawnPoint sp) {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", sp.x());
        obj.addProperty("y", sp.y());
        obj.addProperty("z", sp.z());
        obj.addProperty("yaw", sp.yaw());
        obj.addProperty("dimension", sp.dimension());
        return obj;
    }

    private BixisArenaSpawnsConfig() {}
}
