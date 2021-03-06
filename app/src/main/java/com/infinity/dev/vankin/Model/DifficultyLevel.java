package com.infinity.dev.vankin.Model;

public enum DifficultyLevel {
    EASY(4),
    MEDIUM(6),
    HARD(8),
    CHALLENGE(16);

    int level;

    DifficultyLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
