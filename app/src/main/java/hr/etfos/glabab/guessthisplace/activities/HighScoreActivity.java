package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hr.etfos.glabab.guessthisplace.classes.HighScore;
import hr.etfos.glabab.guessthisplace.classes.HighScoreAdapter;
import hr.etfos.glabab.guessthisplace.R;

public class HighScoreActivity extends Activity {

    ArrayList<HighScore> highScoreList = new ArrayList<HighScore>();
    ListView listViewHS;
    HighScoreAdapter adapter;
    TextView noHighScoreTextView;
    String username = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);
        listViewHS = (ListView) findViewById(R.id.high_score_list_view);

        this.noHighScoreTextView = (TextView) findViewById(R.id.high_score_status);
        noHighScoreTextView.setVisibility(TextView.GONE);

        GetHighScoreTask getHighScoreTask = new GetHighScoreTask();

        SharedPreferences prefs = getSharedPreferences("user", 0);

        username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");

        getHighScoreTask.execute(getString(R.string.domain) + "highscore.php?user="+ username +"&cookie=" + cookie);
    }

    private class GetHighScoreTask extends AsyncTask
            <String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String current = new String();
            try {
                URL myUrl = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream in = connection.getInputStream();
                StringBuilder sBuilder = new StringBuilder();
                BufferedReader bReader = new BufferedReader
                        (new InputStreamReader(in));
                String line;
                while((line = bReader.readLine()) != null){
                    sBuilder.append(line);
                }
                current = sBuilder.toString();

                return current;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return current;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.contains(getString(R.string.invalid_cookie))) {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.clear();
                prefs.commit();
                finish();
            }
            else if(result.contains(getString(R.string.empty))){
                String noHighScoreMessage = getString(R.string.no_high_score);
                noHighScoreTextView.setText(noHighScoreMessage);
                noHighScoreTextView.setVisibility(TextView.VISIBLE);
            }
            else {
                highScoreList = readJSONfromString(result);

                adapter = new HighScoreAdapter(getApplicationContext(), highScoreList, username);
                listViewHS.setAdapter(adapter);
            }
        }
    }

    private ArrayList<HighScore> readJSONfromString(String jsonString) {
        ArrayList<HighScore> arrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("array");
            if(jsonArray.length() != 0)
            {
                HighScore highScore = new HighScore("Username", "Score (Guesses)", "-500", "Rank");
                arrayList.add(highScore);
            }
            for(int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String position = jsonObject.getString("no");
                jsonObject = jsonObject.getJSONObject("item");
                HighScore highScore = new HighScore(jsonObject.getString("user"), jsonObject.getString("score"), jsonObject.getString("numberofguesses"), position);
                arrayList.add(highScore);
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
}
