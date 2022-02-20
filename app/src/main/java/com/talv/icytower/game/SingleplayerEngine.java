package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.game.platform.Platform;

import static com.talv.icytower.gui.GUI.CONTROLS.PAUSE_BTN;
import static com.talv.icytower.gui.GUI.CONTROLS.checkActive;

public class SingleplayerEngine extends Engine {
    public SingleplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
    }

    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);
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
        if (currentGameState == GameState.PAUSED || currentGameState == GameState.LOST) {
            // reduce brightness of background game
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
}
