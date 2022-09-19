package game.shenanigans.engine;

import game.shenanigans.engine.window.Window;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Engine {
    private Window window;

    public static void main(String[] args) {
        new Engine().run();
    }

    public Engine() {

    }

    public void init() {
        if(!glfwInit()) {
            System.err.println("oops");
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        long windowId = glfwCreateWindow(640, 480, "GAME!!!", 0, 0);
        if(windowId == 0) {
            System.err.println("uh oh");
            throw new RuntimeException("Failed to create window");
        }

        this.window = new Window(windowId);

        window.setKeyCallback((key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE) {
                window.setShouldClose(true);
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowId, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            window.setPosition((vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(windowId);
        glfwSwapInterval(1); // vsync

        glfwShowWindow(windowId);
    }

    public void run() {
        this.init();

        GL.createCapabilities();
        glClearColor(0.5f, 1.0f, 0.5f, 0.5f);

        while (!window.getShouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            window.swapBuffers();

            glfwPollEvents();
        }
    }
}
