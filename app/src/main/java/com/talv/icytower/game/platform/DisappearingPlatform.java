package com.talv.icytower.game.platform;

import android.graphics.Paint;

public class DisappearingPlatform extends Platform {

    private static final float STARTING_OPACITY = 0.75f;
    private static final int STARTING_ALPHA = (int) (255 * STARTING_OPACITY);
    private static final int DISAPPEARING_PLATFORM_LIFESPAN = 500;

    private final Paint paint;
    private double timeLeft;

    private boolean disappearing = false;


    public DisappearingPlatform(PlatformTypes type, int num, int x, int y, int width, boolean drawCorners) {
        super(type, num, x, y, width, drawCorners);
        timeLeft = DISAPPEARING_PLATFORM_LIFESPAN;
        paint = new Paint();
        paint.setAlpha(STARTING_ALPHA);
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    public void onPlayerFall() {
        disappearing = true;
    }

    // returns true if the platform should be removed, false otherwise
    public boolean tick(int msPassed) {
        if (disappearing) {
            int newAlpha = Math.max(STARTING_ALPHA - (int) (STARTING_ALPHA * (msPassed / timeLeft)), 0);
            paint.setAlpha(newAlpha);
            timeLeft -= msPassed;
            if (timeLeft <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "DisappearingPlatform{" +
                "paint=" + paint +
                ", lifespan=" + timeLeft +
                ", disappearing=" + disappearing +
                "} " + super.toString();
    }

}
