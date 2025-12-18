package com.space.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.space.game.managers.BackgroundManager;
import com.space.game.graphics.TextureManager;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.MapManager;
import com.space.game.managers.SoundManager;
import com.space.game.managers.UIManager;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;

public class Game {

    private SpriteBatch batch;

    private TextureManager textureManager;
    private UIManager uiManager;
    private ExtendViewport extendViewport;
    private GameStateManager gsm;
    private MapManager mapManager;
    private BackgroundManager backgroundManager;
    private SoundManager soundManager;

    private FrameBuffer fbo;

    private ShaderProgram shader;

    // Post-processing uniforms
    public float vignetteIntensity = 0.15f; // Reduced from 0.6f
    public float chromaticAberrationIntensity = 0.003f;

    public Game() {
        batch = new SpriteBatch();

        extendViewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        extendViewport.getCamera().position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);

        textureManager = new TextureManager();
        textureManager.loadTextures(textureManager);

        soundManager = new SoundManager();
        soundManager.loadSounds();
        soundManager.loadMusics();
        soundManager.initializeVolume();

        initShader();

        uiManager = new UIManager(this, batch);
        mapManager = new MapManager(this);

        backgroundManager = new BackgroundManager(textureManager, this);

        gsm = new GameStateManager(this);

    }

    private void initShader() {
        shader = new ShaderProgram(Gdx.files.internal("shaders/default.vert"),
                Gdx.files.internal("shaders/scifi.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("Shader", "Compilation failed:\n" + shader.getLog());
        }
    }

    public void render() {
        boolean useFbo = fbo != null && shader.isCompiled();

        if (useFbo) {
            fbo.begin();
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        extendViewport.apply();

        batch.begin();

        batch.setProjectionMatrix(extendViewport.getCamera().combined);

        backgroundManager.render(batch);
        backgroundManager.update(Gdx.graphics.getDeltaTime());

        gsm.update(batch);
        soundManager.update();

        batch.end();
        batch.setShader(null);

        if (useFbo) {
            fbo.end();

            // Render FBO to screen with shader
            batch.setShader(shader);
            batch.begin();
            // Reset projection to screen space for FBO drawing
            batch.setProjectionMatrix(
                    new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

            // Update Uniforms
            shader.setUniformf("u_vignetteIntensity", vignetteIntensity);
            shader.setUniformf("u_chromaticAberrationIntensity", chromaticAberrationIntensity);

            Texture fboTex = fbo.getColorBufferTexture();
            // Draw flipped on Y axis because FBOs are inverted
            batch.draw(fboTex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, fboTex.getWidth(),
                    fboTex.getHeight(), false, true);

            batch.end();
            batch.setShader(null);
        }

        // --- UI RENDERING (No Shader) ---
        batch.begin();
        // Reset projection to World/UI space (ExtendViewport)
        batch.setProjectionMatrix(extendViewport.getCamera().combined);

        gsm.renderUI(batch);

        batch.end();
    }

    public void resize(int width, int height) {
        extendViewport.update(width, height);
        extendViewport.getCamera().position.set(getWorldWidth() / 2f, getWorldHeight() / 2f, 0);
        extendViewport.getCamera().update();

        if (fbo != null)
            fbo.dispose();
        try {
            // Attempt to create FBO with Stencil buffer (Required for Dark Level cone
            // effect)
            // Use try-catch or safe instantiation if unsure about constructor support in
            // current libGDX version.
            // Constructor: Format, width, height, hasDepth, hasStencil
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false, true);
            fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("FBO", "Failed to create FBO with Stencil, trying without", e);
            try {
                fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
                fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            } catch (Exception ex) {
                Gdx.app.error("FBO", "Failed to create FBO fallback", ex);
            }
        }

        if (backgroundManager != null) {
            backgroundManager.resize(width, height);
        }
    }

    public void dispose() {
        if (mapManager != null) {
            mapManager.dispose();
        }
        batch.dispose();
        textureManager.dispose();
        backgroundManager.dispose();
        soundManager.dispose();
        if (fbo != null)
            fbo.dispose();
        if (shader != null)
            shader.dispose();
    }

    public GameStateManager getGsm() {
        return gsm;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public UIManager getUiManager() {
        return uiManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public float getWorldWidth() {
        return extendViewport.getWorldWidth();
    }

    public float getWorldHeight() {
        return extendViewport.getWorldHeight();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BackgroundManager getBackground() {
        return backgroundManager;
    }

    public com.space.game.managers.ParticleManager getParticleManager() {
        if (mapManager != null && mapManager.getParticleManager() != null) {
            return mapManager.getParticleManager();
        }
        return null;
    }

}
