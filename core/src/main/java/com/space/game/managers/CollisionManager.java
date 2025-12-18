package com.space.game.managers;

import java.util.Iterator;
import java.util.List;

import com.space.game.entities.Alien;
import com.space.game.entities.Bullet;
import com.space.game.entities.Spaceship;
import com.space.game.SpaceGame;

public class CollisionManager {
    private BulletManager bulletManager;
    private List<Alien> aliens;
    private Spaceship spaceship;
    private SoundManager soundManager;
    private UIManager uiManager;

    private ParticleManager particleManager;

    public CollisionManager(BulletManager bulletManager, AlienManager alienManager, Spaceship spaceship,
            SoundManager soundManager, ParticleManager particleManager) {
        this.soundManager = soundManager;
        this.bulletManager = bulletManager;
        this.spaceship = spaceship;
        this.aliens = alienManager.getAliens();
        this.aliens = alienManager.getAliens();
        this.particleManager = particleManager;
        this.uiManager = SpaceGame.getGame().getUiManager();
    }

    private boolean explosionKilledPlayer = false;

    public void checkBulletCollisions() {
        List<Bullet> bullets = bulletManager.getBullets();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        int frameTotalScore = 0;

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                if (alien.getBounds().overlaps(bullet.getBounds())) {

                    boolean killed = false;

                    if (bullet.isCharged()) {
                        // Charged Shot
                        if (!alien.isDead()) {
                            // Boomer Logic
                            if (alien.getType() == Alien.AlienType.BABY_BOOMER
                                    || alien.getType() == Alien.AlienType.BOSS_BOOMER) {
                                killed = alien.takeDamage(100); // Massive damage
                                if (killed) {
                                    // Disintegrate (Silent)
                                    // No explosion, just removal/particle
                                    if (particleManager != null) {
                                        particleManager.createExplosion(
                                                alien.getBounds().x + alien.getBounds().width / 2,
                                                alien.getBounds().y + alien.getBounds().height / 2, 50);
                                    }
                                }
                            } else {
                                // Normal Alien Instant Kill
                                alien.markForImmediateRemoval();
                                killed = true;
                            }

                            if (killed || alien.isDead()) { // Process killed or previously dead
                                if (killed) { // Only give rewards if we actually killed it
                                    soundManager.playDeadAlienHitSound();
                                    spaceship.addEnergy(5.0f);
                                    uiManager.addEnergyFeedback(5.0f);

                                    if (particleManager != null && !(alien.getType() == Alien.AlienType.BABY_BOOMER
                                            || alien.getType() == Alien.AlienType.BOSS_BOOMER)) {
                                        // Normal explosion if not handled above
                                        particleManager.createExplosion(
                                                alien.getBounds().x + alien.getBounds().width / 2,
                                                alien.getBounds().y + alien.getBounds().height / 2, 50);
                                    }

                                    int scoreGain = spaceship.getStreakCount();
                                    if (spaceship.getStreakCount() == 0)
                                        scoreGain = 1;

                                    spaceship.incrementKillCount();
                                    bullet.incrementKillCount();
                                    int combo = bullet.getKillCount();
                                    if (combo > 1) {
                                        int bonus = (combo - 1) * 10;
                                        scoreGain = bonus + 10;
                                        spaceship.setKillCount(spaceship.getKillCount() + bonus);
                                    }
                                    frameTotalScore += scoreGain;
                                    spaceship.incrementCosecutiveKills();
                                } else {
                                    // Hitting dead/dying alien
                                    alien.markForImmediateRemoval();
                                    soundManager.playDeadAlienHitSound();
                                    spaceship.addEnergy(2.5f);
                                    uiManager.addEnergyFeedback(2.5f);
                                }
                            }
                        } else {
                            // Hit already dead alien
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound();
                            spaceship.addEnergy(2.5f);
                            uiManager.addEnergyFeedback(2.5f);
                        }
                    } else {
                        // Normal Shot
                        bullet.markForRemoval();
                        if (!alien.isDead()) {
                            // Boomer Logic
                            if (alien.getType() == Alien.AlienType.BABY_BOOMER
                                    || alien.getType() == Alien.AlienType.BOSS_BOOMER) {
                                alien.applyKnockback(com.space.game.config.GameConfig.BOOMER_KNOCKBACK_FORCE);
                                killed = alien.takeDamage(1);
                                if (killed) {
                                    explode(alien);
                                } else {
                                    // Hit feedback
                                    soundManager.playAlienHitSound(); // Maybe different sound?
                                }
                            } else {
                                // Normal Alien
                                int scoreGain = spaceship.getStreakCount();
                                if (spaceship.getStreakCount() == 0)
                                    scoreGain = 1;
                                spaceship.incrementKillCount();
                                uiManager.addScoreFeedback(scoreGain);

                                alien.hit();
                                killed = true; // Technically hit() initiates death
                                soundManager.playAlienHitSound();
                                spaceship.incrementCosecutiveKills();
                                if (particleManager != null) {
                                    particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                            alien.getBounds().y + alien.getBounds().height / 2, 20);
                                }
                            }
                        } else {
                            // Hit dead
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound(); // Feedback
                            spaceship.addEnergy(2.5f);
                            uiManager.addEnergyFeedback(2.5f);

                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 50);
                            }
                        }
                    }

                    if (spaceship.getStreakCount() < 7 && spaceship.getConsecutiveKills() >= 3) {
                        spaceship.incrementStreakCount();
                    }
                }
            }
        }

        if (frameTotalScore > 0) {
            uiManager.addScoreFeedback(frameTotalScore);
        }
    }

    private void explode(Alien boomer) {
        float x = boomer.getPosition().x + boomer.getBounds().width / 2;
        float y = boomer.getPosition().y + boomer.getBounds().height / 2;
        float radius = (boomer.getType() == Alien.AlienType.BABY_BOOMER)
                ? com.space.game.config.GameConfig.BABY_EXPLOSION_RADIUS
                : com.space.game.config.GameConfig.BOSS_EXPLOSION_RADIUS;

        if (particleManager != null) {
            particleManager.createExplosion(x, y, 100); // Visuals
        }

        // Damage Aliens
        for (Alien a : aliens) {
            if (!a.isDead() && a != boomer) {
                float dst = new com.badlogic.gdx.math.Vector2(x, y).dst(new com.badlogic.gdx.math.Vector2(
                        a.getPosition().x + a.getBounds().width / 2, a.getPosition().y + a.getBounds().height / 2));
                if (dst < radius) {
                    a.hit(); // Kill/Damage nearby
                }
            }
        }

        // Check Player
        float scale = spaceship.getScale();
        float px = spaceship.getPosition().x + spaceship.getBounds().width * scale / 2;
        float py = spaceship.getPosition().y + spaceship.getBounds().height * scale / 2;

        if (new com.badlogic.gdx.math.Vector2(x, y).dst(new com.badlogic.gdx.math.Vector2(px, py)) < radius) {
            explosionKilledPlayer = true;
        }
    }

    public boolean checkSpaceshipCollisions() {
        if (explosionKilledPlayer)
            return true;

        Iterator<Alien> alienIterator = aliens.iterator();
        while (alienIterator.hasNext()) {
            Alien alien = alienIterator.next();
            if (spaceship.getBounds().overlaps(alien.getBounds())) {
                // If Boomer touches player -> Explode (and Kill)
                if ((alien.getType() == Alien.AlienType.BABY_BOOMER || alien.getType() == Alien.AlienType.BOSS_BOOMER)
                        && !alien.isDead()) {
                    explode(alien);
                    alien.hit(); // Kill alien too
                    return true; // Game Over
                }
                return true;
            }
        }
        return false;
    }
}
