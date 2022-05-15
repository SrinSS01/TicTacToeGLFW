package me.srin.tictactoe;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import static me.srin.tictactoe.Main.*;
import static me.srin.tictactoe.Player.PlayerType.CROSS;
import static me.srin.tictactoe.Player.PlayerType.NONE;

public class Board extends Window {
    static final Random random = new Random();
    static Computer computerPlayer = new Computer();
    static final LinkedList<Main.Pair<ImVec2, Integer>> BUTTON_COORDS = new LinkedList<>(
            Arrays.asList(
                    Main.Pair.of(new ImVec2(10.606f, 12.121f)  , BLANK_TEXTURE),    // 0
                    Main.Pair.of(new ImVec2(72.727f, 12.121f)  , BLANK_TEXTURE),    // 1
                    Main.Pair.of(new ImVec2(134.848f, 12.121f) , BLANK_TEXTURE),    // 2
                    Main.Pair.of(new ImVec2(10.606f, 74.242f)  , BLANK_TEXTURE),    // 3
                    Main.Pair.of(new ImVec2(72.727f, 74.242f)  , BLANK_TEXTURE),    // 4
                    Main.Pair.of(new ImVec2(134.848f, 74.242f) , BLANK_TEXTURE),    // 5
                    Main.Pair.of(new ImVec2(10.606f,  134.848f), BLANK_TEXTURE),    // 6
                    Main.Pair.of(new ImVec2(72.727f,  134.848f), BLANK_TEXTURE),    // 7
                    Main.Pair.of(new ImVec2(134.848f, 134.848f), BLANK_TEXTURE)     // 8
            )
    );
    static final float BUTTON_SIZE = 46.809f;
    public Board(float x, float y, float width, float height) {
        super(x, y, width, height, Board.class.getSimpleName(), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoBackground);
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
            Player.PlayerType type = player == null? null: player.getType();
            if (isButtonClicked && gameState == GameState.PLAYING && engine.place(i)) {
                if (me.srin.tictactoe.HUD.mode == Mode.COMPUTER) computerPlayer.removeIndex(i);
                co_ord.setValue(type == CROSS? CROSS_TEXTURE: NOUGHT_TEXTURE);
                checkWinORDraw(type);
            }
            if (me.srin.tictactoe.HUD.mode == Mode.COMPUTER && gameState == GameState.PLAYING && type == Computer.playerType && computerPlayer.play()) {
//                co_ord.setValue(Computer.playerType == CROSS ? CROSS_TEXTURE : NOUGHT_TEXTURE);
//                computerPlayer.removeIndex(i);
                checkWinORDraw(type);
            }
            ImGui.popID();
        }
        ImGui.popStyleColor(3);
    }

    static void checkWinORDraw(Player.PlayerType type) {
        winner = type;
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

    public void reset() {
        BUTTON_COORDS.forEach(p -> p.setValue(BLANK_TEXTURE));
        if (me.srin.tictactoe.HUD.mode == Mode.COMPUTER) computerPlayer = new Computer();
    }
    static class Computer {
        static Player.PlayerType playerType = NONE;
        private final ArrayList<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        public boolean play() {
            int cell = indices.get(random.nextInt(indices.size()));
            boolean result = engine.place(cell);
            removeIndex(cell);
            BUTTON_COORDS.get(cell).setValue(playerType == CROSS? CROSS_TEXTURE: NOUGHT_TEXTURE);
            return result;
        }
        void removeIndex(int i) {
            indices.remove(Integer.valueOf(i));
        }
    }
}
