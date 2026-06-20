package com.bixis.bixismod.config;

/** Minigame harita noktası: konum, bakış yönü ve dimension. */
public record SpawnPoint(double x, double y, double z, float yaw, String dimension) {}
