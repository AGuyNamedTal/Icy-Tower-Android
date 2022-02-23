package com.talv.icytower.gui.graphiccontrols;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.talv.icytower.game.engine.Engine;

public class ClockControl extends ImageControl implements UpdatingControl {


    private Bitmap arrow;
    private Matrix matrix = new Matrix();

    public long currentTime = 0;
    public long timeTillSpeedIncrease = 1;
    public boolean countTime = false;

    public OnClockTimeUpListener onClockTimeUpListener;

    private final float STARTING_ANGLE = 180;


    public ClockControl(Rect rect, Bitmap clock, Bitmap arrow) {
        super(rect, clock);
        this.arrow = arrow;
        updateMatrix();
    }

    @Override
    public void render(Canvas canvas) {
        super.render(canvas);
        // render arrow:
        updateMatrix();
        canvas.drawBitmap(arrow, matrix, Engine.gamePaint);
    }

    @Override
    public void update(int msPassed) {
        if (!countTime) return;
        currentTime += msPassed;
        if (currentTime > timeTillSpeedIncrease) {
            currentTime = currentTime % timeTillSpeedIncrease;
            timeTillSpeedIncrease = onClockTimeUpListener.clockTimeUp(timeTillSpeedIncrease);
        }
    }

    private void updateMatrix() {
        float angleOfRotation = ((360f * currentTime / timeTillSpeedIncrease) + STARTING_ANGLE) % 360;
        matrix.setRotate(angleOfRotation);
        matrix.postTranslate(rect.exactCenterX() + (arrow.getWidth() / 2), rect.exactCenterY());
    }
}
