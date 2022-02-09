package com.talv.icytower.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;
import com.talv.icytower.game.Engine;

import static com.talv.icytower.firebase.AuthVerifier.isValidEmailAddress;
import static com.talv.icytower.firebase.AuthVerifier.isValidPassword;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.want_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        final EditText emailEdt = findViewById(R.id.loginEmalTxt);
        final EditText passwordEdt = findViewById(R.id.loginPassTxt);
        findViewById(R.id.loginBackBtn).setOnClickListener(view -> finish());
        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEdt.getText().toString();
                String pass = passwordEdt.getText().toString();
                if (!verifyLoginInput(email, pass)) return;
                if (!FirebaseHelper.hasInternetConnection(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "No internet connection available", Toast.LENGTH_LONG).show();
                    return;
                }
                FirebaseHelper.auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Engine.user = FirebaseHelper.auth.getCurrentUser().getDisplayName();
                                    loginWithUser(LoginActivity.this);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }


                            }
                        });
            }
        });
    }

    private boolean verifyLoginInput(String email, String password) {
        if (!isValidEmailAddress(email)) {
            Toast.makeText(LoginActivity.this, "Invalid email address: " + email, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(LoginActivity.this,
                    "Invalid password (must be 5-20 characters long, contain at least one digit, one uppercase and one lowercase letter)",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static void loginWithUser(Activity context) {
        FirebaseHelper.getBestGameStats(Engine.user, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(context, "Unable to retrieve best game stats (highscore), feature will be disabled - "
                                    + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Engine.bestGameStats = null;
                } else {
                    Engine.bestGameStats = ((DataSnapshot) task.getResult()).getValue(GameStats.class);
                }
                FirebaseHelper.getUserProfileInfo(Engine.user, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Unable to retrieve user profile info, feature will be disabled - "
                                            + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            Engine.userProfileInfo = null;
                        } else {
                            Engine.userProfileInfo = ((DataSnapshot) task.getResult()).getValue(UserProfileInfo.class);
                        }
                        context.startActivity(new Intent(context, GameActivity.class));
                        context.finish();
                    }
                });
            }
        });
    }
}
