package com.space.game.entities.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.space.game.config.GameConfig;
import com.space.game.entities.Alien;
import com.space.game.entities.Spaceship;
import com.space.game.entities.movements.MovementStrategy;
import com.space.game.graphics.TextureManager;

public class BoomerAlien extends Alien {
  private boolean isBoss;
  private boolean isDetonating = false;
  private float detonationTimer = 0f;
  private final float DETONATION_TIME = 2.0f;

  public BoomerAlien(TextureManager textureManager, Vector2 position, float scale, float speed, Spaceship spaceship,
      MovementStrategy strategy, boolean isBoss) {
    super(textureManager, position, scale, speed, strategy,
        isBoss ? AlienType.BOSS_BOOMER : AlienType.BABY_BOOMER);
    this.isBoss = isBoss;

    this.texture = textureManager.getTexture("alienBoomer");

    if (isBoss) {
      this.scale = GameConfig.BOSS_BOOMER_SCALE;
      this.hp = GameConfig.BOSS_HP;
      this.maxHp = this.hp;
      this.speed = GameConfig.BOSS_BOOMER_SPEED;
    } else {
      this.scale = GameConfig.BABY_BOOMER_SCALE;
      this.hp = GameConfig.BABY_HP;
      this.maxHp = this.hp;
      this.speed = GameConfig.BABY_BOOMER_SPEED;
    }

    initializeBounds();
  }

  @Override
  protected void initializeBounds() {
    if (texture == null)
      return;

    float width = texture.getWidth() * this.scale;
    float height = texture.getHeight() * this.scale;

    if (isBoss) {
      float topTrim = 50f;
      float bottomTrim = 100f;
      float sideTrim = 50f;

      float finalHeight = height - topTrim - bottomTrim;
      float finalWidth = width - (sideTrim * 2);

      // Adjust Offset logic.
      // We store visual position in `position`, but bounds are offset.
      // If I change bounds x,y, I should ensure render uses `position` correctly.
      // My base `Alien.update` doesn't sync bounds offset!!!
      // I need to override update or manage bounds manually.
      this.bounds = new Rectangle(position.x + sideTrim, position.y + bottomTrim, finalWidth, finalHeight);
    } else {
      // Standard padding logic from original logic
      float boundsPadding = 14f;
      this.bounds = new Rectangle(position.x, position.y, width + boundsPadding, height + boundsPadding);
    }
  }

  @Override
  public void update(float deltaTime, Spaceship spaceship) {
    if (hitTimer > 0)
      hitTimer -= deltaTime;

    if (isDetonating) {
      detonationTimer -= deltaTime;
      return; // No movement
    }

    if (isDead) {
      deathTimer += deltaTime;
      return; // No movement
    }

    strategy.move(this, spaceship, deltaTime);

    // Sync bounds
    updateBoundsPosition();
  }

  private void updateBoundsPosition() {
    if (isBoss) {
      float bottomTrim = 100f;
      float sideTrim = 50f;
      bounds.setPosition(position.x + sideTrim, position.y + bottomTrim);
    } else {
      bounds.setPosition(position.x, position.y);
    }
  }

  @Override
  public void render(SpriteBatch batch) {
    if (!isMarkedForRemoval) {
      float oldR = batch.getColor().r;
      float oldG = batch.getColor().g;
      float oldB = batch.getColor().b;
      float oldA = batch.getColor().a;

      float currentScale = scale;

      if (isDetonating) {
        float progress = 1.0f - (detonationTimer / DETONATION_TIME);
        float maxInflation = 0.3f;
        currentScale = scale * (1.0f + progress * maxInflation);
        batch.setColor(1f, MathUtils.random(0.5f, 1f), 0f, oldA); // Warning tint
      } else if (isDead) {
        // Blink Red/Orange
        float blink = MathUtils.sin(deathTimer * 20);
        if (blink > 0)
          batch.setColor(1, 0, 0, oldA);
        else
          batch.setColor(1, 0.5f, 0, oldA);
      } else if (hitTimer > 0) {
        if (MathUtils.randomBoolean())
          batch.setColor(1, 1, 1, oldA);
        else
          batch.setColor(1, 1, 0, oldA);
      }

      batch.draw(texture, position.x, position.y, texture.getWidth() * currentScale,
          texture.getHeight() * currentScale);

      batch.setColor(oldR, oldG, oldB, oldA);
    }
  }

  // Detonation features
  public void startDetonation() {
    if (!isDetonating) {
      isDetonating = true;
      detonationTimer = DETONATION_TIME;
    }
  }

  public boolean isReadyToExplode() {
    return isDetonating && detonationTimer <= 0;
  }

  public boolean isDetonating() {
    return isDetonating;
  }

  @Override
  public void applyKnockback(float force) {
    if (isBoss)
      force *= 0.3f; // Resistance
    if (isDead || isDetonating)
      return;
    super.applyKnockback(force);
    updateBoundsPosition();
  }

  @Override
  protected void onDeath() {
    // Boomers don't move when dead, they explode later or fade?
    // Logic handled in update/render
  }
}
