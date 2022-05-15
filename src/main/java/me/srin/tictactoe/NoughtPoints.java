package me.srin.tictactoe;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static me.srin.tictactoe.Main.NOUGHT_TEXTURE;
import static me.srin.tictactoe.Main.getNoughtPoints;

public class NoughtPoints extends Window {
    public NoughtPoints(float x, float y, float width, float height) {
        super(x, y, width, height, NoughtPoints.class.getSimpleName(), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration);
    }

    @Override
    protected void renderUI() {
        ImGui.image(NOUGHT_TEXTURE, height - 16, height - 16);
        ImGui.setCursorPos(48 + 14, ((40 - 10) / 2f));
        ImGui.text(String.valueOf(getNoughtPoints()));
    }
}
