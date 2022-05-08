package com.talv.icytower.firebase;

import android.app.Activity;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UserProfileInfo {

    private long creationDate;
    private int gamesPlayed;
    private String countryCode;

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public UserProfileInfo() {

    }

    public UserProfileInfo(long creationDate, int gamesPlayed, String countryCode) {
        this.creationDate = creationDate;
        this.gamesPlayed = gamesPlayed;
        this.countryCode = countryCode;
    }

    public UserProfileInfo(Map<String, Object> map) {
        this((long) map.get("creationDate"), ((Long) map.get("gamesPlayed")).intValue(), (String) map.get("countryCode"));
    }

    public static UserProfileInfo createNew(Activity context) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String countryCode = null;
        try {
            countryCode = new GetIPFromInternetTask().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (countryCode == null) {
            countryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        }
        return new UserProfileInfo(unixTime, 0, countryCode);
    }

    @Override
    public String toString() {
        return "UserProfileInfo{" +
                "creationDate=" + creationDate +
                ", gamesPlayed=" + gamesPlayed +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }


}
