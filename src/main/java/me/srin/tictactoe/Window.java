package me.srin.tictactoe;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

public abstract class Window {
    protected final float x, y, width, height;
    protected final String title;
    protected int hints;

    public Window(float x, float y, float width, float height, String title, int hints) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.hints = hints;
    }
    protected abstract void renderUI();
    protected int noMouse() { return Main.player == null? ImGuiWindowFlags.NoMouseInputs: 0; }
    public void render() {
        int noMouse = noMouse();
        ImGui.begin(title, hints | noMouse); {
            ImGui.setWindowPos(x, y);
            ImGui.setWindowSize(width, height);
            renderUI();
        } ImGui.end();
    }
}
