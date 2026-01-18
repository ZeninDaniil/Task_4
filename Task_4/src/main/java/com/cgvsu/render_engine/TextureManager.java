package com.cgvsu.render_engine;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static TextureManager instance;
    private Map<String, WritableImage> textures = new HashMap<>();
    private WritableImage currentTexture = null;

    private TextureManager() {
    }

    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Загружает текстуру из файла
     */
    public WritableImage loadTexture(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            Image image = new Image(fis);
            
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();
            
            WritableImage writableImage = new WritableImage(width, height);
            PixelReader reader = image.getPixelReader();
            PixelWriter writer = writableImage.getPixelWriter();
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    writer.setColor(x, y, reader.getColor(x, y));
                }
            }
            
            textures.put(file.getName(), writableImage);
            currentTexture = writableImage;
            return writableImage;
        }
    }

    /**
     * Загружает текстуру по имени файла
     */
    public WritableImage loadTexture(String filename) throws IOException {
        return loadTexture(new File(filename));
    }

    /**
     * Получает текстуру по имени
     */
    public WritableImage getTexture(String name) {
        return textures.get(name);
    }

    /**
     * Получает текущую активную текстуру
     */
    public WritableImage getCurrentTexture() {
        return currentTexture;
    }

    /**
     * Устанавливает текущую текстуру
     */
    public void setCurrentTexture(String name) {
        currentTexture = textures.get(name);
    }

    /**
     * Устанавливает текущую текстуру напрямую
     */
    public void setCurrentTexture(WritableImage texture) {
        currentTexture = texture;
    }

    /**
     * Удаляет текстуру
     */
    public void removeTexture(String name) {
        WritableImage removed = textures.remove(name);
        if (removed == currentTexture) {
            currentTexture = null;
        }
    }

    /**
     * Очищает все текстуры
     */
    public void clearTextures() {
        textures.clear();
        currentTexture = null;
    }

    /**
     * Проверяет, загружена ли текстура
     */
    public boolean hasTexture(String name) {
        return textures.containsKey(name);
    }

    /**
     * Получает все имена загруженных текстур
     */
    public String[] getTextureNames() {
        return textures.keySet().toArray(new String[0]);
    }
}
