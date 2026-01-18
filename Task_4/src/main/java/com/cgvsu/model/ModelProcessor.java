package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.ArrayList;

public class ModelProcessor {

    public static void triangulate(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>();
        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
            ArrayList<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
            ArrayList<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() == 3) {
                // Уже треугольник
                newPolygons.add(polygon);
            } else if (vertexIndices.size() > 3) {
                // Триангулируем
                for (int i = 1; i < vertexIndices.size() - 1; i++) {
                    Polygon triangle = new Polygon();
                    ArrayList<Integer> triVertexIndices = new ArrayList<>();
                    triVertexIndices.add(vertexIndices.get(0));
                    triVertexIndices.add(vertexIndices.get(i));
                    triVertexIndices.add(vertexIndices.get(i + 1));
                    triangle.setVertexIndices(triVertexIndices);

                    if (!textureVertexIndices.isEmpty()) {
                        ArrayList<Integer> triTextureIndices = new ArrayList<>();
                        triTextureIndices.add(textureVertexIndices.get(0));
                        triTextureIndices.add(textureVertexIndices.get(i));
                        triTextureIndices.add(textureVertexIndices.get(i + 1));
                        triangle.setTextureVertexIndices(triTextureIndices);
                    }

                    if (!normalIndices.isEmpty()) {
                        ArrayList<Integer> triNormalIndices = new ArrayList<>();
                        triNormalIndices.add(normalIndices.get(0));
                        triNormalIndices.add(normalIndices.get(i));
                        triNormalIndices.add(normalIndices.get(i + 1));
                        triangle.setNormalIndices(triNormalIndices);
                    }

                    newPolygons.add(triangle);
                }
            }
        }
        model.polygons = newPolygons;
    }

    public static void calculateNormals(Model model) {
        // Очищаем существующие нормали
        model.normals.clear();

        // Вычисляем нормали для каждого полигона (грани)
        ArrayList<Vector3f> faceNormals = new ArrayList<>();
        for (Polygon polygon : model.polygons) {
            Vector3f normal = calculateFaceNormal(polygon, model.vertices);
            faceNormals.add(normal);
        }

        // Для каждой вершины собираем нормали граней
        ArrayList<ArrayList<Vector3f>> vertexNormals = new ArrayList<>();
        for (int i = 0; i < model.vertices.size(); i++) {
            vertexNormals.add(new ArrayList<>());
        }

        for (int i = 0; i < model.polygons.size(); i++) {
            Polygon polygon = model.polygons.get(i);
            Vector3f faceNormal = faceNormals.get(i);
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                vertexNormals.get(vertexIndex).add(faceNormal);
            }
        }

        // Усредняем нормали для каждой вершины
        for (ArrayList<Vector3f> normals : vertexNormals) {
            Vector3f averageNormal = averageNormals(normals);
            model.normals.add(averageNormal);
        }

        // Обновляем индексы нормалей в полигонах
        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> normalIndices = new ArrayList<>();
            for (Integer vertexIndex : polygon.getVertexIndices()) {
                normalIndices.add(vertexIndex); // Индекс нормали совпадает с индексом вершины
            }
            polygon.setNormalIndices(normalIndices);
        }
    }

    private static Vector3f calculateFaceNormal(Polygon polygon, ArrayList<Vector3f> vertices) {
        ArrayList<Integer> vertexIndices = polygon.getVertexIndices();
        if (vertexIndices.size() < 3) {
            return new Vector3f(0, 0, 0);
        }

        Vector3f v0 = vertices.get(vertexIndices.get(0));
        Vector3f v1 = vertices.get(vertexIndices.get(1));
        Vector3f v2 = vertices.get(vertexIndices.get(2));

        Vector3f edge1 = Vector3f.subtract(v1, v0);
        Vector3f edge2 = Vector3f.subtract(v2, v0);

        Vector3f normal = Vector3f.cross(edge1, edge2);
        normal.normalize();
        return normal;
    }

    private static Vector3f averageNormals(ArrayList<Vector3f> normals) {
        if (normals.isEmpty()) {
            return new Vector3f(0, 0, 0);
        }

        Vector3f sum = new Vector3f(0, 0, 0);
        for (Vector3f normal : normals) {
            sum = Vector3f.add(sum, normal);
        }
        sum.normalize();
        return sum;
    }
}