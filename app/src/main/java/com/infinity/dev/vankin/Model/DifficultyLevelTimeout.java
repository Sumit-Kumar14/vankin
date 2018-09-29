package com.infinity.dev.vankin.Model;

public enum DifficultyLevelTimeout {

    EASY(15),
    MEDIUM(30),
    HARD(45),
    CHALLENGE(90);

    int timeout;

    DifficultyLevelTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}
