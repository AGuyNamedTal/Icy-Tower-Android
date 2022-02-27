package com.talv.icytower.scoreboard;

import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;

public class ScoreboardData {
    public UserProfileInfo profileInfo;
    public GameStats bestGameStats;
    public String user;

    public ScoreboardData(UserProfileInfo profileInfo, GameStats bestGameStats, String user) {
        this.profileInfo = profileInfo;
        this.bestGameStats = bestGameStats;
        this.user = user;
    }


}
