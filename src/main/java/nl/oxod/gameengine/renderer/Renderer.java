package nl.oxod.gameengine.renderer;

import nl.oxod.gameengine.components.SpriteRenderer;
import nl.oxod.gameengine.engine.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Renderer {
  private static final int MAX_BATCH_SIZE = 1000;
  private final List<RenderBatch> batches;

  public Renderer() {
    this.batches = new ArrayList<>();
  }

  public void add(GameObject go) {
    SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
    if (spr != null) add(spr);
  }

  public void add(SpriteRenderer sprite) {
    boolean added = false;
    for (RenderBatch batch : batches) {
      if (batch.hasRoom() && batch.getZIndex() == sprite.gameObject.getZIndex()) {
        Texture tex = sprite.getTexture();
        if (tex == null || (batch.hasTexture(tex) || batch.hasTextureRoom())) {
          batch.addSprite(sprite);
          added = true;
          break;
        }
      }
    }

    if (!added) {
      RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE, sprite.gameObject.getZIndex());
      newBatch.start();
      batches.add(newBatch);
      newBatch.addSprite(sprite);
      Collections.sort(batches);
    }
  }

  public void render() {
    for (RenderBatch batch : batches) {
      batch.render();
    }
  }
}
