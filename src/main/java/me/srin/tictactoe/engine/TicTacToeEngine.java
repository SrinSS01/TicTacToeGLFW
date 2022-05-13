package me.srin.tictactoe.engine;

import me.srin.tictactoe.Player;

import java.util.Arrays;
import java.util.List;

import static me.srin.tictactoe.Player.PlayerType.CROSS;
import static me.srin.tictactoe.Player.PlayerType.NOUGHT;

public class TicTacToeEngine {
    private static final int LAST_INDEX = 1 << 8;
    private static final int COLUMN_0 = 4 | (4 << 3) | (4 << 3 << 3);
    private static final int COLUMN_1 = 2 | (2 << 3) | (2 << 3 << 3);
    private static final int COLUMN_2 = 1 | (1 << 3) | (1 << 3 << 3);
    private static final int ROW_0 = 7;
    private static final int ROW_1 = 7 << 3;
    private static final int ROW_2 = 7 << 3 << 3;
    private static final int DIAGONAL_RIGHT = 84;
    private static final int DIAGONAL_LEFT = 273;

    private static final int DRAW = ROW_0 | ROW_1 | ROW_2;
    private int x_board;
    private int o_board;
    private final Player player;
    private final List<Integer> win_combinations;
    /*
        +-----------+
        | 8 | 7 | 6 |
        |-----------|
        | 5 | 4 | 3 |
        |-----------|
        | 2 | 1 | 0 |
        +-----------+
    */
    public TicTacToeEngine(Player player) {
        this.x_board = 0;
        this.o_board = 0;
        this.player = player;
        this.win_combinations = Arrays.asList(
                ROW_0,            ROW_1,        ROW_2,    // rows
                COLUMN_2,         COLUMN_1,     COLUMN_0, // columns
                DIAGONAL_RIGHT,   DIAGONAL_LEFT           // diagonals
        );
    }
    public boolean place(int pos) {
        int cell = 1 << pos;
        if (cell > LAST_INDEX || cell < 0 || ((x_board | o_board) & cell) == cell) return false;
        switch (player.getType()) {
            case CROSS: { x_board |= cell; } break;
            case NOUGHT: { o_board |= cell; }
        }
        return true;
    }

    public boolean isWin() {
        switch (player.getType()) {
            case NOUGHT: {
                player.setType(CROSS);
                return win_combinations.stream().anyMatch(it -> (o_board & it) == it);
            }
            case CROSS: {
                player.setType(NOUGHT);
                return win_combinations.stream().anyMatch(it -> (x_board & it) == it);
            }
            default: return false;
        }
    }

    public boolean isDraw() {
        return (x_board | o_board) == DRAW;
    }
}