package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.scene.SceneManager;
import com.cgvsu.util.ErrorHandler;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import com.cgvsu.math.vector.impl.Vector3fImpl;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;

import java.util.HashSet;
import java.util.Set;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float MODEL_TRANSLATION = 1.0F;
    final private float MODEL_ROTATION = 0.1F; // radians
    final private float MODEL_SCALE_STEP = 0.1F;

    @FXML
    BorderPane borderPane;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private ListView<SceneManager.SceneModel> modelsListView;

    @FXML
    private Label modelInfoLabel;

    @FXML
    private ToggleButton themeToggle;

    @FXML
    private VBox sidePanel;

    private SceneManager sceneManager = new SceneManager();
    private boolean isDarkTheme = false;

    private Camera camera = new Camera(
        new Vector3fImpl(0, 0, 100),
        new Vector3fImpl(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private boolean mouseLookActive = false;
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private float mouseSensitivity = 0.005f;
    private float flySpeed = 1.0f;

    private long lastFrameNs = 0L;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // Настройка ListView для моделей
        modelsListView.setCellFactory(param -> new ListCell<SceneManager.SceneModel>() {
            @Override
            protected void updateItem(SceneManager.SceneModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getName() + " (ID: " + item.getId() + ")");
                    if (sceneManager.getActiveModel() == item) {
                        setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            long now = System.nanoTime();
            float dt = (lastFrameNs == 0L) ? 0.015f : (now - lastFrameNs) / 1_000_000_000.0f;
            lastFrameNs = now;

            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            if (sceneManager.hasModels()) {
                RenderEngine.renderScene(canvas.getGraphicsContext2D(), camera, sceneManager, (int) width, (int) height);
            }

            updateModelInfo();
            handleContinuousInput(dt);

        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        setupFpsControls();
        updateModelsList();
        applyTheme();

        anchorPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;

            newScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
                if (pressedKeys.add(e.getCode())) {
                }
            });
            newScene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
                if (pressedKeys.remove(e.getCode())) {
                }
            });

            newScene.addEventHandler(ScrollEvent.SCROLL, e -> {
                float k = (float) (e.getDeltaY() > 0 ? 1.1 : 0.9);
                flySpeed = Math.max(0.05f, Math.min(50f, flySpeed * k));
            });

            anchorPane.requestFocus();
        });
    }

    private void setupFpsControls() {
        anchorPane.setFocusTraversable(true);

        anchorPane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                mouseLookActive = true;
                lastMouseX = e.getSceneX();
                lastMouseY = e.getSceneY();
            }
            anchorPane.requestFocus();
        });

        anchorPane.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                mouseLookActive = false;
            }
        });

        anchorPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!mouseLookActive) return;

            double dx = e.getSceneX() - lastMouseX;
            double dy = e.getSceneY() - lastMouseY;
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();

            camera.addYawPitch((float) (dx * mouseSensitivity), (float) (dy * mouseSensitivity));
        });

        anchorPane.addEventHandler(ScrollEvent.SCROLL, e -> {
            float k = (float) (e.getDeltaY() > 0 ? 1.1 : 0.9);
            flySpeed = Math.max(0.05f, Math.min(50f, flySpeed * k));
        });
    }

    private void handleContinuousInput(final float dt) {
        float step = TRANSLATION * flySpeed * (dt / 0.015f);
        boolean fast = pressedKeys.contains(KeyCode.SHIFT);
        if (fast) step *= 3.0f;

        if (pressedKeys.contains(KeyCode.W)) camera.moveForward(step);
        if (pressedKeys.contains(KeyCode.S)) camera.moveForward(-step);
        if (pressedKeys.contains(KeyCode.D)) camera.moveRight(step);
        if (pressedKeys.contains(KeyCode.A)) camera.moveRight(-step);

        if (pressedKeys.contains(KeyCode.SPACE)) camera.moveUp(step);
        if (pressedKeys.contains(KeyCode.CONTROL)) camera.moveUp(-step);
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog(getStage());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);
            String modelName = file.getName();
            sceneManager.addModel(model, modelName);
            updateModelsList();
            ErrorHandler.showInfo("Success", "Model loaded successfully: " + modelName);
        } catch (ObjReaderException e) {
            ErrorHandler.showException(e);
        } catch (IOException e) {
            ErrorHandler.showError("File Error", "Failed to read file: " + e.getMessage());
        } catch (Exception e) {
            ErrorHandler.showException(e);
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            ErrorHandler.showError("No Model Selected", "Please select a model to save.");
            return;
        }
        saveModel(activeModel.getModel(), false);
    }

    @FXML
    private void onSaveModelOriginalMenuItemClick() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            ErrorHandler.showError("No Model Selected", "Please select a model to save.");
            return;
        }
        saveModel(activeModel.getModel(), false);
    }

    @FXML
    private void onSaveModelTransformedMenuItemClick() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            ErrorHandler.showError("No Model Selected", "Please select a model to save.");
            return;
        }
        saveModel(activeModel.getModel(), true);
    }

    private void saveModel(final Model model, final boolean applyTransform) {
        if (model == null) {
            ErrorHandler.showError("No Model", "No model to save.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle(applyTransform ? "Save Model (With Transform)" : "Save Model (Original)");

        File file = fileChooser.showSaveDialog(getStage());
        if (file == null) {
            return;
        }

        try {
            String content = ObjWriter.write(model, applyTransform);
            Files.writeString(Path.of(file.getAbsolutePath()), content, StandardCharsets.UTF_8);
            ErrorHandler.showInfo("Success", "Model saved successfully.");
        } catch (IOException e) {
            ErrorHandler.showError("Save Error", "Failed to save file: " + e.getMessage());
        } catch (Exception e) {
            ErrorHandler.showException(e);
        }
    }

    @FXML
    public void onModelListClick(MouseEvent event) {
        SceneManager.SceneModel selected = modelsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sceneManager.setActiveModel(selected);
            updateModelsList();
        }
    }

    @FXML
    public void onDeleteModel() {
        SceneManager.SceneModel selected = modelsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorHandler.showError("No Selection", "Please select a model to delete.");
            return;
        }

        if (ErrorHandler.showConfirmation("Delete Model", "Are you sure?", 
                "Do you want to delete model: " + selected.getName() + "?")) {
            sceneManager.removeModel(selected);
            updateModelsList();
        }
    }

    private void updateModelsList() {
        ObservableList<SceneManager.SceneModel> items = FXCollections.observableArrayList(sceneManager.getModels());
        modelsListView.setItems(items);
        if (!items.isEmpty() && sceneManager.getActiveModel() != null) {
            modelsListView.getSelectionModel().select(sceneManager.getActiveModel());
        }
    }

    private void updateModelInfo() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            modelInfoLabel.setText("No model selected");
            return;
        }

        Model model = activeModel.getModel();
        StringBuilder info = new StringBuilder();
        info.append("Name: ").append(activeModel.getName()).append("\n");
        info.append("Vertices: ").append(model.vertices.size()).append("\n");
        info.append("Polygons: ").append(model.polygons.size()).append("\n");
        info.append("Translation: (").append(String.format("%.2f", model.translation.x))
            .append(", ").append(String.format("%.2f", model.translation.y))
            .append(", ").append(String.format("%.2f", model.translation.z)).append(")\n");
        info.append("Rotation: (").append(String.format("%.2f", model.rotation.x))
            .append(", ").append(String.format("%.2f", model.rotation.y))
            .append(", ").append(String.format("%.2f", model.rotation.z)).append(")\n");
        info.append("Scale: ").append(String.format("%.2f", model.scale));
        
        modelInfoLabel.setText(info.toString());
    }

    @FXML
    public void onDeleteSelectedVertex() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            ErrorHandler.showError("No Model Selected", "Please select a model first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Vertex");
        dialog.setHeaderText("Enter vertex index to delete");
        dialog.setContentText("Index:");

        dialog.showAndWait().ifPresent(indexStr -> {
            try {
                int index = Integer.parseInt(indexStr);
                Model model = activeModel.getModel();
                if (index < 0 || index >= model.vertices.size()) {
                    ErrorHandler.showError("Invalid Index", "Vertex index out of range.");
                    return;
                }
                model.removeVertex(index);
                ErrorHandler.showInfo("Success", "Vertex deleted successfully.");
            } catch (NumberFormatException e) {
                ErrorHandler.showError("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    public void onDeleteSelectedPolygon() {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) {
            ErrorHandler.showError("No Model Selected", "Please select a model first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Polygon");
        dialog.setHeaderText("Enter polygon index to delete");
        dialog.setContentText("Index:");

        dialog.showAndWait().ifPresent(indexStr -> {
            try {
                int index = Integer.parseInt(indexStr);
                Model model = activeModel.getModel();
                if (index < 0 || index >= model.polygons.size()) {
                    ErrorHandler.showError("Invalid Index", "Polygon index out of range.");
                    return;
                }
                model.removePolygon(index);
                ErrorHandler.showInfo("Success", "Polygon deleted successfully.");
            } catch (NumberFormatException e) {
                ErrorHandler.showError("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    @FXML
    public void onToggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
    }

    private void applyTheme() {
        Scene scene = borderPane.getScene();
        if (scene == null) return;

        if (isDarkTheme) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/cgvsu/styles/dark-theme.css").toExternalForm());
            themeToggle.setText("Light Theme");
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/com/cgvsu/styles/light-theme.css").toExternalForm());
            themeToggle.setText("Dark Theme");
        }
    }

    private Stage getStage() {
        Scene scene = borderPane.getScene();
        if (scene != null) {
            return (Stage) scene.getWindow();
        }
        // Fallback на canvas, если borderPane еще не добавлен в сцену
        if (canvas.getScene() != null) {
            return (Stage) canvas.getScene().getWindow();
        }
        return null;
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3fImpl(0, -TRANSLATION, 0));
    }

    @FXML
    public void handleModelReset(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        Model model = activeModel.getModel();
        model.translation = new com.cgvsu.math.Vector3f(0f, 0f, 0f);
        model.rotation = new com.cgvsu.math.Vector3f(0f, 0f, 0f);
        model.scale = 1f;
    }

    @FXML
    public void handleModelTranslateXPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.x += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateXMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.x -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateYPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.y += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateYMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.y -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateZPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.z += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateZMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().translation.z -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelRotateXPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.x += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateXMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.x -= MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateYPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.y += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateYMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.y -= MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateZPlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.z += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateZMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().rotation.z -= MODEL_ROTATION;
    }

    @FXML
    public void handleModelScalePlus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().scale += MODEL_SCALE_STEP;
    }

    @FXML
    public void handleModelScaleMinus(ActionEvent actionEvent) {
        SceneManager.SceneModel activeModel = sceneManager.getActiveModel();
        if (activeModel == null) return;
        activeModel.getModel().scale = Math.max(0.01f, activeModel.getModel().scale - MODEL_SCALE_STEP);
    }
}
