package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoundManager {
    private float volume_sound = 0.5f; // Volume padrão é 1.0 (máximo)
    private float volume_music = 0.25f; // Volume padrão é 1.0 (máximo)
    private Sound bulletSound;
    private Sound hitAlienSound;
    private Sound hitDeadAlienSound;

    private Music menu_music;
    private Music gameover_music;

    // Inner class to hold Music and its display name
    private class MusicTrack {
        Music music;
        String displayName;

        public MusicTrack(Music music, String displayName) {
            this.music = music;
            this.displayName = displayName;
        }
    }

    private List<MusicTrack> playlist;
    private int currentTrackIndex = 0;
    private boolean isMusicActive = false;
    private boolean hasCurrentTrackStarted = false;

    public void loadSounds() {
        menu_music = Gdx.audio.newMusic(Gdx.files.internal("musics/menu/Echoes_of_the_Last_Stand.mp3"));
        gameover_music = Gdx.audio.newMusic(Gdx.files.internal("musics/gameover/gameover.mp3"));

        bulletSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Spaceshipshot.wav"));
        hitAlienSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hitAlien.wav"));
        hitDeadAlienSound = Gdx.audio.newSound(Gdx.files.internal("sounds/hitDeadAlien.wav"));
        loadChargingSound();
    }

    public void initializeVolume() {
        if (com.space.game.SpaceGame.settingsHandler != null) {
            float[] settings = com.space.game.SpaceGame.settingsHandler.loadSettings();
            if (settings != null && settings.length >= 2) {
                float m = settings[0];
                float s = settings[1];
                // Clamp values
                if (m < 0)
                    m = 0;
                if (m > 1)
                    m = 1;
                if (s < 0)
                    s = 0;
                if (s > 1)
                    s = 1;

                this.volume_music = m;
                this.volume_sound = s;
                Gdx.app.log("SoundManager", "Initialized volume from settings: Music=" + m + " Sound=" + s);
            }
        }
    }

    public void loadMusics() {
        // Load playlist from JSON
        try {
            com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
            com.badlogic.gdx.utils.JsonValue base = reader.parse(Gdx.files.internal("data/playlist.json"));

            playlist = new ArrayList<>(); // Moved to constructor
            for (com.badlogic.gdx.utils.JsonValue entry = base.child; entry != null; entry = entry.next) {
                String fileNamePath = entry.asString();
                Gdx.app.log("SoundManager", "Loading music file: " + fileNamePath);

                Music music = Gdx.audio.newMusic(Gdx.files.internal(fileNamePath));

                // Format the display name
                String displayName = formatMusicName(fileNamePath);

                music.setOnCompletionListener(new Music.OnCompletionListener() {
                    @Override
                    public void onCompletion(Music music) {
                        Gdx.app.log("SoundManager", "OnCompletionListener triggered");
                        playNextTrack();
                    }
                });

                playlist.add(new MusicTrack(music, displayName));
            }

            if (playlist.isEmpty()) {
                Gdx.app.log("SoundManager", "No music files found in playlist.");
            } else {
                Gdx.app.log("SoundManager", "Found " + playlist.size() + " music files.");
            }

            // Shuffle
            Collections.shuffle(playlist);

            if (!playlist.isEmpty()) {
                currentTrackIndex = 0;
            }

        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Error loading playlist json", e);
        }
    }

    private String formatMusicName(String filePath) {
        // Extract filename from path
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        // Remove extension
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }

        // Replace underscores with spaces
        String[] words = fileName.split("_");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                // Capitalize first letter
                formattedName.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formattedName.append(word.substring(1));
                }
                formattedName.append(" ");
            }
        }

        return formattedName.toString().trim();
    }

    public String getCurrentTrackName() {
        if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            // Append the artist logic as requested
            return playlist.get(currentTrackIndex).displayName + " - OK Machine";
        }
        return "";
    }

    private long lastTrackChangeTime = 0;

    public void playNextTrack() {
        long currentTime = com.badlogic.gdx.utils.TimeUtils.millis();
        if (currentTime - lastTrackChangeTime < 1000) {
            Gdx.app.log("SoundManager", "Debounced playNextTrack");
            return;
        }
        lastTrackChangeTime = currentTime;

        if (playlist == null || playlist.isEmpty())
            return;

        // Stop current if playing
        try {
            if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
                MusicTrack current = playlist.get(currentTrackIndex);
                if (current.music.isPlaying()) {
                    current.music.stop();
                }
            }
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Error stopping current track", e);
        }

        // Move index
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size();

        // Play next
        try {
            MusicTrack nextTrack = playlist.get(currentTrackIndex);
            Music nextMusic = nextTrack.music;

            // Allow replay if it's same track or re-looping entire playlist
            nextMusic.stop(); // Ensure stopped before re-setup

            nextMusic.setPosition(0);
            nextMusic.setVolume(volume_music);
            nextMusic.setLooping(false);
            nextMusic.play();
            hasCurrentTrackStarted = false; // Reset for new track
            Gdx.app.log("SoundManager",
                    "Playing next track: " + currentTrackIndex + " (" + nextTrack.displayName + ")");
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Error playing next track", e);
            // Try next one if this fails?
        }
    }

    public void playPreviousTrack() {
        if (playlist == null)
            return;
        if (playlist.isEmpty())
            return;

        MusicTrack current = playlist.get(currentTrackIndex);
        if (current.music.isPlaying()) {
            current.music.stop();
        }

        currentTrackIndex = (currentTrackIndex - 1 + playlist.size()) % playlist.size();
        MusicTrack prev = playlist.get(currentTrackIndex);

        prev.music.setPosition(0);
        prev.music.play();
        prev.music.setVolume(volume_music);
        hasCurrentTrackStarted = false;
    }

    public void playMusic() {
        if (playlist == null)
            return;
        if (playlist.isEmpty())
            return;

        MusicTrack current = playlist.get(currentTrackIndex);
        if (current.music.isPlaying()) {
            current.music.stop();
        }
        // embaralhar a playlist
        Collections.shuffle(playlist);
        // resetar a música para o início
        currentTrackIndex = 0;

        current = playlist.get(currentTrackIndex);
        current.music.setPosition(0);
        current.music.setLooping(false);
        current.music.setVolume(volume_music);
        current.music.play();
        isMusicActive = true;
        hasCurrentTrackStarted = false;
    }

    public void stopMusic() {
        if (playlist == null)
            return;
        isMusicActive = false;
        if (!playlist.isEmpty() && playlist.get(currentTrackIndex).music.isPlaying()) {
            playlist.get(currentTrackIndex).music.stop();
        }
    }

    public void pauseMusic() {
        if (playlist == null)
            return;
        isMusicActive = false;
        if (!playlist.isEmpty() && playlist.get(currentTrackIndex).music.isPlaying()) {
            playlist.get(currentTrackIndex).music.pause();
        }
    }

    public void resumeMusic() {
        if (playlist == null)
            return;
        isMusicActive = true;
        if (!playlist.isEmpty()) {
            Music current = playlist.get(currentTrackIndex).music;
            if (!current.isPlaying()) {
                current.setVolume(volume_music); // Ensure volume is up to date
                current.play();
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

    public void set_VolumeMusic(float volume) {
        if (volume < 0.0f) {
            this.volume_music = 0.0f;
        } else if (volume > 1.0f) {
            this.volume_music = 1.0f;
        } else {
            this.volume_music = volume;
        }

        // Apply to currently playing music (or paused)
        if (menu_music != null)
            menu_music.setVolume(this.volume_music);
        if (gameover_music != null)
            gameover_music.setVolume(this.volume_music);
        if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            Music current = playlist.get(currentTrackIndex).music;
            current.setVolume(this.volume_music);
        }
    }

    public float getVolumeMusic() {
        return this.volume_music;
    }

    public void playMenuMusic() {
        if (menu_music != null && !menu_music.isPlaying()) {
            menu_music.setLooping(true);
            menu_music.setVolume(volume_music);
            menu_music.play();
        }
    }

    // Helper for Web Autoplay policy
    public void ensureMenuMusicPlaying() {
        if (menu_music != null && !menu_music.isPlaying()) {
            menu_music.setLooping(true);
            menu_music.setVolume(volume_music);
            menu_music.play();
        }
    }

    public void stopMenuMusic() {
        if (menu_music != null && menu_music.isPlaying()) {
            menu_music.stop();
        }
    }

    // Explicit update loop for Web/GWT playlist handling
    public void update() {
        if (isMusicActive && playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0
                && currentTrackIndex < playlist.size()) {
            Music current = playlist.get(currentTrackIndex).music;

            if (current.isPlaying()) {
                if (!hasCurrentTrackStarted) {
                    hasCurrentTrackStarted = true;
                    Gdx.app.log("SoundManager", "Track started playing: " + currentTrackIndex);
                }
            } else if (hasCurrentTrackStarted) {
                // It WAS playing, and now it's NOT. Thus it finished.
                // Reset flag and play next.
                Gdx.app.log("SoundManager", "Track finished (detected by polling): " + currentTrackIndex);
                hasCurrentTrackStarted = false;
                playNextTrack();
            }
        }
    }

    public void playGameOverMusic() {
        if (gameover_music != null && !gameover_music.isPlaying()) {
            // gameover_music.setLooping(true);
            gameover_music.setVolume(volume_music);
            gameover_music.play();
        }
    }

    public void stopGameOverMusic() {
        if (gameover_music != null && gameover_music.isPlaying()) {
            gameover_music.stop();
        }
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

    private long chargingSoundId = -1;
    private Sound chargingSound;

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

    public boolean isPlaying() {
        if (playlist == null || playlist.isEmpty() || currentTrackIndex < 0)
            return false;
        return playlist.get(currentTrackIndex).music.isPlaying();
    }

    public void dispose() {
        bulletSound.dispose();
        hitAlienSound.dispose();
        hitDeadAlienSound.dispose();
        menu_music.dispose();
        if (playlist == null)
            return;
        else if (playlist.isEmpty())
            return;
        else {
            for (MusicTrack track : playlist) {
                track.music.dispose();
            }

        }

    }
}
