package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.srin.tictactoe.engine.TicTacToeEngine;

import static me.srin.tictactoe.Main.*;
import static me.srin.tictactoe.Main.GameState.*;

public class HUD extends Window {
    private final Main.Pair<Float, Float> boardXY = Main.getBoardXY();
    private float selectedPosX = -1;
    Player.PlayerType playerType = null;
    private final ImBoolean showTooltip = new ImBoolean();
    public HUD(float x, float y, float width, float height) {
        super(x, y, width, height, HUD.class.getSimpleName(), ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus);
    }

    @Override
    protected int noMouse() {
        return 0;
    }

    @Override
    protected void renderUI() {
        int noMouse = PLAYER == null? 0: ImGuiWindowFlags.NoMouseInputs;
        ImGui.setCursorPos(10, height - 40 - 10);
        ImGui.beginChild("mode", 72, 40, true); {

        } ImGui.endChild();
        if (ImGui.isItemHovered() && showTooltip.get()) {
            ImGui.setTooltip("click to play against player or computer");
        }
        ImGui.setCursorPos(width - 72 - 10, height - 40 - 10);
        ImGui.beginChild("player", 72, 40, true, noMouse | ImGuiWindowFlags.NoScrollbar); {
            ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
            if (ImGui.imageButton(CROSS_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                playerType = Player.PlayerType.CROSS;
                selectedPosX = 4;
            }
            ImGui.setCursorPos(40, 8);
            if (ImGui.imageButton(NOUGHT_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                playerType = Player.PlayerType.NOUGHT;
                selectedPosX = 4 + 32;
            }
            if (selectedPosX > 0) {
                ImGui.setCursorPos(selectedPosX, 4);
                ImGui.image(HOVER_TEXTURE, 32, 32);
            }
            ImGui.popStyleColor(3);
        } ImGui.endChild();
        if (ImGui.isItemHovered() && showTooltip.get()) {
            ImGui.setTooltip("click to play as X or O");
        }
        float boardY = boardXY.getValue();
        if (PLAYER == null && playerType != null) {
            ImGui.setCursorPos((width - 50) / 2f, boardY + 200 /*BOARD_HEIGHT*/ + 10);
            if (ImGui.button("start", 50, 25)) {
                PLAYER = new Player(playerType);
                engine = new TicTacToeEngine(PLAYER);
            }
        }
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, "show tooltips");
        ImGui.setCursorPos((width - size.x - 20) / 2f, height - 10 - 30);
        ImGui.checkbox("show tooltips", showTooltip);
        if (Main.gameState == null) return;
        float boardX = boardXY.getKey();
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
            selectedPosX = -1;
            playerType = null;
            Main.reset();
        }
    }
}
