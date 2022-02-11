package com.talv.icytower.game.platform;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.game.Engine;

public class PlatformImage {


    private Bitmap middleBitmap;
    private Bitmap leftBitmap;
    private Bitmap rightBitmap;
    private int minWidth;

    private int rightBitmapWidth;
    private int leftBitmapWidth;
    private int height;
    public int linesFilled;


    public PlatformImage(Bitmap middleBitmap, Bitmap leftBitmap, Bitmap rightBitmap) {
        this.middleBitmap = middleBitmap;
        this.rightBitmap = rightBitmap;
        this.leftBitmap = leftBitmap;

        leftBitmapWidth = leftBitmap.getWidth();
        rightBitmapWidth = rightBitmap.getWidth();
        minWidth = leftBitmapWidth + rightBitmapWidth;
        height = middleBitmap.getHeight();
        linesFilled = ImageHelper.measureLinesWithPixels(leftBitmap);
    }

    public Bitmap createPlatformImage(int width, boolean withCorners) {
        if (withCorners) {
            if (width < minWidth) {
                return ImageHelper.stretch(createPlatformImage(minWidth, withCorners), width, height, true);
            } else {
                Bitmap platformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(platformBitmap);
                canvas.drawBitmap(leftBitmap, 0, 0, Engine.gamePaint);
                int middleBitmapWidth = width - leftBitmapWidth - rightBitmapWidth;
                if (middleBitmapWidth > 0) {
                    Bitmap tiledMiddle = ImageHelper.tileImageX(middleBitmap, middleBitmapWidth, height, false);
                    canvas.drawBitmap(tiledMiddle,
                            leftBitmapWidth, 0, Engine.gamePaint);
                    tiledMiddle.recycle();
                }
                canvas.drawBitmap(rightBitmap, width - rightBitmapWidth, 0, Engine.gamePaint);
                return platformBitmap;
            }
        } else {
            return ImageHelper.tileImageX(middleBitmap, width, height, false);
        }
    }

    public void recycle() {
        middleBitmap.recycle();
        rightBitmap.recycle();
        leftBitmap.recycle();
    }


}
