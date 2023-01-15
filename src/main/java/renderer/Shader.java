package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glCompileShader;

/**
 * This class takes in the shader files and compile them
 */
public class Shader {
    private int shaderProgramID;

    private String vertexSource;
    private String fragmentSource;
    private String filePath;

    private boolean beingUsed = false;

    public Shader(String filePath) {
        this.filePath = filePath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filePath)));
            // get shader type and shader code separated
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
            // beginning index of first word after type keyword
            int index = source.indexOf("#type") + 6;
            // end of first line
            int eol = source.indexOf("\n", index);
            // first shader type
            String firstPattern = source.substring(index, eol).trim();
            // find second type keyword
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\n", index);
            // second shader type
            String secondPattern = source.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected shader type: " + firstPattern);
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException("Unexpected shader type: " + secondPattern);
            }


        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: can not open shader file: " + filePath;
        }
    }

    /**
     * Compile & link shaders to a shaderProgram given the source
     */
    public void compile() {
        int vertexID, fragmentID;
        // compile shaders
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // check for errors
        checkShaderCompileStatus(vertexID);

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        checkShaderCompileStatus(fragmentID);

        // link
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // check errors
        checkShaderLinkStatus(shaderProgramID);
    }

    public void use() {
        // avoid re-binding the same shader twice
        if (!beingUsed) {
            glUseProgram(shaderProgramID);
            beingUsed = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        beingUsed = false;
    }

    /**
     * Load value into uniform matrix4 variable
     * @param varName
     * @param mat4
     */
    public void uploadMat4f(String varName, Matrix4f mat4) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        // make sure shader to use is current
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        mat4.get(matBuffer);
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    /**
     * Load value into uniform matrix3 variable
     * @param varName
     * @param mat3
     */
    public void uploadMat3f(String varName, Matrix3f mat3) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        // make sure shader to use is current
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
        mat3.get(matBuffer);
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    /**
     * Load value into uniform matrix2 variable
     * @param varName
     * @param mat2
     */
    public void uploadMat2f(String varName, Matrix2f mat2) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        // make sure shader to use is current
        use();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(4);
        mat2.get(matBuffer);
        glUniformMatrix2fv(varLocation, false, matBuffer);
    }

    /**
     * Load value into uniform vec4 variable
     * @param varName
     * @param vec
     */
    public void uploadVec4f(String varName, Vector4f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }
    /**
     * Load value into uniform vec3 variable
     * @param varName
     * @param vec
     */
    public void uploadVec3f(String varName, Vector3f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    /**
     * Load value into uniform vec2 variable
     * @param varName
     * @param vec
     */
    public void uploadVec2f(String varName, Vector2f vec) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform2f(varLocation, vec.x, vec.y);
    }

    /**
     * Load value into uniform float value
     * @param varName
     * @param value
     */
    public void uploadFloat(String varName, float value) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1f(varLocation, value);
    }

    /**
     * Load value into uniform int value
     * @param varName
     * @param value
     */
    public void uploadInt(String varName, int value) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, value);
    }

    /**
     * Check if shader compiled successfully
     * @param shaderID
     */
    private void checkShaderCompileStatus(int shaderID) {
        int success = glGetShaderi(shaderID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(shaderID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: shader compilation failed");
            System.out.println(glGetShaderInfoLog(shaderID, len));
            assert false : "";
        }
    }

    /**
     * Check if shaders linked successfully
     * @param shaderProgram
     */
    private void checkShaderLinkStatus(int shaderProgram) {
        int success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("Error: shader linking failed");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
            assert false : "";
        }
    }

    /**
     * Load textureID to shader
     * @param varName
     * @param slot
     */
    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(shaderProgramID, varName);
        use();
        glUniform1i(varLocation, slot);
    }
}
