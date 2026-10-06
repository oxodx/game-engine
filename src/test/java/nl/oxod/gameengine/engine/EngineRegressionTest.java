package nl.oxod.gameengine.engine;

import nl.oxod.gameengine.components.Sprite;
import nl.oxod.gameengine.components.SpriteRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.GLFW.*;

class EngineRegressionTest {
  @Test
  void unknownKeysAreIgnored() {
    assertDoesNotThrow(() -> KeyListener.keyCallback(0, GLFW_KEY_UNKNOWN, 0, GLFW_PRESS, 0));
    assertFalse(KeyListener.isKeyPressed(-1));
    assertFalse(KeyListener.isKeyPressed(Integer.MAX_VALUE));
  }

  @Test
  void keyPressAndReleaseAreTracked() {
    KeyListener.keyCallback(0, GLFW_KEY_A, 0, GLFW_PRESS, 0);
    assertTrue(KeyListener.isKeyPressed(GLFW_KEY_A));
    KeyListener.keyCallback(0, GLFW_KEY_A, 0, GLFW_RELEASE, 0);
    assertFalse(KeyListener.isKeyPressed(GLFW_KEY_A));
  }

  @Test
  void invalidMouseButtonsAreIgnored() {
    assertDoesNotThrow(() -> MouseListener.mouseButtonCallback(0, -1, GLFW_PRESS, 0));
    assertFalse(MouseListener.mouseButtonDown(-1));
    assertFalse(MouseListener.mouseButtonDown(Integer.MAX_VALUE));
  }

  @Test
  void releasingOneButtonPreservesDraggingWithAnotherHeld() {
    MouseListener.mouseButtonCallback(0, 0, GLFW_PRESS, 0);
    MouseListener.mouseButtonCallback(0, 1, GLFW_PRESS, 0);
    MouseListener.mousePosCallback(0, 10, 20);
    MouseListener.mouseButtonCallback(0, 0, GLFW_RELEASE, 0);
    assertTrue(MouseListener.isDragging());
    MouseListener.mouseButtonCallback(0, 1, GLFW_RELEASE, 0);
    assertFalse(MouseListener.isDragging());
  }

  @Test
  void objectsAddedToRunningSceneStartImmediately() {
    Scene scene = new Scene() {
      @Override
      public void update(float dt) {}
    };
    scene.start();
    int[] starts = {0};
    GameObject object = new GameObject("test");
    object.addComponent(new Component() {
      @Override
      public void start() { starts[0]++; }
    });
    scene.addGameObjectToScene(object);
    assertEquals(1, starts[0]);
  }

  @Test
  void newSpritesAndExplicitSpriteChangesRequireUploads() {
    Sprite sprite = new Sprite(null);
    SpriteRenderer renderer = new SpriteRenderer(sprite);
    assertTrue(renderer.isDirty());
    renderer.setClean();
    sprite.getTexCoords()[0].set(0.25f, 0.75f);
    renderer.setSprite(sprite);
    assertTrue(renderer.isDirty());
    renderer.setClean();
    renderer.setSprite(new Sprite(null));
    assertTrue(renderer.isDirty());
  }

  @Test
  void cameraViewTracksPositionAndPreservesOrientation() {
    Camera camera = new Camera(new Vector2f(10, 20));
    Matrix4f expected = new Matrix4f().translation(-10, -20, -20);
    assertTrue(expected.equals(camera.getViewMatrix(), 0.0001f));
    camera.position.set(-5, 7);
    assertTrue(new Matrix4f().translation(5, -7, -20).equals(camera.getViewMatrix(), 0.0001f));
  }
}
