package com.talv.icytower.gui;

import android.graphics.Paint;
import android.graphics.Typeface;

public class TextSizeHelper {
    private static final Paint PAINT = new Paint();

    private static final int PAINT_TXT_SIZE = 10;

    static {
        PAINT.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        PAINT.setTextSize(PAINT_TXT_SIZE);
    }

    public static int getTextSizeFromWidth(String text, float width) {
        return (int) Math.round(PAINT_TXT_SIZE * width / PAINT.measureText(text));
    }

}
