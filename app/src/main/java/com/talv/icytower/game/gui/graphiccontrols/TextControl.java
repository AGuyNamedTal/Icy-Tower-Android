package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.talv.icytower.game.utils.RectUtils;

public class TextControl extends Control {


    private String text;
    protected Paint paint = new Paint();
    private final Rect bounds = new Rect();
    private final Point centerXPoint;

    public TextControl(Point point, String text, float textSize, int textColor) {
        this(point, text, textSize, textColor, false);
    }

    public TextControl(Point point, String text, float textSize, int textColor, boolean centerX) {
        paint.setTextSize(textSize);
        this.isEnabled = false;
        this.isVisible = false;
        if (centerX) {
            this.centerXPoint = point;
        } else {
            this.centerXPoint = null;
        }
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
        // update rect
        int width = bounds.width();
        int height = bounds.height();
        if (centerXPoint == null) {
            RectUtils.setRectSize(rect, width, height);
        } else {
            rect = new Rect(centerXPoint.x - width / 2, centerXPoint.y - height / 2,
                    centerXPoint.x + width / 2, centerXPoint.y + height / 2);
        }

    }

    @Override
    public void render(Canvas canvas) {
        int x = rect.left - bounds.left;
        int y = rect.top - bounds.top;
        canvas.drawText(text, x, y, paint);
    }


}
