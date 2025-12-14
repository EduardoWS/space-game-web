package com.space.game.graphics;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;

public class TextureManager {
    private HashMap<String, Texture> textures;

    public TextureManager() {
        textures = new HashMap<String, Texture>();
    }

    public void loadTexture(String key, String path) {
        try{
            Texture texture = new Texture(path);
            textures.put(key, texture);
        } catch (Exception e) {
            System.out.println("Error loading texture: " + path);
            e.printStackTrace();
        }
    }

    public Texture getTexture(String key) {
        return textures.get(key);
    }

    public void dispose() {
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
    }

    public void loadTextures(TextureManager textureManager){
        // Carregar texturas
        textureManager.loadTexture("bullet", TexturePaths.BULLET);
        textureManager.loadTexture("spaceship", TexturePaths.SPACESHIP);
        textureManager.loadTexture("alien", TexturePaths.ALIEN);
        textureManager.loadTexture("alienDead", TexturePaths.ALIEN_DEAD);
        textureManager.loadTexture("background", TexturePaths.BACKGROUND);
        textureManager.loadTexture("star", TexturePaths.STAR);
    }
}