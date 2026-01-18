package com.cgvsu.render_engine;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import com.cgvsu.model.Model;
import static com.cgvsu.render_engine.GraphicConveyor.*;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector2fImpl;
import com.cgvsu.math.vector.impl.Vector3fImpl;

public class RenderEngine {

    private static float[][] zBuffer;
    private static Color[][] colorBuffer;
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final boolean fillPolygons)
    {
        if (fillPolygons) {
            renderFilled(graphicsContext, camera, mesh, width, height);
        } else {
            renderWireframe(graphicsContext, camera, mesh, width, height);
        }
    }

    private static void renderFilled(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        // Инициализируем буферы только если размер изменился
        if (lastWidth != width || lastHeight != height) {
            zBuffer = new float[width][height];
            colorBuffer = new Color[width][height];
            lastWidth = width;
            lastHeight = height;
        } else {
            // Очищаем буферы
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    zBuffer[x][y] = Float.MAX_VALUE;
                    colorBuffer[x][y] = Color.WHITE;
                }
            }
        }

        Matrix4f modelMatrix = mesh.getModelMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = (Matrix4f) projectionMatrix
                .multiply(viewMatrix)
                .multiply(modelMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector3fImpl> screenPoints = new ArrayList<>();
            
            // Трансформируем вершины
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                com.cgvsu.math.Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                Vector3fImpl v = new Vector3fImpl(vertex.x, vertex.y, vertex.z);

                Vector3fImpl ndc = multiplyMatrix4ByPoint(modelViewProjectionMatrix, v);
                screenPoints.add(ndc);
            }

            // Растеризуем треугольники
            if (nVerticesInPolygon >= 3) {
                for (int i = 1; i < nVerticesInPolygon - 1; i++) {
                    rasterizeTriangle(screenPoints.get(0), screenPoints.get(i), screenPoints.get(i + 1), 
                                    width, height, Color.color(0.5, 0.7, 1.0)); // Светло-синий цвет
                }
            }
        }

        // Отрисовываем на холст
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (colorBuffer[x][y] != null && colorBuffer[x][y] != Color.WHITE) {
                    graphicsContext.getPixelWriter().setColor(x, y, colorBuffer[x][y]);
                }
            }
        }
    }

    private static void renderWireframe(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        Matrix4f modelMatrix = mesh.getModelMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = (Matrix4f) projectionMatrix
                .multiply(viewMatrix)
                .multiply(modelMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Vector2fImpl> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                com.cgvsu.math.Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
                Vector3fImpl v = new Vector3fImpl(vertex.x, vertex.y, vertex.z);

                Vector3fImpl ndc = multiplyMatrix4ByPoint(modelViewProjectionMatrix, v);
                Vector2fImpl screen = vertexToPoint(ndc, width, height);
                resultPoints.add(screen);
            }

            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.setLineWidth(1.0);
            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).getX(),
                        resultPoints.get(vertexInPolygonInd - 1).getY(),
                        resultPoints.get(vertexInPolygonInd).getX(),
                        resultPoints.get(vertexInPolygonInd).getY());
            }

            if (nVerticesInPolygon > 0)
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).getX(),
                        resultPoints.get(nVerticesInPolygon - 1).getY(),
                        resultPoints.get(0).getX(),
                        resultPoints.get(0).getY());
        }
    }

    private static void rasterizeTriangle(Vector3fImpl p0, Vector3fImpl p1, Vector3fImpl p2, 
                                         int width, int height, Color color) {
        // Конвертируем NDC координаты в экранные координаты
        Vector2fImpl v0 = vertexToPoint(p0, width, height);
        Vector2fImpl v1 = vertexToPoint(p1, width, height);
        Vector2fImpl v2 = vertexToPoint(p2, width, height);

        // Получаем Z координаты
        float z0 = p0.getZ();
        float z1 = p1.getZ();
        float z2 = p2.getZ();

        // Определяем bounding box
        int minX = (int) Math.max(0, Math.min(v0.getX(), Math.min(v1.getX(), v2.getX())));
        int maxX = (int) Math.min(width - 1, Math.max(v0.getX(), Math.max(v1.getX(), v2.getX())));
        int minY = (int) Math.max(0, Math.min(v0.getY(), Math.min(v1.getY(), v2.getY())));
        int maxY = (int) Math.min(height - 1, Math.max(v0.getY(), Math.max(v1.getY(), v2.getY())));

        // Растеризуем треугольник
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2fImpl p = new Vector2fImpl(x, y);
                
                // Вычисляем барицентрические координаты
                float[] baryCoords = getBarycentricCoordinates(p, v0, v1, v2);
                
                if (baryCoords != null && baryCoords[0] >= -1e-5 && baryCoords[1] >= -1e-5 && baryCoords[2] >= -1e-5) {
                    // Интерполируем Z значение
                    float z = baryCoords[0] * z0 + baryCoords[1] * z1 + baryCoords[2] * z2;
                    
                    // Проверяем Z-буффер
                    if (z < zBuffer[x][y]) {
                        zBuffer[x][y] = z;
                        colorBuffer[x][y] = color;
                    }
                }
            }
        }
    }

    private static float[] getBarycentricCoordinates(Vector2fImpl p, Vector2fImpl v0, Vector2fImpl v1, Vector2fImpl v2) {
        float x0 = v0.getX(), y0 = v0.getY();
        float x1 = v1.getX(), y1 = v1.getY();
        float x2 = v2.getX(), y2 = v2.getY();
        float x = p.getX(), y = p.getY();

        float denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
        
        if (Math.abs(denominator) < 1e-10) {
            return null; // Вырожденный треугольник
        }

        float a = ((y1 - y2) * (x - x2) + (x2 - x1) * (y - y2)) / denominator;
        float b = ((y2 - y0) * (x - x2) + (x0 - x2) * (y - y2)) / denominator;
        float c = 1.0f - a - b;

        return new float[]{a, b, c};
    }
}