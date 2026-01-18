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
}
