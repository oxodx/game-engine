package nl.oxod.gameengine.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {
  private int shaderProgramID;
  private boolean beingUsed = false;

  private String vertexSource;
  private String fragmentSource;
  private final String filepath;
  private final Map<String, Integer> uniformLocations = new HashMap<>();
  private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  public Shader(String filepath) {
    this.filepath = filepath;
    try {
      String source = Files.readString(Paths.get(filepath)).replace("\r\n", "\n");
      String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

      int index = source.indexOf("#type") + 6;
      int eol = source.indexOf("\n", index);
      String firstPattern = source.substring(index, eol).trim();

      index = source.indexOf("#type", eol) + 6;
      eol = source.indexOf("\n", index);
      String secondPattern = source.substring(index, eol).trim();

      if (firstPattern.equals("vertex")) {
        vertexSource = splitString[1];
      } else if (firstPattern.equals("fragment")) {
        fragmentSource = splitString[1];
      } else {
        throw new IOException("Unexpected token '" + firstPattern + "'");
      }

      if (secondPattern.equals("vertex")) {
        vertexSource = splitString[2];
      } else if (secondPattern.equals("fragment")) {
        fragmentSource = splitString[2];
      } else {
        throw new IOException("Unexpected token '" + secondPattern + "'");
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not load shader: " + filepath, e);
    }
  }

  private int uniformLocation(String name) {
    Integer location = uniformLocations.get(name);
    if (location == null) {
      location = glGetUniformLocation(shaderProgramID, name);
      uniformLocations.put(name, location);
    }
    return location;
  }

  public void compile() {
    int vertexID = compileStage(GL_VERTEX_SHADER, vertexSource, "Vertex");
    int fragmentID = 0;
    int programID = 0;
    try {
      fragmentID = compileStage(GL_FRAGMENT_SHADER, fragmentSource, "Fragment");
      programID = glCreateProgram();
      glAttachShader(programID, vertexID);
      glAttachShader(programID, fragmentID);
      glLinkProgram(programID);
      if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
        throw new IllegalStateException("Shader linking failed: " + filepath + "\n"
            + glGetProgramInfoLog(programID));
      }
      detach();
      if (shaderProgramID != 0) glDeleteProgram(shaderProgramID);
      shaderProgramID = programID;
      programID = 0;
      uniformLocations.clear();
    } finally {
      glDeleteShader(vertexID);
      if (fragmentID != 0) glDeleteShader(fragmentID);
      if (programID != 0) glDeleteProgram(programID);
    }
  }

  private int compileStage(int type, String source, String stage) {
    int shaderID = glCreateShader(type);
    glShaderSource(shaderID, source);
    glCompileShader(shaderID);
    if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
      String log = glGetShaderInfoLog(shaderID);
      glDeleteShader(shaderID);
      throw new IllegalStateException(stage + " shader compilation failed: " + filepath + "\n" + log);
    }
    return shaderID;
  }

  public void use() {
    if (!beingUsed) {
      glUseProgram(shaderProgramID);
      beingUsed = true;
    }
  }

  public void detach() {
    if (beingUsed) {
      glUseProgram(0);
      beingUsed = false;
    }
  }

  public void uploadMat4f(String varName, Matrix4f mat4) {
    int varLocation = uniformLocation(varName);
    use();
    matrixBuffer.clear();
    mat4.get(matrixBuffer);
    glUniformMatrix4fv(varLocation, false, matrixBuffer);
  }

  public void uploadMat3f(String varName, Matrix4f mat3) {
    int varLocation = uniformLocation(varName);
    use();
    matrixBuffer.clear();
    matrixBuffer.put(mat3.m00()).put(mat3.m01()).put(mat3.m02());
    matrixBuffer.put(mat3.m10()).put(mat3.m11()).put(mat3.m12());
    matrixBuffer.put(mat3.m20()).put(mat3.m21()).put(mat3.m22());
    matrixBuffer.flip();
    glUniformMatrix3fv(varLocation, false, matrixBuffer);
  }

  public void uploadVec4f(String varName, Vector4f vec) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
  }

  public void uploadVec3f(String varName, Vector3f vec) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform3f(varLocation, vec.x, vec.y, vec.z);
  }

  public void uploadVec2f(String varName, Vector2f vec) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform2f(varLocation, vec.x, vec.y);
  }

  public void uploadFloat(String varName, float val) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform1f(varLocation, val);
  }

  public void uploadInt(String varName, int val) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform1i(varLocation, val);
  }

  public void uploadTexture(String varName, int slot) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform1i(varLocation, slot);
  }

  public void uploadIntArray(String varName, int[] array) {
    int varLocation = uniformLocation(varName);
    use();
    glUniform1iv(varLocation, array);
  }
}
