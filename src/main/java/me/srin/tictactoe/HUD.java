package me.srin.tictactoe;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

import static me.srin.tictactoe.Main.CROSS_TEXTURE;
import static me.srin.tictactoe.Main.GameState.*;
import static me.srin.tictactoe.Main.NOUGHT_TEXTURE;

public class HUD extends Window {
    private final Main.Pair<Float, Float> boardXY = Main.getBoardXY();
    public HUD(float x, float y, float width, float height) {
        super(x, y, width, height, HUD.class.getSimpleName(), ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus);
    }

    @Override
    protected void renderUI() {
        if (Main.gameState == null) return;
        float boardX = boardXY.getKey();
        float boardY = boardXY.getValue();
        if (Main.gameState == WIN) {
            ImGui.setCursorPos(boardX + 50, boardX - 70);
            ImGui.text("winner is   !!");
            ImGui.setCursorPos(boardX + 120, boardY - 67);
            ImGui.image(Main.getWinner() == Player.PlayerType.CROSS? CROSS_TEXTURE: NOUGHT_TEXTURE, 10, 10);
        } else if (Main.gameState == DRAW) {
            ImGui.setCursorPos((width - 25) / 2f, boardY - 70);
            ImGui.text("draw!");
        }
        ImGui.setCursorPos((width - 50) / 2f, boardY + 200 /*BOARD_HEIGHT*/ + 10);
        if (ImGui.button("reset", 50, 25)) {
            Main.reset();
        }
    }
}
