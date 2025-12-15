package com.space.game.managers;

import com.space.game.states.GameStateInterface;
import java.util.HashMap;
import java.util.Map;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.states.IntroState;
import com.space.game.states.MenuState;
import com.space.game.states.PlayingState;
import com.space.game.states.GameOverState;
import com.space.game.states.PausedState;
import com.space.game.states.GlobalScoresState;
import com.space.game.Game;

public class GameStateManager {
    public enum State {
        MENU, PLAYING, GAME_OVER, PAUSED, GLOBAL_SCORES, LOCAL_SCORES, INTRO, SETTINGS
    }

    private Map<State, GameStateInterface> states;
    private GameStateInterface currentState;

    public GameStateManager(Game game) {
        states = new HashMap<>();
        states.put(State.INTRO, new IntroState(this, game.getUiManager()));
        states.put(State.MENU, new MenuState(this, game.getUiManager(), game.getSoundManager()));
        states.put(State.PLAYING, new PlayingState(game, this, game.getUiManager()));
        states.put(State.GAME_OVER,
                new GameOverState(this, game.getMapManager(), game.getUiManager(), game.getSoundManager()));
        states.put(State.PAUSED,
                new PausedState(this, game.getMapManager(), game.getUiManager(), game.getSoundManager()));
        states.put(State.GLOBAL_SCORES, new GlobalScoresState(this, game.getUiManager(), game.getSoundManager()));
        states.put(State.SETTINGS,
                new com.space.game.states.SettingsState(this, game.getUiManager(), game.getSoundManager()));

        setState(State.INTRO);
    }

    private State previousStateEnum;

    public void setState(State newState) {
        if (currentState != null) {
            previousStateEnum = currentState.getState();
            currentState.exit();
        } else {
            previousStateEnum = null;
        }

        currentState = states.get(newState);
        if (currentState != null) {
            currentState.enter();
        }
    }

    public State getPreviousState() {
        return previousStateEnum;
    }

    public State getState() {
        return currentState.getState();
    }

    public GameStateInterface getStateInstance(State state) {
        return states.get(state);
    }

    public void update(SpriteBatch batch) {
        if (currentState != null) {
            currentState.update(batch);
        }
    }

}
