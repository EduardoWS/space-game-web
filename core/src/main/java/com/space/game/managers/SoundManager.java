package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private float volume_sound = 0.5f; // Volume padrão é 1.0 (máximo)
    private Sound bulletSound;
    private Sound hitAlienSound;
    private Sound hitDeadAlienSound;
    private Sound bossExplosionSound;

    // Warning Sounds
    private Sound bossWarningSound;
    private Sound darkLevelWarningSound;
    private long darkLevelWarningSoundId = -1;
    private long bossWarningSoundId = -1;

    // Charging Sound
    private long chargingSoundId = -1;
    private Sound chargingSound;

    // Warning Fade Logic
    private float warningFadeTimer = 0;
    private float warningFadeDuration = 0;
    private float warningInitialVolume = 0;
    private float warningTargetVolume = 0;
    private boolean isWarningFading = false;

    public void loadSounds() {
        bulletSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Spaceshipshot.wav"));
        hitAlienSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hitAlien.wav"));
        hitDeadAlienSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hitDeadAlien.wav"));
        bossExplosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/boss_explosion.wav"));

        loadChargingSound();
        bossWarningSound = Gdx.audio.newSound(Gdx.files.internal("sounds/boss_warning.wav"));
        darkLevelWarningSound = Gdx.audio.newSound(Gdx.files.internal("sounds/warning.wav"));
    }

    public void initializeVolume() {
        if (com.space.game.SpaceGame.settingsHandler != null) {
            float[] settings = com.space.game.SpaceGame.settingsHandler.loadSettings();
            if (settings != null && settings.length >= 2) {
                float s = settings[1];
                // Clamp values
                if (s < 0)
                    s = 0;
                if (s > 1)
                    s = 1;

                this.volume_sound = s;
                Gdx.app.log("SoundManager", "Initialized volume from settings: Sound=" + s);
            }
        }
    }

    public void update() {
        updateWarningFade(Gdx.graphics.getDeltaTime());
    }

    public void playBossWarningSound() {
        if (bossWarningSound != null) {
            bossWarningSound.stop(); // Stop any previous
            bossWarningSoundId = bossWarningSound.play(volume_sound);
        }
    }

    public void playDarkLevelWarningSound() {
        if (darkLevelWarningSound != null) {
            darkLevelWarningSound.stop(); // Stop any previous
            darkLevelWarningSoundId = darkLevelWarningSound.play(volume_sound);
        }
    }

    public void stopDarkLevelWarningSound() {
        if (darkLevelWarningSound != null) {
            darkLevelWarningSound.stop();
            darkLevelWarningSoundId = -1;
        }
    }

    public void fadeWarningSoundIn(float duration) {
        if (darkLevelWarningSoundId == -1)
            return;

        isWarningFading = true;
        warningFadeTimer = 0;
        warningFadeDuration = duration;
        warningInitialVolume = 0.05f;
        warningTargetVolume = volume_sound;

        if (darkLevelWarningSound != null) {
            darkLevelWarningSound.setVolume(darkLevelWarningSoundId, warningInitialVolume);
        }
    }

    public void fadeWarningSoundOut(float duration) {
        if (darkLevelWarningSoundId == -1)
            return;

        isWarningFading = true;
        warningFadeTimer = 0;
        warningFadeDuration = duration;
        warningInitialVolume = volume_sound;
        warningTargetVolume = 0.0f;
    }

    private void updateWarningFade(float dt) {
        if (!isWarningFading)
            return;

        warningFadeTimer += dt;
        float progress = Math.min(1.0f, warningFadeTimer / warningFadeDuration);
        float newVolume = warningInitialVolume + (warningTargetVolume - warningInitialVolume) * progress;

        if (darkLevelWarningSound != null && darkLevelWarningSoundId != -1) {
            darkLevelWarningSound.setVolume(darkLevelWarningSoundId, newVolume);
        }

        if (progress >= 1.0f) {
            isWarningFading = false;
            if (warningTargetVolume == 0.0f) {
                stopDarkLevelWarningSound();
            }
        }
    }

    public void set_VolumeSound(float volume) {
        if (volume < 0.0f) {
            this.volume_sound = 0.0f;
        } else if (volume > 1.0f) {
            this.volume_sound = 1.0f;
        } else {
            this.volume_sound = volume;
        }
    }

    public float getVolumeSound() {
        return this.volume_sound;
    }

    public void playBulletSound() {
        bulletSound.play(volume_sound);
    }

    public void playAlienHitSound() {
        hitAlienSound.play(volume_sound);
    }

    public void playDeadAlienHitSound() {
        hitDeadAlienSound.play(volume_sound);
    }

    public void playBossExplosionSound() {
        if (bossExplosionSound != null) {
            bossExplosionSound.play(volume_sound);
        }
    }

    public void loadChargingSound() {
        chargingSound = Gdx.audio.newSound(Gdx.files.internal("sounds/energyGun.wav"));
    }

    public void playChargingSound() {
        if (chargingSound == null) {
            // Should have been loaded, but load just in case
            loadChargingSound();
        }
        // Stop any previous instances to ensure we don't layer them
        chargingSound.stop();
        chargingSoundId = chargingSound.play(volume_sound);
    }

    public void stopChargingSound() {
        if (chargingSound != null) {
            chargingSound.stop(); // Stop ALL instances of this sound
            chargingSoundId = -1;
        }
    }

    public void dispose() {
        bulletSound.dispose();
        hitAlienSound.dispose();
        hitDeadAlienSound.dispose();
        if (bossExplosionSound != null)
            bossExplosionSound.dispose();
        if (bossWarningSound != null)
            bossWarningSound.dispose();
        if (darkLevelWarningSound != null)
            darkLevelWarningSound.dispose();
        if (chargingSound != null)
            chargingSound.dispose();
    }
}
