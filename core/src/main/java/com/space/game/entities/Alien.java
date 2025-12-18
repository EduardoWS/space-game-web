package com.space.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.space.game.SpaceGame;
import com.space.game.graphics.TextureManager;
import com.space.game.entities.movements.MovementStrategy;

public abstract class Alien {
    protected Texture texture;
    protected TextureManager textureManager;
    protected Vector2 position;
    protected float speed;
    protected boolean isDead = false;
    protected float scale;
    protected Rectangle bounds;
    protected float deathTimer = 0;
    protected boolean isMarkedForRemoval = false;
    protected final float TIME_TO_REMOVE = 2; // Tempo antes da remoção
    protected float hitTimer = 0;
    protected int hp;
    protected int maxHp;
    protected MovementStrategy strategy;
    protected AlienType type;

    public enum AlienType {
        NORMAL, BABY_BOOMER, BOSS_BOOMER
    }

    public Alien(TextureManager textureManager, Vector2 position, float scale, float speed, MovementStrategy strategy,
            AlienType type) {
        this.textureManager = textureManager;
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.strategy = strategy;
        this.type = type;

        // Default bounds initialization - Override in subclasses if needed
        // Assuming texture is loaded by subclass constructor before or after?
        // Subclasses should load texture and THEN call super? No, super is called
        // first.
        // Subclasses must set texture and then initializeBounds().
    }

    protected void initializeBounds() {
        if (texture == null)
            return;
        float width = texture.getWidth() * this.scale;
        float height = texture.getHeight() * this.scale;
        this.bounds = new Rectangle(position.x, position.y, width, height);
    }

    public abstract void update(float deltaTime, Spaceship spaceship);

    public void render(SpriteBatch batch) {
        if (!isMarkedForRemoval) {
            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;

            applyRenderEffects(batch, oldG, oldB, oldA);

            batch.draw(texture, position.x, position.y, texture.getWidth() * scale,
                    texture.getHeight() * scale);

            batch.setColor(oldR, oldG, oldB, oldA);
        }
    }

    protected void applyRenderEffects(SpriteBatch batch, float oldG, float oldB, float oldA) {
        // Base implementation (Normal hit flash)
        if (hitTimer > 0) {
            if (MathUtils.randomBoolean()) {
                batch.setColor(1, 1, 1, oldA);
            } else {
                batch.setColor(1, 1, 0, oldA);
            }
        } else if (isDead) { // Is Dead -> Red Tint
            // Use set color to Tint RED like original
            batch.setColor(1.0f, 0.2f, 0.2f, oldA); // Hard red tint
        }
    }

    public boolean takeDamage(int damage) {
        if (isDead)
            return false;

        hp -= damage;
        hitTimer = 0.1f;

        if (hp <= 0) {
            hp = 0;
            isDead = true;
            deathTimer = 0;
            onDeath();
            return true;
        }
        return false;
    }

    protected abstract void onDeath();

    public boolean shouldRemove() {
        return isMarkedForRemoval || (isDead && deathTimer > TIME_TO_REMOVE);
    }

    public void applyKnockback(float force) {
        if (isDead)
            return; // Simplified: No knockback on death for now, or implement in subclasses

        float centerX = SpaceGame.getGame().getWorldWidth() / 2f;
        float centerY = SpaceGame.getGame().getWorldHeight() / 2f;
        Vector2 knockDir = new Vector2(position.x - centerX, position.y - centerY).nor();

        float shoveDistance = force * 0.05f;
        position.x += knockDir.x * shoveDistance;
        position.y += knockDir.y * shoveDistance;
        if (bounds != null)
            bounds.setPosition(position.x, position.y);
    }

    public void markForImmediateRemoval() {
        isMarkedForRemoval = true;
    }

    public void hit() {
        takeDamage(1);
    }

    public void setStrategy(MovementStrategy strategy) {
        this.strategy = strategy;
    }

    // Boomer/Boss methods (Default implementation)
    public void startDetonation() {
    }

    public boolean isReadyToExplode() {
        return false;
    }

    public boolean isDetonating() {
        return false;
    }

    public void dispose() {
    }

    // Getters and Setters
    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    } // Used by Spiral?

    public boolean isDead() {
        return isDead || deathTimer > TIME_TO_REMOVE;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public AlienType getType() {
        return type;
    }

}
