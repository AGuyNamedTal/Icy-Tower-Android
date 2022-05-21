package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Color;
import android.graphics.Point;

import java.util.Arrays;

public class ColorWheelTxtControl extends TextControl implements UpdatingControl {

    private final int periodTime;
    private int currentTime = 0;
    private final float[] hsv = new float[]{0, 1, 1};

    public ColorWheelTxtControl(Point point, String text, float textSize, int textColor, boolean centerX, int periodTime) {
        super(point, text, textSize, textColor, centerX);
        this.periodTime = periodTime;
    }

    @Override
    public void update(int msPassed) {
        currentTime = (currentTime + msPassed) % periodTime;
        hsv[0] = 360f / periodTime * currentTime;
        int rgb = Color.HSVToColor(hsv);
        super.paint.setColor(rgb);
    }

    @Override
    public String toString() {
        return "ColorWheelTxtControl{" +
                "periodTime=" + periodTime +
                ", currentTime=" + currentTime +
                ", hsv=" + Arrays.toString(hsv) +
                "} " + super.toString();
    }
}
