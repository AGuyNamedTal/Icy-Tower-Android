package com.talv.icytower.game.platform;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.talv.icytower.R;
import com.talv.icytower.game.Engine;
import com.talv.icytower.ImageHelper;
import com.talv.icytower.RectHelper;

import java.util.HashMap;

public class Platform {
    public enum PlatformTypes {
        BASIC_0(R.drawable.platform_basic_0);
        public final int resId;

        PlatformTypes(int resId) {
            this.resId = resId;
        }
    }

    private static final PlatformTypes[][] platformsByLevel = new PlatformTypes[][]{
            new PlatformTypes[]{PlatformTypes.BASIC_0}
    };


    private static HashMap<PlatformTypes, Bitmap> basicTiles = new HashMap<>();


    public Rect rect;
    public Bitmap image;
    public PlatformTypes type;
    public boolean enabled = true;
    public int platformNumber;


    public Platform(PlatformTypes type, int num, int x, int y, int width, int height) {
        this(type, num, x, y, width, height, num % 10 == 0 && num != 0);
    }

    public Platform(PlatformTypes type, int num, int x, int y, int width, int height, boolean drawNumberOnPlatform) {
        platformNumber = num;
        image = ImageHelper.tileImageX(basicTiles.get(type), width, height, false);
        rect = RectHelper.rectFromWidthHeight(x, y, width, height);
        if (drawNumberOnPlatform) {
            Canvas canvas = new Canvas(image);
            drawNumber(canvas, width, height, num);
        }
    }

    private static final float TEXT_Y_PADDING = 0.03f;
    private static final int NUMBER_COLOR = 0xFF4868db;

    private static void drawNumber(Canvas canvas, int width, int height, int num) {
        String text = String.valueOf(num);
        int verticalPadding = (int) (height * TEXT_Y_PADDING);
        int desiredTextHeight = height - verticalPadding * 2;
        Paint textPaint = new Paint();
        // find fitting text size
        int textSize = 10;
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textSize = (int) (textSize * desiredTextHeight / textPaint.measureText(text));
        textPaint.setTextSize(textSize);

        textPaint.setColor(NUMBER_COLOR);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text,
                (width - bounds.width()) / 2 - bounds.left,
                verticalPadding + (desiredTextHeight - bounds.height()) / 2 - bounds.top,
                textPaint);

    }

    protected Paint getPaint() {
        return Engine.gamePaint;
    }

    public void onPlayerFall() {
        Log.d("hey", "fell on platform " + platformNumber);
    }

    public void render(Canvas canvas, Engine engine) {
        canvas.drawBitmap(image, rect.left, rect.top - engine.cameraY, getPaint());
    }

    public void recycle() {
        image.recycle();
        enabled = false;
    }

    public static void loadBitmaps(Resources resources, int level) {
        emptyLoadedPlatforms();
        PlatformTypes[] platformTypes = platformsByLevel[level];
        for (PlatformTypes platformType : platformTypes) {
            basicTiles.put(platformType, BitmapFactory.decodeResource(resources, platformType.resId));
        }
    }

    public static void emptyLoadedPlatforms() {
        for (Bitmap img : basicTiles.values()) {
            if (img != null) {
                img.recycle();
            }
        }
        basicTiles.clear();
    }


}
