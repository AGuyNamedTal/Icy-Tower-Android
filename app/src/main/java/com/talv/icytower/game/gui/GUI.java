package com.talv.icytower.game.gui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import com.talv.icytower.R;
import com.talv.icytower.activities.MainActivity;
import com.talv.icytower.activities.SettingsActivity;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.gui.graphiccontrols.ClockControl;
import com.talv.icytower.game.gui.graphiccontrols.ColorWheelTxtControl;
import com.talv.icytower.game.gui.graphiccontrols.Control;
import com.talv.icytower.game.gui.graphiccontrols.ImageControl;
import com.talv.icytower.game.gui.graphiccontrols.OnControlTouchListener;
import com.talv.icytower.game.gui.graphiccontrols.RectControl;
import com.talv.icytower.game.gui.graphiccontrols.TextControl;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.game.utils.RectUtils;

import java.util.HashMap;
import java.util.Map;

import static com.talv.icytower.game.gui.GUI.CONTROLS.CHOOSE_PLAYER_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.CLOCK;
import static com.talv.icytower.game.gui.GUI.CONTROLS.CONTROL_POSITIONS_PER_GROUP;
import static com.talv.icytower.game.gui.GUI.CONTROLS.MAIN_MENU_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.MULTI_GAME_LOST_CONTROLS;
import static com.talv.icytower.game.gui.GUI.CONTROLS.MULTI_PLAYER_1_RESULT_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.MULTI_PLAYER_2_RESULT_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_1_IMG;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_1_RECT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_2_IMG;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_2_RECT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAY_AGAIN_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.RESUME_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.SETTINGS_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.SHARE_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.YOUR_SCORE_TXT;

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
        public static final int PLAYER_MOVEMENT_CONTROLS_SHIFT = 21;
        public static final int ARROW_UP_2 = ARROW_UP << PLAYER_MOVEMENT_CONTROLS_SHIFT;
        public static final int ARROW_LEFT_2 = ARROW_LEFT << PLAYER_MOVEMENT_CONTROLS_SHIFT;
        public static final int ARROW_RIGHT_2 = ARROW_RIGHT << PLAYER_MOVEMENT_CONTROLS_SHIFT;
        public static final int PAUSE_MID_BTN = 1 << 25;
        public static final int MULTI_PLAYER_1_RESULT_TXT = 1 << 26;
        public static final int MULTI_PLAYER_2_RESULT_TXT = 1 << 27;


        public static final int MAX_FLAGS = MULTI_PLAYER_2_RESULT_TXT << 1;

        public static final int PLAYER_MOVEMENT_CONTROLS = (ARROW_LEFT
                | ARROW_UP | ARROW_RIGHT);
        public static final int PLAYER_2_MOVEMENT_CONTROLS = (ARROW_UP_2 |
                ARROW_LEFT_2 | ARROW_RIGHT_2);


        public static final int GAMEPLAY_CONTROLS = PLAYER_MOVEMENT_CONTROLS |
                PAUSE_BTN | SCORE_TXT | CLOCK;
        public static final int MULTI_GAMEPLAY_CONTROLS = PLAYER_MOVEMENT_CONTROLS | PLAYER_2_MOVEMENT_CONTROLS |
                PAUSE_MID_BTN;

        public static final int PAUSE_MENU_CONTROLS = RESUME_BTN | SETTINGS_BTN
                | MAIN_MENU_BTN;
        public static final int GAME_LOST_CONTROLS = GAME_OVER_TXT | NEW_HIGH_SCORE_TXT | YOUR_SCORE_TXT | PERSONAL_HIGH_SCORE_TXT
                | PLAY_AGAIN_BTN | SHARE_BTN | SETTINGS_BTN | MAIN_MENU_BTN | GAME_STATS_TXT;

        public static final int MULTI_GAME_LOST_CONTROLS = MULTI_PLAYER_1_RESULT_TXT | MULTI_PLAYER_2_RESULT_TXT |
                YOUR_SCORE_TXT | PLAY_AGAIN_BTN | SHARE_BTN | SETTINGS_BTN | MAIN_MENU_BTN;


        public static final int CHOOSE_PLAYER_CONTROLS = CHOOSE_PLAYER_TXT | PLAYER_1_IMG | PLAYER_1_RECT | PLAYER_2_IMG | PLAYER_2_RECT;


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


    private static final int LINE_SEPARATOR_COLOR = 0xFFFFFFFF;

    private static void buildGameControls(Map<Integer, Control> controls, Resources resources, int renderWidth, int renderHeight) {
        int controlSize = (int) (0.18 * renderWidth);
        int controlY = (int) (renderHeight - controlSize - 0.03 * renderHeight);
        Rect upArrow = RectUtils.rectFromWidthHeight(
                (int) (0.05 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        Rect leftArrow = RectUtils.rectFromWidthHeight(
                (int) (0.5 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        Rect rightArrow = RectUtils.rectFromWidthHeight(
                leftArrow.right + (int) (0.1 * renderWidth),
                controlY,
                controlSize,
                controlSize
        );

        int pauseBtnSize = (int) (0.12 * renderWidth);
        Rect pauseBtn = RectUtils.rectFromWidthHeight(
                renderWidth - pauseBtnSize - (int) (0.02f * renderWidth),
                (int) (0.02f * renderHeight),
                pauseBtnSize,
                pauseBtnSize
        );
        ImageControl arrowUpCtrl = new ImageControl(upArrow, resources, R.drawable.up_arrow, controlSize, controlSize);
        ImageControl arrowLeftCtrl = new ImageControl(leftArrow, resources, R.drawable.left_arrow, controlSize, controlSize);
        ImageControl arrowRightCtrl = new ImageControl(rightArrow, resources, R.drawable.right_arrow, controlSize, controlSize);
        ImageControl pauseButtonCtrl = new ImageControl(pauseBtn, resources, R.drawable.pause_btn, pauseBtnSize, pauseBtnSize);
        controls.put(CONTROLS.ARROW_UP, arrowUpCtrl);
        controls.put(CONTROLS.ARROW_LEFT, arrowLeftCtrl);
        controls.put(CONTROLS.ARROW_RIGHT, arrowRightCtrl);
        controls.put(CONTROLS.PAUSE_BTN, pauseButtonCtrl);

        Rect clockControl = RectUtils.rectFromWidthHeight(
                (int) (0.01f * renderWidth),
                (int) (0.015f * renderHeight),
                (int) (0.1 * renderHeight),
                (int) (0.1 * renderHeight)
        );
        Bitmap clockImg = BitmapUtils.stretch(BitmapFactory.decodeResource(resources, R.drawable.clock),
                clockControl.width(), clockControl.height(), true);
        Bitmap arrowImg = BitmapUtils.stretch(BitmapFactory.decodeResource(resources, R.drawable.arrow),
                (int) (clockControl.width() * 45 / 600f), (int) (clockControl.height() * 0.45f), true);

        controls.put(CLOCK, new ClockControl(clockControl, clockImg, arrowImg));

        Point score = new Point(clockControl.left, clockControl.bottom + (int) (0.02f * renderHeight));
        float scoreTextSize = renderHeight / 30f;
        int scoreTextColor = 0xffd4fffe;
        controls.put(CONTROLS.SCORE_TXT, new TextControl(
                score, "Score: XXX",
                scoreTextSize, scoreTextColor));


        controls.put(CONTROLS.ARROW_UP_2, ImageControl.reflectControl(arrowUpCtrl, renderWidth, renderHeight));
        controls.put(CONTROLS.ARROW_LEFT_2, ImageControl.reflectControl(arrowLeftCtrl, renderWidth, renderHeight));
        controls.put(CONTROLS.ARROW_RIGHT_2, ImageControl.reflectControl(arrowRightCtrl, renderWidth, renderHeight));
        controls.put(CONTROLS.PAUSE_MID_BTN, new ImageControl(RectUtils.centerRect(pauseBtnSize, pauseBtnSize, renderWidth, renderHeight),
                BitmapUtils.reflectBitmap(pauseButtonCtrl.image, false)));
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
        Rect settingsBtn = RectUtils.rectFromWidthHeight(
                renderWidth / 2 - buttonWidth / 2,
                renderHeight / 2 - buttonHeight / 2,
                buttonWidth,
                buttonHeight
        );

        Rect resumeBtn = RectUtils.rectFromWidthHeight(
                settingsBtn.left,
                settingsBtn.top - paddingBetweenButtons - buttonHeight,
                buttonWidth,
                buttonHeight
        );

        Rect mainMenuBtn = RectUtils.rectFromWidthHeight(
                resumeBtn.left,
                settingsBtn.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        controls.put(CONTROLS.RESUME_BTN, ImageControl.buildBtn(resumeBtn, "Resume", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.SETTINGS_BTN, ImageControl.buildBtn(settingsBtn, "Settings", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.MAIN_MENU_BTN, ImageControl.buildBtn(mainMenuBtn, "Main Menu", BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        HashMap<Integer, Rect> changingControlsPositions = new HashMap<>();
        changingControlsPositions.put(CONTROLS.SETTINGS_BTN, settingsBtn);
        changingControlsPositions.put(CONTROLS.MAIN_MENU_BTN, mainMenuBtn);
        CONTROLS.CONTROL_POSITIONS_PER_GROUP.put(CONTROLS.PAUSE_MENU_CONTROLS, changingControlsPositions);
    }

    private static void buildLostMenuControls(Map<Integer, Control> controls, int renderWidth, int renderHeight) {
        int buttonWidth = (int) (renderWidth * BUTTON_WIDTH_MULTIPLE);
        int buttonHeight = (int) (renderHeight * BUTTON_HEIGHT_MULTIPLE);
        int paddingBetweenButtons = (int) (renderHeight * BUTTON_PADDING_MULTIPLE);
        Rect playAgain = RectUtils.rectFromWidthHeight(
                (renderWidth - buttonWidth) / 2,
                (renderHeight - buttonHeight) / 2,
                buttonWidth,
                buttonHeight
        );
        Rect share = RectUtils.rectFromWidthHeight(
                playAgain.left,
                playAgain.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect settings = RectUtils.rectFromWidthHeight(
                playAgain.left,
                share.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect mainMenu = RectUtils.rectFromWidthHeight(
                playAgain.left,
                settings.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );

        controls.put(CONTROLS.PLAY_AGAIN_BTN, ImageControl.buildBtn(playAgain, "Play Again",
                BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));
        controls.put(CONTROLS.SHARE_BTN, ImageControl.buildBtn(share, "Share",
                BUTTONS_BACKGROUND_COLOR, BUTTON_TEXT_COLOR));

        HashMap<Integer, Rect> changingControlsPositions = new HashMap<>();
        changingControlsPositions.put(CONTROLS.SETTINGS_BTN, settings);
        changingControlsPositions.put(CONTROLS.MAIN_MENU_BTN, mainMenu);
        changingControlsPositions.put(SHARE_BTN, share);



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
        controls.put(CONTROLS.PERSONAL_HIGH_SCORE_TXT, new TextControl(personalHighScore,
                highScoreStr, scoresTextHeight, SCORES_TEXT_COLOR, true));
        Point yourScore = new Point(
                personalHighScore.x,
                personalHighScore.y - scoresTextHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.YOUR_SCORE_TXT, new TextControl(yourScore,
                yourScoreStr, scoresTextHeight, SCORES_TEXT_COLOR, true));

        int gameOverWidth = (int) (scoresTextWidth * 1.25f);
        int gameOverHeight = TextSizeHelper.getTextSizeFromWidth(gameOverStr, gameOverWidth);
        Point gameOver = new Point(
                personalHighScore.x,
                yourScore.y - gameOverHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.GAME_OVER_TXT, new TextControl(gameOver,
                gameOverStr, gameOverHeight, GAME_OVER_TEXT_COLOR, true));

        int gameStatsWidth = (int) (renderWidth * 0.8f);
        int gameStatsHeight = TextSizeHelper.getTextSizeFromWidth(gameStatsStr, gameStatsWidth);
        Point gameStats = new Point(
                personalHighScore.x,
                gameOver.y - gameStatsHeight - paddingBetweenTexts
        );
        controls.put(CONTROLS.GAME_STATS_TXT, new TextControl(gameStats,
                gameStatsStr, gameStatsHeight, SCORES_TEXT_COLOR, true));

        Point newHighScore = new Point(
                personalHighScore.x,
                mainMenu.bottom + paddingBetweenTexts
        );
        int newHighScoreHeight = TextSizeHelper.getTextSizeFromWidth(highScoreStr, scoresTextWidth);
        controls.put(CONTROLS.NEW_HIGH_SCORE_TXT, new ColorWheelTxtControl(newHighScore,
                newHighScoreStr, newHighScoreHeight, GAME_OVER_TEXT_COLOR, true, 2500));

        changingControlsPositions.put(YOUR_SCORE_TXT, RectUtils.rectFromPoint(yourScore));

        CONTROLS.CONTROL_POSITIONS_PER_GROUP.put(CONTROLS.GAME_LOST_CONTROLS, changingControlsPositions);



        // build multi lost menu controls
        //TODO: MULTI_PLAYER_1_RESULT_TXT | MULTI_PLAYER_2_RESULT_TXT |
        //                YOUR_SCORE_TXT | PLAY_AGAIN_BTN | SHARE_BTN | SETTINGS_BTN | MAIN_MENU_BTN
        final String player2ResultTxt = "YOU LOST";
        Point player2Result = new Point(renderWidth / 2, (int) (renderHeight * 0.2f));
        int player2ResWidth = gameOverWidth;
        int player2Height = TextSizeHelper.getTextSizeFromWidth(player2ResultTxt, player2ResWidth);
        controls.put(MULTI_PLAYER_2_RESULT_TXT,
                new TextControl(player2Result, player2ResultTxt, player2Height, 0, true).setFlipY(true));

        final String player1ResultTxt = player2ResultTxt;
        Point player1Result = new Point(player2Result.x, player2Result.y + player2Height + (int) (renderHeight * 0.05f));
        int player1Height = player2Height;
        controls.put(MULTI_PLAYER_1_RESULT_TXT,
                new TextControl(player1Result, player1ResultTxt, player1Height, 0, true));

        final String winnersScoreTxt = "WINNERS SCORE: 788";
        Point winnersScore = new Point(player1Result.x, player1Result.y + player1Height + paddingBetweenTexts);

        Rect playAgain2 = RectUtils.rectFromWidthHeight(
                (renderWidth - buttonWidth) / 2,
                winnersScore.y + scoresTextHeight + paddingBetweenTexts,
                buttonWidth,
                buttonHeight);
        Rect share2 = RectUtils.rectFromWidthHeight(
                playAgain2.left,
                playAgain2.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect settings2 = RectUtils.rectFromWidthHeight(
                share2.left,
                share2.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );
        Rect mainMenu2 = RectUtils.rectFromWidthHeight(
                settings2.left,
                share2.bottom + paddingBetweenButtons,
                buttonWidth,
                buttonHeight
        );


        HashMap<Integer, Rect> movingControls = new HashMap<>();
        movingControls.put(YOUR_SCORE_TXT, RectUtils.rectFromPoint(winnersScore));
        movingControls.put(PLAY_AGAIN_BTN, playAgain2);
        movingControls.put(SHARE_BTN, share2);
        movingControls.put(MAIN_MENU_BTN, mainMenu2);

        CONTROL_POSITIONS_PER_GROUP.put(MULTI_GAME_LOST_CONTROLS, movingControls);

    }


    private static final int CHOOSE_CHARACTER_COLOR = 0xFF000000;
    private static final int CHARACTER_RECT_COLOR = 0xFF444444;

    private static void buildChoosePlayerControls(Map<Integer, Control> controls, int renderWidth, int renderHeight) {

        final String chooseCharacterStr = "CHOOSE CHARACTER";
        int textWidth = (int) (renderWidth * 0.95f);
        int textSize = TextSizeHelper.getTextSizeFromWidth(chooseCharacterStr, textWidth);
        int textY = (int) (renderHeight * 0.2f);
        Point txtPoint = new Point((renderWidth) / 2, textY);
        controls.put(CHOOSE_PLAYER_TXT, new ColorWheelTxtControl(txtPoint, chooseCharacterStr, textSize,
                CHOOSE_CHARACTER_COLOR, true, 5000));

        int paddingBetweenTextAndRect = (int) (renderHeight * 0.1f);
        // side padding - char width - paddingbetween - char width - side padding
        int paddingBetweenChars = renderWidth / 5;
        int sidePadding = renderWidth / 8;
        int charRectWidth = (renderWidth - paddingBetweenChars - sidePadding * 2) / 2;
        int charRectHeight = (int) (charRectWidth * ((float) Engine.character1.height / Engine.character1.width));

        int rectThickness = (int) (renderWidth * 0.01f);

        Rect player1Rect = RectUtils.rectFromWidthHeight(
                sidePadding,
                textY + textSize + paddingBetweenTextAndRect,
                charRectWidth,
                charRectHeight
        );
        controls.put(PLAYER_1_RECT, new RectControl(player1Rect, CHARACTER_RECT_COLOR, rectThickness));
        Rect player2Rect = RectUtils.rectFromWidthHeight(
                player1Rect.right + paddingBetweenChars,
                player1Rect.top,
                charRectWidth,
                charRectHeight
        );
        controls.put(PLAYER_2_RECT, new RectControl(player2Rect, CHARACTER_RECT_COLOR, rectThickness));

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
                BitmapUtils.stretch(char1Img, player1ImgRect.width(), player1ImgRect.height(), false)));
        Bitmap char2Img = Engine.character2.animations.get(Player.PlayerState.STANDING).bitmapsRight[0];
        controls.put(PLAYER_2_IMG, new ImageControl(player2ImgRect,
                BitmapUtils.stretch(char2Img, player2ImgRect.width(), player2ImgRect.height(), false)));

    }

    private static void addOnClickListeners(Map<Integer, Control> controls) {

        // pause controls

        controls.get(MAIN_MENU_BTN).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        };
        controls.get(SETTINGS_BTN).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                context.startActivity(new Intent(context, SettingsActivity.class));
            }
        };
        controls.get(RESUME_BTN).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                if (engine.currentGameState == Engine.GameState.PAUSED) {
                    engine.updateGameState(Engine.GameState.PLAYING);
                }
                engine.onResume();
            }
        };

        // lost controls

        controls.get(PLAY_AGAIN_BTN).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                engine.clearPlayerCharacter();
                engine.reset();
                engine.updateGameState(Engine.GameState.CHOOSING_CHAR);
                engine.onResume();
                engine.stopBackgroundMusic();
            }
        };
        controls.get(SHARE_BTN).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MY AWESOME SCORE!!!");
                intent.putExtra(android.content.Intent.EXTRA_TEXT,
                        "I just scored " + engine.player.getScore() + " points on Icy Tower!!!");
                context.startActivity(Intent.createChooser(intent, "Share using..."));
            }
        };

        // choose character controls
        controls.get(PLAYER_1_RECT).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                engine.startGame(Engine.character1);
            }
        };
        controls.get(PLAYER_2_RECT).onTouch = new OnControlTouchListener() {
            @Override
            public void onTouch(Engine engine, Context context) {
                engine.startGame(Engine.character2);
            }
        };
    }


}
