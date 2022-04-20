package com.talv.icytower.game.platform;

import android.graphics.Paint;

public class DisappearingPlatform extends Platform {

    private static final float STARTING_OPACITY = 0.75f;
    private static final int STARTING_ALPHA = (int) (255 * STARTING_OPACITY);
    private static final int DISAPPEARING_PLATFORM_LIFESPAN = 500;

    private final Paint paint;
    private double lifespan;

    private boolean disappearing = false;


    public DisappearingPlatform(PlatformTypes type, int num, int x, int y, int width, boolean drawCorners, int lifespan) {
        super(type, num, x, y, width, drawCorners);
        this.lifespan = lifespan;
        paint = new Paint();
        paint.setAlpha(STARTING_ALPHA);
    }

    public DisappearingPlatform(PlatformTypes type, int num, int x, int y, int width, boolean drawCorners) {
        this(type, num, x, y, width, drawCorners, DISAPPEARING_PLATFORM_LIFESPAN);
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    @Override
    public void onPlayerFall() {
        super.onPlayerFall();
        disappearing = true;
    }

    // returns true if the platform should be removed, false otherwise
    public boolean tick(int msPassed) {
        if (disappearing) {
            int newAlpha = Math.max(STARTING_ALPHA - (int) (STARTING_ALPHA * (msPassed / lifespan)), 0);
            paint.setAlpha(newAlpha);
            lifespan -= msPassed;
            if (lifespan <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DisappearingPlatform{" +
                "paint=" + paint +
                ", lifespan=" + lifespan +
                ", disappearing=" + disappearing +
                "} " + super.toString();
    }

}
