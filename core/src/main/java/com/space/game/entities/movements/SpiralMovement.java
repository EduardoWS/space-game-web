package com.space.game.entities.movements;

import com.badlogic.gdx.math.MathUtils;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.SpaceGame;

public class SpiralMovement implements MovementStrategy {
  private float elapsedTime;
  private float angleSpeed;
  private float angleOffset;

  // State fields
  private float currentRadius = -1;
  private float orbitTimer = 0;
  private final float ORBIT_DURATION = 3.0f;

  public SpiralMovement(float speed) {
    this.angleSpeed = SpaceGame.getGame().getWorldWidth() / 3840; // Approx 0.5 rad/s if width=1920

    this.angleOffset = MathUtils.random(0f, MathUtils.PI2);
    this.elapsedTime = 0;
  }

  @Override
  public void move(Alien alien, Spaceship spaceship, float deltaTime) {
    float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
    float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

    elapsedTime += deltaTime;

    float w = SpaceGame.getGame().getWorldWidth();
    float h = SpaceGame.getGame().getWorldHeight();

    // Initialize radius if first run
    if (currentRadius == -1) {
      currentRadius = (float) Math.sqrt((w / 2) * (w / 2) + (h / 2) * (h / 2)) + 50f;
    }

    float currentSpeed = alien.getSpeed();
    float visualRange = h * 0.45f; // Slightly less than half height (radius)
    float attackRange = 100f; // Very close

    float radialSpeed = 0;

    // Movement Logic
    if (currentRadius > visualRange) {
      // Phase 1: Fast Approach from off-screen
      radialSpeed = currentSpeed * 1.5f; // Reduced from 2.5f
    } else if (currentRadius > attackRange) {
      // Phase 2: Visual Range
      if (orbitTimer < ORBIT_DURATION) {
        // Orbiting (Very slow approach, mostly rotation)
        orbitTimer += deltaTime;
        radialSpeed = currentSpeed * 0.2f;
      } else {
        // Phase 3: Dive after orbit
        radialSpeed = currentSpeed * 1.5f;
      }
    } else {
      // Close range dive
      radialSpeed = currentSpeed * 1.2f;
    }

    // Apply Radial Movement
    currentRadius -= radialSpeed * deltaTime;
    if (currentRadius < 2)
      currentRadius = 2;

    // Angular Movement
    float angle = angleSpeed * elapsedTime + angleOffset;

    // Position Update
    alien.getPosition().x = naveCenterX + currentRadius * (float) Math.cos(angle);
    alien.getPosition().y = naveCenterY + currentRadius * (float) Math.sin(angle);
  }
}
