package com.talv.icytower.scoreboard;

import com.google.firebase.database.DatabaseException;

public class ScoreboardResult {
    public DatabaseException exception;
    public ScoreboardData[] scoreboardData;

    public boolean isSuccess() {
        return exception == null;
    }

    public ScoreboardResult(DatabaseException exception) {
        this.exception = exception;
    }

    public ScoreboardResult(ScoreboardData[] scoreboardData) {
        this.scoreboardData = scoreboardData;
    }
}