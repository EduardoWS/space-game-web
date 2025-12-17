package com.space.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
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
    public static final float FIRE_COST = 0.66f;
    public static final float ROTATE_COST = 0.0075f;

    // Charged Shot Constants
    public static final float CHARGED_FIRE_COST = 10.0f; // Reduced from 20
    public static final float CHARGE_DRAIN_RATE = 5.0f; // Reduced from 10
    public static final float MAX_CHARGE_TIME = 5.0f;
    public static final float CHARGE_THRESHOLD = 0.5f; // Time in seconds to hold before charging starts

    private boolean isCharging = false;
    private boolean spaceHeld = false;
    private float chargeTimer = 0f;

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
        // Normal fire
        if (energy >= FIRE_COST) {
            bulletManager.fireBullet(new Vector2(position.x, position.y), angle, texture.getWidth(),
                    texture.getHeight(), scale, false); // false = not charged
            consumeEnergy(FIRE_COST);
            SpaceGame.getGame().getSoundManager().playBulletSound();
        }
    }

    public void startCharging() {
        spaceHeld = true;
        chargeTimer = 0f;
        // Do not start immediate charge state. Wait for threshold in update.
    }

    public void releaseCharge() {
        if (!spaceHeld)
            return; // Already handled (e.g. auto-fired)

        if (isCharging) {
            fireChargedShot();
        } else {
            // Released before threshold -> Normal Shot
            fire();
        }

        // Reset state
        resetChargeState();
    }

    private void resetChargeState() {
        spaceHeld = false;
        isCharging = false;
        chargeTimer = 0f;
        SpaceGame.getGame().getSoundManager().stopChargingSound();
    }

    private void fireChargedShot() {
        // Only fire if we are actually charging or forced by logic

        bulletManager.fireBullet(new Vector2(position.x, position.y), angle, texture.getWidth(),
                texture.getHeight(), scale, true); // true = charged
        SpaceGame.getGame().getSoundManager().playBulletSound();

        // Reset state handled by caller usually, but if auto-fire, we must reset here
        // If called by releaseCharge, resetChargeState will be called immediately
        // after.
        // If called by updateCharging (auto-fire), we must reset.
    }

    public void updateCharging(float delta) {
        if (spaceHeld) {
            chargeTimer += delta;

            // Check if we should START charging
            if (!isCharging && chargeTimer >= CHARGE_THRESHOLD) {
                if (energy >= CHARGED_FIRE_COST) {
                    isCharging = true;
                    SpaceGame.getGame().getSoundManager().playChargingSound();
                    consumeEnergy(5.0f); // Initial cost
                } else {
                    // Not enough energy to start charging.
                    // We just keep waiting. If user releases, it will fire normal shot (if enough
                    // energy for that).
                }
            }

            // Logic while charging
            if (isCharging) {
                float drain = CHARGE_DRAIN_RATE * delta;
                consumeEnergy(drain);

                // Auto-fire checks
                if (chargeTimer >= MAX_CHARGE_TIME || energy <= 0) {
                    fireChargedShot();
                    resetChargeState(); // Stop charging, sound, and mark space as not held so keyUp doesn't fire again
                }

                // Visual Effect
                // -- CUSTOMIZATION START --
                // O centro da nave para a renderização visual é baseada na largura/altura
                // original (origem da rotação)
                float centerX = position.x + texture.getWidth() / 2f;
                float centerY = position.y + texture.getHeight() / 2f;

                // Comprimento do centro até a ponta (Raio) ajustado pela escala
                float len = (texture.getHeight() * scale) / 2f;

                // Ajuste de ângulo e posição da ponta
                float tipX = centerX + MathUtils.cosDeg(angle + 90) * len;
                float tipY = centerY + MathUtils.sinDeg(angle + 90) * len;

                // Espalhamento das partículas (Aumente para ficar mais disperso)
                float spread = 15.0f; // Increased spread from 5.0f to 15.0f
                tipX += MathUtils.random(-spread, spread);
                tipY += MathUtils.random(-spread, spread);

                if (SpaceGame.getGame().getMapManager().getParticleManager() != null) {
                    // Reduce frequency: only spawn 50% of the frames
                    if (MathUtils.randomBoolean(0.5f)) {
                        SpaceGame.getGame().getMapManager().getParticleManager().createChargeParticle(tipX, tipY);
                    }
                }
                // -- CUSTOMIZATION END --
            }
        }
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

    public void update(float delta) {
        updateCharging(delta);
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
