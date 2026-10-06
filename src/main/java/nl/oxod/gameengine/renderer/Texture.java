package nl.oxod.gameengine.renderer;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
  private final int texID;
  @Getter
  private int width;
  @Getter
  private int height;

  public Texture(String filepath) {
    texID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texID);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    IntBuffer width = BufferUtils.createIntBuffer(1);
    IntBuffer height = BufferUtils.createIntBuffer(1);
    IntBuffer channels = BufferUtils.createIntBuffer(1);
    stbi_set_flip_vertically_on_load(true);
    ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

    if (image != null) {
      this.width = width.get(0);
      this.height = height.get(0);

      if (channels.get(0) == 3) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width.get(0), height.get(0),
            0, GL_RGB, GL_UNSIGNED_BYTE, image);
      } else if (channels.get(0) == 4) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
            0, GL_RGBA, GL_UNSIGNED_BYTE, image);
      } else {
        assert  false : "Error: (Texture) Unknown number of channels '" + channels.get(0) + "'";
      }
    } else {
      throw new RuntimeException("Error: (Texture) Could not load image '" + filepath + "'");
    }

    stbi_image_free(image);
  }

  public void bind() {
    glBindTexture(GL_TEXTURE_2D, texID);
  }

  public void unbind() {
    glBindTexture(GL_TEXTURE_2D, 0);
  }
}
