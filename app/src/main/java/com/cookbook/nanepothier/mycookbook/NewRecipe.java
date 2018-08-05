package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.sql.*;
import java.sql.*;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class NewRecipe extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAGN = "***********view";

    private String userEmail = "haleyiron@gmail.com";
    private Integer numIngredients = 1;

    private EditText recipeNameView;
    private EditText primCategoryView;
    private EditText servingsView;
    private EditText prepTimeView;
    private EditText ovenTimeView;
    private EditText ovenTempView;
    private EditText caloriesView;
    private EditText instructionView;
    private EditText amountView;
    private EditText amountView2;
    private SaveTask saveTask;

    private Spinner spinner;
    private Spinner spinnerMeasurements;
    private ArrayList<String> measurements;
    public static ArrayList<String> listIngredients;
    private ArrayList<Spinner> measurementSpinners;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newrecipe_toolbar);
        setSupportActionBar(toolbar);

        // EditTexts
        recipeNameView = (EditText) findViewById(R.id.recipe_name);
        primCategoryView = (EditText) findViewById(R.id.category);
        servingsView = (EditText) findViewById(R.id.servings);
        prepTimeView = (EditText) findViewById(R.id.prep_time);
        ovenTimeView = (EditText) findViewById(R.id.oven_time);
        ovenTempView = (EditText) findViewById(R.id.oven_temp);
        caloriesView = (EditText) findViewById(R.id.calories);
        instructionView = (EditText) findViewById(R.id.editText);
        amountView = (EditText) findViewById(R.id.quantity1);
        amountView2 = (EditText) findViewById(R.id.quantity2);

        // ArrayLists
        listIngredients = new ArrayList<String>();
        measurements = new ArrayList<>();
        measurementSpinners = new ArrayList<>();



        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        spinnerMeasurements = (Spinner) findViewById((R.id.measurement1));
        spinnerMeasurements.setOnItemSelectedListener(this);

        measurements.add("pound");
        measurements.add("oz");

        measurementSpinners.add(spinnerMeasurements);

        // setMeasurementSpinners(measurementSpinners);

        ArrayAdapter<String> adapterMeasurements = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, measurements);
        adapterMeasurements.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurements.setAdapter(adapterMeasurements);

        getIngredients();

    }

    public void setMeasurementSpinners(ArrayList<Spinner> measurementSpinners, ArrayList<String> measurements){

        for(int x = 0; x < measurementSpinners.size(); x++){

            ArrayAdapter<String> adapterMeasurements = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, measurements);
            adapterMeasurements.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            measurementSpinners.get(x).setAdapter(adapterMeasurements);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_recipe, menu);

        return true;
    }


    // retrieve ingredients from database
    public void getIngredients(){

        GetIngredientsTask task = new GetIngredientsTask();
        task.execute((String) null);
    }

    private void onBackgroundTaskObtainedIngredients(ArrayList<String> ingredients){

        for(int z = 0; z < ingredients.size(); z++){

            System.out.println("Back in main activity: " + ingredients.get(z));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ingredients);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    // called when item in spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){

        System.out.println("Item was selected in spinner " + parent.getItemIdAtPosition(pos));
    }

    @Override
    public void onNothingSelected(AdapterView <?> parent){

    }


    // method invoked by appbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        boolean valid = false;

        switch(item.getItemId()){

            // when user clicks save
            case R.id.save_action:

                // check that required fields are filled
                // valid = checkRequiredFields();
                valid = true;

                // get text entered into textfields
                String recipeName = recipeNameView.getText().toString();
                String ingredient = spinner.getSelectedItem().toString();
                String category = primCategoryView.getText().toString();
                Integer prepTime = Integer.parseInt(prepTimeView.getText().toString());
                Integer ovenTime = Integer.parseInt(ovenTimeView.getText().toString());
                Integer ovenTemp = Integer.parseInt(ovenTempView.getText().toString());
                Integer servings = Integer.parseInt(servingsView.getText().toString());
                Integer calories = Integer.parseInt(caloriesView.getText().toString());
                String instructions = instructionView.getText().toString();

                System.out.println(" Selected ingredient: " + ingredient);

                if(valid) {

                    // execute new asynchronous save task
                    saveTask = new SaveTask(userEmail, recipeName, ingredient, category, prepTime
                                            , ovenTime, ovenTemp, servings, calories,
                                            numIngredients, instructions);
                    saveTask.execute((String) null);

                    return true;
                }

                return true;

            case R.id.cancel_action:
                //startActivity(new Intent(NewRecipe.this, MainActivity.class));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // check if required fields are filled out
    protected boolean checkRequiredFields() {

        if(recipeNameView.getText().length() > 0) {
            return true;
        }
        return false;
    }




    public class SaveTask extends AsyncTask<String, Void, String> {

        String recipeName;

        // TODO: store objects in arraylist that store ingredient, amount and measurement
        ArrayList<String> ingredients = new ArrayList<>();
        String ingredient;
        String primCategory;
        Integer prepTime, ovenTime, ovenTemp, servings, calories, numIngredients;
        String instructions;
        String uniqueID;
        String user;


        public SaveTask(String user, String rName, String ingredient, String primCat, Integer pTime, Integer oTime,
                        Integer oTemp, Integer servings, Integer calories, Integer numIng, String instruct){

            this.user = user;
            recipeName = rName;
            //ingredients = ing;
            this.ingredient = ingredient;
            primCategory = primCat;
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
            numIngredients = numIng;
            instructions = instruct;
        }

        @Override
        protected String doInBackground(String... params){

            String recipe;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

                try{
                    url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/save_recipe.php");

                    // generate json object to pass data
                    JSONObject jsonObject = new JSONObject();

                    // create unique id for this recipe
                    uniqueID = UUID.randomUUID().toString();
                    System.out.println("Unique ID generated: " + uniqueID);

                    System.out.println("Recipe data being passed: " + user + " " + recipeName + " " + ingredient + " " + primCategory);
                    System.out.println(" More data: " + prepTime + " " + ovenTime + " " + ovenTemp);

                    jsonObject.put("userEmail", user);
                    jsonObject.put("unique", uniqueID);
                    jsonObject.put("name", recipeName);
                    jsonObject.put("ingredientObject", ingredient);
                    jsonObject.put("primCategory", primCategory);
                    jsonObject.put("prepTime", prepTime);
                    jsonObject.put("ovenTime", ovenTime);
                    jsonObject.put("ovenTemp", ovenTemp);
                    jsonObject.put("servings", servings);
                    jsonObject.put("calories", calories);
                    jsonObject.put("numIngredients", numIngredients);
                    jsonObject.put("instructions", instructions);

                    recipe = jsonObject.toString();

                    System.out.println("Recipe in string format: " + recipe);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(recipe.getBytes().length);

                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                    connection.connect();

                    System.out.println("connection made I think ");

                    outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(recipe.getBytes());
                    outputStream.flush();


                    int responseCode = connection.getResponseCode();

                    if(responseCode == HttpURLConnection.HTTP_OK){

                        System.out.println("retrieving input ");

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
        protected void onPostExecute(String result){

            System.out.println(" final result before calling parsejson" + result);

            String finalResult = ParseJSON.parseJSON(result);

            System.out.println("final result: " + finalResult);

            if(finalResult.equals("success12345")){

                System.out.println("Everything was stored successfully. ");

                //startActivity(new Intent(NewRecipe.this, MainActivity.class));
            }else{

                System.out.println("Storing of data was not successful");
            }
        } // end onPostExecute

    }

    public class GetIngredientsTask extends AsyncTask<String, Void, String>{

        ArrayList<String> listIngredients = new ArrayList<String>();

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

        try{

            url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/retrieve_ingredients.php");

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            System.out.println("now maybe");

            connection.connect();

            System.out.println("connection established");

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

            }catch(Exception ie){
                ie.printStackTrace();
            }
        }

            return result;
        }

        @Override
        protected void onPostExecute(String data){

            try{

                listIngredients = ParseJSON.parseJSONArray(data);

                for(int x = 0; x < listIngredients.size(); x++ ){
                    System.out.println("Ing: " + listIngredients.get(x));
                }

                NewRecipe.this.onBackgroundTaskObtainedIngredients(listIngredients);

                //System.out.println("in string form: " + stringResult);
            }catch(Exception e){

            }

        }

    }

}
