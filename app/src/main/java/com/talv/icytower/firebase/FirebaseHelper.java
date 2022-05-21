package com.talv.icytower.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.scoreboard.ScoreboardData;
import com.talv.icytower.scoreboard.ScoreboardResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static FirebaseAuth auth;
    private static StorageReference photosReference;
    private static DatabaseReference users;

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static DatabaseReference getUsers() {
        return users;
    }


    public interface OnScoreboardRetrieveComplete {
        void onComplete(ScoreboardResult scoreboardResult);
    }

    private static final String USERS_REFERENCE_PATH = "users";
    private static final String PROFILE_PHOTOS_REFERENCE_PATH = "profile_photos";
    private static final String PROFILE_INFO_REFERENCE_PATH = "profile_info";
    private static final String BEST_GAME_STATS_REFERENCE_PATH = "best_game_stats";


    public static void initialize() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        photosReference = storage.getReference().child(PROFILE_PHOTOS_REFERENCE_PATH);
        DatabaseReference dbRef = database.getReference();
        users = dbRef.child(USERS_REFERENCE_PATH);
    }


    public static void setProfilePhoto(String user, byte[] bitmapBytes, OnCompleteListener onCompleteListener) {
        photosReference.child(user).putBytes(bitmapBytes).addOnCompleteListener(onCompleteListener);
    }

    public static void deleteProfilePhoto(String user) {
        photosReference.child(user).delete();
    }

    public static void getProfilePhotoBytes(String user, OnCompleteListener onCompleteListener) {
        photosReference.child(user).getBytes(100L * 1000 * 1000 /*100 MB*/).addOnCompleteListener(onCompleteListener);
    }

    public static void getProfilePhotoBitmap(String user, OnProfilePhotoGetComplete onProfilePhotoGetComplete) {
        getProfilePhotoBytes(user, new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    try {
                        Bitmap bitmap = BitmapUtils.fromBytes((byte[]) task.getResult());
                        onProfilePhotoGetComplete.onComplete(bitmap);
                    } catch (Exception ignored) {
                        onProfilePhotoGetComplete.onComplete(null);
                    }
                } else {
                    onProfilePhotoGetComplete.onComplete(null);
                }
            }
        });
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static Task<Void> setBestGameStats(String user, GameStats bestGameStats) {
        return users.child(user).child(BEST_GAME_STATS_REFERENCE_PATH).setValue(bestGameStats);
    }

    public static void setBestGameStats(String user, GameStats bestGameStats, OnCompleteListener onCompleteListener) {
        setBestGameStats(user, bestGameStats).addOnCompleteListener(onCompleteListener);
    }

    public static void setBestGameStats(String user, GameStats bestGameStats, OnFailureListener onFailureListener) {
        setBestGameStats(user, bestGameStats).addOnFailureListener(onFailureListener);
    }

    public static void getBestGameStats(String user, OnCompleteListener onCompleteListener) {
        users.child(user).child(BEST_GAME_STATS_REFERENCE_PATH).get().addOnCompleteListener(onCompleteListener);
    }

    public static void getUserProfileInfo(String user, OnCompleteListener onCompleteListener) {
        users.child(user).child(PROFILE_INFO_REFERENCE_PATH).get().addOnCompleteListener(onCompleteListener);
    }

    private static Task<Void> setUserProfileInfo(String user, UserProfileInfo userProfileInfo) {
        return users.child(user).child(PROFILE_INFO_REFERENCE_PATH).setValue(userProfileInfo);
    }

    public static void setUserProfileInfo(String user, UserProfileInfo profileInfo, OnCompleteListener onCompleteListener) {
        setUserProfileInfo(user, profileInfo).addOnCompleteListener(onCompleteListener);
    }

    public static void setUserProfileInfo(String user, UserProfileInfo profileInfo, OnFailureListener onFailureListener) {
        setUserProfileInfo(user, profileInfo).addOnFailureListener(onFailureListener);
    }


    public static void getScoreboard(OnScoreboardRetrieveComplete onCompleteListener, int scoreboardPlayerCount) {
        users.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    onCompleteListener.onComplete(new ScoreboardResult(task.getException()));
                    return;
                }
                Map<String, Object> usersMap = (Map<String, Object>) task.getResult().getValue();
                if (usersMap == null) {
                    usersMap = new HashMap<>(0);
                }
                ScoreboardData[] scoreboardData = new ScoreboardData[usersMap.size()];
                int i = 0;
                for (Map.Entry<String, Object> user : usersMap.entrySet()) {
                    String username = user.getKey();
                    Map<String, Object> userData = (Map<String, Object>) user.getValue();
                    GameStats bestGameStats = new GameStats((Map<String, Object>) userData.get(BEST_GAME_STATS_REFERENCE_PATH));
                    UserProfileInfo profileInfo = new UserProfileInfo((Map<String, Object>) userData.get(PROFILE_INFO_REFERENCE_PATH));
                    scoreboardData[i] = new ScoreboardData(profileInfo, bestGameStats, username);
                    i++;
                }

                Arrays.sort(scoreboardData, new Comparator<ScoreboardData>() {
                    @Override
                    public int compare(ScoreboardData sd1, ScoreboardData sd2) {
                        return sd2.getBestGameStats().getHighscore() - sd1.getBestGameStats().getHighscore();
                    }
                });
                if (scoreboardData.length > scoreboardPlayerCount) {
                    scoreboardData = Arrays.copyOfRange(scoreboardData, 0, scoreboardPlayerCount);
                }
                onCompleteListener.onComplete(new ScoreboardResult(scoreboardData));
            }
        });
    }


}
