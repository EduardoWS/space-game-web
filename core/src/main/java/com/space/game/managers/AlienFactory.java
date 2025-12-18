package com.space.game.managers;

import com.badlogic.gdx.math.Vector2;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.graphics.TextureManager;
import com.space.game.entities.enemies.BoomerAlien;
import com.space.game.entities.enemies.NormalAlien;
import com.space.game.entities.movements.*;

public class AlienFactory {
  public static Alien createAlien(TextureManager textureManager, Vector2 position, float scale, float speed,
      Spaceship spaceship, int movementPattern) {
    MovementStrategy strategy;

    // Vary size for "common" aliens (NormalAlien)
    // Target base logic: Default scale multiplier was specific to pattern (1.45 for
    // linear, 1.15 for wave/spiral)
    // User wants final size variation.
    // Let's modify the `scale` passed to the constructor.
    // User wants 86x86 to 94x94 range.
    // If base sprite is 64x64 (Linear) -> 1.45 scale = 92.8
    // If base sprite is 80x80 (Wave/Spiral) -> 1.15 scale = 92
    // So 92 is the "mean".
    // 86 is -6px (~ -7% scale), 94 is +2px (~ +2% scale).
    // Let's implement a randomized variation factor.

    float sizeVariation = 1.0f;
    // Only apply to patterns 0,1,2 (Normal Aliens)
    if (movementPattern >= 0 && movementPattern <= 2) {
      // Gaussian random: mean 0.0, std dev 1.0
      // We want mostly near 1.0, with some outliers.
      // Let's use MathUtils.randomTriangular or similar if available, or clamp
      // Gaussian.
      // Range 86 to 94 is roughly +/- 4% from 92 (actually -6% to +2%).
      // Let's do random(0.93f, 1.02f) but favoring 1.0.
      // Triangular distribution: min 0.93, max 1.02, mode 1.0
      sizeVariation = com.badlogic.gdx.math.MathUtils.randomTriangular(0.93f, 1.02f, 1.0f);
    }

    float variedScale = scale * sizeVariation;

    // Determine Strategy and Type
    switch (movementPattern) {
      case 0: // Linear
        strategy = new LinearMovement();
        return new NormalAlien(textureManager, position, variedScale, speed, spaceship, strategy, 0);
      case 1: // Wave
        strategy = new WaveMovement();
        return new NormalAlien(textureManager, position, variedScale, speed, spaceship, strategy, 1);
      case 2: // Spiral
        strategy = new SpiralMovement(speed);
        return new NormalAlien(textureManager, position, variedScale, speed, spaceship, strategy, 2);
      case 3: // Baby Boomer
        strategy = new LinearMovement(); // They track linearly
        return new BoomerAlien(textureManager, position, scale, speed, spaceship, strategy, false);
      case 4: // Boss Boomer
        strategy = new LinearMovement();
        return new BoomerAlien(textureManager, position, scale, speed, spaceship, strategy, true);
      default:
        strategy = new LinearMovement();
        return new NormalAlien(textureManager, position, scale, speed, spaceship, strategy, 0);
    }
  }
}
