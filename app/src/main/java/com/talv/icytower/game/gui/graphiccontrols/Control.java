package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class Control {

    public boolean isEnabled;
    public boolean isVisible;
    public Rect rect;
    public OnControlTouchListener onTouch;


    public abstract void render(Canvas canvas);
}
