package com.talv.icytower.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.utils.BitmapUtils;

import static com.talv.icytower.firebase.AuthVerifier.isValidEmailAddress;
import static com.talv.icytower.firebase.AuthVerifier.isValidPassword;
import static com.talv.icytower.firebase.AuthVerifier.isValidUsername;

public class RegisterActivity extends AppCompatActivity {

    private class RegistrationInfo {
        private String email;
        private String user;
        private String pass;
        private Bitmap profilePhoto;

        public RegistrationInfo(String email, String user, String pass, Bitmap profilePhoto) {
            this.email = email;
            this.user = user;
            this.pass = pass;
            this.profilePhoto = profilePhoto;
        }

        public String getEmail() {
            return email;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }

        public Bitmap getProfilePhoto() {
            return profilePhoto;
        }
    }

    private static final int CAM_REQUEST_CODE = 100;
    private static final int CAM_PERMISSION_REQUEST_CODE = 200;
    private ImageView profilePhotoImgView;
    private Bitmap profilePhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        profilePhotoImgView = findViewById(R.id.scoreboardProfilePhoto);
        findViewById(R.id.want_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean singleplayer = getIntent().getBooleanExtra(GameActivity.SINGLEPLAYER_KEY, true);
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class).putExtra(GameActivity.SINGLEPLAYER_KEY, singleplayer));
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
                RegistrationInfo regInfo = new RegistrationInfo(email, user, pass, profilePhoto);
                checkUsernameExists(regInfo);
            }
        });
        findViewById(R.id.chooseProfilePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, CAM_PERMISSION_REQUEST_CODE);
                } else {
                    startCamera();
                }
            }
        });
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAM_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != CAM_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            return;
        }
        startCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,
                resultCode,
                data);

        if (requestCode == CAM_REQUEST_CODE && resultCode == RESULT_OK) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            profilePhoto = photo;
            profilePhotoImgView.setImageBitmap(photo);
        }
    }

    private boolean verifyRegistrationInput(String email, String user, String pass1, String pass2) {
        if (!isValidEmailAddress(email)) {
            Toast.makeText(RegisterActivity.this, "Invalid email address: " + email, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidUsername(user)) {
            Toast.makeText(RegisterActivity.this, "Invalid user name: " + user + "\n must be between 4-29 characters long, contain only English alphabets", Toast.LENGTH_LONG).show();
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
        if (profilePhoto == null) {
            Toast.makeText(RegisterActivity.this,
                    "You must choose a profile photo",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private void checkUsernameExists(RegistrationInfo regInfo) {
        FirebaseHelper.users.child(regInfo.getUser()).runTransaction(new Transaction.Handler() {
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
                    createAccount(regInfo);
                } else {
                    // username exists
                    Toast.makeText(RegisterActivity.this, "Username " + regInfo.getUser() + " already exists", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void createAccount(RegistrationInfo regInfo) {
        FirebaseHelper.auth.createUserWithEmailAndPassword(regInfo.email, regInfo.pass).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    updateAccountUsername(regInfo);
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException(), Toast.LENGTH_LONG).show();
                    createAccountFailed(regInfo);
                }
            }
        });
    }

    private void createAccountFailed(RegistrationInfo regInfo) {
        // reverse checkUsernameExists
        FirebaseHelper.users.child(regInfo.getUser()).removeValue();
    }

    private void updateAccountUsername(RegistrationInfo regInfo) {
        FirebaseHelper.auth.getCurrentUser().updateProfile(
                new UserProfileChangeRequest.Builder().setDisplayName(regInfo.getUser()).build()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateGameStats(regInfo);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't update username" + task.getException(), Toast.LENGTH_LONG).show();
                    updateAccountUsernameFailed(regInfo);
                }
            }
        });
    }

    private void updateAccountUsernameFailed(RegistrationInfo regInfo) {
        createAccountFailed(regInfo);
        // reverse createAccount
        // Delete user
        FirebaseHelper.auth.getCurrentUser().delete();
    }

    private void updateGameStats(RegistrationInfo regInfo) {
        GameStats gameStats = new GameStats();
        FirebaseHelper.setBestGameStats(regInfo.getUser(), gameStats, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    updateUserProfileInfo(regInfo, gameStats);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't set game stats" + task.getException(), Toast.LENGTH_LONG).show();
                    updateGameStatsFailed(regInfo);
                }
            }
        });

    }

    private void updateGameStatsFailed(RegistrationInfo regInfo) {
        updateAccountUsernameFailed(regInfo);
        // reverse updateAccountUsername
        // no need to because the user was already deleted
    }

    private void updateUserProfileInfo(RegistrationInfo regInfo, GameStats gameStats) {
        UserProfileInfo userProfileInfo = UserProfileInfo.createNew(this);
        FirebaseHelper.setUserProfileInfo(regInfo.getUser(), userProfileInfo, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    uploadProfilePhoto(regInfo, gameStats, userProfileInfo);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't update user profile info" + task.getException(), Toast.LENGTH_LONG).show();
                    updateUserProfileInfoFailed(regInfo);
                }
            }
        });
    }

    private void updateUserProfileInfoFailed(RegistrationInfo regInfo) {
        updateAccountUsernameFailed(regInfo);
        // no need to reverse updateAccountUsername or updateGameStats because the user was already deleted
    }

    private void uploadProfilePhoto(RegistrationInfo regInfo, GameStats gameStats, UserProfileInfo userProfileInfo) {
        FirebaseHelper.setProfilePhoto(regInfo.getUser(), BitmapUtils.toBytes(regInfo.profilePhoto), new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    finishRegistration(regInfo, gameStats, userProfileInfo);
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't update user profile info" + task.getException(), Toast.LENGTH_LONG).show();
                    uploadUserProfilePhotoFailed(regInfo);
                }
            }
        });
    }

    private void uploadUserProfilePhotoFailed(RegistrationInfo regInfo) {
        FirebaseHelper.deleteProfilePhoto(regInfo.user);
        updateUserProfileInfoFailed(regInfo);
    }


    private void finishRegistration(RegistrationInfo regInfo, GameStats gameStats, UserProfileInfo userProfileInfo) {
        Engine.user = regInfo.getUser();
        Engine.bestGameStats = gameStats;
        Engine.userProfileInfo = userProfileInfo;
        boolean singleplayer = getIntent().getBooleanExtra(GameActivity.SINGLEPLAYER_KEY, true);
        startActivity(new Intent(RegisterActivity.this, GameActivity.class).putExtra(GameActivity.SINGLEPLAYER_KEY, singleplayer));
        finish();
    }


}
