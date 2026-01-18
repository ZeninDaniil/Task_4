package com.cgvsu.math.matrix.impl;

import com.cgvsu.math.matrix.interfaces.Matrix;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import com.cgvsu.math.vector.impl.Vector4fImpl;

public class Matrix4f implements Matrix {

    private final float[] m; // row-major 4x4

    public Matrix4f() {
        m = new float[16];
        m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f;
    }

    public Matrix4f(float[] values) {
        if (values.length != 16) throw new IllegalArgumentException("Matrix4f requires 16 values");
        m = values.clone();
    }

    public static Matrix4f identity() {
        return new Matrix4f();
    }

    public float get(int row, int col) {
        return m[row * 4 + col];
    }
    @Override
    public int getRows() { return 4; }

    @Override
    public int getCols() { return 4; }

    @Override
    public Matrix multiply(Matrix other) {
        if (other.getRows() != 4 || other.getCols() != 4) {
            throw new IllegalArgumentException("Matrix dimensions must match 4x4");
        }
        float[] r = new float[16];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                float sum = 0f;
                for (int k = 0; k < 4; k++) {
                    sum += this.get(row, k) * other.get(k, col);
                }
                r[row * 4 + col] = sum;
            }
        }
        return new Matrix4f(r);
    }

    public Vector4fImpl transform(Vector4fImpl v) {
        float x = get(0,0) * v.getX() + get(0,1) * v.getY() + get(0,2) * v.getZ() + get(0,3) * v.getW();
        float y = get(1,0) * v.getX() + get(1,1) * v.getY() + get(1,2) * v.getZ() + get(1,3) * v.getW();
        float z = get(2,0) * v.getX() + get(2,1) * v.getY() + get(2,2) * v.getZ() + get(2,3) * v.getW();
        float w = get(3,0) * v.getX() + get(3,1) * v.getY() + get(3,2) * v.getZ() + get(3,3) * v.getW();
        return new Vector4fImpl(x, y, z, w);
    }

    public Vector3fImpl transformPoint(Vector3fImpl v) {
        Vector4fImpl r = transform(new Vector4fImpl(v.getX(), v.getY(), v.getZ(), 1f));
        if (r.getW() == 0f) return new Vector3fImpl(r.getX(), r.getY(), r.getZ());
        return new Vector3fImpl(r.getX() / r.getW(), r.getY() / r.getW(), r.getZ() / r.getW());
    }

    public Vector3fImpl transformDirection(Vector3fImpl v) {
        Vector4fImpl r = transform(new Vector4fImpl(v.getX(), v.getY(), v.getZ(), 0f));
        return new Vector3fImpl(r.getX(), r.getY(), r.getZ());
    }

    public static Matrix4f translation(float tx, float ty, float tz) {
        float[] a = new float[16];
        a[0] = 1f; a[5] = 1f; a[10] = 1f; a[15] = 1f;
        a[0*4 + 3] = tx; a[1*4 + 3] = ty; a[2*4 + 3] = tz;
        return new Matrix4f(a);
    }

    public static Matrix4f scale(float sx, float sy, float sz) {
        float[] a = new float[16];
        a[0] = sx; a[5] = sy; a[10] = sz; a[15] = 1f;
        return new Matrix4f(a);
    }

    public static Matrix4f rotationX(float angleRad) {
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        float[] a = new float[16];
        a[0] = 1f; a[5] = c; a[6] = -s; a[9] = s; a[10] = c; a[15] = 1f;
        return new Matrix4f(a);
    }

    public static Matrix4f rotationY(float angleRad) {
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        float[] a = new float[16];
        a[0] = c; a[2] = s; a[5] = 1f; a[8] = -s; a[10] = c; a[15] = 1f;
        return new Matrix4f(a);
    }

    public static Matrix4f rotationZ(float angleRad) {
        float c = (float) Math.cos(angleRad);
        float s = (float) Math.sin(angleRad);
        float[] a = new float[16];
        a[0] = c; a[1] = -s; a[4] = s; a[5] = c; a[10] = 1f; a[15] = 1f;
        return new Matrix4f(a);
    }

    public static Matrix4f perspective(float fovYRad, float aspect, float near, float far) {
        float f = (float) (1.0 / Math.tan(fovYRad / 2.0));
        float[] a = new float[16];
        a[0] = f / aspect;
        a[5] = f;
        a[10] = (far + near) / (near - far);
        a[11] = (2f * far * near) / (near - far);
        a[14] = -1f;
        return new Matrix4f(a);
    }

    public static Matrix4f lookAt(Vector3fImpl eye, Vector3fImpl center, Vector3fImpl up) {
        Vector3fImpl f = center.sub(eye).normalize();
        Vector3fImpl s = f.cross(up).normalize();
        Vector3fImpl u = s.cross(f);

        float[] a = new float[16];
        // Row-major storage + column-vector convention.
        // Put basis vectors into COLUMNS and translation into the last column.
        a[0] = s.getX();   a[1] = s.getY();   a[2] = s.getZ();   a[3] = -s.dot(eye);
        a[4] = u.getX();   a[5] = u.getY();   a[6] = u.getZ();   a[7] = -u.dot(eye);
        a[8] = -f.getX();  a[9] = -f.getY();  a[10] = -f.getZ(); a[11] = f.dot(eye);
        a[12] = 0f;        a[13] = 0f;        a[14] = 0f;       a[15] = 1f;
        return new Matrix4f(a);
    }

    public float[] toArray() {
        return m.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Matrix4f{");
        for (int i = 0; i < 16; i++) {
            sb.append(m[i]);
            if (i < 15) sb.append(',');
        }
        sb.append('}');
        return sb.toString();
    }
}
