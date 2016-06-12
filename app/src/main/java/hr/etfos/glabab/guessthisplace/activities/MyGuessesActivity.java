package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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

import hr.etfos.glabab.guessthisplace.classes.ImageItem;
import hr.etfos.glabab.guessthisplace.classes.MyRecyclerViewAdapter;
import hr.etfos.glabab.guessthisplace.R;

public class MyGuessesActivity extends Activity {

    public static View.OnClickListener myOnClickListener;
    private static RecyclerView recyclerView;
    private static MyRecyclerViewAdapter myAdapter;
    private ArrayList<ImageItem> imagesList;

    TextView noImagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_guesses);

        myOnClickListener = new MyOnClickListener();
        initializeUI();

        GetImagesTask getImagesTask = new GetImagesTask();


        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");

        getImagesTask.execute(getString(R.string.domain) + "myguesses.php?request=all&user="+ username +"&cookie=" + cookie);
    }

    private void initializeUI() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_images);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        this.noImagesTextView = (TextView) findViewById(R.id.no_images_textview);
        noImagesTextView.setVisibility(TextView.GONE);
    }


    private class GetImagesTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String current = new String();
            try{
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
                String noImagesMessage = getString(R.string.no_images_guessed);
                noImagesTextView.setText(noImagesMessage);
                noImagesTextView.setVisibility(TextView.VISIBLE);
            }
            else {
                ArrayList<ImageItem> arrayList = new ArrayList<ImageItem>();
                imagesList = readJSONfromString(result);

                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), imagesList, MyGuessesActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);
            }
        }
    }

    private ArrayList<ImageItem> readJSONfromString(String jsonString) {
        ArrayList<ImageItem> imageItemList = new ArrayList<ImageItem>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("array");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                jsonObject = jsonObject2.getJSONObject("item");
                ImageItem imageItem = new ImageItem(jsonObject.getString("guesscode"), 1, 0);
                imageItemList.add(imageItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageItemList;
    }


    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String selectedImageUrl = getSelectedImageUrl(v);
            String imageCode = getSelectedImageCode(v);
            showSelectedImage(selectedImageUrl, imageCode);
        }
    }

    private String getSelectedImageUrl(View view) {
        int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
        ImageItem imageItem = imagesList.get(selectedItemPosition);
        String url = imageItem.getImageUrl();
        return url;
    }

    private String getSelectedImageCode(View view)
    {
        int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
        ImageItem imageItem = imagesList.get(selectedItemPosition);
        String imageCode = imageItem.getImageCode();
        return imageCode;
    }

    private void showSelectedImage(String imageUrl, String imageCode) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        String sourceActivity = "MyGuessesActivity";
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("imageCode", imageCode);
        intent.putExtra("sourceActivity", sourceActivity);
        startActivity(intent);
    }
}
