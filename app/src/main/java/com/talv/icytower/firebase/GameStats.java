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
    public int highscore;
    @PropertyName(TOTAL_JUMPS_NAME)
    public int totalJumps;
    @PropertyName(TIME_TAKEN_NAME)
    public long timeTaken;

    public GameStats() {

    }

    public GameStats(int highscore, int totalJumps, long timeTaken) {
        this.highscore = highscore;
        this.totalJumps = totalJumps;
        this.timeTaken = timeTaken;
    }

    public GameStats(Map<String, Object> map) {
        this((int) map.get(HIGH_SCORE_NAME), (int) map.get(TOTAL_JUMPS_NAME), (long) map.get(TIME_TAKEN_NAME));
    }
}
