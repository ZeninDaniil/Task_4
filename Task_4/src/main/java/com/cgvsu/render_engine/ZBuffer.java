package com.cgvsu.render_engine;

public class ZBuffer {
    private float[][] buffer;
    private int width;
    private int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width][height];
        clear();
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer[x][y] = Float.POSITIVE_INFINITY;
            }
        }
    }

    public boolean testAndSet(int x, int y, float depth) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        
        if (depth < buffer[x][y]) {
            buffer[x][y] = depth;
            return true;
        }
        return false;
    }

    public void resize(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            this.buffer = new float[width][height];
            clear();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
