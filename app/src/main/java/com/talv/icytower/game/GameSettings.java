package com.talv.icytower.game;

import android.content.SharedPreferences;

public class GameSettings {
    public static boolean BACKG_MUSIC;
    public static boolean SFX;
    public static boolean VIBRATE;

    private static String BACKG_KEY = "BACKG_MUSIC";
    private static String SFX_KEY = "SFX";
    private static String VIBRATE_KEY = "VIBRATE";

    private static final boolean BACKG_MUSIC_DEFAULT = true;
    private static final boolean SFX_DEFAULT = true;
    private static final boolean VIBRATE_DEFAULT = true;

    public static void loadSettingsFromSP(SharedPreferences sp){
        BACKG_MUSIC = sp.getBoolean(BACKG_KEY, BACKG_MUSIC_DEFAULT);
        SFX = sp.getBoolean(SFX_KEY, SFX_DEFAULT);
        VIBRATE = sp.getBoolean(VIBRATE_KEY, VIBRATE_DEFAULT);
    }
    public static void saveSettingsToSP(SharedPreferences sp){
        sp.edit()
                .putBoolean(SFX_KEY, SFX)
                .putBoolean(BACKG_KEY, BACKG_MUSIC)
                .putBoolean(VIBRATE_KEY, VIBRATE)
                .apply();
    }

}
