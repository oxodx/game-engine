package nl.oxod.gameengine.util;

import nl.oxod.gameengine.components.Spritesheet;
import nl.oxod.gameengine.renderer.Shader;
import nl.oxod.gameengine.renderer.Texture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetPool {
  private static Map<String, Shader> shaders = new HashMap<>();
  private static Map<String, Texture> textures = new HashMap<>();
  private static Map<String, Spritesheet> spritesheets = new HashMap<>();

  public static Shader getShader(String resourceName) {
    File file = new File(resourceName);
    if (AssetPool.shaders.containsKey(file.getAbsolutePath())) {
      return AssetPool.shaders.get(file.getAbsolutePath());
    } else {
      Shader shader = new Shader(resourceName);
      shader.compile();
      AssetPool.shaders.put(file.getAbsolutePath(), shader);
      return shader;
    }
  }

  public static Texture getTexture(String resourceName) {
    File file = new File(resourceName);
    if (AssetPool.textures.containsKey(file.getAbsolutePath())) {
      return AssetPool.textures.get(file.getAbsolutePath());
    } else {
      Texture texture = new Texture(resourceName);
      AssetPool.textures.put(file.getAbsolutePath(), texture);
      return texture;
    }
  }

  public static void addSpritesheet(String resourceName, Spritesheet spritesheet) {
    File file = new File(resourceName);
    if (!AssetPool.spritesheets.containsKey(file.getAbsolutePath())) {
      AssetPool.spritesheets.put(file.getAbsolutePath(), spritesheet);
    }
  }

  public static Spritesheet getSpritesheet(String resourceName) {
    File file = new File(resourceName);
    assert AssetPool.spritesheets.containsKey(file.getAbsolutePath())
        : "Error: Tried to access spritesheet '" + resourceName + "' and it has not been added to the asset pool yet.";
    return AssetPool.spritesheets.getOrDefault(file.getAbsolutePath(), null);
  }
}
