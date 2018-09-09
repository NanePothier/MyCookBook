package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class ViewRecipe extends AppCompatActivity {

    enum Indicator{
        SHARE, DELETE
    }

    private String recipeName;
    private String userEmail;
    private String recipeId;
    private Recipe recipe;

    private TextView recipeNameView;
    private TextView primCategoryView;
    private TextView servingsView;
    private TextView preparationTimeView;
    private TextView ovenTimeView;
    private TextView ovenTempView;
    private TextView caloriesView;
    private TextView instructionsView;
    private TextView otherCategoryView;

    private TableLayout tableLayoutIngredients;
    private TableLayout tableLayoutCategories;

    private ArrayList<ConversionObject> conversionArray;

    private ToggleButton systemToggle;

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

        recipeNameView = (TextView) findViewById(R.id.recipe_name_view);
        primCategoryView = (TextView) findViewById(R.id.primCategory_view_view);
        servingsView = (TextView) findViewById(R.id.servings_view_view);
        preparationTimeView = (TextView) findViewById(R.id.prep_time_view);
        ovenTimeView = (TextView) findViewById(R.id.oven_time_view);
        ovenTempView = (TextView) findViewById(R.id.oven_temp_view);
        caloriesView = (TextView) findViewById(R.id.calories_view);
        instructionsView = (TextView) findViewById(R.id.instructions_view);
        otherCategoryView = (TextView) findViewById(R.id.other_cat_label_view);
        tableLayoutIngredients = (TableLayout) findViewById(R.id.table_layout_view);
        tableLayoutCategories = (TableLayout) findViewById(R.id.table_layout_view_categories);

        conversionArray = null;

        systemToggle = (ToggleButton) findViewById(R.id.toggle_sys_button);
        systemToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    useUSSystem();
                }else{
                    useMetricSystem();
                }
            }
        });

        // get recipe name and email through intent
        recipeName = "Fruit Salad";
        userEmail="haleyiron@gmail.com";
        recipeId = "3a39670a-0396-4827-a294-54b55a91dd84";

        retrieveRecipe();
    }

    public void useUSSystem(){

        // ensure it displays us
        setViews();
    }

    public void useMetricSystem(){

        // ensure it shows metric
        setViews();
    }

    public void generateMetricIngredientArray(){

        ArrayList<Ingredient> ingArray = recipe.getIngredientArray();
        ArrayList<Ingredient> metricArray = new ArrayList<>();
        String unit, fromUnit, toUnit, measCategory, defaultCat;


        for(int x = 0; x < ingArray.size(); x++){

            unit = ingArray.get(x).getQuantityUnit();
            // defaultCat = ingArray.get(x).getDefaultMeas()

            // when getting recipe and ingredients, also get default category for that ingredient
            // go through ingredient array and match unit and to desired unit and default cat
            // calculate
            // make a new ingredient object
            // add that object to metricArray

            for(int y = 0; y < conversionArray.size(); y++){

                fromUnit = conversionArray.get(y).getMeasureFrom();

                if(unit.equals(fromUnit)){

                    measCategory = conversionArray.get(y).getMeasureCategory();

                    // if(defaultCat.equals(measCategory))

                        // if equal to milliliter
                            //use factor

                }

            }

        }

        recipe.setMetricIngredients(metricArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.view_recipe_menu, menu);

        return true;
    }

    // method invoked by appbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            case R.id.edit_action:

                Intent intent = new Intent(ViewRecipe.this, NewRecipe.class);
                intent.putExtra("StatusIndicator", "EditRecipe");
                intent.putExtra("RecipeToEdit", recipe);
                startActivity(intent);

                return true;

            case R.id.share_action:

                // TODO: share recipe with other user

                return true;

            case R.id.delete_action:

                MenuTask deleteTask = new MenuTask(Indicator.DELETE, recipe.getRecipeId());
                deleteTask.execute((String) null);

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }

    public void retrieveRecipe(){

        GetRecipeTask task = new GetRecipeTask(userEmail, recipeName, recipeId);
        task.execute();
    }

    public void retrieveConversionFactors(){

        GetConversionFactors conversionTask = new GetConversionFactors();
        conversionTask.execute();
    }

    public void setViews(){

        recipeNameView.setText(recipe.getRecipeName());
        addIngredientsToTableView();

        // if there are other categories besides primary category
        if(recipe.getCategoriesArray().size() > 1){
            addCategoriesToTableView();
        }

        // set other views
        primCategoryView.setText(recipe.getPrimaryCategory().getName());
        servingsView.setText(Integer.toString(recipe.getServings()));
        preparationTimeView.setText(Integer.toString(recipe.getPreparationTime()));
        ovenTimeView.setText(Integer.toString(recipe.getOvenTime()));
        ovenTempView.setText(Integer.toString(recipe.getOvenTemperature()));
        caloriesView.setText(Integer.toString(recipe.getCalories()));
        instructionsView.setText(recipe.getInstructions());
    }

    public void addIngredientsToTableView(){

        ArrayList<Ingredient> arrayIngredients = recipe.getIngredientArray();
        int count = 1;
        TextView ingredientNameCol, quantityCol, quantityUnitCol, countCol;
        TableRow tableRow;

        // for each ingredient object create new row with three columns and add row to table
        for(int x = 0; x < arrayIngredients.size(); x++){

            tableRow = new TableRow(this);
            tableRow.setId(count);
            tableRow.setPadding(5, 5, 5, 5);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            countCol = new TextView(this);
            countCol.setText(count + ".");
            countCol.setTextSize(15);
            countCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
            tableRow.addView(countCol);

            ingredientNameCol = new TextView(this);
            ingredientNameCol.setText(arrayIngredients.get(x).getName());
            ingredientNameCol.setTextSize(15);
            ingredientNameCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
            tableRow.addView(ingredientNameCol);

            quantityCol = new TextView(this);
            quantityCol.setText(arrayIngredients.get(x).getQuantity());
            quantityCol.setTextSize(15);
            quantityCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
            tableRow.addView(quantityCol);

            quantityUnitCol = new TextView(this);
            quantityUnitCol.setText(arrayIngredients.get(x).getQuantityUnit());
            quantityUnitCol.setTextSize(15);
            quantityUnitCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.4f));
            tableRow.addView(quantityUnitCol);

            tableLayoutIngredients.addView(tableRow);
            count++;
        }
    }

    public void addCategoriesToTableView(){

        otherCategoryView.setVisibility(View.VISIBLE);
        tableLayoutCategories.setVisibility(View.VISIBLE);
        ArrayList<Category> arrayCategories = recipe.getCategoriesArray();
        int count = 1;
        TableRow tableRowCat;
        TextView catNameCol;

        for(int y = 0; y < arrayCategories.size(); y++) {

            // if current category object is not the primary category create new row and add to table
            if (!(arrayCategories.get(y).getCategory())) {

                tableRowCat = new TableRow(this);
                tableRowCat.setId(count);
                tableRowCat.setPadding(5, 5, 5, 5);
                tableRowCat.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                catNameCol = new TextView(this);
                catNameCol.setText(arrayCategories.get(y).getName());
                catNameCol.setTextSize(15);
                catNameCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                tableRowCat.addView(catNameCol);

                tableLayoutCategories.addView(tableRowCat);
            }
        }
    }

    // receive recipe object obtained from database by asynchronous background task
    public void onBackgroundTaskObtainedRecipe(Recipe r){
        recipe = r;
        setViews();
        retrieveConversionFactors();
    }

    public void onBackgroundTaskObtainedConversionFactors(ArrayList<ConversionObject> convArray){

        conversionArray = new ArrayList<>();
        conversionArray = convArray;

        generateMetricIngredientArray();
    }

    // asynchronous background task getting recipe data from database
    public class GetRecipeTask extends AsyncTask<String, Void, String> {

        String recipeName;
        String userEmail;
        String data;
        String recipeId;

        public GetRecipeTask(String user, String recipe, String id){

            userEmail = user;
            recipeName = recipe;
            recipeId = id;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{

                url = new URL("http://10.0.0.18:9999/mycookbookservlets/RetrieveRecipe");

                // generate json object to pass data
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("email", userEmail);
                jsonObject.put("recipe", recipeName);
                jsonObject.put("id", recipeId);

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

                connection.connect();

                System.out.println("Connection established");

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

                Recipe recipe = ParseJSON.parseJSONRecipe(data, recipeName, recipeId);
                ViewRecipe.this.onBackgroundTaskObtainedRecipe(recipe);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class MenuTask extends AsyncTask<String, Void, String> {

        Indicator indicator;
        String recipeId;

        public MenuTask(Indicator in, String id){
            indicator = in;
            recipeId = id;
        }

        @Override
        protected String doInBackground(String... params){

            String result = "";

            if(indicator.equals(Indicator.DELETE)) {

                String recipeInfo;
                InputStream inputStream = null;
                OutputStream outputStream = null;
                HttpURLConnection connection;
                URL url = null;

                try {
                    url = new URL("http://10.0.0.18:9999/mycookbookservlets/DeleteRecipe");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("recipeId", recipeId);
                    recipeInfo = jsonObject.toString();

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(recipeInfo.getBytes().length);
                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                    connection.connect();
                    System.out.println("Connection established");

                    outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(recipeInfo.getBytes());
                    outputStream.flush();

                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {

                        inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;

                        while ((line = reader.readLine()) != null) {
                            result += line;
                        }
                    }

                } catch (Exception ioe) {
                    ioe.printStackTrace();
                } finally {

                    try {
                        outputStream.close();
                        inputStream.close();

                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String data){

            if(!(data.equals(""))){

                String finalResult = ParseJSON.parseJSON(data);
                System.out.println("in result string: " + finalResult);

                if(indicator.equals(Indicator.DELETE)){
                    if(finalResult.equals("deletedFirstSecondThirdFourth")){

                        Snackbar.make(findViewById(R.id.view_recipe_coordinator_layout), R.string.delete_user_msg, Snackbar.LENGTH_SHORT)
                                .show();

                        startActivity(new Intent(ViewRecipe.this, Cookbook.class));
                    }else{

                    }
                }else if(indicator.equals(Indicator.SHARE)){
                    if(finalResult.equals("shared")){

                        Snackbar.make(findViewById(R.id.view_recipe_coordinator_layout), R.string.share_user_msg, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }

            }

        }
    }

    public class GetConversionFactors extends AsyncTask<String, Void, String>{

        @Override
        public String doInBackground(String... params){

            String result = "";
            String info;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;

            try {
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/GetConversionFactors");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("retrieval", "retrieval");
                info = jsonObject.toString();

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(info.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                connection.connect();
                System.out.println("Connection established");

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(info.getBytes());
                outputStream.flush();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }
                }

            } catch (Exception ioe) {
                ioe.printStackTrace();
            } finally {

                try {
                    outputStream.close();
                    inputStream.close();

                } catch (IOException ie) {
                    ie.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String data){

            ArrayList<ConversionObject> array = ParseJSON.parseJSONConversionArray(data);
            onBackgroundTaskObtainedConversionFactors(array);
        }
    }
}
