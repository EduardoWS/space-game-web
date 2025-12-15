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


  // Convenience for explosion
  public void createExplosion(float x, float y, int count) {
    for (int i = 0; i < count; i++) {
      float speed = MathUtils.random(50f, 300f);
      float angle = MathUtils.random(0f, 360f);
      float vx = MathUtils.cosDeg(angle) * speed;
      float vy = MathUtils.sinDeg(angle) * speed;

      Color c = new Color(1f, MathUtils.random(0f, 0.5f), 0f, 1f); // Red/Orange

      addParticle(x, y, vx, vy, MathUtils.random(0.5f, 1.0f), c, MathUtils.random(0.8f, 1.5f), -1.0f);
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

    Color originalColor = new Color(batch.getColor());
    int srcWidth = particleTexture.getWidth();
    int srcHeight = particleTexture.getHeight();
    float originX = srcWidth / 2f;
    float originY = srcHeight / 2f;

    for (Particle p : particles) {
      // Fade out alpha
      p.color.a = p.life / p.maxLife;
      batch.setColor(p.color);

      batch.draw(particleTexture,
          p.x - originX, p.y - originY,
          originX, originY,
          srcWidth, srcHeight,
          p.size, p.size,
          0,
          0, 0, srcWidth, srcHeight,
          false, false);
    }
    batch.setColor(originalColor);
  }

  public void dispose() {
    // Texture is owned by TextureManager, don't dispose it here.
  }
}
