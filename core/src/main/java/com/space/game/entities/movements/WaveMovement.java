package com.space.game.entities.movements;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.SpaceGame;

public class WaveMovement implements MovementStrategy {
  private float elapsedTime = 0;
  private float waveAmplitude;
  private float waveFrequency;

  public WaveMovement() {
    // Initialize random values similar to original Alien constructor
    waveAmplitude = MathUtils.random(SpaceGame.getGame().getWorldHeight() / 9,
        SpaceGame.getGame().getWorldHeight() / 5);
    waveFrequency = MathUtils.random(1, 5);
    elapsedTime = MathUtils.random(0, 5);
  }

  @Override
  public void move(Alien alien, Spaceship spaceship, float deltaTime) {
    float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
    float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

    float alienCenterX = alien.getPosition().x + alien.getBounds().width / 2;
    float alienCenterY = alien.getPosition().y + alien.getBounds().height / 2;

    Vector2 direction = new Vector2(naveCenterX - alienCenterX, naveCenterY - alienCenterY);
    direction.nor();

    // Standard Tracking
    alien.getPosition().x += direction.x * alien.getSpeed() * deltaTime;
    alien.getPosition().y += direction.y * alien.getSpeed() * deltaTime;

    // Wave Offset
    elapsedTime += deltaTime;
    float waveOffset = waveAmplitude * (float) Math.sin(waveFrequency * elapsedTime) * deltaTime;

    Vector2 perpendicularDirection = new Vector2(-direction.y, direction.x);
    alien.getPosition().x += perpendicularDirection.x * waveOffset;
    alien.getPosition().y += perpendicularDirection.y * waveOffset;
  }
}
