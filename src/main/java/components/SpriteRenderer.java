package components;

import engine.Component;
import org.joml.Vector2f;
import org.joml.Vector4f;
import renderer.Texture;

public class SpriteRenderer extends Component {

    private Vector4f color;
    private Texture texture;

    public SpriteRenderer(Vector4f color) {
        this.color = color;
    }

    public SpriteRenderer(Texture texture) {
        this.texture = texture;
        this.color = new Vector4f(1, 1, 1, 1);
    }
    @Override
    public void start() {
    }
    @Override
    public void update(float dt) {
    }

    public Vector4f getColor() {
        return color;
    }

    public Vector2f[] getTextCoords() {
        Vector2f[] texCoords = {
                new Vector2f(1, 0),
                new Vector2f(1, 1),
                new Vector2f(0,1),
                new Vector2f(0, 0)
        };

        return  texCoords;
    }

    public Texture getTexture() {
        return texture;
    }
}
