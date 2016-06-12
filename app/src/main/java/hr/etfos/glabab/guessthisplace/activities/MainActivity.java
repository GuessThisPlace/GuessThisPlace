package hr.etfos.glabab.guessthisplace.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.fragments.LoginFragment;
import hr.etfos.glabab.guessthisplace.fragments.MainScreenFragment;
import hr.etfos.glabab.guessthisplace.fragments.ProgressBarFragment;

public class MainActivity extends Activity {

    private final String LOGIN_FRAGMENT = "Login_fragment";
    private final String MAIN_SCREEN_FRAGMENT = "Main_screen_fragment";
    private final String PROGRESS_BAR_FRAGMENT = "Progress_bar_fragment";
    public static final int MINIMAL_PASSWORD_LENGTH = 6;
    public static final int MAXIMAL_PASSWORD_LENGTH = 20;
    public static final int MAXIMAL_USERNAME_LENGTH = 20;

    private String state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            state = savedInstanceState.getString("state",state);
            if (state != null && state.equals("main_screen"))
            {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_screen, new MainScreenFragment(), MAIN_SCREEN_FRAGMENT);
                fragmentTransaction.commit();
            }
            else if (state != null && state.equals("logging_screen"))
            {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_screen, new LoginFragment(), LOGIN_FRAGMENT);
                fragmentTransaction.commit();
            }
        } else {
            initializeUI();
            state = "logging_screen";
        }

    }
    public void SetState(String s)
    {
        state = s;
    }
    public void loginFromFragment(String[] params)
    {
        changeToProgressBarFragment();
        UserLogin userLogin = new UserLogin();
        userLogin.execute(params);
    }
    public void changeToProgressBarFragment()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_screen, new ProgressBarFragment(), PROGRESS_BAR_FRAGMENT);
        fragmentTransaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("state", state);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");

        if (username.equals("") && cookie.equals(""))
        {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_screen, new LoginFragment(), LOGIN_FRAGMENT);
            fragmentTransaction.commit();
        }
        else if (state.equals("main_screen")) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.main_screen, new MainScreenFragment(), MAIN_SCREEN_FRAGMENT);
            fragmentTransaction.commit();
        }
    }

    private void initializeUI() {
        SharedPreferences prefs = getSharedPreferences("user", 0);

        String username = prefs.getString("username", "");
        String cookie = prefs.getString("cookie", "");
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_screen, new ProgressBarFragment(), PROGRESS_BAR_FRAGMENT);
        fragmentTransaction.commit();

        UserLogin userLogin = new UserLogin();
        String[] params = new String[3];
        params[0] = "loginCookie";
        params[1] = username;
        params[2] = cookie;
        userLogin.execute(params);


    }


    public class UserLogin extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... params) {
            String requestType = params[0];
            if (params.length < 3)
                return 4;
            else if (params[1] == null || params[2] == null)
                return 4;
            else if (params[1].equals("") || params[2].equals(""))
                return 4;
            URL myUrl = null;
            if(requestType.equals("login"))
            {
                try {
                    myUrl = new URL(getString(R.string.domain) + "user.php?request=login&user=" + params[1] + "&password=" + params[2]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else if(requestType.equals("loginCookie"))
            {
                try {
                    myUrl = new URL(getString(R.string.domain) + "user.php?request=login&user=" + params[1] + "&cookie=" + params[2]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try {
                    myUrl = new URL(getString(R.string.domain) + "user.php?request=create&user=" + params[1] + "&password=" + params[2]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }


            HttpURLConnection connection ;
            String serverResponse;
            if (myUrl == null)
                return 10;
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
                serverResponse = sBuilder.toString();


            } catch (IOException e) {
                e.printStackTrace();
                return 100;
            }
            if(requestType.equals("login")) {
                if (serverResponse.length() >= 30) {
                    SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                    prefs.putString("username", params[1]);
                    prefs.putString("cookie", serverResponse.substring(0, 32));
                    prefs.commit();
                    return 0;
                }
                else if (serverResponse.equals(getString(R.string.login_not_ok))) {
                    return 12;
                } else
                    return 19;
            }
            else if (requestType.equals("loginCookie"))
            {
                if (serverResponse.equals(getString(R.string.valid_cookie)))
                {
                    return 0;
                }
                else if (serverResponse.equals(getString(R.string.invalid_cookie)))
                {
                    return 20;
                }
                else
                {
                    return 22;
                }
            }
            else {
                if (serverResponse.length() >= 30)
                {
                    SharedPreferences.Editor prefs = getSharedPreferences("user", 0).edit();
                    prefs.putString("username",params[1]);
                    prefs.putString("cookie", serverResponse.substring(0,32));
                    prefs.commit();
                    return 0;
                }
                else if (serverResponse.equals(getString(R.string.username_exists)))
                {
                    return 30;
                }
                else
                    return 39;
            }
        }
        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_screen, new MainScreenFragment(), MAIN_SCREEN_FRAGMENT);
                fragmentTransaction.commit();
                state = "main_screen";
                return;
            }
            else {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.main_screen, new LoginFragment(), LOGIN_FRAGMENT);
                fragmentTransaction.commit();

                if (result == 30)
                    Toast.makeText(MainActivity.this, getString(R.string.username_exists_toast), Toast.LENGTH_LONG).show();
                else if (result == 100)
                    Toast.makeText(MainActivity.this, getString(R.string.unable_to_connect_toast), Toast.LENGTH_LONG).show();
                else if(result != 4)
                    Toast.makeText(MainActivity.this, getString(R.string.login_not_ok_toast), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
