package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;
import com.talv.icytower.RectHelper;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;
import com.talv.icytower.game.platform.DisappearingPlatform;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.gui.graphiccontrols.ClockControl;
import com.talv.icytower.gui.graphiccontrols.Control;
import com.talv.icytower.gui.graphiccontrols.OnClockTimeUpListener;
import com.talv.icytower.gui.graphiccontrols.UpdatingControl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import static com.talv.icytower.gui.GUI.CONTROLS.*;

public class Engine implements OnClockTimeUpListener {

    public static final float PLAYER_SIZE_MULTIPLE = 0.7f;

    public static Paint gamePaint;
    public static Paint pausePaint;

    public static GameStats bestGameStats;
    public static UserProfileInfo userProfileInfo;
    public static String user;


    private int platformHeight;
    private final int maxPlatformWidth;
    private final int minPlatformWidth;
    private final float PLAT_CAMERA_MAX_RATIO = PLAYER_SIZE_MULTIPLE * 0.5f;
    private final float PLAT_CAMERA_MIN_RATIO = PLAYER_SIZE_MULTIPLE * 0.3f;
    public LinkedList<Platform> platforms = new LinkedList<>();


    private Bitmap backgroundImg;

    public int cameraY;
    public int cameraHeight;
    public int cameraWidth;


    public float externalCameraSpeed;
    public float constantCameraSpeed;
    private static final float CAMERA_SPEED_DECELERATION = PLAYER_SIZE_MULTIPLE * 0.004f;
    private static final float CAMERA_SPEED_ACCELERATION = CAMERA_SPEED_DECELERATION / -1.5f;
    private static final float CAMERA_CONSTANT_SPEED_INCREASE = Engine.PLAYER_SIZE_MULTIPLE * -0.0465f;

    private static final int CAMERA_SPEED_INCREASE_TIME = 15 * 1000; // 15 seconds

    private Bitmap frameScaled;
    private Bitmap frame;
    private int renderWidth;
    private int renderHeight;


    public Player player;
    private Random random;

    public GameCanvas gameCanvas;


    public enum GameState {
        PLAYING(GAMEPLAY_CONTROLS),
        PAUSED(PAUSE_MENU_CONTROLS),
        LOST(GAME_LOST_CONTROLS);

        public int controlGroup;

        GameState(int controlGroup) {
            this.controlGroup = controlGroup;
        }

    }

    public GameState currentGameState = GameState.PLAYING;

    public void updateGameState(GameState newGameState) {
        if (currentGameState != newGameState) {
            processingClick = false;
            gameCanvas.setEnabledAndVisible(currentGameState.controlGroup, false);
            gameCanvas.setEnabledAndVisible(newGameState.controlGroup, true);
            currentGameState = newGameState;
            gameCanvas.updateControlsPositions(currentGameState.controlGroup);
        }
    }

    private boolean processingClick = false;

    private int gameOverSound;
    private int gameOverStreamId;

    private MediaPlayer musicPlayer;
    public SoundPool soundPool;

    private ClockControl clock;
    private Vibrator vibrator;
    private static final VibrationEffect CLOCK_VIBRATION = VibrationEffect.createOneShot(
            1000,
            VibrationEffect.DEFAULT_AMPLITUDE
    );

    static {
        gamePaint = new Paint();
        pausePaint = new Paint();
        pausePaint.setColor(0xFF000000);
        pausePaint.setAlpha((int) (0.35f * 255f));
    }

    public Engine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        cameraWidth = ScreenScaleManager.originalWidth;
        cameraHeight = ScreenScaleManager.originalHeight;
        maxPlatformWidth = (int) (cameraWidth * PLAT_CAMERA_MAX_RATIO);
        minPlatformWidth = (int) (cameraWidth * PLAT_CAMERA_MIN_RATIO);
        random = new Random(123);

        backgroundImg = ImageHelper.stretch(BitmapFactory.decodeResource(resources, R.drawable.background_1), cameraWidth, cameraHeight, true);
        frame = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.RGB_565);
        this.gameCanvas = gameCanvas;

        initializeMediaPlayerAndSounds(context);
        gameCanvas.initialize(resources, renderWidth, renderHeight);

        initializeClock();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initializeClock() {
        clock = (ClockControl) gameCanvas.controls.get(CLOCK);
        clock.onClockTimeUpListener = this;
        clock.timeTillSpeedIncrease = CAMERA_SPEED_INCREASE_TIME;
    }

    @Override
    public long clockTimeUp(long time) {
        constantCameraSpeed += CAMERA_CONSTANT_SPEED_INCREASE;
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(musicPlayer.getPlaybackParams().getSpeed() * 1.1f));
        vibrate(CLOCK_VIBRATION);
        return CAMERA_SPEED_INCREASE_TIME;
    }

    private void vibrate(VibrationEffect vibrationEffect) {
        if (GameSettings.VIBRATE) {
            vibrator.vibrate(vibrationEffect);
        }
    }

    private void initializeMediaPlayerAndSounds(Context context) {
        musicPlayer = MediaPlayer.create(context, R.raw.background_music);
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME) // set the sound scene
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(); // set the type of sound effec
        musicPlayer.setAudioAttributes(attr);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.4f, 0.4f);

        soundPool = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(2).build();
        gameOverSound = soundPool.load(context, R.raw.game_over, 1);
    }

    public int playSound(int soundID, float rate) {
        if (GameSettings.SFX) {
            return soundPool.play(soundID, 1, 1, 0, 0, rate);
        }
        return -1;
    }

    public void onPause() {
        if (currentGameState == GameState.PLAYING) {
            musicPlayer.pause();
            updateGameState(GameState.PAUSED);
            clock.countTime = false;
        }
    }

    public void onResume() {
        processingClick = false;
        if (currentGameState == GameState.PLAYING && GameSettings.BACKG_MUSIC && !musicPlayer.isPlaying()) {
            musicPlayer.start();
        }
    }

    public void render(Canvas canvas) {
        canvas.drawBitmap(frameScaled, 0, 0, gamePaint);
    }

    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);

        /*if (isPaused) {
            gamePaint.setAlpha((int) (0.8f * 255));
        } else {
            gamePaint.setAlpha(255);
        }*/

        // draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
        // draw platforms
        for (Platform platform : platforms) {
            platform.render(bitmapCanvas, this);
        }

        //draw player
        player.render(bitmapCanvas, this);


        // final render (stretch)
        frameScaled = ImageHelper.stretch(frame, renderWidth, renderHeight, false);

        // add controls

        Canvas finalFrameCanvas = new Canvas(frameScaled);

        if (currentGameState != GameState.PLAYING) {
            finalFrameCanvas.drawRect(0, 0, renderWidth, renderHeight, pausePaint);
        }


        gameCanvas.renderControls(finalFrameCanvas);
    }

    public void updateGame(int msPassed, Context context) {
        if (Debug.LOG_MSPASSED)
            Log.d("MS PASSED", String.valueOf(msPassed));
        if (checkActive(gameCanvas.getActiveControls(), PAUSE_BTN)) {
            onPause();
        }
        if (currentGameState == GameState.PLAYING) {
            updateGameMechanics(msPassed, context);
        } else {
            if (!processingClick) {
                processingClick = processClick(context);
            }
            updateNonGamingControls(msPassed);
        }
    }


    // returns whether a button was clicked and lock out following clicks
    private boolean processClick(Context context) {
        int activeControls = gameCanvas.getActiveControls();
        int currentBit = 1 << 1;
        while (currentBit < MAX_FLAGS) {
            if ((activeControls & currentBit) == currentBit) {
                Control activeControl = gameCanvas.getControl(currentBit);
                if (activeControl.onClick != null) {
                    activeControl.onClick.OnClick(this, context);
                    return true;
                }
            }
            currentBit <<= 1;
        }
        return false;
    }

    private void updateNonGamingControls(int msPassed) {
        for (Map.Entry<Integer, Control> controlEntry : gameCanvas.controls.entrySet()) {
            if ((controlEntry.getKey() & GAMEPLAY_CONTROLS) != 0) continue;
            Control control = controlEntry.getValue();
            if (control.isVisible && control instanceof UpdatingControl) {
                ((UpdatingControl) control).update(msPassed);
            }
        }
    }

    private void updateGameMechanics(int msPassed, Context context) {
        // update and remove platforms
        Iterator<Platform> iterator = platforms.descendingIterator();
        int removed = 0;
        while (iterator.hasNext()) {
            Platform platform = iterator.next();
            if (platform.rect.top > cameraY + cameraHeight) {
                platform.recycle();
                iterator.remove();
                removed++;
                continue;
            }
            if (platform instanceof DisappearingPlatform) {
                ((DisappearingPlatform) platform).tick(msPassed);
            }
            if (!platform.enabled) {
                iterator.remove();
            }
        }
        generatePlatforms(removed);

        // update player
        player.updatePlayer(msPassed, this);

        // update camera
        int playerY = player.rect.top;
        float playerRelativeToCamera = ((playerY - cameraY) / (float) cameraHeight);
        // Log.d("hey", String.valueOf(playerRelativeToCamera));
        if (player.rect.top < cameraY + cameraHeight * 0.25f) {

            externalCameraSpeed = Math.min(externalCameraSpeed + CAMERA_SPEED_ACCELERATION * msPassed, CAMERA_CONSTANT_SPEED_INCREASE * 1.5f);
        } else {
            // decelerate external camera speed
            //externalCameraSpeed /= 2f;
            externalCameraSpeed = Math.min(0, externalCameraSpeed + msPassed * CAMERA_SPEED_DECELERATION);

        }

        cameraY += (externalCameraSpeed + constantCameraSpeed) * msPassed;


        if (constantCameraSpeed == 0 && player.rect.top < 0) {
            clock.countTime = true;
            constantCameraSpeed = CAMERA_CONSTANT_SPEED_INCREASE;
        }

        clock.update(msPassed);
        // check lost state
        if (player.rect.top > cameraY + cameraHeight) {
            // LOST!
            updateGameState(GameState.LOST);
            musicPlayer.pause();
            musicPlayer.seekTo(0);
            gameOverStreamId = playSound(gameOverSound, 1);
            updateLostUI(context);
        }


    }


    private void updateLostUI(Context context) {
        int score = player.getScore();
        gameCanvas.updateText(YOUR_SCORE_TXT, "Your Score: " + score);
        gameCanvas.updateText(GAME_STATS_TXT, "Total Jumps: " + player.totalJumps +
                "   Time: " + formatMilisecondsToString(player.totalTime) + " (sec)");

        if (bestGameStats == null) {
            // feature disabled
            gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, false);
            gameCanvas.setEnabledAndVisible(PERSONAL_HIGH_SCORE_TXT, false);
        } else {
            gameCanvas.setEnabledAndVisible(PERSONAL_HIGH_SCORE_TXT, true);
            if (score > bestGameStats.highscore) {
                gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, true);
                bestGameStats.highscore = score;
                bestGameStats.timeTaken = player.totalTime;
                bestGameStats.totalJumps = player.totalJumps;

                FirebaseHelper.setBestGameStats(user, bestGameStats, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(context, "Game stats update failed - " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, false);
            }
            gameCanvas.updateText(PERSONAL_HIGH_SCORE_TXT, "Highscore: " + bestGameStats.highscore);
        }
        if (userProfileInfo != null) {
            // feature enabled
            userProfileInfo.gamesPlayed++;
            FirebaseHelper.setUserProfileInfo(user, userProfileInfo, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, "User profile info update failed - " + exception.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static String formatMilisecondsToString(long time) {
        return String.valueOf(time / 1000) + "." + String.valueOf(Math.round((time % 1000) / 100f));

    }

    public static final int PLATFORMS_BETWEEN_LEVELS = 20;

    public void generatePlatforms(int count) {
        int lastPlatformY = 0;
        int lastPlatformNum = 0;
        if (platforms.isEmpty()) {
            Log.wtf("PLATFORMS EMPTY", "platfors.isEmpty() == true");
        } else {
            Platform last = platforms.getLast();
            lastPlatformY = last.rect.top;
            lastPlatformNum = last.platformNumber;
        }
        int distanceBetweenPlatforms = (int) (player.rect.height() * 0.9f);
        int height = platformHeight;
        for (int i = 0; i < count; i++) {
            int y = lastPlatformY - distanceBetweenPlatforms - height;
            lastPlatformNum++;
            int width;
            int x;
            if (lastPlatformNum != 0 && lastPlatformNum % PLATFORMS_BETWEEN_LEVELS == 0) {
                width = cameraWidth;
                x = 0;
            } else {
                width = random.nextInt(maxPlatformWidth - minPlatformWidth) + minPlatformWidth;
                x = random.nextInt(cameraWidth - width);

            }

            platforms.add(new Platform(Platform.PlatformTypes.BASIC_0, lastPlatformNum, x, y, width, height));
            lastPlatformY = y;
        }
    }

    private void clearPlatforms() {
        Iterator<Platform> iterator = platforms.descendingIterator();
        while (iterator.hasNext()) {
            iterator.next().image.recycle();
            iterator.remove();
        }
    }


    public void resetLevel(Resources resources) {
        cameraY = 0;
        clearPlatforms();
        Platform.loadBitmaps(resources, 0);
        int playerHeight = (int) (58 * player.playerSizeMultiple);
        int playerWidth = (int) (30 * player.playerSizeMultiple);
        platformHeight = (int) (playerHeight * 0.6f);
        Platform groundPlatform = new Platform(Platform.PlatformTypes.BASIC_0, 0,
                0, cameraHeight - platformHeight - (int) (0.05f * cameraHeight), cameraWidth, platformHeight);
        platforms.add(groundPlatform);
        player.rect = RectHelper.rectFromWidthHeight(cameraWidth / 2, groundPlatform.rect.top - playerHeight, playerWidth, playerHeight);
        player.resetPlayer();
        generatePlatforms((int) Math.ceil(cameraHeight / (float) (playerHeight)) * 2);
        externalCameraSpeed = 0f;
        constantCameraSpeed = 0f;
        player.updateScore(0, gameCanvas);
        soundPool.stop(gameOverStreamId);
        clock.currentTime = 0;
        clock.timeTillSpeedIncrease = CAMERA_SPEED_INCREASE_TIME;
        clock.countTime = false;
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1f));
    }


}
