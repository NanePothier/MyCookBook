package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AccountCreation extends AppCompatActivity {

    private EditText firstNameView;
    private EditText lastNameView;
    private EditText emailView;
    private EditText passwordView;
    private EditText passwordViewConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);

        firstNameView = (EditText) findViewById(R.id.first_name);
        lastNameView = (EditText) findViewById(R.id.last_name);
        emailView = (EditText) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        passwordViewConfirm = (EditText) findViewById(R.id.password_confirm);



        Button createBtn = (Button) findViewById(R.id.create_btn);
        createBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                storeInfo();
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
        }

        @Override
        protected String doInBackground(String... args){

            String message;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            StringBuilder result2 = null;
            String result = "";

            try{
                url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/create_account.php");

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


            String finalResult = parseJSON(data);
            lastNameView.setText(finalResult);

            if(finalResult.equals("success")){
                //firstNameView.setText("success");
                startActivity(new Intent(AccountCreation.this, Login.class));
            }else if(finalResult.equals("exists")){
                //firstNameView.setText(finalResult);
                emailView.setError("Email already exists.");
                emailView.requestFocus();
                emailView.setText("");

            }else{
                firstNameView.setError("Unable to create account. Please try again.");
                firstNameView.requestFocus();
            }


        }

        private String parseJSON(String jsonData){

            String stringResult = "";

            try{

                JSONObject json = new JSONObject(jsonData);
                stringResult = json.getString("successIndicator");

            }catch(Exception e){
                e.printStackTrace();
            }

            return stringResult;
        }
    }

}


