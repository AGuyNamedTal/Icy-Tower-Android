package com.talv.icytower.game.platform;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.talv.icytower.R;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.game.utils.RectUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Platform {

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

    public static final PlatformTypes[] PLATFORM_TYPE_BY_LEVEL;

    private static final int NUMBER_ON_EVERY_N_PLAT = 10;

    private static final HashMap<PlatformTypes, PlatformImage> platformImages = new HashMap<>();
    private static int platformHeight;


    public Rect rect;
    public Bitmap image;
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

    private static final int NUMBER_COLOR = 0xFFf7a043;
    private static final int NUMBER_STROKE_COLOR = 0xFF000000;

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
                    BitmapUtils.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.middleResId),
                            platformHeight, true),
                    BitmapUtils.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.leftResId),
                            platformHeight, true),
                    BitmapUtils.stretchToHeight(BitmapFactory.decodeResource(resources, platformType.rightResId),
                            platformHeight, true)
            ));
        }
    }

    public Platform(PlatformTypes type, int num, int x, int y, int width, boolean drawCorners) {
        this(type, num, x, y, width, num % NUMBER_ON_EVERY_N_PLAT == 0 && num != 0, drawCorners);
    }

    public Platform(PlatformTypes type, int num, int x, int y, int width, boolean drawNumberOnPlatform, boolean drawCorners) {
        platformNumber = num;
        PlatformImage platformImage = platformImages.get(type);
        image = platformImage.createPlatformImage(width, drawCorners);
        rect = RectUtils.rectFromWidthHeight(x, y, width, platformHeight);
        if (drawNumberOnPlatform) {
            Canvas canvas = new Canvas(image);
            drawNumber(canvas, width, platformImage.linesFilled, num);
        }
    }

    private static void drawNumber(Canvas canvas, int width, int height, int num) {
        String text = String.valueOf(num);
        int verticalPadding = (int) (height * TEXT_Y_PADDING);
        int desiredTextHeight = height - verticalPadding * 2;
        Paint textPaint = new Paint();
        textPaint.setTextSize(desiredTextHeight);
        textPaint.setColor(NUMBER_COLOR);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        Paint strokePaint = new Paint();
        strokePaint.setTextSize(desiredTextHeight);
        strokePaint.setColor(NUMBER_STROKE_COLOR);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        float textX = (width - bounds.width()) / 2f - bounds.left;
        float textY = verticalPadding + (desiredTextHeight - bounds.height()) / 2f - bounds.top;
        canvas.drawText(text, textX, textY, strokePaint);
        canvas.drawText(text, textX, textY, textPaint);

    }

    protected Paint getPaint() {
        return Engine.gamePaint;
    }

    public void onPlayerFall() {
    }

    public void render(Canvas canvas, Engine engine) {
        canvas.drawBitmap(image, rect.left, rect.top - engine.cameraY, getPaint());
    }

    public void recycle() {
        image.recycle();
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
