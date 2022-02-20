package com.talv.icytower.game;

import android.util.DisplayMetrics;
import android.view.Display;

public class ScreenScaleManager {

    public static final int originalWidth = 250;
    public static final int originalHeight = 550;
    public static int newWidth;
    public static int newHeight;

    private static double scaleX;
    private static double scaleY;

    public static void updateWidthHeight(Display display) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        newWidth = displayMetrics.widthPixels;
        newHeight = displayMetrics.heightPixels;
        scaleX = newWidth / (double)originalWidth;
        scaleY = newHeight / (double)originalHeight;
    }

    public static int scaleXVal(int originalX) {
        return (int)(originalX * scaleX);
    }
    public static int scaleYVal(int originalY) {
        return (int)(originalY * scaleY);
    }

}
