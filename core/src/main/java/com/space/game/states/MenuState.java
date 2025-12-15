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

    private float stateTimer;
    private int currentSelection = 0;

    @Override
    public void enter() {
        soundManager.playMenuMusic();
        isPlaying = false;
        currentSelection = 0;

        State previous = gsm.getPreviousState();
        if (previous == State.INTRO || previous == State.GAME_OVER || previous == null) {
            stateTimer = 0f;
        } else {
            stateTimer = 10f; // Skip animation
        }
    }

    private int controlsSelection = 0;

    @Override
    public void update(SpriteBatch batch) {
        stateTimer += Gdx.graphics.getDeltaTime();

        if (isPlaying) {
            uiManager.displayGameControls(controlsSelection);
        } else {
            uiManager.displayMenu(scoreManager.isDatabaseAvailable(), currentSelection, stateTimer);
        }

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            soundManager.ensureMenuMusicPlaying();
        }

        if (!isPlaying) {
            // ... previous menu logic (Start, Scores, Settings, Logout) ...
            // (Copy existing logic for selection 0-3)
            int optionsCount = 4;

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
                    controlsSelection = 0; // Default to Start
                } else if (currentSelection == 1) {
                    com.space.game.states.GlobalScoresState scoresState = (com.space.game.states.GlobalScoresState) gsm
                            .getStateInstance(State.GLOBAL_SCORES);
                    scoresState.setRecentScore(-1);
                    gsm.setState(State.GLOBAL_SCORES);
                } else if (currentSelection == 2) {
                    gsm.setState(State.SETTINGS);
                } else if (currentSelection == 3) {
                    // Exit to Launcher
                    if (com.space.game.SpaceGame.exitHandler != null) {
                        com.space.game.SpaceGame.exitHandler.exitToLauncher();
                    }
                }
            }

        } else {
            // Controls Screen Logic
            // 0: Start, 1: Back
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                    Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                if (controlsSelection == 0)
                    controlsSelection = 1;
                else
                    controlsSelection = 0;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (controlsSelection == 0) {
                    // Start Game
                    soundManager.stopMenuMusic();
                    soundManager.playMusic();
                    gsm.setState(State.PLAYING);
                } else {
                    // Back
                    isPlaying = false;
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                isPlaying = false;
            }
        }
    }
}
