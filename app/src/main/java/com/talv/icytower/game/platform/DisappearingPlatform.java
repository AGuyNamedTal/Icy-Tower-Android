package com.talv.icytower.game.platform;

import android.graphics.Paint;

public class DisappearingPlatform extends Platform {

    public Paint paint;
    public double lifespan;


    public DisappearingPlatform(PlatformTypes type, int num, int x, int y, int width, int height, int lifespan) {
        super(type, num, x, y, width, height);
        this.lifespan = lifespan;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    public void tick(int msPassed) {
        if (enabled) {
            paint.setAlpha(255 - (int) (255 * (msPassed / lifespan)));
            lifespan -= msPassed;
            if (lifespan <= 0) {
                enabled = false;
            }
        }
    }
}
