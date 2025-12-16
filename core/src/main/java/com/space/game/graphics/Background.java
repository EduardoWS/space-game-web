package com.space.game.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.space.game.Game;

public class Background {
    private Texture texture;
    private Texture starTexture;
    private Star[] stars;
    private float alpha_background = 0.25f;
    private boolean alpha_bool = false;
    private static final int NUM_STARS = 500;
    private Game game;
    private Planet planet;

    // Shooting Star variables
    private ShootingStar shootingStar;
    private float shootingStarTimer;

    public Background(TextureManager textureManager, Game game) {
        texture = textureManager.getTexture("background");
        starTexture = textureManager.getTexture("star");

        this.game = game;

        stars = new Star[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) {
            stars[i] = new Star();
            stars[i].brightness = MathUtils.random(1, 100);
            stars[i].duration = MathUtils.random(77, 777);
            stars[i].brightness_f = stars[i].brightness / 100f;
        }

        // Initialize Planet
        planet = new Planet(textureManager.getTexture("planet"), game);

        // Initialize Shooting Star
        shootingStar = new ShootingStar();
        shootingStarTimer = MathUtils.random(1, 5);
    }

    private class Star {
        float x, y;
        int brightness;
        float brightness_f;
        int size;
        int duration;

        public Star() {
            x = MathUtils.random(Gdx.graphics.getWidth());
            y = MathUtils.random(Gdx.graphics.getHeight());
            size = MathUtils.random(2, 5);
        }

        public void update() {
            duration--;
            if (brightness > 0) {
                brightness--;
            }

            if (duration <= 0) {
                duration = MathUtils.random(77, 777);
                brightness = MathUtils.random(1, 100);
            }
            brightness_f = brightness / 100f;
        }
    }

    private class ShootingStar {
        float x, y;
        float speedX, speedY;
        boolean active;
        float scale;

        public ShootingStar() {
            active = false;
        }

        public void spawn() {
            active = true;
            // Spawn from top-right or top mostly
            x = MathUtils.random(game.getWorldWidth() * 0.2f, game.getWorldWidth());
            y = game.getWorldHeight();

            // Move diagonally down-left
            speedX = -MathUtils.random(400, 800);
            speedY = -MathUtils.random(200, 600);
            scale = MathUtils.random(0.5f, 1.2f);
        }

        public void update(float delta) {
            if (!active)
                return;

            x += speedX * delta;
            y += speedY * delta;

            if (x < -100 || y < -100) {
                active = false;
            }
        }

        public void render(SpriteBatch batch) {
            if (!active)
                return;
            // Draw stretched star to look like a streak
            // Use rotation to align with movement?
            // Simple approach: just draw it.
            float rotation = MathUtils.atan2(speedY, speedX) * MathUtils.radiansToDegrees;

            batch.draw(starTexture,
                    x, y,
                    starTexture.getWidth() / 2f, starTexture.getHeight() / 2f, // origin
                    starTexture.getWidth(), starTexture.getHeight(),
                    scale * 4f, scale * 0.5f, // Stretch X, shrink Y (relative to rotation)
                    rotation,
                    0, 0,
                    starTexture.getWidth(), starTexture.getHeight(),
                    false, false);
        }
    }

    public void update() {
        for (Star star : stars) {
            star.update();
        }

        if (alpha_bool == false) {
            alpha_background = MathUtils.clamp(alpha_background + 0.000009f, 0.25f, 0.4f);
            if (alpha_background >= 0.4f) {
                alpha_bool = true;
            }
        } else {
            alpha_background = MathUtils.clamp(alpha_background - 0.000009f, 0.25f, 0.4f);
            if (alpha_background <= 0.25f) {
                alpha_bool = false;
            }
        }

        // Update Planet
        planet.update(Gdx.graphics.getDeltaTime());

        // Update Shooting Star
        if (!shootingStar.active) {
            shootingStarTimer -= Gdx.graphics.getDeltaTime();
            if (shootingStarTimer <= 0) {
                shootingStar.spawn();
                System.out.println("Shooting Star Spawned at " + shootingStar.x + ", " + shootingStar.y);
                shootingStarTimer = MathUtils.random(3, 8); // Next one in 3-8 seconds
            }
        }
        shootingStar.update(Gdx.graphics.getDeltaTime());
    }

    public void render(SpriteBatch batch) {
        // Desenha as estrelas
        batch.setColor(Color.WHITE);
        for (Star star : stars) {
            batch.setColor(1, 1, 1, star.brightness_f);
            batch.draw(starTexture, star.x, star.y, starTexture.getWidth() / star.size,
                    starTexture.getHeight() / star.size);
        }

        // Render Shooting Star (behind planet/overlay?)
        batch.setColor(1, 1, 1, 1);
        shootingStar.render(batch);

        // Background Overlay
        batch.setColor(Color.WHITE);
        batch.setColor(1, 1, 1, alpha_background);
        batch.draw(texture, 0, 0, game.getWorldWidth(), game.getWorldHeight());
        batch.setColor(Color.WHITE);

        // Planet (On top)
        planet.render(batch);
    }

    public void dispose() {
        // texture.dispose();
        // starTexture.dispose();
    }

    public void setBackgroundTexture(Texture texture) {
        this.texture = texture;
    }
}
