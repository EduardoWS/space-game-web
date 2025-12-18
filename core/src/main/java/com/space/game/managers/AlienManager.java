package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.space.game.SpaceGame;
import com.space.game.config.ConfigUtils;
import com.space.game.entities.Alien;
import com.space.game.entities.Bullet;
import com.space.game.entities.Spaceship;
import com.space.game.graphics.TextureManager;
import com.space.game.managers.GameStateManager.State;
import com.space.game.config.LevelConfig;

public class AlienManager {
    private float scale_screen = ConfigUtils.calcularFatorDeEscala();
    private List<Alien> aliens;
    private TextureManager textureManager;
    private float deltaTime;
    private Spaceship spaceship;
    private boolean endLevel;
    private int activeAlienCount;
    private int deadAliensCount;
    private boolean isSpaceshipNoMunition;

    private LevelConfig config;

    private float bossWarningTimer = 0;
    private boolean bossWarningShown = false;
    private boolean bossSpawned = false;
    private Alien bossAlien;

    public AlienManager(TextureManager textureManager, Spaceship spaceship, LevelConfig config) {
        this.config = config;
        this.aliens = new ArrayList<>();
        this.textureManager = textureManager;
        this.deltaTime = Gdx.graphics.getDeltaTime();
        this.spaceship = spaceship;

        this.activeAlienCount = 0;
        this.deadAliensCount = 0;

        this.isSpaceshipNoMunition = false;

        this.endLevel = false;
    }

    public void addAlien(Vector2 position, float scale, float speed, int movementPattern) {
        Alien newAlien = new Alien(textureManager, position, scale, speed, spaceship, movementPattern);
        aliens.add(newAlien);

        // Track Boss
        if (movementPattern == 4) { // Boss Boomer
            bossAlien = newAlien;
        }
    }

    public boolean getEndLevel() {
        return endLevel;
    }

    public void setIsSpaceshipNoMunition(boolean isSpaceshipNoMunition) {
        this.isSpaceshipNoMunition = isSpaceshipNoMunition;
    }

    private float bossDeathTimer = 0;
    private float bossMinionSpawnTimer = 0;
    private final float BOSS_DEATH_DURATION = 8.0f; // 6s message + 2s delay
    private boolean isBossDying = false;

    public void spawnAliens(Spaceship spaceship) {
        // --- BOSS WAVE LOGIC (Wave 10) ---
        if (config.getLevelNumber() == com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL) {
            handleBossLevel(spaceship);
            return;
        }

        // --- NORMAL WAVE LOGIC ---
        if (deadAliensCount >= config.getEnemyCount()) {
            this.endLevel = true;
            return;
        }

        // ... existing spawn logic ...
        activeAlienCount = 0;
        for (Alien alien : this.getAliens()) {
            if (!alien.isDead()) {
                activeAlienCount++;
            }
        }

        if (activeAlienCount <= MathUtils.random(3, 7) && config.getEnemyMovementPatterns().size() > 0) {
            int contSpawn = 1;
            if (config.getEnemyMovementPatterns().size() > 7) {
                contSpawn = MathUtils.random(4, 7);
            } else {
                contSpawn = config.getEnemyMovementPatterns().size();
            }
            for (int i = 0; i < contSpawn; i++) {
                Vector2 alienPosition = calculateAlienSpawnPosition(i, spaceship.getPosition());
                float speed = MathUtils.random(config.getEnemySpeed(), config.getEnemySpeed() + 5);
                float alienScale = 0.6f * scale_screen;

                if (!config.getEnemyMovementPatterns().isEmpty()) {
                    int pattern = config.getEnemyMovementPatterns().get(0);
                    this.addAlien(alienPosition, alienScale, speed, pattern);
                    config.getEnemyMovementPatterns().remove(0);
                }
            }
        }
    }

    private void handleBossLevel(Spaceship spaceship) {
        // Phase 1: Spawn initial minions before boss (Music Starts Here)
        if (!bossWarningShown && deadAliensCount < com.space.game.config.GameConfig.MINIONS_BEFORE_BOSS) {
            // Play Boss Music Start immediately when Boss Phase 1 starts (if not already
            // playing)
            // We can use a flag or check if it's already active in SoundManager logic
            if (!com.space.game.SpaceGame.getGame().getSoundManager().isBossMusicActive()) {
                com.space.game.SpaceGame.getGame().getSoundManager().playBossMusic();
            }

            activeAlienCount = 0;
            for (Alien alien : this.getAliens()) {
                if (!alien.isDead())
                    activeAlienCount++;
            }

            if (activeAlienCount <= 3 && config.getEnemyMovementPatterns().size() > 0) {
                // Frenetic spawn: Use configured count
                int spawn_range = MathUtils.random(com.space.game.config.GameConfig.BOSS_MINION_SPAWN_COUNT,
                        com.space.game.config.GameConfig.MAX_ENEMIES_ON_BOSS_SCREEN);
                spawnNormalAliens(spaceship, spawn_range);
            }
            return;
        }

        if (!bossWarningShown) {
            com.space.game.SpaceGame.getGame().getUiManager().triggerBossWarning();
            com.space.game.SpaceGame.getGame().getSoundManager().playBossWarningSound();
            bossWarningTimer = 8.0f; // Wait time matching UI
            bossWarningShown = true;
            return;
        }

        if (bossWarningTimer > 0) {
            bossWarningTimer -= deltaTime;
            return; // Wait for warning to finish
        }

        // Phase 3: Spawn Boss
        if (!bossSpawned) {
            boolean right = MathUtils.randomBoolean();
            // Fix Left Spawn: -200 (extra padding) instead of -100 to ensure out of view
            float x = right ? com.space.game.SpaceGame.getGame().getWorldWidth() + 200
                    : -400; // Increased left offset to prevent pop-in

            float y = com.space.game.SpaceGame.getGame().getWorldHeight() / 2f;
            Vector2 pos = new Vector2(x, y);

            float bossSpeed = config.getEnemySpeed() * 0.5f;

            // Add Boss (Pattern 4)
            addAlien(pos, 0, bossSpeed, 4); // Scale 0 -> auto boss scale in Alien ctor

            // Re-enable randomized patterns for infinite phase?
            // User: "so depois fique aparecendo os aliens linear" -> So keep linear (0).
            // No changes needed here for Phase 4.

            bossSpawned = true;
            return;
        }

        // Phase 5: Boss Death Sequence (Dramatic)
        if (bossAlien != null && bossAlien.getHp() <= 0) {
            // IF PLAYER IS DEAD, DO NOT TRIGGER VICTORY, JUST LET GAME OVER HAPPEN
            if (spaceship.isDead()) {
                return;
            }

            if (!isBossDying) {
                isBossDying = true;
                bossDeathTimer = BOSS_DEATH_DURATION;
                com.space.game.SpaceGame.getGame().getUiManager().triggerBossDefeated(); //
            }

            bossDeathTimer -= deltaTime;

            // Dramatic explosions during death timer
            if (bossDeathTimer > 0) {
                if (MathUtils.randomBoolean(0.3f)) { // Random small explosions
                    float rx = bossAlien.getPosition().x + MathUtils.random(bossAlien.getBounds().width);
                    float ry = bossAlien.getPosition().y + MathUtils.random(bossAlien.getBounds().height);
                    // Red/Orange/Yellow explosions
                    if (com.space.game.SpaceGame.getGame().getParticleManager() != null) {
                        com.space.game.SpaceGame.getGame().getParticleManager().createExplosion(rx, ry, 20,
                                new com.badlogic.gdx.graphics.Color(1f, MathUtils.random(0.5f), 0f, 1f));
                    }
                }
                return; // Wait for death animation
            } else {
                // Finally kill it
                bossAlien.markForImmediateRemoval();
                endLevel = true;

                // Stop Boss Music
                com.space.game.SpaceGame.getGame().getSoundManager().stopBossMusic();
                return;
            }
        }

        // Phase 4: Boss Fight (Infinite Minions)
        if (bossAlien != null && !bossAlien.isDead() && !isBossDying) {
            // Rage Mode: Spawn Rate based on HP
            float hpPercent = (float) bossAlien.getHp() / (float) bossAlien.getMaxHp();

            float currentSpawnRate = com.space.game.config.GameConfig.MIN_SPAWN_RATE +
                    (hpPercent * (com.space.game.config.GameConfig.MAX_SPAWN_RATE
                            - com.space.game.config.GameConfig.MIN_SPAWN_RATE));

            bossMinionSpawnTimer += deltaTime;

            int activeMinions = 0;
            for (Alien a : aliens) {
                if (!a.isDead() && a != bossAlien)
                    activeMinions++;
            }

            int maxMinions = com.space.game.config.GameConfig.MAX_ENEMIES_ON_BOSS_SCREEN;

            // Use timer instead of chance per frame to be robust
            if (activeMinions < maxMinions && bossMinionSpawnTimer >= currentSpawnRate) {
                spawnNormalAliens(spaceship, -1); // Force spawn 1
                bossMinionSpawnTimer = 0;
            }
        }

        // Phase 5: Victory
        // Wait for boss dying animation/timer to finish before ending level
        if (bossSpawned && (bossAlien == null || bossAlien.isDead()) && !isBossDying) {
            // Cleanup minions? Or let player kill them.
            // End Level only if player is alive
            if (!spaceship.isDead()) {
                this.endLevel = true;
            }
        }
    }

    // Helper to reuse spawn logic
    private void spawnNormalAliens(Spaceship spaceship, int limit) {
        activeAlienCount = 0;
        for (Alien alien : aliens) {
            if (!alien.isDead())
                activeAlienCount++;
        }

        // If limit is -1, it means force spawn one (used by boss logic)
        // If limit > 0, it means spawn only if active count < limit
        boolean shouldSpawn = (limit == -1) || (activeAlienCount <= limit);

        if (shouldSpawn) {
            Vector2 pos = calculateAlienSpawnPosition(MathUtils.random(0, 3), spaceship.getPosition());

            // For boss infinite spawning, we might not have patterns in config left.
            // So we default to linear (0) or mix.
            // "Baby Boomer é desbloqueado" logic suggests we could spawn them too.
            // For now, keep it simple: Linear (0).
            // For Phase 1 (Minions before boss), user wants mixed types (0,1,2).
            // For Phase 4 (Boss Fight), user wants Linear (0).
            // limit == -1 is used for Phase 4 (Boss Fight Infinite Spawn).
            // limit > 0 is used for Phase 1 (Intro).

            int pattern = 0;
            if (limit > 0) {
                // Phase 1 -> Randomize
                pattern = MathUtils.random(0, 2);
            } else {
                // Phase 4 -> Linear
                pattern = 0;
            }

            addAlien(pos, 0.6f * scale_screen, config.getEnemySpeed(), pattern);
        }
    }

    public List<Alien> getAliens() {
        return aliens;
    }

    public LevelConfig getConfig() {
        return config;
    }

    private Vector2 calculateAlienSpawnPosition(int index, Vector2 spaceshipPosition) {
        // fazer o modulo de index por 4 para que o valor de index seja sempre entre 0 e
        // 3
        index = index % 4;

        float x = 0, y = 0;
        // Exemplo simples de spawn positions, pode ser ajustado conforme necessário
        switch (index % 4) {
            case 0: // Topo
                x = MathUtils.random(0, SpaceGame.getGame().getWorldWidth());
                y = SpaceGame.getGame().getWorldHeight() + SpaceGame.getGame().getWorldHeight() / 16;
                break;
            case 1: // Direita
                x = SpaceGame.getGame().getWorldWidth() + SpaceGame.getGame().getWorldHeight() / 16;
                y = MathUtils.random(0, SpaceGame.getGame().getWorldHeight());
                break;
            case 2: // Baixo
                x = MathUtils.random(0, SpaceGame.getGame().getWorldWidth());
                y = 0 - SpaceGame.getGame().getWorldHeight() / 16;
                break;
            case 3: // Esquerda
                x = 0 - SpaceGame.getGame().getWorldHeight() / 16;
                y = MathUtils.random(0, SpaceGame.getGame().getWorldHeight());
                break;
        }
        return new Vector2(x, y);
    }

    public void update(List<Bullet> bullets) {
        if (SpaceGame.getGame().getGsm().getState() != State.PLAYING) {
            return;
        }
        Iterator<Alien> alienIterator = aliens.iterator();
        while (alienIterator.hasNext()) {
            Alien alien = alienIterator.next();
            if (isSpaceshipNoMunition) {
                alien.setMovementPattern(0);
                alien.setSpeed(SpaceGame.getGame().getWorldWidth() / 11);
                setIsSpaceshipNoMunition(false);
            }
            alien.update(deltaTime, spaceship);

            // Remover o alien se ele atende aos critérios de remoção.
            if (alien.shouldRemove()) {
                deadAliensCount++;
                alienIterator.remove();
                alien.dispose();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Alien alien : aliens) {
            alien.render(batch);
        }
    }

    public void dispose() {
        for (Alien alien : aliens) {
            alien.dispose();
        }
        aliens.clear();
    }
}