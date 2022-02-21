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

    public TextControl( Point point, String text, float textSize, int textColor) {
        this( point, text, textSize, textColor, false);
    }

    public TextControl( Point point, String text, float textSize, int textColor, boolean centerX) {
        paint.setTextSize(textSize);
        this.isEnabled = false;
        this.isVisible = false;
        this.centerX = centerX;
        paint.setColor(textColor);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        this.rect = new Rect(point.x, point.y, point.x, point.y);
        setText(text);
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




}
