package com.space.game.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import java.util.ArrayList;
import java.util.List;

public class ScoreManager {

    // Backend URL - Check if your Render URL or localhost is used
    private static final String API_URL = "http://localhost:8000";

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
        // No local preferences anymore
    }

    public void saveGlobalScore(String playerName, int score, final SaveCallback callback) {
        // Manual JSON construction to avoid GWT reflection issues with inner classes
        // Simple JSON escape for playerName just in case
        String safeName = playerName.replace("\"", "\\\"");
        String content = "{ \"playerName\": \"" + safeName + "\", \"score\": " + score + " }";

        Gdx.app.log("ScoreManager",
                "Attempting to save global score (Manual JSON): " + content + " to " + API_URL + "/scores");

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
        request.setUrl(API_URL + "/scores");
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

    public void loadGlobalScores(final ScoreCallback callback) {
        Gdx.app.log("ScoreManager", "Loading global scores from: " + API_URL);
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(API_URL + "/scores");

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
