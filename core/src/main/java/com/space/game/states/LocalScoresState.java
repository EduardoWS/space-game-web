package com.space.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.UIManager;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.ScoreManager;

public class LocalScoresState implements GameStateInterface {

    private UIManager uiManager;
    private GameStateManager gsm;
    private List<ScoreManager.ScoreEntry> scoresList;
    private ScoreManager scoreManager;

    public LocalScoresState(GameStateManager gsm, UIManager uiManager) {
        this.uiManager = uiManager;
        this.gsm = gsm;
        this.scoreManager = new ScoreManager();
        
    }

    @Override
    public void enter() {
        // soundManager.playScoresMusic();
        scoresList = scoreManager.loadLocalScores();
    }

    @Override
    public void update(SpriteBatch batch) {
        uiManager.displayScores(scoresList, false);
        handleInput();
    }


    @Override
    public State getState() {
        return State.LOCAL_SCORES;
    }

    @Override
    public void exit() {
        // soundManager.stopScoresMusic();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            gsm.setState(State.MENU);
        }
    }


}
