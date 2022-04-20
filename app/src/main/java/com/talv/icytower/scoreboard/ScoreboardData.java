package com.talv.icytower.scoreboard;

import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;

public class ScoreboardData {
    private final UserProfileInfo profileInfo;
    private final GameStats bestGameStats;
    private final String user;


    public UserProfileInfo getProfileInfo() {
        return profileInfo;
    }

    public GameStats getBestGameStats() {
        return bestGameStats;
    }

    public String getUser() {
        return user;
    }

    public ScoreboardData(UserProfileInfo profileInfo, GameStats bestGameStats, String user) {
        this.profileInfo = profileInfo;
        this.bestGameStats = bestGameStats;
        this.user = user;
    }

    @Override
    public String toString() {
        return "ScoreboardData{" +
                "profileInfo=" + profileInfo +
                ", bestGameStats=" + bestGameStats +
                ", user='" + user + '\'' +
                '}';
    }

}
