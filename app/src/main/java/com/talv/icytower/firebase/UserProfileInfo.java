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
    public long creationDate;
    @PropertyName(GAMES_PLAYED_NAME)
    public int gamesPlayed;
    @PropertyName(COUNTRY_CODE_NAME)
    public String countryCode;

    public UserProfileInfo() {

    }

    public UserProfileInfo(long creationDate, int gamesPlayed, String countryCode) {
        this.creationDate = creationDate;
        this.gamesPlayed = gamesPlayed;
        this.countryCode = countryCode;
    }

    public UserProfileInfo(Map<String, Object> map) {
        this((long) map.get(CREATION_DATE_NAME), (int) map.get(GAMES_PLAYED_NAME), (String) map.get(COUNTRY_CODE_NAME));
    }

    public static UserProfileInfo createNew(Activity context) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String countryCode = null;
        try {
            countryCode = new GetIPFromInternetTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (countryCode == null) {
            countryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();
        }
        return new UserProfileInfo(unixTime, 0, countryCode);
    }


}
