package com.talv.icytower.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.talv.icytower.game.GameSettings;
import com.talv.icytower.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_SP_FILE_NAME = "GAME_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.settingsBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });

        Switch backgSwitch = ((Switch)findViewById(R.id.backgroundMusicSwitch));
        backgSwitch.setChecked(GameSettings.BACKG_MUSIC);
        backgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                GameSettings.BACKG_MUSIC = b;
            }
        });
        Switch sfxSwitch = ((Switch)findViewById(R.id.soundFxSwitch));
        sfxSwitch.setChecked(GameSettings.SFX);
        sfxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                GameSettings.SFX = b;
            }
        });
        Switch vibSwitch = ((Switch)findViewById(R.id.vibrationSwitch));
        vibSwitch.setChecked(GameSettings.VIBRATE);
        vibSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                GameSettings.VIBRATE = b;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        GameSettings.saveSettingsToSP(getSharedPreferences(SETTINGS_SP_FILE_NAME, MODE_PRIVATE));
    }
}
