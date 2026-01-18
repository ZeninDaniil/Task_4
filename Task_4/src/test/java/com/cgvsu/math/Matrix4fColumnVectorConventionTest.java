package com.cgvsu.math;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Matrix4fColumnVectorConventionTest {

    private static void assertNear(float expected, float actual) {
        assertEquals(expected, actual, 1e-5f);
    }

    @Test
    void translationMovesPoint() {
        Matrix4f t = Matrix4f.translation(1f, 2f, 3f);
        Vector3fImpl p = new Vector3fImpl(10f, 20f, 30f);

        Vector3fImpl r = t.transformPoint(p);

        assertNear(11f, r.getX());
        assertNear(22f, r.getY());
        assertNear(33f, r.getZ());
    }

    @Test
    void scaleScalesPoint() {
        Matrix4f s = Matrix4f.scale(2f, 3f, 4f);
        Vector3fImpl p = new Vector3fImpl(1f, -2f, 0.5f);

        Vector3fImpl r = s.transformPoint(p);

        assertNear(2f, r.getX());
        assertNear(-6f, r.getY());
        assertNear(2f, r.getZ());
    }

    @Test
    void multiplyOrderMatchesColumnVectors() {
        Matrix4f s = Matrix4f.scale(2f, 2f, 2f);
        Matrix4f t = Matrix4f.translation(1f, 0f, 0f);

        Matrix4f m = (Matrix4f) t.multiply(s);

        Vector3fImpl p = new Vector3fImpl(1f, 0f, 0f);
        Vector3fImpl r = m.transformPoint(p);

        assertNear(3f, r.getX());
        assertNear(0f, r.getY());
        assertNear(0f, r.getZ());
    }
}
