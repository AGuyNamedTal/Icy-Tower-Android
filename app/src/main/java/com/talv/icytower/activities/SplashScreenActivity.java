package com.talv.icytower.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.talv.icytower.R;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.game.utils.BitmapUtils;

public class SplashScreenActivity extends AppCompatActivity {

    private static final boolean SKIP_SPLASHSCREEN = false;

    private static final int DELAY = 5000;
    private static final int MAX_ALPHA_DELAY = DELAY / 2;
    private static final int ALPHA_CHANGE_DELAY = 50;

    private static final int ANIMATION_SWITCH_DELAY = 500;
    private Bitmap[] bitmaps;
    private int currentBitmapIndex = 0;
    private ImageView imageView;
    private SoundPool soundPool;
    private int startupSoundId;
    private boolean firstTime = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Engine.loadCharacters(getResources());
        if (SKIP_SPLASHSCREEN) {
            endSplashScreen();
            return;
        }
        bitmaps = Engine.getCharacter1().getAnimations().get(Player.PlayerState.STANDING).getBitmapsLeft().clone();
        setContentView(R.layout.activity_splash_screen);
        imageView = findViewById(R.id.splashScreenImgView);
        imageView.setImageAlpha(0);

        soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(1).build();
        startupSoundId = soundPool.load(this, R.raw.splash_screen_startup, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(startupSoundId, 1, 1, 0, 0, 1);
            }
        });
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus || !firstTime) return;
        firstTime = false;

        int imageWidth = imageView.getMeasuredWidth();
        int imageHeight = imageView.getMeasuredHeight();
        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = BitmapUtils.stretch(bitmaps[i], imageWidth, imageHeight, false);
        }
        new Thread(() -> {
            for (int i = 0; i < DELAY / ANIMATION_SWITCH_DELAY; i++) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchAnimation();
                    }
                });
                try {
                    Thread.sleep(ANIMATION_SWITCH_DELAY);
                } catch (InterruptedException ignored) {

                }
            }

            endSplashScreen();
        }).start();
        new Thread(() -> {
            for (int i = 0; i < MAX_ALPHA_DELAY / ALPHA_CHANGE_DELAY; i++) {
                final int alpha = (int) Math.min(255, (double) i * ALPHA_CHANGE_DELAY / MAX_ALPHA_DELAY * 255);
                Log.d("alpha", String.valueOf(alpha));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageAlpha(alpha);
                    }
                });
                try {
                    Thread.sleep(ALPHA_CHANGE_DELAY);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    private void endSplashScreen() {
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    private void switchAnimation() {
        imageView.setImageBitmap(bitmaps[(currentBitmapIndex = (currentBitmapIndex + 1) % bitmaps.length)]);
    }

}