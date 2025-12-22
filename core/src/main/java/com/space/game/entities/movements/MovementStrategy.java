package com.space.game.entities.movements;

import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;

public interface MovementStrategy {
  void move(Alien alien, Spaceship spaceship, float deltaTime);
}
