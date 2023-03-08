package renderer;

import components.SpriteRenderer;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Wrapper class for rendering sprites in one batch
 * using one VAO/VBO to improve efficiency/fps
 */
public class RenderBatch {
    private static final int MAX_SPRITESHEET_NUM = 8;
    // a single vertex's attributes: position     color     texture coordinates     texture ID
    // x, y         r, g, b, a,        x, y, id
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEXT_COORDS_SIZE = 2;
    private final int TEXT_ID_SIZE = 1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEXT_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEXT_ID_OFFSET = TEXT_COORDS_OFFSET + TEXT_COORDS_SIZE * Float.BYTES;

    // number of floats inside each vertex (position + color + texture coords + texture id)
    private final int VERTEX_SIZE  = 9;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    // each quad consists of 4 vertices, introducing the order to connect all vertices
    private float[] vertices;

    private int vaoID, vboID;
    // how many sprites to render in one batch
    private int maxBatchSize;

    private Shader shader;

    private List<Texture> textures;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    public RenderBatch(int maxBatchSize) {
        this.shader = AssetPool.getShader("assets/shaders/default.glsl");
        this.sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;

        this.vertices = new float[maxBatchSize * VERTEX_SIZE * 4];

        this.numSprites = 0;
        this.hasRoom = true;
        this.textures = new ArrayList<>();
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
        glVertexAttribPointer(2, TEXT_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXT_COORDS_OFFSET);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, TEXT_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXT_ID_OFFSET);
        glEnableVertexAttribArray(3);

    }

    /**
     * Load sprite data to vbo
     */
    public void addSprite(SpriteRenderer spr) {
        // add to sprites array
        int index = numSprites;
        this.sprites[index] = spr;
        numSprites++;

        // if sprite has a texture and the texture does not exist, add it to texture array
        if (spr.getTexture() != null) {
            if (!textures.contains(spr.getTexture())) {
                textures.add(spr.getTexture());
            }
        }

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

        // get the textureId of sprite
        int textId = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i<textures.size();i++) {
                if (textures.get(i) == sprite.getTexture()) {
                    // leave slot 0 out for pure colors
                    textId = i + 1;
                    break;
                }
            }
        }
        Vector2f[] textCoords = sprite.getTextCoords();

        /**
         * 3         0
         * 2(pos)    1
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

            // load texture
            vertices[offset + 6] = textCoords[i].x;
            vertices[offset + 7] = textCoords[i].y;

            // load texture id
            vertices[offset + 8] = textId;

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

        // bind textures to slots
        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        // bind vao
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        // detach vao
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        // unbind textures
        for (int i = 0; i < textures.size(); i++) {
            textures.get(i).unbind();
        }
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

    /**
     * Max number of spritesheet should be limited
     */
    public boolean hasTextureRoom() {
        return this.textures.size() < MAX_SPRITESHEET_NUM;
    }

    /**
     * Checks if the spritesheet has already been loaded
     * @param texture
     * @return
     */
    public boolean hasTexture(Texture texture) {
        return this.textures.contains(texture);
    }
}
