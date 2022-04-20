package com.talv.icytower.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.OnProfilePhotoGetComplete;
import com.talv.icytower.game.GameSettings;
import com.talv.icytower.game.engine.Engine;

import static com.talv.icytower.activities.SettingsActivity.SETTINGS_SP_FILE_NAME;

public class MainActivity extends AppCompatActivity {

    private static final boolean LOGIN_REQUIRED = true;


    private TextView userNameTxt;
    private TextView logOutTxt;
    private ImageView profilePhotoImgView;


    private boolean activityOnForeground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseHelper.initialize();

        GameSettings.loadSettingsFromSP(getSharedPreferences(SETTINGS_SP_FILE_NAME, MODE_PRIVATE));

        Button playSingleBtn = findViewById(R.id.playSingleBtn);
        Button playMultiBtn = findViewById(R.id.playMultiBtn);
        profilePhotoImgView = findViewById(R.id.mainActivityProfilePhoto);
        userNameTxt = findViewById(R.id.userDataTxt);
        logOutTxt = findViewById(R.id.logOutTxt);
        logOutTxt.setOnClickListener(view -> {
            FirebaseHelper.getAuth().signOut();
            updateUI();
        });

        findViewById(R.id.settingsBtn).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        findViewById(R.id.scoreboardBtn).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ScoreboardActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        playSingleBtn.setOnClickListener(v -> startGame(true));
        playMultiBtn.setOnClickListener(view -> startGame(false));
        FirebaseHelper.getAuth().addAuthStateListener(firebaseAuth -> {
            if (activityOnForeground)
                updateUI();
        });

    }


    private void startGame(boolean singleplayer) {
        if (LOGIN_REQUIRED) {
            FirebaseUser currentUser = FirebaseHelper.getAuth().getCurrentUser();
            if (currentUser == null) {
                // login
                startActivity(new Intent(MainActivity.this, LoginActivity.class).putExtra(GameActivity.SINGLEPLAYER_KEY, singleplayer));
                overridePendingTransition(0, 0);
            } else {
                Engine.setUser(currentUser.getDisplayName());
                LoginActivity.loginWithUser(MainActivity.this, singleplayer);
            }
        } else {
            Engine.setUser("");
            LoginActivity.loginWithUser(MainActivity.this, singleplayer);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about_dev_option) {
            startActivity(new Intent(MainActivity.this, AboutDevActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about_game_option) {
            startActivity(new Intent(MainActivity.this, AboutGameActivity.class));
            return true;
        } else if (item.getItemId() == R.id.exit_option) {
            showExitWarning();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityOnForeground = true;
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityOnForeground = false;
    }

    @Override
    public void onBackPressed() {
        showExitWarning();
    }

    private void showExitWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Application Exit")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
    }

    private void updateUI() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            userNameTxt.setText("Currently not logged in");
            logOutTxt.setVisibility(View.GONE);
            profilePhotoImgView.setVisibility(View.GONE);
        } else {
            userNameTxt.setText("Logged in as: " + user.getDisplayName());
            logOutTxt.setVisibility(View.VISIBLE);
            if (FirebaseHelper.hasInternetConnection(MainActivity.this)) {
                user.reload().addOnFailureListener(e -> updateUI());
                FirebaseHelper.getProfilePhotoBitmap(user.getDisplayName(), new OnProfilePhotoGetComplete() {
                    @Override
                    public void onComplete(Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (FirebaseHelper.getAuth().getCurrentUser() == null) {
                                    profilePhotoImgView.setVisibility(View.GONE);
                                } else {
                                    if (bitmap == null) {
                                        Toast.makeText(MainActivity.this, "Unable to retrieve profile photo", Toast.LENGTH_SHORT).show();
                                        profilePhotoImgView.setVisibility(View.GONE);
                                    } else {
                                        profilePhotoImgView.setImageBitmap(bitmap);
                                        profilePhotoImgView.setVisibility(View.VISIBLE);
                                    }
                                }

                            }
                        });
                    }
                });
            }
        }
    }

}
