package com.space.game.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.space.game.graphics.TextureManager;

public class ParticleManager {

  public static class Particle implements Pool.Poolable {
    public float x, y;
    public float vx, vy;
    public float life, maxLife;
    public Color color = new Color();
    public float size;
    public float scaleSpeed = 0; // Speed at which size changes

    @Override
    public void reset() {
      x = 0;
      y = 0;
      vx = 0;
      vy = 0;
      life = 0;
      maxLife = 0;
      color.set(Color.WHITE);
      size = 1f;
      scaleSpeed = 0;
    }
  }

  private final Array<Particle> particles = new Array<>();
  private final Pool<Particle> particlePool = new Pool<Particle>() {
    @Override
    protected Particle newObject() {
      return new Particle();
    }
  };

  private Texture particleTexture;

  public ParticleManager(TextureManager textureManager) {
    this.particleTexture = textureManager.getTexture("star");
    // Fallback if "star" isn't found, though it should be
    if (this.particleTexture == null) {
      // Error handling or fallback logic could go here
    }
  }

  public void addParticle(float x, float y, float vx, float vy, float life, Color color, float size, float scaleSpeed) {
    Particle p = particlePool.obtain();
    p.x = x;
    p.y = y;
    p.vx = vx;
    p.vy = vy;
    p.life = life;
    p.maxLife = life;
    p.color.set(color);
    p.size = size;
    p.scaleSpeed = scaleSpeed;
    particles.add(p);
  }

  public void clear() {
    particlePool.freeAll(particles);
    particles.clear();
  }

  // Convenience for explosion
  // Convenience for explosion
  public void createExplosion(float x, float y, int count) {
    createExplosion(x, y, count, new Color(0f, MathUtils.random(0.5f, 1.0f), 0f, 1f));
  }

  public void createExplosion(float x, float y, int count, Color color) {
    for (int i = 0; i < count; i++) {
      float speed = MathUtils.random(15f, 150f);
      float angle = MathUtils.random(0f, 360f);
      float vx = MathUtils.cosDeg(angle) * speed;
      float vy = MathUtils.sinDeg(angle) * speed;

      // Variation on alpha/size but keep color base
      float duration = MathUtils.random(0.5f, 1.0f);
      float size = MathUtils.random(0.8f, 1.5f);

      addParticle(x, y, vx, vy, duration, color, size, -1.0f);
    }
  }

  public void createChargeParticle(float x, float y) {
    // Spawn only 1 particle per call to reduce density
    for (int i = 0; i < 1; i++) {
      float angle = MathUtils.random(0, 360);
      float speed = MathUtils.random(5f, 20f);
      float vx = MathUtils.cosDeg(angle) * speed;
      float vy = MathUtils.sinDeg(angle) * speed;

      // Cyan/Blue electric color
      Color c = new Color(0.2f, 0.8f, 1f, 1f);

      // Increased size: 0.8f to 1.5f (was 0.2 to 0.5)
      addParticle(x, y, vx, vy, MathUtils.random(0.3f, 0.6f), c, MathUtils.random(0.8f, 1.5f), -0.5f);
    }
  }

  public void createMassiveExplosion(float x, float y, Color color) {
    int count = 150;
    for (int i = 0; i < count; i++) {
      float speed = MathUtils.random(100f, 600f);
      float angle = MathUtils.random(0f, 360f);
      float vx = MathUtils.cosDeg(angle) * speed;
      float vy = MathUtils.sinDeg(angle) * speed;
      float duration = MathUtils.random(1.0f, 2.5f);
      float size = MathUtils.random(2.0f, 6.0f);
      addParticle(x, y, vx, vy, duration, color, size, -2.0f);
    }
  }

  public void update(float dt) {
    for (int i = particles.size - 1; i >= 0; i--) {
      Particle p = particles.get(i);
      p.life -= dt;
      if (p.life <= 0) {
        particles.removeIndex(i);
        particlePool.free(p);
      } else {
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.size += p.scaleSpeed * dt;
        if (p.size < 0)
          p.size = 0;
      }
    }
  }

  public void render(SpriteBatch batch) {
    if (particleTexture == null)
      return;

    float oldR = batch.getColor().r;
    float oldG = batch.getColor().g;
    float oldB = batch.getColor().b;
    float oldA = batch.getColor().a;

    int srcWidth = particleTexture.getWidth();
    int srcHeight = particleTexture.getHeight();
    float originX = srcWidth / 2f;
    float originY = srcHeight / 2f;

    // Use the saved old values as the theme color
    for (Particle p : particles) {
      float alpha = p.life / p.maxLife;
      // Multiply particle color with theme color
      batch.setColor(p.color.r * oldR, p.color.g * oldG, p.color.b * oldB,
          p.color.a * alpha * oldA);

      batch.draw(particleTexture,
          p.x - originX, p.y - originY,
          originX, originY,
          srcWidth, srcHeight,
          p.size, p.size,
          0,
          0, 0, srcWidth, srcHeight,
          false, false);
    }
    // Restore theme color
    batch.setColor(oldR, oldG, oldB, oldA);
  }

  public void dispose() {
    // Texture is owned by TextureManager, don't dispose it here.
  }
}
