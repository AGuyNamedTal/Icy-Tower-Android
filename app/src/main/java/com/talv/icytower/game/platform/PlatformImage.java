package com.talv.icytower.game.platform;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.utils.BitmapUtils;

public class PlatformImage {


    private final Bitmap middleBitmap;
    private final Bitmap leftBitmap;
    private final Bitmap rightBitmap;
    private final int minWidth;

    private final int rightBitmapWidth;
    private final int leftBitmapWidth;
    private final int height;
    private final int linesFilled;

    public int getLinesFilled() {
        return linesFilled;
    }


    public PlatformImage(Bitmap middleBitmap, Bitmap leftBitmap, Bitmap rightBitmap) {
        this.middleBitmap = middleBitmap;
        this.rightBitmap = rightBitmap;
        this.leftBitmap = leftBitmap;

        leftBitmapWidth = leftBitmap.getWidth();
        rightBitmapWidth = rightBitmap.getWidth();
        minWidth = leftBitmapWidth + rightBitmapWidth;
        height = middleBitmap.getHeight();
        linesFilled = BitmapUtils.measureLinesWithPixels(leftBitmap);
    }

    public Bitmap createPlatformImage(int width, boolean withCorners) {
        if (withCorners) {
            if (width < minWidth) {
                return BitmapUtils.stretch(createPlatformImage(minWidth, true), width, height, true);
            } else {
                Bitmap platformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(platformBitmap);
                canvas.drawBitmap(leftBitmap, 0, 0, Engine.getGamePaint());
                int middleBitmapWidth = width - leftBitmapWidth - rightBitmapWidth;
                if (middleBitmapWidth > 0) {
                    Bitmap tiledMiddle = BitmapUtils.tileImageX(middleBitmap, middleBitmapWidth, height, false);
                    canvas.drawBitmap(tiledMiddle,
                            leftBitmapWidth, 0, Engine.getGamePaint());
                    tiledMiddle.recycle();
                }
                canvas.drawBitmap(rightBitmap, width - rightBitmapWidth, 0, Engine.getGamePaint());
                return platformBitmap;
            }
        } else {
            return BitmapUtils.tileImageX(middleBitmap, width, height, false);
        }
    }

    public void recycle() {
        middleBitmap.recycle();
        rightBitmap.recycle();
        leftBitmap.recycle();
    }

    @Override
    public String toString() {
        return "PlatformImage{" +
                "middleBitmap=" + middleBitmap +
                ", leftBitmap=" + leftBitmap +
                ", rightBitmap=" + rightBitmap +
                ", minWidth=" + minWidth +
                ", rightBitmapWidth=" + rightBitmapWidth +
                ", leftBitmapWidth=" + leftBitmapWidth +
                ", height=" + height +
                ", linesFilled=" + linesFilled +
                '}';
    }

}
