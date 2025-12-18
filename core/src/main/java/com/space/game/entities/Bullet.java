package com.space.game.entities;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.space.game.graphics.TextureManager;
import com.space.game.SpaceGame;

public class Bullet {
    private Texture texture;
    private Vector2 position;
    private float angle;
    private float scale;
    private float speed;
    private Rectangle bounds;
    private boolean shouldRemove = false;
    private boolean isCharged = false;

    public Bullet(TextureManager textureManager, Vector2 spaceshipPosition, float angle, float spaceshipWidth,
            float spaceshipHeight, float scale, boolean isCharged) {
        this.angle = angle + 90; // Ajusta o ângulo para a direção correta
        this.isCharged = isCharged;

        if (isCharged) {
            this.scale = scale * 2.5f; // Bigger bullet
            this.speed = SpaceGame.getGame().getWorldWidth() * 0.75f; // Faster? or same. Keeping logic simple.
        } else {
            this.scale = scale;
            this.speed = SpaceGame.getGame().getWorldWidth() / 2; // Velocidade do tiro
        }

        texture = textureManager.getTexture("bullet");
        float bulletWidth = texture.getWidth();
        float bulletHeight = texture.getHeight();

        // Considera o centro da nave como o ponto de origem
        float centerX = spaceshipPosition.x + (spaceshipWidth) / 2;
        float centerY = spaceshipPosition.y + (spaceshipHeight) / 2;

        // Calcula o deslocamento para posicionar o tiro no bico da nave
        float offsetFromCenter = spaceshipHeight / 2; // Base padrão no bico superior/inferior
        float bulletOffsetX = MathUtils.cosDeg(this.angle) * offsetFromCenter;
        float bulletOffsetY = MathUtils.sinDeg(this.angle) * offsetFromCenter;

        // Posiciona o tiro no bico da nave considerando o ângulo
        float bullet_x = centerX + bulletOffsetX - (bulletWidth / 2);
        float bullet_y = centerY + bulletOffsetY - (bulletHeight / 2);

        position = new Vector2(bullet_x, bullet_y);
        // Recalculate bounds with new scale? The logic below uses original width/height
        // which might be issue if logic relies on visual bounds.
        // Actually, for collision it is better to have bigger bounds if bigger bullet.
        bounds = new Rectangle(position.x, position.y, bulletWidth * (isCharged ? 2.5f : 1f),
                bulletHeight * (isCharged ? 2.5f : 1f));
    }

    public boolean isCharged() {
        return isCharged;
    }

    private int killCount = 0;

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

    public void update() {
        position.x += speed * Math.cos(Math.toRadians(angle)) * Gdx.graphics.getDeltaTime();
        position.y += speed * Math.sin(Math.toRadians(angle)) * Gdx.graphics.getDeltaTime();
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        if (shouldRemove)
            return;

        // Save current color components to avoid reference issues
        float oldR = batch.getColor().r;
        float oldG = batch.getColor().g;
        float oldB = batch.getColor().b;
        float oldA = batch.getColor().a;

        // Apply transparency while keeping current ambient color
        // Apply transparency while keeping current ambient color
        if (isCharged) {
            batch.setColor(0.2f, 1.0f, 1.0f, 1.0f); // Cyan/Bright effect
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

        // Restore original color
        batch.setColor(oldR, oldG, oldB, oldA);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        // texture.dispose();

    }
}
