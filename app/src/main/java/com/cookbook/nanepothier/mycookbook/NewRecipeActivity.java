package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class NewRecipeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String userEmail = "haleyiron@gmail.com";
    private String statusIndicator = "NewRecipe";

    // views
    private EditText recipeNameView;
    private EditText primCategoryView;
    private EditText servingsView;
    private EditText prepTimeView;
    private EditText ovenTimeView;
    private EditText ovenTempView;
    private EditText caloriesView;
    private EditText instructionView;
    private EditText amountView, amountView2, amountView3;

    private AutoCompleteTextView autoCompIngredient1;
    private AutoCompleteTextView autoCompIngredient2;
    private AutoCompleteTextView autoCompIngredient3;

    private SaveTask saveTask;
    private GetItemsTask ingTask;
    private GetItemsTask catTask;

    private Recipe recipe;
    private Intent intentReceived;

    private Spinner spinnerMeasurements;
    private Spinner spinnerMeasurements2;
    private Spinner spinnerMeasurements3;
    private Spinner categorySpinner;

    // array lists
    private ArrayList<String> USMeasurements;
    private ArrayList<String> MetricMeasurements;
    public static ArrayList<String> listIngredients;
    private ArrayList<AutoCompleteTextView> ingredientViews;
    private ArrayList<Spinner> measurementSpinners;
    private ArrayList<EditText> quantityViews;
    private ArrayList<AutoCompleteTextView> additionalCategories;
    private ArrayList<String> listCategories;

    private ArrayAdapter<String> ingredientAdapter;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> measurementAdapter;

    // used for setting views when in edit mode
    private ArrayList<Ingredient> ingredientArray;
    private ArrayList<Category> categoryArray;
    private ArrayList<TextView> ingCountArray;
    private ArrayList<TextView> catCountArray;

    private TableLayout tableLayoutIngredients;
    private TableLayout tableLayoutCategories;

    private ImageView addIngredientImageView;
    private ImageView addCategoryImageView;

    // variables used for toggling between us and metric system
    private ToggleButton systemToggle;
    private String systemIndicator;
    private TextView systemTextView;

    private PopupWindow ingredientPopup;
    private PopupWindow categoryPopup;
    private PopupWindow deleteCatPopup;
    private PopupWindow infoPopup;
    private Context context;
    private View progressView;
    private View scrollView;

    private LayoutInflater inflater;

    // new ingredient popup window
    CoordinatorLayout coordinatorLayout;

    // toolbar
    private ImageButton backButton;

    private boolean firstTime;
    private boolean firstTimeCategory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        // get data passed to this activity
        intentReceived = getIntent();
        //statusIndicator = "NewRecipe";
        userEmail = intentReceived.getExtras().getString("user_email");
        statusIndicator = intentReceived.getExtras().getString("status_indicator");

        ViewCountService.setIngredientViewCount(3);
        ViewCountService.setCategoryViewCount(0);

        context = getApplicationContext();
        coordinatorLayout = findViewById(R.id.new_recipe_activity_layout);
        progressView = findViewById(R.id.new_recipe_progress);
        scrollView = findViewById(R.id.new_recipe_scroll_view);

        inflater = (LayoutInflater) NewRecipeActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Toolbar toolbar = findViewById(R.id.newrecipe_toolbar);
        setSupportActionBar(toolbar);
        backButton = toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusIndicator.equals("NewRecipe")){
                    Intent intent = new Intent(NewRecipeActivity.this, MainActivity.class);
                    intent.putExtra("user_email", userEmail);
                    intent.putExtra("action", "back_new_recipe");
                    startActivity(intent);
                }else if(statusIndicator.equals("EditRecipe")) {
                    Intent intent = new Intent(NewRecipeActivity.this, ViewRecipeActivity.class);
                    intent.putExtra("user_email", userEmail);
                    intent.putExtra("action", "back_edit_recipe");
                    intent.putExtra("recipe_id", recipe.getRecipeId());
                    intent.putExtra("recipe_name", recipe.getRecipeName());
                    startActivity(intent);
                }
            }
        });

        // ArrayLists
        listIngredients = new ArrayList<>();
        listCategories = new ArrayList<>();
        USMeasurements = new ArrayList<>();
        MetricMeasurements = new ArrayList<>();
        measurementSpinners = new ArrayList<>();
        ingredientViews = new ArrayList<>();
        quantityViews = new ArrayList<>();
        ingredientArray = new ArrayList<>();
        categoryArray = new ArrayList<>();
        additionalCategories = new ArrayList<>();
        ingCountArray = new ArrayList<>();
        catCountArray = new ArrayList<>();

        // EditTexts
        recipeNameView = findViewById(R.id.recipe_name);
        servingsView = findViewById(R.id.servings);
        prepTimeView = findViewById(R.id.prep_time);
        ovenTimeView = findViewById(R.id.oven_time);
        ovenTempView = findViewById(R.id.oven_temp);
        caloriesView = findViewById(R.id.calories);
        instructionView = findViewById(R.id.editText);

        // ingredient views
        autoCompIngredient1 = findViewById(R.id.auto_complete_view1);
        autoCompIngredient2 = findViewById(R.id.auto_complete_view2);
        autoCompIngredient3 = findViewById(R.id.auto_complete_view3);

        // add existing ingredient views to array
        ingredientViews.add(autoCompIngredient1);
        ingredientViews.add(autoCompIngredient2);
        ingredientViews.add(autoCompIngredient3);

        // quantity views
        amountView = (EditText) findViewById(R.id.quantity1);
        amountView2 = (EditText) findViewById(R.id.quantity2);
        amountView3 = (EditText) findViewById(R.id.quantity3);

        // add existing quantity views to array
        quantityViews.add(amountView);
        quantityViews.add(amountView2);
        quantityViews.add(amountView3);

        // quantity unit views
        spinnerMeasurements = (Spinner) findViewById(R.id.measurement1);
        spinnerMeasurements2 = (Spinner) findViewById(R.id.measurement2);
        spinnerMeasurements3 = (Spinner) findViewById(R.id.measurement3);

        // set listeners on quantity unit spinners
        spinnerMeasurements.setOnItemSelectedListener(this);
        spinnerMeasurements2.setOnItemSelectedListener(this);
        spinnerMeasurements3.setOnItemSelectedListener(this);

        // add existing quantity unit spinners to array
        measurementSpinners.add(spinnerMeasurements);
        measurementSpinners.add(spinnerMeasurements2);
        measurementSpinners.add(spinnerMeasurements3);

        firstTime = true;
        firstTimeCategory = true;

        createUSMeasurementList();
        createMetricMeasurementList();
        setSpinners(measurementSpinners, USMeasurements);

        categorySpinner = (Spinner) findViewById(R.id.category_spinner);

        // US is default measurement system
        systemTextView = (TextView) findViewById(R.id.system_text);
        systemIndicator = "US";
        systemToggle = (ToggleButton) findViewById(R.id.toggle_system_button);
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

        tableLayoutIngredients = (TableLayout) findViewById(R.id.table_layout_ingredients);
        tableLayoutCategories = (TableLayout) findViewById(R.id.table_layout_categories);

        addIngredientImageView = (ImageView) findViewById(R.id.add_ingredient_circle);
        addIngredientImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredientRowToIngredientTable(0);
            }
        });

        addCategoryImageView = (ImageView) findViewById(R.id.add_category_circle);
        addCategoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategoryRowToCategoryTable(0);
            }
        });

        // get ingredients and categories from database
        showProgress(true);
        getIngredients();
        getCategories();

        // if user is trying to edit existing recipe, fill in views with recipe data
        if(statusIndicator.equals("EditRecipe")){
            displayRecipe();
        }
    }

    public void useUSSystem(){
        systemIndicator = "US";
        systemTextView.setText("degrees F");
        setSpinners(measurementSpinners, USMeasurements);
    }

    public void useMetricSystem(){
        systemIndicator = "Metric";
        systemTextView.setText("degrees C");
        setSpinners(measurementSpinners, MetricMeasurements);
    }

    public void addIngredientRowToIngredientTable(int index){

        View ingredientRowView;
        final TableRow tableRow;
        final TextView countCol;
        final AutoCompleteTextView autoView;
        final EditText editText;
        final Spinner mSpinner;
        ImageView deleteIngredientRowView;

        ViewCountService.incrementIngredientViewCount();

        ingredientRowView = inflater.inflate(R.layout.new_ingredient_row, null);
        tableRow = ingredientRowView.findViewById(R.id.new_row);

        countCol = ingredientRowView.findViewById(R.id.count_text_view);
        countCol.setText(Integer.toString(ViewCountService.getIngredientViewCount()) + ".");
        ingCountArray.add(countCol);

        autoView = ingredientRowView.findViewById(R.id.new_auto_complete_view);
        autoView.setAdapter(ingredientAdapter);

        if(statusIndicator.equals("EditRecipe") && firstTime){
            autoView.setText(ingredientArray.get(index).getName());
        }
        ingredientViews.add(autoView);

        editText = ingredientRowView.findViewById(R.id.new_quantity);

        if(statusIndicator.equals("EditRecipe") && firstTime){

            if(ingredientArray.get(index).getQuantity() != -1.0){
                editText.setText(Double.toString(ingredientArray.get(index).getQuantity()));
            }else{
                editText.setText("");
            }
        }
        quantityViews.add(editText);

        mSpinner = ingredientRowView.findViewById(R.id.new_measurement_spinner);
        mSpinner.setAdapter(measurementAdapter);

        if(statusIndicator.equals("EditRecipe") && firstTime){
            mSpinner.setSelection(getUnitIndex(ingredientArray.get(index).getQuantityUnit(), "us"));
        }
        measurementSpinners.add(mSpinner);

        deleteIngredientRowView = ingredientRowView.findViewById(R.id.delete_image_view);
        deleteIngredientRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientViews.remove(autoView);
                quantityViews.remove(editText);
                measurementSpinners.remove(mSpinner);
                ingCountArray.remove(countCol);
                updateIngCountNumbers();
                tableLayoutIngredients.removeView(tableRow);
                ViewCountService.decrementIngredientViewCount();
            }
        });

        if(tableRow.getParent() != null){
            ((ViewGroup)tableRow.getParent()).removeView(tableRow);
        }

        tableLayoutIngredients.addView(tableRow);
    }

    public void addCategoryRowToCategoryTable(int index){

        View categoryRowView;
        final TableRow tableRow;
        final TextView countCol;
        final AutoCompleteTextView autoCompCat;
        ImageView deleteCategoryRowView;

        ViewCountService.incrementCategoryViewCount();

        categoryRowView = inflater.inflate(R.layout.new_category_row, null);
        tableRow = categoryRowView.findViewById(R.id.new_row);

        countCol = categoryRowView.findViewById(R.id.count_text_view);
        countCol.setText(Integer.toString(ViewCountService.getCategoryViewCount()) + ".");
        catCountArray.add(countCol);

        autoCompCat = categoryRowView.findViewById(R.id.new_auto_complete_view);
        autoCompCat.setAdapter(categoryAdapter);
        additionalCategories.add(autoCompCat);

        if(statusIndicator.equals("EditRecipe") && firstTimeCategory){
            autoCompCat.setText(categoryArray.get(index).getName());
        }

        deleteCategoryRowView = categoryRowView.findViewById(R.id.delete_image_view);
        deleteCategoryRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catCountArray.remove(countCol);
                updateCatCountNumbers();
                additionalCategories.remove(autoCompCat);
                tableLayoutCategories.removeView(tableRow);
                ViewCountService.decrementCategoryViewCount();

                if(ViewCountService.getCategoryViewCount() == 0){
                    tableLayoutCategories.setVisibility(View.GONE);
                }
            }
        });

        if(tableRow.getParent() != null){
            ((ViewGroup)tableRow.getParent()).removeView(tableRow);
        }

        tableLayoutCategories.addView(tableRow);
        tableLayoutCategories.setVisibility(View.VISIBLE);
    }

    public void updateIngCountNumbers(){
        for(int x = 0; x < ingCountArray.size(); x++){
            ingCountArray.get(x).setText(Integer.toString(x + 4) + ".");
        }
    }

    public void updateCatCountNumbers(){
        for(int x = 0; x < catCountArray.size(); x++){
            catCountArray.get(x).setText(Integer.toString(x + 1) + ".");
        }
    }

    public void showProgress(boolean show){

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public void displayRecipe(){

        recipe = (Recipe) intentReceived.getExtras().get("recipe_to_edit");

        recipeNameView.setText(recipe.getRecipeName());

        if(recipe.getServings() != -1){
            servingsView.setText(Integer.toString(recipe.getServings()));
        }else{
            servingsView.setText("");
        }

        if(recipe.getPreparationTime() != -1){
            prepTimeView.setText(Integer.toString(recipe.getPreparationTime()));
        }else{
            prepTimeView.setText("");
        }

        if(recipe.getOvenTime() != -1){
            ovenTimeView.setText(Integer.toString(recipe.getOvenTime()));
        }else{
            ovenTimeView.setText("");
        }

        if(recipe.getOvenTemperature() != -1){
            ovenTempView.setText(Integer.toString(recipe.getOvenTemperature()));
        }else{
            ovenTempView.setText("");
        }

        if(recipe.getCalories() != -1){
            caloriesView.setText(Integer.toString(recipe.getCalories()));
        }else{
            caloriesView.setText("");
        }

        instructionView.setText(recipe.getInstructions());
    }

    public void setIngredientViews(){

        ingredientArray = recipe.getIngredientArray();

        if(ingredientArray.size() >= 3){

            ViewCountService.setIngredientViewCount(ingredientArray.size());

            autoCompIngredient1.setText(ingredientArray.get(0).getName());

            if(ingredientArray.get(0).getQuantity() != -1.0){
                amountView.setText(Double.toString(ingredientArray.get(0).getQuantity()));
            }else{
                amountView.setText("");
            }

            spinnerMeasurements.setSelection(getUnitIndex(ingredientArray.get(0).getQuantityUnit(), "us"));

            autoCompIngredient2.setText(ingredientArray.get(1).getName());

            if(ingredientArray.get(1).getQuantity() != -1.0){
                amountView2.setText(Double.toString(ingredientArray.get(1).getQuantity()));
            }else{
                amountView2.setText("");
            }

            spinnerMeasurements2.setSelection(getUnitIndex(ingredientArray.get(1).getQuantityUnit(), "us"));

            autoCompIngredient3.setText(ingredientArray.get(2).getName());

            if(ingredientArray.get(2).getQuantity() != -1.0){
                amountView3.setText(Double.toString(ingredientArray.get(2).getQuantity()));
            }else{
                amountView3.setText("");
            }

            spinnerMeasurements3.setSelection(getUnitIndex(ingredientArray.get(2).getQuantityUnit(), "us"));

            for(int x = 3; x < ingredientArray.size(); x++){

                addIngredientRowToIngredientTable(x);
            }
            firstTime = false;
        }
    }

    public void setCategoryViews(){

        categoryArray = recipe.getCategoriesArray();

        if(categoryArray.size() > 1){

            ViewCountService.setCategoryViewCount(categoryArray.size());

            for(int x = 0; x < categoryArray.size(); x++){

                if(!(categoryArray.get(x).isPrimaryCategory())){

                    addCategoryRowToCategoryTable(x);
                }
            }
            firstTimeCategory = false;
        }
    }

    public int getUnitIndex(String unit, String system){

        if(system.equals("us")){

            switch(unit){

                case " ":
                    return 0;
                case "lb":
                    return 1;
                case "oz":
                    return 2;
                case "cup":
                    return 3;
                case "qt":
                    return 4;
                case "tbsp":
                    return 5;
                case "tsp":
                    return 6;
                case "ct":
                    return 7;
                default:
                    return 0;
            }

        }else if(system.equals("metric")){

            switch(unit){

                case " ":
                    return 0;
                case "g":
                    return 1;
                case "kg":
                    return 2;
                case "ml":
                    return 3;
                case "L":
                    return 4;
                case "tbsp":
                    return 5;
                case "tsp":
                    return 6;
                case "ct":
                    return 7;
                default:
                    return 0;
            }
        }

        return -1;
    }

    public void setSpinners(ArrayList<Spinner> spinners, ArrayList<String> stringList){

        measurementAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stringList);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for(int x = 0; x < spinners.size(); x++){

            spinners.get(x).setAdapter(measurementAdapter);
        }
    }

    public void createUSMeasurementList(){
        USMeasurements.add(" ");
        USMeasurements.add("lb");
        USMeasurements.add("oz");
        USMeasurements.add("cup");
        USMeasurements.add("qt");
        USMeasurements.add("tbsp");
        USMeasurements.add("tsp");
        USMeasurements.add("ct");
    }

    public void createMetricMeasurementList(){
        MetricMeasurements.add(" ");
        MetricMeasurements.add("g");
        MetricMeasurements.add("kg");
        MetricMeasurements.add("ml");
        MetricMeasurements.add("L");
        MetricMeasurements.add("tbsp");
        MetricMeasurements.add("tsp");
        MetricMeasurements.add("ct");
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

    // called when item in spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){ }

    @Override
    public void onNothingSelected(AdapterView <?> parent){ }

    // method invoked by appbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        boolean hasRecipeName = false;
        boolean haveQuantityAndUnit = false;
        boolean ingredientsHaveNames = false;
        boolean categoriesHaveNames = false;
        boolean ingredientExists = true;
        boolean categoryExists = true;
        boolean validInstructions = false;

        switch(item.getItemId()){

            // when user clicks
            case R.id.save_action:

                // check that required fields are filled
                hasRecipeName = checkRequiredFields();
                haveQuantityAndUnit = checkQuantityUnitRequirement();
                ingredientsHaveNames = checkIngredientsHaveNames();
                categoriesHaveNames = checkCategoriesHaveNames();
                validInstructions = checkInstructionLength();

                ArrayList<Ingredient> ingredients = new ArrayList<>();

                // get ingredients
                for(int i = 0; i < ingredientViews.size(); i++){

                    if(listIngredients.contains(ingredientViews.get(i).getText().toString())){

                        Ingredient ing = new Ingredient();

                        ing.setName(ingredientViews.get(i).getText().toString());
                        ing.setQuantityUnit(measurementSpinners.get(i).getSelectedItem().toString());

                        if(!(quantityViews.get(i).getText().toString().isEmpty())){
                            ing.setQuantity(Double.parseDouble(quantityViews.get(i).getText().toString()));
                        }else{
                            ing.setQuantity(-1.0);
                        }

                        ingredients.add(ing);

                    }else if(!(ingredientViews.get(i).getText().toString().isEmpty())){
                        ingredientViews.get(i).setError("Ingredient does not exist. Please create it first.");
                        ingredientExists = false;
                        break;
                    }
                }

                ArrayList<Category> addCategories = new ArrayList<>();

                // get additional categories
                for(int j = 0; j < additionalCategories.size(); j++){

                    if(listCategories.contains(additionalCategories.get(j).getText().toString())){

                        Category cat = new Category();

                        cat.setName(additionalCategories.get(j).getText().toString());
                        cat.setCategory("n");

                        addCategories.add(cat);

                    }else{
                        if(!(additionalCategories.get(j).getText().toString().isEmpty())){
                            additionalCategories.get(j).setError("Category does not exist. Please create it first.");
                        }
                        categoryExists = false;
                        break;
                    }
                }

                if(hasRecipeName && haveQuantityAndUnit && ingredientsHaveNames && categoriesHaveNames && ingredientExists && categoryExists && validInstructions) {

                    // get text entered into textfields
                    String recipeName = recipeNameView.getText().toString();
                    String primCategory = categorySpinner.getSelectedItem().toString();
                    String prepTime = prepTimeView.getText().toString();
                    String ovenTime = ovenTimeView.getText().toString();
                    String ovenTemp = ovenTempView.getText().toString();
                    String servings = servingsView.getText().toString();
                    String calories = caloriesView.getText().toString();
                    String instructions = instructionView.getText().toString();

                    // execute new asynchronous save task
                    if(statusIndicator.equals("NewRecipe")){
                        saveTask = new SaveTask(userEmail, recipeName, ingredients, primCategory, addCategories, prepTime
                                , ovenTime, ovenTemp, servings, calories, instructions, systemIndicator
                                , statusIndicator);
                    }else{
                        saveTask = new SaveTask(recipe.getRecipeId(), userEmail, recipeName, ingredients, primCategory, addCategories, prepTime
                                , ovenTime, ovenTemp, servings, calories, instructions, systemIndicator
                                , statusIndicator);
                    }

                    showProgress(true);
                    saveTask.execute((String) null);

                    return true;
                }else if(!validInstructions){
                    instructionView.setError("Instructions are too long. Only 500 characters are allowed.");
                }

                return true;

            case R.id.cancel_action:

                Intent sendIntent = new Intent(NewRecipeActivity.this, MainActivity.class);
                sendIntent.putExtra("user_email", userEmail);
                sendIntent.putExtra("action", "cancel_action");
                startActivity(sendIntent);

                return true;

            case R.id.new_ingredient_action:

                View ingredientPopupView = inflater.inflate(R.layout.new_item_popup, null);

                ImageButton saveButton = ingredientPopupView.findViewById(R.id.save_image_button);
                ImageButton cancelButton = ingredientPopupView.findViewById(R.id.cancel_image_button);
                final EditText newIngredientView = ingredientPopupView.findViewById(R.id.enter_item_view);
                final Spinner defaultSpinner = ingredientPopupView.findViewById(R.id.default_spinner);
                TextView defaultLab = ingredientPopupView.findViewById(R.id.default_label);
                TextView ingHeading = ingredientPopupView.findViewById(R.id.new_item_heading);
                ingHeading.setText("New Ingredient");

                ArrayList<String> values = new ArrayList<>();
                values.add("weight");
                values.add("liquid");
                ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
                defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                defaultSpinner.setAdapter(defaultAdapter);

                ingredientPopup = new PopupWindow(ingredientPopupView, 1100, 1000, true);
                ingredientPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(newIngredientView.getText().length() > 0 && newIngredientView.getText().length() <= 25){

                            SaveItemTask ingredientTask = new SaveItemTask("ingredient", newIngredientView.getText().toString(), userEmail, defaultSpinner.getSelectedItem().toString());
                            ingredientTask.execute();
                            ingredientPopup.dismiss();

                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ingredientPopup.dismiss();
                    }
                });

                return true;

            case R.id.new_category_action:

                View categoryPopupView = inflater.inflate(R.layout.new_item_popup, null);

                ImageButton saveButtonCat =(ImageButton) categoryPopupView.findViewById(R.id.save_image_button);
                ImageButton cancelButtonCat = (ImageButton) categoryPopupView.findViewById(R.id.cancel_image_button);
                final EditText newCategoryView = (EditText) categoryPopupView.findViewById(R.id.enter_item_view);
                final Spinner defaultSpinner2 = (Spinner) categoryPopupView.findViewById(R.id.default_spinner);
                TextView defaultLab2 = (TextView) categoryPopupView.findViewById(R.id.default_label);
                TextView categoryHeading = (TextView) categoryPopupView.findViewById(R.id.new_item_heading);
                categoryHeading.setText("New Category");

                defaultLab2.setVisibility(View.GONE);
                defaultSpinner2.setVisibility(View.GONE);

                categoryPopup = new PopupWindow(categoryPopupView, 1100, 700, true);
                categoryPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                saveButtonCat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(newCategoryView.getText().length() > 0 && newCategoryView.getText().length() <= 25){

                            SaveItemTask categoryTask = new SaveItemTask("category", newCategoryView.getText().toString(), userEmail, "NA");
                            categoryTask.execute();
                            categoryPopup.dismiss();

                        }
                    }
                });

                cancelButtonCat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        categoryPopup.dismiss();
                    }
                });

                return true;

            case R.id.delete_category_action:

                View deleteCatPopupView = inflater.inflate(R.layout.delete_item_popup, null);

                ImageButton saveButtonDelete =(ImageButton) deleteCatPopupView.findViewById(R.id.delete_category_button);
                ImageButton cancelButtonDelete = (ImageButton) deleteCatPopupView.findViewById(R.id.cancel_image_button);
                final Spinner deleteCatSpinner = (Spinner) deleteCatPopupView.findViewById(R.id.delete_item_spinner);
                TextView deleteCatHeading = (TextView) deleteCatPopupView.findViewById(R.id.delete_item_heading);
                deleteCatHeading.setText("Delete Category");

                ArrayAdapter<String> deleteCatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listCategories);
                deleteCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deleteCatSpinner.setAdapter(deleteCatAdapter);

                deleteCatPopup = new PopupWindow(deleteCatPopupView, 1100, 700, true);
                deleteCatPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                saveButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DeleteItemTask deleteTask = new DeleteItemTask("category", userEmail, deleteCatSpinner.getSelectedItem().toString());
                        deleteTask.execute();
                        deleteCatPopup.dismiss();
                    }
                });

                cancelButtonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCatPopup.dismiss();
                    }
                });

                return true;

            case R.id.action_info:

                View infoPopupView = inflater.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.new_info_title);
                infoText.setText(R.string.new_info);
                infoText2.setText(R.string.new_info_constraints);
                infoText3.setText(R.string.new_info_floating);
                infoText4.setText(R.string.happy_cooking);

                infoPopup = new PopupWindow(infoPopupView, 1200, 1300, true);
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

    // check if recipe has a name and at least three ingredients
    protected boolean checkRequiredFields() {

        boolean valid = false;

        if(recipeNameView.getText().length() > 0 && recipeNameView.getText().length() <= 35) {
            if(autoCompIngredient1.getText().length() > 0 && autoCompIngredient2.getText().length() > 0 && autoCompIngredient3.getText().length() > 0){
                valid = true;
            }else{
                autoCompIngredient3.setError("Each recipe must have at least 3 ingredients");
            }
        }else{
            recipeNameView.setError("Recipe name needs to be between 1 and 35 characters");
        }
        return valid;
    }

    // if user entered a quantity then user also needs to enter a unit for that quantity
    protected boolean checkQuantityUnitRequirement(){

        boolean haveQuantityAndUnit = true;

        for(int x = 0; x < quantityViews.size(); x++){

            if(!(quantityViews.get(x).getText().toString().isEmpty())){

                if(measurementSpinners.get(x).getSelectedItem().toString().equals(" ")) {
                    haveQuantityAndUnit = false;
                    quantityViews.get(x).setError("No unit specified for this quantity");
                }
            }
        }
        return haveQuantityAndUnit;
    }

    protected boolean checkInstructionLength(){
        return (instructionView.getText().toString().length() <= 500);
    }

    protected boolean checkIngredientsHaveNames(){

        boolean haveNames = true;

        for(int x = 3; x < ingredientViews.size(); x++){

            if(ingredientViews.get(x).getText().toString().isEmpty()){
                haveNames = false;
                ingredientViews.get(x).setError("This ingredient needs a name");
            }
        }
        return haveNames;
    }

    protected boolean checkCategoriesHaveNames(){

        boolean haveNames = true;

        for(int x = 0; x < additionalCategories.size(); x++){

            if(additionalCategories.get(x).getText().toString().isEmpty()){
                haveNames = false;
                additionalCategories.get(x).setError("This category needs a name");
            }
        }
        return haveNames;
    }

    // when user has stored a new ingredient, update the autocomplete views so that user can choose new ingredient
    public void updateAutoCompleteViews(){

        System.out.println("Size of ingredient views array: " + ingredientViews.size());

        for(int x = 0; x < ingredientViews.size(); x++){
            ingredientViews.get(x).setAdapter(ingredientAdapter);
        }
    }

    private void onBackgroundTaskObtainedIngredients(ArrayList<String> ingredients){

        listIngredients = ingredients;

        ingredientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ingredients);

        //autoCompIngredient1.setAdapter(ingredientAdapter);
        //autoCompIngredient2.setAdapter(ingredientAdapter);
        //autoCompIngredient3.setAdapter(ingredientAdapter);

        updateAutoCompleteViews();

        if(statusIndicator.equals("EditRecipe")){
            setIngredientViews();
        }

        showProgress(false);
    }

    private void onBackgroundTaskObtainedCategories(ArrayList<String> categories){

        listCategories = categories;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);

        categorySpinner.setSelection(0);

        if(statusIndicator.equals("EditRecipe")){
            setCategoryViews();
        }
    }

    public void onBackgroundDeleteTaskSuccess(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category successfully deleted", Snackbar.LENGTH_SHORT)
        .show();
    }

    public void onBackgroundDeleteTaskFailure(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category cannot be deleted. One or more recipes belong to this category", Snackbar.LENGTH_LONG)
        .show();
    }

    public void onBackgroundDeleteTaskNeutral(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "This category cannot be deleted", Snackbar.LENGTH_LONG)
        .show();
    }

    // user tried to add a new ingredient or category
    // if new item was stored successfully, reload that list of items
    public void onBackgroundTaskSavedItem(String indicator, String finalResult){

        if(indicator.equals("ingredient") && finalResult.equals("success")){
            Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "New ingredient was saved", Snackbar.LENGTH_SHORT).show();
            getIngredients();
        }else if(indicator.equals("ingredient") && finalResult.equals("exists")){
            Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Ingredient already exists", Snackbar.LENGTH_LONG).show();
        }else if(indicator.equals("category") && finalResult.equals("success")){
            Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "New category was saved", Snackbar.LENGTH_SHORT).show();
            getCategories();
        }else if(indicator.equals("category") && finalResult.equals("exists")){
            Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category already exists", Snackbar.LENGTH_LONG).show();
        }
    }

    // save/update recipe
    public class SaveTask extends AsyncTask<String, Void, String> {

        String recipeName;
        ArrayList<Ingredient> ingredients;
        ArrayList<Category> categories;
        String primCategory;
        String prepTime, ovenTime, ovenTemp, servings, calories;
        String instructions;
        String uniqueID;
        String userEmail;
        String systemIndicator;
        String actionIndicator;

        public SaveTask(String user, String rName, ArrayList<Ingredient> ingredients, String primCat,
                        ArrayList<Category> categories, String pTime, String oTime, String oTemp,
                        String servings, String calories, String instruct, String sysInd, String actInd){

            userEmail = user;
            recipeName = rName;
            this.ingredients = ingredients;
            this.categories = categories;
            primCategory = primCat;
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
            instructions = instruct;
            systemIndicator = sysInd;
            actionIndicator = actInd;
        }

        public SaveTask(String recipeId, String user, String rName, ArrayList<Ingredient> ingredients, String primCat,
                        ArrayList<Category> categories, String pTime, String oTime, String oTemp,
                        String servings, String calories, String instruct, String sysInd, String actInd){

            uniqueID = recipeId;
            userEmail = user;
            recipeName = rName;
            this.ingredients = ingredients;
            this.categories = categories;
            primCategory = primCat;
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
            instructions = instruct;
            systemIndicator = sysInd;
            actionIndicator = actInd;
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
                    //url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveRecipe");

                    //connection to aws EC2 server instance
                    url = new URL("http://3.16.170.8:8080/mycookbookservlets/SaveRecipe");

                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    JSONArray jsonArrayCat = new JSONArray();

                    // create unique id for this recipe
                    if(actionIndicator.equals("NewRecipe")){
                        uniqueID = UUID.randomUUID().toString();
                    }
                    System.out.println("Unique ID generated: " + uniqueID);

                    System.out.println("Recipe data being passed: " + userEmail + " " + recipeName + " " + primCategory);
                    System.out.println(" More data: " + prepTime + " " + ovenTime + " " + ovenTemp);

                    for(int x = 0; x < ingredients.size(); x++){

                        JSONObject jObject = new JSONObject();
                        jObject.put("ing_name", ingredients.get(x).getName());
                        jObject.put("quantity_unit", ingredients.get(x).getQuantityUnit());

                        if(ingredients.get(x).getQuantity() != -1.0){

                            jObject.put("quantity", ingredients.get(x).getQuantity());
                        }else{
                            jObject.put("quantity", "");
                        }

                        jsonArray.put(jObject);
                    }

                    for(int y = 0; y < categories.size(); y++){

                        JSONObject catObject = new JSONObject();
                        catObject.put("cat_name", categories.get(y).getName());
                        catObject.put("cat_prime", categories.get(y).isPrimaryCategory());

                        jsonArrayCat.put(catObject);
                    }

                    jsonObject.put("userEmail", userEmail);
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

                    recipe = jsonObject.toString();

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

                    outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(recipe.getBytes());
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
        protected void onPostExecute(String result){

            String finalResult = ParseJSON.parseJSON(result);

            showProgress(false);

            if(finalResult.equals("success")){

                boolean deviceIsKnown = true;

                Intent sendIntent = new Intent(NewRecipeActivity.this, MainActivity.class);
                sendIntent.putExtra("user_email", userEmail);
                sendIntent.putExtra("action", "save_action");
                sendIntent.putExtra("device_is_known", deviceIsKnown);
                startActivity(sendIntent);
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
                //url = new URL("http://10.0.0.18:9999/mycookbookservlets/RetrieveIngredients");

                //connection to aws EC2 server instance
                url = new URL("http://3.16.170.8:8080/mycookbookservlets/RetrieveIngredients");

            }else if(itemIndicator.equals("cat")){
                //url = new URL("http://10.0.0.18:9999/mycookbookservlets/RetrieveCategories");

                //connection to aws EC2 server instance
                url = new URL("http://3.16.170.8:8080/mycookbookservlets/RetrieveCategories");
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

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(send.getBytes());
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
                    NewRecipeActivity.this.onBackgroundTaskObtainedIngredients(listItems);
                }else if(itemIndicator.equals("cat")){
                    NewRecipeActivity.this.onBackgroundTaskObtainedCategories(listItems);
                }

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    class SaveItemTask extends AsyncTask<String, Void, String> {

        String indicator, item;
        String defaultMeasurement, userEmail;

        public SaveItemTask(String indicator, String item, String email, String defaultMeasurement){

            this.indicator = indicator;
            this.item = item;
            userEmail = email;
            this.defaultMeasurement = defaultMeasurement;
        }

        @Override
        protected String doInBackground(String... params){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{

                //url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveItem");

                //connection to aws EC2 server instance
                url = new URL("http://3.16.170.8:8080/mycookbookservlets/SaveItem");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("indicator", indicator);
                jsonObject.put("item", item);

                if(indicator.equals("ingredient")){
                    jsonObject.put("def", defaultMeasurement);
                }else if(indicator.equals("category")){
                    jsonObject.put("userEmail", userEmail);
                }

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

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(send.getBytes());
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
        protected void onPostExecute(String result){

            String finalResult = ParseJSON.parseJSON(result);
            NewRecipeActivity.this.onBackgroundTaskSavedItem(indicator, finalResult);
        }
    }

    public class DeleteItemTask extends AsyncTask<String, Void, String>{

        private String userEmail, category, itemIndicator;

        public DeleteItemTask(String indicator, String email, String category){

            itemIndicator = indicator;
            userEmail = email;
            this.category = category;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{

                if(itemIndicator.equals("category")){
                    //url = new URL("http://10.0.0.18:9999/mycookbookservlets/DeleteCategory");

                    //connection to aws EC2 server instance
                    url = new URL("http://3.16.170.8:8080/mycookbookservlets/DeleteCategory");
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", userEmail);
                jsonObject.put("category", category);
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

                outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(send.getBytes());
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
        protected void onPostExecute(String response){

            String finalResult = ParseJSON.parseJSON(response);

            if(finalResult.equals("success")){
                onBackgroundDeleteTaskSuccess();

            }else if(finalResult.equals("categoryIsUsed")){
                onBackgroundDeleteTaskFailure();
            }else if(finalResult.equals("neutral")){
                onBackgroundDeleteTaskNeutral();
            }
        }
    }

}
