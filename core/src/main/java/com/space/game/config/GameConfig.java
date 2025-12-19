package com.space.game.config;

public class GameConfig {
  public static final String GAME_VERSION = "v1.2";

  // --- ALIEN DIFFICULTY CALIBRATION (LEVELS 1-9 & INFINITE) ---

  // SPEED (Relative to screen width)
  // Base speed is % of screen width per second ideally.
  public static final float BASE_ALIEN_SPEED_PERCENT = 0.009f; // Increased base speed
  public static final float ALIEN_SPEED_GROWTH_PERCENT = 0.0001f; // +0.1% per level

  // Specific Speeds (Percent of Screen Width)
  public static final float SPEED_LINEAR = 0.010f;
  public static final float SPEED_WAVE_FORWARD = 0.010f;
  public static final float SPEED_WAVE_SINE = 0.010f;
  public static final float SPEED_SPIRAL_APPROACH = 0.007f; // Greatly reduced approach speed
  public static final float SPEED_SPIRAL_ROTATION = 0.009f;

  public static final float ALIEN_ACCELERATION = 0.5f;

  // WAVE SIZE (Total aliens per level)
  public static final int BASE_ENEMY_COUNT = 10; // Reduced from 14
  public static final int ENEMY_COUNT_GROWTH = 2; // Reduced from 6

  // SPAWN DENSITY (How many appear at once on screen)
  public static final int MIN_SPAWN_AT_ONCE = 4; // Increased from 2
  public static final int MIN_SPAWN_AT_ONCE_GROWTH = 2;
  public static final int MAX_SPAWN_AT_ONCE_INITIAL = 8; // Increased from 3
  public static final int SPAWN_AT_ONCE_GROWTH_LEVEL = 1; // More frequent growth

  // MAX ALIENS ON SCREEN THRESHOLD (New feature)
  // "Only spawn more if active count < X"
  public static final int MAX_ACTIVE_ALIENS_BASE = 24; // Increased from 14
  public static final int MAX_ACTIVE_ALIENS_GROWTH = 3; // Per level

  // SPAWN REGIONS PROBABILITY (Levels 1-9)
  public static final float SIDE_SPAWN_CHANCE_INITIAL = 0.9f;
  public static final float SIDE_SPAWN_CHANCE_DECAY = 0.005f;

  // DARK LEVEL NERFS
  public static final float DARK_LEVEL_SPEED_MULTIPLIER = 0.9f;
  public static final float DARK_LEVEL_COUNT_MULTIPLIER = 0.8f;

  // --- BOOMER CONFIGURATION ---
  public static final float BABY_KNOCKBACK_FORCE = 200;
  public static final float BOSS_KNOCKBACK_FORCE = 150;
  public static final int MINIONS_BEFORE_BOSS = 30;

  public static final int BABY_HP = 3;
  public static final int BOSS_HP = 150;
  public static final float BABY_EXPLOSION_RADIUS = 350;
  public static final float BOSS_EXPLOSION_RADIUS = 3000f;
  public static final float BOSS_DEATH_EXPLOSION_RADIUS = 200f;
  public static final float BOSS_DETONATION_DISTANCE = 200f;

  public static final int BOSS_APPEAR_LEVEL = 10;
  public static final int MAX_ENEMIES_ON_BOSS_SCREEN = 25;

  // Spawn Rates for Boss Wave (in seconds)
  public static final float MIN_SPAWN_RATE = 1.0f;
  public static final float MAX_SPAWN_RATE = 2.0f;

  public static final float BABY_BOOMER_SCALE = 1.125f;
  public static final float BOSS_BOOMER_SCALE = 3.0f;

  // Boss Gameplay Config
  public static final float BOSS_MINION_SPEED_BONUS = 70f;
  public static final int BOSS_MINION_SPAWN_COUNT = 9;
  public static final int CHARGED_SHOT_BOSS_DAMAGE = 10;

  public static final float BABY_BOOMER_CHANCE_AFTER_LEVEL_10 = 0.01f; // 1%
  public static final int MAX_BABY_BOOMERS_PER_WAVE = 2;
  public static final int MAX_ACTIVE_BABY_BOOMERS = 1;

  public static final float BABY_BOOMER_SPEED = 0.09f; // Increased speed
  public static final float BOSS_BOOMER_SPEED = 16f; // Increased speed (was 0.03f)

  public static final float BOSS_MINION_ENERGY_GAIN = 7.0f;

  // --- DYNAMIC SPAWNING / PACING CONFIGURATION ---

  // MOMENT DURATIONS (Seconds) - How long each phase lasts
  public static final float DURATION_CALM_MIN = 5.0f;
  public static final float DURATION_CALM_MAX = 8.0f;
  public static final float DURATION_STEADY_MIN = 6.0f;
  public static final float DURATION_STEADY_MAX = 10.0f;
  public static final float DURATION_INTENSE_MIN = 5.0f;
  public static final float DURATION_INTENSE_MAX = 8.0f;

  // SPAWN BATCH SIZES (How many aliens spawn at once per tick)
  public static final int BATCH_CALM_MIN = 1;
  public static final int BATCH_CALM_MAX = 2;
  public static final int BATCH_STEADY_MIN = 2;
  public static final int BATCH_STEADY_MAX = 3;
  public static final int BATCH_INTENSE_MIN = 3;
  public static final int BATCH_INTENSE_MAX = 5;

  // SPAWN COOLDOWNS (Seconds) - Delay between spawn batches
  public static final float COOLDOWN_CALM_MIN = 2.0f;
  public static final float COOLDOWN_CALM_MAX = 3.5f;
  public static final float COOLDOWN_STEADY_MIN = 1.5f;
  public static final float COOLDOWN_STEADY_MAX = 2.5f;
  public static final float COOLDOWN_INTENSE_MIN = 0.8f;
  public static final float COOLDOWN_INTENSE_MAX = 1.5f;

  // GLOBAL CAP
  public static final int ABSOLUTE_MAX_ALIENS_ON_SCREEN = 30;

}
