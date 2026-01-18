package com.cgvsu.math.vector.impl;

import com.cgvsu.math.vector.interfaces.Vector;
import java.util.Arrays;

public class Vector3fImpl implements Vector {

    private final float x;
    private final float y;
    private final float z;

    public Vector3fImpl(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3fImpl(float value) {
        this(value, value, value);
    }

    public Vector3fImpl() {
        this(0.f, 0.f, 0.f);
    }

    @Override
    public int getDimension() { return 3; }

    @Override
    public Vector createVector(float... data) {
        if (data.length != 3) {
            throw new IllegalArgumentException("Illegal arguments for 3fVector!");
        }
        return new Vector3fImpl(data[0], data[1], data[2]);
    }

    @Override
    public float getValueByIndex(int index) {
        return switch (index) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        };
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    public Vector3fImpl add(Vector3fImpl other) {
        return new Vector3fImpl(x + other.x, y + other.y, z + other.z);
    }

    public Vector3fImpl sub(Vector3fImpl other) {
        return new Vector3fImpl(x - other.x, y - other.y, z - other.z);
    }

    public Vector3fImpl scale(float s) {
        return new Vector3fImpl(x * s, y * s, z * s);
    }

    public float dot(Vector3fImpl other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3fImpl cross(Vector3fImpl other) {
        float cx = y * other.z - z * other.y;
        float cy = z * other.x - x * other.z;
        float cz = x * other.y - y * other.x;
        return new Vector3fImpl(cx, cy, cz);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public Vector3fImpl normalize() {
        float len = length();
        if (len == 0f) return new Vector3fImpl(0f, 0f, 0f);
        return scale(1.0f / len);
    }

    public Vector3fImpl negate() {
        return new Vector3fImpl(-x, -y, -z);
    }

    public Vector3fImpl lerp(Vector3fImpl to, float t) {
        return new Vector3fImpl(
                x + (to.x - x) * t,
                y + (to.y - y) * t,
                z + (to.z - z) * t
        );
    }

    public float[] toArray() {
        return new float[]{x, y, z};
    }

    @Override
    public String toString() {
        return "Vector3fImpl{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3fImpl)) return false;
        Vector3fImpl that = (Vector3fImpl) o;
        return Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0 &&
                Float.compare(that.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new float[]{x, y, z});
    }
}

