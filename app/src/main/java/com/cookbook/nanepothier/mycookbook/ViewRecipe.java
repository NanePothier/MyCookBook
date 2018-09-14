package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
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
    private ImageButton backButton;

    private CoordinatorLayout coordinatorLayout;
    private PopupWindow sharePopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.view_recipe_coordinator_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.view_recipe_toolbar);
        setSupportActionBar(toolbar);
        backButton = (ImageButton) toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewRecipe.this, Cookbook.class);
                startActivity(intent);
            }
        });

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

        // get recipe name, email and recipe Id through intent
        /*
        Intent intentReceived = getIntent();
        recipeName = intentReceived.getStringExtra("recipe_name");
        userEmail = intentReceived.getStringExtra("user_email");
        recipeId = intentReceived.getStringExtra("recipe_id");

        System.out.println("Recipe id in view activity: " + recipeId);
        */


        recipeName = "Strawberry Cake";
        userEmail="haleyiron@gmail.com";
        recipeId = "fb5f13f0-8ea3-48ba-ad34-8c5bd48bf4ec";

        retrieveRecipe();
    }

    public void useUSSystem(){

        // ensure it displays us
        setViews("us");
    }

    public void useMetricSystem(){

        // ensure it shows metric
        setViews("metric");
    }

    public void generateMetricIngredientArray(){

        ArrayList<Ingredient> ingArray = recipe.getIngredientArray();
        ArrayList<Ingredient> metricArray = new ArrayList<>();
        String unit, fromUnit, toUnit, measCategory, defaultMeas;
        Ingredient metricIngredient;
        double metricTemperature;

        // convert temperature
        metricTemperature = (recipe.getOvenTemperature() - 32) * (5.0/9.0);
        recipe.setMetricTemperature((int) metricTemperature);

        // for each ingredient
        for(int x = 0; x < ingArray.size(); x++){

            unit = ingArray.get(x).getQuantityUnit();
            defaultMeas = ingArray.get(x).getDefaultMeasurement();

            // find match by looping through conversion array
            for(int y = 0; y < conversionArray.size(); y++){

                fromUnit = conversionArray.get(y).getMeasureFrom();

                // match 'from' unit
                if(unit.equals(fromUnit) && !(unit.equals("tablespoon")) && !(unit.equals("teaspoon"))){

                    measCategory = conversionArray.get(y).getMeasureCategory();

                    // match default measurement category (solid or liquid)
                    if(defaultMeas.equals(measCategory)){

                        double convertedNumber;
                        toUnit = conversionArray.get(y).getMeasureTo();

                        // determine which metric measurement to convert to since there may be more than one option
                        // example: could convert to gramm or kilogramm (both are metric and weight measurements)
                        if(toUnit.equals("milliliter")){

                            metricIngredient = new Ingredient();

                            convertedNumber = ingArray.get(x).getQuantity() * conversionArray.get(y).getFactor();

                            metricIngredient.setQuantity((int)convertedNumber);
                            metricIngredient.setQuantityUnit("ml");
                            metricIngredient.setName(ingArray.get(x).getName());
                            metricIngredient.setDefaultMeasurement(ingArray.get(x).getDefaultMeasurement());

                            metricArray.add(metricIngredient);

                        }else if(toUnit.equals("gramm")){

                            metricIngredient = new Ingredient();

                            convertedNumber = ingArray.get(x).getQuantity() * conversionArray.get(y).getFactor();

                            metricIngredient.setQuantity((int) convertedNumber);
                            metricIngredient.setName(ingArray.get(x).getName());
                            metricIngredient.setDefaultMeasurement(ingArray.get(x).getDefaultMeasurement());
                            metricIngredient.setQuantityUnit("g");

                            metricArray.add(metricIngredient);
                        }
                    }
                }

                if(unit.equals("tablespoon") || unit.equals("teaspoon")){

                    metricIngredient = ingArray.get(x);
                    metricArray.add(metricIngredient);
                }
            }
        }

        // add metric ingredient array to the recipe object
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

                LayoutInflater inflater = (LayoutInflater) ViewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View sharePopupView = inflater.inflate(R.layout.share_recipe_popup, null);

                ImageButton shareButton = (ImageButton) sharePopupView.findViewById(R.id.share_recipe_button);
                ImageButton cancelButton = (ImageButton) sharePopupView.findViewById(R.id.cancel_image_button);
                final EditText enterEmailView = (EditText) sharePopupView.findViewById(R.id.enter_item_view);

                sharePopup = new PopupWindow(sharePopupView, 1200, 900, true);
                sharePopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MenuTask shareTask = new MenuTask(Indicator.SHARE, recipe.getRecipeId(), enterEmailView.getText().toString());
                        shareTask.execute();
                        sharePopup.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharePopup.dismiss();
                    }
                });

                return true;

            case R.id.delete_action:

                MenuTask deleteTask = new MenuTask(Indicator.DELETE, recipe.getRecipeId(), "");
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

    public void setViews(String system){

        recipeNameView.setText(recipe.getRecipeName());
        addIngredientsToTableView(system);

        // if there are other categories besides primary category
        if(recipe.getCategoriesArray().size() > 1){
            addCategoriesToTableView();
        }

        // set other views
        primCategoryView.setText(recipe.getPrimaryCategory().getName());

        if(recipe.getServings() != -1){
            servingsView.setText(Integer.toString(recipe.getServings()));
        }

        if(recipe.getPreparationTime() != -1){
            preparationTimeView.setText(Integer.toString(recipe.getPreparationTime()));
        }

        if(recipe.getOvenTime() != -1){
            ovenTimeView.setText(Integer.toString(recipe.getOvenTime()));
        }

        if(recipe.getOvenTemperature() != -1){
            if(system.equals("us")){
                ovenTempView.setText(Integer.toString(recipe.getOvenTemperature()));
            }else{
                ovenTempView.setText(Integer.toString(recipe.getMetricTemperature()));
            }
        }

        if(recipe.getCalories() != -1){
            caloriesView.setText(Integer.toString(recipe.getCalories()));
        }

        if(!(recipe.getInstructions().equals("none"))){
            instructionsView.setText(recipe.getInstructions());
        }
    }

    public void addIngredientsToTableView(String measurementSystem){

        ArrayList<Ingredient> arrayIngredients;

        if(measurementSystem.equals("metric")){
            arrayIngredients = recipe.getMetricIngredientArray();
            // System.out.println("One of the metric ingredients " + recipe.getMetricIngredientArray().get(0).getName() + " " + recipe.getMetricIngredientArray().get(0).getQuantity());
        }else{
            arrayIngredients = recipe.getIngredientArray();
        }

        tableLayoutIngredients.removeAllViews();

        int count = 1;
        TextView ingredientNameCol, quantityCol, quantityUnitCol, countCol;
        TableRow tableRow;

        // for each ingredient object create new row with three columns and add row to table
        for(int x = 0; x < arrayIngredients.size(); x++){

            tableRow = new TableRow(this);
            tableRow.setPadding(15, 10, 10, 10);
            tableRow.setBackgroundResource(R.drawable.thin_black_border_background);
            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 150));

            countCol = new TextView(this);
            countCol.setText(Integer.toString(count) + ".");
            countCol.setTextSize(15);
            countCol.setLayoutParams(new TableRow.LayoutParams(85, TableRow.LayoutParams.WRAP_CONTENT));
            tableRow.addView(countCol);

            ingredientNameCol = new TextView(this);
            ingredientNameCol.setText(arrayIngredients.get(x).getName());
            ingredientNameCol.setTextSize(15);
            ingredientNameCol.setLayoutParams(new TableRow.LayoutParams(850, TableRow.LayoutParams.WRAP_CONTENT));
            tableRow.addView(ingredientNameCol);


            quantityCol = new TextView(this);
            quantityCol.setTextSize(15);
            quantityCol.setLayoutParams(new TableRow.LayoutParams(70, TableRow.LayoutParams.WRAP_CONTENT));

            if(arrayIngredients.get(x).getQuantity() == -1){
                quantityCol.setText(" ");
            }else{
                quantityCol.setText(Integer.toString(arrayIngredients.get(x).getQuantity()));
            }
            tableRow.addView(quantityCol);

            quantityUnitCol = new TextView(this);
            quantityUnitCol.setTextSize(15);
            quantityUnitCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.4f));

            if(arrayIngredients.get(x).getQuantityUnit().equals(" ") || arrayIngredients.get(x).getQuantity() == -1){
                quantityUnitCol.setText(" ");
            }else{
                quantityUnitCol.setText(arrayIngredients.get(x).getQuantityUnit());
            }
            tableRow.addView(quantityUnitCol);

            tableLayoutIngredients.addView(tableRow);
            tableLayoutIngredients.setPadding(10, 10, 10, 10);
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
        setViews("us");
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
        String shareEmail;

        public MenuTask(Indicator in, String id, String email){
            indicator = in;
            recipeId = id;
            shareEmail = email;
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
            }else if(indicator.equals(Indicator.SHARE)){

                String recipeInfo;
                InputStream inputStream = null;
                OutputStream outputStream = null;
                HttpURLConnection connection;
                URL url = null;

                try {
                    url = new URL("http://10.0.0.18:9999/mycookbookservlets/ShareRecipe");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("recipeId", recipeId);
                    jsonObject.put("email", shareEmail);
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

                System.out.println("Get conversion factors result: " + result);

            } catch (Exception ioe) {
                ioe.printStackTrace();
            } finally {

                try {
                    if(outputStream != null){
                        outputStream.close();
                    }
                    if(inputStream != null){
                        inputStream.close();
                    }
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
