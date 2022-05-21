package com.talv.icytower.game.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;
import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.GameSettings;
import com.talv.icytower.game.gui.graphiccontrols.ClockControl;
import com.talv.icytower.game.gui.graphiccontrols.Control;
import com.talv.icytower.game.gui.graphiccontrols.OnClockTimeUpListener;
import com.talv.icytower.game.gui.graphiccontrols.UpdatingControl;
import com.talv.icytower.game.musicService.MusicServiceConnection;
import com.talv.icytower.game.platform.DisappearingPlatform;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.game.utils.RectUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.talv.icytower.game.gui.GUI.CONTROLS.ARROW_LEFT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.ARROW_RIGHT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.CHOOSE_PLAYER_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.CLOCK;
import static com.talv.icytower.game.gui.GUI.CONTROLS.GAMEPLAY_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.GAME_LOST_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.GAME_STATS_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.MAX_FLAGS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.NEW_HIGH_SCORE_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PAUSE_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PAUSE_MENU_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PERSONAL_HIGH_SCORE_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_MOVEMENT_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.YOUR_SCORE_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.checkActive;

public abstract class Engine implements OnClockTimeUpListener {

    // frame and render sizes
    private Bitmap frameScaled;
    private Bitmap frame;
    private int renderWidth;
    private int renderHeight;


    // characters
    private static Character character1;
    private static Character character2;

    public static void loadCharacters(Resources resources) {
        character1 = Character.loadPlayer1(resources, Engine.PLAYER_SIZE_MULTIPLE);
        character2 = Character.loadPlayer2(resources, Engine.PLAYER_SIZE_MULTIPLE);
    }

    public static final float PLAYER_SIZE_MULTIPLE = 0.7f;

    // paints
    private static final Paint gamePaint;
    private static final Paint pausePaint;

    // player/firebase info
    private static GameStats bestGameStats;
    private static UserProfileInfo userProfileInfo;
    private static String user;


    // platforms
    private final int maxPlatformWidth;
    private final int minPlatformWidth;
    private final float PLAT_CAMERA_MAX_RATIO = PLAYER_SIZE_MULTIPLE * 0.55f;
    private final float PLAT_CAMERA_MIN_RATIO = PLAYER_SIZE_MULTIPLE * 0.35f;
    private final LinkedList<Platform> platforms = new LinkedList<>();
    private final Random random;
    public static final int PLATFORMS_BETWEEN_LEVELS = 20;
    private static final int REGULAR_TO_DISAPPEARING_PLATFORM_RATIO = 7;


    protected static Bitmap backgroundImg;

    // camera
    private int cameraY;
    public static final int CAMERA_WIDTH = 250;
    public static final int CAMERA_HEIGHT = 550;


    // camera speed
    private float externalCameraSpeed;
    private float constantCameraSpeed;
    private static final float CAMERA_SPEED_DECELERATION = PLAYER_SIZE_MULTIPLE * 0.004f;
    private static final float CAMERA_SPEED_ACCELERATION = CAMERA_SPEED_DECELERATION / -1.5f;
    private static final float CAMERA_CONSTANT_SPEED_INCREASE = Engine.PLAYER_SIZE_MULTIPLE * -0.0465f;

    private static final int CAMERA_SPEED_INCREASE_TIME = 15 * 1000; // 15 seconds


    private Player player;

    // canvas and touch related
    private GameCanvas gameCanvas;
    private AtomicBoolean touchRestricted = new AtomicBoolean(false);
    protected boolean processingClick = false;


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

    private GameState currentGameState = GameState.PLAYING;

    public GameState getCurrentGameState() {
        return currentGameState;
    }


    public void setGameState(GameState newGameState) {
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

    // sounds
    private SoundPool soundPool;
    private int gameOverSound;
    private int gameOverStreamId;
    private final MusicServiceConnection musicServiceConnection;


    private final Vibrator vibrator;
    private static final VibrationEffect CLOCK_VIBRATION = VibrationEffect.createOneShot(
            1000,
            VibrationEffect.DEFAULT_AMPLITUDE
    );


    // game clock
    private ClockControl clock;

    static {
        // initialize paints
        gamePaint = new Paint();
        pausePaint = new Paint();
        pausePaint.setColor(0xFF000000);
        pausePaint.setAlpha((int) (0.35f * 255f));
    }


    protected int pauseBtnID;

    // Getters and setters
    public static Character getCharacter1() {
        return character1;
    }

    public static Character getCharacter2() {
        return character2;
    }

    public static Paint getGamePaint() {
        return gamePaint;
    }

    public static Paint getPausePaint() {
        return pausePaint;
    }

    public static void setBestGameStats(GameStats bestGameStats) {
        Engine.bestGameStats = bestGameStats;
    }

    public static void setUserProfileInfo(UserProfileInfo userProfileInfo) {
        Engine.userProfileInfo = userProfileInfo;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        Engine.user = user;
    }

    protected Bitmap getFrameScaled() {
        return frameScaled;
    }

    protected void setFrameScaled(Bitmap frameScaled) {
        this.frameScaled = frameScaled;
    }

    protected Bitmap getFrame() {
        return frame;
    }

    protected void setFrame(Bitmap frame) {
        this.frame = frame;
    }

    protected int getRenderWidth() {
        return renderWidth;
    }

    protected void setRenderWidth(int renderWidth) {
        this.renderWidth = renderWidth;
    }

    protected int getRenderHeight() {
        return renderHeight;
    }

    protected void setRenderHeight(int renderHeight) {
        this.renderHeight = renderHeight;
    }

    public LinkedList<Platform> getPlatforms() {
        return platforms;
    }

    public int getCameraY() {
        return cameraY;
    }

    public Player getPlayer() {
        return player;
    }

    public GameCanvas getGameCanvas() {
        return gameCanvas;
    }

    public SoundPool getSoundPool() {
        return soundPool;
    }

    public MusicServiceConnection getMusicServiceConnection() {
        return musicServiceConnection;
    }


    public Engine(int renderWidth, int renderHeight, Resources resources, GameCanvas
            gameCanvas, Context context, MusicServiceConnection musicServiceConnection) {
        this.musicServiceConnection = musicServiceConnection;
        pauseBtnID = PAUSE_BTN;
        this.setRenderWidth(renderWidth);
        this.setRenderHeight(renderHeight);
        maxPlatformWidth = (int) (CAMERA_WIDTH * PLAT_CAMERA_MAX_RATIO);
        minPlatformWidth = (int) (CAMERA_WIDTH * PLAT_CAMERA_MIN_RATIO);
        initializeMediaPlayerAndSounds(context);
        random = new Random();
        if (backgroundImg == null) {
            backgroundImg = BitmapUtils.stretch(BitmapFactory.decodeResource(resources, R.drawable.background), CAMERA_WIDTH, CAMERA_HEIGHT, true);
        }
        setFrame(Bitmap.createBitmap(CAMERA_WIDTH, CAMERA_HEIGHT, Bitmap.Config.ARGB_8888));
        this.gameCanvas = gameCanvas;
        gameCanvas.initializeGUI(resources, renderWidth, renderHeight);
        initializeClock();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        int platformHeight = (int) (character1.getHeight() * 0.6f);
        Platform.loadBitmaps(resources, platformHeight);
        player = new Player(soundPool, context);
    }

    public void reset() {
        cameraY = 0;
        externalCameraSpeed = 0f;
        constantCameraSpeed = 0f;
        clock.setCurrentTime(0);
        clock.setTimeTillSpeedIncrease(CAMERA_SPEED_INCREASE_TIME);
        clock.setCountTime(false);
        stopGameOver();
        musicServiceConnection.resetSpeed();
        clearPlatforms();
        player.resetPlayer();
        player.updateScore(0, gameCanvas);
    }

    private void initializeMediaPlayerAndSounds(Context context) {
        AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        soundPool = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(2).build();
        gameOverSound = soundPool.load(context, R.raw.game_over, 1);
    }

    private void initializeClock() {
        clock = (ClockControl) gameCanvas.getControls().get(CLOCK);
        clock.setOnClockTimeUpListener(this);
        clock.setTimeTillSpeedIncrease(CAMERA_SPEED_INCREASE_TIME);
    }

    public void restrictTouch(int ms) {
        touchRestricted.set(true);
        AsyncTask.execute(() -> {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                touchRestricted.set(false);
            }
        });
    }


    public void stopGameOver() {
        soundPool.stop(gameOverStreamId);
    }

    public void clearPlayerCharacter() {
        player.setCharacter(null);
    }

    protected void setPlayerCharacter(Character character) {
        clearPlatforms();
        Platform groundPlatform = new Platform(Platform.PlatformTypes.LEVEL_0, 0,
                0, CAMERA_HEIGHT - Platform.getPlatformHeight() - (int) (0.05f * CAMERA_HEIGHT), CAMERA_WIDTH, false);
        platforms.add(groundPlatform);
        generatePlatforms((int) Math.ceil(CAMERA_HEIGHT / (float) (character1.getHeight())) * 2);
        player.setCharacter(character);
        RectUtils.setRectPos(player.getRect(), (CAMERA_WIDTH - player.getRect().width()) / 2,
                platforms.peekFirst().getRect().top - player.getRect().height());

    }

    public void startGame(Character character) {
        reset();
        setPlayerCharacter(character);
        setGameState(Engine.GameState.PLAYING);
        onResume();
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

    @Override
    public long clockTimeUp(long time) {
        constantCameraSpeed += CAMERA_CONSTANT_SPEED_INCREASE;
        musicServiceConnection.increaseSpeed();
        vibrate(CLOCK_VIBRATION);
        return CAMERA_SPEED_INCREASE_TIME;
    }

    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(getFrame());
        // draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
        // draw platforms
        for (Platform platform : platforms) {
            if (platform.getRect().bottom >= cameraY) {
                platform.render(bitmapCanvas, this);
            }
        }
        //draw player
        player.render(bitmapCanvas, this);

        // final render (stretch)
        setFrameScaled(BitmapUtils.stretch(getFrame(), getRenderWidth(), getRenderHeight(), false));

        // add controls
        Canvas finalFrameCanvas = new Canvas(getFrameScaled());
        if (currentGameState == GameState.PAUSED || currentGameState == GameState.LOST) {
            // reduce brightness of background game
            finalFrameCanvas.drawRect(0, 0, getRenderWidth(), getRenderHeight(), pausePaint);
        }
        gameCanvas.renderControls(finalFrameCanvas);
    }

    public void updateGame(int msPassed, Context context) {
        int activeControls = gameCanvas.getActiveControls().get();
        if (checkActive(activeControls, pauseBtnID)) {
            onPause();
        }
        if (currentGameState == GameState.PLAYING) {
            updateGameMechanics(msPassed, context, activeControls);
        } else {
            if (!processingClick && !touchRestricted.get()) {
                GameState old = currentGameState;
                processingClick = processClick(context, activeControls);
                GameState newGameState = currentGameState;
                if (newGameState != old) {
                    processingClick = false;
                }
            }
            updateNonGamingControls(msPassed);
        }
    }

    public void render(Canvas canvas) {
        canvas.drawBitmap(getFrameScaled(), 0, 0, gamePaint);
    }

    public void onResume() {
        processingClick = false;
        if (currentGameState == GameState.PLAYING) {
            if (GameSettings.BACKG_MUSIC && !musicServiceConnection.isPlaying()) {
                musicServiceConnection.start();
            }
            activateClockIfNeeded();
        }
    }

    public void onPause() {
        stopGameOver();
        if (currentGameState == GameState.PLAYING) {
            musicServiceConnection.pause();
            setGameState(GameState.PAUSED);
            clock.setCountTime(false);
        }
    }


    protected void activateClockIfNeeded() {
        if (minPlayerY() < 0) {
            activateClock();
        }
    }

    protected void activateClock() {
        clock.setCountTime(true);
        constantCameraSpeed = Math.min(CAMERA_CONSTANT_SPEED_INCREASE, constantCameraSpeed);
    }

    protected void updateNonGamingControls(int msPassed) {
        for (Map.Entry<Integer, Control> controlEntry : gameCanvas.getControls().entrySet()) {
            if ((controlEntry.getKey() & GAMEPLAY_CONTROLS) != 0) continue;
            Control control = controlEntry.getValue();
            if (control.isVisible() && control instanceof UpdatingControl) {
                ((UpdatingControl) control).update(msPassed);
            }
        }
    }

    protected void updateGameMechanics(int msPassed, Context context, int activeControls) {
        updatePlatforms(msPassed);
        updatePlayer(msPassed, activeControls);
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
            if (removePlat || platform.getRect().top > cameraY + CAMERA_HEIGHT) {
                platform.recycle();
                iterator.remove();
                removed++;
            }
        }
        generatePlatforms(removed);
    }

    public void generatePlatforms(int count) {
        int lastPlatformY = 0;
        int lastPlatformNum = 0;
        if (platforms.isEmpty()) {
            Log.wtf("PLATFORMS EMPTY", "platfors.isEmpty() == true");
        } else {
            Platform last = platforms.getLast();
            lastPlatformY = last.getRect().top;
            lastPlatformNum = last.getPlatformNumber();
        }
        int distanceBetweenPlatforms = (int) (character1.getHeight() * 0.9f);
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
            Platform.PlatformTypes platformLevel = Platform.getPlatformTypeByLevel()[Math.min(Platform.getPlatformTypeByLevel().length - 1, lastPlatformNum / PLATFORMS_BETWEEN_LEVELS)];

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
            iterator.next().getImage().recycle();
            iterator.remove();
        }
    }

    protected void updatePlayer(int msPassed, int activeControls) {
        int gameControls = getGameControls(activeControls);
        player.updatePlayer(msPassed, this, gameControls);
    }

    protected int getGameControls(int activeControls) {
        activeControls = activeControls & PLAYER_MOVEMENT_CONTROLS;
        if (checkActive(activeControls, ARROW_LEFT) &&
                checkActive(activeControls, ARROW_RIGHT)) {
            activeControls &= ~ARROW_LEFT;
            activeControls &= ~ARROW_RIGHT;
        }
        return activeControls;
    }

    abstract int minPlayerY();

    abstract int maxPlayerY();

    protected void updateCamera(int msPassed) {
        if (minPlayerY() < cameraY + CAMERA_HEIGHT * 0.25f) {
            accelerateExternalCam(msPassed);
        } else {
            decelerateExternalCam(msPassed);
        }
        cameraY += (externalCameraSpeed + constantCameraSpeed) * msPassed;
    }

    protected void accelerateExternalCam(int msPassed) {
        externalCameraSpeed = Math.min(externalCameraSpeed + CAMERA_SPEED_ACCELERATION * msPassed, CAMERA_CONSTANT_SPEED_INCREASE * 1.5f);
    }

    protected void decelerateExternalCam(int msPassed) {
        externalCameraSpeed = Math.min(0, externalCameraSpeed + msPassed * CAMERA_SPEED_DECELERATION);
    }

    protected void checkLost(Context context) {
        if (maxPlayerY() > cameraY + CAMERA_HEIGHT) {
            // LOST!
            setGameState(GameState.LOST);
            musicServiceConnection.stop();
            gameOverStreamId = playSound(gameOverSound, 1);
            updateLostUI(context, getWinningPlayer());
        }
    }

    abstract Player getWinningPlayer();

    protected void updateLostUI(Context context, Player winningPlayer) {
        int score = winningPlayer.getScore();
        gameCanvas.updateText(YOUR_SCORE_TXT, "Your Score: " + score);
        gameCanvas.updateText(GAME_STATS_TXT, "Total Jumps: " + winningPlayer.getTotalJumps() +
                "   Time: " + formatGameTimeToString(winningPlayer.getTotalTime()) + " (sec)");

        if (bestGameStats == null) {
            // feature disabled
            gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, false);
            gameCanvas.setEnabledAndVisible(PERSONAL_HIGH_SCORE_TXT, false);
        } else {
            gameCanvas.setEnabledAndVisible(PERSONAL_HIGH_SCORE_TXT, true);
            if (score > bestGameStats.getHighscore()) {
                gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, true);
                bestGameStats.setHighscore(score);
                bestGameStats.setTimeTaken(winningPlayer.getTotalTime());
                bestGameStats.setTotalJumps(winningPlayer.getTotalJumps());

                FirebaseHelper.setBestGameStats(user, bestGameStats, (OnFailureListener) exception -> Toast.makeText(context, "Game stats update failed - " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show());
            } else {
                gameCanvas.setEnabledAndVisible(NEW_HIGH_SCORE_TXT, false);
            }
            gameCanvas.updateText(PERSONAL_HIGH_SCORE_TXT, "Highscore: " + bestGameStats.getHighscore());
        }
        if (userProfileInfo != null) {
            // feature enabled
            userProfileInfo.setGamesPlayed(userProfileInfo.getGamesPlayed() + 1);
            FirebaseHelper.setUserProfileInfo(user, userProfileInfo, (OnFailureListener) exception -> Toast.makeText(context, "User profile info update failed - " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show());
        }
    }


    // returns whether a button was clicked and lock out following clicks
    protected boolean processClick(Context context, int activeControls) {
        int currentBit = 1 << 1;
        while (currentBit < MAX_FLAGS) {
            if (checkActive(activeControls, currentBit)) {
                Control activeControl = gameCanvas.getControl(currentBit);
                if (activeControl.getOnTouch() != null) {
                    activeControl.getOnTouch().onTouch(this, context);
                    return true;
                }
            }
            currentBit <<= 1;
        }
        return false;
    }


    public static String formatGameTimeToString(long time) {
        return time / 1000 + "." + Math.round((time % 1000) / 100f);
    }

    @Override
    public String toString() {
        return "Engine{" +
                "frameScaled=" + frameScaled +
                ", frame=" + frame +
                ", renderWidth=" + renderWidth +
                ", renderHeight=" + renderHeight +
                ", maxPlatformWidth=" + maxPlatformWidth +
                ", minPlatformWidth=" + minPlatformWidth +
                ", PLAT_CAMERA_MAX_RATIO=" + PLAT_CAMERA_MAX_RATIO +
                ", PLAT_CAMERA_MIN_RATIO=" + PLAT_CAMERA_MIN_RATIO +
                ", platforms=" + platforms +
                ", backgroundImg=" + backgroundImg +
                ", cameraY=" + cameraY +
                ", externalCameraSpeed=" + externalCameraSpeed +
                ", constantCameraSpeed=" + constantCameraSpeed +
                ", player=" + player +
                ", random=" + random +
                ", gameCanvas=" + gameCanvas +
                ", touchRestricted=" + touchRestricted +
                ", currentGameState=" + currentGameState +
                ", processingClick=" + processingClick +
                ", gameOverSound=" + gameOverSound +
                ", gameOverStreamId=" + gameOverStreamId +
                ", soundPool=" + soundPool +
                ", clock=" + clock +
                ", vibrator=" + vibrator +
                ", pauseBtnID=" + pauseBtnID +
                ", musicServiceConnection=" + musicServiceConnection +
                '}';
    }

}
