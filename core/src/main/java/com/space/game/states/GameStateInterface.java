package com.space.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager.State;

public interface GameStateInterface {
    void enter();
    void update(SpriteBatch batch);
    State getState();
    void exit();
}
