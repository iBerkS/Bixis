package com.bixis.bixismod.minigame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Minigame state machine singleton.
 * Başlangıç durumu: LOBI.
 * Bkz. MINIGAME_DESIGN.md Bölüm 2.
 */
public final class GameStateManager {

    public static final GameStateManager INSTANCE = new GameStateManager();

    private static final Logger LOGGER = LogManager.getLogger("Bixis");

    private GameState currentState = GameState.LOBI;

    private GameStateManager() {}

    /** @return mevcut oyun durumu */
    public GameState getState() {
        return currentState;
    }

    /** Durumu değiştirir ve konsola log basar. */
    public void setState(GameState next) {
        GameState prev = currentState;
        currentState = next;
        LOGGER.info("[Bixis] {} -> {}", prev, next);
    }
}
