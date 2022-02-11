package com.talv.icytower.game.platform;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.talv.icytower.ImageHelper;
import com.talv.icytower.R;
import com.talv.icytower.RectHelper;
import com.talv.icytower.game.Engine;
import com.talv.icytower.gui.TextSizeHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Platform {

    public static final PlatformTypes[] PLATFORM_TYPE_BY_LEVEL;
    private static final int NUMBER_COLOR = 0xFFf7a043;
    private static final int NUMBER_STROKE_COLOR = 0xFF000000;


    private static final HashMap<PlatformTypes, PlatformImage> platformImages = new HashMap<>();
    private static int platformHeight;


    public Rect rect;
    public Bitmap image;
    public boolean enabled = true;
    public int platformNumber;

    static {
        PLATFORM_TYPE_BY_LEVEL = PlatformTypes.values();
        Arrays.sort(PLATFORM_TYPE_BY_LEVEL, new Comparator<PlatformTypes>() {
            @Override
            public int compare(PlatformTypes o1, PlatformTypes o2) {
                return o1.level - o2.level;
            }
        });
    }

    public Platform(PlatformTypes type, int num, int x, int y, int width, boolean drawCorners) {
        this(type, num, x, y, width, num % 10 == 0 && num != 0, drawCorners);
    }

    public Platform(PlatformTypes type, int num, int x, int y, int width, boolean drawNumberOnPlatform, boolean drawCorners) {
        platformNumber = num;
        image = platformImages.get(type).createPlatformImage(width, drawCorners);
        rect = RectHelper.rectFromWidthHeight(x, y, width, platformHeight);
        if (drawNumberOnPlatform) {
            Canvas canvas = new Canvas(image);
            drawNumber(canvas, width, platformHeight / 2, num);
        }
    }

    public static int getPlatformHeight() {
        return platformHeight;
    }

    private static final float TEXT_Y_PADDING = 0.03f;

    public static void loadBitmaps(Resources resources, int platformHeight) {
        Platform.platformHeight = platformHeight;
        emptyLoadedPlatforms();
        PlatformTypes[] platformTypes = PlatformTypes.values();
        for (PlatformTypes platformType : platformTypes) {
            platformImages.put(platformType, new PlatformImage(
                    ImageHelper.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.middleResId),
                            platformHeight, true),
                    ImageHelper.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.leftResId),
                            platformHeight, true),
                    ImageHelper.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.rightResId),
                            platformHeight, true)
            ));
        }
    }

    private static void drawNumber(Canvas canvas, int width, int height, int num) {
        String text = String.valueOf(num);
        int verticalPadding = (int) (height * TEXT_Y_PADDING);
        int desiredTextHeight = height - verticalPadding * 2;
        Paint textPaint = new Paint();
        // find fitting text size
        int textSize = TextSizeHelper.getTextSize(text, width);
        textPaint.setTextSize(textSize);
        textPaint.setColor(NUMBER_COLOR);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text,
                (width - bounds.width()) / 2f - bounds.left,
                verticalPadding + (desiredTextHeight - bounds.height()) / 2f - bounds.top,
                textPaint);

    }

    public enum PlatformTypes {
        LEVEL_0(0, R.drawable.platform_0_left, R.drawable.platform_0_mid, R.drawable.platform_0_right),
        LEVEL_1(1, R.drawable.platform_1_left, R.drawable.platform_1_mid, R.drawable.platform_1_right),
        LEVEL_2(2, R.drawable.platform_2_left, R.drawable.platform_2_mid, R.drawable.platform_2_right);

        private final int middleResId;
        private final int rightResId;
        private final int leftResId;
        private final int level;

        PlatformTypes(int level, int leftResId, int middleResId, int rightResId) {
            this.level = level;
            this.middleResId = middleResId;
            this.rightResId = rightResId;
            this.leftResId = leftResId;
        }
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


    public static void emptyLoadedPlatforms() {
        for (PlatformImage platformImage : platformImages.values()) {
            if (platformImage != null) {
                platformImage.recycle();
            }
        }
        platformImages.clear();
    }


}
