package com.space.game.config;

import com.badlogic.gdx.graphics.Color;

public enum LevelTheme {
  DEFAULT("background", Color.WHITE),
  NEBULA_BLUE("background", new Color(0.8f, 0.8f, 1.0f, 1.0f)),
  DEEP_SPACE_RED("background", new Color(1.0f, 0.8f, 0.8f, 1.0f)),
  VOID_DARK("background", new Color(0.6f, 0.6f, 0.6f, 1.0f));

  private final String backgroundTextureKey;
  private final Color ambientColor;

  LevelTheme(String backgroundTextureKey, Color ambientColor) {
    this.backgroundTextureKey = backgroundTextureKey;
    this.ambientColor = ambientColor;
  }

  public String getBackgroundTextureKey() {
    return backgroundTextureKey;
  }

  public Color getAmbientColor() {
    return ambientColor;
  }
}
