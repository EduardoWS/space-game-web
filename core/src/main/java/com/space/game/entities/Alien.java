package com.space.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.space.game.SpaceGame;
import com.space.game.config.GameConfig;
import com.space.game.graphics.TextureManager;

public class Alien {
    private Texture texture;
    private TextureManager textureManager;
    private Vector2 position;
    private float speed;
    private boolean isDead = false;
    private float scale;
    private Rectangle bounds;
    private float deathTimer = Gdx.graphics.getDeltaTime();
    private boolean isMarkedForRemoval = false;
    private final float TIME_TO_REMOVE = 2; // Tempo em segundos antes da remoção

    private int movementPattern;
    private float elapsedTime;
    private float waveAmplitude;
    private float waveFrequency;
    private float radiusDecay;
    private float angleSpeed;
    private int signal_x;
    private int signal_y;

    // New Fields
    private float hitTimer = 0;
    private int hp;
    private int maxHp;
    private AlienType type;

    public enum AlienType {
        NORMAL, BABY_BOOMER, BOSS_BOOMER
    }

    public Alien(TextureManager textureManager, Vector2 position, float scale, float speed, Spaceship spaceship,
            int movementPattern) {
        this.position = position;
        this.speed = speed;
        this.textureManager = textureManager;
        this.movementPattern = movementPattern;

        // Default type
        this.type = AlienType.NORMAL;
        this.hp = 1;
        this.maxHp = 1;

        // Determine Type and Texture
        switch (movementPattern) {
            case 0: // Linear (Base 64x64 -> Target 80x80)
                this.texture = textureManager.getTexture("alienLinear");
                this.scale = scale * 1.25f;
                // Apply specific scale for Boomers via config if this was reused, but 0 is
                // Linear.
                break;
            case 1: // Wave (Base 80x80 -> Target 90x90)
                this.texture = textureManager.getTexture("alienWave");
                this.scale = scale * 1.125f;
                break;
            case 2: // Spiral (Base 80x80 -> Target 90x90)
                this.texture = textureManager.getTexture("alienSpiral");
                this.scale = scale * 1.125f;
                break;
            case 3: // Baby Boomer
                this.type = AlienType.BABY_BOOMER;
                this.texture = textureManager.getTexture("alienBoomer");
                this.scale = GameConfig.BABY_BOOMER_SCALE;
                this.hp = GameConfig.BABY_HP;
                this.maxHp = this.hp;
                break;
            case 4: // Boss Boomer
                this.type = AlienType.BOSS_BOOMER;
                this.texture = textureManager.getTexture("alienBoomer");
                this.scale = GameConfig.BOSS_BOOMER_SCALE;
                this.hp = GameConfig.BOSS_HP;
                this.maxHp = this.hp;
                break;
            default:
                this.texture = textureManager.getTexture("alienLinear");
                this.scale = scale;
                break;
        }

        // If scale wasn't set by Boomer logic, use passed scale
        if (this.scale == 0)
            this.scale = scale;

        float boundsPadding = 14f; // Ajuste este valor para aumentar a área de colisão
        bounds = new Rectangle(position.x - boundsPadding / 2, position.y - boundsPadding / 2,
                texture.getWidth() * this.scale + boundsPadding, texture.getHeight() * this.scale + boundsPadding);

        // Initialize variables for sine wave and spiral movements
        waveAmplitude = MathUtils.random(SpaceGame.getGame().getWorldHeight() / 9,
                SpaceGame.getGame().getWorldHeight() / 5);
        waveFrequency = MathUtils.random(1, 5);

        // Calcula a direção para o centro da nave
        radiusDecay = speed; // Fator de decaimento do raio
        angleSpeed = SpaceGame.getGame().getWorldWidth() / 3840; // Velocidade angular da espiral

        elapsedTime = MathUtils.random(0, 5); // Randomize the starting time (0 to 5 seconds)

        signal_x = MathUtils.random(0, 1) == 0 ? -1 : 1;
        signal_y = MathUtils.random(0, 1) == 0 ? -1 : 1;
    }

    public void setMovementPattern(int movementPattern) {
        this.movementPattern = movementPattern;
    }

    public void update(float deltaTime, Spaceship spaceship) {
        if (hitTimer > 0) {
            hitTimer -= deltaTime;
        }

        if (isDead) {
            deathTimer += deltaTime;
            if (type == AlienType.NORMAL) {
                // Classic death movement for normal aliens
                // We need to move them backward, but previous logic used 'speed' which was
                // inverted.
                // let's just reuse linear movement with current speed (which is inverted in
                // 'hit()').
                moveLinearly(deltaTime, spaceship);
            }
            return;
        }

        switch (movementPattern) {
            case 0:
                moveLinearly(deltaTime, spaceship);
                speed += (deltaTime * speed / MathUtils.random(12, 16));
                break;
            case 1:
                moveInWave(deltaTime, spaceship);
                speed += (deltaTime * speed / MathUtils.random(14, 18));
                break;
            case 2:
                moveInSpiral(deltaTime, spaceship);
                speed += (deltaTime * speed / MathUtils.random(12, 20));
                break;
            case 3: // Baby Boomer
            case 4: // Boss Boomer
                moveLinearly(deltaTime, spaceship); // Tracking
                break;
            default:
                moveLinearly(deltaTime, spaceship);
                break;
        }
    }

    private void moveLinearly(float deltaTime, Spaceship spaceship) {
        float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
        float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

        float alienCenterX = position.x + bounds.width * scale / 2;
        float alienCenterY = position.y + bounds.height * scale / 2;

        Vector2 direction = new Vector2(naveCenterX - alienCenterX, naveCenterY - alienCenterY);
        direction.nor();

        position.x += direction.x * speed * deltaTime;
        position.y += direction.y * speed * deltaTime;

        bounds.setPosition(position.x, position.y);
    }

    private void moveInWave(float deltaTime, Spaceship spaceship) {
        float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
        float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

        float alienCenterX = position.x + bounds.width * scale / 2;
        float alienCenterY = position.y + bounds.height * scale / 2;

        Vector2 direction = new Vector2(naveCenterX - alienCenterX, naveCenterY - alienCenterY);
        direction.nor();

        position.x += direction.x * speed * deltaTime;
        position.y += direction.y * speed * deltaTime;

        elapsedTime += deltaTime;

        float waveOffset = waveAmplitude * (float) Math.sin(waveFrequency * elapsedTime) * deltaTime;

        Vector2 perpendicularDirection = new Vector2(-direction.y, direction.x);
        position.x += perpendicularDirection.x * waveOffset;
        position.y += perpendicularDirection.y * waveOffset;

        bounds.setPosition(position.x, position.y);
    }

    private void moveInSpiral(float deltaTime, Spaceship spaceship) {
        float naveCenterX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
        float naveCenterY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

        elapsedTime += deltaTime;

        float radius = Math.max(2,
                SpaceGame.getGame().getWorldHeight() - radiusDecay * elapsedTime * speed * deltaTime);

        float angle = angleSpeed * elapsedTime;

        position.x = naveCenterX + (signal_x * radius) * (float) Math.cos(angle);
        position.y = naveCenterY + (signal_y * radius) * (float) Math.sin(angle);

        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        if (!isMarkedForRemoval) {
            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;

            if (isDead) { // Dead State
                if (type == AlienType.NORMAL) {
                    float newR = 1.0f;
                    float newG = Math.max(0f, oldG - 0.6f);
                    float newB = Math.max(0f, oldB - 0.6f);
                    batch.setColor(newR, newG, newB, oldA);
                } else {
                    // Dead Boomer (if not exploded yet)
                    float blink = MathUtils.sin(deathTimer * 20);
                    if (blink > 0)
                        batch.setColor(1, 0, 0, oldA);
                    else
                        batch.setColor(1, 0.5f, 0, oldA);
                }
            } else if (hitTimer > 0) { // Hit Flash State
                if (MathUtils.randomBoolean()) {
                    batch.setColor(1, 1, 1, oldA); // White
                } else {
                    batch.setColor(1, 1, 0, oldA); // Yellow
                }
            }

            batch.draw(texture, position.x, position.y, texture.getWidth() * scale, texture.getHeight() * scale);

            batch.setColor(oldR, oldG, oldB, oldA);
        }
    }

    public void setTextureToDraw(String key) {
        texture = this.textureManager.getTexture(key);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean shouldRemove() {
        return isMarkedForRemoval || (isDead && deathTimer > TIME_TO_REMOVE);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void hit() {
        takeDamage(1);
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
            if (type == AlienType.NORMAL) {
                setMovementPattern(0);
                setSpeed(-speed / 2);
            }
            return true; // Killed
        }
        return false; // Not Killed
    }

    public void applyKnockback(float force) {
        if (type == AlienType.BOSS_BOOMER)
            force *= 0.1f; // Boss resists knockback

        if (isDead)
            return;

        float centerX = SpaceGame.getGame().getWorldWidth() / 2f;
        float centerY = SpaceGame.getGame().getWorldHeight() / 2f;

        // Push away from center
        Vector2 knockDir = new Vector2(position.x - centerX, position.y - centerY).nor();

        float delta = Gdx.graphics.getDeltaTime();
        // Just a simple immediate push, or change velocity?
        // Modifying position directly is easier for now, assuming knockback is instant
        // impulse
        // But force is usually over time or velocity. Let's just push it back a bit.
        // Force here can be interpreted as pixel distance to push?
        // User said "BOOMER_KNOCKBACK_FORCE". Let's assume it's velocity.
        // Since we don't have velocity vector persistent for Linear movement (it's
        // calculated),
        // we can just add to position.

        // To make it smooth, we could add a velocity vector to Alien, but that is a
        // larger refactor.
        // Let's do an instant shove.
        position.x += knockDir.x * force * delta;
        position.y += knockDir.y * force * delta;
        bounds.setPosition(position.x, position.y);
    }

    public void markForImmediateRemoval() {
        isMarkedForRemoval = true;
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

    public boolean isDead() {
        return isDead || deathTimer > TIME_TO_REMOVE;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        // texture.dispose();
    }
}
