package engine;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;

import java.awt.event.KeyEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main() {\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";
    private String fragmentShaderSrc = "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";

    private Shader defaultShader;

    // VBO
    private float[] vertexArray = {
            // pos                  // color
            100.5f, -0.5f, 0.0f,      1.0f, 0.0f, 0.0f, 1.0f,     // bottom right, red
            -0.5f, 100.5f, 0.0f,      0.0f, 1.0f, 0.0f, 1.0f,     // top left, green
            100.5f, 100.5f, 0.0f,       0.0f, 0.0f, 1.0f, 1.0f,     // top right, blue
            -0.5f, -0.5f, 0.0f,      1.0f, 1.0f, 1.0f, 1.0f      // bottom left, white
    };

    // EBO, counter-clockwise order
    private int[] elementArray = {
            2, 1, 0,
            0, 1, 3
    };

    private int vaoID, vboID, eboID;

    public LevelEditorScene() {
        System.out.println("Entering level editor scene...");
    }

    @Override
    public void init() {
        // initialize camera
        camera = new Camera(new Vector2f(0.0f, 0.0f));

        // initialize and compile shaders
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

        // generate VAO
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // create float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // create VBO
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // create indices
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        // create EBO
        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // add vertex attribute ptrs
        int positionSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int vertexSizeBytes = (positionSize + colorSize) * floatSizeBytes;
        // register pos & color pointers
        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        // tell opengl to use shader program compiled
        defaultShader.use();

        // Load projection/view matrix value
        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());

        // bind VAO
        glBindVertexArray(vaoID);
        // enable vertex attrib pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // draw triangle
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // unbind
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        // detach shader program
        defaultShader.detach();

    }



}
