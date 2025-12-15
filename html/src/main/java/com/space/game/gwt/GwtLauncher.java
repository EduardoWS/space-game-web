package com.space.game.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.space.game.SpaceGame;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        // Resizable application, uses available space in browser with no padding:
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        cfg.padVertical = 0;
        cfg.padHorizontal = 0;
        cfg.stencil = true;
        return cfg;
        // If you want a fixed size application, comment out the above resizable
        // section,
        // and uncomment below:
        // return new GwtApplicationConfiguration(640, 480);
    }

    public native String getJsUsername() /*-{
        return $wnd.spaceGameUsername || null;
    }-*/;

    public native String getJsEmail() /*-{
        return $wnd.spaceGameEmail || null;
    }-*/;

    public native void saveScoreNative(String name, int score) /*-{
        if ($wnd.saveScoreJS) {
            $wnd.saveScoreJS(name, score);
        } else {
            console.log("saveScoreJS not found");
        }
    }-*/;

    @Override
    public ApplicationListener createApplicationListener() {
        String username = getJsUsername();
        if (username != null) {
            SpaceGame.PLAYER_NAME = username;
        }

        String email = getJsEmail();
        if (email != null) {
            SpaceGame.PLAYER_EMAIL = email;
        }

        com.space.game.managers.ScoreManager.saver = new com.space.game.managers.ScoreManager.ExternalSaver() {
            @Override
            public void save(String name, int score) {
                saveScoreNative(name, score);
            }
        };

        SpaceGame.auth = new com.space.game.managers.AuthenticationHandler() {
            @Override
            public void logout() {
                logoutNative();
            }
        };

        // Score Loader Implementation
        com.space.game.managers.ScoreManager.loader = new com.space.game.managers.ScoreManager.ExternalLoader() {
            @Override
            public void load(final com.space.game.managers.ScoreManager.ScoreCallback callback) {
                setOnScoresLoadedCallback(new RunnableCallback() {
                    @Override
                    public void run(String json) {
                        try {
                            java.util.ArrayList<com.space.game.managers.ScoreManager.ScoreEntry> scores = new java.util.ArrayList<>();
                            com.badlogic.gdx.utils.JsonValue root = new com.badlogic.gdx.utils.JsonReader().parse(json);

                            for (com.badlogic.gdx.utils.JsonValue val : root) {
                                // Map 'username' from JS to 'playerName' in Java
                                String name = val.getString("username", "Unknown");
                                int s = val.getInt("highScore", 0);
                                scores.add(new com.space.game.managers.ScoreManager.ScoreEntry(name, s));
                            }
                            callback.onScoresLoaded(scores);
                        } catch (Exception e) {
                            callback.onError("Parse Error: " + e.getMessage());
                        }
                    }
                });
                getGlobalScoresNative();
            }
        };

        // Backend Handler Implementation
        SpaceGame.backend = new com.space.game.managers.BackendHandler() {
            @Override
            public void checkBackend(final com.space.game.managers.BackendHandler.Callback callback) {
                setOnBackendReadyCallback(new RunnableCallback() {
                    @Override
                    public void run(String msg) {
                        callback.onReady();
                    }
                });
                checkBackendNative();
            }
        };

        SpaceGame.exitHandler = new SpaceGame.ExitHandler() {
            @Override
            public void exitToLauncher() {
                exitToLauncherNative();
            }
        };

        SpaceGame.settingsHandler = new SpaceGame.SettingsHandler() {
            @Override
            public void saveSettings(float musicVolume, float soundVolume) {
                saveSettingsNative(musicVolume, soundVolume);
            }

            @Override
            public float[] loadSettings() {
                String json = getJsSettings();
                if (json != null) {
                    try {
                        com.badlogic.gdx.utils.JsonValue root = new com.badlogic.gdx.utils.JsonReader().parse(json);
                        float music = root.getFloat("music", 0.4f);
                        float sound = root.getFloat("sound", 0.4f);
                        return new float[] { music, sound };
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        };

        return new SpaceGame();
    }

    // --- Interfaces & Callbacks ---
    interface RunnableCallback {
        void run(String data);
    }

    private static RunnableCallback onScoresLoadedCallback;
    private static RunnableCallback onBackendReadyCallback;

    // JS -> Java Bridges
    private void setOnScoresLoadedCallback(RunnableCallback callback) {
        onScoresLoadedCallback = callback;
    }

    private void setOnBackendReadyCallback(RunnableCallback callback) {
        onBackendReadyCallback = callback;
    }

    public static void onScoresLoaded(String json) {
        if (onScoresLoadedCallback != null)
            onScoresLoadedCallback.run(json);
    }

    public static void onBackendReady() {
        if (onBackendReadyCallback != null)
            onBackendReadyCallback.run("ready");
    }

    // --- JSNI Methods ---

    public native void logoutNative() /*-{
        if ($wnd.logoutJS) $wnd.logoutJS();
    }-*/;

    public native void exitToLauncherNative() /*-{
        if ($wnd.exitToLauncherJS) $wnd.exitToLauncherJS();
    }-*/;

    public native void saveSettingsNative(float music, float sound) /*-{
        if ($wnd.saveSettingsJS) $wnd.saveSettingsJS(music, sound);
    }-*/;

    public native String getJsSettings() /*-{
        return $wnd.getSettingsJS ? $wnd.getSettingsJS() : null;
    }-*/;

    public native void getGlobalScoresNative() /*-{
        if ($wnd.getGlobalScoresJS) {
            $wnd.getGlobalScoresJS(function(jsonString) {
                @com.space.game.gwt.GwtLauncher::onScoresLoaded(Ljava/lang/String;)(jsonString);
            });
        }
    }-*/;

    public native void checkBackendNative() /*-{
        if ($wnd.checkBackendJS) {
            $wnd.checkBackendJS(function() {
                @com.space.game.gwt.GwtLauncher::onBackendReady()();
            });
        } else {
             // If JS function missing, assume ready to avoid locking
             @com.space.game.gwt.GwtLauncher::onBackendReady()();
        }
    }-*/;
}
