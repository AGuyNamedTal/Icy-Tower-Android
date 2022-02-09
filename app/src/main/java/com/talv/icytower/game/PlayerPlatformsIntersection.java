package com.talv.icytower.game;

import android.util.Log;

public class PlayerPlatformsIntersection {

    public int newY;
    public boolean didIntersect = false;

    public PlayerPlatformsIntersection(int newY, boolean didIntersect) {
        this.newY = newY;
        this.didIntersect = didIntersect;
    }

    public PlayerPlatformsIntersection(int newY) {
        this.newY = newY;
    }
}
