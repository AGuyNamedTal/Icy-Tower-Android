package com.talv.icytower.scoreboard;

import java.util.Arrays;

public class ScoreboardResult {
    public Exception exception;
    public ScoreboardData[] scoreboardData;

    public boolean isSuccessful() {
        return exception == null;
    }

    public ScoreboardResult(Exception exception) {
        this.exception = exception;
    }

    public ScoreboardResult(ScoreboardData[] scoreboardData) {
        this.scoreboardData = scoreboardData;
    }

    @Override
    public String toString() {
        return "ScoreboardResult{" +
                "exception=" + exception +
                ", scoreboardData=" + Arrays.toString(scoreboardData) +
                '}';
    }
}