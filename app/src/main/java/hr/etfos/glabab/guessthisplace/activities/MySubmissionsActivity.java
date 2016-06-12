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
import android.widget.Toast;

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

public class MySubmissionsActivity extends Activity {

    public static View.OnClickListener myOnClickListener;
    private static RecyclerView recyclerView;
    private static MyRecyclerViewAdapter myAdapter;
    private ArrayList<ImageItem> imagesList, approvedList, pendingList, deniedList;
    Button buttonApproved, buttonPending, buttonDenied;
    private Integer buttonId;
    int primaryColor, secondaryColor;
    int messageShown = 0;
    TextView noImagesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_submissions);

        myOnClickListener = new MyOnClickListener();
        initializeUI(savedInstanceState);

        GetImagesTask getImagesTask = new GetImagesTask();


        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");

        getImagesTask.execute(getString(R.string.domain) + "getsubmissions.php?user="+ username +"&cookie=" + cookie);

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

        buttonApproved = (Button) findViewById(R.id.approved_button);
        buttonPending = (Button) findViewById(R.id.pending_button);
        buttonDenied = (Button) findViewById(R.id.denied_button);

        if (savedInstanceState != null) {
            buttonId = savedInstanceState.getInt("activeButton");
            messageShown = savedInstanceState.getInt("messageShown");
            noImagesTextView.setText(savedInstanceState.getCharSequence("textViewText"));
            noImagesTextView.setVisibility(savedInstanceState.getInt("textViewVisibility"));
        }
        else {
            buttonId = 1;
        }

        if(buttonId == 1) {
            buttonApproved.setBackgroundColor(primaryColor);
            buttonDenied.setBackgroundColor(secondaryColor);
            buttonPending.setBackgroundColor(secondaryColor);
        }
        else if(buttonId == -1) {
            buttonApproved.setBackgroundColor(secondaryColor);
            buttonPending.setBackgroundColor(secondaryColor);
            buttonDenied.setBackgroundColor(primaryColor);
        }
        else {
            buttonApproved.setBackgroundColor(secondaryColor);
            buttonPending.setBackgroundColor(primaryColor);
            buttonDenied.setBackgroundColor(secondaryColor);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("activeButton", buttonId);
        savedInstanceState.putCharSequence("textViewText", noImagesTextView.getText());
        savedInstanceState.putInt("textViewVisibility", noImagesTextView.getVisibility());
        savedInstanceState.putInt("messageShown", messageShown);
        super.onSaveInstanceState(savedInstanceState);
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
            approvedList = new ArrayList<>();
            deniedList = new ArrayList<>();
            pendingList = new ArrayList<>();
            imagesList = new ArrayList<>();

            if(result.contains(getString(R.string.invalid_cookie))) {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.clear();
                prefs.commit();
                finish();
            }
            else if(result.contains(getString(R.string.empty))){
                if(messageShown == 0) {
                    noImagesTextView.setText(getString(R.string.no_images_submitted));
                    noImagesTextView.setVisibility(TextView.VISIBLE);
                    messageShown = 1;
                }
            }
            else {
                imagesList = readJSONfromString(result);

                for(int i = 0; i < imagesList.size(); i++)
                {
                    Integer imageState = imagesList.get(i).getApproved();

                    if(imageState == 1) {
                        approvedList.add(imagesList.get(i));
                    }
                    else if(imageState == 0) {
                        pendingList.add(imagesList.get(i));
                    }
                    else {
                        deniedList.add(imagesList.get(i));
                    }
                }
            }

            setButtons();
        }
    }

    private void setButtons() {

        if(buttonId == 1) {
            buttonApproved.setBackgroundColor(primaryColor);
            buttonPending.setBackgroundColor(secondaryColor);
            buttonDenied.setBackgroundColor(secondaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), approvedList, MySubmissionsActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);
        }
        else if(buttonId == 0) {
            buttonDenied.setBackgroundColor(secondaryColor);
            buttonPending.setBackgroundColor(primaryColor);
            buttonApproved.setBackgroundColor(secondaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), pendingList, MySubmissionsActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);
        }
        else {
            buttonPending.setBackgroundColor(secondaryColor);
            buttonApproved.setBackgroundColor(secondaryColor);
            buttonDenied.setBackgroundColor(primaryColor);
            myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), deniedList, MySubmissionsActivity.myOnClickListener);
            recyclerView.setAdapter(myAdapter);
        }

        buttonApproved.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = 1;
                    buttonPending.setEnabled(false);
                    buttonDenied.setEnabled(false);
                    buttonApproved.setBackgroundColor(primaryColor);
                    buttonDenied.setBackgroundColor(secondaryColor);
                    buttonPending.setBackgroundColor(secondaryColor);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPending.setEnabled(true);
                    buttonDenied.setEnabled(true);
                }
                if(approvedList.isEmpty())
                {
                    noImagesTextView.setText(getString(R.string.no_approved_images));
                    noImagesTextView.setVisibility(TextView.VISIBLE);
                    messageShown = 1;
                }
                else
                {
                    noImagesTextView.setVisibility(TextView.GONE);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), approvedList, MySubmissionsActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);
                return true;
            }
        });

        buttonPending.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = 0;
                    buttonApproved.setEnabled(false);
                    buttonDenied.setEnabled(false);
                    buttonApproved.setBackgroundColor(secondaryColor);
                    buttonDenied.setBackgroundColor(secondaryColor);
                    buttonPending.setBackgroundColor(primaryColor);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    buttonApproved.setEnabled(true);
                    buttonDenied.setEnabled(true);
                }
                if(pendingList.isEmpty())
                {
                    noImagesTextView.setText(getString(R.string.no_pending_images));
                    noImagesTextView.setVisibility(TextView.VISIBLE);
                }
                else
                {
                    noImagesTextView.setVisibility(TextView.GONE);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), pendingList, MySubmissionsActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);

                return true;
            }
        });

        buttonDenied.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonId = -1;
                    buttonPending.setEnabled(false);
                    buttonApproved.setEnabled(false);
                    buttonApproved.setBackgroundColor(secondaryColor);
                    buttonDenied.setBackgroundColor(primaryColor);
                    buttonPending.setBackgroundColor(secondaryColor);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPending.setEnabled(true);
                    buttonApproved.setEnabled(true);
                }
                if(deniedList.size() == 0)
                {
                    noImagesTextView.setText(getString(R.string.no_denied_images));
                    noImagesTextView.setVisibility(TextView.VISIBLE);
                }
                else
                {
                    noImagesTextView.setVisibility(TextView.GONE);
                }
                myAdapter = new MyRecyclerViewAdapter(getApplicationContext(), deniedList, MySubmissionsActivity.myOnClickListener);
                recyclerView.setAdapter(myAdapter);
                return true;
            }
        });

    }

    private ArrayList<ImageItem> readJSONfromString(String jsonString) {
        ArrayList<ImageItem> imageItemList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("array");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                jsonObject = jsonObject2.getJSONObject("item");
                ImageItem imageItem = new ImageItem(jsonObject.getString("imagecode"), jsonObject.getInt("approved"), jsonObject.getInt("denyreason"));
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
            ImageItem imageItem = getSelectedImage(v);
            if(imageItem.getApproved() == 1) {
                showSelectedImage(imageItem.getImageUrl(), imageItem.getImageCode());
            }
            else if(imageItem.getApproved() == -1)
            {
                if(imageItem.getDenyReason() == 1 ) {
                    Toast.makeText(getApplicationContext(), getString(R.string.denied_bad_image), Toast.LENGTH_LONG).show();
                }
                else if(imageItem.getDenyReason() == 2) {
                    Toast.makeText(getApplicationContext(), getString(R.string.denied_unrecognizable), Toast.LENGTH_LONG).show();
                }

                else if(imageItem.getDenyReason() == 3){
                    Toast.makeText(getApplicationContext(), getString(R.string.denied_similar_image_exists), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.location_mismatch, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private ImageItem getSelectedImage(View view) {
        if(buttonId == 1) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = approvedList.get(selectedItemPosition);
            return imageItem;
        }
        else if(buttonId == -1) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = deniedList.get(selectedItemPosition);
            return imageItem;
        }
        else {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(view);
            ImageItem imageItem = pendingList.get(selectedItemPosition);
            return imageItem;
        }
    }

    private void showSelectedImage(String imageUrl, String imageCode) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        String sourceActivity = "MySubmissionsActivity";
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("imageCode", imageCode);
        intent.putExtra("sourceActivity", sourceActivity);
        startActivity(intent);
    }
}
