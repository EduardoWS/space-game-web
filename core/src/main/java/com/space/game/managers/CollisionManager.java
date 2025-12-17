package com.space.game.managers;

import java.util.Iterator;
import java.util.List;

import com.space.game.entities.Alien;
import com.space.game.entities.Bullet;
import com.space.game.entities.Spaceship;

public class CollisionManager {
    private BulletManager bulletManager;
    private List<Alien> aliens;
    private Spaceship spaceship;
    private SoundManager soundManager;

    private ParticleManager particleManager;

    public CollisionManager(BulletManager bulletManager, AlienManager alienManager, Spaceship spaceship,
            SoundManager soundManager, ParticleManager particleManager) {
        this.soundManager = soundManager;
        this.bulletManager = bulletManager;
        this.spaceship = spaceship;
        this.aliens = alienManager.getAliens();
        this.particleManager = particleManager;
    }

    public void checkBulletCollisions() {
        List<Bullet> bullets = bulletManager.getBullets();
        Iterator<Bullet> bulletIterator = bullets.iterator();
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
                            soundManager.playDeadAlienHitSound(); // Or bigger boom sound
                            spaceship.addEnergy(5.0f); // Reward for charged kill?
                            // Big explosion
                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 20);
                            }
                            spaceship.incrementKillCount();

                            // Multi-kill Score Bonus
                            bullet.incrementKillCount();
                            int combo = bullet.getKillCount();
                            if (combo > 1) {
                                // Bonus: More score for each subsequent alien hit by the same bullet
                                // Example: 2nd hit -> +10, 3rd -> +20, etc.
                                int bonus = (combo - 1) * 10;
                                spaceship.setKillCount(spaceship.getKillCount() + bonus);
                            }

                            spaceship.incrementCosecutiveKills();
                        }
                        // Do NOT mark bullet for removal (piercing)
                    } else {
                        // Normal shot logic
                        bullet.markForRemoval();
                        if (!alien.isDead()) {
                            // Se o alien não está morto, marcar como atingido pela primeira vez.
                            spaceship.incrementKillCount();
                            alien.hit(); // Muda a textura e inverte a direção.
                            soundManager.playAlienHitSound();
                            spaceship.incrementCosecutiveKills();
                            // Small sparks/hit effect
                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 5);
                            }
                        } else {
                            // Se já está morto e foi atingido novamente, marcar para remoção.
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound();
                            spaceship.addEnergy(2.5f);
                            // Big explosion
                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 20);
                            }
                        }
                    }

                    // Se o streak não está no máximo e o jogador fez 3 kills consecutivos,
                    // incrementar streak.
                    if (spaceship.getStreakCount() < 7 && spaceship.getConsecutiveKills() >= 3) {
                        spaceship.incrementStreakCount();
                    }
                }
            }
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
