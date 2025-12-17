package com.space.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.MapManager;
import com.space.game.managers.SoundManager;
import com.space.game.managers.UIManager;

public class PausedState implements GameStateInterface {

    private UIManager uiManager;
    private MapManager mapManager;
    private GameStateManager gsm;
    private SoundManager soundManager;
    private boolean wasPlaying;

    public PausedState(GameStateManager gsm, MapManager mapManager, UIManager uiManager, SoundManager soundManager) {
        this.uiManager = uiManager;
        this.mapManager = mapManager;
        this.gsm = gsm;
        this.soundManager = soundManager;
    }

    private boolean inSettingsMenu = false;
    private int currentSelection = 0;

    @Override
    public State getState() {
        return State.PAUSED;
    }

    // Menu Options: 0: Resume, 1: Restart, 2: Settings, 3: Exit
    // Settings Options: 0: Music, 1: Sound, 2: Back

    @Override
    public void enter() {
        if (soundManager.isPlaying()) {
            soundManager.pauseMusic();
            wasPlaying = true;
        } else {
            wasPlaying = false;
        }
        currentSelection = 0;
        inSettingsMenu = false; // Reset to main pause menu
    }

    @Override
    public void update(SpriteBatch batch) {
        // mapManager.render(batch);
        // mapManager.render(batch);
        mapManager.render(batch);

        handleInput();
    }

    @Override
    public void renderUI(SpriteBatch batch) {
        uiManager.displayPausedMenu(mapManager.getSpaceship(), currentSelection, inSettingsMenu,
                soundManager.getVolumeMusic(), soundManager.getVolumeSound());
    }

    @Override
    public void exit() {
        if (com.space.game.SpaceGame.settingsHandler != null) {
            com.space.game.SpaceGame.settingsHandler.saveSettings(soundManager.getVolumeMusic(),
                    soundManager.getVolumeSound());
        }
    }

    private void handleInput() {
        int maxOptions = inSettingsMenu ? 3 : 4;

        // Navigation (Up/Down)
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            currentSelection--;
            if (currentSelection < 0)
                currentSelection = maxOptions - 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            currentSelection++;
            if (currentSelection >= maxOptions)
                currentSelection = 0;
        }

        if (inSettingsMenu) {
            // Volume Adjustment logic
            float step = 0.05f;
            boolean left = Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A);
            boolean right = Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D);

            if (left)
                adjustVolume(-step);
            else if (right)
                adjustVolume(step);

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (currentSelection == 2) { // Back button
                    inSettingsMenu = false; // Go back to Pause Menu
                    currentSelection = 2; // Return to selection on "Settings" button
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                inSettingsMenu = false;
                currentSelection = 2;
            }

        } else {
            // Main Pause Menu Logic
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                selectOption();
            }
        }
    }

    private void adjustVolume(float delta) {
        if (currentSelection == 0) { // Music
            float vol = soundManager.getVolumeMusic() + delta;
            soundManager.set_VolumeMusic(vol);
        } else if (currentSelection == 1) { // Sound
            float vol = soundManager.getVolumeSound() + delta;
            soundManager.set_VolumeSound(vol);
        }
    }

    private void selectOption() {
        switch (currentSelection) {
            case 0: // Resume
                if (wasPlaying) {
                    soundManager.resumeMusic();
                }
                gsm.setState(State.PLAYING);
                break;
            case 1: // Restart
                soundManager.stopMusic();
                // Ensure volume is saved before restart?
                if (com.space.game.SpaceGame.settingsHandler != null) {
                    com.space.game.SpaceGame.settingsHandler.saveSettings(soundManager.getVolumeMusic(),
                            soundManager.getVolumeSound());
                }

                soundManager.playMusic();
                mapManager.reset();
                mapManager.loadLevel(1);
                gsm.setState(State.PLAYING);
                break;
            case 2: // Settings
                inSettingsMenu = true;
                currentSelection = 0; // Select first option in settings (Music)
                break;
            case 3: // Exit
                soundManager.stopMusic();
                if (com.space.game.SpaceGame.settingsHandler != null) {
                    com.space.game.SpaceGame.settingsHandler.saveSettings(soundManager.getVolumeMusic(),
                            soundManager.getVolumeSound());
                }
                mapManager.reset();
                gsm.setState(State.MENU);
                break;
        }
    }

}
