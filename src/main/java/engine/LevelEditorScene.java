package engine;


import components.Sprite;
import components.SpriteRenderer;
import org.joml.Vector2f;
import util.AssetPool;

public class LevelEditorScene extends Scene {

    public LevelEditorScene() {
        System.out.println("Entering level editor scene...");
    }

    @Override
    public void init() {
        this.camera = new Camera(new Vector2f());

        GameObject obj1 = new GameObject("obj1", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        obj1.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("assets/textures/mario.png"))));
        this.addGameObjectToScene(obj1);

        GameObject obj2 = new GameObject("obj2", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)));
        obj2.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("assets/textures/goomba.png"))));
        this.addGameObjectToScene(obj2);

        loadResources();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
    }

    @Override
    public void update(float dt) {
        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }

        this.renderer.render();

    }



}
