package com.space.game.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.managers.GameStateManager;
import com.space.game.managers.UIManager;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.space.game.managers.GameStateManager.State;
import com.space.game.managers.ScoreManager;

public class GlobalScoresState implements GameStateInterface {

    private UIManager uiManager;
    private GameStateManager gsm;
    private List<ScoreManager.ScoreEntry> scoresList;
    private int recentScore = -1;
    private com.space.game.managers.SoundManager soundManager;
    private ScoreManager scoreManager;
    private boolean isLoading;
    private String errorMessage;

    public GlobalScoresState(GameStateManager gsm, UIManager uiManager,
            com.space.game.managers.SoundManager soundManager) {
        this.uiManager = uiManager;
        this.gsm = gsm;
        this.soundManager = soundManager;
        this.scoreManager = new ScoreManager();
    }

    public void setRecentScore(int score) {
        this.recentScore = score;
    }

    @Override
    public void enter() {
        soundManager.ensureMenuMusicPlaying();
        isLoading = true;
        errorMessage = null;
        scoresList = null;

        scoreManager.loadGlobalScores(new ScoreManager.ScoreCallback() {
            @Override
            public void onScoresLoaded(List<ScoreManager.ScoreEntry> scores) {
                // Optimistic Update: Ensure recent score is reflected
                if (recentScore > 0 && com.space.game.SpaceGame.PLAYER_NAME != null) {
                    boolean found = false;
                    for (ScoreManager.ScoreEntry entry : scores) {
                        if (entry.playerName.equalsIgnoreCase(com.space.game.SpaceGame.PLAYER_NAME)) {
                            // Update only if recent is better (or just force it if we trust recent more?)
                            // Since we just saved it, recent should be the source of truth if higher.
                            if (recentScore > entry.score) {
                                entry.score = recentScore;
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        scores.add(new ScoreManager.ScoreEntry(com.space.game.SpaceGame.PLAYER_NAME, recentScore));
                    }

                    // Sort descending
                    java.util.Collections.sort(scores, new java.util.Comparator<ScoreManager.ScoreEntry>() {
                        @Override
                        public int compare(ScoreManager.ScoreEntry o1, ScoreManager.ScoreEntry o2) {
                            return Integer.compare(o2.score, o1.score);
                        }
                    });

                    // Keep top 10
                    if (scores.size() > 10) {
                        scores = scores.subList(0, 10);
                    }
                }

                scoresList = scores;
                isLoading = false;
            }

            @Override
            public void onError(String error) {
                errorMessage = error;
                isLoading = false;
            }
        });
    }

    private int currentSelection = 0;

    @Override
    public void update(SpriteBatch batch) {
        if (isLoading) {
            uiManager.displayLoading("Connecting...");
        } else if (errorMessage != null) {
            uiManager.displayError("Error: " + errorMessage);
        } else if (scoreManager.isError()) {
            uiManager.displayError("Error loading global scores, please contact the developer: eduardorr.ws@gmail.com");
        } else {
            if (scoresList != null) {
                String title = "GLOBAL HIGH SCORES";
                if (recentScore >= 0) {
                    // ... same logic as before ...
                    boolean inTop10 = false;
                    boolean isFirst = false;
                    for (int i = 0; i < scoresList.size(); i++) {
                        if (com.space.game.SpaceGame.PLAYER_NAME != null &&
                                scoresList.get(i).playerName.equalsIgnoreCase(com.space.game.SpaceGame.PLAYER_NAME)) {
                            if (scoresList.get(i).score == recentScore) {
                                inTop10 = true;
                                if (i == 0)
                                    isFirst = true;
                            }
                            break;
                        }
                    }

                    if (isFirst)
                        title = "CHAMPION! RANK #1";
                    else if (inTop10)
                        title = "TOP 10! NEW RECORD";
                    else
                        title = "GOOD JOB! TRY AGAIN";
                }

                uiManager.displayScores(scoresList, title, currentSelection);
            } else {
                uiManager.displayError("No scores found.");
            }
        }
        handleInput();
    }

    // ...

    @Override
    public State getState() {
        return State.GLOBAL_SCORES;
    }

    @Override
    public void exit() {
        // soundManager.stopScoresMusic();
    }

    private void handleInput() {
        // Only one option: Back (0)
        // Check arrows just to be consistent/responsive even if no other option
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W) ||
                Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S) ||
                Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A) ||
                Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            // Do nothing or toggle if we had more options
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gsm.setState(State.MENU);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) || Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            gsm.setState(State.MENU);
        }
    }

}
