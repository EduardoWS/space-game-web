package com.space.game.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.space.game.config.LevelConfig;
import com.space.game.entities.Spaceship;

import com.space.game.graphics.TextureManager;
import com.space.game.managers.AlienManager;
import com.space.game.managers.BulletManager;
import com.space.game.managers.CollisionManager;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.InputManager;
import com.space.game.managers.SoundManager;
import com.space.game.managers.UIManager;
import com.badlogic.gdx.Gdx;
import com.space.game.SpaceGame;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;

public class DynamicLevel implements Level {
    private Spaceship spaceship;
    private BulletManager bulletManager;
    private AlienManager alienManager;
    private CollisionManager collisionManager;
    private TextureManager textureManager;
    private SoundManager soundManager;
    private UIManager uiManager;
    private GameStateManager gsm;
    private LevelConfig config;
    private InputManager inputManager;
    private boolean endLevel;
    private boolean isSpaceshipNoMunition;

    private ShapeRenderer shapeRenderer;
    private com.space.game.managers.ParticleManager particleManager;

    public DynamicLevel(LevelConfig config, Spaceship spaceship, BulletManager bulletManager,
            com.space.game.managers.ParticleManager particleManager) {
        this.shapeRenderer = new ShapeRenderer();
        this.textureManager = SpaceGame.getGame().getTextureManager();
        this.uiManager = SpaceGame.getGame().getUiManager();
        this.gsm = SpaceGame.getGame().getGsm();
        this.config = config;
        this.soundManager = SpaceGame.getGame().getSoundManager();

        this.particleManager = particleManager;
        this.bulletManager = bulletManager;

        this.spaceship = spaceship;
        // Spaceship bulletManager reference is already updated in MapManager

        inputManager = new InputManager(gsm, spaceship);
        Gdx.input.setInputProcessor(inputManager);

        alienManager = new AlienManager(textureManager, spaceship, config);
        collisionManager = new CollisionManager(bulletManager, alienManager, spaceship, soundManager, particleManager);

        // alienManager.spawnAliens(spaceship); // Removed to avoid spawning during
        // transition
        uiManager.setHordas(config.getLevelNumber());

        if (config.getLevelNumber() == 1) {
            spaceship.setEnergy(42.0f);
        } else {
            spaceship.addEnergy(20.0f);
        }
        spaceship.setStreakCount(config.getStreak());
        spaceship.setConsecutiveKills(config.getConsecutiveKills());
        spaceship.setKillCount(config.getKills());

        isSpaceshipNoMunition = false;

        endLevel = false;
    }

    private boolean isDarkMaskActive = true;
    private boolean isLightsOut = false; // "Pisca inteira" effect

    public void setDarkMaskActive(boolean active) {
        this.isDarkMaskActive = active;
    }

    public void setLightsOut(boolean lightsOut) {
        this.isLightsOut = lightsOut;
    }

    @Override
    public void render(SpriteBatch batch) {
        // Apply Ambient Lighting from Theme
        if (config.getTheme() != null) {
            batch.setColor(config.getTheme().getAmbientColor());
        }

        alienManager.render(batch);
        bulletManager.render(batch);

        if (isLightsOut) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect(-10000, -10000, 20000, 20000);
            shapeRenderer.end();
            batch.begin();
            // Restore ambient color after shapeRenderer usage if needed,
            // though batch.begin() resets color usually to white or previous?
            // SpriteBatch usually retains color. let's re-apply to be safe if we continue
            // drawing sprites.
            if (config.getTheme() != null) {
                batch.setColor(config.getTheme().getAmbientColor());
            }
        } else if (config.isDarkLevel() && isDarkMaskActive) {
            batch.end();

            Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
            Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);

            // Draw Cone to Stencil
            Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
            Gdx.gl.glStencilMask(0xFF);

            // Disable color writing so we only write to the stencil buffer
            Gdx.gl.glColorMask(false, false, false, false);

            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1, 1, 1, 1);

            Rectangle bounds = spaceship.getBounds();
            float shipX = spaceship.getPosition().x + bounds.width / 2;
            float shipY = spaceship.getPosition().y + bounds.height / 2;
            float angle = spaceship.getAngle() + 90;

            shapeRenderer.arc(shipX, shipY, 1200f, angle - 30, 60);
            shapeRenderer.end();

            // Re-enable color writing
            Gdx.gl.glColorMask(true, true, true, true);

            // Draw Black Overlay where Stencil != 1
            Gdx.gl.glStencilFunc(GL20.GL_NOTEQUAL, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 1);
            shapeRenderer.rect(-10000, -10000, 20000, 20000);
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);

            batch.begin();
            // Re-apply ambient color
            if (config.getTheme() != null) {
                batch.setColor(config.getTheme().getAmbientColor());
            }
        }

        if (particleManager != null) {
            particleManager.render(batch);
        }

        spaceship.render(batch);

        // RESET COLOR TO WHITE FOR UI RENDERING LATER
        batch.setColor(Color.WHITE);
    }

    @Override
    public void update() {
        if (alienManager.getEndLevel() == true) {
            endLevel = true;
            return;
        }

        if (spaceship.getEnergy() < Spaceship.FIRE_COST && !isSpaceshipNoMunition) {
            alienManager.setIsSpaceshipNoMunition(true);
            isSpaceshipNoMunition = true;
        }

        spaceship.update();
        bulletManager.update();
        alienManager.update(bulletManager.getBullets());
        collisionManager.checkBulletCollisions();

        if (collisionManager.checkSpaceshipCollisions()) {
            gsm.setState(GameStateManager.State.GAME_OVER);
        }

        alienManager.spawnAliens(spaceship);

        if (particleManager != null) {
            particleManager.update(Gdx.graphics.getDeltaTime());
        }

        inputManager.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void updateTransition() {
        spaceship.update();
        if (particleManager != null)
            particleManager.update(Gdx.graphics.getDeltaTime());
        bulletManager.update();
        inputManager.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void startWave() {
        alienManager.spawnAliens(spaceship);
    }

    @Override
    public void dispose() {
        if (spaceship != null) {
            // spaceship.dispose();
        }

        // bulletManager.dispose(); // Managed by MapManager
        alienManager.dispose();
        collisionManager = null;
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    @Override
    public Spaceship getSpaceship() {
        return spaceship;
    }

    public AlienManager getAlienManager() {
        return alienManager;
    }

    public LevelConfig getConfig() {
        return config;
    }

    public boolean getEndLevel() {
        return endLevel;
    }

    public void freeSpaceship() {
        spaceship = null;
    }
}
