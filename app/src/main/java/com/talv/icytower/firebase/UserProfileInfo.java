package com.talv.icytower.firebase;

import android.app.Activity;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@IgnoreExtraProperties
public class UserProfileInfo {

    private static final String CREATION_DATE_NAME = "creation_date";
    private static final String GAMES_PLAYED_NAME = "games_played";
    private static final String COUNTRY_CODE_NAME = "country_code";
    @PropertyName(CREATION_DATE_NAME)
    private long creationDate;
    @PropertyName(GAMES_PLAYED_NAME)
    private int gamesPlayed;
    @PropertyName(COUNTRY_CODE_NAME)
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
        this((long) map.get(CREATION_DATE_NAME), ((Long) map.get(GAMES_PLAYED_NAME)).intValue(), (String) map.get(COUNTRY_CODE_NAME));
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
