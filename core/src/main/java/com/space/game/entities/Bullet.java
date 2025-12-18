package com.space.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.space.game.SpaceGame;
import com.space.game.graphics.TextureManager;

public class Bullet {
    private Texture texture;
    private Vector2 position;
    private float angle;
    private float scale;
    private float speed;
    private Rectangle bounds;
    private boolean shouldRemove = false;
    private boolean isCharged = false;
    private int killCount = 0;

    public Bullet(TextureManager textureManager, Vector2 spaceshipPosition, float angle, float spaceshipWidth,
            float spaceshipHeight, float scale, boolean isCharged) {
        this.angle = angle + 90;
        this.isCharged = isCharged;

        // Configuration based on type
        if (isCharged) {
            this.scale = scale * 2.5f;
            this.speed = SpaceGame.getGame().getWorldWidth() * 0.75f;
        } else {
            this.scale = scale;
            this.speed = SpaceGame.getGame().getWorldWidth() / 2;
        }

        texture = textureManager.getTexture("bullet");
        float bulletWidth = texture.getWidth();
        float bulletHeight = texture.getHeight();

        // Calculate spawn position
        float centerX = spaceshipPosition.x + (spaceshipWidth) / 2;
        float centerY = spaceshipPosition.y + (spaceshipHeight) / 2;
        float offsetFromCenter = spaceshipHeight / 2;

        float bulletOffsetX = MathUtils.cosDeg(this.angle) * offsetFromCenter;
        float bulletOffsetY = MathUtils.sinDeg(this.angle) * offsetFromCenter;

        float bullet_x = centerX + bulletOffsetX - (bulletWidth / 2);
        float bullet_y = centerY + bulletOffsetY - (bulletHeight / 2);

        position = new Vector2(bullet_x, bullet_y);

        // Bounds
        float boundsScale = isCharged ? 2.5f : 1f;
        bounds = new Rectangle(position.x, position.y, bulletWidth * boundsScale, bulletHeight * boundsScale);
    }

    public void update() {
        // Simple linear movement
        float radianAngle = MathUtils.degreesToRadians * angle;
        position.x += speed * MathUtils.cos(radianAngle) * Gdx.graphics.getDeltaTime();
        position.y += speed * MathUtils.sin(radianAngle) * Gdx.graphics.getDeltaTime();
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        if (shouldRemove)
            return;

        float oldR = batch.getColor().r;
        float oldG = batch.getColor().g;
        float oldB = batch.getColor().b;
        float oldA = batch.getColor().a;

        if (isCharged) {
            batch.setColor(0.2f, 1.0f, 1.0f, 1.0f); // Cyan
        } else {
            batch.setColor(oldR, oldG, oldB, 0.77f);
        }

        batch.draw(texture,
                position.x, position.y,
                texture.getWidth() / 2, texture.getHeight() / 2,
                texture.getWidth(), texture.getHeight(),
                this.scale, this.scale,
                angle + 90, 0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false);

        batch.setColor(oldR, oldG, oldB, oldA);
    }

    // Getters and Setters
    public boolean isCharged() {
        return isCharged;
    }

    public void incrementKillCount() {
        this.killCount++;
    }

    public int getKillCount() {
        return this.killCount;
    }

    public boolean shouldRemove() {
        return shouldRemove;
    }

    public void markForRemoval() {
        shouldRemove = true;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
    }
}
