package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * activity MainActivity displays the menu
 * the menu allows the user to go to the Cookbook activity,
 * the NewRecipe activity or to exit the application
 */
public class MainActivity extends AppCompatActivity {

    private String userEmail;
    private PopupWindow infoPopup;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // retrieve data sent by other activity
        Intent receivedIntent = getIntent();
        userEmail = receivedIntent.getExtras().getString("user_email");
        String action = receivedIntent.getExtras().getString("action");
        boolean deviceIsKnown = receivedIntent.getExtras().getBoolean("device_is_known");

        coordinatorLayout = findViewById(R.id.main_activity_coordinator_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // go to cookbook activivty
        ImageView cookbookBtn = (ImageView) findViewById(R.id.cookbook_btn);
        cookbookBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MainActivity.this, CookbookActivity.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("action", "from_menu");
                startActivity(intent);
            }
        });

        // go to new recipe activity
        ImageView newRecipeBtn = (ImageView) findViewById(R.id.newrecipe_btn);
        newRecipeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MainActivity.this, NewRecipeActivity.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("status_indicator", "NewRecipe");
                startActivity(intent);
            }
        });

        // exit activity
        ImageView logoutBtn = (ImageView) findViewById(R.id.exit_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
                logoutIntent.putExtra("device_is_known", true);
                logoutIntent.putExtra("user_email", userEmail);
                logoutIntent.putExtra("user_password", "");
                startActivity(logoutIntent);
            }
        });

        handleAction(action);

        if(!deviceIsKnown){

            StoreDevice storeDeviceIdTask = new StoreDevice(userEmail);
            storeDeviceIdTask.execute();
        }
    }

    // display message to user depending on what activity user came from
    public void handleAction(String takeAction){

        if(takeAction.equals("save_action")){
            Snackbar.make(findViewById(R.id.main_activity_coordinator_layout), "Recipe saved successfully", Snackbar.LENGTH_LONG)
            .show();
        }

        if(takeAction.equals("cancel_action")){
            Snackbar.make(findViewById(R.id.main_activity_coordinator_layout), "Recipe not saved", Snackbar.LENGTH_LONG)
            .show();
        }
    }

    // toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_info:

                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View infoPopupView = inflater.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.menu_info_title);
                infoText.setText(R.string.menu_info);
                infoText2.setText(R.string.enjoy);
                infoText3.setVisibility(View.GONE);
                infoText4.setVisibility(View.GONE);

                infoPopup = new PopupWindow(infoPopupView, 1000, 950, true);
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

    /**
     * Represents an asynchronous background task used to store the current device
     * and associate it with the current user
     */
    public class StoreDevice extends AsyncTask<String, Void, String> {

        private String userEmail;

        public StoreDevice(String userEmail){
            this.userEmail = userEmail;
        }

        @Override
        protected String doInBackground(String... params){

            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String device;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                //url = new URL("http://10.0.0.18:9999/mycookbookservlets/StoreUserDevice");

                //connection to aws EC2 server instance
                url = new URL("http://3.16.170.8:8080/mycookbookservlets/StoreUserDevice");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_email", userEmail);
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
                    if(outputStream != null){
                        outputStream.close();
                    }
                    if(inputStream != null){
                        inputStream.close();
                    }
                }catch(IOException ie){
                    ie.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String jsonData) {

        }
    }
}
