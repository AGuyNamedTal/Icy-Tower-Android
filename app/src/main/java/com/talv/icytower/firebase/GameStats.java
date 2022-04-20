package com.talv.icytower.firebase;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.Map;

@IgnoreExtraProperties
public class GameStats {

    private static final String HIGH_SCORE_NAME = "high_score";
    private static final String TOTAL_JUMPS_NAME = "total_jumps";
    private static final String TIME_TAKEN_NAME = "time_taken";
    @PropertyName(HIGH_SCORE_NAME)
    private int highscore;
    @PropertyName(TOTAL_JUMPS_NAME)
    private int totalJumps;
    @PropertyName(TIME_TAKEN_NAME)
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

        this(((Long) map.get(HIGH_SCORE_NAME)).intValue(), ((Long) map.get(TOTAL_JUMPS_NAME)).intValue(), (long) map.get(TIME_TAKEN_NAME));
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
