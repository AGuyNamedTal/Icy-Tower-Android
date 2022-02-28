package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class Control {

    public boolean isEnabled;
    public boolean isVisible;
    public Rect rect;
    public OnControlTouchListener onTouch;
    public boolean flipY = false;


    public abstract void render(Canvas canvas);

    public Control setFlipY(boolean flipY){
        this.flipY = flipY;
        return this;
    }
}
