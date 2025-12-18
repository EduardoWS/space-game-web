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
    private int bossWarningPhase = 0; // 0: Init, 1: Fading, 2: Warning

    // --- Pacing System ---
    private enum SpawnMoment {
        CALM,
        STEADY,
        INTENSE
    }

    private SpawnMoment currentMoment = SpawnMoment.CALM;
    private float momentTimer = 0;
    private float spawnTimer = 0;
    private boolean intenseMomentOccurred = false;

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

        // Initialize Pacing
        // Initialize Pacing
        this.currentMoment = SpawnMoment.CALM;
        this.momentTimer = MathUtils.random(com.space.game.config.GameConfig.DURATION_CALM_MIN,
                com.space.game.config.GameConfig.DURATION_CALM_MAX); // Start calm
        this.spawnTimer = 1.0f; // Initial small delay
    }

    public void addAlien(Vector2 position, float scale, float speed, int movementPattern) {
        Alien newAlien = AlienFactory.createAlien(textureManager, position, scale, speed, spaceship, movementPattern);
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

        activeAlienCount = 0;
        for (Alien alien : this.getAliens()) {
            if (!alien.isDead()) {
                activeAlienCount++;
            }
        }

        // If we still have aliens to spawn...
        if (config.getEnemyMovementPatterns().size() > 0) {
            updatePacing(deltaTime);
            updateSpawning(deltaTime, spaceship);
        }
    }

    private void updatePacing(float delta) {
        momentTimer -= delta;

        if (momentTimer <= 0) {
            // Pick next moment logic
            switch (currentMoment) {
                case CALM:
                    // After Calm, go to Steady (70%) or Intense (30%)
                    if (MathUtils.randomBoolean(0.3f)) {
                        switchToMoment(SpawnMoment.INTENSE);
                    } else {
                        switchToMoment(SpawnMoment.STEADY);
                    }
                    break;
                case STEADY:
                    // After Steady, usually go Intense (to ensure excitement) or back to Calm
                    if (MathUtils.randomBoolean(0.6f)) {
                        switchToMoment(SpawnMoment.INTENSE);
                    } else {
                        switchToMoment(SpawnMoment.CALM);
                    }
                    break;
                case INTENSE:
                    // After Intense, always cooldown to Calm or Steady
                    if (MathUtils.randomBoolean(0.7f)) {
                        switchToMoment(SpawnMoment.CALM);
                    } else {
                        switchToMoment(SpawnMoment.STEADY);
                    }
                    break;
            }
        }
    }

    private void switchToMoment(SpawnMoment next) {
        currentMoment = next;

        switch (next) {
            case CALM:
                momentTimer = MathUtils.random(com.space.game.config.GameConfig.DURATION_CALM_MIN,
                        com.space.game.config.GameConfig.DURATION_CALM_MAX);
                break;
            case STEADY:
                momentTimer = MathUtils.random(com.space.game.config.GameConfig.DURATION_STEADY_MIN,
                        com.space.game.config.GameConfig.DURATION_STEADY_MAX);
                break;
            case INTENSE:
                momentTimer = MathUtils.random(com.space.game.config.GameConfig.DURATION_INTENSE_MIN,
                        com.space.game.config.GameConfig.DURATION_INTENSE_MAX);
                intenseMomentOccurred = true;
                break;
        }
        // Gdx.app.log("PACING", "Switched to: " + currentMoment + " for " + momentTimer
        // + "s");
    }

    private void updateSpawning(float delta, Spaceship spaceship) {
        spawnTimer -= delta;

        if (spawnTimer <= 0) {
            // Time to spawn!
            int batchSize = 0;
            float cooldown = 0;

            int currentLevel = config.getLevelNumber();

            // Dynamic Limits
            int maxActiveBase = com.space.game.config.GameConfig.MAX_ACTIVE_ALIENS_BASE +
                    (currentLevel * com.space.game.config.GameConfig.MAX_ACTIVE_ALIENS_GROWTH);
            // Ensure reasonable cap
            if (maxActiveBase > com.space.game.config.GameConfig.ABSOLUTE_MAX_ALIENS_ON_SCREEN)
                maxActiveBase = com.space.game.config.GameConfig.ABSOLUTE_MAX_ALIENS_ON_SCREEN;

            if (activeAlienCount >= maxActiveBase) {
                spawnTimer = 0.5f; // Wait a bit if full
                return;
            }

            switch (currentMoment) {
                case CALM:
                    batchSize = MathUtils.random(com.space.game.config.GameConfig.BATCH_CALM_MIN,
                            com.space.game.config.GameConfig.BATCH_CALM_MAX);
                    cooldown = MathUtils.random(com.space.game.config.GameConfig.COOLDOWN_CALM_MIN,
                            com.space.game.config.GameConfig.COOLDOWN_CALM_MAX);
                    break;
                case STEADY:
                    batchSize = MathUtils.random(com.space.game.config.GameConfig.BATCH_STEADY_MIN,
                            com.space.game.config.GameConfig.BATCH_STEADY_MAX);
                    cooldown = MathUtils.random(com.space.game.config.GameConfig.COOLDOWN_STEADY_MIN,
                            com.space.game.config.GameConfig.COOLDOWN_STEADY_MAX);
                    break;
                case INTENSE:
                    batchSize = MathUtils.random(com.space.game.config.GameConfig.BATCH_INTENSE_MIN,
                            com.space.game.config.GameConfig.BATCH_INTENSE_MAX); // Frenetic!
                    cooldown = MathUtils.random(com.space.game.config.GameConfig.COOLDOWN_INTENSE_MIN,
                            com.space.game.config.GameConfig.COOLDOWN_INTENSE_MAX);
                    break;
            }

            // Cap batch by available slots
            int availableSlots = maxActiveBase - activeAlienCount;
            batchSize = Math.min(batchSize, availableSlots);

            // Cap by remaining enemies in config
            batchSize = Math.min(batchSize, config.getEnemyMovementPatterns().size());

            // Exec Spawning
            for (int i = 0; i < batchSize; i++) {
                spawnSingleAlien(spaceship, currentLevel);
            }

            spawnTimer = cooldown;
        }
    }

    private void spawnSingleAlien(Spaceship spaceship, int currentLevel) {
        if (config.getEnemyMovementPatterns().isEmpty())
            return;

        int spawnSideIndex = determineSpawnSideIndex(currentLevel);
        Vector2 alienPosition = calculateAlienSpawnPosition(spawnSideIndex, spaceship.getPosition());

        float levelSpeedMultiplier = config.getEnemySpeed();
        float alienScale = 0.6f * scale_screen;

        int pattern = config.getEnemyMovementPatterns().get(0);

        // Determine specific speed based on type/pattern
        float baseSpeedPercent = 0.05f; // Default
        switch (pattern) {
            case 0:
                baseSpeedPercent = com.space.game.config.GameConfig.SPEED_LINEAR;
                break;
            case 1:
                baseSpeedPercent = com.space.game.config.GameConfig.SPEED_WAVE_FORWARD;
                break;
            case 2:
                baseSpeedPercent = com.space.game.config.GameConfig.SPEED_SPIRAL_APPROACH;
                break;
            case 3:
                baseSpeedPercent = com.space.game.config.GameConfig.BABY_BOOMER_SPEED;
                break;
            case 4:
                baseSpeedPercent = com.space.game.config.GameConfig.BOSS_BOOMER_SPEED;
                break;
        }

        float finalSpeed = (baseSpeedPercent * SpaceGame.getGame().getWorldWidth()) * levelSpeedMultiplier;

        // Add significant random variation (85% to 135% speed) per alien
        finalSpeed *= MathUtils.random(0.85f, 1.35f);

        // Add small random pixel variation
        finalSpeed += MathUtils.random(-5f, 5f);

        this.addAlien(alienPosition, alienScale, finalSpeed, pattern);

        // Remove from queue
        config.getEnemyMovementPatterns().remove(0);
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
            if (bossWarningPhase == 0) {
                // Step 1: Start Fade Out
                com.space.game.SpaceGame.getGame().getSoundManager().fadeMusicOut(2.0f);
                bossWarningTimer = 2.0f; // Wait for fade to complete
                bossWarningPhase = 1;
            } else if (bossWarningPhase == 1) {
                // Step 2: Wait for fade
                bossWarningTimer -= deltaTime;
                if (bossWarningTimer <= 0) {
                    // Step 3: Trigger Warning
                    com.space.game.SpaceGame.getGame().getUiManager().triggerBossWarning(); // UI Duration is now 4s
                    com.space.game.SpaceGame.getGame().getSoundManager().playBossWarningSound();
                    bossWarningTimer = 4.0f; // Wait for warning duration
                    bossWarningPhase = 2;
                }
            } else if (bossWarningPhase == 2) {
                // Step 4: Wait for Warning
                bossWarningTimer -= deltaTime;
                if (bossWarningTimer <= 0) {
                    // Step 5: Resume
                    bossWarningShown = true;
                    com.space.game.SpaceGame.getGame().getSoundManager().fadeMusicIn(2.0f);
                }
            }
            return; // Block until sequence finishes
        }

        // Phase 3: Spawn Boss
        if (!bossSpawned) {
            boolean right = MathUtils.randomBoolean();
            // Fix Left Spawn: -200 (extra padding) instead of -100 to ensure out of view
            float x = right ? com.space.game.SpaceGame.getGame().getWorldWidth() + 200
                    : -400; // Increased left offset to prevent pop-in

            float y = com.space.game.SpaceGame.getGame().getWorldHeight() / 2f;
            Vector2 pos = new Vector2(x, y);

            // Correctly calculate Boss Speed using percentage
            float bossSpeedPercent = com.space.game.config.GameConfig.BOSS_BOOMER_SPEED;
            // config.getEnemySpeed() is a multiplier (e.g. 1.0, 1.1)
            float bossSpeed = (bossSpeedPercent * com.space.game.SpaceGame.getGame().getWorldWidth())
                    * config.getEnemySpeed();

            // Add Boss (Pattern 4)
            addAlien(pos, 0, bossSpeed, 4); // Scale 0 -> auto boss scale in AlienFactory

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

                // Allow Music to continue until end of track/loop
                com.space.game.SpaceGame.getGame().getSoundManager().setBossDefeatedMode(true);
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
            Vector2 pos;
            if (bossAlien != null && !bossAlien.isDead()) {
                // Boss is active -> Spawn opposite side
                float bossX = bossAlien.getPosition().x;
                float centerX = com.space.game.SpaceGame.getGame().getWorldWidth() / 2f;
                if (bossX > centerX) {
                    // Boss Right -> Spawn Left (3)
                    pos = calculateAlienSpawnPosition(3, spaceship.getPosition());
                } else {
                    // Boss Left -> Spawn Right (1)
                    pos = calculateAlienSpawnPosition(1, spaceship.getPosition());
                }
            } else {
                pos = calculateAlienSpawnPosition(MathUtils.random(0, 3), spaceship.getPosition());
            }

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

    private int determineSpawnSideIndex(int level) {
        float sideChance = com.space.game.config.GameConfig.SIDE_SPAWN_CHANCE_INITIAL -
                (level * com.space.game.config.GameConfig.SIDE_SPAWN_CHANCE_DECAY);
        // Floor at 30% chance minimum for sides (or whatever balance desires)
        sideChance = Math.max(0.3f, sideChance);

        if (MathUtils.random() < sideChance) {
            // Spawn on Sides (Left or Right)
            return MathUtils.randomBoolean() ? 1 : 3;
        } else {
            // Spawn Top or Bottom (Harder)
            return MathUtils.randomBoolean() ? 0 : 2;
        }
    }

    private Vector2 calculateAlienSpawnPosition(int index, Vector2 spaceshipPosition) {
        // index is now determined by determineSpawnSideIndex (0, 1, 2, 3)
        // No need for modulo unless caller sends raw counter
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
                x = -200f; // Fix: Ensure off-screen spawn
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
                alien.setStrategy(new com.space.game.entities.movements.LinearMovement()); // Force Linear
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