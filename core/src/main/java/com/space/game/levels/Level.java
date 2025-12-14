package com.space.game.levels;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.space.game.entities.Spaceship;
import com.space.game.config.LevelConfig;

public interface Level {
    void render(SpriteBatch batch);
    void update();
    void dispose();
    Spaceship getSpaceship();
    void freeSpaceship();
    boolean getEndLevel();
    LevelConfig getConfig();
}
