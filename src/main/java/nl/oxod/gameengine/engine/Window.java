package nl.oxod.gameengine.engine;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {
  private static Window instance = null;
  private static int width;
  private static int height;
  private String title;
  private long glfwWindow;
  private ImGuiLayer imguiLayer;

  private static Scene currentScene;

  private Window() {
    width = 1920;
    height = 1080;
    this.title = "Test game";
  }

  public static void changeScene(int newScene) {
    switch (newScene) {
      case 0:
        currentScene = new LevelEditorScene();
        currentScene.init();
        currentScene.start();
        break;
      case 1:
        currentScene = new LevelScene();
        currentScene.init();
        currentScene.start();
        break;
      default:
        assert false : "Unknown scene '" + newScene + "'";
        break;
    }
  }

  public static Window get() {
    if (Window.instance == null) Window.instance = new Window();
    return Window.instance;
  }

  public static Scene getScene() {
    return get().currentScene;
  }

  public void run() {
    System.out.println("Hello LWJGL " + Version.getVersion() + "!");

    init();
    loop();

    // Free the memory
    imguiLayer.destroyImGui();
    glfwFreeCallbacks(glfwWindow);
    glfwDestroyWindow(glfwWindow);

    // Terminate GLFW and free the error callback
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  public void init() {
    // Setup an error callback
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW
    if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW.");

    // Configure GLFW
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

    // Create the window
    glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
    if (glfwWindow == NULL) throw new IllegalStateException("Failed to create the GLFW window.");

    glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
    glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
    glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
    glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

    // Make the OpenGL context current
    glfwMakeContextCurrent(glfwWindow);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(glfwWindow);

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

    this.imguiLayer = new ImGuiLayer(glfwWindow);
    this.imguiLayer.initImGui();

    Window.changeScene(0);
  }

  public void loop() {
    float beginTime = (float) glfwGetTime();
    float endTime;
    float dt = -1.0f;

    while (!glfwWindowShouldClose(glfwWindow)) {
      // Poll events
      glfwPollEvents();

      glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT);

      if (dt >= 0) currentScene.update(dt);

      this.imguiLayer.update(dt);

      glfwSwapBuffers(glfwWindow);

      endTime = (float) glfwGetTime();
      dt = endTime - beginTime;
      beginTime = endTime;
    }
  }

  public static int getWidth() {
    return width;
  }

  public static int getHeight() {
    return height;
  }
}
