package com.space.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.Game;
import com.space.game.config.GameConfig;

public class MenuRenderer {
  private Game game;
  private SpriteBatch batch;
  private BitmapFont font30;
  private BitmapFont font150;
  private Color cian_color = new Color(0.0f, 1.0f, 1.0f, 1.0f);
  private final int const_larg = 21;

  public MenuRenderer(Game game, SpriteBatch batch, BitmapFont font30, BitmapFont font100, BitmapFont font150) {
    this.game = game;
    this.batch = batch;
    this.font30 = font30;
    this.font150 = font150;
  }

  private float getScaleFactor() {
    float widthScale = game.getWorldWidth() / 1920f;
    float heightScale = game.getWorldHeight() / 1080f;
    return Math.min(widthScale, heightScale);
  }

  public void renderMainMenu(boolean isDatabaseAvailable, int currentSelection, float stateTimer) {
    float scale = getScaleFactor();
    float titleAnimDuration = 1.0f;
    float menuAnimDelay = 0.5f;
    float menuAnimDuration = 1.0f;

    // Title Animation
    float titleAlpha = Math.min(1.0f, stateTimer / titleAnimDuration);
    String title = "SPACE GAME";

    font150.getData().setScale(scale);
    GlyphLayout titleLayout = new GlyphLayout(font150, title);
    float title_x = game.getWorldWidth() / const_larg;
    float targetTitleY = game.getWorldHeight() / 1.5f + titleLayout.height;
    float startTitleY = targetTitleY + (100f * scale);
    float title_y = startTitleY + (targetTitleY - startTitleY) * titleAlpha;

    font150.setColor(0, 1, 1, titleAlpha);
    font150.draw(batch, title, title_x, title_y);
    font150.setColor(cian_color);

    // Menu Options
    float menuTimer = Math.max(0, stateTimer - menuAnimDelay);
    float menuAlpha = Math.min(1.0f, menuTimer / menuAnimDuration);

    if (menuTimer > 0) {
      drawMenuOptions(isDatabaseAvailable, currentSelection, menuAlpha, scale, targetTitleY, titleLayout.height,
          title_x);

      // Version
      String versionText = GameConfig.GAME_VERSION;
      font30.getData().setScale(scale);
      GlyphLayout versionLayout = new GlyphLayout(font30, versionText);
      float versionX = game.getWorldWidth() - versionLayout.width - (20 * scale);
      float versionY = 30 * scale;
      font30.setColor(Color.WHITE);
      font30.draw(batch, versionText, versionX, versionY);
    }
  }

  private void drawMenuOptions(boolean isDatabaseAvailable, int currentSelection, float alpha, float scale,
      float titleY, float titleHeight, float startX) {
    String startText = "Start Arcade Mode";
    String scoresText = "Global Scores";

    float targetY = titleY - titleHeight * 3;
    float startY = targetY - (50f * scale);
    float currentY = startY + (targetY - startY) * alpha;

    font30.getData().setScale(scale);
    float cursorOffset = 40f * scale;

    Color selectedColor = new Color(cian_color.r, cian_color.g, cian_color.b, alpha);
    Color unselectedColor = new Color(1, 1, 1, alpha);

    // Start
    if (currentSelection == 0) {
      font30.setColor(selectedColor);
      font30.draw(batch, ">", startX - cursorOffset, currentY);
      font30.draw(batch, startText, startX, currentY);
    } else {
      font30.setColor(unselectedColor);
      font30.draw(batch, startText, startX, currentY);
    }

    if (isDatabaseAvailable) {
      GlyphLayout layout = new GlyphLayout(font30, startText); // Approx height ref
      currentY = currentY - layout.height * 3;

      // Global Scores
      if (currentSelection == 1) {
        font30.setColor(selectedColor);
        font30.draw(batch, ">", startX - cursorOffset, currentY);
        font30.draw(batch, scoresText, startX, currentY);
      } else {
        font30.setColor(unselectedColor);
        font30.draw(batch, scoresText, startX, currentY);
      }

      // Settings
      String settingsText = "Settings";
      currentY = currentY - layout.height * 3;
      if (currentSelection == 2) {
        font30.setColor(selectedColor);
        font30.draw(batch, ">", startX - cursorOffset, currentY);
        font30.draw(batch, settingsText, startX, currentY);
      } else {
        font30.setColor(unselectedColor);
        font30.draw(batch, settingsText, startX, currentY);
      }

      // Exit
      String exitText = "Exit";
      float exitY = currentY - layout.height * 9;
      if (currentSelection == 3) {
        font30.setColor(selectedColor);
        font30.draw(batch, ">", startX - cursorOffset, exitY);
        font30.draw(batch, exitText, startX, exitY);
      } else {
        font30.setColor(unselectedColor);
        font30.draw(batch, exitText, startX, exitY);
      }
    }
    font30.setColor(Color.WHITE);
  }
}
