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
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class Cookbook extends AppCompatActivity {

    private ArrayList<String> arrayRecipeNames;
    private ArrayList<String> arrayUserCategories;
    private ArrayList<HeaderRecipeModel> arrayHeaderRecipeModels;
    private Map<String, ArrayList<RecipeNameId>> categoryRecipesMap;
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

        SearchView searchView = (SearchView) toolbar.findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // start async task to query recipes

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
    public void populateRecyclerView(){

        arrayHeaderRecipeModels = new ArrayList<>();
        String categoryName;

        for(int x = 0; x < arrayUserCategories.size(); x++){
            categoryName = arrayUserCategories.get(x);
            arrayHeaderRecipeModels.add(new HeaderRecipeModel(categoryName, categoryRecipesMap.get(categoryName)));
        }

        // TODO: sort categories

        SectionRecyclerViewAdapter recyclerViewAdapter = new SectionRecyclerViewAdapter(this, arrayHeaderRecipeModels, userEmail);
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
    public void onBackgroundTaskObtainedRecipeNamesAndCategories(Map<String, ArrayList<RecipeNameId>> map){

        categoryRecipesMap = map;
        arrayUserCategories = new ArrayList<>(categoryRecipesMap.keySet());
        populateRecyclerView();
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

            try{
                recipeNameCategoryMap = ParseJSON.parseJSONRecipeNameCategory(data);
                onBackgroundTaskObtainedRecipeNamesAndCategories(recipeNameCategoryMap);

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }




}//end class
