package com.talv.icytower.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class RectControl extends Control {

    private Paint paint;

    public RectControl(Rect rect, int color, int thickness) {
        this.rect = rect;
        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }
}
