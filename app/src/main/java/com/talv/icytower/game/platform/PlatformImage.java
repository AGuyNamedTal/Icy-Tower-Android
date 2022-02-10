package com.talv.icytower.game.platform;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.game.Engine;

public class PlatformImage {


    private Bitmap middleBitmap;
    private Bitmap leftBitmap;
    private Bitmap rightBitmap;

    private int sideBitmapsWidth;

    public PlatformImage(Bitmap middleBitmap, Bitmap leftBitmap, Bitmap rightBitmap) {
        this.middleBitmap = middleBitmap;
        this.rightBitmap = rightBitmap;
        sideBitmapsWidth = rightBitmap.getWidth();
        this.leftBitmap = leftBitmap;
    }

    public Bitmap createPlatformImage(int width, int height) {
        Bitmap platformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(platformBitmap);
        canvas.drawBitmap(leftBitmap, 0, 0, Engine.gamePaint);
        canvas.drawBitmap(ImageHelper.tileImageX(middleBitmap, width - sideBitmapsWidth * 2, height, false),
                sideBitmapsWidth, 0, Engine.gamePaint);
        canvas.drawBitmap(rightBitmap, width - sideBitmapsWidth, 0, Engine.gamePaint);

        return platformBitmap;
    }

    public void recycle() {
        middleBitmap.recycle();
        rightBitmap.recycle();
        leftBitmap.recycle();
    }


}
