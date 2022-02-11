package com.talv.icytower.gui;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class TextSizeHelper {
    private static final Paint TEXT_HEIGHT_PAINT = new Paint();
    private static final Rect TEXT_HEIGHT_BOUNDS = new Rect();

    static {
        TEXT_HEIGHT_PAINT.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public static int getTextSizeFromWidth(String text, int width) {
        setTextSizeFromWidth(width, text, TEXT_HEIGHT_PAINT);
        TEXT_HEIGHT_PAINT.getTextBounds(text, 0, text.length(), TEXT_HEIGHT_BOUNDS);
        return TEXT_HEIGHT_BOUNDS.height();
    }

    private static void setTextSizeFromWidth(int desiredWidth, String text, Paint paint) {
        int textSize = 10;
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textSize = (int) (textSize * desiredWidth / paint.measureText(text));
        paint.setTextSize(textSize);
    }
}
