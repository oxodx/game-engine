package nl.oxod.gameengine.renderer;

import nl.oxod.gameengine.components.SpriteRenderer;
import nl.oxod.gameengine.engine.GameObject;
import nl.oxod.gameengine.engine.Transform;
import nl.oxod.gameengine.engine.Window;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

@EnabledOnOs(OS.LINUX)
class RenderRegressionTest {
  private static long window;

  @BeforeAll
  static void createContext() {
    glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_NULL);
    assumeTrue(glfwInit(), "Headless GLFW unavailable");
    glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_EGL_CONTEXT_API);
    window = glfwCreateWindow(64, 64, "Render test", 0, 0);
    assumeTrue(window != 0, "Headless EGL context unavailable");
    glfwMakeContextCurrent(window);
    GL.createCapabilities();
  }

  @AfterAll
  static void destroyContext() {
    if (window != 0) glfwDestroyWindow(window);
    glfwTerminate();
    GL.setCapabilities(null);
  }

  private static SpriteRenderer sprite(float x) {
    GameObject object = new GameObject("test",
        new Transform(new Vector2f(x, 20), new Vector2f(10, 10)), 0);
    SpriteRenderer sprite = new SpriteRenderer(new Vector4f(1));
    object.addComponent(sprite);
    object.start();
    return sprite;
  }

  @Test
  void batchUploadsNewSpritesAndOnlyChangesDirtyVertices() {
    Window.changeScene(0);
    RenderBatch batch = new RenderBatch(4, 0);
    batch.start();
    SpriteRenderer first = sprite(10);
    SpriteRenderer second = sprite(30);
    first.setClean();
    second.setClean();
    batch.addSprite(first);
    batch.addSprite(second);
    batch.render();

    float[] vertices = new float[72];
    glGetBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
    assertEquals(20, vertices[0]);
    assertEquals(40, vertices[36]);

    first.gameObject.transform.position.x = 50;
    first.update(0);
    batch.render();
    glGetBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
    assertEquals(60, vertices[0]);
    assertEquals(40, vertices[36]);

    second.gameObject.transform.position.x = 110;
    second.update(0);
    batch.render();
    glGetBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
    assertEquals(60, vertices[0]);
    assertEquals(120, vertices[36]);

    SpriteRenderer third = sprite(70);
    third.setClean();
    batch.addSprite(third);
    first.gameObject.transform.position.x = 90;
    first.update(0);
    batch.render();
    vertices = new float[108];
    glGetBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
    assertEquals(100, vertices[0]);
    assertEquals(80, vertices[72]);
    assertEquals(GL_NO_ERROR, glGetError());
  }

  @Test
  void invalidShadersFailImmediately(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("invalid.glsl");
    Files.writeString(file, "#type vertex\n#version 330 core\ninvalid shader\n"
        + "#type fragment\n#version 330 core\nvoid main() {}\n");
    Shader shader = new Shader(file.toString());
    assertThrows(IllegalStateException.class, shader::compile);
    assertEquals(GL_NO_ERROR, glGetError());
  }

  @Test
  void matrixUploadsUseCorrectPackingAndReuseBuffer(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("matrix.glsl");
    Files.writeString(file, """
        #type vertex
        #version 330 core
        uniform mat3 uThree;
        uniform mat4 uFour;
        void main() { gl_Position = uFour * vec4(uThree * vec3(1), 1); }
        #type fragment
        #version 330 core
        out vec4 color;
        void main() { color = vec4(1); }
        """);
    Shader shader = new Shader(file.toString());
    shader.compile();
    int program = 0;
    try {
      shader.uploadMat3f("uThree", new Matrix4f().scaling(2, 3, 4));
      program = glGetInteger(GL_CURRENT_PROGRAM);
      float[] actual = new float[9];
      glGetUniformfv(program, glGetUniformLocation(program, "uThree"), actual);
      assertArrayEquals(new float[]{2, 0, 0, 0, 3, 0, 0, 0, 4}, actual);

      shader.uploadMat4f("uFour", new Matrix4f().scaling(5, 6, 7));
      actual = new float[16];
      glGetUniformfv(program, glGetUniformLocation(program, "uFour"), actual);
      assertArrayEquals(new float[]{5, 0, 0, 0, 0, 6, 0, 0, 0, 0, 7, 0, 0, 0, 0, 1}, actual);
      assertEquals(GL_NO_ERROR, glGetError());
    } finally {
      shader.detach();
      if (program != 0) glDeleteProgram(program);
    }
  }
}
