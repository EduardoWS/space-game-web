package com.space.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.SoundManager;
import com.space.game.managers.UIManager;
import com.space.game.managers.ScoreManager;
import com.space.game.managers.GameStateManager.State;

public class MenuState implements GameStateInterface {
    private UIManager uiManager;
    private SoundManager soundManager;
    private GameStateManager gsm;
    private ScoreManager scoreManager;
    private boolean isPlaying;

    public MenuState(GameStateManager gsm, UIManager uiManager, SoundManager soundManager) {
        this.uiManager = uiManager;
        this.soundManager = soundManager;
        this.gsm = gsm;
        this.scoreManager = new ScoreManager();
    }

    private int currentSelection = 0;

    @Override
    public void enter() {
        soundManager.playMenuMusic();
        isPlaying = false;
        currentSelection = 0;
    }

    @Override
    public void update(SpriteBatch batch) {
        if (isPlaying) {
            uiManager.displayGameControls();
        } else {
            uiManager.displayMenu(scoreManager.isDatabaseAvailable(), currentSelection);
        }
        // Verificar entrada do usuário para iniciar o jogo
        handleInput();

        // Ensure menu music plays on user interaction (Web Autoplay fix)
        if (Gdx.input.justTouched()) {
            soundManager.ensureMenuMusicPlaying();
        }
    }

    @Override
    public State getState() {
        return State.MENU;
    }

    @Override
    public void exit() {
        // soundManager.stopMenuMusic();
    }

    private void handleInput() {
        // Try to play music on any key press if not playing
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            soundManager.ensureMenuMusicPlaying();
        }

        if (!isPlaying) {
            int optionsCount = scoreManager.isDatabaseAvailable() ? 2 : 1;

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                currentSelection--;
                if (currentSelection < 0) {
                    currentSelection = optionsCount - 1;
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                currentSelection++;
                if (currentSelection >= optionsCount) {
                    currentSelection = 0;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (currentSelection == 0) {
                    isPlaying = true;
                } else if (currentSelection == 1) {
                    gsm.setState(State.GLOBAL_SCORES);
                }
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                soundManager.stopMenuMusic();
                soundManager.playMusic();
                gsm.setState(State.PLAYING);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                isPlaying = false;
            }
        }
    }
}
