package com.space.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Logger;

public class SpaceGame extends ApplicationAdapter {

    static private Game game;
    private static final Logger LOGGER = new Logger(SpaceGame.class.getName(), Logger.DEBUG);
    public static String PLAYER_NAME = null;
    public static String PLAYER_EMAIL = null;
    public static com.space.game.managers.AuthenticationHandler auth;
    public static com.space.game.managers.BackendHandler backend;
    public static ExitHandler exitHandler;
    public static SettingsHandler settingsHandler;

    public interface ExitHandler {
        void exitToLauncher();
    }

    public interface SettingsHandler {
        void saveSettings(float musicVolume, float soundVolume);

        float[] loadSettings(); // Return [music, sound] or null
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Logger.DEBUG);

        // Prevent browser from going back when Backspace is pressed, and catch Escape
        Gdx.input.setCatchKey(com.badlogic.gdx.Input.Keys.BACKSPACE, true);

        // Captura o cursor para fazer ele desaparecer
        Gdx.input.setCursorCatched(true);
        game = new Game();
    }

    @Override
    public void render() {
        game.render();
    }

    @Override
    public void resize(int width, int height) {
        game.resize(width, height);
    }

    @Override
    public void dispose() {
        game.dispose();

    }

    static public Game getGame() {
        return game;
    }

    static public Logger getLogger() {
        return LOGGER;
    }

}
