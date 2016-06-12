package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hr.etfos.glabab.guessthisplace.classes.ImageDetails;
import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.classes.WorkaroundMapFragment;


public class ImageDetailsActivity extends Activity implements OnMapReadyCallback{

    ImageView imageView;
    String sourceActivity = null;
    GoogleMap mMap;
    ImageDetails imageDetails = new ImageDetails();
    String username = new String();
    String url;
    ScrollView mScrollView;
    TextView noGuessesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        initializeUI();

        GetImagesTask getImagesTask = new GetImagesTask();

        SharedPreferences prefs = getSharedPreferences("user", 0);

        username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");
        String imagecode = getIntent().getStringExtra("imageCode");
        sourceActivity = getIntent().getStringExtra("sourceActivity");

        getImagesTask.execute(getString(R.string.domain) + "imagedetails.php?user=" + username + "&cookie=" + cookie + "&imagecode=" + imagecode);
    }

    private void initializeUI() {

        mScrollView = (ScrollView) findViewById(R.id.sv_image_details);
        WorkaroundMapFragment mapFragment = ((WorkaroundMapFragment) getFragmentManager().findFragmentById(R.id.map));

        if(mScrollView != null) {
            mapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        mapFragment.getMapAsync(this);

        this.noGuessesTextView = (TextView) findViewById(R.id.no_guesses_textview);
        noGuessesTextView.setVisibility(TextView.GONE);

        imageView = (ImageView) findViewById(R.id.submitted_image);
        url = getIntent().getStringExtra("imageUrl");
        if(url == null)
        {
            Picasso.with(ImageDetailsActivity.this)
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
                            Picasso.with(ImageDetailsActivity.this)
                                    .load(url)
                                    .error(R.drawable.error)
                                    .into(imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
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
            else {
                ArrayList<ImageDetails> arrayList;
                arrayList = readJSONfromString(result);

                mMap.addMarker(new MarkerOptions()
                        .position(imageDetails.getCoordinates())
                        .title(getString(R.string.correct_position))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLng(imageDetails.getCoordinates()));

                if(sourceActivity.equals("MySubmissionsActivity"))
                {
                    for (int i = 0; i < arrayList.size(); i++) {
                        mMap.addMarker(new MarkerOptions()
                                .position(arrayList.get(i).getCoordinates())
                                .title(arrayList.get(i).getUser() + " score: " + arrayList.get(i).getScore())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    }
                }
                else if(sourceActivity.equals("MyGuessesActivity"))
                {
                    for (int i = 0; i < arrayList.size(); i++) {
                        if(arrayList.get(i).getUser().equals(username)) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(arrayList.get(i).getCoordinates())
                                    .title(getString(R.string.your_score) + " " + arrayList.get(i).getScore())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    }
                }
            }

        }
    }

    private ArrayList<ImageDetails> readJSONfromString(String jsonString) {
        ArrayList<ImageDetails> imageItemList = new ArrayList<ImageDetails>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonObject = jsonObject.getJSONObject("array");
            JSONObject jsonObjectImageInfo;
            jsonObjectImageInfo = jsonObject.getJSONObject("imageinfo");
            LatLng tempLatLng = new LatLng(jsonObjectImageInfo.getDouble("submissionlat"), jsonObjectImageInfo.getDouble("submissionlong"));
            imageDetails.setImageDetails(username, tempLatLng, 0);
            if(jsonObject.has("guesses")){
                JSONArray jsonArray = jsonObject.getJSONArray("guesses");
                for(int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    jsonObject = jsonObject.getJSONObject("guess");
                    tempLatLng = new LatLng(jsonObject.getDouble("guesslat"), jsonObject.getDouble("guesslong"));
                    ImageDetails imageDetails = new ImageDetails(jsonObject.getString("guessuser"), tempLatLng, jsonObject.getInt("guessscore"));
                    imageItemList.add(imageDetails);
                }
            }
            else {
                noGuessesTextView.setText(getString(R.string.no_guesses_your_image));
                noGuessesTextView.setVisibility(TextView.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageItemList;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
