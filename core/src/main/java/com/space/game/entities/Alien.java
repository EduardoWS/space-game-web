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

    // Hitbox Offsets
    private float boundsOffsetX = 0;
    private float boundsOffsetY = 0;

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
                this.speed = GameConfig.BABY_BOOMER_SPEED;
                break;
            case 4: // Boss Boomer
                this.type = AlienType.BOSS_BOOMER;
                this.texture = textureManager.getTexture("alienBoomer");
                this.scale = GameConfig.BOSS_BOOMER_SCALE;
                this.hp = GameConfig.BOSS_HP;
                this.maxHp = this.hp;
                this.speed = GameConfig.BOSS_BOOMER_SPEED;
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

        float width = texture.getWidth() * this.scale;
        float height = texture.getHeight() * this.scale;

        // Custom Boss Hitbox
        if (type == AlienType.BOSS_BOOMER) {
            float topTrim = 50f; // Cut off top
            float bottomTrim = 100f; // Cut off bottom (larger cut)

            height = height - topTrim - bottomTrim;
            boundsOffsetY = bottomTrim;
            // Width adjustments if needed? Let's keep width mostly standard but maybe
            // slightly tighter
            float sideTrim = 50f;
            width = width - (sideTrim * 2);
            boundsOffsetX = sideTrim;

            bounds = new Rectangle(position.x + boundsOffsetX, position.y + boundsOffsetY, width, height);
        } else {
            // Standard Logic
            bounds = new Rectangle(position.x - boundsPadding / 2, position.y - boundsPadding / 2,
                    width + boundsPadding, height + boundsPadding);
        }

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

        // Boss Detonation logic
        if (isDetonating) {
            detonationTimer -= deltaTime;
            // No movement updates
            return;
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

        updateBoundsPosition();
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

        updateBoundsPosition();
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

        updateBoundsPosition();
    }

    public void render(SpriteBatch batch) {
        if (!isMarkedForRemoval) {
            float oldR = batch.getColor().r;
            float oldG = batch.getColor().g;
            float oldB = batch.getColor().b;
            float oldA = batch.getColor().a;

            // Apply Swelling scale if detonating
            float currentScale = scale;
            if (isDetonating) {
                // Linear Inflation: 1.0 -> 1.3
                // detonationTimer goes from 2.0 -> 0.0
                float progress = 1.0f - (detonationTimer / DETONATION_TIME);
                float maxInflation = 0.3f; // 30% bigger
                currentScale = scale * (1.0f + progress * maxInflation);

                // Red/Orange tint warning
                batch.setColor(1f, MathUtils.random(0.5f, 1f), 0f, oldA);
            } else if (isDead) { // Dead State
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

            batch.draw(texture, position.x, position.y, texture.getWidth() * currentScale,
                    texture.getHeight() * currentScale);

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

    // Detonation Logic
    private boolean isDetonating = false;
    private float detonationTimer = 0f;
    private final float DETONATION_TIME = 2.0f; // 2 seconds warning

    public void startDetonation() {
        if (!isDetonating) {
            isDetonating = true;
            detonationTimer = DETONATION_TIME;
        }
    }

    public boolean isReadyToExplode() {
        return isDetonating && detonationTimer <= 0;
    }

    public boolean isDetonating() {
        return isDetonating;
    }

    public void applyKnockback(float force) {
        if (type == AlienType.BOSS_BOOMER) {
            // force *= 0.1f; // REMOVED resistance to ensure movement
            // Actually, ensure we use a reasonable multiplier
            force *= 0.3f;
        }

        if (isDead || isDetonating) // Don't move if detonating
            return;

        float centerX = SpaceGame.getGame().getWorldWidth() / 2f;
        float centerY = SpaceGame.getGame().getWorldHeight() / 2f;

        // Push away from center
        Vector2 knockDir = new Vector2(position.x - centerX, position.y - centerY).nor();

        // Remove Delta Time dependency for "Impulse" feel
        // Assume force is "Pixels of shove"
        float shoveDistance = force * 0.05f; // Tune this: 800 * 0.05 = 40 pixels shove

        position.x += knockDir.x * shoveDistance;
        position.y += knockDir.y * shoveDistance;
        updateBoundsPosition();
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

    private void updateBoundsPosition() {
        if (type == AlienType.BOSS_BOOMER) {
            bounds.setPosition(position.x + boundsOffsetX, position.y + boundsOffsetY);
        } else {
            // Maintain original padding logic offset
            // Original was: position.x - boundsPadding/2
            // But we didn't store boundsPadding as a field.
            // However, bounds.width is (textureW * scale + padding).
            // We can just center it relative to position if we assume position is top-left
            // of texture...
            // Wait, the original code did:
            // bounds = new Rectangle(position.x - boundsPadding / 2, ...
            // bounds.setPosition(position.x, position.y); <-- WAIT.

            // BUG FINDING:
            // The original code initialized bounds with -padding/2.
            // BUT in update/move methods, it did `bounds.setPosition(position.x,
            // position.y)`.
            // `Rectangle.setPosition` sets the X,Y of the rectangle.
            // checks: position.x is the Alien's top-left (presumably).
            // If we set bounds.x = position.x, we LOSE the padding offset (-7f).
            // So the original code actually shifted the hitbox slightly to the right/up
            // relative to the "padded" intention
            // immediately after the first frame update.

            // Let's stick to simple: bounds should track position.
            // If we want to preserve the "padding", we should probably apply it here too.
            // But since I don't want to change behavior of normal aliens unexpectedly:
            // I will use `bounds.setPosition(position.x - 7f, position.y - 7f)` roughly?
            // Or just stick to what `moveLinearly` was doing:
            // `bounds.setPosition(position.x, position.y)`.
            // If `moveLinearly` was doing that, then the padding in constructor was
            // effectively ignored for X/Y
            // after the first movement update, but the WIDTH/HEIGHT remained larger.
            // So the hitbox was slightly offset (shifted +7,+7 relative to intended
            // centered padding).

            // I will STRICTLY replicate the previous behavior for normal aliens:
            bounds.setPosition(position.x, position.y);
        }
    }

    public void dispose() {
        // texture.dispose();
    }
}
