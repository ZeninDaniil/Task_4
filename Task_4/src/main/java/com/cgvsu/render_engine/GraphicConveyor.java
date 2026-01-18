package com.cgvsu.render_engine;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector2fImpl;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import com.cgvsu.math.vector.impl.Vector4fImpl;

public class GraphicConveyor {

    private GraphicConveyor() {
    }

    public static Matrix4f lookAt(final Vector3fImpl eye, final Vector3fImpl target) {
        return lookAt(eye, target, new Vector3fImpl(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(final Vector3fImpl eye, final Vector3fImpl target, final Vector3fImpl up) {
        return Matrix4f.lookAt(eye, target, up);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        return Matrix4f.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    public static Vector3fImpl multiplyMatrix4ByPoint(final Matrix4f matrix, final Vector3fImpl point) {
        return matrix.transformPoint(point);
    }

    public static Vector4fImpl multiplyMatrix4ByVector4(final Matrix4f matrix, final Vector4fImpl v) {
        return matrix.transform(v);
    }

    public static Vector2fImpl vertexToPoint(final Vector3fImpl vertex, final int width, final int height) {
        return new Vector2fImpl(vertex.getX() * width + width / 2.0F, -vertex.getY() * height + height / 2.0F);
    }
}
