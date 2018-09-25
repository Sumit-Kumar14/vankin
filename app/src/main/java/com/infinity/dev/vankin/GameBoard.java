package com.infinity.dev.vankin;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.infinity.dev.vankin.GamePresenter.GamePresenter;
import com.infinity.dev.vankin.Model.DifficultyLevel;
import com.infinity.dev.vankin.Model.GridType;
import com.infinity.dev.vankin.Model.Points;

import java.util.List;
import java.util.Random;

public class GameBoard extends AppCompatActivity implements GridAdapter.ItemClickListener, BottomSheetFragment.OnOptionSelected {

    private GridAdapter adapter;
    private static final int MAX = 99;
    private static final int MIN = -99;
    private Points[] data;
    private int numberOfColumns;
    private int numberOfRows;
    private int score = 0;
    private int maxScore = 0;

    private GamePresenter gamePresenter;
    private DifficultyLevel difficultyLevel;

    private TextView tvGameLevel;
    private TextView tvGameScore;
    private TextView tvTimer;
    private ImageButton ibPause;
    private ImageButton ibReset;

    private final int INTERVAL = 1000;
    private long startTime = 0L;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable(){
        public void run() {
            setTvTimer(gamePresenter.getTimer(System.currentTimeMillis() - startTime));
            handler.postDelayed(runnable, INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        difficultyLevel = DifficultyLevel.EASY;

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_game_board);

        populateData();

        initUI();

        handler.postDelayed(runnable, INTERVAL);
    }

    private void initUI() {
        tvGameLevel = findViewById(R.id.tv_game_level);
        tvGameScore = findViewById(R.id.tv_score);
        tvTimer = findViewById(R.id.tv_timer);
        ibPause = findViewById(R.id.ib_pause);
        ibReset = findViewById(R.id.ib_reset);

        ibReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.setmOnOptionSelected(GameBoard.this);
                bottomSheetFragment.show(getSupportFragmentManager(), "Dialog");
            }
        });

        setTvGameLevel(gamePresenter.getGameLevel(difficultyLevel));
        setTvGameScore(gamePresenter.getGameScore(score, maxScore));

        startTime = System.currentTimeMillis();
    }

    private void setTvGameLevel(String gameLevel) {
        tvGameLevel.setText(gameLevel);
    }

    private void setTvGameScore(String gameScore) {
        tvGameScore.setText(gameScore);
    }

    private void setTvTimer(String timer) {
        tvTimer.setText(timer);
    }

    private void populateData() {
        score = 0;
        if(difficultyLevel == DifficultyLevel.CHALLENGE) {
            numberOfColumns = getRowCount(this);
            numberOfRows = getColumnCount(this) - 1;
        }else {
            numberOfRows = difficultyLevel.getLevel();
            numberOfColumns = difficultyLevel.getLevel();
        }

        data = new Points[numberOfColumns * numberOfRows];

        gamePresenter = new GamePresenter(numberOfRows, numberOfColumns);

        for(int i = 0; i < numberOfColumns * numberOfRows; i++) {
            int number = new Random().nextInt(MAX + 1 -MIN) + MIN;
            Points point = new Points();
            point.setScore(number);
            point.setGridType(GridType.OPEN);
            data[i] = point;
        }

        int arr[][] = new int[numberOfRows][numberOfColumns];

        for(int i = 0; i < numberOfColumns * numberOfRows; i++) {
            int row = i / numberOfColumns;
            int column = i - row * numberOfColumns;
            arr[row][column] = data[i].getScore();
        }

        maxScore = gamePresenter.gameMaxScore(arr);

        RecyclerView recyclerView = findViewById(R.id.rvNumbers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new GridAdapter(this, data);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        printPath(gamePresenter.getPath(arr, maxScore));
    }

    @Override
    public void onItemClick(View view, int position) {

        if(data[position].getGridType() == GridType.CLOSED || data[position].getGridType() == GridType.SELECTED)
            return;

        data[position].setGridType(GridType.SELECTED);

        score += data[position].getScore();

        markAllPositionsClosedExceptSelected(data);

        int bottomPosition = position + numberOfColumns;
        int rightPosition = position + 1;

        if((position + 1) % numberOfColumns != 0)
            data[rightPosition].setGridType(GridType.PROBABLE);

        if(bottomPosition < numberOfColumns * numberOfRows)
            data[bottomPosition].setGridType(GridType.PROBABLE);

        adapter.notifyDataSetChanged();
        setTvGameScore(gamePresenter.getGameScore(score, maxScore));
    }

    private int getRowCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 50);
    }

    private int getColumnCount(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.heightPixels / displayMetrics.density - 120;
        return (int) (dpWidth / 50);
    }

    private void markAllPositionsClosedExceptSelected(Points[] points) {
        for (Points point : points) {
            if(point.getGridType() != GridType.SELECTED)
                point.setGridType(GridType.CLOSED);
        }
    }

    private void printPath(List<Pair> path) {
        for (Pair pair: path) {
            data[Integer.parseInt(pair.first.toString()) * numberOfColumns + Integer.parseInt(pair.second.toString())].setGridType(GridType.GAME_ACTUAL);
            adapter.notifyDataSetChanged();
            Log.d("Some Tag", "(" + pair.first + ", " + pair.second + ")");
        }
    }

    @Override
    public void onOptionSelected(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        gamePresenter.reset();
        populateData();
        initUI();
    }
}