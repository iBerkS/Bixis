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

/**
 * config/bixis-race-settings.json — yarış süresi, PVP süresi ve diğer ayarlar.
 * Format: {"race_time_minutes": 15, "pvp_time_minutes": 3}
 * Bkz. MINIGAME_DESIGN.md Bölüm 3.3 ve 3.6.
 */
public final class BixisRaceSettingsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_RACE_TIME_MINS = 15;
    private static final int DEFAULT_PVP_TIME_MINS  = 3;

    private static Path configFile;
    private static int raceTimeMins = DEFAULT_RACE_TIME_MINS;
    private static int pvpTimeMins  = DEFAULT_PVP_TIME_MINS;

    /** @param configDir Forge config dizini */
    public static void init(Path configDir) {
        configFile = configDir.resolve("bixis-race-settings.json");
        load();
    }

    // ── Yarış süresi ─────────────────────────────────────────────────────────

    /** @return yarış süresi saniye cinsinden */
    public static int getRaceTimeSecs() { return raceTimeMins * 60; }

    /** @return yarış süresi dakika cinsinden */
    public static int getRaceTimeMins() { return raceTimeMins; }

    /** Yarış süresini günceller ve dosyaya yazar. */
    public static void setRaceTimeMins(int mins) {
        raceTimeMins = Math.max(1, mins);
        save();
    }

    // ── PVP süresi ───────────────────────────────────────────────────────────

    /** @return PVP (kapışma) süresi saniye cinsinden */
    public static int getPvpTimeSecs() { return pvpTimeMins * 60; }

    /** @return PVP (kapışma) süresi dakika cinsinden */
    public static int getPvpTimeMins() { return pvpTimeMins; }

    /** PVP süresini günceller ve dosyaya yazar. */
    public static void setPvpTimeMins(int mins) {
        pvpTimeMins = Math.max(1, mins);
        save();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static void load() {
        if (configFile == null || !Files.exists(configFile)) {
            save();
            return;
        }
        try (Reader r = Files.newBufferedReader(configFile)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;
            if (root.has("race_time_minutes")) raceTimeMins = root.get("race_time_minutes").getAsInt();
            if (root.has("pvp_time_minutes"))  pvpTimeMins  = root.get("pvp_time_minutes").getAsInt();
            BixisMod.LOGGER.info("[Bixis] bixis-race-settings.json yüklendi (yarış {}dk, pvp {}dk).",
                raceTimeMins, pvpTimeMins);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-race-settings.json okunamadı.", e);
        }
    }

    private static void save() {
        if (configFile == null) return;
        JsonObject root = new JsonObject();
        root.addProperty("race_time_minutes", raceTimeMins);
        root.addProperty("pvp_time_minutes",  pvpTimeMins);
        try (Writer w = Files.newBufferedWriter(configFile)) {
            GSON.toJson(root, w);
        } catch (IOException e) {
            BixisMod.LOGGER.error("[Bixis] bixis-race-settings.json yazılamadı.", e);
        }
    }

    private BixisRaceSettingsConfig() {}
}
