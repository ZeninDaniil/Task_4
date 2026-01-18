package com.cgvsu.math.matrix.impl;

import com.cgvsu.math.matrix.interfaces.Matrix;
import com.cgvsu.math.vector.impl.Vector3fImpl;

public class Matrix3f implements Matrix {

    private final float[] m;

    public Matrix3f() {
        m = new float[9];
        m[0] = 1f; m[4] = 1f; m[8] = 1f;
    }

    public Matrix3f(float[] values) {
        if (values.length != 9) throw new IllegalArgumentException("Matrix3f requires 9 values");
        m = values.clone();
    }

    public static Matrix3f identity() {
        return new Matrix3f();
    }

    @Override
    public float get(int row, int col) {
        return m[row * 3 + col];
    }
    @Override
    public int getRows() { return 3; }

    @Override
    public int getCols() { return 3; }
    @Override
    public Matrix multiply(Matrix other) {
        if (other.getRows() != 3 || other.getCols() != 3) {
            throw new IllegalArgumentException("Matrix dimensions must match 3x3");
        }
        float[] r = new float[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                float sum = 0f;
                for (int k = 0; k < 3; k++) {
                    sum += this.get(row, k) * other.get(k, col);
                }
                r[row * 3 + col] = sum;
            }
        }
        return new Matrix3f(r);
    }

    public Vector3fImpl transform(Vector3fImpl v) {
        float x = get(0,0) * v.getX() + get(0,1) * v.getY() + get(0,2) * v.getZ();
        float y = get(1,0) * v.getX() + get(1,1) * v.getY() + get(1,2) * v.getZ();
        float z = get(2,0) * v.getX() + get(2,1) * v.getY() + get(2,2) * v.getZ();
        return new Vector3fImpl(x, y, z);
    }

    public static Matrix3f fromMatrix4(Matrix4f mat4) {
        float[] out = new float[9];
        // copy upper-left 3x3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                out[row * 3 + col] = mat4.get(row, col);
            }
        }
        return new Matrix3f(out);
    }

    public float[] toArray() {
        return m.clone();
    }

    @Override
    public String toString() {
        return "Matrix3f{" +
                "m0=" + m[0] + ", m1=" + m[1] + ", m2=" + m[2] +
                ", m3=" + m[3] + ", m4=" + m[4] + ", m5=" + m[5] +
                ", m6=" + m[6] + ", m7=" + m[7] + ", m8=" + m[8] +
                '}';
    }
}
