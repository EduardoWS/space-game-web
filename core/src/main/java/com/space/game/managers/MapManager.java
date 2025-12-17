package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.entities.Spaceship;
import com.space.game.levels.Level;
import com.space.game.levels.LevelFactory;
import com.space.game.Game;
import com.space.game.SpaceGame;

public class MapManager {
    // private Game game;
    private Level currentLevel;
    private Spaceship spaceship;
    private LevelFactory levelFactory;
    private BulletManager bulletManager;
    private com.space.game.managers.ParticleManager particleManager;
    private com.space.game.managers.SoundManager soundManager;

    private float waveTimer = 0;
    private final float TIME_TO_WAVE = 3; // Tempo em segundos antes da próxima onda
    private boolean waveActive;

    public MapManager(Game game) {
        this.levelFactory = new LevelFactory();
        // this.game = game;
        this.soundManager = game.getSoundManager();
        this.bulletManager = new com.space.game.managers.BulletManager(game.getTextureManager(), this.soundManager);
        this.particleManager = new com.space.game.managers.ParticleManager(game.getTextureManager());
    }

    public void loadLevel(int levelNumber) {
        // Do NOT dispose BulletManager or ParticleManager here

        if (currentLevel != null) {
            currentLevel.dispose();
        }

        if (spaceship == null) {
            spaceship = new Spaceship(SpaceGame.getGame().getTextureManager(), bulletManager);
        } else {
            // Update reference if needed, but spaceship maintains it usually
            spaceship.setBulletManager(bulletManager);
        }

        currentLevel = levelFactory.createLevel(levelNumber, spaceship, bulletManager, particleManager);
        waveActive = false;

        // Initialize dark mask to false and lightsOut to false
        if (currentLevel instanceof com.space.game.levels.DynamicLevel
                && currentLevel.getConfig().isDarkLevel()) {
            ((com.space.game.levels.DynamicLevel) currentLevel).setDarkMaskActive(false);
            ((com.space.game.levels.DynamicLevel) currentLevel).setLightsOut(false);
        }

        if (currentLevel == null) {
            throw new IllegalArgumentException("Invalid level number: " + levelNumber);
        }

        // --- THEME APPLICATION ---
        com.space.game.config.LevelConfig config = currentLevel.getConfig();
        if (config.getTheme() != null) {
            String bgKey = config.getTheme().getBackgroundTextureKey();
            com.badlogic.gdx.graphics.Texture bgTexture = SpaceGame.getGame().getTextureManager().getTexture(bgKey);
            if (bgTexture != null) {
                SpaceGame.getGame().getBackground().setBackgroundTexture(bgTexture);
            }
        }

    }

    public void render(SpriteBatch batch) {
        if (currentLevel != null) {
            currentLevel.render(batch);

            if (!waveActive && SpaceGame.getGame().getGsm()
                    .getState() != com.space.game.managers.GameStateManager.State.PAUSED) {

                boolean isDark = currentLevel.getConfig().isDarkLevel();

                if (isDark) {
                    // Phase 1: Wave Text (0 - 2.0s)
                    if (waveTimer < 2.0f) {
                        SpaceGame.getGame().getUiManager().displayNewLevel(waveTimer, 2.0f);
                    }
                    // Phase 2: Warning (2.0s - 4.5s)
                    else if (waveTimer >= 2.0f && waveTimer < 4.5f) {
                        SpaceGame.getGame().getUiManager().displayDarkLevelWarning(waveTimer - 2.0f, 2.5f);
                    }
                    // Phase 3: Blinking happens in logic, no UI text
                } else {
                    SpaceGame.getGame().getUiManager().displayNewLevel(waveTimer, TIME_TO_WAVE);
                }
            }
        }

    }

    public void update() {
        if (currentLevel != null && currentLevel.getEndLevel()) {
            loadLevel(currentLevel.getConfig().getLevelNumber() + 1);
        }
        if (currentLevel != null && waveActive) {
            currentLevel.update();
        } else if (currentLevel != null && !waveActive) {
            currentLevel.updateTransition();

            float currentTimeToWave = TIME_TO_WAVE;

            // Handle Dark Level Transition Effects
            if (currentLevel instanceof com.space.game.levels.DynamicLevel
                    && currentLevel.getConfig().isDarkLevel()) {

                currentTimeToWave = 6.0f;
                com.space.game.levels.DynamicLevel dl = (com.space.game.levels.DynamicLevel) currentLevel;

                // Phase 3: Blinking (4.5s - 6.0s)
                if (waveTimer >= 4.5f) {
                    // Toggle lights out every 0.25s (slower blinking)
                    boolean lightsOut = ((int) ((waveTimer * 4)) % 2 == 0);
                    dl.setLightsOut(lightsOut);
                }
            }

            waveTimer += Gdx.graphics.getDeltaTime();
            if (waveTimer >= currentTimeToWave) {
                waveActive = true;
                waveTimer = 0;

                // Ensure dark mask is active and lights out is false when wave starts
                if (currentLevel instanceof com.space.game.levels.DynamicLevel
                        && currentLevel.getConfig().isDarkLevel()) {
                    ((com.space.game.levels.DynamicLevel) currentLevel).setDarkMaskActive(true);
                    ((com.space.game.levels.DynamicLevel) currentLevel).setLightsOut(false);
                }

                currentLevel.startWave();
            }
        }
    }

    public void reset() {
        if (currentLevel != null) {
            currentLevel.dispose();
            currentLevel = null;
        }
        spaceship = null;
        waveActive = false;
        waveTimer = 0;
        if (levelFactory != null) {
            levelFactory.reset();
        }
        if (bulletManager != null) {
            bulletManager.clear();
        }
        if (particleManager != null) {
            particleManager.clear();
        }
    }

    public void dispose() {
        if (currentLevel != null) {
            currentLevel.dispose();
            currentLevel = null;
        }
        if (levelFactory != null) {
            levelFactory.dispose();
        }
        if (bulletManager != null) {
            bulletManager.dispose();
        }
        if (particleManager != null) {
            particleManager.dispose();
        }
    }

    public Spaceship getSpaceship() {
        return currentLevel != null ? currentLevel.getSpaceship() : null;
    }

    public void freeSpaceship() {
        if (currentLevel != null) {
            currentLevel.freeSpaceship();
        }
    }

    public boolean isWaveActive() {
        return waveActive;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

}
