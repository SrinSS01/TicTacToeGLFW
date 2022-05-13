package me.srin.tictactoe;

import imgui.ImGui;

public abstract class Window {
    protected final float x, y, width, height;
    protected final String title;
    protected final int hints;

    public Window(float x, float y, float width, float height, String title, int hints) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.hints = hints;
    }
    protected abstract void renderUI();
    public void render() {
        ImGui.begin(title, hints); {
            ImGui.setWindowPos(x, y);
            ImGui.setWindowSize(width, height);
            renderUI();
        } ImGui.end();
    }
}
