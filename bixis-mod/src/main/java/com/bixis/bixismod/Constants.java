package com.bixis.bixismod;

/** Mod genelinde kullanılan sabit değerler. Magic number kullanma; buraya ekle. */
public final class Constants {

    // --- Döviz varsayılan oranları (config yoksa kullanılır) ---
    public static final int DEFAULT_IRON_RATE        = 1;
    public static final int DEFAULT_GOLD_RATE        = 1;
    public static final int DEFAULT_EMERALD_RATE     = 1;
    public static final int DEFAULT_DIAMOND_RATE     = 2;
    public static final int DEFAULT_NETHERITE_RATE   = 2;
    public static final int DEFAULT_ENDER_EYE_RATE   = 2;
    public static final int DEFAULT_BEACON_RATE      = 5;
    public static final int DEFAULT_NETHER_STAR_RATE = 5;

    // --- Killa Hakan varsayılan fiyatlar (TL) ---
    public static final int DEFAULT_FENERBAHCE_FORMA_PRICE    = 15;
    public static final int DEFAULT_OTOMATIK_TUFEK_PRICE     = 10;
    public static final int DEFAULT_YARI_OTOMATIK_TUFEK_PRICE = 10;
    public static final int DEFAULT_TABANCA_PRICE            = 7;
    public static final int DEFAULT_MERMI_PRICE              = 1;
    public static final int DEFAULT_LUCKY_POTION_PRICE       = 1;
    public static final int DEFAULT_FENERBAHCE_KILICI_PRICE  = 15;
    public static final int DEFAULT_TC_PASAPORTU_PRICE       = 3;

    // --- Trade miktarları ---
    public static final int MERMI_PER_TRADE         = 32;
    public static final int LUCKY_POTION_PER_TRADE  = 3;
    public static final int TRADE_MAX_USES          = Integer.MAX_VALUE;
    public static final int TRADE_XP                = 0;
    public static final float TRADE_PRICE_MULTIPLIER = 0f;

    // --- Entity boyutları (Steve iskelet) ---
    public static final float NPC_WIDTH  = 0.6f;
    public static final float NPC_HEIGHT = 1.95f;

    // --- TC Pasaportu efekt süreleri ---
    public static final int TC_PASAPORTU_EFFECT_TICKS   = 300; // 15 saniye
    public static final int TC_PASAPORTU_SPEED_AMP      = 1;   // Speed II (0-indexed)
    public static final int TC_PASAPORTU_JUMP_AMP       = 1;   // Jump Boost II
    public static final int TC_PASAPORTU_REGEN_AMP      = 0;   // Regeneration I

    // --- Render ---
    public static final float NPC_SHADOW_RADIUS = 0.5f;
    public static final float NPC_LOOK_RANGE    = 8.0f;

    private Constants() {}
}
