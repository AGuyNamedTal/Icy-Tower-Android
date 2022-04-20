package com.talv.icytower.game;

public class PlayerPlatformsIntersection {

    private final int newY;
    private boolean didIntersect = false;

    public int getNewY() {
        return newY;
    }

    public boolean didIntersect() {
        return didIntersect;
    }

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
