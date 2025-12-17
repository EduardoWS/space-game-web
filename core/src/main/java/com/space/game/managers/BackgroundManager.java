package com.space.game.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.Game;
import com.space.game.graphics.TextureManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class BackgroundManager {
  private Texture nebulaTexture;
  private Texture starsTexture;
  private Texture dustTexture;

  // These represent PIXEL offsets for tiled layers
  private float nebulaX, nebulaY;
  private float dustX, dustY;

  // Procedural Stars
  private Star[] stars;

  // --- CALIBRATION CONSTANTS ---
  private static final int NUM_STARS = 400; // Increased to 800

  // Alpha/Opacity Settings
  private static final float DUST_ALPHA = 0.35f;
  private static final float NEBULA_ALPHA = 1f;
  private static final float STAR_ALPHA_MIN = 0.3f;
  private static final float STAR_ALPHA_MAX = 1.0f;

  // Movement speeds (Pixels per second) - Increased for smoother diagonal drift
  private static final float NEBULA_SPEED = 10f;
  private static final float STARS_SPEED = 16f;
  private static final float DUST_SPEED = 20f;

  // Drift Direction (Diagonal drift)
  private float driftX = 0.4f;
  private float driftY = 0.2f;

  private Game game;

  // Custom features
  private ShootingStar shootingStar;
  private float shootingStarTimer;

  public BackgroundManager(TextureManager textureManager, Game game) {
    this.game = game;

    nebulaTexture = textureManager.getTexture("bg_nebula");
    starsTexture = textureManager.getTexture("star"); // Use "star" (16x16)
    dustTexture = textureManager.getTexture("bg_dust");

    // Initialize Procedural Stars
    stars = new Star[NUM_STARS];
    for (int i = 0; i < NUM_STARS; i++) {
      stars[i] = new Star();
    }

    // Initialize Shooting Star
    shootingStar = new ShootingStar();
    shootingStarTimer = MathUtils.random(1, 5);
  }

  // Compatibility method for LevelTheme changing background
  public void setBackgroundTexture(Texture texture) {
    this.nebulaTexture = texture;
  }

  public void resize(int width, int height) {
    // Re-distribute stars when screen resizes to prevent clumping in TOP-LEFT
    if (stars != null) {
      for (Star star : stars) {
        star.x = MathUtils.random(width);
        star.y = MathUtils.random(height);
      }
    }
  }

  public void update(float delta) {
    // Update pixel coordinates/drift for Tiled layers
    nebulaX += driftX * NEBULA_SPEED * delta;
    nebulaY += driftY * NEBULA_SPEED * delta;

    dustX += driftX * DUST_SPEED * delta;
    dustY += driftY * DUST_SPEED * delta;

    // Update Procedural Stars (Drift + Blinking)
    for (Star star : stars) {
      star.update(delta);
    }

    // Update Shooting Star
    if (!shootingStar.active) {
      shootingStarTimer -= delta;
      if (shootingStarTimer <= 0) {
        shootingStar.spawn();
        shootingStarTimer = MathUtils.random(3, 8);
      }
    }
    shootingStar.update(delta);
  }

  public void render(SpriteBatch batch) {
    float width = game.getWorldWidth();
    float height = game.getWorldHeight();

    // 1. NEBULA (Back - Tiled)
    batch.setColor(1, 1, 1, NEBULA_ALPHA);
    if (nebulaTexture != null)
      drawTiled(batch, nebulaTexture, width, height, nebulaX, nebulaY);

    // 2. STARS (Middle - Procedural)
    if (starsTexture != null) {
      for (Star star : stars) {
        // Apply blinking + base opacity
        // Brightness factor (0.0 - 1.0) interacts with Alpha range
        float alpha = STAR_ALPHA_MIN + (star.brightness_f * (STAR_ALPHA_MAX - STAR_ALPHA_MIN));
        batch.setColor(1, 1, 1, alpha);
        batch.draw(starsTexture, star.x, star.y, starsTexture.getWidth() / star.size,
            starsTexture.getHeight() / star.size);
      }
    }

    // 3. SHOOTING STAR (Middle/Front)
    // Render shooting star with its own logic (handles alpha internally)
    shootingStar.render(batch);

    // 4. DUST (Front - Tiled - Overlay)
    batch.setColor(1, 1, 1, DUST_ALPHA);
    if (dustTexture != null)
      drawTiled(batch, dustTexture, width, height, dustX, dustY);

    // Reset color to white for subsequent renders
    batch.setColor(Color.WHITE);
  }

  /**
   * Tiles the texture across the specified view dimensions.
   */
  private void drawTiled(SpriteBatch batch, Texture tex, float viewW, float viewH, float scrollX, float scrollY) {
    int texW = tex.getWidth();
    int texH = tex.getHeight();

    if (texW == 0 || texH == 0)
      return;

    // Calculate offset (modulo)
    float offX = scrollX % texW;
    float offY = scrollY % texH;

    if (offX > 0)
      offX -= texW;
    if (offY > 0)
      offY -= texH;

    for (float x = offX; x < viewW; x += texW) {
      for (float y = offY; y < viewH; y += texH) {
        batch.draw(tex, x, y, texW, texH);
      }
    }
  }

  public void dispose() {
  }

  private class Star {
    float x, y;
    int brightness;
    float brightness_f;
    float size;
    int duration;

    public Star() {
      reset();
      // Desync blinking
      duration = MathUtils.random(0, 777);
      brightness = MathUtils.random(1, 100);
    }

    public void reset() {
      try {
        x = MathUtils.random(game.getWorldWidth());
        y = MathUtils.random(game.getWorldHeight());
      } catch (Exception e) {
        x = 0;
        y = 0;
      }
      // Random size
      float r = MathUtils.random();
      if (r < 0.7f) {
        size = MathUtils.random(3f, 5f); // Tiny
      } else {
        size = MathUtils.random(1.5f, 3f); // Bigger
      }

      duration = MathUtils.random(77, 777);
      brightness = MathUtils.random(1, 100);
      brightness_f = brightness / 100f;
    }

    public void update(float delta) {
      // Blinking Logic
      duration--;
      if (brightness > 0) {
        brightness--;
      }
      if (duration <= 0) {
        duration = MathUtils.random(77, 777);
        brightness = MathUtils.random(1, 100);
      }
      brightness_f = brightness / 100f;

      // Drift Logic
      x += driftX * STARS_SPEED * delta;
      y += driftY * STARS_SPEED * delta;

      // Wrap around screen
      float w = game.getWorldWidth();
      float h = game.getWorldHeight();

      if (w > 0 && h > 0) {
        if (x > w)
          x -= w;
        if (x < 0)
          x += w;
        if (y > h)
          y -= h;
        if (y < 0)
          y += h;
      }
    }
  }

  private class ShootingStar {
    float x, y;
    float speedX, speedY;
    boolean active;
    float scale;

    // New fields for trail and fading
    float lifeTime;
    float maxLifeTime;
    Array<Vector2> trail;
    static final int MAX_TRAIL_LENGTH = 15;
    static final float TRAIL_INTERVAL = 0.05f; // Time between trail points
    float trailTimer;

    public ShootingStar() {
      active = false;
      trail = new Array<>();
    }

    public void spawn() {
      active = true;
      x = MathUtils.random(game.getWorldWidth() * 0.2f, game.getWorldWidth());
      y = game.getWorldHeight() + 50; // Start slightly above screen

      speedX = -MathUtils.random(500, 900);
      speedY = -MathUtils.random(300, 700);
      scale = MathUtils.random(0.5f, 1.0f);

      maxLifeTime = MathUtils.random(1.5f, 2.5f);
      lifeTime = maxLifeTime;

      trail.clear();
      trailTimer = 0;
    }

    public void update(float delta) {
      if (!active)
        return;

      // Update life
      lifeTime -= delta;
      if (lifeTime <= 0) {
        active = false;
        return;
      }

      // Update position
      x += speedX * delta;
      y += speedY * delta;

      // Update trail
      trailTimer += delta;
      if (trailTimer >= TRAIL_INTERVAL) {
        trailTimer = 0;
        if (trail.size >= MAX_TRAIL_LENGTH) {
          trail.removeIndex(0); // Remove oldest
        }
        trail.add(new Vector2(x, y));
      }

      if (x < -100 || y < -100) {
        active = false;
      }
    }

    public void render(SpriteBatch batch) {
      if (!active)
        return;

      Texture starTex = game.getTextureManager().getTexture("star");
      if (starTex == null)
        return;

      float rotation = MathUtils.atan2(speedY, speedX) * MathUtils.radiansToDegrees;

      // Calculate alpha based on remaining life
      float lifeAlpha = MathUtils.clamp(lifeTime / (maxLifeTime * 0.3f), 0f, 1f); // Fade out in last 30% of life
      if (lifeTime > maxLifeTime * 0.3f)
        lifeAlpha = 1f; // Full alpha otherwise

      // Reduce overall brightness as requested (0.6f max)
      float baseAlpha = 0.6f * lifeAlpha;

      // Render Trail
      for (int i = 0; i < trail.size; i++) {
        Vector2 point = trail.get(i);
        // Trail points get more transparent as they get further from the head
        float trailAlpha = (float) i / trail.size * baseAlpha * 0.5f;
        float trailScale = scale * ((float) i / trail.size);

        batch.setColor(1, 1, 1, trailAlpha);
        batch.draw(starTex,
            point.x, point.y,
            starTex.getWidth() / 2f, starTex.getHeight() / 2f,
            starTex.getWidth(), starTex.getHeight(),
            trailScale * 4f, trailScale * 0.5f,
            rotation,
            0, 0,
            starTex.getWidth(), starTex.getHeight(),
            false, false);
      }

      // Render Head
      batch.setColor(1, 1, 1, baseAlpha);
      batch.draw(starTex,
          x, y,
          starTex.getWidth() / 2f, starTex.getHeight() / 2f,
          starTex.getWidth(), starTex.getHeight(),
          scale * 4f, scale * 0.5f,
          rotation,
          0, 0,
          starTex.getWidth(), starTex.getHeight(),
          false, false);

      // Reset color
      batch.setColor(Color.WHITE);
    }
  }
}
