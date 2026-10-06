package nl.oxod.gameengine.engine;

import nl.oxod.gameengine.components.SpriteRenderer;
import nl.oxod.gameengine.components.Spritesheet;
import nl.oxod.gameengine.util.AssetPool;
import org.joml.Vector2f;

public class LevelEditorScene extends Scene {
  private GameObject obj1;
  private Spritesheet sprites;

  public LevelEditorScene() {
  }

  @Override
  public void init() {
    loadResources();

    this.camera = new Camera(new Vector2f());

    sprites = AssetPool.getSpritesheet("src/main/resources/images/spritesheet.png");

    obj1 = new GameObject("Object 1",
        new Transform(new Vector2f(100, 100), new Vector2f(256, 256)), 0);
    obj1.addComponent(new SpriteRenderer(sprites.getSprite(0)));
    this.addGameObjectToScene(obj1);

    GameObject obj2 = new GameObject("Object 2",
        new Transform(new Vector2f(400, 100), new Vector2f(256, 256)), 0);
    obj2.addComponent(new SpriteRenderer(sprites.getSprite(1)));
    this.addGameObjectToScene(obj2);
  }

  private void loadResources() {
    AssetPool.getShader("src/main/resources/shaders/default.glsl");
    AssetPool.addSpritesheet("src/main/resources/images/spritesheet.png",
        new Spritesheet(AssetPool.getTexture("src/main/resources/images/spritesheet.png"),
            16, 16, 26, 0));
  }

  private int spriteIndex = 0;
  private static final float SPRITE_FLIP_TIME = 0.2f;
  private float spriteFlipTimeLeft = 0.0f;

  @Override
  public void update(float dt) {
    spriteFlipTimeLeft -= dt;
    if (spriteFlipTimeLeft <= 0) {
      spriteFlipTimeLeft = SPRITE_FLIP_TIME;
      spriteIndex++;
      if (spriteIndex > 4) spriteIndex = 0;
      obj1.getComponent(SpriteRenderer.class).setSprite(sprites.getSprite(spriteIndex));
    }

    for (GameObject go : this.gameObjects) {
      go.update(dt);
    }

    this.renderer.render();
  }
}
