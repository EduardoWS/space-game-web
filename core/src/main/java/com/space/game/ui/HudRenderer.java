package com.space.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.Game;
import com.space.game.entities.Spaceship;
import com.space.game.managers.FeedbackQueue;

public class HudRenderer {
  private Game game;
  private SpriteBatch batch;
  private BitmapFont font30;
  private Color cian_color = new Color(0.0f, 1.0f, 1.0f, 1.0f);

  public HudRenderer(Game game, SpriteBatch batch, BitmapFont font30) {
    this.game = game;
    this.batch = batch;
    this.font30 = font30;
  }

  public void render(Spaceship spaceship, float xOffset, int hordas, FeedbackQueue scoreQueue,
      FeedbackQueue energyQueue) {
    float scale = getScaleFactor();
    font30.getData().setScale(scale);
    font30.setColor(cian_color);

    // Energy (Bottom Left)
    renderEnergy(spaceship, xOffset, scale, energyQueue);

    // Wave (Bottom Right)
    renderWave(hordas, scale);

    // Music Info (Bottom Center)
    renderMusicInfo(scale);

    // Score (Top Left)
    renderScore(spaceship, xOffset, scale, scoreQueue);

    // Streak (Top Right)
    renderStreak(spaceship, scale);
  }

  private void renderEnergy(Spaceship spaceship, float xOffset, float scale, FeedbackQueue energyQueue) {
    if (spaceship.getEnergy() <= 10.0f) {
      font30.setColor(Color.RED);
    } else {
      font30.setColor(cian_color);
    }
    String energyText = formatEnergy(spaceship.getEnergy());
    GlyphLayout energyLayout = new GlyphLayout(font30, energyText);
    float energy_x = xOffset + (game.getWorldWidth() / 21);
    float energy_y = energyLayout.height / 2 + energyLayout.height;
    font30.draw(batch, energyText, energy_x, energy_y);

    // Feedback
    font30.getData().setScale(scale * 0.85f);
    energyQueue.render(batch, font30, energy_x, energy_y + (50 * scale), true);
    font30.getData().setScale(scale);
    font30.setColor(cian_color);
  }

  private void renderWave(int hordas, float scale) {
    String hordasText = "WAVE: " + hordas;
    GlyphLayout hordasLayout = new GlyphLayout(font30, hordasText);
    float hordas_x = (21 - 1) * (game.getWorldWidth() / 21) - hordasLayout.width;
    float hordas_y = hordasLayout.height / 2 + hordasLayout.height;
    font30.draw(batch, hordasText, hordas_x, hordas_y);
  }

  private void renderMusicInfo(float scale) {
    String musicText = game.getSoundManager().getCurrentTrackName();
    if (musicText != null && !musicText.isEmpty()) {
      GlyphLayout musicLayout = new GlyphLayout(font30, musicText);
      float music_x = game.getWorldWidth() / 2 - musicLayout.width / 2;
      float music_y = musicLayout.height / 2 + musicLayout.height;
      font30.setColor(cian_color);
      font30.draw(batch, musicText, music_x, music_y);
    }
  }

  private void renderScore(Spaceship spaceship, float xOffset, float scale, FeedbackQueue scoreQueue) {
    String killsText = "SCORE: " + (spaceship.getKillCount());
    GlyphLayout killsLayout = new GlyphLayout(font30, killsText);
    float kills_x = xOffset + (game.getWorldWidth() / 21);
    float kills_y = game.getWorldHeight() - killsLayout.height;
    font30.draw(batch, killsText, kills_x, kills_y);

    // Feedback
    font30.getData().setScale(scale * 0.85f);
    scoreQueue.render(batch, font30, kills_x, kills_y - (50 * scale), false);
    font30.getData().setScale(scale);
    font30.setColor(cian_color);
  }

  private void renderStreak(Spaceship spaceship, float scale) {
    String streakText = "STREAK: x" + spaceship.getStreakCount();
    GlyphLayout streakLayout = new GlyphLayout(font30, streakText);
    float streak_x = (21 - 1) * (game.getWorldWidth() / 21) - streakLayout.width;
    float streak_y = game.getWorldHeight() - streakLayout.height;
    font30.draw(batch, streakText, streak_x, streak_y);
  }

  private String formatEnergy(float energy) {
    int val = (int) (energy * 100);
    int intPart = val / 100;
    int decPart = val % 100;
    String decStr = decPart < 10 ? "0" + decPart : "" + decPart;
    return "ENERGY: " + intPart + "." + decStr + "%";
  }

  private float getScaleFactor() {
    float widthScale = game.getWorldWidth() / 1920f;
    float heightScale = game.getWorldHeight() / 1080f;
    return Math.min(widthScale, heightScale);
  }
}
