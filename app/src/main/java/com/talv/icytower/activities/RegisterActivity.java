package com.talv.icytower.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.GameStats;
import com.talv.icytower.firebase.UserProfileInfo;
import com.talv.icytower.game.Engine;

import static com.talv.icytower.firebase.AuthVerifier.isValidEmailAddress;
import static com.talv.icytower.firebase.AuthVerifier.isValidPassword;
import static com.talv.icytower.firebase.AuthVerifier.isValidUsername;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViewById(R.id.want_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
        final EditText emailEdt = findViewById(R.id.regEmailTxt);
        final EditText userEdt =
                findViewById(R.id.regNameTxt);
        final EditText password1Edt = findViewById(R.id.regPass1Txt);
        final EditText password2Edt = findViewById(R.id.regPass2Txt);

        findViewById(R.id.registerBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.registerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailEdt.getText().toString();
                final String user = userEdt.getText().toString();
                final String pass = password1Edt.getText().toString();
                final String pass2 = password2Edt.getText().toString();
                if (!verifyRegistrationInput(email, user, pass, pass2)) return;
                if (!FirebaseHelper.hasInternetConnection(RegisterActivity.this)) {
                    Toast.makeText(RegisterActivity.this, "No internet connection available", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(RegisterActivity.this, "Starting registration...", Toast.LENGTH_SHORT).show();
                checkUsernameExists(email, user, pass);
            }
        });
    }

    private boolean verifyRegistrationInput(String email, String user, String pass1, String pass2) {
        if (!isValidEmailAddress(email)) {
            Toast.makeText(RegisterActivity.this, "Invalid email address: " + email, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidUsername(user)) {
            Toast.makeText(RegisterActivity.this, "Invalid user name: " + user + "\n must be between 3-29 characters long, contain only English alphabets", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!pass1.equals(pass2)) {
            Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidPassword(pass1)) {
            Toast.makeText(RegisterActivity.this,
                    "Invalid password (must be 5-20 characters long, contain at least one digit, one uppercase and one lowercase letter)",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }


    private void checkUsernameExists(String email, String user, String pass) {
        FirebaseHelper.users.child(user).runTransaction(new Transaction.Handler() {
            @androidx.annotation.NonNull
            @Override
            public Transaction.Result doTransaction(@androidx.annotation.NonNull MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue("");
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean success, @Nullable DataSnapshot dataSnapshot) {
                if (success) {
                    // username saved
                    createAccount(email, user, pass);
                } else {
                    // username exists
                    Toast.makeText(RegisterActivity.this, "Username " + user + " already exists", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void createAccount(String email, String user, String pass) {
        FirebaseHelper.auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateAccountUsername(email, user, pass);
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException(), Toast.LENGTH_LONG).show();
                    createAccountFailed(email, user, pass);
                }
            }
        });
    }

    private void createAccountFailed(String email, String user, String pass) {
        // reverse checkUsernameExists
        FirebaseHelper.users.child(user).removeValue();
    }

    private void updateAccountUsername(String email, String user, String pass) {
        FirebaseHelper.auth.getCurrentUser().updateProfile(
                new UserProfileChangeRequest.Builder().setDisplayName(user).build()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateGameStats(email, user, pass);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't update username" + task.getException(), Toast.LENGTH_LONG).show();
                    updateAccountUsernameFailed(email, user, pass);
                }
            }
        });
    }

    private void updateAccountUsernameFailed(String email, String user, String pass) {
        createAccountFailed(email, user, pass);
        // reverse createAccount
        // Delete user
        FirebaseHelper.auth.getCurrentUser().delete();
    }

    private void updateGameStats(String email, String user, String pass) {
        GameStats gameStats = new GameStats();
        FirebaseHelper.setBestGameStats(user, gameStats, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    updateUserProfileInfo(email, user, pass, gameStats);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't set game stats" + task.getException(), Toast.LENGTH_LONG).show();
                    updateGameStatsFailed(email, user, pass);
                }
            }
        });

    }

    private void updateGameStatsFailed(String email, String user, String pass) {
        updateAccountUsernameFailed(email, user, pass);
        // reverse updateAccountUsername
        // no need to because the user was already deleted
    }

    private void updateUserProfileInfo(String email, String user, String pass, GameStats gameStats) {
        UserProfileInfo userProfileInfo = UserProfileInfo.createNew(this);
        FirebaseHelper.setUserProfileInfo(user, userProfileInfo, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    finishRegistration(user, gameStats, userProfileInfo);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't update user profile info" + task.getException(), Toast.LENGTH_LONG).show();
                    updateUserProfileInfoFailed(email, user, pass);
                }
            }
        });
    }

    private void updateUserProfileInfoFailed(String email, String user, String pass) {
        updateAccountUsernameFailed(email, user, pass);
        // no need to reverse updateAccountUsername or updateGameStats because the user was already deleted
    }

    private void finishRegistration(String user, GameStats gameStats, UserProfileInfo userProfileInfo) {
        Engine.user = user;
        Engine.bestGameStats = gameStats;
        Engine.userProfileInfo = userProfileInfo;
        startActivity(new Intent(RegisterActivity.this, GameActivity.class));
        finish();
    }


}
