package game.shenanigans.engine.window;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Window {
    private final long windowId;

    public Window(String title, int width, int height) {
        if(!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        this.windowId = glfwCreateWindow(width, height, title, 0, 0);
        if(windowId == 0) {
            throw new RuntimeException("Failed to create window");
        }

        this.setKeyCallback((key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE) {
                this.setShouldClose(true);
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowId, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            setPosition((vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(windowId);
        glfwSwapInterval(1); // vsync

        glfwShowWindow(windowId);
    }

    private void setKeyCallback(KeyCallback callback) {
        glfwSetKeyCallback(this.windowId, (windowId, key, scancode, actions, mods) -> callback.event(key, scancode, actions, mods));
    }

    public void setShouldClose(boolean value) {
        glfwSetWindowShouldClose(this.windowId, value);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(this.windowId);
    }

    public void setPosition(int x, int y) {
        glfwSetWindowPos(windowId, x, y);
    }

    public void swapBuffers() {
        glfwSwapBuffers(windowId);
    }

    public boolean isKeyPressed(int key) {
        return glfwGetKey(this.windowId, key) == GLFW_PRESS;
    }

    public void discard() {
        glfwFreeCallbacks(this.windowId);
        glfwDestroyWindow(this.windowId);
    }
}
