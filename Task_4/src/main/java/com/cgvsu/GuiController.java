package com.cgvsu;

import com.cgvsu.render_engine.RenderEngine;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
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
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.Camera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.HashSet;
import java.util.Set;

public class GuiController {

    final private float TRANSLATION = 0.5F;
    final private float MODEL_TRANSLATION = 1.0F;
    final private float MODEL_ROTATION = 0.1F; // radians
    final private float MODEL_SCALE_STEP = 0.1F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Model mesh = null;

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

            if (mesh != null) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, mesh, (int) width, (int) height);
            }

            handleContinuousInput(dt);

        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        setupFpsControls();

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

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }

    @FXML
    private void onSaveModelOriginalMenuItemClick() {
        saveModel(false);
    }

    @FXML
    private void onSaveModelTransformedMenuItemClick() {
        saveModel(true);
    }

    private void saveModel(final boolean applyTransform) {
        if (mesh == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle(applyTransform ? "Save Model (With Transform)" : "Save Model (Original)");

        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        String content = ObjWriter.write(mesh, applyTransform);
        try {
            Files.writeString(Path.of(file.getAbsolutePath()), content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            // todo: show error dialog
        }
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
        if (mesh == null) return;
        mesh.translation = new com.cgvsu.math.Vector3f(0f, 0f, 0f);
        mesh.rotation = new com.cgvsu.math.Vector3f(0f, 0f, 0f);
        mesh.scale = 1f;
    }

    @FXML
    public void handleModelTranslateXPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.x += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateXMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.x -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateYPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.y += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateYMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.y -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateZPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.z += MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelTranslateZMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.translation.z -= MODEL_TRANSLATION;
    }

    @FXML
    public void handleModelRotateXPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.x += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateXMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.x -= MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateYPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.y += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateYMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.y -= MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateZPlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.z += MODEL_ROTATION;
    }

    @FXML
    public void handleModelRotateZMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.rotation.z -= MODEL_ROTATION;
    }

    // Scale (uniform)
    @FXML
    public void handleModelScalePlus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.scale += MODEL_SCALE_STEP;
    }

    @FXML
    public void handleModelScaleMinus(ActionEvent actionEvent) {
        if (mesh == null) return;
        mesh.scale = Math.max(0.01f, mesh.scale - MODEL_SCALE_STEP);
    }
}