package com.talv.icytower.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.talv.icytower.R;
import com.talv.icytower.firebase.FirebaseHelper;
import com.talv.icytower.scoreboard.ScoreboardAdapter;
import com.talv.icytower.scoreboard.ScoreboardData;
import com.talv.icytower.scoreboard.ScoreboardResult;

import java.util.ArrayList;

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
                            scoreboardResult.exception.getMessage(), Toast.LENGTH_LONG);
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {


    }
}
