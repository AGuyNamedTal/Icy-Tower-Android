package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.game.platform.Platform;

public class MultiplayerEngine extends Engine {

    private Matrix topMatrix;
    private Matrix bottomMatrix;
    private int halfRenderHeight;


    public MultiplayerEngine(int renderWidth, int renderHeight, Resources resources, GameCanvas gameCanvas, Context context) {
        super(renderWidth, renderHeight, resources, gameCanvas, context);
        halfRenderHeight = renderHeight / 2;
        initializeMatrices();
    }


    public void updateFrame() {
        // draw on frame
        Canvas bitmapCanvas = new Canvas(frame);
        // draw top

        bitmapCanvas.setMatrix(topMatrix);
        bitmapCanvas.save();
//         draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
//        // draw platforms

        for (Platform platform : platforms) {
            if (platform.rect.bottom >= cameraY) {
                platform.render(bitmapCanvas, this);
            }
        }


        //draw player
        player.render(bitmapCanvas, this);


        // draw bottom
        bitmapCanvas.restore();
        bitmapCanvas.setMatrix(bottomMatrix);
        bitmapCanvas.save();
        // draw background
        bitmapCanvas.drawBitmap(backgroundImg, 0, 0, gamePaint);
//         draw platforms
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

    private void initializeMatrices() {
        topMatrix = new Matrix();
        bottomMatrix = new Matrix();

        topMatrix.preScale(1, 0.5f);
        topMatrix.postRotate(180);
        topMatrix.postTranslate(cameraWidth, cameraHeight / 2f);
        bottomMatrix.preScale(1, 0.5f);
        bottomMatrix.postTranslate(0, cameraHeight / 2f);
    }
}
