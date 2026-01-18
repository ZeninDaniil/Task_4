package com.cgvsu.math;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LookAtViewMatrixTest {

    @Test
    void viewMatrixChangesWithEyePosition() {
        Vector3fImpl up = new Vector3fImpl(0f, 1f, 0f);
        Vector3fImpl target = new Vector3fImpl(0f, 0f, 0f);

        Matrix4f v1 = Matrix4f.lookAt(new Vector3fImpl(0f, 0f, 5f), target, up);
        Matrix4f v2 = Matrix4f.lookAt(new Vector3fImpl(0f, 0f, 10f), target, up);

        float[] a1 = v1.toArray();
        float[] a2 = v2.toArray();

        boolean differs = false;
        for (int i = 0; i < 16; i++) {
            if (a1[i] != a2[i]) {
                differs = true;
                break;
            }
        }

        assertTrue(differs, "View matrix should depend on eye position");
    }
}
