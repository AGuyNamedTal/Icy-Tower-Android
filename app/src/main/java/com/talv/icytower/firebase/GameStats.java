package com.talv.icytower.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.Map;

public class GameStats {

    private int highscore;
    private int totalJumps;
    private long timeTaken;

    public int getHighscore() {
        return highscore;
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }

    public int getTotalJumps() {
        return totalJumps;
    }

    public void setTotalJumps(int totalJumps) {
        this.totalJumps = totalJumps;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }


    public GameStats() {

    }

    public GameStats(int highscore, int totalJumps, long timeTaken) {
        this.highscore = highscore;
        this.totalJumps = totalJumps;
        this.timeTaken = timeTaken;
    }

    public GameStats(Map<String, Object> map) {
        this(((Long) map.get("highscore")).intValue(), ((Long) map.get("totalJumps")).intValue(), (long) map.get("timeTaken"));
    }

    @Override
    public String toString() {
        return "GameStats{" +
                "highscore=" + highscore +
                ", totalJumps=" + totalJumps +
                ", timeTaken=" + timeTaken +
                '}';
    }


}
