package me.srin.tictactoe;

public class Player {
    private PlayerType type;

    public Player(PlayerType type) {
        this.type = type;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public PlayerType getType() {
        return type;
    }

    public enum PlayerType {
        NOUGHT('o'), CROSS('x'), NONE('\0');
        private final char type;
        PlayerType(char type) { this.type = type; }

        @Override
        public String toString() {
            return String.valueOf(type);
        }
    }
}
