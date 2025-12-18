package com.space.game.config;

public class GameConfig {
  public static final String GAME_VERSION = "v1.2";

  // Boomer Configuration
  public static final float BABY_KNOCKBACK_FORCE = 500f; // Lighter -> Higher Force
  public static final float BOSS_KNOCKBACK_FORCE = 400; // Heavier -> Lower Force
  public static final int MINIONS_BEFORE_BOSS = 30;

  public static final int BABY_HP = 7;
  public static final int BOSS_HP = 50;
  public static final float BABY_EXPLOSION_RADIUS = 150f;
  public static final float BOSS_EXPLOSION_RADIUS = 3000f;
  public static final float BOSS_DETONATION_DISTANCE = 200f; // New Config

  public static final int BOSS_APPEAR_LEVEL = 10;
  public static final int MAX_ENEMIES_ON_BOSS_SCREEN = 15;

  // Spawn Rates for Boss Wave (in seconds between spawns)
  public static final float MIN_SPAWN_RATE = 2.0f; // Fast spawn (Low Boss HP)
  public static final float MAX_SPAWN_RATE = 5.0f; // Slow spawn (High Boss HP)

  public static final float BABY_BOOMER_SCALE = 1.125f; // Target 80x80 (if texture is 80x80)
  public static final float BOSS_BOOMER_SCALE = 3.0f; // Giant

  // Boss Gameplay Config
  public static final float BOSS_MINION_SPEED_BONUS = 70f; // Extra speed for minions during boss
  public static final int BOSS_MINION_SPAWN_COUNT = 7; // Number of minions to spawn at once in intro
  public static final int CHARGED_SHOT_BOSS_DAMAGE = 10;

  public static final float BABY_BOOMER_SPEED = 30f;
  public static final float BOSS_BOOMER_SPEED = 20f; // Slower than normal

  public static final float BOSS_MINION_ENERGY_GAIN = 5.0f; // Energy gain per minion kill in boss phase

}
