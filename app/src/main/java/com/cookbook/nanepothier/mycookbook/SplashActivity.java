package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import org.json.JSONException;
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

    private String deviceId;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressView = findViewById(R.id.splash_progress);
        progressView.setVisibility(View.VISIBLE);

        checkIfDeviceKnown();
    }

    private void checkIfDeviceKnown(){

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        GetUserCredentials credentialsTask = new GetUserCredentials(deviceId);
        credentialsTask.execute();

        //onBackgroundCheckedDevice(true, "haleyiron@gmail.com", "cGx1c21pbnVzMg==");
    }

    /**
     * receive data obtained from background task and send data to login activity
     */
    public void onBackgroundCheckedDevice(boolean deviceIsKnown, String userEmail, String userPassword){

        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.putExtra("device_is_known", deviceIsKnown);
        intent.putExtra("user_email", userEmail);
        intent.putExtra("user_password", userPassword);

        progressView.setVisibility(View.GONE);

        startActivity(intent);
    }

    /**
     * Background task to check whether this device is already known
     * If it is known, return the user and password for this device
     */
    public class GetUserCredentials extends AsyncTask<String, Void, String> {

        private String deviceId;

        public GetUserCredentials(String deviceId){
            this.deviceId = deviceId;
        }

        @Override
        protected String doInBackground(String... params){

            String device;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                //url used when application was connecting to local server
                //url = new URL("http://10.0.0.18:9999/mycookbookservlets/GetUserCredentials");

                //connection to aws EC2 server instance
                url = new URL("http://3.16.170.8:8080/mycookbookservlets/GetUserCredentials");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("device_id", deviceId);
                device = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(device.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                connection.connect();

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(device.getBytes());
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
                    if(inputStream != null){
                        inputStream.close();
                    }
                    if(outputStream != null){
                        outputStream.close();
                    }
                }catch(IOException ie){
                    ie.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String jsonData) {

            try{
                String userEmail, userPassword;
                boolean deviceIsKnown;

                JSONObject jsonObject = new JSONObject(jsonData);
                deviceIsKnown = jsonObject.getBoolean("device_is_known");

                if(deviceIsKnown){

                    userEmail = jsonObject.getString("user_email");
                    userPassword = jsonObject.getString("user_password");
                }else{
                    userEmail = "";
                    userPassword = "";
                }

                onBackgroundCheckedDevice(deviceIsKnown, userEmail, userPassword);

            }catch(JSONException jException){
                jException.printStackTrace();
            }
        }
    }

}
