package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ScoreManager {

    // Backend URL - Check if your Render URL or localhost is used
    private static String apiUrl = "https://space-game-web.onrender.com";
    private static boolean configLoaded = false;

    // ScoreEntry is kept for data holding, internal JSON usage is manual
    public static class ScoreEntry {
        public String playerName;
        public int score;

        public ScoreEntry() {
        }

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }
    }

    public interface ScoreCallback {
        void onScoresLoaded(List<ScoreEntry> scores);

        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();

        void onError(String error);
    }

    public ScoreManager() {
        if (!configLoaded) {
            loadConfig();
            configLoaded = true;
        }
    }

    private void loadConfig() {
        try {
            FileHandle file = Gdx.files.internal("config.json");
            if (file.exists()) {
                JsonValue root = new JsonReader().parse(file);
                if (root.has("api_url")) {
                    apiUrl = root.getString("api_url");
                    Gdx.app.log("ScoreManager", "Loaded API URL from config: " + apiUrl);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ScoreManager", "Error loading config.json, using default URL", e);
        }
    }

    public interface ExternalSaver {
        void save(String name, int score);
    }

    public static ExternalSaver saver;

    public void saveGlobalScore(String playerName, int score, final SaveCallback callback) {
        if (saver != null) {
            saver.save(playerName, score);
            if (callback != null)
                callback.onSuccess();
            return;
        }

        // Manual JSON construction to avoid GWT reflection issues with inner classes
        // Simple JSON escape for playerName just in case
        String safeName = playerName.replace("\"", "\\\"");
        String content = "{ \"playerName\": \"" + safeName + "\", \"score\": " + score + " }";

        Gdx.app.log("ScoreManager",
                "Attempting to save global score (Manual JSON): " + content + " to " + apiUrl + "/scores");

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(apiUrl + "/scores");
        request.setHeader("Content-Type", "application/json");
        request.setContent(content);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                Gdx.app.log("ScoreManager", "Global save response: " + statusCode);

                if (statusCode >= 200 && statusCode < 300) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onSuccess();
                        }
                    });
                } else {
                    final String errorMsg = "Error saving score: " + statusCode;
                    Gdx.app.error("ScoreManager", errorMsg);
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null)
                                callback.onError(errorMsg);
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                final String errorMsg = "Request failed: " + t.getMessage();
                Gdx.app.error("ScoreManager", errorMsg, t);
                // t.printStackTrace(); // Helpful for GWT dev mode console
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onError(errorMsg);
                    }
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.log("ScoreManager", "Request cancelled");
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null)
                            callback.onError("Cancelled");
                    }
                });
            }
        });
    }

    public interface ExternalLoader {
        void load(ScoreCallback callback);
    }

    public static ExternalLoader loader;

    public void loadGlobalScores(final ScoreCallback callback) {
        if (loader != null) {
            loader.load(callback);
            return;
        }

        Gdx.app.log("ScoreManager", "Loading global scores from: " + apiUrl);
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(apiUrl + "/scores");

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    final String responseString = httpResponse.getResultAsString();
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Manual parsing using JsonReader to avoid reflection issues
                                ArrayList<ScoreEntry> scores = new ArrayList<>();
                                com.badlogic.gdx.utils.JsonValue root = new com.badlogic.gdx.utils.JsonReader()
                                        .parse(responseString);

                                for (com.badlogic.gdx.utils.JsonValue val : root) {
                                    String name = val.getString("playerName", "Unknown");
                                    int s = val.getInt("score", 0);
                                    scores.add(new ScoreEntry(name, s));
                                }

                                callback.onScoresLoaded(scores);
                            } catch (Exception e) {
                                Gdx.app.error("ScoreManager", "Parse error", e);
                                callback.onError("Parse error: " + e.getMessage());
                            }
                        }
                    });
                } else {
                    final String errorMsg = "Http Error: " + statusCode;
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMsg);
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable t) {
                final String errorMsg = "Connection failed: " + t.getMessage();
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(errorMsg);
                    }
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError("Cancelled");
                    }
                });
            }
        });
    }

    public void saveLocalScore(String playerName, int score) {
        // Disabled
    }

    public List<ScoreEntry> loadLocalScores() {
        return new ArrayList<>();
    }

    public boolean isLocalHighScore(int score) {
        return false;
    }

    // Always assume it's a potential high score to trigger the name entry
    public boolean isHighScore(int score) {
        return true;
    }

    public void close() {
    }

    public boolean isError() {
        return false;
    }

    public boolean isDatabaseAvailable() {
        return true;
    }
}
