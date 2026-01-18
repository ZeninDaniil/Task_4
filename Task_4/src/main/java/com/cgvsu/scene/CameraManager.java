package com.cgvsu.scene;

import com.cgvsu.math.vector.impl.Vector3fImpl;
import com.cgvsu.render_engine.Camera;

import java.util.ArrayList;
import java.util.List;

public class CameraManager {
    private final List<SceneCamera> cameras = new ArrayList<>();
    private SceneCamera activeCamera = null;
    private int nextId = 0;

    public static class SceneCamera {
        private final Camera camera;
        private final String name;
        private int id;
        private boolean visible = true; // Видима ли камера как 3D объект на сцене

        public SceneCamera(Camera camera, String name, int id) {
            this.camera = camera;
            this.name = name;
            this.id = id;
        }

        public Camera getCamera() {
            return camera;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }

    public CameraManager() {
        // Создаем камеру по умолчанию
        Camera defaultCamera = new Camera(
            new Vector3fImpl(0, 0, 100),
            new Vector3fImpl(0, 0, 0),
            1.0F, 1, 0.01F, 100
        );
        addCamera(defaultCamera, "Камера 1");
    }

    public void addCamera(Camera camera, String name) {
        SceneCamera sceneCamera = new SceneCamera(camera, name, nextId++);
        cameras.add(sceneCamera);
        if (activeCamera == null) {
            activeCamera = sceneCamera;
            // Активная камера не видима как 3D объект
            sceneCamera.setVisible(false);
        }
    }

    public void removeCamera(SceneCamera sceneCamera) {
        if (cameras.size() <= 1) {
            // Нельзя удалить последнюю камеру
            return;
        }
        
        cameras.remove(sceneCamera);
        if (activeCamera == sceneCamera) {
            // Переключаемся на первую доступную камеру
            activeCamera = cameras.get(0);
            updateCameraVisibility();
        }
    }

    public void setActiveCamera(SceneCamera sceneCamera) {
        if (cameras.contains(sceneCamera)) {
            activeCamera = sceneCamera;
            updateCameraVisibility();
        }
    }

    private void updateCameraVisibility() {
        // Активная камера не отображается как 3D объект, все остальные - да
        for (SceneCamera cam : cameras) {
            cam.setVisible(cam != activeCamera);
        }
    }

    public SceneCamera getActiveCamera() {
        return activeCamera;
    }

    public Camera getActiveCameraObject() {
        return activeCamera != null ? activeCamera.getCamera() : null;
    }

    public List<SceneCamera> getCameras() {
        return new ArrayList<>(cameras);
    }

    public List<SceneCamera> getVisibleCameras() {
        List<SceneCamera> visible = new ArrayList<>();
        for (SceneCamera cam : cameras) {
            if (cam.isVisible()) {
                visible.add(cam);
            }
        }
        return visible;
    }

    public boolean hasCameras() {
        return !cameras.isEmpty();
    }

    public int getCameraCount() {
        return cameras.size();
    }
}
