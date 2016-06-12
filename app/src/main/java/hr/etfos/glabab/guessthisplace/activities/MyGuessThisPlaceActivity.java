package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import hr.etfos.glabab.guessthisplace.R;

public class MyGuessThisPlaceActivity extends Activity {

    Button myGuessesButton, mySubmissionsButton, changePasswordButton, logOutButton;
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_guess_this_place);
        initializeUI();
    }

    private void initializeUI() {

        this.myGuessesButton = (Button) findViewById(R.id.my_guesses_button);
        this.mySubmissionsButton = (Button) findViewById(R.id.my_submissions_button);
        this.changePasswordButton = (Button) findViewById(R.id.my_change_password_button);
        this.logOutButton = (Button) findViewById(R.id.logout_button);
        this.titleText = (TextView) findViewById(R.id.main_title);

        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        titleText.setText(username+getString(R.string.my_guess_this_place_title));

        myGuessesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartMyGuessesActivity = new Intent(getApplicationContext(), MyGuessesActivity.class);
                startActivity(StartMyGuessesActivity);
            }
        });

        mySubmissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StartMySubmissionsActivity = new Intent(getApplicationContext(), MySubmissionsActivity.class);
                startActivity(StartMySubmissionsActivity);
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePasswordActivity = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePasswordActivity);
            }
        });
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.putString("username", "");
                prefs.putString("cookie", "");
                prefs.commit();
                finish();
            }
        });
    }
}
