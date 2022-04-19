package com.talv.icytower.game;

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

    @Override
    public String toString() {
        return "PlayerPlatformsIntersection{" +
                "newY=" + newY +
                ", didIntersect=" + didIntersect +
                '}';
    }
}
