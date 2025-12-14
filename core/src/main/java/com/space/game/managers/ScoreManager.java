package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScoreManager {

    // Using Preferences for local storage which works great on GWT (LocalStorage)
    // and Desktop
    private static final String PREFS_NAME = "space_game_scores";
    private Preferences prefs;

    // Must be static and public for Json serialization to work easily in GWT
    public static class ScoreEntry {
        public String playerName;
        public int score;

        public ScoreEntry() {
        } // Required for Json serialization

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }
    }

    public ScoreManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public void saveGlobalScore(String playerName, int score) {
        // TODO: Implement API call to backend (Render/Firebase)
        Gdx.app.log("ScoreManager", "Global scores not yet implemented for Web/Backend. Saving locally as fallback.");
        saveLocalScore(playerName, score);
    }

    public List<ScoreEntry> loadGlobalScores() {
        // TODO: Implement API call to backend
        // Since this method is synchronous in the original code, we can't easily make a
        // network call here
        // without refactoring the whole game state machine to be async.
        // For now, we return local scores or a placeholder to keep the game running.
        Gdx.app.log("ScoreManager", "Global scores not yet implemented. Returning local scores.");
        return loadLocalScores();
    }

    public void saveLocalScore(String playerName, int score) {
        List<ScoreEntry> scores = loadLocalScores();
        scores.add(new ScoreEntry(playerName, score));

        // Sort and limit
        scores.sort(new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry o1, ScoreEntry o2) {
                return Integer.compare(o2.score, o1.score);
            }
        });

        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }

        // Serialize to simple JSON
        Json json = new Json();
        String scoresJson = json.toJson(scores);
        prefs.putString("local_scores", scoresJson);
        prefs.flush();
    }

    @SuppressWarnings("unchecked")
    public List<ScoreEntry> loadLocalScores() {
        String scoresJson = prefs.getString("local_scores", "[]");
        Json json = new Json();
        try {
            ArrayList<ScoreEntry> scores = json.fromJson(ArrayList.class, ScoreEntry.class, scoresJson);
            if (scores == null)
                return new ArrayList<>();
            return scores;
        } catch (Exception e) {
            Gdx.app.error("ScoreManager", "Error parsing local scores", e);
            return new ArrayList<>();
        }
    }

    public boolean isHighScore(int score) {
        List<ScoreEntry> scores = loadGlobalScores();
        if (scores.isEmpty())
            return true;
        return scores.size() < 10 || score > scores.get(scores.size() - 1).score;
    }

    public boolean isLocalHighScore(int score) {
        List<ScoreEntry> scores = loadLocalScores();
        if (scores.isEmpty())
            return true;
        return scores.size() < 10 || score > scores.get(scores.size() - 1).score;
    }

    public void close() {
        // Nothing to close
    }

    public boolean isError() {
        return false;
    }

    public boolean isDatabaseAvailable() {
        return false; // Global DB not available yet
    }
}
