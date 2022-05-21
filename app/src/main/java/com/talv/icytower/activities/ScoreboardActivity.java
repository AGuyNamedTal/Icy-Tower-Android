package com.talv.icytower.activities;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.firebase.OnProfilePhotoGetComplete;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.game.utils.BitmapUtils;
import com.talv.icytower.scoreboard.ScoreboardAdapter;
import com.talv.icytower.scoreboard.ScoreboardData;
import com.talv.icytower.scoreboard.ScoreboardResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScoreboardActivity extends AppCompatActivity implements ScoreboardAdapter.ItemClickListener {

    private ScoreboardAdapter adapter;
    private static final int SCOREBOARD_PLAYERS_COUNT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        RecyclerView recyclerView = findViewById(R.id.scoreboardRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreboardAdapter(this, new ScoreboardData[0]);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.scoreboardBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (!FirebaseHelper.hasInternetConnection(this)) {
            Toast.makeText(this, "No internet connection, can't view scoreboard", Toast.LENGTH_SHORT).show();
            finish();
        }
        Toast.makeText(this, "Retrieving scoreboard...", Toast.LENGTH_SHORT).show();
        FirebaseHelper.getScoreboard(new FirebaseHelper.OnScoreboardRetrieveComplete() {
            @Override
            public void onComplete(ScoreboardResult scoreboardResult) {
                if (scoreboardResult.isSuccessful()) {
                    adapter.setData(scoreboardResult.getScoreboardData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ScoreboardActivity.this, "Unable to retrieve scoreboard - " +
                            scoreboardResult.getException().getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }, SCOREBOARD_PLAYERS_COUNT);
    }

    @Override
    public void onItemClick(View view, int position) {
        ScoreboardData scoreboardData = adapter.getData()[position];
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.user_profile_popup, null);

        TextView usernameTxt = ((TextView) popupView.findViewById(R.id.up_username));
        usernameTxt.setText(scoreboardData.getUser());
        usernameTxt.setPaintFlags(usernameTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        ((TextView) popupView.findViewById(R.id.up_date)).setText(formatDate(scoreboardData.getProfileInfo().getCreationDate()));
        ((TextView) popupView.findViewById(R.id.up_gamesPlayed)).setText(String.valueOf(scoreboardData.getProfileInfo().getGamesPlayed()));
        ((TextView) popupView.findViewById(R.id.up_highscore)).setText(String.valueOf(scoreboardData.getBestGameStats().getHighscore()));
        ((TextView) popupView.findViewById(R.id.up_time)).setText(Engine.formatGameTimeToString(scoreboardData.getBestGameStats().getTimeTaken()) + " (sec)");
        ((TextView) popupView.findViewById(R.id.up_totalJumps)).setText(String.valueOf(scoreboardData.getBestGameStats().getTotalJumps()));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.findViewById(R.id.up_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        final ImageView imageView = ((ImageView) (popupView.findViewById(R.id.scoreboardProfilePhoto)));
        FirebaseHelper.getProfilePhotoBytes(scoreboardData.getUser(), new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    try {
                        Bitmap bitmap = BitmapUtils.fromBytes((byte[]) task.getResult());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (Exception ignored) {
                        Toast.makeText(ScoreboardActivity.this, "Unable to retrieve profile photo", Toast.LENGTH_SHORT).show();
                        imageView.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(ScoreboardActivity.this, "Unable to retrieve profile photo", Toast.LENGTH_SHORT).show();
                    imageView.setVisibility(View.GONE);
                }
            }
        });
        FirebaseHelper.getProfilePhotoBitmap(scoreboardData.getUser(), new OnProfilePhotoGetComplete() {
            @Override
            public void onComplete(Bitmap bitmap) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bitmap == null) {
                            Toast.makeText(ScoreboardActivity.this, "Unable to retrieve profile photo", Toast.LENGTH_SHORT).show();
                            imageView.setVisibility(View.GONE);
                        } else {
                            imageView.setImageBitmap(bitmap);
                            imageView.setVisibility(View.VISIBLE);
                        }

                    }
                });

            }
        });
    }

    private String formatDate(long unixTime) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(unixTime * 1000));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
