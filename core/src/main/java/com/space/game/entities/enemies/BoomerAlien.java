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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.space.game.graphics.AnimationHelper;

public class BoomerAlien extends Alien {
  // Animation Fields
  private Animation<TextureRegion> idleAnimation;
  private TextureRegion restFrame;
  private float stateTime = 0f;
  private Texture spriteSheet;

  private boolean isBoss;
  private boolean isDetonating = false;
  private float detonationTimer = 0f;
  private final float DETONATION_TIME = 2.0f;

  // Resting Mechanics
  private boolean isResting = false;
  private float restTimer = 0f;
  private int damageTakenSinceRest = 0;

  public BoomerAlien(TextureManager textureManager, Vector2 position, float scale, float speed, Spaceship spaceship,
      MovementStrategy strategy, boolean isBoss) {
    super(textureManager, position, scale, speed, strategy,
        isBoss ? AlienType.BOSS_BOOMER : AlienType.BABY_BOOMER);
    this.isBoss = isBoss;

    this.texture = textureManager.getTexture("alienBoomer");

    if (isBoss) {
      this.scale = (scale > 0) ? scale : GameConfig.BOSS_BOOMER_SCALE;
      this.hp = GameConfig.BOSS_HP;
      this.maxHp = this.hp;
      this.speed = GameConfig.BOSS_BOOMER_SPEED;

      // Initialize Animation
      this.spriteSheet = textureManager.getTexture("alienBoomerSheet");
      if (this.spriteSheet != null) {
        this.idleAnimation = AnimationHelper.createAnimation(
            this.spriteSheet,
            GameConfig.BOSS_TILE_WIDTH,
            GameConfig.BOSS_TILE_HEIGHT,
            GameConfig.BOSS_FRAME_DURATION,
            true);

        // Resting Frame: Frame 4 (Index 3)
        // We can extract it from the animation frames or split again,
        // but easier via split since Animation doesn't expose list directly without
        // cast/get
        // But AnimationHelper created it. Let's just grab Frame 3 from the animation
        // logic for consistency
        // Or simpler: just use split for initialization
        TextureRegion[][] tmp = TextureRegion.split(this.spriteSheet, GameConfig.BOSS_TILE_WIDTH,
            GameConfig.BOSS_TILE_HEIGHT);
        if (tmp.length > 0 && tmp[0].length >= 4) {
          this.restFrame = tmp[0][3];
        }
      }

    } else {
      this.scale = (scale > 0) ? scale : GameConfig.BABY_BOOMER_SCALE;
      this.hp = GameConfig.BABY_HP;
      this.maxHp = this.hp;
      this.speed = GameConfig.BABY_BOOMER_SPEED;
    }

    initializeBounds();
  }

  @Override
  protected void initializeBounds() {
    // If animating, bounds should assume frame size, not entire sheet size.
    // However, texture field is still the static image for Baby Boomers or
    // fallbacks.

    float width, height;

    if (isBoss && spriteSheet != null) {
      width = GameConfig.BOSS_TILE_WIDTH * this.scale;
      height = GameConfig.BOSS_TILE_HEIGHT * this.scale;
    } else {
      if (texture == null)
        return;
      width = texture.getWidth() * this.scale;
      height = texture.getHeight() * this.scale;
    }

    if (isBoss) {
      float topTrim = 50f;
      float bottomTrim = 100f;
      float sideTrim = 50f;

      // Boss bounds tuning (heuristic based on visible sprite area)
      float finalHeight = height - topTrim - bottomTrim;
      float finalWidth = width - (sideTrim * 2);

      this.bounds = new Rectangle(position.x + sideTrim, position.y + bottomTrim, finalWidth, finalHeight);
    } else {
      float boundsPadding = 14f;
      this.bounds = new Rectangle(position.x, position.y, width + boundsPadding, height + boundsPadding);
    }
  }

  @Override
  public void update(float deltaTime, Spaceship spaceship) {
    stateTime += deltaTime;

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

    if (isResting) {
      restTimer -= deltaTime;
      if (restTimer <= 0) {
        isResting = false;
        damageTakenSinceRest = 0;
      }
      updateBoundsPosition();
      return;
    }

    strategy.move(this, spaceship, deltaTime);
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
  public boolean takeDamage(int damage) {
    boolean killed = super.takeDamage(damage);
    if (isBoss && !isDead && !killed) {
      damageTakenSinceRest += damage;
      if (!isResting && damageTakenSinceRest >= GameConfig.BOSS_REST_HP_THRESHOLD) {
        isResting = true;
        restTimer = GameConfig.BOSS_REST_DURATION;
        damageTakenSinceRest = 0; // Reset for next cycle
      }
    }
    return killed;
  }

  public boolean isResting() {
    return isResting;
  }

  @Override
  public void render(SpriteBatch batch) {
    if (!isMarkedForRemoval) {
      float oldR = batch.getColor().r;
      float oldG = batch.getColor().g;
      float oldB = batch.getColor().b;
      float oldA = batch.getColor().a;

      float currentScale = scale;

      TextureRegion regionToDraw = null;
      Texture textureToDraw = null;

      if (isBoss) {
        // Boss Logic
        if (isDead || isDetonating) {
          // Dead/Exploding: Static Image (Original 'texture')
          textureToDraw = this.texture;
        } else if (isResting && restFrame != null) {
          // Resting: Frame 4
          regionToDraw = restFrame;
        } else if (idleAnimation != null) {
          // Normal: Loop Animation
          regionToDraw = idleAnimation.getKeyFrame(stateTime, true);
        } else {
          // Fallback
          textureToDraw = this.texture;
        }
      } else {
        // Baby Boomer Logic (Always Static)
        textureToDraw = this.texture;
      }

      // Effects Logic (Scale/Color)
      if (isDetonating) {
        float progress = 1.0f - (detonationTimer / DETONATION_TIME);
        float maxInflation = 0.3f;
        currentScale = scale * (1.0f + progress * maxInflation);
        batch.setColor(1f, MathUtils.random(0.5f, 1f), 0f, oldA);
      } else if (isDead) {
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

      // Draw
      if (regionToDraw != null) {
        batch.draw(regionToDraw, position.x, position.y, regionToDraw.getRegionWidth() * currentScale,
            regionToDraw.getRegionHeight() * currentScale);
      } else if (textureToDraw != null) {
        batch.draw(textureToDraw, position.x, position.y, textureToDraw.getWidth() * currentScale,
            textureToDraw.getHeight() * currentScale);
      }

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
    if (isBoss) {
      if (isResting)
        return;
      force *= 0.3f;
    }
    if (isDead || isDetonating)
      return;
    super.applyKnockback(force);
    updateBoundsPosition();
  }

  @Override
  protected void onDeath() {
    // Logic handled in update/render
  }
}
