package com.infinity.dev.vankin.GamePresenter;

import android.annotation.SuppressLint;
import android.util.Pair;

import com.infinity.dev.vankin.Model.DifficultyLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GamePresenter {

    private int mem[][];
    private static int x, y;
    private List<Pair> gameBestPath;

    private static int ROWS;
    private static int COLUMNS;

    public GamePresenter(int rows, int columns) {
        ROWS = rows;
        COLUMNS = columns;

        x = 0;
        y = 0;
        mem = new int[ROWS][COLUMNS];
        gameBestPath = new ArrayList<>();
    }

    private int vankin(int x, int y, int arr[][]) {
        if(x >= ROWS || y >= COLUMNS)
            return 0;

        if(mem[x][y] == 0) {
            int v1 = vankin(x + 1, y, arr);
            int v2 = vankin(x, y + 1, arr);
            mem[x][y] = arr[x][y] + Math.max(v1, v2);
        }
        return mem[x][y];
    }

    public int gameMaxScore(int arr[][]) {
        int maxScore = 0;

        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                int next = vankin(i, j, arr);
                if(next > maxScore) {
                    x = i;
                    y = j;
                }
                maxScore = Math.max(maxScore, next);
            }
        }
        return maxScore;
    }

    private void updatePath(int arr[][], int x, int y, int sum, int currentSum, Stack<Pair<Integer, Integer>> pairs) {
        if(x >= ROWS || y >= COLUMNS)
            return;

        currentSum += arr[x][y];
        pairs.push(new Pair<>(x, y));

        if(currentSum == sum) {
            for (Pair pair : pairs) {
                gameBestPath.add(pair);
            }
            return;
        }

        updatePath(arr, x + 1, y, sum, currentSum, pairs);
        updatePath(arr, x, y + 1, sum, currentSum, pairs);

        pairs.pop();
    }

    public List<Pair> getPath(int arr[][], int maxScore) {
        updatePath(arr, x, y, maxScore, 0, new Stack<Pair<Integer, Integer>>());
        return gameBestPath;
    }

    public void reset() {
        x = 0;
        y = 0;
        mem = new int[ROWS][COLUMNS];
        gameBestPath = new ArrayList<>();
    }

    public String getGameLevel(DifficultyLevel level) {
        if(level == DifficultyLevel.EASY) {
            return "Easy";
        }else if(level == DifficultyLevel.MEDIUM) {
            return "Medium";
        }else if(level == DifficultyLevel.HARD) {
            return "Hard";
        }else {
            return "Challenge";
        }
    }

    public String getGameScore(int actual, int max) {
        return String.valueOf(actual) + "/" + String.valueOf(max);
    }

    @SuppressLint("DefaultLocale")
    public String getTimer(long timeDiff) {
        int seconds = (int)(timeDiff / 1000);
        int minutes = (seconds / 60);
        int hours = minutes / 60;
        return String.format("%02d", hours) + ":" + String.format("%02d", minutes % 60) + ":" + String.format("%02d", seconds % 60);
    }
}