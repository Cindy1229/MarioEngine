package engine;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This is a singleton window class
 */
public class Window {
    private int width;
    private int height;
    private String title;
    private long glfwWindow;
    // singleton
    private static Window window = null;

    private Window() {
        this.width = 1920;
        height = 1080;
        this.title = "Mario";
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public void run() {
        System.out.println("This Engine is running LWJGL - Version " + Version.getVersion());
        init();
        loop();

        // free mem
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // clean up
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // init
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // hide window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        // create window memory address
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create window");
        }

        // register mouse listener to glfw
        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);

        // make openGL context current
        glfwMakeContextCurrent(glfwWindow);
        // enable v-sync by monitor refresh rate
        glfwSwapInterval(1);

        // make window visible
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();

    }

    public void loop() {
        while(!glfwWindowShouldClose(glfwWindow)) {
            // poll events
            glfwPollEvents();
            glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(glfwWindow);
        }
    }
}
