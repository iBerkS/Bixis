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
 * config/bixis-race-spawns.json dosyasından yarış başlangıç noktalarını okur/yazar.
 * Format: {"1": {"x":..,"y":..,"z":..,"yaw":..,"dimension":"minecraft:overworld"}, ...}
 * Bkz. MINIGAME_DESIGN.md Bölüm 5.
 */
public final class BixisRaceSpawnsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configFile;
    private static final Map<Integer, SpawnPoint> spawns = new HashMap<>();

    /** Bir takımın yarış başlangıç noktası. */
    public record SpawnPoint(double x, double y, double z, float yaw, String dimension) {}

    /**
     * Config dosyasını başlatır ve varsa yükler.
     *
     * @param configDir Forge config dizini (FMLPaths.CONFIGDIR.get())
     */
    public static void init(Path configDir) {
        configFile = configDir.resolve("bixis-race-spawns.json");
        load();
    }

    /**
     * Belirtilen takımın spawn noktasını döner.
     *
     * @param teamNum 1-4 arası takım numarası
     */
    public static Optional<SpawnPoint> getSpawn(int teamNum) {
        return Optional.ofNullable(spawns.get(teamNum));
    }

    /**
     * Belirtilen takımın spawn noktasını kaydeder ve dosyaya yazar.
     *
     * @param teamNum 1-4 arası takım numarası
     */
    public static void setSpawn(int teamNum, double x, double y, double z, float yaw, String dimension) {
        spawns.put(teamNum, new SpawnPoint(x, y, z, yaw, dimension));
        save();
        BixisMod.LOGGER.info("[Bixis] Takım {} race spawn kaydedildi: {:.1f} {:.1f} {:.1f} yaw={:.1f} dim={}",
            teamNum, x, y, z, yaw, dimension);
    }

    // -------------------------------------------------------------------------

    private static void load() {
        spawns.clear();
        if (!Files.exists(configFile)) return;
        try (Reader r = Files.newBufferedReader(configFile)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            for (int i = 1; i <= 4; i++) {
                String key = String.valueOf(i);
                if (!root.has(key)) continue;
                JsonObject sp = root.getAsJsonObject(key);
                spawns.put(i, new SpawnPoint(
                    sp.get("x").getAsDouble(),
                    sp.get("y").getAsDouble(),
                    sp.get("z").getAsDouble(),
                    sp.get("yaw").getAsFloat(),
                    sp.has("dimension") ? sp.get("dimension").getAsString() : "minecraft:overworld"
                ));
            }
            BixisMod.LOGGER.info("[Bixis] bixis-race-spawns.json yüklendi ({} takım).", spawns.size());
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-race-spawns.json okunamadı.", e);
        }
    }

    private static void save() {
        if (configFile == null) return;
        JsonObject root = new JsonObject();
        for (Map.Entry<Integer, SpawnPoint> entry : spawns.entrySet()) {
            SpawnPoint sp = entry.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("x", sp.x());
            obj.addProperty("y", sp.y());
            obj.addProperty("z", sp.z());
            obj.addProperty("yaw", sp.yaw());
            obj.addProperty("dimension", sp.dimension());
            root.add(String.valueOf(entry.getKey()), obj);
        }
        try (Writer w = Files.newBufferedWriter(configFile)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-race-spawns.json yazılamadı.", e);
        }
    }

    private BixisRaceSpawnsConfig() {}
}
