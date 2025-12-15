package com.space.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.SoundManager;
import com.space.game.managers.UIManager;
import com.space.game.managers.GameStateManager.State;

public class SettingsState implements GameStateInterface {
  private GameStateManager gsm;
  private UIManager uiManager;
  private SoundManager soundManager;
  private int currentSelection = 0;

  // Options: 0: Music, 1: Sound, 2: Back
  private final int MAX_OPTIONS = 3;

  public SettingsState(GameStateManager gsm, UIManager uiManager, SoundManager soundManager) {
    this.gsm = gsm;
    this.uiManager = uiManager;
    this.soundManager = soundManager;
  }

  @Override
  public void enter() {
    currentSelection = 0;
  }

  @Override
  public void update(SpriteBatch batch) {
    handleInput();

    float musicVol = soundManager.getVolumeMusic();
    float soundVol = soundManager.getVolumeSound();

    uiManager.displaySettings(soundVol, musicVol, currentSelection);
  }

  @Override
  public void exit() {
    if (com.space.game.SpaceGame.settingsHandler != null) {
      com.space.game.SpaceGame.settingsHandler.saveSettings(soundManager.getVolumeMusic(),
          soundManager.getVolumeSound());
    }
  }

  @Override
  public State getState() {
    return State.SETTINGS;
  }

  private void handleInput() {
    // Navigation (Up/Down)
    if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
      currentSelection--;
      if (currentSelection < 0)
        currentSelection = MAX_OPTIONS - 1;
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
      currentSelection++;
      if (currentSelection >= MAX_OPTIONS)
        currentSelection = 0;
    }

    // Adjustment (Left/Right)
    float step = 0.05f;
    if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
      // Check for hold delay if using isKeyPressed? For now stick to just pressed or
      // basic repeat
      // Let's use isKeyPressed but we need a timer or just use justPressed for
      // precision
      // For smoother slider, isKeyPressed with a small timer is better, but let's
      // stick to simple first
    }

    // Simpler: JustPressed for stepped changes, or KeyPressed for continuous
    // Using KeyPressed directly might be too fast. Let's use JustPressed for now.
    boolean left = Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A);
    boolean right = Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D);

    if (left) {
      adjustVolume(-step);
    } else if (right) {
      adjustVolume(step);
    }

    // Selection
    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
      if (currentSelection == 2) { // Back
        returnToPreviousState();
      }
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      returnToPreviousState();
    }
  }

  private void returnToPreviousState() {
    State prev = gsm.getPreviousState();
    if (prev == State.PAUSED) {
      gsm.setState(State.PAUSED);
    } else {
      gsm.setState(State.MENU);
    }
  }

  private void adjustVolume(float delta) {
    if (currentSelection == 0) { // Music
      float vol = soundManager.getVolumeMusic() + delta;
      soundManager.set_VolumeMusic(vol);
    } else if (currentSelection == 1) { // Sound
      float vol = soundManager.getVolumeSound() + delta;
      soundManager.set_VolumeSound(vol);
      if (delta > 0 || (delta < 0 && soundManager.getVolumeSound() > 0)) {
        // Play a test sound only if we are changing it? simpler to just set it.
        // Maybe play a small blip?
        // soundManager.playBulletSound(); // Might be annoying
      }
    }
  }
}
