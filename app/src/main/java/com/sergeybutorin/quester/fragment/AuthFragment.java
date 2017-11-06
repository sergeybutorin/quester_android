package com.sergeybutorin.quester.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.activity.MainActivity;
import com.sergeybutorin.quester.model.LoginRequest;
import com.sergeybutorin.quester.model.UserProfile;
import com.sergeybutorin.quester.network.Api;
import com.sergeybutorin.quester.network.AuthController;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sergeybutorin on 03/11/2017.
 */

public class AuthFragment extends Fragment implements View.OnClickListener,
        AuthController.LoginListener,
        AuthController.SignupListener {

    boolean signupMode = false;

    Button loginButton;
    Button signupButton;
    EditText emailEditText;
    EditText passwordEditText;
    EditText firstnameEditText;
    EditText lastnameEditText;

    private AuthController controller;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        controller = new AuthController(getContext());
        controller.setLoginResultListener(this);
        controller.setSignupResultListener(this);

        loginButton = view.findViewById(R.id.button_login);
        signupButton = view.findViewById(R.id.button_signup);

        emailEditText = view.findViewById(R.id.email_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        firstnameEditText = view.findViewById(R.id.firstname_edit_text);
        lastnameEditText = view.findViewById(R.id.lastname_edit_text);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                if (checkLoginFields()) {
                    controller.login(emailEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                break;
            case R.id.button_signup:
                if (!signupMode) {
                    controller.setLoginResultListener(null);
                    showSignupFields();
                } else if (checkSignupFields()) {
                    System.out.println("Signup in fragment");
                    controller.signup(emailEditText.getText().toString(),
                            passwordEditText.getText().toString(),
                            firstnameEditText.getText().toString(),
                            lastnameEditText.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.setLoginResultListener(null);
        controller.setSignupResultListener(null);
    }

    private void showSignupFields() {
        signupMode = true;
        loginButton.setVisibility(View.GONE);
        firstnameEditText.setVisibility(View.VISIBLE);
        lastnameEditText.setVisibility(View.VISIBLE);
    }

    private boolean checkLoginFields() {
        if (emailEditText.getText().toString().length() == 0 ||
                passwordEditText.getText().toString().length() == 0) {
            Toast.makeText(getContext(), R.string.error_empty_strings, Toast.LENGTH_LONG).show();
            return false;
        }
        if (!emailEditText.getText().toString().matches(Constants.EMAIL_REGEX)){
            Toast.makeText(getContext(), R.string.error_incorrect_email, Toast.LENGTH_LONG).show();
            emailEditText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean checkSignupFields() {
        if (!checkLoginFields()) {
            return false;
        }
        if (firstnameEditText.getText().toString().length() == 0 ||
                lastnameEditText.getText().toString().length() == 0) {
            Toast.makeText(getContext(), R.string.error_empty_strings, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onResult(boolean success, int message, UserProfile user) {
        if (!success) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        } else if (user != null) {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.USER_KEYS.EMAIL.getValue(), user.getEmail());
            editor.putString(Constants.USER_KEYS.FIRSTNAME.getValue(), user.getFirstName());
            editor.putString(Constants.USER_KEYS.LASTNAME.getValue(), user.getLastName());
            editor.putString(Constants.USER_KEYS.TOKEN.getValue(), user.getToken());
            editor.apply();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content, new QMapFragment()).commit();
            ((MainActivity)getActivity()).setUserInformation();
        }
    }
}
