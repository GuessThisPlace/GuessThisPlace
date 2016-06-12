package hr.etfos.glabab.guessthisplace.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hr.etfos.glabab.guessthisplace.classes.ImageDetails;
import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.classes.WorkaroundMapFragment;

@SuppressWarnings("ResourceType")
public class SingleGuessActivity extends FragmentActivity implements OnMapReadyCallback{

    Button takeGuessButton;
    TextView postGuessTextView;
    ImageView imageView;
    GoogleMap googleMap;
    WorkaroundMapFragment mapFragment;
    ScrollView mScrollView;
    LatLng guessLatLng;
    ProgressBar progressBar;
    String url;
    LatLng correctLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_guess);
        correctLocation = null;
        guessLatLng = null;
        initializeUI();

        if (savedInstanceState != null)
        {
            takeGuessButton.setVisibility(savedInstanceState.getInt("guessButtonVisibility"));
            progressBar.setVisibility(savedInstanceState.getInt("progressBarVisibility"));
            guessLatLng = savedInstanceState.getParcelable("guessLatLng");
            postGuessTextView.setVisibility(savedInstanceState.getInt("postGuessTextViewVisibility"));
            postGuessTextView.setText(savedInstanceState.getCharSequence("guessText"));
            correctLocation = savedInstanceState.getParcelable("correctLocation");
        }


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, GuessPickerActivity.class));
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("guessLatLng", guessLatLng);
        savedInstanceState.putInt("guessButtonVisibility", takeGuessButton.getVisibility());
        savedInstanceState.putInt("progressBarVisibility", progressBar.getVisibility());
        savedInstanceState.putInt("postGuessTextViewVisibility", postGuessTextView.getVisibility());
        savedInstanceState.putCharSequence("guessText", postGuessTextView.getText().toString());
        savedInstanceState.putParcelable("correctLocation", correctLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void initializeUI() {

        progressBar = (ProgressBar) findViewById(R.id.guess_photo_progress_bar);

        mScrollView = (ScrollView) findViewById(R.id.sv_single_guess);
        mapFragment = ((WorkaroundMapFragment) getFragmentManager().findFragmentById(R.id.map));

        if(mScrollView != null) {
            mapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        mapFragment.getMapAsync(this);

        this.takeGuessButton = (Button) findViewById(R.id.button_guess);
        takeGuessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitGuess();
            }
        });
        takeGuessButton.setVisibility(View.GONE);

        this.postGuessTextView = (TextView) findViewById(R.id.post_guess_textview);
        postGuessTextView.setVisibility(TextView.GONE);

        imageView = (ImageView) findViewById(R.id.guess_image);
        url = getIntent().getStringExtra("imageUrl");

        if(url == null)
        {
            Picasso.with(SingleGuessActivity.this)
                    .load(url)
                    .error(R.drawable.error)
                    .into(imageView);
        }
        else {
            Picasso.with(this)
                    .load(url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                            takeGuessButton.setVisibility(View.VISIBLE);

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent ViewPhotoAct = new Intent(
                                            getBaseContext(), ViewPhotoActivity.class
                                    );
                                    ViewPhotoAct.putExtra("fileUrl", url);
                                    startActivity(ViewPhotoAct);
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(SingleGuessActivity.this)
                                    .load(url)
                                    .error(R.drawable.error)
                                    .into(imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            takeGuessButton.setVisibility(View.VISIBLE);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent ViewPhotoAct = new Intent(
                                                            getBaseContext(), ViewPhotoActivity.class
                                                    );
                                                    ViewPhotoAct.putExtra("fileUrl", url);
                                                    startActivity(ViewPhotoAct);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError() {
                                        }
                                    });
                        }
                    });
        }
    }


    private void submitGuess() {
        if (guessLatLng != null) {

            progressBar.setVisibility(View.VISIBLE);
            String [] params = new String[3];
            params[0] = getIntent().getStringExtra("imageCode");
            params[1] = ""+guessLatLng.latitude;
            params[2] = ""+guessLatLng.longitude;
            SendGuessTask sendGuess = new SendGuessTask();
            sendGuess.execute(params);
        }
        else
        {
            Toast.makeText(SingleGuessActivity.this, R.string.add_marker_please, Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                googleMap.clear();
                googleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Your guess")
                );
                guessLatLng = point;
            }
        });
        if (guessLatLng != null)
        {
            googleMap.addMarker(new MarkerOptions()
                            .position(guessLatLng)
                            .title("Your guess")
            );
        }
        if (correctLocation != null)
        {
            googleMap.addMarker(new MarkerOptions()
                            .position(correctLocation)
                            .title(getString(R.string.correct_position))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
        }
    }

    private class SendGuessTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params) {
            String current = new String();

            SharedPreferences prefs = getSharedPreferences("user", 0);

            String username = prefs.getString("username", "");
            String cookie = prefs.getString("cookie", "");

            String photoPublicId = params[0];
            String latitude = params[1];
            String longitude = params[2];
            try {

                URL myUrl = new URL(getString(R.string.domain) + "guess.php?user=" + username + "&cookie=" + cookie + "&imagecode=" + photoPublicId + "&latitude="+ latitude + "&longitude=" + longitude);
                HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream in = connection.getInputStream();
                StringBuilder sBuilder = new StringBuilder();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(in));
                String line;
                while((line = bReader.readLine()) != null){
                    sBuilder.append(line);
                }
                current = sBuilder.toString();


                return current;

            } catch (IOException e) {
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
            else if(result.contains(getString(R.string.guess_exists))){
                Toast.makeText(SingleGuessActivity.this, "GUESS_EXIST", Toast.LENGTH_SHORT).show();
            }
            else {
                ImageDetails imageDetails = readJSONfromString(result);

                progressBar.setVisibility(View.GONE);
                correctLocation = imageDetails.getCoordinates();
                googleMap.addMarker(new MarkerOptions()
                        .position(correctLocation)
                        .title(getString(R.string.correct_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );

                googleMap.animateCamera(CameraUpdateFactory.newLatLng(correctLocation));

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                    }
                });

                takeGuessButton.setVisibility(View.GONE);

                Integer score = imageDetails.getScore();
                String postGuessMessage;

                if(score > 900) {
                    postGuessMessage = getString(R.string.post_guess_message_high) + Integer.toString(score);
                }
                else if(score > 500)
                {
                    postGuessMessage = getString(R.string.post_guess_message_medium) + Integer.toString(score);
                }
                else if(score > 0)
                {
                    postGuessMessage = getString(R.string.post_guess_message_low) + Integer.toString(score);
                }
                else {
                    postGuessMessage = getString(R.string.post_guess_message_zero) + Integer.toString(score);
                }

                postGuessTextView.setText(postGuessMessage);
                postGuessTextView.setVisibility(TextView.VISIBLE);
            }
        }

    }

    private ImageDetails readJSONfromString(String jsonString) {
        ImageDetails imageDetails = new ImageDetails();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonObject = jsonObject.getJSONObject("array");
            int score = jsonObject.getInt("score");
            jsonObject = jsonObject.getJSONObject("item");
            LatLng tempLatLng = new LatLng(jsonObject.getDouble("submissionlat"),jsonObject.getDouble("submissionlong"));
            imageDetails = new ImageDetails(jsonObject.getString("imageuser"), tempLatLng, score);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return imageDetails;
    }

}
