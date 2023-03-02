package renderer;

import components.SpriteRenderer;
import engine.Window;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Wrapper class for rendering sprites in one batch
 * using one VAO/VBO to improve efficiency/fps
 */
public class RenderBatch {
    // a single vertex's attributes: position     color
    // x, y         r, g, b, a
    private int POS_SIZE = 2;
    private int COLOR_SIZE = 4;

    private int POS_OFFSET = 0;
    private int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;

    // number of floats inside each vertex (position + color)
    private int VERTEX_SIZE  = 6;
    private int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    // each quad consists of 4 vertices, introducing the order to connect all vertices
    private float[] vertices;

    private int vaoID, vboID;
    // how many sprites to render in one batch
    private int maxBatchSize;

    private Shader shader;

    public RenderBatch(int maxBatchSize) {
        shader = new Shader("assets/shaders/default.glsl");
        shader.compile();
        this.sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;

        this.vertices = new float[maxBatchSize * VERTEX_SIZE * 4];

        this.numSprites = 0;
        this.hasRoom = true;
    }

    /**
     * Allocate memory for vao/vbo on GPU
     */
    public void start() {
        // generate vao
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // allocate space for vbo
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // create ebo
        int ebo = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // enable vertex attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

    }

    /**
     * Load sprite data to vbo
     */
    public void addSprite(SpriteRenderer spr) {
        // add to sprites array
        int index = numSprites;
        this.sprites[index] = spr;
        numSprites++;

        // add properties data to vbo
        loadVertexProperties(index);

        if (this.numSprites == maxBatchSize) {
            this.hasRoom = false;
        }

    }

    private void loadVertexProperties(int index) {
        SpriteRenderer sprite = this.sprites[index];

        // find offset within vbo array
        // each sprite has 4 vertices, each vertices has 6 floats representing color + position
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = sprite.getColor();

        /**
         * 3   0 (sprites' transform position)
         * 2   1
         */
        // start at top right, pos is (1, 1)
        float xAdd = 1.0f;
        float yAdd = 1.0f;
        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                yAdd = 0.0f;
            } else if (i == 2) {
                xAdd = 0.0f;
            } else if (i == 3){
                yAdd = 1.0f;
            }

            // load position
            vertices[offset] = sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x);
            vertices[offset + 1] = sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y);

            // load color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // go to next vertex in the quad
            offset += VERTEX_SIZE;
        }
        
    }

    public void render() {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        // use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        // detach
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        shader.detach();
    }

    /**
     * Generate ebo, the order to connect vertices are fixed
     * @return
     */
    private int[] generateIndices() {
        // 6 indices per quad, 3 per triangle
        int[] elements = new int[6 * maxBatchSize];

        for (int i = 0; i< maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    /**
     * load ebo starting at index, counter closewise direction
     * @param elements
     * @param index
     */
    private void loadElementIndices(int[] elements, int index) {
        // 3, 2, 0, 0, 2, 1         7, 6, 4, 4, 6, 5
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset + 0;

        // triangle 2
        elements[offsetArrayIndex+ 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    /**
     * If the batch is full
     */
    public boolean hasRoom() {
        return this.hasRoom;
    }
}
