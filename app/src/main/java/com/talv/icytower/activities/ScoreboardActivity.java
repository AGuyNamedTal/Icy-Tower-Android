package com.talv.icytower.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.game.engine.Engine;
import com.talv.icytower.scoreboard.ScoreboardAdapter;
import com.talv.icytower.scoreboard.ScoreboardData;
import com.talv.icytower.scoreboard.ScoreboardResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScoreboardActivity extends AppCompatActivity implements ScoreboardAdapter.ItemClickListener {

    ScoreboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        RecyclerView recyclerView = findViewById(R.id.scoreboardRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScoreboardAdapter(this, new ScoreboardData[0]);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        FirebaseHelper.getScoreboard(new FirebaseHelper.OnScoreboardRetrieveComplete() {
            @Override
            public void onComplete(ScoreboardResult scoreboardResult) {
                if (scoreboardResult.isSuccess()) {
                    adapter.data = scoreboardResult.scoreboardData;
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ScoreboardActivity.this, "Unable to retrieve scoreboard - " +
                            scoreboardResult.exception.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
        findViewById(R.id.scoreboardBackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        ScoreboardData scoreboardData = adapter.data[position];
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.user_profile_popup, null);

        TextView usernameTxt = ((TextView) popupView.findViewById(R.id.up_username));
        usernameTxt.setText(scoreboardData.user);
        usernameTxt.setPaintFlags(usernameTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        ((TextView) popupView.findViewById(R.id.up_date)).setText(formatDate(scoreboardData.profileInfo.creationDate));
        ((TextView) popupView.findViewById(R.id.up_gamesPlayed)).setText(String.valueOf(scoreboardData.profileInfo.gamesPlayed));
        ((TextView) popupView.findViewById(R.id.up_highscore)).setText(String.valueOf(scoreboardData.bestGameStats.highscore));
        ((TextView) popupView.findViewById(R.id.up_time)).setText(Engine.formatGameTimeToString(scoreboardData.bestGameStats.timeTaken));
        ((TextView) popupView.findViewById(R.id.up_totalJumps)).setText(String.valueOf(scoreboardData.bestGameStats.totalJumps));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.findViewById(R.id.up_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
    }

    private String formatDate(long unixTime) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(unixTime * 1000));
    }

}
