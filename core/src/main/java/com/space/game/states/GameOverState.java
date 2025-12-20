package com.space.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.MapManager;
import com.space.game.managers.UIManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.TimeUtils;
import com.space.game.managers.ScoreManager;
import com.badlogic.gdx.Input.Keys;
import java.util.HashMap;
import java.util.Map;

public class GameOverState implements GameStateInterface {

    private UIManager uiManager;
    private MapManager mapManager;
    private com.space.game.managers.MusicManager musicManager;
    private GameStateManager gsm;
    private String playerName; // Para armazenar o nome do jogador
    private boolean enterName = false;
    private long lastBlinkTime; // Variável para controlar o tempo de piscar
    private boolean showCursor = true; // Variável para alternar a exibição do cursor
    private ScoreManager scoreManager;
    Map<Integer, String> keyToCharMap;
    private int whatHighScore;

    private float gameoverTimer = 0;
    private final float TIME_TO_GAMEOVER = 7; // Tempo em segundos antes da próxima onda

    public GameOverState(GameStateManager gsm, MapManager mapManager, UIManager uiManager,
            com.space.game.managers.MusicManager musicManager) {
        this.uiManager = uiManager;
        this.mapManager = mapManager;
        this.musicManager = musicManager;
        this.gsm = gsm;
        this.scoreManager = new ScoreManager();

        // Mapear teclas para caracteres correspondentes
        keyToCharMap = new HashMap<>();

        keyToCharMap.put(Keys.A, "A");
        keyToCharMap.put(Keys.B, "B");
        keyToCharMap.put(Keys.C, "C");
        keyToCharMap.put(Keys.D, "D");
        keyToCharMap.put(Keys.E, "E");
        keyToCharMap.put(Keys.F, "F");
        keyToCharMap.put(Keys.G, "G");
        keyToCharMap.put(Keys.H, "H");
        keyToCharMap.put(Keys.I, "I");
        keyToCharMap.put(Keys.J, "J");
        keyToCharMap.put(Keys.K, "K");
        keyToCharMap.put(Keys.L, "L");
        keyToCharMap.put(Keys.M, "M");
        keyToCharMap.put(Keys.N, "N");
        keyToCharMap.put(Keys.O, "O");
        keyToCharMap.put(Keys.P, "P");
        keyToCharMap.put(Keys.Q, "Q");
        keyToCharMap.put(Keys.R, "R");
        keyToCharMap.put(Keys.S, "S");
        keyToCharMap.put(Keys.T, "T");
        keyToCharMap.put(Keys.U, "U");
        keyToCharMap.put(Keys.V, "V");
        keyToCharMap.put(Keys.W, "W");
        keyToCharMap.put(Keys.X, "X");
        keyToCharMap.put(Keys.Y, "Y");
        keyToCharMap.put(Keys.Z, "Z");
    }

    @Override
    public void enter() {
        enterName = false;
        playerName = "";
        lastBlinkTime = TimeUtils.millis(); // Inicializa o tempo de piscar
        musicManager.stopMusic();
        musicManager.playGameOverMusic();
        whatHighScore = 0;
        gameoverTimer = 0;
    }

    @Override
    public void update(SpriteBatch batch) {
        if (enterName) {
            updateInputUI();
        } else {
            // uiManager.displayGameOverInfo is moved to renderUI
            gameoverTimer += Gdx.graphics.getDeltaTime();
            if (gameoverTimer >= TIME_TO_GAMEOVER) {
                gameoverTimer = TIME_TO_GAMEOVER;
            }
            handleInput();
        }
    }

    @Override
    public void renderUI(SpriteBatch batch) {
        if (enterName) {
            drawInputUI();
        } else {
            uiManager.displayGameOverInfo(mapManager.getSpaceship(), gameoverTimer, TIME_TO_GAMEOVER);
        }
    }

    @Override
    public State getState() {
        return State.GAME_OVER;
    }

    @Override
    public void exit() {
        mapManager.reset(); // Use reset to valid re-entry
        // mapManager.dispose(); // Do not dispose factory here if we want to play
        // again!
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // verificar se ja tem 10 scores salvos e se o score atual é menor que o ultimo
            // salvo
            // whatHighScore = 0 -> não é highscore
            // whatHighScore = 1 -> é highscore local
            // whatHighScore = 2 -> é highscore global
            // whatHighScore = 3 -> é highscore local e global

            boolean isGlobalHigh = scoreManager.isDatabaseAvailable() &&
                    scoreManager.isHighScore(mapManager.getSpaceship().getKillCount());

            Gdx.app.log("GameOverState", "Checking Score: " + mapManager.getSpaceship().getKillCount() +
                    " GlobalHigh: " + isGlobalHigh);

            if (isGlobalHigh) {
                whatHighScore = 2; // Treat as global high score

                // Check if we have a logged in user
                if (com.space.game.SpaceGame.PLAYER_NAME != null) {
                    saveScore(com.space.game.SpaceGame.PLAYER_NAME, mapManager.getSpaceship().getKillCount());
                    // Transition is handled in saveScore callback
                } else {
                    setupUI();
                }
            } else {
                whatHighScore = 0;
                // Transition with recent score
                com.space.game.states.GlobalScoresState scoresState = (com.space.game.states.GlobalScoresState) gsm
                        .getStateInstance(State.GLOBAL_SCORES);
                scoresState.setRecentScore(mapManager.getSpaceship().getKillCount());
                musicManager.stopGameOverMusic();
                gsm.setState(State.GLOBAL_SCORES);
            }

        }
    }

    private void updateInputUI() {
        // Alterna a exibição do cursor
        if (TimeUtils.timeSinceMillis(lastBlinkTime) > 500) {
            showCursor = !showCursor;
            lastBlinkTime = TimeUtils.millis();
        }

        // Input Logic Only
        // Verificar teclas pressionadas e atualizar o nome do jogador
        for (Map.Entry<Integer, String> entry : keyToCharMap.entrySet()) {
            if (Gdx.input.isKeyJustPressed(entry.getKey()) && playerName.length() < 10) {
                playerName += entry.getValue();
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.BACKSPACE) && playerName.length() > 0) {
            playerName = playerName.substring(0, playerName.length() - 1);
        }

        if (Gdx.input.isKeyJustPressed(Keys.ENTER) && enterName) {
            if (playerName.isEmpty()) {
                System.out.println("Player name is empty, setting to UNKNOWN");
                playerName = "UNKNOWN";
            }
            try {
                if (mapManager.getSpaceship() != null) {
                    saveScore(playerName, mapManager.getSpaceship().getKillCount());
                } else {
                    Gdx.app.log("GameOverState", "Spaceship is null, cannot save score properly. Saving 0.");
                    saveScore(playerName, 0);
                }
                System.out.println("Score saved successfully");
            } catch (Exception e) {
                Gdx.app.error("GameOverState", "Error saving score", e);
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            musicManager.stopGameOverMusic();
            gsm.setState(State.MENU);
        }

        if (!enterName) {
            enterName = true;
        }
    }

    private void drawInputUI() {
        uiManager.displaySaveScore(mapManager.getSpaceship(), playerName, showCursor);
    }

    private void setupUI() { // Legacy method removed or kept if called elsewhere - but here replaced.
        // Replaced by updateInputUI and drawInputUI
    }

    private void saveScore(String playerName, int score) {
        ScoreManager.SaveCallback callback = new ScoreManager.SaveCallback() {
            @Override
            public void onSuccess() {
                Gdx.app.log("GameOverState", "Score saved successfully to backend.");
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        com.space.game.states.GlobalScoresState scoresState = (com.space.game.states.GlobalScoresState) gsm
                                .getStateInstance(State.GLOBAL_SCORES);
                        scoresState.setRecentScore(score);
                        musicManager.stopGameOverMusic();
                        gsm.setState(State.GLOBAL_SCORES);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Gdx.app.error("GameOverState", "Failed to save score to backend: " + error);
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // Even if error, transition? Or stay?
                        // Let's transition but maybe user sees error in console.
                        com.space.game.states.GlobalScoresState scoresState = (com.space.game.states.GlobalScoresState) gsm
                                .getStateInstance(State.GLOBAL_SCORES);
                        scoresState.setRecentScore(score); // Pass it anyway
                        musicManager.stopGameOverMusic();
                        gsm.setState(State.GLOBAL_SCORES);
                    }
                });
            }
        };

        if (whatHighScore == 1) {
            scoreManager.saveLocalScore(playerName, score);
            // Local save doesn't have callback in this older API?
            // Assuming it's synchronous for now or we just move on.
            // Wait, the original code didn't wait for local save.
            // But if we want to show it in global scores we need to wait if it was global.
            // For consistency let's just transition for local too.
            musicManager.stopGameOverMusic();
            gsm.setState(State.GLOBAL_SCORES);

        } else if (whatHighScore == 2 || whatHighScore == 3) {
            // saveGlobalScore also saves locally as backup
            scoreManager.saveGlobalScore(playerName, score, callback);
        } else {
            System.out.println("Score not saved (not a high score)");
            // Pass the score even if not saved, so we can say "GOOD JOB"
            com.space.game.states.GlobalScoresState scoresState = (com.space.game.states.GlobalScoresState) gsm
                    .getStateInstance(State.GLOBAL_SCORES);
            scoresState.setRecentScore(score);
            musicManager.stopGameOverMusic();
            gsm.setState(State.GLOBAL_SCORES);
        }
    }
}
