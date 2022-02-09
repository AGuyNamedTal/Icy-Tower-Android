package com.talv.icytower.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

public class TextControl extends Control {


    private String text;
    protected Paint paint = new Paint();
    private Rect bounds = new Rect();
    private boolean centerX;

    public TextControl(boolean isEnabled, boolean isVisible, Point point, String text, float textSize, int textColor) {
        this(isEnabled, isVisible, point, text, textSize, textColor, false);
    }

    public TextControl(boolean isEnabled, boolean isVisible, Point point, String text, float textSize, int textColor, boolean centerX) {
        paint.setTextSize(textSize);
        this.isEnabled = isEnabled;
        this.isVisible = isVisible;
        this.centerX = centerX;
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.rect = new Rect(point.x, point.y, point.x, point.y);
        setText(text);
    }


    public TextControl(Point point, String text, float textSize, int textColor) {
        this(false, false, point, text, textSize, textColor);
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        paint.getTextBounds(text, 0, text.length(), bounds);
    }

    @Override
    public void render(Canvas canvas) {
        int x = rect.left - bounds.left;
        int y = rect.top - bounds.top;
        if (centerX) {
            // center text
            x -= bounds.width() / 2;
        }
        canvas.drawText(text, x, y, paint);
    }

    private static final Paint TEXT_HEIGHT_PAINT = new Paint();
    private static final Rect TEXT_HEIGHT_BOUNDS = new Rect();

    static {
        TEXT_HEIGHT_PAINT.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public static int getTextSize(String text, int width) {
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
