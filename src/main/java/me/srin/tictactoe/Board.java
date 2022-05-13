package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.util.Arrays;
import java.util.LinkedList;

import static me.srin.tictactoe.Main.*;
import static me.srin.tictactoe.Player.PlayerType.CROSS;

public class Board extends Window {
    static final LinkedList<Main.Pair<ImVec2, Integer>> BUTTON_COORDS = new LinkedList<>(
            Arrays.asList(
                    Main.Pair.of(new ImVec2(10.606f, 12.121f)  , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(72.727f, 12.121f)  , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(134.848f, 12.121f) , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(10.606f, 74.242f)  , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(72.727f, 74.242f)  , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(134.848f, 74.242f) , BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(10.606f,  134.848f), BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(72.727f,  134.848f), BLANK_TEXTURE),
                    Main.Pair.of(new ImVec2(134.848f, 134.848f), BLANK_TEXTURE)
            )
    );
    static final float BUTTON_SIZE = 46.809f;
    public Board(float x, float y, float width, float height) {
        super(x, y, width, height, Board.class.getSimpleName(), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoBackground);
    }
    void resetButtonTextures() {
        BUTTON_COORDS.forEach(p -> p.setValue(BLANK_TEXTURE));
    }
    @Override
    protected void renderUI() {
        ImGui.image(BOARD_TEXTURE, width - 16, height - 16);
        ImGui.pushStyleColor(ImGuiCol.Button, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0, 0, 0, 0);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0, 0, 0, 0);

        int size = BUTTON_COORDS.size();
        for (int i = 0; i < size; i++) {
            ImGui.pushID(i);
            Main.Pair<ImVec2, Integer> co_ord = BUTTON_COORDS.get(i);
            ImVec2 pos = co_ord.getKey();
            int texture = co_ord.getValue();
            ImGui.setCursorPos(pos.x, pos.y);
            boolean isButtonClicked = ImGui.imageButton(texture, BUTTON_SIZE, BUTTON_SIZE);
            if (ImGui.isItemHovered()) {
                ImGui.setCursorPos(pos.x, pos.y);
                ImGui.image(HOVER_TEXTURE, BUTTON_SIZE + 7, BUTTON_SIZE + 5);
            }
            if (isButtonClicked && engine.place(8 - i) && gameState == null) {
                co_ord.setValue(PLAYER.getType() == CROSS? CROSS_TEXTURE: NOUGHT_TEXTURE);
                winner = PLAYER.getType();
                if (engine.isWin()) {
                    gameState = GameState.WIN;
                    switch (winner) {
                        case CROSS: crossPoints++; break;
                        case NOUGHT: noughtPoints++;
                    }
                } else if (engine.isDraw()) {
                    gameState = GameState.DRAW;
                }
            }
            ImGui.popID();
        }
        ImGui.popStyleColor(3);
    }
}
