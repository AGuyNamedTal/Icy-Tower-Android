package com.talv.icytower.scoreboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.talv.icytower.R;


public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ViewHolder> {

    public ScoreboardData[] data;
    private final LayoutInflater inflater;
    private ItemClickListener onClickListener;

    // data is passed into the constructor
    public ScoreboardAdapter(Context context, ScoreboardData[] data) {
        this.inflater = LayoutInflater.from(context);
        this.data = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.scoreboard_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScoreboardData data = this.data[position];
        holder.countryTxt.setText(countryCodeToEmoji(data.profileInfo.countryCode));
        holder.scoreTxt.setText(String.valueOf(data.bestGameStats.highscore));
        holder.userTxt.setText(data.user);
    }

    private String countryCodeToEmoji(String countryCode) {
        if (countryCode.length() != 2) {
            return countryCode;
        }
        String countryCodeCaps = countryCode.toUpperCase(); // upper case is important because we are calculating offset
        int firstLetter = Character.codePointAt(countryCodeCaps, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCodeCaps, 1) - 0x41 + 0x1F1E6;

        // 2. It then checks if both characters are alphabet
        if (!Character.isLetter(countryCodeCaps.charAt(0)) || !Character.isLetter(countryCodeCaps.charAt(1))) {
            return countryCode;
        }

        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return data.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView countryTxt;
        private final TextView userTxt;
        private final TextView scoreTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            countryTxt = itemView.findViewById(R.id.scb_country);
            userTxt = itemView.findViewById(R.id.scb_user);
            scoreTxt = itemView.findViewById(R.id.scb_highscore);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onClickListener != null) onClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public ScoreboardData getItem(int id) {
        return data[id];
    }



    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.onClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
