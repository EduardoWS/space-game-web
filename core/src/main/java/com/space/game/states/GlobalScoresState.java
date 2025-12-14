package com.space.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.UIManager;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.ScoreManager;

public class GlobalScoresState implements GameStateInterface {

    private UIManager uiManager;
    private GameStateManager gsm;
    private List<ScoreManager.ScoreEntry> scoresList;
    private ScoreManager scoreManager;
    private boolean isLoading;
    private String errorMessage;

    public GlobalScoresState(GameStateManager gsm, UIManager uiManager) {
        this.uiManager = uiManager;
        this.gsm = gsm;
        this.scoreManager = new ScoreManager();
    }

    @Override
    public void enter() {
        // soundManager.playScoresMusic();
        isLoading = true;
        errorMessage = null;
        scoresList = null;

        scoreManager.loadGlobalScores(new ScoreManager.ScoreCallback() {
            @Override
            public void onScoresLoaded(List<ScoreManager.ScoreEntry> scores) {
                scoresList = scores;
                isLoading = false;
            }

            @Override
            public void onError(String error) {
                errorMessage = error;
                isLoading = false;
            }
        });
    }

    @Override
    public void update(SpriteBatch batch) {
        if (isLoading) {
            uiManager.displayLoading("Connecting...");
        } else if (errorMessage != null) {
            uiManager.displayError("Error: " + errorMessage);
        } else if (scoreManager.isError()) {
            uiManager.displayError("Error loading global scores, please contact the developer: eduardorr.ws@gmail.com");
        } else {
            if (scoresList != null) {
                uiManager.displayScores(scoresList, true);
            } else {
                uiManager.displayError("No scores found.");
            }
        }
        handleInput();
    }

    @Override
    public State getState() {
        return State.GLOBAL_SCORES;
    }

    @Override
    public void exit() {
        // soundManager.stopScoresMusic();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            gsm.setState(State.MENU);
        }
    }

}
