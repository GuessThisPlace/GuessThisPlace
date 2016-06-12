package hr.etfos.glabab.guessthisplace.classes;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import hr.etfos.glabab.guessthisplace.R;

public class HighScoreAdapter extends BaseAdapter{
    Context ctx;
    ArrayList<HighScore> highScoreList;
    String currentUser;

    public HighScoreAdapter(Context ctx, ArrayList<HighScore> highScoreList, String currentUser) {
        this.ctx = ctx;
        this.highScoreList = highScoreList;
        this.currentUser = currentUser;
    }

    @Override
    public int getCount() {
        return this.highScoreList.size();
    }

    @Override
    public Object getItem(int position) {
        return highScoreList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = View.inflate(ctx, R.layout.single_high_score, null);
        }
        int colorGold = ContextCompat.getColor(ctx, R.color.gold);
        int transparent = ContextCompat.getColor(ctx, R.color.transparent);
        int fullTransparent = ContextCompat.getColor(ctx, R.color.full_transparent);
        int highlightHS = ContextCompat.getColor(ctx, R.color.highlight_high_score);
        HighScore current = highScoreList.get(position);

        TextView twPosition = (TextView) convertView.findViewById(R.id.textView_position);
        TextView twUser = (TextView) convertView.findViewById(R.id.textView_user);
        TextView twScore = (TextView) convertView.findViewById(R.id.textView_score);
        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.high_score_single);

        twPosition.setText(current.getPosition());
        twUser.setText(current.getUsername());

        if (position % 2 == 1) {
            layout.setBackgroundColor(fullTransparent);
        } else {
            layout.setBackgroundColor(transparent);
        }



        if(!current.getNumberOfGuesses().equals("-500")) {
            twScore.setText(current.getScore() + " (" + current.getNumberOfGuesses() + ")");
        }
        else {
            twScore.setText(current.getScore());
        }

        if(currentUser.equals(current.getUsername()))
        {
            twPosition.setTextColor(colorGold);
            twScore.setTextColor(colorGold);
            twUser.setTextColor(colorGold);
            layout.setBackgroundColor(highlightHS);
        }
        else {
            twPosition.setTextColor(Color.WHITE);
            twScore.setTextColor(Color.WHITE);
            twUser.setTextColor(Color.WHITE);
        }

        return convertView;
    }
}
