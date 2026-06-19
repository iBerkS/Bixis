package com.bixis.bixismod.config;

import com.bixis.bixismod.BixisMod;
import com.bixis.bixismod.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * config/bixis-rates.json dosyasından NPC trade fiyatlarını okur.
 * Dosya yoksa varsayılan değerlerle oluşturur.
 */
public final class BixisRatesConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Rahim Koç — kazanılan TL miktarları
    private static int ironRate        = Constants.DEFAULT_IRON_RATE;
    private static int goldRate        = Constants.DEFAULT_GOLD_RATE;
    private static int emeraldRate     = Constants.DEFAULT_EMERALD_RATE;
    private static int diamondRate     = Constants.DEFAULT_DIAMOND_RATE;
    private static int netheriteRate   = Constants.DEFAULT_NETHERITE_RATE;
    private static int enderEyeRate    = Constants.DEFAULT_ENDER_EYE_RATE;
    private static int beaconRate      = Constants.DEFAULT_BEACON_RATE;
    private static int netherStarRate  = Constants.DEFAULT_NETHER_STAR_RATE;

    // Villa Hakan — ödenen TL miktarları
    private static int fenerbahceFormaPrice       = Constants.DEFAULT_FENERBAHCE_FORMA_PRICE;
    private static int otomatikTufekPrice        = Constants.DEFAULT_OTOMATIK_TUFEK_PRICE;
    private static int yariOtomatikTufekPrice    = Constants.DEFAULT_YARI_OTOMATIK_TUFEK_PRICE;
    private static int tabancaPrice              = Constants.DEFAULT_TABANCA_PRICE;
    private static int mermiPrice                = Constants.DEFAULT_MERMI_PRICE;
    private static int luckyPotionPrice          = Constants.DEFAULT_LUCKY_POTION_PRICE;
    private static int fenerbahceKiliciPrice     = Constants.DEFAULT_FENERBAHCE_KILICI_PRICE;
    private static int tcPasaportuPrice          = Constants.DEFAULT_TC_PASAPORTU_PRICE;

    /**
     * Verilen config dizininden bixis-rates.json'ı yükler.
     * Dosya yoksa varsayılan değerlerle oluşturur.
     *
     * @param configDir Forge config dizini (FMLPaths.CONFIGDIR.get())
     */
    public static void load(Path configDir) {
        Path file = configDir.resolve("bixis-rates.json");
        if (!Files.exists(file)) {
            writeDefaults(file);
            BixisMod.LOGGER.info("bixis-rates.json oluşturuldu: {}", file);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) return;

            // Geriye uyumluluk: eski "rahmi_koc" anahtarı da okunur
            String rahimKey = root.has("rahim_koc") ? "rahim_koc" : "rahmi_koc";
            if (root.has(rahimKey)) {
                JsonObject r = root.getAsJsonObject(rahimKey);
                ironRate       = getInt(r, "iron_ingot_rate",      ironRate);
                goldRate       = getInt(r, "gold_ingot_rate",      goldRate);
                emeraldRate    = getInt(r, "emerald_rate",         emeraldRate);
                diamondRate    = getInt(r, "diamond_rate",         diamondRate);
                netheriteRate  = getInt(r, "netherite_ingot_rate", netheriteRate);
                enderEyeRate   = getInt(r, "eye_of_ender_rate",    enderEyeRate);
                beaconRate     = getInt(r, "beacon_rate",          beaconRate);
                netherStarRate = getInt(r, "nether_star_rate",     netherStarRate);
            }
            // Geriye uyumluluk: eski "killa_hakan" anahtarı da okunur
            String villaKey = root.has("villa_hakan") ? "villa_hakan" : "killa_hakan";
            if (root.has(villaKey)) {
                JsonObject k = root.getAsJsonObject(villaKey);
                fenerbahceFormaPrice   = getInt(k, "fenerbahce_forma_price",      fenerbahceFormaPrice);
                otomatikTufekPrice     = getInt(k, "otomatik_tufek_price",       otomatikTufekPrice);
                yariOtomatikTufekPrice = getInt(k, "yari_otomatik_tufek_price",  yariOtomatikTufekPrice);
                tabancaPrice           = getInt(k, "tabanca_price",              tabancaPrice);
                mermiPrice             = getInt(k, "mermi_price",                mermiPrice);
                luckyPotionPrice       = getInt(k, "lucky_potion_price",         luckyPotionPrice);
                fenerbahceKiliciPrice  = getInt(k, "fenerbahce_kilici_price",    fenerbahceKiliciPrice);
                tcPasaportuPrice       = getInt(k, "tc_pasaportu_price",         tcPasaportuPrice);
            }
            BixisMod.LOGGER.info("bixis-rates.json yüklendi.");
        } catch (IOException e) {
            BixisMod.LOGGER.error("bixis-rates.json okunamadı, varsayılanlar kullanılıyor.", e);
        }
    }

    // --- Rahim Koç getters ---

    /** @return 1 iron_ingot karşılığı kazanılan TL */
    public static int getIronRate()       { return ironRate; }
    /** @return 1 gold_ingot karşılığı kazanılan TL */
    public static int getGoldRate()       { return goldRate; }
    /** @return 1 emerald karşılığı kazanılan TL */
    public static int getEmeraldRate()    { return emeraldRate; }
    /** @return 1 diamond karşılığı kazanılan TL */
    public static int getDiamondRate()    { return diamondRate; }
    /** @return 1 netherite_ingot karşılığı kazanılan TL */
    public static int getNetheriteRate()  { return netheriteRate; }
    /** @return 1 eye_of_ender karşılığı kazanılan TL */
    public static int getEnderEyeRate()   { return enderEyeRate; }
    /** @return 1 beacon karşılığı kazanılan TL */
    public static int getBeaconRate()     { return beaconRate; }
    /** @return 1 nether_star karşılığı kazanılan TL */
    public static int getNetherStarRate() { return netherStarRate; }

    // --- Villa Hakan getters ---

    /** @return fenerbahce_forma fiyatı (TL) */
    public static int getFenerbahceFormaPrice()    { return fenerbahceFormaPrice; }
    /** @return otomatik_tufek fiyatı (TL) */
    public static int getOtomatikTufekPrice()     { return otomatikTufekPrice; }
    /** @return yari_otomatik_tufek fiyatı (TL) */
    public static int getYariOtomatikTufekPrice() { return yariOtomatikTufekPrice; }
    /** @return tabanca fiyatı (TL) */
    public static int getTabancaPrice()           { return tabancaPrice; }
    /** @return 1 TL = 32 mermi fiyatı */
    public static int getMermiPrice()             { return mermiPrice; }
    /** @return lucky_potion fiyatı (TL) */
    public static int getLuckyPotionPrice()       { return luckyPotionPrice; }
    /** @return fenerbahce_kilici fiyatı (TL) */
    public static int getFenerbahceKiliciPrice()  { return fenerbahceKiliciPrice; }
    /** @return tc_pasaportu fiyatı (TL) */
    public static int getTcPasaportuPrice()       { return tcPasaportuPrice; }

    // -------------------------------------------------------------------------

    private static int getInt(JsonObject obj, String key, int fallback) {
        return obj.has(key) ? obj.get(key).getAsInt() : fallback;
    }

    private static void writeDefaults(Path file) {
        JsonObject root = new JsonObject();

        JsonObject rahim = new JsonObject();
        rahim.addProperty("iron_ingot_rate",      Constants.DEFAULT_IRON_RATE);
        rahim.addProperty("gold_ingot_rate",       Constants.DEFAULT_GOLD_RATE);
        rahim.addProperty("emerald_rate",          Constants.DEFAULT_EMERALD_RATE);
        rahim.addProperty("diamond_rate",          Constants.DEFAULT_DIAMOND_RATE);
        rahim.addProperty("netherite_ingot_rate",  Constants.DEFAULT_NETHERITE_RATE);
        rahim.addProperty("eye_of_ender_rate",     Constants.DEFAULT_ENDER_EYE_RATE);
        rahim.addProperty("beacon_rate",           Constants.DEFAULT_BEACON_RATE);
        rahim.addProperty("nether_star_rate",      Constants.DEFAULT_NETHER_STAR_RATE);
        root.add("rahim_koc", rahim);

        JsonObject villa = new JsonObject();
        villa.addProperty("fenerbahce_forma_price",       Constants.DEFAULT_FENERBAHCE_FORMA_PRICE);
        villa.addProperty("otomatik_tufek_price",        Constants.DEFAULT_OTOMATIK_TUFEK_PRICE);
        villa.addProperty("yari_otomatik_tufek_price",   Constants.DEFAULT_YARI_OTOMATIK_TUFEK_PRICE);
        villa.addProperty("tabanca_price",               Constants.DEFAULT_TABANCA_PRICE);
        villa.addProperty("mermi_price",                 Constants.DEFAULT_MERMI_PRICE);
        villa.addProperty("lucky_potion_price",          Constants.DEFAULT_LUCKY_POTION_PRICE);
        villa.addProperty("fenerbahce_kilici_price",     Constants.DEFAULT_FENERBAHCE_KILICI_PRICE);
        villa.addProperty("tc_pasaportu_price",          Constants.DEFAULT_TC_PASAPORTU_PRICE);
        root.add("villa_hakan", villa);

        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            BixisMod.LOGGER.error("bixis-rates.json yazılamadı.", e);
        }
    }

    private BixisRatesConfig() {}
}
