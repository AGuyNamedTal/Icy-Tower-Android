package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class Control {



    protected boolean isEnabled;
    protected boolean isVisible;
    protected Rect rect;
    protected OnControlTouchListener onTouch;


    public abstract void render(Canvas canvas);


    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public OnControlTouchListener getOnTouch() {
        return onTouch;
    }

    public void setOnTouch(OnControlTouchListener onTouch) {
        this.onTouch = onTouch;
    }

    @Override
    public String toString() {
        return "Control{" +
                "isEnabled=" + isEnabled +
                ", isVisible=" + isVisible +
                ", rect=" + rect +
                ", onTouch=" + onTouch +
                '}';
    }
}
