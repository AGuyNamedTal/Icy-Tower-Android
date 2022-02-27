package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Color;
import android.graphics.Point;

public class ColorWheelTxtControl extends TextControl implements UpdatingControl {

    private final int periodTime;

    public ColorWheelTxtControl( Point point, String text, float textSize, int textColor, boolean centerX, int periodTime) {
        super( point, text, textSize, textColor, centerX);
        this.periodTime = periodTime;
    }


    private int currentTime = 0;
    private float[] hsv = new float[]{0, 1, 1};

    @Override
    public void update(int msPassed) {
        currentTime = (currentTime + msPassed) % periodTime;
        hsv[0] = 360f / periodTime * currentTime;
        int rgb = Color.HSVToColor(hsv);
        super.paint.setColor(rgb);
    }
}
