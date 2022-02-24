package com.talv.icytower.game;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.talv.icytower.RectHelper;
import com.talv.icytower.gui.GUI;
import com.talv.icytower.gui.graphiccontrols.Control;
import com.talv.icytower.gui.graphiccontrols.TextControl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class GameCanvas extends SurfaceView implements SurfaceHolder.Callback {


    public final Map<Integer, Control> controls = new HashMap<>();

    private static final int MAX_FINGERS = 10;
    private final PointF[] FINGERS = new PointF[MAX_FINGERS];
    //public static PointF[] CLICK_FINGERS = new PointF[MAX_FINGERS];
    public SurfaceHolder holder;

    public AtomicInteger activeControls = new AtomicInteger(0);

    public GameCanvas(Context context) {
        super(context);
        setClickable(true);
        getHolder().addCallback(this);
    }


    public void initializeGUI(Resources resources, int renderWidth, int renderHeight) {
        GUI.buildControls(controls, resources, renderWidth, renderHeight);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int cappedPointerCount = Math.min(pointerCount, MAX_FINGERS);
        int actionIndex = event.getActionIndex();
        int action = event.getActionMasked();
        int id = event.getPointerId(actionIndex);
        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) && id < MAX_FINGERS) {
            FINGERS[id] = new PointF(event.getX(actionIndex), event.getY(actionIndex));
        } else if ((action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP) && id < MAX_FINGERS) {
            // CLICK_FINGERS[id] = new PointF(FINGERS[id].x, FINGERS[id].y);
            FINGERS[id] = null;

        }
        int activeControls = 0;
        for (int i = 0; i < cappedPointerCount; i++) {
            if (FINGERS[i] != null) {
                int index = event.findPointerIndex(i);
                try {
                    PointF fingerPoint = new PointF(event.getX(index), event.getY(index));
                    FINGERS[index] = fingerPoint;
                    for (Map.Entry<Integer, Control> controlEntry : controls.entrySet()) {
                        Control control = controlEntry.getValue();
                        if (!control.isEnabled) continue;
                        if (RectHelper.isPointInRect(control.rect, fingerPoint.x, fingerPoint.y)) {
                            activeControls |= controlEntry.getKey();
                        }
                    }

                } catch (IllegalArgumentException ex) {
                    Log.d("ERROR", "finger weird", ex);
                }
            }
        }
        this.activeControls.set(activeControls);
        return true;
    }

    public void renderControls(Canvas canvas) {
        for (Map.Entry<Integer, Control> entry : controls.entrySet()) {
            Control control = entry.getValue();
            if (!control.isVisible) continue;
            control.render(canvas);
        }
    }

    public void setEnabledAndVisible(int controlsID, boolean val) {
        for (int i = 0; i < 32; i++) {
            int bit = 1 << i;
            if ((controlsID & bit) == bit) {
                Control control = controls.get(bit);
                if (control == null) {
                    System.out.println("h");
                }
                control.isVisible = val;
                control.isEnabled = val;
            }
        }
    }

    public void updateText(int controlID, String newText) {
        Control control = controls.get(controlID);
        if (control instanceof TextControl) {
            ((TextControl) control).setText(newText);
        }
    }

    public void updateControlsPositions(int controlsID) {
        if (!GUI.CONTROLS.CONTROL_POSITIONS_PER_GROUP.containsKey(controlsID)) return;
        HashMap<Integer, Rect> positions = GUI.CONTROLS.CONTROL_POSITIONS_PER_GROUP.get(controlsID);
        for (Map.Entry<Integer, Rect> controlPosition :
                positions.entrySet()) {
            int controlId = controlPosition.getKey();
            Rect controlRect = controlPosition.getValue();
            if (controls.containsKey(controlId))
                controls.get(controlPosition.getKey()).rect = controlRect;
        }
    }

    public Control getControl(int controlID) {
        return controls.get(controlID);
    }
}
