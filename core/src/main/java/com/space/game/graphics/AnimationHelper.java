package com.space.game.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationHelper {

  /**
   * Splits a sprite sheet into an Animation.
   * Assumes a single row strip.
   *
   * @param texture       The sprite sheet texture.
   * @param tileWidth     Width of a single frame.
   * @param tileHeight    Height of a single frame.
   * @param frameDuration Duration of each frame.
   * @param loop          Whether the animation should loop.
   * @return The created Animation.
   */
  public static Animation<TextureRegion> createAnimation(Texture texture, int tileWidth, int tileHeight,
      float frameDuration, boolean loop) {
    TextureRegion[][] tmp = TextureRegion.split(texture, tileWidth, tileHeight);

    // Flatten the 2D array into a 1D array (since it's a strip, we take the first
    // row)
    // Adjust for multi-row logic if needed in future, but for now single row strip.
    Array<TextureRegion> frames = new Array<>();

    // Iterate through all rows and cols to be generic, or just row 0.
    // User specified "single row strip", so split[0] array contains all columns.

    if (tmp.length > 0) {
      for (int j = 0; j < tmp[0].length; j++) {
        frames.add(tmp[0][j]);
      }
    }

    Animation<TextureRegion> animation = new Animation<>(frameDuration, frames,
        loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
    return animation;
  }
}
