package com.cgvsu.math;

/**
 * Legacy-friendly mutable vector used by the OBJ model representation.
 *
 * Rendering/math pipeline uses immutable vectors in {@code com.cgvsu.math.vector.impl}.
 */
public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3f subtract(Vector3f a, Vector3f b) {
        return new Vector3f(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3f add(Vector3f a, Vector3f b) {
        return new Vector3f(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3f cross(Vector3f a, Vector3f b) {
        return new Vector3f(
            a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x
        );
    }

    public void normalize() {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len != 0) {
            x /= len;
            y /= len;
            z /= len;
        }
    }
}
