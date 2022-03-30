package com.talv.icytower.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.talv.icytower.R;
import com.talv.icytower.batteryChange.BatteryChangeListener;
import com.talv.icytower.batteryChange.BatteryChangeReceiver;
import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.engine.MultiplayerEngine;
import com.talv.icytower.game.engine.SingleplayerEngine;

public class GameActivity extends AppCompatActivity {

    public static final String SINGLEPLAYER_KEY = "SINGLEPLAYER";
    private static final int UI_VISIBILITY_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private static final int FPS = 60;
    public static final int FRAME_WAIT = 1000 / FPS;

    private Engine engine;
    private GameCanvas gameCanvas;
    private Thread gameThread;


    private boolean loopGame;
    private BatteryChangeReceiver batteryChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        batteryChangeReceiver = new BatteryChangeReceiver(0.3, new BatteryChangeListener() {
            @Override
            public void onBatteryLow(double battery) {
                showBatteryLowNotification();
            }

            @Override
            public void onBatteryNotLow(double battery) {

            }
        });
        registerReceiver(batteryChangeReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        gameCanvas = new GameCanvas(this);
        gameCanvas.setSystemUiVisibility(UI_VISIBILITY_FLAGS);

        Resources resources = getResources();
        Size screenSize = getRealSize();
        int renderWidth = screenSize.getWidth();
        int renderHeight = screenSize.getHeight();

        boolean singleplayer = getIntent().getBooleanExtra(SINGLEPLAYER_KEY, true);
        if (singleplayer) {
            engine = new SingleplayerEngine(renderWidth, renderHeight, resources, gameCanvas, this);
        } else {
            engine = new MultiplayerEngine(renderWidth, renderHeight, resources, gameCanvas, this);
        }
        engine.updateGameState(Engine.GameState.CHOOSING_CHAR);
        gameThread = new Thread(this::gameThread);

        setContentView(gameCanvas);

    }

    private void showBatteryLowNotification(){
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(GameActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryChangeReceiver);
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
    protected void onPause() {
        super.onPause();
        engine.onPause();
        loopGame = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            gameThread = new Thread(this::gameThread);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            window.getDecorView().setSystemUiVisibility(UI_VISIBILITY_FLAGS);
        }
    }


    private void gameThread() {
        long lastTime = System.currentTimeMillis();
        loopGame = true;
        while (loopGame) {
            long currentTime = System.currentTimeMillis();
            int timeBetweenTicks = Math.min((int) (currentTime - lastTime), 750);
            lastTime = currentTime;
            gameTick(timeBetweenTicks);
            long timeToSleep = Math.max(750 - (System.currentTimeMillis() - lastTime), 0);
            if (timeToSleep != 0)
                try {
                    Thread.sleep(FRAME_WAIT);
                } catch (InterruptedException e) {
                    loopGame = false;
                    break;
                }
        }
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
