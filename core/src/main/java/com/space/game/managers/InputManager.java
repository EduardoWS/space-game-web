package com.space.game.managers;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.space.game.SpaceGame;
import com.space.game.entities.Spaceship;
import com.space.game.managers.GameStateManager.State;
import com.space.game.config.GameConfig;

public class InputManager extends InputAdapter {
    private Spaceship spaceship;
    private GameStateManager gsm;
    private boolean turningLeft;
    private boolean turningRight;
    private float rotationHeldTime;

    public InputManager(GameStateManager gsm, Spaceship spaceship) {
        this.spaceship = spaceship;
        this.gsm = gsm;
        this.turningLeft = false;
        this.turningRight = false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (gsm.getState() != State.PLAYING) {
            return false;
        }
        switch (keycode) {
            // virar caso o jogador pressione a seta pra esquerda
            case Keys.LEFT:
                turningLeft = true;
                break;
            // virar caso o jogador pressione a seta pra direita
            case Keys.RIGHT:
                turningRight = true;
                break;

            case Keys.A:
                turningLeft = true;
                break;
            case Keys.D:
                turningRight = true;
                break;
            case Keys.SPACE:
                spaceship.startCharging();
                break;
            case Keys.P:
                turningLeft = false;
                turningRight = false;
                gsm.setState(State.PAUSED);
                break;
            case Keys.Q:
                SpaceGame.getGame().getMusicManager().playPreviousTrack();
                break;
            case Keys.E:
                SpaceGame.getGame().getMusicManager().playNextTrack();
                break;
            case Keys.W:
                if (SpaceGame.getGame().getMusicManager().isPlaying()) {
                    SpaceGame.getGame().getMusicManager().pauseMusic();
                } else {
                    SpaceGame.getGame().getMusicManager().resumeMusic();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        // Allow key release events to be processed even if not playing,
        // to prevent "stuck" keys when resuming from pause.
        switch (keycode) {
            case Keys.LEFT:
                turningLeft = false;
                break;
            case Keys.RIGHT:
                turningRight = false;
                break;
            case Keys.A:
                turningLeft = false;
                break;
            case Keys.D:
                turningRight = false;
                break;
            case Keys.SPACE:
                spaceship.releaseCharge();
                break;
        }
        return true;
    }

    public void update(float deltaTime) {
        if (turningLeft || turningRight) {
            rotationHeldTime += deltaTime;
        } else {
            rotationHeldTime = 0;
        }

        float currentRotationSpeed = GameConfig.SPACESHIP_ROTATION_SPEED;
        // if (rotationHeldTime >= GameConfig.ROTATION_ACCEL_DELAY) {
        //     currentRotationSpeed = GameConfig.SPACESHIP_ROTATION_SPEED_FAST;
        // }

        if (turningLeft) {
            if (spaceship.consumeRotationEnergy()) {
                spaceship.setAngle(spaceship.getAngle() + currentRotationSpeed * deltaTime);
            }
        }
        if (turningRight) {
            if (spaceship.consumeRotationEnergy()) {
                spaceship.setAngle(spaceship.getAngle() - currentRotationSpeed * deltaTime);
            }
        }
    }
}