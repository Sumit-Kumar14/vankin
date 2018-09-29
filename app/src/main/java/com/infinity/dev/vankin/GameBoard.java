package com.infinity.dev.vankin;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.infinity.dev.vankin.GamePresenter.GamePresenter;
import com.infinity.dev.vankin.Model.DifficultyLevel;
import com.infinity.dev.vankin.Model.DifficultyLevelTimeout;
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
    private boolean isRightMoveAvailable = true;
    private boolean isBottomMoveAvailable = true;
    private boolean isTimedOut = false;
    private CountDownTimer countDownTimer;
    private int[][] arr;

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        difficultyLevel = SharedPrefsUtils.getGameLevel(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_game_board);

        populateData();

        initUI();

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        initFullScreenAd();

        if(!SharedPrefsUtils.isOnboardingDone(this)) {
            if(countDownTimer != null) {
                countDownTimer.cancel();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setMessage(R.string.intro)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(countDownTimer != null) {
                                countDownTimer.start();
                            }
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
            SharedPrefsUtils.setOnboarding(this, true);
        }
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

        initTimer(getDifficultyLevelTimeout(difficultyLevel));
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

        arr = new int[numberOfRows][numberOfColumns];

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
    }

    @Override
    public void onItemClick(View view, int position) {

        if(data[position].getGridType() == GridType.CLOSED || data[position].getGridType() == GridType.SELECTED || data[position].getGridType() == GridType.GAME_ACTUAL)
            return;

        data[position].setGridType(GridType.SELECTED);

        score += data[position].getScore();

        markAllPositionsClosedExceptSelected(data);

        int bottomPosition = position + numberOfColumns;
        int rightPosition = position + 1;

        if((position + 1) % numberOfColumns != 0) {
            data[rightPosition].setGridType(GridType.PROBABLE);
        }else {
            isRightMoveAvailable = false;
        }

        if(bottomPosition < numberOfColumns * numberOfRows) {
            data[bottomPosition].setGridType(GridType.PROBABLE);
        }else {
            isBottomMoveAvailable = false;
        }

        checkGameState();

        adapter.notifyDataSetChanged();
        setTvGameScore(gamePresenter.getGameScore(score, maxScore));
    }

    private void resetGameState() {
        isRightMoveAvailable = true;
        isBottomMoveAvailable = true;
        isTimedOut = false;
    }

    private void checkGameState() {
        if(!isRightMoveAvailable && !isBottomMoveAvailable || isTimedOut) {
            markAllPositionsClosedExceptSelected(data);
            stopCountDownTimer(countDownTimer);
            if(score < maxScore) {
                Toast.makeText(this, "No moves available! You loose", Toast.LENGTH_LONG).show();
                printPath(gamePresenter.getPath(arr, maxScore));
            }else {
                Toast.makeText(this, "You found the path!", Toast.LENGTH_LONG).show();
            }
        }

        if(score >= maxScore) {
            markAllPositionsClosedExceptSelected(data);
            stopCountDownTimer(countDownTimer);
            Toast.makeText(this, "You found the path!", Toast.LENGTH_LONG).show();
        }
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
        adapter.notifyDataSetChanged();
    }

    private void printPath(List<Pair> path) {
        for (Pair pair: path) {
            data[Integer.parseInt(pair.first.toString()) * numberOfColumns + Integer.parseInt(pair.second.toString())].setGridType(GridType.GAME_ACTUAL);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onOptionSelected(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        SharedPrefsUtils.setGameLevel(this, difficultyLevel);
        resetGameState();
        gamePresenter.reset();
        populateData();
        initUI();

        loadFullScreenAd();
    }

    private DifficultyLevelTimeout getDifficultyLevelTimeout(DifficultyLevel difficultyLevel) {
        if(difficultyLevel == DifficultyLevel.EASY) {
            return DifficultyLevelTimeout.EASY;
        }else if(difficultyLevel == DifficultyLevel.HARD) {
            return DifficultyLevelTimeout.HARD;
        }else if(difficultyLevel == DifficultyLevel.MEDIUM) {
            return DifficultyLevelTimeout.MEDIUM;
        }else if(difficultyLevel == DifficultyLevel.CHALLENGE) {
            return DifficultyLevelTimeout.CHALLENGE;
        }else {
            return DifficultyLevelTimeout.EASY;
        }
    }

    private void initTimer(DifficultyLevelTimeout difficultyLevelTimeout) {
        int INTERVAL = 1000;
        int timeoutMillis = difficultyLevelTimeout.getTimeout() * 1000;
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(timeoutMillis, INTERVAL) {
            @Override
            public void onTick(long timeRemaining) {
                setTvTimer(gamePresenter.getTimer(timeRemaining));
            }

            @Override
            public void onFinish() {
                markAllPositionsClosedExceptSelected(data);
                isTimedOut = true;
                checkGameState();
            }
        }.start();
    }

    private void stopCountDownTimer(CountDownTimer countDownTimer) {
        countDownTimer.cancel();
    }

    private void initFullScreenAd() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.test_interstitial_full_screen));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                if(countDownTimer != null) {
                    countDownTimer.start();
                }
            }

            @Override
            public void onAdOpened() {
                if(countDownTimer != null) {
                    countDownTimer.cancel();
                }
            }

        });
    }

    private void loadFullScreenAd() {
        if(mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
}