package com.cgvsu.render_engine;

import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import com.cgvsu.model.Model;
import static com.cgvsu.render_engine.GraphicConveyor.*;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector2fImpl;
import com.cgvsu.math.vector.impl.Vector3fImpl;

public class RenderEngine {

    public static void render(
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
}