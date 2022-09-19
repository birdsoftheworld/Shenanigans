package game.shenanigans.engine.window;

import static org.lwjgl.glfw.GLFW.*;

public class Window {
    private final long window;

    public Window(long id) {
        this.window = id;
    }

    public void setKeyCallback(KeyCallback callback) {
        glfwSetKeyCallback(this.window, (windowId, key, scancode, actions, mods) -> callback.event(key, scancode, actions, mods));
    }

    public void setShouldClose(boolean value) {
        glfwSetWindowShouldClose(this.window, value);
    }

    public boolean getShouldClose() {
        return glfwWindowShouldClose(this.window);
    }

    public void setPosition(int x, int y) {
        glfwSetWindowPos(window, x, y);
    }

    public void swapBuffers() {
        glfwSwapBuffers(window);
    }
}
