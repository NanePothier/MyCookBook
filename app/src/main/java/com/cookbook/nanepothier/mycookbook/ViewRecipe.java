package com.cookbook.nanepothier.mycookbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ViewRecipe extends AppCompatActivity {

    private String recipeName;
    private String userEmail;

    private TextView primCategoryView;
    private TextView servingsView;
    private TextView preparationTimeView;
    private TextView ovenTimeView;
    private TextView ovenTempView;
    private TextView caloriesView;
    private TextView instructionsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view_recipe);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        primCategoryView = (TextView) findViewById(R.id.primCategory_view_view);
        servingsView = (TextView) findViewById(R.id.servings_view_view);
        preparationTimeView = (TextView) findViewById(R.id.prep_time_view);
        ovenTimeView = (TextView) findViewById(R.id.oven_time_view);
        ovenTempView = (TextView) findViewById(R.id.oven_temp_view);
        caloriesView = (TextView) findViewById(R.id.calories_view);
        instructionsView = (TextView) findViewById(R.id.instructions_view);

        // get recipe name and email through intent
        recipeName = "Spaghetti";
        userEmail="haleyiron@gmail.com";

        retrieveRecipe();

        setViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.view_recipe_menu, menu);

        return true;
    }

    public void retrieveRecipe(){

        GetRecipeTask task = new GetRecipeTask(userEmail, recipeName);
        task.execute();
    }

    public void setViews(){

        // primCategoryView.setText("Pasta");
        // servingsView.setText("");
        // preparationTimeView.setText("");
        // ovenTimeView.setText("");
        // ovenTempView.setText("");
        // caloriesView.setText("");
        // instructionsView.setText("");
    }

    public void addIngredientView(){

    }

    public void onBackgroundTaskObtainedRecipe(String x){

    }



    public class GetRecipeTask extends AsyncTask<String, Void, String> {

        ArrayList<String> listItems = new ArrayList<String>();
        String itemIndicator;
        String fileURL;
        String recipeName;
        String userEmail;
        String data;

        public GetRecipeTask(String user, String recipe){

            userEmail = user;
            recipeName = recipe;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/retrieve_recipe.php");

                // generate json object to pass data
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("email", userEmail);
                jsonObject.put("recipe", recipeName);

                data = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(data.getBytes().length);

                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                System.out.println("now maybe");

                connection.connect();

                System.out.println("connection established");

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(data.getBytes());
                outputStream.flush();

                int responseCode = connection.getResponseCode();


                if(responseCode == HttpURLConnection.HTTP_OK){

                    System.out.println("Connection is ok");
                    inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while((line = reader.readLine())!= null){
                        result += line;
                    }
                }

                System.out.println("result string: " + result);


            }catch(Exception ioe){
                ioe.printStackTrace();
            }finally{

                try{
                    inputStream.close();
                    outputStream.close();

                }catch(Exception ie){
                    ie.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String data){

            try{

                // parse data

                // ViewRecipe.this.onBackgroundTaskObtainedRecipe(recipeObject);

                //System.out.println("in string form: " + stringResult);
            }catch(Exception e) {

            }
        }
    }

}
