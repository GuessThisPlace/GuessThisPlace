package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import hr.etfos.glabab.guessthisplace.R;

public class HelpAboutActivity extends Activity {

    Button buttonHelp, buttonAbout;
    TextView helpAboutTextView1, helpAboutTextView2, helpAboutTextView3, helpAboutTextView4;
    Integer activeButton;
    Integer primaryColor;
    Integer secondaryColor;
    Integer transparentColor;
    Integer fullTransparentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_about);


        
        initializeUI(savedInstanceState);
        setButtons();
    }

    private void initializeUI(Bundle savedInstanceState) {
        primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.primaryBackground);
        secondaryColor = ContextCompat.getColor(getApplicationContext(), R.color.secondaryBackground);
        transparentColor = ContextCompat.getColor(getApplicationContext(), R.color.transparent);
        fullTransparentColor = ContextCompat.getColor(getApplicationContext(), R.color.full_transparent);

        helpAboutTextView1 = (TextView) findViewById(R.id.help_about_textview1);
        helpAboutTextView2 = (TextView) findViewById(R.id.help_about_textview2);
        helpAboutTextView3 = (TextView) findViewById(R.id.help_about_textview3);
        helpAboutTextView4 = (TextView) findViewById(R.id.help_about_textview4);

        helpAboutTextView1.setBackgroundColor(fullTransparentColor);
        helpAboutTextView2.setBackgroundColor(transparentColor);
        helpAboutTextView3.setBackgroundColor(fullTransparentColor);
        helpAboutTextView4.setBackgroundColor(transparentColor);

        buttonHelp = (Button) findViewById(R.id.button_help);
        buttonAbout = (Button) findViewById(R.id.button_about);

        if (savedInstanceState != null) {
            activeButton = savedInstanceState.getInt("activeButton");
        }
        else {
            activeButton = 1;
        }

        if(activeButton == 1) {
            buttonAbout.setBackgroundColor(primaryColor);
            buttonHelp.setBackgroundColor(secondaryColor);
        }
        else {
            buttonAbout.setBackgroundColor(secondaryColor);
            buttonHelp.setBackgroundColor(primaryColor);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("activeButton", activeButton);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setButtons() {
        final int primaryColor = ContextCompat.getColor(getApplicationContext(), R.color.primaryBackground);
        final int secondaryColor = ContextCompat.getColor(getApplicationContext(), R.color.secondaryBackground);

        if(activeButton == 1) {
            buttonAbout.setBackgroundColor(primaryColor);
            buttonHelp.setBackgroundColor(secondaryColor);
            helpAboutTextView1.setText(getString(R.string.about_1));
            helpAboutTextView2.setText(getString(R.string.about_2));
            helpAboutTextView3.setText(getString(R.string.about_3));
            helpAboutTextView4.setVisibility(TextView.GONE);
        }
        else {
            buttonAbout.setBackgroundColor(secondaryColor);
            buttonHelp.setBackgroundColor(primaryColor);
            helpAboutTextView1.setText(getString(R.string.help_1));
            helpAboutTextView2.setText(getString(R.string.help_2));
            helpAboutTextView3.setText(getString(R.string.help_3));
            helpAboutTextView4.setText(getString(R.string.help_4));
            helpAboutTextView4.setVisibility(TextView.VISIBLE);
        }

        buttonAbout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    activeButton = 1;
                    buttonHelp.setEnabled(false);
                    buttonAbout.setBackgroundColor(primaryColor);
                    buttonHelp.setBackgroundColor(secondaryColor);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    buttonHelp.setEnabled(true);
                }
                helpAboutTextView1.setText(getString(R.string.about_1));
                helpAboutTextView2.setText(getString(R.string.about_2));
                helpAboutTextView3.setText(getString(R.string.about_3));
                helpAboutTextView4.setVisibility(TextView.GONE);
                return true;
            }
        });

        buttonHelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    activeButton = 0;
                    buttonAbout.setEnabled(false);
                    buttonHelp.setBackgroundColor(primaryColor);
                    buttonAbout.setBackgroundColor(secondaryColor);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    buttonAbout.setEnabled(true);
                }
                helpAboutTextView1.setText(getString(R.string.help_1));
                helpAboutTextView2.setText(getString(R.string.help_2));
                helpAboutTextView3.setText(getString(R.string.help_3));
                helpAboutTextView4.setText(getString(R.string.help_4));
                helpAboutTextView4.setVisibility(TextView.VISIBLE);
                return true;
            }
        });
    }
}
