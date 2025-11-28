package model.trail;

public enum Difficulty {
    EASY(0), MODERATE(1), HARD(2);

    private final int rank;
    Difficulty(int rank) { this.rank = rank; }
    public int rank() { return rank; }

    @Override
    public String toString() {
        return switch (this) {
            case EASY -> "Easy";
            case MODERATE -> "Moderate";
            case HARD -> "Hard";
        };
    }
}
