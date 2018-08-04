package com.cookbook.nanepothier.mycookbook;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Cookbook extends AppCompatActivity {

    private ArrayList<String> arrayRecipes;
    private String user = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookbook);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        arrayRecipes = new ArrayList<>();

        getRecipeNames();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager linearManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RecipeAdapter recipeAdapter = new RecipeAdapter(this, arrayRecipes);

        recyclerView.setLayoutManager(linearManager);
        recyclerView.setAdapter(recipeAdapter);

    }//onCreate

    public void getRecipeNames(){

        String message;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection connection;
        URL url = null;
        StringBuilder result2 = null;
        String result = "";

        try{
            url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/getRecipeNames.php");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user", user);
            message = jsonObject.toString();

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(message.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            connection.connect();

            outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(message.getBytes());
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

            parseJsonArray(result);


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

    }

    private void parseJsonArray(String jsonData){

        String stringResult = "";
        JSONObject object = new JSONObject();

        try{

            JSONArray json = new JSONArray(jsonData);

            for(int x = 0; x < json.length(); x++){

                //object = json.getJSONObject(x);
                arrayRecipes.add(json.getString(x));

            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }



}//end class
