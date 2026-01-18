package com.cgvsu.scene;

import com.cgvsu.model.Model;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {
    private final List<SceneModel> models = new ArrayList<>();
    private SceneModel activeModel = null;

    public static class SceneModel {
        private final Model model;
        private final String name;
        private int id;

        public SceneModel(Model model, String name, int id) {
            this.model = model;
            this.name = name;
            this.id = id;
        }

        public Model getModel() {
            return model;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    public void addModel(Model model, String name) {
        int newId = models.size();
        SceneModel sceneModel = new SceneModel(model, name, newId);
        models.add(sceneModel);
        if (activeModel == null) {
            activeModel = sceneModel;
        }
    }

    public void removeModel(SceneModel sceneModel) {
        models.remove(sceneModel);
        if (activeModel == sceneModel) {
            activeModel = models.isEmpty() ? null : models.get(0);
        }
        // Обновляем ID после удаления
        for (int i = 0; i < models.size(); i++) {
            models.get(i).id = i;
        }
    }

    public void setActiveModel(SceneModel sceneModel) {
        if (models.contains(sceneModel)) {
            activeModel = sceneModel;
        }
    }

    public SceneModel getActiveModel() {
        return activeModel;
    }

    public List<SceneModel> getModels() {
        return new ArrayList<>(models);
    }

    public boolean hasModels() {
        return !models.isEmpty();
    }
}
