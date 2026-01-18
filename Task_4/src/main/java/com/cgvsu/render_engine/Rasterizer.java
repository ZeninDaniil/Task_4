package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Rasterizer {

    /**
     * Растеризация треугольника с использованием барицентрических координат
     * @param v0, v1, v2 - вершины треугольника (x, y, z)
     * @param n0, n1, n2 - нормали вершин
     * @param t0, t1, t2 - текстурные координаты (могут быть null)
     * @param color - цвет для заливки
     * @param zBuffer - Z-буфер для проверки глубины
     * @param pixelWriter - для записи пикселей
     * @param texture - текстура (может быть null)
     * @param lightDir - направление света (нормализованное)
     * @param ambientStrength - сила окружающего освещения (0-1)
     */
    public static void rasterizeTriangle(
            Vector3f v0, Vector3f v1, Vector3f v2,
            Vector3f n0, Vector3f n1, Vector3f n2,
            Vector2f t0, Vector2f t1, Vector2f t2,
            Color color,
            ZBuffer zBuffer,
            PixelWriter pixelWriter,
            WritableImage texture,
            Vector3f lightDir,
            float ambientStrength) {

        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.min(Math.min(v0.x, v1.x), v2.x));
        int maxX = (int) Math.min(zBuffer.getWidth() - 1, Math.max(Math.max(v0.x, v1.x), v2.x));
        int minY = (int) Math.max(0, Math.min(Math.min(v0.y, v1.y), v2.y));
        int maxY = (int) Math.min(zBuffer.getHeight() - 1, Math.max(Math.max(v0.y, v1.y), v2.y));

        // Для каждого пикселя в ограничивающем прямоугольнике
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Вычисляем барицентрические координаты
                Vector3f bary = barycentric(v0, v1, v2, x + 0.5f, y + 0.5f);

                // Проверяем, находится ли точка внутри треугольника
                if (bary.x < 0 || bary.y < 0 || bary.z < 0) {
                    continue;
                }

                // Интерполируем глубину
                float depth = v0.z * bary.x + v1.z * bary.y + v2.z * bary.z;

                // Проверяем Z-буфер
                if (!zBuffer.testAndSet(x, y, depth)) {
                    continue;
                }

                // Интерполируем нормаль
                Vector3f normal = interpolateVector(n0, n1, n2, bary);
                normal.normalize();

                // Вычисляем освещение (простая модель Ламберта)
                float diffuse = Math.max(0, Vector3f.dot(normal, lightDir));
                float lightIntensity = ambientStrength + (1 - ambientStrength) * diffuse;
                lightIntensity = Math.min(1.0f, lightIntensity);

                Color finalColor = color;

                // Если есть текстура, используем её
                if (texture != null && t0 != null && t1 != null && t2 != null) {
                    Vector2f texCoord = interpolateTexCoord(t0, t1, t2, bary);
                    finalColor = sampleTexture(texture, texCoord);
                }

                // Применяем освещение
                finalColor = new Color(
                        finalColor.getRed() * lightIntensity,
                        finalColor.getGreen() * lightIntensity,
                        finalColor.getBlue() * lightIntensity,
                        finalColor.getOpacity()
                );

                pixelWriter.setColor(x, y, finalColor);
            }
        }
    }

    /**
     * Вычисление барицентрических координат точки (px, py) относительно треугольника
     */
    private static Vector3f barycentric(Vector3f v0, Vector3f v1, Vector3f v2, float px, float py) {
        float x0 = v0.x, y0 = v0.y;
        float x1 = v1.x, y1 = v1.y;
        float x2 = v2.x, y2 = v2.y;

        float denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
        if (Math.abs(denom) < 1e-6f) {
            return new Vector3f(-1, -1, -1); // Вырожденный треугольник
        }

        float w0 = ((y1 - y2) * (px - x2) + (x2 - x1) * (py - y2)) / denom;
        float w1 = ((y2 - y0) * (px - x2) + (x0 - x2) * (py - y2)) / denom;
        float w2 = 1.0f - w0 - w1;

        return new Vector3f(w0, w1, w2);
    }

    /**
     * Интерполяция вектора по барицентрическим координатам
     */
    private static Vector3f interpolateVector(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f bary) {
        return new Vector3f(
                v0.x * bary.x + v1.x * bary.y + v2.x * bary.z,
                v0.y * bary.x + v1.y * bary.y + v2.y * bary.z,
                v0.z * bary.x + v1.z * bary.y + v2.z * bary.z
        );
    }

    /**
     * Интерполяция текстурных координат
     */
    private static Vector2f interpolateTexCoord(Vector2f t0, Vector2f t1, Vector2f t2, Vector3f bary) {
        return new Vector2f(
                t0.x * bary.x + t1.x * bary.y + t2.x * bary.z,
                t0.y * bary.x + t1.y * bary.y + t2.y * bary.z
        );
    }

    /**
     * Получение цвета из текстуры по текстурным координатам
     */
    private static Color sampleTexture(WritableImage texture, Vector2f texCoord) {
        int width = (int) texture.getWidth();
        int height = (int) texture.getHeight();

        // Wrap текстурных координат (повторение)
        float u = texCoord.x - (float) Math.floor(texCoord.x);
        float v = texCoord.y - (float) Math.floor(texCoord.y);

        int x = (int) (u * (width - 1));
        int y = (int) ((1.0f - v) * (height - 1)); // Инвертируем V

        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));

        return texture.getPixelReader().getColor(x, y);
    }
}
