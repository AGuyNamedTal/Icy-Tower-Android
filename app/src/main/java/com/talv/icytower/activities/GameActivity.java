package com.talv.icytower.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.engine.MultiplayerEngine;
import com.talv.icytower.game.engine.SingleplayerEngine;

public class GameActivity extends AppCompatActivity {

    private static boolean SINGLEPLAYER = false;

    private static final int FPS = 60;
    public static final int FRAME_WAIT = 1000 / FPS;

    private Engine engine;
    private GameCanvas gameCanvas;
    private Thread gameThread;


    private boolean gameRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        gameCanvas = new GameCanvas(this);
        gameCanvas.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(gameCanvas);


        Resources resources = getResources();
        Size screenSize = getRealSize();
        int renderWidth = screenSize.getWidth();
        int renderHeight = screenSize.getHeight();

        Engine.loadCharacters(resources);
        if (SINGLEPLAYER) {
            engine = new SingleplayerEngine(renderWidth, renderHeight, resources, gameCanvas, this);
        } else {
            engine = new MultiplayerEngine(renderWidth, renderHeight, resources, gameCanvas, this);
        }
        engine.initialize(renderWidth, renderHeight, resources, gameCanvas, this);
        gameRun = true;

        gameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long lastTime = System.currentTimeMillis();
                while (gameRun) {
                    long currentTime = System.currentTimeMillis();
                    int timeBetweenTicks = Math.min((int) (currentTime - lastTime), 750);
                    lastTime = currentTime;
                    gameTick(timeBetweenTicks);
                    long timeToSleep = Math.max(750 - (System.currentTimeMillis() - lastTime), 0);
                    if (timeToSleep != 0)
                        try {
                            Thread.sleep(FRAME_WAIT);
                        } catch (InterruptedException e) {
                            gameRun = false;
                            break;
                        }
                }
            }
        });
    }

    private Size getRealSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        return new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameThread.getState() == Thread.State.NEW) {
            gameThread.start();
        }
        engine.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.onPause();
    }

    private void gameTick(int time) {
        if (gameCanvas.holder == null) return;
        engine.updateFrame();
        Canvas canvas = gameCanvas.holder.lockCanvas();
        if (canvas == null) return;
        engine.render(canvas);
        gameCanvas.holder.unlockCanvasAndPost(canvas);
        engine.updateGame(time, GameActivity.this);

    }


}
