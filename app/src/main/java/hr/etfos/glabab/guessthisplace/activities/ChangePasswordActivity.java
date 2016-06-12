package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import hr.etfos.glabab.guessthisplace.R;

public class ChangePasswordActivity extends Activity {
    Button changePasswordButton;
    EditText currentPassword, newPassword1, newPassword2;
    String currentPasswordString, newPassword1String, newPassword2String;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeUI();

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentPasswordString.equals(""))
                {
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.enter_current_password), Toast.LENGTH_SHORT).show();
                }
                else if(!newPassword1String.equals(newPassword2String))
                {
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.reenter_new_password), Toast.LENGTH_SHORT).show();
                }
                else if (newPassword1String.length() < MainActivity.MINIMAL_PASSWORD_LENGTH)
                {
                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_minimal_length), Toast.LENGTH_SHORT).show();
                }
                else {

                    ChangePasswordTask myTask = new ChangePasswordTask();
                    String[] params = new String[2];
                    params[0] = currentPasswordString;
                    params[1] = newPassword1String;
                    myTask.execute(params);
                }
            }
        });
    }

    private void initializeUI() {
        setContentView(R.layout.activity_change_password);
        currentPassword = (EditText) findViewById(R.id.editText_current_password);
        newPassword1 = (EditText) findViewById(R.id.editText_new_password_first);
        newPassword2 = (EditText) findViewById(R.id.editText_new_password_second);
        changePasswordButton = (Button) findViewById(R.id.button_change_password);

        currentPasswordString = currentPassword.getText().toString();
        newPassword1String = newPassword1.getText().toString();
        newPassword2String = newPassword2.getText().toString();
    }

    public class ChangePasswordTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params) {
            SharedPreferences prefs = getSharedPreferences("user", 0);

            String username = prefs.getString("username", "");
            String cookie = prefs.getString("cookie", "");

            URL myUrl = null;
            try {
                myUrl = new URL(getString(R.string.domain) + "user.php?request=changepassword&user=" + username + "&cookie=" + cookie  + "&oldpassword=" + params[0] + "&newpassword=" +params[1]);
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
            if(result.length() >= 32)
            {
                Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.putString("cookie", result.substring(0,32));
                prefs.commit();
                finish();
            }
            else if(result.contains(getString(R.string.invalid_cookie)))
            {
                SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                prefs.putString("username", "");
                prefs.putString("cookie", "");
                prefs.commit();
                finish();
            }

            else if(result.contains(getString(R.string.password_not_ok)))
            {
                Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_current_password_wrong), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(ChangePasswordActivity.this, getString(R.string.password_change_try_again_later), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
