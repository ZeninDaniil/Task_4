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

    public void removeVertex(int index) {
        if (index < 0 || index >= vertices.size()) {
            return;
        }
        vertices.remove(index);
        
        // Обновляем индексы в полигонах
        for (Polygon polygon : polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            for (int i = vertexIndices.size() - 1; i >= 0; i--) {
                int vertexIndex = vertexIndices.get(i);
                if (vertexIndex == index) {
                    vertexIndices.remove(i);
                } else if (vertexIndex > index) {
                    vertexIndices.set(i, vertexIndex - 1);
                }
            }
        }
        
        // Удаляем полигоны с менее чем 3 вершинами
        polygons.removeIf(p -> p.getVertexIndices().size() < 3);
    }

    public void removePolygon(int index) {
        if (index < 0 || index >= polygons.size()) {
            return;
        }
        polygons.remove(index);
    }

    public void removePolygons(ArrayList<Integer> indices) {
        // Сортируем по убыванию, чтобы удаление не влияло на индексы
        ArrayList<Integer> sortedIndices = new ArrayList<>(indices);
        sortedIndices.sort(Collections.reverseOrder());
        
        for (int index : sortedIndices) {
            if (index >= 0 && index < polygons.size()) {
                polygons.remove(index);
            }
        }
    }
}
