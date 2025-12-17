package com.space.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.UIManager;

public class IntroState implements GameStateInterface {

  private GameStateManager gsm;
  private UIManager uiManager;

  public IntroState(GameStateManager gsm, UIManager uiManager) {
    this.gsm = gsm;
    this.uiManager = uiManager;
  }

  @Override
  public void enter() {
    // No music or specific initialization needed for now
  }

  @Override
  public void update(SpriteBatch batch) {
    // uiManager.displayIntro(); // Moved to renderUI

    if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
      gsm.setState(State.MENU);
    }
  }

  @Override
  public State getState() {
    return State.INTRO;
  }

  @Override
  public void renderUI(SpriteBatch batch) {
    uiManager.displayIntro();
  }

  @Override
  public void exit() {
    // Prepare for menu
  }
}
