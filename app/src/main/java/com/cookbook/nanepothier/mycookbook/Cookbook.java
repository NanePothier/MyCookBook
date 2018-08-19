package com.cookbook.nanepothier.mycookbook;

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
import android.widget.ListView;
import org.json.JSONArray;
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
    private Map<String, ArrayList<String>> categoryRecipesMap;
    private String user = "";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookbook);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // retrieve user recipes and categories recipes belong to
        arrayRecipeNames = new ArrayList<>();
        arrayUserCategories = new ArrayList<>();
        getRecipeNames();
        getUserCategories();

        setUpRecyclerView();
        populateRecyclerView();





        // LinearLayoutManager linearManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // RecipeAdapter recipeAdapter = new RecipeAdapter(this, arrayRecipeNames);

    }//onCreate

    public void setUpRecyclerView(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void populateRecyclerView(){
        arrayHeaderRecipeModels = new ArrayList<>();

        for(int x = 0; x < arrayUserCategories.size(); x++){

            arrayHeaderRecipeModels.add(new HeaderRecipeModel(arrayUserCategories.get(x), categoryRecipesMap.get(x)));
        }

        SectionRecyclerViewAdapter recyclerViewAdapter = new SectionRecyclerViewAdapter(this, arrayHeaderRecipeModels);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    public void getRecipeNames(){

        GetItemsTask nameTask = new GetItemsTask("rec");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            nameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            nameTask.execute((String) null);
        }
    }

    public void getUserCategories(){

        GetItemsTask userCatTask = new GetItemsTask("cat");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            userCatTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (String) null);
        }else{
            userCatTask.execute((String) null);
        }
    }

    public void onBackgroundTaskObtainedRecipeNames(ArrayList<String> names){
        arrayRecipeNames = names;
    }

    public void onBackgroundTaskObtainedUserCategories(ArrayList<String> categories){
        arrayUserCategories = categories;
    }


    public class GetItemsTask extends AsyncTask<String, Void, String> {

        ArrayList<String> listItems = new ArrayList<String>();
        String itemIndicator;
        String fileURL;

        public GetItemsTask(String indicator){
            itemIndicator = indicator;
        }

        @Override
        protected String doInBackground(String... args){

            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";

            try{

                if(itemIndicator.equals("rec")){
                    url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/get_recipe_names.php");
                    System.out.println("setting url for ingredients");
                }else if(itemIndicator.equals("cat")){
                    url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/retrieve_categories.php");
                    System.out.println("setting url for categories task");
                }

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

                if(itemIndicator.equals("rec")){
                    listItems = ParseJSON.parseJSONArray(data, "ingredient");
                }else if(itemIndicator.equals("cat")){
                    listItems = ParseJSON.parseJSONArray(data, "category");
                }

                for(int x = 0; x < listItems.size(); x++ ){
                    System.out.println("Item: " + listItems.get(x));
                }

                if(itemIndicator.equals("rec")){
                    Cookbook.this.onBackgroundTaskObtainedRecipeNames(listItems);
                }else if(itemIndicator.equals("cat")){
                    System.out.println("Calling categoies function to pass data back");
                    Cookbook.this.onBackgroundTaskObtainedUserCategories(listItems);
                }

                //System.out.println("in string form: " + stringResult);
            }catch(Exception e) {

            }
        }
    }




}//end class
