package com.space.game.entities.enemies;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.entities.movements.MovementStrategy;
import com.space.game.graphics.TextureManager;

public class NormalAlien extends Alien {
  private int movementPattern;

  public NormalAlien(TextureManager textureManager, Vector2 position, float scale, float speed, Spaceship spaceship,
      MovementStrategy strategy, int movementPattern) {
    super(textureManager, position, scale, speed, strategy, AlienType.NORMAL);
    this.movementPattern = movementPattern;

    // Texture selection based on pattern
    switch (movementPattern) {
      case 0: // Linear
        this.texture = textureManager.getTexture("alienLinear");
        this.scale = scale * 1.25f;
        break;
      case 1: // Wave
        this.texture = textureManager.getTexture("alienWave");
        this.scale = scale * 1.125f;
        break;
      case 2: // Spiral
        this.texture = textureManager.getTexture("alienSpiral");
        // Spiral scale
        this.scale = scale * 1.125f;
        break;
      default:
        this.texture = textureManager.getTexture("alienLinear");
        break;
    }

    this.hp = 1;
    this.maxHp = 1;

    // Ensure bounds are set after texture is assigned
    initializeBounds();
  }

  @Override
  public void update(float deltaTime, Spaceship spaceship) {
    if (hitTimer > 0)
      hitTimer -= deltaTime;

    if (isDead) {
      deathTimer += deltaTime;
      // Using Linear Strategy set in onDeath to move backwards
      strategy.move(this, spaceship, deltaTime);

      // Sync bounds for dead aliens too so particles spawn correctly!
      if (bounds != null) {
        bounds.setPosition(position.x, position.y);
      }
      return;
    }

    strategy.move(this, spaceship, deltaTime);

    if (bounds != null) {
      bounds.setPosition(position.x, position.y);
    }

    // Speed scaling for Normal Aliens (from original code)
    switch (movementPattern) {
      case 0:
        speed += (deltaTime * speed / MathUtils.random(12, 16));
        break;
      case 1:
        speed += (deltaTime * speed / MathUtils.random(14, 18));
        break;
      case 2:
        speed += (deltaTime * speed / MathUtils.random(12, 20));
        break;
    }
  }

  @Override
  protected void onDeath() {
    // Change strategy to Linear to ensure backward movement is always linear
    setStrategy(new com.space.game.entities.movements.LinearMovement());

    // Invert speed to move backwards
    this.speed = -Math.abs(this.speed) / 2;
  }
}
