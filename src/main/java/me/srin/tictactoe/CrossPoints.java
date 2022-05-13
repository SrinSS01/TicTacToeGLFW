package me.srin.tictactoe;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static me.srin.tictactoe.Main.CROSS_TEXTURE;
import static me.srin.tictactoe.Main.getCrossPoints;

public class CrossPoints extends Window {
    public CrossPoints(float x, float y, float width, float height) {
        super(x, y, width, height, CrossPoints.class.getSimpleName(), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration);
    }
    @Override
    protected void renderUI() {
        ImGui.setWindowSize(width, height);
        ImGui.setWindowPos(x, y);
        ImGui.image(CROSS_TEXTURE, 40 - 16, 40 - 16);
        ImGui.setCursorPos(48 + 14, ((40 - 10) / 2f));
        ImGui.text(String.valueOf(getCrossPoints()));
    }
}
