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
    private com.space.game.managers.AlienManager alienManager; // Stored reference
    private Spaceship spaceship;
    private SoundManager soundManager;
    private com.space.game.managers.MusicManager musicManager;
    private UIManager uiManager;

    private ParticleManager particleManager;

    public CollisionManager(BulletManager bulletManager, AlienManager alienManager, Spaceship spaceship,
            SoundManager soundManager, com.space.game.managers.MusicManager musicManager,
            ParticleManager particleManager) {
        this.soundManager = soundManager;
        this.musicManager = musicManager;
        this.bulletManager = bulletManager;
        this.spaceship = spaceship;
        this.alienManager = alienManager; // Store it
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

                                if (alien.getType() == Alien.AlienType.BOSS_BOOMER) {
                                    // Charged Shot does fixed damage to Boss (Configurable)
                                    // Does NOT pass through (part of bullet collision logic usually handled by
                                    // caller,
                                    // but here we just mark bullet as hit if we want it to stop?
                                    // Bullet logic usually removes bullet on collision unless piercing.
                                    // Assuming bullet is removed by default unless specified otherwise.
                                    // Logic at line 116 marks removal for normal shot.
                                    // Bullet.isCharged usually pierces. logic needs check.

                                    killed = alien
                                            .takeDamage(com.space.game.config.GameConfig.CHARGED_SHOT_BOSS_DAMAGE);

                                    // Fix: Apply Knockback to Boss on Charged Shot
                                    alien.applyKnockback(com.space.game.config.ConfigUtils
                                            .scale(com.space.game.config.GameConfig.BOSS_CHARGED_KNOCKBACK_FORCE));

                                    bullet.markForRemoval(); // Stop charged shot on boss
                                    if (!killed) {
                                        soundManager.playAlienHitSound();
                                    }
                                } else {
                                    // Baby Boomer -> Instant Kill (Disintegrate)
                                    killed = alien.takeDamage(100);
                                }

                                if (killed) {
                                    // Disintegrate (Silent)
                                    // No explosion, just removal/particle
                                    if (particleManager != null) {
                                        // Green for disintegration (Acid/Plasma)
                                        com.badlogic.gdx.graphics.Color greenColor = new com.badlogic.gdx.graphics.Color(
                                                0f, 1f, 0f, 1f);
                                        particleManager.createExplosion(
                                                alien.getBounds().x + alien.getBounds().width / 2,
                                                alien.getBounds().y + alien.getBounds().height / 2, 50, greenColor);
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
                                    float energyGain = (alienManager.getConfig()
                                            .getLevelNumber() == com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL)
                                                    ? com.space.game.config.GameConfig.BOSS_MINION_ENERGY_GAIN
                                                    : 5.0f;

                                    // Passive Buff: +0.25% Energy per kill per boss (starts at 0 if 0 bosses)
                                    // User said: "starts 2.5%, then 2.75%". Wait, base is 5.0f?
                                    // If base 5.0f on 100 max is 5%.
                                    // Let's stick to adding the requested bonus.
                                    if (spaceship.getBossesDefeated() > 0) {
                                        energyGain += spaceship.getMaxEnergy()
                                                * (com.space.game.config.GameConfig.PASSIVE_ENERGY_PER_KILL_PERCENT
                                                        * spaceship.getBossesDefeated());
                                    }

                                    spaceship.addEnergy(energyGain);
                                    uiManager.addEnergyFeedback(energyGain);

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
                            // Hit dead
                            alien.markForImmediateRemoval();
                            soundManager.playDeadAlienHitSound(); // Feedback

                            float energyGain = (alienManager.getConfig()
                                    .getLevelNumber() == com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL)
                                            ? com.space.game.config.GameConfig.BOSS_MINION_ENERGY_GAIN // 5.0
                                            : 2.5f;

                            // Passive Buff: +0.5% Energy per kill after first boss (even for dead hits?)
                            // Maybe smaller? Or same. Let's keep it consistent or small.
                            // Usually "per alien dead" implies full kill.
                            // For hitting a corpse, maybe partial?
                            if (spaceship.getBossesDefeated() > 0) {
                                energyGain += spaceship.getMaxEnergy()
                                        * com.space.game.config.GameConfig.PASSIVE_ENERGY_PER_KILL_PERCENT * 0.5f;
                            }

                            spaceship.addEnergy(energyGain);
                            uiManager.addEnergyFeedback(energyGain);

                            if (particleManager != null) {
                                particleManager.createExplosion(alien.getBounds().x + alien.getBounds().width / 2,
                                        alien.getBounds().y + alien.getBounds().height / 2, 50);
                            }
                        }
                    } else {
                        // Normal Shot
                        bullet.markForRemoval();
                        if (!alien.isDead()) {
                            // Boomer Logic
                            if (alien.getType() == Alien.AlienType.BABY_BOOMER
                                    || alien.getType() == Alien.AlienType.BOSS_BOOMER) {

                                float force = (alien.getType() == Alien.AlienType.BABY_BOOMER)
                                        ? com.space.game.config.ConfigUtils
                                                .scale(com.space.game.config.GameConfig.BABY_KNOCKBACK_FORCE)
                                        : com.space.game.config.ConfigUtils
                                                .scale(com.space.game.config.GameConfig.BOSS_KNOCKBACK_FORCE);

                                alien.applyKnockback(force);
                                killed = alien.takeDamage(1);
                                if (killed) {
                                    if (alien.getType() == Alien.AlienType.BOSS_BOOMER) {
                                        // Do nothing. AlienManager handles dramatic death sequence.
                                        // Still play hit sound? AlienManager plays explosion later.
                                        soundManager.playAlienHitSound();
                                    } else {
                                        explode(alien, false); // Killed by player -> Small explosion
                                    }
                                } else {
                                    // Hit feedback
                                    soundManager.playAlienHitSound();
                                }
                            } else {
                                // Normal Alien
                                int scoreGain = spaceship.getStreakCount();
                                if (spaceship.getStreakCount() == 0)
                                    scoreGain = 1;
                                spaceship.incrementKillCount();
                                spaceship.incrementKillCount();
                                uiManager.addScoreFeedback(scoreGain);

                                // Energy Reward for Normal Kill
                                float energyGain = 2.5f; // Base for normal alien
                                if (spaceship.getBossesDefeated() > 0) {
                                    energyGain += spaceship.getMaxEnergy()
                                            * (com.space.game.config.GameConfig.PASSIVE_ENERGY_PER_KILL_PERCENT
                                                    * spaceship.getBossesDefeated());
                                }
                                spaceship.addEnergy(energyGain);
                                uiManager.addEnergyFeedback(energyGain);

                                alien.hit();
                                killed = true;
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

                            float energyGain = (alienManager.getConfig()
                                    .getLevelNumber() == com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL)
                                            ? com.space.game.config.GameConfig.BOSS_MINION_ENERGY_GAIN // 5.0
                                            : 2.5f;

                            spaceship.addEnergy(energyGain);
                            uiManager.addEnergyFeedback(energyGain);

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

    private void explode(Alien boomer, boolean isSelfDestruct) {
        float x = boomer.getPosition().x + boomer.getBounds().width / 2;
        float y = boomer.getPosition().y + boomer.getBounds().height / 2;
        float radius = 0f;

        if (boomer.getType() == Alien.AlienType.BABY_BOOMER) {
            radius = com.space.game.config.ConfigUtils.scale(com.space.game.config.GameConfig.BABY_EXPLOSION_RADIUS);
        } else {
            // Boss Boomer
            if (isSelfDestruct) {
                radius = com.space.game.config.ConfigUtils
                        .scale(com.space.game.config.GameConfig.BOSS_EXPLOSION_RADIUS); // Massive
            } else {
                radius = com.space.game.config.ConfigUtils
                        .scale(com.space.game.config.GameConfig.BOSS_DEATH_EXPLOSION_RADIUS); // Small
            }
        }

        if (particleManager != null) {
            // Fire colors for explosion (Red/Orange/Yellow)
            com.badlogic.gdx.graphics.Color fireColor = new com.badlogic.gdx.graphics.Color(1f,
                    com.badlogic.gdx.math.MathUtils.random(0f, 0.6f), 0f, 1f);

            int pCount = 100;
            if (boomer.getType() == Alien.AlienType.BOSS_BOOMER) {
                // Massive explosion
                // Mix colors for Boss
                particleManager.createMassiveExplosion(x, y, com.badlogic.gdx.graphics.Color.ORANGE);
                particleManager.createMassiveExplosion(x, y, com.badlogic.gdx.graphics.Color.RED);

                // Play Sound and Remove Boss Immediately
                musicManager.stopBossMusic(false); // Stop music for dramatic effect
                soundManager.playBossExplosionSound();
                boomer.takeDamage(1000); // Ensure dead state logic triggers (score etc)
                boomer.markForImmediateRemoval(); // Don't leave a corpse
            } else {
                particleManager.createExplosion(x, y, pCount, fireColor);
                // Fix: Play sound for Baby Boomer too
                soundManager.playBossExplosionSound(); // Reusing boss explosion as requested ("same logic")
            }
        }

        // Damage Aliens
        for (Alien a : aliens) {
            if (!a.isDead() && a != boomer) {
                float dst = new com.badlogic.gdx.math.Vector2(x, y).dst(new com.badlogic.gdx.math.Vector2(
                        a.getPosition().x + a.getBounds().width / 2, a.getPosition().y + a.getBounds().height / 2));
                if (dst < radius) {
                    a.takeDamage(100);
                    // If Boss Explosion, disintegrate immediately (no corpses)
                    if (boomer.getType() == Alien.AlienType.BOSS_BOOMER) {
                        a.markForImmediateRemoval();
                    }
                }
            }
        }

        // Check Player
        float scale = spaceship.getScale();
        float px = spaceship.getPosition().x + spaceship.getBounds().width * scale / 2;
        float py = spaceship.getPosition().y + spaceship.getBounds().height * scale / 2;

        if (new com.badlogic.gdx.math.Vector2(x, y).dst(new com.badlogic.gdx.math.Vector2(px, py)) < radius) {
            explosionKilledPlayer = true;
            // Handle Dark Level Vision Reset if killed by Boomer
            if (SpaceGame.getGame().getMapManager().getCurrentLevel() instanceof com.space.game.levels.DynamicLevel) {
                com.space.game.levels.DynamicLevel dl = (com.space.game.levels.DynamicLevel) SpaceGame.getGame()
                        .getMapManager().getCurrentLevel();
                if (dl.getConfig().isDarkLevel()) {
                    dl.setDarkMaskActive(false); // Remove mask to show explosion
                    dl.setLightsOut(false);
                }
            }
        }
    }

    public boolean checkSpaceshipCollisions() {
        if (spaceship.isDead())
            return false; // Already dead handling animation
        if (explosionKilledPlayer) {
            spaceship.setDead(true);
            return false;
        }

        // Check Proximity for Boss Boomer
        float shipCX = spaceship.getPosition().x + spaceship.getBounds().width * spaceship.getScale() / 2;
        float shipCY = spaceship.getPosition().y + spaceship.getBounds().height * spaceship.getScale() / 2;

        Iterator<Alien> alienIterator = aliens.iterator();
        while (alienIterator.hasNext()) {
            Alien alien = alienIterator.next();

            // Proximity Check for Boss
            if (alien.getType() == Alien.AlienType.BOSS_BOOMER && !alien.isDead()) {
                float alienCX = alien.getBounds().x + alien.getBounds().width / 2;
                float alienCY = alien.getBounds().y + alien.getBounds().height / 2;

                float dist = com.badlogic.gdx.math.Vector2.dst(shipCX, shipCY, alienCX, alienCY);
                // Trigger at Configured Distance
                if (dist < com.space.game.config.ConfigUtils
                        .scale(com.space.game.config.GameConfig.BOSS_DETONATION_DISTANCE)) {
                    if (!alien.isDetonating()) {
                        alien.startDetonation();
                    } else if (alien.isReadyToExplode()) {
                        explode(alien, true); // Self Destruct -> Massive
                        alien.hit(); // Trigger death logic
                        // If explosion killed player, MARK spaceship as dead but DO NOT return true
                        // immediately
                        if (explosionKilledPlayer) {
                            spaceship.setDead(true);
                            return false; // Let DynamicLevel handle it
                        }
                    }
                }
            }

            if (spaceship.getBounds().overlaps(alien.getBounds())) {
                // If Boomer touches player -> Explode (and Kill)
                if ((alien.getType() == Alien.AlienType.BABY_BOOMER || alien.getType() == Alien.AlienType.BOSS_BOOMER)
                        && !alien.isDead()) {
                    explode(alien, true); // Touched Player -> Massive
                    alien.hit(); // Kill alien too
                    spaceship.setDead(true);
                    return false; // Game Over handled by DynamicLevel
                }
                return true;
            }
        }
        return false;
    }
}
