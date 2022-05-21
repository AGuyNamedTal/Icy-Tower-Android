package com.talv.icytower.game.gui.graphiccontrols;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.talv.icytower.game.engine.Engine;

public class ClockControl extends ImageControl implements UpdatingControl {


    private final Bitmap arrow;
    private final Matrix matrix = new Matrix();

    private long currentTime = 0;
    private long timeTillSpeedIncrease = 1;
    private boolean countTime = false;
    private OnClockTimeUpListener onClockTimeUpListener;

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void setTimeTillSpeedIncrease(long timeTillSpeedIncrease) {
        this.timeTillSpeedIncrease = timeTillSpeedIncrease;
    }

    public void setCountTime(boolean countTime) {
        this.countTime = countTime;
    }

    public void setOnClockTimeUpListener(OnClockTimeUpListener onClockTimeUpListener) {
        this.onClockTimeUpListener = onClockTimeUpListener;
    }


    private final float STARTING_ANGLE = 180;


    public ClockControl(Rect rect, Bitmap clock, Bitmap arrow) {
        super(rect, clock);
        this.arrow = arrow;
        updateMatrix();
    }

    private void updateMatrix() {
        float angleOfRotation = ((360f * currentTime / timeTillSpeedIncrease) + STARTING_ANGLE) % 360;
        matrix.setRotate(angleOfRotation);
        matrix.postTranslate(getRect().exactCenterX() + (arrow.getWidth() / 2f), getRect().exactCenterY());
    }

    @Override
    public void render(Canvas canvas) {
        super.render(canvas);
        // render arrow:
        updateMatrix();
        canvas.drawBitmap(arrow, matrix, Engine.getGamePaint());
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


    @Override
    public String toString() {
        return "ClockControl{" +
                "arrow=" + arrow +
                ", matrix=" + matrix +
                ", currentTime=" + currentTime +
                ", timeTillSpeedIncrease=" + timeTillSpeedIncrease +
                ", countTime=" + countTime +
                ", onClockTimeUpListener=" + onClockTimeUpListener +
                ", STARTING_ANGLE=" + STARTING_ANGLE +
                "} " + super.toString();
    }


}
