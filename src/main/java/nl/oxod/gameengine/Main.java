package nl.oxod.gameengine;

import nl.oxod.gameengine.engine.Window;

public class Main {
  public static void main(String[] args) {
    Window window = Window.get();
    window.run();
  }
}
