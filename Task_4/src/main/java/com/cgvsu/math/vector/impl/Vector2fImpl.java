package com.cgvsu.math.vector.impl;

import com.cgvsu.math.vector.interfaces.Vector;

public class Vector2fImpl implements Vector {

    private final float x;
    private final float y;

    public Vector2fImpl(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2fImpl(float value) {
        this(value, value);
    }

    public Vector2fImpl() {
        this(0f, 0f);
    }

    @Override
    public Vector createVector(float... data) {
        if (data.length != 2) {
            throw new IllegalArgumentException("Illegal arguments for 2fVector!");
        }
        return new Vector2fImpl(data[0], data[1]);
    }

    @Override
    public float getValueByIndex(int index) {
        float tmp;
        switch (index) {
            case 0 -> tmp = x;
            case 1 -> tmp = y;
            default -> throw new IndexOutOfBoundsException("Index out of bounce");
        }
        return tmp;
    }

    @Override
    public int getDimension() {
        return 2;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
    
    public Vector2fImpl add(Vector2fImpl other) {
        return new Vector2fImpl(x + other.x, y + other.y);
    }

    public Vector2fImpl sub(Vector2fImpl other) {
        return new Vector2fImpl(x - other.x, y - other.y);
    }

    public Vector2fImpl scale(float s) {
        return new Vector2fImpl(x * s, y * s);
    }

    public float dot(Vector2fImpl other) {
        return x * other.x + y * other.y;
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public Vector2fImpl normalize() {
        float len = length();
        if (len == 0f) return new Vector2fImpl(0f, 0f);
        return scale(1.0f / len);
    }

    public Vector2fImpl negate() {
        return new Vector2fImpl(-x, -y);
    }

    public Vector2fImpl lerp(Vector2fImpl to, float t) {
        return new Vector2fImpl(
                x + (to.x - x) * t,
                y + (to.y - y) * t
        );
    }

    public float[] toArray() {
        return new float[]{x, y};
    }

    @Override
    public String toString() {
        return "Vector2fImpl{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2fImpl)) return false;
        Vector2fImpl that = (Vector2fImpl) o;
        return Float.compare(that.x, x) == 0 && Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int h = 31;
        h = h * 17 + Float.hashCode(x);
        h = h * 17 + Float.hashCode(y);
        return h;
    }
}