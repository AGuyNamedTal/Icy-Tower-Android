package com.talv.icytower.gui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;
import com.talv.icytower.RectHelper;
import com.talv.icytower.activities.MainActivity;
import com.talv.icytower.activities.SettingsActivity;
import com.talv.icytower.game.Engine;
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.gui.graphiccontrols.ClockControl;
import com.talv.icytower.gui.graphiccontrols.Control;
import com.talv.icytower.gui.graphiccontrols.HighscoreControl;
import com.talv.icytower.gui.graphiccontrols.ImageControl;
import com.talv.icytower.gui.graphiccontrols.OnButtonClickListener;
import com.talv.icytower.gui.graphiccontrols.RectControl;
import com.talv.icytower.gui.graphiccontrols.TextControl;

import java.util.HashMap;
import java.util.Map;

import static com.talv.icytower.gui.GUI.CONTROLS.*;

public class GUI {
    public static class CONTROLS {
        public static final int ARROW_UP = 1 << 0;
        public static final int ARROW_LEFT = 1 << 1;
        public static final int ARROW_RIGHT = 1 << 2;
        public static final int PAUSE_BTN = 1 << 3;
        public static final int RESUME_BTN = 1 << 4;
        public static final int MAIN_MENU_BTN = 1 << 5;
        public static final int SETTINGS_BTN = 1 << 6;
        public static final int SCORE_TXT = 1 << 7;
        public static final int PLAY_AGAIN_BTN = 1 << 8;
        public static final int SHARE_BTN = 1 << 9;
        public static final int GAME_OVER_TXT = 1 << 10;
        public static final int YOUR_SCORE_TXT = 1 << 11;
        public static final int PERSONAL_HIGH_SCORE_TXT = 1 << 12;
        public static final int NEW_HIGH_SCORE_TXT = 1 << 13;
        public static final int CLOCK = 1 << 14;
        public static final int GAME_STATS_TXT = 1 << 15;
        public static final int CHOOSE_PLAYER_TXT = 1 << 16;
        public static final int PLAYER_1_IMG = 1 << 17;
        public static final int PLAYER_1_RECT = 1 << 18;
        public static final int PLAYER_2_IMG = 1 << 19;
        public static final int PLAYER_2_RECT = 1 << 20;

        public static final int MAX_FLAGS = GAME_STATS_TXT << 1;
        public static final int PLAYER_MOVEMENT_CONTROLS = (ARROW_LEFT
                | ARROW_UP | ARROW_RIGHT);
        public static final int GAMEPLAY_CONTROLS = PLAYER_MOVEMENT_CONTROLS |
                PAUSE_BTN | SCORE_TXT | CLOCK;

        public static final int PAUSE_MENU_CONTROLS = RESUME_BTN | SETTINGS_BTN
                | MAIN_MENU_BTN;
        public static final int GAME_LOST_CONTROLS = GAME_OVER_TXT | NEW_HIGH_SCORE_TXT | YOUR_SCORE_TXT | PERSONAL_HIGH_SCORE_TXT
                | PLAY_AGAIN_BTN | SHARE_BTN | SETTINGS_BTN | MAIN_MENU_BTN | GAME_STATS_TXT;

        public static final int CHOOSE_PLAYER_CONTROLS = CHOOSE_PLAYER_TXT | PLAYER_1_IMG | PLAYER_1_RECT | PLAYER_2_IMG | PLAYER_2_RECT;

        public static final int[] CONTROL_GROUPS = new int[]{
                GAMEPLAY_CONTROLS, PAUSE_MENU_CONTROLS, GAME_LOST_CONTROLS, CHOOSE_PLAYER_CONTROLS
        };

        // first key is the control group, seconds key is the specific control, and the value is the location of the control
        public static HashMap<Integer, HashMap<Integer, Rect>> CONTROL_POSITIONS_PER_GROUP = new HashMap<>();

        public static boolean checkActive(int activeControls, int control) {
            return (activeControls & control) == control;
        }

    }

    public static void buildControls(Map<Integer, Control> controls, Resources resources, int renderWidth, int renderHeight) {
        buildGameControls(controls, resources, renderWidth, renderHeight);
        buildPauseMenuControls(controls, renderWidth, renderHeight);
        buildLostMenuControls(controls, renderWidth, renderHeight);
        buildChoosePlayerControls(controls, renderWidth, renderHeight);
        addOnClickListeners(controls);
    }

    private static void buildGameControls(Map<Integer, Control> controls, Resources resources, int renderWidth, int renderHeight) {
        int controlSize = (int) (0.18 * renderWidth);
        int controlY = (int) (renderHeight - controlSize - 0.03 * renderHeight);
        Rect upArrow = RectHelper.rectFromWidthHeight(
                (int) (0.05 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        Rect leftArrow = RectHelper.rectFromWidthHeight(
                (int) (0.5 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        Rect rightArrow = RectHelper.rectFromWidthHeight(
                leftArrow.right + (int) (0.1 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        int pauseBtnSize = (int) (0.12 * renderWidth);
        Rect pauseBtn = RectHelper.rectFromWidthHeight(
                renderWidth - pauseBtnSize - (int) (0.02f * renderWidth),
                (int) (0.02f * renderHeight),
                pauseBtnSize,
                pauseBtnSize
        );
        controls.put(CONTROLS.ARROW_UP, new ImageControl(upArrow, resources, R.drawable.up_arrow, controlSize, controlSize));
        controls.put(CONTROLS.ARROW_LEFT, new ImageControl(leftArrow, resources, R.drawable.left_arrow, controlSize, controlSize));
        controls.put(CONTROLS.ARROW_RIGHT, new ImageControl(rightArrow, resources, R.drawable.right_arrow, controlSize, controlSize));
        controls.put(CONTROLS.PAUSE_BTN, new ImageControl(pauseBtn, resources, R.drawable.pause_btn, pauseBtnSize, pauseBtnSize));


        Rect clockControl = RectHelper.rectFromWidthHeight(
                (int) (0.01f * renderWidth),
                (int) (0.015f * renderHeight),
                (int) (0.1 * renderHeight),
                (int) (0.1 * renderHeight)
        );
        Bitmap clockImg = ImageHelper.stretch(BitmapFactory.decodeResource(resources, R.drawable.clock),
                clockControl.width(), clockControl.height(), true);
        Bitmap arrowImg = ImageHelper.stretch(BitmapFactory.decodeResource(resources, R.drawable.arrow),
                (int) (clockControl.width() * 45 / 600f), (int) (clockControl.height() * 0.45f), true);

        controls.put(CLOCK, new ClockControl(clockControl, clockImg, arrowImg));

        Point score = new Point(clockControl.left, clockControl.bottom + (int) (0.02f * renderHeight));
        float scoreTextSize = renderHeight / 30f;
        int scoreTextColor = 0xffd4fffe;
        controls.put(CONTROLS.SCORE_TXT, new TextControl(false, true,
                score, "Score: XXX",
                scoreTextSize, scoreTextColor));


    }

    private static final float BUTTON_WIDTH_MULTIPLE = 1 / 2.5f;
    private static final float BUTTON_HEIGHT_MULTIPLE = 1 / 12f;
    private static final float BUTTON_PADDING_MULTIPLE = BUTTON_HEIGHT_MULTIPLE / 2.5f;
    private static final int BUTTONS_BACKGROUND_COLOR = 0xFF15c3eb;
    private static final int BUTTON_TEXT_COLOR = 0xFFFFFFFF;
    private static final int SCORES_TEXT_COLOR = 0xFFc9c9c9;
    private static final int GAME_OVER_TEXT_COLOR = 0xFFf73b3b;

    private static void buildPauseMenuControls(Map<Integer, Control> controls, int renderWidth, int renderHeight) {
        int buttonWidth = (int) (renderWidth * BUTTON_WIDTH_MULTIPLE);
        int buttonHeight = (int) (renderHeight * BUTTON_HEIGHT_MULTIPLE);
        int paddingBetweenButtons = (int) (renderHeight * BUTTON_PADDING_MULTIPLE);
        Rect settingsBtn = RectHelper.rectFromWidthHeight(
                renderWidth / 2 - buttonWidth / 2,
                renderHeight / 2 - buttonHeight / 2,
                buttonWidth,
                buttonHeight
        );

        Rect resumeBtn = RectHelper.rectFromWidthHeight(
                settingsBtn.left,
                settingsBtn.top - paddingBetweenButtons - buttonHeight,
                buttonWidth,
                buttonHeight
        );

        Rect mainMenuBtn = RectHelper.rectFromWidthHeight(
                resumeBtn.left,
                settingsBtn.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        controls.put(CONTROLS.RESUME_BTN, ImageControl.buildDisabledBtn(resumeBtn, "Resume", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.SETTINGS_BTN, ImageControl.buildDisabledBtn(settingsBtn, "Settings", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.MAIN_MENU_BTN, ImageControl.buildDisabledBtn(mainMenuBtn, "Main Menu", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        HashMap<Integer, Rect> changingControlsPositions = new HashMap<>();
        changingControlsPositions.put(CONTROLS.SETTINGS_BTN, settingsBtn);
        changingControlsPositions.put(CONTROLS.MAIN_MENU_BTN, mainMenuBtn);
        CONTROLS.CONTROL_POSITIONS_PER_GROUP.put(CONTROLS.PAUSE_MENU_CONTROLS, changingControlsPositions);
    }

    private static void buildLostMenuControls(Map<Integer, Control> controls, int renderWidth, int renderHeight) {
        int buttonWidth = (int) (renderWidth * BUTTON_WIDTH_MULTIPLE);
        int buttonHeight = (int) (renderHeight * BUTTON_HEIGHT_MULTIPLE);
        int paddingBetweenButtons = (int) (renderHeight * BUTTON_PADDING_MULTIPLE);
        Rect playAgain = RectHelper.rectFromWidthHeight(
                (renderWidth - buttonWidth) / 2,
                (renderHeight - buttonHeight) / 2,
                buttonWidth,
                buttonHeight
        );
        Rect share = RectHelper.rectFromWidthHeight(
                playAgain.left,
                playAgain.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect settings = RectHelper.rectFromWidthHeight(
                playAgain.left,
                share.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect mainMenu = RectHelper.rectFromWidthHeight(
                playAgain.left,
                settings.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );

        controls.put(CONTROLS.PLAY_AGAIN_BTN, ImageControl.buildDisabledBtn(playAgain, "Play Again",
                BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.SHARE_BTN, ImageControl.buildDisabledBtn(share, "Share",
                BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));

        HashMap<Integer, Rect> changingControlsPositions = new HashMap<>();
        changingControlsPositions.put(CONTROLS.SETTINGS_BTN, settings);
        changingControlsPositions.put(CONTROLS.MAIN_MENU_BTN, mainMenu);
        CONTROLS.CONTROL_POSITIONS_PER_GROUP.put(CONTROLS.GAME_LOST_CONTROLS, changingControlsPositions);


        final String yourScoreStr = "Your Score: XXXX";
        final String highScoreStr = "Highscore: XXXX";
        final String gameOverStr = "GAME OVER";
        final String newHighScoreStr = "NEW HIGH SCORE!!!";
        final String gameStatsStr = "Total Jumps: XX   Time: XX.X (sec)";


        int scoresTextWidth = buttonWidth * 2;
        int scoresTextHeight = TextSizeHelper.getTextSizeFromWidth(highScoreStr, scoresTextWidth);
        int paddingBetweenTexts = (int) (scoresTextHeight * 1.1f);
        Point personalHighScore = new Point(
                renderWidth / 2,
                playAgain.top - paddingBetweenTexts - scoresTextHeight
        );
        controls.put(CONTROLS.PERSONAL_HIGH_SCORE_TXT, new TextControl(false, false, personalHighScore,
                highScoreStr, scoresTextHeight, SCORES_TEXT_COLOR, true));
        Point yourScore = new Point(
                personalHighScore.x,
                personalHighScore.y - scoresTextHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.YOUR_SCORE_TXT, new TextControl(false, false, yourScore,
                yourScoreStr, scoresTextHeight, SCORES_TEXT_COLOR, true));

        int gameOverWidth = (int) (scoresTextWidth * 1.25f);
        int gameOverHeight = TextSizeHelper.getTextSizeFromWidth(gameOverStr, gameOverWidth);
        Point gameOver = new Point(
                personalHighScore.x,
                yourScore.y - gameOverHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.GAME_OVER_TXT, new TextControl(false, false, gameOver,
                gameOverStr, gameOverHeight, GAME_OVER_TEXT_COLOR, true));

        int gameStatsWidth = (int) (renderWidth * 0.8f);
        int gameStatsHeight = TextSizeHelper.getTextSizeFromWidth(gameStatsStr, gameStatsWidth);
        Point gameStats = new Point(
                personalHighScore.x,
                gameOver.y - gameStatsHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.GAME_STATS_TXT, new TextControl(false, false, gameStats,
                gameStatsStr, gameStatsHeight, SCORES_TEXT_COLOR, true));

        Point newHighScore = new Point(
                personalHighScore.x,
                mainMenu.bottom + paddingBetweenTexts
        );
        int newHighScoreHeight = TextSizeHelper.getTextSizeFromWidth(highScoreStr, scoresTextWidth);
        controls.put(CONTROLS.NEW_HIGH_SCORE_TXT, new HighscoreControl(false, false, newHighScore,
                newHighScoreStr, newHighScoreHeight, GAME_OVER_TEXT_COLOR, true));


    }

    private static final int CHOOSE_CHARACTER_COLOR = 0xFF000000;
    private static final int CHARACTER_RECT_COLOR = 0xFF444444;
    private static final int CHARACTER_RECT_THICKNESS = 5;

    private static void buildChoosePlayerControls(Map<Integer, Control> controls, int renderWidth, int renderHeight) {

        final String chooseCharacterStr = "CHOOSE CHARACTER";
        int textWidth = (int) (renderWidth * 0.8f);
        int textSize = TextSizeHelper.getTextSizeFromWidth(chooseCharacterStr, textWidth);
        int textY = (int) (renderHeight * 0.2f);
        Point txtPoint = new Point((renderWidth - textWidth) / 2, textY);
        controls.put(CHOOSE_PLAYER_TXT, new TextControl(false, false, txtPoint, chooseCharacterStr, textSize,
                CHOOSE_CHARACTER_COLOR));

        int paddingBetweenTextAndRect = (int) (renderHeight * 0.1f);
        // side padding - char width - paddingbetween - char width - side padding
        int paddingBetweenChars = renderWidth / 5;
        int sidePadding = renderWidth / 8;
        int charRectWidth = (renderWidth - paddingBetweenChars - sidePadding * 2) / 2;
        int charRectHeight = (int) (charRectWidth * ((float) Engine.character1.height / Engine.character1.width));

        Rect player1Rect = RectHelper.rectFromWidthHeight(
                sidePadding,
                textY + textSize + paddingBetweenTextAndRect,
                charRectWidth,
                charRectHeight
        );
        controls.put(PLAYER_1_RECT, new RectControl(player1Rect, CHARACTER_RECT_COLOR, CHARACTER_RECT_THICKNESS));
        Rect player2Rect = RectHelper.rectFromWidthHeight(
                player1Rect.right + paddingBetweenChars,
                player1Rect.top,
                charRectWidth,
                charRectHeight
        );
        controls.put(PLAYER_2_RECT, new RectControl(player2Rect, CHARACTER_RECT_COLOR, CHARACTER_RECT_THICKNESS));

        int rectPaddingX = (int) (charRectWidth * 0.1f);
        int rectPaddingY = (int) (charRectHeight * 0.1f);
        Rect player1ImgRect = new Rect(
                player1Rect.left + rectPaddingX,
                player1Rect.top + rectPaddingY,
                player1Rect.right - rectPaddingX,
                player1Rect.bottom - rectPaddingY
        );
        Rect player2ImgRect = new Rect(
                player2Rect.left + rectPaddingX,
                player2Rect.top + rectPaddingY,
                player2Rect.right - rectPaddingX,
                player2Rect.bottom - rectPaddingY
        );
        Bitmap char1Img = Engine.character1.animations.get(Player.PlayerState.STANDING).bitmapsRight[0];
        controls.put(PLAYER_1_IMG, new ImageControl(player1ImgRect,
                ImageHelper.stretch(char1Img, player1ImgRect.width(), player1ImgRect.height(), false)));
        Bitmap char2Img = Engine.character2.animations.get(Player.PlayerState.STANDING).bitmapsRight[0];
        controls.put(PLAYER_2_IMG, new ImageControl(player2ImgRect,
                ImageHelper.stretch(char2Img, player2ImgRect.width(), player2ImgRect.height(), false)));

    }

    private static void addOnClickListeners(Map<Integer, Control> controls) {

        // pause controls

        controls.get(MAIN_MENU_BTN).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        };
        controls.get(SETTINGS_BTN).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                context.startActivity(new Intent(context, SettingsActivity.class));
            }
        };
        controls.get(RESUME_BTN).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                if (engine.currentGameState == Engine.GameState.PAUSED) {
                    engine.updateGameState(Engine.GameState.PLAYING);
                }
                engine.onResume();
            }
        };

        // lost controls

        controls.get(PLAY_AGAIN_BTN).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                engine.resetLevel(context, context.getResources());
                engine.updateGameState(Engine.GameState.PLAYING);
                engine.onResume();
            }
        };
        controls.get(SHARE_BTN).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MY AWESOME SCORE!!!");
                intent.putExtra(android.content.Intent.EXTRA_TEXT,
                        "I just scored " + engine.player.getScore() + " points on Icy Tower!!!");
                context.startActivity(Intent.createChooser(intent, "Share using..."));

            }
        };
        controls.get(PLAYER_1_RECT).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                setPlayer(Engine.character1, engine, context);
            }
        };
        controls.get(PLAYER_2_RECT).onClick = new OnButtonClickListener() {
            @Override
            public void OnClick(Engine engine, Context context) {
                setPlayer(Engine.character2, engine, context);
            }
        };
    }

    private static void setPlayer(Character character, Engine engine, Context context){
        Resources resources = context.getResources();
        engine.setPlayer(new Player(character), context, resources);
        engine.resetLevel(context, resources);
    }


}
