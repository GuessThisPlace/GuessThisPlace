package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

public class GuessPickerActivity extends Activity {

    public static View.OnClickListener myOnClickListener;
    private static RecyclerView recyclerView;
    private static MyRecyclerViewAdapter myAdapter;
    private ArrayList<ImageItem> newList, oldList, popularList;
    Button newButton, oldButton, popularButton;
    private Integer buttonId;
    TextView noImagesTextView;
    int primaryColor;
    int secondaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_picker);


        myOnClickListener = new MyOnClickListener();
        initializeUI(savedInstanceState);

        GetImagesTask getImagesTask = new GetImagesTask();

        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");

        getImagesTask.execute(getString(R.string.domain) + "listguesses.php?user="+ username +"&cookie=" + cookie);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) finish();
    }

    private void initializeUI(Bundle savedInstanceState){

        primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.primaryBackground);
        secondaryColor = ContextCompat.getColor(getApplicationContext(), R.color.secondaryBackground);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_images);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        this.noImagesTextView = (TextView) findViewById(R.id.no_images_textview);
        noImagesTextView.setVisibility(TextView.GONE);

        newButton = (Button) findViewById(R.id.new_button);
        oldButton = (Button) findViewById(R.id.old_button);
        popularButton = (Button) findViewById(R.id.popular_button);

        if (savedInstanceState != null) {
            buttonId = savedInstanceState.getInt("activeButton");

        }
        else {
            SharedPreferences prefs = getSharedPreferences("guessPicker", 0);
            buttonId = prefs.getInt("buttonId", 1);
        }

        if(buttonId == 1) {
            newButton.setBackgroundColor(primaryColor);
            oldButton.setBackgroundColor(secondaryColor);
            popularButton.setBackgroundColor(secondaryColor);
        }
        else if(buttonId == -1) {
            newButton.setBackgroundColor(secondaryColor);
            oldButton.setBackgroundColor(secondaryColor);
            popularButton.setBackgroundColor(primaryColor);
        }
        else {
            newButton.setBackgroundColor(secondaryColor);
            oldButton.setBackgroundColor(primaryColor);
            popularButton.setBackgroundColor(secondaryColor);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("activeButton", buttonId);
        super.onSaveInstanceState(savedInstanceState);
    }

    private class GetImagesTask extends AsyncTask
    <String, String, String>{
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
            newList = new ArrayList<>();
            oldList = new ArrayList<>();
            popularList = new ArrayList<>();

            if(result.contains(getString(R.string.invalid_cookie))) {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.clear();
                prefs.commit();
                finish();
            }
            else if(result.contains(getString(R.string.empty))){
                String noImagesMessage = getString(R.string.no_images_to_guess);
                noImagesTextView.setText(noImagesMessage);
                noImagesTextView.setVisibility(TextView.VISIBLE);
            }
            else {
                newList = readJSONfromString(result, "new");
                oldList = readJSONfromString(result, "old");
                popularList = readJSONfromString(result, "popular");
            }

            setButtons();
        }
    }

    private ArrayList<ImageItem> readJSONfromString(String jsonString, String listType) {
        ArrayList<ImageItem> imageItemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("array");
            for(int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.has(listType)) {
                    jsonObject = jsonObject.getJSONObject(listType);
                    ImageItem imageItem = new ImageItem(jsonObject.getString("imagecode"), jsonObject.getInt("approved"), jsonObject.getInt("denyreason"));
                    imageItemList.add(imageItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageItemList;
    }

    private void setButtons() {
        if(buttonId == 1) {
            newButton.setBackgroundColor(primaryColor);
            oldButton.setBackgroundColor(secondaryColor);
            popularButton.setBackgroundColor(secondaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), newList, GuessPickerActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);

        }
        else if(buttonId == 0) {
            newButton.setBackgroundColor(secondaryColor);
            oldButton.setBackgroundColor(primaryColor);
            popularButton.setBackgroundColor(secondaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), oldList, GuessPickerActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);
        }
        else {
            newButton.setBackgroundColor(secondaryColor);
            oldButton.setBackgroundColor(secondaryColor);
            popularButton.setBackgroundColor(primaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), popularList, GuessPickerActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);
        }

        newButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = 1;
                    oldButton.setEnabled(false);
                    popularButton.setEnabled(false);
                    newButton.setBackgroundColor(primaryColor);
                    oldButton.setBackgroundColor(secondaryColor);
                    popularButton.setBackgroundColor(secondaryColor);
                    SharedPreferences.Editor prefs = getSharedPreferences("guessPicker", 0).edit();
                    prefs.putInt("buttonId", buttonId);
                    prefs.commit();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    oldButton.setEnabled(true);
                    popularButton.setEnabled(true);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), newList, GuessPickerActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);
                return true;
            }
        });

        oldButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = 0;
                    newButton.setEnabled(false);
                    popularButton.setEnabled(false);
                    newButton.setBackgroundColor(secondaryColor);
                    popularButton.setBackgroundColor(secondaryColor);
                    oldButton.setBackgroundColor(primaryColor);
                    SharedPreferences.Editor prefs = getSharedPreferences("guessPicker", 0).edit();
                    prefs.putInt("buttonId", buttonId);
                    prefs.commit();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    newButton.setEnabled(true);
                    popularButton.setEnabled(true);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), oldList, GuessPickerActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);

                return true;
            }
        });

        popularButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = -1;
                    newButton.setEnabled(false);
                    oldButton.setEnabled(false);
                    newButton.setBackgroundColor(secondaryColor);
                    popularButton.setBackgroundColor(primaryColor);
                    oldButton.setBackgroundColor(secondaryColor);
                    SharedPreferences.Editor prefs = getSharedPreferences("guessPicker", 0).edit();
                    prefs.putInt("buttonId", buttonId);
                    prefs.commit();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    newButton.setEnabled(true);
                    oldButton.setEnabled(true);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), popularList, GuessPickerActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);
                return true;
            }
        });
    }


    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ImageItem selectedImage = getSelectedImage(v);
            showSelectedImage(selectedImage.getImageUrl(), selectedImage.getImageCode());
        }
    }

    private ImageItem getSelectedImage(View view) {
        if(buttonId == 1) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = newList.get(selectedItemPosition);
            return imageItem;
        }
        else if(buttonId == -1) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = popularList.get(selectedItemPosition);
            return imageItem;
        }
        else {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = oldList.get(selectedItemPosition);
            return imageItem;
        }
    }

    private void showSelectedImage(String imageUrl, String imageCode) {
        Intent intent = new Intent(this, SingleGuessActivity.class);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("imageCode", imageCode);
        startActivity(intent);
    }
}
