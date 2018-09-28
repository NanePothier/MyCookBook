package com.cookbook.nanepothier.mycookbook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The login page allows the user to log into their account using
 * their email address and password
 */
public class Login extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask userLoginTask = null;

    // Views
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button signInButton;
    private Button createAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the enter email view
        mEmailView = findViewById(R.id.email);

        // set up password view
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                //if enter key is pressed attempt to login
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //if sign in button is clicked attempt to login
                attemptLogin();
            }
        });

        createAccountButton = (Button) findViewById(R.id.create_account_btn);
        createAccountButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //direct to account creation page if create account button is clicked
                startActivity(new Intent(Login.this, AccountCreation.class));
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to login.
     * If there are form errors (invalid email, missing fields, ...), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        if (userLoginTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error, so request focus of view in which last error occurred
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt.
            showProgress(true);
            userLoginTask = new UserLoginTask(email, password);
            userLoginTask.execute((String) null);
        }
    }

    // check if email entered is valid
    private boolean isEmailValid(String email) {

        boolean isValid = false;

        if(email.contains("@") && email.contains(".") && email.length() <= 35 && email.length() >= 8){
            isValid = true;
        }
        return isValid;
    }

    //check if password is at least 8 characters and at most 16 characters long
    private boolean isPasswordValid(String password) {

        boolean isValid = false;

        if(password.length() >= 8 && password.length() <= 16){
            isValid = true;
        }
        return isValid;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {

        String email;
        String password;

        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;

            encodePassword();
        }

        // encode password before sending
        // comparison with stored password will occur in encrypted form
        public void encodePassword(){
            try{
                byte [] data = password.getBytes("UTF-8");
                password = Base64.encodeToString(data, Base64.DEFAULT);
            }catch(UnsupportedEncodingException exception){
                exception.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params){

            String user;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/ValidateUser");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", email);
                jsonObject.put("password", password);

                user = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(user.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                connection.connect();

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(user.getBytes());
                outputStream.flush();

                int responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){

                    inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while((line = reader.readLine())!= null){
                        result += line;
                    }
                }
            }catch(Exception ioe){
                ioe.printStackTrace();
            }finally{
                try{
                    outputStream.close();
                    inputStream.close();

                }catch(IOException ie){
                    ie.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String jsonData) {

            userLoginTask = null;
            showProgress(false);

            String finalResult = ParseJSON.parseJSON(jsonData);

            if(finalResult.equals("match")){

                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("user_email", email);
                intent.putExtra("action", "login");
                startActivity(intent);

            }else if(finalResult.equals("wrong_password")){

                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
                mPasswordView.setText("");

            }else if(finalResult.equals("user_does_not_exist")){
                mEmailView.requestFocus();
                mEmailView.setError("Account with this email address does not exist");
            }
        }

        @Override
        protected void onCancelled() {
            userLoginTask = null;
            showProgress(false);
        }
    }
}


