package com.talv.icytower.firebase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.talv.icytower.scoreboard.ScoreboardData;
import com.talv.icytower.scoreboard.ScoreboardResult;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class FirebaseHelper {
    public static FirebaseAuth auth;
    public static FirebaseDatabase database;
    public static DatabaseReference dbRef;
    public static DatabaseReference users;

    private static final String USERS_REFERENCE_PATH = "users";
    private static final String PROFILE_INFO_REFERENCE_PATH = "profile_info";
    private static final String BEST_GAME_STATS_REFERENCE_PATH = "best_game_stats";


    public static void initialize() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        users = dbRef.child(USERS_REFERENCE_PATH);
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

    public static void getScoreboard(OnScoreboardRetrieveComplete onCompleteListener) {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> usersMap = (Map<String, Object>) snapshot.getValue();
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
                        return sd2.bestGameStats.highscore - sd1.bestGameStats.highscore;
                    }
                });
                onCompleteListener.onComplete(new ScoreboardResult(scoreboardData));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onCompleteListener.onComplete(new ScoreboardResult(error.toException()));
            }
        });
    }


    public interface OnScoreboardRetrieveComplete {
        void onComplete(ScoreboardResult scoreboardResult);
    }

}
