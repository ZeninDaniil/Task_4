package com.cgvsu.model;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelMatrixTest {

    private static void assertNear(float expected, float actual) {
        assertEquals(expected, actual, 1e-5f);
    }

    @Test
    void modelMatrixComposesScaleThenTranslate() {
        Model m = new Model();
        m.scale = 2;
        m.translation = new com.cgvsu.math.Vector3f(1f, 0f, 0f);

        Matrix4f M = m.getModelMatrix();
        Vector3fImpl p = new Vector3fImpl(1f, 0f, 0f);

        Vector3fImpl r = M.transformPoint(p);

        // scale: (2,0,0) then translate: (3,0,0)
        assertNear(3f, r.getX());
        assertNear(0f, r.getY());
        assertNear(0f, r.getZ());
    }
}
