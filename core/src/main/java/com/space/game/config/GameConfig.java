package com.space.game.config;

public class GameConfig {
  public static final String GAME_VERSION = "v1.3";

  // ============================================================================
  // SPACESHIP CONFIGURATION
  // ============================================================================

  public static final float SPACESHIP_ROTATION_SPEED = 160f; // Default/Start speed
  // Passive Energy Growth
  public static final float PASSIVE_ENERGY_PER_LEVEL_PERCENT = 0.05f; // +5% energy capacity per level
  public static final float PASSIVE_ENERGY_PER_KILL_PERCENT = 0.0025f; // +0.25% energy capacity per alien kill
  // public static final float SPACESHIP_ROTATION_SPEED_FAST = 140f; // Max speed
  // after delay
  // public static final float ROTATION_ACCEL_DELAY = 1.0f; // Seconds needed to
  // hold key to accelerate

  // ============================================================================
  // ALIEN SPEED CONFIGURATION
  // ============================================================================

  // Base speeds for different movement patterns (as percentage of screen width)
  public static final float SPEED_LINEAR = 0.010f;
  public static final float SPEED_WAVE_FORWARD = 0.010f;
  public static final float SPEED_SPIRAL_APPROACH = 0.007f;

  // Speed progression across levels
  public static final float ALIEN_SPEED_GROWTH_RATE = 0.005f; // 0.5% increase per level

  // Special level speed modifiers
  public static final float DARK_LEVEL_SPEED_MULTIPLIER = 0.9f; // 10% slower in dark levels
  public static final float BOSS_LEVEL_SPEED_MULTIPLIER = 1.2f; // Boss minions 20% faster
  public static final float CHALLENGE_LEVEL_SPEED_MULTIPLIER = 1.0f; // Same speed, just more enemies

  // ============================================================================
  // WAVE CONFIGURATION (Enemy Count)
  // ============================================================================

  public static final int BASE_ENEMY_COUNT = 10;
  public static final int ENEMY_COUNT_GROWTH = 2; // Additional aliens per level

  // Dark level modifier
  public static final float DARK_LEVEL_COUNT_MULTIPLIER = 0.8f; // 20% fewer aliens in dark levels

  // ============================================================================
  // SPAWN SYSTEM CONFIGURATION
  // ============================================================================

  // Max aliens on screen
  public static final int MAX_ACTIVE_ALIENS_BASE = 7; // Starting number of simultaneous aliens (level 1)
  public static final int MAX_ACTIVE_ALIENS_GROWTH = 1; // How many slots to add when increasing
  public static final int ALIENS_GROWTH_EVERY_N_LEVELS = 2; // Increase every N levels (2 = every 2 levels)
  public static final int ABSOLUTE_MAX_ALIENS_ON_SCREEN = 42; // Hard cap - never exceeds this

  // Spawn location probability
  public static final float SIDE_SPAWN_CHANCE_INITIAL = 0.9f; // 90% spawn from sides initially
  public static final float SIDE_SPAWN_CHANCE_DECAY = 0.005f; // Decreases as levels progress

  // Dynamic pacing system - Moment durations (seconds)
  public static final float DURATION_CALM_MIN = 5.0f;
  public static final float DURATION_CALM_MAX = 8.0f;
  public static final float DURATION_STEADY_MIN = 6.0f;
  public static final float DURATION_STEADY_MAX = 10.0f;
  public static final float DURATION_INTENSE_MIN = 5.0f;
  public static final float DURATION_INTENSE_MAX = 8.0f;

  // Spawn batch sizes (how many aliens spawn at once)
  public static final int BATCH_CALM_MIN = 1;
  public static final int BATCH_CALM_MAX = 2;
  public static final int BATCH_STEADY_MIN = 2;
  public static final int BATCH_STEADY_MAX = 3;
  public static final int BATCH_INTENSE_MIN = 3;
  public static final int BATCH_INTENSE_MAX = 5;

  // Spawn cooldowns (seconds between spawn batches)
  public static final float COOLDOWN_CALM_MIN = 2.0f;
  public static final float COOLDOWN_CALM_MAX = 3.5f;
  public static final float COOLDOWN_STEADY_MIN = 1.5f;
  public static final float COOLDOWN_STEADY_MAX = 2.5f;
  public static final float COOLDOWN_INTENSE_MIN = 0.8f;
  public static final float COOLDOWN_INTENSE_MAX = 1.5f;

  // ============================================================================
  // BOSS CONFIGURATION
  // ============================================================================

  public static final int BOSS_APPEAR_LEVEL = 10;

  // Boss stats
  public static final int BOSS_HP = 150; // Base HP at level 10
  public static final int BOSS_HEALTH_GROWTH = 25; // +25 HP per boss level encounter
  public static final float BOSS_BOOMER_SCALE = 4f;
  public static final float BOSS_BOOMER_SPEED = 16f;

  // Boss Animation
  public static final int BOSS_TILE_WIDTH = 80;
  public static final int BOSS_TILE_HEIGHT = 80;
  public static final float BOSS_FRAME_DURATION = 0.2f; // 0.3s per frame

  // Boss behavior
  public static final int BOSS_REST_HP_THRESHOLD = 40; // Rests every 40 HP lost
  public static final float BOSS_REST_DURATION = 4.0f; // Rest duration in seconds

  // Boss explosion effects
  public static final float BOSS_EXPLOSION_RADIUS = 3000f;
  public static final float BOSS_DEATH_EXPLOSION_RADIUS = 250f;
  public static final float BOSS_DETONATION_DISTANCE = 250f;
  public static final float BOSS_KNOCKBACK_FORCE = 150f;
  public static final float BOSS_CHARGED_KNOCKBACK_FORCE = 600f;

  // Boss wave configuration
  public static final int MINIONS_BEFORE_BOSS = 40; // Minions to spawn before boss appears
  public static final int MAX_ENEMIES_ON_BOSS_SCREEN = 25;
  public static final int BOSS_MINION_SPAWN_COUNT = 9;

  // Boss spawn rates (seconds)
  public static final float MIN_SPAWN_RATE = 1.0f; // Fastest spawn rate (low HP)
  public static final float MAX_SPAWN_RATE = 2.0f; // Slowest spawn rate (high HP)

  // Combat rewards
  public static final int CHARGED_SHOT_BOSS_DAMAGE = 10;
  public static final float BOSS_MINION_ENERGY_GAIN = 7.0f;
  public static final float BOSS_REWARD_ENERGY_PERCENT = 0.25f; // +25% Max Energy reward

  // ============================================================================
  // BABY BOOMER CONFIGURATION
  // ============================================================================

  public static final int BABY_HP = 3;
  public static final float BABY_BOOMER_SCALE = 2f;
  public static final float BABY_BOOMER_SPEED = 20f;

  // Baby Boomer explosion
  public static final float BABY_EXPLOSION_RADIUS = 400f;
  public static final float BABY_KNOCKBACK_FORCE = 200f;

  // Baby Boomer spawn chances (after level 10)
  public static final float BABY_BOOMER_CHANCE_AFTER_LEVEL_10 = 0.01f; // 1% chance
  public static final int MAX_BABY_BOOMERS_PER_WAVE = 2;
  public static final int MAX_ACTIVE_BABY_BOOMERS = 1;

  // ============================================================================
  // DEPRECATED / UNUSED CONFIGURATION
  // ============================================================================
  // These are kept for backward compatibility but are no longer actively used

  @Deprecated
  public static final float BASE_ALIEN_SPEED_PERCENT = 0.009f; // Use SPEED_LINEAR instead

  @Deprecated
  public static final float ALIEN_SPEED_GROWTH_PERCENT = 0.0001f; // Use ALIEN_SPEED_GROWTH_RATE instead

  @Deprecated
  public static final float SPEED_WAVE_SINE = 0.010f; // Not used

  @Deprecated
  public static final float SPEED_SPIRAL_ROTATION = 0.009f; // Not used

  @Deprecated
  public static final float ALIEN_ACCELERATION = 0.5f; // Not used

  @Deprecated
  public static final int MIN_SPAWN_AT_ONCE = 4; // Use BATCH_* instead

  @Deprecated
  public static final int MIN_SPAWN_AT_ONCE_GROWTH = 2; // Use BATCH_* instead

  @Deprecated
  public static final int MAX_SPAWN_AT_ONCE_INITIAL = 8; // Use BATCH_* instead

  @Deprecated
  public static final int SPAWN_AT_ONCE_GROWTH_LEVEL = 1; // Use BATCH_* instead

  @Deprecated
  public static final float BOSS_MINION_SPEED_BONUS = 70f; // Use BOSS_LEVEL_SPEED_MULTIPLIER instead
}
