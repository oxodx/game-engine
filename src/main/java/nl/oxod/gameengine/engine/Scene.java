package nl.oxod.gameengine.engine;

import nl.oxod.gameengine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public abstract class Scene {
  protected Renderer renderer = new Renderer();
  protected Camera camera;
  private boolean isRunning = false;
  protected List<GameObject> gameObjects = new ArrayList<>();

  public Scene() {
  }

  public void init() {
  }

  public void start() {
    for (GameObject go : gameObjects) {
      go.start();
      this.renderer.add(go);
    }
    isRunning = true;
  }

  public void addGameObjectToScene(GameObject go) {
    gameObjects.add(go);
    if (isRunning) {
      go.start();
      this.renderer.add(go);
    }
  }

  public abstract void update(float dt);

  public Camera camera() {
    return this.camera;
  }
}
