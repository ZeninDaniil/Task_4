package com.cgvsu.math.vector.impl;

import com.cgvsu.math.vector.interfaces.Vector;
import java.util.Arrays;

public class Vector4fImpl implements Vector {

    private final float x;
    private final float y;
    private final float z;
    private final float w;

    public Vector4fImpl(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4fImpl(float value) {
        this(value, value, value, value);
    }

    public Vector4fImpl() {
        this(0f, 0f, 0f, 0f);
    }

    @Override
    public int getDimension() { return 4; }

    @Override
    public Vector createVector(float... data) {
        if (data.length != 4) {
            throw new IllegalArgumentException("Illegal arguments for 4fVector!");
        }
        return new Vector4fImpl(data[0], data[1], data[2], data[3]);
    }

    @Override
    public float getValueByIndex(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            case 3 -> w;
            default -> throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        };
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getW() { return w; }

    public Vector4fImpl add(Vector4fImpl other) {
        return new Vector4fImpl(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    public Vector4fImpl sub(Vector4fImpl other) {
        return new Vector4fImpl(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    public Vector4fImpl scale(float s) {
        return new Vector4fImpl(x * s, y * s, z * s, w * s);
    }

    public float dot(Vector4fImpl other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public Vector4fImpl normalize() {
        float len = length();
        if (len == 0f) return new Vector4fImpl(0f, 0f, 0f, 0f);
        return scale(1.0f / len);
    }

    public Vector4fImpl negate() {
        return new Vector4fImpl(-x, -y, -z, -w);
    }

    public Vector4fImpl lerp(Vector4fImpl to, float t) {
        return new Vector4fImpl(
                x + (to.x - x) * t,
                y + (to.y - y) * t,
                z + (to.z - z) * t,
                w + (to.w - w) * t
        );
    }

    public float[] toArray() {
        return new float[]{x, y, z, w};
    }

    @Override
    public String toString() {
        return "Vector4fImpl{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector4fImpl)) return false;
        Vector4fImpl that = (Vector4fImpl) o;
        return Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0 &&
                Float.compare(that.z, z) == 0 &&
                Float.compare(that.w, w) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new float[]{x, y, z, w});
    }
}
