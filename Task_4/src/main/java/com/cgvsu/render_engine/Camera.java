package com.cgvsu.render_engine;

import com.cgvsu.math.matrix.impl.Matrix4f;
import com.cgvsu.math.vector.impl.Vector3fImpl;

public class Camera {

    public Camera(
        final Vector3fImpl position,
        final Vector3fImpl target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;

        Vector3fImpl forward = target.sub(position).normalize();
        this.pitch = (float) Math.asin(clamp(forward.getY(), -1f, 1f));
        this.yaw = (float) Math.atan2(forward.getX(), forward.getZ());
        updateTargetFromAngles();
    }

    public void setPosition(final Vector3fImpl position) {
        this.position = position;
    }

    public void setTarget(final Vector3fImpl target) {
        this.target = target;

        Vector3fImpl forward = target.sub(position).normalize();
        this.pitch = (float) Math.asin(clamp(forward.getY(), -1f, 1f));
        this.yaw = (float) Math.atan2(forward.getX(), forward.getZ());
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Vector3fImpl getPosition() {
        return position;
    }

    public Vector3fImpl getTarget() {
        return target;
    }

    public void movePosition(final Vector3fImpl translation) {
        this.position = this.position.add(translation);
        updateTargetFromAngles();
    }

    public void moveTarget(final Vector3fImpl translation) {
        this.target = this.target.add(translation);
    }

    public void addYawPitch(final float deltaYaw, final float deltaPitch) {
        yaw += deltaYaw;
        pitch = clamp(pitch + deltaPitch, -MAX_PITCH, MAX_PITCH);
        updateTargetFromAngles();
    }

    public void moveForward(final float amount) {
        Vector3fImpl f = getForward();
        Vector3fImpl flat = new Vector3fImpl(f.getX(), 0f, f.getZ()).normalize();
        position = position.add(flat.scale(amount));
        updateTargetFromAngles();
    }

    public void moveRight(final float amount) {
        Vector3fImpl r = getRight();
        Vector3fImpl flat = new Vector3fImpl(r.getX(), 0f, r.getZ()).normalize();
        position = position.add(flat.scale(amount));
        updateTargetFromAngles();
    }

    public void moveUp(final float amount) {
        position = position.add(new Vector3fImpl(0f, amount, 0f));
        updateTargetFromAngles();
    }

    public Vector3fImpl getForward() {
        float cp = (float) Math.cos(pitch);
        float sp = (float) Math.sin(pitch);
        float cy = (float) Math.cos(yaw);
        float sy = (float) Math.sin(yaw);

        return new Vector3fImpl(sy * cp, sp, cy * cp).normalize();
    }

    public Vector3fImpl getRight() {
        Vector3fImpl up = new Vector3fImpl(0f, 1f, 0f);
        return getForward().cross(up).normalize();
    }

    private void updateTargetFromAngles() {
        float cp = (float) Math.cos(pitch);
        float sp = (float) Math.sin(pitch);
        float cy = (float) Math.cos(yaw);
        float sy = (float) Math.sin(yaw);

        Vector3fImpl forward = new Vector3fImpl(sy * cp, sp, cy * cp).normalize();
        this.target = this.position.add(forward);
    }

    private static float clamp(final float v, final float min, final float max) {
        return Math.max(min, Math.min(max, v));
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    private Vector3fImpl position;
    private Vector3fImpl target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    private float yaw;
    private float pitch;

    private static final float MAX_PITCH = (float) Math.toRadians(89.0);
}