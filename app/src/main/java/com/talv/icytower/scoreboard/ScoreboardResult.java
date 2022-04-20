package com.talv.icytower.scoreboard;

import java.util.Arrays;

public class ScoreboardResult {
    private Exception exception;
    private ScoreboardData[] scoreboardData;

    public Exception getException() {
        return exception;
    }

    public ScoreboardData[] getScoreboardData() {
        return scoreboardData;
    }

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