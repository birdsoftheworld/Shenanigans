package game.shenanigans.engine.shader;

import static org.lwjgl.opengl.GL30C.*;

public class Shader {

    private final int programId;

    public Shader(String vertexShader, String fragmentShader) {
        this.programId = glCreateProgram();

        int vertShaderId = glCreateShader(GL_VERTEX_SHADER);
        int fragShaderId = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(vertShaderId, vertexShader);
        glShaderSource(fragShaderId, fragmentShader);

        glAttachShader(this.programId, vertShaderId);
        glAttachShader(this.programId, fragShaderId);

        glDeleteShader(vertShaderId);
        glDeleteShader(fragShaderId);
    }

    public void use() {
        glUseProgram(this.programId);
    }

    public void discard() {
        glDeleteProgram(programId);
    }
}
