package engine;

import java.awt.event.KeyEvent;

import static org.lwjgl.glfw.GLFW.*;

public class LevelEditorScene extends Scene {
    private boolean changingScene = false;

    private float timeToChangeScene = 2.0f;

    public LevelEditorScene() {
        System.out.println("Entering level editor scene...");
    }
    @Override
    public void update(float dt) {
        if (!changingScene && KeyListener.isKeyPressed(GLFW_KEY_SPACE)) {
            changingScene = true;

        }
        // fade to black
        if (changingScene && timeToChangeScene > 0) {
            timeToChangeScene -= dt;
            Window.get().r -= dt * 5.0f;
            Window.get().g -= dt * 5.0f;
            Window.get().b -= dt * 5.0f;
        } else if (changingScene) {
            Window.changeScene(1);
        }
    }

}
