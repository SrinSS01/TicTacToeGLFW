package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.srin.tictactoe.engine.TicTacToeEngine;

import static me.srin.tictactoe.Main.*;
import static me.srin.tictactoe.Main.GameState.*;
import static me.srin.tictactoe.Mode.*;

public class HUD extends Window {
    private final Main.Pair<Float, Float> boardXY = Main.getBoardXY();
    private float selectedPlayerPosX = -1;
    private float selectedModePosX = -1;
    Player.PlayerType playerType = Player.PlayerType.NONE;
    static Mode mode = NONE;
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
        int noMouse = player == null? 0: ImGuiWindowFlags.NoMouseInputs;
        ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);
        ImGui.setCursorPos(10, height - 40 - 10);
        ImGui.beginChild("mode", 72, 40, true, noMouse | ImGuiWindowFlags.NoScrollbar); {
            if (ImGui.imageButton(PC_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                mode = COMPUTER;
                selectedModePosX = 4;
                selectedPlayerPosX = -1;
                playerType = Player.PlayerType.NONE;
            }
            if (ImGui.isItemHovered() && showTooltip.get()) {
                ImGui.setTooltip("PC vs Player");
            }
            ImGui.setCursorPos(40, 8);
            if (ImGui.imageButton(HUMAN_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                mode = HUMAN;
                selectedModePosX = 4 + 32;
                selectedPlayerPosX = 4;
                playerType = Player.PlayerType.CROSS;
            }
            if (ImGui.isItemHovered() && showTooltip.get())
                ImGui.setTooltip("Player vs Player");
            if (selectedModePosX > 0) {
                ImGui.setCursorPos(selectedModePosX, 4);
                ImGui.image(HOVER_TEXTURE, 32, 32);
            }
        } ImGui.endChild();
        noMouse = player == null && mode == COMPUTER? 0: ImGuiWindowFlags.NoMouseInputs;
        ImGui.setCursorPos(width - 72 - 10, height - 40 - 10);
        ImGui.beginChild("player", 72, 40, true, noMouse | ImGuiWindowFlags.NoScrollbar); {
            if (ImGui.imageButton(CROSS_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                playerType = Player.PlayerType.CROSS;
                selectedPlayerPosX = 4;
            }
            if (ImGui.isItemHovered() && showTooltip.get())
                ImGui.setTooltip("play as cross");
            ImGui.setCursorPos(40, 8);
            if (ImGui.imageButton(NOUGHT_TEXTURE, 40 - 16, 40 - 16, 0, 0, 1, 1, 0)) {
                playerType = Player.PlayerType.NOUGHT;
                selectedPlayerPosX = 4 + 32;
            }
            if (ImGui.isItemHovered() && showTooltip.get())
                ImGui.setTooltip("play as nought");
            if (selectedPlayerPosX > 0) {
                ImGui.setCursorPos(selectedPlayerPosX, 4);
                ImGui.image(HOVER_TEXTURE, 32, 32);
            }
        } ImGui.endChild();
        ImGui.popStyleColor(3);
        float boardY = boardXY.getValue();
        if (player == null && mode != NONE && playerType != Player.PlayerType.NONE) {
            ImGui.setCursorPos((width - 50) / 2f, boardY + 200 /*BOARD_HEIGHT*/ + 10);
            if (ImGui.button("start", 50, 25)) {
                player = new Player(Player.PlayerType.CROSS);
                engine = new TicTacToeEngine(player);
                if (mode == COMPUTER) {
                    if (playerType == Player.PlayerType.NOUGHT) {
                        int[] indices = { 0, 2, 4, 6, 8 };
                        int randomIndex = Board.random.nextInt(indices.length);
                        Board.Computer.playerType = Player.PlayerType.CROSS;
                        engine.place(indices[randomIndex]);
                        Board.checkWinORDraw(Board.Computer.playerType);
                        Board.computerPlayer.removeIndex(indices[randomIndex]);
                        Board.BUTTON_COORDS.get(indices[randomIndex]).setValue(CROSS_TEXTURE);
                    } else if (playerType == Player.PlayerType.CROSS) {
                        Board.Computer.playerType = Player.PlayerType.NOUGHT;
                    }
                }
            }
        }
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, "show tooltips");
        ImGui.setCursorPos((width - size.x - 20) / 2f, height - 10 - 30);
        ImGui.checkbox("show tooltips", showTooltip);
        if (Main.gameState == PLAYING) return;
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
            Main.reset();
        }
    }
}
