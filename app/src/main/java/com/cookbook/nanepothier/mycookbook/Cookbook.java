package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Cookbook extends AppCompatActivity {

    private ArrayList<String> arrayRecipeNames;
    private ArrayList<String> arrayUserCategories;
    private ArrayList<HeaderRecipeModel> arrayHeaderRecipeModels;
    private Map<String, ArrayList<RecipeNameId>> categoryRecipesMap;
    private Map<String, ArrayList<RecipeNameId>> sharedRecipesMap;
    private Map<String, ArrayList<RecipeNameId>> ownRecipesMap;
    private String userEmail;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookbook);

        Toolbar toolbar = (Toolbar) findViewById(R.id.cookbook_toolbar);
        setSupportActionBar(toolbar);
        ImageButton backButton = (ImageButton) toolbar.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Cookbook.this, MainActivity.class);
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
                return false;
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // retrieve user email through intent
        // Intent intentReceived = getIntent();
        // userEmail = intentReceived.getStringExtra("userEmail");

        userEmail = "haleyiron@gmail.com";

        getRecipeNamesAndCategories();
        setUpRecyclerView();

    }// onCreate

    public void setUpRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    // create views using retrieved categories and recipe names
    public void populateCategoryRecyclerView(Map<String, ArrayList<RecipeNameId>> map){

        arrayHeaderRecipeModels = new ArrayList<>();
        String categoryName;

        for(int x = 0; x < arrayUserCategories.size(); x++){
            categoryName = arrayUserCategories.get(x);
            arrayHeaderRecipeModels.add(new HeaderRecipeModel(categoryName, map.get(categoryName)));
        }

        // TODO: sort categories

        SectionRecyclerViewAdapter recyclerViewAdapter = new SectionRecyclerViewAdapter(this, arrayHeaderRecipeModels, userEmail);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public void populateSearchRecyclerView(ArrayList<RecipeNameId> array){

        ItemRecyclerViewAdapter recyclerViewAdapter = new ItemRecyclerViewAdapter(this, array, userEmail);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

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

        categoryRecipesMap = map;
        sharedRecipesMap = sharedMap;
        ownRecipesMap = ownMap;
        arrayUserCategories = new ArrayList<>(categoryRecipesMap.keySet());
        populateCategoryRecyclerView(categoryRecipesMap);
    }

    public void onBackgroundTaskObtainedSearchResult(ArrayList<RecipeNameId> nameIdArray){

        // display recipes retrieved that fit search criteria
        populateSearchRecyclerView(nameIdArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_cookbook, menu);

        return true;
    }

    // method invoked by appbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_view_own:

                populateCategoryRecyclerView(ownRecipesMap);
                return true;

            case R.id.action_view_shared:

                populateCategoryRecyclerView(sharedRecipesMap);
                return true;

            case R.id.action_view_all:

                populateCategoryRecyclerView(categoryRecipesMap);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

            Map<String, ArrayList<RecipeNameId>> recipeNameCategoryMap;
            Map<String, ArrayList<RecipeNameId>> sharedRecipesMap;
            Map<String, ArrayList<RecipeNameId>> ownRecipesMap;
            ArrayList<HashMap<String, ArrayList<RecipeNameId>>> mapsArray = new ArrayList<>();

            try{
                mapsArray = ParseJSON.parseJSONRecipeNameCategory(data);
                recipeNameCategoryMap = mapsArray.get(0);
                sharedRecipesMap = mapsArray.get(1);
                ownRecipesMap = mapsArray.get(2);
                onBackgroundTaskObtainedRecipeNamesAndCategories(recipeNameCategoryMap, sharedRecipesMap, ownRecipesMap);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }




}//end class
