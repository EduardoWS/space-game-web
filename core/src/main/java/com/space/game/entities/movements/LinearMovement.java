package com.space.game.entities.movements;

import com.badlogic.gdx.math.Vector2;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;

public class LinearMovement implements MovementStrategy {
  @Override
  public void move(Alien alien, Spaceship spaceship, float deltaTime) {
    moveLinearly(alien, spaceship, deltaTime);
  }

  private void moveLinearly(Alien alien, Spaceship spaceship, float deltaTime) {
    float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
    float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

    float alienCenterX = alien.getPosition().x + alien.getBounds().width / 2;
    float alienCenterY = alien.getPosition().y + alien.getBounds().height / 2;

    Vector2 direction = new Vector2(naveCenterX - alienCenterX, naveCenterY - alienCenterY);
    direction.nor();

    alien.getPosition().x += direction.x * alien.getSpeed() * deltaTime;
    alien.getPosition().y += direction.y * alien.getSpeed() * deltaTime;
  }
}
