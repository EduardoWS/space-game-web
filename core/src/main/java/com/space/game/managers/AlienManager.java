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

    public void spawnAliens(Spaceship spaceship) {
        // --- BOSS WAVE LOGIC (Wave 10) ---
        if (config.getLevelNumber() == com.space.game.config.GameConfig.BOSS_APPEAR_LEVEL) {
            handleBossLevel(spaceship);
            return;
        }

        // --- NORMAL LEVELS ---
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

                this.addAlien(alienPosition, alienScale, speed, config.getEnemyMovementPatterns().get(0));
                config.getEnemyMovementPatterns().remove(0);
            }
        }
    }

    private void handleBossLevel(Spaceship spaceship) {
        // Phase 1: Intro Minions (Until 5 kills or so)
        // User request: "primeiro devem aparecer os aliens normais antes do boss"
        // Let's use deadAliensCount for this.
        if (deadAliensCount < 5 && !bossWarningShown) {
            // Normal Spawning Logic for Intro
            spawnNormalAliens(spaceship, 5); // Limit to 5 alive
            return;
        }

        // Phase 2: Warning
        if (!bossWarningShown) {
            // Trigger Warning
            SpaceGame.getGame().getUiManager().triggerBossWarning();
            bossWarningShown = true;
            return; // Wait for next frame
        }

        // Wait for Warning Duration (4 seconds)
        if (bossWarningTimer < 4.0f) {
            bossWarningTimer += Gdx.graphics.getDeltaTime();
            return; // Stop spawning
        }

        // Phase 3: Spawn Boss
        if (!bossSpawned) {
            // Boss Spawn Position: Right or Left
            boolean right = MathUtils.randomBoolean();
            float x = right ? SpaceGame.getGame().getWorldWidth() + 100 : -100;
            float y = SpaceGame.getGame().getWorldHeight() / 2f;
            Vector2 pos = new Vector2(x, y);

            // Speed for Boss? Should be slow?
            float bossSpeed = config.getEnemySpeed() * 0.5f;

            // Add Boss (Pattern 4)
            addAlien(pos, 0, bossSpeed, 4); // Scale 0 -> auto boss scale in Alien ctor
            bossSpawned = true;
        }

        // Phase 4: Boss Fight (Infinite Minions)
        if (bossAlien != null && !bossAlien.isDead()) {
            // Rage Mode: Spawn Rate based on HP
            float hpPercent = (float) bossAlien.getHp() / (float) bossAlien.getMaxHp();

            // Invert HP percent for difficulty (Lower HP -> Higher Difficulty)
            // Low HP -> Faster Spawns (Lower Wait Time)
            // Config: MIN_SPAWN_RATE(0.5s) to MAX_SPAWN_RATE(2.0s)
            // If HP=100%, rate = MAX. If HP=0%, rate = MIN.
            float currentSpawnRate = com.space.game.config.GameConfig.MIN_SPAWN_RATE +
                    (hpPercent * (com.space.game.config.GameConfig.MAX_SPAWN_RATE
                            - com.space.game.config.GameConfig.MIN_SPAWN_RATE));

            // We need a timer for spawns in this phase?
            // Or just check active count.
            // "MAX_ENEMIES_ON_BOSS_SCREEN"

            int activeMinions = 0;
            for (Alien a : aliens) {
                if (!a.isDead() && a != bossAlien)
                    activeMinions++;
            }

            int maxMinions = com.space.game.config.GameConfig.MAX_ENEMIES_ON_BOSS_SCREEN;

            // Only spawn if below cap and coin flip based on rate (simplified)
            // Better: use a timer. But AlienManager update is per frame.
            // Let's use random chance based on DeltaTime to approximate rate.
            // Chance per second = 1 / rate.
            // Chance per frame = (1/rate) * dt.

            if (activeMinions < maxMinions) {
                float spawnChance = (1.0f / currentSpawnRate) * deltaTime;
                if (MathUtils.random() < spawnChance) {
                    Vector2 pos = calculateAlienSpawnPosition(MathUtils.random(0, 3), spaceship.getPosition());
                    // Spawn Normal or Baby Boomer?
                    // "Após matar o Boss... Baby Boomer é desbloqueado".
                    // During boss fight, maybe spawn normal aliens? Or Baby Boomers?
                    // "outros inimigos (normais) devem nascer infinitamente" -> Normais.

                    addAlien(pos, 0.6f * scale_screen, config.getEnemySpeed(), 0); // Linear normal
                }
            }
        }

        // Phase 5: Victory
        if (bossSpawned && (bossAlien == null || bossAlien.isDead())) {
            // Cleanup minions? Or let player kill them.
            // End Level.
            this.endLevel = true;
        }
    }

    // Helper to reuse spawn logic
    private void spawnNormalAliens(Spaceship spaceship, int limit) {
        activeAlienCount = 0;
        for (Alien alien : aliens) {
            if (!alien.isDead())
                activeAlienCount++;
        }

        if (activeAlienCount <= limit) {
            Vector2 pos = calculateAlienSpawnPosition(MathUtils.random(0, 3), spaceship.getPosition());
            addAlien(pos, 0.6f * scale_screen, config.getEnemySpeed(), 0);
        }
    }

    public List<Alien> getAliens() {
        return aliens;
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