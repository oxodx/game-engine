package nl.oxod.gameengine.engine;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
  private static KeyListener instance;
  private final boolean[] keyPressed = new boolean[350];

  private KeyListener() {
  }

  public static KeyListener get() {
    if (KeyListener.instance == null) KeyListener.instance = new KeyListener();
    return KeyListener.instance;
  }

  public static void keyCallback(long window, int key, int scancode, int action, int mods) {
    KeyListener listener = get();
    if (key < 0 || key >= listener.keyPressed.length) return;

    if (action == GLFW_PRESS) {
      listener.keyPressed[key] = true;
    } else if (action == GLFW_RELEASE) {
      listener.keyPressed[key] = false;
    }
  }

  public static boolean isKeyPressed(int keyCode) {
    KeyListener listener = get();
    return keyCode >= 0 && keyCode < listener.keyPressed.length && listener.keyPressed[keyCode];
  }
}
