package com.space.game.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.space.game.Game;

public class Planet {
  private Texture texture;
  private Game game;
  private boolean positionSet = false;
  private float x, y;
  private float time;
  private static final float AMPLITUDE = 20f;
  private static final float SPEED = 1.5f;
  private static final float OPACITY = 0.9f;

  public Planet(Texture texture, Game game) {
    this.texture = texture;
    this.game = game;
    this.time = 0;
  }

  public void update(float deltaTime) {
    time += deltaTime;

    // Dynamically update position based on world size
    // This handles resizing and initial 0-size issues
    float targetX = game.getWorldWidth() * 0.7f;
    float targetY = game.getWorldHeight() * 0.6f;

    // If not set or updated, log once
    if (!positionSet && targetX > 0) {
      System.out.println("Planet initialized at: " + targetX + ", " + targetY);
      positionSet = true;
    }

    x = targetX;
    // Simple harmonic motion for up/down movement relative to current targetY
    y = targetY + MathUtils.sin(time * SPEED) * AMPLITUDE;
  }

  public void render(SpriteBatch batch) {
    if (texture == null)
      return;
    batch.setColor(1, 1, 1, OPACITY);
    batch.draw(texture, x, y);
    batch.setColor(Color.WHITE); // Restore default color
  }
}
