package com.space.game.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
// import com.space.game.config.ConfigUtils;
import com.space.game.entities.Spaceship;
import com.badlogic.gdx.Gdx;
import java.util.List;
import com.space.game.Game;

public class UIManager {
    private BitmapFont font30, font100, font150;
    private Game game;
    private SpriteBatch batch;
    private int hordas;
    private final int const_larg = 21;
    private Color cian_color;
    private Color red_color;
    private com.space.game.managers.FeedbackQueue scoreQueue;
    private com.space.game.managers.FeedbackQueue energyQueue;

    public UIManager(Game game, SpriteBatch batch) {
        this.game = game;
        this.batch = batch;
        this.cian_color = new Color(0.0f, 1.0f, 1.0f, 1.0f);
        this.red_color = new Color(1.0f, 0.0f, 0.0f, 1.0f);

        initializeFonts();
        this.scoreQueue = new com.space.game.managers.FeedbackQueue();
        this.energyQueue = new com.space.game.managers.FeedbackQueue();
    }

    private void initializeFonts() {
        // Load pre-generated high quality fonts
        // Using Nasalization for smaller text (size 30) for better readability
        font30 = new BitmapFont(Gdx.files.internal("fonts/nasalization-30.fnt"));
        // Using Space Age for titles (sizes 100/150) for style
        font100 = new BitmapFont(Gdx.files.internal("fonts/space-age-100.fnt"));
        font150 = new BitmapFont(Gdx.files.internal("fonts/space-age-150.fnt"));
    }

    // Create a new scale factor method
    private float getScaleFactor() {
        // Base resolution 1920x1080
        float widthScale = game.getWorldWidth() / 1920f;
        float heightScale = game.getWorldHeight() / 1080f;
        // Use the smaller scale to ensure it fits
        return Math.min(widthScale, heightScale);
    }

    public void displayMenu(boolean isDatabaseAvailable, int currentSelection, float stateTimer) {
        float titleAnimDuration = 1.0f;
        float menuAnimDelay = 0.5f;
        float menuAnimDuration = 1.0f;

        // Title Animation
        float titleAlpha = Math.min(1.0f, stateTimer / titleAnimDuration);

        // Desenha o título "SPACE GAME"
        // Desenha o título "SPACE GAME"
        String title = "SPACE GAME";

        // Apply scaling
        float scale = getScaleFactor();
        font150.getData().setScale(scale);

        GlyphLayout titleLayout = new GlyphLayout(font150, title);
        float title_x = game.getWorldWidth() / const_larg;
        // Slide down effect for title
        float targetTitleY = game.getWorldHeight() / 1.5f + titleLayout.height;
        float startTitleY = targetTitleY + (100f * scale); // Start 100 pixels higher
        float title_y = startTitleY + (targetTitleY - startTitleY) * titleAlpha;

        font150.setColor(0, 1, 1, titleAlpha); // Cyan with alpha
        font150.draw(batch, title, title_x, title_y);
        font150.setColor(cian_color); // Reset to solid for safety

        // Menu Options Animation
        float menuTimer = Math.max(0, stateTimer - menuAnimDelay);
        float menuAlpha = Math.min(1.0f, menuTimer / menuAnimDuration);

        if (menuTimer > 0) {
            // Opções do menu
            String startText = "Start Arcade Mode";
            String scoresText = "Global Scores";

            // Coordenadas iniciais
            float targetY = targetTitleY - titleLayout.height * 3;
            float startY = targetY - (50f * scale); // Start 50 pixels lower
            float currentY = startY + (targetY - startY) * menuAlpha;

            // Start Option
            font30.getData().setScale(scale); // Apply scale to small font

            GlyphLayout startLayout = new GlyphLayout(font30, startText);
            float startX = game.getWorldWidth() / const_larg;
            float cursorOffset = 40f * scale; // Distance between cursor and text

            Color selectedColor = cian_color;
            Color unselectedColor = Color.WHITE;

            // Apply alpha to colors
            Color currentColorUnselected = new Color(unselectedColor.r, unselectedColor.g, unselectedColor.b,
                    menuAlpha);
            Color currentColorSelected = new Color(selectedColor.r, selectedColor.g, selectedColor.b, menuAlpha);

            if (currentSelection == 0) {
                font30.setColor(currentColorSelected);
                font30.draw(batch, ">", startX - cursorOffset, currentY);
                font30.draw(batch, startText, startX, currentY);
            } else {
                font30.setColor(currentColorUnselected);
                font30.draw(batch, startText, startX, currentY);
            }

            // Global Scores Option
            if (isDatabaseAvailable) {
                currentY = currentY - startLayout.height * 3;
                if (currentSelection == 1) {
                    font30.setColor(currentColorSelected);
                    font30.draw(batch, ">", startX - cursorOffset, currentY);
                    font30.draw(batch, scoresText, startX, currentY);
                } else {
                    font30.setColor(currentColorUnselected);
                    font30.draw(batch, scoresText, startX, currentY);
                }

                // Settings Option
                String settingsText = "Settings";
                currentY = currentY - startLayout.height * 3;
                if (currentSelection == 2) {
                    font30.setColor(currentColorSelected);
                    font30.draw(batch, ">", startX - cursorOffset, currentY);
                    font30.draw(batch, settingsText, startX, currentY);
                } else {
                    font30.setColor(currentColorUnselected);
                    font30.draw(batch, settingsText, startX, currentY);
                }

                // Exit Option
                String exitText = "Exit";
                float exitY = currentY - startLayout.height * 9; // Extra spacing for exit

                if (currentSelection == 3) {
                    font30.setColor(currentColorSelected);
                    font30.draw(batch, ">", startX - cursorOffset, exitY);
                    font30.draw(batch, exitText, startX, exitY);
                } else {
                    font30.setColor(currentColorUnselected);
                    font30.draw(batch, exitText, startX, exitY);
                }
            }
            font30.setColor(Color.WHITE); // Reset

            // Version Display
            String versionText = com.space.game.config.GameConfig.GAME_VERSION;
            GlyphLayout versionLayout = new GlyphLayout(font30, versionText);
            float versionX = game.getWorldWidth() - versionLayout.width - (20 * scale);
            float versionY = 30 * scale; // Close to bottom
            font30.draw(batch, versionText, versionX, versionY);
        }

        // Reset color logic if needed, though we set it before drawing each time.
    }

    public void addScoreFeedback(int score) {
        scoreQueue.addMessage("+" + score, Color.YELLOW);
    }

    public void addEnergyFeedback(float energy) {
        energyQueue.addMessage("+" + (int) energy + "%", Color.CYAN);
    }

    public void update(float dt) {
        scoreQueue.update(dt);
        energyQueue.update(dt);
    }

    public void resetFeedback() {
        scoreQueue.clear();
        energyQueue.clear();
    }

    public void displayGameControls(int selectedOption) {
        // Removed dynamic scaling to ensure consistent layout across zoom levels
        // float scaleFactor = ConfigUtils.calcularFatorDeEscala();
        float scale = getScaleFactor();

        String title = "GAME CONTROLS";

        font100.getData().setScale(scale);

        GlyphLayout titleLayout = new GlyphLayout(font100, title);
        float title_x = game.getWorldWidth() / const_larg;
        float title_y = game.getWorldHeight() / 1.2f + titleLayout.height;
        font100.setColor(cian_color);
        font100.draw(batch, title, title_x, title_y);

        font30.setColor(cian_color);
        font30.getData().setScale(scale);

        float startY = game.getWorldHeight() / 2 + 3 * (30 * scale); // 3 é o número de controles

        // Desenhar cabeçalhos da tabela
        String actionHeader = "Action";
        String controlHeader = "Control";

        GlyphLayout controlLayout = new GlyphLayout(font30, controlHeader);
        GlyphLayout actionLayout = new GlyphLayout(font30, actionHeader);

        float actionX = game.getWorldWidth() / const_larg; // Espaçamento entre colunas
        float controlX = actionX + actionLayout.width + (100 * scale);

        float headerY = startY + (60 * scale); // Cabeçalhos um pouco acima da lista de controles
        font30.draw(batch, controlHeader, controlX + controlLayout.width / 2, headerY);
        font30.draw(batch, actionHeader, actionX, headerY);

        // Controles do jogo
        String[] actions = { "Turn Left", "Turn Right", "Shoot", "Pause Game", "Prev. Song", "Pause Song",
                "Next Song" };
        String[] controls = { "A | Left Arrow", "D | Right Arrow", "Spacebar", "P", "Q", "W", "E" };

        // Desenhar controles
        float y = startY;

        for (int i = 0; i < controls.length; i++) {
            String control = controls[i];
            String action = actions[i];

            font30.draw(batch, control, controlX + controlLayout.width / 2, y);
            font30.draw(batch, action, actionX, y);

            y -= 50 * scale;
        }

        // Desenha as instruções de iniciar e voltar na parte inferior da tela
        String startText = "Start";
        GlyphLayout startLayout = new GlyphLayout(font30, startText);
        float start_x = (const_larg - 1) * (game.getWorldWidth() / const_larg) - startLayout.width;
        float start_y = game.getWorldHeight() * 0.1f; // Posição inferior

        if (selectedOption == 0) {
            font30.setColor(cian_color);
            font30.draw(batch, "> ", start_x - (40 * scale), start_y);
            font30.draw(batch, startText, start_x, start_y);
        } else {
            font30.setColor(Color.WHITE);
            font30.draw(batch, startText, start_x, start_y);
        }

        String backText = "Back";
        float back_x = game.getWorldWidth() / const_larg;
        float back_y = start_y;

        if (selectedOption == 1) {
            font30.setColor(cian_color);
            font30.draw(batch, "> ", back_x - (40 * scale), back_y);
            font30.draw(batch, backText, back_x, back_y);
        } else {
            font30.setColor(Color.WHITE);
            font30.draw(batch, backText, back_x, back_y);
        }
    }

    private String formatEnergy(float energy) {
        // Manual formatting for GWT compatibility
        int val = (int) (energy * 100);
        int intPart = val / 100;
        int decPart = val % 100;
        String decStr = decPart < 10 ? "0" + decPart : "" + decPart;
        return "ENERGY: " + intPart + "." + decStr + "%";
    }

    // Overloaded for backward compatibility call in other methods
    private void drawHud(Spaceship spaceship) {
        drawHud(spaceship, 0);
    }

    private void drawHud(Spaceship spaceship, float xOffset) {
        // Update Feedback Queues
        update(Gdx.graphics.getDeltaTime());

        font30.setColor(cian_color);

        float scale = getScaleFactor();
        font30.getData().setScale(scale);

        // Energy (Bottom Left) -> Shifted by xOffset
        if (spaceship.getEnergy() <= 10.0f) {
            font30.setColor(Color.RED);
        } else {
            font30.setColor(cian_color);
        }
        String energyText = formatEnergy(spaceship.getEnergy());
        GlyphLayout energyLayout = new GlyphLayout(font30, energyText);
        float energy_x = xOffset + (game.getWorldWidth() / const_larg);
        float energy_y = energyLayout.height / 2 + energyLayout.height;
        font30.draw(batch, energyText, energy_x, energy_y);

        // Render Energy Feedback (Stack Upwards above Energy HUD)
        font30.getData().setScale(scale * 0.85f); // Smaller font for feedback
        energyQueue.render(batch, font30, energy_x, energy_y + (50 * scale), true);
        font30.getData().setScale(scale); // Restore scale
        font30.setColor(cian_color); // Restore color

        // Wave (Bottom Right)
        String hordasText = "WAVE: " + hordas;
        GlyphLayout hordasLayout = new GlyphLayout(font30, hordasText);
        float hordas_x = (const_larg - 1) * (game.getWorldWidth() / const_larg) - hordasLayout.width;
        float hordas_y = hordasLayout.height / 2 + hordasLayout.height;
        font30.draw(batch, hordasText, hordas_x, hordas_y);

        // Score (Top Left) -> Shifted by xOffset
        String killsText = "SCORE: " + (spaceship.getKillCount());
        GlyphLayout killsLayout = new GlyphLayout(font30, killsText);
        float kills_x = xOffset + (game.getWorldWidth() / const_larg);
        float kills_y = game.getWorldHeight() - killsLayout.height;
        font30.draw(batch, killsText, kills_x, kills_y);

        // Render Score Feedback (Stack Downwards below Score HUD)
        font30.getData().setScale(scale * 0.85f); // Smaller font for feedback
        scoreQueue.render(batch, font30, kills_x, kills_y - (50 * scale), false);
        font30.getData().setScale(scale); // Restore scale
        font30.setColor(cian_color); // Restore color for Streak

        // Streak (Top Right)
        String streakText = "STREAK: x" + spaceship.getStreakCount();
        GlyphLayout streakLayout = new GlyphLayout(font30, streakText);
        float streak_x = (const_larg - 1) * (game.getWorldWidth() / const_larg) - streakLayout.width;
        float streak_y = game.getWorldHeight() - streakLayout.height;
        font30.draw(batch, streakText, streak_x, streak_y);
    }

    public void displayGameInfo(Spaceship spaceship) {
        drawHud(spaceship);
    }

    public void displayError(String error) {
        float scale = getScaleFactor();
        font30.getData().setScale(scale);

        GlyphLayout errorLayout = new GlyphLayout(font30, error);
        float error_x = game.getWorldWidth() / 2 - errorLayout.width / 2;
        float error_y = game.getWorldHeight() / 2 + errorLayout.height;
        font30.draw(batch, error, error_x, error_y);

        String backText = "Esc. Back";
        GlyphLayout backLayout = new GlyphLayout(font30, backText);
        // float back_x = game.getWorldWidth() / 2 - game.getWorldWidth() / 4 -
        // backLayout.width / 2;
        float back_x = game.getWorldWidth() / 2 - backLayout.width / 2;
        float back_y = game.getWorldHeight() * 0.1f;
        font30.draw(batch, backText, back_x, back_y);
    }

    public void displayGameOverInfo(Spaceship spaceship, float gameoverTimer, float TIME_TO_GAMEOVER) {
        // Calcular a porcentagem do tempo decorrido
        float progress = gameoverTimer / TIME_TO_GAMEOVER;
        float alpha;

        alpha = progress;

        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        String gameOverText = "GAME OVER";
        GlyphLayout gameOverLayout = new GlyphLayout(font100, gameOverText);
        float gameOver_x = game.getWorldWidth() / 2 - gameOverLayout.width / 2;
        float gameOver_y = game.getWorldHeight() / 2 + gameOverLayout.height;
        font100.setColor(0, 1, 1, alpha);
        font100.setColor(red_color);
        font100.draw(batch, gameOverText, gameOver_x, gameOver_y);
        font100.setColor(0, 1, 1, 1); // Restaurar a cor padrão

        String restartText = "Press Enter to Continue";
        GlyphLayout restartLayout = new GlyphLayout(font30, restartText);
        font30.setColor(0, 1, 1, alpha);
        font30.setColor(red_color);
        font30.draw(batch, restartText, game.getWorldWidth() / 2 - restartLayout.width / 2,
                gameOver_y - gameOverLayout.height * 2);
        font30.setColor(0, 1, 1, 1); // Restaurar a cor padrão

    }

    public void displayPausedMenu(Spaceship spaceship, int currentSelection, boolean inSettingsMenu, float musicVolume,
            float soundVolume) {
        // 1. Draw Semi-Transparent Full Screen Background
        batch.end();
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f); // High opacity black overlay
        shapeRenderer.rect(0, 0, game.getWorldWidth(), game.getWorldHeight());
        shapeRenderer.end();
        shapeRenderer.dispose();
        batch.begin();

        // 2. Draw Content
        // Removed dynamic scaling
        // float scaleFactor = ConfigUtils.calcularFatorDeEscala();
        float scale = getScaleFactor();

        String title = inSettingsMenu ? "SETTINGS" : "PAUSED";

        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        // Title
        GlyphLayout titleLayout = new GlyphLayout(font100, title);
        float titleX = (game.getWorldWidth() - titleLayout.width) / 2;
        float titleY = game.getWorldHeight() * 0.75f;
        font100.setColor(cian_color);
        font100.draw(batch, title, titleX, titleY);

        float spacing = 80f * scale; // Fixed spacing

        float startY = titleY - 1.5f * spacing; // Start below title

        if (inSettingsMenu) {
            // Manual Volume Render Centered
            String[] labels = { "Music Volume", "Sound Volume" };
            float[] values = { musicVolume, soundVolume };

            // Fixed Centered Columns Logic
            float centerX = game.getWorldWidth() / 2;
            // Define fixed column starts relative to center
            float labelX = centerX - (165 * scale);
            float valueX = centerX + (125 * scale);

            for (int i = 0; i < 2; i++) {
                int pct = (int) (values[i] * 100);

                if (i == currentSelection) {
                    font30.setColor(cian_color);
                    // Arrow is fixed relative to label column start
                    font30.draw(batch, ">", labelX - (40 * scale), startY);

                    font30.draw(batch, labels[i], labelX, startY);
                    font30.draw(batch, "< " + pct + "% >", valueX, startY);
                } else {
                    font30.setColor(Color.WHITE);
                    font30.draw(batch, labels[i], labelX, startY);
                    font30.draw(batch, pct + "%", valueX, startY);
                }

                startY -= spacing;
            }

            // Back Button
            String backText = "Back";
            GlyphLayout backLayout = new GlyphLayout(font30, backText);
            float backX = (game.getWorldWidth() - backLayout.width) / 2;

            if (currentSelection == 2) {
                font30.setColor(cian_color);
                font30.draw(batch, "> " + backText + " <",
                        (game.getWorldWidth() - new GlyphLayout(font30, "> " + backText + " <").width) / 2, startY);
            } else {
                font30.setColor(Color.WHITE);
                font30.draw(batch, backText, backX, startY);
            }

        } else {
            String[] options = { "Resume", "Restart", "Settings", "Exit" };
            for (int i = 0; i < options.length; i++) {
                GlyphLayout layout = new GlyphLayout(font30, options[i]);
                float x = (game.getWorldWidth() - layout.width) / 2;

                if (i == currentSelection) {
                    font30.setColor(cian_color);
                    // Add decorative markers for selection
                    String selText = "- " + options[i] + " -";
                    layout.setText(font30, selText);
                    float selX = (game.getWorldWidth() - layout.width) / 2;
                    font30.draw(batch, selText, selX, startY);
                } else {
                    font30.setColor(Color.WHITE);
                    font30.draw(batch, options[i], x, startY);
                }
                startY -= spacing;
            }
        }

        // 3. Draw HUD overlay (no offset)
        drawHud(spaceship);
    }

    public void displayNewLevel(float waveTimer, float TIME_TO_WAVE) {
        // Calcular a porcentagem do tempo decorrido
        float progress = waveTimer / TIME_TO_WAVE;
        float alpha;

        // Se a progressão está na primeira metade
        if (progress <= 0.5f) {
            // Interpolação linear de 0 a 1
            alpha = progress * 2;
        } else {
            // Interpolação linear de 1 a 0
            alpha = 1 - ((progress - 0.5f) * 2);
        }

        // Definir a posição do texto
        float scale = getScaleFactor();
        font100.getData().setScale(scale);

        String newLevelText = "WAVE " + hordas;
        GlyphLayout newLevelLayout = new GlyphLayout(font100, newLevelText);
        float newLevel_x = game.getWorldWidth() / 2 - newLevelLayout.width / 2;
        float newLevel_y = game.getWorldHeight() / 1.1f + newLevelLayout.height;

        // Desenhar o texto com a opacidade atualizada
        font100.setColor(1, 1, 1, alpha);
        font100.draw(batch, newLevelText, newLevel_x, newLevel_y);

        if (hordas > 1) {
            String bonusText = "+20% ENERGY";
            font30.getData().setScale(scale * 0.8f);
            GlyphLayout bonusLayout = new GlyphLayout(font30, bonusText);
            float bonusX = game.getWorldWidth() / 2 - bonusLayout.width / 2;
            float bonusY = newLevel_y - newLevelLayout.height - (20 * scale);
            font30.setColor(0, 1, 0, alpha); // Green color
            font30.draw(batch, bonusText, bonusX, bonusY);
            font30.getData().setScale(scale); // Restore scale
        }

        font100.setColor(1, 1, 1, 1); // Restaurar a cor padrão

    }

    public void displayDarkLevelWarning(float waveTimer, float TIME_TO_WAVE) {
        // Warning appears in the second half of the transition
        // Modified to be shown based on calling logic, assuming waveTimer passed is
        // relevant time window

        float alpha = (float) Math.abs(Math.sin(waveTimer * 5)); // Slower blink

        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        GlyphLayout layout = new GlyphLayout(font100, "WARNING");
        float x = game.getWorldWidth() / 2 - layout.width / 2;
        float y = game.getWorldHeight() / 1.1f + layout.height; // Same height as WAVE message

        // Red color for warning
        font100.setColor(1, 0, 0, alpha);
        font100.draw(batch, "WARNING", x, y);

        String subText = "DARK ZONE APPROACHING - SENSORS FAILURE";
        GlyphLayout subLayout = new GlyphLayout(font30, subText);
        float subX = game.getWorldWidth() / 2 - subLayout.width / 2;
        float subY = y - layout.height - (20 * scale);

        font30.setColor(1, 0, 0, alpha);
        font30.draw(batch, subText, subX, subY);

        font100.setColor(Color.WHITE);
        font30.setColor(Color.WHITE);
    }

    public void displaySaveScore(Spaceship spaceship, String playerName, boolean showCursor) {
        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        String highscore = "HIGH SCORE: " + (spaceship.getKillCount());
        GlyphLayout highscoreLayout = new GlyphLayout(font100, highscore);
        float highscore_x = game.getWorldWidth() / 2 - highscoreLayout.width / 2;
        float highscore_y = game.getWorldHeight() / 1.3f + highscoreLayout.height;
        font100.setColor(cian_color);
        font100.draw(batch, highscore, highscore_x, highscore_y);

        // String scoreText = "Score: " + (spaceship.getKillCount());
        // GlyphLayout scoreLayout = new GlyphLayout(font100, scoreText);
        // font100.draw(batch, scoreText, game.getWorldWidth() / 2 - scoreLayout.width /
        // 2, highscore_y - highscoreLayout.height * 2);

        font30.setColor(cian_color);
        String playerText = "Player: " + playerName + (showCursor ? "_" : "  ");
        GlyphLayout playerLayout = new GlyphLayout(font30, playerText);
        float player_x = game.getWorldWidth() / 2 - playerLayout.width / 2;
        float player_y = game.getWorldHeight() / 2;
        font30.draw(batch, playerText, player_x, player_y);

        String continueText = "Enter. Save";
        GlyphLayout continueLayout = new GlyphLayout(font30, continueText);
        float continue_x = (const_larg - 1) * (game.getWorldWidth() / const_larg) - continueLayout.width;
        float continue_y = continueLayout.height / 2 + continueLayout.height;
        font30.draw(batch, continueText, continue_x, continue_y);

        String cancelText = "Esc. Cancel";
        GlyphLayout cancelLayout = new GlyphLayout(font30, cancelText);
        float cancel_x = game.getWorldWidth() / const_larg;
        float cancel_y = cancelLayout.height / 2 + cancelLayout.height;
        font30.draw(batch, cancelText, cancel_x, cancel_y);
    }

    public void displayScores(List<ScoreManager.ScoreEntry> scoresList, boolean isGlobal, int selectedOption,
            boolean showPlayAgain) {
        String title;
        if (isGlobal) {
            title = "LEADERBOARD";
        } else {
            title = "LOCAL HIGH SCORES";
        }
        displayScores(scoresList, title, selectedOption, showPlayAgain, -1);
    }

    public void displayScores(List<ScoreManager.ScoreEntry> scoresList, String title, int selectedOption,
            boolean showPlayAgain, int recentScore) {
        // Removed dynamic scaling
        // float scaleFactor = ConfigUtils.calcularFatorDeEscala();
        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        GlyphLayout titleLayout = new GlyphLayout(font100, title);
        float title_x = game.getWorldWidth() / const_larg;
        float title_y = game.getWorldHeight() / 1.2f + titleLayout.height;
        font100.setColor(cian_color);
        font100.draw(batch, title, title_x, title_y);

        if (recentScore >= 0) {
            String scoreMsg = "YOUR SCORE: " + recentScore;
            font100.getData().setScale(scale * 0.5f);
            // GlyphLayout scoreLayout = new GlyphLayout(font30, scoreMsg); // Unused
            float score_y = title_y - titleLayout.height - (60 * scale);
            font100.setColor(cian_color);
            font100.draw(batch, scoreMsg, title_x, score_y);
            font100.getData().setScale(scale);
        }

        font30.setColor(cian_color);
        // Using fixed spacing instead of scaleFactor
        float startY = game.getWorldHeight() / 2 + (scoresList.size() / 2) * (30 * scale);

        // Desenhar cabeçalhos da tabela
        String rankHeader = "Rank";
        String playerHeader = "Player";
        String scoreHeader = "Score";

        GlyphLayout rankLayout = new GlyphLayout(font30, rankHeader);
        GlyphLayout playerLayout = new GlyphLayout(font30, playerHeader);

        float rankX = game.getWorldWidth() / const_larg;
        float playerX = rankX + rankLayout.width + (25 * scale); // Espaçamento entre colunas
        float scoreX = playerX + playerLayout.width + (280 * scale);

        float headerY = startY + (60 * scale); // Cabeçalhos um pouco acima da lista de scores
        font30.draw(batch, rankHeader, rankX, headerY);
        font30.draw(batch, playerHeader, playerX, headerY);
        font30.draw(batch, scoreHeader, scoreX, headerY);

        // Determinar a largura máxima da coluna Rank
        float maxRankWidth = rankLayout.width;
        for (int i = 0; i < scoresList.size(); i++) {
            String rank = (i + 1) + ".";
            GlyphLayout rankTextLayout = new GlyphLayout(font30, rank);
            if (rankTextLayout.width > maxRankWidth) {
                maxRankWidth = rankTextLayout.width;
            }
        }

        // Desenhar scores
        float y = startY;

        for (int i = 0; i < scoresList.size(); i++) {
            ScoreManager.ScoreEntry entry = scoresList.get(i);
            String rank = (i + 1) + ".";
            String player = entry.playerName;
            String score = String.valueOf(entry.score);

            GlyphLayout rankTextLayout = new GlyphLayout(font30, rank);
            // Right align rank number
            float rankXAdjusted = rankX + maxRankWidth - rankTextLayout.width;

            // Highlight if it's the current player
            if (com.space.game.SpaceGame.PLAYER_NAME != null
                    && com.space.game.SpaceGame.PLAYER_NAME.equalsIgnoreCase(player)) {
                font30.setColor(Color.YELLOW);
            } else {
                font30.setColor(cian_color);
            }

            font30.draw(batch, rank, rankXAdjusted, y);
            font30.draw(batch, player, playerX, y);
            font30.draw(batch, score, scoreX, y);

            y -= 42 * scale;
        }

        // Buttons at Bottom
        String playText = "Play Again";
        String backText = "Back";

        float buttonY = game.getWorldHeight() * 0.1f;

        // Draw Play Again (Option 0) - Bottom Right
        if (showPlayAgain) {
            GlyphLayout playLayout = new GlyphLayout(font30, playText);
            float playX = (const_larg - 1) * (game.getWorldWidth() / const_larg) - playLayout.width;

            if (selectedOption == 0) {
                font30.setColor(cian_color);
                font30.draw(batch, "> ", playX - (40 * scale), buttonY);
                font30.draw(batch, playText, playX, buttonY);
            } else {
                font30.setColor(Color.WHITE);
                font30.draw(batch, playText, playX, buttonY);
            }
        }

        // Draw Back (Option 1) - Bottom Left
        float backX = game.getWorldWidth() / const_larg;

        if (selectedOption == 1) {
            font30.setColor(cian_color);
            font30.draw(batch, "> ", backX - (40 * scale), buttonY);
            font30.draw(batch, backText, backX, buttonY);
        } else {
            font30.setColor(Color.WHITE);
            font30.draw(batch, backText, backX, buttonY);
        }
    }

    public void displayLoading(String message) {
        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        GlyphLayout layout = new GlyphLayout(font100, "LOADING...");
        float x = game.getWorldWidth() / 2 - layout.width / 2;
        float y = game.getWorldHeight() / 2 + layout.height;
        font100.setColor(cian_color);
        font100.draw(batch, "LOADING...", x, y);

        GlyphLayout msgLayout = new GlyphLayout(font30, message);
        float msgX = game.getWorldWidth() / 2 - msgLayout.width / 2;
        float msgY = y - layout.height * 1.5f;
        font30.setColor(cian_color);
        font30.draw(batch, message, msgX, msgY);

        String continueText = "Esc. Back";
        font30.draw(batch, continueText, game.getWorldWidth() / const_larg, game.getWorldHeight() * 0.1f);
    }

    public void dispose() {
        font30.dispose();
        font100.dispose();
        font150.dispose();
    }

    public void displayIntro() {
        float scale = getScaleFactor();
        font30.getData().setScale(scale);

        String pressKeyText = "Press any key to start";
        GlyphLayout pressKeyLayout = new GlyphLayout(font30, pressKeyText);
        float pressKeyX = game.getWorldWidth() / 2 - pressKeyLayout.width / 2;
        float pressKeyY = game.getWorldHeight() / 2 + pressKeyLayout.height / 2;

        // Blink effect
        float alpha = (float) Math.abs(Math.sin(System.currentTimeMillis() / 500.0));
        font30.setColor(1, 1, 1, alpha);
        font30.draw(batch, pressKeyText, pressKeyX, pressKeyY);
        font30.setColor(Color.WHITE); // Reset
    }

    public void setHordas(int hordas) {
        this.hordas = hordas;
    }

    public int getHordas() {
        return hordas;
    }

    public void displaySettings(float soundVolume, float musicVolume, int selectedOption) {
        // Removed dynamic scaling
        // float scaleFactor = ConfigUtils.calcularFatorDeEscala();
        float scale = getScaleFactor();
        font100.getData().setScale(scale);
        font30.getData().setScale(scale);

        String title = "SETTINGS";
        GlyphLayout titleLayout = new GlyphLayout(font100, title);
        float title_x = game.getWorldWidth() / const_larg;
        float title_y = game.getWorldHeight() / 1.2f + titleLayout.height;
        font100.setColor(cian_color);
        font100.draw(batch, title, title_x, title_y);

        float startY = game.getWorldHeight() / 1.5f;

        // User Info (Left Aligned)
        String pName = com.space.game.SpaceGame.PLAYER_NAME;
        String pEmail = com.space.game.SpaceGame.PLAYER_EMAIL;
        if (pName == null)
            pName = "GUEST";
        if (pEmail == null)
            pEmail = "N/A";

        font30.setColor(Color.WHITE);
        font30.draw(batch, "Username: " + pName, title_x, startY);
        font30.draw(batch, "Email: " + pEmail, title_x, startY - (50 * scale));

        // Volume Options (Left Aligned Columns)
        float optionsY = startY - (150 * scale);
        String[] options = { "Music Volume", "Sound Volume" };
        float[] values = { musicVolume, soundVolume };
        float col2Offset = 400 * scale; // Fixed distance for second column

        for (int i = 0; i < 2; i++) {
            int pct = (int) (values[i] * 100);

            float labelX = title_x;
            float valueX = title_x + col2Offset;

            if (i == selectedOption) {
                font30.setColor(cian_color);
                font30.draw(batch, ">", labelX - (40 * scale), optionsY);
                font30.draw(batch, options[i], labelX, optionsY);
                font30.draw(batch, "< " + pct + "% >", valueX, optionsY);
            } else {
                font30.setColor(Color.WHITE);
                font30.draw(batch, options[i], labelX, optionsY);
                font30.draw(batch, pct + "%", valueX, optionsY);
            }

            optionsY -= 80 * scale;
        }

        // Back Button at Bottom (Left Aligned)
        String backText = "Back";
        float backX = game.getWorldWidth() / const_larg;
        float backY = game.getWorldHeight() * 0.1f;

        if (selectedOption == 2) {
            font30.setColor(cian_color);
            font30.draw(batch, "> ", backX - (40 * scale), backY);
            font30.draw(batch, backText, backX, backY);
        } else {
            font30.setColor(Color.WHITE);
            font30.draw(batch, backText, backX, backY);
        }
    }
}
