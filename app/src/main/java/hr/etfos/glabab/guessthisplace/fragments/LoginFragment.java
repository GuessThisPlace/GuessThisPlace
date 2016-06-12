package hr.etfos.glabab.guessthisplace.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hr.etfos.glabab.guessthisplace.R;
import hr.etfos.glabab.guessthisplace.activities.MainActivity;

public class LoginFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View Layout = inflater.inflate(R.layout.fragment_user_logging,null);

        Button btnSignIn = (Button) Layout.findViewById(R.id.button_sign_in);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etUsername = (EditText) Layout.findViewById(R.id.editText_username);
                EditText etPassword = (EditText) Layout.findViewById(R.id.editText_password);

                hideKeyboard();

                etUsername.setText(etUsername.getText().toString().toLowerCase());

                if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals(""))
                {

                    Toast.makeText(getActivity(), getString(R.string.enter_valid_user_pass), Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] params = new String[3];
                params[0] = "login";
                params[1] = etUsername.getText().toString();
                params[2] = etPassword.getText().toString();

                ((MainActivity)getActivity()).loginFromFragment(params);

            }
        });

        Button btnSignUp = (Button) Layout.findViewById(R.id.button_sign_up);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etUsername = (EditText) Layout.findViewById(R.id.editText_username);
                EditText etPassword = (EditText) Layout.findViewById(R.id.editText_password);

                etUsername.setText(etUsername.getText().toString().toLowerCase());
                hideKeyboard();

                if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")) {

                    Toast.makeText(getActivity(), R.string.enter_valid_user_pass, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(etPassword.getText().toString().length() < MainActivity.MINIMAL_PASSWORD_LENGTH)
                {
                    Toast.makeText(getActivity(), R.string.password_minimal_length, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(etPassword.getText().toString().length() > MainActivity.MAXIMAL_PASSWORD_LENGTH)
                {
                    Toast.makeText(getActivity(), getString(R.string.password_maximal_length), Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (etUsername.getText().toString().length() > MainActivity.MAXIMAL_USERNAME_LENGTH)
                {
                    Toast.makeText(getActivity(), getString(R.string.username_maximal_length), Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] params = new String[3];
                params[0] = "register";
                params[1] = etUsername.getText().toString();
                params[2] = etPassword.getText().toString();

                ((MainActivity)getActivity()).loginFromFragment(params);

            }
        });
        return Layout;
    }

    private void hideKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
