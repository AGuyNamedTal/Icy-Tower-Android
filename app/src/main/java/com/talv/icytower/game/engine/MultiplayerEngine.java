package com.talv.icytower.game.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.gui.GUI;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.game.utils.RectUtils;

import static com.talv.icytower.game.gui.GUI.CONTROLS.GAME_STATS_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.NEW_HIGH_SCORE_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PAUSE_MID_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PERSONAL_HIGH_SCORE_TXT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_MOVEMENT_CONTROLS_SHIFT;
import static com.talv.icytower.game.gui.GUI.CONTROLS.YOUR_SCORE_TXT;

public class MultiplayerEngine extends Engine {

    private Matrix topMatrix;
    private Matrix bottomMatrix;

    private Player player2;


    public MultiplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
        pauseBtnID = PAUSE_MID_BTN;
        GameState.PLAYING.controlGroup = GUI.CONTROLS.MULTI_GAMEPLAY_CONTROLS;
        GameState.LOST.controlGroup = GUI.CONTROLS.MULTI_GAME_LOST_CONTROLS;
        initializeMatrices();
        player2 = new Player(soundPool, context);

    }

    private void initializeMatrices() {
        topMatrix = new Matrix();
        bottomMatrix = new Matrix();
        topMatrix.preScale(1, 0.5f);
        topMatrix.postRotate(180);
        topMatrix.postTranslate(CAMERA_WIDTH, CAMERA_HEIGHT / 2f); // rotation tilts on 0,0 so reverse that
        bottomMatrix.preScale(1, 0.5f);
        bottomMatrix.postTranslate(0, CAMERA_HEIGHT / 2f);

    }


    @Override
    public void reset() {
        super.reset();
        player2.resetPlayer();
        player2.updateScore(0, gameCanvas);
    }

    @Override
    public void setPlayerCharacter(Character character) {
        super.setPlayerCharacter(character);
        Character player2Char;
        if (character == Engine.character1) {
            player2Char = Engine.character2;
        } else {
            player2Char = Engine.character1;
        }
        player2.setCharacter(player2Char);
        RectUtils.setRectPos(player2.rect, (CAMERA_WIDTH - player2.rect.width()) / 2,
                platforms.peekFirst().rect.top - player2.rect.height());
    }

    @Override
    public void clearPlayerCharacter() {
        super.clearPlayerCharacter();
        player2.setCharacter(null);
    }

    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);
        // draw top

        bitmapCanvas.setMatrix(topMatrix);
        bitmapCanvas.save();
        drawGame(bitmapCanvas, false);
        // draw bottom
        bitmapCanvas.restore();
        bitmapCanvas.setMatrix(bottomMatrix);
        bitmapCanvas.save();
        drawGame(bitmapCanvas, true);

        // final render (stretch)
        frameScaled = BitmapUtils.stretch(frame, renderWidth, renderHeight, false);

        // add controls
        Canvas finalFrameCanvas = new Canvas(frameScaled);
        if (currentGameState == GameState.PAUSED || currentGameState == GameState.LOST) {
            // reduce brightness of background game
            finalFrameCanvas.drawRect(0, 0, renderWidth, renderHeight, pausePaint);
        }

        gameCanvas.renderControls(finalFrameCanvas);
    }

    private void drawGame(Canvas canvas, boolean player1OnTop) {
        // draw background
        canvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
        // draw platforms
        for (Platform platform : platforms) {
            if (platform.rect.bottom >= cameraY) {
                platform.render(canvas, this);
            }
        }
        //draw players
        if (player1OnTop) {
            player2.render(canvas, this);
            player.render(canvas, this);
        } else {
            player.render(canvas, this);
            player2.render(canvas, this);

        }
    }


    protected void updatePlayer(int msPassed, int activeControls) {
        int player1Controls = getGameControls(activeControls);
        int player2Controls = getGameControls(activeControls >> PLAYER_MOVEMENT_CONTROLS_SHIFT);
        // update player
        player.updatePlayer(msPassed, this, player1Controls);
        player2.updatePlayer(msPassed, this, player2Controls);
    }

    @Override
    int minPlayerY() {
        return Math.min(player.rect.top, player2.rect.top);
    }

    @Override
    int maxPlayerY() {
        return Math.max(player.rect.top, player2.rect.top);
    }


    protected void updateLostUI(Context context) {
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


}
