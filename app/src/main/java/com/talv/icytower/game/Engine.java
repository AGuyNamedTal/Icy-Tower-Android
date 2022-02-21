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
import android.os.AsyncTask;
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
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.gui.graphiccontrols.ClockControl;
import com.talv.icytower.gui.graphiccontrols.Control;
import com.talv.icytower.gui.graphiccontrols.OnClockTimeUpListener;
import com.talv.icytower.gui.graphiccontrols.UpdatingControl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.talv.icytower.gui.GUI.CONTROLS.*;

public class Engine implements OnClockTimeUpListener {

    public static Character character1;
    public static Character character2;

    public static final float PLAYER_SIZE_MULTIPLE = 0.7f;

    public static Paint gamePaint;
    public static Paint pausePaint;

    public static GameStats bestGameStats;
    public static UserProfileInfo userProfileInfo;
    public static String user;


    private final int maxPlatformWidth;
    private final int minPlatformWidth;
    private final float PLAT_CAMERA_MAX_RATIO = PLAYER_SIZE_MULTIPLE * 0.5f;
    private final float PLAT_CAMERA_MIN_RATIO = PLAYER_SIZE_MULTIPLE * 0.3f;
    public LinkedList<Platform> platforms = new LinkedList<>();


    protected Bitmap backgroundImg;

    public int cameraY;
    public static final int CAMERA_WIDTH = 250;
    public static final int CAMERA_HEIGHT = 550;


    public float externalCameraSpeed;
    public float constantCameraSpeed;
    private static final float CAMERA_SPEED_DECELERATION = PLAYER_SIZE_MULTIPLE * 0.004f;
    private static final float CAMERA_SPEED_ACCELERATION = CAMERA_SPEED_DECELERATION / -1.5f;
    private static final float CAMERA_CONSTANT_SPEED_INCREASE = Engine.PLAYER_SIZE_MULTIPLE * -0.0465f;

    private static final int CAMERA_SPEED_INCREASE_TIME = 15 * 1000; // 15 seconds

    protected Bitmap frameScaled;
    protected Bitmap frame;
    protected int renderWidth;
    protected int renderHeight;


    public Player player;
    private Random random;

    public GameCanvas gameCanvas;

    public AtomicBoolean touchRestricted = new AtomicBoolean(false);

    public enum GameState {
        PLAYING(GAMEPLAY_CONTROLS),
        PAUSED(PAUSE_MENU_CONTROLS),
        CHOOSING_CHAR(CHOOSE_PLAYER_CONTROLS),
        LOST(GAME_LOST_CONTROLS);

        public int controlGroup;

        GameState(int controlGroup) {
            this.controlGroup = controlGroup;
        }

    }

    public GameState currentGameState = GameState.PLAYING;

    public void updateGameState(GameState newGameState) {
        if (currentGameState != newGameState) {
            gameCanvas.setEnabledAndVisible(currentGameState.controlGroup, false);
            gameCanvas.setEnabledAndVisible(newGameState.controlGroup, true);
            currentGameState = newGameState;
            gameCanvas.updateControlsPositions(currentGameState.controlGroup);
            if (newGameState != GameState.PLAYING) {
                restrictTouch(400);
            }
        }
    }

    protected boolean processingClick = false;

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

    public static final int PLATFORMS_BETWEEN_LEVELS = 20;
    private static final int REGULAR_TO_DISAPPEARING_PLATFORM_RATIO = 7;

    public static void loadCharacters(Resources resources) {
        character1 = Character.loadPlayer1(resources, Engine.PLAYER_SIZE_MULTIPLE);
        character2 = Character.loadPlayer2(resources, Engine.PLAYER_SIZE_MULTIPLE);
    }

    protected int pauseBtnID;

    public Engine(int renderWidth, int renderHeight, Resources resources, GameCanvas
            gameCanvas, Context context) {
        pauseBtnID = PAUSE_BTN;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        maxPlatformWidth = (int) (CAMERA_WIDTH * PLAT_CAMERA_MAX_RATIO);
        minPlatformWidth = (int) (CAMERA_WIDTH * PLAT_CAMERA_MIN_RATIO);

    }

    public void initialize(int renderWidth, int renderHeight, Resources resources, GameCanvas
            gameCanvas, Context context) {

        initializeMediaPlayerAndSounds(context);
        random = new Random();
        backgroundImg = ImageHelper.stretch(BitmapFactory.decodeResource(resources, R.drawable.background_1), CAMERA_WIDTH, CAMERA_HEIGHT, true);
        frame = Bitmap.createBitmap(CAMERA_WIDTH, CAMERA_HEIGHT, Bitmap.Config.ARGB_8888);
        this.gameCanvas = gameCanvas;
        gameCanvas.initializeGUI(resources, renderWidth, renderHeight);
        initializeClock();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        updateGameState(GameState.CHOOSING_CHAR);
        int platformHeight = (int) (character1.height * 0.6f);
        Platform.loadBitmaps(resources, platformHeight);
        player = new Player(soundPool, context);
    }

    public void restrictTouch(int ms) {
        touchRestricted.set(true);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    touchRestricted.set(false);
                }
            }
        });
    }

    public void reset() {
        cameraY = 0;
        externalCameraSpeed = 0f;
        constantCameraSpeed = 0f;
        player.resetPlayer();
        player.updateScore(0, gameCanvas);
        soundPool.stop(gameOverStreamId);
        clock.currentTime = 0;
        clock.timeTillSpeedIncrease = CAMERA_SPEED_INCREASE_TIME;
        clock.countTime = false;
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1f));
        clearPlatforms();

    }

    public void setPlayerCharacter(Character character) {
        clearPlatforms();
        Platform groundPlatform = new Platform(Platform.PlatformTypes.LEVEL_0, 0,
                0, CAMERA_HEIGHT - Platform.getPlatformHeight() - (int) (0.05f * CAMERA_HEIGHT), CAMERA_WIDTH, false);
        platforms.add(groundPlatform);
        generatePlatforms((int) Math.ceil(CAMERA_HEIGHT / (float) (character1.height)) * 2);
        player.setCharacter(character);
        RectHelper.setRectPos(player.rect, (CAMERA_WIDTH - player.rect.width()) / 2,
                platforms.peekFirst().rect.top - player.rect.height());
    }

    private void initializeMediaPlayerAndSounds(Context context) {
        musicPlayer = MediaPlayer.create(context, R.raw.background_music);
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        musicPlayer.setAudioAttributes(attr);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.4f, 0.4f);

        soundPool = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(2).build();
        gameOverSound = soundPool.load(context, R.raw.game_over, 1);
    }

    private void initializeClock() {
        clock = (ClockControl) gameCanvas.controls.get(CLOCK);
        clock.onClockTimeUpListener = this;
        clock.timeTillSpeedIncrease = CAMERA_SPEED_INCREASE_TIME;
    }

    public int playSound(int soundID, float rate) {
        if (GameSettings.SFX) {
            return soundPool.play(soundID, 1, 1, 0, 0, rate);
        }
        return -1;
    }

    private void vibrate(VibrationEffect vibrationEffect) {
        if (GameSettings.VIBRATE) {
            vibrator.vibrate(vibrationEffect);
        }
    }

    public void stopBackgroundMusic() {
        musicPlayer.pause();
        musicPlayer.seekTo(0);
    }

    @Override
    public long clockTimeUp(long time) {
        constantCameraSpeed += CAMERA_CONSTANT_SPEED_INCREASE;
        musicPlayer.setPlaybackParams(new PlaybackParams().setSpeed(musicPlayer.getPlaybackParams().getSpeed() * 1.1f));
        vibrate(CLOCK_VIBRATION);
        return CAMERA_SPEED_INCREASE_TIME;
    }


    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);
        // draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
        // draw platforms
        for (Platform platform : platforms) {
            if (platform.rect.bottom >= cameraY) {
                platform.render(bitmapCanvas, this);
            }
        }
        //draw player
        player.render(bitmapCanvas, this);

        // final render (stretch)
        frameScaled = ImageHelper.stretch(frame, renderWidth, renderHeight, false);

        // add controls
        Canvas finalFrameCanvas = new Canvas(frameScaled);
        if (currentGameState == GameState.PAUSED || currentGameState == GameState.LOST) {
            // reduce brightness of background game
            finalFrameCanvas.drawRect(0, 0, renderWidth, renderHeight, pausePaint);
        }
        gameCanvas.renderControls(finalFrameCanvas);
    }

    public void updateGame(int msPassed, Context context) {
        if (Debug.LOG_MSPASSED)
            Log.d("MS PASSED", String.valueOf(msPassed));
        if (checkActive(gameCanvas.getActiveControls(), pauseBtnID)) {
            onPause();
        }
        if (currentGameState == GameState.PLAYING) {
            updateGameMechanics(msPassed, context);
        } else {
            if (!processingClick && !touchRestricted.get()) {
                GameState old = currentGameState;
                processingClick = processClick(context);
                GameState newGameState = currentGameState;
                if (newGameState != old) {
                    processingClick = false;
                }
            }
            updateNonGamingControls(msPassed);
        }
    }

    public void render(Canvas canvas) {
        canvas.drawBitmap(frameScaled, 0, 0, gamePaint);
    }

    public void onResume() {
        processingClick = false;
        if (currentGameState == GameState.PLAYING) {
            if (GameSettings.BACKG_MUSIC && !musicPlayer.isPlaying()) {
                musicPlayer.start();
            }
            activateClockIfNeeded();
        }
    }

    public void onPause() {
        if (currentGameState == GameState.PLAYING) {
            musicPlayer.pause();
            updateGameState(GameState.PAUSED);
            clock.countTime = false;
        }
    }


    private void activateClockIfNeeded() {
        if (player.rect.top < 0) {
            clock.countTime = true;
            constantCameraSpeed = Math.min(CAMERA_CONSTANT_SPEED_INCREASE, constantCameraSpeed);
        }
    }

    protected void updateNonGamingControls(int msPassed) {
        for (Map.Entry<Integer, Control> controlEntry : gameCanvas.controls.entrySet()) {
            if ((controlEntry.getKey() & GAMEPLAY_CONTROLS) != 0) continue;
            Control control = controlEntry.getValue();
            if (control.isVisible && control instanceof UpdatingControl) {
                ((UpdatingControl) control).update(msPassed);
            }
        }
    }

    protected void updateGameMechanics(int msPassed, Context context) {
        updatePlatforms(msPassed);
        updatePlayer(msPassed);
        updateCamera(msPassed);
        activateClockIfNeeded();
        clock.update(msPassed);
        checkLost(context);
    }

    private void updatePlatforms(int msPassed) {
        // update and remove platforms
        Iterator<Platform> iterator = platforms.descendingIterator();
        int removed = 0;
        while (iterator.hasNext()) {
            Platform platform = iterator.next();
            boolean removePlat = false;
            if (platform instanceof DisappearingPlatform) {
                removePlat = ((DisappearingPlatform) platform).tick(msPassed);
            }
            if (removePlat || platform.rect.top > cameraY + CAMERA_HEIGHT) {
                platform.recycle();
                iterator.remove();
                removed++;
                continue;
            }
        }
        generatePlatforms(removed);
    }

    private void updatePlayer(int msPassed) {
        int activeControls = gameCanvas.getActiveControls() & PLAYER_MOVEMENT_CONTROLS;
        if (checkActive(activeControls, ARROW_LEFT) &&
                checkActive(activeControls, ARROW_RIGHT)) {
            activeControls &= ~ARROW_LEFT;
            activeControls &= ~ARROW_RIGHT;
        }
        // update player
        player.updatePlayer(msPassed, this, activeControls);
    }

    private void updateCamera(int msPassed) {
        if (player.rect.top < cameraY + CAMERA_HEIGHT * 0.25f) {
            externalCameraSpeed = Math.min(externalCameraSpeed + CAMERA_SPEED_ACCELERATION * msPassed, CAMERA_CONSTANT_SPEED_INCREASE * 1.5f);
        } else {
            // decelerate external camera speed
            externalCameraSpeed = Math.min(0, externalCameraSpeed + msPassed * CAMERA_SPEED_DECELERATION);

        }
        cameraY += (externalCameraSpeed + constantCameraSpeed) * msPassed;
    }

    private void checkLost(Context context) {
        if (player.rect.top > cameraY + CAMERA_HEIGHT) {
            // LOST!
            updateGameState(GameState.LOST);
            stopBackgroundMusic();
            gameOverStreamId = playSound(gameOverSound, 1);
            updateLostUI(context);
        }
    }

    private void updateLostUI(Context context) {
        int score = player.getScore();
        gameCanvas.updateText(YOUR_SCORE_TXT, "Your Score: " + score);
        gameCanvas.updateText(GAME_STATS_TXT, "Total Jumps: " + player.totalJumps +
                "   Time: " + formatGameTimeToString(player.totalTime) + " (sec)");

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
        int distanceBetweenPlatforms = (int) (character1.height * 0.9f);
        int height = Platform.getPlatformHeight();
        for (int i = 0; i < count; i++) {
            int y = lastPlatformY - distanceBetweenPlatforms - height;
            lastPlatformNum++;
            int width;
            boolean drawCorners;
            int x;
            boolean fullLevel = lastPlatformNum % PLATFORMS_BETWEEN_LEVELS == 0;
            if (lastPlatformNum != 0 && fullLevel) {
                width = CAMERA_WIDTH;
                drawCorners = false;
                x = 0;
            } else {
                width = random.nextInt(maxPlatformWidth - minPlatformWidth) + minPlatformWidth;
                x = random.nextInt(CAMERA_WIDTH - width);
                drawCorners = true;
            }
            Platform.PlatformTypes platformLevel = Platform.PLATFORM_TYPE_BY_LEVEL[Math.min(Platform.PLATFORM_TYPE_BY_LEVEL.length - 1, lastPlatformNum / PLATFORMS_BETWEEN_LEVELS)];

            Platform newPlatform;
            if (fullLevel || random.nextInt(REGULAR_TO_DISAPPEARING_PLATFORM_RATIO) != 0) {
                newPlatform = new Platform(platformLevel, lastPlatformNum, x, y, width, drawCorners);
            } else {
                newPlatform = new DisappearingPlatform(platformLevel, lastPlatformNum, x, y, width, drawCorners);
            }

            platforms.add(newPlatform);
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

    // returns whether a button was clicked and lock out following clicks
    protected boolean processClick(Context context) {
        int activeControls = gameCanvas.getActiveControls();
        int currentBit = 1 << 1;
        while (currentBit < MAX_FLAGS) {
            if ((activeControls & currentBit) == currentBit) {
                Control activeControl = gameCanvas.getControl(currentBit);
                if (activeControl.onTouch != null) {
                    activeControl.onTouch.onTouch(this, context);
                    return true;
                }
            }
            currentBit <<= 1;
        }
        return false;
    }


    public static String formatGameTimeToString(long time) {
        return String.valueOf(time / 1000) + "." + String.valueOf(Math.round((time % 1000) / 100f));
    }


}
