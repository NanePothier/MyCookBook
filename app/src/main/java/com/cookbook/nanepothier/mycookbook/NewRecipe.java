package com.cookbook.nanepothier.mycookbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class NewRecipe extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAGN = "***********view";


    private EditText recipeNameView;
    private EditText primCategoryView;
    private EditText servingsView;
    private EditText prepTimeView;
    private EditText ovenTimeView;
    private EditText ovenTempView;
    private EditText caloriesView;
    private EditText instructView;


    //private SaveTask saveTask;

    private Spinner spinner;
    public static ArrayList<String> listIngredients;

    //private String user;


    //private InputStream inputStream = null;
    //private OutputStream outputStream = null;
    //HttpURLConnection connection;
    //URL url = null;
    //String result = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recipe);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newrecipe_toolbar);
        setSupportActionBar(toolbar);

        listIngredients = new ArrayList<String>();

        recipeNameView = (EditText) findViewById(R.id.recipe_name);
        primCategoryView = (EditText) findViewById(R.id.category);
        servingsView = (EditText) findViewById(R.id.servings);
        prepTimeView = (EditText) findViewById(R.id.prep_time);
        ovenTimeView = (EditText) findViewById(R.id.oven_time);
        ovenTempView = (EditText) findViewById(R.id.oven_temp);
        caloriesView = (EditText) findViewById(R.id.calories);
        instructView = (EditText) findViewById(R.id.editText);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        getIngredients();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_recipe, menu);

        return true;
    }


    // retrieve ingredients from database
    public void getIngredients(){

        GetIngredientsTask task = new GetIngredientsTask();
        task.execute((String) null);
    }

    private void onBackgroundTaskObtainedIngredients(ArrayList<String> ingredients){

        for(int z = 0; z < ingredients.size(); z++){

            System.out.println("Back in main activity: " + ingredients.get(z));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ingredients);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){

    }

    @Override
    public void onNothingSelected(AdapterView <?> parent){

    }


    //method invoked by appbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        boolean valid = false;

        switch(item.getItemId()){

            case R.id.save_action:

                /*
                valid = checkRequiredFields();

                if(valid) {

                    //saveTask = new SaveTask();
                    //saveTask.execute((String) null);

                    return true;
                }
                */

                return true;

            case R.id.cancel_action:
                //startActivity(new Intent(NewRecipe.this, MainActivity.class));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //check if required fields are filled out
    protected boolean checkRequiredFields() {

        if(recipeNameView.getText().length() > 0) {
            return true;
        }
        return false;
    }




    public class SaveTask extends AsyncTask<String, Void, String> {

        String recipeName;
        ArrayList<String> ingredients = new ArrayList<>();
        String primCategory;
        Integer prepTime, ovenTime, ovenTemp, servings, calories, numIngredients;
        String instructions;
        String taskIndicator;


        //constructor used for ingredient retrieval
        public SaveTask(){
        }

        //constructor for saving recipe
        public SaveTask(String rName, ArrayList<String> ing, String primCat, Integer pTime, Integer oTime, Integer oTemp, Integer servings, Integer calories, Integer numIng, String instruct){

            recipeName = rName;
            ingredients = ing;
            primCategory = primCat;
            prepTime = pTime;
            ovenTime = oTime;
            ovenTemp = oTemp;
            this.servings = servings;
            this.calories = calories;
            numIngredients = numIng;
            instructions = instruct;
        }

        @Override
        protected String doInBackground(String... params){

            String user;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            String result = "";


                try{
                    url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/save_recipe.php");

                    JSONObject jsonObject = new JSONObject();
                    //jsonObject.put("user", mEmail);
                    //jsonObject.put("password", mPassword);

                    user = jsonObject.toString();

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(user.getBytes().length);

                    connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                    connection.connect();

                    outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(user.getBytes());
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
                        outputStream.close();
                        inputStream.close();

                    }catch(IOException ie){
                        ie.printStackTrace();
                    }
                }

            return result;

        }


        @Override
        protected void onPostExecute(String data){

            String finalResult;
            //finalResult = parseJSON(data);


        }

        private String parseJSON(String jsonData){

            String stringResult = "";

            try{

                JSONObject json = new JSONObject(jsonData);
                stringResult = json.getString("successIndicator");

            }catch(Exception e){
                e.printStackTrace();
            }

            return stringResult;
        }
    }

    public class GetIngredientsTask extends AsyncTask<String, Void, String>{

        ArrayList<String> listIngredients = new ArrayList<String>();

        @Override
        protected String doInBackground(String... args){

            String message;
            String a = "hello";
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection;
            URL url = null;
            StringBuilder result2 = null;
            String result = "";

        try{

            url = new URL("http://weblab.salemstate.edu/~S0280202/android_connect/retrieve_ingredients.php");

            //JSONObject jsonObject = new JSONObject();

            //jsonObject.put("first", a);

            //message = jsonObject.toString();

            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //connection.setFixedLengthStreamingMode(message.getBytes().length);

            //connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            System.out.println("now maybe");

            connection.connect();

            //outputStream = new BufferedOutputStream(connection.getOutputStream());
            //outputStream.write(message.getBytes());
            //outputStream.flush();

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

            //Log.d(TAGN, result);

            //recipeNameView.setText(result);

            //'ingredient'=>$row['ingredient_name']

        }catch(Exception ioe){
            ioe.printStackTrace();
        }finally{

            try{
                //outputStream.close();
                //inputStream.close();

            }catch(Exception ie){
                ie.printStackTrace();
            }
        }

        //System.out.println(" returned from php: " + parseJSON(result));
        //listIngredients = parseJSONArray(result);

            return result;

        }

        @Override
        protected void onPostExecute(String data){

            try{

                listIngredients = ParseJSON.parseJSONArray(data);

                for(int x = 0; x < listIngredients.size(); x++ ){
                    System.out.println("Ing: " + listIngredients.get(x));
                }

                NewRecipe.this.onBackgroundTaskObtainedIngredients(listIngredients);

                //System.out.println("in string form: " + stringResult);
            }catch(Exception e){

            }

        }

        /*
        private ArrayList<String> parseJSONArray(String jsonData){

            String ingredientText;
            ArrayList<String> listIng = new ArrayList<String>();
            JSONObject jObject;

            System.out.println("trying to parse json array");

            try{

                JSONArray json = new JSONArray(jsonData);

                System.out.println("json array: " + json);

                for(int x = 0; x < json.length(); x++){

                    jObject = json.getJSONObject(x);
                    String s = jObject.getString("ingredient");
                    s = s.substring(2, s.length() - 2);

                    System.out.println("string of object: " + s);

                    listIng.add(s);

                }

            }catch(Exception e){
                e.printStackTrace();
            }

            return listIng;
        }
        */


    }


}
