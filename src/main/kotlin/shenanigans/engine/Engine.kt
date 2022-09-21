package game.shenanigans.engine;

import game.shenanigans.engine.window.Window;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class Engine {
    private Window window;

    public static void main(String[] args) {
        new Engine().run();
    }

    public Engine() {

    }

    public void init() {
        this.window = new Window("game", 640, 480);
    }

    public void run() {
        this.init();

        GL.createCapabilities();
        glClearColor(0.5f, 1.0f, 0.5f, 0.5f);

        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            window.swapBuffers();

            glfwPollEvents();
        }

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void loop() {

    }
}
