package com.talv.icytower.gui.graphiccontrols;

import android.graphics.Color;
import android.graphics.Point;

public class HighscoreControl extends TextControl implements UpdatingControl {
    public HighscoreControl(boolean isEnabled, boolean isVisible, Point point, String text, float textSize, int textColor) {
        super(isEnabled, isVisible, point, text, textSize, textColor);
    }
    public HighscoreControl(boolean isEnabled, boolean isVisible, Point point, String text, float textSize, int textColor, boolean centerX) {
        super(isEnabled, isVisible, point, text, textSize, textColor, centerX);
    }
    public HighscoreControl(Point point, String text, float textSize, int textColor) {
        super(point, text, textSize, textColor);
    }

    private final int PERIOD_TIME = 2500;
    private int currentTime = 0;
    private float[] hsv = new float[]{0, 1, 1};
    @Override
    public void update(int msPassed) {
        currentTime = (currentTime + msPassed) % PERIOD_TIME;
        hsv[0] = 360f / PERIOD_TIME * currentTime;
        int rgb = Color.HSVToColor(hsv);
        super.paint.setColor(rgb);
    }
}
