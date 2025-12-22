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
    private boolean warningSoundPlayed = false;
    private boolean fadeWarningOutTriggered = false;
    private boolean fadeTriggered = false;

    public boolean isWaveActive() {
        return waveActive;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

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
        waveActive = false;
        warningSoundPlayed = false;
        fadeWarningOutTriggered = false;
        fadeTriggered = false;

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

                boolean isSwarm = currentLevel.getConfig().isSwarmWarning();

                if (isDark) {
                    // Phase 1: Wave Text (0 - 2.0s)
                    if (waveTimer < 2.0f) {
                        SpaceGame.getGame().getUiManager().displayNewLevel(waveTimer, 2.0f);
                    }
                    // Phase 2: Fade Out (2.0s onwards)
                    if (waveTimer >= 2.0f && !fadeTriggered) {
                        SpaceGame.getGame().getMusicManager().fadeMusicOut(2.0f);
                        fadeTriggered = true;
                    }
                    // Phase 3: Warning (4.0s - 8.0s) -> 4 Seconds Duration
                    else if (waveTimer >= 4.0f && waveTimer < 8.0f) {
                        if (!warningSoundPlayed) {
                            SpaceGame.getGame().getSoundManager().playDarkLevelWarningSound();
                            // Optional: Fade In if desired, but user asked for "fade in/fade out"
                            // Fade IN over 1 second?
                            SpaceGame.getGame().getSoundManager().fadeWarningSoundIn(1.0f);
                            warningSoundPlayed = true;
                        }

                        // Fade OUT logic: Trigger when nearing end (e.g. at 7.0s, giving 1s fade out)
                        if (waveTimer >= 7.0f && waveTimer < 8.0f) {
                            // Only trigger fade out once? SoundManager fade method handles state
                            // But we call it every frame? No, fadeWarningSoundOut resets timer if called
                            // again?
                            // Let's rely on checking if it is already fading? SoundManager.isFading is
                            // private.
                            // But fadeWarningSoundOut resets timer. We should only call it once.
                            // Add a flag or check time imprecise.
                            // Let's use a local flag inside the method? No method is generic render.
                            // We probably need a boolean `fadeWarningOutTriggered` in class.
                            if (!fadeWarningOutTriggered) {
                                SpaceGame.getGame().getSoundManager().fadeWarningSoundOut(1.0f);
                                fadeWarningOutTriggered = true;
                            }
                        }

                        SpaceGame.getGame().getUiManager().displayDarkLevelWarning(waveTimer - 4.0f, 4.0f);
                    }
                    // Phase 4: Blinking happens in logic
                } else if (isSwarm) {
                    // Phase 1: Wave Text (0 - 2.0s)
                    if (waveTimer < 2.0f) {
                        SpaceGame.getGame().getUiManager().displayNewLevel(waveTimer, 2.0f);
                    }
                    // Phase 2: Fade Out (2.0s onwards)
                    if (waveTimer >= 2.0f && !fadeTriggered) {
                        SpaceGame.getGame().getMusicManager().fadeMusicOut(2.0f);
                        fadeTriggered = true;
                    }
                    // Phase 3: Warning (4.0s - 8.0s) -> 4 Seconds Duration
                    else if (waveTimer >= 4.0f && waveTimer < 8.0f) {
                        if (!warningSoundPlayed) {
                            SpaceGame.getGame().getSoundManager().playDarkLevelWarningSound();
                            SpaceGame.getGame().getSoundManager().fadeWarningSoundIn(1.0f);
                            warningSoundPlayed = true;
                        }

                        if (waveTimer >= 7.0f && waveTimer < 8.0f) {
                            if (!fadeWarningOutTriggered) {
                                SpaceGame.getGame().getSoundManager().fadeWarningSoundOut(1.0f);
                                fadeWarningOutTriggered = true;
                            }
                        }

                        SpaceGame.getGame().getUiManager().displaySwarmWarning(waveTimer - 4.0f, 4.0f);
                    }
                } else {
                    float baseRefill = 20.0f;
                    float bonus = spaceship.getBossesDefeated() * 5.0f;
                    String bonusText = "+" + (int) (baseRefill + bonus) + "% ENERGY";
                    SpaceGame.getGame().getUiManager().displayNewLevel(waveTimer, TIME_TO_WAVE, bonusText);
                }
            }
        }

    }

    public void update() {
        if (currentLevel != null && currentLevel.getEndLevel()) {
            if (spaceship.getBossesDefeated() > 0) {
                // Fix: Do NOT increase max energy here. It's already done when Boss dies.
                // Doing it here would increase it every level indefinitely.
            }
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

                currentTimeToWave = 10.5f; // 2s (Info) + 2s (Fade) + 4s (Warning) + 1s (Gap) + 1.5s (Blink)
                com.space.game.levels.DynamicLevel dl = (com.space.game.levels.DynamicLevel) currentLevel;

                // Phase 3: Blinking (9.0s - 10.5s) -- Added 1s delay (Warning ends at 8s)
                if (waveTimer >= 9.0f) {
                    // Toggle lights out every 0.25s (slower blinking)
                    boolean lightsOut = ((int) ((waveTimer * 4)) % 2 == 0);
                    dl.setLightsOut(lightsOut);
                }
            } else if (currentLevel.getConfig().isSwarmWarning()) {
                currentTimeToWave = 8.0f; // 2s (Info) + 2s (Fade) + 4s (Warning)
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
                // Ensure warning sound is stopped
                SpaceGame.getGame().getSoundManager().stopDarkLevelWarningSound();

                // Only fade music in if it was faded out (Dark Level or Swarm)
                if (currentLevel.getConfig().isDarkLevel() || currentLevel.getConfig().isSwarmWarning()) {
                    SpaceGame.getGame().getMusicManager().fadeMusicIn(2.0f);
                } else if (!SpaceGame.getGame().getMusicManager().isPlaying()
                        && !SpaceGame.getGame().getMusicManager().isBossMusicActive()) {
                    // If no music playing (e.g. after Boss silence), start playlist
                    SpaceGame.getGame().getMusicManager().playMusic();
                }
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
        waveActive = false;
        warningSoundPlayed = false;
        fadeWarningOutTriggered = false;
        fadeTriggered = false;
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

    public ParticleManager getParticleManager() {
        return particleManager;
    }

}
