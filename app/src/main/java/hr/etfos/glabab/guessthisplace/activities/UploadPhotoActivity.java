package hr.etfos.glabab.guessthisplace.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.maps.GoogleMap;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.cloudinary.*;

import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.classes.AutoResizeTextView;
import hr.etfos.glabab.guessthisplace.classes.WorkaroundMapFragment;


public class UploadPhotoActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int JPEG_QUALITY = 60;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private GoogleMap googleMap;
    private WorkaroundMapFragment googleMapFragment;
    LatLng currentLocation = null;
    Button uploadBtn;
    AutoResizeTextView infoText;
    Bitmap photoBitmap;
    ImageView myPhoto;
    boolean imageShown;
    ScrollView mScrollView;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload_photo);

        imageShown = false;

        progressBar = (ProgressBar) findViewById(R.id.upload_photo_progress_bar);

        infoText = (AutoResizeTextView) findViewById(R.id.upload_photo_info_text);

        mScrollView = (ScrollView) findViewById(R.id.sv_upload_photo);
        googleMapFragment = (WorkaroundMapFragment) getFragmentManager().findFragmentById(R.id.map);
        if(mScrollView != null) {
            googleMapFragment.setListener(new WorkaroundMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }

        uploadBtn = (Button) findViewById(R.id.button_capture);
        uploadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    uploadPhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        uploadBtn.setVisibility(View.GONE);
        myPhoto = (ImageView) findViewById(R.id.upload_photo_image);
        myPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UploadPhotoActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });


        myPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (photoBitmap != null) {
                    Intent ViewPhotoAct = new Intent(
                            getBaseContext(), ViewPhotoActivity.class
                    );
                    ViewPhotoAct.putExtra("fileUri", fileUri);
                    startActivity(ViewPhotoAct);
                }


            }
        });

        progressBar.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.VISIBLE);

        if (savedInstanceState != null)
        {
            currentLocation = savedInstanceState.getParcelable("currentLocation");
            googleMapFragment.onCreate(savedInstanceState);
            imageShown = savedInstanceState.getBoolean("imageShown");
            fileUri = savedInstanceState.getParcelable("fileUri");
            if (imageShown)
            {
                try {
                    photoBitmap = getBitmap(fileUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                myPhoto.setImageBitmap(photoBitmap);
            }
            googleMapFragment.getMapAsync(this);

        }
        else {
            googleMapFragment.getMapAsync(this);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
            if (fileUri != null)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;

        if (currentLocation != null)
        {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));

            googleMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.marker_title))
                    .position(currentLocation));

            progressBar.setVisibility(View.GONE);
            infoText.setVisibility(View.GONE);
            uploadBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        googleMapFragment.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("currentLocation", currentLocation);
        savedInstanceState.putBoolean("imageShown",imageShown);
        savedInstanceState.putParcelable("fileUri", fileUri);

        super.onSaveInstanceState(savedInstanceState);
    }

    // Create a file Uri for saving an image or video
    private static Uri getOutputMediaFileUri(int type) {
        File f = getOutputMediaFile(type);
        if (f == null)
            return null;
        return Uri.fromFile(f);
    }

    // Create a File for saving an image or video
    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void makeUseOfNewLocation(Location loc) {
        currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

        if (googleMap != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));

            googleMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.marker_title))
                    .position(currentLocation));
            progressBar.setVisibility(View.GONE);
            infoText.setVisibility(View.GONE);
            uploadBtn.setVisibility(View.VISIBLE);
        }


    }

    private void uploadPhoto() throws IOException {

        if (currentLocation == null)
        {
            Toast.makeText(UploadPhotoActivity.this, getString(R.string.location_not_found), Toast.LENGTH_LONG).show();

        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
            uploadBtn.setVisibility(View.GONE);

            UploadTask myTask = new UploadTask();

            myTask.execute();
        }

    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (photoBitmap != null)
        {
            ImageView iv = (ImageView) findViewById(R.id.upload_photo_image);
            iv.setImageBitmap(null);
            photoBitmap.recycle();
            photoBitmap = null;
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(getApplicationContext(), UploadPhotoActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        makeUseOfNewLocation(location);
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, Looper.getMainLooper());

                ImageView iv = (ImageView) findViewById(R.id.upload_photo_image);
                try {
                    photoBitmap = getBitmap(fileUri);
                    if (photoBitmap == null)
                    {
                        Toast.makeText(this, getString(R.string.free_memory), Toast.LENGTH_LONG).show();
                        finish();
                    }
                    iv.setImageBitmap((photoBitmap));

                    imageShown = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                Toast.makeText(this, getString(R.string.image_save_fail), Toast.LENGTH_LONG).show();
                finish();
            }
        }

    }

    public static Bitmap getBitmap(Uri fUri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        File file = new File(fUri.getPath());
        FileInputStream fileInputStream = new FileInputStream(file);
        return BitmapFactory.decodeStream(fileInputStream, null, options);
    }

    private class UploadTask extends AsyncTask<Void, Void, String>

    {
        @Override
        protected String doInBackground(Void... params) {
            SharedPreferences prefs = getSharedPreferences("user", 0);

            String username = prefs.getString("username", "");
            String cookie = prefs.getString("cookie", "");

            if (cookie.equals("") || username.equals(""))
                return getString(R.string.invalid_cookie);

            Map config = new HashMap();
            config.put("cloud_name", "guessthisplace");
            Cloudinary cloudinary = new Cloudinary(config);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photoBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);


            try {
                Map result = cloudinary.uploader().unsignedUpload(bs, getString(R.string.cloudinary_preset),
                        ObjectUtils.emptyMap());

                String photoPublicId = (String) result.get("public_id");

                URL myUrl = new URL(getString(R.string.domain) + "upload.php?user=" + username + "&cookie=" + cookie + "&imagecode=" + photoPublicId + "&latitude="+ currentLocation.latitude + "&longitude=" + currentLocation.longitude);
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
                String serverResponse = sBuilder.toString();
                return serverResponse;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return getString(R.string.unknown_error);
        }
        @Override
        protected void onPostExecute(String result) {
            if (result.contains(getString(R.string.upload_ok))) {
                Toast.makeText(UploadPhotoActivity.this, getString(R.string.submission_successful), Toast.LENGTH_LONG).show();
                finish();
            }
            else if (result.contains(getString(R.string.invalid_cookie)))
            {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.clear();
                prefs.commit();
                finish();
            }
            else
            {
                Toast.makeText(UploadPhotoActivity.this, getString(R.string.image_upload_failed), Toast.LENGTH_LONG).show();
                uploadBtn.setVisibility(View.VISIBLE);
            }

            progressBar.setVisibility(View.GONE);
        }

    }

}


