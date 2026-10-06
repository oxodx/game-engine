package nl.oxod.gameengine.engine;

import imgui.ImGui;
import imgui.ImFontConfig;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.IOException;
import java.io.InputStream;

public class ImGuiLayer {
  private final long glfwWindow;
  private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
  private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

  public ImGuiLayer(long glfwWindow) {
    this.glfwWindow = glfwWindow;
  }

  public void initImGui() {
    byte[] fontData;
    try (InputStream font = ImGuiLayer.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
      if (font == null) throw new IllegalStateException("Missing UI font: NotoSans-Regular.ttf");
      fontData = font.readAllBytes();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to load UI font", e);
    }

    ImGui.createContext();
    ImFontConfig fontConfig = new ImFontConfig();
    try {
      // Let ImGui copy the Java-owned data rather than free JNI array memory.
      fontConfig.setFontDataOwnedByAtlas(false);
      ImGui.getIO().getFonts().addFontFromMemoryTTF(fontData, 18.0f, fontConfig);
    } finally {
      fontConfig.destroy();
    }

    imGuiGlfw.init(glfwWindow, true);
    imGuiGl3.init("#version 330 core");
  }

  public void update(float dt) {
    imGuiGl3.newFrame();
    imGuiGlfw.newFrame();

    ImGui.newFrame();
    ImGui.showDemoWindow();
    ImGui.render();

    imGuiGl3.renderDrawData(ImGui.getDrawData());
  }

  public void destroyImGui() {
    imGuiGl3.shutdown();
    imGuiGlfw.shutdown();
    ImGui.destroyContext();
  }
}
