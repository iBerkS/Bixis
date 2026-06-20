package com.bixis.bixismod.config;

import com.bixis.bixismod.BixisMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * config/bixis-checkpoints.json dosyasından takım başına checkpoint listelerini okur/yazar.
 * Format: {"1": [{"x":..,"y":..,"z":..,"yaw":..,"dimension":..}, ...], "2": [...], ...}
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.3.
 */
public final class BixisCheckpointsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configFile;
    private static final Map<Integer, List<SpawnPoint>> checkpoints = new HashMap<>();

    /** @param configDir Forge config dizini */
    public static void init(Path configDir) {
        configFile = configDir.resolve("bixis-checkpoints.json");
        load();
    }

    /**
     * Belirtilen takımın checkpoint listesini döner (1-indexed eklenme sırası).
     *
     * @param teamNum 1-4 arası takım numarası
     */
    public static List<SpawnPoint> getCheckpoints(int teamNum) {
        return Collections.unmodifiableList(checkpoints.getOrDefault(teamNum, List.of()));
    }

    /**
     * Belirtilen takımın listesine yeni bir checkpoint ekler.
     * Otomatik sıra numarası alır (listenin sonuna eklenir).
     */
    public static int addCheckpoint(int teamNum, double x, double y, double z, float yaw, String dimension) {
        checkpoints.computeIfAbsent(teamNum, k -> new ArrayList<>())
            .add(new SpawnPoint(x, y, z, yaw, dimension));
        save();
        return checkpoints.get(teamNum).size(); // dönen değer: yeni sıra numarası (1-based)
    }

    /**
     * Belirtilen takımın belirtilen sıra numaralı checkpoint'ini siler.
     * Sonraki checkpoint'ler otomatik olarak kaydırılır.
     *
     * @param teamNum  1-4 arası takım numarası
     * @param indexOneBased 1-based sıra numarası
     * @return true silindi, false geçersiz sıra numarası
     */
    public static boolean removeCheckpoint(int teamNum, int indexOneBased) {
        List<SpawnPoint> list = checkpoints.get(teamNum);
        if (list == null || indexOneBased < 1 || indexOneBased > list.size()) return false;
        list.remove(indexOneBased - 1);
        save();
        return true;
    }

    /** Tüm takımların tüm checkpoint'lerini siler ve dosyayı günceller. */
    public static void clearAll() {
        checkpoints.clear();
        save();
    }

    // -------------------------------------------------------------------------

    public static void loadFrom(Path file) {
        configFile = file;
        load();
    }

    private static void load() {
        checkpoints.clear();
        if (configFile == null || !Files.exists(configFile)) return;
        try (Reader r = Files.newBufferedReader(configFile)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            for (int i = 1; i <= 4; i++) {
                String key = String.valueOf(i);
                if (!root.has(key)) continue;
                JsonArray arr = root.getAsJsonArray(key);
                List<SpawnPoint> list = new ArrayList<>();
                for (int j = 0; j < arr.size(); j++) {
                    list.add(BixisArenaSpawnsConfig.parseSpawnPoint(arr.get(j).getAsJsonObject()));
                }
                checkpoints.put(i, list);
            }
            int total = checkpoints.values().stream().mapToInt(List::size).sum();
            BixisMod.LOGGER.info("[Bixis] bixis-checkpoints.json yüklendi ({} checkpoint).", total);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-checkpoints.json okunamadı.", e);
        }
    }

    private static void save() {
        if (configFile == null) return;
        JsonObject root = new JsonObject();
        for (Map.Entry<Integer, List<SpawnPoint>> entry : checkpoints.entrySet()) {
            JsonArray arr = new JsonArray();
            for (SpawnPoint sp : entry.getValue()) {
                arr.add(BixisArenaSpawnsConfig.spawnPointToJson(sp));
            }
            root.add(String.valueOf(entry.getKey()), arr);
        }
        try (Writer w = Files.newBufferedWriter(configFile)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-checkpoints.json yazılamadı.", e);
        }
    }

    private BixisCheckpointsConfig() {}
}
