package com.talv.icytower.game.engine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.talv.icytower.game.GameCanvas;
import com.talv.icytower.game.gui.GUI;
import com.talv.icytower.game.musicService.MusicServiceConnection;
import com.talv.icytower.game.platform.Platform;
import com.talv.icytower.game.player.Character;
import com.talv.icytower.game.player.Player;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.game.utils.RectUtils;

import static com.talv.icytower.game.gui.GUI.CONTROLS.PAUSE_MID_BTN;
import static com.talv.icytower.game.gui.GUI.CONTROLS.PLAYER_MOVEMENT_CONTROLS_SHIFT;

public class MultiplayerEngine extends Engine {

    private Matrix topMatrix;
    private Matrix bottomMatrix;

    private final Player player2;


    public MultiplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context, MusicServiceConnection musicServiceConnection) {
        super(renderWidth, renderHeight, resources, gameCanvas, context, musicServiceConnection);
        pauseBtnID = PAUSE_MID_BTN;
        GameState.PLAYING.controlGroup = GUI.CONTROLS.MULTI_GAMEPLAY_CONTROLS;
        initializeMatrices();
        player2 = new Player(getSoundPool(), context);
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
        player2.updateScore(0, getGameCanvas());
    }

    @Override
    public void setPlayerCharacter(Character character) {
        super.setPlayerCharacter(character);
        Character player2Char;
        if (character == Engine.getCharacter1()) {
            player2Char = Engine.getCharacter2();
        } else {
            player2Char = Engine.getCharacter1();
        }
        player2.setCharacter(player2Char);
        RectUtils.setRectPos(player2.getRect(), (CAMERA_WIDTH - player2.getRect().width()) / 2,
                getPlatforms().peekFirst().getRect().top - player2.getRect().height());
    }

    @Override
    public void clearPlayerCharacter() {
        super.clearPlayerCharacter();
        player2.setCharacter(null);
    }

    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(getFrame());
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
        setFrameScaled(BitmapUtils.stretch(getFrame(), getRenderWidth(), getRenderHeight(), false));

        // add controls
        Canvas finalFrameCanvas = new Canvas(getFrameScaled());
        if (getCurrentGameState() == GameState.PAUSED || getCurrentGameState() == GameState.LOST) {
            // reduce brightness of background game
            finalFrameCanvas.drawRect(0, 0, getRenderWidth(), getRenderHeight(), getPausePaint());
        }

        getGameCanvas().renderControls(finalFrameCanvas);
    }

    private void drawGame(Canvas canvas, boolean player1OnTop) {
        // draw background
        canvas.drawBitmap(backgroundImg, 0, 0, getGamePaint());
        // draw platforms
        for (Platform platform : getPlatforms()) {
            if (platform.getRect().bottom >= getCameraY()) {
                platform.render(canvas, this);
            }
        }
        //draw players
        if (player1OnTop) {
            player2.render(canvas, this);
            getPlayer().render(canvas, this);
        } else {
            getPlayer().render(canvas, this);
            player2.render(canvas, this);

        }
    }


    protected void updatePlayer(int msPassed, int activeControls) {
        super.updatePlayer(msPassed, activeControls);
        int player2Controls = getGameControls(activeControls >> PLAYER_MOVEMENT_CONTROLS_SHIFT);
        player2.updatePlayer(msPassed, this, player2Controls);
    }

    @Override
    int minPlayerY() {
        return Math.min(getPlayer().getRect().top, player2.getRect().top);
    }

    @Override
    int maxPlayerY() {
        return Math.max(getPlayer().getRect().top, player2.getRect().top);
    }


    @Override
    Player getWinningPlayer() {
        if (player2.getScore() > getPlayer().getScore()) {
            return player2;
        }
        return getPlayer();
    }

    @Override
    public String toString() {
        return "MultiplayerEngine{" +
                "topMatrix=" + topMatrix +
                ", bottomMatrix=" + bottomMatrix +
                ", player2=" + player2 +
                "} " + super.toString();
    }
}
