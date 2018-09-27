package com.infinity.dev.vankin;

import android.content.Context;
import android.content.SharedPreferences;

import com.infinity.dev.vankin.Model.DifficultyLevel;

public class SharedPrefsUtils {

    private static final String GAME_PREF_FILE = "game_pref";
    private static final String GAME_LEVEL_KEY_NAME = "GAME_LEVEL";

    public static void setGameLevel(Context context, DifficultyLevel difficultyLevel) {
        SharedPreferences sharedPref = context.getSharedPreferences(GAME_PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.edit().putInt(GAME_LEVEL_KEY_NAME, difficultyLevel.getLevel()).apply();
    }

    public static DifficultyLevel getGameLevel(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(GAME_PREF_FILE, Context.MODE_PRIVATE);
        int level = sharedPref.getInt(GAME_LEVEL_KEY_NAME, -1);
        if(level == DifficultyLevel.EASY.getLevel()) {
            return DifficultyLevel.EASY;
        }else if(level == DifficultyLevel.MEDIUM.getLevel()) {
            return DifficultyLevel.MEDIUM;
        }else if(level == DifficultyLevel.HARD.getLevel()) {
            return DifficultyLevel.HARD;
        }else if(level == DifficultyLevel.CHALLENGE.getLevel()) {
            return DifficultyLevel.CHALLENGE;
        }else {
            return DifficultyLevel.EASY;
        }
    }
}
