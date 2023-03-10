package renderer;

import components.SpriteRenderer;
import engine.GameObject;

import java.util.ArrayList;
import java.util.List;

public class Renderer {
    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches;

    public Renderer() {
        this.batches = new ArrayList<>();
    }

    public void add(GameObject go) {
        SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
        if (spr != null) {
            add(spr);
        }
    }

    /**
     * Loop through all RenderBatches and find the first that has room to render
     * @param sprite
     */
    public void add(SpriteRenderer sprite) {
        boolean added = false;

        for (RenderBatch batch : batches) {
            if (batch.hasRoom()) {
                // limit the number of texture spritesheet to 8 per sprite
                Texture tex = sprite.getTexture();
                if (tex == null || batch.hasTexture(tex) || batch.hasTextureRoom()) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
        }
    }

    /**
     * Render all the batches
     */
    public void render() {
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }
}
