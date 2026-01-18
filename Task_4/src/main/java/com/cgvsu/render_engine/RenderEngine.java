package com.cgvsu.render_engine;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import static com.cgvsu.render_engine.GraphicConveyor.*;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector2fImpl;
import com.cgvsu.math.vector.impl.Vector3fImpl;
import com.cgvsu.scene.SceneManager;

public class RenderEngine {

    public static boolean useRasterization = true;
    public static boolean useWireframe = true;
    private static ZBuffer zBuffer;
    private static WritableImage frameBuffer;
    private static Color fillColor = Color.LIGHTGRAY;
    private static float ambientStrength = 0.3f;

    public static void setUseRasterization(boolean enabled) {
        useRasterization = enabled;
    }

    public static void setUseWireframe(boolean enabled) {
        useWireframe = enabled;
    }

    public static void setFillColor(Color color) {
        fillColor = color;
    }

    public static void setAmbientStrength(float strength) {
        ambientStrength = Math.max(0, Math.min(1, strength));
    }

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {
        // Инициализация буферов
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
            frameBuffer = new WritableImage(width, height);
        }
        
        zBuffer.clear();
        
        if (useRasterization) {
            renderModelRasterized(graphicsContext, camera, mesh, width, height);
        } else {
            renderModel(graphicsContext, camera, mesh, width, height);
        }
    }

    public static void renderScene(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final SceneManager sceneManager,
            final int width,
            final int height)
    {
        // Инициализация буферов
        if (zBuffer == null || zBuffer.getWidth() != width || zBuffer.getHeight() != height) {
            zBuffer = new ZBuffer(width, height);
            frameBuffer = new WritableImage(width, height);
        }
        
        zBuffer.clear();
        
        List<SceneManager.SceneModel> models = sceneManager.getModels();
        for (SceneManager.SceneModel sceneModel : models) {
            if (useRasterization) {
                renderModelRasterized(graphicsContext, camera, sceneModel.getModel(), width, height);
            } else {
                renderModel(graphicsContext, camera, sceneModel.getModel(), width, height);
            }
        }
    }

    private static void renderModel(
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

    private static void renderModelRasterized(
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

        Matrix4f modelViewMatrix = (Matrix4f) viewMatrix.multiply(modelMatrix);

        // Направление света (от камеры)
        Vector3f lightDir = new Vector3f(0, 0, 1);
        lightDir.normalize();

        PixelWriter pixelWriter = frameBuffer.getPixelWriter();
        WritableImage texture = TextureManager.getInstance().getCurrentTexture();

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            Polygon polygon = mesh.polygons.get(polygonInd);
            ArrayList<Integer> vertexIndices = polygon.getVertexIndices();

            // Обрабатываем только треугольники
            if (vertexIndices.size() != 3) {
                continue;
            }

            // Получаем вершины треугольника
            Vector3f[] screenVertices = new Vector3f[3];
            Vector3f[] normals = new Vector3f[3];
            Vector2f[] texCoords = new Vector2f[3];

            boolean hasNormals = !mesh.normals.isEmpty() && !polygon.getNormalIndices().isEmpty();
            boolean hasTexCoords = !mesh.textureVertices.isEmpty() && !polygon.getTextureVertexIndices().isEmpty();

            for (int i = 0; i < 3; i++) {
                // Преобразование вершины в экранные координаты
                com.cgvsu.math.Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));
                Vector3fImpl v = new Vector3fImpl(vertex.x, vertex.y, vertex.z);
                Vector3fImpl ndc = multiplyMatrix4ByPoint(modelViewProjectionMatrix, v);
                Vector2fImpl screen = vertexToPoint(ndc, width, height);
                
                screenVertices[i] = new Vector3f(screen.getX(), screen.getY(), ndc.getZ());

                // Получение нормали
                if (hasNormals) {
                    int normalIndex = polygon.getNormalIndices().get(i);
                    com.cgvsu.math.Vector3f normal = mesh.normals.get(normalIndex);
                    
                    // Преобразуем нормаль в пространство камеры
                    Vector3fImpl normalVec = new Vector3fImpl(normal.x, normal.y, normal.z);
                    Vector3fImpl transformedNormal = multiplyMatrix4ByVector(modelViewMatrix, normalVec);
                    normals[i] = new Vector3f(transformedNormal.getX(), transformedNormal.getY(), transformedNormal.getZ());
                } else {
                    normals[i] = new Vector3f(0, 0, 1);
                }

                // Получение текстурных координат
                if (hasTexCoords) {
                    int texIndex = polygon.getTextureVertexIndices().get(i);
                    com.cgvsu.math.Vector2f texCoord = mesh.textureVertices.get(texIndex);
                    texCoords[i] = new Vector2f(texCoord.x, texCoord.y);
                } else {
                    texCoords[i] = null;
                }
            }

            // Растеризация треугольника
            Rasterizer.rasterizeTriangle(
                    screenVertices[0], screenVertices[1], screenVertices[2],
                    normals[0], normals[1], normals[2],
                    texCoords[0], texCoords[1], texCoords[2],
                    fillColor,
                    zBuffer,
                    pixelWriter,
                    texture,
                    lightDir,
                    ambientStrength
            );
        }

        // Отображение растеризованного изображения
        graphicsContext.drawImage(frameBuffer, 0, 0);

        // Опционально: рисуем каркас поверх
        if (useWireframe) {
            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.setLineWidth(1);
            renderModel(graphicsContext, camera, mesh, width, height);
        }
    }

    private static Vector3fImpl multiplyMatrix4ByVector(Matrix4f matrix, Vector3fImpl vector) {
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();

        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = matrix.get(i, 0) * x + matrix.get(i, 1) * y + matrix.get(i, 2) * z;
        }

        return new Vector3fImpl(result[0], result[1], result[2]);
    }
}
