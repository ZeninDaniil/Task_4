package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;

import com.cgvsu.math.matrix.impl.Matrix4f;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    public Vector3f translation = new Vector3f(0f, 0f, 0f);
    public Vector3f rotation = new Vector3f(0f, 0f, 0f);
    public float scale = 1f;

    public Matrix4f getModelMatrix() {
        Matrix4f s = Matrix4f.scale(scale, scale, scale);
        Matrix4f rx = Matrix4f.rotationX(rotation.x);
        Matrix4f ry = Matrix4f.rotationY(rotation.y);
        Matrix4f rz = Matrix4f.rotationZ(rotation.z);
        Matrix4f t = Matrix4f.translation(translation.x, translation.y, translation.z);

        return (Matrix4f) t.multiply(rz).multiply(ry).multiply(rx).multiply(s);
    }
}
