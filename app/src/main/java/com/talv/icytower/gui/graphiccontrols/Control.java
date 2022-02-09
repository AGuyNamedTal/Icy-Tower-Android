package com.talv.icytower.gui.graphiccontrols;

import android.graphics.Canvas;
import android.graphics.Rect;

public abstract class Control {

    public boolean isEnabled;
    public boolean isVisible;
    public Rect rect;
    public OnButtonClickListener onClick;


    public abstract void render(Canvas canvas);
}
