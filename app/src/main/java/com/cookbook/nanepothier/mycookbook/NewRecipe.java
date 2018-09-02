package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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

    private String userEmail = "haleyiron@gmail.com";
    private Integer numIngredients = 1;

    // views
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
    private GetItemsTask ingTask;
    private GetItemsTask catTask;

    // spinners
    private Spinner spinner;
    private Spinner spinner2;

    private Spinner spinnerMeasurements;
    private Spinner spinnerMeasurements2;

    private Spinner categorySpinner;

    // array lists
    private ArrayList<String> USMeasurements;
    private ArrayList<String> MetricMeasurements;
    public static ArrayList<String> listIngredients;
    private ArrayList<Spinner> ingredientSpinners;
    private ArrayList<Spinner> measurementSpinners;
    private ArrayList<String> quantityViews;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newrecipe_toolbar);
        setSupportActionBar(toolbar);

        // EditTexts
        recipeNameView = (EditText) findViewById(R.id.recipe_name);
        servingsView = (EditText) findViewById(R.id.servings);
        prepTimeView = (EditText) findViewById(R.id.prep_time);
        ovenTimeView = (EditText) findViewById(R.id.oven_time);
        ovenTempView = (EditText) findViewById(R.id.oven_temp);
        caloriesView = (EditText) findViewById(R.id.calories);
        instructionView = (EditText) findViewById(R.id.editText);
        amountView = (EditText) findViewById(R.id.quantity1);
        amountView2 = (EditText) findViewById(R.id.quantity2);

        // ArrayLists
        listIngredients = new ArrayList<>();
        USMeasurements = new ArrayList<>();
        MetricMeasurements = new ArrayList<>();
        measurementSpinners = new ArrayList<>();
        ingredientSpinners = new ArrayList<>();
        quantityViews = new ArrayList<>();

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner.setOnItemSelectedListener(this);
        spinner2.setOnItemSelectedListener(this);

        spinnerMeasurements = (Spinner) findViewById(R.id.measurement1);
        spinnerMeasurements2 = (Spinner) findViewById(R.id.measurement2);
        spinnerMeasurements.setOnItemSelectedListener(this);
        spinnerMeasurements2.setOnItemSelectedListener(this);

        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        createUSMeasurementList();

        ingredientSpinners.add(spinner);
        ingredientSpinners.add(spinner2);

        measurementSpinners.add(spinnerMeasurements);
        measurementSpinners.add(spinnerMeasurements2);

        setSpinners(measurementSpinners, USMeasurements);

        getIngredients();
        getCategories();
    }

    public void setSpinners(ArrayList<Spinner> spinners, ArrayList<String> stringList){

        for(int x = 0; x < spinners.size(); x++){

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stringList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinners.get(x).setAdapter(adapter);
        }
    }

    public void createUSMeasurementList(){

        USMeasurements.add("pound");
        USMeasurements.add("oz");
        USMeasurements.add("cup");
    }

    public void createMetricMeasurementList(){

        MetricMeasurements.add("g");
        MetricMeasurements.add("kg");
        MetricMeasurements.add("ml");
        MetricMeasurements.add("L");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_recipe, menu);

        return true;
    }


    // retrieve ingredients from database
    public void getIngredients(){

        ingTask = new GetItemsTask("ing", userEmail);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            ingTask.execute((String) null);
        }
    }

    public void getCategories(){

        catTask = new GetItemsTask("cat", userEmail);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            catTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            catTask.execute((String) null);
        }
    }

    private void onBackgroundTaskObtainedIngredients(ArrayList<String> ingredients){

        for(int z = 0; z < ingredients.size(); z++){

            System.out.println("Back in main activity: " + ingredients.get(z));
        }

        setSpinners(ingredientSpinners, ingredients);
    }

    private void onBackgroundTaskObtainedCategories(ArrayList<String> categories){

        for(int z = 0; z < categories.size(); z++){

            System.out.println("Back in main activity categories: " + categories.get(z));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }


    // called when item in spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){

        System.out.println("Item was selected in spinner " + parent.getItemIdAtPosition(pos));
    }

    @Override
    public void onNothingSelected(AdapterView <?> parent){

    }


    // TODO: change category to spinner item
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
                String category = categorySpinner.getSelectedItem().toString();
                Integer prepTime = Integer.parseInt(prepTimeView.getText().toString());
                Integer ovenTime = Integer.parseInt(ovenTimeView.getText().toString());
                Integer ovenTemp = Integer.parseInt(ovenTempView.getText().toString());
                Integer servings = Integer.parseInt(servingsView.getText().toString());
                Integer calories = Integer.parseInt(caloriesView.getText().toString());
                String instructions = instructionView.getText().toString();

                quantityViews.add((amountView.getText()).toString());
                quantityViews.add((amountView2.getText()).toString());

                ArrayList<Ingredient> ingredients = new ArrayList<>();

                // get ingredients
                for(int i = 0; i < ingredientSpinners.size(); i++){

                    Ingredient ing = new Ingredient();

                    ing.setName(ingredientSpinners.get(i).getSelectedItem().toString());
                    ing.setQuantity(quantityViews.get(i));
                    ing.setQuantityUnit(measurementSpinners.get(i).getSelectedItem().toString());

                    ingredients.add(ing);
                }

                if(valid) {

                    // execute new asynchronous save task
                    saveTask = new SaveTask(userEmail, recipeName, ingredients, category, prepTime
                                            , ovenTime, ovenTemp, servings, calories, instructions);
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
        ArrayList<Ingredient> ingredients;
        String ingredient;
        String primCategory;
        Integer prepTime, ovenTime, ovenTemp, servings, calories;
        String instructions;
        String uniqueID;
        String user;

        public SaveTask(String user, String rName, ArrayList<Ingredient> ingredients, String primCat, Integer pTime, Integer oTime,
                        Integer oTemp, Integer servings, Integer calories, String instruct){

            this.user = user;
            recipeName = rName;
            this.ingredients = ingredients;
            primCategory = primCat;
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
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
                    url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveRecipe");

                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();

                    // create unique id for this recipe
                    uniqueID = UUID.randomUUID().toString();
                    System.out.println("Unique ID generated: " + uniqueID);

                    System.out.println("Recipe data being passed: " + user + " " + recipeName + " " + ingredient + " " + primCategory);
                    System.out.println(" More data: " + prepTime + " " + ovenTime + " " + ovenTemp);

                    for(int x = 0; x < ingredients.size(); x++){

                        JSONObject jObject = new JSONObject();
                        jObject.put("ing_name", ingredients.get(x).getName());
                        jObject.put("quantity", ingredients.get(x).getQuantity());
                        jObject.put("quantity_unit", ingredients.get(x).getQuantityUnit());

                        jsonArray.put(jObject);
                    }

                    jsonObject.put("userEmail", user);
                    jsonObject.put("unique", uniqueID);
                    jsonObject.put("name", recipeName);
                    jsonObject.put("ingredientObjectArray", jsonArray);
                    jsonObject.put("primCategory", primCategory);
                    jsonObject.put("prepTime", prepTime);
                    jsonObject.put("ovenTime", ovenTime);
                    jsonObject.put("ovenTemp", ovenTemp);
                    jsonObject.put("servings", servings);
                    jsonObject.put("calories", calories);
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

                    System.out.println("connection established");

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

            String finalResult = ParseJSON.parseJSON(result);

            System.out.println("final result: " + finalResult);

            if(finalResult.equals("success")){

                System.out.println("Everything was stored successfully. ");

                //startActivity(new Intent(NewRecipe.this, MainActivity.class));
            }else{

                System.out.println("Storing of data was not successful");
            }
        } // end onPostExecute

    }

    public class GetItemsTask extends AsyncTask<String, Void, String>{

        ArrayList<String> listItems = new ArrayList<>();
        String itemIndicator;
        String userEmail;

        public GetItemsTask(String indicator, String email){
            itemIndicator = indicator;
            userEmail = email;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

        try{

            if(itemIndicator.equals("ing")){
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/RetrieveIngredients");
            }else if(itemIndicator.equals("cat")){
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/RetrieveCategories");
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", userEmail);
            String send = jsonObject.toString();

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(send.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            connection.connect();
            System.out.println("connection established");

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(send.getBytes());
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
                if(inputStream != null){
                    inputStream.close();
                }

                if(outputStream != null){
                    outputStream.close();
                }

            }catch(Exception ie){
                ie.printStackTrace();
            }
        }
            return result;
        }

        @Override
        protected void onPostExecute(String data){

            try{

                if(itemIndicator.equals("ing")){
                    listItems = ParseJSON.parseJSONArray(data, "ingredient");
                }else if(itemIndicator.equals("cat")){
                    listItems = ParseJSON.parseJSONArray(data, "category");
                }

                for(int x = 0; x < listItems.size(); x++ ){
                    System.out.println("Item: " + listItems.get(x));
                }

                if(itemIndicator.equals("ing")){
                    NewRecipe.this.onBackgroundTaskObtainedIngredients(listItems);
                }else if(itemIndicator.equals("cat")){
                    NewRecipe.this.onBackgroundTaskObtainedCategories(listItems);
                }

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
