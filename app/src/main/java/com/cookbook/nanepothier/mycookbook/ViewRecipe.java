package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class ViewRecipe extends AppCompatActivity {

    enum Indicator{
        SHARE, DELETE, SAVE_SHARED
    }

    private String recipeName;
    private String userEmail;
    private String recipeId;
    private Recipe recipe;
    private String shareEmail;

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
    private PopupWindow infoPopup;
    private View progressView;
    private View scrollView;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recipe);

        // get recipe name, email and recipe Id through intent
        Intent intentReceived = getIntent();
        recipeName = intentReceived.getStringExtra("recipe_name");
        userEmail = intentReceived.getStringExtra("user_email");
        recipeId = intentReceived.getStringExtra("recipe_id");
        shareEmail = "";

        /*
        recipeName = "Strawberry Cake";
        userEmail="haleyiron@gmail.com";
        recipeId = "fb5f13f0-8ea3-48ba-ad34-8c5bd48bf4ec";
        */

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.view_recipe_coordinator_layout);
        inflater = (LayoutInflater) ViewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.view_recipe_toolbar);
        setSupportActionBar(toolbar);
        backButton = (ImageButton) toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewRecipe.this, Cookbook.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("action", "back_button");
                startActivity(intent);
            }
        });

        scrollView = findViewById(R.id.view_recipe_scroll);
        scrollView.setVisibility(View.GONE);
        progressView = findViewById(R.id.view_recipe_progress);
        progressView.setVisibility(View.VISIBLE);

        FloatingActionButton editfab = (FloatingActionButton) findViewById(R.id.edit_fab);
        editfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ViewRecipe.this, NewRecipe.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("status_indicator", "EditRecipe");
                intent.putExtra("recipe_to_edit", recipe);
                startActivity(intent);
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
        if(recipe.getOvenTemperature() > 50){
            metricTemperature = (recipe.getOvenTemperature() - 32) * (5.0/9.0);
        }else{
            metricTemperature = 0;
        }
        recipe.setMetricTemperature((int) metricTemperature);

        // for each ingredient
        for(int x = 0; x < ingArray.size(); x++){

            unit = ingArray.get(x).getQuantityUnit();
            defaultMeas = ingArray.get(x).getDefaultMeasurement();

            if(ingArray.get(x).getQuantity() != -1 && !unit.equals("ct")){

                // find match by looping through conversion array
                for(int y = 0; y < conversionArray.size(); y++){

                    fromUnit = conversionArray.get(y).getMeasureFrom();

                    // match 'from' unit
                    if(unit.equals(fromUnit) && !(unit.equals("tablespoon")) && !(unit.equals("teaspoon"))){

                        measCategory = conversionArray.get(y).getMeasureCategory();
                        double convertedNumber;

                        // match default measurement category (solid or liquid)
                        if(defaultMeas.equals(measCategory)){

                            toUnit = conversionArray.get(y).getMeasureTo();

                            // determine which metric measurement to convert to since there may be more than one option
                            // example: could convert to gramm or kilogramm (both are metric and weight measurements)
                            if(toUnit.equals("milliliter")){

                                metricIngredient = new Ingredient();

                                convertedNumber = ingArray.get(x).getQuantity() * conversionArray.get(y).getFactor();

                                metricIngredient.setQuantity(convertedNumber);
                                metricIngredient.setQuantityUnit("ml");
                                metricIngredient.setName(ingArray.get(x).getName());
                                metricIngredient.setDefaultMeasurement(ingArray.get(x).getDefaultMeasurement());

                                metricArray.add(metricIngredient);

                            }else if(toUnit.equals("gramm")){

                                metricIngredient = new Ingredient();

                                convertedNumber = ingArray.get(x).getQuantity() * conversionArray.get(y).getFactor();

                                metricIngredient.setQuantity(convertedNumber);
                                metricIngredient.setName(ingArray.get(x).getName());
                                metricIngredient.setDefaultMeasurement(ingArray.get(x).getDefaultMeasurement());
                                metricIngredient.setQuantityUnit("g");

                                metricArray.add(metricIngredient);
                            }
                        }

                        if(unit.equals("cup") && ingArray.get(x).getDefaultMeasurement().equals("w")){

                            metricIngredient = new Ingredient();

                            // convert from cup to ml
                            convertedNumber = ingArray.get(x).getQuantity() * conversionArray.get(y).getFactor();

                            metricIngredient.setQuantity(convertedNumber);
                            metricIngredient.setQuantityUnit("g");
                            metricIngredient.setName(ingArray.get(x).getName());
                            metricIngredient.setDefaultMeasurement(ingArray.get(x).getDefaultMeasurement());

                            metricArray.add(metricIngredient);
                        }
                    }

                    if(unit.equals("tablespoon") || unit.equals("teaspoon")){

                        metricIngredient = ingArray.get(x);
                        metricArray.add(metricIngredient);
                    }
                }

            }else{
                metricIngredient = ingArray.get(x);
                metricArray.add(metricIngredient);
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
                intent.putExtra("user_email", userEmail);
                intent.putExtra("status_indicator", "EditRecipe");
                intent.putExtra("recipe_to_edit", recipe);
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

                        shareEmail = enterEmailView.getText().toString();

                        MenuTask saveRecipeTask = new MenuTask(Indicator.SAVE_SHARED, shareEmail, userEmail, recipe.getRecipeName(), recipe.getIngredientArray(),
                                Integer.toString(recipe.getPreparationTime()), Integer.toString(recipe.getOvenTime()),
                                Integer.toString(recipe.getOvenTemperature()), Integer.toString(recipe.getServings()), Integer.toString(recipe.getCalories()),
                                recipe.getInstructions(), "US", "NewRecipe");

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                            saveRecipeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
                        }else{
                            saveRecipeTask.execute((String) null);
                        }

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

                MenuTask deleteTask = new MenuTask(Indicator.DELETE, recipe.getRecipeId());
                deleteTask.execute((String) null);

                return true;

            case R.id.action_info:

                LayoutInflater inflater4 = (LayoutInflater) ViewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View infoPopupView = inflater4.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.view_info_title);
                infoText.setText(R.string.view_info);
                infoText2.setText(R.string.view_info_toggle);
                infoText3.setText(R.string.view_info_toolbar);
                infoText4.setVisibility(View.GONE);

                infoPopup = new PopupWindow(infoPopupView, 1200, 1200, true);
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
        }else{
            servingsView.setText("-");
        }

        if(recipe.getPreparationTime() != -1){
            preparationTimeView.setText(Integer.toString(recipe.getPreparationTime()));
        }else{
            preparationTimeView.setText("-");
        }

        if(recipe.getOvenTime() != -1){
            ovenTimeView.setText(Integer.toString(recipe.getOvenTime()));
        }else{
            ovenTimeView.setText("-");
        }

        if(recipe.getOvenTemperature() != -1){
            if(system.equals("us")){
                ovenTempView.setText(Integer.toString(recipe.getOvenTemperature()));
            }else{
                ovenTempView.setText(Integer.toString(recipe.getMetricTemperature()));
            }
        }else{
            ovenTempView.setText("-");
        }

        if(recipe.getCalories() != -1){
            caloriesView.setText(Integer.toString(recipe.getCalories()));
        }else{
            caloriesView.setText("-");
        }

        if(!(recipe.getInstructions().equals("none"))){
            instructionsView.setText(recipe.getInstructions());
        }else{
            instructionsView.setText("No instructions available");
        }
    }

    public void addIngredientsToTableView(String measurementSystem){

        ArrayList<Ingredient> arrayIngredients;

        if(measurementSystem.equals("metric")){
            arrayIngredients = recipe.getMetricIngredientArray();
        }else{
            arrayIngredients = recipe.getIngredientArray();
        }

        tableLayoutIngredients.removeAllViews();

        View ingredientRowView;
        int count = 1;
        TextView ingredientNameCol, quantityCol, quantityUnitCol, countCol;
        TableRow tableRow;

        // for each ingredient object create new row with three columns and add row to table
        for(int x = 0; x < arrayIngredients.size(); x++){

            ingredientRowView = inflater.inflate(R.layout.view_ingredient_row, null);
            tableRow = ingredientRowView.findViewById(R.id.new_row);

            countCol = ingredientRowView.findViewById(R.id.count_text_view);
            countCol.setText(Integer.toString(count) + ".");

            ingredientNameCol = ingredientRowView.findViewById(R.id.ingredient_text_view);
            ingredientNameCol.setText(" " + arrayIngredients.get(x).getName());

            quantityCol = ingredientRowView.findViewById(R.id.quantity_text_view);
            double qu = Math.round(arrayIngredients.get(x).getQuantity() * 10)/10.0;
            arrayIngredients.get(x).setQuantity(qu);

            if(arrayIngredients.get(x).getQuantity() == -1.0){
                quantityCol.setText("");
            }else{
                quantityCol.setText(Double.toString(arrayIngredients.get(x).getQuantity()));
            }

            quantityUnitCol = ingredientRowView.findViewById(R.id.quantity_unit_view);

            if(arrayIngredients.get(x).getQuantityUnit().equals(" ") || arrayIngredients.get(x).getQuantity() == -1){
                quantityUnitCol.setText(" ");
            }else{
                quantityUnitCol.setText(arrayIngredients.get(x).getQuantityUnit());
            }

            if(tableRow.getParent() != null){
                ((ViewGroup)tableRow.getParent()).removeView(tableRow);
            }

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
            if (!(arrayCategories.get(y).isPrimaryCategory())) {

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

        progressView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    public void onBackgroundTaskObtainedRecipeId(String uniqueID){

        MenuTask shareTask = new MenuTask(Indicator.SHARE, uniqueID, shareEmail, userEmail);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            shareTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            shareTask.execute((String) null);
        }
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

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(data.getBytes());
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
        String sharedByEmail;
        String recipeName;
        ArrayList<Ingredient> ingredients;
        ArrayList<Category> categories;
        String primCategory;
        String prepTime, ovenTime, ovenTemp, servings, calories;
        String instructions;
        String systemIndicator;
        String actionIndicator;
        String uniqueID;

        // constructor for saving a copy of a shared recipe with new recipe Id
        public MenuTask(Indicator in, String shareEmail, String sharedByEmail, String recipeName, ArrayList<Ingredient> ingredients,
                        String pTime, String oTime, String oTemp,
                        String servings, String calories, String instruct, String sysInd, String actInd){

            indicator = in;
            this.shareEmail = shareEmail;
            this.sharedByEmail = sharedByEmail;
            this.recipeName = recipeName;
            this.ingredients = ingredients;
            this.categories = new ArrayList<>();
            primCategory = "Under Review";
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
            instructions = instruct;
            systemIndicator = sysInd;
            actionIndicator = actInd;
        }

        // constructor for saving shared recipe information
        public MenuTask(Indicator in, String recipeId, String shareEmail, String sharedByEmail){

            indicator = in;
            this.recipeId = recipeId;
            this.shareEmail = shareEmail;
            this.sharedByEmail = sharedByEmail;
        }

        // constructor for deleting a recipe
        public MenuTask(Indicator in, String id){
            indicator = in;
            recipeId = id;

            shareEmail = "";
            sharedByEmail = "no_sharing";
        }

        @Override
        protected String doInBackground(String... params){

            String result = "";
            String recipeInfo;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;
            URL url = null;

            if(indicator.equals(Indicator.DELETE)) {

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
            }else if(indicator.equals(Indicator.SAVE_SHARED)){

                try {
                    if(!shareEmail.equals(sharedByEmail)){

                        url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveRecipe");

                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        JSONArray jsonArrayCat = new JSONArray();

                        uniqueID = UUID.randomUUID().toString();

                        for(int x = 0; x < ingredients.size(); x++){

                            JSONObject jObject = new JSONObject();
                            jObject.put("ing_name", ingredients.get(x).getName());
                            jObject.put("quantity", ingredients.get(x).getQuantity());
                            jObject.put("quantity_unit", ingredients.get(x).getQuantityUnit());

                            jsonArray.put(jObject);
                        }

                        for(int y = 0; y < categories.size(); y++){

                            JSONObject catObject = new JSONObject();
                            catObject.put("cat_name", categories.get(y).getName());
                            catObject.put("cat_prime", categories.get(y).isPrimaryCategory());

                            jsonArrayCat.put(catObject);
                        }

                        jsonObject.put("userEmail", shareEmail);
                        jsonObject.put("unique", uniqueID);
                        jsonObject.put("name", recipeName);
                        jsonObject.put("ingredientObjectArray", jsonArray);
                        jsonObject.put("primCategory", primCategory);
                        jsonObject.put("other_categories", jsonArrayCat);
                        jsonObject.put("prepTime", prepTime);
                        jsonObject.put("ovenTime", ovenTime);
                        jsonObject.put("ovenTemp", ovenTemp);
                        jsonObject.put("servings", servings);
                        jsonObject.put("calories", calories);
                        jsonObject.put("instructions", instructions);
                        jsonObject.put("systemInd", systemIndicator);
                        jsonObject.put("actionInd", actionIndicator);

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
                    }
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if(outputStream != null){
                            outputStream.close();
                        }

                        if(inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            } else if(indicator.equals(Indicator.SHARE)){

                try {
                    if(!shareEmail.equals(sharedByEmail)){

                        url = new URL("http://10.0.0.18:9999/mycookbookservlets/ShareRecipe");

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("recipe_id", recipeId);
                        jsonObject.put("user_email", shareEmail);
                        jsonObject.put("shared_by_email", sharedByEmail);
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
                    }
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if(outputStream != null){
                            outputStream.close();
                        }
                        if(inputStream != null) {
                            inputStream.close();
                        }
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

                if(indicator.equals(Indicator.DELETE)){

                    if(finalResult.equals("deletedDDDD")){

                        // return to cookbook activity once recipe has been deleted
                        Intent intent = new Intent(ViewRecipe.this, Cookbook.class);
                        intent.putExtra("user_email", userEmail);
                        intent.putExtra("action", "deleted_recipe");
                        startActivity(intent);
                    }
                }else if (indicator.equals(Indicator.SAVE_SHARED)){

                    // send new recipe Id back to main UI thread so shared recipe connection can be stored next with new Id
                    ViewRecipe.this.onBackgroundTaskObtainedRecipeId(uniqueID);

                }else if(indicator.equals(Indicator.SHARE)){

                    if(finalResult.equals("success")){

                        Snackbar.make(findViewById(R.id.view_recipe_coordinator_layout), R.string.share_user_msg, Snackbar.LENGTH_SHORT)
                                .show();
                    }else if(finalResult.equals("exists")){
                        Snackbar.make(findViewById(R.id.view_recipe_coordinator_layout), R.string.not_shared_msg, Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
            }

            // inform user that he cannot share a recipe with himself
            if(shareEmail.equals(sharedByEmail)){
                Snackbar.make(findViewById(R.id.view_recipe_coordinator_layout), "Cannot share recipe with yourself", Snackbar.LENGTH_LONG).show();
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
