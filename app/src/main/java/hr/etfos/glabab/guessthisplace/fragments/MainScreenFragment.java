package hr.etfos.glabab.guessthisplace.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.activities.GuessPickerActivity;
import hr.etfos.glabab.guessthisplace.activities.HelpAboutActivity;
import hr.etfos.glabab.guessthisplace.activities.HighScoreActivity;
import hr.etfos.glabab.guessthisplace.activities.MainActivity;
import hr.etfos.glabab.guessthisplace.activities.MyGuessThisPlaceActivity;
import hr.etfos.glabab.guessthisplace.activities.UploadPhotoActivity;

public class MainScreenFragment extends Fragment  {
    private boolean canTakePhoto;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View Layout = inflater.inflate(R.layout.fragment_main_screen, null);

        Button takePhotoButton, guessPickerButton, myGTPButton, scoresButton, helpAboutButton;
        takePhotoButton = (Button) Layout.findViewById(R.id.take_photo_button);
        guessPickerButton = (Button) Layout.findViewById(R.id.guess_picker_button);
        myGTPButton = (Button) Layout.findViewById(R.id.my_gtp_button);
        scoresButton = (Button) Layout.findViewById(R.id.scores_button);
        helpAboutButton = (Button) Layout.findViewById(R.id.help_about_button);
        canTakePhoto = true;

        takePhotoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                PackageManager mgr = getActivity().getPackageManager();
                boolean hasCamera = mgr.hasSystemFeature(PackageManager.FEATURE_CAMERA);
                boolean hasGPS = mgr.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPS && hasCamera) {
                    if (canTakePhoto)
                    {
                        canTakePhoto = false;
                        QuotaCheckTask qCT = new QuotaCheckTask();
                        qCT.execute();
                    }
                } else {
                    if (!hasGPS || (hasCamera && hasGPS))
                        showGPSDisabledAlertToUser();
                    if (!hasCamera)
                        Toast.makeText(getActivity(), R.string.no_camera_toast, Toast.LENGTH_LONG).show();
                }
            }
        });

        guessPickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefs = getActivity().getSharedPreferences("guessPicker", 0).edit();
                prefs.clear();
                prefs.commit();
                Intent StartGuessPickerActivity = new Intent(
                        getActivity(), GuessPickerActivity.class
                );
                startActivity(StartGuessPickerActivity);
            }
        });

        myGTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartSettingsActivity = new Intent(
                        getActivity(), MyGuessThisPlaceActivity.class
                );
                startActivity(StartSettingsActivity);
            }
        });

        scoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartScoresActivity = new Intent(
                        getActivity(), HighScoreActivity.class
                );
                startActivity(StartScoresActivity);
            }
        });

        helpAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartHelpAboutActivity = new Intent(
                        getActivity(), HelpAboutActivity.class
                );
                startActivity(StartHelpAboutActivity);
            }
        });
        return Layout;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(getString(R.string.gps_disable_dialog))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.go_to_settings_gps),
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public class QuotaCheckTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params) {
            SharedPreferences prefs = getActivity().getSharedPreferences("user", 0);

            String username = prefs.getString("username", "");
            String cookie = prefs.getString("cookie", "");

            URL myUrl = null;
            try {
                myUrl = new URL(getString(R.string.domain) + "quotacheck.php?user=" + username + "&cookie=" + cookie);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) myUrl.openConnection();

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
            if (result.contains(getString(R.string.quota_ok)))
            {
                Intent StartTakePhotoActivity = new Intent(
                        getActivity(), UploadPhotoActivity.class
                );
                startActivity(StartTakePhotoActivity);
            }
            else if(result.contains(getString(R.string.quota_used)))
            {
                Toast.makeText(getActivity(), R.string.quota_used_message, Toast.LENGTH_LONG).show();
            }
            else if(result.contains(getString(R.string.invalid_cookie)))
            {
                SharedPreferences.Editor prefs = getActivity().getSharedPreferences("user", 0).edit();
                prefs.clear();
                prefs.commit();
                ((MainActivity)getActivity()).SetState("logging_screen");
                getActivity().recreate();
            }
            else
            {
                Toast.makeText(getActivity(), R.string.server_unreachable, Toast.LENGTH_SHORT).show();
            }
            canTakePhoto = true;
        }

    }
}
