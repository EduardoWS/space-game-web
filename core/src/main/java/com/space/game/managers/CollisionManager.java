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

    public void checkBulletCollisions() {
        List<Bullet> bullets = bulletManager.getBullets();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        int frameTotalScore = 0; // Aggregate score for charged shots in this frame

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<Alien> alienIterator = aliens.iterator();
            while (alienIterator.hasNext()) {
                Alien alien = alienIterator.next();
                if (alien.getBounds().overlaps(bullet.getBounds())) {
                    if (bullet.isCharged()) {
                        // Charged shot logic: Pierce and instant kill
                        if (!alien.isDead()) {
                            alien.markForImmediateRemoval(); // Instant kill
                            soundManager.playDeadAlienHitSound();
                            spaceship.addEnergy(5.0f);
                            uiManager.addEnergyFeedback(5.0f); // Feedback

                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 50);
                            }

                            int scoreGain = spaceship.getStreakCount();
                            if (spaceship.getStreakCount() == 0)
                                scoreGain = 1; // Minimum 1

                            spaceship.incrementKillCount();

                            // Multi-kill Score Bonus
                            bullet.incrementKillCount();
                            int combo = bullet.getKillCount();
                            if (combo > 1) {
                                int bonus = (combo - 1) * 10;
                                scoreGain = bonus + 10;
                                spaceship.setKillCount(spaceship.getKillCount() + bonus);
                            }

                            // Aggregate score
                            frameTotalScore += scoreGain;

                            spaceship.incrementCosecutiveKills();
                        } else {
                            // Hit dead alien with charged shot
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound();
                            spaceship.addEnergy(2.5f);
                            uiManager.addEnergyFeedback(2.5f);

                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 50);
                            }
                        }
                    } else {
                        // Normal shot logic
                        bullet.markForRemoval();
                        if (!alien.isDead()) {
                            int scoreGain = spaceship.getStreakCount();
                            if (spaceship.getStreakCount() == 0)
                                scoreGain = 1;

                            spaceship.incrementKillCount();
                            uiManager.addScoreFeedback(scoreGain); // Immediate feedback for normal shot

                            alien.hit();
                            soundManager.playAlienHitSound();
                            spaceship.incrementCosecutiveKills();
                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 20);
                            }
                        } else {
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound();
                            spaceship.addEnergy(2.5f);
                            uiManager.addEnergyFeedback(2.5f); // Feedback

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

        // Report aggregated charged shot score
        if (frameTotalScore > 0) {
            uiManager.addScoreFeedback(frameTotalScore);
        }
    }

    public boolean checkSpaceshipCollisions() {
        // Verifica colisões entre a spaceship e aliens
        Iterator<Alien> alienIterator = aliens.iterator();
        while (alienIterator.hasNext()) {
            Alien alien = alienIterator.next();
            if (spaceship.getBounds().overlaps(alien.getBounds())) {
                return true;
            }
        }
        return false;
    }
}
