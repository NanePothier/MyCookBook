package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The SplashActivity functions as a loading screen when the app is
 * first started and loaded into memory. If the user has used logged into
 * this app before, the user will be redirected to the menu page,
 * otherwise the user will be redirected to the login page
 */

public class SplashActivity extends AppCompatActivity {

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // TODO: start async task to retrieve user for this device or retrieve cookie that was created when user first logged in
    }

    /*
    public void onBackgroundRetrievedUser(boolean isAvailable, String user){

        if(isAvailable){

            userEmail = user;
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("action", "start");
            startActivity(intent);
        }else{

            Intent intent = new Intent(SplashActivity.this, Login.class);
            startActivity(intent);
        }
    }
    */

    /*
    public class GetUserCredentials extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params){

            String user;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/GetUserCredentials");

                JSONObject jsonObject = new JSONObject();
                //jsonObject.put("user", email);
                //jsonObject.put("password", password);

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

            // TODO: update
            boolean isAvailable = true;
            String userEmail = "t";

            onBackgroundRetrievedUser(isAvailable, userEmail);
        }

    }
    */

}
