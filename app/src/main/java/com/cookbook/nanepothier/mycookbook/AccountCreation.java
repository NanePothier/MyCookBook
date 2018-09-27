package com.cookbook.nanepothier.mycookbook;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The Account Creation Activity allows the user to
 * create an account using their email address and a chosen password
 */

public class AccountCreation extends AppCompatActivity {

    private EditText firstNameView;
    private EditText lastNameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText passwordViewConfirm;
    private boolean firstNameFirstTime, lastNameFirstTime, emailFirstTime;
    private boolean passwordFirstTime, passwordConfirmFirstTime;

    private View progressView;
    private PopupWindow infoPopup;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        coordinatorLayout = findViewById(R.id.account_creation_coordinator_layout);

        Toolbar toolbar = findViewById(R.id.account_creation_toolbar);
        setSupportActionBar(toolbar);

        firstNameView = findViewById(R.id.first_name);
        lastNameView = findViewById(R.id.last_name);
        emailView = findViewById(R.id.email);
        passwordView = findViewById(R.id.password);
        passwordViewConfirm = findViewById(R.id.password_confirm);

        progressView = findViewById(R.id.account_creation_progress);

        firstNameFirstTime = true;
        lastNameFirstTime = true;
        emailFirstTime = true;
        passwordFirstTime = true;
        passwordConfirmFirstTime = true;

        Button createBtn = findViewById(R.id.create_btn);
        createBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                storeInfo();
            }
        });

        firstNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){

                firstNameView.setTextColor(Color.BLACK);

                if(hasFocus && firstNameFirstTime){
                    firstNameView.setText("");
                    firstNameFirstTime = false;
                }
            }
        });

        lastNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){

                if(hasFocus && lastNameFirstTime){
                    lastNameView.setText("");
                    lastNameView.setTextColor(Color.BLACK);
                    lastNameFirstTime = false;
                }
            }
        });

        emailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){

                if(hasFocus && emailFirstTime){
                    emailView.setText("");
                    emailView.setTextColor(Color.BLACK);
                    emailFirstTime = false;
                }
            }
        });

        passwordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){

                if(hasFocus && emailFirstTime){
                    passwordView.setText("");
                    passwordFirstTime = false;
                }
            }
        });

        passwordViewConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus){

                if(hasFocus && emailFirstTime){
                    passwordViewConfirm.setText("");
                    passwordConfirmFirstTime = false;
                }
            }
        });
    }

    /**
     * start asyncronous background task to store user info if entered
     * information is valid
     */
    public void storeInfo(){

        String first = firstNameView.getText().toString();
        String last = lastNameView.getText().toString();
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = passwordViewConfirm.getText().toString();

        emailView.setError(null);
        passwordView.setError(null);
        firstNameView.setError(null);

        boolean validEmail = isEmailValid(email);
        boolean validPass = isPasswordValid(password);

        if(validEmail){

            if(validPass && !TextUtils.isEmpty(password)){

                if(password.equals(confirmPassword)){
                    showProgress(true);
                    UserInfoTask userInfoTask = new UserInfoTask(first, last, email, password);
                    userInfoTask.execute((String) null);
                }else{
                    passwordView.setError("The two passwords entered do not match");
                    passwordView.requestFocus();
                    passwordView.setText("");
                    passwordViewConfirm.setText("");
                }
            }else{
                passwordView.setError("Incorrect password format. Password has to be between 8 and 16 characters.");
                passwordView.requestFocus();
                passwordView.setText("");
                passwordViewConfirm.setText("");
            }
        }else{
            emailView.setError("Incorrect Email Format");
            emailView.requestFocus();
            emailView.setText("");
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

    // check if password is at least 8 characters and at most 16 characters long
    private boolean isPasswordValid(String password) {

        boolean isValid = false;

        if(password.length() >= 8 && password.length() <= 16){
            isValid = true;
        }
        return isValid;
    }

    // display progress bar
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);

        if(show){
            emailView.setEnabled(false);
            firstNameView.setEnabled(false);
            lastNameView.setEnabled(false);
            passwordView.setEnabled(false);
            passwordViewConfirm.setEnabled(false);
        }else {
            emailView.setEnabled(true);
            firstNameView.setEnabled(true);
            lastNameView.setEnabled(true);
            passwordView.setEnabled(true);
            passwordViewConfirm.setEnabled(true);
        }
    }

    // toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    // handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_info:

                LayoutInflater inflater = (LayoutInflater) AccountCreation.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View infoPopupView = inflater.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.account_info_title);
                infoText.setText(R.string.account_string_general);
                infoText2.setText(R.string.account_string_pw);
                infoText3.setText(R.string.account_press_btn);
                infoText4.setVisibility(View.GONE);

                infoPopup = new PopupWindow(infoPopupView, 1200, 1300, true);
                infoPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infoPopup.dismiss();
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // async task used to store user account data
    public class UserInfoTask extends AsyncTask<String, Void, String> {

        String firstName, lastName, userEmail, password;

        UserInfoTask(String first, String last, String user, String pass){
            firstName = first;
            lastName = last;
            userEmail = user;
            password = pass;

            encodePassword();
        }

        // send and store password in encoded form
        public void encodePassword(){
            try{
                byte [] data = password.getBytes("UTF-8");
                password = Base64.encodeToString(data, Base64.DEFAULT);
            }catch(UnsupportedEncodingException exception){
                exception.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... args){

            String message;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/CreateAccount");

                // send data to be stored
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("first", firstName);
                jsonObject.put("last", lastName);
                jsonObject.put("user", userEmail);
                jsonObject.put("password", password);

                message = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(message.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                connection.connect();

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(message.getBytes());
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
        protected void onPostExecute(String data){

            String finalResult = ParseJSON.parseJSON(data);

            showProgress(false);

            // if data was stored successfully, go to login activity
            if(finalResult.equals("success")){

                startActivity(new Intent(AccountCreation.this, Login.class));
            }else if(finalResult.equals("exists")){

                emailView.setError("Email already exists.");
                emailView.requestFocus();
                emailView.setText("");
            }else{

                firstNameView.setError("Unable to create account. Please try again.");
                firstNameView.requestFocus();
            }
        }
    }
}


