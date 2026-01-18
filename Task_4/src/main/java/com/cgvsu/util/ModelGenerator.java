package com.cgvsu.util;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилита для создания простых 3D моделей
 */
public class ModelGenerator {

    /**
     * Создает модель камеры в виде усеченной пирамиды (frustum)
     * @return Model представляющая камеру
     */
    public static Model createCameraModel() {
        Model model = new Model();
        
        // Вершины усеченной пирамиды камеры
        // Ближняя плоскость (маленький прямоугольник)
        model.vertices.add(new Vector3f(-0.3f, 0.3f, 0.5f));   // 0 - верхний левый ближний
        model.vertices.add(new Vector3f(0.3f, 0.3f, 0.5f));    // 1 - верхний правый ближний
        model.vertices.add(new Vector3f(0.3f, -0.3f, 0.5f));   // 2 - нижний правый ближний
        model.vertices.add(new Vector3f(-0.3f, -0.3f, 0.5f));  // 3 - нижний левый ближний
        
        // Дальняя плоскость (большой прямоугольник)
        model.vertices.add(new Vector3f(-1.0f, 1.0f, -2.0f));   // 4 - верхний левый дальний
        model.vertices.add(new Vector3f(1.0f, 1.0f, -2.0f));    // 5 - верхний правый дальний
        model.vertices.add(new Vector3f(1.0f, -1.0f, -2.0f));   // 6 - нижний правый дальний
        model.vertices.add(new Vector3f(-1.0f, -1.0f, -2.0f));  // 7 - нижний левый дальний
        
        // Вершина для "объектива" камеры
        model.vertices.add(new Vector3f(0.0f, 0.0f, 1.0f));     // 8 - точка объектива
        
        // Полигоны
        // Ближняя плоскость
        model.polygons.add(createPolygon(0, 1, 2));
        model.polygons.add(createPolygon(0, 2, 3));
        
        // Дальняя плоскость
        model.polygons.add(createPolygon(4, 6, 5));
        model.polygons.add(createPolygon(4, 7, 6));
        
        // Боковые грани
        // Верхняя
        model.polygons.add(createPolygon(0, 5, 1));
        model.polygons.add(createPolygon(0, 4, 5));
        
        // Нижняя
        model.polygons.add(createPolygon(3, 2, 6));
        model.polygons.add(createPolygon(3, 6, 7));
        
        // Левая
        model.polygons.add(createPolygon(0, 3, 7));
        model.polygons.add(createPolygon(0, 7, 4));
        
        // Правая
        model.polygons.add(createPolygon(1, 5, 6));
        model.polygons.add(createPolygon(1, 6, 2));
        
        // Объектив (маленький конус)
        model.polygons.add(createPolygon(8, 1, 0));
        model.polygons.add(createPolygon(8, 2, 1));
        model.polygons.add(createPolygon(8, 3, 2));
        model.polygons.add(createPolygon(8, 0, 3));
        
        return model;
    }
    
    /**
     * Создает простой полигон с тремя вершинами
     */
    private static Polygon createPolygon(int v1, int v2, int v3) {
        Polygon polygon = new Polygon();
        ArrayList<Integer> vertexIndices = new ArrayList<>();
        vertexIndices.add(v1);
        vertexIndices.add(v2);
        vertexIndices.add(v3);
        polygon.setVertexIndices(vertexIndices);
        return polygon;
    }
    
    /**
     * Создает модель куба для тестирования
     */
    public static Model createCube(float size) {
        Model model = new Model();
        float half = size / 2;
        
        // 8 вершин куба
        model.vertices.add(new Vector3f(-half, -half, -half)); // 0
        model.vertices.add(new Vector3f(half, -half, -half));  // 1
        model.vertices.add(new Vector3f(half, half, -half));   // 2
        model.vertices.add(new Vector3f(-half, half, -half));  // 3
        model.vertices.add(new Vector3f(-half, -half, half));  // 4
        model.vertices.add(new Vector3f(half, -half, half));   // 5
        model.vertices.add(new Vector3f(half, half, half));    // 6
        model.vertices.add(new Vector3f(-half, half, half));   // 7
        
        // 12 треугольников (6 граней * 2 треугольника)
        // Передняя грань
        model.polygons.add(createPolygon(4, 5, 6));
        model.polygons.add(createPolygon(4, 6, 7));
        
        // Задняя грань
        model.polygons.add(createPolygon(1, 0, 3));
        model.polygons.add(createPolygon(1, 3, 2));
        
        // Верхняя грань
        model.polygons.add(createPolygon(7, 6, 2));
        model.polygons.add(createPolygon(7, 2, 3));
        
        // Нижняя грань
        model.polygons.add(createPolygon(0, 1, 5));
        model.polygons.add(createPolygon(0, 5, 4));
        
        // Левая грань
        model.polygons.add(createPolygon(0, 4, 7));
        model.polygons.add(createPolygon(0, 7, 3));
        
        // Правая грань
        model.polygons.add(createPolygon(5, 1, 2));
        model.polygons.add(createPolygon(5, 2, 6));
        
        return model;
    }
}
