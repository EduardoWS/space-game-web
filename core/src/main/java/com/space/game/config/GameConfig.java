package com.space.game.config;

public class GameConfig {
  public static final String GAME_VERSION = "v1.2";

  // Boomer Configuration
  public static final float BOOMER_KNOCKBACK_FORCE = 450f; // Force of the push back
  public static final int BABY_HP = 7;
  public static final int BOSS_HP = 50;
  public static final float BABY_EXPLOSION_RADIUS = 150f;
  public static final float BOSS_EXPLOSION_RADIUS = 400f;
  public static final int BOSS_APPEAR_LEVEL = 2;
  public static final int MAX_ENEMIES_ON_BOSS_SCREEN = 15;

  // Spawn Rates for Boss Wave (in seconds between spawns)
  public static final float MIN_SPAWN_RATE = 0.5f; // Fast spawn (Low Boss HP)
  public static final float MAX_SPAWN_RATE = 2.0f; // Slow spawn (High Boss HP)

  public static final float BABY_BOOMER_SCALE = 1.125f; // Target 80x80 (if texture is 80x80)
  public static final float BOSS_BOOMER_SCALE = 3.0f; // Giant

}
