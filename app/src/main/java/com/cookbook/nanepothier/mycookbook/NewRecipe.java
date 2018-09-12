package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.*;
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
    private Context context;

    // new ingredient popup window
    CoordinatorLayout coordinatorLayout;

    // toolbar
    private ImageButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        context = getApplicationContext();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.new_recipe_activity_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newrecipe_toolbar);
        setSupportActionBar(toolbar);
        backButton = (ImageButton) toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statusIndicator.equals("NewRecipe")){
                    Intent intent = new Intent(NewRecipe.this, MainActivity.class);
                    intent.putExtra("action", "back_new_recipe");
                    startActivity(intent);
                }else if(statusIndicator.equals("EditRecipe")) {
                    Intent intent = new Intent(NewRecipe.this, ViewRecipe.class);
                    intent.putExtra("action", "back_edit_recipe");
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
        recipeNameView = (EditText) findViewById(R.id.recipe_name);
        servingsView = (EditText) findViewById(R.id.servings);
        prepTimeView = (EditText) findViewById(R.id.prep_time);
        ovenTimeView = (EditText) findViewById(R.id.oven_time);
        ovenTempView = (EditText) findViewById(R.id.oven_temp);
        caloriesView = (EditText) findViewById(R.id.calories);
        instructionView = (EditText) findViewById(R.id.editText);

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
                addIngredientRowToIngredientTable();
            }
        });

        addCategoryImageView = (ImageView) findViewById(R.id.add_category_circle);
        addCategoryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategoryRowToCategoryTable();
            }
        });

        // get data passed to this activity
        // intentReceived = getIntent();
        statusIndicator = "NewRecipe";
        // intentReceived.getExtras().getString("StatusIndicator");

        // get ingredients and categories from database
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

    public void addIngredientRowToIngredientTable(){

        final TableRow tableRow;
        final TextView countCol;
        final AutoCompleteTextView autoView;
        final EditText editText;
        final Spinner mSpinner;
        ImageView deleteIngredientRowView;

        Service.incrementIngredientViewCount();

        tableRow = new TableRow(this);
        tableRow.setPadding(5, 5, 5, 5);
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        countCol = new TextView(this);
        countCol.setText(Integer.toString(Service.getIngredientViewCount()) + ".");
        countCol.setTextSize(15);
        countCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        ingCountArray.add(countCol);
        tableRow.addView(countCol);

        autoView = new AutoCompleteTextView(this);
        autoView.setAdapter(ingredientAdapter);
        autoView.setTextSize(15);
        autoView.setMaxLines(2);
        autoView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 95, 1.0f));
        autoView.setBackgroundResource(R.drawable.thin_black_border_background);
        ingredientViews.add(autoView);
        tableRow.addView(autoView);

        editText = new EditText(this);
        editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 95));
        editText.setMaxLines(1);
        editText.setTextSize(15);
        editText.setBackgroundResource(R.drawable.thin_black_border_background);
        quantityViews.add(editText);
        tableRow.addView(editText);

        mSpinner = new Spinner(this);
        mSpinner.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        mSpinner.setAdapter(measurementAdapter);
        measurementSpinners.add(mSpinner);
        tableRow.addView(mSpinner);

        deleteIngredientRowView = new ImageView(this);
        deleteIngredientRowView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        deleteIngredientRowView.setImageResource(R.mipmap.ic_remove_circle_outline_black_18dp);
        deleteIngredientRowView.setPadding(0,60,0,0);
        tableRow.addView(deleteIngredientRowView);
        deleteIngredientRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredientViews.remove(autoView);
                quantityViews.remove(editText);
                measurementSpinners.remove(mSpinner);
                ingCountArray.remove(countCol);
                updateIngCountNumbers();
                tableLayoutIngredients.removeView(tableRow);
                Service.decrementIngredientViewCount();
            }
        });

        tableLayoutIngredients.addView(tableRow);
    }

    public void addCategoryRowToCategoryTable(){

        final TableRow tableRow;
        final TextView countCol;
        final AutoCompleteTextView autoCompCat;
        ImageView deleteCategoryRowView;

        Service.incrementCategoryViewCount();

        tableRow = new TableRow(this);
        tableRow.setPadding(5, 5, 5, 5);
        tableRow.setId(Service.getCategoryViewCount());
        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        countCol = new TextView(this);
        countCol.setText(Integer.toString(Service.getCategoryViewCount()) + ".");
        countCol.setTextSize(15);
        countCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.05f));
        catCountArray.add(countCol);
        tableRow.addView(countCol);

        autoCompCat = new AutoCompleteTextView(this);
        autoCompCat.setAdapter(categoryAdapter);
        autoCompCat.setTextSize(15);
        autoCompCat.setPadding(0, 35, 0, 0);
        autoCompCat.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 95, 1.0f));
        autoCompCat.setBackgroundResource(R.drawable.thin_black_border_background);
        additionalCategories.add(autoCompCat);
        tableRow.addView(autoCompCat);

        deleteCategoryRowView = new ImageView(this);
        deleteCategoryRowView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
        deleteCategoryRowView.setImageResource(R.mipmap.ic_remove_circle_outline_black_18dp);
        deleteCategoryRowView.setPadding(0, 45, 0, 0);
        tableRow.addView(deleteCategoryRowView);
        deleteCategoryRowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catCountArray.remove(countCol);
                updateCatCountNumbers();
                additionalCategories.remove(autoCompCat);
                tableLayoutCategories.removeView(tableRow);
                Service.decrementCategoryViewCount();

                if(Service.getCategoryViewCount() == 0){
                    tableLayoutCategories.setVisibility(View.GONE);
                }
            }
        });

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

    public void displayRecipe(){

        recipe = (Recipe) intentReceived.getExtras().get("RecipeToEdit");

        recipeNameView.setText(recipe.getRecipeName());
        servingsView.setText(Integer.toString(recipe.getServings()));
        prepTimeView.setText(Integer.toString(recipe.getPreparationTime()));
        ovenTimeView.setText(Integer.toString(recipe.getOvenTime()));
        ovenTempView.setText(Integer.toString(recipe.getOvenTemperature()));
        instructionView.setText(recipe.getInstructions());
    }

    public void setIngredientViews(){

        ingredientArray = recipe.getIngredientArray();

        if(ingredientArray.size() >= 3){

            Service.setIngredientViewCount(ingredientArray.size());

            autoCompIngredient1.setText(ingredientArray.get(0).getName());
            amountView.setText(ingredientArray.get(0).getQuantity());
            spinnerMeasurements.setSelection(getUnitIndex(ingredientArray.get(0).getQuantityUnit(), "us"));

            autoCompIngredient2.setText(ingredientArray.get(1).getName());
            amountView2.setText(ingredientArray.get(1).getQuantity());
            spinnerMeasurements2.setSelection(getUnitIndex(ingredientArray.get(1).getQuantityUnit(), "us"));

            autoCompIngredient3.setText(ingredientArray.get(2).getName());
            amountView3.setText(ingredientArray.get(2).getQuantity());
            spinnerMeasurements3.setSelection(getUnitIndex(ingredientArray.get(2).getQuantityUnit(), "us"));

            TableRow tableRow;
            TextView countCol;
            AutoCompleteTextView autoView;
            EditText editText;
            Spinner mSpinner;
            int count = 4;

            for(int x = 3; x < ingredientArray.size(); x++){

                tableRow = new TableRow(this);
                tableRow.setId(count);
                tableRow.setPadding(5, 5, 5, 5);
                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                countCol = new TextView(this);
                countCol.setText(count + ".");
                countCol.setTextSize(15);
                countCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
                tableRow.addView(countCol);

                autoView = new AutoCompleteTextView(this);
                autoView.setAdapter(ingredientAdapter);
                autoView.setText(ingredientArray.get(x).getName());
                autoView.setTextSize(15);
                autoView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f));
                tableRow.addView(autoView);

                editText = new EditText(this);
                editText.setText(ingredientArray.get(x).getQuantity());
                editText.setTextSize(15);
                editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
                tableRow.addView(editText);

                mSpinner = new Spinner(this);
                mSpinner.setSelection(getUnitIndex(ingredientArray.get(x).getQuantityUnit(), "us"));
                mSpinner.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
                tableRow.addView(mSpinner);

                tableLayoutIngredients.addView(tableRow);
                count++;
            }
        }
    }

    public void setCategoryViews(){

        categoryArray = recipe.getCategoriesArray();

        if(categoryArray.size() > 1){

            Service.setCategoryViewCount(categoryArray.size());

            TableRow tableRow;
            int count = 2;
            TextView countCol;
            AutoCompleteTextView autoCompCat;

            for(int x = 0; x < categoryArray.size(); x++){

                if(!(categoryArray.get(x).getCategory())){

                    tableRow = new TableRow(this);
                    tableRow.setPadding(5, 5, 5, 5);
                    tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    countCol = new TextView(this);
                    countCol.setText(count + ".");
                    countCol.setTextSize(15);
                    countCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
                    tableRow.addView(countCol);

                    autoCompCat = new AutoCompleteTextView(this);
                    autoCompCat.setAdapter(categoryAdapter);
                    autoCompCat.setText(categoryArray.get(x).getName());
                    autoCompCat.setTextSize(15);
                    autoCompCat.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    tableRow.addView(autoCompCat);

                    tableLayoutCategories.addView(tableRow);
                    count++;
                }
            }
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
    }

    public void createMetricMeasurementList(){
        MetricMeasurements.add(" ");
        MetricMeasurements.add("g");
        MetricMeasurements.add("kg");
        MetricMeasurements.add("ml");
        MetricMeasurements.add("L");
        MetricMeasurements.add("tbsp");
        MetricMeasurements.add("tsp");
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

        listIngredients = ingredients;

        ingredientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ingredients);

        autoCompIngredient1.setAdapter(ingredientAdapter);
        autoCompIngredient2.setAdapter(ingredientAdapter);
        autoCompIngredient3.setAdapter(ingredientAdapter);

        if(statusIndicator.equals("EditRecipe")){
            setIngredientViews();
        }
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

        boolean hasRecipeName = false;
        boolean haveQuantityAndUnit = false;
        boolean ingredientsHaveNames = false;
        boolean categoriesHaveNames = false;
        boolean ingredientExists = true;
        boolean categoryExists = true;

        switch(item.getItemId()){

            // when user clicks save
            case R.id.save_action:

                // check that required fields are filled
                // hasRecipeName = checkRequiredFields();
                // haveQuantityAndUnit = checkQuantityUnitRequirement();
                // ingredientsHaveNames = checkIngredientsHaveNames();
                // categoriesHaveNames = checkCategoriesHaveNames();
                hasRecipeName = true;
                haveQuantityAndUnit = true;
                ingredientsHaveNames = true;
                categoriesHaveNames = true;

                // get text entered into textfields
                String recipeName = recipeNameView.getText().toString();
                String primCategory = categorySpinner.getSelectedItem().toString();
                String prepTime = prepTimeView.getText().toString();
                String ovenTime = ovenTimeView.getText().toString();
                String ovenTemp = ovenTempView.getText().toString();
                String servings = servingsView.getText().toString();
                String calories = caloriesView.getText().toString();
                String instructions = instructionView.getText().toString();

                ArrayList<Ingredient> ingredients = new ArrayList<>();

                // get ingredients
                for(int i = 0; i < ingredientViews.size(); i++){

                    if(listIngredients.contains(ingredientViews.get(i).getText().toString())){

                        Ingredient ing = new Ingredient();

                        ing.setName(ingredientViews.get(i).getText().toString());
                        ing.setQuantity(Integer.parseInt(quantityViews.get(i).getText().toString()));
                        ing.setQuantityUnit(measurementSpinners.get(i).getSelectedItem().toString());

                        ingredients.add(ing);
                    }else{
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
                        additionalCategories.get(j).setError("Category does not exist. Please create it first.");
                        categoryExists = false;
                        break;
                    }
                }

                if(hasRecipeName && haveQuantityAndUnit && ingredientsHaveNames && categoriesHaveNames && ingredientExists && categoryExists) {

                    // execute new asynchronous save task
                    saveTask = new SaveTask(userEmail, recipeName, ingredients, primCategory, addCategories, prepTime
                                            , ovenTime, ovenTemp, servings, calories, instructions, systemIndicator
                                            , statusIndicator);
                    saveTask.execute((String) null);

                    return true;
                }

                return true;

            case R.id.cancel_action:

                Intent sendIntent = new Intent(NewRecipe.this, MainActivity.class);
                sendIntent.putExtra("action", "cancel_action");
                startActivity(sendIntent);

                return true;

            case R.id.new_ingredient_action:

                LayoutInflater inflater = (LayoutInflater) NewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View ingredientPopupView = inflater.inflate(R.layout.new_item_popup, null);

                ImageButton saveButton =(ImageButton) ingredientPopupView.findViewById(R.id.save_image_button);
                ImageButton cancelButton = (ImageButton) ingredientPopupView.findViewById(R.id.cancel_image_button);
                final EditText newIngredientView = (EditText) ingredientPopupView.findViewById(R.id.enter_item_view);
                final Spinner defaultSpinner = (Spinner) ingredientPopupView.findViewById(R.id.default_spinner);
                TextView defaultLab = (TextView) ingredientPopupView.findViewById(R.id.default_label);
                TextView ingHeading = (TextView) ingredientPopupView.findViewById(R.id.new_item_heading);
                ingHeading.setText("New Ingredient");

                ArrayList<String> values = new ArrayList<>();
                values.add("weight");
                values.add("liquid");
                ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values);
                defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                defaultSpinner.setAdapter(defaultAdapter);

                ingredientPopup = new PopupWindow(ingredientPopupView, 1200, 900, true);
                ingredientPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SaveItemTask ingredientTask = new SaveItemTask("ingredient", newIngredientView.getText().toString(), userEmail, defaultSpinner.getSelectedItem().toString());
                        ingredientTask.execute();
                        ingredientPopup.dismiss();
                        Snackbar.make(findViewById(R.id.new_recipe_content_layout), "Ingredient saved", Snackbar.LENGTH_SHORT);
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

                LayoutInflater inflater2 = (LayoutInflater) NewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View categoryPopupView = inflater2.inflate(R.layout.new_item_popup, null);

                ImageButton saveButtonCat =(ImageButton) categoryPopupView.findViewById(R.id.save_image_button);
                ImageButton cancelButtonCat = (ImageButton) categoryPopupView.findViewById(R.id.cancel_image_button);
                final EditText newCategoryView = (EditText) categoryPopupView.findViewById(R.id.enter_item_view);
                final Spinner defaultSpinner2 = (Spinner) categoryPopupView.findViewById(R.id.default_spinner);
                TextView defaultLab2 = (TextView) categoryPopupView.findViewById(R.id.default_label);
                TextView categoryHeading = (TextView) categoryPopupView.findViewById(R.id.new_item_heading);
                categoryHeading.setText("New Category");

                defaultLab2.setVisibility(View.GONE);
                defaultSpinner2.setVisibility(View.GONE);

                categoryPopup = new PopupWindow(categoryPopupView, 1000, 600, true);
                categoryPopup.showAtLocation(coordinatorLayout, Gravity.CENTER, 0, 0);

                saveButtonCat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SaveItemTask categoryTask = new SaveItemTask("category", newCategoryView.getText().toString(), userEmail, "NA");
                        categoryTask.execute();
                        categoryPopup.dismiss();
                        Snackbar.make(findViewById(R.id.new_recipe_content_layout), "Ingredient saved", Snackbar.LENGTH_SHORT);
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

                LayoutInflater inflater3 = (LayoutInflater) NewRecipe.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View deleteCatPopupView = inflater3.inflate(R.layout.delete_item_popup, null);

                ImageButton saveButtonDelete =(ImageButton) deleteCatPopupView.findViewById(R.id.delete_category_button);
                ImageButton cancelButtonDelete = (ImageButton) deleteCatPopupView.findViewById(R.id.cancel_image_button);
                final Spinner deleteCatSpinner = (Spinner) deleteCatPopupView.findViewById(R.id.delete_item_spinner);
                TextView deleteCatHeading = (TextView) deleteCatPopupView.findViewById(R.id.delete_item_heading);
                deleteCatHeading.setText("Delete Category");

                ArrayAdapter<String> deleteCatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listCategories);
                deleteCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                deleteCatSpinner.setAdapter(deleteCatAdapter);

                deleteCatPopup = new PopupWindow(deleteCatPopupView, 1200, 900, true);
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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // check if recipe has a name and at least three ingredients
    protected boolean checkRequiredFields() {

        boolean valid = false;

        if(recipeNameView.getText().length() > 0) {
            if(autoCompIngredient1.getText().length() > 0 && autoCompIngredient2.getText().length() > 0 && autoCompIngredient3.getText().length() > 0){
                valid = true;
            }else{
                autoCompIngredient3.setError("Recipe must have at least 3 ingredients");
            }
        }else{
            recipeNameView.setError("Recipe needs a name");
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

    protected boolean checkIngredientsHaveNames(){

        boolean haveNames = true;

        for(int x = 0; x < ingredientViews.size(); x++){

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

    public void onBackgroundDeleteTaskSuccess(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category successfully deleted", Snackbar.LENGTH_SHORT);
    }

    public void onBackgroundDeleteTaskFailure(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category cannot be deleted. One or more recipes belong to this category", Snackbar.LENGTH_LONG);
    }

    public void onBackgroundDeleteTaskNeutral(){
        Snackbar.make(findViewById(R.id.new_recipe_activity_layout), "Category to be deleted does not exist", Snackbar.LENGTH_LONG);
    }

    public class SaveTask extends AsyncTask<String, Void, String> {

        String recipeName;
        ArrayList<Ingredient> ingredients;
        ArrayList<Category> categories;
        String primCategory;
        String prepTime, ovenTime, ovenTemp, servings, calories;
        String instructions;
        String uniqueID;
        String user;
        String systemIndicator;
        String actionIndicator;

        public SaveTask(String user, String rName, ArrayList<Ingredient> ingredients, String primCat,
                        ArrayList<Category> categories, String pTime, String oTime, String oTemp,
                        String servings, String calories, String instruct, String sysInd, String actInd){

            this.user = user;
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
                    url = new URL("http://10.0.0.18:9999/mycookbookservlets/SaveRecipe");

                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    JSONArray jsonArrayCat = new JSONArray();

                    // create unique id for this recipe
                    uniqueID = UUID.randomUUID().toString();
                    System.out.println("Unique ID generated: " + uniqueID);

                    System.out.println("Recipe data being passed: " + user + " " + recipeName + " " + primCategory);
                    System.out.println(" More data: " + prepTime + " " + ovenTime + " " + ovenTemp);

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
                        catObject.put("cat_prime", categories.get(y).getCategory());

                        jsonArrayCat.put(catObject);
                    }

                    jsonObject.put("userEmail", user);
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

                    /*
                    try{
                         outputStream.close();
                         inputStream.close();

                    }catch(IOException ie){
                        ie.printStackTrace();
                    }
                    */
                }

            return result;
        }


        @Override
        protected void onPostExecute(String result){

            String finalResult = ParseJSON.parseJSON(result);

            System.out.println("final result: " + finalResult);

            if(finalResult.equals("success")){

                System.out.println("Everything was stored successfully. ");

                Intent sendIntent = new Intent(NewRecipe.this, MainActivity.class);
                sendIntent.putExtra("action", "save_action");
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
                    url = new URL("http://10.0.0.18:9999/mycookbookservlets/DeleteCategory");
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
