package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicManager {
  private float volume_music = 0.25f; // Standard volume (max 1.0)

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

  private Music bossMusic1;
  private Music bossMusic2;
  private boolean isBossMusicActive = false;
  private int bossMusicPhase = 0;

  // Music Fade Logic
  private float fadeTimer = 0;
  private float fadeDuration = 0;
  private float initialFadeVolume = 0;
  private float targetFadeVolume = 0;
  private boolean isFading = false;
  private Music fadingMusic = null;

  private boolean bossDefeatedMode = false;

  // Boss explosion pause state
  private boolean bossMusicPausedForExplosion = false;
  private float bossMusicExplosionTimer = 0f;
  private float bossMusicExplosionDuration = 0f;

  // Manual pause state (when user pauses game)
  private boolean manuallyPaused = false;

  public MusicManager() {
    // Constructor
  }

  public void loadMusics() {
    menu_music = Gdx.audio.newMusic(Gdx.files.internal("musics/menu/Echoes_of_the_Last_Stand.mp3"));
    gameover_music = Gdx.audio.newMusic(Gdx.files.internal("musics/gameover/gameover.mp3"));

    // Load Boss Musics
    bossMusic1 = Gdx.audio.newMusic(Gdx.files.internal("musics/playing/boss/majestic_heraldic_1.m4a"));
    bossMusic2 = Gdx.audio.newMusic(Gdx.files.internal("musics/playing/boss/majestic_heraldic_2.m4a"));

    // Load playlist from JSON
    try {
      com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
      com.badlogic.gdx.utils.JsonValue base = reader.parse(Gdx.files.internal("data/playlist.json"));

      playlist = new ArrayList<>();
      for (com.badlogic.gdx.utils.JsonValue entry = base.child; entry != null; entry = entry.next) {
        String fileNamePath = entry.asString();
        Gdx.app.log("MusicManager", "Loading music file: " + fileNamePath);

        Music music = Gdx.audio.newMusic(Gdx.files.internal(fileNamePath));

        // Format the display name
        String displayName = formatMusicName(fileNamePath);

        music.setOnCompletionListener(new Music.OnCompletionListener() {
          @Override
          public void onCompletion(Music music) {
            Gdx.app.log("MusicManager", "OnCompletionListener triggered");
            playNextTrack();
          }
        });

        playlist.add(new MusicTrack(music, displayName));
      }

      if (playlist.isEmpty()) {
        Gdx.app.log("MusicManager", "No music files found in playlist.");
      } else {
        Gdx.app.log("MusicManager", "Found " + playlist.size() + " music files.");
      }

      // Shuffle
      Collections.shuffle(playlist);

      if (!playlist.isEmpty()) {
        currentTrackIndex = 0;
      }

    } catch (Exception e) {
      Gdx.app.error("MusicManager", "Error loading playlist json", e);
    }
  }

  public void initializeVolume() {
    if (com.space.game.SpaceGame.settingsHandler != null) {
      float[] settings = com.space.game.SpaceGame.settingsHandler.loadSettings();
      if (settings != null && settings.length >= 2) {
        float m = settings[0];
        // Clamp values
        if (m < 0)
          m = 0;
        if (m > 1)
          m = 1;

        this.volume_music = m;
        Gdx.app.log("MusicManager", "Initialized volume from settings: Music=" + m);
      }
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

  public void setBossDefeatedMode(boolean mode) {
    this.bossDefeatedMode = mode;
    if (mode && isBossMusicActive) {
      // Disable looping so it finishes naturally if it's currently looping
      if (bossMusicPhase == 1 && bossMusic1 != null) {
        // Still on phase 1, set completion listener to handle phase 2 properly
        bossMusic1.setOnCompletionListener(new Music.OnCompletionListener() {
          @Override
          public void onCompletion(Music music) {
            if (!isBossMusicActive)
              return;
            // Move to phase 2 but don't loop
            bossMusicPhase = 2;
            bossMusic2.setLooping(false); // Don't loop phase 2
            bossMusic2.setVolume(volume_music);
            bossMusic2.play();
            // Set completion listener for phase 2
            bossMusic2.setOnCompletionListener(new Music.OnCompletionListener() {
              @Override
              public void onCompletion(Music music2) {
                if (!isBossMusicActive)
                  return;
                stopBossMusic(true); // Stop and resume regular playlist
              }
            });
          }
        });
      } else if (bossMusicPhase == 2 && bossMusic2 != null) {
        bossMusic2.setLooping(false);
        // OnCompletion will trigger return to playlist
        bossMusic2.setOnCompletionListener(new Music.OnCompletionListener() {
          @Override
          public void onCompletion(Music music) {
            if (!isBossMusicActive)
              return; // Already stopped manually?
            // Determine next steps
            stopBossMusic(true); // Stop and resume regular playlist
          }
        });
      }
    }
  }

  public void pauseBossMusicForExplosion(float duration) {
    if (!isBossMusicActive)
      return;

    bossMusicPausedForExplosion = true;
    bossMusicExplosionTimer = 0f;
    bossMusicExplosionDuration = duration;

    // Pause the currently playing boss music
    if (bossMusicPhase == 1 && bossMusic1 != null && bossMusic1.isPlaying()) {
      bossMusic1.pause();
    } else if (bossMusicPhase == 2 && bossMusic2 != null && bossMusic2.isPlaying()) {
      bossMusic2.pause();
    }
  }

  public void playBossMusic() {
    if (bossMusic1 == null || bossMusic2 == null)
      return;

    // Stop current normal music
    if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
      if (playlist.get(currentTrackIndex).music.isPlaying()) {
        playlist.get(currentTrackIndex).music.stop();
      }
    }
    isMusicActive = false; // Pause normal playlist logic

    bossMusicPhase = 1;
    isBossMusicActive = true;

    bossMusic1.setLooping(false);
    bossMusic1.setVolume(volume_music);
    bossMusic1.play();

    bossMusic1.setOnCompletionListener(new Music.OnCompletionListener() {
      @Override
      public void onCompletion(Music music) {
        // Trigger Phase 2 immediately
        if (isBossMusicActive && bossMusicPhase == 1) {
          bossMusicPhase = 2;
          bossMusic2.setLooping(true);
          bossMusic2.setVolume(volume_music);
          bossMusic2.play();
        }
      }
    });
  }

  public void stopBossMusic() {
    stopBossMusic(true);
    bossDefeatedMode = false;
  }

  public void stopBossMusic(boolean resumePlaylist) {
    isBossMusicActive = false;
    bossMusicPhase = 0;
    bossDefeatedMode = false;

    if (bossMusic1 != null) {
      bossMusic1.stop();
      bossMusic1.setOnCompletionListener(null); // Reset listener
    }
    if (bossMusic2 != null) {
      bossMusic2.stop();
      bossMusic2.setOnCompletionListener(null); // Reset listener (important!)
    }

    // Resume normal playlist only if requested
    if (resumePlaylist) {
      if (playlist != null && !playlist.isEmpty() && currentTrackIndex < playlist.size())
        playMusic(); // Or resume specific track logic
    }
  }

  public boolean isBossMusicActive() {
    return isBossMusicActive;
  }

  public String getCurrentTrackName() {
    if (isBossMusicActive) {
      return "Majestic Heraldic - Boss Theme";
    }
    if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
      // Append the artist logic
      return playlist.get(currentTrackIndex).displayName + " - OK Machine";
    }
    return "";
  }

  private long lastTrackChangeTime = 0;

  public void playNextTrack() {
    // Block track switching during boss music
    if (isBossMusicActive) {
      Gdx.app.log("MusicManager", "Cannot switch tracks during boss music");
      return;
    }

    long currentTime = com.badlogic.gdx.utils.TimeUtils.millis();
    if (currentTime - lastTrackChangeTime < 1000) {
      Gdx.app.log("MusicManager", "Debounced playNextTrack");
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
      Gdx.app.error("MusicManager", "Error stopping current track", e);
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
      Gdx.app.log("MusicManager",
          "Playing next track: " + currentTrackIndex + " (" + nextTrack.displayName + ")");
    } catch (Exception e) {
      Gdx.app.error("MusicManager", "Error playing next track", e);
    }
  }

  public void playPreviousTrack() {
    // Block track switching during boss music
    if (isBossMusicActive) {
      Gdx.app.log("MusicManager", "Cannot switch tracks during boss music");
      return;
    }

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
    // shuffle the playlist
    Collections.shuffle(playlist);
    // reset music to start
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

    // Stop Everything
    if (isBossMusicActive) {
      stopBossMusic(false); // Do not resume playlist
    }

    if (!playlist.isEmpty() && currentTrackIndex < playlist.size()
        && playlist.get(currentTrackIndex).music.isPlaying()) {
      playlist.get(currentTrackIndex).music.stop();
    }
  }

  public void pauseMusic() {
    if (playlist == null)
      return;
    isMusicActive = false;
    manuallyPaused = true; // Track manual pause

    // Do NOT cancel explosion pause if active. Just let manuallyPaused hold the
    // update.
    // We only reset timer if we were NOT in explosion pause? No, just keep state.

    if (isBossMusicActive) {
      if (bossMusic1 != null && bossMusic1.isPlaying())
        bossMusic1.pause();
      if (bossMusic2 != null && bossMusic2.isPlaying())
        bossMusic2.pause();
    } else {
      if (playlist != null && !playlist.isEmpty() && currentTrackIndex < playlist.size()
          && playlist.get(currentTrackIndex).music.isPlaying()) {
        playlist.get(currentTrackIndex).music.pause();
      }
    }
  }

  public void resumeMusic() {
    if (playlist == null)
      return;
    manuallyPaused = false; // Clear manual pause

    // If explicit silence is forced by logic (explosion), do NOT play yet.
    if (bossMusicPausedForExplosion) {
      return; // Timer in update() will handle resuming when time is up.
    }

    if (isBossMusicActive) {
      // Resume Boss Music
      if (bossMusicPhase == 1 && bossMusic1 != null && !bossMusic1.isPlaying()) {
        bossMusic1.play();
      }
      if (bossMusicPhase == 2 && bossMusic2 != null && !bossMusic2.isPlaying()) {
        bossMusic2.play();
      }
    } else {
      isMusicActive = true;
      if (!playlist.isEmpty()) {
        Music current = playlist.get(currentTrackIndex).music;
        if (!current.isPlaying()) {
          current.setVolume(volume_music); // Ensure volume is up to date
          current.play();
        }
      }
    }
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

    // Apply to boss music
    if (bossMusic1 != null)
      bossMusic1.setVolume(this.volume_music);
    if (bossMusic2 != null)
      bossMusic2.setVolume(this.volume_music);

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

  public void update() {
    if (isFading) {
      updateFade(Gdx.graphics.getDeltaTime());
    }

    // Handle boss music explosion pause
    if (bossMusicPausedForExplosion && !manuallyPaused) {
      bossMusicExplosionTimer += Gdx.graphics.getDeltaTime();
      if (bossMusicExplosionTimer >= bossMusicExplosionDuration) {
        bossMusicPausedForExplosion = false;
        // Resume boss music with fade in only if not manually paused
        if (isBossMusicActive) {
          fadeMusicIn(1.0f); // 1.0 second fade in
          // Manually resume if fade doesn't trigger it fully or logic slightly off
          // fadeMusicIn handles resume
        }
      }
    }

    if (isMusicActive && playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0
        && currentTrackIndex < playlist.size()) {
      Music current = playlist.get(currentTrackIndex).music;

      if (current.isPlaying()) {
        if (!hasCurrentTrackStarted) {
          hasCurrentTrackStarted = true;
          Gdx.app.log("MusicManager", "Track started playing: " + currentTrackIndex);
        }
      } else if (hasCurrentTrackStarted && !isFading) { // Don't skip track if simply faded out/paused logic
        // It WAS playing, and now it's NOT. Thus it finished.
        // Reset flag and play next.
        Gdx.app.log("MusicManager", "Track finished (detected by polling): " + currentTrackIndex);
        hasCurrentTrackStarted = false;
        playNextTrack();
      }
    }
  }

  private void updateFade(float dt) {
    fadeTimer += dt;
    float progress = Math.min(1.0f, fadeTimer / fadeDuration);
    float newVolume = initialFadeVolume + (targetFadeVolume - initialFadeVolume) * progress;

    // Apply volume to active music
    if (fadingMusic != null) {
      fadingMusic.setVolume(newVolume);
    } else if (isBossMusicActive) {
      if (bossMusicPhase == 1 && bossMusic1 != null)
        bossMusic1.setVolume(newVolume);
      if (bossMusicPhase == 2 && bossMusic2 != null)
        bossMusic2.setVolume(newVolume);
    } else if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0) {
      playlist.get(currentTrackIndex).music.setVolume(newVolume);
    }

    if (progress >= 1.0f) {
      isFading = false;
      // specific logic for fade out completion
      if (targetFadeVolume == 0) {
        pauseMusic(); // Actually pause it now
      }
    }
  }

  public void fadeMusicOut(float duration) {
    if (!isMusicActive && !isBossMusicActive && fadingMusic == null)
      return;

    isFading = true;
    fadeTimer = 0;
    fadeDuration = duration;

    // Determine current volume source
    if (fadingMusic != null) {
      initialFadeVolume = fadingMusic.getVolume();
    } else if (isBossMusicActive) {
      if (bossMusicPhase == 1 && bossMusic1 != null)
        initialFadeVolume = bossMusic1.getVolume();
      else if (bossMusic2 != null)
        initialFadeVolume = bossMusic2.getVolume();
      else
        initialFadeVolume = volume_music;
    } else if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0) {
      initialFadeVolume = playlist.get(currentTrackIndex).music.getVolume();
    } else {
      initialFadeVolume = volume_music;
    }

    targetFadeVolume = volume_music * 0.1f; // Fade to 10% instead of 0
    if (targetFadeVolume < 0.05f)
      targetFadeVolume = 0.05f;
    if (targetFadeVolume > volume_music)
      targetFadeVolume = volume_music;

  }

  public void fadeMusicIn(float duration) {
    // Resume boss music specifically (don't call resumeMusic which would unpause
    // normal playlist)
    if (isBossMusicActive) {
      if (bossMusicPhase == 1 && bossMusic1 != null && !bossMusic1.isPlaying()) {
        bossMusic1.play();
      }
      if (bossMusicPhase == 2 && bossMusic2 != null && !bossMusic2.isPlaying()) {
        bossMusic2.play();
      }
    } else {
      // Only resume normal music if not in boss mode
      resumeMusic();
    }

    isFading = true;
    fadeTimer = 0;
    fadeDuration = duration;
    initialFadeVolume = volume_music * 0.1f;
    if (initialFadeVolume < 0.05f)
      initialFadeVolume = 0.05f;
    if (initialFadeVolume > volume_music)
      initialFadeVolume = 0;

    targetFadeVolume = volume_music;

    // Ensure volume starts at floor for fade in
    if (isBossMusicActive) {
      if (bossMusicPhase == 1 && bossMusic1 != null)
        bossMusic1.setVolume(initialFadeVolume);
      if (bossMusicPhase == 2 && bossMusic2 != null)
        bossMusic2.setVolume(initialFadeVolume);
    } else if (playlist != null && !playlist.isEmpty() && currentTrackIndex >= 0) {
      playlist.get(currentTrackIndex).music.setVolume(initialFadeVolume);
    }
  }

  public void playGameOverMusic() {
    // Force stop boss music without resuming playlist
    stopBossMusic(false);

    if (gameover_music != null && !gameover_music.isPlaying()) {
      gameover_music.setVolume(volume_music);
      gameover_music.play();
    }
  }

  public void stopGameOverMusic() {
    if (gameover_music != null && gameover_music.isPlaying()) {
      gameover_music.stop();
    }
  }

  public boolean isPlaying() {
    if (isBossMusicActive) {
      if (bossMusic1 != null && bossMusic1.isPlaying())
        return true;
      if (bossMusic2 != null && bossMusic2.isPlaying())
        return true;
      if (bossMusicPausedForExplosion) {
        return true; // Treat as playing so PausedState will "pause/resume" it (handling
                     // manuallyPaused flag)
      }
      return false; // Boss music active but not emitting sound (paused/stopped logic)
    }
    if (playlist == null || playlist.isEmpty() || currentTrackIndex < 0)
      return false;
    return playlist.get(currentTrackIndex).music.isPlaying();
  }

  public void dispose() {
    if (menu_music != null)
      menu_music.dispose();
    if (gameover_music != null)
      gameover_music.dispose();
    if (bossMusic1 != null)
      bossMusic1.dispose();
    if (bossMusic2 != null)
      bossMusic2.dispose();

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
