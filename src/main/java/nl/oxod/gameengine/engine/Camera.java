package nl.oxod.gameengine.engine;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {
  private final Matrix4f projectionMatrix;
  private final Matrix4f viewMatrix;
  public Vector2f position;

  public Camera(Vector2f position) {
    this.position = position;
    this.projectionMatrix = new Matrix4f();
    this.viewMatrix = new Matrix4f();
    adjustProjection();
  }

  public void adjustProjection() {
    projectionMatrix.identity();
    projectionMatrix.ortho(0.0f, 32.0f * 40.0f, 0.0f, 32.0f * 21.0f, 0.0f, 100.0f);
  }

  public Matrix4f getViewMatrix() {
    viewMatrix.identity().lookAt(
        position.x, position.y, 20.0f,
        position.x, position.y, -1.0f,
        0.0f, 1.0f, 0.0f);
    return this.viewMatrix;
  }

  public Matrix4f getProjectionMatrix() {
    return this.projectionMatrix;
  }
}
