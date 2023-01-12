package engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
    private Matrix4f projectionMatrix, viewMatrix;
    public Vector2f position;

    public Camera(Vector2f position) {
        this.position = position;

        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        adjustProjection();
    }

    /**
     * calculate projection matrix - how the camera is
     */
    public void adjustProjection() {
        projectionMatrix.identity();
        // right side of the screen will of 40 grid tile of 32 * 32 pixels, top will be 22.5 grid tiles, so this will be a
        // 40 * 22.5 rectangular orthographic camera (normalize 1920 * 1080 to 1:1)
        projectionMatrix.ortho(0.0f, 32.0f * 40.0f, 0.0f, 32.0f * 22.5f, 0.0f, 100.0f);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * calculate view matrix - where the camera is
     * @return viewMatrix
     */
    public Matrix4f getViewMatrix() {
        // define front, up direction for camera
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        viewMatrix.identity();
        viewMatrix = viewMatrix.lookAt(
                new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f),
                cameraUp);

        return viewMatrix;

    }
}
