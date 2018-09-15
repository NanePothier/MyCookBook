package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AccountCreation extends AppCompatActivity {

    private EditText firstNameView;
    private EditText lastNameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText passwordViewConfirm;
    private boolean firstNameFirstTime, lastNameFirstTime, emailFirstTime;
    private boolean passwordFirstTime, passwordConfirmFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        firstNameView = (EditText) findViewById(R.id.first_name);
        lastNameView = (EditText) findViewById(R.id.last_name);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        passwordViewConfirm = (EditText) findViewById(R.id.password_confirm);

        firstNameFirstTime = true;
        lastNameFirstTime = true;
        emailFirstTime = true;
        passwordFirstTime = true;
        passwordConfirmFirstTime = true;

        Button createBtn = (Button) findViewById(R.id.create_btn);
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

    private boolean isEmailValid(String email) {
        //ensure email entered is valid
        boolean isValid = false;

        if(email.contains("@") && email.contains(".") && email.length() <= 35 && email.length() >= 8){
            isValid = true;
        }
        return isValid;
    }

    private boolean isPasswordValid(String password) {
        //ensure password is at least 8 characters and at most 16 characters long
        boolean isValid = false;

        if(password.length() >= 8 && password.length() <= 16){
            isValid = true;
        }
        return isValid;
    }


    public class UserInfoTask extends AsyncTask<String, Void, String> {

        String firstName, lastName, userEmail, password;

        UserInfoTask(String first, String last, String user, String pass){
            firstName = first;
            lastName = last;
            userEmail = user;
            password = pass;

            encodePassword();
        }

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


