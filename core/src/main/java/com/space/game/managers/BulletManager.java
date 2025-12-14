package com.space.game.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.math.Vector2;
import com.space.game.entities.Bullet;
import com.space.game.graphics.TextureManager;
import com.space.game.managers.GameStateManager.State;
import com.space.game.SpaceGame;


public class BulletManager {
    private List<Bullet> bullets;
    private TextureManager textureManager;
    private GameStateManager gsm;
    private SoundManager soundManager;

    public BulletManager(TextureManager textureManager, SoundManager soundManager, GameStateManager gsm) {
        this.bullets = new ArrayList<>();
        this.textureManager = textureManager;
        this.soundManager = soundManager;
        this.gsm = gsm;
    }

    public void fireBullet(Vector2 position, float angle, float spaceshipWidth, float spaceshipHeight, float scale) {
        if (bullets.size() < 7) {  // Limita o número de balas ativas
            Bullet newBullet = new Bullet(textureManager, position, angle, spaceshipWidth, spaceshipHeight, scale);
            bullets.add(newBullet);
            soundManager.playBulletSound();
        }
    }

    public void update() {
        if (gsm.getState() != State.PLAYING) {
            return;
        }
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();
            // Remove bullets that are off-screen or have collided
            if (bullet.getPosition().x < 0-SpaceGame.getGame().getWorldWidth()/2 || 
            bullet.getPosition().x > SpaceGame.getGame().getWorldWidth()*1.5f || bullet.getPosition().y < 0 - SpaceGame.getGame().getWorldHeight()/2 || 
            bullet.getPosition().y > SpaceGame.getGame().getWorldHeight()*1.5f) {
                bulletIterator.remove();
                bullet.dispose();
                SpaceGame.getGame().getMapManager().getSpaceship().setStreakCount(1);
                SpaceGame.getGame().getMapManager().getSpaceship().setConsecutiveKills(0);
            } else if (bullet.shouldRemove() ){
                bulletIterator.remove();
                bullet.dispose();
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Bullet bullet : bullets) {
            bullet.render(batch);
        }
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void dispose() {
        for (Bullet bullet : bullets) {
            bullet.dispose();
        }
        bullets.clear();
    }
}
