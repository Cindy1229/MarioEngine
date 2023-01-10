package renderer;

import java.io.IOException;
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
        glUseProgram(shaderProgramID);

    }

    public void detach() {
        glUseProgram(0);
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
}
