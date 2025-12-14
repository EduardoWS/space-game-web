package com.space.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.space.game.SpaceGame;
import com.space.game.graphics.TextureManager;
import com.space.game.managers.BulletManager;
import com.badlogic.gdx.math.Rectangle;

public class Spaceship {
    private Texture texture;
    private float energy;
    private int streak;
    private int consecutiveKills;
    private int kills;
    private BulletManager bulletManager;
    private float angle = 0;

    private float x_nave, y_nave;

    private float scale;
    private Vector2 position = new Vector2(0, 0);

    // Energy Constants
    public static final float MAX_ENERGY = 100.0f;
    public static final float FIRE_COST = 0.75f;
    public static final float ROTATE_COST = 0.0075f;

    public Spaceship(TextureManager textureManager, BulletManager bulletManager) {

        this.texture = textureManager.getTexture("spaceship");

        scale = Math.min(SpaceGame.getGame().getWorldWidth() / (float) texture.getWidth(),
                SpaceGame.getGame().getWorldHeight() / (float) texture.getHeight());
        scale *= 0.075f;

        x_nave = SpaceGame.getGame().getWorldWidth() / 2f - texture.getWidth() * scale / 2f;
        y_nave = SpaceGame.getGame().getWorldHeight() / 2f - texture.getHeight() * scale / 2f;
        position = new Vector2(x_nave, y_nave);

        this.consecutiveKills = 0;

        this.energy = MAX_ENERGY; // Initialize with full energy
        this.bulletManager = bulletManager;

    }

    public void setBulletManager(BulletManager bulletManager) {
        this.bulletManager = bulletManager;
    }

    public void incrementKillCount() {
        if (this.streak != 1) {
            this.kills = this.kills + this.streak;
        } else {
            this.kills++;
        }
    }

    public int getKillCount() {
        return kills;
    }

    public void setKillCount(int kills) {
        this.kills = kills;
    }

    public void incrementStreakCount() {
        this.streak++;
    }

    public void setStreakCount(int streak) {
        this.streak = streak;
    }

    public int getStreakCount() {
        return streak;
    }

    public void incrementCosecutiveKills() {
        this.consecutiveKills++;
    }

    public void setConsecutiveKills(int consecutiveKills) {
        this.consecutiveKills = consecutiveKills;
    }

    public int getConsecutiveKills() {
        return consecutiveKills;
    }

    public void fire() {
        if (energy >= FIRE_COST) {
            bulletManager.fireBullet(new Vector2(position.x, position.y), angle, texture.getWidth(),
                    texture.getHeight(), scale);
            consumeEnergy(FIRE_COST);
        } // adicionar som de sem energia
    }

    public boolean consumeRotationEnergy() {
        if (energy >= ROTATE_COST) {
            consumeEnergy(ROTATE_COST);
            return true;
        }
        return false;
    }

    private void consumeEnergy(float amount) {
        this.energy -= amount;
        if (this.energy < 0)
            this.energy = 0;
    }

    public float getScale() {
        return scale;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void addEnergy(float amount) {
        this.energy += amount;
        if (this.energy > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        }
    }

    public void setEnergy(float energy) {
        this.energy = energy;
        if (this.energy > MAX_ENERGY)
            this.energy = MAX_ENERGY;
        if (this.energy < 0)
            this.energy = 0;
    }

    public float getEnergy() {
        return energy;
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, texture.getWidth() * scale, texture.getHeight() * scale);
    }

    public void update() {

    }

    public void render(SpriteBatch batch) {
        // Desenha a textura da nave com a rotação e a escala aplicadas
        batch.draw(texture,
                position.x, position.y, // x e y da posição da nave
                texture.getWidth() / 2, texture.getHeight() / 2, // x e y do ponto de origem da rotação
                texture.getWidth(), texture.getHeight(), // largura e altura da textura
                scale, scale, // escala em x e y
                angle, 0, 0, // rotação e coordenadas da textura
                texture.getWidth(), texture.getHeight(), // srcWidth e srcHeight (largura e altura da textura original)
                false, false); // flip horizontal e vertical
    }

    public void dispose() {
        texture.dispose();
    }

}
