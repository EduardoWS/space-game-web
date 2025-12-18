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

    // Determine Strategy and Type
    switch (movementPattern) {
      case 0: // Linear
        strategy = new LinearMovement();
        return new NormalAlien(textureManager, position, scale, speed, spaceship, strategy, 0);
      case 1: // Wave
        strategy = new WaveMovement();
        return new NormalAlien(textureManager, position, scale, speed, spaceship, strategy, 1);
      case 2: // Spiral
        strategy = new SpiralMovement(speed);
        return new NormalAlien(textureManager, position, scale, speed, spaceship, strategy, 2);
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
