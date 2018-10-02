package com.cookbook.nanepothier.mycookbook;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * class/activity Cookbook is responsible for displaying the user's
 * recipes in a list view
 */
public class Cookbook extends AppCompatActivity {

    private ArrayList<String> arrayRecipeNames;
    private ArrayList<String> arrayUserCategories;

    private ArrayList<HeaderRecipeModel> allHeaderRecipeModelsArray;
    private ArrayList<HeaderRecipeModel> sharedHeaderRecipeModelsArray;
    private ArrayList<HeaderRecipeModel> ownHeaderRecipeModelsArray;

    private Map<String, ArrayList<RecipeNameId>> allRecipesMap;
    private Map<String, ArrayList<RecipeNameId>> sharedRecipesMap;
    private Map<String, ArrayList<RecipeNameId>> ownRecipesMap;

    private String userEmail;
    private RecyclerView recyclerView;
    private View progressView;
    private LinearLayout contentLayout;
    private PopupWindow infoPopup;
    private CoordinatorLayout coordinatorLayout;
    private String activityAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookbook);

        // retrieve user email through intent
        Intent intentReceived = getIntent();
        userEmail = intentReceived.getStringExtra("user_email");
        activityAction = intentReceived.getStringExtra("action");
        //userEmail = "haleyiron@gmail.com";

        if(activityAction.equals("deleted_recipe")){
            Snackbar.make(findViewById(R.id.cookbook_coordinator_layout), "Recipe was deleted successfully", Snackbar.LENGTH_LONG).show();
        }

        coordinatorLayout = findViewById(R.id.cookbook_coordinator_layout);

        // show progress bar until recipe names have been retrieved
        progressView = findViewById(R.id.cookbook_progress);
        contentLayout = findViewById(R.id.cookbook_content_layout);
        showProgress(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.cookbook_toolbar);
        setSupportActionBar(toolbar);

        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Cookbook.this, MainActivity.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("action", "cookbook");
                startActivity(intent);
            }
        });

        final SearchView searchView = (SearchView) toolbar.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(!searchView.getQuery().toString().isEmpty()){
                    SearchRecipesTask searchTask = new SearchRecipesTask(userEmail, searchView.getQuery().toString());
                    searchTask.execute();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                populateCategoryRecyclerView(allHeaderRecipeModelsArray);

                return false;
            }
        });

        allHeaderRecipeModelsArray = new ArrayList<>();
        ownHeaderRecipeModelsArray = new ArrayList<>();
        sharedHeaderRecipeModelsArray = new ArrayList<>();

        getRecipeNamesAndCategories();
        setUpRecyclerView();
    }

    public void setUpRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    // show progress bar
    public void showProgress(boolean show){

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * create a different array for all, shared and personal recipes to easily switch between displaying a certain
     * group of recipes in the recycler view
     */
    public void generateHeaderRecipeModelsFromMap(Map<String, ArrayList<RecipeNameId>> map, ArrayList<HeaderRecipeModel> arrayHeaderRecipeModels){

        String categoryName;
        arrayUserCategories = new ArrayList<>(map.keySet());

        for(int x = 0; x < arrayUserCategories.size(); x++){
            categoryName = arrayUserCategories.get(x);
            arrayHeaderRecipeModels.add(new HeaderRecipeModel(categoryName, map.get(categoryName)));
        }
    }

    // create views using retrieved categories and recipe names
    public void populateCategoryRecyclerView(ArrayList<HeaderRecipeModel> arrayHeaderRecipeModels){

        SectionRecyclerViewAdapter recyclerViewAdapter = new SectionRecyclerViewAdapter(this, arrayHeaderRecipeModels, userEmail);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * display recipes retrieved due to a search initiated by the user
     * display only recipe names, no categories
     */
    public void populateSearchRecyclerView(ArrayList<RecipeNameId> array){

        ItemRecyclerViewAdapter recyclerViewAdapter = new ItemRecyclerViewAdapter(this, array, userEmail);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * start an asynchronous background task to retrieve all of the user's recipes and the categories
     * they belong to
     */
    public void getRecipeNamesAndCategories(){

        GetItemsTask nameTask = new GetItemsTask(userEmail);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            nameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            nameTask.execute((String) null);
        }
    }

    // receive recipe names and categories retrieved from database
    public void onBackgroundTaskObtainedRecipeNamesAndCategories(Map<String, ArrayList<RecipeNameId>> map, Map<String, ArrayList<RecipeNameId>> sharedMap, Map<String, ArrayList<RecipeNameId>> ownMap){

        allRecipesMap = map;
        sharedRecipesMap = sharedMap;
        ownRecipesMap = ownMap;

        // generate array lists holding category-recipes data so view can easily be switched
        generateHeaderRecipeModelsFromMap(allRecipesMap, allHeaderRecipeModelsArray);
        generateHeaderRecipeModelsFromMap(sharedRecipesMap, sharedHeaderRecipeModelsArray);
        generateHeaderRecipeModelsFromMap(ownRecipesMap, ownHeaderRecipeModelsArray);

        // populate the initial view with all categories and all recipes
        populateCategoryRecyclerView(allHeaderRecipeModelsArray);

        showProgress(false);
    }

    public void onBackgroundTaskObtainedSearchResult(ArrayList<RecipeNameId> nameIdArray){

        // display recipes retrieved that fit search criteria
        populateSearchRecyclerView(nameIdArray);
    }

    // create menu on toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_cookbook, menu);

        return true;
    }

    // method invoked when a menu item is clicked from toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_view_own:

                populateCategoryRecyclerView(ownHeaderRecipeModelsArray);
                return true;

            case R.id.action_view_shared:

                populateCategoryRecyclerView(sharedHeaderRecipeModelsArray);
                return true;

            case R.id.action_view_all:

                populateCategoryRecyclerView(allHeaderRecipeModelsArray);
                return true;

            case R.id.action_info:

                LayoutInflater inflater = (LayoutInflater) Cookbook.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View infoPopupView = inflater.inflate(R.layout.app_info_popup, null);

                ImageButton doneButton = infoPopupView.findViewById(R.id.info_button);
                TextView infoTitle = infoPopupView.findViewById(R.id.info_title);
                TextView infoText = infoPopupView.findViewById(R.id.info_text_view);
                TextView infoText2 = infoPopupView.findViewById(R.id.info_text_view2);
                TextView infoText3 = infoPopupView.findViewById(R.id.info_text_view3);
                TextView infoText4 = infoPopupView.findViewById(R.id.info_text_view4);

                infoTitle.setText(R.string.cookbook_info_title);
                infoText.setText(R.string.cookbook_info);
                infoText2.setText(R.string.cookbook_info_search);
                infoText3.setText(R.string.cookbook_info_click);
                infoText4.setVisibility(View.GONE);

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

    /**
     * asynchronous background task used to search all of the user's recipes
     * for the given search keyword
     */
    public class SearchRecipesTask extends AsyncTask<String, Void, String>{

        private String userEmail, searchItem, data;

        public SearchRecipesTask(String email, String item){
            userEmail = email;
            searchItem = item;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/SearchRecipes");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_email", userEmail);
                jsonObject.put("search_item", searchItem);
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
                    if(outputStream != null) {
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

            ArrayList<RecipeNameId> array = ParseJSON.parseJSONArrayModel(data);
            onBackgroundTaskObtainedSearchResult(array);
        }
    }

    /**
     * asynchronous background task used to retrieve recipes
     */
    public class GetItemsTask extends AsyncTask<String, Void, String> {

        String userEmail, data;

        public GetItemsTask(String email){
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
                url = new URL("http://10.0.0.18:9999/mycookbookservlets/GetRecipeNames");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userEmail", userEmail);
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
                    if(outputStream != null) {
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

            Map<String, ArrayList<RecipeNameId>> allRecipesMap;
            Map<String, ArrayList<RecipeNameId>> sharedRecipesMap;
            Map<String, ArrayList<RecipeNameId>> ownRecipesMap;
            ArrayList<HashMap<String, ArrayList<RecipeNameId>>> mapsArray = new ArrayList<>();

            try{
                mapsArray = ParseJSON.parseJSONRecipeNameCategory(data);
                allRecipesMap = mapsArray.get(0);
                sharedRecipesMap = mapsArray.get(1);
                ownRecipesMap = mapsArray.get(2);

                // send retrieved data back to main UI thread so data can be displayed
                onBackgroundTaskObtainedRecipeNamesAndCategories(allRecipesMap, sharedRecipesMap, ownRecipesMap);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
